#!/usr/bin/env bash
# F3 W3 T2 (F-C aportes + F-D rescates: custody and costs): records the goldens of cases-cer-t2/ with the SAME oracle as T1 (record_cer.sh: the unmodified legacy
# CertificarSolicitudAporteRescate and its real collaborators on H2, registered doubles for the rest, 20 runs per case; a case that is not stable gets NO golden, exit 4).
# Only the directory of the cases and the stability report change. Same knobs as record_cer.sh (ORACLE_WORK, ORACLE_SKIP_COMPILE, ORACLE_REPEAT, JAVA_HOME ...).
#   ./record_cer_t2.sh [caseNamePrefix]        e.g. ./record_cer_t2.sh FMM-CER   (all)   or   ./record_cer_t2.sh FMM-CER-2
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
export ORACLE_CASES="${ORACLE_CASES:-$BASE/cases-cer-t2}"
export ORACLE_REPORT="${ORACLE_REPORT:-$BASE/determinism-cer-t2.txt}"
exec "$HERE/record_cer.sh" "${1:-FMM-CER}"
