#!/bin/bash
# usage: probe.sh <out.json> <rpt> [rpt...]   (paths relative to BcsCrystalReports/ or absolute)
source "$(dirname "${BASH_SOURCE[0]}")/env.sh"
build_legacy
OUTJ="$1"; shift
cd "$MOD"
"$JAVA" $JVMFLAGS -cp "$(w "$OUT/classes");$CP_LEGACY" ProbeTemplates "$(w "$OUTJ")" "$@"
