#!/usr/bin/env bash
# F3 W3 T4-A (pruebas): oracle of cl.bcs.fondomutuo.CrearSolicitudAporteReinversion (AR, 1.496 lines, the class that Certificar calls from crearAporteReinversion), the class
# REAL and UNMODIFIED (or its LEGACY_OVERLAY copy = mutation of the goldens). Same runner (OracleRunner, run with -Doracle.cases), same session emulation (SessionConnection),
# same doubles runtime and knobs as record.sh / record_rre.sh / record_cer.sh (ORACLE_WORK, ORACLE_SKIP_COMPILE, LEGACY_OVERLAY, ORACLE_SUBJECT, ORACLE_OUT, ORACLE_EXTRA_SOURCES,
# ORACLE_EXTRA_CP, ORACLE_CASES, ORACLE_REPEAT, ORACLE_REPORT, JAVA_OPTS). Everything is compiled into a TEMPORARY directory (legacy/ is only read; nothing is copied into
# legacy/ or modernized/); H2 in memory only: no HANA, no network, no credential, no .env. The legacy main() (a user id and a real-looking portfolio) is compiled and NEVER called.
#  - REAL (unmodified): CrearSolicitudAporteReinversion and the closure of the sibling oracles that is already ported in the module: CalcularFechaSolicitudAporteRescate,
#    CalcularMontoNumeroCuota, ServicioCumpleRequisitosCrearSolicitudAporteRescate, ValidaMontoInversion, ValidaPatrimonioEmisor, ConsultarFlujoCustodiaFondoMutuo,
#    CrearSolicitudAGF, ObtenerPerfilFondoMutuo, pojo/*, utils/*, BcsUtils SQL/Fecha/..., Plataforma ConfiguracionSistema/Diccionario/MonedaFormato/RevisarRequisitosBasicos/ObtenerDia*;
#  - REGISTERED DOUBLES generated from the real sources by tools/gen_doubles.py (the shared tool, not edited): what is NOT ported or is a port behind a gate: Plataforma
#    CalcularParidad / EntregarPreciosTasas / SaldoFlujoCaja / AgregarLog / ConsultarEmpresa ..., Custodia ConsultarFlujoCustodia, Tesoreria CrearIngresoEgreso, Mesa Spot, AND the two
#    FondoMutuo helpers ProcesarIngresoEgresoTesoreria and ProcesarIngresoOperacionSpot (as record_cer.sh does: the golden records the WHOLE argument the subject builds, the
#    ported helpers have their own oracles). A double answers from seed/doubles-base.json + the case "colaboradores"; "error" makes it throw;
#  - the class imports Certificar's nested SolicitudesProcesoCertificacion (C-W0A: unused): compile-only shim in shims-csr/ (empty type, never executed);
#  - the cases live in cases-csr/ (NOT cases/); schema-csr.sql + seed/base-csr.sql are loaded through "esquemasExtra" of case.json;
#  - -Doracle.repeat=N (default 20, ORACLE_REPEAT): every case runs N times in fresh sessions and the dumps are compared; an unstable case is listed in $ORACLE_REPORT (default
#    determinism-csr.txt) and gets NO golden (exit 4).
#   ./record_csr.sh [caseNamePrefix]
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
WS="$(cd "$BASE/../../../.." && pwd)"
LEG="$WS/legacy/otr-sva-optimus-corredores/BSCL"
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
OVERLAY="${LEGACY_OVERLAY:-}"
PY="${PYTHON_CMD:-py -3}"

SEP=":"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";";; esac
mx() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi; }
CP=""
for j in "com/h2database/h2/2.4.240/h2-2.4.240.jar" "com/google/code/gson/gson/2.13.2/gson-2.13.2.jar" \
         "commons-io/commons-io/2.11.0/commons-io-2.11.0.jar" "commons-beanutils/commons-beanutils/1.9.4/commons-beanutils-1.9.4.jar" \
         "commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar" "commons-logging/commons-logging/1.2/commons-logging-1.2.jar" \
         "commons-lang/commons-lang/2.6/commons-lang-2.6.jar" "commons-configuration/commons-configuration/1.2/commons-configuration-1.2.jar" \
         "org/apache/logging/log4j/log4j-api/2.24.3/log4j-api-2.24.3.jar"; do
  [ -f "$M2/$j" ] || { echo "missing jar $M2/$j" >&2; exit 3; }
  CP="${CP:+$CP$SEP}$(mx "$M2/$j")"
done
pick() {
  local f="$1"
  if [ -n "$OVERLAY" ] && [ -f "$OVERLAY/$(basename "$f")" ]; then echo "$OVERLAY/$(basename "$f")"; else echo "$f"; fi
}

FM="$LEG/FondoMutuo/JAVA/BcsFondoMutuoServicios/src"; FMP="$FM/cl/bcs/fondomutuo"
PLS="$LEG/Plataforma/JAVA/BcsPlataformaServicios/src"; PLP="$PLS/cl/bcs/plataforma"
UT="$LEG/Plataforma/JAVA/BcsUtils/src/cl/bcs/utils"
TES="$LEG/Tesoreria/JAVA/BcsTesoreriaServicios/src/cl/bcs/tesoreria"
MES="$LEG/Mesa/JAVA/BcsMesaServicios/src/cl/bcs/mesa/SPT"
CUS="$LEG/Custodia/JAVA/BcsCustodiaServicios/src/cl/bcs/custodia"

echo "== generating shim Utils and doubles into $WORK/gen"
rm -rf "$WORK/gen"; mkdir -p "$WORK/gen" "$WORK/classes" "$WORK/run"
$PY -I "$HERE/tools/gen_utils.py" "$(mx "$UT/Utils.java")" "$(mx "$WORK/gen")"
DOUBLE_SRC=( "$TES/CrearIngresoEgreso.java" "$MES/CrearOperacionSpot.java" "$MES/ConsultarPuntasPorTramo.java" "$CUS/ConsultarFlujoCustodia.java"
  "$PLP/mer/CalcularParidad.java" "$PLP/tas/EntregarPreciosTasas.java" "$PLP/pla/SaldoFlujoCaja.java" "$PLP/pla/AgregarLog.java"
  "$PLP/bat/InscribirProcesoBatch.java" "$PLP/com/ConsultarProductosPortafolio.java" "$PLP/pla/NotificarEstadoOrdenesOWP.java"
  "$PLP/ins/ConsultarDatosInstrumentos.java" "$PLP/pla/utils/ConsultarEmpresa.java"
  "$FMP/ProcesarIngresoEgresoTesoreria.java" "$FMP/ProcesarIngresoOperacionSpot.java" )
DS=(); for f in "${DOUBLE_SRC[@]}"; do DS+=("$(mx "$f")"); done
$PY -I "$HERE/tools/gen_doubles.py" "$(mx "$WORK/gen")" "${DS[@]}"
mkdir -p "$WORK/gen/cl/bcs/plataforma/pla/utils"
sed 's/System.currentTimeMillis()/cl.bcs.oracle.Clock.millis()/g' "$(pick "$PLP/pla/utils/FechaDB.java")" > "$WORK/gen/cl/bcs/plataforma/pla/utils/FechaDB.java"

SRC=()
for c in CrearSolicitudAporteReinversion CalcularFechaSolicitudAporteRescate CalcularComisiones CalcularMontoNumeroCuota ServicioCumpleRequisitosCrearSolicitudAporteRescate          ConsultarFlujoCustodiaFondoMutuo CrearSolicitudAGF ValidaMontoInversion ValidaPatrimonioEmisor ObtenerPerfilFondoMutuo; do SRC+=("$(pick "$FMP/$c.java")"); done
for f in "$FMP"/pojo/*.java "$FMP"/utils/*.java; do SRC+=("$(pick "$f")"); done
for c in SQL NamedParameterStatement NamedCallableStatement JavaCall GsonTimestampDeserializer GsonDateDeserializer Fecha BcsPojo; do SRC+=("$(pick "$UT/$c.java")"); done
for c in pla/utils/ConfiguracionSistema pla/utils/Diccionario pla/utils/PlataformaUtils pla/utils/FileLoader pla/utils/BarraProgreso pla/utils/MonedaFormato pla/RevisarRequisitosBasicos \
         pla/ObtenerDiaSiguiente pla/ObtenerDiaAnterior pla/ObtenerDiaHabilMultiplesPaises; do SRC+=("$(pick "$PLP/$c.java")"); done
SRC+=( "$TES/pojo/IngresoEgreso.java" "$TES/pojo/IngresoEgresoFormaDePago.java" )
: > "$WORK/sources.txt"
for f in "${SRC[@]}"; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$WORK/gen" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$HERE/shims" "$HERE/shims-csr" "$HERE/runner" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
for f in ${ORACLE_EXTRA_SOURCES:-}; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done

if [ -z "${ORACLE_SKIP_COMPILE:-}" ]; then
echo "== compiling oracle into $WORK/classes (release 11)"
rm -rf "$WORK/classes"; mkdir -p "$WORK/classes"
"$JH/bin/javac" --release 11 -encoding UTF-8 -proc:none -nowarn -Xlint:none -Xmaxerrs 200 -d "$(mx "$WORK/classes")" -cp "$CP" @"$(mx "$WORK/sources.txt")"
fi

echo "== running"
cd "$WORK/run"
OPTS=( "-Duser.timezone=UTC" "-Dfile.encoding=UTF-8" "-Duser.language=en" "-Dlog4j2.StatusLogger.level=OFF" "-Dlog4j2.statusLoggerLevel=OFF"  )
[ -n "${ORACLE_OUT:-}" ] && OPTS+=( "-Doracle.out=$(mx "$ORACLE_OUT")" )
[ -n "${ORACLE_SUBJECT:-}" ] && OPTS+=( "-Doracle.subject=$ORACLE_SUBJECT" )
[ -n "${ORACLE_MUTANT:-}" ] && OPTS+=( "-Doracle.mutant=$ORACLE_MUTANT" )
OPTS+=( "-Doracle.cases=$(mx "${ORACLE_CASES:-$BASE/cases-csr}")" "-Doracle.report=$(mx "${ORACLE_REPORT:-$BASE/determinism-csr.txt}")" "-Doracle.repeat=${ORACLE_REPEAT:-20}" )
RUNCP="$(mx "$WORK/classes")$SEP$CP$SEP$(mx "$FM")$SEP$(mx "$PLS")"
[ -n "${ORACLE_EXTRA_CP:-}" ] && RUNCP="$RUNCP$SEP$ORACLE_EXTRA_CP"
"$JH/bin/java" "${OPTS[@]}" ${JAVA_OPTS:-} -cp "$RUNCP" "${ORACLE_MAIN:-OracleRunner}" "$(mx "$BASE")" "${1:-}"
