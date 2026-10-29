# ============================================================
# Stage 1: Build – Gradle 6.3 + JDK 11 on Alpine
# Compila PLA.war y PLUGIN_OPTIMUS.jar usando Gradle
# (reemplaza BcsJarOptimusTomEE8.xml + BcsPlataformaWebTomEE8.xml)
#
# Se usa JDK 11 en el stage de compilación:
#   • JDK 8 en Alpine no soporta los cipher suites TLS modernos de
#     Maven Central ni services.gradle.org (SSLHandshakeException).
#   • El bytecode generado es Java 8 (class file version 52) gracias a
#     sourceCompatibility = 1.8 / targetCompatibility = 1.8 en build.gradle.
# El runtime (Stage 2) usa JRE 8 — la versión Java del aplicativo no cambia.
# ============================================================
FROM eclipse-temurin:11-jdk-alpine AS builder

WORKDIR /repo

# Copiar árbol fuente completo (todos los módulos BSCL + build files)
COPY . .

# Dar permisos de ejecución al wrapper (puede perderse en COPY)
RUN chmod +x gradlew

# Compilar PLUGIN_OPTIMUS.jar, PLA.war y recolectar runtime JARs
RUN ./gradlew :pluginJar :plaWar :collectRuntimeLibs --no-daemon \
        -Dfile.encoding=UTF-8

# ============================================================
# Stage 2: Runtime – TomEE 8.0.16 plume sobre JRE Alpine
# plume incluye soporte CXF/JAX-WS para los servicios SOAP
# ============================================================
FROM eclipse-temurin:8-jre-alpine AS runtime

ENV TOMEE_VERSION=8.0.16
ENV TOMEE_HOME=/opt/tomee
ENV CATALINA_HOME=${TOMEE_HOME}
ENV PATH="${TOMEE_HOME}/bin:${PATH}"

# Descargar TomEE 8 plume desde el archivo oficial Apache
RUN apk add --no-cache curl tar && \
    mkdir -p ${TOMEE_HOME} && \
    curl -fSL \
      "https://archive.apache.org/dist/tomee/tomee-${TOMEE_VERSION}/apache-tomee-${TOMEE_VERSION}-plume.tar.gz" \
      -o /tmp/tomee.tar.gz && \
    tar xzf /tmp/tomee.tar.gz -C ${TOMEE_HOME} --strip-components=1 && \
    rm -f /tmp/tomee.tar.gz && \
    apk del curl tar

# WAR en contexto /PLA — los filtros Java (SesionFilter, ServiceFilter) validan
# URIs con prefijo /PLA/ (noAuthURLs: /PLA/CrearSesion, /PLA/bootstrap, etc.).
# En producción un Apache httpd reescribe /BSCL/Plataforma/GWJAVA/PLA/ → /PLA/.
# En Docker ese rol lo cumple corredores-nginx (docker-compose, puerto 8081).
COPY --from=builder /repo/build/libs/PLA.war \
     ${TOMEE_HOME}/webapps/PLA.war

# Desplegar PLUGIN_OPTIMUS.jar en el lib compartido de TomEE
# (classloader compartido entre el WAR y el plugin)
COPY --from=builder /repo/build/libs/PLUGIN_OPTIMUS.jar \
     ${TOMEE_HOME}/lib/PLUGIN_OPTIMUS.jar

# Copiar runtime JARs al subdirectorio lib/lib de TomEE.
# Class-Path en PLUGIN_OPTIMUS.jar referencia "lib/<jar>.jar" →
# TomEE resuelve relativo a su lib/ → /opt/tomee/lib/lib/<jar>.jar
RUN mkdir -p ${TOMEE_HOME}/lib/lib
COPY --from=builder /repo/build/runtime-libs/ ${TOMEE_HOME}/lib/lib/

# Configuración de datasource HANA (tomee.xml referencia propiedades JVM)
COPY docker/tomee.xml ${TOMEE_HOME}/conf/tomee.xml

# setenv.sh: mapea env vars del contenedor a system properties JVM
# TomEE lo ejecuta automáticamente antes de arrancar Catalina
COPY docker/setenv.sh ${TOMEE_HOME}/bin/setenv.sh
RUN chmod +x ${TOMEE_HOME}/bin/setenv.sh

EXPOSE 8080

CMD ["catalina.sh", "run"]
