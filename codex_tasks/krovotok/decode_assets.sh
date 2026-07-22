#!/usr/bin/env bash
set -euo pipefail

TASK_DIR="$(cd "$(dirname "$0")" && pwd)"
B64="$TASK_DIR/assets/Krovotok_complete_v2_with_particles.zip.b64"
ZIP="$TASK_DIR/Krovotok_complete_v2_with_particles.zip"
OUT="$TASK_DIR/unpacked"

if [[ ! -f "$B64" ]]; then
  echo "Asset archive not found: $B64" >&2
  exit 1
fi

rm -rf "$OUT"
mkdir -p "$OUT"
base64 --decode "$B64" > "$ZIP"
unzip -o "$ZIP" -d "$OUT"

echo "Krovotok assets unpacked to: $OUT"
