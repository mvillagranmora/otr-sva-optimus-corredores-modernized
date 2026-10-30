# BcsReplica — encolado y respaldo de la replica Optimus -> externos

Migracion del modulo legacy `legacy/otr-sva-optimus-corredores/BSCL/Replica/JAVA/BcsReplica` (Java EE 8 / TomEE) a Spring Boot 4.1.1 / Java 25.

## Estado
**Pasos 1-3 de modernize-transform hechos: 469 pruebas de caracterizacion en verde (264 casos contra el golden del legacy) sobre una implementacion que replica el legacy bug a bug (D-REP-01..22).** Ver `TRANSFORMATION_NOTES.md` ("Replica: pruebas" y "Replica: implementacion") y `src/test/java/cl/bcs/replica/caracterizacion/README.md`. Falta el paso 4 (verificacion independiente) y la respuesta del experto a Q-REP-1..13.

## Estructura (`cl.bcs.replica`)
| Paquete | Contenido |
|---|---|
| `kit` | `Compuertas` (G-TRG, G-SEB, G-OPT, cerradas por defecto), `ServicioNoServidoException`, `Sql` (SQL de recursos, `exec`, `batch` con parametros nombrados), `EntradaDiaSiguiente` |
| `port.out` | 6 puertos de colaborador de Plataforma (`ConsultarEmpresaPort`, `ObtenerDiaSiguientePort`, `FeriadosPort`, `ConfiguracionSistemaPort`, `FechaSistemaPort`, `SecuenciaPort`), 3 puertos de escritura con compuerta (`TriggersReplicaPort`, `RespaldoSebraPort`, `RespaldoOptimusPort`) y `ColaboradoresNoPortados` (todo lanza `ServicioNoServidoException`) |
| `adapter.out.jdbc` | `TriggersReplicaJdbc`, `RespaldoSebraJdbc`, `RespaldoOptimusJdbc`: envian por la `Connection` recibida los SQL de `src/main/resources/cl/bcs/replica/sql/` (58, byte a byte del legacy) |
| `envio` | `EnviarTablaReplicaServicio` (`EnviarTablaReplica`) |
| `respaldo` | `ReplicaUtilsServicio` (`ReplicaUtils`) |
| `inicio` | `IniciarReplicaFull`, `IniciarReplicaDelta` |
| `domain` | `Workstack` |
| `config` | `ReplicaConfig`: cableado por defecto (compuertas cerradas, colaboradores no portados) |

No hay controladores REST ni endpoint nuevo: el legacy no los tiene (se invoca por `JavaCall` desde otros modulos).

## Que hace el legacy (y que se porto)
- `EnviarTablaReplica.logica(proceso, FechaSistemaEjecucion)`: para cada proceso de la pauta (INICIO_PAUTA, VALORIZAR_FFMM, PAUTA_PATRIMONIO, COPIA_PATRIMONIO_PARA_INHABIL, PAUTA_RESPALDAR_CUSTODIA, CAMBIO_FECHA_SISTEMA, VALORIZAR_CARTERA_RV_PRECIO_MERCADO, FIN_PAUTA) encola filas en `PLA.Replica.WorkStack` (las consume el job externo de replica), limpia e inserta el historico de cuenta inversion y custodia, copia los dias inhabiles consecutivos y, en INICIO/FIN_PAUTA, apaga y reactiva los triggers de replica en linea; FIN_PAUTA registra `PLA.ResumenReplica`.
- `ReplicaUtils`: respaldo mensual de 13 tablas TBC* desde las vistas Sebra, respaldo diario de 4 carteras, respaldo Optimus (cuenta inversion y custodia a los dias inhabiles) y `guardarEnvioPostPauta`.
- `IniciarReplicaFull` / `IniciarReplicaDelta`: encolan la replica completa o diferencial de la pauta.
- 58 SQL `EnviarTablaReplica_*` / `IniciarReplica_InsertWorkstack` (HANA).

Los triggers (CREATE/DROP TRIGGER), el respaldo Sebra (base externa) y el respaldo Optimus quedan detras de puertos con compuerta cerrada por defecto (`replica.compuertas.triggers`, `replica.compuertas.respaldo-sebra`, `replica.compuertas.respaldo-optimus`); los colaboradores de Plataforma no portados (ObtenerDiaSiguiente, ConsultarEmpresa, Feriados, ConfiguracionSistema, Utils.getFechaSistema/getSecuencia) tambien: su implementacion por defecto lanza `ServicioNoServidoException`.

## No se porta
`BD/Scripts/*` (14 scripts de instalacion: SQL Server, Oracle SURA, triggers por motor, 27.844 lineas), la interfaz `UI/ACC/cambiarClaveUsuarioReplica.*` y `BcsReplicaTests/MapeoTIPO_COMP_FACTest` (compara HANA contra PostgreSQL con conexiones reales). Detalle y preguntas abiertas en `TRANSFORMATION_NOTES.md`.

## Tecnologia
Spring Boot 4.1.1 / Java 25, Maven (`mvn -o test`), H2 para las pruebas. Puerto 8093. SAP HANA (driver `ngdbc`, runtime) con `HANA_URL`, `HANA_USER`, `HANA_PASSWORD`.
