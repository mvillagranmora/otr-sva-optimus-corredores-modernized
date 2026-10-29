# bcs-cuenta-inversion-jobs uses BcsCuentaInversionServicios as a library (its plain jar), so both are built here.
FROM eclipse-temurin:25-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven
COPY pom.xml /build/pom.xml
RUN mvn -f /build/pom.xml install -N -q
COPY BcsUtils/pom.xml /build/BcsUtils/pom.xml
COPY BcsUtils/src /build/BcsUtils/src
RUN cd /build/BcsUtils && mvn install -DskipTests -q
COPY BcsCuentaInversionServicios/pom.xml /build/BcsCuentaInversionServicios/pom.xml
COPY BcsCuentaInversionServicios/src /build/BcsCuentaInversionServicios/src
RUN cd /build/BcsCuentaInversionServicios && mvn install -DskipTests -q
COPY BcsCuentaInversionJobs/pom.xml /build/BcsCuentaInversionJobs/pom.xml
COPY BcsCuentaInversionJobs/src /build/BcsCuentaInversionJobs/src
RUN cd /build/BcsCuentaInversionJobs && mvn package -DskipTests -q

FROM eclipse-temurin:25-alpine
WORKDIR /app
COPY --from=builder /build/BcsCuentaInversionJobs/target/bcs-cuenta-inversion-jobs-*.jar app.jar
EXPOSE 8075
ENTRYPOINT ["java", "-jar", "app.jar"]
