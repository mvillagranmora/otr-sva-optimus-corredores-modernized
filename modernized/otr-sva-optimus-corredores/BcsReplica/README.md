# BcsReplica — encolado y respaldo de la replica Optimus -> externos

Migracion del modulo legacy `legacy/otr-sva-optimus-corredores/BSCL/Replica/JAVA/BcsReplica` (Java EE 8 / TomEE) a Spring Boot 4.1.1 / Java 25.

## Estado
**Paso 1 de modernize-transform: pruebas de caracterizacion en rojo, sin codigo de produccion de la logica.** Ver `TRANSFORMATION_NOTES.md` ("Replica: pruebas") y `src/test/java/cl/bcs/replica/caracterizacion/README.md`.
El contenido de `src/main` es el esqueleto de una iteracion anterior: NO corresponde al legacy (tablas `WORKSTACK_T` y `CONFIGURACION_T` inexistentes, endpoints REST sin equivalente, `EnviarTablaReplica` marcada "no portable") y se reemplaza en el paso 3.

## Que hace el legacy (lo que se va a portar)
- `EnviarTablaReplica.logica(proceso, FechaSistemaEjecucion)`: para cada proceso de la pauta (INICIO_PAUTA, VALORIZAR_FFMM, PAUTA_PATRIMONIO, COPIA_PATRIMONIO_PARA_INHABIL, PAUTA_RESPALDAR_CUSTODIA, CAMBIO_FECHA_SISTEMA, VALORIZAR_CARTERA_RV_PRECIO_MERCADO, FIN_PAUTA) encola filas en `PLA.Replica.WorkStack` (las consume el job externo de replica), limpia e inserta el historico de cuenta inversion y custodia, copia los dias inhabiles consecutivos y, en INICIO/FIN_PAUTA, apaga y reactiva los triggers de replica en linea; FIN_PAUTA registra `PLA.ResumenReplica`.
- `ReplicaUtils`: respaldo mensual de 13 tablas TBC* desde las vistas Sebra, respaldo diario de 4 carteras, respaldo Optimus (cuenta inversion y custodia a los dias inhabiles) y `guardarEnvioPostPauta`.
- `IniciarReplicaFull` / `IniciarReplicaDelta`: encolan la replica completa o diferencial de la pauta.
- 58 SQL `EnviarTablaReplica_*` / `IniciarReplica_InsertWorkstack` (HANA).

Los triggers (CREATE/DROP TRIGGER), el respaldo Sebra (base externa) y el respaldo Optimus quedan detras de puertos con compuerta cerrada por defecto (`replica.compuertas.triggers`, `replica.compuertas.respaldo-sebra`, `replica.compuertas.respaldo-optimus`); los colaboradores de Plataforma no portados (ObtenerDiaSiguiente, ConsultarEmpresa, Feriados, ConfiguracionSistema, Utils.getFechaSistema/getSecuencia) tambien: su implementacion por defecto lanza `ServicioNoServidoException`.

## No se porta
`BD/Scripts/*` (14 scripts de instalacion: SQL Server, Oracle SURA, triggers por motor, 27.844 lineas), la interfaz `UI/ACC/cambiarClaveUsuarioReplica.*` y `BcsReplicaTests/MapeoTIPO_COMP_FACTest` (compara HANA contra PostgreSQL con conexiones reales). Detalle y preguntas abiertas en `TRANSFORMATION_NOTES.md`.

## Tecnologia
Spring Boot 4.1.1 / Java 25, Maven (`mvn -o test`), H2 para las pruebas. Puerto 8093. SAP HANA (driver `ngdbc`, runtime) con `HANA_URL`, `HANA_USER`, `HANA_PASSWORD`.
