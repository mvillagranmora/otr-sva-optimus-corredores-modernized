#!/usr/bin/env bash
# Re-records tesoreria-b/cases/*/expected.json by EXECUTING the unmodified legacy Tesoreria classes (family B).
#
#   ./record.sh [caseNamePrefix]            e.g.  ./record.sh TES-B-03     (default: every case)
#
# Needs JDK 25 (JAVA_HOME or the Eclipse Adoptium path below) and the jars in ~/.m2 (h2, gson, commons-collections, opencsv).
# Never writes into legacy/ or into modernized/ except cases/<case>/expected.json: all compiled output goes to $ORACLE_WORK
# (default: a fresh temporary directory; on Windows pass a SHORT path or map a drive with `subst` if paths exceed 260 characters).
# Nothing here contacts HANA, a bank, Cloper or any real service; no credential of the legacy code is used.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RES="$(cd "$HERE/.." && pwd)"                                    # .../src/test/resources/tesoreria-b
MODULE="$(cd "$RES/../../../.." && pwd)"                         # .../BcsTesoreriaServicios
WS="$(cd "$MODULE/../../.." 2>/dev/null && pwd || true)"                # workspace root (not needed when LEGACY_BSCL is given)
LEG="${LEGACY_BSCL:-$WS/legacy/otr-sva-optimus-corredores/BSCL}"   # LEGACY_BSCL / LEGACY_TES_SRC: used by mutation-check.sh to run a mutated scratch copy
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
mkdir -p "$WORK/classes" "$WORK/run"

H2="$M2/com/h2database/h2/2.4.240/h2-2.4.240.jar"
GSON="$M2/com/google/code/gson/gson/2.13.2/gson-2.13.2.jar"
COLL="$M2/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar"
CSV="$M2/com/opencsv/opencsv/5.9/opencsv-5.9.jar"
LANG3="$(ls "$M2"/org/apache/commons/commons-lang3/*/commons-lang3-*.jar 2>/dev/null | grep -v sources | tail -1)"
COMTEXT="$(ls "$M2"/org/apache/commons/commons-text/*/commons-text-*.jar 2>/dev/null | grep -v sources | tail -1)"
BEANUTILS="$(ls "$M2"/commons-beanutils/commons-beanutils/*/commons-beanutils-*.jar 2>/dev/null | grep -v sources | tail -1)"
SEP=";"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) ;; *) SEP=":";; esac
mx() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi; }
CP="$(mx "$H2")$SEP$(mx "$GSON")$SEP$(mx "$COLL")"   # opencsv: replaced by the shim shims/com/opencsv/CSVWriter.java (the 3-argument constructor of opencsv 3/4 is gone in 5.9)
for j in "$LANG3" "$COMTEXT" "$BEANUTILS"; do [ -n "$j" ] && CP="$CP$SEP$(mx "$j")"; done

TES="${LEGACY_TES_SRC:-$LEG/Tesoreria/JAVA/BcsTesoreriaServicios/src}"
UTL="$LEG/Plataforma/JAVA/BcsUtils/src"
PLA="$LEG/Plataforma/JAVA/BcsPlataformaServicios/src"
FMM="$LEG/FondoMutuo/JAVA/BcsFondoMutuoServicios/src"
SUP="$MODULE/src/test/java/cl/bcs/tesoreria/familia_b/support"

# --- legacy classes under test (family B), relative to cl/bcs/tesoreria/
TES_CLASSES="${TES_CLASSES:-ban/ConsultarSaldoCuentaBanco ban/ConsultarLibretaBanco liq/ConsultarClearingFinanciero liq/ProcesarClearingFinanciero car/ProcesarCargaMasiva CargaMasivaIngresoEgresoDAPFFMM ConsultarFormatoArchivo ActivarMandatosArchivoCargo ban/ActualizarSaldosCuentaBanco dep/GestionDeDepositos nor/AnularNominasTransferencias nor/QuitarDetalleNominasTransferencias ActualizarSaldosCuentaBancoCorredora CrearCargaMasivaPac RecaudacionBCP ConsultaBCP IngresarMandatoPAC IngresarMandatoPACManejador ModificarPAC ProcesarDistribucion CrearCargaMasivaPacRen AnulacionMandatoPAC RevocacionMandatoPAC}"
SRC_FILES=()
for c in $TES_CLASSES; do SRC_FILES+=("$TES/cl/bcs/tesoreria/$c.java"); done
# real POJO/constants of the module used by those classes
for c in ${TES_POJOS:-pojo/Camara pojo/CamaraCompensacion pojo/Traspaso pojo/ConstantesTesoreria}; do SRC_FILES+=("$TES/cl/bcs/tesoreria/$c.java"); done

# --- legacy utility classes used AS IS (real source, no shim): pure algorithms
REAL_FILES=(
  "$UTL/cl/bcs/utils/Fecha.java" "$UTL/cl/bcs/utils/StringUtils.java" "$UTL/cl/bcs/utils/BcsPojo.java"
  "$UTL/cl/bcs/utils/GsonDateDeserializer.java" "$UTL/cl/bcs/utils/GsonTimestampDeserializer.java"
  "$PLA/cl/bcs/plataforma/pla/utils/MonedaFormato.java" "$PLA/cl/bcs/plataforma/pla/utils/FileLoader.java"
  "$PLA/cl/bcs/plataforma/pla/utils/SalidaServicios.java" "$PLA/cl/bcs/plataforma/pla/ObtenerDiaSiguiente.java" "$FMM/cl/bcs/fondomutuo/pojo/ResultadoSolicitud.java" "$FMM/cl/bcs/fondomutuo/pojo/Traspaso.java"
)

SHIMS=()
while IFS= read -r f; do SHIMS+=("$f"); done < <(find "$HERE/shims" -name '*.java')

echo "== compiling oracle into $WORK/classes"
"$JH/bin/javac" -encoding UTF-8 -proc:none -nowarn -Xlint:none -d "$(mx "$WORK/classes")" -cp "$CP" \
  "$SUP/H2Hana.java" "$SUP/Dump.java" "$SUP/Archivos.java" "$HERE/runner/OracleRunner.java" \
  "${SHIMS[@]}" "${REAL_FILES[@]}" "${SRC_FILES[@]}" ${EXTRA_SOURCES:-}

echo "== recording"
cd "$WORK/run"
RUNCP="$(mx "$WORK/classes")$SEP$(mx "$TES")$SEP$(mx "$PLA")$SEP$(mx "$FMM")$SEP$(mx "$RES/..")$SEP$CP"
"$JH/bin/java" -Duser.timezone=UTC ${JAVA_OPTS:-} -cp "$RUNCP" OracleRunner "$(mx "$RES")" "${1:-}"
