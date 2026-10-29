FROM eclipse-temurin:25-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven
COPY pom.xml /build/pom.xml
RUN mvn -f /build/pom.xml install -N -q
COPY BcsUtils/pom.xml /build/BcsUtils/pom.xml
COPY BcsUtils/src /build/BcsUtils/src
RUN cd /build/BcsUtils && mvn install -DskipTests -q
# fondo-mutuo uses BcsTesoreriaServicios (tesoreria-service) and BcsCustodiaServicios (bcs-custodia-servicios) as IN-PROCESS libraries (their plain jars,
# installed into the ~/.m2 of the builder; the -exec jars are never used), so both are built here, as BcsTesoreriaJobs does.
COPY BcsTesoreriaServicios/pom.xml /build/BcsTesoreriaServicios/pom.xml
COPY BcsTesoreriaServicios/src /build/BcsTesoreriaServicios/src
RUN cd /build/BcsTesoreriaServicios && mvn install -DskipTests -q
COPY BcsCustodiaServicios/pom.xml /build/BcsCustodiaServicios/pom.xml
COPY BcsCustodiaServicios/src /build/BcsCustodiaServicios/src
RUN cd /build/BcsCustodiaServicios && mvn install -DskipTests -q
COPY BcsFondoMutuoServicios/pom.xml /build/BcsFondoMutuoServicios/pom.xml
COPY BcsFondoMutuoServicios/src /build/BcsFondoMutuoServicios/src
RUN cd /build/BcsFondoMutuoServicios && mvn package -DskipTests -q

FROM eclipse-temurin:25-alpine
WORKDIR /app
# The module's repackaged jar (no classifier); the *.jar.original left by the plugin does not match.
COPY --from=builder /build/BcsFondoMutuoServicios/target/bcs-fondo-mutuo-servicios-*.jar app.jar
RUN addgroup -S app && adduser -S app -G app
USER app
EXPOSE 8111
# SEC-009: liveness against the public health endpoint (busybox wget of the alpine runtime image; no token needed for /actuator/health). Port = server.port default 8111.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 CMD wget -q -O /dev/null http://localhost:8111/actuator/health || exit 1
# UTF-8 (SQL resources are read as UTF-8) and English locale: set here, never relied on from the host.
# Time zone: NOT fixed here; docker-compose sets JAVA_TOOL_OPTIONS=-Duser.timezone=UTC (same as the proven services) and it can be overridden per country
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Dfile.encoding=UTF-8", "-Duser.language=en", "-jar", "app.jar"]
