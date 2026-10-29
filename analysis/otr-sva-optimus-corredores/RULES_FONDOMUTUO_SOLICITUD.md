# Reglas extraidas: FondoMutuo, solicitud de aporte/rescate (paso 1A)

Fecha: 2026-10-22. Paso 1A de `PLAN_FONDOMUTUO_SOLICITUD_APORTE_RESCATE.md` (leido completo, incluida la seccion 12 de decisiones: F1 con compuertas; DDL de sesion `#p_Bloqueo...` aceptado como "no es cambio de BD"; DEV-F1..F7 aprobadas).
Alcance leido linea a linea (legacy de solo lectura, `legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/`): `ManejadorSolicitudAporteRescate` (id 601190), `CrearSolicitudAporteRescateNormal` (id 357) y las 12 clases del cierre FondoMutuo: `CalcularFechaSolicitudAporteRescate`, `CalcularMontoNumeroCuota`, `CalcularComisiones`, `ServicioCumpleRequisitosCrearSolicitudAporteRescate`, `ValidaMontoInversion`, `ValidaPatrimonioEmisor`, `ConsultarFlujoCustodiaFondoMutuo`, `CrearSolicitudAGF`, `CrearSolicitudRescateComisionNN`, `ObtenerPerfilFondoMutuo`, y (diferidas, lectura parcial) `ProcesarIngresoEgresoTesoreria` (:100-325) y `ProcesarIngresoOperacionSpot` (:130-470). Tambien `pojo/ConstantesFondoMutuo.java`, `utils/FondoMutuoUtils.java`, los 36 SQL que cargan estas clases, `BcsUtils/.../Utils.java:768 (List2Hash)`, `PlataformaUtils.java:598 (MensajeSalida)`, `RevisarRequisitosBasicos.java` y `CallServlet.java:433-499`.
No se toco codigo, ni legacy, ni HANA. Nada de lo de aqui es veredicto de equivalencia: son reglas observadas en el codigo para convertir en casos `FM-*` del oraculo.

Convenciones. Cita `Clase:linea` (archivo `.java` del paquete `cl.bcs.fondomutuo` salvo que se indique). Alcance: **F1** = se porta en F1; **F1-G{n}** = se porta pero la rama queda detras de la compuerta Gn (503 -> TomEE); **F2** = diferida. "Experto" = requiere confirmacion de dominio/regulatoria (ver seccion 7). Los defectos `D-xxx` se mantienen bug a bug (decision vigente) salvo que el usuario diga otra cosa; los ids son provisorios.

## 1. Resumen ejecutivo

1. **Lizard SI esta disponible** (`lizard 1.24.2` en `...\Python314\Scripts`). Mide CCN real; reemplaza el conteo manual del plan (seccion 4). El conteo manual del plan **subestimaba**: Normal 294 decisiones (CCN total 314; la funcion `logica` sola tiene CCN 286, no 241) y el cierre de 12 clases suma **604** decisiones, no 447.
2. **Los 752 SQL "modernos" son 376 reales duplicados** (`src/main/resources/sql` = 376 y `target/classes/sql` = 376, artefacto de build). Comparacion SHA-256 byte a byte contra el legacy: ver seccion 5. **Ninguno de los 36 SQL del alcance difiere.**
3. **Nombres de tablas:** el catalogo local (`hana_qa_results/cat_*.csv`) solo cubre 7 de los 46 objetos HANA que tocan estas clases; el resto necesita la consulta de catalogo que el usuario ya autorizo (seccion 6, lista exacta).
4. **El plan omitio un colaborador:** `ServicioCumpleRequisitosCrearSolicitudAporteRescate` llama a `cl.bcs.plataforma.com.ConsultarProductosPortafolio` (:190-191, :471; 2 SQL `ConsultarProductosPortafolio_*` de Plataforma) y consulta `ACC.Usuario`. Hay que agregarlo a la tabla 5.2 del plan (lectura; destino sugerido K, kit de lectura).
5. **El plan hablaba de "11 SQL Comunes*":** lo real que cargan estas clases son 13 SQL compartidos: 9 `ComunesSolicitudes_*` (SerieFondo, Portafolio, PortafolioAPV, RegimenSerieDeFondo, IndicadoresPortafolio, Moneda, ConfiguracionPropositoPortafolio, InsertSolicitud, ModalidadPago [solo Tesoreria, diferida]), 3 `Comunes_*` (SelectTipoMovimientoFondoMutuo, SelectTramoComision, SelectPerfilFondoMutuo) y `CertificarSolicitudAporteRescate_SelectSolicitudes`. Ademas 23 SQL propios de las clases (Normal 9, CalcularFecha 2, CalcularMonto 1, Cumple 1, FlujoCustodia 2, Patrimonio 4, AGF 4). Total 36.
6. **Defectos nuevos relevantes (detalle en seccion 8):** D-REQ-1 (el filtro por descripcion de `ServicioCumple...` elimina los errores de las solicitudes siguientes que repiten texto: pasan validaciones que fallan), D-MAN-3 corregido (`Utils.List2Hash` LANZA excepcion ante `IdInterno` repetido: la comision NN colisiona con IdInterno 1..n de la peticion), D-MAN-5 (los mensajes de la pasada real se pierden: el bloqueo anti-duplicado llega al cliente como contador 1 sin mensaje), D-NOR-14 (comision NN tambien para solicitudes rechazadas), D-NOR-15 (rescates de una misma peticion no descuentan custodia entre si), D-NOR-16 (consulta de flujo de caja omite pares portafolio/moneda), D-NOR-17 (MontoPago negativo con comision trimestral), D-COM-1/3/4 (EsTraspaso fijo "N"; cuenta regresiva de carteras; cantidad comision parcial), D-REQ-6 (tope maximo total 0 rechaza todo aporte), D-SQL-1 (JOIN de `SelectSerieFondo` mal escrito) y D-NOR-4 (confirmado, con impacto en jobs PAC: ver D-NOR-4).
7. Tarjetas: ver conteo en la seccion 9.

## 2. Alcance F1 vs diferido (resumen)

| Pieza | F1 | Nota |
|---|---|---|
| Manejador: despacho solo lista Normal; otras 7 listas -> 503 (G0) | si | MAN-01..12; compuerta MAN-11 |
| Normal: validaciones, fechas, cuotas, comisiones, requisitos, custodia, bloqueo, INSERT, AGF, log, batch, NN | si | NOR-01..52 |
| Aportes con medio de pago distinto de CIN (Tesoreria `CrearIngresoEgreso`) | no (G1) | NOR-38, 39, 90, 91 |
| Aportes con `GeneraSpot=S` / `p_FormaPagoSpot` con filas (Spot) | no (G2) | NOR-38, 40, 92..94 (solo aportes) |
| Aportes CIN con origen manual (`SaldoFlujoCaja`) | G3 hasta que Plataforma lo exponga | NOR-33, 37 |
| Canal OWP con `PLA.NotificarEstadoOrdenOWP=S` | no (G4) | NOR-47 |
| `ProcesarIngresoEgresoTesoreria` / `ProcesarIngresoOperacionSpot` | F2 (reglas solo anotadas) | NOR-90..95 |
| Cierre puro: fechas, cuotas, comisiones, requisitos, monto inversion, patrimonio emisor, flujo custodia, AGF, NN, perfil | si | FEC/CUO/COM/REQ |

## 3. Colaboradores observados en el codigo (confirmacion y correcciones al plan 5.2)

- Confirmados con cita: `RevisarRequisitosBasicos` (Normal:309-315; NN:131-137), `ConfiguracionSistema` (Normal:323,395,398,1667,1744), `ConsultarEmpresa` (Normal:392), `FechaDB` (Normal:738-740), `ObtenerDiaAnterior/ObtenerDiaSiguiente/ObtenerDiaHabilMultiplesPaises` (CalcularFecha), `CalcularParidad` (Normal:651-676, CUO:241, COM:319, Patrimonio:164), `EntregarPreciosTasas` (CUO:483, COM:542), `ConsultarFlujoCustodia` (COM:191; ConsultarFlujoCustodiaFondoMutuo:181), `SaldoFlujoCaja` (Normal:1168-1206), `AgregarLog` (Normal:1602; NN:609), `InscribirProcesoBatch` (Normal:1564), `NotificarEstadoOrdenesOWP` (Normal:1682-1706), `CrearIngresoEgreso` (Tesoreria:141,317), `CrearOperacionSpot`/`ConsultarPuntasPorTramo`/`ConsultarDatosInstrumentos` (Spot:160,236,266), `MonedaFormato` (Normal:891; CUO:327 ...), `Diccionario.getMensaje` (via `MensajeSalida`, PlataformaUtils:625).
- **Nuevo (no estaba):** `cl.bcs.plataforma.com.ConsultarProductosPortafolio` (Cumple:190-191,471) y `MonedaFormato`, `Fecha`, `Utils` (BcsUtils). Hay que sumar `MonedaFormato` al kit: formatea montos por moneda (decimales) y se usa en CUO:327-345,411-436, Flujo:290, Normal:891.
- `CrearOrdenes` y `cuentaInversion.*`: sin llamada directa (coincide con el plan). `ConsultarFechaSolicitudAporteRescate` (id 3022) usa `CrearSolicitudAporteRescateNormal_SelectDiaEsFeriado.sql`; ese SQL no lo carga Normal ni el cierre (confirma "5 SQL sin uso" del plan para estas clases; solo ese 1 de los 5 si lo usa otra clase).

## 4. Complejidad ciclomatica real (lizard 1.24.2, `lizard -l java`, sin instalar nada)

`lizard` ya estaba instalado (`C:\Users\m.villagran.mora\AppData\Local\Programs\Python\Python314\Scripts\lizard`; pip no hizo falta, no se probo la red). Se ejecuto sobre los 14 archivos. "Decisiones" = suma de CCN por funcion menos el numero de funciones (equivale a puntos de decision `if|for|while|case|catch|&&|\|\||?:`). La CCN de la mayor funcion (`logica`) es la que dimensiona el caso de prueba.

| Clase | LOC bruto | NLOC | Funciones | Suma CCN | Decisiones | CCN max (funcion) | Plan (manual) |
|---|---:|---:|---:|---:|---:|---:|---:|
| ManejadorSolicitudAporteRescate | 337 | 245 | 7 | 29 | 22 | 22 | 22 |
| CrearSolicitudAporteRescateNormal | 1.789 | 1.359 | 20 | 314 | 294 | **286** (`logica`) | 241 |
| CalcularFechaSolicitudAporteRescate | 1.519 | 1.108 | 81 | 273 | 192 | 41 (`copiarObjeto`) | 118 |
| CrearSolicitudRescateComisionNN | 634 | 479 | 6 | 67 | 61 | 62 | 49 |
| CalcularComisiones | 600 | 409 | 16 | 72 | 56 | 53 | 46 |
| CalcularMontoNumeroCuota | 552 | 351 | 14 | 74 | 60 | 54 | 43 |
| ServicioCumpleRequisitos... | 529 | 333 | 11 | 99 | 88 | 88 | 88 |
| ProcesarIngresoOperacionSpot (G2) | 500 | 338 | 20 | 55 | 35 | 27 | 24 |
| ConsultarFlujoCustodiaFondoMutuo | 428 | 276 | 22 | 44 | 22 | 16 | 21 |
| ProcesarIngresoEgresoTesoreria (G1) | 370 | 219 | 10 | 50 | 40 | 40 | 12 |
| CrearSolicitudAGF | 370 | 180 | 14 | 31 | 17 | 18 | 16 |
| ValidaMontoInversion | 344 | 234 | 28 | 45 | 17 | 8 | 17 |
| ValidaPatrimonioEmisor | 233 | 129 | 10 | 25 | 15 | 16 | 12 |
| ObtenerPerfilFondoMutuo | 74 | 31 | 5 | 6 | 1 | 2 | 1 |
| **Cierre FondoMutuo (12 clases)** | 6.153 | 4.087 | 237 | 841 | **604** | | 447 |
| **F1 portado (Manejador + Normal + 10 del cierre, sin Spot ni Tesoreria)** | | | | | **845** | | |

Lectura: el plan subestimo el cierre (+35 %) y `CalcularFechaSolicitudAporteRescate` (192 vs 118) y `ProcesarIngresoEgresoTesoreria` (40 vs 12). `Normal.logica` tiene CCN 286: es un metodo de 1.415 lineas; JaCoCo >= 80 % de ramas exige descomponerlo en el nuevo core (reglas NOR-xx como metodos puros) sin cambiar el orden de las comprobaciones, porque el orden determina que mensaje/contador sale (ver D-NOR-7).
Reproducir: `lizard -l java <archivos>` (salida completa en `C:\Users\m.villagran.mora\AppData\Local\Temp\mvscratch\lizard.txt`).

## 5. Comparacion byte a byte de SQL (SHA-256, sin normalizar nada)

Script `mvscratch\cmp.py` (solo lectura). Legacy: `.../BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/sql` = **376** `.sql`. Moderno `BcsFondoMutuoServicios`:

| Carpeta moderna | .sql | Identicos | Distintos | Faltan en moderno | Sobran en moderno |
|---|---:|---:|---:|---:|---:|
| `src/main/resources/sql` | 376 | **374** | **2** | 0 | 0 |
| `target/classes/sql` (salida de build) | 376 | 376 | 0 | 0 | 0 |
| Total "752" del plan = 376 x 2 | 752 | | | | |

- **Los "752" son 376 archivos contados dos veces** (fuente + `target/classes`). Los SQL reales son 376; ya estaban todos.
- Distintos (2), **ninguno en el alcance**: `ModificarCSC_ConsultarMandatoPAC.sql` (714 B legacy, 686 B moderno, 28 CRLF -> 0) y `ModificarCSC_InsertarMandatoPAC.sql` (849 B vs 801 B, 48 CRLF -> 0). La unica diferencia es fin de linea CRLF -> LF en `src/main/resources`; `target/classes` aun tiene la version CRLF identica al legacy (el moderno fue normalizado despues de compilar). Si se exige igualdad byte a byte hay que decidir: o se copian con CRLF, o se acepta LF para estos 2 (afectan `ModificarCSC`, otra clase fuera de F1).
- Los 36 SQL de estas clases (13 compartidos + 23 propios, seccion 1.5) estan **identicos** en `src/main/resources/sql`. Lista de los 9 propios de Normal usados: `CrearSolicitudAporteRescateNormal_{CallCrearBloqueoSolicitudAporteRescate, CallEliminarBloqueoSolicitudAporteRescate, CreateTableBloqueoSolicitudAporteRescate, DropTableBloqueoSolicitudAporteRescate, InsertTableBloqueoSolicitudAporteRescate, SelectBloqueoSolicitud, SelectConsultaTraspasoAPV, SelectCountCustodia, SelectParametroOperaConSaldoNegativo}`.
- Advertencia: que los SQL sean identicos NO prueba nada de la logica (los 132 `*Service` del modulo son stubs, `VERIFICATION.md` 0/60). Hay que cargarlos desde el classpath moderno con la ruta `cl/bcs/fondomutuo/sql/` o ajustar la ruta: el moderno los tiene en `sql/` plano, el legacy los pide como `cl/bcs/fondomutuo/sql/<archivo>` (Normal:95-113).

## 6. Nombres de tablas frente al catalogo real

Se extrajeron los objetos `"BSCL.*::*"` de los 36 SQL del alcance: **46 objetos distintos**. Catalogo local (`hana_qa_results/cat_columnas_tablas.csv`, `cat_columnas_vistas.csv`, `cat_tablas_tipo.csv`, `cat_temporales_pasos.csv`; son de otro bloque): solo **7** estan: `Custodia.BD::CUS.Cartera`, `Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio`, `Plataforma.BD::PLA.Moneda`, `Portafolio.BD::POR.Portafolio`, `SocioNegocio.BD::CLI.Cliente`, `SocioNegocio.BD::PER.Persona`, `SocioNegocio.BD::PER.DocumentoIdentidad`. No hay ninguna tabla `FMM.*` ni `CTA.*` de este bloque; `cat_temporales_pasos.csv` solo trae 2 tablas de paso de Plataforma, no la de `ValidaPatrimonioEmisor`. No se conecto a HANA.

**Lista para la consulta de catalogo de solo lectura (`SYS.TABLE_COLUMNS`/`SYS.VIEW_COLUMNS`/`SYS.TABLES`; autorizada por el usuario en la seccion 12 del plan), 39 objetos faltantes:**
- FMM (tablas): `FMM.SolicitudAporteRescate` (todas las columnas del INSERT: `Monto`, `NumeroCuotas`, `ValorCuota*`, `Costo*`, `Paridad*`, `Monto*Comision*`, `MontoPago*`, `Porcentaje`, `FolioContrato`, `FolioOmnibus`), `FMM.SerieDeFondo` (`MontoMinimo`, `MontoMaximo`, `PatrimonioMinimo`, `MontoPatrimonioEmisor`, `Dias*`, `Porcentaje*`), `FMM.Fondo`, `FMM.TipoFondo`, `FMM.TipoMovimientoFondoMutuo`, `FMM.RegimenFondoMutuo`, `FMM.RegimenSerieDeFondo`, `FMM.ConfiguracionPropositoPortafolio`, `FMM.ConfiguracionFechasFondoMutuo`, `FMM.ConfiguracionFondoMutuo`, `FMM.ValorCuota` (**importante: si guarda historia por fecha**, ver D-REQ-7), `FMM.ValorTipoCambio` (`ValorDolarAporte`, `ValorDolarRescate`), `FMM.Comision`, `FMM.InterfazAgf`, `FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate` (tipo/escala de `Monto`/`NumeroCuotas`; la tabla local usa `DECIMAL(26,8)`).
- FMM (vistas/secuencias/procedimientos): `FMM.ConsultarSolicitudAporteRescate`, `FMM.ConsultarTramoComision`, `FMM.ConsultarComisionMaxima`, secuencias `Secuencias::SolicitudAporteRescate`, `::FolioContrato`, `::InterfazAgf`; procedimientos `Servicios::CrearBloqueoSolicitudAporteRescate` y `::EliminarBloqueoSolicitudAporteRescate` (confirmar `AUTONOMOUS TRANSACTION` y firma).
- Plataforma: `IND.Indicador`, `IND.TipoIndicador`, `PLA.Configuracion` (escala/longitud de `Valor`), `REG.DiaFeriado`, `ACC.Usuario`, `ACC.ConsultarRolesPerfilesUsuario`, `MER.Emisor`, `PAG.MedioDePagoYModalidadDePago`, `BSCL.Plataforma.BD.Secuencias::SecuenciaMovimiento`.
- Otros esquemas: `APV: REC.RegimenTributario`, `REC.ConsultarRegimenTributario`, `TRA.Traspaso`, `TRA.OrdenTraspaso`; `Portafolio.BD.Vistas::ConsultarPortafolio`; `SocioNegocio: CLI.ConsultarClienteEstado`, `PER.ConsultarPersonaNombreCompleto`, `EMP.Empleado`.
- Tabla de paso de Patrimonio: **`BSCL.Orden.BD::GBL.ValidaPatrimonioEmisor_Proceso_Paso`** (esquema Orden, no FondoMutuo): confirmar si es `GLOBAL TEMPORARY` (`IS_TEMPORARY`/`TEMPORARY_TABLE_TYPE`). El plan la daba "a confirmar". Si NO es temporal global, `ValidaPatrimonioEmisor` hace `DELETE` de toda la tabla compartida entre sesiones (carrera real).
- Modulo de consulta de columnas ya cubiertas: no repetir las 7 de arriba.

## 7. Preguntas para el experto de dominio / regulatorias (QD-nn; no se decide nada)

- QD-01 Fondo cerrado solo advierte (FM-REQ-06): confirmar que la solicitud sigue con fechas del dia habil siguiente y no debe rechazarse.
- QD-02 Calendarios multi-pais, money market, lunes, fondo nacional dolar, repasos (FM-FEC-01..12): confirmar reglas y que no hay otra hora de corte.
- QD-03 Redondeo de cuotas `HALF_DOWN` y caso cuotas=0 con un decimal mas y `HALF_UP` (FM-CUO-05,06,09).
- QD-04 El precio se busca a la fecha de aceptacion y la comision a `FechaValorCuota` (FM-NOR-14, FM-COM-03): intencional?
- QD-05 Comision trimestral: IVA 19 % fijo, base 365, trimestre, NN tambien para rechazadas, `MontoPago` negativo (FM-COM-05, FM-NOR-21,46).
- QD-06 Exencion de patrimonio minimo a PAC/SAAPV (D-NOR-4): hoy solo opera en llamadas en proceso, no por JSON.
- QD-07 Topes maximos en 0 (serie y `MontoMaximo*Inversion`): 0 significa sin tope? (FM-NOR-23, FM-REQ-22).
- QD-08 Rescates parciales ya ingresados no descuentan custodia; dos rescates de una peticion tampoco (FM-NOR-35, FM-REQ-24).
- QD-09 Usuario con ambos perfiles (FM-REQ-20).
- QD-10 Monto minimo no aplica a PAC; EUR invalido en PAC es codigo muerto (FM-REQ-13, D-NOR-8).
- QD-11 Aporte por cuenta de inversion con saldo negativo solo si indicador "S" e `IngresoDesde=CORREDORES` (FM-NOR-37).
- QD-12 Aportes consumen lotes FIFO para comision `ENTRADA` (FM-COM-02).
- QD-13 Valor cuota de aportes en lunes y dia no habil (FM-FEC-09,12).
- QD-14 Bloqueo anti-duplicados, regimen DC, traspaso total (FM-NOR-03..06,24; FM-REQ-17).
- QD-15 Patrimonio emisor: monto de entrada vs calculado y `FMM.ValorCuota` sin fecha (FM-NOR-27,32; FM-REQ-23).

## 8. Defectos (revision de D-NOR-1..12 y D-MAN-1..4 del plan, y nuevos)

Revision de la lista del plan, leida contra el codigo:
- D-NOR-1,2,3,5,6,7,8,10,11,12: **confirmados** con las lineas del plan (6 tambien se confirma en `NN:561-572`: el INSERT corre con "N" fijo).
- D-NOR-4: confirmado (`:1309`, `ConstantesFondoMutuo` define `SAAPV` como literal final). **Impacto ampliado:** los jobs migrados (Tesoreria `PAC`, APV `REC`) llegan por router/JSON; con JSON la exencion PAC no opera, asi que el comportamiento de los jobs ya difiere del de la ejecucion en proceso.
- D-NOR-9: confirmado y ampliado: ademas de `toMap`, `Utils.List2Hash` lanza `key field is not unique` (9 mapas, `:522-530`); NPE adicionales `:704-705,808,1011,1038,1143,1236,1342,1546,1580`.
- D-MAN-1,2,4: confirmados. **D-MAN-3 corregido:** `List2Hash` lanza ante IdInterno repetido (comision NN usa 1..n) en vez de solo "pasar el filtro".

Nuevos (provisorios):
| Id | Defecto | Evidencia |
|---|---|---|
| D-MAN-5 | Mensajes de la pasada real se descartan; el bloqueo duplicado llega como contador 1 sin mensaje | Manejador:135-137 |
| D-MAN-6 | No propaga origen/dividendo/prospectos; canal nulo pisa "OPT" | Manejador:117-125 |
| D-MAN-7 | CambioSerie/RescateReinversion con una sola lista: no hace nada y sin mensaje | Manejador:210-260 |
| D-MAN-8 | Anfitrion responde antes del `commit` | CallServlet:494-495 |
| D-NOR-13 | `SQLException` en el bloqueo retorna sin `DROP` de la tabla local | Normal:365-370 |
| D-NOR-14 | Comision NN tambien para solicitudes rechazadas | Normal:1627-1629 |
| D-NOR-15 | Custodia no se descuenta entre rescates de la misma peticion | Normal:1289-1298 |
| D-NOR-16 | Consulta de flujo de caja omite pares portafolio/moneda (listas independientes) | Normal:1191 |
| D-NOR-17 | `MontoPago` negativo con comision trimestral | Normal:879-880 |
| D-NOR-18 | Patrimonio emisor usa `Monto` de entrada (puede ser nulo o de otra moneda) | Normal:972 |
| D-NOR-19 | Fecha del precio de cuota distinta de `FechaValorCuota` guardada | Normal:626-632,895 |
| D-FEC-1 | `IdPaisLocal := IdPaisEmisor` | CalcularFecha:495 |
| D-FEC-2 | Configuracion faltante o `Switch` raro: NPE o solicitud perdida; ultima fila de configuracion pisa | :516-523,822 |
| D-FEC-3 | Dedupe de feriados con `Long ==` (identidad); `toMap` sin dedupe | :1237,1247,1294 |
| D-FEC-4 | Claves por concatenacion sin separador | :855,1088 |
| D-FEC-5 | Codificacion `IdInterno*3+k` | :1308,1319 |
| D-FEC-6 | Dia de semana y dias por zona de la JVM | :1034-1081 |
| D-FEC-10 | Clave de busqueda usa valutas de la serie o de configuracion segun estado: puede dar NPE | :917-921 |
| D-CUO-1 | `equals(BigDecimal.ZERO)` sensible a escala | CUO:301 |
| D-CUO-2 | NPE con `NumeroCuotas` nulo | CUO:420 |
| D-CUO-3 | Redondeo mixto `HALF_DOWN`/`HALF_UP` y +1 decimal | CUO:325,337,421 |
| D-CUO-6,7 | `obtenerPrecios` nulo; `List2Hash` con duplicados | CUO:224,222 |
| D-COM-1 | `EsTraspaso` fijo "N" | COM:337,342 |
| D-COM-2 | `new BigDecimal(1.19)`, IVA fijo, dias por milisegundos | COM:389,502-510 |
| D-COM-3 | Cuenta regresiva `CantidadCarteras` | COM:343,383,412 |
| D-COM-4 | `CantidadComision` solo del ultimo lote vs `Comision` total | COM:406-414 |
| D-COM-5 | Paridad faltante vale 1 | COM:479-488 |
| D-COM-6 | Aportes consumen lotes | COM:270-300 |
| D-COM-7 | NN degrada errores a advertencia | NN:536 |
| D-REQ-1 | Dedupe por descripcion deja pasar solicitudes con error | Cumple:456-465 |
| D-REQ-2 | Mensaje de fecha rescate usa mapa equivocado | Cumple:332 |
| D-REQ-3 | `p_IdUsuario` como empresa; `p_IdEmpleado` nunca asignado | Cumple:495,500 |
| D-REQ-4 | `CLIENTE_NO_EXISTE`/`PORTAFOLIO_NO_EXISTE` inalcanzables desde Normal | Cumple:196; Normal:1038 |
| D-REQ-6 | Maximo 0 rechaza todo aporte | MontoInversion:160-170,313-318 |
| D-REQ-7 | `FMM.ValorCuota` sin filtro de fecha en la custodia (si hay historia, suma inflada); tabla de paso compartida | SQL Patrimonio |
| D-REQ-8 | `TOP 1` sin `ORDER BY` | SQL Perfil |
| D-REQ-9 | Regimen vacio toma el primer flujo | FondoMutuoUtils:19 |
| D-REQ-10 | Solo se descuenta rescate total en transito | SQL RescateTotalTransito |
| D-REQ-11 | `getNumeroErrores` compara por referencia | PlataformaUtils:636 |
| D-SQL-1 | JOIN `ConfMonedaSistema` condicionado a `ConfMoneda`: `EmisorFondoNacionalPesos` siempre "N" | SelectSerieFondo.sql |
| D-SQL-2 | `SelectPortafolio` puede duplicar filas y romper `List2Hash` | SelectPortafolio.sql |
| D-SQL-3 | `SelectParametroOperaConSaldoNegativo` sin empresa ni estado | .sql |
| D-TES-1 | `TitularPago` siempre "N"; fecha liquidacion = emision | Tesoreria:211,258 |
| D-SPOT-1,2 | Parametros de flujo de caja cruzados; ids duros | Spot:334-341,437,444 |

## 9. Conteo de tarjetas

FM-MAN 12 (MAN-01..12), FM-NOR 57 (01..52 + 90..94), FM-FEC 14, FM-CUO 10, FM-COM 10, FM-REQ 25: **128 tarjetas**. Alcance: F1 puro ~96; con compuerta (G1..G5) NOR-33,37,38,39,40,43,44,47 y MAN-11; diferidas (F2): NOR-90..94, MAN-07, REQ-03,11,14,15.



## 10. Tarjetas FM-MAN (ManejadorSolicitudAporteRescate.java)

Formato: Given/When/Then; `Fuente`; `E/S`; `Defectos`; `Alcance`; `Experto`. IdInterno = correlativo de la peticion.

**FM-MAN-01 Orden fijo de despacho por tipo.** Given una peticion con varias listas con filas. When se ejecuta. Then se procesan en este orden: Normal, Programado, Overnight, Reinversion (aporte), CambioSerie, RescateReinversion, RescateTransferencia, RescateTraspaso; cada tipo termina (simulacion y ejecucion) antes de empezar el siguiente. Fuente `:115,141,164,187,210,236,262,285`. E/S: 10 listas `p_Solicitud*` -> `P_OUT_RESULTADO` en ese orden. Defectos: -. Alcance F1 (solo Normal; el orden se escribe completo, 7 handlers "no servido"). Experto: no.

**FM-MAN-02 Simular antes de ejecutar.** Given una lista con filas. When se procesa. Then (1) llama a la clase destino con `p_SimularOperacion="S"`; (2) agrega sus mensajes a la salida; (3) deja solo las solicitudes cuyo `IdInterno` esta en `P_OUT_RESULTADO` de la simulacion; (4) si queda alguna, vuelve a llamar con `"N"` sobre la misma conexion. Fuente `:116-138` (Normal; igual para los otros 7 tipos). E/S: sim -> mensajes; real -> contador y resultados. Defectos D-MAN-1, D-MAN-5. Alcance F1. Experto: no.

**FM-MAN-03 Filtro de "buenas" por IdInterno con diccionario unico.** Given `P_OUT_RESULTADO` de la simulacion. When arma `Utils.List2Hash(..., "IdInterno")`. Then si hay dos resultados con el mismo IdInterno **lanza `Exception("key field is not unique")`** (`BcsUtils/Utils.java:768-779`) y la peticion falla con excepcion. Fuente `Manejador:128`. Defectos D-MAN-3 (corregido). Alcance F1. Experto: no.

**FM-MAN-04 Forma de pago Spot filtrada por IdOperacion.** Given `p_FormaPagoSpot`. When hay "buenas" de Normal. Then conserva solo las formas de pago cuyo `IdOperacion` esta en los resultados (`:130`) y las pasa a la pasada real (`:133`). Si `p_FormaPagoSpot` no vino (campo sin valor por defecto, `:75` y constructor `:62-73` no lo inicializa) vale `null` y `in.p_FormaPagoSpot.parallelStream()` lanza `NullPointerException` **despues** de la simulacion. Fuente `:125,130,133`. Defectos D-MAN-2. Alcance F1. Experto: Q-03 del plan (la UI envia siempre el campo?).

**FM-MAN-05 Salida mezcla mensajes de simulacion con contador de la pasada real.** Given dos pasadas. When arma la salida. Then `P_OUT_MENSAJES` = mensajes de la simulacion (`:127`), `P_OUT_ERRORESCONTADOR` y `P_OUT_RESULTADO` = los de la pasada real (`:136-137`); los mensajes de la pasada real se descartan. Consecuencia: un error que solo nace en la pasada real (p. ej. `UI_ERROR_BLOQUEO_SOLICITUD`, saldo, comision NN) devuelve contador >0 sin mensaje. Fuente `:127,135-137`. Defectos D-MAN-5. Alcance F1. Experto: si (desea mensaje?).

**FM-MAN-06 Contador no incluye errores de la simulacion.** Given una simulacion que rechaza todo. When `entradaBuenos.size()==0`. Then no hay pasada real, contador=0, mensajes de error y `P_OUT_RESULTADO` vacio; `CallServlet` ve contador 0 y hace `commit` (`CallServlet:442-453,494-495`). Fuente `:131`. Defectos D-MAN-1. Alcance F1. Experto: si (el cliente recibe exito con mensajes de error).

**FM-MAN-07 CambioSerie y RescateReinversion exigen par aporte+rescate.** Given solo una de las dos listas con filas (o sin pares buenos). When simula. Then no ejecuta ninguno, sin mensaje y contador 0. Given ambas buenas. Then ejecuta ambas (`&&`). Fuente `:210,226,236,252`. Defectos D-MAN-7. Alcance F2. Experto: no.

**FM-MAN-08 Campos de entrada que no se propagan.** Given `p_SimularOperacion`, `p_IdOrigenProceso`, `p_NumeroDividendo`, `p_PermiteOperarProspectos`. When el Manejador arma la entrada de Normal. Then `p_SimularOperacion` de entrada se ignora (`:121,134` fijos "S"/"N"); origen, dividendo y prospectos quedan en los valores por defecto de Normal (`OPT`, 0, `N`: Normal:130-137); y `p_IdTipoCanalIngreso` se copia tal cual (`:123`) **pisando** el valor por defecto "OPT" con `null` si no vino. Fuente `:117-125`. Defectos D-MAN-6. Alcance F1. Experto: no.

**FM-MAN-09 Excepcion.** Given cualquier excepcion en una pasada. When propaga. Then pone `out.P_OUT_ERRORESCONTADOR=1`, escribe el log y **relanza**; el cuerpo con contador nunca se envia (error HTTP / rollback del anfitrion). Fuente `:310-314`. Defectos D-MAN-4. Alcance F1. Experto: no.

**FM-MAN-10 Transaccion y orden de respuesta (regla del anfitrion).** Given una llamada tipo J. When devuelve. Then si el contador falta o es >0, `rollback` de TODO y 200 con el cuerpo (`CallServlet:442-453`); si es 0 (tras ejecutar los "extras" `:457-480`), **escribe y vacia la respuesta ANTES del `commit`** (`CallServlet:493-495`): un fallo de `commit` despues de enviar 200 no llega al cliente. Fuente `CallServlet:436-495`. Defectos D-MAN-8. Alcance F1 (el nuevo UoW debe responder despues del commit; plan 7.4). Experto: no.

**FM-MAN-11 (nuevo) Compuerta G0.** Given una peticion con filas en cualquier lista distinta de `p_SolicitudesAporteRescateNormal`. When llega al modulo nuevo. Then 503 antes de tocar nada para que el router repita TODA la peticion en TomEE. Fuente: plan 6.3 / DEV-F4 (no existe en el legacy). Defectos -. Alcance F1. Experto: no.

**FM-MAN-12 Peticion sin ninguna lista con filas.** Given las 10 listas vacias. When se ejecuta. Then ningun `if` entra: devuelve `out` con contador 0, sin mensajes ni resultados (el anfitrion confirma). Nota: Normal si rechaza lista vacia (`NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE`, Normal:317-321) pero el Manejador nunca la llama. Fuente `:115-308`. Defectos -. Alcance F1. Experto: no.

## 11. Tarjetas FM-NOR (CrearSolicitudAporteRescateNormal.java y colaboradores de escritura)

Cita sin prefijo = `CrearSolicitudAporteRescateNormal.java`. "sol" = objeto `SolicitudAporteRescate` armado (`:769-917`), "solicitud" = fila de entrada.

**FM-NOR-01 Requisitos basicos.** Given empresa/usuario/modulo. When `RevisarRequisitosBasicos` devuelve algun mensaje `ERROR` (empresa inexistente o sin socio/cliente, usuario inexistente o no `ACT`, modulo inexistente). Then contador=1, mensajes de plataforma, return sin escribir. Fuente `:309-315` (`PlataformaUtils.java:633-636 getNumeroErrores` compara `tipo == MENSAJE_ERROR` por referencia: solo vale en proceso, ver D-REQ-11). E/S: `p_IdEmpresa,p_IdUsuario,p_IdModulo` -> `P_OUT_MENSAJES`. Defectos D-REQ-11. Alcance F1 (kit K). Experto: no.

**FM-NOR-02 Lista vacia.** Given `p_SolicitudesAporteRescate` vacia. When corre. Then contador=1, mensaje `NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE` (id 0), return. Fuente `:317-321`. Defectos -. Alcance F1. Experto: no.

**FM-NOR-03 Cuando aplica el bloqueo anti-duplicados.** Given `p_SimularOperacion != "S"` (null cuenta como "no simula") y origen "manual". When el origen no es `PAC`,`REC`,`BONIF` (`equals`, sensible a mayusculas) ni `CAR`,`CSC` (`equalsIgnoreCase`). Then se aplica el bloqueo. Fuente `:326, 1727-1740`. E/S: `p_IdOrigenProceso` (def. "OPT"). Defectos D-NOR-1..3. Alcance F1. Experto: si (QD-14).

**FM-NOR-04 Clave del bloqueo.** Given cada solicitud. When arma `IdBloqueo = IdPortafolio_IdSerieFondo_IdRegimenTributario_TipoSolicitud_Monto_NumeroCuotas_FechaSistema` con `String.valueOf` (BigDecimal.toString: depende de escala; null -> "null"), `fechaSistema = PLA.FechaSistema`. Fuente `:323,328-339`. Defectos D-NOR-2. Alcance F1. Experto: si.

**FM-NOR-05 Deteccion de duplicado.** Given ids calculados. When `SELECT ... IN` sobre `FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate` devuelve filas, con igual cantidad que las solicitudes y **la lista de ids es igual y en el mismo orden**. Then contador+1, mensaje `UI_ERROR_BLOQUEO_SOLICITUD` (id 1), return (no hay bloqueo creado aun). Given `SQLException` en cualquier paso (select, create, insert, call). Then mismo error (`:365-370`), return **sin** `DROP` de la tabla local ya creada. Fuente `:341-370`. Defectos D-NOR-1,2,13. Alcance F1. Experto: si.

**FM-NOR-06 Creacion del bloqueo.** Given no hay duplicado. When corre. Then `CREATE LOCAL TEMPORARY TABLE #p_BloqueoSolicitudAporteRescate (... DECIMAL(26,8))`, `INSERT` batch de las solicitudes, `CALL ...::CrearBloqueoSolicitudAporteRescate(p_Bloqueo => #p_...)` (autonomo: confirma fuera de la transaccion). Fuente `:357-363`; SQL `CrearSolicitudAporteRescateNormal_{Create,Insert}Table...,CallCrearBloqueo...`. Defectos D-NOR-3,13. Alcance F1 (DDL de sesion aceptado, decision D-2). Experto: no.

**FM-NOR-07 IdInterno 0, nulo o repetido.** Given `IdInterno` 0 -> contador=1, `ID_INTERNO_NO_CERO`; repetido -> contador=1, `CORRELATIVO_INCORRECTO`; en ambos casos elimina el bloqueo y return. `IdInterno` nulo -> `NullPointerException` (el `catch` elimina bloqueo y relanza). Fuente `:375-390`. Defectos D-NOR-7,9. Alcance F1. Experto: no.

**FM-NOR-08 Perfil del usuario.** Given `ObtenerPerfilFondoMutuo` sin perfil (usuario sin `PERFIL_COMERCIAL`/`PERFIL_BACKOFFICE`). When no todas las solicitudes son `SolicitudBatch="S"`. Then contador=1, `PERFIL_INCORRECTO`, elimina bloqueo, return. Fuente `:439-450` (ver FM-REQ-20 para el SQL). Defectos D-REQ-8. Alcance F1. Experto: si (QD-09).

**FM-NOR-09 Preparacion del lote.** Given las solicitudes. When recorre. Then `IdMonedaPago` vacia/nula := `IdMoneda` (muta la entrada); cuenta `SolicitudBatch="S"`; junta series, clientes, regimenes, portafolios, folios; `existeTraspasoTotal` si alguna es `CalculoPorMontoCuota="T"`. Fuente `:403-437`. Defectos -. Alcance F1. Experto: no.

**FM-NOR-10 Lecturas de apoyo en lote.** Given listas. When ejecuta 10-11 SELECT: serie+fondo, portafolio+cliente, tipo movimiento (APONORM, RESNORM, RESTRASP, APOTRASP), regimen por serie, indicador `GEN_OCCINV`, portafolio APV, configuracion de proposito, `TRA.Traspaso` por `FolioContrato`, conteo custodia DC (solo si hay "T"), moneda, y despues arma 9 `HashMap` con `Utils.List2Hash` (lanza `key field is not unique` si una consulta devuelve dos filas con la misma clave) y 1 `Collectors.toMap` (lanza `IllegalStateException`). Fuente `:454-531`. Defectos D-NOR-9, D-SQL-2. Alcance F1. Experto: no.

**FM-NOR-11 Entradas para el calculo de fechas.** Given cada solicitud con serie existente (las sin serie se omiten y despues NPE). When arma `InFechas`: aporte -> estado de fondo comercial/backoffice segun perfil, retencion y valutas de aporte; rescate -> idem de rescate; otro tipo -> `C`, valutas 0; `EstadoLiquidacion` = estado de liquidez segun perfil. **Batch** (`SolicitudBatch="S"`): fondo y liquidacion `A`; si ademas `RespetaValutasCargaMasiva="N"` (por defecto si falta) todas las valutas 0. Fuente `:540-612` (batch `:549-556,580-584`). Defectos D-NOR-9. Alcance F1. Experto: si (QD-02).

**FM-NOR-12 Fecha de emision.** Given `FechaOperacion`. When es "0001-01-01" (`FechaDefecto`) -> aporte/rescate usan `serie.FechaFondo` si el estado de aporte/rescate segun perfil es `A` y la fecha de sistema si no; otra fecha -> esa fecha. `FechaOperacion` nula con modulo `APV` := fecha de sistema. Fuente `:541-542,587-597`. Defectos -. Alcance F1. Experto: si (QD-02).

**FM-NOR-13 Rescate genera Spot en fechas.** Given param `FONDOMUTUO/RESCATE_GENERA_VENTA_SPOT_DOLAR="S"` y rescate con `IdMoneda=USD` y `IdMonedaPago=CLP`. Then `RescateGeneraSpotCertificacion="S"` (afecta el repaso de certificacion, FM-FEC-10). Tambien envia `FondoNacionalDolar`, `IdPaisMoneda`, `IdMercadoFondoMutuo`. Fuente `:538,603-609`. Defectos -. Alcance F1. Experto: si (QD-02).

**FM-NOR-14 Fecha usada para el valor cuota.** Given cada solicitud. When fija `serie.Fecha` del calculo de cuota. Then `solicitud.FechaValorCuota` si viene; si no `FechaAceptacion` calculada; si no la fecha de sistema. Lo mismo para la paridad a moneda de pago. La `FechaValorCuota` que se guarda en la solicitud (`:895`) es la calculada por `CalcularFecha...`, distinta de la fecha del precio usado. Fuente `:621-643,655-667,895`. Defectos D-NOR-19. Alcance F1. Experto: si (QD-04).

**FM-NOR-15 Secuencias y folio.** Given N solicitudes. When pide `Utils.getSecuencia` de `SolicitudAporteRescate` (N ids) y de `FolioContrato` (N). Then `IdSolicitudAporteRescate` = secuencia i; `FolioContrato` = el de entrada si `>0`, si no el de la secuencia en la posicion de la solicitud (`indexOf`); `FolioContrato` nulo en la entrada -> NPE. Se pierden secuencias en cada rechazo/rollback. Fuente `:647,736,770,808`. Defectos D-NOR-9. Alcance F1. Experto: no.

**FM-NOR-16 Paridad a moneda de pago.** Given `IdMoneda` y `IdMonedaPago`. When llama `CalcularParidad` con `MontoOrigen` = monto de la cuota (o `solicitud.Monto`). Then `MontoMonedaPago = MonedaFormato(MontoDestino, IdMonedaPago)` si hay resultado. `GeneraSpot = IdMoneda != IdMonedaPago` (no solo USD/CLP). Fuente `:651-677,830,890-892`. Defectos -. Alcance F1 (H `ParidadPort`). Experto: no.

**FM-NOR-17 Comisiones solo si el parametro lo pide.** Given `FONDOMUTUO/CALCULAR_COMISION_CERTIFICACION="S"`. When llama `CalcularComisiones` con cantidad = cuotas calculadas, fecha de curse = `FechaAceptacion`, `FechaValorCuota` = la de entrada o la calculada, `EsTraspaso="S"` si el `FolioContrato` esta en `TRA.Traspaso`. Then resultados por IdInterno; con parametro distinto no hay comision. Fuente `:395,679-716`. Defectos D-COM-1. Alcance F1. Experto: si (QD-05).

**FM-NOR-18 Tipo de movimiento.** Given tipo/EsTraspaso. When APO -> `APOTRASP` si `EsTraspaso="S"` si no `APONORM`; RES -> `RESTRASP` si `EsTraspaso` es "S" o "TRA" si no `RESNORM`; otro -> "". Fuente `:743`. Defectos -. Alcance F1. Experto: no.

**FM-NOR-19 Canal de procedencia.** Given origen. When `p_IdOrigenProceso` es "PAC" (ignora mayusculas) -> "PAC"; si no `solicitud.Canal` si no vacio, si no `p_IdTipoCanalIngreso`, si no "". Fuente `:760-766`. Defectos D-MAN-6. Alcance F1. Experto: no.

**FM-NOR-20 Armado de la solicitud.** Given los datos. When arma `sol`. Then `Estado=ING`, `EstadoProceso=PEN`, `FechaEmision=fechaSistema`, `FechaIngresoRegistro` = fecha de sistema + hora local de la empresa convertida a UTC (offset de `FechaDB`; fija `Fecha.setLocalOffSet` estatico), `OrigenOperacion/SolicitudBatch/IdAutorizadoOrigen` de la entrada, `Visado="N"` para rescates y aportes con medio `CIN`, si no el de la entrada, `IdEmpleadoEjecutivo` del portafolio o 0, `IdMonedaSistema` vacio hasta tener cuota, montos en 0, `TipoCarteraNormativa` solo para aporte `APONORM` con regimen. El INSERT agrega `FechaIngreso` = `CURRENT_UTCTIMESTAMP` de la BD (otro reloj). Fuente `:738-740,769-837`; `ComunesSolicitudes_InsertSolicitud.sql:132`. Defectos D-NOR-10, D-NOR-9. Alcance F1. Experto: no.

**FM-NOR-21 Montos a partir del valor cuota.** Given resultado de `CalcularMontoNumeroCuota` con `ValorCuota != null`. When arma montos. Then `Monto,NumeroCuotas,ValorCuota,CostoUF,CostoMonedaLocal,ValorUFFechaInversion,ParidadFechaInversion,IdMonedaSistema,MontoMonedaSistema` = del resultado; comision: sin dato o (trimestral y aporte) -> todo 0; trimestral y rescate -> `Monto = Monto - Comision`, `NumeroCuotas = NumeroCuotas - CantidadComision`, `MontoInversion/NumeroCuotasInversion` = esos; otra comision -> `MontoComision = Comision`. `MontoPagoLocal = MontoDestino - ComisionLocal`; `MontoPago = Monto - MontoComision` salvo comision trimestral >0 en que `MontoPago = 0 - comision` (negativo). Sin `ValorCuota` todo queda en 0 (se rechaza en FM-REQ-16). Fuente `:838-889`. Defectos D-NOR-17, D-COM-4. Alcance F1. Experto: si (QD-05).

**FM-NOR-22 Fechas y tributacion.** Given resultado de fechas y regimen. When asigna. Then `FechaAceptacion`, `FechaCertificacion`, `FechaLiquidacion`, `FechaValorCuota` (la de entrada si viene); `Tributacion` por `GrupoRegimen`: `APV`->`ModeloRetiroAPV`, `ART108`->`MayorMenorValor`, `ART107`->`Beneficio107LIR`, `57BIS`->`Beneficio57BIS`, otro->`MayorMenorValor`; sin regimen no asigna (null). Fuente `:893-917`. Defectos -. Alcance F1. Experto: si (tributario).

**FM-NOR-23 Datos de la serie para el chequeo de requisitos.** Given serie existente. When arma `solVal`. Then: `EstadoFondo` = estado de aporte/rescate segun tipo y perfil; `EstadoBloqueo` = `BloqueoAporte`/`BloqueoRescate`; `EstadoLiquidez` segun perfil; aporte: `EsMontoMaximo="N"` si `MontoMaximo != 0` y `MontoMaximo < sol.Monto` (`compareTo == -1`); `EsMontoMinimo="N"` si el regimen existe con `ValidaMontoMinimoInstrumento="S"`, `MontoMinimo != 0` y `MontoMinimo > sol.Monto`; rescate: ambos "S"; `EsDiferido="N"` si `IdMoneda != serie.IdMoneda` y `serie.Diferido="N"`. Serie inexistente: `IdEstadoSerie=""` y el resto por defecto "S" (el requisito de serie no vigente igual falla, FM-REQ-04). Fuente `:922-988`. Defectos D-NOR-9 (`serie.MontoMaximo` nulo -> NPE). Alcance F1. Experto: si (QD-07: 0 = sin tope).

**FM-NOR-24 Regimen, mandato, proposito, cuenta de inversion.** Given regimen `r` de la serie. When (a) `r==null` y (rescate no total o aporte) -> `EsRegimenValido="N"`; si no `PermiteAporte = r.PermiteAporte` (solo aportes); (b) con `r`: `PermiteRescate="S"` si `r.PermiteRescate="S"` o (`r.PermiteTraspaso="S"` y `EsTraspaso="TRA"`), si no `r.PermiteRescate` en rescates y "S" en aportes; sin `r` y con custodia DC (`Cantidad>0` en `SelectCountCustodia`) -> `PermiteRescate="N"`; (c) sin configuracion de proposito (`regimen_proposito`) y (aporte o rescate no total) -> `EsPortafolioRegimenValido="N"`; (d) medio `CIN` y portafolio sin indicador `GEN_OCCINV="S"` -> `EsCuentaInversionValido="N"`. Fuente `:989-1013`. Defectos D-NOR-9 (`indicadorPortafolio` nulo -> NPE si medio `CIN`). Alcance F1. Experto: si (QD-14 regimen/DC/traspaso total).

**FM-NOR-25 Portafolio de Bonificacion Fiscal y "ignorar monto minimo".** Given `cliente.CodigoPortafolio = "BF"` o (`EsMontoMinimo="N"` y `IgnorarMontoMinimo="S"`). Then `EsMontoMinimo := "S"`. `cliente` nulo -> NPE (`:1038`, sin guarda aunque otras lineas la tienen). Fuente `:1037-1043`. Defectos D-NOR-9. Alcance F1. Experto: si.

**FM-NOR-26 Datos de portafolio/cliente para requisitos.** Given `cliente != null`. Then arma `InPortafolio` (ids reales, estados, documento, empleado; destino en 0 y ""). **Aunque `cliente` sea nulo** igual agrega el `portVal` vacio a la lista (`:1045`), con `IdInterno` nulo. `IdTipoOperacion` = `APORFM`/`RESCFM`, `IdTipoProducto="INFM"`. Fuente `:1015-1034,1045-1046`. Defectos D-REQ-4. Alcance F1. Experto: no.

**FM-NOR-27 Acumulado por emisor y candidatos a validar patrimonio.** Given aporte. When suma `MontoMonedaSistema` por `cliente_emisor` (`hashMontosPortafolioEmisor`, sin uso posterior) y, si `serie.PatrimonioAltos="S"`, agrega a la validacion `{IdInterno,IdCliente,IdSerieFondo,IdEmisor,MontoPatrimonio = solicitud.Monto (de ENTRADA), ValorCuota}`. Fuente `:961-975`. Defectos D-NOR-18. Alcance F1. Experto: si (QD-15).

**FM-NOR-28 Resultado por solicitud.** Given `sol`. When copia a `ResultadoSolicitud` y agrega datos de serie (fondo, nemo, nombres, codigo serie, emisor) y cliente (documento, codigo portafolio, nombre, `IdPortafolio` = real), `MontoTransferencia = Monto`, `NumeroCuotaTransferencia = NumeroCuotas`, fechas como "yyyy-MM-dd"/timestamp. Un resultado por solicitud, que luego solo se emite si queda `PEN`. Fuente `:1049-1074`. Defectos -. Alcance F1. Experto: no.

**FM-NOR-29 Aplicar el resultado de los requisitos.** Given mensajes de `ServicioCumple...` agrupados por `Math.abs(mje.id)`. When para cada solicitud con mensajes. Then los agrega a la salida; suma al contador **uno por cada mensaje `ERROR`** (una solicitud con 3 errores suma 3); si hubo algun error `EstadoProceso=RZD`. Mensajes cuyo id no coincide con ningun IdInterno (id 0, p. ej. `Diccionario` sin id) se pierden. Si no queda ninguna `PEN`: elimina bloqueo y return (sin simulacion de resultados). Fuente `:1083-1102`. Defectos D-NOR-7, D-REQ-1. Alcance F1. Experto: no.

**FM-NOR-30 Monto total de inversion.** Given aportes `PEN`. When llama `ValidaMontoInversion` con `Monto = MontoMonedaSistema`, `AportesAltos` de la serie, `FolioContrato`, `ValidaMontoMinimo` del regimen (S si no hay). Then por cada mensaje `ERROR` suma contador y rechaza la solicitud cuyo IdInterno = `mje.id` (`break` en la primera). Mensajes no `ERROR` se agregan igualmente. Fuente `:1106-1139`. Defectos D-REQ-6. Alcance F1. Experto: si (QD-07).

**FM-NOR-31 Fecha maxima de certificacion.** Given solicitudes `PEN`. Then `fechaMaximaCertificacion = max(FechaCertificacion)` (desde "0001-01-01"); `FechaCertificacion` nula -> NPE. Fuente `:1141-1146`. Defectos D-NOR-9. Alcance F1. Experto: no.

**FM-NOR-32 Patrimonio del emisor.** Given `entradaValidaPatrimonioEmisor` con filas **y** origen manual. When llama `ValidaPatrimonioEmisor`. Then por cada mensaje `ERROR`: contador+1, agrega el mensaje y rechaza esa solicitud. Para origen PAC/REC/BONIF no se valida. Fuente `:1148-1162,1720-1725`. Defectos D-NOR-18, D-REQ-7. Alcance F1. Experto: si (QD-15).

**FM-NOR-33 Saldo de flujo de caja: armado de la consulta [G3].** Given origen manual y aportes `PEN` con medio `CIN`. When acumula `portTotalesMontos["portafolio_monedaPago"] += MontoMonedaPago` y arma `P_PORTAFOLIOACONSULTAR` agregando el par portafolio/moneda **solo si el portafolio no esta en `portFlujo` o la moneda no esta en `monedaFlujo`** (listas independientes). Llama `SaldoFlujoCaja` con `P_USO="INGRESO_APORTE_FMM"`, desde fecha de sistema hasta la maxima certificacion. En la rama `PAC`+`EUR` (`MONEDA_INVALIDA_PAC`) nunca entra (esta dentro de "manual"). Fuente `:1164-1207`. Defectos D-NOR-8, D-NOR-16. Alcance F1-G3. Experto: si.

**FM-NOR-34 Flujo de custodia: armado [G-ninguna].** Given solicitudes `PEN` que son rescates, o aportes con `serie.PatrimonioMinimo > 0`. When arma carteras (una por portafolio) e instrumentos (clave `IdInstrumento + regimen|null`) y llama `ConsultarFlujoCustodiaFondoMutuo` (fecha = fecha de sistema, 0 dias de proyeccion). Sin carteras no llama. Fuente `:1210-1251`. Defectos D-NOR-9 (`IdRegimenTributario` nulo, `serie` nula -> NPE). Alcance F1. Experto: no.

**FM-NOR-35 Custodia insuficiente en rescates.** Given rescate no rechazado. When busca el flujo de custodia con `FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso` (mismo portafolio e instrumento; regimen vacio coincide con cualquiera; primer coincidente) y `NumeroCuotas > CantidadDisponible` (`compareTo == 1`). Then `RZD`, contador+1, mensaje `UI_ERROR_FM_CLIENTE_SIN_CUSTODIA {Nemotecnico, CantidadRetiro, CantidadCustodia}`, continue. La custodia **no se descuenta entre solicitudes** de la misma peticion. Fuente `:1284-1307`; `FondoMutuoUtils.java:16-23`. Defectos D-NOR-15, D-REQ-9. Alcance F1. Experto: si (QD-08).

**FM-NOR-36 Patrimonio minimo del fondo (aportes).** Given aporte. When `serie.PatrimonioMinimo > (sol.Monto + montoCustodiaDisponibleValorizado)` (`compareTo == 1`, con `monto = CantidadDisponible * ValorCuota`) **y** `p_IdOrigenProceso != "PAC"` **y** origen distinto de `BONIF` (`equalsIgnoreCase`) **y** `TipoContratoOperacion != "SAAPV"` (ambas `!=` son comparaciones de referencia). Then `RZD`, contador+1, `UI_PATRIMONIO_MINIMO_INSUFICIENTE`. Con JSON (router, `/call`, Gson) los `String` no estan internados: las exenciones PAC y SAAPV no operan. En proceso con literal si operan. Fuente `:1309-1314`. Defectos D-NOR-4. Alcance F1. Experto: **si, regulatorio** (QD-06).

**FM-NOR-37 Saldo suficiente en flujo de caja para aportes por cuenta de inversion.** Given aporte `CIN` de origen manual no rechazado. When `total = suma montos CIN del portafolio/moneda` y `saldo` = ultimo registro del flujo para (portafolio, moneda de pago, `FechaSaldo == FechaAceptacion`). Then si `ValorIndicador` del tipo `GEN_OCSNEG` (opera con saldo negativo) del portafolio es "N" o `IngresoDesde != "CORREDORES"`, y `SimulaCambioPortafolioEjecutadoOk="N"` y `total > saldo`: `RZD`, contador+1, `UI_SALDO_INSUFICIENTE_FLUJO_CAJA {CodigoPortafolio, DocumentoIdentidad}`. Con indicador "S" **y** `IngresoDesde="CORREDORES"` no se valida (CELLINCECT-763). Fuente `:1316-1352`; SQL `SelectParametroOperaConSaldoNegativo` (sin filtro de empresa ni estado). Defectos D-NOR-16, D-SQL-3. Alcance F1-G3. Experto: si (QD-11).

**FM-NOR-38 Seleccion de movimientos externos para aportes [G1, G2].** Given aporte no rechazado. When (a) `IdMedioDePago != "CIN"` -> se agrega a la lista de Tesoreria (los rescates NUNCA mueven Tesoreria en Normal); (b) `GeneraSpot="S"` -> `sol.Estado := "PEN"` (en vez de "ING"), se agrega a la lista Spot y se copian las formas de pago de `p_FormaPagoSpot` con `IdOperacion == IdInterno` (`FechaLiquidacion` = `FechaAceptacion`). Una forma de pago con `IdOperacion` nulo -> NPE. Fuente `:1354-1409`. Defectos D-NOR-9. Alcance: G1 si hay aportes no-CIN no simulados; G2 si hay **aportes** con `GeneraSpot="S"` (rescates con `GeneraSpot="S"` no llaman Spot: correccion al plan 6.3 G2). Experto: no.

**FM-NOR-39 Ingreso/egreso de Tesoreria [G1, F2].** Given `p_SimularOperacion != "S"` y lista Tesoreria no vacia. When llama `ProcesarIngresoEgresoTesoreria` (FM-NOR-90). Then por cada mensaje `ERROR`: contador+1 y mensaje `(0, tipo, codigo, "TES : " + descripcion)`; y **rechaza solo la primera solicitud de `solicitudesProcesar`** (bucle con `break`, no la fallida); por cada `ResultadoProceso` con `Resultado="RECH"` y `Campo="NumeroReferencia"` rechaza la solicitud con ese IdInterno; por cada `IngresoEgresoGenerado` guarda `IdIngresoEgreso` en la solicitud con `NumeroReferencia==IdInterno`. Fuente `:1415-1463`. Defectos D-NOR-5, D-NOR-12. Alcance F1-G1 (F2: libreria en proceso, seccion 12 plan). Experto: no.

**FM-NOR-40 Operacion Spot [G2, F2].** Given lista Spot no vacia y no simula. When llama `ProcesarIngresoOperacionSpot` (FM-NOR-92). Then por cada `IdOperacionSpot`: `Estado="RECH"` -> rechaza la solicitud; si no guarda `IdOperacionSpot`; por cada mensaje de tipo `"ERROR"` contador+1, mensaje `"SPT : "...`, y rechaza **la primera solicitud** de la lista. Fuente `:1465-1504`. Defectos D-NOR-5, D-NOR-12. Alcance F1-G2. Experto: no.

**FM-NOR-41 Seleccion e INSERT de las solicitudes aceptadas.** Given no simula. When recorre `solicitudesProcesar`: las `PEN` se agregan a `solicitudesInsertar` y a `P_OUT_RESULTADO` (resultado armado en FM-NOR-28). Then `INSERT INTO FMM.SolicitudAporteRescate` en batch (`ComunesSolicitudes_InsertSolicitud.sql`, ~110 columnas, `FechaIngreso`=`CURRENT_UTCTIMESTAMP`, `SecuenciaMovimiento`=`NEXTVAL`). Si no hay ninguna, no inserta ni emite resultado. Fuente `:1512-1531`. Defectos D-NOR-5. Alcance F1. Experto: no.

**FM-NOR-42 Solicitud AGF.** Given solicitud `PEN` con `SolicitudBatch="N"` (el literal "N"; nulo no cuenta). When despues del INSERT. Then llama `CrearSolicitudAGF` con `p_EsManual="N"`; suma su contador y mensajes. Fuente `:1514-1519,1533-1543`; reglas FM-NOR-51/52. Defectos -. Alcance F1. Experto: no.

**FM-NOR-43 Rescates de portafolio APV sin traspaso -> programa batch.** Given rescate `PEN` de un portafolio de `portafolioAPV` (regimen `APV-A`) cuyo `FolioContrato` no esta en `TRA.Traspaso`, y `p_IdOrigenProceso` distinto de "PAC" (`equalsIgnoreCase`; nulo -> NPE). Then `InscribirProcesoBatch` programa `INGRESAR_ORDEN_FFMM_BONIFICACION_FISCAL` con parametros `IdEmpresa`, `IdUsuario` y un `IdSolicitudAporteRescate` por solicitud, `SegundosDesfase=0`. El resultado de `procesoBatch.logica` no se revisa. Fuente `:1522-1526,1546-1565`. Defectos D-NOR-12. Alcance F1-G5 (H `BatchPort`). Experto: no.

**FM-NOR-44 Log de ingreso.** Given solicitudes insertadas. When arma un `AgregarLog` por solicitud (tipo `INGRESO`, `SOLICITUD_APORTE_RESCATE`, cliente/portafolio/estados, folio, cantidad, precio, monto, `TipoCambio = ParidadFechaInversion.doubleValue()+""`). Then una sola llamada a `AgregarLog.logica`; `cliente` nulo -> NPE. Resultado no revisado. Fuente `:1566-1604`. Defectos D-NOR-9,12. Alcance F1-G5. Experto: no.

**FM-NOR-45 Modo simulacion.** Given `p_SimularOperacion="S"`. Then no inserta, no llama Tesoreria/Spot/AGF/batch/log; solo emite en `P_OUT_RESULTADO` las `PEN`. Fuente `:1606-1612`. Defectos -. Alcance F1. Experto: no.

**FM-NOR-46 Comision de rescate NN (siempre, aun simulando).** Given `solicitudesProcesar` con comision `TipoComision="PER"` (trimestral), `Comision>0`, `CantidadComision>0` y tipo `RES`. When arma una solicitud NN por cada una (**sin mirar `EstadoProceso`: tambien las rechazadas**) con `IdInterno` 1,2,3..., `Monto=1`, `NumeroCuotas=CantidadComision`, `IdSolicitudAporteRescateRelacionado`. Then llama a `CrearSolicitudRescateComisionNN` con `p_SimularOperacion="N"` fijo y `UsaReglasNegocio="S"`, **fuera del `if(!simula)`**; si su contador >0 agrega sus mensajes, **recalcula** el contador de Normal = cantidad de mensajes `ERROR` acumulados, elimina bloqueo y return; si no, agrega sus resultados a `P_OUT_RESULTADO`. Fuente `:1614-1662`. Defectos D-NOR-6, D-NOR-14, D-NOR-7, D-MAN-3. Alcance F1 (solo si `CALCULAR_COMISION_CERTIFICACION=S` y comision trimestral). Experto: si (QD-05).

**FM-NOR-47 Notificacion OWP [G4].** Given `PLA.NotificarEstadoOrdenOWP="S"` y `p_IdTipoCanalIngreso="OWP"` (ignora mayusculas). When relee las solicitudes insertadas (`CertificarSolicitudAporteRescate_SelectSolicitudes.sql`) y llama `NotificarEstadoOrdenesOWP.go` (`Mercado="F"`, `Gastos=0`, `IdClasificacionTipoInstrumento="FM"`). Corre tambien en simulacion, con `solicitudesInsertar` vacio: el `SELECT ... IN (:IdSolicitudAporteRescate)` se ejecuta con lista vacia (comportamiento de `BcsUtils.SQL.query` ante `IN` vacio por verificar con el oraculo; si lanza, toda simulacion OWP con parametro "S" falla). Fuente `:1664-1709`. Defectos D-NOR-9. Alcance F1-G4. Experto: no.

**FM-NOR-48 Cierre exitoso.** Given fin normal. Then elimina el bloqueo y devuelve `out`. `eliminarBloqueo...` (solo si no simula y hubo bloqueo): `CALL EliminarBloqueo...`; si `SQLException` agrega `UI_ERROR_DELETE_BLOQUEO_SOLICITUD` (id 1) **sin subir el contador** (el parametro va por valor); `DROP TABLE #p_...` en `finally` (si falla lanza). Fuente `:1710-1712,1749-1766`. Defectos D-NOR-3. Alcance F1. Experto: no.

**FM-NOR-49 Excepcion.** Given cualquier excepcion en el bloque principal. Then elimina el bloqueo, `printStackTrace()` y relanza (el contador no se devuelve; rollback del anfitrion). Fuente `:1713-1717`. Defectos D-NOR-9,11. Alcance F1. Experto: no.

**FM-NOR-50 Contrato JSON (defaults).** Entrada: `p_IdTipoCanalIngreso="OPT"`, `p_NumeroDividendo=0`, `p_IdOrigenProceso="OPT"`, `p_PermiteOperarProspectos="N"`, listas vacias, `IdMonedaPago=""`, `MontoPatrimonio=0`, `IngresoAutomatico="N"`, `ForzarSpot="N"`, `EsTraspaso="N"`, `IgnorarMontoMinimo="N"`, `SimulaCambioPortafolioEjecutadoOk="N"`, `IdSolicitudAporteRescateRelacionado=0`, `IdOrigenRecaCliente=0`; `FolioContrato`, `IdInterno`, `Monto`, `NumeroCuotas`, `IdMoneda`, `IdRegimenTributario`, `Visado` sin default. `Gson` plano (`new Gson()`): `Date FechaValorCuota` depende del locale de la JVM. Salida: `P_OUT_ERRORESCONTADOR`, `P_OUT_MENSAJES`, `P_OUT_RESULTADO`. Fuente `:117-258,293-302`. Defectos D-NOR-9. Alcance F1. Experto: no.

**FM-NOR-51 AGF: condicion y validaciones de entrada.** Given `FONDOMUTUO/UsaReplicaSigaFondoMutuo` (por defecto "N" si falta). When no es "S" -> `CrearSolicitudAGF` no hace nada (0 mensajes, 0 resultados). Si "S": lista vacia -> contador=1 `NO_SE_INGRESARON_SOLICITUDES`; id repetido -> contador=1 `SOLICITUDES_REPETIDAS`. Fuente `CrearSolicitudAGF.java:206-231`. Defectos -. Alcance F1. Experto: si (replica a SIGA).

**FM-NOR-52 AGF: beneficiarios y solicitudes de la interfaz.** Given solicitudes recien insertadas. When lee de `FMM.ConsultarSolicitudAporteRescate` y beneficiarios aun no presentes en `FMM.InterfazAgf` (clave documento+codigo portafolio). Then solicitud no encontrada -> contador+1 `NO_SE_ENCONTRO_SOLICITUD`; `NumeroCuotas` o `ValorCuota` nulos/0 -> contador+1 `VALOR_CUOTA_VACIO`; inserta beneficiarios (`TipoRegistro="PA"`, `IdEstado="PEN"`, `FechaIngreso=fechaSistema`) y solicitudes (`TipoRegistro="TR"` si movimiento `RESTRANSF`/`RESTRAPOR`, si no "AR"), `IdInterfazAgf = Secuencias::InterfazAgf.NEXTVAL`. Fuente `CrearSolicitudAGF.java:232-298`; SQL `CrearSolicitudAGF_*`. Defectos D-NOR-9. Alcance F1. Experto: no.

### Reglas de colaboradores diferidos (lectura parcial; F2 o detras de compuerta; solo para dimensionar y para los dobles del oraculo)

**FM-NOR-90 Ingreso de Tesoreria por aporte [G1, F2].** Given aporte con medio distinto de `CIN`. When `ProcesarIngresoEgresoTesoreria` arma `IngresoEgreso_Entrada`. Then `IdTipoMovimiento` = `MOVING` (aporte) / `MOVEGR` (rescate); concepto = `APORFM`, o `APODOL`/`RESDOL` si `GeneraSpot="S"`; moneda y monto = (`IdMonedaSistema`,`MontoMonedaSistema`) si `GeneraSpot="S"` si no (`IdMoneda`,`MontoPagoLocal`); `FechaLiquidacion = FechaEmision` (no la `FechaLiquidacion` de la solicitud); persona = cliente (o contraparte si `RelacionTraspaso="S"`); `NumeroReferencia = IdInterno`; `IdComprobante = IdOperacion = IdSolicitudAporteRescate`; `TitularPago` siempre "N" (compara `IdPersona` consigo). Fuente `ProcesarIngresoEgresoTesoreria.java:195-258`. Defectos D-TES-1. Alcance F2. Experto: si (contable).

**FM-NOR-91 Forma de pago y llamada a Tesoreria [G1, F2].** Given el ingreso. When arma `FormaDePago_Entrada`. Then medio de pago = el de la solicitud (o `TRA` si `CIN` con Spot); modalidad por `PAG.MedioDePagoYModalidadDePago` (por defecto `MODCAJ`); cuenta del socio de la preferencia de pago del portafolio; cuenta de la corredora solo si `SecuenciaRelacionada != null`; `CrearIngresoEgreso` con `p_SimularOperacion` nulo := "N". Salida: contador, ingresos generados, resultado por proceso. Fuente `:260-321`. Defectos D-TES-1. Alcance F2. Experto: no.

**FM-NOR-92 Operacion Spot por aporte con `GeneraSpot` [G2, F2].** Given aportes Spot. When `ProcesarIngresoOperacionSpot` consulta fecha de sistema, moneda sistema, mesa por defecto, dolar observado (`CalcularParidad` USD->sistema, 0 si falta), instrumento Spot (`DISTRI`) y puntas por tramo (canal `Punta`, USD); arma una `OperacionObjIn` por solicitud (venta Spot para aportes, compra para rescates; `FechaOperacion` = `FechaAceptacion` aporte / `FechaCertificacion` en otro caso; hardcode `IdTipoDolar=4`, `CodigoComercio=300206`) y llama `CrearOperacionSpot`. Fuente `ProcesarIngresoOperacionSpot.java:142-170,316-457`. Defectos D-SPOT-1,2. Alcance F2. Experto: si (mesa).

**FM-NOR-93 Validacion de flujo de caja del Spot con parametros cruzados.** Given rescate normal (`RESNORM`) -> lee `VALIDA_FLUJO_CAJA_SPOT_PARA_PAC`; given canal `PAC` -> lee `VALIDA_FLUJO_CAJA_SPOT_PARA_RESCATES_NORMALES`; si el parametro es "N" no valida, si no valida; cualquier excepcion -> valida "S". Fuente `ProcesarIngresoOperacionSpot.java:332-346`. Defectos D-SPOT-1. Alcance F2. Experto: si.

**FM-NOR-94 Punta por tramo.** Given monto de la operacion. When `monto < MontoPorTramo` del primer tramo -> punta del primer tramo; si no, invierte la lista y toma el primero con `monto >= MontoPorTramo`; sin puntas -> 0. `TipoCambioCosto` = punta de compra/venta segun operacion. Fuente `:278-313`. Defectos -. Alcance F2. Experto: si.

## 12. Tarjetas FM-FEC (CalcularFechaSolicitudAporteRescate.java)

**FM-FEC-01 Preparacion por solicitud.** Given `InFechas` (serie, estados, valutas, paises). When `obtenerSolicitudesAProcesar` arma `ProcesamientoFechas`: valutas/retencion nulas -> 0; `IdPaisLocal := IdPaisEmisor` (no el pais local); `IdPaisEmpresa` = pais de `ConsultarEmpresa`; `MoneyMarket` de la serie (o "N"); parametro `PERMITE_DIA_NO_HABIL_FECHA_VALOR_CUOTA_MONEY_MARKET` por defecto "N". Fuente `:377,472-512,558-569`. Defectos D-FEC-1. Alcance F1. Experto: si (QD-02).

**FM-FEC-02 Separacion Switch / no Switch.** Given `Switch`. When `"S"` -> fechas del rescate de origen (`FechaCertificacionRescateOrigen` para digitacion, emision, curse y certificacion; `FechaValorCuotaRescateOrigen`; `FechaLiquidacionRescateOrigen`); `"N"` -> flujo normal; otro valor (o nulo) -> la solicitud se pierde (no entra a ninguna lista). Normal siempre envia "N". Fuente `:380-397,514-541`. Defectos D-FEC-2. Alcance F1 (rama Switch es de reinversion, F2). Experto: no.

**FM-FEC-03 Bandera "dia no habil para valor cuota".** Given aporte o rescate. When aporte: `serie.MoneyMarket="S"`; rescate: `MoneyMarket="S"` o `IdMercadoFondoMutuo="RF"`; tipo distinto -> excepcion `Tipo Solicitud no valida.` Then `PermiteDiaNoHabilFechaValorCuota = esMoneyMarket && parametro="S"`. Fuente `:571-604`. Defectos -. Alcance F1. Experto: si (QD-02).

**FM-FEC-04 Seleccion de la configuracion por tipo de fecha.** Given filas activas de `FMM.ConfiguracionFechasFondoMutuo` de la empresa. When filtra: `TipoSolicitud`, `EstadoFondo`, `FondoExtranjero` de la fila nulos o iguales (ignora mayusculas); y modalidad: si el parametro `MODALIDAD_SOLICITUD_FECHA_APORTE_RESCATE` es nulo/vacio -> `UNITARIO` con `SolicitudBatch="N"` o `BATCH` con "S"; si no, la fila cuya modalidad = parametro. Then por `IdTipoFecha` (`DIGITA`,`CURSE`,`VCUOTA`,`CERTIF`,`LIQUID`) guarda `CantidadDiasValuta`, `EsHabil`, `IdTipoFechaOrigen`, `DebeSerHabilFechaOrigen` y paises; **si varias filas cumplen, la ultima pisa**. Fuente `:606-731`. Defectos D-FEC-2. Alcance F1. Experto: si (calendario regulatorio).

**FM-FEC-05 Valuta total.** Given fila de configuracion. When suma `CantidadDias` + valuta de curse si `ValutaCurse="S"` + de certificacion si `ValutaCertificacion="S"` + retencion si `ValutaRetencion="S"` + valor cuota si `ValutaValorCuota="S"` (valores de la serie). Fuente `:733-766`. Defectos -. Alcance F1. Experto: si.

**FM-FEC-06 Pais del calendario.** Given flags `CalendarioLocal/Emisor/Fondo/Moneda`. When "S" -> el pais de la solicitud; si no el de la empresa (moneda tambien "S" si `FondoNacionalDolar="S"`). Pais de calculo (1a pasada) = primer pais >0 en orden emisor, fondo, local, moneda, empresa; 2a pasada orden fondo, emisor, local, moneda, empresa, **o el de la moneda para rescate de fondo nacional dolar**. Fuente `:768-794,821-848`. Defectos D-FEC-1. Alcance F1. Experto: si.

**FM-FEC-07 Orden de calculo.** Given configuracion. When calcula con `ObtenerDiaSiguiente` (dia habil y "solo cae dia habil" = "S") en este orden: Digitacion (desde `FechaEmision`), Curse (desde Digitacion), luego ValorCuota, Certificacion, Retencion (liquidacion) todos desde Curse. Falta de configuracion de un tipo de fecha -> NPE. Fuente `:796-811,860-908`. Defectos D-FEC-2,4,10. Alcance F1. Experto: no.

**FM-FEC-08 Certificacion y liquidacion con dos calendarios.** Given certificacion o retencion y `IdPaisEmisor != paisCalculo`. When compara la fecha calculada con calendario del emisor y del fondo (con las valutas de la serie si fondo y liquidacion estan `A`, con los dias de configuracion si no) y la **mayor** gana (`compareTo` de textos ISO). Fuente `:910-929`. Defectos D-FEC-10. Alcance F1. Experto: si (QD-02).

**FM-FEC-09 Fecha valor cuota.** Given `PermiteDiaNoHabilFechaValorCuota`. When "S": rescate -> fecha de certificacion menos 1 dia; aporte -> fecha de curse menos 1 dia. When "N": calculo normal; si emisor != fondo, la mayor de los dos calendarios. Fuente `:931-963`. Defectos D-FEC-10. Alcance F1. Experto: si (QD-02, QD-13).

**FM-FEC-10 Repaso de certificacion/liquidacion para series extranjeras.** Given `IdPaisEmpresa` distinto del pais emisor de la serie, del pais del fondo o del pais de la moneda. When la fecha de certificacion o liquidacion cae en feriado (`REG.DiaFeriado`, activo) de empresa, fondo, emisor o moneda. Then recalcula con `ObtenerDiaHabilMultiplesPaises` (1 dia habil siguiente en todos los paises); los resultados se encuentran por `IdInterno*3+1` (certif.) y `+2` (liquidacion). Fuente `:1164-1252,1301-1332,1435-1447`. Defectos D-FEC-3,5. Alcance F1. Experto: si (QD-02).

**FM-FEC-11 Repaso certificacion de rescate que genera Spot.** Given `RescateGeneraSpotCertificacion="S"` y serie extranjera. When la certificacion es anterior a la liquidacion-menos-1-dia habil local, o igual a la liquidacion. Then certificacion := dia anterior habil a la liquidacion si `emision + 2 dias habiles <= ese dia`, si no la liquidacion. Fuente `:1093-1162`. Defectos -. Alcance F1. Experto: si (QD-02).

**FM-FEC-12 Repaso de fondo nacional dolar.** Given `FondoNacionalDolar="S"`. When recalcula curse, certificacion, liquidacion y valor cuota con feriados de `IdPaisFondo` y `IdPaisMoneda`: rescate no RF -> valor cuota = curse; en otro caso `ObtenerDiaAnterior` (1 dia, para pais de la moneda y pais del fondo) con `diaHabil`: lunes -> rescate no RF "S", rescate RF "N", aporte no money market "S", aporte money market "N"; otro dia de semana -> rescate "S", aporte "N"; y `soloCaeDiaHabil`: aporte = "S" si es lunes y el fondo no es money market, si no "N"; rescate = "N" si es lunes y (money market o RF), si no "S" (dia de la semana con `Calendar` de la JVM, `DAY_OF_WEEK==2`). Valor cuota = el mayor entre USA y pais del fondo si `PermiteDiaNoHabilFechaValorCuota`; aporte de fondo USD no money market con parametro "S" -> valor cuota = curse. Rescate RF de fondo nacional dolar excluye la consulta de feriados. Fuente `:409-461,965-1084`. Defectos D-FEC-6. Alcance F1. Experto: **si, regulatorio** (QD-02, QD-13).

**FM-FEC-13 Resultado.** Given fechas calculadas. Then `FechaAceptacion := FechaCurse`; devuelve emision, digitacion, aceptacion, valor cuota, certificacion, liquidacion como `Date`. Fuente `:1473-1489`. Defectos -. Alcance F1. Experto: no.

**FM-FEC-14 Consultas de apoyo.** `SelectConfiguracionFechasFFMM` (tabla `FMM.ConfiguracionFechasFondoMutuo`, `IdEmpresa`, `IdEstado='ACT'`), `SelectDiaEsFeriado` (`REG.DiaFeriado`, `IdEstado='ACT'`, `Fecha IN`, `IdPais IN`), `SelectSerieFondo` (compartido). Los SQL `CallCalcularFecha...`/tabla temporal del mismo prefijo no se cargan (confirmado). Fuente `:48-50`. Defectos D-SQL-1. Alcance F1. Experto: no.

## 13. Tarjetas FM-CUO (CalcularMontoNumeroCuota.java)

**FM-CUO-01 Parametros obligatorios.** Given empresa. When lee `FONDOMUTUO/UsaDolarObservado` y `PLATAFORMA/PLA.MonedaSistema`. Then si falta alguno la excepcion de `ConfiguracionSistema` aborta la peticion. Fuente `:191-192`. Defectos -. Alcance F1. Experto: no.

**FM-CUO-02 Precio por solicitud.** Given series. When llama `EntregarPreciosTasas` (`FechaConsulta = Serie.Fecha`, `IdRegistro = IdInterno`, uso `CARTERA`, `GARANTIA` si `INGGTA`) y arma mapa por `IdRegistro` (`toMap`: IdInterno repetido o 0 repetido lanza). Sin series -> `obtenerPrecios` devuelve `null` y `hashVCTP.size()` lanza NPE. Fuente `:200,464-514,224`. Defectos D-CUO-6. Alcance F1 (H `PreciosTasasPort`). Experto: no.

**FM-CUO-03 Sin datos de valor cuota.** Given consulta `SelectValorCuotaTipoCambio` vacia -> un resultado `ERROR_VALORCUOTA` por solicitud (con IdInterno); precios o decimales vacios -> resultados `ERROR_VALORCUOTA` **sin IdInterno** por serie. Normal los ignora (no encuentra `vc`) y la solicitud cae en FM-REQ-16. Fuente `:205-233`. Defectos D-CUO-7. Alcance F1. Experto: no.

**FM-CUO-04 Monto por cuota.** Given `NumeroCuotas != null` -> `MontoPorCuota = ValorCuota * NumeroCuotas` (DECIMAL128); nulo -> `MontoPorCuota = Monto`. Fuente `:262-268`. Defectos D-CUO-2. Alcance F1. Experto: no.

**FM-CUO-05 Moneda de inversion distinta de la del fondo (diferido).** Given `IdMonedaInversion != IdMonedaFondo`. When `UsaDolarObservado="N"`: fondo USD -> paridad = tipo cambio de la serie (`ValorDolarAporte`/`Rescate`), destino = Monto/paridad (modo M) o MontoPorCuota*paridad; fondo en moneda sistema e inversion USD -> inverso; otro caso -> `CalcularParidad`. Con "S" siempre `CalcularParidad`. Luego modo M: `NumeroCuotas = destino/ValorCuota` redondeado `HALF_DOWN` a `DecimalesCuota`; `Monto = formato(destino, moneda fondo)`, `MontoDestino = Monto original`; otros modos: `Monto = formato(MontoPorCuota)`, `MontoDestino = destino`. Fuente `:271-333`. Defectos D-CUO-3. Alcance F1. Experto: **si, regulatorio** (QD-03).

**FM-CUO-06 Misma moneda.** Given monedas iguales. Then paridad 1; modo M: cuotas = Monto/ValorCuota (`HALF_DOWN`); otros modos: si `MontoPorCuota=0` y valor cuota !=0 cuotas = Monto/ValorCuota, si no `Monto = formato(MontoPorCuota)`; `MontoDestino = MontoMonedaSistema = Monto`. Fuente `:334-351`. Defectos D-CUO-3. Alcance F1. Experto: si (QD-03).

**FM-CUO-07 Valor cuota cero se omite del calculo.** Given `ValorCuota.equals(BigDecimal.ZERO)` (sensible a escala: `0.00000000` NO es igual). Then `continue`. Un cero con escala >0 continua y divide por cero. Fuente `:300-303`. Defectos D-CUO-1. Alcance F1. Experto: no.

**FM-CUO-08 Moneda sistema, UF y factor.** Given cada serie. When arma 3 calculos de paridad (trabajo 2, `IdInterno*4 + k*signo`): monto a moneda sistema (si moneda fondo != sistema), monto a `UF`, factor UF->sistema. Then `ValorFactorAjuste`, `CostoEnFactorAjuste`, `MontoMonedaSistema` (formateados) o 0 si falta el calculo. Fuente `:353-389,402-446`. Defectos -. Alcance F1 (H `ParidadPort`). Experto: no.

**FM-CUO-09 Numero de cuotas final.** Given resultado. Then `NumeroCuotas = serie.NumeroCuotas` con escala `DecimalesCuota` (`HALF_DOWN`); si es 0 -> `Monto/ValorCuota` con **`DecimalesCuota+1` y `HALF_UP`**; `serie.NumeroCuotas` nulo en esa comparacion -> NPE. Fuente `:411-429`. Defectos D-CUO-2,3. Alcance F1. Experto: si (QD-03).

**FM-CUO-10 Estado del resultado.** `CodigoMensaje="OK"`, salvo `ValorCuota==0` -> `ERROR_VALOR_CUOTA`. `FechaValorCuota` = fecha fuente del precio (no se usa en Normal). Fuente `:438-445`. Defectos D-NOR-19. Alcance F1. Experto: no.

## 14. Tarjetas FM-COM (CalcularComisiones.java y CrearSolicitudRescateComisionNN.java)

**FM-COM-01 Parametros.** `CALCULAR_COMISION_VALOR_CUOTA_ACTUAL` por defecto "S" si falta; `PLA.FechaSistema`, `PLA.MonedaSistema`, `PLA.PaisSistema` obligatorios. Fuente `COM:181-188,318,545`. Defectos -. Alcance F1. Experto: no.

**FM-COM-02 Lotes de custodia FIFO.** Given cada solicitud (aporte o rescate). When consulta `ConsultarFlujoCustodia` (uso `RESCFM`, agrupado por instrumento) y ordena el detalle por `FechaOrigenCompra` ascendente. Then consume cantidad por lote (`cuotaRetirar = min(Cantidad, lote)`) mutando `cartera.Cantidad`, hasta agotar. Tambien para aportes. Fuente `COM:191-300`. Defectos D-COM-6. Alcance F1 (H `CustodiaPort`). Experto: si (QD-12).

**FM-COM-03 Valor cuota para comision.** Given `FechaValorCuota` de la solicitud. When `EntregarPreciosTasas`. Then precio por IdInterno; con parametro "N" y rescate se usa el precio de compra del lote. Fuente `COM:316,339,351-353,539-575`. Defectos D-NOR-19. Alcance F1. Experto: si (QD-04).

**FM-COM-04 Comision de entrada/salida por tramos.** Given aporte -> `ENTRADA`, otro -> `SALIDA`; solo si movimiento != `RESREINV`, serie no APV y `EsTraspaso="N"` (siempre "N", el dato de entrada se ignora). Then por cada lote y tramo de la serie con ese tipo (distinto de `PER`): dias = (`APOREI`: ingreso de cartera -> curse; si no ingreso de inversion -> curse, o ingreso de cartera -> fecha sistema si no hay curse) por division de milisegundos; si `DiasDesde <= dias <= DiasHasta`: comision += cantidad*precio*porcentaje/100 con escala 8 `HALF_DOWN`. Fuente `COM:337-379,502-510`. Defectos D-COM-1,2. Alcance F1. Experto: si (QD-05).

**FM-COM-05 Comision trimestral `PER`.** Given tramo `PER` de la serie. When `comisionDiaria = %/100/365 * 1.19` (`new BigDecimal(1.19)`, escala 8 `HALF_DOWN`); dias = duracion del trimestre de ingreso si el trimestre actual es otro y la fecha de sistema es posterior, si no dias entre ingreso y fecha de sistema; `CantidadComision` = cantidad*diaria*dias (escala 8). `CantidadCarteras` baja 1 por cada tramo `PER` iterado; solo cuando llega a 0 se fija `Comision = suma cantidades * precio`. Fuente `COM:380-418,521-537`. Defectos D-COM-2,3,4. Alcance F1. Experto: **si, regulatorio/tributario** (QD-05).

**FM-COM-06 Conversion de monedas de la comision.** Given comision en moneda del fondo. When 4 calculos de paridad (a moneda de operacion, a sistema, tipo de cambio de fondo-local y fondo-sistema, `IdInterno*5+k`). Then si falta uno el valor es **1**. Fuente `COM:424-491`. Defectos D-COM-5. Alcance F1. Experto: no.

**FM-COM-07 NN: validaciones de entrada.** Mismas que Normal: requisitos basicos, lista vacia (`NO_SE_INGRESARON_SOLICITUDES_RESCATE`), `ID_INTERNO_NO_CERO`, `CORRELATIVO_INCORRECTO` (contador=1). Fuente `NN:131-159`. Defectos -. Alcance F1. Experto: no.

**FM-COM-08 NN: fechas y cuota.** Given rescate de comision. Then fechas con estado `A`, valutas 0 si `RespetaValutasCargaMasiva="N"`, calculo por cuotas (`C`), tipo movimiento `RESCNN`, medio `TRA`, `Visado="S"`, `SolicitudBatch="S"`, origen `M`, canal "" (o el recibido), `MontoPago = Monto`, comision 0. Fuente `NN:217-425`. Defectos D-COM-7. Alcance F1. Experto: si.

**FM-COM-09 NN: los errores de requisitos se degradan a advertencia.** Given mensajes de `ServicioCumple...`. Then todos se copian como `ADVERTENCIA` con id = folio de cartera; los `ERROR` rechazan igual la solicitud NN; el contador nunca sube. Sin pendientes -> return sin escribir. Fuente `NN:529-557`. Defectos D-COM-7. Alcance F1. Experto: si.

**FM-COM-10 NN: insercion y log.** Given pendientes y no simula. Then `INSERT` batch (`ComunesSolicitudes_InsertSolicitud`) + `AgregarLog` (`SOLICITUD_RESCATE_COMISION`); `P_OUT_COMISIONES_INSERTADAS`. Normal la invoca con "N" fijo (FM-NOR-46). Fuente `NN:559-621`. Defectos D-NOR-6. Alcance F1. Experto: no.

## 15. Tarjetas FM-REQ (requisitos y validaciones: ServicioCumple..., ValidaMontoInversion, ValidaPatrimonioEmisor, ConsultarFlujoCustodiaFondoMutuo, ObtenerPerfilFondoMutuo)

Salvo indicacion, fuente `ServicioCumpleRequisitosCrearSolicitudAporteRescate.java` (Cumple). Cada regla emite un `MensajeSalida` con id = IdInterno y texto del `Diccionario`.

**FM-REQ-01 Parametro de reinversion entre familias.** `VALIDAR_REINVERSION_DISTINTAS_FAMILIAS`, "S" si falta. Fuente `:174-180,406-410`. Alcance F1 (inerte en Normal). Experto: no.

**FM-REQ-02 Cliente y portafolio.** Error `CLIENTE_NO_EXISTE`, `CLIENTE_BLOQUEADO` (estado cliente != `ACT`, salvo `p_PermiteOperarProspectos="S"` y `EsClienteNuevoProspecto="S"`), `PORTAFOLIO_NO_EXISTE`, `PORTAFOLIO_NO_HABILITADO` (!= `ACT`). Con Normal, un portafolio inexistente nunca llega aqui (NPE antes en `:196` y Normal `:1038`). Fuente `:221-238`. Defectos D-REQ-4. Alcance F1. Experto: si (prospectos).

**FM-REQ-03 Cliente/portafolio destino.** Solo si el destino es distinto de 0; Normal siempre manda 0, regla inerte. Fuente `:239-267`. Alcance F2. Experto: no.

**FM-REQ-04 Serie.** `IdSerieFondo=""` -> `INSTRUMENTO_NO_EXISTE`; `IdEstadoSerie != ACT` (incluye serie inexistente) -> `INSTRUMENTO_NO_VIGENTE_RUT_CLIENTE`. `cliente` nulo con estos mensajes -> NPE. Fuente `:269-280`. Alcance F1. Experto: no.

**FM-REQ-05 Tipo de movimiento valido.** `IdTipoMovimientoFondoMutuo=""` -> `TIPO_OPERACION_NO_VALIDA_REQ_APO_RES`. Fuente `:282-287`. Alcance F1. Experto: no.

**FM-REQ-06 Fondo cerrado es solo advertencia.** `EstadoFondo != A` -> `FONDO_CERRADO_REQ_APO_RES` tipo **ADVERTENCIA** (no rechaza; la solicitud sigue con fechas del dia habil siguiente). Fuente `:289-294`. Defectos -. Alcance F1. Experto: **si, regulatorio** (QD-01).

**FM-REQ-07 Fondo bloqueado/invalido.** `EstadoBloqueo != "N"` -> `FONDO_BLOQUEADO_REQ_APO_RES`; `EsFondoValido != "S"` -> `FONDO_INVALIDO_REQ_APO_RES`. Fuente `:296-308`. Alcance F1. Experto: no.

**FM-REQ-08 Serie restringida.** `RestringeAporte != "N"` y operacion `APORFM` -> `FONDO_SERIE_RESTRINGIDO_APORTE_REQ_APO_RES`; `RestringeRescate != "N"` y `RESCFM` -> `..._RESCATE_...`. Fuente `:310-322`. Alcance F1. Experto: no.

**FM-REQ-09 Fechas validas.** `EsFechaValidaAporte/Rescate` siempre "S" desde Normal: inertes (el segundo mensaje usa el mapa de parametros equivocado). Fuente `:324-334`. Defectos D-REQ-2. Alcance F1. Experto: no.

**FM-REQ-10 Fondo no diferido.** `EsDiferido != "S"` y movimiento != `RESTRASP` y glosa != "DESINVERSION APV" -> `FONDO_NO_DIFERIDO_REQ_APO_RES`. Fuente `:336-341`. Alcance F1. Experto: si (moneda distinta en fondos no diferidos).

**FM-REQ-11 Cambio de fondo.** Solo `RESREINV`/`APOREINV`: inerte en Normal. Fuente `:343-348`. Alcance F2. Experto: no.

**FM-REQ-12 Monto maximo.** `EsMontoMaximo != "S"` -> `MONTO_MAXIMO_REQ_APO_RES`. Fuente `:350-356`. Alcance F1. Experto: si (QD-07).

**FM-REQ-13 Monto minimo (no aplica a PAC).** `ProcedenciaCanal != "PAC"` y `EsMontoMinimo != "S"` -> `MONTO_MINIMO`. Fuente `:358-366`. Alcance F1. Experto: si (QD-10).

**FM-REQ-14 Cumplimiento.** `INGTIT` y cliente bloqueado por cumplimiento -> `CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO`: inerte en Normal. Fuente `:368-377`. Alcance F2. Experto: no.

**FM-REQ-15 Overnight sin liquidez.** `Overnight="S"` y `EstadoLiquidez != A` -> advertencia `FECHA_LIQUIDEZ_CERRADO`; Normal manda `Overnight="N"` (inerte). Fuente `:386-388`. Alcance F2. Experto: no.

**FM-REQ-16 Valor cuota, monto y cuotas positivos.** `ValorCuota <= 0` -> `VALOR_CUOTA_INCORRECTO`; `Monto <= 0` -> `MONTO_VALIDO`; `NumeroCuotas <= 0` -> `NUMERO_CUOTA_VALIDO_REQ_APO_RES`. Es la unica via por la que un precio faltante rechaza. Fuente `:382-404`. Alcance F1. Experto: no.

**FM-REQ-17 Banderas de regimen, mandato, proposito, cuenta inversion, permisos.** Errores `NO_MONEY_MARKET`, `PORTAFOLIO_REGIMEN_INVALIDO`, `REGIMEN_NO_EXISTE_REQ_APO_RES`, `RESCATE_MANDATO_NO_PERMITIDO`, `CAMBIO_GRUPO_REGIMEN_NO_PERMITIDO`, `PORTAFOLIO_DESTINO_NO_COINCIDE_PROPOSITO`, `PORTAFOLIO_NO_OPERA_CUENTA_INVERSION_FFMM`, `REGIMEN_NO_PERMITE_APORTE/TRASPASO/RESCATE`, `COMBINACION_REGIMEN_REINVERSION_NO_CONFIGURADO` cuando la bandera != "S" (las llena FM-NOR-23/24). Fuente `:390-453`. Alcance F1. Experto: si (QD-14).

**FM-REQ-18 Se conserva un solo mensaje por texto.** Los mensajes de la primera fase se pasan por `toMap(descripcion)` con "gana el primero": un mismo texto para dos solicitudes (frecuente: textos sin parametros, o mismo cliente y serie) deja solo el de la primera; la segunda **no se rechaza**. El orden de los mensajes sale de `HashMap.values()` (no determinista). Fuente `:456-465`. Defectos D-REQ-1. Alcance F1. Experto: **si, critico**.

**FM-REQ-19 Producto del portafolio.** `ConsultarProductosPortafolio` (Plataforma): la solicitud necesita un detalle con mismo portafolio y tipo de producto `INFM`, operacion "0" o igual, estado `ACT`; si no -> `PORTAFOLIO_NO_OPERA_PRODUCTO_SELECCIONADO` (no pasa por el filtro de FM-REQ-18). Fuente `:467-485`. Alcance F1 (K). Experto: no.

**FM-REQ-20 Perfil del usuario.** `SELECT TOP 1 "Key" FROM FMM.ConfiguracionFondoMutuo JOIN ACC.ConsultarRolesPerfilesUsuario ... WHERE Key IN (PERFIL_COMERCIAL, PERFIL_BACKOFFICE) AND IdUsuario`; sin `ORDER BY`. Sin fila -> perfil nulo. Fuente `ObtenerPerfilFondoMutuo.java:55-68`; `Comunes_SelectPerfilFondoMutuo.sql`. Defectos D-REQ-8. Alcance F1. Experto: si (QD-09).

**FM-REQ-21 Usuario-empleado.** `ACC.Usuario` (`TipoEntidad='EMP'`): fila con `IdEntidad` nulo -> `ERROR_AL_ENCONTRAR_ID_USUARIO` para todas las solicitudes (con `p_IdUsuario` como empresa del diccionario); sin fila no hay error. Fuente `:487-503`. Defectos D-REQ-3. Alcance F1. Experto: no.

**FM-REQ-22 Monto total de inversion.** Agrupa por `FolioContrato` (0 = cada solicitud sola), suma `Monto` (y `Monto` de series `AportesAltos="S"`), y por solicitud: error `UI_ERROR_ALTOS_APORTE_FFMM` si total altos < minimo (solo `ValidaMontoMinimo="S"`) o > maximo; error `UI_ERROR_MONTO_TOTAL_INVERSION_FFMM` si total < `MontoMinimoTotalInversion` o > `MontoMaximoTotalInversion`. Parametros `MontoMinimo/MaximoTotalInversion`, `MontoMinimo/MaximoAportesAltos` ausentes = 0 (maximo 0 rechaza todo monto positivo). Fuente `ValidaMontoInversion.java:113-318`. Defectos D-REQ-6. Alcance F1. Experto: **si** (QD-07).

**FM-REQ-23 Patrimonio minimo del emisor.** Para series `PatrimonioAltos="S"`: custodia del cliente con el mismo emisor (`CUS.Cartera` vigente, `FechaLiquidacion <= fecha`, valorizada con `FMM.ValorCuota`), convertida a la moneda de la serie con `CalcularParidad`, mas `MontoPatrimonio`; si es menor que `serie.MontoPatrimonioEmisor` -> `UI_PATRIMONIO_MINIMO_EMISOR_INSUFICIENTE`; serie inexistente -> mismo error. Borra e inserta la tabla de paso global por cliente. Fuente `ValidaPatrimonioEmisor.java:121-229`; SQL `ValidaPatrimonioEmisor_*`. Defectos D-REQ-7, D-NOR-18. Alcance F1. Experto: **si, regulatorio** (QD-15).

**FM-REQ-24 Flujo de custodia disponible.** Para cada cartera x instrumento: `CantidadDisponible` = cantidad del flujo totalizado (mismo cliente, portafolio, instrumento y regimen o regimen vacio -> primero); si existe una solicitud en `ING` con `CalculoPorMontoCuota='T'` (rescate total en transito, `FolioOmnibus=0`) para esa cartera/instrumento/regimen -> disponible 0 y `RescateTotal="S"`; valoriza con `CalcularMontoNumeroCuota` (modo cuotas). No descuenta rescates parciales ya ingresados. Fuente `ConsultarFlujoCustodiaFondoMutuo.java:160-384`; SQL `..._SelectRescateTotalTransito`. Defectos D-REQ-9,10. Alcance F1 (H `CustodiaPort`). Experto: si (QD-08).

**FM-REQ-25 Coincidencia de flujo con la solicitud.** Mismo portafolio e instrumento; regimen de la solicitud vacio o nulo coincide con cualquier regimen. Fuente `FondoMutuoUtils.java:16-30`. Defectos D-REQ-9. Alcance F1. Experto: no.

Fin del documento (paso 1A). Plan no modificado.

## 6b. Resultado del catalogo real (HANA QA, solo lectura, 2026-10-22)

Programa `tools/HanaCatalogoFondoMutuoSolicitud.java` (solo `SELECT` sobre `SYS.*`); resultados en `hana_qa_results/fmm_*.csv` (562 columnas de tablas, 334 de vistas, 28 tablas, 9 vistas, 38 filas de claves, 5 secuencias, 2 procedimientos).

- **`GBL.ValidaPatrimonioEmisor_Proceso_Paso` es tabla temporal GLOBAL** (`IS_TEMPORARY=TRUE`, `GLOBAL`, tipo ROW): los datos viven por sesion, **no hay carrera entre sesiones** en el `DELETE` de `ValidaPatrimonioEmisor` (se descarta el riesgo planteado en el paso 6). Debe usarse UNA conexion por llamada.
- **`FMM.ValorCuota`: la clave primaria es solo `IdSerieFondo`** (una fila por serie): la tabla **no guarda historia por fecha**; guarda el ultimo valor cuota con `FechaCierre`/`FechaCarga`. Relevante para D-REQ-7 (fecha de precio frente a `FechaValorCuota`).
- **Procedimientos `CrearBloqueoSolicitudAporteRescate` y `EliminarBloqueoSolicitudAporteRescate`: ambos `AUTONOMOUS TRANSACTION`** (confirmado), no son de solo lectura. El bloqueo se confirma al instante en su propia transaccion.
- Tabla de bloqueo: `Monto` y `NumeroCuotas` son `DECIMAL(26,8)`; `IdBloqueoSolicitudAporteRescate` NVARCHAR(120).
- Secuencias reales: `FondoMutuo.BD.Secuencias::SolicitudAporteRescate` (inicio 11.413.788, sin ciclo), `::FolioContrato` (inicio 100.049.222), `::InterfazAgf`; `Plataforma.BD.Secuencias::SecuenciaMovimiento` es **ciclica** (0..99.999.999, `IS_CYCLED=TRUE`).
- Los tipos y escalas reales sustituyen a los supuestos del oraculo (`schema-types.sql`).

## 6c. Mezcla de uso real de `FMM.SolicitudAporteRescate` (HANA QA, agregados sin datos personales, 2026-10-22)

Programa `tools/HanaUsoSolicitudAporteRescate.java` (solo `SELECT` agregados); resultados `hana_qa_results/uso_sar_*.csv`. Total **9.027.151 solicitudes** (2008-09-25 a 2026-10-19). Son solicitudes persistidas (historico), no llamadas.

- Por tipo: APO (aporte) 7.569.0xx (CRT 7.562.384, ANU 5.168, ING 1.225, PEN 16) y RES (rescate) 1.458.358 (CRT 1.454.507, ANU 3.344, ING 507).
- Medio de pago: aportes sin medio 4.711.858, **CIN (cuenta de inversion) 2.856.920**, TRA 15; rescates sin medio 463.181, **TRA 520.035**, **CIN 346.711**, TLR 85.962, VVI 42.462, CHE 7. Para F1 (compuertas) importa que **el 33,8 % de los aportes y el 23,8 % de los rescates usan CIN** y que TRA/TLR/VVI (Tesoreria) quedan tras compuerta.
- Todas con `OrigenOperacion='M'`; `SolicitudBatch='S'`: 1.020.229 aportes y 72.178 rescates (14 % y 5 %).
- Modulo origen: FMM 6.002.620 aportes y 1.424.724 rescates; CUS 904.151; APV 506.920 y 33.634; TES 155.102.
- **Tipo de movimiento (aportes):** `APONORM` 2.804.556 (la ruta de `CrearSolicitudAporteRescateNormal`), `APODIV` 3.170.009, `APOREINV` 1.290.736, `APOTRASP` 207.757, `APOFUSION` 53.198: **solo ~37 % de los aportes historicos son "normales"**; el resto pasa por otras clases `CrearSolicitud*` (fase F2). Conclusion: F1 cubre la ruta normal pero no la mayoria del volumen historico de aportes; el Manejador sirve solo las solicitudes cuya unica lista es Normal.
