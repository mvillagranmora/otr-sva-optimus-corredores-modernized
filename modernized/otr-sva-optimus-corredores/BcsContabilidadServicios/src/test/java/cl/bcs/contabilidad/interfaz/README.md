# WP-A "interfaz diaria": characterization tests (CONT-A-NN)

These tests pin what legacy `CreaInterfazDiaria` and the `CreaBroad*`, `CreaDescriptivasBroad`, `CreaCuentasBPC`,
`CreaMovimientoBPC` and `SFTPConnector` classes actually do. The legacy code is the oracle. Where it looks wrong,
the tests still assert the legacy behaviour; the suspected bugs are listed below for a human to decide on.

## Running

```bash
mvn test -pl modernized/otr-sva-optimus-corredores/BcsContabilidadServicios -Dtest='cl.bcs.contabilidad.interfaz.*Test'
```

Run them from the module root. `InterfazSftpTest` scans `src/main/...` using paths relative to that root.

No test is `@Disabled`, and none is skipped by an assumption. A missing golden file or missing production
source makes the test **fail**. Until `src/main` provides the target API, this package does not compile.
That is intended, and it means nothing is proven yet.

Full-run equivalence: 13 golden files are compared byte for byte (`cont_a_06_fullRunMatchesAllThirteenGoldens`).
The isolation matrix compares them again with each of the 8 SAP queries failing in turn.

## Golden files (`src/test/resources/interfaz/golden/{FI,BPC}/`)

These files were produced by a scratch harness. It ran the legacy `String.format`, `Calendar` and
`BigDecimal` statements, copied verbatim, against the rows in `InterfazFixtures`. The settings were
`TimeZone=America/Santiago` and now = `2026-10-13T18:04:05Z`, which is the Santiago date 2026-10-13, so
fechaEjecucion = 2026-10-12. The contador was 3. The goldens are written in ISO-8859-1 with LF line ends.
`B1_DES_CC_PROD.TXT` is ASCII only, because the legacy writes that file with the platform charset.
`.gitattributes` marks the goldens `-text` so that git never rewrites their line endings.

**Adding a case:** add or change rows in `InterfazFixtures`. Then regenerate the goldens by running the
legacy formatting statements again (never by running the modern code). Then add a test whose name
carries the `cont_a_NN_` id, with a one-line `// LegacyFile.java:line` comment.

## Secrets

All SFTP values are fake: `sftp.example.invalid`, `interfaz-test`, `fake-password-0000`. Live values come only
from the env vars `SFTP_HOST`, `SFTP_PORT`, `SFTP_USER`, `SFTP_PASSWORD` and `SFTP_KNOWN_HOSTS`. No test connects to a real server.

Host-key verification (approved deviation from legacy `StrictHostKeyChecking=no`): the server key must match an
entry of the OpenSSH known_hosts file named by `SFTP_KNOWN_HOSTS`. CONT-A-37 pins that a missing or empty file, an
unknown host and a different key are rejected with `SftpHostKeyException`, and that a matching key is accepted. The
keys are generated in-process; no SSH server is started (none is available offline).

## Legacy quirks pinned (flag for review)

- `FEC-PROCESO` is `YEAR + MONTH + DAY` added as integers (2026-10-13 gives `2049`). `HRA-PROCESO` uses the
  12-hour `Calendar.HOUR`, with minutes and seconds unpadded (`3:4:5`).
- The CTAS and MOV_CONT control files read the mtime of a path that does not exist (ctrl name + data name).
  The mtime is therefore epoch 0, which gives `2012    9:0:0` in Santiago.
- Rows are written in reverse query order. Values are padded but never truncated. A null value prints `null`.
- Files are opened in append mode, so a rerun on the same day duplicates the content.
- `SFTPConnector.downloadDir` actually **uploads**. The tests pin removal of the remote copy, then upload.
