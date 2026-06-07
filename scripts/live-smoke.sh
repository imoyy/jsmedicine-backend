#!/usr/bin/env sh
set -eu

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_command curl
require_command jq

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
ADMIN_USERNAME="${ADMIN_USERNAME:-td_admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-Admin@123456}"
APP_USERNAME="${APP_USERNAME:-td_user_01}"
APP_PASSWORD="${APP_PASSWORD:-User@123456}"
CALLBACK_TOKEN="${APP_LIVE_CALLBACK_TOKEN:-${CALLBACK_TOKEN:-}}"

timestamp="$(date '+%Y%m%d-%H%M%S')"
title="[SMOKE] Live ${timestamp}"
stream_name="smoke-${timestamp}"
start_at="$(date -d '+10 minutes' '+%Y-%m-%dT%H:%M:%S')"
end_at="$(date -d '+70 minutes' '+%Y-%m-%dT%H:%M:%S')"

admin_login_payload="$(jq -n \
  --arg username "$ADMIN_USERNAME" \
  --arg password "$ADMIN_PASSWORD" \
  '{username: $username, password: $password}')"

app_login_payload="$(jq -n \
  --arg username "$APP_USERNAME" \
  --arg password "$APP_PASSWORD" \
  '{username: $username, password: $password}')"

live_create_payload="$(jq -n \
  --arg title "$title" \
  --arg anchorName "Smoke Anchor" \
  --arg streamName "$stream_name" \
  --arg speakerName "Smoke Speaker" \
  --arg startAt "$start_at" \
  --arg endAt "$end_at" \
  '{
    title: $title,
    anchorName: $anchorName,
    streamName: $streamName,
    speakerName: $speakerName,
    tags: ["smoke", "live"],
    startAt: $startAt,
    endAt: $endAt,
    reviewStatus: 1,
    liveStatus: 0
  }')"

review_payload='{"reviewStatus":2,"comment":"smoke approved"}'

build_hook_payload() {
  action="$1"
  jq -n \
    --arg action "$action" \
    --arg app "live" \
    --arg stream "$resolved_stream_name" \
    --arg streamUrl "/live/${resolved_stream_name}" \
    --arg param "" \
    --arg vhost "__defaultVhost__" \
    --arg clientId "smoke-client" \
    --arg ip "127.0.0.1" \
    --arg tcUrl "rtmp://127.0.0.1/live" \
    --arg serverId "smoke-server" \
    '{
      action: $action,
      app: $app,
      stream: $stream,
      stream_url: $streamUrl,
      param: $param,
      vhost: $vhost,
      client_id: $clientId,
      ip: $ip,
      tcUrl: $tcUrl,
      server_id: $serverId
    }'
}

api_post() {
  url="$1"
  token="${2:-}"
  payload="${3:-}"

  if [ -n "$token" ]; then
    curl -fsS -X POST \
      -H "Authorization: Bearer ${token}" \
      -H "Content-Type: application/json" \
      -d "$payload" \
      "${BASE_URL}${url}"
  else
    curl -fsS -X POST \
      -H "Content-Type: application/json" \
      -d "$payload" \
      "${BASE_URL}${url}"
  fi
}

api_patch() {
  url="$1"
  token="${2:-}"
  payload="${3:-}"

  curl -fsS -X PATCH \
    -H "Authorization: Bearer ${token}" \
    -H "Content-Type: application/json" \
    -d "$payload" \
    "${BASE_URL}${url}"
}

api_get() {
  url="$1"
  token="${2:-}"

  if [ -n "$token" ]; then
    curl -fsS -H "Authorization: Bearer ${token}" "${BASE_URL}${url}"
  else
    curl -fsS "${BASE_URL}${url}"
  fi
}

api_hook() {
  payload="$1"
  hook_url="${BASE_URL}/api/v1/integrations/srs/live-hooks"
  if [ -n "$CALLBACK_TOKEN" ]; then
    hook_url="${hook_url}?token=${CALLBACK_TOKEN}"
  fi
  curl -fsS -X POST \
    -H "Content-Type: application/json" \
    -d "$payload" \
    "$hook_url"
}

admin_login_response="$(api_post "/api/v1/auth/login" "" "$admin_login_payload")"
admin_token="$(printf '%s' "$admin_login_response" | jq -r '.data.accessToken')"

if [ -z "$admin_token" ] || [ "$admin_token" = "null" ]; then
  echo "Admin login failed" >&2
  printf '%s\n' "$admin_login_response" >&2
  exit 1
fi

create_response="$(api_post "/api/v1/admin/live-sessions" "$admin_token" "$live_create_payload")"
live_id="$(printf '%s' "$create_response" | jq -r '.data.id')"

if [ -z "$live_id" ] || [ "$live_id" = "null" ]; then
  echo "Create live session failed" >&2
  printf '%s\n' "$create_response" >&2
  exit 1
fi

review_response="$(api_patch "/api/v1/admin/live-sessions/${live_id}/review" "$admin_token" "$review_payload")"
review_status="$(printf '%s' "$review_response" | jq -r '.data.reviewStatus')"

if [ "$review_status" != "2" ] && [ "$review_status" != "APPROVED" ]; then
  echo "Review live session failed" >&2
  printf '%s\n' "$review_response" >&2
  exit 1
fi

streaming_response="$(api_get "/api/v1/admin/live-sessions/${live_id}/streaming" "$admin_token")"
resolved_stream_name="$(printf '%s' "$streaming_response" | jq -r '.data.streamName')"
publish_url="$(printf '%s' "$streaming_response" | jq -r '.data.publishUrl')"
http_flv_url="$(printf '%s' "$streaming_response" | jq -r '.data.httpFlvUrl')"
hls_url="$(printf '%s' "$streaming_response" | jq -r '.data.hlsUrl')"

publish_hook_response="$(api_hook "$(build_hook_payload "on_publish")")"
publish_hook_code="$(printf '%s' "$publish_hook_response" | jq -r '.code')"
if [ "$publish_hook_code" != "0" ]; then
  echo "SRS publish hook failed" >&2
  printf '%s\n' "$publish_hook_response" >&2
  exit 1
fi

live_status_response="$(api_get "/api/v1/admin/live-sessions/${live_id}" "$admin_token")"
live_status="$(printf '%s' "$live_status_response" | jq -r '.data.liveStatus')"
if [ "$live_status" != "1" ] && [ "$live_status" != "LIVE" ]; then
  echo "Live status was not switched to LIVE" >&2
  printf '%s\n' "$live_status_response" >&2
  exit 1
fi

app_login_response="$(api_post "/api/v1/app/auth/login" "" "$app_login_payload")"
app_token="$(printf '%s' "$app_login_response" | jq -r '.data.accessToken')"

if [ -z "$app_token" ] || [ "$app_token" = "null" ]; then
  echo "App login failed" >&2
  printf '%s\n' "$app_login_response" >&2
  exit 1
fi

app_live_response="$(api_get "/api/v1/app/live-sessions/${live_id}" "$app_token")"

printf '%s\n' "$app_live_response" | jq -e '.data.streamName and .data.httpFlvUrl and .data.hlsUrl' >/dev/null

unpublish_hook_response="$(api_hook "$(build_hook_payload "on_unpublish")")"
unpublish_hook_code="$(printf '%s' "$unpublish_hook_response" | jq -r '.code')"
if [ "$unpublish_hook_code" != "0" ]; then
  echo "SRS unpublish hook failed" >&2
  printf '%s\n' "$unpublish_hook_response" >&2
  exit 1
fi

ended_status_response="$(api_get "/api/v1/admin/live-sessions/${live_id}" "$admin_token")"
ended_status="$(printf '%s' "$ended_status_response" | jq -r '.data.liveStatus')"
if [ "$ended_status" != "2" ] && [ "$ended_status" != "ENDED" ]; then
  echo "Live status was not switched to ENDED" >&2
  printf '%s\n' "$ended_status_response" >&2
  exit 1
fi

echo "Live smoke check passed."
echo "liveId=${live_id}"
echo "streamName=${resolved_stream_name}"
echo "publishUrl=${publish_url}"
echo "httpFlvUrl=${http_flv_url}"
echo "hlsUrl=${hls_url}"
echo "publishHookCode=${publish_hook_code}"
echo "unpublishHookCode=${unpublish_hook_code}"
echo
echo "OBS settings:"
echo "  Server: $(printf '%s' "$publish_url" | sed -E 's#(rtmp://[^/]+/[^/]+)/.*#\1#')"
echo "  Stream Key: ${resolved_stream_name}"
