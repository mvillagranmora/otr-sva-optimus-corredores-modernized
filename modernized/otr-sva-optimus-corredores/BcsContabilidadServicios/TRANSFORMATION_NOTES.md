# TRANSFORMATION_NOTES — BcsContabilidadServicios (Phase 1, piloto)

**Transformación:** Java EE 8 / TomEE 8.0.16 → Spring Boot 4.1.1 / Java 25
**Rama:** `contabilidad/caracterizacion` (commits `dfe8ac6` … `a6f66c2`)
**Fecha:** 2026-10-15
**Oráculo:** el código legacy (decisión del usuario: "derivar del código"; no hay oráculo externo).

> **La equivalencia se basa en trazas: el legacy no se pudo ejecutar aquí.** No hay TomEE 8 local, y QA GWJAVA responde
> 401 Invalid Session. Los valores esperados salen de leer el legacy, de arneses que ejecutan sus SQL con shims
> sobre H2 (`MODE=LEGACY`) y de archivos golden derivados del código. No hubo corrida contra HANA, SAP B1 ni QA, ni
> doble ejecución. `/code-modernization:modernize-verify` hará la verificación independiente.

---

## 1. Resultado y métricas de prueba

| Métrica | Valor |
|---|---|
| Comando | `cd modernized/otr-sva-optimus-corredores/BcsContabilidadServicios && mvn -o clean test` (JDK 25) |
| Tests ejecutados | **1.315** · 0 fallos · 0 errores · **0 omitidos** (1.297 de caracterización, 15 de la revisión de arquitectura y 3 de RULE-076) |
| Casos de equivalencia ejecutados | **1.297 casos de caracterización**: 32 golden de envío/reversa (EnvioContabilidad 11, GenerarJsonAsientoSap 4, ReversaPreComprobante 6, InterfazContable 11), 24 de paridad SQL legacy (`LegacySqlParityTest`) y 13 archivos golden de interfaz |
| Comparación legacy vs. nuevo (`compare.py`) | **34 comparaciones** (17 casos de desarrollo CONT-B-01..12 y CONT-B-30..35, salida y tablas): **33 iguales, 1 distinta** (`B35-salida`, reversa duplicada: el legacy lanza `Exception` y el nuevo responde ERROR `PRECOMPROBANTE_ANTERIOR_REPETIDO`, con tablas idénticas; es la desviación aprobada DEV-04 y está pendiente de que una persona la registre como `approvedDifference`). Lado legacy: el `EnvioContabilidad` y el `ReversaPreComprobante` originales, sin modificar, vía `OracleRunner`. Archivos en `analysis/otr-sva-optimus-corredores/equivalence/BcsContabilidadServicios/{cases.json,EQUIVALENCE.json,dev/}` |
| Código legacy retirado | 104 archivos Java, **20.440 líneas** (`BcsContabilidadServicios/src/cl/bcs/contabilidad/**`) |
| Código nuevo | 209 archivos main (18.002 líneas) · 121 archivos de test (31.229 líneas) |
| SQL | 357 SQL legacy copiados byte a byte · 17 copiados tal cual desde otros módulos del legacy (DEV-13, DEV-21) · 6 nuevos (DEV-12) |

| Paquete de test | WP | Tests |
|---|---|---|
| `servicios/` | G | 188/188 |
| `centraliza/` | E | 322/322 |
| `centralizagz/` | F | 251/251 |
| `generico/` | C | 144/144 |
| `interfaz/` | A | 114/114 |
| `informe/` | D | 207/207 |
| `envio/` | B | 46/46 |
| nivel superior (orquestador y otros) | — | 25/25 |
| `infrastructure/` (revisión de arquitectura) | — | 15/15 |
| `seguridad/` (RULE-076, P0) | — | 3/3 |

### Canarios (Step 3)

Los 8 canarios corrieron en una copia scratch, cada uno con un build limpio. La evidencia (cambio, log y XML de
surefire) está en `analysis/otr-sva-optimus-corredores/equivalence/canary/BcsContabilidadServicios-transform/<n>-<área>/`.
El código del repo no se modificó.

| # | Ruptura | Tests que fallaron |
|---|---|---|
| 1 | `CreaInterfazDiariaJob.java:77` `minusDays(1)` → `minusDays(2)` (este canario había sobrevivido en el verify anterior) | **10** / 114 |
| 2 | `DevengoCalculator.java:43` `signum() >= 0` → `> 0` | **7** / 144 |
| 3 | `soporte/Campo.java:16` `yyyy-MM-dd` → `yyyy-dd-MM` | **41** / 322 |
| 4 | `CentralizacionTesoreria.java:208` `HALF_UP` → `HALF_DOWN` | **2** / 251 |
| 5 | `CentralizacionOrchestrator.java:138` `errores +=` → `errores =` | **1** / 15 |
| 6 | `AsientoSapMapper.java:92-93` Debit ↔ Credit | **12** / 46 |
| 7 | `ConsultarOperacionesClienteRelacionadoService.java:73` `isAfter` → `isBefore` | **13** / 207 |
| 8 | `EnviarPreComprobanteService.java:71` `split(";")` → `split(",")` | **6** / 188 |

`Canary: 8 rupturas → 92 tests fallaron (todas detectadas)`. Las áreas con menor sensibilidad son el redondeo de
Tesorería (2) y el acumulado del orquestador (1); conviene reforzarlas. **Ojo:** el canario 5 y el side-by-side de §3
apuntan a `CentralizacionOrchestrator`, que en producción no se inyecta (la ruta JavaCall usa
`CentralizaMultiSeleccionService`); ver ARQ-M1.

---

## 2. Tabla de mapeo (legacy → target)

Rutas legacy relativas a `legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/`.
Rutas target relativas a `src/main/java/cl/bcs/contabilidad/`.

| Comportamiento | Legacy (archivo:líneas) | Target (archivo:líneas) | Tests |
|---|---|---|---|
| Despacho multiselección, J/P, B-03 | `CentralizaMultiSeleccion.java:1-496` (catch `:361-366`, vigentes `:240`) | `domain/service/CentralizacionOrchestrator.java:1-157` · `service/CentralizaMultiSeleccionService.java:1-397` | nivel superior, `centralizagz/CentralizacionOrchestrator*` |
| Base de centralizadores | `BcsCentralizacion.java:1-113` (`llenarResultado :100-111`) | `domain/service/CentralizadorSql.java:1-99` · `domain/service/soporte/*` | `centraliza/`, `centralizagz/` |
| 50 tipos `Centraliza*` (A–Z) | `Centraliza<Tipo>.java` (uno por tipo) | `domain/service/Centraliza<Tipo>.java` (mismo nombre) | `centraliza/` (A–F), `centralizagz/` (G–Z) |
| Tesorería, conversión CLP | `CentralizaTesoreria.java:1-485` | `domain/service/CentralizacionTesoreria.java` · `CentralizaTesoreria*.java` | `centralizagz/` |
| Persistencia CentralizaGenerico | `CentralizaGenerico.java:1-730` | `domain/service/CentralizaGenerico.java:1-385` · `infrastructure/persistence/CentralizaGenericoJdbcAdapter.java:1-246` | `generico/` |
| Devengos | `DevengoATasaPacto.java:1-133`, `DevengoATasaCompraCarteraPactada.java`, `DevengoCarteraPropiaLibreYGarantia.java` y sus SQL | `domain/service/devengo/DevengoCalculator.java:1-162` · `domain/service/Devengo*.java` | `generico/` |
| Interfaz diaria FI/BPC | `CreaBroadMovimiento.java:1-286`, `CreaBroadCuentas/Saldo/DescriptivasBroad/CuentasBPC/MovimientoBPC.java`, `ArchivoControl.java` y el job de `BcsContabilidadJobs` | `infrastructure/batch/CreaInterfazDiariaJob.java:1-134` · `infrastructure/interfaz/*` | `interfaz/` |
| Subida SFTP | `SFTPConnector.java:1-273` | `infrastructure/sftp/JschInterfazFileTransfer.java:1-150` · `KnownHostsVerifier.java` | `interfaz/InterfazSftpTest` |
| Envío a SAP B1 | `EnvioContabilidad.java:1-697` | `service/EnvioContabilidadService.java:1-511` · `infrastructure/sap/SapB1Gateway.java` | `envio/` |
| JSON del asiento | `GenerarJsonAsientoSap.java:1-358` | `service/GenerarJsonAsientoSapService.java` · `domain/service/AsientoSapMapper.java` | `envio/` |
| Reversa | `ReversaPreComprobante.java:1-260` | `service/ReversaPreComprobanteService.java:1-233` | `envio/` |
| Interfaz contable (job ASIENTO_SAP) | `BcsContabilidadJobs/.../InterfazContable.java` | `service/InterfazContableService.java` · `infrastructure/persistence/InterfazContableJdbcRepository.java` | `envio/` |
| Precomprobantes | `GeneraPreComprobanteMultiSeleccion.java:1-1001`, `EnviarPreComprobante.java:1-194` | `service/GeneraPreComprobanteMultiSeleccionService.java` · `service/EnviarPreComprobanteService.java` · `service/PreComprobantePostProcesoService.java` | `servicios/`, `envio/` |
| Informe cliente relacionado | `ConsultarOperacionesClienteRelacionado.java:1-441`, `GenerarInformeClienteRelacionado.java:1-646` | `service/ConsultarOperacionesClienteRelacionadoService.java` · `service/GenerarInformeClienteRelacionadoService.java:1-139` · `domain/service/clienterelacionado/*` · `infrastructure/informe/InformeClienteRelacionadoWriter.java` | `informe/` |
| Interfaz Basilea | `ProcesarInterfazBasilea.java:1-468`, `EditarCuentaBasilea.java` | `service/ProcesarInterfazBasileaService.java:1-127` · `domain/service/basilea/*` · `service/EditarCuentaBasileaService.java` | `informe/`, `servicios/` |
| Metadata, cuentas, conceptos normativos, cuadratura, homologación, anulación, carga de comprobantes | `AgregarMetadata.java`, `EditarMetadata.java`, `EliminarMetadata.java`, `AgregarCuentasPorMetadata.java`, `FiltrarCuentas.java`, `*ConceptoNormativo*.java`, `CuadraturaCierreDiario.java`, `HomologarCuentasContables.java`, `AnularCentralizacion.java`, `Cargar*Comprobante.java`, `CentralizaGeneraCabecera.java` | `service/<Nombre>Service.java` (mismo nombre) | `servicios/` |
| Carga de SQL | `BcsUtils SQL.java` (por classpath) | `infrastructure/persistence/SqlResources.java` (falla con `MissingSqlResourceException`) | `SqlResourcesTest` |
| Entrada JavaCall | despacho TomEE por reflexión | `adapter/in/InternalJavaCallController.java` (503 → fallback a TomEE) | nivel superior |

---

## 3. Side-by-side representativo (B-03: un tipo que falla no corta el lote)

Legacy `CentralizaMultiSeleccion.java:340-367` (izquierda) vs. `CentralizacionOrchestrator.java:131-146` (derecha). Sin
credenciales en ninguno de los dos rangos.

```text
entradaCentralizacion.p_FechaContable = entrada.p_FechaCon |   List<ResultadoCentralizacion> resultados = new ArrayList<>();
entradaCentralizacion.p_IdInforme = conf.IdInforme;        |   for (ConfiguracionTipoCentralizacion cfg : aProcesar) {
entradaCentralizacion.p_Ejercicio = entrada.p_Ejercicio;   |       if (!"J".equals(cfg.tipo())) continue;
entradaCentralizacion.p_SimulaOperacion = entrada.p_Simula |       try {
SalidaCentralizacion salidaCentralizacion = centralizacion |           SalidaCentralizacion salida = centralizadores.apply(cfg.servic…
out.P_OUT_ERRORESCONTADOR = salidaCentralizacion.P_OUT_ERR |           errores += salida.erroresContador();
out.P_OUT_MENSAJES.addAll(salidaCentralizacion.P_OUT_MENSA |           mensajes.addAll(salida.mensajes());
out.P_OUT_RESULTADO.addAll(salidaCentralizacion.P_OUT_RESU |           resultados.addAll(salida.resultados());
…entradaCentralizaGenerico.p_*.addAll(…)                   <       } catch (RuntimeException e) {
}catch(Exception e) {                                      |           // B-03 (legacy :361-366): logged, not counted
    log.error(e);                                          |           log.error("Centralizacion del tipo {} fallo", …, e);
    … p_TipoCentralizacion.add(tipoCentralizacion);        <       }
}                                                          |   }
```

- **Igual que el legacy:** la excepción se registra en el log, no suma al contador y el lote continúa.
- **Distinto, con decisión explícita:**
  - El contador acumula (`+=`), porque CONT-F-247 lo exige; el legacy asigna (`=`, defecto SME-01).
  - La cabecera de error de los tipos fallidos la arma `CentralizaMultiSeleccionService`.
  - El reflection (`Class.forName`) se reemplaza por un mapa de beans.

---

## 4. Desviaciones deliberadas respecto al legacy

### Aprobadas por el usuario (2026-10-14)

| Id | Desviación | Dónde |
|---|---|---|
| DEV-01 | La sesión con SAP B1 usa la cookie `B1SESSION`. Se envían todas las cookies del login como `name=value`; el legacy enviaba solo la última. | `infrastructure/sap/SapB1Gateway.java` |
| DEV-02 | La clave SAP nunca se registra ni se devuelve. `SapB1Config`, `SapSesion` y `SftpSettings` la enmascaran en `toString`. | `infrastructure/sap/*`, `infrastructure/sftp/SftpSettings.java` |
| DEV-03 | Se verifica el host key del SFTP: `StrictHostKeyChecking=yes` contra `SFTP_KNOWN_HOSTS`, y se lanza `SftpHostKeyException` si el archivo falta, está vacío, el host es desconocido o la clave no coincide. El legacy usaba `StrictHostKeyChecking=no`. | `infrastructure/sftp/*` · CONT-A-37 |
| DEV-04 | Error controlado en vez de crash: ids duplicados en la reversa (`PRECOMPROBANTE_ANTERIOR_REPETIDO`, `PRECOMPROBANTE_REVERSADO_MAS_DE_UNA_VEZ`), Basilea sin cuenta propia o con RUT no parseable, columnas obligatorias nulas (`DatoRequeridoException`, `InterfazDatoInvalidoException`) y `PLA.RutaBaseArchivos` en blanco. | varios |
| DEV-05 | B-03: una excepción de un tipo no suma al contador y el lote continúa (`CentralizaMultiSeleccion.java:361-378`). | `CentralizacionOrchestrator.java:141-144` |

### Decididas en esta transformación (para revisión)

| Id | Desviación | Motivo |
|---|---|---|
| DEV-06 | Cada tipo J se persiste en su propia transacción. El legacy hacía un único CentralizaGenerico al final para todos los tipos J. Si un tipo falla al persistir, recibe la cabecera de error y los tipos que ya terminaron quedan commiteados. | Los tests fijan la persistencia por servicio (pregunta abierta #15). Hay una sola implementación de los inserts. |
| DEV-07 | Los tipos P/J corren en secuencia; el legacy usaba `parallelStream`. | Orden determinista. |
| DEV-08 | Reflection reemplazado por inyección: mapa de beans `BcsCentralizacionService`. | Ciclo de vida de Spring, sin `Class.forName`. |
| DEV-09 | La `Linea` del asiento sigue el orden de definición; el legacy dependía del orden de un `HashMap`. Los conceptos del informe se ordenan por id (`TreeMap`), como fija CONT-D-23. | Determinismo. |
| DEV-10 | Los valores de plataforma se leen de forma perezosa desde `PlataformaPort`. Los mapas de tasas de Tesorería son locales a cada corrida. | Un singleton de Spring no puede tener estado por corrida. |
| DEV-11 | FechaProceso y HoraProceso salen de un `SELECT CURRENT_UTCTIMESTAMP` en cada corrida (el legacy lo cacheaba hasta 1 hora). En envío salen del `Clock` inyectado. | Sin estado obsoleto. |
| DEV-12 | 6 SQL nuevos: `CentralizaDevengo{ATasaCompraCarteraPactada,ATasaPacto,CarteraPropiaLibreYGarantia}_SelectCartera{Movimientos,Eventos}.sql`. Mantienen el FROM, JOIN y WHERE del legacy, y el cálculo pasó a `DevengoCalculator`. | Los exige CONT-C-91. Es la única excepción a "no inventar SQL". |
| DEV-13 | 2 SQL copiados tal cual de otros lugares del legacy: `ProcesarInterfazBasilea_SelectCuentaPropia.sql` (desde Custodia `ConsultarCustodiaPortafolio_ConsultarCtaPropia.sql`, verificado con `cmp`) y `GenerarInformeClienteRelacionado_SelectLogoCorredora.sql` (el texto inline de `GenerarInformeClienteRelacionado.java:38`). | Los requieren los tests. |
| DEV-14 | El `debug=true` hardcodeado del legacy (`GenerarInformeClienteRelacionado.java:165-166`, que redirige a `C:\__ARCHIVO_PRUEBA\`) pasa a la propiedad opcional `contabilidad.informe-cliente-relacionado.ruta-depuracion`, vacía por defecto. La ruta UI deja de escribir en esa carpeta. | Los tests fijan `debug=false`. Ver Q-07. |
| DEV-15 | SAP B1: TLS y hostname normales (el legacy confiaba en cualquier certificado), CardCode codificado en la URL y `Content-Type` JSON. | Seguridad. |
| DEV-16 | SFTP: rutas remotas absolutas, dos sesiones (borrado y subida), subida en orden, el archivo `Log` ya no se genera ni se sube, y los fallos del borrado remoto se registran. | CONT-A-30 exige exactamente 13 archivos. Pendiente de confirmar por el usuario. |
| DEV-17 | `CentralizaMultiSeleccion` ya no ejecuta el DELETE incondicional de datos-movimiento. Solo corre después del procedimiento almacenado. | Era destructivo. |
| DEV-18 | Los mensajes llevan como `descripcion` el código y sus valores (`"CODIGO {k=v}"`), porque el diccionario de mensajes de la plataforma no está en este módulo. | Los textos no coinciden con el legacy hasta migrar el diccionario. |
| DEV-19 | `GenerarJsonAsientoSap` queda enrutado y devuelve datos; el legacy devolvía null (CONT-B-24). HTTP 200 de `JournalVouchersService_Add` registra el texto de SAP en vez de un NPE. | |
| DEV-21 | **Unidad de trabajo por JavaCall** (`infrastructure/web/JavaCallUnitOfWorkFilter`): cada llamada a `/internal/java-call` corre en una transacción sobre una sola conexión. Hace commit solo si la respuesta es 2xx con `P_OUT_ERRORCOUNTER`/`P_OUT_ERRORESCONTADOR` = 0, y rollback en cualquier otro caso. Es la regla del legacy `BcsPlataformaWeb CallServlet.java:436-441,495`, que la primera versión había perdido (ARQ-H1); mantiene las tablas temporales globales de HANA en la sesión que las escribió (ARQ-H2). `persistirCentralizacion` pasa a `NESTED` (savepoint), para que B-03 siga funcionando. La configuración PLA y la empresa se leen por JDBC con los 15 SQL verbatim del legacy `BcsPlataformaServicios/.../pla/sql/` (`ConfiguracionSistema`, `ConsultarEmpresa_Select.sql`); solo "parámetro ausente" da el valor por defecto (ARQ-H4). | Revisión de arquitectura. |
| DEV-20 | Varios casos que en el legacy daban NPE se manejan sin caerse: GlosaDetalle o Memo nulos, tipo de socio nulo, monto nulo en ANCSPT/ANVSPT (se toma como 0), clave nula al agrupar y fecha de sistema nula (se trata como corrida histórica). | Error controlado (DEV-04). |

---

## 5. Defectos del legacy que se mantienen (para revisión de un SME)

Decisión del usuario: se mantienen tal cual y se listan. Cada uno está fijado por al menos un test.

| Id | Área | Defecto |
|---|---|---|
| SME-01 | Multiselección | `:347` asigna (`=`) el contador de errores del tipo. El servicio JavaCall conserva eso; el orquestador acumula porque lo exige CONT-F-247. |
| SME-02 | Multiselección | `FECHA_VALORIZACION_INVALIDA` no elimina nada, porque compara nombres con ids (`:266-270`). |
| SME-03 | Envío | **Montos duplicados en lotes mixtos D+R.** Los SQL detallado y resumido no filtran por formato, así que un precomprobante puede recibir líneas de los dos. SAP recibe un solo asiento cuadrado con cada monto dos veces (CONT-B-05: 4 líneas en vez de 2). Ver Q-05. |
| SME-04 | Envío | El FolioDTE se agrega dos veces cuando un movimiento tiene dos eventos (CONT-B-08). |
| SME-05 | Envío | Si el procedimiento de estado falla después de que SAP aceptó el asiento (204), el id queda marcado `E` aunque ya está contabilizado en SAP. |
| SME-06 | Envío | El ShortName por defecto es `"C"+rut` e ignora `PrefijoClienteSAP`. Un AcctCode duplicado en OACT hace fallar EnvioContabilidad, mientras GenerarJson se queda con el último. `U_BCS_IDPreComp` se envía como `"null"`. El socio se crea también en líneas de monto cero. Los reintentos que no son de login no esperan. |
| SME-07 | Envío | La rama `PRE_COMPROBANTE_DESCUADRADO` es código muerto, y los tipos de los precomprobantes rechazados igual aparecen en la salida. |
| SME-08 | Reversa | `PRECOMPROBANTE_YA_ESTA_REVERSADO` es ERROR, pero el contador queda en 0. Se consumen secuencias aun para ids inexistentes. El `Operador` del evento no se invierte. |
| SME-09 | CentralizaGenerico | El contador de errores siempre es 0 (`:83-88`). La clave de deduplicación de MovimientoDato concatena sin separador, así que `(1,"2X")` choca con `(12,"X")` (`:663`). El chequeo MAMEV se hace por IdMovimiento sin el tipo (`:485`). Un tipo inexistente termina en NPE (`:470/:559`). |
| SME-10 | Tesorería | La ventana de búsqueda de tasas retrocede `PLA.DiasFechaMinimaCalculoParidad` días por cada movimiento. Si falta la tasa o el monto del dólar observado, la conversión del resto se corta en silencio. El insert de comprobantes asociados corre también en simulación. |
| SME-11 | SpotUtilidadPerdida | Se traga todos los errores (0 errores, sin resultados) y libera el bloqueo aun si falla. |
| SME-12 | Facturas | No consulta nada fuera de CL/PE. FacturasDetallada solo genera datos en CL. |
| SME-13 | Devengo | Lee `CUS.UsaFechaLiquidacionEnVencimiento` y no la usa. IdTipoMovimiento `??????????` en el tipo 440. El nombre "Interes  Diario" va con doble espacio. `String.valueOf` produce el literal `"null"`. |
| SME-14 | Interfaz FI/BPC | `FEC-PROCESO` suma año+mes+día como enteros. `HRA-PROCESO` usa formato de 12 horas sin ceros. Los controles CTAS y MOV_CONT muestran siempre el epoch. Las filas se escriben en orden inverso y sin truncar, y null se imprime `null`. Los archivos se escriben con append, así que una re-corrida duplica. Se mantienen los errores tipográficos de las cabeceras, y la cabecera BPC de movimientos no calza con sus columnas. |
| SME-15 | Interfaz FI/BPC | `B1_DES_CC_PROD.TXT` usa el charset por defecto de la plataforma. El borrado remoto salta los nombres que empiezan con `.`. |
| SME-16 | Informe | La validación de rango deja pasar casi 4 meses (`:145`). Las fechas inválidas se aceptan sin error (2025-02-30 → 02-03-2025). Los préstamos nunca se deduplican (`SelectPrestamos.sql` no trae `FolioOperacion`). `FechaTermino` siempre es null (alias "FechFin"). Un monto nulo hace fallar el informe completo. |
| SME-17 | Basilea | La rama RF nunca asigna `CodigoMoneda`. `ClaseEmision` sale de `NemoBasilea` para acciones. |
| SME-18 | ConsultarCondiciones | El NPE con `IdTipoDatoEvento` nulo se captura y se reporta como errores 1. No hay WHERE: el input se ignora. |
| SME-19 | Mensajes | Se mantienen las erratas ("EXISTOSAMENTE") y el carácter U+FFFD en "configuración". |

---

## 6. Lo que no se migró

| Qué | Por qué |
|---|---|
| Disparadores programados: el job de interfaz diaria, InterfazContable (ASIENTO_SAP) y los batch del lado Jobs (`GenerarInformeClienteRelacionadoBatch`, Basilea) | Los beans existen pero nada los dispara. Siguen corriendo en TomEE hasta que exista el scheduler (Phase 1 `BcsContabilidadJobs`). |
| `CentralizaGenerico` invocado directo como JavaCall | Es interno a la multiselección; esa llamada cae a TomEE (503). |
| `RevisarRequisitosBasicos`, filas de auditoría AgregarLog/InsertarLogServicio, diccionario de mensajes | Pertenecen al módulo de plataforma (Phase 5). |
| `SapGateway.postJournalEntries` | No es un flujo del legacy; queda rechazado. |
| `Crypto.java` (Static3DES) | RULE-077, defecto de seguridad. |
| `Sesion.java main()` | RULE-076, código muerto con credenciales. |
| `GeneraPreComprobante`: alinear el algoritmo con el legacy | Ningún test de `envio/` lo ejercita. Queda abierto (Q-09). |
| `ObtenerDiaAnterior`, `ConsultarClearingFinanciero` e `InscribirProcesoBatch` | Siguen en TomEE por HTTP; esos endpoints aún no existen en la plataforma moderna, así que la llamada completa responde 503 y cae a TomEE (con rollback) hasta que la plataforma los exponga. |
| Pruebas end-to-end de los adaptadores JDBC, SFTP y Service Layer | No hay HANA, SAP B1 ni servidor SSH offline. Los 8 SQL nuevos y copiados (DEV-12, DEV-13) no se probaron contra HANA. |

---

## 7. Prerrequisitos de despliegue (entry criteria abiertos de la Phase 1)

Decisión del usuario (2026-10-14): son de despliegue y no bloquean la transformación, pero **sí bloquean cualquier
despliegue**.

1. **Rotación de credenciales (RULE-076 / RULE-077):** HANA MASTER01/MASTER02, las credenciales cifradas con
   Static3DES y las de `Sesion.java`.
   - Agregar el usuario y la clave de SAP que aparecen comentados en el legacy `BcsContabilidadJobs/.../InterfazContable.java:97-99` (no se copiaron). Hay que purgarlos del archivo y del historial.
   - La IP del host HANA QA y su clave estuvieron en `README.md` y en este archivo desde el commit `38abfb0`. Ya están enmascaradas, pero siguen en el historial git: rotar y, si el repo se comparte, limpiar el historial.
2. **API Gateway (Spring Cloud Gateway) desplegado en dev**, con rutas de Contabilidad y fallback a TomEE.
   **⚠ ARQ-H7:** `modernized/gateway/contabilidad-routes.yml` no sirve tal como está. Reescribe hacia
   `/contabilidad/centralizar` y `/contabilidad/jobs/crea-interfaz-diaria`, que no existen (el único endpoint es
   `/internal/java-call`), y su ruta `{IdServicio}` es literal, así que da 404 en vez de 503. Hay que reescribirlo
   o borrarlo antes de desplegar.
3. **Estrategia de consumidores de la API (§7b) y shim de compatibilidad** en el gateway.
4. **Configuración nueva** que debe existir en el entorno:
   - `SFTP_*` y `SFTP_KNOWN_HOSTS`;
   - `SAP_B1_URL`, `SAP_B1_COMPANY`, `SAP_B1_USER`, `SAP_B1_PASSWORD`;
   - `SAP_B1_DB_URL`, `SAP_B1_DB_USER`, `SAP_B1_DB_PASSWORD`;
   - `contabilidad.informe-cliente-relacionado.ruta-depuracion` (opcional).
   - Los nombres de `SAP_B1_DB_*` y `SFTP_KNOWN_HOSTS` fueron elegidos en esta transformación; confirmar.
5. **Diccionario de mensajes:** agregar `PRECOMPROBANTE_ANTERIOR_REPETIDO` y `PRECOMPROBANTE_REVERSADO_MAS_DE_UNA_VEZ`.
6. **Doble ejecución en staging** (exit criterion de la Phase 1): al menos 1.000 llamadas comparadas contra TomEE.
7. **⚠ ARQ-H5 (identidad de sesión):** el legacy (`CallServlet.java:680-683`) sobrescribía `IdEmpresa`,
   `p_IdEmpresa`, `IdUsuario` y `p_IdUsuario` con los valores de la sesión antes de cada JavaCall. El router moderno
   (`BcsPlataformaWeb/.../JavaCallRouterService.java:108-124`) los reenvía tal como llegan, y `docker-compose.yml`
   publica el puerto 8081. Antes de salir a producción hay que:
   - sobrescribir los ids en el router;
   - proteger `/internal/**` con una credencial entre servicios;
   - dejar de publicar el puerto.
   Hoy está latente, porque el `CallController` de plataforma responde siempre 503.
8. **⚠ ARQ-H6 (re-ejecución en TomEE):** el router cae a TomEE ante cualquier `RestClientException`, incluidos los
   5xx y los timeouts. Con DEV-21, un 500 o un 503 de este módulo implica rollback completo, así que la re-ejecución
   ya no duplica escrituras. Aun así, hay que limitar el fallback del router a 503 y conexión rechazada; un timeout
   de lectura puede llegar después de un commit.

> **Ojo:** en `MODERNIZATION_BRIEF.md` hay exit criteria de la Phase 1 marcados con fecha 2026-10-02 ("34/34 tests PASS",
> "All P0 rules … proven"). Esa evidencia correspondía a la versión anterior del módulo, que era un placeholder. No los
> modifiqué, porque los ticks son de una persona. Hay que revisarlos contra esta versión y contra el resultado de
> `modernize-verify`.

---

## 8. Preguntas abiertas para una persona

| Id | Pregunta |
|---|---|
| Q-01 | ¿`ROUND` en HANA es HALF_UP? (Lo suponen los devengos y Tesorería.) |
| Q-02 | ¿Zona horaria America/Santiago para las fechas de proceso? (El job usa el `Clock` de la zona por defecto de la JVM.) |
| Q-03 | ¿Cuál es el mapeo correcto NCE/NDR? |
| Q-04 | ¿Se corrige `FEC-PROCESO` (SME-14)? Lo consumen sistemas aguas abajo. |
| Q-05 | ¿Los montos duplicados en lotes mixtos D+R (SME-03) son intencionales? |
| Q-06 | ¿Hay que purgar el historial y rotar la clave del comentario de `InterfazContable.java:97-99` del legacy? |
| Q-07 | ¿Qué hace la UI con `RutaArchivo` del informe de cliente relacionado, ahora que no se escribe en `C:\__ARCHIVO_PRUEBA\` (DEV-14)? |
| Q-08 | ¿Se aceptan la persistencia por tipo y su atomicidad parcial (DEV-06, pregunta #15)? |
| Q-09 | ¿Se alinea `GeneraPreComprobante` con el algoritmo del legacy? |
| Q-10 | ¿Qué charset debe usar `B1_DES_CC_PROD.TXT` (SME-15)? |
| Q-11 | ¿Se confirman las desviaciones SFTP de DEV-16? |
| Q-12 | El repo git de `legacy/otr-sva-optimus-corredores` muestra `.claude/settings.json` borrado y locks de `.gradle` modificados. Esta transformación no los tocó. ¿Se restauran? |

---

## 9. Revisión de arquitectura (Step 5)

Revisor: subagente `architecture-critic`, 2026-10-15. Encontró 7 HIGH, 8 MEDIUM y 7 LOW.

### HIGH

| Id | Hallazgo | Estado |
|---|---|---|
| ARQ-H1 | Faltaba la regla del legacy de una transacción por llamada con rollback si el contador es mayor que 0; había commits parciales. | **Corregido** (DEV-21). 9 tests en `infrastructure/web/JavaCallUnitOfWorkFilterTest`. |
| ARQ-H2 | Las tablas temporales globales de HANA se escribían y leían en conexiones distintas del pool. | **Corregido** por DEV-21: una conexión por llamada. |
| ARQ-H3 | `HomologarCuentasContables` dependía de un bean `sapJdbc` que nadie define, así que en producción siempre devolvía "configuración no encontrada". | **Corregido.** Hay un único pool SAP B1 perezoso (`infrastructure/sap/SapB1Jdbc`, Hikari) compartido por la interfaz diaria, el envío y Homologar; también cubre ARQ-M3. Verificado arrancando el contexto real. |
| ARQ-H4 | El cliente de plataforma llamaba a endpoints que no existen y convertía cualquier falla de configuración en el valor por defecto. | **Corregido:** la configuración y la empresa se leen por JDBC (DEV-21), el HTTP tiene timeouts y una falla da 503 de fallback. 6 tests en `infrastructure/plataforma/PlataformaConfiguracionJdbcTest`. |
| ARQ-H5 | El router no impone la identidad de sesión (`p_IdEmpresa`/`p_IdUsuario`). | **Abierto, fuera del módulo** (`BcsPlataformaWeb`, `docker-compose.yml`). Bloquea el despliegue (§7 punto 7). |
| ARQ-H6 | Un 500 después de un commit hace que el router re-ejecute la llamada en TomEE. | **Mitigado** en el módulo por DEV-21. El ajuste del router queda **abierto, fuera del módulo** (§7 punto 8). |
| ARQ-H7 | `gateway/contabilidad-routes.yml` apunta a rutas que no existen. | **Abierto, fuera del módulo.** Bloquea el despliegue (§7 punto 2). |

### MEDIUM y LOW (pendientes)

| Id | Hallazgo |
|---|---|
| ARQ-M1 | `CentralizacionOrchestrator` (y `CentralizacionUseCase` con sus modelos) es un `@Service` que nadie inyecta: duplica `CentralizaMultiSeleccionService` con otra regla de contador. Borrarlo, o hacerlo la única implementación, y repetir el canario 5 sobre la ruta de producción. |
| ARQ-M2 | `ObjectProvider` y los constructores públicos pensados para tests debilitan beans en silencio (`CentralizaMultiSeleccion`, `EnviarPreComprobante`, `GeneraPreComprobante`, `EditarCuentaBasilea`). Usar un constructor único con dependencias obligatorias. |
| ARQ-M3 | La base SAP B1 se abría sin pool (`DriverManagerDataSource`). **Corregido junto con ARQ-H3.** |
| ARQ-M4 | Zona horaria (Q-02): hay `Clock.systemDefaultZone()` y `Clock.systemUTC()` mezclados, y el contenedor corre en UTC. Definir un solo `Clock` con `ZoneId` configurable. |
| ARQ-M5 | Ningún test levanta el contexto de Spring. Agregar un `@SpringBootTest` que resuelva las 26 rutas; la verificación que hice a mano está en el scratchpad. |
| ARQ-M6 | Los puertos dejan ver infraestructura (`MapSqlParameterSource` y nombres de SQL en `ContabilidadRepository`). Con SQL verbatim, los transaction scripts son el diseño honesto: eliminar los puertos que tienen una sola implementación. |
| ARQ-M7 | El informe se escribe en el disco del contenedor; hace falta un volumen compartido con TomEE y la UI (relacionado con Q-07). |
| ARQ-M8 | `spring-boot-starter-batch` no se usa, y `spring.batch.jdbc.initialize-schema=always` es peligroso contra HANA. Eliminar ambos. |
| ARQ-L1–L7 | El constructor de 26 argumentos del controller, que conviene reemplazar por una interfaz `JavaCallHandler`. `@ConditionalOnMissingBean` fuera de auto-config. El `ScopedValue` es innecesario: basta con devolver los mensajes. JSON mal tipado da 500 en vez de 400. `SapB1DatabaseSettings.toString` imprime la URL. El tipo de host key de JSch. El Dockerfile corre como root y omite los tests. |

## 10. Seguimiento para el siguiente módulo

1. **`BcsContabilidadJobs`:** agregar los disparadores (scheduler) para `CreaInterfazDiariaJob`, `InterfazContableService` y los batch de informe y Basilea.
2. **Plataforma (Phase 5):** migrar el diccionario de mensajes; con eso se cierra DEV-18.
3. **Adaptadores:** pruebas de integración contra HANA, SAP B1 Service Layer y SFTP en un entorno de staging.
4. **Canarios débiles:** reforzar los tests del redondeo de Tesorería (canario 4: 2 fallos) y del acumulado del orquestador (canario 5: 1 fallo).
5. **`ContabilidadRepository`:** tiene muchos métodos abstractos; cada doble de test escrito a mano debe implementarlos todos.
6. **Otros módulos (repo):** el `.gitignore` excluía `src/**/out/`. Ya se corrigió, pero ~25 módulos tienen paquetes `port/out` y `adapter/out` sin versionar y deben commitearse aparte.

### Actualización 2026-10-19 (ARQ-H5, H6, H7)

- **ARQ-H5, cerrado en `BcsPlataformaWeb`:** `JavaCallRouterService` impone `IdEmpresa`, `p_IdEmpresa`, `IdUsuario` y `p_IdUsuario` con los valores de la sesión (solo si la clave viene en la petición, como `CallServlet.java:680-683`) y envía `X-Internal-Token` si `plataforma.javacall.internal-token` está configurado. Los puertos ya no se publican: `docker-compose.yml` usa `expose`. **Falta:** que cada `InternalJavaCallController` de los servicios valide ese header; hoy ninguno lo hace.
- **ARQ-H6, cerrado:** el fallback a TomEE solo ocurre ante HTTP 503 o conexión rechazada; un 500, un 4xx o un timeout de lectura se propaga y no se re-ejecuta. Tests en `JavaCallRouterServiceTest` (14 en la clase, 0 fallos).
- **ARQ-H7, pendiente de una persona:** la carpeta `modernized/otr-sva-optimus-corredores/gateway/` (solo `contabilidad-routes.yml`) debe borrarse; su contenido ya está en `BcsGateway/src/main/resources/application.yml`. Lo borra el dueño del repo.
