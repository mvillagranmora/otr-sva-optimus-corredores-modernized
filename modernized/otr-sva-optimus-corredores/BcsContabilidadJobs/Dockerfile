# bcs-contabilidad-jobs uses BcsContabilidadServicios as a library (its plain jar), so both are built here.
FROM eclipse-temurin:25-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven
COPY pom.xml /build/pom.xml
RUN mvn -f /build/pom.xml install -N -q
COPY BcsUtils/pom.xml /build/BcsUtils/pom.xml
COPY BcsUtils/src /build/BcsUtils/src
RUN cd /build/BcsUtils && mvn install -DskipTests -q
COPY BcsContabilidadServicios/pom.xml /build/BcsContabilidadServicios/pom.xml
COPY BcsContabilidadServicios/src /build/BcsContabilidadServicios/src
RUN cd /build/BcsContabilidadServicios && mvn install -DskipTests -q
COPY BcsContabilidadJobs/pom.xml /build/BcsContabilidadJobs/pom.xml
COPY BcsContabilidadJobs/src /build/BcsContabilidadJobs/src
RUN cd /build/BcsContabilidadJobs && mvn package -DskipTests -q

FROM eclipse-temurin:25-alpine
WORKDIR /app
COPY --from=builder /build/BcsContabilidadJobs/target/bcs-contabilidad-jobs-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
