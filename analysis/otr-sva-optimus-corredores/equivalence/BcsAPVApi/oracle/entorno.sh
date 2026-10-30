#!/usr/bin/env bash
# Common environment of the BcsAPVApi oracle (sourced by the other scripts). legacy/ is only READ; everything is compiled into a TEMPORARY directory.
#   ORACLE_WORK  scratch directory (default: $TMPDIR|$TEMP/apv-oracle)      ORACLE_JAVA_HOME  JDK 21+ (default: Temurin 25 of this workstation; JAVA_HOME is ignored on purpose)      M2_REPO  maven repository
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EQ="$(cd "$HERE/.." && pwd)"                                   # analysis/otr-sva-optimus-corredores/equivalence/BcsAPVApi
WS="$(cd "$EQ/../../../.." && pwd)"                            # workspace root
LEG="$WS/legacy/otr-sva-optimus-corredores"
MODS="$WS/modernized/otr-sva-optimus-corredores"
export PYTHONUTF8=1 PYTHONDONTWRITEBYTECODE=1
export JAVA_HOME="${ORACLE_JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
WORK="${ORACLE_WORK:-${TMPDIR:-${TEMP:-/tmp}}/apv-oracle}"
PY="${PYTHON_CMD:-py -3}"
SEP=":"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";";; esac
mx() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi; }

# jars the legacy sources of the APV service (and what they pull in through the source path) need; versions are the ones already in the local repository
JARS_M2=(
  com/google/code/gson/gson/2.13.2/gson-2.13.2.jar
  org/apache/logging/log4j/log4j-api/2.24.3/log4j-api-2.24.3.jar
  commons-io/commons-io/2.11.0/commons-io-2.11.0.jar
  commons-lang/commons-lang/2.6/commons-lang-2.6.jar
  commons-beanutils/commons-beanutils/1.9.4/commons-beanutils-1.9.4.jar
  commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar
  commons-logging/commons-logging/1.2/commons-logging-1.2.jar
  commons-configuration/commons-configuration/1.2/commons-configuration-1.2.jar
  com/google/guava/guava/23.3-jre/guava-23.3-jre.jar
  commons-codec/commons-codec/1.15/commons-codec-1.15.jar
  net/sf/jasperreports/jasperreports/6.21.0/jasperreports-6.21.0.jar
  org/jfree/jfreechart/1.5.4/jfreechart-1.5.4.jar
  org/jfree/jcommon/1.0.23/jcommon-1.0.23.jar
  org/apache/poi/poi/3.17/poi-3.17.jar
  com/fasterxml/jackson/core/jackson-core/2.13.5/jackson-core-2.13.5.jar
  com/fasterxml/jackson/core/jackson-databind/2.13.5/jackson-databind-2.13.5.jar
  com/fasterxml/jackson/core/jackson-annotations/2.13.5/jackson-annotations-2.13.5.jar
  org/sql2o/sql2o/1.6.0/sql2o-1.6.0.jar
  org/apache/commons/commons-lang3/3.14.0/commons-lang3-3.14.0.jar
  jakarta/xml/soap/jakarta.xml.soap-api/3.0.2/jakarta.xml.soap-api-3.0.2.jar
  jakarta/xml/bind/jakarta.xml.bind-api/2.3.3/jakarta.xml.bind-api-2.3.3.jar
  jakarta/activation/jakarta.activation-api/1.2.2/jakarta.activation-api-1.2.2.jar
)
classpath_legacy() {
  local cp="" j
  for j in "$LEG"/libs-private/*.jar; do cp="$cp$SEP$(mx "$j")"; done
  for j in "${JARS_M2[@]}"; do
    [ -f "$M2/$j" ] || { echo "missing jar $M2/$j" >&2; return 3; }
    cp="$cp$SEP$(mx "$M2/$j")"
  done
  echo "${cp#"$SEP"}"
}
GSON_JAR="$(mx "$M2/com/google/code/gson/gson/2.13.2/gson-2.13.2.jar")"
RUNOPTS=( "-Duser.timezone=UTC" "-Dfile.encoding=UTF-8" "-Duser.language=en" "-Duser.country=US" "-Dlog4j2.StatusLogger.level=OFF" "-Dlog4j2.statusLoggerLevel=OFF" )
