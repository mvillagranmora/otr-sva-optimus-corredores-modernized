# WP-B "envío a SAP" characterization tests

Characterization of the legacy SAP-posting chain of `BcsContabilidadServicios` (+ the job that drives it):

| Legacy class | Modern target (API defined by these tests) | Test class |
|---|---|---|
| `cl.bcs.contabilidad.EnvioContabilidad` | `cl.bcs.contabilidad.service.EnvioContabilidadService` | `EnvioContabilidadServiceTest` |
| `cl.bcs.contabilidad.GenerarJsonAsientoSap` | `cl.bcs.contabilidad.service.GenerarJsonAsientoSapService` + `cl.bcs.contabilidad.domain.service.AsientoSapMapper` | `GenerarJsonAsientoSapServiceTest`, `AsientoSapMapperTest` |
| `cl.bcs.contabilidad.ReversaPreComprobante` | `cl.bcs.contabilidad.service.ReversaPreComprobanteService` | `ReversaPreComprobanteServiceTest` |
| `cl.bcs.contabilidad.jobs.CallerWSJournalVoucher` (Jobs) | `cl.bcs.contabilidad.infrastructure.sap.SapB1Gateway` | `SapB1GatewayTest` |
| `cl.bcs.contabilidad.jobs.InterfazContable` (Jobs) | `cl.bcs.contabilidad.service.InterfazContableService` | `InterfazContableServiceTest` |

Behaviour ids are `CONT-B-NN` (BUSINESS_RULES.md has no Contabilidad rules). Every test method name starts with
its id (`contB42_...`) and carries a comment citing the legacy `file:line` it pins.

## Where the expected values come from

`src/test/resources/envio/cases/<CONT-B-NN_name>/expected.json` were **recorded by executing the unmodified legacy
classes**, not written by hand. `src/test/resources/envio/legacy-oracle/` holds:

* `shims/`: minimal stand-ins for the legacy runtime the classes import (`cl.bcs.utils.SQL/Utils/JavaCall/Log/Fecha`,
  `ConfiguracionSistema`, `Diccionario`, `FechaDB`, `PlataformaUtils.MensajeSalida`, `OACT`, `BcsJob`, log4j, org.json).
  Each one documents the legacy lines whose semantics it reproduces (for example, an empty list becomes `IN (NULL)`, and batches use `setScale(16)`).
* `runner/OracleRunner.java`: builds H2 per case, a fake SAP HANA `FAKECO.OACT`, fake `interfazSAP.properties`, runs
  the legacy class and writes `expected.json` (outputs + table dumps + recorded HTTP traffic + log file).

The modern tests run the new code against **the same H2 data** (`schema-envio.sql` + `seed/base-envio.sql` + case
`seed.sql`, H2 `MODE=LEGACY` so the HANA `"seq".NEXTVAL` and `''`-vs-NULL behave like HANA) and the same
deterministic fakes (`support/Fakes.java` mirrors the shims one-to-one: dictionary text `[CODE]{k=v}`, sequences
from 5000/9000, clock 2025-04-01 10:15:30, config from `seed/config.json`).

SAP B1 Service Layer is `support/FakeSapServiceLayer` (raw socket, JDK only). WireMock is not used: WireMock 3.x pulls
Jetty 11 while the Spring Boot 4 BOM manages Jetty 12. The JDK HttpServer is not used either, because it rewrites
`Set-Cookie` to `Set-cookie`, which the case-sensitive lookup in `CallerWSJournalVoucher.java:90-92` does not find.

## Running

The tests compile only once the target API exists in `src/main` (see the API list in the WP-B report / class
Javadocs). Then, from the workspace root with JDK 25:

```
mvn -f modernized/otr-sva-optimus-corredores/BcsContabilidadServicios/pom.xml test -Dtest='cl.bcs.contabilidad.envio.**'
```

Each golden class prints `equivalence cases executed (<Service>): N/M` and fails in `@AfterAll` when N is 0.
Nothing is `@Disabled` or assumed away. A missing case directory, `case.json`, `expected.json` or SQL resource is an
`AssertionError`, never a skip. `EnvioGoldenInventoryTest` fails if a case directory is not run by exactly one test,
or if a SQL resource under `src/main/resources/cl/bcs/contabilidad/sql/` is missing.

Comparison rules (`support/JsonCanon`): keys order-insensitive, null-valued keys ignored, numbers by value (H2 column
scale is not legacy behaviour; literal SAP number formatting is pinned in `AsientoSapMapperTest` CONT-B-25), arrays
fed by SQL without ORDER BY (`P_OUT_ASIENTOS`, `JournalEntryLines`, partners, table rows) compared as multisets.

## Adding a case

1. Create `src/test/resources/envio/cases/CONT-B-NN_short-name/case.json` (`servicio`: `EnvioContabilidad` |
   `GenerarJsonAsientoSap` | `ReversaPreComprobante` | `InterfazContable`; `entrada`; optional `config` overrides
   (JSON null removes a key), `planCuentas`, `sinArchivoConfiguracion`, `sap` script) and an optional `seed.sql`.
   Use synthetic data only. Never put real RUTs, credentials or production rows in a seed.
2. Record it with the legacy oracle (JDK 25; never against a real SAP/HANA, everything is local and fake):
   ```
   L=legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA
   T=modernized/otr-sva-optimus-corredores/BcsContabilidadServicios/src/test
   CP="h2-2.4.240.jar;spring-jdbc/core/beans/tx-7.0.9.jar;commons-logging-1.3.5.jar;gson-2.11.0.jar;commons-io-2.17.0.jar"  # from ~/.m2
   javac -encoding UTF-8 -proc:none -d <tmp>/oracle -cp "$CP" $(find $T/resources/envio/legacy-oracle -name '*.java') \
     $T/java/cl/bcs/contabilidad/envio/support/FakeSapServiceLayer.java \
     $L/BcsContabilidadServicios/src/cl/bcs/contabilidad/{EnvioContabilidad,GenerarJsonAsientoSap,ReversaPreComprobante}.java \
     $L/BcsContabilidadServicios/src/cl/bcs/contabilidad/pojo/{JournalEntry,JournalEntryLine,JournalVoucher,JournalVoucher_,BusinessPartner}.java \
     $L/BcsContabilidadJobs/src/cl/bcs/contabilidad/pojo/LoginSAP.java \
     $L/BcsContabilidadJobs/src/cl/bcs/contabilidad/jobs/{InterfazContable,CallerWSJournalVoucher,Respuesta}.java
   cd <empty work dir>   # the runner writes fake interfazSAP.properties there
   java -cp "<tmp>/oracle;$L/BcsContabilidadServicios/src;$L/BcsContabilidadJobs/src;$CP" OracleRunner $T/resources/envio CONT-B-NN
   ```
   Add `-Doracle.debug=true` to see legacy log output. Note that legacy prints the (fake) login JSON, including the password, to stdout.
3. Add the case name to the `CASOS` list of the matching test class and write a `contBNN_...` test that calls
   `ejecutarYComparar("CONT-B-NN_...")`, cites the legacy `file:line`, and restates the rule with explicit asserts.

## Secrets

All credential-shaped values are fake and of the legacy shape (`fake-b1-user`, `fake-b1-pass-0000`, `FAKECO`,
`FAKE_HANA_USER`, `fake-session-0001`). Real values are read from the environment by the implementation and never
appear in fixtures. The tests contact no real SAP, HANA or third-party host.
