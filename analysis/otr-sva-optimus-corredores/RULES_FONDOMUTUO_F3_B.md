# Reglas extraidas F3 (W0, agente B): ciclo de vida de la solicitud (Certificar, CertificarPorFiltros, Liquidacion, Anulacion)

Fecha: 2026-10-26. Ola W0 de `PLAN_FONDOMUTUO_F3.md` (secciones 0, 2.B, 3, 8, 9). Formato modelo: `RULES_FONDOMUTUO_F2_REINVERSION.md` (que a su vez sigue `RULES_FONDOMUTUO_SOLICITUD.md`).
Alcance leido linea a linea (legacy de solo lectura, `legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/`): `CertificarSolicitudAporteRescate.java` (3.052 LOC; en las citas **C**), `CertificarPorFiltrosSolicitudAporteRescate.java` (154; **X**), `LiquidacionSolicitudAporteRescate.java` (765; **L**), `AnulacionSolicitudAporteRescateFolioContrato.java` (223; **AF**) y `AnulacionSolicitudAporteRescate.java` (981; **A**, solo para decir que hace y si esta portada), mas lo necesario de `BcsJobsFondoMutuo/.../BcsJobCertificacionMasivaSolicitudes.java` (**J**), `CallServlet.java` (anfitrion), `BcsUtils/.../SQL.java` y `Utils.java`, `pojo/ConstantesFondoMutuo.java` (valores de las constantes `_c.*`), los 32 SQL propios (y `ConsultarRetencion.sql` de APV), y el lado moderno por nombre (`modernized/.../BcsFondoMutuoServicios/src/main/java`, solo lectura).
No se toco legacy ni modernized, no se conecto a HANA, no hubo llamadas de red, no se creo ningun archivo (este texto lo entrega el agente para que lo escriba la sesion). Nada de aqui es veredicto de equivalencia: son reglas observadas para convertir en casos del oraculo.
Revision de contenido hostil: no se encontro texto en comentarios o literales dirigido a manipular un analisis automatico (los comentarios son del tipo `CELLINCECT-763`, `TODO Auto-generated`, `//SE SUMA LA DIFERENCIA AL ULTIMO APORTE`). Ningun comentario se uso como prueba de comportamiento: cuando un comentario y el codigo no coinciden se informa (ej. "ultimo aporte" que en realidad es el primero, FM-CER-59).

Convenciones. Cita `Clase:linea` con C/X/L/AF/A/J. Tarjeta: Given/When/Then; `Fuente`; `E/S`; `Defectos`; `Gate`; `Depende F1`; `Experto`; `Confianza`. Ids: **FM-CER-01..73** (Certificar, por fase), **FM-CXF-01..06** (CertificarPorFiltros; el prefijo FM-CPF lo usa el grupo A para ConsultarCustodiaPortafolio), **FM-LIQ-01..20**, **FM-ANU-01..07**. Se REUSAN sin duplicar: `FM-NOR-01` (requisitos basicos), `FM-NOR-08/FM-REQ-20` (perfil), `FM-NOR-44` (log), `FM-NOR-51/52` (AGF), `FM-CUO-01..10` (cuotas), `FM-COM-*` (comisiones), `FM-FEC-*`, `FM-REQ-*`, `FM-TES-01..13`, `FM-SPO-01..13`, y las 28 tarjetas **FM-REI-41..68** de `RULES_FONDOMUTUO_F2_REINVERSION.md` seccion 8.2 (AporteReinversion; NO se re-extraen). Defectos: se reusan `D-NOR-n`, `D-REQ-n`, `D-FEC-n`, `D-CUO-n`, `D-COM-n`, `D-F2-n`, `D-TES-n`, `D-MAN-n` por id; los nuevos son `D-F3B-nn` (seccion 9). Todo defecto se mantiene bug a bug (decision vigente) y se lista para el experto (Q-01 sin respuesta). Los valores de los ejemplos son inventados (no son datos reales); los ids reales que traen los `main()` no se copian.

## 1. Resumen ejecutivo

1. **Certificar es una orquesta de una sola funcion**: `logica` (`C:370-868`, 387 NLOC, CCN **95**) mas 84 funciones auxiliares; 395 decisiones en total (plan: ~396). Las fases, en el orden en que se ejecutan y escriben: requisitos y parametros -> lecturas -> validacion por solicitud (hasta 11 reglas, la primera que falla gana) -> salida temprana si ninguna sobrevive -> recalculo de cuotas y montos -> partir en aportes y rescates -> aportes (saldo de flujo de caja, costo UF, custodia, cuenta de inversion) -> rescates (custodia disponible, retencion APV, comision, custodia, costo UF/spread, cuenta de inversion, Tesoreria, facturacion, aporte de reinversion, Spot) -> UPDATE masivo `Estado='CRT'` -> notificaciones -> certificacion recursiva de los aportes `Switch` -> log y salida. Ver seccion 12 (frentes).
2. **"Simular" no existe como tal**: `p_SimularOperacion` solo se usa para (a) copiarlo al log y al hijo `Switch`, y (b) forzar `P_OUT_ERRORESCONTADOR=1` al final (`C:862-864`). Todos los colaboradores se llaman con `"N"` (`C:1731,1919,2047,2301,2333,2543`) y escriben de verdad; el rollback lo hace el anfitrion (`CallServlet` revierte si el contador es > 0, `CallServlet.java:~432-437`). Efectos no transaccionales (notificacion asincrona `C:836` y OWP `C:848-850`) salen igual en simulacion. D-F3B-01/02.
3. **Fallo parcial que se confirma**: los rechazos de Tesoreria (`RECH`+`NumeroReferencia`, `C:699-709`) y de Spot (`C:798-829`) marcan la solicitud `RZD` pero **no suben el contador**; el contador de Tesoreria ni siquiera se lee. Si el resto del lote va bien el anfitrion hace `commit`, y quedan confirmados el retiro de custodia, los cargos/abonos de cuenta de inversion y los comprobantes de facturacion de la solicitud rechazada, que no se actualiza a `CRT`. D-F3B-03 (Media-Alta; depende de los contadores internos de los colaboradores, que no se leyeron).
4. **`ResumenPatrimonial` (Normativa) no se invoca**: `C:67` solo importa `AporteRescate` y no hay ningun uso (grep). El plan la lista como colaborador HTTP; en el codigo no lo es. D-F3B-15.
5. **Los servicios modernos de las 5 clases son andamios, no portes**: `CertificarSolicitudAporteRescateService` (109 LOC), `CertificarPorFiltros...Service` (52), `LiquidacionSolicitudAporteRescateService` (69), `AnulacionSolicitudAporteRescateService` (103), `...FolioContratoService` (77) leen los mismos SQL pero devuelven filas crudas (`P_OUT_SOLICITUDES`, otro contrato), no validan, y ejecutan un `batchUpdate` con 2 parametros (`IdSolicitudAporteRescate`, `IdEmpresa`) contra SQL que exigen ~20 parametros con nombre. Estan cableados en `adapter/in/web/InternalJavaCallController.java:489-531` bajo los nombres legacy. Nada de F3-B esta portado. D-F3B-28.
6. **lizard (seccion 3)**: C 395 decisiones, X 2, L 76, AF 12, A 125. **SQL (seccion 4)**: 32 propios identicos (0 distintos, 0 faltantes) en `src/main/resources/cl/bcs/fondomutuo/sql`; `ConsultarRetencion.sql` identico en `BcsAPVServicios`.
7. Tarjetas (seccion 8): **106** (CER 73, CXF 6, LIQ 20, ANU 7). Defectos nuevos D-F3B-01..30 (seccion 9). Matriz de casos (seccion 11), frentes (seccion 12), preguntas Q-W0B-1..16 (seccion 13).

## 2. Alcance, entradas de las clases y quien las llama

| Clase | Entra por | La llaman en proceso | Escribe |
|---|---|---|---|
| `CertificarSolicitudAporteRescate` (C) | `/call` tipo J (`CallServlet`); registro e id no verificados (el plan reporta uso 894 en QA, no verificado) | `CertificarPorFiltros...:122`; `BcsJobCertificacionMasivaSolicitudes:136-154` (job `CERTIFICACIONMASIVASOLICITUDESFFMM`); `AdministracionLiquidez:373-398` (certifica los aportes overnight; `p_IdTipoCanalIngreso=""`, simulacion de la entrada); `ProcesarCargaSolicitudesAporteRescate:1129-1147` (si `certificacionAutomaticaCargaMasiva="S"`; `p_SimularOperacion="N"`, fecha = fecha de sistema); `RevalorizarMovimientosFFMM:84-152` (Plataforma; anula y luego certifica); recursion propia `C:2764-2775` (aportes `Switch`). Solo importan pojos: `SimulaCalculoSpreadRescate` (`Estado`, `InSolicitudes`, `SolicitudesProcesoCertificacion`), `CrearSolicitudAporteReinversion:27`, `ProcesarCobroComisionPatrimonio` y `ConfirmarLiquidacionSpot` (`Cobro`), `CrearComprobante:62` (`Persona`) | `FMM.SolicitudAporteRescate` (UPDATE), `FMM.LogCertificacionAporteRescate`, custodia, cuenta de inversion, Tesoreria, Facturacion, Mesa (Spot), reinversion (APOREINV), log de plataforma |
| `CertificarPorFiltrosSolicitudAporteRescate` (X) | `/call`; plan: uso 51 | `BcsJobCertificacionMasivaSolicitudes:81-109` (`EsMasivo="S"`) | nada propio (delegada) |
| `LiquidacionSolicitudAporteRescate` (L) | `/call`; plan: uso 450 | ninguna (grep) | `FMM.SolicitudAporteRescate` (UPDATE), Tesoreria `TES.IngresoEgreso*`, cuenta de inversion, log |
| `AnulacionSolicitudAporteRescateFolioContrato` (AF) | `/call`; plan: uso 277 | ninguna (grep) | via `AnulacionSolicitudAporteRescate`, mas `APV TRA.Traspaso.IdEstado` |
| `AnulacionSolicitudAporteRescate` (A) | `/call` (id 402 de procedimiento legacy, `PLA_Insert_Servicios.sql:890`; el id de la clase Java no se verifico) | `AF:163`, `RevalorizarMovimientosFFMM:123-125` | ver FM-ANU-06 |

Detalle del job (`J`): parametros `IdEmpresa, IdUsuario, SimularOperacion, UsaReglasNegocio, FechaCertificacion, IdInternoFiltro...IdCarga` (listas separadas por `;`), `EsMasivo`, `IdSolicitud`; `EsMasivo="S"` -> arma `InFiltros` por posicion y llama a X; si no, llama a C con los `IdSolicitud`; si `P_OUT_ERRORESCONTADOR>0` lanza `Exception` con la **ultima** descripcion de error (`J:111-125,155-170`); no pasa `p_IdTipoCanalIngreso`. El `commit` del job lo hace el planificador (`BcsJob`, no leido). El `main()` del job y los `main()` de las 5 clases NO se portan (llevan alias de conexion de QA/DESA/PRESURA y ids reales de solicitud/cliente, que se omiten aqui).

## 3. Complejidad ciclomatica real (lizard, `python -m lizard -l java <archivo>`)

Decisiones = suma de CCN por funcion menos numero de funciones (los nombres de clase que imprime lizard, `ParametroOperaCSN::logica`, son artefacto de las clases anidadas).

| Clase | LOC | NLOC | Funciones | Suma CCN | Decisiones | Funciones con CCN alta |
|---|---:|---:|---:|---:|---:|---|
| `CertificarSolicitudAporteRescate` | 3.052 | 2.666 | 85 | 480 | **395** | `logica` (`C:370-868`, 387 NLOC) **95**; `crearAporteReinversion` (`2343-2557`) **43**; `validarSaldoCuentaInversion` (`1149-1225`) 29; `validarSaldoCuentaInversionPAC` (`1227-1303`) 29; `reCalcularCostoUFAportes` (`2939-3019`) 26; `mueveCuentaInversion` (`1741-1935`) 25; `creaMovimientoFacturacion` (`2159-2309`) 24; `mueveSpot` (`2057-2157`) 23; `mueveTesoreria` (`1953-2054`) 18; `cambiarMontosRescatesPorCantidadCustodia` 13; `actualizarMontosSolicitudes` 9; `marcarRescatesConRetencion` 8; `creaComprobanteFacturacion` 8; `calcularCostoUFRentabilidadRescates` 7; `ingresoRetiroCustodia` 7 |
| `CertificarPorFiltrosSolicitudAporteRescate` | 154 | 128 | 8 | 10 | **2** | `logica` 2 |
| `LiquidacionSolicitudAporteRescate` | 765 | 639 | 15 | 91 | **76** | `logica` (`L:215-736`, 433 NLOC) **67**; `Grupo.equals` 10 |
| `AnulacionSolicitudAporteRescateFolioContrato` | 223 | 176 | 12 | 24 | **12** | `logica` 12 |
| `AnulacionSolicitudAporteRescate` | 981 | 869 | 18 | 143 | **125** | `logica` (`A:224-750`) **101**; `mueveCuentaInversion` 24 |
| **Total** | 5.175 | 4.478 | 138 | 748 | **610** | 11 funciones sobre CCN 15 |

Lectura: `Certificar.logica` ya es "una receta" de CCN 95; el peso real esta en las 9 funciones de CCN 18-43 que construyen entradas de colaboradores. Se descompone en reglas puras sin cambiar el orden de comprobaciones (el orden decide que codigo `Codigo` queda en cada solicitud: la primera regla que rechaza gana, y despues un `continue`).

## 4. Comparacion SHA-256 de los SQL (sin normalizar; prefijo de 16 hex del legacy)

Legacy: `.../fondomutuo/sql`. Moderno: `modernized/.../BcsFondoMutuoServicios/src/main/resources/cl/bcs/fondomutuo/sql`. De los **32** SQL propios: **32 identicos / 0 distintos / 0 faltantes**. APV `ConsultarRetencion.sql` (`26785d286424e99e`): identico en `BcsAPVServicios/src/main/resources/sql/tra/`.

- C (15): `CertificarSolicitudAporteRescate_SelectSolicitudes` e0ae667cab0ac5ec (129 lineas), `_SelectEstados` 9807520638771402, `_SelectComprobantesNoLiquidados` a424da79df80f92b, `_UpdateSolicitudes` 3a0837e18309caed, `_ConsultaTraspasoAPVAsociado` fc7dffb634319ddb, `_ConsultaTraspasoNoAPVAsociado` 1f61886f3779b728, `_SelectPersonaPreferenciaPagoTGR` 1ee1c385a0ea2dd0, `_SelectCobroIva` 0016baa444b5263e, `_SelectMontoMonedaSistemaSolicitudesReinversion` 220c305ae737292e, `_UpdateMontoMonedaSistemaSolicitudesReinversion` 7a4f7873356f4fc4, `_SelectTipoMovimientoFondoMutuoGeneraCargoAbono` b29755228b3d697d; compartidos: `ComunesSolicitudes_SelectIndicadoresPortafolioFFMM` b5365d78d1b221f9, `ComunesSolicitudes_SelectReinversionSerieFondoDestino` d6b316bfb03686a6, `ComunesSolicitudes_SelectReinversionSerieFondosSolicitudRescateReinversion` 238f4e1e6ad72c41, `CrearSolicitudAporteRescateNormal_SelectParametroOperaConSaldoNegativo` 450d0a5a042ba216.
- Colaboradores del modulo: `AgregarLogCertificacionAporteRescate_InsertLogCertificacion` 6d983aef2e7cce78, `CalculaCustodiaRescate_SelectCarteras` 0e668a1c649d464e.
- X (1): `CertificarPorFiltrosSolicitudAporteRescate_SelectSolicitudes` 1187dfe3e85966b8.
- L (4 + 1 APV): `LiquidacionSolicitudAporteRescate_SelectSolicitudes` 9069fa2a0bb4a52a, `_SelectSolicitudesFiltros` 5889aed6e7be9ceb, `_UpdateSolicitudes` 704c706b58b3c7fa, `ComunesSolicitudes_SelectModalidadPago` 69512e6614364d5d (el archivo `LiquidacionSolicitudAporteRescate_SelectModalidadPago.sql` existe pero la clase carga el `Comunes...`), APV `ConsultarRetencion` 26785d286424e99e.
- A (8 + 1 compartido): `AnulacionSolicitudAporteRescate_SelectSolicitudes` 2dcea227c3835026, `_SelectCartaLiquidacion` 350954eb6c643366, `_SelectIngresoEgreso` 39d9081d6481d02e, `_SelectCarteraAportes` 13114eeb28a0f3d3, `_SelectCarteraRescates` db3b24d4288b134d (80 lineas), `_UpdateSolicitudes` 0d294118f355d666, `_SelectOperacionSpot` 1ddb001cd8f9788c; usa ademas `CertificarSolicitudAporteRescate_SelectSolicitudes` (OWP).
- AF (3): `AnulacionSolicitudAporteRescateFolioContrato_SelectSolicitudes` 0ae5af80224c2c8e, `ConsultaTraspasoEgresoAPVAsociado` c30c18f812e38520, `ActualizarTraspasoEgresoAPVAsociado` b3f326826f8439df.
- SHA-256 de las clases Java (prefijo 16): C c2efc7bcc9764dee, X c47b543173a4a0ea, L 24fa99a5d27b51df, AF 1c3ffc84174079e0, A ce0bc5391eeb851d, J 5a4a6ecc415c6a8d.
- No hay SQL en linea en estas clases (todo viene de archivos). Los 4 SQL del plan de F2 en linea no aplican aqui.
- Nota de `SQL.getSqlParams` (`SQL.java:502-527`): una lista vacia se expande a `IN (NULL)`; solo se expanden valores `instanceof ArrayList`; el reemplazo es `String.replace(":"+clave)`, por lo que una clave que sea prefijo de otra (`:IdSolicitudAporteRescate` contra `:IdSolicitudAporteRescateRelacionado`) colisionaria; en estos SQL no hay ese caso (verificado por lectura).

## 5. Objetos HANA que tocan (solo lo que dicen los SQL; sin catalogo nuevo, sin consultas)

| Objeto | Quien | Uso |
|---|---|---|
| vista `FMM.ConsultarSolicitudAporteRescate` (`BSCL.FondoMutuo.BD.Vistas`) | C (SelectSolicitudes), X, L, A | lectura; sus columnas (`EsFondoNacional`, `MoneyMarket`, `IdPersonaAGF`, `IdMedioDePagoCargoAGF`...) NO estan catalogadas aqui |
| `FMM.SolicitudAporteRescate` | C (`sr` relacionada, `solRelNN`, UPDATE `Estado='CRT'` + 21 columnas, UPDATE `MontoMonedaSistema`), L (UPDATE `EstadoLiquidacion='LIQ'`, `FolioCartaLiquidacion`, `FechaLiquidacionRegistro`), A (UPDATE `Estado`, `EstadoLiquidacion='ING'`, `FolioCartaLiquidacion=0`, `FechaAnulacion*`), AF (SELECT por cliente y folio) | escritura principal |
| `FMM.ReinversionSerieFondoDestino`, `FMM.SerieDeFondo`, `FMM.Fondo`, `PLA.Configuracion` (`PLA.MonedaAdicional`) | C (reinversion) | lectura |
| `FMM.TipoMovimientoFondoMutuo` (`GeneraCargoAbono`, `GeneraOmnibus`), `FMM.RegimenFondoMutuo` (`CertificaSinAsociado`), `FMM.Comision` (`TipoComision='PER'`), `MER.Emisor`, `PLA.Estado` | C | lectura |
| `APV TRA.OrdenTraspaso`, `TRA.Traspaso`, `SCS.EntidadRecaudadora`, `FMM TRA.TraspasoDetalleOrigen`/`TRA.Traspaso`, `SocioNegocio PER.ConsultarPersona`/`PER.Persona`/`PER.DocumentoIdentidad`/`PER.CuentaBancaria`, `BAN.TipoCuentaBancaria` | C (traspasos, TGR, SII) | lectura |
| vista Tesoreria `ConsultarPorComprobanteOrigenLiquidado`; `TES.IngresoEgreso` | C (comprobantes no liquidados), A (carta, ingreso/egreso) | lectura |
| `COM.Cobro` (`IdCobro='IVA'`) | C | lectura (valor del IVA) |
| `IND.Indicador` + `IND.TipoIndicador` (`GEN_OCSNEG`), vista `FMM.ConsultarIndicadoresPortafolioFondoMutuo` (`OperaLineaSobregiro`) | C | lectura |
| `FMM.DetalleCargaSolicitudesAporteRescate` | X | lectura (carga) |
| `FMM.LogCertificacionAporteRescate` + secuencia `Secuencias::LogCertificacionAporteRescate` | C (via `AgregarLogCertificacionAporteRescate`) | INSERT por solicitud (22 columnas, incl. `Simulacion`) |
| `CUS.Cartera` | C (via `CalculaCustodiaRescate`, `ProcesarCustodiaFondoMutuo`), A | lectura/escritura via utilidades de custodia |
| `PAG.MedioDePagoYModalidadDePago`, `APV TRA.ConfiguracionRetencionTraspaso` (`IdEstado='VIG'`), secuencias `Secuencias::FolioCartaLiquidacionAporteRescate` y `SecuenciaComprobanteLiquidacion` | L | lectura / `NEXTVAL` |
| `Mesa SPT.OperacionSpot` | A (lectura), C (escritura via Mesa) | |
| `APV TRA.Traspaso` | AF (UPDATE `IdEstado` por `IdEmpresa`+`IdTraspaso`) | escritura |

Brecha para el esquema H2 del oraculo: tipos/escala de las ~40 columnas de `FMM.SolicitudAporteRescate` que usa el UPDATE (`ParidadFechaCertificacion`, `Spread`, `RentabilidadReal`, `CostoUF`), definicion de la vista `FMM.ConsultarSolicitudAporteRescate`, tablas `TES.IngresoEgreso`, `CUS.Cartera` y las secuencias de Liquidacion (ciclo, inicio). Todo lo que ya fue catalogado en F1/F2 se reutiliza (`hana_qa_results`), no se reconsulto.

## 6. Colaboradores, compuertas y clases del modulo

### 6.1 Colaboradores externos de Certificar (P = en proceso y portado en el modulo; K = kit de lectura; H = HTTP al router; L = libreria en proceso (misma conexion); X = compuerta cerrada)

| Colaborador | Dueno | Modo | Cita en C | Nota |
|---|---|---|---|---|
| `RevisarRequisitosBasicos`, `ConfiguracionSistema`, `MensajeSalida`, `MonedaFormato`, `Fecha`, `Utils`, `SQL` | Plataforma / BcsUtils | K | 375-384, 403-411, 1436-1455 | `ConfiguracionSistema` mantiene un `HashMap` estatico (D-NOR-10) |
| `CalcularMontoNumeroCuota` | FondoMutuo (cierre F1) | P (FM-CUO-*) | 1058-1092 | |
| `CalcularComisiones` | FondoMutuo (F1 `comisiones`) | P (FM-COM-*) | 1645-1682 | solo si `CALCULAR_COMISION_CERTIFICACION="S"`; aqui `EsTraspaso` si va (D-COM-1 aplica a Normal) |
| `CrearSolicitudAporteReinversion` | FondoMutuo | P (FM-REI-41..68) | 2343-2557, 762 | canal fijo `AGE`; `p_SimularOperacion="N"`; trae G1/G2/G3/G5 propios |
| `ProcesarIngresoEgresoTesoreria` -> `CrearIngresoEgreso` | FondoMutuo -> Tesoreria | P -> L (FM-TES-*) | 1953-2054, 698-721 | G1; el contador de Tesoreria no se lee |
| `ProcesarIngresoOperacionSpot` -> `CrearOperacionSpot` | FondoMutuo -> Mesa | P -> H (FM-SPO-*) | 2057-2157, 790-830 | G2 |
| `CrearCargoAbono` | CuentaInversion | L (misma conexion) | 1741-1935, 2593-2636 | no es compuerta; sus errores suben el contador |
| `CrearMovimientoComprobante`, `CrearComprobante` | Facturacion | H (G6 propuesta) | 2159-2341 | compuerta cerrada segun D-F3-3 |
| `ResumenPatrimonial` (Normativa) | Normativa | **no invocado** | 67 (import) | ver D-F3B-15; no hay compuerta que cerrar |
| `CalcularParidad` | Plataforma (mer) | H (lectura) | 1524-1578, 2939-3019 | via UTM y UF |
| `SaldoFlujoCaja` | Plataforma | H (lectura) G3 | 1149-1303 | siempre que haya aporte `MueveCta="S"`, origen manual, sin sobregiro |
| `EntregarPreciosTasas` | Plataforma (tas) | H (lectura) | 2903-2937 | valor cuota cargado |
| `AgregarLog` (log de sistema) | Plataforma | H (escritura) G5 | 2837-2871 | insertar en la misma conexion en legacy |
| `NotificarEstadoOrdenFondoMutuo.logicaAsync` -> `Utils.CallBatchAsync` | FondoMutuo / BcsUtils | H asincrono (G8 propuesta) | 2752-2762 | llamada HTTP al servidor de lotes (`PLA.CallBatchURL`, `PLA.CallBatchToken`; valores no leidos) |
| `NotificarEstadoOrdenesOWP.go` | Plataforma | H (G8) | 2777-2818 | solo canal `OWP` |
| `ProcesarCustodiaFondoMutuo` | FondoMutuo | P (custodia) | 1684-1739 | usa `IngresoCustodia`/`RetiroCustodia` (cl.bcs.custodia.utils); tema ya cubierto por la linea de custodia |
| `CalculaCustodiaRescate`, `AgregarLogCertificacionAporteRescate` | FondoMutuo | P | 1305-1321, 1613-1643 | una linea cada una, abajo |

Gates ya numerados en F1/F2: G1 (Tesoreria medio distinto de `CIN`), G2 (Spot), G3 (`SaldoFlujoCaja`), G5 (`AgregarLog`/`InscribirProcesoBatch`). Propuesta de numeracion (la fija el plan): **G6** Facturacion (`CrearMovimientoComprobante`, `CrearComprobante`), **G8** notificaciones (`CallBatchAsync` y OWP). No hay G7 porque Normativa no se llama.

### 6.2 Clases del modulo que Certificar usa (una linea cada una; no se extraen aqui)
- `CalculaCustodiaRescate` (542 LOC): agrupa `CUS.Cartera` vigente (`INGCantidad`) por portafolio/instrumento/regimen y devuelve por solicitud `Estado` `APD` (alcanza), `ADV` (insuficiente: reduce `NumeroCuotas` al disponible y recalcula montos con `CalcularMontoNumeroCuota`) o `RZD` (sin saldo) mas `CantidadDisponible`; descuenta lo ya asignado dentro del lote (`CalculaCustodiaRescate.java:445`).
- `AgregarLogCertificacionAporteRescate` (104): inserta una fila por solicitud en `FMM.LogCertificacionAporteRescate` (`sql.batch`), sin validar nada.
- `ProcesarCustodiaFondoMutuo` (534): traduce `MovimientoCustodiaFondoMutuo` a ingreso (aporte) o retiro (rescate) de cartera y devuelve movimientos (`IdCustodia`, `MontoCompraFactorOriginal`, `CostoInversionEnFactorAjusteARetirar`, `MontoCompraFactorARetirar`, `Cantidad`, `FechaOrigenCompra`).
- `CalcularMontoNumeroCuota` (FM-CUO-01..10), `CalcularComisiones` (FM-COM-*), `CrearSolicitudAporteReinversion` (FM-REI-41..68), `ProcesarIngresoEgresoTesoreria` (FM-TES-01..13), `ProcesarIngresoOperacionSpot` (FM-SPO-01..13): ya extraidas.
- `NotificarEstadoOrdenFondoMutuo` (217): `logicaAsync` serializa la entrada y la envia a `PLUGIN_FMM.jar@cl.bcs.fondomutuo.NotificarEstadoOrdenFondoMutuo` del servidor de lotes; `logica` (en el lote) arma eventos `CERTIFICACION_FFMM` y los agrega con `AgregarEvento`.
- `pojo/FormaPagoSpot` (el "FormaPago" que Certificar importa): `IdInternoPagoOperacion`, `IdComprobantePagoOperacion`, `IdTipoMovimiento`, `IdFormaDePago`, `IdMoneda`, `IdMonedaLiquidacion`, `IdOperacion`, `RecibePagaPortafolio` ("R"/"P"), `MontoFormaDePago`, `AfectarTesoreria`, `FechaLiquidacion`; solo datos.
- `pojo/ResultadoCertificacion` (9 campos de salida), `pojo/SolicitudAporteRescate`, `pojo/ConstantesFondoMutuo`: datos; las constantes que mandan estan citadas inline.
- Pojos anidados de Certificar que otras clases importan: `Cobro`, `Persona`, `Estado`, `InSolicitudes`, `SolicitudesProcesoCertificacion` (acoplamiento inverso, igual que D-F2-5).

## 7. Transaccion, commit y fallo parcial

**Modelo**: una sola conexion JDBC (`serviceConn`) para todo el `/call` (`CallServlet.java:~409-430`). Las clases no hacen `commit` ni `rollback` (solo los `main()`). El anfitrion hace `rollback` si `P_OUT_ERRORESCONTADOR` falta o es > 0 y `commit` si es 0 (`CallServlet.java:432-437` y `:495`); escribe la respuesta **antes** del `commit` (D-MAN-8). No hay procedimientos autonomos en estas clases (a diferencia del bloqueo de reinversion). `Certificar` no tiene `try/catch` en `logica`: cualquier excepcion sube al anfitrion y revierte todo lo escrito en la conexion (pero no lo ya enviado por HTTP: notificaciones, log por router, Spot si es por HTTP).

**Linea de tiempo de Certificar (que se escribe en que orden)**:

| # | Paso | Escribe | Cita |
|---|---|---|---|
| 0 | parametros, lecturas, validaciones | nada | `C:375-531` |
| 0b | salida temprana (0 solicitudes validas) | solo `FMM.LogCertificacionAporteRescate`, luego contador=1 -> el anfitrion revierte (el log se pierde; los motivos solo viven en la respuesta) | `C:532-537` |
| 1 | aportes: custodia (`ProcesarCustodiaFondoMutuo`, ingreso), luego cuenta de inversion (`CrearCargoAbono`) | `CUS.Cartera`, movimientos de cuenta | `C:600-640` |
| 2 | rescates: custodia (2 llamadas: con y sin regimen), cuenta de inversion, Tesoreria, movimiento de facturacion, comprobante, aporte de reinversion (incluye INSERT `APOREINV`, y dentro de AR: Tesoreria/Spot), Spot | `CUS.Cartera`, cuenta, `TES.*`, `FAC.*`, `FMM.SolicitudAporteRescate` (APOREINV), `MES.*` | `C:644-833` |
| 3 | UPDATE masivo `Estado='CRT'` (PEN o ADV) | `FMM.SolicitudAporteRescate` | `C:835` |
| 4 | notificacion asincrona por HTTP | **externo, no revierte**; el lote lee las solicitudes con otra conexion y puede ver el estado previo (carrera) | `C:836` |
| 5 | recursion `Switch` (misma conexion; todo lo de 1-3 otra vez para los aportes de switch) | idem | `C:838-843` |
| 6 | `UPDATE MontoMonedaSistema` de rescates de reinversion internacional sin Spot | `FMM.SolicitudAporteRescate` | `C:845` |
| 7 | OWP por HTTP | **externo** | `C:848-850` |
| 8 | log de sistema (`AgregarLog`) y log de certificacion | `PLA` log, `FMM.LogCertificacionAporteRescate` | `C:853-860` |
| 9 | si `p_SimularOperacion="S"`: contador=1 | el anfitrion revierte 1-3, 5, 6, 8 (pero 4 y 7 ya salieron) | `C:862-864` |

**Que queda confirmado si hay solo rechazos "blandos"** (ninguna excepcion; el contador solo sube por colaboradores que lo informan): custodia, cuenta de inversion, facturacion, reinversion y Spot si suman contador; Tesoreria nunca; los rechazos por validacion (fase 2, FM-CER-06..13) tampoco suben el contador si al menos una solicitud sobrevive. Resultado: un lote mixto hace `commit` y deja filas `RZD` solo en la respuesta (no se persisten; `actualizarSolicitudes` solo toca PEN/ADV). Los rechazos hechos *despues* de que un colaborador ya escribio (Tesoreria, Spot, rechazos tardios de comprobante) dejan efectos huerfanos (D-F3B-03).

**Liquidacion (`L`)**: misma conexion; en cuanto `CrearIngresoEgreso` informa errores hace `return` con **sus** mensajes y contador (`L:558-564`), sin tocar lo ya escrito por Tesoreria; el anfitrion revierte por contador > 0. El UPDATE de solicitudes (`LIQ`) se hace por cada ingreso generado dentro del bucle (`L:575`), antes de los cargos/abonos de cuenta de inversion (`L:591-688`), asi que un fallo de cuenta de inversion deja `LIQ` ya escrito hasta que el anfitrion revierte.

**Anulacion (`A`)**: orden: cuenta de inversion -> custodia (retiro, ingreso) -> Spot (`AnularOperacionSpot`) -> UPDATE -> AGF -> log -> OWP; los rechazos tardios (custodia, Spot) marcan `RZD` y suman al contador del colaborador. `AF` agrega el UPDATE de `TRA.Traspaso` solo si el contador final es 0 (`AF:169-194`).

## 8. Tarjetas

Vocabulario: `sol` = `SolicitudesProcesoCertificacion` (fila de `SelectSolicitudes`); "PEN/ADV" = `EstadoProceso` pendiente o advertencia; `RZD` = rechazada; `APD` = aprobada; `CRT` = estado de solicitud certificado; `_c.SI/NO` = "S"/"N". Salvo que se diga otra cosa, cada regla corre dentro del `logica` de `C` y rechaza con `EstadoProceso="RZD"` y un `Codigo` que despues se traduce a descripcion con `MensajeSalida` (`C:1383-1393`).

### 8.1 FM-CER-01..73: `CertificarSolicitudAporteRescate` (C), por fase

#### Fase 0: contrato, requisitos, parametros y lecturas

**FM-CER-01 Contrato JSON.** Given la entrada `{p_IdEmpresa,p_IdUsuario,p_IdModulo,p_SimularOperacion,p_UsaReglasNegocio,p_FechaCertificacion (texto yyyy-MM-dd),p_IdTipoCanalIngreso,p_NotificaOWP (def "S"),p_CertificacionAporteSwitchSimulado (def "N"),p_Solicitudes:[{IdSolicitudAporteRescate,IdTipoCanalIngreso (def "")}]}`. When entra por `call`. Then se decodifica con `new Gson()` plano, se ejecuta `logica` y se serializa `{P_OUT_ERRORESCONTADOR (0), P_OUT_MENSAJES, P_OUT_RESULTADO:[{IdSolicitudAporteRescate,IdSolicitudAporteRescateRelacionado,IdSerieFondo,Codigo,Estado,NombreEstado,DescripcionCodigo,DocumentoIdentidad,CodigoPortafolio}]}`. `p_IdTipoCanalIngreso` y `InSolicitudes.IdTipoCanalIngreso` no influyen en ninguna regla (solo se copia el primero a la entrada del hijo `Switch`); el campo `p_Filtros` de los JSON de prueba no existe en la entrada y Gson lo ignora. Fuente `C:114-152,360-368`. E/S: lo listado. Defectos D-NOR-9 (ids `Long` sin default). Confianza Alta.

**FM-CER-02 Requisitos basicos, lista vacia, ids repetidos.** Given empresa 1, usuario 3, modulo `FMM`. When `PLA.PaisSistema` se lee primero (obligatorio: si falta `getParametroConfiguracionLong` lanza), luego `RevisarRequisitosBasicos` trae un `ERROR` -> contador=1, mensajes de plataforma, return (FM-NOR-01). Given `p_Solicitudes=[]` -> contador=1, `NO_SE_INGRESARON_SOLICITUDES` (id 0), return. Given `[{1001},{1001}]` -> contador=1, `SOLICITUD_DUPLICADA` (id 0), return; dos `null` tambien cuentan como duplicado. Fuente `C:375-401`. Defectos D-REQ-11 (`getNumeroErrores` compara por referencia). Depende F1: kit. Confianza Alta.

**FM-CER-03 Parametros de empresa.** Given la empresa. When lee: obligatorios (lanzan `Exception("Parametro ... no encontrado")` si faltan) `PLATAFORMA/PLA.FechaSistema`, `FONDOMUTUO/TipoComprobanteFacturaNoApv`, `TipoComprobanteFacturaApv`, `CALCULAR_COMISION_CERTIFICACION`, `VALIDAR_CERTIFICACION`, `RESCATE_GENERA_VENTA_SPOT_DOLAR`, `RESCATE_COMISION_COMPROBANTE_AUTOMATICO`, `PLATAFORMA/PLA.NotificarEstadoOrdenOWP`; opcionales con valor por defecto: `APV.PorcentajeRetencion` (0 y `logger.error`), `APV.DocumentoIdentidadPersonaTGR` y `...SII` (`""` y `logger.warn`); perezoso (solo si llega un rescate APV-B/APV-AN a Tesoreria): `FONDOMUTUO/CONCEPTO_RETENCION_PAGO_SII` (obligatorio, se lee dentro del bucle). Then los usa en las fases siguientes. Fuente `C:870-917,978-1030,2708-2736`. Parametros: ver seccion 10b. Defectos D-NOR-10 (estado estatico de `ConfiguracionSistema`). Confianza Alta.

**FM-CER-04 Lecturas de la unidad.** Given los ids distintos de la entrada (`distinct()`, orden estable). When lee: `SelectSolicitudes` (por ids y empresa, `ORDER BY TipoSolicitud`), `SelectEstados`, comprobantes no liquidados, traspasos APV y no APV por ids de solicitud mas ids relacionados distintos de 0 (`HashMap.putAll`: el no APV **pisa** al APV con el mismo id), persona TGR y persona SII (`get(0)` sin `ORDER BY`), `List2Hash` de estados. Then si `SelectSolicitudes` devuelve 0 filas -> contador=1, `UI_NO_EXISTEN_SOLICITUDES_CUMPLAN_FILTROS`, return; los ids no encontrados se ignoran sin mensaje. Fuente `C:413-433,926-1030`; SQL `CertificarSolicitudAporteRescate_*`. Defectos D-NOR-9 (`toMap` paralelo lanza `IllegalStateException` ante id repetido), D-F3B-12 (el SELECT puede duplicar filas). Confianza Alta.

**FM-CER-05 Valor cuota cargado.** Given cada solicitud. When `EntregarPreciosTasas` con `FechaConsulta=FechaValorCuota`, `IdClasificacionTipoInstrumento="FM"`, `IdInstrumento`, `IdMoneda=IdMonedaFondo`, `IdRegistro=IdSolicitudAporteRescate`, `IdTipoCriterioTasa="0"`, `IdUso="CARTERA"`, `Nemotecnico=IdSerieFondo`, `PlazoResidual=0`, `TipoCartera="0"`, pais = `PLA.PaisSistema`. Then se arma un mapa por `IdRegistro` descartando precios sin `IdInstrumento` (`Collectors.toMap`: duplicados lanzan). Fuente `C:437,2903-2937`. Confianza Alta.

#### Fase 1: validacion por solicitud (orden fijo; la primera regla que falla rechaza y salta a la siguiente solicitud)

**FM-CER-06 Estado.** Given sol con `EstadoSolicitud` distinto de `ING` y de `ACP`. When valida. Then `RZD` con `UI_ESTADO_INCORRECTO_CERTIFICACION`. Ej: estado `CRT` -> rechazada. Fuente `C:445-449`. Confianza Alta.

**FM-CER-07 Fecha de certificacion.** Given `p_FechaCertificacion="2026-03-10"` y sol con `FechaCertificacion` 2026-03-11. When compara `Fecha.stringToSqlDate(FechaCertificacion)` (zona de la JVM, D-FEC-6) con el texto de entrada. Then `RZD` `UI_FECHA_INVALIDA_CERTIFICACION_FONDO_MUTUO`; con `p_FechaCertificacion` nulo todas fallan aqui. Fuente `C:451-455`. Defectos D-FEC-6. Confianza Alta.

**FM-CER-08 Valor cuota cargado.** Given sol sin precio en `EntregarPreciosTasas`, o con `FechaFuente` nula, o con `FechaFuente` != `FechaValorCuota` (`equalsIgnoreCase` de textos yyyy-MM-dd). When valida. Then `RZD` `UI_NO_EXISTE_VALOR_CUOTA` (las tres ramas, mismo codigo). Fuente `C:457-478`. Confianza Alta.

**FM-CER-09 Omnibus no creado.** Given `GeneraOmnibus="S"` (tipo de movimiento), `IdOmnibus` nulo y NO (`Switch="S"` y `APOREINV`). When valida. Then `RZD` `UI_ONMIBUS_NO_CREADO` (nombre con la errata del legacy). `IdOmnibus` por defecto del pojo es 0, asi que solo aplica si la vista devuelve nulo y el mapeo lo conserva (ver Q-W0B-9). Fuente `C:480-484`. Confianza Media. Pregunta: ¿la vista entrega `IdOmnibus` nulo o 0 para solicitudes sin omnibus?

**FM-CER-10 Comprobante de Tesoreria no liquidado.** Given una fila en `ConsultarPorComprobanteOrigenLiquidado` con `IdComprobanteOrigen = IdSolicitudAporteRescate`, tipo de origen `FMM` y `IdEstado != 'LIQ'`. When valida. Then `RZD` `UI_DOCUMENTO_ASOCIADO_NO_LIQUIDADO` y `IdComprobanteTesoreria` = ese comprobante (se usa despues como parametro del mensaje). Fuente `C:486-498`; SQL `SelectComprobantesNoLiquidados`. Confianza Alta.

**FM-CER-11 Liquidacion previa del aporte (VALIDAR_CERTIFICACION).** Given `Switch != "S"`, parametro `VALIDAR_CERTIFICACION="S"`, `TipoSolicitud="APO"`, `EstadoLiquidacion != "LIQ"` y `FolioCartaLiquidacion == 0`. When valida. Then `RZD` `UI_VALIDACION_LIQUIDACION_FONDO_MUTUO`. `FolioCartaLiquidacion` nulo -> NPE. Fuente `C:499-504`. Experto: si (QD-F3B-01). Confianza Alta.

**FM-CER-12 Asociada no certificada.** Given `Switch != "S"`, `CertificaSinAsociado="N"` (del regimen; si es nulo no se valida), `IdSolicitudAporteRescateRelacionado != 0`, `IdSolicitudAporteRescate > Relacionado` y (`EstadoRelacionado != "CRT"` o `EstadoLiquidacionRelacionado != "LIQ"`). When valida. Then `RZD` `UI_SOLICITUD_ASOCIADA_NO_CERTIFICADA`. Ej: aporte 2002 de reinversion con rescate 2001 en `CRT` y liquidacion `ING` -> rechazado (el aporte de reinversion solo se certifica cuando el rescate esta certificado y liquidado). Fuente `C:506-514`. Experto: si (QD-F3B-01). Confianza Alta.

**FM-CER-13 TGR y SII no configuradas.** Given `personaTGR` nula e `IdRegimenTributarioAPV="BF"` -> `RZD` `UI_NO_EXISTE_TGR_CONFIGURADA`; `personaSII` nula e `IdRegimenTributarioAPV="APV-B"` -> `RZD` `UI_NO_EXISTE_SII_CONFIGURADA`. (`IdRegimenTributarioAPV` = regimen de la solicitud si el proposito es `PROPOSITOAPV`, o el codigo de portafolio si el regimen viene vacio, o `""`.) **`APV-AN` no se valida contra SII** aunque Tesoreria la exige despues (NPE, D-F3B-08). Fuente `C:516-526`; SQL `SelectSolicitudes` (CASE de `IdRegimenTributarioAPV`). Defectos D-F3B-08. Experto: si. Confianza Alta.

**FM-CER-14 Conteo y salida temprana.** Given que ninguna solicitud paso las reglas 06..13. When `solicitudesPorProcesar == 0`. Then contador=1, `P_OUT_RESULTADO` = salida de todas las solicitudes (codigo y descripcion), `insertarLog` (INSERT en `FMM.LogCertificacionAporteRescate`) y return; el anfitrion revierte y el log se pierde. Si al menos una pasa, las rechazadas solo viven en la respuesta (FM-CER-69). Fuente `C:528-537`. Defectos D-F3B-16. Confianza Alta.

#### Fase 2: recalculo de montos

**FM-CER-15 Indicadores de portafolio.** Given las solicitudes PEN/ADV. When lee `OperaLineaSobregiro` por portafolio distinto y arma `List2Hash` por `IdPortafolio`. Then se usa solo en FM-CER-21/22 (ausente = "N"). Fuente `C:540,1032-1042`. Defectos D-NOR-9 (la vista duplica portafolio). Confianza Alta.

**FM-CER-16 Recalculo de cuotas y monto.** Given cada solicitud PEN/ADV. When arma `CalcularMontoNumeroCuota.Serie{IdInterno=IdSolicitud, Fecha=FechaValorCuota, IdSerieFondo, CalculoPorMontoCuota (el de la solicitud), Monto=CostoMonedaLocal, NumeroCuotas, IdMonedaFondo, IdMonedaInversion=IdMoneda, TipoSolicitud, TipoFactorAjuste="UF", IdTipoMovimientoFondoMutuo ("" si nulo), IdEmpresa}` y llama `CalcularMontoNumeroCuota.logica` (FM-CUO-01..10); sin series no llama. Then si el resultado falta o `Resultado != "OK"` -> `RZD` `ERROR_CALCULO_MONTO_CUOTA`. Fuente `C:543,1058-1092,1094-1116`. Defectos D-CUO-1..3, D-NOR-19. Experto: si (QD-03/04). Confianza Alta.

**FM-CER-17 Aplicacion del recalculo.** Given resultado `OK`. When `TieneComisionNNRelacionada == "N"` (la solicitud no tiene un `RESCNN` hijo). Then `CostoMonedaLocal=MontoDestino; Monto; ValorCuota; NumeroCuotas; ParidadFechaCertificacion=ValorParidad; IdMonedaSistema`; `CostoUF` y `ValorUFFechaInversion` solo cambian si el movimiento NO es `APOREINV` (en ese caso se conservan); `MontoMonedaSistema = r.MontoMonedaSistema`, salvo aporte con `IdOperacionSpot != 0`, que conserva el del Spot; `MontoPago = r.Monto; MontoPagoLocal = r.MontoDestino`. Con `TieneComisionNNRelacionada = "S"` **no se actualiza ninguno de estos campos** (se conservan los de la BD). Ej: rescate 1001 `RESNORM`, `CostoMonedaLocal` 1.000.000 CLP, resultado `Monto` 1.000.000, `NumeroCuotas` 500,0000 -> `MontoPago`=`MontoPagoLocal`=1.000.000. Fuente `C:1094-1116`. Defectos D-F3B-12. Experto: si (comision NN). Confianza Alta.

**FM-CER-18 Fecha efectiva de certificacion.** Given `fechaSistema=2026-03-10` y `p_FechaCertificacion=2026-03-09`. When toma la mayor. Then 2026-03-10. Solo la usa la consulta de saldo (FM-CER-21/22). Fuente `C:546,1118-1123`. Defectos D-FEC-6. Confianza Alta.

**FM-CER-19 Particion y orden.** Given las solicitudes PEN/ADV. When separa por `TipoSolicitud` (`APO` -> aportes, `RES` -> rescates) y ordena cada lista ascendente por `IdSolicitudAporteRescate` (comparador `-1`/`0`/`1`, `parallelStream().sorted`). Then el orden fija el correlativo `NumeroCargoAbono` y el orden de las llamadas. Una solicitud con otro `TipoSolicitud` no entra en ninguna lista pero igual se marca exitosa al final (D-F3B-16). Fuente `C:549-550,1125-1147`. Confianza Alta.

**FM-CER-20 Parametro "opera con saldo negativo" (CELLINCECT-763).** Given los portafolios de los aportes. When consulta `IND.Indicador` con `GEN_OCSNEG` por `IdEntidad IN portafolios` (se ejecuta aunque no haya aportes: `IN (NULL)`) y para cada aporte toma el **primer** indicador de su portafolio. Then `OperaSaldoNegativo = ValorIndicador` (por defecto "N"). Fuente `C:552-572`; SQL `..._SelectParametroOperaConSaldoNegativo`. Experto: si (QD-F3B-02). Confianza Alta.

#### Fase 3: aportes

**FM-CER-21 Saldo de cuenta de inversion (canal distinto de PAC).** Given aportes no rechazados con `IdTipoCanalIngreso != "PAC"`, `MueveCta="S"`, `OrigenOperacion="M"` e `OperaLineaSobregiro="N"` (ausente = "N"). When acumula `portTotalesMontos["portafolio_moneda"] += CostoMonedaLocal` (DECIMAL128), arma `P_PORTAFOLIOACONSULTAR` agregando el par solo si el portafolio no esta en `portFlujo` **o** la moneda no esta en `monedaFlujo` (listas independientes), y llama `SaldoFlujoCaja` (`P_USO="CERTIFICACION_APORTE_FMM"`, `FECHADESDE=FECHAHASTA` = fecha efectiva, `DESPLEGARENUI="N"`, `AGRUPARPORORIGEN="N"`) solo si hay pares. Then para cada aporte compara `total` con `saldo` = **ultimo** registro del flujo con portafolio, moneda y `FechaSaldo` iguales a `fecha` (`fecha` = `fechaSistema` si `FechaCertificacion` < fechaSistema, si no la `FechaCertificacion` en texto); si `OperaSaldoNegativo` es "N" (`equalsIgnoreCase`) y `total > saldo` -> `RZD` `UI_SALDO_INSUFICIENTE_FLUJO_CAJA`. Ej: portafolio 77 CLP, saldo 500.000, aportes 1001 por 300.000 y 1002 por 300.000, mismo portafolio y moneda -> total 600.000 > 500.000 y **ambos** se rechazan (no es progresivo). Un par omitido por la regla de listas independientes queda con saldo 0 y se rechaza cualquier monto > 0. Fuente `C:575-577,1149-1225`. Defectos D-NOR-16, D-F3B-11. Gate G3. Experto: si (QD-F3B-02). Confianza Alta.

**FM-CER-22 Saldo de cuenta de inversion (canal PAC).** Given aportes con `IdTipoCanalIngreso="PAC"`. When misma regla que FM-CER-21 pero `P_USO="CERTIFICACION_APORTE_FMM_PAC"`; corre siempre despues de FM-CER-21 (segunda llamada a `SaldoFlujoCaja`, copia exacta del metodo). Then igual codigo de rechazo. Fuente `C:578,1227-1303`. Defectos D-NOR-16, D-F3B-11. Gate G3. Confianza Alta.

**FM-CER-23 Formato de montos.** Given aportes (despues del saldo) y rescates (despues de retencion y comision). When `MonedaFormato.formatear(MONTO)` sobre 11 campos: `Monto, MontoComision, MontoPago, MontoLiquido, MontoRetencion` con la moneda del fondo; `CostoMonedaLocal, MontoComisionLocal, MontoPagoLocal, MontoLiquidoLocal, MontoRetencionLocal` con la moneda de inversion; `MontoMonedaSistema` con `IdMonedaSistema`. Then se sobreescriben los campos (tambien los nulos; el comportamiento con nulo de `MonedaFormato` no se verifico). Fuente `C:579,595,1436-1455`. Depende F1: kit. Confianza Media. Pregunta: ¿`MonedaFormato.formatear(null)` devuelve 0 o nulo?

**FM-CER-24 Recalculo del costo UF de los aportes de reinversion (entrada).** Given aportes `APOREINV` (los demas se saltan). When `reCalcularCostoUFAportes`: lee los rescates origen (`SelectSolicitudes` por los `Relacionado` distintos); por cada aporte arma una entrada de `CalcularParidad` (`UF`->`CLP` a `FechaValorCuota` del aporte, `MontoOrigen=1`) reusando **el mismo objeto** y `IdInterno` creciente; busca el rescate origen con `findFirst().get()`. Then solo continua si el rescate tiene `ValorUFFechaInversion > 0` y `CostoUF > 0`; sin rescate origen -> `NoSuchElementException`. **Todo el cuerpo esta en `try/catch(Exception)` con `printStackTrace` y no relanza**: cualquier error deja los aportes sin cambio y la certificacion sigue. Fuente `C:600-601,2939-3019`. Defectos D-F3B-10, D-NOR-11. Confianza Alta.

**FM-CER-25 Valor UF del rescate origen.** Given aporte y rescate origen con monedas. When `IdMonedaSistema != IdMoneda` en ambos y `rescate.IdMoneda == aporte.IdMoneda` (reinversion de moneda extranjera sin Spot): `ValorUFRescate = (suma de los MontoMonedaSistema DISTINTOS de los aportes de reinversion no rechazados, sin Spot, con `Relacionado>0`) / rescate.CostoUF` (DECIMAL64); si no: `rescate.MontoMonedaSistema / rescate.CostoUF`. Then queda en una variable que usan las dos ramas de FM-CER-26. La suma usa `distinct()` de valores: dos aportes con igual monto se cuentan una vez. Fuente `C:2972-2994`. Defectos D-F3B-10. Experto: si (QD-F3B-03). Confianza Media. Pregunta: ¿el `distinct()` sobre montos es intencional?

**FM-CER-26 Herencia o recalculo del costo UF.** Given `aporte.IdRegimenTributario` (nota: AR usa `CodigoPortafolio`, aqui el regimen). When es `APV-A`, `BF`, `DC-DL`, `DC-AL` o `DC-R` (`equalsIgnoreCase`): `CostoUF = aporte.MontoMonedaSistema / ValorUFRescate` y `ValorUFFechaInversion = rescate.ValorUFFechaInversion`; si no y (`IndicadorMLT="S"` y aporte y rescate son regimen `18Q`): igual; en otro caso: `UFValorCuotaAporte` = `MontoLocal` de **cualquier** salida de `CalcularParidad` (`findAny().get()`); `CostoUF = MontoMonedaSistema / UF` si `UF != null && UF != BigDecimal.ZERO` (comparacion por referencia), si no `MontoMonedaSistema / ValorUFFechaInversion` propio; `ValorUFFechaInversion = UF`. Ej: `MontoMonedaSistema` 1.200.000, rescate con `MontoMonedaSistema` 1.200.000 y `CostoUF` 40 -> `ValorUFRescate`=30.000; aporte `BF` -> `CostoUF` = 1.200.000/30.000 = 40. Fuente `C:2996-3011`. Defectos D-F3B-10, D-F2-20 (analoga en AR), D-NOR-11. Experto: si (QD-F2-02). Confianza Media. Pregunta: ¿por que AR usa `CodigoPortafolio` y Certificar `IdRegimenTributario`?

**FM-CER-27 Custodia de los aportes (ingreso).** Given aportes no rechazados. When arma un movimiento por aporte `IdGenerarMovimientoCustodia=IdSolicitud`, `FechaOperacion=FechaComprobante=FechaLiquidacion=FechaCertificacion`, `IdTipoProducto="INFM"`, `IdTipoOperacion="APORFM"`, `IdTipoComprobante="FFMM"`, `IdBolsa="BCS"`, `IdBoveda="COR"`, `Cantidad=NumeroCuotas`, `ValorRescate=0`, `PrecioCompra=ValorCuota`, `MontoCompra=MontoMonedaSistema`, `MontoCompraMonedaInstrumento=Monto`, `TipoFactorAjuste="UF"`, `ValorFactorAjusteaFechaCurse=ValorUFFechaInversion`, `CostoEnFactorAjuste=CostoUF`, `IdTipoCanalIngreso=""`, `FechaOrigenCompra` = `FechaOriginalInversion` si es `APOREINV` y distinta de `0001-01-01`, si no `FechaValorCuota`; llama `ProcesarCustodiaFondoMutuo.logica` con `p_simularOperacion="N"` y `p_UsaReglasNegocio="S"` (siempre). Then suma el contador de custodia y por cada mensaje `ERROR` agrega mensaje (id absoluto) y rechaza el primer aporte con ese id con su codigo y descripcion. Fuente `C:600-618,1684-1739`. Defectos D-F3B-01. Depende F1: custodia. Experto: si. Confianza Alta.

**FM-CER-28 Cartera y rentabilidad real de los aportes.** Given aportes no rechazados que no son rescates. When busca el movimiento de custodia con `IdGenerarMovimientoCustodia = IdSolicitud` (`findFirst`). Then `IdCartera = IdCustodia` y `RentabilidadReal = MontoCompraFactorOriginal`. Fuente `C:620,1467-1478`. Confianza Alta.

**FM-CER-29 Cuenta de inversion de los aportes.** Given aporte no rechazado con `MueveCta="S"`, `OrigenOperacion="M"` y `GeneraCargoAbono="S"` del tipo de movimiento (consulta por solicitud, N+1). When arma por aporte: cargo `MOVCAR` en moneda del fondo por `MontoPago` (`IdConcepto = sol.IdConcepto`: `APORFM`, `APOREI`, `APODIV`, `APOOVE`, `APOPRO`, `APOTRS`, `APOCFI`; descripcion `APORTE_FONDO_MUTUO`; `IdTipoOperacion="APORFM"`; `FechaOperacion=FechaCertificacion`; `FechaCondicionDeLiquidacion = fechaSistema` si `fechaSistema > FechaLiquidacion` o `Switch="S"`, si no `FechaLiquidacion`; `FechaLiquidacionOperacion = fechaSistema` si `> FechaLiquidacion`, si no `FechaLiquidacion`; `CertificadoAutomatico="S"`); y si `IdMoneda != IdMonedaFondo` (diferido): cargo en moneda de inversion por `MontoPagoLocal` y abono en moneda del fondo por `MontoPago`, quedando el principal al final de la lista. Llama `CrearCargoAbono` con `p_simularOperacion="N"`, `p_EsIngresoManual="N"`. Ej: aporte 3001 fondo CLP, `MontoPago` 250.000 -> un cargo de 250.000; aporte diferido USD (`IdMoneda`=USD, fondo CLP) -> 3 lineas. `Switch` nulo -> NPE (`sol.Switch.equalsIgnoreCase`). Fuente `C:1752-1825,1915-1923,1938-1951`. Defectos D-F3B-01. Depende F1: cuenta de inversion. Experto: si. Confianza Alta.

**FM-CER-30 Errores de cuenta de inversion (aportes).** Given mensajes `ERROR` de `CrearCargoAbono` con `id` = correlativo de la linea. When mapea `id -> IdSolicitud` con `ids.get` (si no existe, `Math.abs(null)` lanza NPE). Then suma el contador de `CrearCargoAbono`, agrega el mensaje con el id de la solicitud y rechaza el primer aporte con ese id. Fuente `C:624-639,1926-1931`. Confianza Alta.

#### Fase 4: rescates

**FM-CER-31 Custodia disponible: rechazo.** Given `CalculaCustodiaRescate` para los rescates (`getPojoSolCus`). When el resultado es `RZD`, o es `ADV` y el rescate ya tiene omnibus procesado (`IdOmnibus > 0`), o no hay resultado. Then `RZD` `UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION` con parametros `DocumentoIdentidad`, `CodigoPortafolio`, `Nemotecnico`, `CantidadRetiro` y `CantidadCustodia`. **Si no hay resultado, `custodia.CantidadDisponible` lanza NPE** (D-F3B-05). Se ejecuta en `parallelStream` con la misma `Connection` para armar la descripcion (D-F3B-06). Fuente `C:584-585,1305-1367`. Defectos D-F3B-05/06. Confianza Alta.

**FM-CER-32 Custodia disponible: advertencia y ajuste.** Given resultado `ADV` (saldo insuficiente) sin omnibus procesado. When procesa. Then `EstadoProceso="ADV"`, `Codigo=UI_ERROR_FM_SALDO_CUSTODIA_INSUFICIENTE_CERTIFICACION` con `CantidadRetiroInicial`, `CantidadRetiroFinal`, `CantidadCustodia`, y los montos de la solicitud pasan a los de `CalculaCustodiaRescate`. Ej: rescate 1003 `NumeroCuotas` 120,0000 contra custodia 100,0000 -> `NumeroCuotas` 100,0000 y monto reducido proporcionalmente, estado `ADV`; la solicitud sigue el flujo y se certifica (`ADV` entra en el UPDATE), pero queda con estado `ADV` y su codigo en la respuesta. Fuente `C:1338-1357`. Experto: si (QD-F3B-04). Confianza Alta.

**FM-CER-33 Rescate total con omnibus por procesar.** Given `CalculoPorMontoCuota="T"` e `IdOmnibus` nulo o 0 (aunque el resultado no sea `ADV`). When procesa. Then los montos de la solicitud pasan a los de custodia (`CostoMonedaLocal, Monto, ValorCuota, NumeroCuotas, ParidadFechaCertificacion, CostoUF, ValorUFFechaInversion, MontoMonedaSistema, MontoPago, MontoPagoLocal`). Fuente `C:1346-1357,1376-1381`. Confianza Alta.

**FM-CER-34 Marca de venta Spot.** Given rescate no rechazado en esta fase. When `RESCATE_GENERA_VENTA_SPOT_DOLAR` es `S` (`equalsIgnoreCase`) y (tipo `RESCNN` o `IdMoneda` es `USD`, ignorando mayusculas) y `IdMonedaPago` es `CLP`. Then `GeneraSpot="S"`, si no `"N"`. Las solicitudes rechazadas aqui quedan con `GeneraSpot` nulo (FM-CER-49 pregunta por `"N"`). Fuente `C:1359-1363`. Parametros: moneda `USD`/`CLP` literales. Experto: si (QD-F3B-05). Confianza Alta.

**FM-CER-35 Rescate relacionado con traspaso.** Given un traspaso para la solicitud o para su relacionada. When consulta el mapa unido APV+no APV. Then `RelacionTraspaso="S"`. Fuente `C:587,1395-1403`. Confianza Alta.

**FM-CER-36 Monto de retencion APV.** Given rescate de tipo `RESNORM` (`_c.RescateFondoMutuo`), `IdRegimenTributarioAPV` `APV-AN` o `APV-B`, `IdMedioDePago != "CIN"`, `RelacionTraspaso != "S"`, `GeneraSpot != "S"` y `APV.PorcentajeRetencion != 0`. When calcula **antes** de la comision. Then `TieneMontoRetencion="S"`, `MontoRetencion = Monto x P / 100` y `MontoRetencionLocal = CostoMonedaLocal x P / 100` (`divide` exacto, sin `MathContext`). Ej: `Monto`=`CostoMonedaLocal`=1.000.000 y `P`=15 -> 150.000. Fuente `C:588,1405-1420`. Defectos D-F3B-09. Experto: si (QD-F3B-06). Confianza Alta.

**FM-CER-37 Comision de certificacion.** Given `CALCULAR_COMISION_CERTIFICACION="S"`. When llama `CalcularComisiones` con `p_SimularOperacion="N"`, `p_IdModulo`, datos de custodia por rescate no rechazado (`IdInterno`, serie, instrumento, cliente, portafolio, `Cantidad=NumeroCuotas`, monedas, `TipoSolicitud`, movimiento, `FechaValorCuota`, `FechaCurse=FechaAceptacion`, `EsTraspaso=RelacionTraspaso`). Then por cada rescate con comision y `TieneComisionNNRelacionada="N"`: `MontoComision`, `MontoComisionLocal`, `MontoPagoLocal = CostoMonedaLocal - MontoComisionLocal`, `MontoPago = Monto - MontoComision` (DECIMAL128). Con el parametro distinto de `S` no se calcula comision (los campos quedan nulos hasta `formatearMontos`). Ej: `CostoMonedaLocal` 1.000.000, comision local 11.900 -> `MontoPagoLocal` 988.100. Fuente `C:591-595,1422-1434,1645-1682`. Defectos D-COM-1..6 (heredados). Experto: si. Confianza Alta.

**FM-CER-38 Retencion valida.** Given `TieneMontoRetencion="S"` y `MontoRetencion == 0` (`compareTo`) despues del formato. When corre. Then `TieneMontoRetencion="N"`. Fuente `C:596,1457-1465`. Confianza Alta.

**FM-CER-39 Custodia de los rescates (retiro) en dos llamadas.** Given rescates no rechazados. When separa por `IdRegimenTributario` no vacio / vacio (nulo -> NPE) y llama `ProcesarCustodiaFondoMutuo` dos veces (con regimen, sin regimen), cada una con los movimientos del rescate (`IdTipoOperacion="RESCFM"`, mismos campos que FM-CER-27, `p_simularOperacion="N"`). Then suma ambos contadores; une los mensajes `ERROR` y rechaza el primer rescate con ese id (`Math.abs`). Fuente `C:644-669,1684-1739`. Defectos D-F3B-01. Depende F1: custodia. Confianza Alta.

**FM-CER-40 Rescate sin cartera retirada.** Given rescate no rechazado sin ningun movimiento de custodia con `IdGenerarMovimientoCustodia = IdSolicitud`. When valida. Then `RZD` `UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO`. Fuente `C:671-675,1512-1522`. Confianza Alta.

**FM-CER-41 Costo UF, rentabilidad real y spread del rescate.** Given los movimientos retirados por solicitud y la paridad `UTM`->`CLP` por fecha de certificacion distinta (`IdTrabajo=1`, `IdInterno` 1..n; reusa **un solo objeto** de entrada, ver D-F3B-11). When para **cada** rescate (rechazado o no) suma `CostoInversionEnFactorAjusteARetirar` y `MontoCompraFactorARetirar` de sus movimientos; `valorUTM` = `MontoDestino` si el calculo es `OK`, si no 1. Then `RentabilidadReal = MontoMonedaSistema / valorUTM - sumaMontoCompraFactor`, `Spread = CostoUF(previo) - sumaCostoUF` y `CostoUF = sumaCostoUF` (DECIMAL128). Ej: movimientos con costo UF 10,5 y 4,5 y monto de compra 6,0 y 3,5; `CostoUF` previo 16; `MontoMonedaSistema` 600.000 y UTM 60.000 -> `CostoUF`=15,0; `Spread`=1,0; `RentabilidadReal`=10-9,5=0,5. `CostoUF` previo nulo -> NPE. Fuente `C:676,1524-1578`. Defectos D-F3B-11. Experto: si (QD-F3B-07). Confianza Media. Pregunta: ¿se espera que el costo previo ya venga calculado?

**FM-CER-42 Cuenta de inversion de los rescates.** Given rescate no rechazado con `GeneraCargoAbono="S"` (no se exige `MueveCta` ni origen manual, a diferencia de FM-CER-29). When arma: abono `MOVABN` en moneda del fondo (`Monto` = `sol.Monto`, `MontoOperacion` = `MontoPago`; descripcion `RESCATE FONDO MUTUO`; `IdTipoOperacion="RESCFM"`; `FechaOperacion=FechaAceptacion`; mismas reglas de fecha que FM-CER-29), y si es diferido: cargo en moneda del fondo por `MontoPago` y abono en moneda de inversion por `MontoPagoLocal`; y si el movimiento es `RESREINV` o `RESCOM` y `MontoComisionLocal > 0`: cargo en moneda de inversion por la comision. Llama `CrearCargoAbono`. Ej: rescate 1005 `RESREINV` diferido con comision local 5.000 -> abono principal + cargo + abono + cargo de comision (4 lineas). `MontoComisionLocal` nulo -> NPE. Fuente `C:679,1827-1911`. Defectos D-F3B-01. Experto: si (QD-F3B-08). Confianza Alta.

**FM-CER-43 Errores de cuenta de inversion (rescates).** Given mensajes `ERROR`. When igual que FM-CER-30 pero sobre rescates. Then rechaza el primer rescate con ese id; suma el contador de `CrearCargoAbono`. Fuente `C:680-694`. Confianza Alta.

**FM-CER-44 Tesoreria: seleccion de rescates.** Given rescate no rechazado y `GeneraSpot != "S"`. When: (a) `IdMedioDePago != "CIN"`, tipo distinto de `RESREINV`, `IndicadorMLT = "N"` y `RelacionTraspaso != "S"`; (b) o bien `RelacionTraspaso = "S"` (aunque el medio sea `CIN`). Then (a) agrega una `SolicitudAporteRescate` a `ProcesarIngresoEgresoTesoreria` con `IdInterno=IdSolicitud`, `FechaEmision = FechaLiquidacion` (ver D-F3B-07), `ParidadFechaInversion = ParidadFechaCertificacion`, `RelacionTraspaso="N"`; (b) agrega una con los datos de cuenta bancaria del traspaso (`obtenerTraspasoSolicitud`: el de la solicitud o el de su relacionada; si ninguno, no agrega), `RelacionTraspaso="S"`, medio `TRA`. Solo rescates; los aportes no pasan por aqui. Fuente `C:1960-2042`. Defectos D-F3B-07. Gate G1. Experto: si. Confianza Alta.

**FM-CER-45 Tesoreria: retencion y pago al SII.** Given una entrada de (a) con `TieneMontoRetencion="S"`. When resta `MontoRetencion` a `MontoPagoLocal` (moneda de **inversion** menos monto de moneda del **fondo**, ver D-F3B-09); y si `IdRegimenTributarioAPV` es `APV-B` o `APV-AN`, agrega una **segunda** solicitud con `IdInterno = -IdSolicitud`, `MontoPagoLocal = MontoRetencion`, persona/cuenta del SII, `RelacionTraspaso="S"`, medio `TRA` y `IdConcepto` = parametro `CONCEPTO_RETENCION_PAGO_SII`. `personaSII` nula con `APV-AN` -> NPE. Fuente `C:1972-1973,1992-2018`. Defectos D-F3B-08/09. Experto: si (QD-F3B-06). Confianza Alta.

**FM-CER-46 Tesoreria: bonificacion fiscal.** Given regimen APV `BF` en una entrada de (a). When completa los datos con la persona/cuenta de la TGR, `RelacionTraspaso="S"` y medio `TRA`. Fuente `C:1975-1985`. Experto: si. Confianza Alta.

**FM-CER-47 Tesoreria: invocacion y lectura del resultado.** Given entradas armadas. When llama `ProcesarIngresoEgresoTesoreria.logica` (`p_SimularOperacion="N"`, `p_EsIngresoAutomatico="N"`). Then **solo** se leen `P_OUT_RESULTADOPROCESO` con `Resultado="RECH"` y `Campo="NumeroReferencia"` (rechaza el primer rescate con `Math.abs(Id)` igual a su id) y los `P_OUT_INGRESOEGRESOGENERADO` (asigna `IdIngresoEgreso` a la primera solicitud con `NumeroReferencia == IdSolicitud`); el contador y los mensajes de Tesoreria **no se suman a la salida** (D-F3B-03; analoga D-F2-11). Fuente `C:698-721,2043-2051`. Defectos D-F3B-03, D-TES-1/2. Gate G1. Confianza Alta.

**FM-CER-48 Facturacion de la comision: seleccion.** Given rescate no rechazado con `GeneraSpot = "N"` (`equalsIgnoreCase`), tipo `RESCOM`, `RESNORM` o `RESTRAPOR`, `MontoComisionLocal > 0` y `RelacionTraspaso != "S"`. When arma un `MovimientoComprobante_Entrada`. Then `NumeroMvtoCmbte=IdSolicitud`, `Cantidad=1`, `IdTipoProducto="INFM"`, `IdBoveda="COR"`, `IdBolsa="BCS"`, `IdTipoOperacion="RESCFM"`, `IdCondicionLiquidacion="T0"`, `IdTipoConvenio="SCV"`, `IdFormaCustodia="CUS"`, `IdTipoCanalIngreso="AGE"`, banderas `Es...="N"`, `ModPrefFact="MODPREFNORM"`, y ~40 campos en cero o vacios. Tipo `RESNORM` o `RESTRAPOR`: comprobante `FOE`, concepto `COSAPV` si el proposito de portafolio es `APV` o `COSAFM`, `ActualizarCuentaInversion="S"`; tipo `RESCOM`: tipo de comprobante = `TipoComprobanteFacturaApv`/`NoApv` segun proposito, concepto `COCAPV`/`COAFFM`, monto = `CostoMonedaLocal`, `ActualizarCuentaInversion="N"`. Fuente `C:2159-2262`. Defectos D-F3B-14 no; D-F3B-12. Gate G6. Experto: si. Confianza Alta.

**FM-CER-49 Facturacion: IVA, neto y cobro.** Given `COM.Cobro` `IVA` con `ValorCobro` V (dato; ejemplo V=19; `cobros.get(0)` lanza `IndexOutOfBounds` si no hay fila). When `IVA = V/100`; para tipo `RESNORM`/`RESTRAPOR`: `Monto = MontoMovimientoComprobante = MontoLiquidacion = MontoComisionLocal / (1 + IVA)` (DECIMAL128; si `MontoComisionLocal` es igual a `new BigDecimal(0)` con la **misma escala**, queda tal cual: `equals` sensible a escala) y, si `Monto > 0`, agrega un cobro `IdCobro='IVA'` por `MontoComisionLocal/(1+IVA) x IVA` con `Porcentaje=V`; para `RESNORM`, ademas una forma de pago `CIN` (`AfectarCuentaInversion="S"`, `AfectarTesoreria="N"`) por `MontoComisionLocal`. Ej: comision local 11.900 y V=19 -> neto 10.000, cobro IVA 1.900, forma de pago 11.900. Fuente `C:2169-2172,2189-2292`. Defectos D-COM-2 (IVA fijo analogo), D-F3B-12. Gate G6. Experto: si (QD-F3B-09). Confianza Alta.

**FM-CER-50 Facturacion: invocacion y resultado.** Given movimientos. When llama `CrearMovimientoComprobante.logica` (`p_simularOperacion="N"`, `p_UsaReglasNegocio="S"`, `p_EsIngresoAutomatico="N"`) solo si hay movimientos. Then suma su contador; cada `ERROR` rechaza el primer rescate con ese id. Fuente `C:724-739,2297-2306`. Gate G6. Confianza Alta.

**FM-CER-51 Comprobante automatico de comision.** Given `RESCATE_COMISION_COMPROBANTE_AUTOMATICO="S"` (`equalsIgnoreCase`). When por cada movimiento generado cuyo rescate (`rescatesHash` por `List2Hash`) no este rechazado, sea `RESNORM` o `RESREINV`, tenga `MontoComisionLocal > 0` y `RelacionTraspaso != "S"`: arma `P_MOVIMIENTOFACTURAR{IdMovimientoComprobante, FechaComprobante=fechaSistema}` y llama `CrearComprobante`. Then **el contador que se suma es el de `salidaCrearMovimientoComprobante`, no el de `CrearComprobante`** (se cuenta dos veces el del movimiento y nunca el del comprobante); los `ERROR` de `CrearComprobante` sí rechazan al rescate. `RESTRAPOR` y `RESCOM` quedan fuera del comprobante (la condicion de movimiento los incluye y la de comprobante no). Fuente `C:741-759,2311-2341`. Defectos D-F3B-04, D-NOR-9 (`List2Hash` ante duplicados). Gate G6. Experto: si. Confianza Alta.

**FM-CER-52 Reinversion: seleccion y lectura.** Given rescates (todos; el filtro esta dentro). When lee `ReinversionSerieFondoDestino` (con familia y moneda del fondo destino) para los ids y las banderas de origen/destino (`FondoOrigen/DestinoNacionalDolar`, `...InternacionalDolar` contra `PLA.MonedaAdicional`); agrupa destinos por `IdSolicitudAporteRescate` (`groupingBy`, orden de `HashMap`) y movimientos de custodia por solicitud; `List2Hash` de solicitudes y de series. Then solo opera sobre rescates no rechazados de tipo `RESREINV` (`_c.Reinversion`). Fuente `C:762,2343-2376`; SQL `ComunesSolicitudes_SelectReinversionSerieFondoDestino`. Defectos D-F3B-12, D-NOR-9. Confianza Alta.

**FM-CER-53 Reinversion: rama misma familia y misma moneda.** Given destino con `IdFamiliaFondo` igual al del rescate (no vacio) y `IdMoneda` del fondo destino igual a `sol.IdMoneda`. When por cada movimiento de custodia retirado arma un aporte con `ratio = Cantidad(movimiento) / NumeroCuotas(rescate)` (DECIMAL128): `MontoOrigen = Monto = MontoPagoLocal x (Porcentaje/100) x ratio` (formateado a la moneda), `CostoUF = CostoInversionEnFactorAjusteARetirar x Porcentaje/100`, `FechaOrigenInversion = FechaOrigenCompra` del movimiento, `NumeroCuotas=0`, `Visado="S"`, `IdAutorizadoOrigen=0`; solo si `Monto > 0`. Then `montoEsperado = MontoPagoLocal x Porcentaje/100` (formateado); `diferencia = montoEsperado - suma de los montos`; ver FM-CER-55. Ej: rescate 1005, `MontoPagoLocal` 1.000.000 CLP, un destino al 100 % y movimientos de 60 y 40 cuotas sobre 100 -> aportes de 600.000 y 400.000 (diferencia 0). Sin movimientos o todos con monto 0 -> no hay aporte y FM-CER-55 revienta. Fuente `C:2378-2448`. Defectos D-F3B-13. Experto: si (QD-F3B-10). Confianza Alta.

**FM-CER-54 Reinversion: rama otra familia o moneda.** Given que no se cumple FM-CER-53. When arma **un** aporte por destino: `CostoUF = sol.CostoUF x Porcentaje/100`, `FechaOrigenInversion = FechaCertificacion`, `MontoOrigen = Monto = MontoPagoLocal x Porcentaje/100` (formateado). Then queda en la lista; `montoCuotasTotal` suma `Monto`. Fuente `C:2463-2523`. Experto: si (QD-F3B-10). Confianza Alta.

**FM-CER-55 Reinversion: ajuste de redondeo.** Given que se agregaron los aportes del destino. When (rama FM-CER-53) `diferencia != 0` -> se suma al `Monto` y `MontoOrigen` del **primer** aporte con `IdSolicitudAporteRescateRelacionado` igual (`.get(0)`; el comentario dice "ultimo") y se re-formatea; (ambas ramas, al terminar el ultimo destino del rescate: `indexTotal > tamano`) `diferencia = sol.MontoPagoLocal - montoCuotasTotal` -> idem sobre el primer aporte. Lista vacia -> `IndexOutOfBoundsException` (la comprobacion `ultimoAporte != null` es inutil). Ej: 3 destinos al 33,3333 % de 1.000.000 -> aportes 333.333,30 formateados; el residuo se carga al primero. Fuente `C:2449-2460,2524-2534`. Defectos D-F3B-13. Experto: si (QD-F3B-10). Confianza Alta.

**FM-CER-56 Reinversion: fechas del aporte.** Given sol y destino. When `FechaEmision`: si `sol.IdMoneda` es igual (ignorando mayusculas) a la moneda del fondo destino: `FechaCertificacion` si origen y destino son `FondoInternacionalDolar="S"` y `apor.Switch="N"`, si no `FechaLiquidacion`; si las monedas difieren: `FechaLiquidacion`. `FechaCertificacionRescateOrigen`, `FechaLiquidacionRescateOrigen`, `FechaValorCuotaRescateOrigen` = las del rescate. Spot: si `sol.IdMoneda = USD`: `FechaCertificaSpotCompra = FechaCertificacion` y `FechaLiquidaSpotCompra = FechaLiquidacion`; luego, si `IdMonedaSistema = IdMoneda` (se pisa lo anterior): ambas = `FechaAceptacion` (solo si `FechaCertificacion` y `FechaLiquidacion` no son nulas). Fuente `C:2412-2441,2489-2518`. Defectos D-F2-4 (fechas Spot de AR). Experto: si (QD-F2-01, QD-F2-07). Confianza Alta.

**FM-CER-57 Reinversion: llamada a AR y mapeo de ids.** Given aportes armados (`IdInterno` correlativo 1..n con `map[IdInterno] = IdSolicitud` del rescate), `Canal`=canal del rescate, `IdAgrupador`, `Switch`=el del destino, `IngresoAutomatico`, `MontoTotalInversion = MontoPagoLocal x Porcentaje/100` (sin formato), `IdMedioDePago`/`SolicitudBatch`/`OrigenOperacion` del rescate. When llama `CrearSolicitudAporteReinversion.logica` con `p_SimularOperacion="N"`, `p_IdTipoCanalIngreso="AGE"` y `p_UsaReglasNegocio` de la entrada (el detalle de AR esta en FM-REI-41..68: lotes de 2.000, G1 Tesoreria si el medio no es `CIN`, G2 Spot, G3 `SaldoFlujoCaja`, INSERT `APOREINV`, actualizacion de `FechaFinalReinversion`, AGF y log). Then reemplaza `msj.id` y `ResultadoSolicitud.IdInterno` por el id del rescate; suma el contador de AR; cada `ERROR` rechaza el primer rescate con ese id. Fuente `C:2539-2556,762-777`. Defectos D-F2-11/12/13 (AR). Gate G1, G2, G3, G5 (de AR). Experto: si. Confianza Alta.

**FM-CER-58 Aportes de switch.** Given `P_OUT_RESULTADO` de AR. When `res.Switch.equals("S")` (nulo -> NPE). Then crea un `InSolicitudes{IdSolicitudAporteRescate, IdTipoCanalIngreso}` y lo agrega a `aportesSwitch` (aunque AR haya rechazado ese aporte: no se revisa su estado). Se usan en FM-CER-63 y FM-CER-65. Fuente `C:779-787`. Defectos D-F3B-30. Confianza Media. Pregunta: ¿un aporte de switch rechazado por AR llega a certificarse?

**FM-CER-59 Spot de los rescates: construccion.** Given rescate no rechazado con `GeneraSpot="S"`. When `idTipoOperacionSpot`: `VENSPT` si es aporte de reinversion en `USD`, `COMSPT` en los demas casos de aporte, y **`COMSPT` para rescates**; para `COMSPT`: forma de pago "recibe" (`RecibePagaPortafolio="R"`): `MOVEGR`, moneda `sol.IdMoneda`, monto `sol.Monto`, `AfectarTesoreria` = `S` solo para `RESCNN`, `IdFormaDePago = COMSPT_CIN_<moneda>_0` (o `COMSPT_TRA_<moneda>_0` si `TipoComision="PER"`); "paga" (`P`): `MOVING`, `VENSPT_<medio>_<IdMonedaSistema>_0` (medio por defecto `CIN`), monto `MontoMonedaSistema`, `AfectarTesoreria="S"` salvo medio `CIN`. `FechaLiquidacion` = `FechaAceptacion` para aportes y `FechaLiquidacion` para rescates. Then `ProcesarIngresoOperacionSpot.logica` con `p_CalculaMargen="S"`. Los codigos `COMSPT/VENSPT/MOVEGR/MOVING/_0/CIN/TRA` son literales (D-F2-4). Fuente `C:2057-2157`. Defectos D-F2-4, D-F3B-14. Gate G2. Experto: si (QD-F2-07). Confianza Alta.

**FM-CER-60 Spot: lectura del resultado.** Given la salida de Spot. When recorre `P_OUT_IDOPERACIONSPOT`: `Estado="RECH"` -> `RZD` `ERROR_CREAR_SPOT` en la solicitud con ese `IdInterno`; si no `IdOperacionSpot = rs.IdOperacionSpot`. Los mensajes `ERROR`: rechaza la primera solicitud con `IdSolicitud = abs(id)` **o cualquiera si `id == 0`** (rechaza la primera de la lista). Suma el contador de Spot. Fuente `C:790-830`. Defectos D-F3B-03, D-F3B-14. Gate G2. Confianza Alta.

#### Fase 5: cierre

**FM-CER-61 Actualizacion masiva a certificado.** Given las solicitudes PEN o ADV. When `UPDATE FMM.SolicitudAporteRescate SET FechaCertificacionRegistro=CURRENT_UTCTIMESTAMP, Monto, ValorCuota, NumeroCuotas, ParidadFechaCertificacion=COALESCE(.,0), CostoMonedaLocal, MontoPago, MontoPagoLocal, IdSolicitudAporteRescateRelacionado, Spread (solo RES, si no 0), CostoUF (RES o APO), RentabilidadReal, IdMonedaSistema, MontoMonedaSistema, IdIngresoEgreso, IdOperacionSpot, IdCartera, MontoComision, MontoComisionLocal, ValorUFFechaInversion (solo APOREINV), Estado='CRT'` por `IdSolicitudAporteRescate`. Then las rechazadas no se actualizan. Sin guarda de estado en el `WHERE`. Fuente `C:835,2738-2750`; SQL `UpdateSolicitudes`. Defectos D-F3B-16. Confianza Alta.

**FM-CER-62 Notificacion asincrona de certificacion.** Given las solicitudes actualizadas. When `NotificarEstadoOrdenFondoMutuo.logicaAsync` arma la entrada (ids) y llama por HTTP al servidor de lotes (`PLA.CallBatchURL`, `PLA.CallBatchToken`; se cae si faltan). Then no se espera el resultado ni se revierte; se dispara tambien en simulacion y antes de que termine la transaccion. Fuente `C:836,2752-2762`. Defectos D-F3B-02. Gate G8. Confianza Alta.

**FM-CER-63 Certificacion recursiva de aportes switch.** Given `aportesSwitch` no vacio. When crea una entrada hija con los datos de cabecera de la original (`getPojoEntradaSwitch`), `p_NotificaOWP="N"`, `p_CertificacionAporteSwitchSimulado = p_SimularOperacion`, y llama `new CertificarSolicitudAporteRescate().logica` en la misma conexion. Then suma contador, mensajes y resultados del hijo; en el hijo, si `p_CertificacionAporteSwitchSimulado="S"`, `procesarSalida` devuelve `IdSolicitudAporteRescate=0`. En el hijo se repiten FM-CER-02..61 para los aportes (el aporte switch se salta FM-CER-11/12). Fuente `C:838-843,2764-2775,1602-1604`. Defectos D-F3B-01. Confianza Alta.

**FM-CER-64 Monto en moneda del sistema del rescate de reinversion internacional sin Spot.** Given solicitudes no rechazadas `APOREINV` con `Relacionado > 0`, `IdMonedaSistema != IdMoneda` y `GeneraSpot != "S"`. When lee `SUM(MontoMonedaSistema)` de `FMM.SolicitudAporteRescate` agrupado por `IdSolicitudAporteRescateRelacionado`. Then `UPDATE MontoMonedaSistema` del **rescate** (el alias de la columna es `IdSolicitudAporteRescate`) con esa suma. Corre despues de FM-CER-61, asi que pisa el valor ya actualizado. Fuente `C:845,2820-2835`. Experto: si (QD-F2-01). Confianza Alta.

**FM-CER-65 Notificacion OWP.** Given parametro `PLA.NotificarEstadoOrdenOWP="S"` y `p_NotificaOWP="S"` (ambos `equalsIgnoreCase`). When toma las solicitudes actualizadas y los aportes switch con canal `OWP`, las re-lee con `SelectSolicitudes` y llama `NotificarEstadoOrdenesOWP.go` por solicitud: `Solsec`, `NTitulos=NumeroCuotas`, `Precio=ValorCuota`, `Fecha=FechaAceptacion`, `Estado=EstadoSolicitud` (valor re-leido), `Comision=MontoComision`, `Gastos=0`, `Mercado="F"`, `Divisa=IdMoneda`, `TipoCambio=CostoMonedaLocal`, `SolsecAsoc` = "" si el movimiento es `RESREINV`, si no el relacionado, `IdClasificacionTipoInstrumento="FM"`. Then se dispara aunque el contador ya sea > 0 por el hijo o la simulacion. Fuente `C:848-850,2777-2818`. Defectos D-F3B-02. Gate G8. Confianza Alta.

**FM-CER-66 Logs.** Given el cierre. When `agregarLogSistema` (un registro `CERTIFICACION APORTE RESCATE`, `EstadoRegistro="CRT"`, todo lo demas vacio o cero, `IdProceso=0`) por `AgregarLog` y luego (despues de marcar exitosas) `insertarLog` (una fila por solicitud en `FMM.LogCertificacionAporteRescate` con `Codigo`, `IdEstado`, montos, `Simulacion = p_SimularOperacion`). Then el resultado de `AgregarLog` no se revisa. Fuente `C:853-860,1613-1643,2837-2871`. Defectos D-NOR-11, FM-NOR-44. Gate G5. Confianza Alta.

**FM-CER-67 Marca de exito y salida.** Given solicitudes aun PEN. When `marcarSolicitudesExitosas`: `EstadoProceso="APD"`, `Codigo="UI_EXITO"`; `procesarSalida` arma un `ResultadoCertificacion` por solicitud (incluidas rechazadas, ADV y APD) con `NombreEstado` del `PLA.Estado` (si el estado no esta en la tabla -> NPE), `DescripcionCodigo` = la ya armada o la traduccion del `Codigo` con parametros (`IdComprobante`, `IdSolicitudAporteRescateRelacionado`, `IdSolicitudAporteRescate`, `DocumentoIdentidad`, `CodigoPortafolio`; la traduccion usa la `Connection` desde `parallelStream`, D-F3B-06). Then la salida del padre se concatena con la del hijo switch. Ej: solicitud 1001 certificada -> `{Codigo:"UI_EXITO", Estado:"APD"}`; rechazada por fecha -> `{Codigo:"UI_FECHA_INVALIDA_CERTIFICACION_FONDO_MUTUO", Estado:"RZD"}`. Fuente `C:857-860,1580-1611,2873-2880`. Defectos D-F3B-06. Confianza Alta.

**FM-CER-68 Modo simulacion.** Given `p_SimularOperacion="S"`. When llega al final. Then `P_OUT_ERRORESCONTADOR = 1` (**aunque no haya errores**, pisa el valor acumulado) y el anfitrion revierte todo lo de la conexion; la simulacion ejecuto de verdad cada paso (D-F3B-01). Con otro valor (incluido `""`, que usa el job) no hay efecto. Fuente `C:862-864`. Defectos D-F3B-01/02. Confianza Alta.

**FM-CER-69 Rechazos que no suben el contador.** Given una solicitud rechazada por FM-CER-06..13, FM-CER-16, FM-CER-21/22, FM-CER-31, FM-CER-40 o FM-CER-47 (RECH) o FM-CER-60 (RECH de Spot). When al menos una otra solicitud del lote se certifica. Then el contador queda en 0 (salvo colaboradores que lo suman), el anfitrion hace `commit` y la rechazada solo aparece en `P_OUT_RESULTADO` con `Estado="RZD"`; el `job` y `ProcesarCarga` leen `res.Estado`. Fuente `C:441-537,859`. Confianza Alta.

**FM-CER-70 Orden de efectos y atomicidad (resumen).** Given la linea de tiempo de la seccion 7. When algun paso lanza excepcion. Then el anfitrion revierte toda la conexion; no hay compensaciones para HTTP (notificaciones, `AgregarLog` por router, Spot/Facturacion si pasan a HTTP: ahi la no atomicidad es nueva y esta bajo compuerta). Fuente `C:370-868`. Defectos D-F3B-01/02/03. Confianza Alta.

**FM-CER-71 Excepciones y NPE catalogados.** Given entradas fuera de lo esperado. Then se propaga la excepcion (sin `catch`; salvo FM-CER-24 y los `try` de parametros opcionales): parametros obligatorios (FM-CER-03); `FolioCartaLiquidacion`, `Switch`, `IdRegimenTributario`, `CostoUF`, `MontoComisionLocal` nulos (`C:500,1771,1482,1573,1897`); `custodia == null` (`C:1335`); `personaSII` nula con `APV-AN` (`C:2005`); `cobros.get(0)` (`C:2170`); `hCalculo`/`rescatesHash.get` nulo (`C:2321`); `reinversionSerie.get` nulo (`C:2375`); `.get(0)` de lista vacia (`C:2452,2527`); `res.Switch` nulo (`C:781`); `estado` desconocido (`C:1597`); `toMap` con claves repetidas (`C:962,973,2933`); `List2Hash` con claves repetidas (`C:427,1039,1089,1318,1555,1678,2319,2364,2365`); `ids.get(mje.id)` nulo (`C:1928`). Fuente `C:370-3019`. Defectos D-NOR-9, D-F3B-05/08/13. Confianza Alta.

**FM-CER-72 Concurrencia y estado compartido.** Given `parallelStream` en `C:921,962,973,1034,1046,1053,1064,1127,1325,1397,1407,1424,1459,1471,1482,1489,1514,1527,1582,1656,2361,2362,2452,2527,2759,2781,2782,2822,2913,2933`. When mutan campos de cada `sol` (sin compartir) o leen la **misma `Connection`** (FM-CER-31, FM-CER-67: `MensajeSalida(... con ...)` consulta la BD desde hilos del `ForkJoinPool`). Then el resultado depende de que el driver tolere el acceso concurrente; el orden de las listas se conserva (`collect(toList())` sobre origen ordenado) pero no el de los efectos de `forEach`. Fuente citada. Defectos D-F3B-06, D-NOR-10. Confianza Media. Pregunta: ¿se ha observado falla intermitente en `UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION`?

**FM-CER-73 Parametros y literales de negocio fijos en el codigo.** Given el codigo. Then literales: estados `ING/ACP/PEN/ADV/RZD/APD/CRT/LIQ/ANU/VIG/RECH`; medios `CIN`, `TRA`, `PAC`; origen `M`; canal `AGE`, `OWP`; tipos de movimiento `RESNORM`, `RESREINV`, `RESCOM`, `RESCNN`, `RESTRAPOR`, `APOREINV`; regimenes `BF`, `APV-A`, `APV-B`, `APV-AN`, `DC-DL`, `DC-AL`, `DC-R`, `18Q`; monedas `USD`, `CLP`, `UF`, `UTM`; codigos Spot `COMSPT`/`VENSPT`; comprobantes `FFMM`, `FFM`, `FOE`; conceptos `COSAPV`, `COSAFM`, `COCAPV`, `COAFFM`; `MODPREFNORM`; `GEN_OCSNEG`; `PROPOSITOAPV`; `0001-01-01`. Todos deben pasar a configuracion o constantes nombradas con prueba. Fuente `ConstantesFondoMutuo.java` y citas anteriores. Confianza Alta.

### 8.2 FM-CXF-01..06: `CertificarPorFiltrosSolicitudAporteRescate` (X)

**FM-CXF-01 Contrato.** Given `{p_IdEmpresa,p_IdUsuario,p_IdModulo,p_SimularOperacion,p_UsaReglasNegocio,p_FechaCertificacion,p_IdTipoCanalIngreso,p_Filtros:[{IdEmisor,IdMoneda,IdSerieFondo,Nacional,TipoSolicitud,IdTipoMovimientoFondoMutuo,MoneyMarket,IdCarga}]}`. When entra por `call` (`new Gson()`). Then devuelve el contador, mensajes y resultados de Certificar tal cual. Fuente `X:32-91`. Confianza Alta.

**FM-CXF-02 Seleccion por filtros.** Given un filtro. When `SELECT DISTINCT IdSolicitudAporteRescate` desde `FMM.ConsultarSolicitudAporteRescate` (left join `DetalleCargaSolicitudesAporteRescate`) con: cada filtro de texto vacio o nulo = sin filtro (`IdEmisor`, `IdMoneda`, `IdSerieFondo`, `TipoSolicitud`, `IdTipoMovimientoFondoMutuo`); `Nacional` y `MoneyMarket` nulos o `N` = sin filtro, otro valor = igualdad con `EsFondoNacional`/`MoneyMarket`; `EstadoSolicitud IN ('ING','ACP')`; `FechaCertificacion = p_FechaCertificacion`; `IdEmpresa`; `IdCarga = 0` o (`IdCargaSolicitudesAporteRescate = IdCarga` y detalle en `PEN`). Then agrega los ids a `p_Solicitudes`. Un `IdCarga` nulo hace falso el `OR` (no devuelve filas). Ej: filtro `{TipoSolicitud:"RES", IdMoneda:"USD", IdCarga:0, Nacional:"N", MoneyMarket:"N"}` y fecha 2026-03-10 -> todos los rescates USD ING/ACP con esa fecha. Fuente `X:107-120`; SQL `CertificarPorFiltros..._SelectSolicitudes`. Experto: no. Confianza Alta.

**FM-CXF-03 Varios filtros.** Given N filtros (el comentario dice "generalmente uno"). When concatena los resultados sin deduplicar. Then un id devuelto por dos filtros causa `SOLICITUD_DUPLICADA` en Certificar (FM-CER-02) y no se certifica nada. Fuente `X:106-120`. Confianza Alta.

**FM-CXF-04 Sin filtros o sin coincidencias.** Given `p_Filtros=[]` o filtros sin filas. When llama a Certificar con lista vacia. Then `NO_SE_INGRESARON_SOLICITUDES` (contador 1). Fuente `X:122`, FM-CER-02. Confianza Alta.

**FM-CXF-05 Entrada y salida.** Given la llamada. When copia a Certificar solo empresa, usuario, modulo, simular, reglas, fecha y canal (no `p_NotificaOWP`, queda "S" por defecto). Then copia contador, mensajes y resultados. No agrega validaciones propias. Fuente `X:97-126`. Confianza Alta.

**FM-CXF-06 Uso desde el job.** Given `EsMasivo="S"`. When el job arma filtros por posicion de listas `;` (largos distintos -> `ArrayIndexOutOfBounds`). Then si contador > 0 lanza `Exception` con la ultima descripcion de error. Fuente `J:81-125`. Defectos D-F3B-29 (main no portado). Confianza Alta.

### 8.3 FM-LIQ-01..20: `LiquidacionSolicitudAporteRescate` (L)

**FM-LIQ-01 Contrato.** Given `{p_IdEmpresa,p_IdUsuario,p_IdModulo,p_SimularOperacion,p_UsaReglasNegocio,p_Movimientos:[{IdMovimiento}],p_Filtros:[{IdInterno,IdEmisor,TipoSolicitud,IdEstado,FechaCertificacion,FechaLiquidacion,IdSerieFondo}]}`. When entra por `call` (decodifica con `Utils.getGsonBuilder()`). Then **recalcula** `P_OUT_ERRORESCONTADOR` con `MensajeSalida.getNumeroErrores(P_OUT_MENSAJES)` (compara `tipo == MENSAJE_ERROR` por referencia, D-REQ-11); salida `{P_OUT_RESULTADO[{IdInterno,IdSolicitudAporteRescate,IdSerieFondo,Codigo,Estado,NombreEstado}],P_OUT_MENSAJES,P_OUT_ERRORESCONTADOR}`. No usa `RevisarRequisitosBasicos`. Fuente `L:52-121,203-213`. Defectos D-REQ-11. Confianza Alta.

**FM-LIQ-02 Parametros.** Given empresa. When lee `PLA.FechaSistema`, `FONDOMUTUO/LiquidacionCuentaInversionCliente`, `FONDOMUTUO/VALIDAR_LIQUIDACION_OMNIBUS` (obligatorios) y `PLATAFORMA/APV.Habilitado` **con empresa 1 fija** (`IdEmpresa = 1L`), y la tabla de modalidades de pago (`toMap` por `IdMedioDePago`, duplicados lanzan). Fuente `L:224-228,245-246`. Defectos D-F3B-18, D-NOR-9. Experto: si (QD-F3B-11). Confianza Alta.

**FM-LIQ-03 Filtros vacios.** Given `p_Filtros=[]`. When valida. Then agrega `NO_SE_INGRESARON_FILTROS` (id 0, contador de mensajes) pero **no corta**: si hay movimientos el proceso sigue y liquida, y la respuesta trae error (el anfitrion revierte por contador > 0). Sin movimientos ni filtros: `findFirst().get()` lanza `NoSuchElementException`. Fuente `L:232-236,257`. Defectos D-F3B-22. Confianza Alta.

**FM-LIQ-04 Seleccion por movimientos.** Given `p_Movimientos` con ids (`IdMovimiento` = `IdSolicitudAporteRescate`). When `SelectSolicitudes`: `IdEmpresa`, id en la lista, `EstadoLiquidacion='ING'`, movimiento en `APONORM, RESNORM, APOREINV, RESREINV, APORESPRO, APOOVER, RESTRAPOR, RESTRASP, APOTRASP, APODIVCFI, RESCNN`. Then **no** se exige `EstadoSolicitud` certificado (ni para aportes ni para rescates por este camino). Fuente `L:249-254`; SQL `LiquidacionSolicitudAporteRescate_SelectSolicitudes`. Experto: si (QD-F3B-12). Confianza Media. Pregunta: ¿la UI garantiza que solo se envian solicitudes certificadas?

**FM-LIQ-05 Seleccion por filtros.** Given sin movimientos. When toma **solo el primer** filtro: `IdEmisor`, `TipoSolicitud`, `IdSerieFondo` vacios o nulos = sin filtro; `FechaCertificacion`/`FechaLiquidacion` vacias -> `0001-01-01` (que el SQL trata como "sin filtro" por `<> ''`... el valor `0001-01-01` NO es vacio: **filtra por esa fecha**; ver Q-W0B-12); estado: aporte con `EstadoSolicitud = IdEstado` (o cualquiera si vacio) y **rescate solo `CRT`**; `EstadoLiquidacion='ING'`; mismos tipos de movimiento. Then `.get()` de `fecha.equals("")` con fecha nula -> NPE. Fuente `L:255-266`; SQL `..._SelectSolicitudesFiltros`. Defectos D-F3B-22. Experto: si. Confianza Media. Pregunta: ¿el `0001-01-01` reemplazo de vacio filtra de verdad por esa fecha?

**FM-LIQ-06 Omnibus.** Given `VALIDAR_LIQUIDACION_OMNIBUS="S"`. When detecta aportes (`TipoSolicitud="APO"`) con `IdOmnibus = 0`. Then los **quita** de la lista (`removeAll`) y les pone `Codigo=UI_ERROR_APORTE_SIN_OMNIBUS`/`Rechazo=1` sobre objetos que ya no se usan; el mensaje de error esta bajo `sinOmnibus.size() == 0`, condicion imposible dentro de `numOmnibus > 0`, asi que **no sale mensaje**: los aportes desaparecen en silencio. Fuente `L:269-289`. Defectos D-F3B-23. Experto: si. Confianza Alta.

**FM-LIQ-07 Agrupacion.** Given las solicitudes. When agrupa por `Grupo{IdPersonaAGF, TipoSolicitud, IdMoneda=IdMonedaFondo, IdPortafolioAGF, IdMedioDePago, FechaLiquidacion, FechaCertificacion}` con `IdMedioDePago` = `IdMedioDePagoCargoAGF` para aportes y `IdMedioDePagoAbonoAGF` para rescates; `hashCode` suma de hashes (colisiones posibles) y `equals` por todos los campos (campo nulo -> NPE). Then iteracion en el orden del `HashMap` (determinista para las mismas claves, no por orden de entrada). Fuente `L:170-201,297-308`. Defectos D-F3B-32. Confianza Alta.

**FM-LIQ-08 Secuencias.** Given `G` grupos y `S` solicitudes. When pide `G` (o `G+S` si `APV.Habilitado="S"`) folios de `Secuencias::FolioCartaLiquidacionAporteRescate` y de `Secuencias::SecuenciaComprobanteLiquidacion` (se consumen aunque despues falle). Fuente `L:310-320`. Depende F1: kit (`Utils.getSecuencia`). Confianza Alta.

**FM-LIQ-09 Linea de Tesoreria por grupo (AGF).** Given un grupo con suma `Monto` M (BigDecimal.add). When arma un `IngresoEgreso_Entrada`: `NumeroReferencia = k` (0..G-1); aporte -> `IdTipoMovimiento="MOVEGR"`, `IdConcepto="CAAEFM"`, `IdTipoOperacion="APORFM"`; rescate -> `MOVING`, `ABREFM`, `RESCFM`; `IdTipoPersona`/`TitularPago` = `CLI` si `LiquidacionCuentaInversionCliente="S"` si no `CNT`; `IdPortafolio` = portafolio AGF si `S` si no 0; `IdTipoProducto="INFM"`; `IdTipoComprobante="LFM"`; `IdComprobante = carta[k]`, `IdOperacion = comprobante[k]`; `FechaLiquidacion=FechaOperacion` = fecha del grupo; `IdCondicionLiquidacion="T0"`; `IdBolsa="BCS"`; `MueveCuentaInversion` = `N` si la liquidacion es por cuenta del cliente (`S`) y `S` si no; `Bilateral/GeneraComprobante/AgruparEnComprobante/TitularPago` = `N`. Forma de pago: medio del grupo, modalidad de la tabla o `MODCAJ`, monto M, resto en cero/vacio/`0001-01-01`. Ej: dos aportes de 100.000 y 250.000 de la misma persona AGF, CLP, medio `TRA`, mismas fechas -> una linea `MOVEGR` por 350.000, concepto `CAAEFM`, y las dos solicitudes con `FolioCartaLiquidacion = carta[0]`. Fuente `L:328-411`. Defectos D-TES-1 (`TitularPago="N"`). Experto: si (QD-F3B-11). Gate G1. Confianza Alta.

**FM-LIQ-10 Lineas por cliente (APV habilitado).** Given `APV.Habilitado="S"`. When por **cada solicitud** agrega una linea espejo (`MOVING`/`ABREFM`/`RESCFM` para aportes; `MOVEGR`/`CAAEFM`/`APORFM` para rescates; es decir lo opuesto al grupo) con `NumeroReferencia` continuando la cuenta (`G`, `G+1`, ...), persona/portafolio de la solicitud, monto `Monto - MontoComision` (nulo -> NPE). Fuente `L:415-483`. Experto: si (QD-F3B-11). Confianza Alta.

**FM-LIQ-11 Retencion APV-B en rescate.** Given solicitud `RESNORM` o `RESTRASP`, `CodigoPortafolio="APV-B"` y `IdMedioDePago != "CIN"`. When recorre la configuracion de retencion (`PorcentajeRetencion/100` desde el SQL, tipo `RES`, `!= 0`) y toma la **primera** fila con `IdRegimenTributario` nulo o igual a `IdMatrizAgrupadorAPV` (nulo -> NPE). Then `Monto = (MontoPago - MontoPago x P) - MontoComision`. Ej: `MontoPago` 1.000.000, `P`=0,15, comision 10.000 -> 840.000. Fuente `L:468-483`; SQL `ConsultarRetencion`. Experto: si (QD-F3B-06). Confianza Alta.

**FM-LIQ-12 Enrutamiento de la linea de cliente.** Given la linea por cliente. When el medio es `CIN`: solo a la lista de cuenta de inversion; si no es `CIN` y es egreso (`MOVEGR`): solo a la lista de cuenta; si es ingreso (`MOVING`): a la lista de Tesoreria, a la de cuenta y a las formas de pago. Then `carta_solicitud[numReferencia] = solicitudesLiquidar` (la lista **completa**, no la de la solicitud). Fuente `L:512-537`. Defectos D-F3B-20. Confianza Media. Pregunta: ¿el mapeo por lista completa es intencional?

**FM-LIQ-13 Llamada a Tesoreria.** Given las listas. When llama `CrearIngresoEgreso` con `p_SimularOperacion` de la entrada, `p_EsIngresoAutomatico="N"`, aunque las listas esten vacias. Then si `P_OUT_ERRORESCONTADOR > 0`: devuelve **ese** contador y **esos** mensajes (se pierden los acumulados antes, p. ej. `NO_SE_INGRESARON_FILTROS`) sin deshacer lo escrito. Fuente `L:543-564`. Defectos D-F3B-24. Gate G1. Confianza Alta.

**FM-LIQ-14 Actualizacion a liquidado.** Given cada ingreso generado. When busca `carta_solicitud[NumeroReferencia]`, asigna `FolioCartaLiquidacion = carta[NumeroReferencia]` a **todas** las solicitudes de la lista y `UPDATE ... EstadoLiquidacion='LIQ', FolioCartaLiquidacion, FechaLiquidacionRegistro=CURRENT_UTCTIMESTAMP` en lote; agrega un resultado por solicitud con `Codigo="UI_EXITO"` y `Estado = sol.EstadoLiquidacion` (el valor leido, `ING`, no `LIQ`), `NombreEstado=""`. Then con lineas de cliente la lista completa se actualiza varias veces (queda el ultimo folio) y el resultado se duplica. `NumeroReferencia` sin entrada -> NPE. Fuente `L:566-588`; SQL `UpdateSolicitudes`. Defectos D-F3B-20/25. Experto: si. Confianza Media. Pregunta: ¿el folio final que queda en las solicitudes APV es el esperado?

**FM-LIQ-15 Cargos y abonos de cuenta de inversion.** Given `LiquidacionCuentaInversionCliente="S"` y la solicitud tiene medio `CIN`. When por **cada** solicitud `CIN` recorre **todas** las lineas de `lista_ingreso_egreso_inv` (no solo las suyas) y arma un `ObjCargoAbonoAGenerar` por linea: `IdTipoMovimiento` = `MOVABN` si `ie.IdTipoMovimiento == "APO"` (**nunca cierto**: el campo vale `MOVING`/`MOVEGR`) si no `MOVCAR`; `IdConcepto` `ABPEFM`/`CAPEFM` con el mismo defecto; `IdTipoOperacion` = `ABPEFM` si `ie.IdTipoOperacion="APORFM"`, si no `RESCFM`; `IdTipoComprobante="FMM"`, `IdOperacion`/`FolioComprobante` = comprobante de la linea, `CertificadoAutomatico="S"`; `NumeroCargoAbono = i` (continuo entre solicitudes). Llama `CrearCargoAbono` con `p_simularOperacion="N"`, `p_UsaReglasNegocio="S"`, `p_EsIngresoManual="N"`. Then con N solicitudes `CIN` y M lineas se generan N x M movimientos (repetidos). Fuente `L:591-649`. Defectos D-F3B-21/26, D-F3B-24. Experto: si (QD-F3B-11). Confianza Media. Pregunta: ¿el producto N x M es el comportamiento vigente en produccion?

**FM-LIQ-16 Errores de cuenta de inversion.** Given la salida de `CrearCargoAbono`. When cuenta los mensajes `ERROR` (`equalsIgnoreCase`). Then si `P_OUT_ERRORESCONTADOR > 0` o hay errores: devuelve **su** contador (el de la salida, que puede ser 0 aunque haya mensajes `ERROR`) y sus mensajes; `call` lo recalcula por mensajes. Fuente `L:651-657`. Defectos D-REQ-11. Confianza Alta.

**FM-LIQ-17 Folios tras cargo/abono.** Given cargos/abonos generados (`P_OUT_CARGOABONOGENERADO`). When por cada uno toma `carta_solicitud[0]`, reinicia `K=0`, recorre `carta_solicitud[0]` poniendo `FolioCartaLiquidacion = carta[K]` con `K` creciente, y si `K > 0` actualiza y agrega resultados. Then re-liquida y re-reporta las solicitudes del grupo 0 por cada movimiento generado. Fuente `L:660-682`. Defectos D-F3B-26. Experto: no. Confianza Media. Pregunta: ¿se esperan resultados duplicados?

**FM-LIQ-18 Log.** Given filtros. When arma `logList` con un registro `LIQUIDACION APORTE RESCATE` por filtro (`EstadoRegistro="LIQ"`, `NombreProceso="cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate"`) y llama `AgregarLog` con `p_IdTrabajo=1`. Then `logList` **nunca se asigna** a `entradaAgregarLog.p_Log`: se llama a `AgregarLog` con un log vacio, no se registra nada. Fuente `L:693-732`. Defectos D-F3B-19. Gate G5. Confianza Alta.

**FM-LIQ-19 Simulacion y atomicidad.** Given `p_SimularOperacion="S"`. When pasa solo a `CrearIngresoEgreso`. Then los `UPDATE` de solicitudes y `CrearCargoAbono` (`"N"`) se ejecutan igual; el contador no se fuerza a 1, de modo que un `/call` con simulacion y sin errores **hace commit**. Fuente `L:547,575,642`. Defectos D-F3B-24. Experto: si. Confianza Media. Pregunta: ¿`CrearIngresoEgreso` en simulacion devuelve `P_OUT_INGRESOEGRESOGENERADO` vacio (con lo que no se actualiza nada)?

**FM-LIQ-20 Excepciones.** Given `IdPersonaAGF`, `IdPortafolioAGF`, `FechaLiquidacion`, `FechaCertificacion`, `IdMedioDePago` nulos (`Grupo.hashCode`), `Monto`/`MontoComision` nulos, `hashModalidadPago` duplicado, `carta_solicitud.get(...)` nulo, filtros/fechas nulos. Then NPE sin `catch`; el anfitrion revierte. El `main()` (conexion `PRESURA`, ids reales) no se porta. Fuente `L:181,228,257-262,432,568,738-763`. Defectos D-F3B-29, D-NOR-9. Confianza Alta.

### 8.4 FM-ANU-01..07: `AnulacionSolicitudAporteRescateFolioContrato` (AF) y `AnulacionSolicitudAporteRescate` (A, resumen)

**FM-ANU-01 Contrato de AF.** Given `{p_IdEmpresa,p_IdUsuario,p_IdModulo,p_Solicitudes:[{FolioContrato (def 0),IdSolicitudAporteRescate,IdCliente,Estado}]}`. When entra por `call` (`new Gson()`). Then devuelve el contador, los mensajes y `P_SOLICITUDES` de `AnulacionSolicitudAporteRescate`. Sin requisitos basicos propios (los hace A). Fuente `AF:31-116`. Confianza Alta.

**FM-ANU-02 Separacion por folio.** Given solicitudes con `FolioContrato = 0`. When pasan directo a `AnulacionSolicitudAporteRescate` con su `IdSolicitudAporteRescate` y `Estado`. Given `FolioContrato != 0`. When se acumulan folios e `IdCliente` en dos listas paralelas. Fuente `AF:131-141`. Confianza Alta.

**FM-ANU-03 Resolucion de folios a solicitudes.** Given listas de folios y clientes. When `SELECT IdSolicitudAporteRescate, FolioContrato FROM FMM.SolicitudAporteRescate WHERE IdCliente IN (...) AND FolioContrato IN (...)`; por cada fila toma el `Estado` de la **primera** entrada con el mismo folio. Then el filtro es producto cruzado (cliente A con folio de cliente B coinciden) y una fila sin entrada con su folio queda con `Estado` nulo. Ej: entradas {folio 55, cliente 9, `ANU`} y {folio 60, cliente 8, `ANU`}: la fila (cliente 9, folio 60) tambien se selecciona. Fuente `AF:143-159`; SQL `AnulacionSolicitudAporteRescateFolioContrato_SelectSolicitudes`. Defectos D-F3B-27. Experto: si (QD-F3B-13). Confianza Alta.

**FM-ANU-04 Delegacion y contador.** Given la lista combinada. When llama `AnulacionSolicitudAporteRescate.logica`. Then copia contador, mensajes y `P_SOLICITUDES`. Fuente `AF:161-167`. Confianza Alta.

**FM-ANU-05 Traspasos de egreso APV asociados.** Given contador 0. When lee `Traspaso` (por `IdCliente` y `FolioContrato`, con `Solicitud.IdTipoMovimientoFondoMutuo='RESTRASP'`, uniendo `TRA.Traspaso.IdTraspaso = FolioContrato`) y por cada fila y cada entrada con el mismo folio agrega `CambiarEstadoTraspaso{IdTraspaso, IdEmpresa, IdEstado = Estado de la entrada}` **reusando el mismo objeto** (todas las filas de la lista son iguales a la ultima). Then `UPDATE APV TRA.Traspaso SET IdEstado = :IdEstado WHERE IdEmpresa AND IdTraspaso` en lote. Con varios traspasos solo el ultimo queda realmente actualizado (N veces). Ej: traspasos 700 y 701 con estado `ANU` -> se actualiza 701 dos veces y 700 nunca. Fuente `AF:169-194`; SQL `ConsultaTraspasoEgresoAPVAsociado`, `ActualizarTraspasoEgresoAPVAsociado`. Defectos D-F3B-27. Experto: si (QD-F3B-13). Confianza Alta.

**FM-ANU-06 Lo que hace `AnulacionSolicitudAporteRescate` (A) (resumen; no se extrae).** Given estados nuevos `ANU` o `ING` por solicitud. When: (1) `RevisarRequisitosBasicos`, lista vacia, ids repetidos; (2) lee solicitudes y relacionadas no anuladas y propaga el estado nuevo (aporte de un rescate -> `ANU`); (3) rechaza con codigos `UI_APORTE_RESCATE_ANULADA`, `UI_OPERACION_NO_DESMARCAR`, `UI_APORTE_DESMARCAR_PENDIENTE`, `UI_APORTE_RESCATE_INGRESADO`, `FOLIO_COMPROBANTE_LIQUIDACION_ASOCIADO`, `APORTE_CON_DOCUMENTO_ASOCIADO`, `RESCATE_CON_DOCUMENTO_ASOCIADO`, `APORTE_SIN_CARTERA`, `ANULACION_OMNIBUS_CREADO` (el contador es la cantidad de rechazos y sale sin escribir); (4) revierte cuenta de inversion (abono/cargo con concepto `ANUAPO`), custodia (retiro de la cartera del aporte; reingreso de las carteras del rescate), Spot (`AnularOperacionSpot`), `UPDATE Estado, EstadoLiquidacion='ING', FolioCartaLiquidacion=0, FechaAnulacion`, AGF, log `ANULACION FONDO MUTUO` y OWP. Sus propios `main()` y literales no se copian. Fuente `A:224-750,764-957`. Defectos D-F3B-01 (siempre `"N"`), D-F3B-29. Confianza Media (resumen, no linea a linea).

**FM-ANU-07 Estado de porte.** Given `modernized/.../service/javacall/AnulacionSolicitudAporteRescateService.java` (103 LOC) y `...FolioContratoService.java` (77). When se leen. Then **no son portes**: leen los mismos SQL, devuelven filas crudas (`P_OUT_SOLICITUDES`, `P_OUT_CARTERA_APORTES`, `P_OUT_CARTERA_RESCATES`) y ejecutan `batchUpdate` con solo `IdSolicitudAporteRescate` e `IdEmpresa` contra un SQL que exige `IdEstadoNuevo` y `FechaAnulacion`; no validan, no tocan custodia, cuenta de inversion ni Spot, no sacan el contador. Estan cableados en `InternalJavaCallController.java:489-491`. Fuente moderna citada. Defectos D-F3B-28. Confianza Alta (lectura); no ejecutado.

## 9. Defectos (se mantienen bug a bug; reusados por id; nuevos D-F3B-nn)

Reusados sin cambio de lectura: D-NOR-9 (`List2Hash`/`toMap` ante claves repetidas y NPE; sitios en FM-CER-71), D-NOR-10 (estado estatico de `ConfiguracionSistema`), D-NOR-11 (`printStackTrace`, `C:3017`), D-NOR-16 (pares portafolio/moneda omitidos en el flujo de caja, `C:1176-1183,1254-1261`), D-NOR-19, D-REQ-11, D-FEC-6, D-CUO-1..3, D-COM-1..6, D-MAN-8, D-TES-1/2, D-F2-4/11/12/13/20/21 (lado AR).

| Id | Defecto | Evidencia | Efecto |
|---|---|---|---|
| D-F3B-01 | "Simulacion" = ejecucion real + rollback del anfitrion: todos los colaboradores reciben `"N"`; solo `P_OUT_ERRORESCONTADOR=1` al final | `C:862-864,1731,1919,2047,2301,2333,2543` | si el anfitrion no revierte (llamada en proceso, job con `""`), la simulacion deja efectos; cada colaborador debe poder correr sin commit |
| D-F3B-02 | Notificacion asincrona (`CallBatchAsync`) y OWP salen antes del commit y tambien en simulacion o con contador > 0 posterior | `C:836,848-850,2760` | notificaciones de certificaciones revertidas; el lote puede leer el estado previo |
| D-F3B-03 | Rechazos de Tesoreria y de Spot no suben el contador (el de Tesoreria no se lee): commit con efectos huerfanos (custodia, cuenta, facturacion) en solicitudes que quedan sin certificar | `C:698-721,790-830` | estado inconsistente por solicitud; Media-Alta (no se leyeron los contadores internos) |
| D-F3B-04 | `creaComprobanteFacturacion`: se suma el contador de `salidaCrearMovimientoComprobante` en vez del de `CrearComprobante` | `C:744` | doble conteo del movimiento; errores de comprobante no cuentan |
| D-F3B-05 | `custodia == null` entra a la rama de rechazo y lanza NPE en `custodia.CantidadDisponible` | `C:1331,1335` | cualquier rescate sin resultado de custodia aborta todo el lote |
| D-F3B-06 | Uso de la misma `Connection` JDBC desde `parallelStream` (descripcion de mensajes) | `C:1325-1365,1582-1607` | riesgo de fallos intermitentes |
| D-F3B-07 | `fechaSistemaDate.compareTo(FechaLiquidacion) > 1` nunca es cierto (`compareTo` da -1/0/1): `FechaEmision` de Tesoreria siempre es `FechaLiquidacion` | `C:1967,2002,2027` | la condicion `max(sistema, liquidacion)` de los cargos de cuenta no se replica aqui |
| D-F3B-08 | `personaSII` nula no se valida para `APV-AN` (solo `APV-B`) y Tesoreria la usa | `C:522-526,2005` | NPE tardia, despues de escribir custodia y cuenta |
| D-F3B-09 | Retencion APV calculada sobre el monto bruto (antes de comision) y descontada en Tesoreria de `MontoPagoLocal` usando `MontoRetencion` (moneda del fondo) y no `MontoRetencionLocal`; mismo monto para el pago al SII | `C:588,1415-1416,1972-1973,2004` | error de moneda en fondos no CLP |
| D-F3B-10 | `reCalcularCostoUFAportes`: un solo objeto `CalculoEntrada` agregado N veces; `findAny().get()`; `findFirst().get()`; comparacion `!= BigDecimal.ZERO` por referencia; literales de regimenes; todo en `try/catch` con `printStackTrace` que traga el error | `C:2939-3019` | costo UF distinto del esperado sin aviso |
| D-F3B-11 | `calcularCostoUFRentabilidadRescates` reusa un unico `CalculoEntrada` para todas las fechas (todas las entradas quedan iguales a la ultima; `List2Hash` por `IdInterno` puede lanzar); recorre rescates rechazados; `CostoUF` previo nulo -> NPE. Ademas la validacion de saldo esta duplicada (PAC / no PAC) | `C:1527-1576,1149-1303` | rentabilidad/spread erroneos con varias fechas de certificacion |
| D-F3B-12 | `SelectSolicitudes` hace LEFT JOIN a `FMM.Comision (PER)`, `OrdenTraspaso`/`Traspaso`, `RESCNN` relacionados y `Emisor`: una fila por coincidencia, sin dedupe | SQL `e0ae667cab0ac5ec`; `C:416` | solicitudes duplicadas (procesadas dos veces) o `List2Hash` lanza; depende de los datos |
| D-F3B-13 | `crearAporteReinversion`: el "ultimo aporte" es el primero (`get(0)`); lista vacia -> `IndexOutOfBounds`; ajuste doble (por destino y por rescate); orden por `HashMap` | `C:2449-2460,2524-2534` | residuo de redondeo en el aporte equivocado |
| D-F3B-14 | Mensajes de Spot con `id == 0` rechazan la primera solicitud de la lista; `salidaSpot != null` siempre cierto | `C:819,796` | rechazo de un rescate ajeno |
| D-F3B-15 | `ResumenPatrimonial.AporteRescate` solo importado; el plan lo cuenta como colaborador HTTP | `C:67` | discrepancia plan/codigo: no hay compuerta de Normativa que cerrar |
| D-F3B-16 | Solicitud con `TipoSolicitud` distinto de `APO`/`RES` (o no encontrada en ninguna lista) se marca exitosa y se actualiza a `CRT` sin efecto; el `UPDATE` no tiene guarda de estado | `C:549-550,835,2873-2880`; SQL | certificacion silenciosa |
| D-F3B-17 | Cuenta de inversion de rescates sin `MueveCta` ni origen manual (los aportes si los exigen); asimetria | `C:1827-1828` vs `C:1756-1757` | movimientos de cuenta en rescates por otros medios de pago |
| D-F3B-18 | Liquidacion lee `APV.Habilitado` con empresa 1 fija | `L:245-246` | ignora la empresa de la entrada |
| D-F3B-19 | Liquidacion: `logList` nunca se asigna a `entradaAgregarLog.p_Log` (log vacio) | `L:693-732` | sin trazabilidad |
| D-F3B-20 | Liquidacion: lineas de cliente mapean `carta_solicitud` a la lista completa; el folio final es el de la ultima referencia | `L:515,522,529` | folio de carta de solicitudes APV pisado |
| D-F3B-21 | Liquidacion: `ie.IdTipoMovimiento.equals("APO")` nunca es cierto: cargo/abono de cuenta siempre `MOVCAR`/`CAPEFM`/`RESCFM` (o `ABPEFM` por operacion) | `L:600-609` | tipo de movimiento de cuenta fijo |
| D-F3B-22 | Liquidacion: `NO_SE_INGRESARON_FILTROS` no corta; sin filtros ni movimientos `NoSuchElementException`; solo se usa el primer filtro; `0001-01-01` como "vacio" | `L:232-266` | procesa y responde error |
| D-F3B-23 | Liquidacion: aportes sin omnibus desaparecen sin mensaje (rama de mensaje muerta) | `L:269-289` | solicitudes no liquidadas sin aviso |
| D-F3B-24 | Liquidacion: la simulacion solo llega a Tesoreria; UPDATE y cargo/abono corren; el contador no se fuerza; al devolver error de Tesoreria se pierden los mensajes acumulados | `L:547,562,575,642` | un /call de simulacion sin errores hace commit |
| D-F3B-25 | Liquidacion: `Estado` del resultado es el leido (`ING`), no `LIQ` | `L:583,677` | UI ve estado viejo |
| D-F3B-26 | Liquidacion: cargo/abono N x M y reinicio de `K` en cada movimiento | `L:591-682` | movimientos de cuenta repetidos |
| D-F3B-27 | Anulacion por folio: listas paralelas de folio/cliente (producto cruzado) y `CambiarEstadoTraspaso` reusado N veces | `AF:131-190`; SQL | traspaso incorrecto actualizado |
| D-F3B-28 | Servicios modernos de las 5 clases son andamios no equivalentes, con UPDATE incompleto y cableados en el router | `modernized/.../service/javacall/*Service.java`; `InternalJavaCallController.java:489-531` | riesgo si se ruteara trafico real; no se ejecuto |
| D-F3B-29 | `main()` de las 5 clases y del job con alias de conexion de QA/DESA/PRESURA, JSON con ids reales de solicitud/cliente y `commit` indistinto del resultado | `C:3022-3052`, `X:133-152`, `L:738-763`, `AF:202-222`, `A:959-980` | no se portan ni se copian |
| D-F3B-30 | `res.Switch.equals("S")` con `Switch` nulo; aportes switch rechazados por AR igual se certifican | `C:779-787` | NPE o certificacion de aporte rechazado |
| D-F3B-31 | Mensajes y literales con errata (`UI_ONMIBUS_NO_CREADO`) y codigos de negocio fijos en el codigo | FM-CER-73 | contrato visible; no corregir |
| D-F3B-32 | Orden de iteracion por `HashMap` (agrupacion y reinversion) fija correlativos y folios | `L:297-308`, `C:2361` | resultados sensibles al orden de claves |

## 10. Aspectos regulatorios y de dominio (se senalan al experto; no se decide nada)

- QD-F3B-01 Validaciones previas a certificar: liquidacion previa del aporte (`VALIDAR_CERTIFICACION`) y certificacion y liquidacion del rescate asociado antes del aporte de reinversion (FM-CER-11/12).
- QD-F3B-02 Saldo suficiente de la cuenta de inversion al certificar aportes, indicadores `OperaLineaSobregiro` y `GEN_OCSNEG` (CELLINCECT-763), canal PAC con flujo distinto (FM-CER-20..22).
- QD-F3B-03 Costo UF en reinversion: herencia segun regimen (`APV-A`, `BF`, `DC-*`, `18Q` con MLT) y el `distinct()` de montos (FM-CER-24..26).
- QD-F3B-04 Rescate con custodia insuficiente: se certifica por lo disponible con advertencia, no se rechaza (FM-CER-32).
- QD-F3B-05 Venta de dolares al certificar rescates (`RESCATE_GENERA_VENTA_SPOT_DOLAR`): moneda USD pagada en CLP, `RESCNN` (FM-CER-34, FM-CER-59).
- QD-F3B-06 Retencion en rescates APV-B y APV-AN, pago al SII y bonificacion fiscal a la TGR; porcentaje, base (bruto antes de comision) y moneda (FM-CER-36, 45, 46; FM-LIQ-11).
- QD-F3B-07 Spread y rentabilidad real de rescates frente a UTM (`valorUTM` por defecto 1) (FM-CER-41).
- QD-F3B-08 Cuenta de inversion de rescates sin `MueveCta` y cargo de comision solo para `RESREINV`/`RESCOM` (FM-CER-42).
- QD-F3B-09 IVA de la comision, neto y comprobante automatico solo para `RESNORM`/`RESREINV` (FM-CER-48..51).
- QD-F3B-10 Reparticion de reinversion por porcentaje y cantidad de cuotas retiradas; residuo al primer aporte (FM-CER-53..55).
- QD-F3B-11 Liquidacion: movimientos AGF contra cuenta del cliente, retencion APV-B, `APV.Habilitado` con empresa fija (FM-LIQ-02, 09..12, 15).
- QD-F3B-12 Liquidacion no exige certificacion previa por el camino de movimientos (FM-LIQ-04, 05).
- QD-F3B-13 Anulacion por folio y estado de los traspasos de egreso APV (FM-ANU-03, 05).
- QD-F3B-14 Notificacion al cliente (`CERTIFICACION_FFMM`) y a OWP en simulacion o revertida (FM-CER-62, 65).

### 10b. Parametros de configuracion (nombre y valor actual en el codigo)

| Parametro | Origen | Defecto | Uso |
|---|---|---|---|
| `PLATAFORMA/PLA.PaisSistema`, `PLA.FechaSistema`, `PLA.NotificarEstadoOrdenOWP`, `PLA.CallBatchURL`, `PLA.CallBatchToken` (el token es una credencial: `<credential - masked, ver NotificarEstadoOrdenFondoMutuo.logicaAsync>`) | `ConfiguracionSistema` | sin defecto (lanza) | fechas, pais, notificaciones |
| `FONDOMUTUO/TipoComprobanteFacturaNoApv`, `TipoComprobanteFacturaApv`, `CALCULAR_COMISION_CERTIFICACION`, `VALIDAR_CERTIFICACION`, `RESCATE_GENERA_VENTA_SPOT_DOLAR`, `RESCATE_COMISION_COMPROBANTE_AUTOMATICO`, `CONCEPTO_RETENCION_PAGO_SII` | idem | sin defecto (lanza) | FM-CER-37,48,59 |
| `PLATAFORMA/APV.PorcentajeRetencion` | idem | 0 | FM-CER-36 |
| `PLATAFORMA/APV.DocumentoIdentidadPersonaTGR`, `...SII` | idem | `""` | FM-CER-13 |
| `FONDOMUTUO/LiquidacionCuentaInversionCliente`, `VALIDAR_LIQUIDACION_OMNIBUS`, `PLATAFORMA/APV.Habilitado` (empresa 1) | idem | sin defecto | FM-LIQ-02 |
| `COM.Cobro` `IVA` (`ValorCobro`) | tabla | sin defecto | FM-CER-49 |
| Literales fijos | codigo | ver FM-CER-73 | |

## 11. Matriz de casos propuesta para el oraculo (ids de golden `FM-CER-Cnn_slug`, `FM-CXF-Cnn`, `FM-LIQ-Cnn`, `FM-ANU-Cnn`)

Reglas generales: reloj fijo, `PLA.FechaSistema` sembrada (ej. 2026-03-10), UTC, N = 20 corridas por caso (orden de `HashMap`/`parallelStream`), `expected.json` solo grabado ejecutando el legacy compilado sin modificar sobre H2, dobles registrados para `SaldoFlujoCaja`, `CalcularParidad`, `EntregarPreciosTasas`, `AgregarLog`, `CrearOperacionSpot`, `CrearMovimientoComprobante`, `CrearComprobante`, `Notificar*`, `CallBatchAsync`; `CrearIngresoEgreso` y `CrearCargoAbono` doblados en la capa 1 y reales en la capa 2. Cada caso se corre con simulacion `S` y `N` cuando aplica (D-F3B-01). Los 5 `main()` no se copian.

### 11.1 Certificar (`FM-CER-C01..C52`)
| Id | Caso | Pina |
|---|---|---|
| C01 | lista vacia; duplicado; requisitos con error | 02 |
| C02 | parametro obligatorio faltante; opcionales ausentes | 03 |
| C03 | un rescate RESNORM CIN normal, sin comision: certificado | 04..08,16..19,31,39..43,61,67 |
| C04 | una validacion por regla, una solicitud por caso (estado, fecha, valor cuota x3, omnibus, comprobante, liquidacion, asociada, TGR, SII) | 06..13 |
| C05 | lote mixto (1 ok, 1 rechazada): contador 0 y rechazada solo en respuesta | 14,69 |
| C06 | ningun valido: contador 1 y log | 14 |
| C07 | `TieneComisionNNRelacionada=S`: montos no se actualizan | 17 |
| C08 | `ERROR_CALCULO_MONTO_CUOTA` | 16 |
| C09 | fecha efectiva mayor/menor que fecha de sistema | 18 |
| C10 | `TipoSolicitud` ajeno: exitosa sin efecto | 19, D-F3B-16 |
| C11 | `OperaSaldoNegativo` S/N/ausente | 20 |
| C12 | aporte no PAC con saldo insuficiente (2 aportes, uno basta, ambos rechazados) | 21 |
| C13 | aporte PAC con saldo suficiente / insuficiente | 22 |
| C14 | par portafolio/moneda omitido por listas independientes | 21, D-NOR-16 |
| C15 | formato de montos por moneda | 23 |
| C16 | aporte APOREINV BF / MLT 18Q / otro con paridad UF; error tragado | 24..26 |
| C17 | custodia de aporte con error | 27 |
| C18 | cuenta de inversion de aporte normal / diferido / Switch | 29,30 |
| C19 | rescate con custodia suficiente / ADV / RZD / sin resultado (NPE) / omnibus | 31..33 |
| C20 | GeneraSpot S/N (USD pagada en CLP, RESCNN) | 34 |
| C21 | rescate con traspaso | 35 |
| C22 | retencion APV-B y APV-AN, P=0 y P=15 | 36,38 |
| C23 | comision S/N, con y sin NN | 37 |
| C24 | retiro de custodia: con y sin regimen; sin cartera | 39,40 |
| C25 | costo UF, spread y rentabilidad (una fecha; dos fechas -> D-F3B-11) | 41 |
| C26 | cuenta de inversion de rescate (normal, diferido, RESREINV con comision) | 42,43 |
| C27 | Tesoreria: CIN, no CIN, MLT, RESREINV, traspaso, BF, APV-B, APV-AN sin SII | 44..46, D-F3B-07/08/09 |
| C28 | Tesoreria RECH: solicitud rechazada con efectos previos (D-F3B-03) | 47 |
| C29 | facturacion RESNORM/RESCOM/RESTRAPOR, IVA 19, comision con y sin escala | 48..50 |
| C30 | comprobante automatico S/N y contador (D-F3B-04) | 51 |
| C31 | reinversion misma familia: 60/40 cuotas; 3 destinos; sin movimientos | 52..55 |
| C32 | reinversion otra familia/moneda; fechas Spot | 54,56 |
| C33 | llamada a AR: simulacion N siempre, canal AGE, mapeo de ids, errores | 57 |
| C34 | aporte switch certificado por recursion; Switch nulo | 58,63 |
| C35 | Spot de rescate USD/CLP, `RECH`, error id 0 | 59,60 |
| C36 | UPDATE de CRT: columnas y COALESCE | 61 |
| C37 | monto en moneda de sistema en reinversion internacional | 64 |
| C38 | notificacion OWP S/N y asincrona | 62,65 |
| C39 | log de sistema y de certificacion | 66 |
| C40 | simulacion S: contador 1 con todo escrito | 68, D-F3B-01 |
| C41 | excepciones: NPE catalogados (uno por caso) | 71 |
| C42 | `SelectSolicitudes` con filas duplicadas | D-F3B-12 |
| C43 | concurrencia: N corridas del caso C19 | 72 |
| C44..C52 | variantes con orden de ids invertido (comparador) y particion | 19 |

### 11.2 CertificarPorFiltros (`FM-CXF-C01..C06`)
| Id | Caso | Pina |
|---|---|---|
| C01 | un filtro vacio con fecha | 02 |
| C02 | filtro con `IdCarga` y detalle `PEN`; `IdCarga` nulo | 02 |
| C03 | dos filtros que repiten un id | 03 |
| C04 | sin filtros | 04 |
| C05 | `Nacional`/`MoneyMarket` N y S | 02 |
| C06 | listas del job con largos distintos | 06 |

### 11.3 Liquidacion (`FM-LIQ-C01..C20`)
| Id | Caso | Pina |
|---|---|---|
| C01 | aporte unico por movimientos | 01,04,07,09,13,14 |
| C02 | dos aportes mismo grupo (350.000) | 07,09 |
| C03 | rescate CRT por filtros; fecha vacia | 05 |
| C04 | omnibus S con aporte sin omnibus | 06 |
| C05 | `APV.Habilitado=S`: lineas de cliente, retencion APV-B | 10,11,12 |
| C06 | medio CIN con cuenta cliente S | 15,16,17 |
| C07 | error de Tesoreria | 13 |
| C08 | error de cuenta de inversion | 16 |
| C09 | filtros vacios con y sin movimientos | 03 |
| C10 | simulacion S | 19 |
| C11 | log vacio | 18 |
| C12..C20 | NPE catalogados | 20 |

### 11.4 Anulacion (`FM-ANU-C01..C07`)
| Id | Caso | Pina |
|---|---|---|
| C01 | folio 0: delega directo | 02 |
| C02 | folio con producto cruzado cliente/folio | 03 |
| C03 | contador 0 y traspasos RESTRASP (2 traspasos) | 05 |
| C04 | contador > 0: no toca traspasos | 05 |
| C05 | rechazos de A (9 codigos) | 06 |
| C06 | anulacion completa de aporte certificado | 06 |
| C07 | contrato del servicio moderno frente al legacy | 07 |

## 12. Particion propuesta de Certificar en frentes cortos (propuesta; no decide)

Cada frente conserva el orden de comprobaciones y escribe su golden antes de pasar al siguiente (R-F3-2).

| Frente | Contenido | Tarjetas | Colaboradores/compuertas |
|---|---|---|---|
| F-A Cabecera y validacion | contrato, requisitos, parametros, lecturas, valor cuota, 8 reglas por solicitud, salida temprana | FM-CER-01..14 | kit |
| F-B Montos y reglas puras | indicadores, recalculo, particion/orden, formato | 15..20, 23 | cierre F1 (FM-CUO) |
| F-C Aportes | saldo (PAC / no PAC), costo UF, custodia ingreso, rentabilidad | 21,22,24..28 | G3, custodia |
| F-D Rescates: custodia y costos | custodia disponible, GeneraSpot, traspaso, retencion, comision, retiro, costo UF/spread | 31..41 | custodia, FM-COM |
| F-E Cuenta de inversion | aportes y rescates (constructor de lineas) | 29,30,42,43 | `CrearCargoAbono` (L) |
| F-F Tesoreria | seleccion, retencion SII/TGR, traspasos, lectura | 44..47 | G1, FM-TES |
| F-G Facturacion | movimiento, IVA, comprobante | 48..51 | G6 |
| F-H Spot | construccion y lectura | 59,60 | G2, FM-SPO |
| F-I Reinversion y switch | `crearAporteReinversion`, llamada a AR, aportes switch, recursion | 52..58,63,64 | FM-REI-41..68 |
| F-J Cierre | UPDATE, notificaciones, log, salida, simulacion | 61,62,65..70 | G5, G8 |
| F-K Liquidacion | FM-LIQ-01..20 | 01..20 | G1, `CrearCargoAbono` |
| F-L Anulacion | A y AF (extraccion pendiente de A) | FM-ANU-01..07 | custodia, Spot |
| F-M CertificarPorFiltros | delegada | FM-CXF-01..06 | |
Orden sugerido: F-A, F-B, F-C, F-D, F-E, F-F, F-H, F-G, F-I, F-J (unir F-A..F-J detras de una fachada que preserve el orden de la seccion 7), luego F-K, F-L, F-M.

## 13. Preguntas abiertas (Q-W0B-n)

1. **Q-W0B-1:** ¿que cuenta con un `/call` de Certificar con `p_SimularOperacion="S"` que dispara notificacion asincrona y OWP antes de revertir? (D-F3B-02).
2. **Q-W0B-2:** ¿la conducta "rechazo de Tesoreria/Spot sin subir contador" (commit con custodia/cuenta ya escritas) es vigente o un defecto a corregir? (D-F3B-03).
3. **Q-W0B-3:** ¿`distinct()` de `MontoMonedaSistema` al sumar aportes de reinversion es intencional? (FM-CER-25).
4. **Q-W0B-4:** ¿por que AR decide la herencia del costo UF por `CodigoPortafolio` y Certificar por `IdRegimenTributario`? (FM-CER-26).
5. **Q-W0B-5:** ¿Facturacion (`CrearMovimientoComprobante`, `CrearComprobante`) acepta correr como llamada HTTP no atomica? (D-F3-3, G6).
6. **Q-W0B-6:** ¿Normativa `ResumenPatrimonial` no se invoca: se descarta del plan? (D-F3B-15).
7. **Q-W0B-7:** ¿`MontoRetencion` (moneda del fondo) restado de `MontoPagoLocal` es el calculo esperado en fondos no CLP? (D-F3B-09).
8. **Q-W0B-8:** ¿`APV-AN` deberia validar el SII? (D-F3B-08).
9. **Q-W0B-9:** ¿la vista entrega `IdOmnibus` nulo o 0 sin omnibus? (FM-CER-09).
10. **Q-W0B-10:** `MonedaFormato.formatear` con nulo (FM-CER-23).
11. **Q-W0B-11:** ¿`APV.Habilitado` con empresa 1 fija en Liquidacion es aceptable? (D-F3B-18).
12. **Q-W0B-12:** ¿la fecha `0001-01-01` reemplaza "sin filtro" en Liquidacion por filtros? (FM-LIQ-05).
13. **Q-W0B-13:** ¿el producto cruzado cliente/folio y el objeto de traspaso reutilizado en la anulacion por folio son aceptables? (D-F3B-27).
14. **Q-W0B-14 (catalogo, solo lectura):** definicion de la vista `FMM.ConsultarSolicitudAporteRescate`, columnas de `FMM.SolicitudAporteRescate` y secuencias `FolioCartaLiquidacionAporteRescate`, `SecuenciaComprobanteLiquidacion`, `LogCertificacionAporteRescate`.
15. **Q-W0B-15:** ¿el servicio moderno `*Service` de estas 5 clases esta en trafico real? (D-F3B-28; no se verifico).
16. Heredadas: Q-01 (experto de dominio), Q-W0A-1 (AR "PROVEN-no-activo": ahora la llamada de AR sale de esta clase; migrar Certificar es lo que activa el trafico de 438.870 `APOREINV`).

## 14. Lo que no se pudo comprobar

No se ejecuto nada (ni legacy, ni HANA, ni modernized, ni tests, ni red). Las reglas son lectura de codigo. No se leyeron linea a linea: `CalculaCustodiaRescate`, `ProcesarCustodiaFondoMutuo`, `CalcularMontoNumeroCuota`, `CalcularComisiones`, `ProcesarIngresoEgresoTesoreria`, `ProcesarIngresoOperacionSpot`, `CrearCargoAbono`, `CrearIngresoEgreso`, `CrearMovimientoComprobante`, `CrearComprobante`, `AnularOperacionSpot`, `RetiroCustodia`, `IngresoCustodia`, `SaldoFlujoCaja`, `CalcularParidad`, `EntregarPreciosTasas`, `AgregarEvento`, `MonedaFormato`, `RevisarRequisitosBasicos` (solo sus llamadas y firmas); por eso los contadores internos (D-F3B-03), el comportamiento de `CalcularParidad` con entradas repetidas (D-F3B-10/11) y el de `MonedaFormato` con nulos quedan marcados Media. Tampoco se leyo la definicion de la vista `FMM.ConsultarSolicitudAporteRescate`, ni `BcsJob` (quien hace commit en el job), ni `Utils.CallBatchAsync` mas alla de su firma, ni los ids de registro ni los usos de QA del plan (894, 51, 450, 277: solo citados). El mapeo de nulos de `SQL.query` a los valores por defecto de los pojos (p. ej. `IdOmnibus = 0L`) no se verifico. De `AnulacionSolicitudAporteRescate` solo se leyeron las lineas 1-957 en diagonal (resumen FM-ANU-06). No se verifico si los servicios modernos reciben trafico.


---
**Corrección del 2026-10-26 (orquestador):** D-F3B-28 y Q-W0B-15 dicen que los servicios andamio están "cableados en el router". Verificado después en `BcsFondoMutuoApplication`: el controlador antiguo `adapter.in.web.InternalJavaCallController` y todo `service.*` están **excluidos del contexto de Spring desde F1** (filtro `STUBS`; `StubsFueraDelContextoTest`), por lo que no reciben tráfico ni siquiera con el router apuntado a este módulo. Siguen en el árbol de archivos hasta que se porte cada clase. Q-W0B-2 se resolvió: **se replica tal cual** (usuario, 2026-10-26).
