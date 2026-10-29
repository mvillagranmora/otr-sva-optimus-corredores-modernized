#!/usr/bin/env bash
# Step 4A: runs the NEW CrearSolicitudAporteRescateNormal as a Subject of the oracle (same OracleRunner, H2, doubles, cases) and writes <ORACLE_OUT>/<case>.json
# in the golden format. Needs: ORACLE_WORK already filled by record.sh (classes of the oracle), the module compiled (target/classes), JAVA_HOME (JDK 25).
#   ORACLE_WORK=... ORACLE_OUT=... ./subject4a.sh [casePrefix]
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
WS="$(cd "$BASE/../../../.." && pwd)"
JH="${JAVA_HOME:-/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot}"
M2="${M2_REPO:-$HOME/.m2/repository}"
SEP=":"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";";; esac
mx() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else echo "$1"; fi; }
: "${ORACLE_WORK:?}"; : "${ORACLE_OUT:?}"
MOD="${MODULE_CLASSES:-$WS/modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios/target/classes}"
SP="$M2/org/springframework"
JARS="$(mx "$SP/spring-jdbc/7.0.9/spring-jdbc-7.0.9.jar")$SEP$(mx "$SP/spring-tx/7.0.9/spring-tx-7.0.9.jar")$SEP$(mx "$SP/spring-core/7.0.9/spring-core-7.0.9.jar")$SEP$(mx "$SP/spring-beans/7.0.9/spring-beans-7.0.9.jar")$SEP$(mx "$SP/spring-jcl/7.0.9/spring-jcl-7.0.9.jar")"
GSON="$(mx "$M2/com/google/code/gson/gson/2.13.2/gson-2.13.2.jar")"
H2="$(mx "$M2/com/h2database/h2/2.4.240/h2-2.4.240.jar")"
# F2 W1: the new Normal calls the ported ProcesarIngresoEgresoTesoreria, whose contract types are Tesoreria's (plain jar of tesoreria-service)
JARS="$JARS$SEP$(mx "$M2/cl/bcs/tesoreria-service/1.0.0-SNAPSHOT/tesoreria-service-1.0.0-SNAPSHOT.jar")"
SUB="$ORACLE_WORK/subject4a"
rm -rf "$SUB"; mkdir -p "$SUB"
"$JH/bin/javac" -encoding UTF-8 -proc:none -nowarn -Xlint:none -d "$(mx "$SUB")" -cp "$(mx "$ORACLE_WORK/classes")$SEP$(mx "$MOD")$SEP$GSON$SEP$JARS" "$(mx "$HERE/subject4a/cl/bcs/oracle/NormalSubject.java")"
export ORACLE_SKIP_COMPILE=1 ORACLE_SUBJECT=cl.bcs.oracle.NormalSubject
export ORACLE_EXTRA_CP="$(mx "$SUB")$SEP$(mx "$MOD")$SEP$JARS"
"$HERE/record.sh" "${1:-FMM-NOR}"
