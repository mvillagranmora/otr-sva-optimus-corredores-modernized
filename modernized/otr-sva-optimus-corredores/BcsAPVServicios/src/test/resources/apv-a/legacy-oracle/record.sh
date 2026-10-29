#!/usr/bin/env bash
# Re-records apv-a/cases/*/expected.json by EXECUTING the unmodified legacy classes (family A of BcsAPVServicios: contratos SAAPV).
#
#   ./record.sh [caseNamePrefix]            e.g.  ./record.sh APV-A-031     (default: every case)
#
# Needs JDK 25 (JAVA_HOME or the Eclipse Adoptium path below) and the jars in ~/.m2 (h2, gson, commons-collections).
# Never writes into legacy/ or into modernized/ except cases/<case>/expected.json: all compiled output goes to $ORACLE_WORK
# (default: a fresh temporary directory). Nothing here contacts HANA, a service or a real credential.
#
# Knobs:  LEGACY_ROOT    legacy tree root (default: <workspace>/legacy/otr-sva-optimus-corredores/BSCL); read only
#         ORACLE_WORK    scratch directory (compiled classes). On Windows keep it SHORT (e.g. a `subst Q:` drive): javac output paths over 260 chars fail
#         LEGACY_OVERLAY directory with MUTATED copies of legacy sources (same file name) used instead of the real ones: mutation check of
#                        the goldens; together with ORACLE_OUT (directory for <case>.expected.json) so cases/ is left untouched
#         ORACLE_OUT     write <case>.expected.json there instead of into cases/
#         ORACLE_SKIP_COMPILE  reuse the classes already compiled in ORACLE_WORK (only when neither the shims nor the legacy sources changed)
#         JAVA_OPTS      extra JVM options
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TRANS="$(cd "$HERE/.." && pwd)"                                  # .../src/test/resources/apv-a
MODULE="$(cd "$TRANS/../../../.." && pwd)"                       # .../BcsAPVServicios
WS="$(cd "$MODULE/../../.." && pwd)"                             # workspace root
LEG="${LEGACY_ROOT:-$WS/legacy/otr-sva-optimus-corredores/BSCL}"
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
OVERLAY="${LEGACY_OVERLAY:-}"
mkdir -p "$WORK/classes" "$WORK/run"

H2="$M2/com/h2database/h2/2.4.240/h2-2.4.240.jar"
GSON="$M2/com/google/code/gson/gson/2.13.2/gson-2.13.2.jar"
COLL="$M2/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar"
SEP=";"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) ;; *) SEP=":";; esac
mx() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi; }
CP="$(mx "$H2")$SEP$(mx "$GSON")$SEP$(mx "$COLL")"

APV="$LEG/APV/JAVA/BcsAPVServicios/src"
UTL="$LEG/Plataforma/JAVA/BcsUtils/src"
PLA="$LEG/Plataforma/JAVA/BcsPlataformaServicios/src"
SOC="$LEG/SocioNegocio/JAVA/BcsSocioNegocioServicios/src"
FMM="$LEG/FondoMutuo/JAVA/BcsFondoMutuoServicios/src"
SUP="$MODULE/src/test/java/cl/bcs/apv/familia_a/support"

pick() { # use the mutated copy when LEGACY_OVERLAY has one with the same file name
  local f="$1"
  if [ -n "$OVERLAY" ] && [ -f "$OVERLAY/$(basename "$f")" ]; then echo "$OVERLAY/$(basename "$f")"; else echo "$f"; fi
}

# --- legacy classes under test (family A, 19 classes + the authorization hook AutorizacionCambiarAPlanActivoContratoSAAPV)
A="$APV/cl/bcs/APV"
CLASSES="CrearContratoSAAPV GrabarContratoSAAPV ModificarContratoSAAPV CambiarEstadoContratosSAAPV CambiarEstadoEnvioNotificacionContratoSAAPV
 CambiarEstadoRevocacionSAAPV CambiarAPlanActivoContratoSAAPV CambiarAPlanActivoContratoSAAPVWrapper ValidaContratoSAAPV ConsultarClienteContratoSAAPV
 ObtenerFolioSuscripcion AsociarPerfilesAPV ModificarRelacionLaboral BusquedaContratoSAAPVRecaudacion AsignarContratoARecaudacion
 ValidarYAsignarContratosARecaudaciones CargaMasivaArchivo ConsultarCustodiaPortafolioAPV ConsultarPreferenciaPagoPortafolio
 AutorizacionCambiarAPlanActivoContratoSAAPV"
SRC_FILES=()
for c in $CLASSES; do SRC_FILES+=("$(pick "$A/$c.java")"); done
SRC_FILES+=("$(pick "$A/pojo/DatosRecaudaciones.java")")

# --- legacy utility/pojo classes of other modules used AS IS (real source, no shim)
REAL_FILES=(
  "$UTL/cl/bcs/utils/Fecha.java" "$UTL/cl/bcs/utils/StringUtils.java" "$UTL/cl/bcs/utils/BcsPojo.java"
  "$UTL/cl/bcs/utils/GsonDateDeserializer.java" "$UTL/cl/bcs/utils/GsonTimestampDeserializer.java"
  "$SOC/cl/bcs/socionegocio/pojo/Enrolamiento.java" "$SOC/cl/bcs/socionegocio/pojo/ErrorDetalle.java"
  "$SOC/cl/bcs/socionegocio/pojo/AdvertenciaDetalle.java" "$SOC/cl/bcs/socionegocio/pojo/SalidaEstandar.java"
  "$FMM/cl/bcs/fondomutuo/pojo/ConstantesFondoMutuo.java" "$FMM/cl/bcs/fondomutuo/pojo/ResultadoSolicitud.java"
  "$PLA/cl/bcs/plataforma/aut/BcsAutorizaciones.java" "$PLA/cl/bcs/plataforma/aut/pojo/Evento.java"
)

SHIMS=()
while IFS= read -r f; do SHIMS+=("$f"); done < <(find "$HERE/shims" -name '*.java')

# the verbatim copies of legacy statements used as keys of the H2 dialect overrides must still equal the legacy source (else the override would stop matching)
for f in "$TRANS"/h2-dialect/hana/*.sql; do
  [ -e "$f" ] || continue
  cmp -s "$f" "$APV/cl/bcs/APV/sql/$(basename "$f")" || { echo "h2-dialect/hana/$(basename "$f") differs from the legacy source" >&2; exit 1; }
done

: > "$WORK/sources.txt"
for f in "$SUP/H2Hana.java" "$SUP/Dump.java" "$HERE/runner/OracleRunner.java" "${SHIMS[@]}" "${REAL_FILES[@]}" "${SRC_FILES[@]}" ${EXTRA_SOURCES:-}; do
  echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"
done

if [ -n "${ORACLE_SKIP_COMPILE:-}" ] && [ -d "$WORK/classes/cl" ]; then
  echo "== reusing the classes compiled in $WORK/classes (ORACLE_SKIP_COMPILE set)"
else
  echo "== compiling oracle into $WORK/classes"
  rm -rf "$WORK/classes"; mkdir -p "$WORK/classes"
  "$JH/bin/javac" -encoding UTF-8 -proc:none -nowarn -Xlint:none -d "$(mx "$WORK/classes")" -cp "$CP" @"$(mx "$WORK/sources.txt")"
fi

echo "== recording"
cd "$WORK/run"
# the legacy source folders are on the classpath for their RESOURCES only (cl/bcs/APV/sql/*.sql, socionegocio/per/sql, fondomutuo/sql): the classes come from $WORK/classes
RUNCP="$(mx "$WORK/classes")$SEP$(mx "$APV")$SEP$(mx "$SOC")$SEP$(mx "$FMM")$SEP$(mx "$TRANS/..")$SEP$CP"
OUTOPT=""
[ -n "${ORACLE_OUT:-}" ] && OUTOPT="-Doracle.out=$(mx "$ORACLE_OUT")"
"$JH/bin/java" -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US $OUTOPT ${JAVA_OPTS:-} -cp "$RUNCP" OracleRunner "$(mx "$TRANS")" "${1:-}"
