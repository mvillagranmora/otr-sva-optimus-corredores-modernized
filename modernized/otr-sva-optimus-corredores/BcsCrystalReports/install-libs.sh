#!/usr/bin/env bash
# One-time local setup so a normal `mvn -o` build works (the proprietary jars are not on any repository).
# Installs libs/*.jar (gitignored) into ~/.m2 with the same coordinates the Dockerfile uses, plus the two
# non-proprietary jars (icu4j 71.1, commons-configuration 1.2) that are not cached in ~/.m2 but are in the gradle cache.
# Usage (from anywhere):  JAVA_HOME=<jdk11> bash install-libs.sh
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
LIBS="$HERE/libs"
cd "$(mktemp -d)"   # no pom here: avoids the Boot-pinned plugin versions
MVN="mvn -o -q org.apache.maven.plugins:maven-install-plugin:3.1.4:"  # 3.1.4 is the install-plugin version complete in ~/.m2 (2.5.2 pinned by Boot is not)

inst() { # file group artifact version
  [ -f "$1" ] || { echo "MISSING $1" >&2; exit 1; }
  local t="$HOME/.m2/repository/$(echo "$2" | tr . /)/$3/$4/$3-$4.jar"
  if [ -f "$t" ] && cmp -s "$1" "$t"; then echo "already installed $2:$3:$4"; return 0; fi
  # (re-installing over an existing different jar fails on Windows: delete the old one first)
  $MVN""install-file -Dfile="$1" -DgroupId="$2" -DartifactId="$3" -Dversion="$4" -Dpackaging=jar
  echo "installed $2:$3:$4"
}

for a in CrystalReportsRuntime CrystalCommon2 webreporting JDBInterface DatabaseConnectors pfjgraphics cvom logging keycodeDecoder; do
  inst "$LIBS/$a.jar" com.crystaldecisions "$a" 14.1
done
inst "$LIBS/log4j.jar" com.crystaldecisions log4j 1.2.6
[ -f "$HOME/.m2/repository/net/sf/barcode4j/barcode4j/2.1/barcode4j-2.1.jar" ] || inst "$LIBS/barcode4j.jar" net.sf.barcode4j barcode4j 2.1   # already on Central/.m2 normally
inst "$LIBS/avalon-framework-4.2.0.jar" avalon-framework avalon-framework 4.2.0

# Non-proprietary: Maven Central has them (Docker build downloads them); offline they come from the gradle cache.
GC="${GRADLE_CACHE:-$HOME/.gradle/caches/modules-2/files-2.1}"
ICU="$(find "$GC/com.ibm.icu/icu4j/71.1" -name 'icu4j-71.1.jar' | head -1)"
CC="$(find "$GC/commons-configuration/commons-configuration/1.2" -name 'commons-configuration-1.2.jar' | head -1)"
[ -f "$HOME/.m2/repository/com/ibm/icu/icu4j/71.1/icu4j-71.1.jar" ] || inst "$ICU" com.ibm.icu icu4j 71.1
[ -f "$HOME/.m2/repository/commons-configuration/commons-configuration/1.2/commons-configuration-1.2.jar" ] || inst "$CC" commons-configuration commons-configuration 1.2

# surefire 3.5.6 needs junit-platform-launcher == the Jupiter platform version (1.8.2 with Spring Boot 2.7.18); ~/.m2 only has
# 1.12.2/6.0.3 (incompatible with Jupiter 5.8.2). Gradle 8.12's distribution ships the unmodified 1.8.2 jar. Not needed with network access.
if [ ! -f "$HOME/.m2/repository/org/junit/platform/junit-platform-launcher/1.8.2/junit-platform-launcher-1.8.2.jar" ]; then
  LJ="$(find "$HOME/.gradle/wrapper/dists" -name 'junit-platform-launcher-1.8.2.jar' | head -1)"
  inst "$LJ" org.junit.platform junit-platform-launcher 1.8.2
fi
