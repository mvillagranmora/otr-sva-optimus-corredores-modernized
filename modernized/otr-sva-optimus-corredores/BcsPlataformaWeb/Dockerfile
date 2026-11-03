FROM eclipse-temurin:25-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven
COPY pom.xml /build/pom.xml
RUN mvn -f /build/pom.xml install -N -q
COPY BcsUtils/pom.xml /build/BcsUtils/pom.xml
COPY BcsUtils/src /build/BcsUtils/src
RUN cd /build/BcsUtils && mvn install -DskipTests -q
WORKDIR /build/app
COPY BcsPlataformaWeb/pom.xml .
COPY BcsPlataformaWeb/src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:25-alpine
WORKDIR /app
COPY --from=builder /build/app/target/*.jar app.jar
# ---------------------------------------------------------------------------------------------------------------------
# Base de datos del login: una sola por despliegue, elegida por PLATAFORMA_DB_DIALECT (hana | postgres; otro valor detiene el arranque).
# La imagen NO lleva credenciales: todo se entrega al ejecutar el contenedor (docker run -e / orquestador / secretos).
#
# SAP HANA (por defecto):
#   PLATAFORMA_DB_DIALECT=hana
#   HANA_URL, HANA_USER, HANA_PASSWORD            (o SPRING_DATASOURCE_URL / _USERNAME / _PASSWORD)
#
# Postgres (no se necesita ninguna variable de HANA):
#   PLATAFORMA_DB_DIALECT=postgres
#   SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver      (obligatoria: sin ella se usa el driver de HANA)
#   SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<puerto>/<base>?currentSchema=<esquema>&stringtype=unspecified&escapeSyntaxCallMode=callIfNoReturn
#   SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
#     - currentSchema:                       las sentencias usan nombres de tabla sin esquema.
#     - stringtype=unspecified:              sin ella los textos y nulos se envian como varchar y fallan contra bigint/date/timestamp.
#     - escapeSyntaxCallMode=callIfNoReturn: sin ella los procedimientos (PROCEDURE) fallan con SQLState 42809.
#   La aplicacion avisa al arrancar si falta stringtype o escapeSyntaxCallMode.
#
# Las propiedades audit.datasource.* no las usa ningun codigo. En modo Postgres, /call y los despachadores genericos responden 503.
# ---------------------------------------------------------------------------------------------------------------------
ENV PLATAFORMA_DB_DIALECT=hana
EXPOSE 8117
ENTRYPOINT ["java", "-jar", "app.jar"]
