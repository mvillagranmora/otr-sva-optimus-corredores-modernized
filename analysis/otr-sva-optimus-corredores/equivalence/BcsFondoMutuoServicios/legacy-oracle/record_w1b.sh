#!/usr/bin/env bash
# F2 W1, layer 2 of PLAN_FONDOMUTUO_F2 7: the FondoMutuo Normal oracle with Tesoreria's legacy CrearIngresoEgreso REAL (not a double).
# Same as record.sh (same runner, same doubles, same knobs: ORACLE_WORK, ORACLE_SKIP_COMPILE, ORACLE_SUBJECT, ORACLE_OUT, ORACLE_EXTRA_CP, JAVA_OPTS)
# with these differences, all in the temporary directory (legacy/ is only read):
#  - cl.bcs.tesoreria.CrearIngresoEgreso is compiled from the UNMODIFIED legacy source, with the legacy classes it instantiates (CrearComprobante,
#    CrearComprobanteJava, LiquidarComprobante, pojo/*) and the Ordenes pojos it imports;
#  - the collaborators of that closure that are NOT in it use the shims of Tesoreria's own family-A oracle (BcsTesoreriaServicios
#    src/test/resources/tesoreria-a/legacy-oracle/shims): SocioNegocioUtils (verbatim members + the real SQL resource), and, never reached by the
#    W1B cases (liquidation / Java voucher / authorizations), CrearCargoAbono, ConsultarFlujoCaja, CrearEventoAutorizacionMasivo,
#    ActualizarSaldosCuentaBanco (+ their Hooks). Plataforma RevisarRequisitosBasicos stays REAL and AgregarLog / SaldoFlujoCaja stay registered doubles,
#    exactly as for Normal;
#  - Tesoreria's H2 dialect bridge (support/H2Hana.java of BcsTesoreriaServicios, read in place) is compiled in and -Doracle.tesoreria=true makes the
#    oracle session apply it to the SQL issued from cl.bcs.tesoreria.* classes (SessionConnection), and stand in for the HANA procedure CrearComprobante.
#  - the cases (FMM-NOR-Tnn, prefix FMM-NOR-T so the layer-1 goldens FMM-NOR-87..96 are never re-recorded here) load schema-tesoreria-fm.sql / seed/base-tesoreria-fm.sql through "esquemasExtra" of case.json.
#   ./record_w1b.sh [caseNamePrefix]
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
WS="$(cd "$BASE/../../../.." && pwd)"
LEG="$WS/legacy/otr-sva-optimus-corredores/BSCL"
TESMOD="$WS/modernized/otr-sva-optimus-corredores/BcsTesoreriaServicios/src/test"
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
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
FM="$LEG/FondoMutuo/JAVA/BcsFondoMutuoServicios/src"; FMP="$FM/cl/bcs/fondomutuo"
PLS="$LEG/Plataforma/JAVA/BcsPlataformaServicios/src"; PLP="$PLS/cl/bcs/plataforma"
UT="$LEG/Plataforma/JAVA/BcsUtils/src/cl/bcs/utils"
TSRC="$LEG/Tesoreria/JAVA/BcsTesoreriaServicios/src"; TES="$TSRC/cl/bcs/tesoreria"
ORD="$LEG/Orden/JAVA/BcsOrdenServicios/src"
SOC="$LEG/SocioNegocio/JAVA/BcsSocioNegocioServicios/src"
MES="$LEG/Mesa/JAVA/BcsMesaServicios/src/cl/bcs/mesa/SPT"
CUS="$LEG/Custodia/JAVA/BcsCustodiaServicios/src/cl/bcs/custodia"
TSH="$TESMOD/resources/tesoreria-a/legacy-oracle/shims"

if [ -z "${ORACLE_SKIP_COMPILE:-}" ]; then
echo "== generating shim Utils and doubles into $WORK/gen"
rm -rf "$WORK/gen"; mkdir -p "$WORK/gen" "$WORK/classes" "$WORK/run"
GEN_UTILS_EXTRA="${GEN_UTILS_EXTRA:-}" $PY -I "$HERE/tools/gen_utils.py" "$(mx "$UT/Utils.java")" "$(mx "$WORK/gen")"
DOUBLE_SRC=( "$MES/CrearOperacionSpot.java" "$MES/ConsultarPuntasPorTramo.java" "$CUS/ConsultarFlujoCustodia.java"
  "$PLP/mer/CalcularParidad.java" "$PLP/tas/EntregarPreciosTasas.java" "$PLP/pla/SaldoFlujoCaja.java" "$PLP/pla/AgregarLog.java"
  "$PLP/bat/InscribirProcesoBatch.java" "$PLP/com/ConsultarProductosPortafolio.java" "$PLP/pla/NotificarEstadoOrdenesOWP.java"
  "$PLP/ins/ConsultarDatosInstrumentos.java" "$PLP/pla/utils/ConsultarEmpresa.java"
  "$FMP/CrearSolicitudAporteRescateProgramado.java" "$FMP/CrearSolicitudAporteOvernight.java" "$FMP/CrearSolicitudAporteReinversion.java"
  "$FMP/CrearSolicitudCambioSerie.java" "$FMP/CrearSolicitudRescateReinversion.java" "$FMP/CrearSolicitudRescateTransferencia.java"
  "$FMP/CrearSolicitudRescateTraspaso.java" )
DS=(); for f in "${DOUBLE_SRC[@]}"; do DS+=("$(mx "$f")"); done
$PY -I "$HERE/tools/gen_doubles.py" "$(mx "$WORK/gen")" "${DS[@]}"
mkdir -p "$WORK/gen/cl/bcs/plataforma/pla/utils"
sed 's/System.currentTimeMillis()/cl.bcs.oracle.Clock.millis()/g' "$PLP/pla/utils/FechaDB.java" > "$WORK/gen/cl/bcs/plataforma/pla/utils/FechaDB.java"

SRC=()
for c in ManejadorSolicitudAporteRescate CrearSolicitudAporteRescateNormal CalcularFechaSolicitudAporteRescate CrearSolicitudRescateComisionNN CalcularComisiones \
         CalcularMontoNumeroCuota ServicioCumpleRequisitosCrearSolicitudAporteRescate ConsultarFlujoCustodiaFondoMutuo CrearSolicitudAGF ValidaMontoInversion \
         ValidaPatrimonioEmisor ObtenerPerfilFondoMutuo ProcesarIngresoEgresoTesoreria ProcesarIngresoOperacionSpot; do SRC+=("$FMP/$c.java"); done
for f in "$FMP"/pojo/*.java "$FMP"/utils/*.java; do SRC+=("$f"); done
for c in SQL NamedParameterStatement NamedCallableStatement JavaCall GsonTimestampDeserializer GsonDateDeserializer Fecha BcsPojo StringUtils; do SRC+=("$UT/$c.java"); done
for c in pla/utils/ConfiguracionSistema pla/utils/Diccionario pla/utils/PlataformaUtils pla/utils/FileLoader pla/utils/BarraProgreso pla/utils/MonedaFormato pla/RevisarRequisitosBasicos \
         pla/ObtenerDiaSiguiente pla/ObtenerDiaAnterior pla/ObtenerDiaHabilMultiplesPaises; do SRC+=("$PLP/$c.java"); done
# layer 2: Tesoreria's legacy closure of CrearIngresoEgreso, REAL
for c in CrearIngresoEgreso CrearComprobante CrearComprobanteJava LiquidarComprobante; do SRC+=("$TES/$c.java"); done
for p in IngresoEgreso IngresoEgresoFormaDePago ConstantesTesoreria; do SRC+=("$TES/pojo/$p.java"); done
SRC+=( "$ORD/cl/bcs/ordenes/pojo/ConstantesOrdenes.java" "$ORD/cl/bcs/ordenes/pojo/IndicadoresPortafolio.java" )
for s in socionegocio/soc/SocioNegocioUtils cuentaInversion/CrearCargoAbono plataforma/pla/ConsultarFlujoCaja plataforma/aut/CrearEventoAutorizacionMasivo \
         tesoreria/ban/ActualizarSaldosCuentaBanco oracle/Hooks; do SRC+=("$TSH/cl/bcs/$s.java"); done
SRC+=( "$TESMOD/java/cl/bcs/tesoreria/familia_a/support/H2Hana.java" )
: > "$WORK/sources.txt"
for f in "${SRC[@]}"; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$WORK/gen" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$HERE/shims" "$HERE/runner" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
echo "== compiling oracle (layer 2) into $WORK/classes"
rm -rf "$WORK/classes"; mkdir -p "$WORK/classes"
"$JH/bin/javac" --release 21 -encoding UTF-8 -proc:none -nowarn -Xlint:none -Xmaxerrs 300 -d "$(mx "$WORK/classes")" -cp "$CP" @"$(mx "$WORK/sources.txt")"
fi

echo "== running"
mkdir -p "$WORK/run"; cd "$WORK/run"
OPTS=( "-Duser.timezone=UTC" "-Dfile.encoding=UTF-8" "-Duser.language=en" "-Dlog4j2.StatusLogger.level=OFF" "-Doracle.tesoreria=true" )
[ -n "${ORACLE_OUT:-}" ] && OPTS+=( "-Doracle.out=$(mx "$ORACLE_OUT")" )
[ -n "${ORACLE_SUBJECT:-}" ] && OPTS+=( "-Doracle.subject=$ORACLE_SUBJECT" )
RUNCP="$(mx "$HERE/h2-dialect-w1b")$SEP$(mx "$WORK/classes")$SEP$CP$SEP$(mx "$FM")$SEP$(mx "$PLS")$SEP$(mx "$TSRC")$SEP$(mx "$SOC")$SEP$(mx "$ORD")$SEP$(mx "$TESMOD/resources")"
[ -n "${ORACLE_EXTRA_CP:-}" ] && RUNCP="$RUNCP$SEP$ORACLE_EXTRA_CP"
"$JH/bin/java" "${OPTS[@]}" ${JAVA_OPTS:-} -cp "$RUNCP" "${ORACLE_MAIN:-OracleRunner}" "$(mx "$BASE")" "${1:-FMM-NOR-T}"
