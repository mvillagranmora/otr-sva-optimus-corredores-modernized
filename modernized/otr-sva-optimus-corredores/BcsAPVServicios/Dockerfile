FROM eclipse-temurin:25-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven
COPY pom.xml /build/pom.xml
RUN mvn -f /build/pom.xml install -N -q
COPY BcsUtils/pom.xml /build/BcsUtils/pom.xml
COPY BcsUtils/src /build/BcsUtils/src
RUN cd /build/BcsUtils && mvn install -DskipTests -q
COPY BcsAPVApi/pom.xml /build/BcsAPVApi/pom.xml
COPY BcsAPVApi/src /build/BcsAPVApi/src
RUN cd /build/BcsAPVApi && mvn install -DskipTests -q
COPY BcsApvJavaCallAdapter/pom.xml /build/BcsApvJavaCallAdapter/pom.xml
COPY BcsApvJavaCallAdapter/src /build/BcsApvJavaCallAdapter/src
RUN cd /build/BcsApvJavaCallAdapter && mvn install -DskipTests -q
WORKDIR /build/app
COPY BcsAPVServicios/pom.xml .
COPY BcsAPVServicios/src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:25-alpine
WORKDIR /app
# The executable jar carries the "exec" classifier; the plain jar is the library used by the Jobs module.
COPY --from=builder /build/app/target/*-exec.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
