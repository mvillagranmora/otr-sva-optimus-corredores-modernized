#!/usr/bin/env bash
# Re-records apv-jobs/cases/*/expected.json by EXECUTING the unmodified legacy job classes of BcsAPVJobs (and the unmodified BcsJob.run()).
#
#   ./record.sh [caseNamePrefix]            e.g.  ./record.sh APVJ-CMP-0   (default: every case)
#
# Needs JDK 25 (JAVA_HOME or the Eclipse Adoptium path below) and the jars in ~/.m2 (h2, gson).
# Never writes into legacy/ or into modernized/ except cases/<case>/expected.json: all compiled output goes to $ORACLE_WORK
# (default: a fresh temporary directory). Nothing here contacts HANA, a service or a real credential.
#
# Knobs:  LEGACY_ROOT    legacy tree root (default: <workspace>/legacy/otr-sva-optimus-corredores/BSCL); read only
#         ORACLE_WORK    scratch directory. On Windows keep it SHORT (e.g. a `subst Q:` drive): javac output paths over 260 chars fail
#         LEGACY_OVERLAY directory with MUTATED copies of legacy sources (same file name) used instead of the real ones (mutation check of the goldens);
#                        together with ORACLE_OUT (directory for <case>.expected.json) so cases/ is left untouched
#         ORACLE_SKIP_COMPILE  reuse the classes already compiled in ORACLE_WORK
#         JAVA_OPTS      extra JVM options (-Doracle.debug=true shows the legacy stdout/stderr)
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RES="$(cd "$HERE/.." && pwd)"                                    # .../src/test/resources/apv-jobs
MODULE="$(cd "$RES/../../../.." && pwd)"                         # .../BcsAPVJobs
WS="$(cd "$MODULE/../../.." && pwd)"                             # workspace root
LEG="${LEGACY_ROOT:-$WS/legacy/otr-sva-optimus-corredores/BSCL}"
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
OVERLAY="${LEGACY_OVERLAY:-}"
mkdir -p "$WORK/classes" "$WORK/run"

H2="$M2/com/h2database/h2/2.4.240/h2-2.4.240.jar"
GSON="$M2/com/google/code/gson/gson/2.13.2/gson-2.13.2.jar"
SEP=";"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) ;; *) SEP=":";; esac
mx() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi; }
CP="$(mx "$H2")$SEP$(mx "$GSON")"

JOBS="$LEG/APV/JAVA/BcsAPVJobs/src"
UTL="$LEG/Plataforma/JAVA/BcsUtils/src"
PLA="$LEG/Plataforma/JAVA/BcsPlataformaServicios/src"
FMM="$LEG/FondoMutuo/JAVA/BcsFondoMutuoServicios/src"
MGR="$LEG/Plataforma/JAVA/BcsJobManager/src"
SUP="$MODULE/src/test/java/cl/bcs/apv/jobs/support"

pick() { # use the mutated copy when LEGACY_OVERLAY has one with the same file name
  local f="$1"
  if [ -n "$OVERLAY" ] && [ -f "$OVERLAY/$(basename "$f")" ]; then echo "$OVERLAY/$(basename "$f")"; else echo "$f"; fi
}

# --- legacy classes under test (the four jobs) and the legacy framework (BcsJob.run)
J="$JOBS/cl/bcs/APV/jobs"
SRC_FILES=()
for c in BcsJobCargaMasivaPrevired IngresarOrdenBonificacionFiscal IngresarOrdenFMBonificacionFiscal IngresarOrdenRVBonificacionFiscal; do SRC_FILES+=("$(pick "$J/$c.java")"); done
SRC_FILES+=("$(pick "$MGR/cl/bcs/scheduler/BcsJob.java")" "$(pick "$MGR/cl/bcs/scheduler/BcsJobListener.java")")

# --- legacy utility/pojo classes used AS IS (real source, no shim)
REAL_FILES=(
  "$UTL/cl/bcs/utils/BcsPojo.java" "$UTL/cl/bcs/utils/GsonDateDeserializer.java" "$UTL/cl/bcs/utils/GsonTimestampDeserializer.java"
  "$PLA/cl/bcs/plataforma/pla/utils/MonedaFormato.java" "$PLA/cl/bcs/plataforma/pla/utils/FileLoader.java" "$PLA/cl/bcs/plataforma/bat/AgregarLogPrograma.java"
  "$FMM/cl/bcs/fondomutuo/pojo/ConstantesFondoMutuo.java" "$FMM/cl/bcs/fondomutuo/pojo/ResultadoSolicitud.java" "$FMM/cl/bcs/fondomutuo/pojo/SerieFondoSolicitudes.java"
)

SHIMS=()
while IFS= read -r f; do SHIMS+=("$f"); done < <(find "$HERE/shims" -name '*.java')

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
# the legacy source folders are on the classpath for their RESOURCES only (the .sql files): the classes come from $WORK/classes
RUNCP="$(mx "$WORK/classes")$SEP$(mx "$JOBS")$SEP$(mx "$FMM")$SEP$(mx "$PLA")$SEP$CP"
OUTOPT=""
[ -n "${ORACLE_OUT:-}" ] && OUTOPT="-Doracle.out=$(mx "$ORACLE_OUT")"
"$JH/bin/java" -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US $OUTOPT ${JAVA_OPTS:-} -cp "$RUNCP" OracleRunner "$(mx "$RES")" "${1:-}"
