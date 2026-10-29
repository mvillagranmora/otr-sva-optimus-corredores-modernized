#!/usr/bin/env bash
# F2 W1 front 2: oracle of cl.bcs.fondomutuo.CrearSolicitudRescateReinversion (JavaCall 1436) with the class REAL (record.sh keeps it as a registered double
# for the Manejador cases). Same runner, same doubles, same knobs as record.sh (ORACLE_WORK, ORACLE_SKIP_COMPILE, LEGACY_OVERLAY, ORACLE_SUBJECT, ORACLE_OUT,
# ORACLE_EXTRA_SOURCES, ORACLE_EXTRA_CP, JAVA_OPTS) with these differences, all in the temporary directory (legacy/ is only read):
#  - CrearSolicitudRescateReinversion is compiled from the UNMODIFIED legacy source (or its LEGACY_OVERLAY copy, mutation of the goldens) instead of a double;
#  - its seven dead imports of ProcesarReinversionRescate (RR:36-42) compile against shims-rre/ (empty nested types, never executed);
#  - -Doracle.rre=true: OracleRunner runs ONLY the cases with "rescateReinversionReal": true (FMM-RRE-*), and record.sh never runs them;
#  - the session stands in for the two AUTONOMOUS reinversion lock procedures (runner/cl/bcs/oracle/ReinversionLock.java, from their source);
#  - the cases load schema-rre.sql (persistent lock table, FMM.ReinversionSerieFondoDestino, the views/tables the RR SQL read that the F1 schema lacks,
#    the two sequences) through "esquemasExtra" of case.json.
#   ./record_rre.sh [caseNamePrefix]
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
# the same doubles as record.sh EXCEPT CrearSolicitudRescateReinversion, which is real here
DOUBLE_SRC=( "$TES/CrearIngresoEgreso.java" "$MES/CrearOperacionSpot.java" "$MES/ConsultarPuntasPorTramo.java" "$CUS/ConsultarFlujoCustodia.java"
  "$PLP/mer/CalcularParidad.java" "$PLP/tas/EntregarPreciosTasas.java" "$PLP/pla/SaldoFlujoCaja.java" "$PLP/pla/AgregarLog.java"
  "$PLP/bat/InscribirProcesoBatch.java" "$PLP/com/ConsultarProductosPortafolio.java" "$PLP/pla/NotificarEstadoOrdenesOWP.java"
  "$PLP/ins/ConsultarDatosInstrumentos.java" "$PLP/pla/utils/ConsultarEmpresa.java"
  "$FMP/CrearSolicitudAporteRescateProgramado.java" "$FMP/CrearSolicitudAporteOvernight.java" "$FMP/CrearSolicitudAporteReinversion.java"
  "$FMP/CrearSolicitudCambioSerie.java" "$FMP/CrearSolicitudRescateTransferencia.java" "$FMP/CrearSolicitudRescateTraspaso.java" )
DS=(); for f in "${DOUBLE_SRC[@]}"; do DS+=("$(mx "$f")"); done
$PY -I "$HERE/tools/gen_doubles.py" "$(mx "$WORK/gen")" "${DS[@]}"
mkdir -p "$WORK/gen/cl/bcs/plataforma/pla/utils"
sed 's/System.currentTimeMillis()/cl.bcs.oracle.Clock.millis()/g' "$(pick "$PLP/pla/utils/FechaDB.java")" > "$WORK/gen/cl/bcs/plataforma/pla/utils/FechaDB.java"

SRC=()
for c in ManejadorSolicitudAporteRescate CrearSolicitudAporteRescateNormal CrearSolicitudRescateReinversion CalcularFechaSolicitudAporteRescate CrearSolicitudRescateComisionNN \
         CalcularComisiones CalcularMontoNumeroCuota ServicioCumpleRequisitosCrearSolicitudAporteRescate ConsultarFlujoCustodiaFondoMutuo CrearSolicitudAGF ValidaMontoInversion \
         ValidaPatrimonioEmisor ObtenerPerfilFondoMutuo ProcesarIngresoEgresoTesoreria ProcesarIngresoOperacionSpot; do SRC+=("$(pick "$FMP/$c.java")"); done
for f in "$FMP"/pojo/*.java "$FMP"/utils/*.java; do SRC+=("$(pick "$f")"); done
for c in SQL NamedParameterStatement NamedCallableStatement JavaCall GsonTimestampDeserializer GsonDateDeserializer Fecha BcsPojo; do SRC+=("$(pick "$UT/$c.java")"); done
for c in pla/utils/ConfiguracionSistema pla/utils/Diccionario pla/utils/PlataformaUtils pla/utils/FileLoader pla/utils/BarraProgreso pla/utils/MonedaFormato pla/RevisarRequisitosBasicos \
         pla/ObtenerDiaSiguiente pla/ObtenerDiaAnterior pla/ObtenerDiaHabilMultiplesPaises; do SRC+=("$(pick "$PLP/$c.java")"); done
SRC+=( "$TES/pojo/IngresoEgreso.java" "$TES/pojo/IngresoEgresoFormaDePago.java" )
: > "$WORK/sources.txt"
for f in "${SRC[@]}"; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$WORK/gen" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$HERE/shims" "$HERE/shims-rre" "$HERE/runner" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
for f in ${ORACLE_EXTRA_SOURCES:-}; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done

if [ -z "${ORACLE_SKIP_COMPILE:-}" ]; then
echo "== compiling oracle into $WORK/classes (release 11)"
rm -rf "$WORK/classes"; mkdir -p "$WORK/classes"
"$JH/bin/javac" --release 11 -encoding UTF-8 -proc:none -nowarn -Xlint:none -Xmaxerrs 200 -d "$(mx "$WORK/classes")" -cp "$CP" @"$(mx "$WORK/sources.txt")"
fi

echo "== running"
cd "$WORK/run"
OPTS=( "-Duser.timezone=UTC" "-Dfile.encoding=UTF-8" "-Duser.language=en" "-Dlog4j2.StatusLogger.level=OFF" "-Dlog4j2.statusLoggerLevel=OFF" "-Doracle.rre=true" )
[ -n "${ORACLE_OUT:-}" ] && OPTS+=( "-Doracle.out=$(mx "$ORACLE_OUT")" )
[ -n "${ORACLE_SUBJECT:-}" ] && OPTS+=( "-Doracle.subject=$ORACLE_SUBJECT" )
[ -n "${ORACLE_MUTANT:-}" ] && OPTS+=( "-Doracle.mutant=$ORACLE_MUTANT" )
[ -n "${ORACLE_CASES:-}" ] && OPTS+=( "-Doracle.cases=$(mx "$ORACLE_CASES")" )
RUNCP="$(mx "$WORK/classes")$SEP$CP$SEP$(mx "$FM")$SEP$(mx "$PLS")"
[ -n "${ORACLE_EXTRA_CP:-}" ] && RUNCP="$RUNCP$SEP$ORACLE_EXTRA_CP"
"$JH/bin/java" "${OPTS[@]}" ${JAVA_OPTS:-} -cp "$RUNCP" "${ORACLE_MAIN:-OracleRunner}" "$(mx "$BASE")" "${1:-}"
