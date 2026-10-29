#!/usr/bin/env bash
# F2 W1 front 2: JaCoCo branch coverage of the package cl/bcs/fondomutuo/rescatereinversion, same offline agent approach as tools5/cov5.sh (agent of the
# VS Code Java Test extension, JaCoCo core API reporting through tools3b/CoverageReport.java, no pom change). Tests: the RescateReinversion tests.
#   tools-w1b-rre/cov_rre.sh [outFile]
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"   # the host default is JDK 11
export JAVA_TOOL_OPTIONS="-Duser.timezone=UTC -Dfile.encoding=UTF-8"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WS="$(cd "$HERE/../../../../.." && pwd)"
OUTF="${1:-$HERE/branch-coverage-rre.txt}"
S="$HOME/AppData/Local/Temp/mvscratch/covrre"
EXT=C:/Users/m.villagran.mora/.vscode/extensions/vscjava.vscode-java-test-0.47.0/server
M2=C:/Users/m.villagran.mora/.m2/repository
mkdir -p "$S"; rm -f "$S/rre.exec"
mvn -o -f "$WS/modernized/otr-sva-optimus-corredores/pom.xml" -pl :bcs-fondo-mutuo-servicios test \
  -Dtest='Rre*Test,ManejadorRescateReinversionTest' -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false -Dfile.encoding=UTF-8 \
  "-DargLine=-javaagent:$EXT/jacocoagent.jar=destfile=C:/Users/m.villagran.mora/AppData/Local/Temp/mvscratch/covrre/rre.exec,includes=cl.bcs.fondomutuo.*" > "$S/mvn.log" 2>&1
SW="$(cygpath -m "$S")"; WSW="$(cygpath -m "$WS")"
CP="$EXT/org.jacoco.core_0.8.15.202606040825.jar;$EXT/org.objectweb.asm.commons_9.10.1.jar;$EXT/org.objectweb.asm.tree_9.10.1.jar;$M2/org/ow2/asm/asm/9.10.1/asm-9.10.1.jar"
"$JAVA_HOME/bin/javac" -cp "$CP" -d "$SW" "$(cygpath -m "$HERE/../tools3b/CoverageReport.java")" > "$S/javac.log" 2>&1
CLS="$WSW/modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios/target/classes"
{ grep -a -E "Tests run:.*Fail" "$S/mvn.log" | tail -n 1
  echo "== package rescatereinversion"; "$JAVA_HOME/bin/java" -cp "$SW;$CP" CoverageReport "$SW/rre.exec" "$CLS" "cl/bcs/fondomutuo/rescatereinversion/" v
  echo COVDONE; } > "$OUTF" 2>&1
tail -40 "$OUTF"
