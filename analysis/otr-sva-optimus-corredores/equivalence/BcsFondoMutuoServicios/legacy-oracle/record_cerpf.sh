#!/usr/bin/env bash
# F3 W3 T6-A (CertificarPorFiltrosSolicitudAporteRescate: a SELECT per filter + the REAL CertificarSolicitudAporteRescate; the WHOLE run is the golden): OWN COPY of record_cer_t5.sh (that file is NOT
# edited). Same oracle machinery (the real legacy classes compiled into a TEMPORARY directory from legacy/, same real collaborators, same registered doubles, same runner knobs, 20 runs per case,
# H2 in memory only, no HANA / network / credential / .env) with these differences:
#  1. the SUBJECT is the whole real legacy class cl.bcs.fondomutuo.CertificarPorFiltrosSolicitudAporteRescate (case.json "servicio": the entry JSON of the case is the entry of ITS call()); it calls
#     the REAL CertificarSolicitudAporteRescate (no double). The oracle subject of the runner stays "legacy" (LegacySubject loads cl.bcs.fondomutuo.<servicio>);
#  2. own runner (runner-cerpf/OracleRunnerCerPf.java), own spy (runner-cerpf/cl/bcs/oracle/PfSpy.java), own cases (cases-cerpf/, report determinism-cerpf.txt, main OracleRunnerCerPf);
#  3. the observed COPY of CertificarPorFiltrosSolicitudAporteRescate (ONE line differs: its call to Certificar goes through cl.bcs.oracle.PfSpy.certificar; ORACLE_PF_SPY=0 compiles it as it is) and
#     the observed copy of Certificar (T4Spy.hijo on the recursion, as T5; ORACLE_T4_SPY=0 compiles it as it is) are built from the LEGACY_OVERLAY copy when there is one (golden mutation);
#  4. schema-cerpf.sql (H2 stand-ins of what the select of the filters reads and the F1 schema lacks) is loaded through "esquemasExtra".
# Knobs as before: ORACLE_WORK, ORACLE_SKIP_COMPILE, LEGACY_OVERLAY, LEGACY_OVERLAY_RES (a mutated copy of the SQL resource), ORACLE_OUT, ORACLE_EXTRA_SOURCES, ORACLE_EXTRA_CP, ORACLE_CASES,
# ORACLE_REPEAT, ORACLE_REPORT, JAVA_OPTS. The legacy main() is compiled and NEVER called (RULE-076).
#   ./record_cerpf.sh [caseNamePrefix]
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

echo "== generating shim Utils and doubles into $WORK/gen"
rm -rf "$WORK/gen"; mkdir -p "$WORK/gen" "$WORK/classes" "$WORK/run"
$PY -I "$HERE/tools/gen_utils.py" "$(mx "$UT/Utils.java")" "$(mx "$WORK/gen")"
DOUBLE_SRC=( "$TES/CrearIngresoEgreso.java" "$MES/CrearOperacionSpot.java" "$CUS/ConsultarFlujoCustodia.java"
  "$PLP/mer/CalcularParidad.java" "$PLP/tas/EntregarPreciosTasas.java" "$PLP/pla/SaldoFlujoCaja.java" "$PLP/pla/AgregarLog.java"
  "$PLP/pla/NotificarEstadoOrdenesOWP.java" "$PLP/pla/utils/ConsultarEmpresa.java"
  "$CIN/CrearCargoAbono.java" "$FAC/CrearComprobante.java" "$FAC/CrearMovimientoComprobante.java" "$NOR/ResumenPatrimonial.java"
  "$FMP/CrearSolicitudAporteReinversion.java" "$FMP/ProcesarCustodiaFondoMutuo.java" "$FMP/CalculaCustodiaRescate.java" "$FMP/NotificarEstadoOrdenFondoMutuo.java"
  "$FMP/ProcesarIngresoEgresoTesoreria.java" "$FMP/ProcesarIngresoOperacionSpot.java" "$FMP/CrearSolicitudAporteRescateNormal.java"
  "$MES/ConsultarPuntasPorTramo.java" "$PLP/bat/InscribirProcesoBatch.java" "$PLP/com/ConsultarProductosPortafolio.java" "$PLP/ins/ConsultarDatosInstrumentos.java" )   # the last four: collaborators of the REAL AR closure
DS=(); for f in "${DOUBLE_SRC[@]}"; do DS+=("$(mx "$f")"); done
$PY -I "$HERE/tools-cer/gen_doubles_cer.py" "$(mx "$WORK/gen")" "${DS[@]}"
mkdir -p "$WORK/gen/cl/bcs/plataforma/pla/utils"
sed 's/System.currentTimeMillis()/cl.bcs.oracle.Clock.millis()/g' "$(pick "$PLP/pla/utils/FechaDB.java")" > "$WORK/gen/cl/bcs/plataforma/pla/utils/FechaDB.java"
# NOTIF: logicaAsync of the generated double reads PLA.CallBatchURL / PLA.CallBatchToken first when the case says "asyncParametros" (T5Notif); the script call stays
NOD="$WORK/gen/cl/bcs/fondomutuo/NotificarEstadoOrdenFondoMutuo.java"
sed -i 's/{ cl.bcs.oracle.Doubles.answer("NotificarEstadoOrdenFondoMutuo", "logicaAsync", new Object\[\]{con, entrada}, Void.class); }/{ cl.bcs.oracle.T5Notif.parametros(con, entrada.p_IdEmpresa); cl.bcs.oracle.Doubles.answer("NotificarEstadoOrdenFondoMutuo", "logicaAsync", new Object[]{con, entrada}, Void.class); }/' "$NOD"
grep -q 'cl.bcs.oracle.T5Notif.parametros(con, entrada.p_IdEmpresa)' "$NOD" || { echo "the double of NotificarEstadoOrdenFondoMutuo.logicaAsync could not be routed to T5Notif" >&2; exit 5; }
# AR: the generated double answers from the script unless the case says "arReal" (then T4Real delegates to the real class, compiled under another name)
ARD="$WORK/gen/cl/bcs/fondomutuo/CrearSolicitudAporteReinversion.java"
sed -i 's/{ return cl.bcs.oracle.Doubles.answer("CrearSolicitudAporteReinversion", "logica", new Object\[\]{con, entrada}, SalidaCrearSolicitudAporteReinversion.class); }/{ if (cl.bcs.oracle.T4Spy.arReal()) return cl.bcs.oracle.T4Real.ar(con, entrada); return cl.bcs.oracle.Doubles.answer("CrearSolicitudAporteReinversion", "logica", new Object[]{con, entrada}, SalidaCrearSolicitudAporteReinversion.class); }/' "$ARD"
grep -q 'cl.bcs.oracle.T4Real.ar(con, entrada)' "$ARD" || { echo "the double of AR could not be routed to the real class" >&2; exit 5; }
mkdir -p "$WORK/real"
# (the rename of the identifier, plus ONE type bridge: the double of ProcesarIngresoOperacionSpot declares `p_DatosOrigenInversion` with the nested type of the DOUBLE of AR)
sed -e 's/\bCrearSolicitudAporteReinversion\b/CrearSolicitudAporteReinversionReal/g' -e 's/entradaprocesarSpot\.p_DatosOrigenInversion\.add(inSolicitud);/entradaprocesarSpot.p_DatosOrigenInversion.add(cl.bcs.oracle.T4Real.puente(inSolicitud));/' "$(pick "$FMP/CrearSolicitudAporteReinversion.java")" > "$WORK/real/CrearSolicitudAporteReinversionReal.java"
grep -q 'cl.bcs.oracle.T4Real.puente(inSolicitud)' "$WORK/real/CrearSolicitudAporteReinversionReal.java" || { echo "the type bridge of AR was not applied" >&2; exit 5; }

SRC=()
# the observed copy of Certificar (T4Spy.hijo on the recursion) or the source as it is
mkdir -p "$WORK/copia"
if [ "${ORACLE_T4_SPY:-1}" = "0" ]; then
  cp "$(pick "$FMP/CertificarSolicitudAporteRescate.java")" "$WORK/copia/CertificarSolicitudAporteRescate.java"
else
  sed 's/certificarSwitch\.logica(con,entradaSwitch)/cl.bcs.oracle.T4Spy.hijo(certificarSwitch, con, entradaSwitch)/' "$(pick "$FMP/CertificarSolicitudAporteRescate.java")" > "$WORK/copia/CertificarSolicitudAporteRescate.java"
  grep -q 'cl.bcs.oracle.T4Spy.hijo(certificarSwitch, con, entradaSwitch)' "$WORK/copia/CertificarSolicitudAporteRescate.java" || { echo "the recursion call was not found in the legacy source" >&2; exit 5; }
  [ "$(diff "$(pick "$FMP/CertificarSolicitudAporteRescate.java")" "$WORK/copia/CertificarSolicitudAporteRescate.java" | grep -c '^>')" = "1" ] || { echo "the observed copy must differ in exactly ONE line" >&2; exit 5; }
fi
SRC+=("$(mx "$WORK/copia/CertificarSolicitudAporteRescate.java")")
# the observed copy of CertificarPorFiltros (PfSpy.certificar on its call to Certificar) or the source as it is
PFN=CertificarPorFiltrosSolicitudAporteRescate
if [ "${ORACLE_PF_SPY:-1}" = "0" ]; then
  cp "$(pick "$FMP/$PFN.java")" "$WORK/copia/$PFN.java"
else
  sed 's/certificar\.logica(con, entradaCertificarSolicitudAporteRescate)/cl.bcs.oracle.PfSpy.certificar(certificar, con, entradaCertificarSolicitudAporteRescate)/' "$(pick "$FMP/$PFN.java")" > "$WORK/copia/$PFN.java"
  grep -q 'cl.bcs.oracle.PfSpy.certificar(certificar, con, entradaCertificarSolicitudAporteRescate)' "$WORK/copia/$PFN.java" || { echo "the call to Certificar was not found in the legacy source of CertificarPorFiltros" >&2; exit 5; }
  [ "$(diff "$(pick "$FMP/$PFN.java")" "$WORK/copia/$PFN.java" | grep -c '^>')" = "1" ] || { echo "the observed copy of CertificarPorFiltros must differ in exactly ONE line" >&2; exit 5; }
fi
SRC+=("$(mx "$WORK/copia/$PFN.java")")
for c in AgregarLogCertificacionAporteRescate CalcularMontoNumeroCuota CalcularComisiones; do SRC+=("$(pick "$FMP/$c.java")"); done
# the REAL closure of AR (rename of the class) and the real classes it calls (same list as record_rre.sh)
SRC+=("$(mx "$WORK/real/CrearSolicitudAporteReinversionReal.java")")
for c in CalcularFechaSolicitudAporteRescate ServicioCumpleRequisitosCrearSolicitudAporteRescate ConsultarFlujoCustodiaFondoMutuo CrearSolicitudAGF ValidaMontoInversion ValidaPatrimonioEmisor ObtenerPerfilFondoMutuo; do SRC+=("$(pick "$FMP/$c.java")"); done
for f in "$FMP"/utils/*.java; do SRC+=("$(pick "$f")"); done
for f in "$FMP"/pojo/*.java; do SRC+=("$(pick "$f")"); done
for c in SQL NamedParameterStatement NamedCallableStatement JavaCall JavaCallMetadata GsonTimestampDeserializer GsonDateDeserializer Fecha BcsPojo; do SRC+=("$(pick "$UT/$c.java")"); done
for c in pla/utils/ConfiguracionSistema pla/utils/Diccionario pla/utils/PlataformaUtils pla/utils/FileLoader pla/utils/BarraProgreso pla/utils/MonedaFormato pla/RevisarRequisitosBasicos \
         pla/ObtenerDiaSiguiente pla/ObtenerDiaAnterior pla/ObtenerDiaHabilMultiplesPaises; do SRC+=("$(pick "$PLP/$c.java")"); done
for f in "$FAC"/pojo/*.java; do SRC+=("$f"); done
SRC+=( "$TES/pojo/IngresoEgreso.java" "$TES/pojo/IngresoEgresoFormaDePago.java" )   # types imported by the CrearIngresoEgreso double
: > "$WORK/sources.txt"
for f in "${SRC[@]}"; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$WORK/gen" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
find "$HERE/shims" "$HERE/shims-cer" "$HERE/runner" "$HERE/runner-cer" "$HERE/runner-cer-t4/cl" "$HERE/runner-cer-t5/cl" "$HERE/runner-cerpf" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
for f in ${ORACLE_EXTRA_SOURCES:-}; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done

if [ -z "${ORACLE_SKIP_COMPILE:-}" ]; then
echo "== compiling oracle into $WORK/classes (release 11)"
rm -rf "$WORK/classes"; mkdir -p "$WORK/classes"
"$JH/bin/javac" --release 11 -encoding UTF-8 -proc:none -nowarn -Xlint:none -Xmaxerrs 200 -d "$(mx "$WORK/classes")" -cp "$CP" @"$(mx "$WORK/sources.txt")"
fi

echo "== running"
cd "$WORK/run"
REPORT="${ORACLE_REPORT:-$BASE/determinism-cerpf.txt}"
OPTS=( "-Duser.timezone=UTC" "-Dfile.encoding=UTF-8" "-Duser.language=en" "-Dlog4j2.StatusLogger.level=OFF" "-Dlog4j2.statusLoggerLevel=OFF"
       "-Doracle.repeat=${ORACLE_REPEAT:-20}" "-Doracle.report=$(mx "$REPORT")" "-Doracle.cases=$(mx "${ORACLE_CASES:-$BASE/cases-cerpf}")" )
[ -n "${ORACLE_OUT:-}" ] && OPTS+=( "-Doracle.out=$(mx "$ORACLE_OUT")" )
[ -n "${ORACLE_SUBJECT:-}" ] && OPTS+=( "-Doracle.subject=$ORACLE_SUBJECT" )
[ -n "${ORACLE_MUTANT:-}" ] && OPTS+=( "-Doracle.mutant=$ORACLE_MUTANT" )
RUNCP="$(mx "$WORK/classes")$SEP$CP"
[ -n "$OVERLAY_RES" ] && RUNCP="$RUNCP$SEP$(mx "$OVERLAY_RES")"
RUNCP="$RUNCP$SEP$(mx "$FM")$SEP$(mx "$PLS")"
[ -n "${ORACLE_EXTRA_CP:-}" ] && RUNCP="$RUNCP$SEP$ORACLE_EXTRA_CP"
"$JH/bin/java" "${OPTS[@]}" ${JAVA_OPTS:-} -cp "$RUNCP" "${ORACLE_MAIN:-OracleRunnerCerPf}" "$(mx "$BASE")" "${1:-}"
