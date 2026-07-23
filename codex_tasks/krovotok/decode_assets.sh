#!/usr/bin/env bash
set -euo pipefail

TASK_DIR="$(cd "$(dirname "$0")" && pwd)"

python3 "$TASK_DIR/verify_asset_archive.py" --extract

echo "Krovotok assets repaired, verified and unpacked successfully."
