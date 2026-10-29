FROM eclipse-temurin:11-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven

# ── Private JARs (Crystal Reports SDK + barcode4j + avalon) ──────────────────
COPY BcsCrystalReports/libs /build/libs

RUN mvn install:install-file -q \
      -Dfile=/build/libs/CrystalReportsRuntime.jar \
      -DgroupId=com.crystaldecisions \
      -DartifactId=CrystalReportsRuntime \
      -Dversion=14.1 \
      -Dpackaging=jar && \
    mvn install:install-file -q \
      -Dfile=/build/libs/CrystalCommon2.jar \
      -DgroupId=com.crystaldecisions \
      -DartifactId=CrystalCommon2 \
      -Dversion=14.1 \
      -Dpackaging=jar && \
    mvn install:install-file -q \
      -Dfile=/build/libs/webreporting.jar \
      -DgroupId=com.crystaldecisions \
      -DartifactId=webreporting \
      -Dversion=14.1 \
      -Dpackaging=jar && \
    mvn install:install-file -q \
      -Dfile=/build/libs/JDBInterface.jar \
      -DgroupId=com.crystaldecisions \
      -DartifactId=JDBInterface \
      -Dversion=14.1 \
      -Dpackaging=jar && \
    mvn install:install-file -q \
      -Dfile=/build/libs/DatabaseConnectors.jar \
      -DgroupId=com.crystaldecisions \
      -DartifactId=DatabaseConnectors \
      -Dversion=14.1 \
      -Dpackaging=jar && \
    mvn install:install-file -q \
      -Dfile=/build/libs/pfjgraphics.jar \
      -DgroupId=com.crystaldecisions \
      -DartifactId=pfjgraphics \
      -Dversion=14.1 \
      -Dpackaging=jar && \
    mvn install:install-file -q \
      -Dfile=/build/libs/cvom.jar \
      -DgroupId=com.crystaldecisions \
      -DartifactId=cvom \
      -Dversion=14.1 \
      -Dpackaging=jar && \
    mvn install:install-file -q \
      -Dfile=/build/libs/barcode4j.jar \
      -DgroupId=net.sf.barcode4j \
      -DartifactId=barcode4j \
      -Dversion=2.1 \
      -Dpackaging=jar && \
    mvn install:install-file -q \
      -Dfile=/build/libs/avalon-framework-4.2.0.jar \
      -DgroupId=avalon-framework \
      -DartifactId=avalon-framework \
      -Dversion=4.2.0 \
      -Dpackaging=jar && \
    mvn install:install-file -q \
      -Dfile=/build/libs/logging.jar \
      -DgroupId=com.crystaldecisions \
      -DartifactId=logging \
      -Dversion=14.1 \
      -Dpackaging=jar && \
    mvn install:install-file -q \
      -Dfile=/build/libs/keycodeDecoder.jar \
      -DgroupId=com.crystaldecisions \
      -DartifactId=keycodeDecoder \
      -Dversion=14.1 \
      -Dpackaging=jar && \
    mvn install:install-file -q \
      -Dfile=/build/libs/log4j.jar \
      -DgroupId=com.crystaldecisions \
      -DartifactId=log4j \
      -Dversion=1.2.6 \
      -Dpackaging=jar

# ── Service build ─────────────────────────────────────────────────────────────
WORKDIR /build/app
COPY BcsCrystalReports/pom.xml .
COPY BcsCrystalReports/src ./src
RUN mvn package -DskipTests -q

# ── Runtime image ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:11-alpine
# JRC needs fonts: JDK 9+ has no ${java.home}/lib/fonts and JRC's OpenTypeFontManager throws an NPE on every PDF
# export when that directory is missing. Install DejaVu + fontconfig, create the dir and populate it.
# (Verified: the image was built and rendered with podman on 2026-10-21, see analysis/.../CRYSTAL_CONTAINER_CHECK.md.)
RUN apk add --no-cache fontconfig ttf-dejavu && \
    mkdir -p "$JAVA_HOME/lib/fonts" && \
    cp /usr/share/fonts/dejavu/*.ttf "$JAVA_HOME/lib/fonts/" && \
    fc-cache -f
# Non-root runtime user (M5). /app and /app/tmp (templates and pictures written during a render, see TempFiles) are
# writable by it; everything else stays root-owned. The fonts above were copied as root at build time.
RUN addgroup -S app && adduser -S -G app -h /app app && mkdir -p /app/tmp && chown -R app:app /app
WORKDIR /app
COPY --from=builder --chown=app:app /build/app/target/*.jar app.jar
USER app
EXPOSE 8100
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3   CMD wget -qO- http://localhost:8100/reports/health || exit 1
# --add-exports/--add-opens: ListMapResultSet extends com.sun.rowset.CachedRowSetImpl (JDK internal)
# -Djava.awt.headless / -Dfile.encoding=UTF-8: legacy ran headless with file.encoding=UTF-8 (TXT export uses the default charset)
# -DSystemFontPath: where JRC looks for system fonts besides ${java.home}/lib/fonts
# -XX:MaxRAMPercentage / ExitOnOutOfMemoryError (H6): heap bounded by the container limit; an OOM kills the process so
# the restart policy brings a clean one (an OutOfMemoryError is an Error and is not caught by the controller).
ENTRYPOINT ["java",   "--add-exports", "java.sql.rowset/com.sun.rowset=ALL-UNNAMED",   "--add-exports", "java.sql.rowset/com.sun.rowset.internal=ALL-UNNAMED",   "--add-opens", "java.sql.rowset/com.sun.rowset=ALL-UNNAMED",   "-Djava.awt.headless=true", "-Dfile.encoding=UTF-8", "-DSystemFontPath=/usr/share/fonts",   "-XX:MaxRAMPercentage=70", "-XX:+ExitOnOutOfMemoryError",   "-jar", "app.jar"]
