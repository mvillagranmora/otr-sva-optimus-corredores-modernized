#!/bin/bash
# Re-records the LEGACY goldens (unmodified legacy classes compiled from legacy/, never written to) into
#   src/test/resources/crystal/equiv/<case>/legacy/<format>.<ext> (+ meta.json)
# usage: record.sh [case-name-substring ...]      (JAVA_HOME = JDK 11; libs/ with the proprietary jars; ~/.m2 offline)
# Takes ~ (cases x formats x 2 passes) renders plus >= 65 s between the passes of each case.
source "$(dirname "${BASH_SOURCE[0]}")/env.sh"
build_legacy
EQ="${EQUIV_DIR:-$MOD/src/test/resources/crystal/equiv}"   # EQUIV_DIR: scratch set of cases
mkdir -p "$OUT/run/tmp"
cd "$OUT/run"
"$JAVA" $JVMFLAGS -cp "$(w "$OUT/classes");$CP_LEGACY" RecordLegacy "$(w "$EQ")" "$(w "$MOD")" "$@"
