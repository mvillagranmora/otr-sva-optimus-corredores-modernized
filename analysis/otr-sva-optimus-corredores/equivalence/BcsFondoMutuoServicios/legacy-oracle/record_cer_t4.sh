#!/usr/bin/env bash
# F3 W3 T4-B (F-G facturacion + F-I reinversion y switch): OWN COPY of record_cer.sh (T1..T3; that file is NOT edited). Same oracle (the whole legacy class
# CertificarSolicitudAporteRescate real, same real collaborators, same registered doubles, same runner knobs, 20 runs per case) with THREE differences:
#  1. the call of the recursion (C:2772, `certificarSwitch.logica(con,entradaSwitch)`) is replaced, in a COPY of the legacy source compiled from $WORK/copia (legacy/ is only read), by
#     `cl.bcs.oracle.T4Spy.hijo(certificarSwitch, con, entradaSwitch)`: an OBSERVATION line that records the entry and the answer of the child and delegates untouched
#     (ORACLE_T4_SPY=0 compiles the source as it is: the "neutrality" check compares both recordings, the spy only ADDS its entry to the list of collaborators);
#  2. `CrearSolicitudAporteReinversion` (AR) may run REAL: the case says `"arReal": true`; the double generated from the legacy source delegates to a rename of the real class
#     (`CrearSolicitudAporteReinversionReal`, identifier only) and T4Spy records the call; every other case answers from its script (the real class is always compiled);
#  3. own runner (runner-cer-t4/OracleRunnerCerT4.java), cases in cases-cer-t4/, report determinism-cer-t4.txt.
# (Header of the original follows.)
# F3 W3 T1 (F-A cabecera/validacion + F-B montos y reglas puras): oracle of cl.bcs.fondomutuo.CertificarSolicitudAporteRescate (3.052 lines, `logica` CCN 95), the WHOLE class real,
# unmodified. Same session emulation, doubles runtime and knobs as record_cvc.sh / record_rre.sh (ORACLE_WORK, ORACLE_SKIP_COMPILE, LEGACY_OVERLAY, ORACLE_SUBJECT, ORACLE_OUT,
# ORACLE_EXTRA_SOURCES, ORACLE_EXTRA_CP, ORACLE_CASES, ORACLE_REPEAT, ORACLE_REPORT, JAVA_OPTS). Everything is compiled into a TEMPORARY directory (legacy/ is only read; nothing is
# copied into legacy/ or modernized/); H2 in memory only: no HANA, no network, no credential, no .env. The legacy main() (a connection alias and a real-looking solicitud id) is
# compiled and NEVER called (RULE-076).
#  - REAL (unmodified, or its LEGACY_OVERLAY copy = mutation of the goldens): CertificarSolicitudAporteRescate, AgregarLogCertificacionAporteRescate (the FM-CER-14 log insert),
#    CalcularMontoNumeroCuota, CalcularComisiones, RevisarRequisitosBasicos, ConfiguracionSistema, MonedaFormato, MensajeSalida, SQL / NamedParameterStatement, Fecha and the pojo/ of the
#    package;
#  - REGISTERED DOUBLES generated from the real sources by tools-cer/gen_doubles_cer.py (the shared tools/gen_doubles.py plus `logicaAsync`): everything Certificar calls that is not
#    portable in T1 (Plataforma EntregarPreciosTasas / CalcularParidad / SaldoFlujoCaja / AgregarLog / NotificarEstadoOrdenesOWP, Custodia ProcesarCustodiaFondoMutuo /
#    CalculaCustodiaRescate, CuentaInversion CrearCargoAbono, Tesoreria CrearIngresoEgreso / ProcesarIngresoEgresoTesoreria, Mesa Spot, Facturacion CrearMovimientoComprobante /
#    CrearComprobante, Normativa ResumenPatrimonial (imported, never called), CrearSolicitudAporteReinversion, NotificarEstadoOrdenFondoMutuo). A double answers from seed/doubles-base.json
#    + seed/doubles-cer.json (benign answers) + the case "colaboradores"; "error" makes it throw = the STOP of a truncated (parada) case;
#  - the runner is OWN: runner-cer/OracleRunnerCer.java (the shared OracleRunner / OracleRunnerLect / OracleRunnerCvc / LegacySubject are used as they are, never edited);
#  - the cases live in cases-cer/ (NOT cases/); schema-cer.sql + seed/base-cer.sql are loaded through "esquemasExtra";
#  - -Doracle.repeat=N (default 20, ORACLE_REPEAT): every case runs N times in fresh sessions and the dumps are compared; an unstable case is listed in $ORACLE_REPORT (default
#    determinism-cer.txt) and gets NO golden (exit 4).
#   ./record_cer.sh [caseNamePrefix]
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
find "$HERE/shims" "$HERE/shims-cer" "$HERE/runner" "$HERE/runner-cer" "$HERE/runner-cer-t4" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
for f in ${ORACLE_EXTRA_SOURCES:-}; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done

if [ -z "${ORACLE_SKIP_COMPILE:-}" ]; then
echo "== compiling oracle into $WORK/classes (release 11)"
rm -rf "$WORK/classes"; mkdir -p "$WORK/classes"
"$JH/bin/javac" --release 11 -encoding UTF-8 -proc:none -nowarn -Xlint:none -Xmaxerrs 200 -d "$(mx "$WORK/classes")" -cp "$CP" @"$(mx "$WORK/sources.txt")"
fi

echo "== running"
cd "$WORK/run"
REPORT="${ORACLE_REPORT:-$BASE/determinism-cer-t4.txt}"
OPTS=( "-Duser.timezone=UTC" "-Dfile.encoding=UTF-8" "-Duser.language=en" "-Dlog4j2.StatusLogger.level=OFF" "-Dlog4j2.statusLoggerLevel=OFF"
       "-Doracle.repeat=${ORACLE_REPEAT:-20}" "-Doracle.report=$(mx "$REPORT")" "-Doracle.cases=$(mx "${ORACLE_CASES:-$BASE/cases-cer-t4}")" )
[ -n "${ORACLE_OUT:-}" ] && OPTS+=( "-Doracle.out=$(mx "$ORACLE_OUT")" )
[ -n "${ORACLE_SUBJECT:-}" ] && OPTS+=( "-Doracle.subject=$ORACLE_SUBJECT" )
[ -n "${ORACLE_MUTANT:-}" ] && OPTS+=( "-Doracle.mutant=$ORACLE_MUTANT" )
RUNCP="$(mx "$WORK/classes")$SEP$CP"
[ -n "$OVERLAY_RES" ] && RUNCP="$RUNCP$SEP$(mx "$OVERLAY_RES")"
RUNCP="$RUNCP$SEP$(mx "$FM")$SEP$(mx "$PLS")"
[ -n "${ORACLE_EXTRA_CP:-}" ] && RUNCP="$RUNCP$SEP$ORACLE_EXTRA_CP"
"$JH/bin/java" "${OPTS[@]}" ${JAVA_OPTS:-} -cp "$RUNCP" "${ORACLE_MAIN:-OracleRunnerCerT4}" "$(mx "$BASE")" "${1:-}"
