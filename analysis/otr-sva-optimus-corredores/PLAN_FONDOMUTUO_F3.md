# Plan F3: FondoMutuo, lo que queda después de F1 y F2

Fecha: 2026-10-26. Estado: **PLAN para aprobación (sin código)**. No se tocó legacy, modernized ni HANA al producirlo; no se ejecutó ninguna consulta HANA (se usó el export de solo lectura ya existente `hana_qa_results/pla_servicio_filas.csv`, del 2026-10-21) y se leyó el código legacy.
Fuentes: `PLAN_FONDOMUTUO_F2.md` (secciones 12, 16, 17: decisiones), `RULES_FONDOMUTUO_F2_REINVERSION.md`, `TRANSFORMATION_NOTES.md` y `RUNBOOK_LISTAS.md` de BcsFondoMutuoServicios, `PRIORIDAD_POR_USO.md`, fuente legacy de `BcsFondoMutuoServicios` y `BcsJobsFondoMutuo`.

## 0. Resumen y corte recomendado

1. **Qué hay hecho:** F1 (Manejador y `CrearSolicitudAporteRescateNormal`, con su cierre) y F2 W1 (libreria Tesoreria en proceso y `CrearSolicitudRescateReinversion`) están PROVEN y firmados (verify del 2026-10-26). Sirven por configuración y solo con firma del experto; la lista `RESCATE_REINVERSION` no sirve mientras la custodia esté cerrada.
2. **Qué queda en el registro:** 68 clases Java (tipo J) de FondoMutuo en `PLA.Servicio`, 3 migradas por nombre (`ManejadorSolicitudAporteRescate`, `CrearSolicitudAporteRescateNormal`, `CrearSolicitudRescateReinversion`), más las del cierre de F1 que viven dentro del módulo. Las 177 filas de FondoMutuo incluyen además 100+ servicios tipo Q (vistas) y P (procedimientos) que **viven en HANA y no se migran**.
3. **Corte recomendado: empezar por las lecturas de la UI (W1 de F3).** Ocho clases solo de lectura reúnen **37.661 usos** acumulados (cuatro de las diez más llamadas del dominio J), no escriben nada, no tocan Custodia ni Tesorería y casi no tienen colaboradores externos. Es el mayor valor con el menor riesgo.
4. **Lo pesado y valioso viene después:** `CertificarSolicitudAporteRescate` (3.052 líneas, ~400 decisiones) es el eje del ciclo de vida de la solicitud y la dependencia que el usuario fijó para destrabar `AporteReinversion` (decisión de F2 del 2026-10-22). Se planifica como W3, con su propia extracción de reglas antes de escribir código.
5. **El valor de tráfico real es incierto** (R-F3-1): el contador de uso es acumulado en QA, y el grueso del volumen histórico de reinversión viene de cargas masivas y jobs que llaman estas clases **en proceso**; no entra por el router. Migrar la clase no mueve ese tráfico hasta migrar también al llamador (p. ej. `BcsJobsFondoMutuo`, 1.928 líneas).

## 1. Reglas vinculantes (se repiten tal cual de F2; cualquier cambio exige su aprobación)

1. **Ningún cambio de base de datos, de ningún tipo.** Mismos objetos, mismos SQL. Los dos DDL de sesión ya aceptados (`#p_BloqueoSolicitudAporteRescate`, `#p_BloqueoProcesarReinversion`) son los únicos; cualquier otro hay que pedirlo.
2. **HANA solo SELECT de solo lectura y solo si es estrictamente necesario**, con las herramientas existentes de `analysis/otr-sva-optimus-corredores/tools`; cada consulta nueva se propone con su texto exacto antes de ejecutarla.
3. **El legacy nunca se modifica.** Defectos del legacy bug a bug, listados para el experto; las desviaciones DEV-F1..F7 y DEV-6-H1 aprobadas aplican; cualquier DEV nueva requiere aprobación antes de implementarse.
4. **Sin secretos del legacy como fixtures** (varias clases traen `main()` con `desa01` y JSON con ids reales: no se copian; el test RULE-076 se extiende a las clases nuevas).
5. **Colaboradores aún no migrados: HTTP detrás de puertos** (Custodia, Mesa, Plataforma, Facturación, Normativa, Portafolio, SocioNegocio). Los migrados y PROVEN (Tesorería, CuentaInversión, APV) en proceso como librería.
6. **Commits sin línea de coautor.** Producción Linux: LF, zona horaria, charset y locale fijados; pruebas en una JVM UTC.
7. **Ningún tráfico real hasta PROVEN y firmado**; enrutamiento opt-in por clase (`plataforma.javacall.clases-habilitadas`, vacío = todo a TomEE). Guarda de jobs fail-closed (`JOBS_PATTERN.md`, paso 6) para las clases de escritura que llame un job migrado.

## 2. Inventario por grupo (uso = contador acumulado de QA, no criticidad; "dec." = decisiones estimadas por conteo de `if/for/while/case/catch/&&/||/?`, no es lizard)

### A. Lecturas de la UI (solo SELECT)
| Clase | LOC | Uso | Dec. | Colaboradores fuera del paquete |
|---|---:|---:|---:|---|
| `ConsultarFechaSolicitudAporteRescate` | 375 | 16.217 | 24 | `Fecha`, `SQL`, `Utils`, plataforma.pla; usa `CalcularFechaSolicitudAporteRescate` y `ObtenerPerfilFondoMutuo` (ya migradas en el cierre de F1) |
| `ConsultarCustodiaPortafolioFondoMutuo` | 1.164 | 8.886 | 34 | plataforma.mer y pla; `CalcularMontoNumeroCuota` (migrada); no escribe |
| `ConsultarSolicitudesFFMM` + `ConsultarSolicitudesFFMMWS` | 479 + 485 | 4.261 + 4.261 | 22 + 22 | solo plataforma y utilidades; son casi gemelas |
| `ConsultarFondoMutuoReinversion` | 142 | 1.773 | 2 | solo plataforma |
| `ConsultarInstrumentosFondosMutuos` | 293 | 1.183 | 5 | solo plataforma |
| `ConsultarDetalleSolicitud` | 367 | 693 | 3 | solo plataforma |
| `ConsultarValorDolarFondoMutuo` | 213 | 387 | 7 | plataforma.mer y pla |
| **Total grupo** | **~3.700** | **~37.700** | | |

### B. Ciclo de vida de la solicitud (escribe)
| Clase | LOC | Uso | Dec. | Colaboradores fuera del paquete |
|---|---:|---:|---:|---|
| `CertificarSolicitudAporteRescate` | 3.053 | 894 | ~396 | CuentaInversión `CrearCargoAbono` (en proceso), Tesorería `CrearIngresoEgreso` (en proceso), **Facturación** `CrearComprobante` y `CrearMovimientoComprobante` (HTTP), **Mesa** `CrearOperacionSpot` (HTTP, G2), **Normativa** `ResumenPatrimonial` (HTTP), plataforma `CalcularParidad`, `AgregarLog`, `SaldoFlujoCaja`, `NotificarEstadoOrdenesOWP`, `EntregarPreciosTasas`, `RevisarRequisitosBasicos` (HTTP); invoca `CrearSolicitudAporteReinversion` |
| `CertificarPorFiltrosSolicitudAporteRescate` | 154 | 51 | 2 | delgada: llama a la anterior |
| `LiquidacionSolicitudAporteRescate` | 765 | 450 | ~76 | CuentaInversión `CrearCargoAbono`, Tesorería `CrearIngresoEgreso` (en proceso), APV `CalculoRetencionTraspasos` |
| `AnulacionSolicitudAporteRescateFolioContrato` | 223 | 277 | 12 | llama a `AnulacionSolicitudAporteRescate` (cierre de F1) |
| `ProcesarReinversionRescate` | 2.505 | 623 | ~233 | Custodia `ConsultarFlujoCustodia` (ya migrada como slice, PROVEN), Portafolio `IngresaPreferenciaPagoPortafolio` (HTTP); la llaman `CrearSolicitudRescateReinversion` y `CrearAporteCSC` |
| `CrearSolicitudAporteReinversion` (diferida desde F2) | 1.496 | 0 | ~225 | 28 tarjetas ya extraídas; la crea `Certificar` |

### C. Cargas
| Clase | LOC | Uso | Dec. | Notas |
|---|---:|---:|---:|---|
| `CargarValorCuota` | 673 | 923 | ~47 | escribe valores cuota; solo plataforma (`ObtenerDiaAnterior`, `AgregarLog`, `RevisarRequisitosBasicos`) |
| `CargarCustodiaFondoMutuo`, `CargarArchivoCuadratura*UI`, `CuadraturaCierreDiario` | 206, 143, 108, 340 | 4, 0, 5, 14 | 12–17 | uso marginal |

### D. Traspasos
`CrearTraspaso` (511, 95 usos), `ModificarTraspaso` (427, 86), `ConfirmarTraspaso` (493, 65), `RecaudacionTraspaso` (1.058, 87; Tesorería `CrearComprobante`, `CrearIngresoEgreso`, `LiquidarComprobante`, CuentaInversión), `AnulacionTraspaso` (251, 11). ~2.740 líneas, ~340 usos; la recaudación depende de Tesorería en proceso (ya disponible).

### E. Contratos CSC (aporte programado con mandato)
`CrearIngresoCSC` (1.004, 127) y su Manejador (542, 135), `ModificarCSC` (701, 40) y su Manejador (180, 40), `CrearAporteCSC` (1.860, 14), `RevocacionContratoCSC` (317, 25) y Manejador (238, 0), `AnulacionContratoCSC` (270, 3). ~5.100 líneas, ~380 usos; dependen de mandatos PAC de Tesorería y de APV `ObtenerFolioSuscripcion`.

### F. Otras de uso marginal o nulo
`CrearAporteSAAPV` (1.471, 53; 8+ clases de APV), `CrearIngresoRetiroTituloFondoMutuo` (801, 65) y `...Garantias` (745, 5) (custodia), `CrearSolicitudAportePorDividendosCFI` (1.106, 0), `SimulaCalculoSpreadRescate` (848, 1), comisiones NN y administración de comisiones (351 + 294, 0), `ActualizarFondoIndustria` (303, 0), `ActualizarFechaGestionOperaciones` (609, 0), `ConfigurarFondoAdministracionLiquidez` (246, 7), las `CrearSolicitud*` de F2 sin uso (Programado, Overnight, CambioSerie, Transferencia, Traspaso, `*PorCliente`, `RescateReinversion1N`). Con 0 usos directos: **no se portan salvo que el negocio confirme uso** (D-F2-4, vigente).

### G. Módulo de jobs
`BcsJobsFondoMutuo` (1.928 líneas, módulo aparte): por ejemplo `BcsJobCertificacionMasivaSolicitudes` llama a `CertificarSolicitudAporteRescate`; es la ruta por la que sigue entrando el volumen masivo. Se migra después de las clases que llama, con la guarda fail-closed.

## 3. Orden recomendado y olas (máximo 2 agentes en paralelo)

| Ola | Contenido | Depende de | Cierre |
|---|---|---|---|
| **W0** (sin código) | Decisiones D-F3-1..6; extracción de reglas: agente A = grupo A (lecturas) + `CargarValorCuota`; agente B = `CertificarSolicitudAporteRescate` + `Liquidacion` + `Anulacion` (reutilizando las 28 tarjetas de `AporteReinversion`); consultas HANA de solo lectura solo si se aprueban una a una | nada | `RULES_FONDOMUTUO_F3.md` + decisiones |
| **W1: lecturas** | las 8 clases del grupo A, mismo kit y oráculo de F1/F2 (clases compiladas del legacy sin modificar sobre H2, dobles registrados, goldens, entradas frescas, canarios) | W0 | lista de clases de lectura abiertas por configuración, tras firma |
| **W2: valor cuota** | `CargarValorCuota` (escribe; guarda de repetición) | W1 | equivalencia propia |
| **W3: ciclo de vida** | `CertificarSolicitudAporteRescate` en frentes (cabecera y reglas, Tesorería/CuentaInversión en proceso, Facturación/Mesa/Normativa/plataforma por puertos HTTP con compuertas cerradas), `Liquidacion`, `Anulacion`, `CertificarPorFiltros`; después `CrearSolicitudAporteReinversion` y el helper de Spot (G2 cerrada) | W0, W1 | veredicto por clase; destraba AporteReinversion |
| **W4: reinversión por lote** | `ProcesarReinversionRescate` (usa `ConsultarFlujoCustodia` ya migrada y Portafolio por HTTP) | W3 | |
| **W5: traspasos** | familia D | W3 | |
| **W6: CSC** | familia E (solo si D-F3-5 confirma uso) | W5 | |
| **Jobs** | `BcsJobsFondoMutuo` con guarda fail-closed | W3..W6 | |
| **Cierre F3** | revisión de arquitectura y seguridad, cableado del router y runbook por ola, `modernize-verify` por clase, `SIGNOFF.md` del experto | todas | |

Tamaños (planificación, no compromiso): W0 mediano; W1 mediano (mucho SQL, poca lógica); W2 chico; W3 **muy grande** (3.052 líneas con ~400 decisiones; se parte en frentes como RescateReinversion); W4 grande; W5 mediano; W6 grande y condicionada.

## 4. Cómo se prueba (igual que F1 y F2)
Clases compiladas del legacy sin modificar en un directorio temporal; esquema H2 con tipos reales del catálogo; dobles registrados para lo que está fuera del cierre; goldens grabados solo ejecutando el legacy; entradas frescas con semilla fija en ambos lados; mutación de goldens; canario por ola (≥ 5 mutaciones en copia temporal); N = 20 corridas por caso donde hay `parallelStream`/`List2Hash`; un job único que re-ejecuta todos los goldens previos; JaCoCo ≥ 80 % de ramas por clase; comparación con `compare.py` (el script decide, no yo). Cada ola agrega casos; no hay un examen único al final.
**Lo que no se puede probar** (se declara en cada veredicto): todo lo que dependa de HANA real (autonomía y concurrencia de procedimientos, tipos y escalas, secuencias, DDL de sesión), los efectos reales de Facturación, Mesa, Normativa y custodia (solo dobles), y el volumen real que entra por los jobs.

## 5. Decisiones que necesito de usted

| Id | Pregunta | Recomendación |
|---|---|---|
| D-F3-1 | ¿Aprueba empezar por las lecturas de la UI (W1) y dejar `Certificar` para W3? | Sí. |
| D-F3-2 | ¿Se extraen las reglas de `Certificar` + `Liquidación` + `Anulación` en W0 aunque se codifiquen más tarde? | Sí: es la mayor incertidumbre del plan y no cuesta código. |
| D-F3-3 | Colaboradores de Facturación, Mesa y Normativa: ¿HTTP detrás de puertos con compuertas cerradas hasta que existan sus módulos, como en F2? | Sí. |
| D-F3-4 | ¿Se confirma que las clases con 0 usos directos (grupo F y las de F2) quedan fuera? | Sí, salvo que el negocio confirme uso. |
| D-F3-5 | CSC (grupo E) y SAAPV: ¿entran a F3 o se planifican como F4? | F4: uso bajo y dependencias APV/Tesorería de mandatos. |
| D-F3-6 | ¿Hay consultas de HANA de solo lectura que autorice para medir el peso real por clase (p. ej., cuántas veces entra cada J por el router vs. en proceso)? | Proponer el texto exacto en W0 y ejecutar solo con su OK. |

## 6. Riesgos
- **R-F3-1** Poco valor de tráfico inmediato: parte del volumen entra por jobs y cargas masivas en proceso. Mitigación: declarar "PROVEN-no-activo" donde corresponda y planificar los jobs al final de cada ola.
- **R-F3-2** `Certificar` tiene ~400 decisiones y ocho colaboradores externos: riesgo de regresión silenciosa. Mitigación: frentes cortos, un golden por regla, mutación y ≥ 20 corridas por caso.
- **R-F3-3** Colaboradores sin módulo (Facturación, Mesa, Normativa, Portafolio, SocioNegocio): HTTP rompe la atomicidad si escriben. Mitigación: compuertas cerradas y declaración explícita.
- **R-F3-4** El contador de uso es acumulado y mide frecuencia, no criticidad. Mitigación: validar con el negocio antes de W5/W6.
- **R-F3-5** Los hallazgos de la revisión de F2 siguen abiertos (fuga previa de la tabla de sesión, sin tope de listas, sin conciliación tras un 500 posterior al commit): conviene aplicarlos al inicio de W1 antes de abrir más listas.

## 7. Qué no se pudo comprobar al hacer este plan
- Las decisiones por clase son estimadas por conteo de palabras clave; `lizard` real se mide en W0.
- Los colaboradores externos salen de los `import`, no de una lectura completa de cada clase; W0 los confirma.
- El estado exacto "migrada o no" de las clases del cierre de F1 por nombre (p. ej. `CalcularFechaSolicitudAporteRescate`, `ObtenerPerfilFondoMutuo`) se tomó de las notas del módulo; W0 lo confirma contra el código.
- No se midió cuántas veces entra cada clase por el router frente a en proceso.

## 8. Decisiones del usuario (2026-10-26)

El usuario aprobó el plan con las recomendaciones de la sección 5:

| Decisión | Resultado |
|---|---|
| D-F3-1 | **Sí**: se empieza por las lecturas de la UI (W1) y `Certificar` queda para W3. |
| D-F3-2 | **Sí**: las reglas de `CertificarSolicitudAporteRescate`, `Liquidacion` y `Anulacion` se extraen en W0. |
| D-F3-3 | **Sí**: Facturación, Mesa y Normativa por HTTP detrás de puertos con compuertas cerradas. |
| D-F3-4 | **Sí**: las clases con 0 usos directos quedan fuera, salvo que el negocio confirme uso. |
| D-F3-5 | **F4**: CSC y SAAPV se planifican después. |
| D-F3-6 | **Texto exacto antes de ejecutar**: ninguna consulta HANA nueva se corre sin que el usuario apruebe su texto. |

## 9. Estado de W0 (2026-10-26)

W0 corrió con dos agentes de extracción de reglas en paralelo, solo lectura del legacy, sin HANA ni red (los agentes no tienen herramienta de escritura: entregaron el texto y lo guardé sin cambios).

| Entregable | Tarjetas | Defectos nuevos | Lo más duro |
|---|---|---|---|
| `RULES_FONDOMUTUO_F3_A.md` (lecturas de la UI y `CargarValorCuota`) | 75 (FSA 8, CPF 14, SFM 8 más 13 de la gemela WS, FMR 4, IFM 6, DET 4, VDO 4, CVC 14) | D-F3A-01..28 | `ConsultarCustodiaPortafolioFondoMutuo` (CCN 80 en 46 funciones, orden de salida por `HashMap`, la garantía siempre queda en 0) y `CargarValorCuota` (única que escribe; hay lógica muerta) |
| `RULES_FONDOMUTUO_F3_B.md` (`Certificar`, `CertificarPorFiltros`, `Liquidacion`, `Anulacion`) | 106 (CER 73, CXF 6, LIQ 20, ANU 7) | D-F3B-01..30 | validación por solicitud, custodia/costo UF/spread, Tesorería, Facturación, reinversión (`crearAporteReinversion` CCN 43), cierre transaccional |

Hallazgos que cambian el plan:
1. **Simulación de `Certificar` = rollback del anfitrión**: todo se escribe con "N" y se revierte; dispara notificación asíncrona y OWP antes de revertir (D-F3B-02). El oráculo y la guarda de jobs deben tenerlo en cuenta.
2. **Rechazos de Tesorería y Spot no suben el contador de errores y se confirman a medias** (custodia y cuenta ya escritas) (D-F3B-03): ¿es vigente o un defecto? Pregunta Q-W0B-2.
3. **`ResumenPatrimonial` de Normativa solo se importa, no se invoca**: sale del alcance salvo que el experto diga otra cosa (Q-W0B-6).
4. **Servicios "andamio" ya existentes en BcsFondoMutuoServicios**: `CertificarSolicitudAporteRescateService` (109 líneas), `CertificarPorFiltros...Service`, `LiquidacionSolicitudAporteRescateService`, `AnulacionSolicitudAporteRescateService` y `...FolioContratoService` leen los mismos SQL pero devuelven filas crudas con otro contrato, no validan y ejecutan un `batchUpdate` incompleto. Están escritos dentro de `adapter/in/web/InternalJavaCallController` (líneas 489-531). **No son equivalentes**. *Corrección del 2026-10-26 tras revisar el arranque del módulo:* ese controlador y los `service.*` están **fuera del contexto de Spring desde F1** (`BcsFondoMutuoApplication` los excluye con el filtro `STUBS`, y `StubsFueraDelContextoTest` falla si alguno se registra), así que hoy son inalcanzables; además el router no tiene destino para `cl.bcs.fondomutuo.` y `clases-habilitadas` está vacío (D-F3B-28 del agente B decía "cableados": solo lo están en el código fuente).
5. **Q-F3A-1 (de valor):** `PLA_Insert_Servicios.sql` registra como procedimientos HANA (tipo P) cuatro de las clases de lectura y `CargarValorCuota`: hay que saber cuál implementación está viva en producción antes de portarlas, porque si corre el procedimiento HANA, portar la clase Java no mueve nada.
6. **Q-F3A-7:** los dos FileManager llaman a `CargarValorCuota` siempre en simulación; ¿quién carga realmente los valores cuota hoy?

Propuesta de partición de `Certificar` en 13 frentes cortos (A cabecera y validación, B montos, C aportes, D rescates con custodia y spread, E CuentaInversión, F Tesorería, G Facturación, H Spot, I reinversión y switch, J cierre, K Liquidación, L Anulación, M PorFiltros) en la sección 12 de `RULES_FONDOMUTUO_F3_B.md`. Preguntas abiertas: Q-F3A-1..8 y Q-W0B-1..16.

**Pendiente de W0:** las decisiones de las preguntas de valor (Q-F3A-1, Q-F3A-7), la nueva D-F3-7 (andamios), y el texto exacto de las consultas HANA de solo lectura si se quiere medir el peso real (D-F3-6). W1 no arranca hasta cerrar Q-F3A-1.

## 10. Decisiones y respuestas (2026-10-26, después de W0)

| Tema | Resultado |
|---|---|
| **D-F3-7** (andamios de `Certificar`, `Liquidacion`, `Anulacion`) | **Se dejan desactivados**: ya están fuera del contexto (ver sección 9, punto 4) y siguen así; cada archivo se elimina solo cuando se porte su clase. No se cambia código. |
| **Q-W0B-2** (rechazos de Tesorería y Spot que no suben el contador y se confirman a medias) | **Se replica tal cual** (bug a bug, D-F3B-03). Queda fijado por goldens y listado para el experto; sin DEV nueva. |
| **Q-F3A-1** (¿corre el procedimiento HANA o la clase Java?) | **Corre la clase Java.** SELECT de solo lectura a `PLA.Servicio` (HANA QA, 2026-10-26, autorizado por el usuario): las filas 3013 (`ConsultarDetalleSolicitud`, 693 usos), 3022 (`ConsultarFechaSolicitudAporteRescate`, 16.217), 3035 (`CargarValorCuota`, 923) y 3041 (`ConsultarCustodiaPortafolioFondoMutuo`, 8.886) están registradas hoy como **tipo J** (`PLUGIN_FMM.jar@...` / `PLUGIN_OPTIMUS@...`) con `RegistraUso=S`. El `PLA_Insert_Servicios.sql` del repo las trae como tipo P: está desactualizado frente al registro vivo. Portar las clases Java sí mueve tráfico. |
| **Q-F3A-7** (¿quién carga los valores cuota?) | **Cargas esporádicas, no un job diario.** SELECT de solo lectura a `FMM.ValorCuota` (agrupado por día de `FechaIngresoRegistro`): últimas cargas 2026-10-19 (14 series, 20:48), **2026-10-16 (572 series entre 10:57 y 17:10)**, 2026-10-09 (2), 2026-10-01 (1), 2026-09-30 (1), y días sueltos de 1 a 13 filas hacia atrás (la tabla no guarda usuario ni origen: `Observacion` va vacío en las 594 filas de los últimos 120 días). Los dos FileManager llaman a `CargarValorCuota` solo en simulación; el patrón (ráfagas manuales de un minuto y una carga masiva en un día) apunta a la UI y no a un proceso programado. **Es una inferencia**, no una prueba: confirmar con el negocio antes de W2. |

Consultas ejecutadas (solo SELECT, conexión de solo lectura, credenciales del `.env` local sin imprimirlas ni guardarlas): `SELECT ... FROM PLA.Servicio WHERE IdServicio IN (3013,3022,3035,3041)`; `SELECT COLUMN_NAME, DATA_TYPE_NAME FROM SYS.TABLE_COLUMNS` de `FMM.ValorCuota`; dos agregados de `FMM.ValorCuota` por día y por texto de `Observacion`. Ninguna devolvió datos personales.

**Estado:** W0 cerrado en lo que no depende de nada más. Pendiente: las preguntas del experto de dominio (Q-F3A-8, Q-W0B-1, 3, 4, 7, 8, 9..13) y el catálogo de vistas (Q-F3A-2, Q-W0B-14), que se piden como consultas de solo lectura con texto exacto cuando se aborde cada ola. **W1 (lecturas) puede arrancar.**
