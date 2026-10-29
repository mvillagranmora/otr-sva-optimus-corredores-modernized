#!/usr/bin/env bash
# Re-records apv-b/cases/*/expected.json by EXECUTING the unmodified legacy APV classes (family B: traspasos).
#
#   ./record.sh [caseNamePrefix]            e.g.  ./record.sh APV-B-03     (default: every case)
#
# Needs JDK 25 (JAVA_HOME or the Eclipse Adoptium path below) and the jars in ~/.m2 (h2, gson, commons-collections).
# Never writes into legacy/ or into modernized/ except cases/<case>/expected.json: all compiled output goes to $ORACLE_WORK
# (default: a fresh temporary directory; on Windows pass a SHORT path or map a drive with `subst` if paths exceed 260 characters).
# Environment:
#   LEGACY_BSCL      legacy BSCL root            (default: <workspace>/legacy/otr-sva-optimus-corredores/BSCL)
#   LEGACY_APV_SRC   legacy APV source folder    (default: $LEGACY_BSCL/APV/JAVA/BcsAPVServicios/src; mutation-check.sh points it at a scratch copy)
#   ORACLE_WORK      scratch folder for the compiled oracle
#   ORACLE_REUSE_CLASSES=1  skip the compilation when $ORACLE_WORK/classes already holds it (re-recording only the cases)
# Nothing here contacts HANA, a bank or any real service; no credential of the legacy code is used.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RES="$(cd "$HERE/.." && pwd)"                                    # .../src/test/resources/apv-b
MODULE="$(cd "$RES/../../../.." && pwd)"                         # .../BcsAPVServicios
WS="$(cd "$MODULE/../../.." 2>/dev/null && pwd || true)"         # workspace root (not needed when LEGACY_BSCL is given)
LEG="${LEGACY_BSCL:-$WS/legacy/otr-sva-optimus-corredores/BSCL}"
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
mkdir -p "$WORK/classes" "$WORK/run"

H2="$M2/com/h2database/h2/2.4.240/h2-2.4.240.jar"
GSON="$M2/com/google/code/gson/gson/2.13.2/gson-2.13.2.jar"
COLL="$M2/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar"
LANG3="$(ls "$M2"/org/apache/commons/commons-lang3/*/commons-lang3-*.jar 2>/dev/null | grep -v sources | tail -1)"
COMTEXT="$(ls "$M2"/org/apache/commons/commons-text/*/commons-text-*.jar 2>/dev/null | grep -v sources | tail -1)"
BEANUTILS="$(ls "$M2"/commons-beanutils/commons-beanutils/*/commons-beanutils-*.jar 2>/dev/null | grep -v sources | tail -1)"
SEP=";"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) ;; *) SEP=":";; esac
mx() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi; }
CP="$(mx "$H2")$SEP$(mx "$GSON")$SEP$(mx "$COLL")"
for j in "$LANG3" "$COMTEXT" "$BEANUTILS"; do [ -n "$j" ] && CP="$CP$SEP$(mx "$j")"; done

APV="${LEGACY_APV_SRC:-$LEG/APV/JAVA/BcsAPVServicios/src}"
UTL="$LEG/Plataforma/JAVA/BcsUtils/src"
PLA="$LEG/Plataforma/JAVA/BcsPlataformaServicios/src"
FMM="$LEG/FondoMutuo/JAVA/BcsFondoMutuoServicios/src"
SNG="$LEG/SocioNegocio/JAVA/BcsSocioNegocioServicios/src"
SUP="$MODULE/src/test/java/cl/bcs/apv/familia_b/support"

# --- legacy classes under test (family B) and the APV classes they call in process, relative to cl/bcs/APV/
# (javac -sourcepath $APV compiles any other APV class they reference; every class OUTSIDE APV is a shim)
APV_CLASSES="${APV_CLASSES:-GrabarContratoTraspasoSAAPV ModificarPortafolioTraspaso CrearTraspasoTesoreria GrabarTraspasos CalculoMontosDesinversion ReaperturarTraspaso TraspasoCuentasBancarias CrearTraspaso AnularTraspasoEgreso CalculoRetencionTraspasos CrearRecaudacionDeposito CrearRecaudacionCliente ConciliarDeposito AsignarContratoARecaudacion ProcesarDistribucion GrabarRecaudacionTraspaso IngresoRecaudacionTraspaso CambiarEstadoTraspaso CrearOrdenTraspaso CargaRegistroHistoricoTraspaso ModificarOrigenDestino ValidaContratoSAAPV CambiarEstadoContratosSAAPV}"
SRC_FILES=()
for c in $APV_CLASSES; do SRC_FILES+=("$APV/cl/bcs/APV/$c.java"); done

# --- legacy utility classes used AS IS (real source, no shim): pure algorithms and POJOs
REAL_FILES=(
  "$UTL/cl/bcs/utils/Fecha.java" "$UTL/cl/bcs/utils/StringUtils.java" "$UTL/cl/bcs/utils/BcsPojo.java"
  "$UTL/cl/bcs/utils/GsonDateDeserializer.java" "$UTL/cl/bcs/utils/GsonTimestampDeserializer.java"
  "$PLA/cl/bcs/plataforma/pla/utils/MonedaFormato.java" "$PLA/cl/bcs/plataforma/pla/utils/FileLoader.java" "$FMM/cl/bcs/fondomutuo/pojo/ConstantesFondoMutuo.java" "$PLA/cl/bcs/plataforma/pla/utils/SalidaServicios.java"
  "$FMM/cl/bcs/fondomutuo/pojo/ResultadoSolicitud.java"
  "$SNG/cl/bcs/socionegocio/pojo/Enrolamiento.java" "$SNG/cl/bcs/socionegocio/pojo/ErrorDetalle.java"
  "$SNG/cl/bcs/socionegocio/pojo/AdvertenciaDetalle.java" "$SNG/cl/bcs/socionegocio/pojo/SalidaEstandar.java"
  # pure POJOs the recaudacion classes (family C, run for real since GrabarRecaudacionTraspaso & co.) use
  "$FMM/cl/bcs/fondomutuo/pojo/RegimenSerieDeFondo.java"
  "$SNG/cl/bcs/socionegocio/por/pojo/CuentaCustodio.java" "$SNG/cl/bcs/socionegocio/por/pojo/PreferenciaPagoPortafolio.java"
)

SHIMS=()
while IFS= read -r f; do SHIMS+=("$f"); done < <(find "$HERE/shims" -name '*.java')

# the hana-sql copies used by the modern run of h2-dialect/* must be byte-identical to the legacy statement they stand for
for f in "$RES"/hana-sql/*.sql; do
  [ -e "$f" ] || continue
  cmp -s "$f" "$APV/cl/bcs/APV/sql/$(basename "$f")" || { echo "hana-sql/$(basename "$f") differs from the legacy source" >&2; exit 1; }
done

if [ "${ORACLE_REUSE_CLASSES:-0}" = "1" ] && [ -f "$WORK/classes/OracleRunner.class" ]; then
  echo "== reusing the oracle compiled in $WORK/classes (ORACLE_REUSE_CLASSES=1; only valid when neither the legacy sources nor the shims changed)"
else
echo "== compiling oracle into $WORK/classes"
"$JH/bin/javac" -encoding UTF-8 -proc:none -nowarn -Xlint:none -Xmaxerrs 2000 -d "$(mx "$WORK/classes")" -cp "$CP" -sourcepath "$(mx "$APV")" \
  "$SUP/H2Hana.java" "$SUP/Dump.java" "$HERE/runner/OracleRunner.java" \
  "${SHIMS[@]}" "${REAL_FILES[@]}" "${SRC_FILES[@]}" ${EXTRA_SOURCES:-}
fi

echo "== recording"
cd "$WORK/run"
RUNCP="$(mx "$WORK/classes")$SEP$(mx "$APV")$SEP$(mx "$FMM")$SEP$(mx "$SNG")$SEP$(mx "$PLA")$SEP$(mx "$RES/..")$SEP$CP"   # $PLA: the legacy MonedaFormato reads its SQL resource from the Plataforma sources
"$JH/bin/java" -Duser.timezone=UTC ${JAVA_OPTS:-} -cp "$RUNCP" OracleRunner "$(mx "$RES")" "${1:-}"
