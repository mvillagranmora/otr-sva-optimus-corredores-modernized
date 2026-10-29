#!/usr/bin/env bash
# Oracle for cl.bcs.plataforma.cre.ConsultarEstadoModulo (JavaCall 600017): compiles the REAL legacy sources into a temporary directory
# (never inside legacy/) and runs them on in-memory H2 seeded per case.
#
#   ./record.sh [caseNamePrefix]            re-records cases/<case>/expected.json (the goldens) from the legacy   (default: every case)
#   ORACLE_SUBJECT=<FQCN> ORACLE_OUT=<dir> ./record.sh      run ANOTHER subject (the new implementation, or cl.bcs.oracle.MutantSubject)
#                                                           and dump <dir>/<case>.json in the golden format (same cases, same dump)
#
# Real legacy sources used AS IS: cre/ConsultarEstadoModulo, BcsUtils cl/bcs/utils/{SQL,NamedParameterStatement,JavaCall,
# NamedCallableStatement,GsonTimestampDeserializer,GsonDateDeserializer}. Shims: datadog.trace.api.Trace (annotation only) and
# org.apache.commons.dbcp2.BasicDataSource (compile-only, referenced by the legacy main(); always throws). Real jars from ~/.m2: h2, gson,
# commons-io, commons-beanutils, commons-collections, commons-logging, log4j-api. Java 11 source level (--release 11), run with JDK 25.
# Knobs: ORACLE_WORK scratch dir (default a fresh temp dir) | LEGACY_OVERLAY dir with MUTATED copies of legacy sources (same file name),
#        used instead of the real ones (mutation check of the goldens) | ORACLE_MUTANT mutant name | ORACLE_SUBJECT | ORACLE_OUT | JAVA_OPTS.
# No HANA, no network, no credential: nothing here opens a connection other than jdbc:h2:mem.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"                       # .../equivalence/BcsPlataformaServicios
WS="$(cd "$BASE/../../../.." && pwd)"                # workspace root
LEG="$WS/legacy/otr-sva-optimus-corredores/BSCL/Plataforma/JAVA"
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
WORK="${ORACLE_WORK:-$(mktemp -d)}"
OVERLAY="${LEGACY_OVERLAY:-}"

SEP=":"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";";; esac
mx() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi; }
CP=""
for j in "com/h2database/h2/2.4.240/h2-2.4.240.jar" "com/google/code/gson/gson/2.13.2/gson-2.13.2.jar" \
         "commons-io/commons-io/2.11.0/commons-io-2.11.0.jar" "commons-beanutils/commons-beanutils/1.9.4/commons-beanutils-1.9.4.jar" \
         "commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar" "commons-logging/commons-logging/1.2/commons-logging-1.2.jar" \
         "org/apache/logging/log4j/log4j-api/2.24.3/log4j-api-2.24.3.jar"; do
  [ -f "$M2/$j" ] || { echo "missing jar $M2/$j" >&2; exit 3; }
  CP="${CP:+$CP$SEP}$(mx "$M2/$j")"
done

pick() { # use the mutated copy when LEGACY_OVERLAY has one with the same file name
  local f="$1"
  if [ -n "$OVERLAY" ] && [ -f "$OVERLAY/$(basename "$f")" ]; then echo "$OVERLAY/$(basename "$f")"; else echo "$f"; fi
}

UTL="$LEG/BcsUtils/src/cl/bcs/utils"
SRC=( "$(pick "$LEG/BcsPlataformaServicios/src/cl/bcs/plataforma/cre/ConsultarEstadoModulo.java")"
      "$(pick "$UTL/SQL.java")" "$(pick "$UTL/NamedParameterStatement.java")" "$(pick "$UTL/JavaCall.java")"
      "$UTL/GsonTimestampDeserializer.java" "$UTL/GsonDateDeserializer.java" "$UTL/NamedCallableStatement.java"
      "$HERE/shims/datadog/trace/api/Trace.java" "$HERE/shims/org/apache/commons/dbcp2/BasicDataSource.java"
      "$HERE/runner/OracleRunner.java" "$HERE/runner/cl/bcs/oracle/Subject.java" "$HERE/runner/cl/bcs/oracle/LegacySubject.java"
      "$HERE/runner/cl/bcs/oracle/MutantSubject.java" )
mkdir -p "$WORK/classes" "$WORK/run"
: > "$WORK/sources.txt"
for f in "${SRC[@]}"; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done
EXTRA="${ORACLE_EXTRA_SOURCES:-}"   # sources of the new implementation adapter (a cl.bcs.oracle.Subject), when pointing the harness at it
for f in $EXTRA; do echo "\"$(mx "$f")\"" >> "$WORK/sources.txt"; done

echo "== compiling oracle into $WORK/classes (release 11)"
rm -rf "$WORK/classes"; mkdir -p "$WORK/classes"
"$JH/bin/javac" --release 11 -encoding UTF-8 -proc:none -nowarn -Xlint:none -d "$(mx "$WORK/classes")" -cp "$CP" @"$(mx "$WORK/sources.txt")"

echo "== running"
cd "$WORK/run"
OPTS=( "-Duser.timezone=UTC" "-Dfile.encoding=UTF-8" "-Dlog4j2.StatusLogger.level=OFF" "-Dlog4j2.statusLoggerLevel=OFF" )
[ -n "${ORACLE_OUT:-}" ] && OPTS+=( "-Doracle.out=$(mx "$ORACLE_OUT")" )
[ -n "${ORACLE_SUBJECT:-}" ] && OPTS+=( "-Doracle.subject=$ORACLE_SUBJECT" )
[ -n "${ORACLE_MUTANT:-}" ] && OPTS+=( "-Doracle.mutant=$ORACLE_MUTANT" )
RUNCP="$(mx "$WORK/classes")$SEP$CP"
# ORACLE_EXTRA_CP: directory/jar of the NEW implementation (RUN classpath only; not needed to compile because the subject uses reflection)
[ -n "${ORACLE_EXTRA_CP:-}" ] && RUNCP="$RUNCP$SEP$(mx "$ORACLE_EXTRA_CP")"
"$JH/bin/java" "${OPTS[@]}" ${JAVA_OPTS:-} -cp "$RUNCP" OracleRunner "$(mx "$BASE")" "${1:-}"
