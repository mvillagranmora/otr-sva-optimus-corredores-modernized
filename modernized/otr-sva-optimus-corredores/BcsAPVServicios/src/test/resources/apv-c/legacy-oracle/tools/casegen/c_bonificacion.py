"""Family C cases: ProcesarBonificacion (APV-C-340 .. 349) and CargaBonificacionTGR (APV-C-270 .. 289)."""
from fix import T_CARGA, T_INVBON, T_V_CLIENTE, ins
from lib import L, caso, seed, tabla

T_DJ = 'BSCL.Normativa.BD.Vistas::TRI.DJ1871.Datos'
T_CART = 'BSCL.Custodia.BD::CUS.Cartera'
T_VCART = 'BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera'
T_POR = 'BSCL.Portafolio.BD::POR.Portafolio'
T_MATRIZ = 'BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV'
T_REG = 'BSCL.APV.BD::REC.RegimenTributario'
T_ORDT = 'BSCL.APV.BD::TRA.OrdenTraspaso'
T_TRAS = 'BSCL.APV.BD::TRA.Traspaso'

# ------------------------------------------------------------------------------------------------ ProcesarBonificacion
F = 'ProcesarBonificacion.java'
TBON = tabla(T_INVBON, '"IdBonificacion"', columnas=['IdBonificacion', 'IdCliente', 'AnoAhorro', 'IdEstado', 'MontoCalculado', 'PorcentajeEgreso', 'PorcentajeTraspasoEgreso', 'MontoEgreso',
                                                      'MontoTraspasoEgreso', 'MontoTraspasoDevolucion', 'MontoEgresoDevolucion'], enmascarar=['FechaCalculado'])


def ent(ano=2024, empresa=1):
    return {'p_IdEmpresa': empresa, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_AnoAhorro': ano}


def bon(idb, idcli, reaj, ano=2024, estado='ING', empresa=1):
    return ins(T_INVBON, IdBonificacion=idb, IdCliente=idcli, AnoAhorro=ano, IdEmpresa=empresa, IdEstado=estado, MontoReajustadoPagadoAfiliado=reaj, MontoBonificacionAfiliadoNeto=1, MontoCalculado=0)


def dj(idcli, ahorros, retiros, ano=2024):
    return ins(T_DJ, ID=idcli, IdCliente=idcli, AnoAhorro=ano, AhorrosDeposito=ahorros, RetirosDeposito=retiros, RutCliente='1%d-9' % idcli)


REGIMENES = [ins(T_REG, IdRegimenTributario=1, Codigo='APV-A', IdEstado='VIG'), ins(T_REG, IdRegimenTributario=2, Codigo='APV-B', IdEstado='VIG'),
             ins(T_MATRIZ, IdMatrizAgrupadorAPV=1, IdRegimenTributario=1), ins(T_MATRIZ, IdMatrizAgrupadorAPV=2, IdRegimenTributario=2),
             ins(T_POR, IdPortafolio=901, IdMatrizAgrupadorAPV=1, IdEstado='ACT', IdCliente=701), ins(T_POR, IdPortafolio=902, IdMatrizAgrupadorAPV=2, IdEstado='ACT', IdCliente=701),
             ins(T_POR, IdPortafolio=903, IdMatrizAgrupadorAPV=1, IdEstado='INA', IdCliente=701)]


def cart(idcli, cant, precio, op=5, fop='2024-06-01', efop='2025-02-01', portafolio=901, clase='FM', modulo='FMM', estado='VIG', egr_concepto='RESCAT', ing_concepto='APORTE'):
    return ins(T_CART, IdCliente=idcli, EGRCantidad=cant, PrecioCompra=precio, IdPortafolio=portafolio, IdClasificacionTipoInstrumento=clase, FechaOperacion=fop, EGRFechaOperacion=efop,
               EGRIdOperacion=op, IdModulo=modulo, IdEstado=estado, EGRIdConcepto=egr_concepto, INGIdConcepto=ing_concepto)


def vcart(idcli, cant, precio, op, fop='2024-06-01', efop='2025-02-10', portafolio=901, modulo='FMM'):
    return ins(T_VCART, IdCliente=idcli, EGRCantidad=cant, PrecioCompra=precio, IdPortafolio=portafolio, EGRIdOperacion=op, IdModulo=modulo, IdClasificacionTipoInstrumento='FM',
               FechaOperacion=fop, EGRFechaOperacion=efop, IdEstado='VIG')


def traspaso(idtras, idcli, solicitud=None, orden=None):
    return [ins(T_TRAS, IdTraspaso=idtras, IdCliente=idcli, IdEmpresa=1), ins(T_ORDT, IdTraspaso=idtras, IdSolicitudAporteRescate=solicitud, IdOrden=orden)]


BASE_PB = [REGIMENES, bon(1, 701, 70000), dj(701, 300000, 100000)]

caso(340, 'ProcesarBonificacion', 'sin-bonificaciones-a-procesar', 'no ING bonificacion for the year: the batch update matches nothing, NO_SE_ENCONTRARON_REGISTROS_PARA_PROCESAR (id 1, counted as error)',
     L(F, 'NO_SE_ENCONTRARON_REGISTROS_PARA_PROCESAR'), ent(2030), seed(BASE_PB), tablas=[TBON])
caso(341, 'ProcesarBonificacion', 'sin-retiros-calculado-igual-al-reajustado', 'one client without withdrawals: MontoCalculado = MontoReajustadoPagadoAfiliado, percentages 0, state PRO, message UI_PROCESO_EXITOSO',
     L(F, 'msg.codigo = "UI_PROCESO_EXITOSO"'), ent(), seed(BASE_PB), tablas=[TBON], enmascarar=['FechaCalculado'])
caso(342, 'ProcesarBonificacion', 'retiro-del-anio-descuenta-proporcional', 'a withdrawn portfolio lot (10.5 x 1234.5678) in the process year: percent = withdrawn*100/(ahorros-retiros) and the devolution is withdrawn*reajustado/net, all DECIMAL128',
     L(F, 'montoRestarRetiro = montoRetirada.multiply'), ent(), seed(BASE_PB, cart(701, 10.5, 1234.5678)), tablas=[TBON])
caso(343, 'ProcesarBonificacion', 'retiro-por-traspaso-del-anio-proceso-y-del-ahorro', 'withdrawals that are traspasos (view ConsultarCartera joined to OrdenTraspaso) count as MontoTraspasoEgreso for the process year (2025) and for the saving year (2024)',
     L(F, 'listaCarterasRetiradasTraspasoAnioProceso'), ent(), seed(BASE_PB, traspaso(1, 701, solicitud=61), traspaso(2, 701, orden=62), vcart(701, 4, 1000, 61, efop='2025-02-10'),
                                                                       vcart(701, 2.5, 2000, 62, efop='2024-11-05', modulo='FAC')), tablas=[TBON])
caso(344, 'ProcesarBonificacion', 'retiro-que-es-traspaso-no-cuenta-como-retiro', 'a Cartera row whose EGRIdOperacion is an OrdenTraspaso of the same client is excluded from the plain withdrawals (NOT EXISTS), so only the traspaso counts',
     L(F, 'SQL_SELECT_ConsultarCarterasRetiradas'), ent(), seed(BASE_PB, traspaso(1, 701, solicitud=61), cart(701, 4, 1000, op=61), vcart(701, 4, 1000, 61, efop='2025-02-10')), tablas=[TBON])
caso(345, 'ProcesarBonificacion', 'filtros-de-cartera-excluyen-las-filas', 'portfolio lots excluded by the SQL filters (regimen APV-B, portfolio INA, annulled, concept RESFUS, zero quantity, other client, other year): no withdrawal',
     L(F, 'montoRetirada = montoRetirada.add'), ent(), seed(BASE_PB, cart(701, 5, 1000, op=11, portafolio=902), cart(701, 5, 1000, op=12, portafolio=903), cart(701, 5, 1000, op=13, estado='ANU'),
                                                              cart(701, 5, 1000, op=14, egr_concepto='RESFUS'), cart(701, 0, 1000, op=15), cart(702, 5, 1000, op=16),
                                                              cart(701, 5, 1000, op=17, fop='2023-06-01'), cart(701, 5, 1000, op=18, clase='RF')), tablas=[TBON])
caso(346, 'ProcesarBonificacion', 'filtros-de-bonificacion-estado-empresa-y-ahorro-cero', 'only ING bonificaciones of the company whose DJ has AhorrosDeposito != 0 are processed: PRO state, other company and zero-savings client are left untouched',
     L(F, 'SQL_SELECT_ConsultarBonificacionAProcesar'), ent(), seed(REGIMENES, dj(701, 300000, 100000), dj(702, 0, 0), dj(703, 500, 100), dj(704, 500, 100), bon(1, 701, 70000), bon(2, 702, 100),
                                                                     bon(3, 703, 100, estado='PRO'), bon(4, 704, 100, empresa=2)), tablas=[TBON])
caso(347, 'ProcesarBonificacion', 'ahorro-neto-cero-lanza-ArithmeticException', 'AhorrosDJ == RetirosDJ (net 0): BigDecimal division by zero throws ArithmeticException; nothing is updated',
     L(F, 'divide\\(montoAhorroNeto'), ent(), seed(REGIMENES, bon(1, 701, 70000), dj(701, 100000, 100000)), tablas=[TBON])
caso(348, 'ProcesarBonificacion', 'dos-clientes-cada-uno-con-sus-retiros', 'two clients, each only receives its own lots (IdCliente equality): rounding of a non terminating quotient to 8 decimals on storage',
     L(F, 'fila.IdCliente.equals\\(cartera.IdCliente\\)'), ent(), seed(REGIMENES, bon(1, 701, 70000), bon(2, 702, 33333), dj(701, 300000, 100000), dj(702, 1000, 333), cart(701, 3, 1111.1111, op=21),
                                                                          cart(702, 7, 99.99, op=22)), tablas=[TBON])
caso(349, 'ProcesarBonificacion', 'sin-anio-ahorro-lanza-NullPointerException', 'p_AnoAhorro missing: the first queries run with a NULL year and (in.p_AnoAhorro + 1) unboxes null: NullPointerException before any update',
     L(F, 'in.p_AnoAhorro \+ 1'), {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV'}, seed(BASE_PB), tablas=[TBON])

# ------------------------------------------------------------------------------------------------ CargaBonificacionTGR
F = 'CargaBonificacionTGR.java'
SEQ_TGR = {'BSCL.APV.BD.Secuencia::APV.IdCargaMasiva': 100, 'BSCL.APV.BD.Secuencias::INV.IdBonificacion': 500}
NOMBRE = 'BONIF_TGR_2025.txt'
TCARGA = tabla(T_CARGA, '"IdCargaMasiva"')
TBON2 = tabla(T_INVBON, '"IdBonificacion"', columnas=['IdEmpresa', 'IdBonificacion', 'IdEntidadRecaudadora', 'FechaGeneracion', 'TipoRegistro', 'AnoAhorro', 'AnoProceso', 'FolioEgreso', 'MontoPagadoNeto',
                                                       'MontoSaldoNeto', 'MontoPagadoReajustado', 'FechaPago', 'IdCliente', 'MontoBonificacionAfiliadoNeto', 'PA1', 'PA2', 'PA3', 'MontoAfiliadoNeto',
                                                       'MontoAfiliadoSaldoNeto', 'MontoReajustadoPagadoAfiliado', 'MontoCalculado', 'IdEstado', 'CodigoArchivo'], enmascarar=['FechaCalculado'])


def fila(rut='11111111', dv='1', tipo='3', ano='2024', proceso='2025', **kw):
    d = {'TipoDeRegistro': tipo, 'RutAfiliado': rut, 'DVAfiliado': dv, 'MontoBonificacionAfiliadoNeto': '15000', 'PA1': '10050', 'PA2': '05025', 'PA3': '00100', 'MontoAfiliadoNeto': '120000',
         'MontoAfiliadoSaldoNeto': '20000', 'MontoReajustadoPagadoAfiliado': '130000', 'AnoAhorro': ano, 'AnoProceso': proceso, 'FolioEgreso': '4455', 'MontoPagadoNeto': '110000', 'MontoSaldoNeto': '10000',
         'MontoPagadoReajustado': '115000', 'FechaDePago': '20250315', 'RutAFP': '76123456', 'DVAFP': '7', 'FechaDeGeneracion': '20250310'}
    d.update(kw)
    return d


def ent_tgr(filas, nombre=NOMBRE, archivos=None):
    arch = archivos if archivos is not None else [{'IdEmpresa': 1, 'IdCargaMasiva': 0, 'NombreCargaMasiva': nombre, 'CantidadRegistros': len(filas), 'IdUsuario': 17, 'IdTipoCarga': 4,
                                                  'Descripcion': 'Carga bonificacion TGR', 'FechaCarga': '2025-01-01', 'IdEstado': 'VIG'}]
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 17, 'p_IdModulo': 'APV', 'p_NombreCarga': nombre, 'p_Origen': 'BON', 'p_IdEmpleado': 7312, 'p_Archivo': arch, 'p_Parametros': filas}


def cli(idcli, doc):
    return ins(T_V_CLIENTE, IdCliente=idcli, IdPersona=500 + idcli, DocumentoIdentidad=doc, IdEstado='ACT')


def djt(idcli, ano=2024):
    return ins(T_DJ, ID=idcli, IdCliente=idcli, AnoAhorro=ano, AhorrosDeposito=100, RetirosDeposito=0, RutCliente='x')


C1 = [cli(701, '11111111-1'), djt(701)]
M = ['*IdTrabajo']

caso(270, 'CargaBonificacionTGR', 'archivo-ya-cargado', 'a bonificacion already carries the file name as CodigoArchivo: ERROR "El archivo ya fue cargado previamente." (id 1) and nothing is written',
     L(F, 'El archivo ya fue cargado previamente'), ent_tgr([fila()]), seed(C1, ins(T_INVBON, IdBonificacion=1, CodigoArchivo=NOMBRE, IdCliente=999, AnoAhorro=2020, AnoProceso=2021)), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(271, 'CargaBonificacionTGR', 'ingresa-bonificacion-con-dj', 'client exists, has a DJ and no bonificacion for the year pair: row inserted (state ING, PA1 "100.50", TipoRegistro ACTUAL even for record type 3), "Bonificacion Ingresada." and the totals message',
     L(F, 'Bonificacion Ingresada'), ent_tgr([fila()]), seed(C1), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(272, 'CargaBonificacionTGR', 'tipo-registro-5-tambien-es-actual', 'defect: TipoRegistro is decided with fila.TipoDeRegistro == "3" (reference comparison), so the value is ACTUAL for "3", "5" and anything else',
     L(F, 'fila.TipoDeRegistro == "3"'), ent_tgr([fila(tipo='5'), fila(rut='22222222', dv='2', tipo='3')]), seed(C1, cli(702, '22222222-2'), djt(702)), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(273, 'CargaBonificacionTGR', 'cliente-inexistente-se-rechaza', 'client not found in CLI.ConsultarCliente: two ERROR messages (id = running counter) and the rejected count in the totals message',
     L(F, 'No existe el Cliente con RUT'), ent_tgr([fila(rut='99999999', dv='K')]), seed(C1), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(274, 'CargaBonificacionTGR', 'cliente-ya-ingresado-en-el-anio', 'the client already has a bonificacion for the same AnoAhorro/AnoProceso: ERROR "ya fue ingresado en el ano ahorro" and rejected',
     L(F, 'ya fue ingresado en el ano ahorro'), ent_tgr([fila()]), seed(C1, ins(T_INVBON, IdBonificacion=1, IdCliente=701, AnoAhorro=2024, AnoProceso=2025, CodigoArchivo='OTRO')), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(275, 'CargaBonificacionTGR', 'sin-declaracion-jurada-informa-y-ingresa', 'no DJ row for the client: INFORMACION "No se encontro una Declaracion Jurada" but the bonificacion is still inserted',
     L(F, 'No se encontro una Declaracion Jurada'), ent_tgr([fila()]), seed(cli(701, '11111111-1')), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(276, 'CargaBonificacionTGR', 'mismo-afiliado-dos-veces-en-el-archivo', 'the same RUT twice in the file: the existing-bonificacion check runs before the insert, so both rows are inserted (two messages Ingresada)',
     L(F, 'for \\(ExisteCliente cliente : ExisteCliente\\)'), ent_tgr([fila(), fila(FolioEgreso='4456')]), seed(C1), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(277, 'CargaBonificacionTGR', 'mezcla-ok-rechazado-y-ya-ingresado', 'three rows: unknown client, client with a previous bonificacion and a valid client: counters and ids of messages follow the running counter',
     L(F, 'Count\\+\\+;'), ent_tgr([fila(rut='99999999', dv='K'), fila(rut='22222222', dv='2'), fila(rut='33333333', dv='3')]),
     seed(cli(702, '22222222-2'), cli(703, '33333333-3'), djt(703), ins(T_INVBON, IdBonificacion=1, IdCliente=702, AnoAhorro=2024, AnoProceso=2025, CodigoArchivo='OTRO')), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(278, 'CargaBonificacionTGR', 'dv-en-minuscula-no-encuentra-al-cliente', 'the SQL lookup is case sensitive: DV "k" in the file does not find the client stored with "K" (the lower-casing only affects the Java sets), so it is rejected',
     L(F, 'cli.toLowerCase\\(\\)'), ent_tgr([fila(rut='44444444', dv='k')]), seed(cli(704, '44444444-K'), djt(704)), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(279, 'CargaBonificacionTGR', 'lista-vacia-solo-registra-la-carga', 'no records: no bonificacion, the CargaMasiva row is written and the totals message says Ingresadas 0 Rechazadas 0',
     L(F, 'Total Bonificaciones'), ent_tgr([]), seed(C1), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(280, 'CargaBonificacionTGR', 'sin-archivo-lanza-IndexOutOfBounds', 'empty p_Archivo with records: the rows are batched but in.p_Archivo.get(0) throws IndexOutOfBoundsException and the pipeline rolls back (nothing persists)',
     L(F, 'in.p_Archivo.get\\(0\\).IdCargaMasiva'), ent_tgr([fila()], archivos=[]), seed(C1), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(281, 'CargaBonificacionTGR', 'fecha-invalida-lanza-ParseException', 'FechaDePago that SimpleDateFormat(yyyyMMdd) cannot parse: ParseException (checked) before any write',
     L(F, 'Date fechaPago = format.parse'), ent_tgr([fila(FechaDePago='ABCDEFGH')]), seed(C1), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(287, 'CargaBonificacionTGR', 'fecha-con-guiones-se-interpreta-con-parseo-laxo', 'defect: FechaDePago "15-03-2025" is parsed leniently by SimpleDateFormat(yyyyMMdd) into the year 0014 (no validation), and the row is inserted',
     L(F, 'Date fechaPago = format.parse'), ent_tgr([fila(FechaDePago='15-03-2025')]), seed(C1), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(282, 'CargaBonificacionTGR', 'monto-no-numerico-lanza-NumberFormatException', 'MontoPagadoNeto "12.5x": new BigDecimal throws NumberFormatException',
     L(F, 'MontoPagadoNeto = new BigDecimal'), ent_tgr([fila(MontoPagadoNeto='12.5x')]), seed(C1), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(283, 'CargaBonificacionTGR', 'porcentaje-corto-lanza-StringIndexOutOfBounds', 'PA1 shorter than 5 characters: substring(3, 5) throws StringIndexOutOfBoundsException',
     L(F, 'fila.PA1.substring'), ent_tgr([fila(PA1='10')]), seed(C1), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(284, 'CargaBonificacionTGR', 'ano-ahorro-no-numerico-lanza-NumberFormatException', 'AnoAhorro "20x4": Long.parseLong throws NumberFormatException',
     L(F, 'Long.parseLong\\(fila.AnoAhorro\\)'), ent_tgr([fila(ano='20x4')]), seed(C1), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
caso(285, 'CargaBonificacionTGR', 'anio-de-la-primera-fila-valida-todo-el-lote', 'the year pair used for the "already ingested" and DJ checks is the FIRST row of the batch: a second row of another year whose client has a bonificacion for its own year is still inserted',
     L(F, 'for \\(Bonificacion fila : ParamsInsertBonificacion\\) \\{\\s*$'), ent_tgr([fila(), fila(rut='22222222', dv='2', ano='2023', proceso='2024')]),
     seed(C1, cli(702, '22222222-2'), djt(702, 2023), ins(T_INVBON, IdBonificacion=1, IdCliente=702, AnoAhorro=2023, AnoProceso=2024, CodigoArchivo='OTRO')), secuencias=SEQ_TGR, tablas=[TBON2, TCARGA])
