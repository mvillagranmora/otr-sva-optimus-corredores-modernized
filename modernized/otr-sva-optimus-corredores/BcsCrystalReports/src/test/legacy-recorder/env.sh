#!/bin/bash
# Sourced by record.sh / probe.sh. Builds the LEGACY engine from the unmodified legacy sources and exports CP_LEGACY.
# Needs: JAVA_HOME = JDK 11, libs/ (proprietary jars, gitignored) and ~/.m2 (poi 3.17, sql2o, gson, log4j 2.17.1, h2, ...).
set -e
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOD="$(cd "$HERE/../../.." && pwd)"
REPO="$(cd "$MOD/../../.." && pwd)"
LEG="$REPO/legacy/otr-sva-optimus-corredores/BSCL/Plataforma/JAVA"
API="$LEG/BcsPlataformaReporteApi/src/cl/bcs/plataforma/report/api"
LMRS="$LEG/BcsUtils/src/cl/bcs/utils/ListMapResultSet.java"
M2="${M2_REPO:-$HOME/.m2/repository}"
OUT="$MOD/target/legacy-recorder"          # compiled legacy classes + recorder (never inside legacy/)
export JAVA_HOME="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-11.0.32.101-hotspot}"
JAVAC="$JAVA_HOME/bin/javac"; JAVA="$JAVA_HOME/bin/java"
w() { cygpath -w "$1" 2>/dev/null || echo "$1"; }

J=()
for f in CrystalReportsRuntime CrystalCommon2 webreporting JDBInterface DatabaseConnectors pfjgraphics cvom \
         avalon-framework-4.2.0 logging keycodeDecoder log4j barcode4j; do J+=("$MOD/libs/$f.jar"); done
J+=("$M2/org/apache/poi/poi/3.17/poi-3.17.jar" "$M2/org/sql2o/sql2o/1.6.0/sql2o-1.6.0.jar"
    "$M2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
    "$M2/org/apache/logging/log4j/log4j-api/2.17.1/log4j-api-2.17.1.jar"
    "$M2/org/apache/logging/log4j/log4j-core/2.17.1/log4j-core-2.17.1.jar"
    "$M2/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar"
    "$M2/commons-configuration/commons-configuration/1.2/commons-configuration-1.2.jar"
    "$M2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar"
    "$M2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar"
    "$M2/com/ibm/icu/icu4j/71.1/icu4j-71.1.jar" "$M2/com/h2database/h2/2.1.214/h2-2.1.214.jar")
CP_LEGACY=""
for j in "${J[@]}"; do
  [ -f "$j" ] || { echo "MISSING jar: $j" >&2; exit 2; }
  CP_LEGACY="$CP_LEGACY$(w "$j");"
done
export CP_LEGACY

ADD="--add-exports java.sql.rowset/com.sun.rowset=ALL-UNNAMED --add-exports java.sql.rowset/com.sun.rowset.internal=ALL-UNNAMED --add-opens java.sql.rowset/com.sun.rowset=ALL-UNNAMED"
JVMFLAGS="$ADD -Djava.awt.headless=true -Dfile.encoding=UTF-8"

build_legacy() {
  mkdir -p "$OUT/classes"
  local SRC=("$API/ReportDocument.java" "$API/SubreportDocument.java" "$API/JsonResultSet.java" "$LMRS")
  for f in "$HERE"/*.java; do SRC+=("$f"); done
  # dependency-free test classes shared with the sidecar test
  local T="$MOD/src/test/java/cl/bcs/reports"
  for f in equiv/EquivCase equiv/H2Shim equiv/Masks equiv/XlsWindows equiv/PdfStructure; do SRC+=("$T/$f.java"); done
  local WS=(); for f in "${SRC[@]}"; do WS+=("$(w "$f")"); done
  "$JAVAC" -nowarn -encoding UTF-8 $ADD -cp "$CP_LEGACY" -d "$(w "$OUT/classes")" "${WS[@]}"
}
