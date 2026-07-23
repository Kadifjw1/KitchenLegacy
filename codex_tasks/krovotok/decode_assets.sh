#!/usr/bin/env bash
set -euo pipefail

TASK_DIR="$(cd "$(dirname "$0")" && pwd)"
B64="$TASK_DIR/assets/Krovotok_complete_v2_with_particles.zip.b64"
ZIP="$TASK_DIR/Krovotok_complete_v2_with_particles.zip"
OUT="$TASK_DIR/unpacked"

if [[ ! -f "$B64" ]]; then
  cat "$TASK_DIR"/assets/archive.*.b64 > "$B64" 2>/dev/null || true
fi
if [[ ! -s "$B64" ]]; then
  echo "Asset archive not found: $B64" >&2
  exit 1
fi

rm -rf "$OUT"
mkdir -p "$OUT"
if base64 --decode "$B64" > "$ZIP" 2>/dev/null && unzip -o "$ZIP" -d "$OUT" >/dev/null; then
  echo "Krovotok assets unpacked to: $OUT"
else
  rm -f "$ZIP"
  mkdir -p "$OUT"
  echo "Base64 archive shards are present, but no unpackable archive is available in this checkout." > "$OUT/README.txt"
  echo "Krovotok asset archive could not be unpacked; generated resources are materialized by materialize_krovotok_resources.py"
fi
