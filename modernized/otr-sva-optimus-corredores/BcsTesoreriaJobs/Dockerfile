# tesoreria-jobs uses BcsTesoreriaServicios as a library (its plain jar), so both are built here.
FROM eclipse-temurin:25-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven
COPY pom.xml /build/pom.xml
RUN mvn -f /build/pom.xml install -N -q
COPY BcsUtils/pom.xml /build/BcsUtils/pom.xml
COPY BcsUtils/src /build/BcsUtils/src
RUN cd /build/BcsUtils && mvn install -DskipTests -q
COPY BcsTesoreriaServicios/pom.xml /build/BcsTesoreriaServicios/pom.xml
COPY BcsTesoreriaServicios/src /build/BcsTesoreriaServicios/src
RUN cd /build/BcsTesoreriaServicios && mvn install -DskipTests -q
COPY BcsTesoreriaJobs/pom.xml /build/BcsTesoreriaJobs/pom.xml
COPY BcsTesoreriaJobs/src /build/BcsTesoreriaJobs/src
RUN cd /build/BcsTesoreriaJobs && mvn package -DskipTests -q

FROM eclipse-temurin:25-alpine
WORKDIR /app
COPY --from=builder /build/BcsTesoreriaJobs/target/tesoreria-jobs-*.jar app.jar
EXPOSE 8074
ENTRYPOINT ["java", "-jar", "app.jar"]
