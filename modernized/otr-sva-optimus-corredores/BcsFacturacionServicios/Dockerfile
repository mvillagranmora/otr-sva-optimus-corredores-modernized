FROM eclipse-temurin:25-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven
COPY pom.xml /build/pom.xml
RUN mvn -f /build/pom.xml install -N -q
WORKDIR /build/app
COPY BcsFacturacionServicios/pom.xml .
COPY BcsFacturacionServicios/src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:25-alpine
WORKDIR /app
COPY --from=builder /build/app/target/*.jar app.jar
EXPOSE 8099
ENTRYPOINT ["java", "-jar", "app.jar"]
