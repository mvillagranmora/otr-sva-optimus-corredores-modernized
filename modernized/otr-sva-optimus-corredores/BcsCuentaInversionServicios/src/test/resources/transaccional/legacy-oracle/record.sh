#!/usr/bin/env bash
# Re-records transaccional/cases/*/expected.json by EXECUTING the unmodified legacy classes.
#
#   ./record.sh [caseNamePrefix]            e.g.  ./record.sh CTA-A-03     (default: every case)
#
# Needs JDK 25 (JAVA_HOME or the Eclipse Adoptium path below) and the jars in ~/.m2 (h2, gson).
# Never writes into legacy/ or into modernized/ except cases/<case>/expected.json: all compiled output goes to $ORACLE_WORK
# (default: a fresh temporary directory). Nothing here contacts HANA, a service or a real credential.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TRANS="$(cd "$HERE/.." && pwd)"                                  # .../src/test/resources/transaccional
MODULE="$(cd "$TRANS/../../../.." && pwd)"                       # .../BcsCuentaInversionServicios
WS="$(cd "$MODULE/../../.." && pwd)"                             # workspace root
LEG="$WS/legacy/otr-sva-optimus-corredores/BSCL"
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
mkdir -p "$WORK/classes" "$WORK/run"

H2="$M2/com/h2database/h2/2.4.240/h2-2.4.240.jar"
GSON="$M2/com/google/code/gson/gson/2.13.2/gson-2.13.2.jar"
COLL="$M2/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar"
SEP=";"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) ;; *) SEP=":";; esac
mx() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi; }
CP="$(mx "$H2")$SEP$(mx "$GSON")$SEP$(mx "$COLL")"

CTA="$LEG/CuentaInversion/JAVA/BcsCuentaInversionServicios/src"
UTL="$LEG/Plataforma/JAVA/BcsUtils/src"
PLA="$LEG/Plataforma/JAVA/BcsPlataformaServicios/src"
ORD="$LEG/Orden/JAVA/BcsOrdenServicios/src"
SOC="$LEG/SocioNegocio/JAVA/BcsSocioNegocioServicios/src"
FMM="$LEG/FondoMutuo/JAVA/BcsFondoMutuoServicios/src"
SUP="$MODULE/src/test/java/cl/bcs/cuentainversion/transaccional/support"

# --- legacy classes under test (family "transaccional")
CTA_CLASSES="${CTA_CLASSES:-ActualizarSaldoCargoAbono CertificarCargoAbono AnularCargoAbono CrearCargoAbono SaldoCuentaInversion ConsultarSaldoCuentaInversion
 ConsultarCartolaDeMovimiento ConsultarMovimientoCuentaInversion ConsultaComparacionSaldo CuadraturaCierreDiario CrearCargaMasiva
 ProcesaCargaSeleccionada EliminaCargaSeleccionada GenerarArchivoCargoAbono}"
SRC_FILES=()
for c in $CTA_CLASSES; do [ -f "$CTA/cl/bcs/cuentaInversion/$c.java" ] && SRC_FILES+=("$CTA/cl/bcs/cuentaInversion/$c.java"); done
for c in ${WS_CLASSES:-CrearCargoAbonoWS ConsultarMovimientos}; do [ -f "$CTA/cl/bcs/cuentaInversion/webservices/$c.java" ] && SRC_FILES+=("$CTA/cl/bcs/cuentaInversion/webservices/$c.java"); done

# --- legacy utility/service classes used AS IS (real source, no shim): pure algorithms whose only dependency is plain SQL
REAL_FILES=(
  "$UTL/cl/bcs/utils/Fecha.java" "$UTL/cl/bcs/utils/StringUtils.java" "$UTL/cl/bcs/utils/BcsPojo.java"
  "$UTL/cl/bcs/utils/GsonDateDeserializer.java" "$UTL/cl/bcs/utils/GsonTimestampDeserializer.java"
  "$PLA/cl/bcs/plataforma/pla/ObtenerDiaSiguiente.java" "$PLA/cl/bcs/plataforma/pla/ObtenerDiaAnterior.java" "$PLA/cl/bcs/plataforma/pla/ObtenerPlazo.java"
  "$PLA/cl/bcs/plataforma/pla/utils/Feriados.java" "$PLA/cl/bcs/plataforma/pla/utils/MonedaFormato.java"
  "$PLA/cl/bcs/plataforma/pla/utils/FileLoader.java"
  "$FMM/cl/bcs/fondomutuo/pojo/ConstantesFondoMutuo.java" "$FMM/cl/bcs/fondomutuo/pojo/SerieFondoSolicitudes.java"
  "$SOC/cl/bcs/socionegocio/pojo/SalidaEstandar.java" "$SOC/cl/bcs/socionegocio/pojo/AdvertenciaDetalle.java" "$SOC/cl/bcs/socionegocio/pojo/ErrorDetalle.java"
)

SHIMS=()
while IFS= read -r f; do SHIMS+=("$f"); done < <(find "$HERE/shims" -name '*.java')

echo "== compiling oracle into $WORK/classes"
"$JH/bin/javac" -encoding UTF-8 -proc:none -nowarn -Xlint:none -d "$(mx "$WORK/classes")" -cp "$CP" \
  "$SUP/H2Hana.java" "$SUP/Dump.java" "$SUP/Archivos.java" "$HERE/runner/OracleRunner.java" \
  "${SHIMS[@]}" "${REAL_FILES[@]}" "${SRC_FILES[@]}" ${EXTRA_SOURCES:-}

echo "== recording"
cd "$WORK/run"
RUNCP="$(mx "$WORK/classes")$SEP$(mx "$CTA")$SEP$(mx "$PLA")$SEP$(mx "$ORD")$SEP$(mx "$FMM")$SEP$(mx "$TRANS/..")$SEP$CP"
"$JH/bin/java" -Duser.timezone=UTC ${JAVA_OPTS:-} -cp "$RUNCP" OracleRunner "$(mx "$TRANS")" "${1:-}"
