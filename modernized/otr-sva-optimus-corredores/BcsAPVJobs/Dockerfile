# apv-jobs uses BcsAPVServicios as a library (its plain jar, with its typed API and JavaCall adapter modules), so all of them are built here.
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
COPY BcsAPVServicios/pom.xml /build/BcsAPVServicios/pom.xml
COPY BcsAPVServicios/src /build/BcsAPVServicios/src
RUN cd /build/BcsAPVServicios && mvn install -DskipTests -q
COPY BcsAPVJobs/pom.xml /build/BcsAPVJobs/pom.xml
COPY BcsAPVJobs/src /build/BcsAPVJobs/src
RUN cd /build/BcsAPVJobs && mvn package -DskipTests -q

FROM eclipse-temurin:25-alpine
WORKDIR /app
COPY --from=builder /build/BcsAPVJobs/target/apv-jobs-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
