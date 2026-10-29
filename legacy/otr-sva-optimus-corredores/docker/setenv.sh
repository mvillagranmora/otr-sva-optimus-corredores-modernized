#!/bin/sh
# Mapea variables de ambiente del contenedor a system properties JVM.
# TomEE/Catalina ejecuta este script automáticamente antes de arrancar.

JVM_XMS="${JVM_XMS:-512m}"
JVM_XMX="${JVM_XMX:-2048m}"

HANA_JDBC_URL="${HANA_JDBC_URL:-jdbc:sap://localhost:30441/?currentschema=BSCL}"
HANA_USER="${HANA_USER:-SOPORTE}"
HANA_PASS="${HANA_PASS:-Bolsa123}"

export CATALINA_OPTS="\
  -Xms${JVM_XMS} \
  -Xmx${JVM_XMX} \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Dfile.encoding=UTF-8 \
  -DHANA_JDBC_URL=${HANA_JDBC_URL} \
  -DHANA_USER=${HANA_USER} \
  -DHANA_PASS=${HANA_PASS}"
