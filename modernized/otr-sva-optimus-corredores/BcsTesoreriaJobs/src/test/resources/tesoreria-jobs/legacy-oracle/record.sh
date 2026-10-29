#!/usr/bin/env bash
# Re-records tesoreria-jobs/cases/*/expected.json by EXECUTING the unmodified legacy BcsTesoreriaJobs classes under the real legacy BcsJob.run().
#
#   ./record.sh [caseNamePrefix]            e.g.  ./record.sh CM-      (default: every case)
#
# Needs JDK 25 (JAVA_HOME or the Eclipse Adoptium path below) and the jars in ~/.m2 (h2, gson, commons-lang3).
# Never writes into legacy/ or into the module except cases/<case>/expected.json: all compiled output goes to $ORACLE_WORK (default: a fresh
# temporary directory; on Windows pass a SHORT path or map a drive with `subst` if paths exceed 260 characters).
# Nothing here contacts HANA, a bank or any real service; no credential of the legacy code is used.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RES="$(cd "$HERE/.." && pwd)"                                    # .../src/test/resources/tesoreria-jobs
MODULE="$(cd "$RES/../../../.." && pwd)"                         # .../BcsTesoreriaJobs
WS="$(cd "$MODULE/../../.." 2>/dev/null && pwd || true)"         # workspace root (not needed when LEGACY_BSCL is given)
LEG="${LEGACY_BSCL:-$WS/legacy/otr-sva-optimus-corredores/BSCL}"
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
mkdir -p "$WORK/classes"

H2="$M2/com/h2database/h2/2.4.240/h2-2.4.240.jar"
GSON="$M2/com/google/code/gson/gson/2.13.2/gson-2.13.2.jar"
LANG3="$(ls "$M2"/org/apache/commons/commons-lang3/*/commons-lang3-*.jar 2>/dev/null | grep -v sources | tail -1)"
SEP=";"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) ;; *) SEP=":";; esac
mx() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi; }
CP="$(mx "$H2")$SEP$(mx "$GSON")$SEP$(mx "$LANG3")"

JOBS="$LEG/Tesoreria/JAVA/BcsTesoreriaJobs/src"
UTL="$LEG/Plataforma/JAVA/BcsUtils/src"
PLA="$LEG/Plataforma/JAVA/BcsPlataformaServicios/src"
FMM="$LEG/FondoMutuo/JAVA/BcsFondoMutuoServicios/src"
SCH="$LEG/Plataforma/JAVA/BcsJobManager/src"
SUP="$MODULE/src/test/java/cl/bcs/tesoreria/jobs/support"

# legacy classes compiled AS IS: the whole jobs module, the real BcsJob framework class and the pure utility classes
JOB_FILES=()
while IFS= read -r f; do JOB_FILES+=("$f"); done < <(find "$JOBS" -name '*.java')
REAL_FILES=("$SCH/cl/bcs/scheduler/BcsJob.java" "$SCH/cl/bcs/scheduler/BcsJobListener.java"
  "$UTL/cl/bcs/utils/Fecha.java" "$UTL/cl/bcs/utils/StringUtils.java" "$UTL/cl/bcs/utils/BcsPojo.java"
  "$UTL/cl/bcs/utils/GsonDateDeserializer.java" "$UTL/cl/bcs/utils/GsonTimestampDeserializer.java")
SHIMS=()
while IFS= read -r f; do SHIMS+=("$f"); done < <(find "$HERE/shims" -name '*.java')

echo "== compiling oracle into $WORK/classes"
"$JH/bin/javac" -encoding UTF-8 -proc:none -nowarn -Xlint:none -d "$(mx "$WORK/classes")" -cp "$CP" \
  "$SUP/H2Hana.java" "$SUP/Dump.java" "$SUP/Archivos.java" "$SUP/Canon.java" "$HERE"/runner/*.java \
  "${SHIMS[@]}" "${REAL_FILES[@]}" "${JOB_FILES[@]}"

echo "== recording"
mkdir -p "$WORK/run"; cd "$WORK/run"
# the legacy classes load their .sql resources from the classpath: the jobs source tree and the two library trees that hold the FTP insert statements
RUNCP="$(mx "$WORK/classes")$SEP$(mx "$JOBS")$SEP$(mx "$PLA")$SEP$(mx "$FMM")$SEP$CP"
"$JH/bin/java" -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US ${JAVA_OPTS:-} -cp "$RUNCP" OracleRunner "$(mx "$RES")" "${1:-}"
