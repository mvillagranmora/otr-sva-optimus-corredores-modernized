# BcsContabilidadJobs

## Descripción

Los cinco jobs del legacy `BSCL/Contabilidad/JAVA/BcsContabilidadJobs`, ejecutados **en proceso** con el contrato
del `BcsJob` legacy (una transacción por corrida; `true` = commit, `false` = rollback, excepción = rollback + estado de
error). El Job Manager ya no instancia la clase por nombre: llama a `POST /internal/job-call`. No hay `@Scheduled`.

| Clase legacy (`clase`) | Qué hace |
|---|---|
| `cl.bcs.contabilidad.jobs.CentralizaMultiSeleccionJob` | Centralización multi-selección (servicio de BcsContabilidadServicios) |
| `cl.bcs.contabilidad.jobs.CreaInterfazDiaria` | Interfaz SIGA diaria: 9 archivos FI + 4 BPC con la fecha de ayer, subida SFTP |
| `cl.bcs.contabilidad.jobs.CreaInterfazDemanda` | Interfaz SIGA a demanda bajo `APPS/GAB1/`, fecha del parámetro `FechaEjecucion` |
| `cl.bcs.contabilidad.jobs.InterfazContable` | Envío de precomprobantes a SAP B1 (Service Layer) y log `ITZ/<ts>_InterfazContable.log` |
| `cl.bcs.contabilidad.jobs.GenerarInformeClienteRelacionadoBatch` | Informe de clientes relacionados y `RutaSalida` |

## Tecnología

- Spring Boot 4.1.1 / Java 25, puerto 8082 (solo red interna).
- Artifact `cl.bcs:bcs-contabilidad-jobs`.
- Usa **BcsContabilidadServicios como librería** (`cl.bcs:contabilidad-service`, su jar plano; el ejecutable de
  Servicios es `*-exec.jar`). Servicios, generadores de interfaz, `SapB1Gateway`, `SapB1Jdbc` e
  `InterfazContableService` se reutilizan; no hay un segundo cliente SAP ni un segundo `DataSource`.

## Endpoint

`POST /internal/job-call`

```json
{"clase": "cl.bcs.contabilidad.jobs.InterfazContable", "idPrograma": 5150,
 "parametros": {"IdEmpresa": "1", "IdUsuario": "14", "SecCmbte": "MTAw", "Destino": "B"}}
```

| Caso | Respuesta |
|---|---|
| COMMIT o ROLLBACK | 200 `{clase, idPrograma, resultado, error:false, mensaje:""}` |
| ERROR (excepción del job) | 500 `{..., resultado:"ERROR", error:true, mensaje:<mensaje de la excepción>}` |
| Clase no migrada a este módulo | 503 `{error:true, clase, mensaje}` (el Job Manager legacy la sigue ejecutando) |
| JSON inválido, sin `clase`, sin `idPrograma` o `idPrograma` no numérico | 400 |

Los valores de `parametros` se pasan como texto; los `null` se descartan. Un ROLLBACK responde 200 con
`error:false`, igual que el legacy (`BcsJob.java:151`). `GET /actuator/health` también responde.

## Variables de entorno

Sin valores por defecto para credenciales. Se leen al primer uso: la aplicación arranca sin ellas y solo falla el job
que las necesita.

| Variable | Uso |
|---|---|
| `HANA_URL`, `HANA_USER`, `HANA_PASSWORD` | El único `DataSource` (HANA): jobs, servicios y `BAT.Programa.RutaSalida` |
| `SAP_B1_URL`, `SAP_B1_USER`, `SAP_B1_PASSWORD`, `SAP_B1_COMPANY` | Service Layer de SAP B1 (InterfazContable) |
| `SAP_B1_DB_URL`, `SAP_B1_DB_USER`, `SAP_B1_DB_PASSWORD` | Base de datos SAP B1 (interfaces SIGA, plan de cuentas) |
| `SFTP_HOST`, `SFTP_PORT`, `SFTP_USER`, `SFTP_PASSWORD`, `SFTP_KNOWN_HOSTS` | Subida SFTP de las interfaces SIGA |
| `PLATAFORMA_BASE_URL` | Proxy TomEE de plataforma (operaciones aún no migradas) |

## Construir y probar

```
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
mvn -o -f modernized/otr-sva-optimus-corredores/pom.xml -pl BcsContabilidadJobs -am clean test
```

Las pruebas (`src/test/java/cl/bcs/contabilidad/jobs`, ver su `README.md`) usan H2 en memoria, mocks y un Service
Layer falso en localhost. Ninguna se conecta a SAP, HANA, SFTP, QA ni desa02. Detalle de la transformación,
desviaciones y defectos legacy conservados: `TRANSFORMATION_NOTES.md`.
