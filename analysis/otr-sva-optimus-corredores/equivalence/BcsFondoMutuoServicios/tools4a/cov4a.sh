#!/usr/bin/env bash
# usage: cov4a.sh   -> branch coverage of the step-4A classes (JaCoCo core API over the exec of the jacoco agent of the VS Code Java Test extension; no pom change)
# Runs only the Normal tests of the real module tree (no clean) and writes tools4a/branch-coverage-4A.txt. CoverageReport.java is the one of 3A/3B.
export JAVA_HOME="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WS="$(cd "$HERE/../../../../.." && pwd)"
S="$HOME/AppData/Local/Temp/mvscratch/cov4a"
EXT=C:/Users/m.villagran.mora/.vscode/extensions/vscjava.vscode-java-test-0.47.0/server
M2=C:/Users/m.villagran.mora/.m2/repository
mkdir -p "$S"; rm -f "$S/fm4a.exec"
mvn -o -f "$WS/modernized/otr-sva-optimus-corredores/pom.xml" -pl :bcs-fondo-mutuo-servicios test -Dtest='NormalEquivalenciaTest,NormalCompuertasYDefectosTest,NormalTesoreriaSpotAbiertasTest,ManejadorOraculoTest' \
  -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false -Dfile.encoding=UTF-8 \
  "-DargLine=-javaagent:$EXT/jacocoagent.jar=destfile=C:/Users/m.villagran.mora/AppData/Local/Temp/mvscratch/cov4a/fm4a.exec,includes=cl.bcs.fondomutuo.*" > "$S/mvn.log" 2>&1
SW="$(cygpath -m "$S")"; WSW="$(cygpath -m "$WS")"
CP="$EXT/org.jacoco.core_0.8.15.202606040825.jar;$EXT/org.objectweb.asm.commons_9.10.1.jar;$EXT/org.objectweb.asm.tree_9.10.1.jar;$M2/org/ow2/asm/asm/9.10.1/asm-9.10.1.jar"
"$JAVA_HOME/bin/javac" -cp "$CP" -d "$SW" "$(cygpath -m "$HERE/../tools3b/CoverageReport.java")" > "$S/javac.log" 2>&1
"$JAVA_HOME/bin/java" -cp "$SW;$CP" CoverageReport "$SW/fm4a.exec" "$WSW/modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios/target/classes" "cl/bcs/fondomutuo/normal/" v > "$HERE/branch-coverage-4A.txt" 2>&1
echo COVDONE >> "$HERE/branch-coverage-4A.txt"
