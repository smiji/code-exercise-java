#!/bin/sh
set -eu

TEMPLATE_PATH="/usr/share/nginx/html/config.js.template"
OUTPUT_PATH="/usr/share/nginx/html/config.js"

API_PROTOCOL="${VITE_API_PROTOCOL:-http}"
API_HOST="${VITE_API_HOST:-localhost}"
API_PORT="${VITE_API_PORT:-8585}"
API_BASE_URL="${VITE_API_BASE_URL:-}"

escape_for_sed() {
  printf '%s' "$1" | sed 's/[&|]/\\&/g'
}

if [ -f "$TEMPLATE_PATH" ]; then
  sed \
    -e "s|__API_PROTOCOL__|$(escape_for_sed "$API_PROTOCOL")|g" \
    -e "s|__API_HOST__|$(escape_for_sed "$API_HOST")|g" \
    -e "s|__API_PORT__|$(escape_for_sed "$API_PORT")|g" \
    -e "s|__API_BASE_URL__|$(escape_for_sed "$API_BASE_URL")|g" \
    "$TEMPLATE_PATH" > "$OUTPUT_PATH"
fi
