# Plan F2: FondoMutuo, las 7 clases `CrearSolicitud*` restantes del Manejador y los helpers Tesoreria / Spot

Fecha: 2026-10-22. Estado: PLAN para aprobacion (sin codigo). No se toco legacy, modernized ni HANA al producirlo; no se ejecuto ninguna consulta HANA (se usaron los CSV ya existentes en `hana_qa_results/`).
Fuentes: `PLAN_FONDOMUTUO_SOLICITUD_APORTE_RESCATE.md` (F1, completo, con la seccion 12 de decisiones), `RULES_FONDOMUTUO_SOLICITUD.md` (128 tarjetas, defectos, 6b/6c), `hana_qa_results/uso_sar_*.csv`, `fmm_*.csv`, `pla_servicio_filas.csv`, `TRANSFORMATION_NOTES.md` de BcsTesoreriaServicios, codigo legacy de las 7 clases, de `ProcesarIngresoEgresoTesoreria`, `ProcesarIngresoOperacionSpot`, `ProcesarCustodiaFondoMutuo`, y el codigo moderno de Tesoreria, TesoreriaJobs y Mesa (solo lectura). `lizard 1.24.2` medido sobre los archivos (no es conteo manual).

## 0. Resumen y corte recomendado

1. **Las 7 clases** que el Manejador despacha (ademas de Normal) son: `CrearSolicitudAporteRescateProgramado`, `...AporteOvernight`, `...AporteReinversion`, `...CambioSerie`, `...RescateReinversion`, `...RescateTransferencia`, `...RescateTraspaso`. Suman **7.905 LOC brutas, 6.969 NLOC, 80 funciones y ~1.060 decisiones (lizard)**, no ~866: el conteo manual de F1 subestimo otra vez (+22 %). Seis de las siete tienen una unica funcion `logica`/`procesarSolicitudes` de CCN 82 a 221 (la de `RescateReinversion` tiene CCN 221, 982 NLOC).
2. **El peso real es muy desigual** (seccion 2.2, sobre 9.027.151 solicitudes historicas): `AporteReinversion` + `RescateReinversion` explican 1,90 M filas (21,0 %), `RescateTraspaso` 116 k (1,3 %), `RescateTransferencia` 21 filas, y **`Programado`, `Overnight` y `CambioSerie` tienen 0 filas historicas con su `IdTipoMovimiento`** (`APORESPRO`, `APOOVER`, `RESCAM`) y 0 usos directos en el registro. F1 (Normal) cubre 39,8 % del historico; F2 (las 7) cubre 22,3 %; **el 37,9 % restante NO lo sirve ninguna de las dos fases** (sobre todo `APODIV` 3,17 M = 35,1 %, de `CrearSolicitudAportePorDividendos`, que el Manejador no despacha: la llaman un job de eventos de capital y la carga masiva). Hallazgo para el usuario, seccion 2.3.
3. **Dos bloqueos estructurales distintos** (seccion 3): (a) `CambioSerie`, `RescateTransferencia` y `RescateTraspaso` escriben custodia llamando en proceso a `ProcesarCustodiaFondoMutuo` (534 LOC), que a su vez llama a `IngresoCustodia` (2.746 LOC) y `RetiroCustodia` (3.637 LOC) de Custodia, **sin migrar y no PROVEN**: no pueden hacerse "en proceso" y por HTTP pierden la atomicidad; (b) `Programado`, `Overnight` y `AporteReinversion` usan `ProcesarIngresoEgresoTesoreria` (Tesoreria, libreria en proceso: decidido) y `AporteReinversion` tambien `ProcesarIngresoOperacionSpot` (Mesa, sin migrar).
4. **Tesoreria en proceso es viable** (seccion 4.1): `tesoreria-service` publica un jar plano; `CrearIngresoEgresoService.execute(JsonObject)` es `@Transactional(REQUIRED, rollbackFor=Exception)` sobre `NamedParameterJdbcTemplate`; no hay ciclo Maven (Tesoreria no depende de FondoMutuo). Condicion que hay que fijar YA en el paso 2 de F1: la unidad de trabajo del modulo debe ser una transaccion Spring sobre el mismo `DataSource` (no una `Connection` cruda de un filtro), o Tesoreria no podra unirse.
5. **Mesa Spot: no se porta el slice** (seccion 4.3). `CrearOperacionSpot` legacy son 2.752 LOC (el servicio moderno de Mesa tiene 110: esqueleto) y `ConsultarPuntasPorTramo` 203 (moderno 60). Se escribe `ProcesarIngresoOperacionSpot` contra un `SpotPort` con adaptador HTTP y la compuerta G2 queda cerrada por configuracion hasta que el usuario decida (D-F2-5).
6. **Corte en 4 olas** (seccion 5, max 2 agentes en paralelo): **W0** decisiones + consultas agregadas + reglas de las 7 clases; **W1** libreria Tesoreria + `RescateReinversion`; **W2** `AporteReinversion` + `Programado`/`Overnight` (solo si el negocio confirma uso); **W3** (bloqueada por Custodia) `RescateTraspaso`, `RescateTransferencia`, `CambioSerie` + `ProcesarCustodiaFondoMutuo`. Cada ola cierra con equivalencia propia y levanta su lista del Manejador (seccion 6).
7. **Advertencia de valor (R-F2-1):** el Manejador se usa por UI (167.843 llamadas en total en el registro) frente a 9,03 M de solicitudes historicas: casi todo el volumen viene de cargas masivas y batch que llaman a estas clases EN PROCESO desde clases legacy aun no migradas (`ProcesarReinversionRescate` 2.504 LOC, `ProcesarCargaSolicitudesAporteRescate`, `CertificarSolicitudAporteRescate` 3.052 LOC, `AdministracionLiquidez`). Migrar las 7 clases NO mueve ese trafico; solo el que entra por el Manejador. Hay que medirlo antes de gastar W2/W3 (consulta propuesta en 2.4).

## 1. Reglas vinculantes (se repiten tal cual para los agentes)

1. **Ningun cambio de base de datos, de ningun tipo** (tablas, columnas, vistas, procedimientos, indices, datos de configuracion). Mismos objetos, mismos SQL. El DDL de sesion `CREATE LOCAL TEMPORARY TABLE #p_Bloqueo...`/`DROP TABLE` ya fue aceptado (F1 D-2) **solo para `#p_BloqueoSolicitudAporteRescate`**. En F2 aparece **otro**: `#p_BloqueoProcesarReinversion` (`CrearReinversionRescate_Tabla_Bloqueo.sql`) en `RescateReinversion` (seccion 3.3): hace falta que el usuario confirme que la aceptacion de D-2 cubre este segundo caso identico (D-F2-1); sin eso `RescateReinversion` no se porta.
2. **HANA solo SELECT de solo lectura y solo si es estrictamente necesario**, con las herramientas existentes de `analysis/otr-sva-optimus-corredores/tools` (`HanaUsoSolicitudAporteRescate`, `HanaCatalogoFondoMutuoSolicitud`), credenciales del `.env` local, sin imprimirlas ni guardarlas. Los SELECT agregados sin datos personales y de catalogo ya estan autorizados (F1, seccion 12); las consultas nuevas de 2.4 y 3.3 son del mismo tipo, y se piden de todos modos con el texto exacto antes de ejecutarlas.
3. **El legacy nunca se modifica**; el oraculo compila copias en un directorio temporal. Defectos del legacy **bug a bug**, listados para el experto de dominio (seccion 8); cada uno fijado por al menos un test.
4. **Sin secretos del legacy como fixtures:** 6 de las 7 clases y `ProcesarIngresoEgresoTesoreria` traen `main()` con `getDESAConnection()`/`desa01` y JSON con ids reales de cliente/portafolio (p. ej. `CrearSolicitudRescateTraspaso:871`): no se copian (RULE-076; el test de escaneo de F1 se extiende a las clases nuevas).
5. **Colaboradores aun no migrados: HTTP/TomEE detras de puertos** (Custodia, Mesa, Plataforma). Los ya migrados y PROVEN (Tesoreria) en proceso como libreria (decision del usuario).
6. **Commits sin `Co-Authored-By`** (prevalece sobre la plantilla por defecto). Produccion Linux: LF, zona horaria, charset y locale fijados; tests en una sola JVM UTC.
7. **Ningun trafico real hasta PROVEN y firmado** (`VERIFICATION.md`/`SIGNOFF.md`); enrutamiento opt-in por clase (`plataforma.javacall.clases-habilitadas`, vacio = todo a TomEE). Las clases F2 de escritura no repetible no reciben trafico de jobs sin la guarda fail-closed (`JOBS_PATTERN.md` paso 6).
8. Las desviaciones DEV-F1..F7 de F1 (aprobadas) aplican igual a F2; cualquier DEV-F8+ requiere aprobacion antes de implementarse.

## 2. Inventario de las 7 clases y peso real

Ruta legacy comun: `legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/`. Todas implementan `JavaCall` (`PLUGIN_FMM.jar@cl.bcs.fondomutuo.<Clase>`, `RegistraUso=S`, `ValidaAccesoServicio=N`). Decisiones = suma de CCN menos funciones (promedio x funciones de lizard, error +-1). Orden = el de despacho del Manejador (`ManejadorSolicitudAporteRescate:141-285`).

### 2.1 Tabla

| # | Clase (id registro) | Lista(s) del Manejador que la activan | `IdTipoMovimiento` que escribe | LOC (NLOC) | Decisiones; CCN de la funcion mayor | Uso directo registro |
|---|---|---|---|---:|---|---:|
| 1 | `CrearSolicitudAporteRescateProgramado` (1434) | `p_SolicitudesAporteRescateProgramado` | aporte `APORESPRO` y, dentro de la misma clase, rescate `RESNORM` (`:420,:525`) | 1.019 (906) | ~145; 145 (`logica`) | 0 |
| 2 | `CrearSolicitudAporteOvernight` (1431) | `p_SolicitudAporteOvernight` | aporte `APOOVER` y rescate `RESNORM` (`:499,:605`) | 1.112 (992) | ~161; 161 | 0 (la llama en proceso `AdministracionLiquidez`) |
| 3 | `CrearSolicitudAporteReinversion` (1442) | `p_SolicitudAporteReinversion` | `APOREINV` (`:769`) | 1.496 (1.317) | ~225; 206 (`procesarSolicitudes`) | 0 |
| 4 | `CrearSolicitudCambioSerie` (1456) | `p_SolicitudCambioSerieRescate` **o** `p_SolicitudCambioSerieAporte` (`:210`) | `RESCAM` (`:361`) | 927 (819) | ~95; 95 | 0 |
| 5 | `CrearSolicitudRescateReinversion` (1436) | `p_SolicitudRescateReinversionRescate` **o** `...Aporte` (`:236`) | `RESREINV` (`:737`) y valida/inserta el lado aporte `APOREINV` | 1.616 (1.397) | ~264; 221 (`logica`, clase interna `DesbloquearReinversion`) | **239** |
| 6 | `CrearSolicitudRescateTransferencia` (1438) | `p_SolicitudRescateTransferencia` | `RESTRANSF` (`:319`) | 845 (751) | ~82; 82 | 0 |
| 7 | `CrearSolicitudRescateTraspaso` (1437) | `p_SolicitudRescateTraspaso` | `RESTRASP` (`:318`) | 890 (787) | ~88; 88 | 0 |
| | **Total** | | | **7.905 (6.969)** | **~1.060** | |

Helpers que F1 dejo en compuerta y F2 porta: `ProcesarIngresoEgresoTesoreria` 370 LOC (40 decisiones), `ProcesarIngresoOperacionSpot` 500 LOC (35). Helper nuevo de F2: `ProcesarCustodiaFondoMutuo` 534 LOC (478 NLOC, ~34 decisiones, `logica` CCN 28; solo lo usan las clases 4, 6 y 7 de esta lista, ademas de clases F3). **Total F2: 7.905 + 870 + 534 = 9.309 LOC, ~1.170 decisiones.**

Hechos comunes verificados por busqueda de texto (a confirmar leyendo en W0):
- Las 7 reutilizan el cierre de F1 (`CalcularMontoNumeroCuota`, `ServicioCumpleRequisitos...`, `ObtenerPerfilFondoMutuo`, `ConsultarFlujoCustodiaFondoMutuo`, `CrearSolicitudAGF`, `RevisarRequisitosBasicos`, `AgregarLog`, `ConsultarEmpresa`), 4 reutilizan `CalcularFechaSolicitudAporteRescate` y 4 `ValidaPatrimonioEmisor`. Es decir: **lo que F1 deja PROVEN es el 60-70 % del trabajo de cada clase; lo nuevo es la orquestacion propia de cada una** (copia-pega de Normal con variantes).
- SQL: casi todos compartidos `Comunes*` (los mismos 13 de F1, mas `SelectIndicadoresPortafolioFFMM`, `SelectConfiguracionRegimenFondoMutuo`, `SelectPortafolioAPV`, `SelectRegimenSerieDeFondoMandato`, `InsertReinversionSolicitud`); propios solo 3 en `AporteReinversion` (`SelectRegimenDestino`, `SelectReinversionSerieFondoDestino`, `UpdateFechaFinalReinversion`) y los de bloqueo de reinversion de `RescateReinversion` (`ProcesarReinversionRescate_Call_CrearBloqueoReinversion`, `ProcesarReinversion_Call_EliminarBloqueoReinversion`, `CrearReinversionRescate_Tabla_Bloqueo`, `DropReinversionRescate_Tabla_Bloqueo`). W0 compara su SHA-256 con los 376 modernos como en F1.
- Patrones de defecto ya conocidos presentes en las 7: `Fecha.setLocalOffSet` estatico (7/7, D-NOR-10), `printStackTrace` (7/7, D-NOR-11), `logger.trace` con JSON (solo `AporteReinversion`), `Utils.List2Hash` (9 a 15 usos por clase: lanza con clave repetida, D-NOR-9), `parallelStream` (6/7).
- Ninguna de las 7 usa el bloqueo anti-duplicados de Normal; `RescateReinversion` tiene uno propio (seccion 3.3) y `CambioSerie` menciona "Bloqueo" 2 veces (revisar en W0).

### 2.2 Peso real por `IdTipoMovimientoFondoMutuo` (`uso_sar_tipo_movimiento.csv`, 9.027.151 solicitudes 2008-09-25 a 2026-10-19)

| Grupo | Movimientos | Filas | % | Clase que las escribe | Fase |
|---|---|---:|---:|---|---|
| Normal | `APONORM` 2.804.556, `RESNORM` 576.814, `APOTRASP` 207.757 | 3.589.127 | 39,8 | `CrearSolicitudAporteRescateNormal` (F1; `APOTRASP` por `EsTraspaso`=S, FM-NOR-18) | F1 |
| Reinversion | `APOREINV` 1.290.736, `RESREINV` 606.441 | 1.897.177 | 21,0 | clases 3 y 5 | F2 |
| Traspaso / transferencia | `RESTRASP` 116.462, `RESTRANSF` 21 | 116.483 | 1,3 | clases 7 y 6 | F2 |
| Programado, overnight, cambio de serie | `APORESPRO`, `APOOVER`, `RESCAM` | **0** | 0 | clases 1, 2, 4 | F2 (dudoso) |
| **Subtotal F2 (7 clases)** | | **2.013.660** | **22,3** | | |
| Dividendos | `APODIV` 3.170.009, `APODIVCFI` 20.806 | 3.190.815 | 35,3 | `CrearSolicitudAportePorDividendos`/`...CFI` (fuera del Manejador) | **no cubierto** |
| Fusion | `APOFUSION` 53.198, `RESFUSION` 53.190 | 106.388 | 1,2 | `CrearAporteRescateFusion` | no cubierto |
| Comision | `RESCOM` 79.717, `RESCNN` 1 | 79.718 | 0,9 | `CrearSolicitudRescatePorComision` / `...ComisionNN` | no cubierto (NN en F1) |
| Otros | `APOCDF`/`RESCDF` 17.711, `INGTIT`/`RETTIT` 11.630, `RESTRI` 8.551, `APOCANJE`/`RESCANJE` 8.924, `INGGTA`/`RETGTA` 409, `APOREPRO` 217, `APOREGULA` 1 | 47.443 | 0,5 | varias de `ProcesarCargaSolicitudesAporteRescate` | no cubierto |

Lectura honesta: son solicitudes persistidas (historico completo, no llamadas ni periodo reciente) y mezclan todas las vias. `APOREINV` (1,29 M) duplica con holgura a `RESREINV` (606 k): o bien cada rescate de reinversion genera varios aportes, o bien las aporta la clase 3 desde el proceso batch de reinversion; **no se puede decidir sin leer** (W0). Los `RESNORM` de `Programado`/`Overnight` quedan contados en Normal. `APOCANJE`/`RESCANJE` no coinciden con la constante `RESCAM` de `CambioSerie` (ninguna clase Java de FondoMutuo contiene la palabra CANJE): el mapa de esos dos es una hipotesis sin comprobar.

### 2.3 Lo que ni F1 ni F2 cubren (para el usuario)
`APODIV` (35,1 %) es la mayor via de aportes y no pasa por el Manejador: `CrearSolicitudAportePorDividendos` (640 LOC) la llaman `CerrarEventoCapitalDVFFMM` (job) y `ProcesarCargaSolicitudesAporteRescate`, y usa `ProcesarCustodiaFondoMutuo`. Tampoco entran `Fusion`, `RescatePorComision` ni la carga masiva. Si el objetivo es retirar TomEE del dominio, F3 deberia incluirlas; si el objetivo es el trafico del Manejador, F2 alcanza. **Decision D-F2-2.**

### 2.4 Consulta de solo lectura que se propone (autorizada la clase de consulta; texto exacto antes de ejecutar)
Un solo `SELECT` agregado sin datos personales sobre `FMM.SolicitudAporteRescate` (columnas verificadas en `fmm_columnas_tablas.csv`): conteos por `IdTipoMovimientoFondoMutuo` x `IdModulo` x `IdTipoCanalIngreso` x `SolicitudBatch` x `IdCarga > 0` x anio de `FechaIngresoRegistro` (ultimos 24 meses), y por separado conteo de filas con `IdOperacionSpot` no nulo y `IdIngresoEgreso` no nulo por tipo (peso de las compuertas Spot y Tesoreria). Con eso se separa "entra por UI/Manejador" de "entra por carga masiva/batch" y se dimensiona G2. Se ejecuta con `HanaUsoSolicitudAporteRescate` ampliado (programa nuevo en `tools/`, no se modifica el existente).

## 3. Colaboradores, objetos HANA, compuertas y defectos por clase

Leyenda de destino (igual que F1): **P** portar en el modulo; **K** kit de lectura; **H** HTTP al router `/internal/java-call` detras de puerto; **L** libreria en proceso (Tesoreria); **X** compuerta (503, el router repite en TomEE). Las lecturas HANA comunes son las 13 de F1; aqui solo lo propio.

### 3.1 Tabla comparativa

| Clase | Colaboradores fuera del cierre F1 | Escrituras HANA propias (existentes) | Compuertas / dependencias | Notas de riesgo |
|---|---|---|---|---|
| 1 Programado | `ProcesarIngresoEgresoTesoreria` (L, no-CIN), `SaldoFlujoCaja` (H, 17 refs), `ObtenerDiaSiguiente` (H/K, 7), `CrearSolicitudAGF`, `AgregarLog` | INSERT `FMM.SolicitudAporteRescate` (2 tipos: `APORESPRO` + `RESNORM`), AGF | G1 (Tesoreria) se levanta con la libreria; G3 `SaldoFlujoCaja` (D-5 de F1) | 0 uso historico |
| 2 Overnight | igual que 1 (`ObtenerDiaSiguiente` 8 refs) | INSERT (`APOOVER` + `RESNORM`), AGF; `Overnight`/`AdministracionLiquidez` en la fila | igual que 1 | 0 uso; la llama en proceso `AdministracionLiquidez` (legacy sigue en TomEE) |
| 3 AporteReinversion | `ProcesarIngresoEgresoTesoreria` (L), **`ProcesarIngresoOperacionSpot` (X, G2)**, `SaldoFlujoCaja` (H), `CalcularParidad` (H, 8 refs), AGF, log | INSERT `APOREINV`, **UPDATE `FMM.ReinversionSerieFondoDestino.FechaFinalReinversion`** | G1 lib, G2 Spot cerrada, G3 | rama Spot (`:1293-1343`, `venta/compra` con `MOVING`/`MOVEGR` duros); la llaman en proceso `CertificarSolicitudAporteRescate`, `ProcesarCargaSolicitudes...`, `ConsultarFechaSolicitudAporteRescate` y el propio `ProcesarIngresoOperacionSpot` |
| 4 CambioSerie | `ProcesarCustodiaFondoMutuo` (**X, G-CUS**), AGF, log | INSERT `RESCAM` (rescate y aporte del par) y movimientos de custodia | G-CUS | todo-o-nada por par (FM-MAN-07, D-MAN-7); 0 uso |
| 5 RescateReinversion | `InscribirProcesoBatch` (H, 6 refs), `ValidaPatrimonioEmisor` (K, ya F1), AGF, log | INSERT `RESREINV` y `FMM.ReinversionSerieFondoDestino` (`InsertReinversionSolicitud`); **DDL de sesion `#p_BloqueoProcesarReinversion` + `CALL CrearBloqueoProcesarReinversion`/`EliminarBloqueoReinversion`** | D-F2-1 (segundo DDL de sesion); batch H (G5) | bloqueo propio (34 referencias), `RegimenMandato`, `SeriesMonedasDiferentes`, `KeyAporteMonto`; importa la clase anidada `BloqueoReinversion` de `ProcesarReinversionRescate` (2.504 LOC): solo se porta el pojo, NO esa clase |
| 6 RescateTransferencia | `ProcesarCustodiaFondoMutuo` (G-CUS), `CalcularParidad` (H, 8), AGF, log | INSERT `RESTRANSF`, custodia | G-CUS | uso casi nulo (21 filas) |
| 7 RescateTraspaso | `ProcesarCustodiaFondoMutuo` (G-CUS), `CalcularParidad` (H, 8), **`NotificarEstadoOrdenesOWP` (X, G4)**, AGF, log | INSERT `RESTRASP`, custodia | G-CUS, G4 | 116 k filas historicas; la llama tambien `CrearSolicitudRescateTraspasoPortafolio` (893 LOC, F3) |

### 3.2 Procedimientos y tablas de reinversion que el catalogo local no cubre
`fmm_procedimientos.csv` solo tiene los 2 de bloqueo de Normal (ambos `AUTONOMOUS_TRANSACTION=TRUE`). Faltan `CrearBloqueoProcesarReinversion` y `EliminarBloqueoReinversion` (si son autonomos, el bloqueo de reinversion sobrevive al rollback igual que D-NOR-3) y las escalas `DECIMAL` de `FMM.ReinversionSerieFondoDestino` (`Porcentaje`, `PorcentajeReal`). **Pedir en W0 un catalogo de solo lectura (`SYS.PROCEDURES`, `SYS.TABLE_COLUMNS`) de esos 2 procedimientos y de las tablas de reinversion/AGF que las 7 clases tocan** (ya autorizado como clase en F1; programa `HanaCatalogoFondoMutuoSolicitud` ampliado). El oraculo H2 usa esas escalas, no supuestos.

### 3.3 Por que `RescateReinversion` se hace primero entre las de escritura
Es la unica con uso directo real (239, id 1436), mueve `RESREINV` (606 k), y no necesita Tesoreria, Spot ni custodia: su cierre externo es solo `InscribirProcesoBatch` (programa batch, H, no repetible: queda tras G5) y su bloqueo. Es la mas compleja (CCN 221, `logica` de 1.138 lineas): igual que `Normal.logica`, hay que descomponerla en reglas puras (`FM-RRE-nn`) sin cambiar el orden de comprobaciones para que JaCoCo >= 80 % de ramas sea alcanzable. Lleva el patron "solo si el par aporte+rescate llega completo" del Manejador (FM-MAN-07).

### 3.4 Dependencias de custodia (clases 4, 6, 7) y por que bloquean
`ProcesarCustodiaFondoMutuo.logica(con, ...)` (`CambioSerie:796`, `RescateTransferencia:674`, `RescateTraspaso:675`, solo si `p_SimularOperacion != "S"`) llama **tipado y en la misma conexion** a `IngresoCustodia.logica` (`:194`) y `RetiroCustodia`, ambos `JavaCall` de Custodia (2.746 y 3.637 LOC), mas `GestionMovimientoCarteraEnTransito` (204) y `ActualizaCarteraValorizadaFFMM` (354, de FondoMutuo). `BcsCustodiaServicios` esta en 97/99 stubs y NOT PROVEN (memoria del proyecto): no hay libreria que usar. Por HTTP, Custodia confirmaria en su propia conexion y un rollback posterior de la solicitud no lo deshace (mismo problema R-03/D-NOR-12 de F1, con custodia = posicion del cliente). Opciones (D-F2-3): (a) esperar a que Custodia migre y enlazarla como libreria (patron de Tesoreria) [recomendada]; (b) HTTP con compensacion explicita (cambia comportamiento, requiere DEV-F8 y experto); (c) dejar las 3 clases en TomEE para siempre (G0 fija) dado su uso (RESTRASP 116 k, resto ~0). Mientras tanto W3 puede escribirse y probarse con `CustodiaPort` doblado, dentro de un modulo apagado por configuracion.

## 4. Helpers Tesoreria y Mesa Spot

Ambos son clases FondoMutuo (`...fondomutuo.ProcesarIngresoEgresoTesoreria`, `...ProcesarIngresoOperacionSpot`), sin id en el registro (solo se llaman en proceso). Ojo: **Ordenes tiene otra clase con el mismo nombre simple** (`cl.bcs.ordenes.ProcesarIngresoOperacionSpot`, `BcsOrdenServicios`): no es esta, no mezclar en el oraculo ni en los puertos.

### 4.1 `ProcesarIngresoEgresoTesoreria` (370 LOC, 40 decisiones, 1 SQL propio `ComunesSolicitudes_SelectModalidadPago`)
Que hace (FM-NOR-90/91, `:141-321`): para cada aporte no simulado cuyo medio de pago NO es `CIN`, arma el ingreso (`IdTipoMovimiento` `MOVING`/`MOVEGR`; concepto `APORFM`, o `APODOL`/`RESDOL` si `GeneraSpot=S`; moneda y monto del sistema si hay Spot; persona = cliente o contraparte si `RelacionTraspaso=S`; `NumeroReferencia=IdInterno`; `IdComprobante=IdOperacion=IdSolicitudAporteRescate`) y la forma de pago (medio de la solicitud, o `TRA` si `CIN` con Spot; modalidad por `PAG.MedioDePagoYModalidadDePago`, por defecto `MODCAJ`; cuenta del socio de la preferencia de pago del portafolio; cuenta de la corredora solo con `SecuenciaRelacionada`), y llama **tipado y en la misma conexion** a `tesoreria.CrearIngresoEgreso`. Defecto conocido D-TES-1 (`TitularPago` siempre "N", `FechaLiquidacion` = emision).
Quien la usa: Normal, Programado, Overnight, AporteReinversion (F1/F2); `CertificarSolicitudAporteRescate` y `RecaudacionTraspaso` (F3, siguen en TomEE con su copia); el `ProcesarDistribucion` legacy de Tesoreria solo importa su clase anidada `Empleado`.

**Reutilizar la libreria ya migrada: SI, con cuatro condiciones.** Evidencia:
- `tesoreria-service` (BcsTesoreriaServicios) publica un **jar plano** y un `-exec.jar` (`spring-boot-maven-plugin` con `<classifier>exec</classifier>`), igual que lo consumen `BcsTesoreriaJobs` y `BcsAPVJobs` (`pom.xml` de ambos: "BcsTesoreriaServicios as an in-process library"). `CrearIngresoEgresoService.execute(JsonObject)` (1.406 LOC; 4.332 casos de equivalencia de Tesoreria en 0 diferencias, `TRANSFORMATION_NOTES.md`) es `@Transactional(rollbackFor = Exception.class)` (REQUIRED), usa `NamedParameterJdbcTemplate` y los contratos con nombres legacy estan en `javacall/kit/colaborador/CrearIngresoEgreso` (mismos `CrearIngresoEgreso_Entrada`, `IngresoEgreso_Entrada`, `FormaDePago_Entrada`).
- **Direccion de dependencia:** FondoMutuo -> Tesoreria. No hay ciclo Maven: el codigo moderno de Tesoreria solo tiene un contrato (`kit/colaborador/CrearSolicitudAporteRescateNormal`) para llamar a FondoMutuo por HTTP, no depende del jar. Hay ciclo de ejecucion (Tesoreria `ProcesarDistribucion` -> HTTP -> FondoMutuo Normal -> en proceso Tesoreria `CrearIngresoEgreso`): aceptable, cada proceso usa su propio pool.
- **C1, transaccion compartida (decidir en el paso 2 de F1, no en F2):** el servicio se une a la transaccion Spring del `PlatformTransactionManager` del mismo `DataSource`. Por eso `JavaCallUnitOfWorkFilter` de F1 debe abrir una **transaccion Spring** (commit solo si 2xx y contador = 0; rollback si falta el contador o es > 0) y las clases F1/F2 deben obtener la conexion por `DataSourceUtils`/`JdbcTemplate` de esa transaccion; una `Connection` cruda en un filtro impediria unir a Tesoreria. Sus tablas `GBL.CrearIngresoEgreso_Proceso`/`...FormaDePago_Proceso` (temporales globales) funcionan porque la sesion es la misma.
- **C2, contexto Spring:** no escanear `cl.bcs.tesoreria` entero (trae controlador, `TokenInternoFilter`, esqueleto SOAP, codigo muerto, `application.yml`). Una `TesoreriaEnProcesoConfiguration` importa solo `CrearIngresoEgresoService`, `CrearComprobanteService`, `LiquidarComprobanteService` (dependencias de su constructor) y los puertos `Colaboradores`, `ConfiguracionSistema`, `DiccionarioMensajes`, `Secuencias`, `Reloj` (precedente: `EnProcesoConfiguration` de TesoreriaJobs, que ademas fija `jakarta.xml.ws-api 4.0.2` y documenta el `application.yml` que se sombrea).
- **C3, choque de recursos:** Tesoreria carga `sql/CrearIngresoEgreso_*.sql` con rutas planas en `sql/`; `BcsFondoMutuoServicios` hoy tiene 376 SQL planos en `src/main/resources/sql/` (+376 en `target`). F1 ya manda sus SQL a `cl/bcs/fondomutuo/sql/`; el paso 2 de F1 debe **eliminar el directorio plano** y agregar un test que falle si dos jars aportan el mismo recurso (el primero en el classpath ganaria en silencio).
- **C4, que sigue por HTTP:** los colaboradores propios de `CrearIngresoEgreso` (`RevisarRequisitosBasicos` lectura, `SaldoFlujoCaja` lectura, `AgregarLog` escritura append-only) pasan por el puerto `Colaboradores` de Tesoreria, que FondoMutuo implementa con su `RouterClient` (o reutiliza `ColaboradoresJavaCall`, con el mapa explicito de nombres). `AgregarLog` por HTTP no comparte transaccion: queda bajo G5 (rollback + 503 si falla, DEV-F5).
Punto de entrada: llamada directa al bean `execute(JsonObject)` con el JSON armado por el helper portado desde las clases del kit (preserva el contrato y el orden de campos del legacy), no por `JavaCallDispatcher` (evita su lista blanca y el salto).

### 4.2 `ProcesarIngresoOperacionSpot` (500 LOC, 35 decisiones; sin SQL propio)
Que hace (FM-NOR-92..94, `:130-470`): para aportes con `GeneraSpot=S` (y rescates con `RESCATE_GENERA_VENTA_SPOT_DOLAR`/`ForzarSpot`), lee fecha de sistema, moneda del sistema, mesa por defecto, dolar observado (`CalcularParidad` USD->sistema, 0 si falta, `:225`), instrumento Spot `DISTRI` (`ConsultarDatosInstrumentos`, `:254`), puntas por tramo (`ConsultarPuntasPorTramo`, `:273`); arma una `OperacionObjIn` por solicitud (hardcode `IdTipoDolar=4`, `CodigoComercio=300206`, D-SPOT-2; parametros de flujo de caja cruzados entre `PAC` y `RESNORM`, D-SPOT-1) y llama **estatico, tipado y en la misma conexion** a `CrearOperacionSpot.logica(con, ...)` (`:496`). Usuarios: Normal, AporteReinversion, `CertificarSolicitudAporteRescate`, `CrearSolicitudAportePorDividendosCFI`.

### 4.3 Mesa Spot: decision de acceso (BcsMesaServicios NO migrado)
Estado: legacy `CrearOperacionSpot` 2.752 LOC y `ConsultarPuntasPorTramo` 203; en `BcsMesaServicios` moderno existen `CrearOperacionSpotService` (110 LOC) y `ConsultarPuntasPorTramoService` (60), es decir esqueletos con 10 SQL cargados y un `PlataformaBridge`, no equivalentes.

| Opcion | Que implica | Veredicto |
|---|---|---|
| A portar el slice a FondoMutuo | +2.955 LOC y ~11 SQL de otro dueno; duplica lo que Mesa debera migrar; el dueno futuro tendria dos copias | No: contradice la regla de colaboradores no migrados y multiplica el alcance |
| B HTTP detras de `SpotPort` (router -> TomEE) | cumple la regla; `CrearOperacionSpot` confirma en la conexion de TomEE: un rollback posterior de la solicitud deja la operacion Spot viva (D-NOR-12). Sin compensacion no hay atomicidad; con `AnularOperacionSpot` (existe en Mesa) seria DEV-F8 y requiere experto de mesa | Implementar el puerto y el adaptador, **dejar G2 cerrada por configuracion** |
| C esperar la libreria de Mesa | mismo patron que Tesoreria: en proceso, misma transaccion | Meta: pedir que `CrearOperacionSpot` y `ConsultarPuntasPorTramo` entren al orden de Mesa; al llegar se abre G2 |

Recomendacion: B con G2 cerrada (`fondomutuo.spot.habilitado=false` por defecto, 503 antes de la primera escritura, borrando el bloqueo como en F1) mas C como camino de apertura. Las lecturas del helper (`CalcularParidad`, `ConsultarDatosInstrumentos`, `ConsultarPuntasPorTramo`) son lecturas por HTTP, no problema de atomicidad. **El peso de G2 se mide con la consulta de 2.4 (`IdOperacionSpot` no nulo); si es marginal, no se invierte mas** (D-F2-5).

### 4.4 Resumen de modos y compuertas
| Colaborador | Dueno | Modo F2 | Atomico con la solicitud | Compuerta |
|---|---|---|---|---|
| `CrearIngresoEgreso` (via `ProcesarIngresoEgresoTesoreria`) | Tesoreria, PROVEN | **L** libreria en proceso | si (C1) | G1 se levanta en W1 |
| `CrearOperacionSpot` | Mesa, sin migrar | H (`SpotPort`) | no | G2 cerrada hasta D-F2-5 |
| `IngresoCustodia`/`RetiroCustodia` (via `ProcesarCustodiaFondoMutuo`) | Custodia, NOT PROVEN | H (`CustodiaPort`) o libreria futura | no por HTTP | G-CUS cerrada |
| `SaldoFlujoCaja` | Plataforma (pedido de F1 D-5) | H (`FlujoCajaPort`) | lectura | G3 hasta que Plataforma lo exponga |
| `InscribirProcesoBatch`, `AgregarLog` | Plataforma | H | no (escritura) | G5 (rollback + 503) |
| `NotificarEstadoOrdenesOWP` | Plataforma | X | externo | G4 (igual que F1) |

## 5. Orden recomendado y corte en olas (maximo 2 agentes en paralelo)

### 5.1 Orden por volumen y riesgo
| Orden | Clase | Volumen historico | Riesgo | Razon |
|---:|---|---:|---|---|
| 1 | RescateReinversion | 606 k, 239 usos directos | alto (CCN 221, DDL de bloqueo, batch) | unico uso directo; sin Tesoreria/Spot/custodia; su aporte-par abre el camino de 2 |
| 2 | AporteReinversion | 1.290 k | alto (Tesoreria, Spot, flujo de caja, `ReinversionSerieFondoDestino`) | mayor volumen de F2; reutiliza el helper de Tesoreria de W1 y el kit de reinversion |
| 3 | Programado y Overnight | 0 | medio (gemelas de Normal, `ObtenerDiaSiguiente`, Tesoreria) | solo si D-F2-4 confirma uso; si no, se retiran del alcance (G0 fija) |
| 4 | RescateTraspaso | 116 k | medio (custodia, OWP) | bloqueada por Custodia |
| 5 | RescateTransferencia, CambioSerie | 21 y 0 | medio | bloqueadas por Custodia; candidatas a no portarse |

### 5.2 Olas
Cada ola termina con tests en verde, notas en `TRANSFORMATION_NOTES.md` (mapa legacy -> nuevo con LOC, DEV, SME, Q), equivalencia propia anexada, y un commit pequeno **sin `Co-Authored-By`**. Ningun paso toca `legacy/`, HANA (fuera de las consultas de lectura de W0) ni el esquema.

| Ola | Contenido | Agentes | Depende de | Cierre |
|---|---|---|---|---|
| **W0** (sin codigo) | (a) usuario: D-F2-1..8; (b) consultas de 2.4 y 3.2 (solo lectura); (c) **extraccion de reglas** `RULES_FONDOMUTUO_F2.md`: A = RescateReinversion + AporteReinversion + `ProcesarIngresoEgresoTesoreria` completo (3.482 LOC); B = Programado, Overnight, CambioSerie, Transferencia, Traspaso + `ProcesarIngresoOperacionSpot` + `ProcesarCustodiaFondoMutuo` (5.827 LOC, mas `ActualizaCarteraValorizadaFFMM` 354 si hace falta); tarjetas `FM-RRE/ARE/PRO/OVE/CSE/TRF/TRP/SPT/CUS`, SHA-256 de los SQL, defectos nuevos | 2 en paralelo (A y B) | puede correr en paralelo con F1; no depende de codigo | reglas + decisiones; confirma o corrige este plan (lizard real, D-ids nuevos) |
| **W1** | Agente 1: **libreria Tesoreria** (C1..C4 de 4.1) + `ProcesarIngresoEgresoTesoreria` + prueba de rollback conjunto + **levantar G1 en Normal** (re-ejecutar los goldens F1 y agregar `FM-NOR-38/39/90/91`). Agente 2: **`CrearSolicitudRescateReinversion`** (bloqueo propio, handler del Manejador, doble de `InscribirProcesoBatch`) | 2 en paralelo (archivos disjuntos; comparten solo el kit F1, congelado) | W0; F1 pasos 2, 3A, 3B, 4B cerrados y C1..C3 incorporados al paso 2 | G1 y la lista `RescateReinversion` abiertas en configuracion |
| **W2** | Agente 1: **`CrearSolicitudAporteReinversion`** + `ProcesarIngresoOperacionSpot` portado contra `SpotPort` (G2 cerrada). Agente 2: **Programado + Overnight** (solo con D-F2-4 afirmativa; si no, hace la equivalencia dual/fresca/canario de W1) | 2 en paralelo | W1 | lista `AporteReinversion` abierta; G2 cerrada |
| **W3** (bloqueada por D-F2-3) | W3a: `ProcesarCustodiaFondoMutuo` (534) + `ActualizaCarteraValorizadaFFMM` (354) + `RescateTraspaso` (890; G4). W3b (en paralelo con la verificacion de W3a): `RescateTransferencia` + `CambioSerie` (solo si D-F2-4) | 1 + 1 | W2 y Custodia migrada (o decision b/c) | listas abiertas segun la decision |
| **Cierre F2** | arquitectura y seguridad (solo lectura), cableado del router y guarda de jobs para las clases habilitadas, runbook, `modernize-verify` por clase y `SIGNOFF.md` del experto | 2 | W1..W3 | veredicto por clase |

Tamano (planificacion, no compromiso): W0 mediano; W1 grande (la libreria es chica pero `RescateReinversion` es comparable a Normal); W2 grande; W3 mediano. Las 7 clases siguen el mismo esqueleto que Normal, por lo que la parte de orquestacion ya probada en F1 se reutiliza como patron, **no como codigo compartido**: cada clase se porta por separado para conservar sus diferencias (bug a bug); la unica refactorizacion permitida es extraer reglas puras sin cambiar el orden de comprobaciones.

## 6. Como se levantan las compuertas del Manejador (503) clase a clase

1. **Mecanismo (se fija en F1 4B, se usa en F2):** propiedad `fondomutuo.solicitud.listas-servidas` con un valor por handler (`NORMAL`, `PROGRAMADO`, `OVERNIGHT`, `REINVERSION_APORTE`, `CAMBIO_SERIE`, `RESCATE_REINVERSION`, `TRANSFERENCIA`, `TRASPASO`; F1 = solo `NORMAL`). G0 se evalua **antes de simular o escribir cualquier cosa**, sobre todas las listas con filas: una peticion con una lista servida y otra no servida responde 503 completa (si no, Normal escribiria y luego caeria el 503 de la siguiente: el orden de despacho es fijo, FM-MAN-01). Test de F1 que F2 extiende por ola: peticion mixta => 503 sin filas nuevas ni bloqueos.
2. **Compuertas internas por handler** (`G1`, `G2`, `G3`, `G4`, `G5`, `G-CUS`, `G-SPT`) siguen siendo condiciones sobre el contenido (medio de pago, `GeneraSpot`, canal OWP...), evaluadas despues de las validaciones y antes de la primera escritura; todas borran el bloqueo propio de la clase (el de Normal, o el de reinversion) antes de responder 503. `G1` y la propiedad de Tesoreria desaparecen al cerrar W1; `G2` se controla con `fondomutuo.spot.habilitado`; `G-CUS` con `fondomutuo.custodia.habilitada`.
3. **Criterio para levantar una lista** (todos obligatorios): clase PROVEN en H2 (goldens, entradas frescas, mutacion, canario); test "antes: 503 sin efectos" y "despues: servida"; cableado del router (el FQN del Manejador ya esta habilitado desde F1; los FQN directos de las clases 1431..1456 se habilitan solo para `RescateReinversion`, unico con uso directo; los otros 6 siguen a TomEE, que es lo correcto con 0 usos); guarda de jobs probada si algun llamador migrado la usa; firma del experto.
4. **Reversion:** quitar el nombre de la lista (reinicio) devuelve la lista a TomEE sin cambio de codigo; no hay estado propio que migrar salvo las filas ya escritas, que son las mismas tablas.
5. **Lo que no cambia:** los llamadores legacy en proceso de estas clases (`ProcesarReinversionRescate`, `Certificar...`, `ProcesarCarga...`, `AdministracionLiquidez`) siguen ejecutando su copia en TomEE hasta su propia migracion; levantar una lista solo mueve el trafico que entra por el Manejador (R-F2-1).

## 7. Equivalencia y pruebas por ola

Base: el oraculo de F1 (`legacy-oracle/`, `record.sh`, esquema H2 con tipos reales del catalogo, shims de `AUTONOMOUS TRANSACTION`/`#tabla`/secuencias, dobles registrados, `compare.py` con autoprueba, `make_fresh.py`, `LEGACY_OVERLAY`). Cada ola **agrega** casos a `cases.json`/`EQUIVALENCE.json`; no hay un gran examen al final.

| Elemento | Regla F2 |
|---|---|
| Clases del oraculo | cada clase nueva se compila del legacy sin modificar, junto con el cierre ya usado por F1. Las clases en proceso que F1 doblaba pasan a reales cuando su dueno esta en el repo: `CrearIngresoEgreso` legacy real en el oraculo (existe el oraculo de Tesoreria `src/test/resources/tesoreria-a/legacy-oracle` y su esquema) |
| Dobles que siguen | `CrearOperacionSpot`/`ConsultarPuntasPorTramo`/`ConsultarDatosInstrumentos` (Mesa/Plataforma), `IngresoCustodia`/`RetiroCustodia` (Custodia), `SaldoFlujoCaja`, `AgregarLog`, `InscribirProcesoBatch`, `NotificarEstadoOrdenesOWP`, `CalcularParidad`, `EntregarPreciosTasas`, `ObtenerDiaSiguiente`. Se graba el JSON recibido y se compara con lo que envia el modulo nuevo (mismo patron que `ColaboradoresEnrutamientoTest`) |
| Goldens | `src/test/resources/fondomutuo-solicitud/cases/FM-<RRE|ARE|PRO|OVE|CSE|TRF|TRP|SPT|CUS>-nn_<slug>/`; `expected.json` solo se graba ejecutando el legacy; reloj fijo, `PLA.FechaSistema` sembrada, UTC |
| Matriz minima por clase | aporte/rescate; simulacion vs real; medios CIN y no CIN; mezcla de solicitudes buenas y rechazadas; el par aporte+rescate completo e incompleto (FM-MAN-07/D-MAN-7); `IdInterno` repetido y colision con ids de comision NN (D-MAN-3); cada mensaje de error; defectos D-xxx fijados |
| Tesoreria en proceso | dos capas: (1) golden del helper con `CrearIngresoEgreso` doblado (el JSON enviado coincide con el del legacy), (2) 30-40 casos integrados con la libreria real contra el legacy real sobre el esquema de Tesoreria reutilizado, incluido el **rollback conjunto** (la solicitud falla despues de que Tesoreria escribio: 0 filas en `IngresoEgreso`) y el rollback por contador > 0 |
| Entradas frescas | `make_fresh.py` con semilla fija, ejecutado en ambos lados, por ola |
| Mutacion de goldens | `LEGACY_OVERLAY` + `ORACLE_OUT`: se muta una copia del legacy y el golden debe cambiar |
| Canario por ola (>= 5 mutaciones nuevas en copia temporal) | W1: orden de borrado del bloqueo de reinversion antes del 503; `&&` del par aporte/rescate de reinversion; el `UPDATE FechaFinalReinversion`; union a la transaccion (quitar `@Transactional` efectivo = el test de rollback conjunto debe caer); G1. W2: `MOVING`/`MOVEGR` de la rama Spot; gate G2 abierto sin flag. W3: orden de custodia antes/despues del INSERT |
| Determinismo | 6 de 7 usan `parallelStream` y todas `List2Hash`: oraculo con N = 20 corridas por caso; los inestables se excluyen con motivo escrito |
| Regresion | las clases del cierre de F1 estan congeladas tras su PROVEN: cualquier cambio exige re-ejecutar TODOS los goldens previos (job unico `fm-equivalence-all`) |
| Pruebas de seguridad/forma | RULE-076 extendido a las clases nuevas (`main()` con `desa01`), `SinCambiosDeEsquemaTest` con la lista blanca ampliada (`INSERT ReinversionSerieFondoDestino`, `UPDATE ...FechaFinalReinversion`, `CREATE LOCAL TEMPORARY TABLE #p_BloqueoProcesarReinversion`, `DROP TABLE #p_...`, `CALL CrearBloqueoProcesarReinversion/EliminarBloqueoReinversion`), test de recursos SQL duplicados entre jars, `FondoMutuoCableadoRealTest` actualizado por ola |
| Cobertura | JaCoCo >= 80 % de ramas por clase (D-6 de F1) y `lizard` por ola; las funciones de CCN > 80 se descomponen en reglas puras sin cambiar el orden |

**Lo que NO se puede probar (declarar en cada veredicto):** todo lo de F1 (7.5) mas: autonomia real de `CrearBloqueoProcesarReinversion` y concurrencia del bloqueo de reinversion; el comportamiento de Tesoreria sobre HANA (tablas globales temporales reales, secuencias ciclicas); cualquier efecto de Spot y de custodia reales (solo dobles); la mezcla de uso por lista del Manejador (hasta la consulta de 2.4); los datos reales de configuracion de reinversion (porcentajes y series destino); que los llamadores legacy en proceso se comporten igual con la clase nueva (no se cambian). La verdad es el legacy ejecutado sobre H2; PROVEN significa eso y nada mas.

## 8. Defectos

Se reutilizan los ids de `RULES_FONDOMUTUO_SOLICITUD.md` (seccion 8). Presentes por busqueda de texto en las 7 clases (se confirman con linea en W0): **D-NOR-9** (`List2Hash` 9 a 15 usos por clase: lanza con clave repetida, NPE con campos `Long` sin valor), **D-NOR-10** (`Fecha.setLocalOffSet` en las 7; cubierto por DEV-F2), **D-NOR-11** (`printStackTrace` en las 7 y `logger.trace` del JSON de entrada en `AporteReinversion`; cubierto por DEV-F7), **D-MAN-3/5/7** (filtro de "buenas" por `IdInterno`, mensajes de la pasada real descartados, par aporte/rescate incompleto sin mensaje), **D-REQ-1..11** y **D-COM/D-CUO/D-FEC** (cierre compartido, heredados), **D-TES-1** y **D-SPOT-1/2** (helpers). No se hallo por grep el patron `!= "literal"` de **D-NOR-4** en las 7 (a re-verificar con otras formas de comparacion por referencia en W0).

Candidatos nuevos que W0 debe confirmar o descartar (ids provisorios, ninguno leido linea a linea):
| Id | Candidato | Evidencia inicial |
|---|---|---|
| D-F2-1 | El bloqueo de reinversion (`#p_BloqueoProcesarReinversion` + `CALL`) es del mismo tipo que D-NOR-1..3: consulta e inserta sin atomicidad y, si los procedimientos son autonomos, sobrevive al rollback | `RescateReinversion:96-99,1541-1557`; falta el catalogo de los procedimientos (3.2) |
| D-F2-2 | `Programado` y `Overnight` insertan en la misma solicitud un rescate `RESNORM` ademas del aporte: filas de rescate que ningun conteo por clase atribuye a la clase | `Programado:525`, `Overnight:605` |
| D-F2-3 | `ProcesarCustodiaFondoMutuo` mueve custodia con `p_SimularOperacion="N"` fijo (`:190`) dentro de una clase que puede estar simulando | `ProcesarCustodiaFondoMutuo:190`; las 3 clases solo la llaman si no simulan (`:668/674/796`): confirmar que no hay otra via |
| D-F2-4 | `AporteReinversion` usa codigos duros `MOVING`/`MOVEGR` y condiciona la rama Spot a fechas calculadas (`fechaCertificaSpotCompra`) | `AporteReinversion:1293-1343` |
| D-F2-5 | `RescateReinversion` importa la clase anidada de otra clase de 2.504 LOC: acoplamiento que obliga a copiar el pojo exacto | `RescateReinversion:36` |
Todos se mantienen bug a bug y se listan para el experto de dominio (Q-01 de F1 sigue sin respuesta).

## 9. Riesgos (R-F2-1 a R-F2-5 son los cinco mas importantes)

| Id | Riesgo | Mitigacion |
|---|---|---|
| R-F2-1 | **Poco valor de trafico:** F2 sirve solo lo que entra por el Manejador; 3 de las 7 clases tienen 0 filas historicas y el grueso de reinversion/dividendos entra por batch y carga masiva desde clases legacy en proceso | consulta de 2.4 antes de W2/W3; D-F2-2/D-F2-4; declarar "PROVEN-no-activo" |
| R-F2-2 | **Custodia no migrada** bloquea 3 clases (custodia por HTTP rompe atomicidad con posiciones de clientes) | G-CUS cerrada; D-F2-3 (esperar libreria / compensacion con DEV-F8 / no portar) |
| R-F2-3 | **La union de transaccion con Tesoreria depende de una decision de F1** (transaccion Spring, no `Connection` cruda) y de dos choques de empaquetado (contexto y recursos SQL planos) | C1..C4 de 4.1 como requisito del paso 2 de F1; test de rollback conjunto y de recursos duplicados |
| R-F2-4 | **Spot y custodia confirman por su cuenta por HTTP** (D-NOR-12): una operacion Spot o un movimiento de custodia huerfano es dinero/posicion real | G2 y G-CUS cerradas por configuracion; compensacion solo con aprobacion (DEV-F8) y experto |
| R-F2-5 | **Funciones monstruo** (CCN 221, 206, 161, 145) y ~1.060 decisiones nuevas: tests y JaCoCo >= 80 % exigen descomponer sin cambiar el orden; riesgo de regresion en el cierre congelado | tarjetas por regla en W0, `fm-equivalence-all`, canario por ola |
| R-F2-6 | Segundo DDL de sesion (`#p_BloqueoProcesarReinversion`) no cubierto expresamente por la aceptacion de F1 D-2 | D-F2-1 |
| R-F2-7 | Equivalencia solo en H2 (autonomia de procedimientos, escalas, orden sin `ORDER BY`, secuencias ciclicas) | catalogo de 3.2; doble ejecucion en staging para las listas con trafico real |
| R-F2-8 | `SaldoFlujoCaja` (D-5 de F1) sin exponer: G3 deja fuera aportes CIN en 1, 2 y 3 | pedirlo a Plataforma junto con F1 |

## 10. Decisiones y preguntas que necesitan al usuario

| Id | Pregunta | Recomendacion |
|---|---|---|
| D-F2-1 | La aceptacion de D-2 (DDL de sesion que ya ejecuta el legacy) cubre tambien `#p_BloqueoProcesarReinversion` y el `CALL` de sus 2 procedimientos? | si, es el mismo tipo; sin eso `RescateReinversion` no se porta |
| D-F2-2 | Alcance de F2/F3: solo el trafico del Manejador (7 clases) o tambien las vias que hoy mueven el volumen (`APODIV` 35 %, fusion, comision, carga masiva, `Certificar...`)? | decidir con los datos de 2.4; F3 si el objetivo es retirar TomEE del dominio |
| D-F2-3 | Custodia: esperar su libreria (a), compensar por HTTP con DEV-F8 (b), o no portar las 3 clases (c) | (a), con W3 escrita pero apagada |
| D-F2-4 | Hay uso de negocio vigente de Programado, Overnight, CambioSerie, Transferencia? (0 filas con su movimiento en 18 anos) | si no hay: retirarlas del alcance (G0 fija a 503 -> TomEE) y documentar como candidatas a baja |
| D-F2-5 | Spot: B con G2 cerrada (recomendado), compensar con `AnularOperacionSpot` (DEV-F8), o esperar a Mesa; y pedir que Mesa priorice `CrearOperacionSpot` | B cerrada + C |
| D-F2-6 | Aprobar C1..C4 (transaccion Spring, contexto selectivo, recursos SQL sin colision, `Colaboradores` HTTP) como requisitos del paso 2 de F1 | aprobar, incorporar ya |
| D-F2-7 | Autorizar el `SELECT` agregado de 2.4 y el catalogo de 3.2 | autorizar |
| D-F2-8 | Aprobar el corte W0..W3 y la regla "una clase por vez, sin codigo compartido nuevo" | aprobar |
Preguntas abiertas heredadas de F1 y que afectan a F2: Q-01 (experto de dominio), Q-03 (`p_FormaPagoSpot` en la UI y la ruta de `t=601190`), Q-08 (`NotificarEstadoOrdenesOWP`), Q-09 (estado de la ola A de Plataforma: `SaldoFlujoCaja`, `CalcularParidad`), Q-10 (empaquetado de Tesoreria: **respondida por este analisis**: ya existe como jar plano y lo usan los jobs), Q-11. Nuevas: Q-F2-1 (que mueve realmente `APOREINV` frente a `RESREINV`: ratio 2,1), Q-F2-2 (de donde vienen `APOCANJE`/`RESCANJE` si ninguna clase Java los nombra), Q-F2-3 (orden de Custodia: entran `IngresoCustodia`/`RetiroCustodia` al orden aprobado y cuando).

## 11. Que no se pudo comprobar al hacer este plan
No se leyeron linea a linea las 7 clases ni los 3 helpers (solo conteos por texto, imports, constantes y las lineas citadas); las decisiones por clase son promedio x funciones de lizard (+-1); el mapa movimiento -> clase sale de las constantes y asignaciones `IdTipoMovimientoFondoMutuo` (`APOREINV` pudiera escribirlo tambien `RescateReinversion`; `RESCAM`/`CANJE` sin conciliar); el peso es historico completo, no periodo reciente ni via de entrada; el estado de los servicios modernos de Mesa se infiere por tamano (110 y 60 LOC frente a 2.752 y 203) y no se releyo su logica; no se verifico que el `-exec` de `tesoreria-service` conviva con otro `spring-boot-maven-plugin` en el mismo reactor (los jobs ya lo hacen); `ActualizaCarteraValorizadaFFMM` (354 LOC) no esta en los 9.309 LOC de la seccion 2.1 (suma ~9.663 con ella). Nada se ejecuto contra HANA; ningun archivo fuera de este plan se modifico.

## 12. Decisiones del usuario (2026-10-22)

| Decision | Resultado |
|---|---|
| D-F2-6 | **Aprobada**: C1 (unidad de trabajo de F1 = transaccion de Spring sobre el mismo `DataSource`, no `Connection` cruda en un filtro), C2 (importar solo los beans y puertos necesarios de Tesoreria; sin escanear todo `cl.bcs.tesoreria`), C3 (F1 elimina el directorio plano `sql/` de 376 archivos y usa rutas por clase/paquete para evitar choques en el classpath) y C4 (colaboradores propios de Tesoreria por HTTP tras su puerto `Colaboradores`; `AgregarLog` por HTTP no es atomico y cae en G5) son **requisitos del paso 2 de F1**. |
| D-F2-1..5, D-F2-7 | Sin decision aun. |

Actualizacion 2026-10-22: el usuario indico "para las D-F abiertas, sigue las recomendaciones". Quedan decididas asi:

| Decision | Resultado (recomendacion del plan) |
|---|---|
| D-F2-1 | **Si**: la aceptacion del DDL de sesion de F1 cubre tambien `#p_BloqueoProcesarReinversion` y sus 2 procedimientos. |
| D-F2-2 | F2 = solo el trafico del Manejador; `APODIV`, fusion, comision y carga masiva se planifican despues como F3. |
| D-F2-3 | Custodia: esperar su libreria (no compensar por HTTP; no hay DEV-F8). Las 3 clases dependientes quedan en W3. |
| D-F2-4 | Programado, Overnight, CambioSerie y Transferencia (0 filas historicas): fuera del alcance salvo que el negocio confirme uso. |
| D-F2-5 | Spot: compuerta G2 cerrada por configuracion con el puerto `SpotPort`; sin compensacion. Se pide a Mesa priorizar `CrearOperacionSpot`/`ConsultarPuntasPorTramo`. |
| D-F2-7 | Autorizado el SELECT agregado de solo lectura y el catalogo de los 2 procedimientos de reinversion. |

## 13. Resultados de las consultas autorizadas (HANA QA, solo lectura, 2026-10-22)

Programa `tools/HanaFondoMutuoF2.java`; resultados `hana_qa_results/f2_*.csv`.

- **Procedimientos `CrearBloqueoProcesarReinversion` y `EliminarBloqueoReinversion`: ambos `AUTONOMOUS TRANSACTION`** (no de solo lectura): el bloqueo de reinversion sobrevive al rollback igual que el de Normal (D-NOR-3). Confirma D-F2-1.
- `FMM.ReinversionSerieFondoDestino` (9 columnas): `Porcentaje` y `PorcentajeReal` son `DECIMAL(26,8)`; `FechaFinalReinversion` DATE; `Switch` NVARCHAR(1); `IdReinversionSerieFondo`/`IdSolicitudAporteRescate`/`IdCarga` BIGINT. Sustituyen las escalas supuestas del oraculo.
- **Ultimos 24 meses (por tipo de movimiento y modulo):** `APOREINV` 438.870 (FMM, 429.790 no batch) y `RESREINV` 83.612 (74.777 no batch): **la reinversion tiene uso vivo y es la mayor parte del volumen reciente de las clases de F2**; `APONORM` ~248.657 (APV 130.561, FMM 73.139, TES 44.957); `APODIV` ~260.886 (CUS batch 247.509: **fuera de F2/F1, F3**); `APOFUSION`/`RESFUSION` ~43.700 cada una (batch); `RESNORM` ~83.475; `RESTRASP` 10.161 (APV 9.007) y `APOTRASP` 8.344.
- **Programado, Overnight, CambioSerie y Transferencia: 0 filas tambien en los ultimos 24 meses** (consistente con 0 historico): apoya D-F2-4 (fuera de alcance).
- No se ejecutaron los dos agregados de "filas con operacion Spot / asiento de Tesoreria" (columnas por confirmar); se piden en W0 si hacen falta.

## 14. Correcciones W0 (2026-10-22, agente A: RescateReinversion, AporteReinversion, helpers Tesoreria y Spot)

Fuente: `RULES_FONDOMUTUO_F2_REINVERSION.md` (lectura linea a linea de las 4 clases). Se anexan sin editar el texto anterior.

| # | Texto del plan | Correccion | Evidencia |
|---|---|---|---|
| C-W0A-1 | Seccion 2.1 fila 5: `RescateReinversion` "valida/inserta el lado aporte `APOREINV`" | Inserta solo `RESREINV` (`:737`) y filas de `FMM.ReinversionSerieFondoDestino`; **valida** el aporte pero no lo inserta. `APOREINV` lo crea `CrearSolicitudAporteReinversion`, llamada desde `CertificarSolicitudAporteRescate.crearAporteReinversion` (`:762,2343-2556`, canal `AGE`) y `ProcesarCargaSolicitudesAporteRescate:485-554`. Responde Q-F2-1: la razon 2,1 es 1:N destinos por rescate | `RR:737,1248-1279` |
| C-W0A-2 | Seccion 3.1/2.1: "`AporteReinversion`... la llaman en proceso `Certificar...`, `ProcesarCarga...`, `ConsultarFechaSolicitudAporteRescate` y el propio `ProcesarIngresoOperacionSpot`" | `ConsultarFechaSolicitudAporteRescate:21` y `ProcesarIngresoOperacionSpot:20` solo **importan su pojo** `InSolicitudesAporte`; no llaman a `logica`. Los llamadores reales son `Certificar...`, `ProcesarCarga...` y el Manejador | grep de `CrearSolicitudAporteReinversion` |
| C-W0A-3 | R-F2-1 / seccion 13 (`APOREINV` 438.870 en 24 meses = "uso vivo de las clases de F2") | Los 438.870 `APOREINV` **no entran por el Manejador**: `AporteReinversion` tiene 0 usos directos y se invoca al certificar desde legacy en proceso. Migrar AR no mueve ese volumen hasta que `Certificar...` (F3) se migre o su llamada se reapunte; declarar "PROVEN-no-activo". RR (`RESREINV`, 239 usos directos) si entra por `/call` y por el Manejador | `Certificar...:762,2343-2556`; seccion 13 |
| C-W0A-4 | D-F2-5 (seccion 8): "`RescateReinversion` importa la clase anidada `BloqueoReinversion` de `ProcesarReinversionRescate`: hay que copiar el pojo exacto" | **Refutado.** RR declara `BloqueoReinversion` (`:247-266`, con `Simula`) y `DesbloquearReinversion` (`:268-271`) propias, que ocultan los imports `:36-42` (sin uso). El pojo de `ProcesarReinversionRescate` tiene `RescateTotal` y no `Simula`. Acoplamiento real: 4 clases importan `InSolicitudesRescate/Aporte` de RR | `RR:36-42,247-271`; `ProcesarReinversionRescate.java:387-410` |
| C-W0A-5 | Seccion 4.1: `ProcesarIngresoEgresoTesoreria` "1 SQL propio"; 4.2: `ProcesarIngresoOperacionSpot` "sin SQL propio" | TES: 1 archivo + **3 SQL en linea** (`ACC.Usuario`, `ConsultarPreferenciaPagoPortafolio`, `TES.PreferenciaPagoCorredor` con filtros fijos); SPO: **1 SQL en linea** (`MES.Mesa`, `TOP 1` sin orden). No estan en ninguna comparacion SHA-256 | `TES:40,42,45`; `SPO:61` |
| C-W0A-6 | Seccion 4.1: punto de entrada "llamada directa al bean `execute(JsonObject)`" | Existe tambien `logica(CrearIngresoEgreso_Entrada)` tipado y `@Transactional` (REQUIRED), el que usa `CrearTraspasoService`; los contratos tipados son clases anidadas de `CrearIngresoEgresoService`. Recomendado: `logica` tipado. Ademas la costura requiere **C1b**: el `DataSourceTransactionManager` con `setGlobalRollbackOnParticipationFailure(false)` (como `TransaccionesLegacyConfig` de Tesoreria), o una excepcion tragada dentro de un colaborador dejaria la transaccion compartida en rollback-only | `CrearIngresoEgresoService:605-633`; `TransaccionesLegacyConfig` |
| C-W0A-7 | Seccion 3.1/3.3: RR "`InscribirProcesoBatch` (H, 6 refs)" y "su cierre externo es solo `InscribirProcesoBatch`" | RR llama ademas a `AgregarLog` (G5) siempre que inserte; `InscribirProcesoBatch` solo con canal distinto de `WEB`/`EJE` y portafolio APV de la primera solicitud; el programa es `INGRESAR_BONIFICACION_FISCAL_REINVERSION` | `RR:1294-1394` |
| C-W0A-8 | D-F2-4: "`AporteReinversion:1293-1343`" | Lineas reales `AR:1286-1363` (formas de pago y ids codificados `COMSPT_CIN_CLP_0`, `VENSPT_CIN_USD_0`, `COMSPT_CIN_USD_0`, `VENSPT_CIN_CLP_0`) | `AR:1286-1363` |
| C-W0A-9 | Seccion 2.1 hecho comun: "`logger.trace` con JSON (solo `AporteReinversion`)" | No se encontro `logger.trace` del JSON de entrada en AR; si usa `new Log("FM.CrearSolicitudAporteReinversion")` (`log.write`, `AR:83,256`) y `printStackTrace` en `AR:410` | grep |
| C-W0A-10 | Seccion 3.2: catalogo pedido solo para los 2 procedimientos y `ReinversionSerieFondoDestino` | Faltan en el catalogo la tabla `FMM.ProcesarReinversionRescate_BloqueoReinversion` (la que escriben/leen el bloqueo), las secuencias `IdAgrupador` y `IdReinversionSerieFondo` y el default de `IdCarga`. La fuente de ambos procedimientos esta en `BSCL/FondoMutuo/BD/Servicios/*.sql`; `Scripts/FMM_Create_Tables.sql` esta desactualizado (4 columnas en vez de 9) | seccion 5 del archivo de reglas |
| C-W0A-11 | Seccion 8: D-NOR-4 presente por busqueda de texto en las 7 clases "a re-verificar" | En RR, AR, TES y SPO no hay comparaciones `String` por `!=`/`==` (todas `equals`/`equalsIgnoreCase`): D-NOR-4 no aplica a estas 4 clases | grep |
| C-W0A-12 | Seccion 2.1: lizard AR ~225 / RR ~264 / TES 40 / SPO 35 | Confirmado con `lizard`: AR 226, RR 264, TES 40, SPO 35 decisiones (CCN maxima 206 / 221 / 40 / 27) | `mvscratch\liz_sum.out` |

Hechos nuevos que afectan al plan (detalle en el archivo de reglas): `IdAgrupador` por defecto 0 funde todos los rescates en una solicitud (D-F2-6); RR no tiene `try/catch` y deja el bloqueo autonomo tras una excepcion (D-F2-1); AR ignora los errores de Tesoreria salvo `RECH` y no guarda `IdIngresoEgreso` (D-F2-11); los SQL propios de las 4 clases (23) son identicos al moderno; RR y AR activan por primera vez reglas del cierre antes "inertes en Normal" (FM-REQ-01, 03, 11, 15).

## 15. Consultas W0 respondidas (HANA QA, solo lectura, 2026-10-22; programa `tools/HanaFondoMutuoF2b.java`, `hana_qa_results/f2b_*.csv`)

- `FMM.ProcesarReinversionRescate_BloqueoReinversion` es tabla **COLUMN normal, no temporal** (`IS_TEMPORARY=FALSE`): columnas `IdBloqueaReinversion` NVARCHAR(70), `IdPortafolio` BIGINT, `IdRegimenTributario` NVARCHAR(40), `IdSerieFondo` NVARCHAR(40), `Monto` DECIMAL(26,8) def. 0, `NumeroCuotas` DECIMAL(26,8) def. 0, `RescateTotal` NVARCHAR(1) def. 'N', `FechaSistema` DATE def. 0001-01-01, `IdCliente` BIGINT, `IdGrupoRegimen` NVARCHAR(40). (El bloqueo de reinversion toca una tabla persistente compartida entre sesiones, ademas de la temporal de sesion: relevante para la autonomia y la concurrencia.)
- Secuencias: `IdAgrupador` (inicio 99.857.636, sin ciclo), `IdReinversionSerieFondo` (inicio 1, sin ciclo).
- `IdCarga` es `BIGINT NOT NULL DEFAULT 0` en `FMM.SolicitudAporteRescate` y `FMM.ReinversionSerieFondoDestino`.
- **Medio de pago de la reinversion (24 meses): 100 % CIN** (`APOREINV` 438.870 y `RESREINV` 83.612, todos `IdMedioDePago='CIN'`): la compuerta G1 (medio distinto de CIN) **no se activa en la practica** para la reinversion.
- Agregado de `IdIngresoEgreso`/`IdOperacionSpot`: **no concluyente** (todas las filas aparecen "con valor": las columnas son NOT NULL con valor por defecto, no sirve la prueba `IS NULL`). El peso real de Tesoreria/Spot (G2) queda sin medir; si hace falta, comparar contra el valor por defecto.

## 16. Decisiones del usuario sobre la W0 (2026-10-22)

- **C1b aprobado** junto con C1..C4 como requisito del paso 2 de F1 (`DataSourceTransactionManager` de FondoMutuo con `setGlobalRollbackOnParticipationFailure(false)`, como `TransaccionesLegacyConfig` de Tesoreria).
- **`AporteReinversion`: se pospone** hasta migrar `CertificarSolicitudAporteRescate` (F3); `RescateReinversion` va primero.
- **Concepto de Tesoreria de `APOREINV` (APORFM frente a APOREI)** investigado en codigo y en HANA (solo lectura; programa `tools/HanaConceptoReinversion.java`, `hana_qa_results/concepto_*.csv`):
  - Codigo legacy: `ConstantesTesoreria` define `ConceptoAporteNormal = "APORFM"`, `ConceptoAporteCambioSerie = "APORFM"` y `ConceptoAporteReinversion = "APOREI"` (esta ultima existe pero `AporteReinversion` no la usa: usa APORFM; defecto D-TES-2). Las vistas y centralizaciones de contabilidad/homologacion/`ConsultarOperaciones` mapean `APOREINV` -> `APORFM`, es decir, los consumidores aguas abajo esperan APORFM para la reinversion.
  - Catalogo HANA (`TES.ConceptoTesoreria`): `APOREI` es de `Tipo='EG'` (egreso), `Origen='FFM'`, visible en Fiori/WS; `APORFM` tiene `Tipo` vacio, `Origen='FFM'`, visible tambien en Optimus. Un concepto de egreso no encaja semanticamente con un aporte.
  - Datos reales en QA: `TES.IngresoEgreso` tiene 637.493 filas y **ninguna** con concepto `APORFM` ni `APOREI` (los ingresos/egresos de fondos mutuos usan otros conceptos: `RESCFM` 92.783, `ABREFM` 12.860, `CAAEFM` 9.591, `APORCI` 6.184, `RESTRS` 11.266, ...); el cruce `FMM.SolicitudAporteRescate.IdIngresoEgreso` con `TES.IngresoEgreso` no devuelve filas en 24 meses. Los datos **no permiten confirmar** que concepto se espera; muestran que la reinversion (100 % CIN) no genera en la practica un ingreso/egreso de Tesoreria con esos conceptos.
  - **Conclusion:** se replica el comportamiento legacy (APORFM), que es lo que la regla del proyecto exige; el concepto "esperado" deja de ser bloqueante y queda como pregunta de bajo riesgo para el experto contable, junto con por que `APOREI` (egreso) existe sin uso.

## 17. Decisiones del usuario sobre W1 frente 1 (2026-10-24)

- **DEV-F5 dentro de Tesoreria: aprobada.** `CrearIngresoEgresoService.logica` se traga las excepciones de sus colaboradores; el helper de FondoMutuo relanza la primera falla HTTP, asi la solicitud responde 503/500 con rollback completo en vez de confirmarse.
- **C4 precisada: aprobada.** Las dos escrituras propias de Tesoreria alcanzadas por el comprobante (procedimiento `CrearComprobante` y `ActualizarSaldosCuentaBanco`) corren en proceso, en la transaccion de la solicitud; solo los colaboradores de Plataforma (`RevisarRequisitosBasicos`, `SaldoFlujoCaja`, `AgregarLog`) van por HTTP.
- **Capa 2 del oraculo (seccion 7, 30-40 casos integrados con el `CrearIngresoEgreso` legacy real y rollback conjunto) antes de `RescateReinversion`.**
- Frente 1 entregado en el commit `modernized` del 2026-10-24 (771 pruebas, 0 fallas; G1 abierta por configuracion, reversible).
- **Capa 2 entregada** (2026-10-24): 32 casos FMM-NOR-T01..T32 contra el `CrearIngresoEgreso` legacy real, 32/32 iguales; 803 pruebas.
- Decisiones sobre la capa 2: (a) golden FMM-NOR-91: **medir 20 grabaciones** (plan 7, determinismo); si el texto de la excepcion varia, se marca inestable con motivo y se compara solo la clase. (b) **Agregar un golden de DEV-F5 en Tesoreria** (falla del `AgregarLog` llamado por Tesoreria: legacy 200 con error, modulo 503) con `approvedDifference` citando esta aprobacion del usuario. (c) **Aceptado** el registro de peticiones consecutivas de la misma secuencia de Tesoreria como una sola entrada (mismos valores y orden).
