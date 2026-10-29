#!/usr/bin/env bash
# usage: cov5.sh [outFile]   Step 5 branch coverage of the package cl/bcs/fondomutuo/normal (and, second report, manejador): same offline JaCoCo agent approach as
# tools4a/cov4a.sh (agent of the VS Code Java Test extension, JaCoCo core API reporting, no pom change), tests = the Normal + Manejador oracle tests.
# Runs on the REAL module tree without clean; writes the report to <outFile> (default tools5/branch-coverage-5.txt).
export JAVA_HOME="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WS="$(cd "$HERE/../../../../.." && pwd)"
OUTF="${1:-$HERE/branch-coverage-5.txt}"
S="$HOME/AppData/Local/Temp/mvscratch/cov5"
EXT=C:/Users/m.villagran.mora/.vscode/extensions/vscjava.vscode-java-test-0.47.0/server
M2=C:/Users/m.villagran.mora/.m2/repository
mkdir -p "$S"; rm -f "$S/fm5.exec"
mvn -o -f "$WS/modernized/otr-sva-optimus-corredores/pom.xml" -pl :bcs-fondo-mutuo-servicios test \
  -Dtest='NormalEquivalenciaTest,NormalCompuertasYDefectosTest,NormalTesoreriaSpotAbiertasTest,ManejadorOraculoTest' \
  -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false -Dfile.encoding=UTF-8 \
  "-DargLine=-javaagent:$EXT/jacocoagent.jar=destfile=C:/Users/m.villagran.mora/AppData/Local/Temp/mvscratch/cov5/fm5.exec,includes=cl.bcs.fondomutuo.*" > "$S/mvn.log" 2>&1
SW="$(cygpath -m "$S")"; WSW="$(cygpath -m "$WS")"
CP="$EXT/org.jacoco.core_0.8.15.202606040825.jar;$EXT/org.objectweb.asm.commons_9.10.1.jar;$EXT/org.objectweb.asm.tree_9.10.1.jar;$M2/org/ow2/asm/asm/9.10.1/asm-9.10.1.jar"
"$JAVA_HOME/bin/javac" -cp "$CP" -d "$SW" "$(cygpath -m "$HERE/../tools3b/CoverageReport.java")" > "$S/javac.log" 2>&1
CLS="$WSW/modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios/target/classes"
{ grep -E "Tests run:.*Fail" "$S/mvn.log" | tail -n 1
  echo "== package normal"; "$JAVA_HOME/bin/java" -cp "$SW;$CP" CoverageReport "$SW/fm5.exec" "$CLS" "cl/bcs/fondomutuo/normal/" v
  echo "== package manejador"; "$JAVA_HOME/bin/java" -cp "$SW;$CP" CoverageReport "$SW/fm5.exec" "$CLS" "cl/bcs/fondomutuo/manejador/" v
  echo COVDONE; } > "$OUTF" 2>&1
