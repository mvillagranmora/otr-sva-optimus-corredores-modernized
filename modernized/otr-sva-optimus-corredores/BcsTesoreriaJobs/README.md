# BcsTesoreriaJobs

## Descripción

Los jobs del legacy `BSCL/Tesoreria/JAVA/BcsTesoreriaJobs`, ejecutados **en proceso** con el contrato del `BcsJob` legacy
(una transacción por corrida; `true` = commit, `false` = rollback, excepción = rollback + estado de error). El Job Manager
ya no instancia la clase por nombre: llama a `POST /internal/job-call`. No hay `@Scheduled` ni modo demonio (RULE-061), ni
`main()` de depuración ni credenciales (RULE-076), ni 3DES (RULE-077).

| Clase legacy (`clase`) | Qué hace |
|---|---|
| `cl.bcs.tesoreria.cargaMasivaIngresoEgresoDAPFFMM.BcsJobCargaMasivaIngresoEgresoDAPFFMM` | Carga masiva DAP / FFMM: 6 guards (módulo cerrado, ya cargado, sin archivo, pauta del día hábil anterior, sin horario, fuera de horario) con CSV de errores, o el servicio de carga y AgregarLog |
| `cl.bcs.tesoreria.BcsJobProcesarDistribucion` | Rendición PAC: distribución, `ValidarMandatosPAC`, CSV `tes/RendicionPAC<ts>.csv` con BOM, `RutaSalida`, reinicio del flag |
| `cl.bcs.tesoreria.generarArchivoCargos.GenerarArchivoCargos` | Archivo de cargos PAC (formato 8): procedimiento, formateo, archivo TXT / CSV, `FTP.Cabecera` / `FTP.Detalle` |
| `cl.bcs.tesoreria.generarArchivoPago.GenerarArchivoPago` | Archivo de pagos bancarios (genérico TXT / CSV / DAT, BCP telecrédito, BancoEstado) y actualización de la nómina |

`GenerarArchivoPagos` y `GenerarArchivoPagoBancoEstado` (clases JavaCall) son ayudantes: la primera la usa el job de pagos;
la segunda no la llama ningún job y se conserva caracterizada por separado.

## Tecnología

- Spring Boot 4.1.1 / Java 25, puerto 8074 (solo red interna).
- Artifact `cl.bcs:tesoreria-jobs`.
- Usa **BcsTesoreriaServicios como librería** (`cl.bcs:tesoreria-service`, su jar plano; el ejecutable de Servicios es
  `*-exec.jar`): los jobs llaman a sus servicios directamente, sin HTTP entre job y servicio. Los colaboradores que viven
  fuera de Tesoreria (`ValidarMandatosPAC`, `ValidaEjecucionProcesoRecPAC`, `ConsultarEstadoModulo`, `ObtenerDiaAnterior`,
  `AgregarLog`) van por el router JavaCall de la plataforma (`ColaboradoresJobs`).
- Los CSV usan `CsvLegacyWriter`, una reproducción de opencsv 3.7 (la versión del runtime legacy); el pom padre trae 5.9.

## Endpoint

`POST /internal/job-call` (cabecera `X-Internal-Token`; sin token configurado se rechaza todo)

```json
{"clase": "cl.bcs.tesoreria.BcsJobProcesarDistribucion", "idPrograma": 900,
 "parametros": {"IdEmpresa": "1", "IdUsuario": "21", "Simular": "N"}}
```

| Caso | Respuesta |
|---|---|
| COMMIT o ROLLBACK | 200 `{clase, idPrograma, resultado, error:false, mensaje:""}` |
| ERROR (excepción del job) | 500 `{..., resultado:"ERROR", error:true, mensaje:<mensaje de la excepción>}` |
| Clase no migrada a este módulo | 503 `{error:true, clase, mensaje}` |
| Mismo programa o misma clase ya en ejecución | 409 |
| JSON inválido, sin `clase`, sin `idPrograma` o `idPrograma` no numérico | 400 |

Los valores de `parametros` se pasan como texto; los `null` se descartan. Un ROLLBACK responde 200 con `error:false`, igual
que el legacy (`BcsJob.java:151`). `GET /actuator/health` es público.

## Variables de entorno

| Variable | Uso |
|---|---|
| `HANA_URL`, `HANA_USER`, `HANA_PASSWORD` | El único `DataSource` (HANA): jobs, servicios, `BAT.Programa.RutaSalida` |
| `JAVACALL_URL` | Router JavaCall de la plataforma (sin valor por defecto) |
| `INTERNAL_TOKEN` | Secreto de `/internal/**` (vacío: se rechaza todo) |
| `PLATAFORMA_BASE_URL` | Proxy TomEE de plataforma (por defecto `http://localhost:8080`) |

`tesoreria.jobs.sesion-hana=false` solo para una base que no sea HANA (las sentencias de sesión `SET SCHEMA BSCL` y
`SET TRANSACTION AUTOCOMMIT DDL OFF` se ejecutan en cada transacción de la corrida).

## Construir y probar

```
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl :tesoreria-jobs clean test
```

Las pruebas (`src/test/java/cl/bcs/tesoreria/jobs`, ver su `README.md`) usan H2 en memoria, mocks y archivos en un
directorio temporal; ninguna se conecta a HANA. `TRANSFORMATION_NOTES.md` tiene la transformación, las pruebas de
equivalencia, los 19 defectos legacy conservados y lo no probado.

## Código obsoleto del piloto

Las clases del primer intento (Spring Batch `Tasklet`, clientes REST) siguen en el árbol hasta que el dueño las borre; no
forman parte de la aplicación (el escaneo de componentes se limita a `wiring`, `adapter` y `web`). La lista está en
`TRANSFORMATION_NOTES.md`.
