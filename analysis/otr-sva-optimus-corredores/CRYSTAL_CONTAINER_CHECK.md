# BcsCrystalReports - container image check (podman), 2026-10-21

## Environment
- podman 6.1.3 (client); `podman machine`: podman-machine-default (WSL, 8 CPU, 2 GiB) already "Currently running"; nothing started/changed.
- Network available: `eclipse-temurin:11-alpine` was already local; `apk add maven`, Maven Central downloads and `apk add fontconfig ttf-dejavu` all worked.

## Commands (script: `BcsCrystalReports/container-smoke.sh`)
1. `podman build -f modernized/otr-sva-optimus-corredores/BcsCrystalReports/Dockerfile -t bcs-crystal-reports:verify modernized/otr-sva-optimus-corredores`
   (context = parent dir; matches both compose files: `context: .` and `./otr-sva-optimus-corredores`). The REAL Dockerfile was built unmodified, both stages: exit 0 (~3 min).
2. Run A (real ENTRYPOINT, HANA_URL=jdbc:sap://127.0.0.1:39015 dummy, never contacted): app starts in 4.7 s.
3. Run B (render): same image, entrypoint overridden with identical JVM flags but `PropertiesLauncher -Dloader.path=/seed/extra`
   (H2 is test-scope, not in app.jar), `SPRING_DATASOURCE_*` env -> in-memory H2 seeded via `INIT=RUNSCRIPT` with the .rpt (`FILE_READ`). No main code changed.

## Results
| Check | Result |
|---|---|
| `GET /reports/health` | 200 `BcsCrystalReports UP` (also via `wget` inside the container, as the compose healthcheck does) |
| `POST /reports/generate` body `{}` | 400 `{"error":"templateId is required"}` |
| `POST /reports/generate` with NO body | **500** "Required request body is missing" (see findings) |
| `POST /reports/generate` ComprobanteCuentaInversion, pdf | 200 application/pdf, 30,735 bytes, `%PDF-1.7`, `/Count 1` (1 page), 6.8 s first render; pdftotext shows `Cliente JUAN PEREZ GONZALEZ` and `Monto CLP 1,500,000.50` |
| runtime | `openjdk 11.0.32.1`, user root (uid 0), `$JAVA_HOME/lib/fonts` holds 22 DejaVu TTFs |

## Proven
- The Dockerfile builds from scratch on Linux (private jars installed with the documented coordinates, Boot jar produced, `target/*.jar` glob picks one jar).
- Fonts layout works on Alpine/JDK 11: JRC exports a real PDF (no NPE in OpenTypeFontManager) - first Linux evidence.
- ENTRYPOINT flags (add-exports/add-opens for com.sun.rowset, headless, UTF-8, SystemFontPath) are sufficient: ListMapResultSet + JRC + data binding work.
- Healthcheck command used by compose (busybox wget) works in the image.

## Not proven
- Java 8 runtime of the legacy; real HANA (ngdbc driver never connected); other formats (xls/rtf/txt/word) and the other 152 templates / subreports / images in the container (only pdf of one template);
  determinism/equivalence vs legacy (that is the harness job). The render ran with the ENTRYPOINT flags but via PropertiesLauncher (needed only to add H2); the stock ENTRYPOINT with `-jar app.jar` was exercised in Run A only (start/health/400).
- The compose wiring (`depends_on: service_healthy`, env HANA_*) was not run (it needs HANA vars and the 40 other services).

## Defects
- Dockerfile/compose: none found, no edit made.
- Findings outside my scope (main code, not changed): POST with no body returns 500 instead of 400 (HttpMessageNotReadableException handled as generic error in GlobalExceptionHandler);
  the app has no profile without HANA (H2 must be added from outside the fat jar, as above); container runs as root (acceptable for now); Dockerfile has no HEALTHCHECK of its own (compose has one);
  `apk add maven` pulls openjdk25 into the builder stage, harmless because JAVA_HOME=11 is used (build compiled OK) but wasteful.

## Cleanup
Container `crystal-verify` removed; image `bcs-crystal-reports:verify` kept locally (not pushed). An older `bcs-crystal-reports:latest` (11 days old) was left untouched.

## Next steps
- Add a non-root user and a Dockerfile HEALTHCHECK; map empty body to 400.
- Add H2 as optional runtime/`test` profile image target so the smoke needs no loader.path trick.
- Run the real compose with HANA once credentials/network exist; render the other formats and templates in the container; pin font set (DejaVu) when comparing with legacy output.


## Addendum (step 5, architecture-review fixes): image `bcs-crystal-reports:verify2`
- Build: same command as above, tag `bcs-crystal-reports:verify2` (exit 0). Changes in the image: non-root user `app` (uid 100), `/app/tmp` writable, `HEALTHCHECK`,
  `-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError`, `BcsCrystalReports/Dockerfile.dockerignore`. (podman builds the OCI format, which does not keep HEALTHCHECK in the image config; `--format docker` does.)
- Run with compose-like hardening: `--read-only --tmpfs /app/tmp:rw,size=512m,mode=1777 --tmpfs /tmp:rw,size=256m,mode=1777 --cap-drop ALL --security-opt no-new-privileges:true`,
  user `uid=100(app)`, same JVM flags + `PropertiesLauncher -Dloader.path` (H2 only), `INTERNAL_TOKEN` set.
  The read-only rootfs needed only those two tmpfs (no other writable path found).
- Results: `GET /reports/health` 200; `POST /reports/generate {}` 400; `POST /reports/generate` ComprobanteCuentaInversion pdf 200, `%PDF-1.7`, `/Count 1`, text `Cliente JUAN PEREZ GONZALEZ` and `Monto CLP 1,500,000.50`;
  `POST /report` without token 401 `401 - NOT AUTHORIZED` (also wrong token 401, `GET /actuator/info` 401), with `X-Internal-Token` 200 PDF 1 page with the same text,
  `Content-Disposition: attachment; filename="_.PDF"` (empty document name: legacy quirk). Startup log shows the WARN lines for the rejected requests (no token value).
- NOT verified: `--memory 2g` / compose `mem_limit`: the podman machine rejects it (`crun: open memory.max: No such file or directory`, no memory cgroup controller in this WSL VM), so the container
  was run without a memory limit; `MaxRAMPercentage` therefore sized from the VM. Fonts are still DejaVu (pixel equivalence with the Windows-font goldens is not proven).
- The two `modernized` compose files were only YAML-parsed (`docker compose config` was not run: it needs `.env` with `INTERNAL_TOKEN` and HANA vars, none of the credentials was added).
- Containers started for this check (`crystal-verify2`) were removed; the images `verify` and `verify2` are kept.
