#!/usr/bin/env bash
# Container smoke test of the sidecar image with podman (no HANA: H2 in memory seeded with one .rpt template).
# Run from modernized/otr-sva-optimus-corredores/BcsCrystalReports (needs libs/*.jar locally; they are gitignored).
# Verified 2026-10-21 (podman 6.1.3, eclipse-temurin:11-alpine): health 200, empty body 400, real PDF render 1 page.
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"; CTX="$HERE/.."
H2JAR="${H2JAR:-$HOME/.m2/repository/com/h2database/h2/2.1.214/h2-2.1.214.jar}"   # h2 is test-scope: not inside app.jar
# 1) build (context = parent dir: the Dockerfile COPYs BcsCrystalReports/...). Maven needs network (~3 min).
podman build -f "$HERE/Dockerfile" -t bcs-crystal-reports:verify "$CTX"
# 2) seed dir: template + init script + h2 jar
S="$(mktemp -d)"; mkdir "$S/extra"; cp "$H2JAR" "$S/extra/"
cp "$HERE/templates/CuentaInversion/Templates/ComprobanteCuentaInversion.rpt" "$S/t.rpt"
cat > "$S/init.sql" <<'SQL'
CREATE SCHEMA IF NOT EXISTS BSCL;
CREATE TABLE BSCL."BSCL.Report::Template"("Id" VARCHAR(64), "ReportName" VARCHAR(255), "MasterReportId" VARCHAR(64), "ReportFile" BLOB, "ReportVersion" INT, "CompanyId" BIGINT);
INSERT INTO BSCL."BSCL.Report::Template" VALUES ('T-COMPROBANTE','ComprobanteCuentaInversion',NULL,FILE_READ('/seed/t.rpt'),1,1);
SQL
# 3) run: SAME flags as the Dockerfile ENTRYPOINT, but PropertiesLauncher so that -Dloader.path adds the H2 jar
podman rm -f crystal-verify >/dev/null 2>&1 || true
MSYS_NO_PATHCONV=1 podman run -d --name crystal-verify -p 18100:8100 -v "$(cygpath -w "$S" 2>/dev/null || echo "$S"):/seed:ro" \
  -e "SPRING_DATASOURCE_URL=jdbc:h2:mem:render;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM '/seed/init.sql'" \
  -e SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver -e SPRING_DATASOURCE_USERNAME=sa -e SPRING_DATASOURCE_PASSWORD= \
  --entrypoint java bcs-crystal-reports:verify \
  --add-exports java.sql.rowset/com.sun.rowset=ALL-UNNAMED --add-exports java.sql.rowset/com.sun.rowset.internal=ALL-UNNAMED \
  --add-opens java.sql.rowset/com.sun.rowset=ALL-UNNAMED -Djava.awt.headless=true -Dfile.encoding=UTF-8 \
  -DSystemFontPath=/usr/share/fonts -Dloader.path=/seed/extra -cp app.jar org.springframework.boot.loader.PropertiesLauncher
until curl -s -m 2 localhost:18100/reports/health; do sleep 3; done; echo
# 4) checks
curl -s -o /dev/null -w "empty JSON -> %{http_code} (expect 400)\n" -X POST -H 'Content-Type: application/json' -d '{}' localhost:18100/reports/generate
row="$(tr -d '\r\n' < "$HERE/src/test/resources/crystal/comprobante.json")"; row="${row#[}"; row="${row%]}"
curl -s -o /tmp/crystal-verify.pdf -w "render -> %{http_code} %{size_download} bytes\n" -X POST -H 'Content-Type: application/json' \
  -d "{\"templateId\":\"T-COMPROBANTE\",\"format\":\"pdf\",\"beanData\":$row}" localhost:18100/reports/generate
pdftotext -layout /tmp/crystal-verify.pdf - | grep -E "JUAN PEREZ GONZALEZ|1,500,000.50"
# 5) cleanup (the image bcs-crystal-reports:verify is kept)
podman rm -f crystal-verify
