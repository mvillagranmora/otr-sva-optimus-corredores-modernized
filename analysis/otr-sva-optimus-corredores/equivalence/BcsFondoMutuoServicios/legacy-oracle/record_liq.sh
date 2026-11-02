#!/usr/bin/env bash
# F3 W3 T6-B (LiquidacionSolicitudAporteRescate; the WHOLE run is the golden): OWN COPY of record_cerpf.sh (T6-A; that file is NOT edited). Same oracle machinery (the real legacy classes compiled
# into a TEMPORARY directory from legacy/, same registered doubles for what is outside the module, same runner knobs, 20 runs per case, H2 in memory only, no HANA / network / credential / .env)
# with these differences:
#  1. the SUBJECT is the UNMODIFIED real legacy class cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate (case.json "servicio": the entry JSON is the entry of ITS call()). Its collaborators are
#     registered doubles generated from their real sources: Tesoreria CrearIngresoEgreso, CuentaInversion CrearCargoAbono and Plataforma AgregarLog (every call recorded in order with its arguments);
#     the sequences go through the shim Utils.getSecuencia over H2 sequences of the same name (recorded as secuenciasSolicitadas);
#  2. the closure is MINIMAL (no Certificar, no spies): the class only needs the kit (SQL, Fecha, ConfiguracionSistema, Diccionario, PlataformaUtils.MensajeSalida), ConstantesFondoMutuo / ModalidadPago
#     and the nested class Retencion of the APV module, which is a SHIM (shims-liq) whose field block this script compares with the legacy source (it must be identical);
#  3. own runner (runner-liq/OracleRunnerLiq.java), own cases (cases-liq/, report determinism-liq.txt, main OracleRunnerLiq); schema-liq.sql is loaded through "esquemasExtra";
#  4. the SQL of the APV module (cl/bcs/APV/sql/ConsultarRetencion.sql) is read from its legacy module source directory (class path), the overlay LEGACY_OVERLAY_RES comes FIRST.
# Knobs as before: ORACLE_WORK, ORACLE_SKIP_COMPILE, LEGACY_OVERLAY (a mutated copy of the Java source), LEGACY_OVERLAY_RES (a mutated copy of a SQL resource), ORACLE_OUT, ORACLE_EXTRA_SOURCES,
# ORACLE_EXTRA_CP, ORACLE_CASES, ORACLE_REPEAT, ORACLE_REPORT, JAVA_OPTS. The legacy main() is compiled and NEVER called (RULE-076).
#   ./record_liq.sh [caseNamePrefix]
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
WS="$(cd "$BASE/../../../.." && pwd)"
LEG="$WS/legacy/otr-sva-optimus-corredores/BSCL"
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
OVERLAY="${LEGACY_OVERLAY:-}"
OVERLAY_RES="${LEGACY_OVERLAY_RES:-}"
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
CIN="$LEG/CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion"
FAC="$LEG/Facturacion/JAVA/BcsFacturacionServicios/src/cl/bcs/facturacion"
NOR="$LEG/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa"

APV="$LEG/APV/JAVA/BcsAPVServicios/src"
echo "== generating shim Utils and doubles into $WORK/gen"
rm -rf "$WORK/gen"; mkdir -p "$WORK/gen" "$WORK/classes" "$WORK/run"
$PY -I "$HERE/tools/gen_utils.py" "$(mx "$UT/Utils.java")" "$(mx "$WORK/gen")"
DOUBLE_SRC=( "$TES/CrearIngresoEgreso.java" "$CIN/CrearCargoAbono.java" "$PLP/pla/AgregarLog.java" )
DS=(); for f in "${DOUBLE_SRC[@]}"; do DS+=("$(mx "$f")"); done
$PY -I "$HERE/tools-cer/gen_doubles_cer.py" "$(mx "$WORK/gen")" "${DS[@]}"
mkdir -p "$WORK/gen/cl/bcs/plataforma/pla/utils"
sed 's/System.currentTimeMillis()/cl.bcs.oracle.Clock.millis()/g' "$(pick "$PLP/pla/utils/FechaDB.java")" > "$WORK/gen/cl/bcs/plataforma/pla/utils/FechaDB.java"
# the shim of Retencion must be the legacy nested class, token for token
blk() { sed -n '/public static class Retencion/,/}/p' "$1" | tr -cd '[:alnum:]{};,'; }
[ "$(blk "$APV/cl/bcs/APV/CalculoRetencionTraspasos.java")" = "$(blk "$HERE/shims-liq/cl/bcs/APV/CalculoRetencionTraspasos.java")" ] || { echo "the shim of Retencion differs from the legacy nested class" >&2; exit 5; }

SRC=()
SRC+=("$(pick "$FMP/LiquidacionSolicitudAporteRescate.java")")
for c in ConstantesFondoMutuo ModalidadPago; do SRC+=("$(pick "$FMP/pojo/$c.java")"); done
for c in SQL NamedParameterStatement NamedCallableStatement JavaCall JavaCallMetadata GsonTimestampDeserializer GsonDateDeserializer Fecha BcsPojo; do SRC+=("$(pick "$UT/$c.java")"); done
for c in pla/utils/ConfiguracionSistema pla/utils/Diccionario pla/utils/PlataformaUtils pla/utils/FileLoader pla/utils/BarraProgreso pla/utils/MonedaFormato pla/RevisarRequisitosBasicos          pla/ObtenerDiaSiguiente pla/ObtenerDiaAnterior pla/ObtenerDiaHabilMultiplesPaises; do SRC+=("$(pick "$PLP/$c.java")"); done
SRC+=( "$TES/pojo/IngresoEgreso.java" "$TES/pojo/IngresoEgresoFormaDePago.java" )   # types imported by the CrearIngresoEgreso double
: > "$WORK/sources.txt"
for f in "${SRC[@]}"; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$WORK/gen" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$HERE/shims" "$HERE/shims-cer" "$HERE/shims-liq" "$HERE/runner/cl" "$HERE/runner-liq" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
for f in ${ORACLE_EXTRA_SOURCES:-}; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done

if [ -z "${ORACLE_SKIP_COMPILE:-}" ]; then
echo "== compiling oracle into $WORK/classes (release 11)"
rm -rf "$WORK/classes"; mkdir -p "$WORK/classes"
"$JH/bin/javac" --release 11 -encoding UTF-8 -proc:none -nowarn -Xlint:none -Xmaxerrs 200 -d "$(mx "$WORK/classes")" -cp "$CP" @"$(mx "$WORK/sources.txt")"
fi

echo "== running"
cd "$WORK/run"
REPORT="${ORACLE_REPORT:-$BASE/determinism-liq.txt}"
OPTS=( "-Duser.timezone=UTC" "-Dfile.encoding=UTF-8" "-Duser.language=en" "-Dlog4j2.StatusLogger.level=OFF" "-Dlog4j2.statusLoggerLevel=OFF"
       "-Doracle.repeat=${ORACLE_REPEAT:-20}" "-Doracle.report=$(mx "$REPORT")" "-Doracle.cases=$(mx "${ORACLE_CASES:-$BASE/cases-liq}")" )
[ -n "${ORACLE_OUT:-}" ] && OPTS+=( "-Doracle.out=$(mx "$ORACLE_OUT")" )
[ -n "${ORACLE_SUBJECT:-}" ] && OPTS+=( "-Doracle.subject=$ORACLE_SUBJECT" )
[ -n "${ORACLE_MUTANT:-}" ] && OPTS+=( "-Doracle.mutant=$ORACLE_MUTANT" )
RUNCP="$(mx "$WORK/classes")$SEP$CP"
[ -n "$OVERLAY_RES" ] && RUNCP="$RUNCP$SEP$(mx "$OVERLAY_RES")"
RUNCP="$RUNCP$SEP$(mx "$FM")$SEP$(mx "$PLS")$SEP$(mx "$APV")"
[ -n "${ORACLE_EXTRA_CP:-}" ] && RUNCP="$RUNCP$SEP$ORACLE_EXTRA_CP"
"$JH/bin/java" "${OPTS[@]}" ${JAVA_OPTS:-} -cp "$RUNCP" "${ORACLE_MAIN:-OracleRunnerLiq}" "$(mx "$BASE")" "${1:-}"
