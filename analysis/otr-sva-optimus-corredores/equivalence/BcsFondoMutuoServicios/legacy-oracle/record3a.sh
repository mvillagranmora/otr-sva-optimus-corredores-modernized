#!/usr/bin/env bash
# Step 3A focused oracle: records cases3a/<case>/expected.json by executing the REAL legacy CalcularFechaSolicitudAporteRescate,
# CalcularMontoNumeroCuota, CalcularComisiones and ObtenerPerfilFondoMutuo (logica) on H2. Same compile/run machinery and knobs as record.sh
# (ORACLE_WORK, ORACLE_SKIP_COMPILE, LEGACY_OVERLAY, JAVA_OPTS, ...); only the main class and the extra source differ.
#   ./record3a.sh [caseNamePrefix]
#   ORACLE_OUT=<dir> ./record3a.sh      dumps <dir>/<case>.json instead of goldens (golden mutation check with LEGACY_OVERLAY)
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export ORACLE_MAIN=Oracle3ARunner
export ORACLE_EXTRA_SOURCES="$HERE/runner3a/Oracle3ARunner.java ${ORACLE_EXTRA_SOURCES:-}"
if [ -n "${ORACLE_OUT:-}" ]; then export JAVA_OPTS="-Doracle.out=$ORACLE_OUT ${JAVA_OPTS:-}"; fi
exec "$HERE/record.sh" "$@"
