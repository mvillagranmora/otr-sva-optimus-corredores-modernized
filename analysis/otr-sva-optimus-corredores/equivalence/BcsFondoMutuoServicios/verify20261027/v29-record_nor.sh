#!/usr/bin/env bash
# verify 2026-10-29 (v29): COPY of legacy-oracle/record.sh (unmodified legacy, same doubles) + ORACLE_CASES / ORACLE_REPEAT / ORACLE_REPORT. Not a golden recorder: always use ORACLE_OUT.
# Oracle for cl.bcs.fondomutuo.ManejadorSolicitudAporteRescate (JavaCall 601190) and CrearSolicitudAporteRescateNormal (JavaCall 357) and the
# FondoMutuo closure of the plan (PLAN_FONDOMUTUO_SOLICITUD_APORTE_RESCATE.md paso 1B). Compiles the REAL legacy sources into a temporary
# directory (never inside legacy/) and runs them on in-memory H2 seeded per case. No HANA, no network, no credential.
#
#   ./record.sh [caseNamePrefix]            re-records cases/<case>/expected.json (the goldens) from the legacy   (default: every case)
#   ORACLE_SUBJECT=<FQCN> ORACLE_OUT=<dir> ./record.sh      run ANOTHER subject (new implementation, or cl.bcs.oracle.MutantSubject) and
#                                                           dump <dir>/<case>.json in the golden format (same cases, same dump)
#
# REAL legacy sources used AS IS: the 12 closure classes + Manejador + Normal + pojo/* + utils/* of cl.bcs.fondomutuo; BcsUtils SQL,
# NamedParameterStatement, NamedCallableStatement, JavaCall, Gson*Deserializer, Fecha, BcsPojo; Plataforma kit ConfiguracionSistema, Diccionario,
# PlataformaUtils (MensajeSalida), FechaDB, MonedaFormato, RevisarRequisitosBasicos, ObtenerDiaSiguiente/Anterior/HabilMultiplesPaises.
# GENERATED from the real sources (tools/gen_utils.py, tools/gen_doubles.py): shim Utils (verbatim methods + H2 getSecuencia) and the registered
# DOUBLES (collaborators outside the closure; method bodies replaced by cl.bcs.oracle.Doubles, declarations are the legacy ones).
# Hand written shims (shims/): datadog Trace, dbcp2 BasicDataSource (compile only), cl.bcs.utils.Log (no file writes). Real jars from ~/.m2:
# h2, gson, commons-io, commons-beanutils, commons-collections, commons-logging, commons-lang 2.6, commons-configuration 1.2, log4j-api.
# Java 11 source level (--release 11), run with JDK 25. Knobs: ORACLE_WORK scratch dir | LEGACY_OVERLAY dir with MUTATED copies of legacy sources
# (same file name; mutation check of the goldens) | ORACLE_MUTANT | ORACLE_SUBJECT | ORACLE_OUT | ORACLE_EXTRA_SOURCES / ORACLE_EXTRA_CP (new impl) | JAVA_OPTS.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")/../legacy-oracle" && pwd)"   # v29 copy: tools/shims/runner stay in legacy-oracle (read only)
BASE="$(cd "$HERE/.." && pwd)"                       # .../equivalence/BcsFondoMutuoServicios
WS="$(cd "$BASE/../../../.." && pwd)"                # workspace root
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
pick() { # use the mutated copy when LEGACY_OVERLAY has one with the same file name
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
  "$FMP/CrearSolicitudAporteRescateProgramado.java" "$FMP/CrearSolicitudAporteOvernight.java" "$FMP/CrearSolicitudAporteReinversion.java"
  "$FMP/CrearSolicitudCambioSerie.java" "$FMP/CrearSolicitudRescateReinversion.java" "$FMP/CrearSolicitudRescateTransferencia.java"
  "$FMP/CrearSolicitudRescateTraspaso.java" )
DS=(); for f in "${DOUBLE_SRC[@]}"; do DS+=("$(mx "$f")"); done
$PY -I "$HERE/tools/gen_doubles.py" "$(mx "$WORK/gen")" "${DS[@]}"
# clock-pinned COPY of the real FechaDB (only System.currentTimeMillis() is replaced by cl.bcs.oracle.Clock.millis(): the original adds real elapsed ms)
mkdir -p "$WORK/gen/cl/bcs/plataforma/pla/utils"
sed 's/System.currentTimeMillis()/cl.bcs.oracle.Clock.millis()/g' "$(pick "$PLP/pla/utils/FechaDB.java")" > "$WORK/gen/cl/bcs/plataforma/pla/utils/FechaDB.java"

SRC=()
for c in ManejadorSolicitudAporteRescate CrearSolicitudAporteRescateNormal CalcularFechaSolicitudAporteRescate CrearSolicitudRescateComisionNN CalcularComisiones \
         CalcularMontoNumeroCuota ServicioCumpleRequisitosCrearSolicitudAporteRescate ConsultarFlujoCustodiaFondoMutuo CrearSolicitudAGF ValidaMontoInversion \
         ValidaPatrimonioEmisor ObtenerPerfilFondoMutuo ProcesarIngresoEgresoTesoreria ProcesarIngresoOperacionSpot; do SRC+=("$(pick "$FMP/$c.java")"); done
for f in "$FMP"/pojo/*.java "$FMP"/utils/*.java; do SRC+=("$(pick "$f")"); done
for c in SQL NamedParameterStatement NamedCallableStatement JavaCall GsonTimestampDeserializer GsonDateDeserializer Fecha BcsPojo; do SRC+=("$(pick "$UT/$c.java")"); done
for c in pla/utils/ConfiguracionSistema pla/utils/Diccionario pla/utils/PlataformaUtils pla/utils/FileLoader pla/utils/BarraProgreso pla/utils/MonedaFormato pla/RevisarRequisitosBasicos \
         pla/ObtenerDiaSiguiente pla/ObtenerDiaAnterior pla/ObtenerDiaHabilMultiplesPaises; do SRC+=("$(pick "$PLP/$c.java")"); done
SRC+=( "$TES/pojo/IngresoEgreso.java" "$TES/pojo/IngresoEgresoFormaDePago.java" )   # types imported by the CrearIngresoEgreso double
: > "$WORK/sources.txt"
for f in "${SRC[@]}"; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$WORK/gen" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$HERE/shims" "$HERE/runner" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
for f in ${ORACLE_EXTRA_SOURCES:-}; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done

if [ -z "${ORACLE_SKIP_COMPILE:-}" ]; then   # ORACLE_SKIP_COMPILE=1: reuse $WORK/classes (data-only iterations)
echo "== compiling oracle into $WORK/classes (release 11)"
rm -rf "$WORK/classes"; mkdir -p "$WORK/classes"
"$JH/bin/javac" --release 11 -encoding UTF-8 -proc:none -nowarn -Xlint:none -Xmaxerrs 200 -d "$(mx "$WORK/classes")" -cp "$CP" @"$(mx "$WORK/sources.txt")"
fi

echo "== running"
cd "$WORK/run"
OPTS=( "-Duser.timezone=UTC" "-Dfile.encoding=UTF-8" "-Duser.language=en" "-Dlog4j2.StatusLogger.level=OFF" "-Dlog4j2.statusLoggerLevel=OFF" )
[ -n "${ORACLE_OUT:-}" ] && OPTS+=( "-Doracle.out=$(mx "$ORACLE_OUT")" )
[ -n "${ORACLE_SUBJECT:-}" ] && OPTS+=( "-Doracle.subject=$ORACLE_SUBJECT" )
[ -n "${ORACLE_MUTANT:-}" ] && OPTS+=( "-Doracle.mutant=$ORACLE_MUTANT" )
# v29: the knobs of the later record_*.sh scripts (cases dir, repetitions, determinism report) that record.sh lacks; nothing else changes
[ -n "${ORACLE_CASES:-}" ] && OPTS+=( "-Doracle.cases=$(mx "$ORACLE_CASES")" )
OPTS+=( "-Doracle.repeat=${ORACLE_REPEAT:-1}" )
[ -n "${ORACLE_REPORT:-}" ] && OPTS+=( "-Doracle.report=$(mx "$ORACLE_REPORT")" )
# the legacy loads its SQL with loadResourceToString("cl/bcs/.../sql/x.sql"): the legacy src roots (read only) are on the RUN classpath
RUNCP="$(mx "$WORK/classes")$SEP$CP$SEP$(mx "$FM")$SEP$(mx "$PLS")"
[ -n "${ORACLE_EXTRA_CP:-}" ] && RUNCP="$RUNCP$SEP$ORACLE_EXTRA_CP"   # step 4A: a list in mixed form, already separated with $SEP (the new implementation + its jars)
"$JH/bin/java" "${OPTS[@]}" ${JAVA_OPTS:-} -cp "$RUNCP" "${ORACLE_MAIN:-OracleRunner}" "$(mx "$BASE")" "${1:-}"
