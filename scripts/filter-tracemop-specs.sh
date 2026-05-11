#!/bin/bash

# Script keep only those that exist in lazymop
# Usage: ./filter_specs.sh <lazymop_props_dir> <target_props_dir>

set -e

if [ $# -ne 2 ]; then
    echo "Usage: $0 <lazymop_props_dir> <target_props_dir>"
    exit 1
fi

LAZYMOP_PROPS_DIR="$1"
TARGET_PROPS_DIR="$2"

echo "Filtering specs in $TARGET_PROPS_DIR to match $LAZYMOP_PROPS_DIR..."

LAZYMOP_SPECS=$(ls "$LAZYMOP_PROPS_DIR"/*.mop 2>/dev/null | xargs -n1 basename | sed 's/\.mop$//' || true)
TARGET_SPECS=$(ls "$TARGET_PROPS_DIR"/*.mop 2>/dev/null | xargs -n1 basename | sed 's/\.mop$//' || true)

# remove specs from target that don't exist in lazymop
for spec in $TARGET_SPECS; do
    if ! echo "$LAZYMOP_SPECS" | grep -q "^$spec$"; then
        echo "Removing: $spec.mop"
        rm -f "$TARGET_PROPS_DIR/$spec.mop"
    fi
done

# Count remaining specs
REMAINING_COUNT=$(ls "$TARGET_PROPS_DIR"/*.mop 2>/dev/null | wc -l || echo "0")

echo "$REMAINING_COUNT specs remaining in $TARGET_PROPS_DIR"