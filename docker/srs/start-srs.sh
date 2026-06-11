#!/bin/sh
set -eu

callback_url="${SRS_CALLBACK_BASE_URL:-http://app:8080/api/v1/integrations/srs/live-hooks}"
if [ -n "${APP_LIVE_CALLBACK_TOKEN:-}" ]; then
  separator='?'
  case "$callback_url" in
    *\?*) separator='&' ;;
  esac
  callback_url="${callback_url}${separator}token=${APP_LIVE_CALLBACK_TOKEN}"
fi

http_port="${SRS_HTTP_PORT:-8080}"

escaped_callback_url=$(printf '%s' "$callback_url" | sed 's/[|&\\]/\\&/g')

sed -e "s|__CALLBACK_URL__|${escaped_callback_url}|g" \
    -e "s|__HTTP_PORT__|${http_port}|g" \
  /usr/local/srs/conf/docker-live.template.conf \
  > /usr/local/srs/conf/docker-live.conf

exec ./objs/srs -c conf/docker-live.conf
