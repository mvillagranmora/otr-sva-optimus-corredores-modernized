# hana-jdbc

Container (Podman) to run read-only JDBC checks and the BcsSURA tests against HANA QA. The image holds no credentials.

1. Fill `qa01.saphana.*` in the git-ignored `.env` at the repo root (`.env.example` is the template).
2. `modernized/otr-sva-optimus-corredores/hana-jdbc/run.sh build` (copies ngdbc 2.8.12 from `~/.m2`).
3. `.../run.sh smoke` connects read-only and prints `CURRENT_USER`/`CURRENT_SCHEMA`.
   `.../run.sh sql "SELECT ..."` runs one SELECT (anything else is refused, the connection is read-only).
   `.../run.sh test [mvn args]` runs `mvn -o test` of BcsSURA with `HANA_URL`/`HANA_USER`/`HANA_PASSWORD` set from the same file.

`dbprop` is `key=value` pairs separated by `;` (for example `currentschema=BSCL`, needed by the SQL of the module); `run.sh test` turns it into the URL query. The module is mounted read-only, so `target/` is written inside the container only.
The 7 `@Disabled` BcsSURA tests are still disabled: pointing a test at HANA is a separate change.
