#!/bin/sh
# Container side. Compiles the unmodified legacy classes from /legacy and runs LegacyOracle.  Args: <inputs.tsv> <outDir>
set -eu
M=/root/.m2/repository
CP="$M/commons-beanutils/commons-beanutils/1.9.4/commons-beanutils-1.9.4.jar:$M/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:$M/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:$M/org/apache/logging/log4j/log4j-api/2.24.3/log4j-api-2.24.3.jar:/opt/hana/lib/ngdbc.jar"
L=/legacy/BSCL; U=$L/Plataforma/JAVA/BcsUtils/src/cl/bcs/utils; S=/work/hana-jdbc/oracle
mkdir -p /tmp/orc && javac -nowarn -d /tmp/orc -cp "$CP" $S/shims/datadog/trace/api/Trace.java $S/shims/cl/bcs/utils/BcsPojo.java \
  $U/SQL.java $U/NamedParameterStatement.java $U/NamedCallableStatement.java \
  $L/Corredor/SURA/JAVA/Servicios/src/cl/bcs/sura/socionegocio/pojo/IdsEntidadYPerfiles.java 2>&1 | grep -v '^Note:' || true
exec java -cp "/tmp/orc:$CP" $S/LegacyOracle.java "$@"
