#!/usr/bin/env sh
set -eu

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_command docker
require_command curl
require_command ffmpeg

SRS_IMAGE="${SRS_IMAGE:-ossrs/srs:5}"
SRS_CONTAINER_NAME="${SRS_CONTAINER_NAME:-jsmedicine-srs-smoke}"
SRS_RTMP_PORT="${SRS_RTMP_PORT:-1935}"
SRS_API_PORT="${SRS_API_PORT:-1985}"
SRS_HTTP_PORT="${SRS_HTTP_PORT:-8080}"
STREAM_NAME="${STREAM_NAME:-smoke-media-test}"
WORK_DIR="$(mktemp -d /tmp/jsmedicine-live-media-smoke.XXXXXX)"
SRS_CONFIG_FILE="${WORK_DIR}/srs-smoke.conf"
FFMPEG_LOG_FILE="${WORK_DIR}/ffmpeg.log"
FLV_SAMPLE_FILE="${WORK_DIR}/${STREAM_NAME}.flv"

cleanup() {
  docker rm -f "${SRS_CONTAINER_NAME}" >/dev/null 2>&1 || true
  rm -rf "${WORK_DIR}"
}

trap cleanup EXIT INT TERM

cat > "${SRS_CONFIG_FILE}" <<'EOF'
listen              1935;
max_connections     1000;
daemon              off;
srs_log_tank        console;

http_api {
    enabled         on;
    listen          1985;
}

http_server {
    enabled         on;
    listen          8080;
    dir             ./objs/nginx/html;
}

vhost __defaultVhost__ {
    http_remux {
        enabled         on;
        mount           [vhost]/[app]/[stream].flv;
    }

    hls {
        enabled         on;
        hls_path        ./objs/nginx/html;
        hls_mount       [vhost]/[app]/[stream].m3u8;
        hls_fragment    2;
        hls_window      10;
    }
}
EOF

docker rm -f "${SRS_CONTAINER_NAME}" >/dev/null 2>&1 || true
docker run -d \
  --name "${SRS_CONTAINER_NAME}" \
  -p "${SRS_RTMP_PORT}:1935" \
  -p "${SRS_API_PORT}:1985" \
  -p "${SRS_HTTP_PORT}:8080" \
  -v "${SRS_CONFIG_FILE}:/usr/local/srs/conf/smoke.conf:ro" \
  "${SRS_IMAGE}" \
  ./objs/srs -c conf/smoke.conf >/dev/null

for _ in 1 2 3 4 5 6 7 8 9 10; do
  if curl -fsS "http://127.0.0.1:${SRS_API_PORT}/api/v1/versions" >/dev/null; then
    break
  fi
  sleep 1
done

curl -fsS "http://127.0.0.1:${SRS_API_PORT}/api/v1/versions" >/dev/null

ffmpeg -re \
  -f lavfi -i testsrc=size=640x360:rate=25 \
  -f lavfi -i sine=frequency=1000:sample_rate=44100 \
  -t 5 \
  -c:v libx264 -preset veryfast -pix_fmt yuv420p \
  -c:a aac \
  -f flv "rtmp://127.0.0.1:${SRS_RTMP_PORT}/live/${STREAM_NAME}" \
  >"${FFMPEG_LOG_FILE}" 2>&1 &
ffmpeg_pid="$!"

sleep 2
curl -sS --max-time 3 "http://127.0.0.1:${SRS_HTTP_PORT}/live/${STREAM_NAME}.flv" -o "${FLV_SAMPLE_FILE}" || true
hls_playlist="$(curl -fsS "http://127.0.0.1:${SRS_HTTP_PORT}/live/${STREAM_NAME}.m3u8")"
streams_response="$(curl -fsS "http://127.0.0.1:${SRS_API_PORT}/api/v1/streams/")"

wait "${ffmpeg_pid}"

if ! printf '%s' "${hls_playlist}" | grep -q '#EXTM3U'; then
  echo "HLS playlist was not generated" >&2
  exit 1
fi

if ! printf '%s' "${streams_response}" | grep -q "\"url\":\"/live/${STREAM_NAME}\""; then
  echo "SRS stream list does not include ${STREAM_NAME}" >&2
  exit 1
fi

if ! docker logs "${SRS_CONTAINER_NAME}" 2>&1 | grep -q "FLV /live/${STREAM_NAME}.flv"; then
  echo "SRS logs do not show HTTP-FLV consumer for ${STREAM_NAME}" >&2
  exit 1
fi

echo "Live media smoke check passed."
echo "srsApi=http://127.0.0.1:${SRS_API_PORT}/api/v1/versions"
echo "publishUrl=rtmp://127.0.0.1:${SRS_RTMP_PORT}/live/${STREAM_NAME}"
echo "httpFlvUrl=http://127.0.0.1:${SRS_HTTP_PORT}/live/${STREAM_NAME}.flv"
echo "hlsUrl=http://127.0.0.1:${SRS_HTTP_PORT}/live/${STREAM_NAME}.m3u8"
echo "flvSampleBytes=$(wc -c < "${FLV_SAMPLE_FILE}" | tr -d ' ')"
