# Golden fixtures: provenance

Every file under this directory was **recorded from the unmodified legacy BcsUtils**, run locally. Nothing was hand-written.

- **Legacy sources (read-only):** `legacy/otr-sva-optimus-corredores/BSCL/Plataforma/JAVA/BcsUtils/src/cl/bcs/utils/*.java`.
  - They were compiled into a scratch folder: JDK 11.0.32 (Temurin), `javac -source 8 -target 8`.
  - The legacy build targets Java 8; production ran on a Java 8 JRE.
- **Compile and run classpath** (closest versions in `~/.m2` to `build.gradle`):
  - gson 2.8.2 (legacy pins 2.6.2). Re-recording `json` and `collections` with gson 2.11.0 (the version this reactor uses) gives identical output.
  - commons-io 2.5, commons-codec 1.10, commons-lang 2.6, guava 32.1.2-jre, jackson 2.8.10, log4j 2.12.1, commons-beanutils 1.9.4.
  - org.json: the old HashMap-based copy inside `libs-private/cvom.jar`.
  - `libs-private/storedprocedure-2.0-SNAPSHOT.jar`: `cl.bcs.nuam`, dbcp2.
  - H2 2.4.240, used as the database for the `db/` records.
  - A compile-only stand-in for the Datadog `@Trace` annotation.
- **JVM settings:**
  - `-Dfile.encoding=UTF-8`.
  - `-Duser.language=es -Duser.country=CL`; the tests apply the same Locale while they run in a golden zone.
  - `-Duser.timezone`: the zone of each directory.
- **Directories:**
  - `any/`: zone-independent. The run used UTC.
  - `America_Santiago/` and `UTC/`: the same inputs, run with that JVM default zone. The inputs cover Chile's DST changes: 2024-09-08 has no 00:00, and 2024-04-06 23:xx happens twice.
  - `db/`: legacy code that needs a JDBC connection, run for real on in-memory H2, in UTC. It covers:
    - `ValidarNuevaClave.logica`;
    - `Sesion.insertarSesion`;
    - `SesionUtils.GetParametro` / `ObtenerFechaHoraUTCTs`;
    - `ProcesaLlamadaServletCall.logica`.

    The H2 tables carry the HANA names the legacy SQL uses. HANA's `CURRENT_UTCTIMESTAMP` is an H2 schema constant, so the oracle controls "now". Classes with JVM-wide static caches were loaded in a fresh class loader for each case.
- **Record shape:**
  - Each case has `{"id","rule","input","out"}`.
  - `out` is `{"value": ...}`, or `{"throws": "<exception class>"}`. Exception messages are never recorded or compared.
  - A string with an unpaired surrogate is recorded as `{"utf16": [code units]}`.
  - Dates are `{type, ms, local, nanos (Timestamp only), toString}`.
  - Zip archives are recorded by their entries (name, dir flag, size, sha256, method). Entry times are never recorded.
- **Secret handling:**
  - Before publishing, every record was checked against the string literals of the legacy secret-bearing lines: the debug `main()` in `Sesion.java:303-331`, `CryptoUtils.java`, `Crypto.java:15-25`, and the `JsonWebToken` HMAC key. The check read those literals live from the legacy source and never wrote them down. Plain lower-case words are exempt. The publish was refused if any literal was present.
  - No credential appears here.
  - JWT records (removed, decision 5) were signed with the FAKE secret `FAKE_KEY_1`, through the legacy private `hmacSha256(data, secret)`. `iat`/`jti` were fixed in the legacy payload before signing.
  - `isValid()` of tokens signed with the legacy key is recorded as booleans only.
  - Session tokens in `db/session.json` encode synthetic users (`usuario.prueba`) from a scratch H2 database. They are valid nowhere.
- **Re-recording:**
  - Oracle runner: `<scratchpad>/bcsutils/oracle/runner/oracle/*.java` (`Main <outDir> ANY|DB|America/Santiago|UTC`).
  - Publisher: `<scratchpad>/bcsutils/oracle/publish.py`.
  - See `src/test/java/README.md`.
- **Recorded:** 2026-10-16. 2011 cases recorded; the 116 `jwt.json` cases (`America_Santiago/`, `UTC/`) were removed with JsonWebToken (decision 5, 2026-10-16), leaving 1895. `AlcanceLibreriaTest.util91_inventarioGolden` pins the count.
