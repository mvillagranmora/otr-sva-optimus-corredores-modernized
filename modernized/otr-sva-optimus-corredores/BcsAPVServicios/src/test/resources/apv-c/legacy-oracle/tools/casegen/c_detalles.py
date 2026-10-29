"""Family C cases: CrearRecaudacionCliente (220-229), CrearRecaudacionEmpleador (230-239), CargaRecaudacion (210-219), CargaMasivaRecaudacion (140-159)."""
from fix import (T_CARGA, T_CARGADET, T_RCLI, T_REC, T_REMP, banco, caja, empresa, msg, rcli, rec, remp, res_proceso, salida_ing_egreso, T_V_EMPRESA)
from lib import L, caso, ins, seed, tabla

TREC = tabla(T_REC, '"IdRecaudacion"', columnas=['IdRecaudacion', 'IdEmpresa', 'IdCarga', 'Origen', 'IdEstado', 'MontoDeposito'])
TEMP = tabla(T_REMP, '"IdRecaudacionEmpleador"', columnas=['IdRecaudacionEmpleador', 'IdRecaudacion', 'IdPersona', 'PeriodoPago', 'MontoDepositoConvenido', 'MontoDepositoAPV', 'IdEstado'])
TCLI = tabla(T_RCLI, '"IdRecaudacionCliente"', columnas=['IdRecaudacionCliente', 'IdRecaudacion', 'IdRecaudacionEmpleador', 'IdCliente', 'TipoAfiliado', 'IdRegimenTriburario',
                                                         'MontoDepositoConvenido', 'MontoDepositoAPV', 'Origen', 'IdEstado'])

# ------------------------------------------------------------------------------------------------ CrearRecaudacionCliente
F = 'CrearRecaudacionCliente.java'
SEQC = 'BSCL.APV.BD.Secuencia::REC.IdRecaudacionCliente'


def cli(**kw):
    c = {'IdInterno': 1, 'IdRecaudacionCliente': 0, 'IdRecaudacion': 10, 'IdRecaudacionEmpleador': 0, 'IdCliente': 7001, 'FechaIngreso': '2025-03-31', 'TipoAfiliado': 'DEP',
         'IdRegimenTriburario': 'APV', 'MontoDepositoConvenido': 0, 'MontoDepositoAPV': 1900, 'Origen': 'CARGA', 'IdEstado': 'VIG'}
    c.update(kw)
    return c


def ecli(tipo='I', params=None, **kw):
    e = {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_Tipo': tipo, 'p_Origen': 'CARGA', 'p_Parametros': params if params is not None else [cli()]}
    e.update(kw)
    return e


BASE_C = seed(rec(10, 'CON'), remp(20, 10, 'CON'))

caso(220, 'CrearRecaudacionCliente', 'ingreso-un-cliente-y-estado-ing', 'type I: the client detail is inserted, OK message, the deposit goes to ING',
     L(F, 'sql.batch\\(con, in.p_Tipo.compareTo'), ecli(), BASE_C, secuencias={SEQC: 500}, tablas=[TREC, TEMP, TCLI])
caso(221, 'CrearRecaudacionCliente', 'con-empleador-lo-pasa-a-ing', 'client detail of an employer: CambiarEstadoRecaudacion also sets the employer detail to ING',
     L(F, 'RecaudacionEstado.IdRecaudacionEmpleador = fila.IdRecaudacionEmpleador'), ecli(params=[cli(IdRecaudacionEmpleador=20)]), BASE_C, secuencias={SEQC: 510},
     tablas=[TREC, TEMP, TCLI])
caso(222, 'CrearRecaudacionCliente', 'modificacion-actualiza', 'type M: the client detail is updated, MODIFICACION_CLIENTE_OK, the sequence is still requested',
     L(F, 'MODIFICACION_CLIENTE_OK'),
     ecli(tipo='M', params=[cli(IdRecaudacionCliente=40, IdRecaudacion=10, MontoDepositoAPV=2500, IdEstado='REZ')]),
     seed(BASE_C, rcli(40, 10, None, 'ING')), secuencias={SEQC: 520}, tablas=[TREC, TCLI])
caso(223, 'CrearRecaudacionCliente', 'modificacion-inexistente', 'type M of an unknown detail: ERROR message but tipo APV, so the counter stays 0',
     L(F, 'Problemas al Modificar Detalle Cliente'), ecli(tipo='M', params=[cli(IdRecaudacionCliente=999)]), BASE_C, secuencias={SEQC: 530}, tablas=[TREC, TCLI])
caso(224, 'CrearRecaudacionCliente', 'dos-clientes-mensaje-compartido', 'two clients: consecutive ids, results in order; the single MensajeSalida instance appears twice',
     L(F, 'MensajeSalida               msg             = new MensajeSalida'),
     ecli(params=[cli(IdInterno=1, IdCliente=7001), cli(IdInterno=2, IdCliente=7002, MontoDepositoAPV=100)]), BASE_C, secuencias={SEQC: 540}, tablas=[TREC, TCLI])
caso(225, 'CrearRecaudacionCliente', 'recaudacion-nula-lanza-NullPointerException', 'IdRecaudacion missing: the insert works but CambiarEstadoRecaudacion unboxes null (everything rolled back)',
     L(F, 'RecaudacionEstado.FolioRecaudacion = fila.IdRecaudacion'), ecli(params=[cli(IdRecaudacion=None)]), BASE_C, secuencias={SEQC: 550}, tablas=[TREC, TCLI])
caso(226, 'CrearRecaudacionCliente', 'lista-vacia', 'empty list: no insert, empty answer, counter 0', L(F, 'List<Long> idRecaudacionCliente = Utils.getSecuencia'),
     ecli(params=[]), BASE_C, secuencias={SEQC: 560}, tablas=[TCLI])
caso(227, 'CrearRecaudacionCliente', 'tipo-nulo-lanza-NullPointerException', 'p_Tipo missing: NullPointerException', L(F, 'if\\(in.p_Tipo.compareTo\\("M"\\)==0\\)\\{'),
     {'p_IdEmpresa': 1, 'p_Parametros': [cli()]}, BASE_C, secuencias={SEQC: 570}, tablas=[TCLI])
caso(228, 'CrearRecaudacionCliente', 'montos-con-decimales', 'decimal amounts are stored with the batch scale (16, HALF_DOWN) into the column',
     L(F, 'Cli.MontoDepositoConvenido'), ecli(params=[cli(MontoDepositoConvenido=33.5, MontoDepositoAPV=1866.25, IdRegimenTriburario='APV-A')]), BASE_C, secuencias={SEQC: 580},
     tablas=[TCLI])

# ------------------------------------------------------------------------------------------------ CrearRecaudacionEmpleador
F = 'CrearRecaudacionEmpleador.java'
SEQE = 'BSCL.APV.BD.Secuencia::REC.IdRecaudacionEmpleador'


def emp(**kw):
    e = {'IdInterno': 1, 'IdRecaudacionEmpleador': 0, 'IdRecaudacion': 10, 'CodigoArchivo': 'A1', 'IdPersona': 4400, 'FechaIngreso': '2025-03-31', 'PeriodoPago': '2025-03',
         'NumeroHojas': 1, 'TipoAfiliado': 'DEP', 'TipoPago': 'Normal', 'Insistencias': '', 'MontoDepositoConvenido': 0, 'MontoDepositoAPV': 1900, 'Reajustes': 0, 'Intereses': 0,
         'Origen': 'CARGA', 'Observaciones': '', 'IdEstado': 'VIG'}
    e.update(kw)
    return e


def eemp(tipo='I', params=None, **kw):
    e = {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_Tipo': tipo, 'p_Origen': 'CARGA', 'p_Parametros': params if params is not None else [emp()]}
    e.update(kw)
    return e


BASE_E = seed(rec(10, 'CON'), rcli(30, 10, 600, 'CON'), rcli(31, 10, 600, 'DIS'), rcli(32, 10, 600, 'DEV'), rcli(33, 10, 601, 'CON'))

caso(230, 'CrearRecaudacionEmpleador', 'ingreso-pasa-a-ing-deposito-y-clientes-vigentes', 'type I: the employer detail is inserted; deposit and its client details (not DIS/DEV) go to ING',
     L(F, 'for\\(DetalleCliente detCli : detalleClientes\\)'), eemp(), BASE_E, secuencias={SEQE: 600}, tablas=[TREC, TEMP, TCLI])
caso(231, 'CrearRecaudacionEmpleador', 'modificacion-actualiza', 'type M: the employer detail is updated, MODIFICACION_EMPLEADOR_OK, P_OUT_IDRECEMP is the given id',
     L(F, 'MODIFICACION_EMPLEADOR_OK'), eemp(tipo='M', params=[emp(IdRecaudacionEmpleador=700, MontoDepositoAPV=4000, PeriodoPago='2025-02')]),
     seed(BASE_E, remp(700, 10, 'CON')), secuencias={SEQE: 610}, tablas=[TREC, TEMP])
caso(232, 'CrearRecaudacionEmpleador', 'modificacion-inexistente', 'type M of an unknown detail: ERROR message (tipo APV: counter 0), no P_OUT_IDRECEMP', L(F, 'Problemas al modificar Detalle Empleador'),
     eemp(tipo='M', params=[emp(IdRecaudacionEmpleador=999)]), BASE_E, secuencias={SEQE: 620}, tablas=[TREC, TEMP])
caso(233, 'CrearRecaudacionEmpleador', 'dos-empleadores', 'two employers: consecutive ids, two results, P_OUT_IDRECEMP is the last',
     L(F, 'salida.P_OUT_IDRECEMP = fila.IdRecaudacionEmpleador'), eemp(params=[emp(IdInterno=1, PeriodoPago='2025-02'), emp(IdInterno=2, PeriodoPago='2025-03', IdPersona=4401)]),
     BASE_E, secuencias={SEQE: 630}, tablas=[TREC, TEMP, TCLI])
caso(234, 'CrearRecaudacionEmpleador', 'recaudacion-nula-lanza-NullPointerException', 'IdRecaudacion missing: the insert passes but CambiarEstadoRecaudacion unboxes null (rolled back)',
     L(F, 'RecaudacionEstado.FolioRecaudacion = fila.IdRecaudacion'), eemp(params=[emp(IdRecaudacion=None)]), BASE_E, secuencias={SEQE: 640}, tablas=[TEMP])
caso(235, 'CrearRecaudacionEmpleador', 'lista-vacia', 'empty list: empty answer, no P_OUT_IDRECEMP', L(F, 'List<Long> idRecaudacionEmpleador = Utils.getSecuencia'),
     eemp(params=[]), BASE_E, secuencias={SEQE: 650}, tablas=[TEMP])
caso(236, 'CrearRecaudacionEmpleador', 'tipo-nulo-lanza-NullPointerException', 'p_Tipo missing: NullPointerException', L(F, 'if\\(in.p_Tipo.compareTo\\("M"\\)==0\\)\\{'),
     {'p_IdEmpresa': 1, 'p_Parametros': [emp()]}, BASE_E, secuencias={SEQE: 660}, tablas=[TEMP])

# ------------------------------------------------------------------------------------------------ CargaRecaudacion
F = 'CargaRecaudacion.java'
SEQM = 'BSCL.APV.BD.Secuencia::APV.IdCargaMasiva'
SEQD = 'BSCL.APV.BD.Secuencia::REC.IdRecaudacion'
TCARGA = tabla(T_CARGA, '"IdCargaMasiva"')
ING = 'BSCL.Tesoreria.BD.Servicios::CrearComprobante'
TESO = {'CrearIngresoEgreso': {'salida': salida_ing_egreso(resultados=[res_proceso('IdComprobante', 8001, ING, 'ING', 1)])}}


def archivo(**kw):
    a = {'IdEmpresa': 1, 'NombreCargaMasiva': 'Depositos.txt', 'CantidadRegistros': 1, 'IdUsuario': 39, 'IdTipoCarga': 4, 'Descripcion': 'carga', 'FechaCarga': '2018-03-13',
         'IdEstado': 'VIG'}
    a.update(kw)
    return a


def depo(**kw):
    d = {'IdEmpresa': 1, 'IdRecaudacion': 0, 'FolioCartola': 0, 'LineaCartola': 0, 'Sucursal': 'SUCUR', 'FechaDeposito': '2025-03-31', 'CuentaCorriente': '13602227',
         'MontoDeposito': 1000, 'Observacion': 'CARGA MASIVA', 'NumeroDocumento': 17580478, 'ConceptoRecaudacion': '00', 'IdBanco': 12, 'IdTipoDeposito': 'DEP', 'IdEstado': 'ING',
         'IdCuentaBanco': 122, 'IdCarga': 0}
    d.update(kw)
    return d


def ecar(archivos=None, deps=None):
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 39, 'p_IdModulo': 'APV', 'p_Archivo': [archivo()] if archivos is None else archivos,
            'p_ParametrosRecaudacion': [depo()] if deps is None else deps}


BASE_CR = seed(empresa(), caja(), banco())

caso(210, 'CargaRecaudacion', 'carga-y-un-deposito', 'one file and one deposit: CargaMasiva row, CARGA_MASIVA_OK, the deposit is created (IdCarga = the load id) and Tesoreria called',
     L(F, 'CrearRecaudacionDeposito grabaRecaudacion'), ecar(), BASE_CR, secuencias={SEQM: 40, SEQD: 900}, colaboradores=TESO, tablas=[TCARGA, TREC])
caso(211, 'CargaRecaudacion', 'deposito-con-error-no-se-informa', 'defect: CrearRecaudacionDeposito sets its counter to 0 (never null), so its messages (an ERROR here) are NOT copied',
     L(F, 'if\\(paramsalida.P_OUT_ERRORESCONTADOR == null\\)'), ecar(), seed(empresa(), banco()), secuencias={SEQM: 41, SEQD: 901}, tablas=[TCARGA, TREC])
caso(212, 'CargaRecaudacion', 'sin-depositos-mensaje-3', 'no deposits: the shared message instance becomes id 3 "La tabla de deposito viene vacia" and appears twice',
     L(F, 'La tabla de deposito viene vacia'), ecar(deps=[]), BASE_CR, secuencias={SEQM: 42}, tablas=[TCARGA, TREC])
caso(213, 'CargaRecaudacion', 'sin-archivo-no-hace-nada', 'empty p_Archivo: nothing inserted, empty answer', L(F, 'if\\(in.p_Archivo.size\\(\\) > 0\\)'),
     ecar(archivos=[]), BASE_CR, secuencias={SEQM: 43}, tablas=[TCARGA, TREC])
caso(214, 'CargaRecaudacion', 'excepcion-interna-se-informa-como-mensaje-5', 'an exception inside (settlement parameter missing) is swallowed: message id 5 CARGA_ERROR_JSON with the exception text; the CargaMasiva row stays',
     L(F, 'CARGA_ERROR_JSON'), ecar(), BASE_CR, secuencias={SEQM: 44, SEQD: 902}, config={'APV/REC.LiquidacionAutomatica': None}, colaboradores=TESO, tablas=[TCARGA, TREC])
caso(215, 'CargaRecaudacion', 'sin-fecha-sistema-lanza', 'PLA.FechaSistema missing: the first statement throws (outside the try)', L(F, 'String fechaSistema = ConfiguracionSistema'),
     ecar(), BASE_CR, secuencias={SEQM: 45}, config={'PLATAFORMA/PLA.FechaSistema': None}, tablas=[TCARGA])
caso(216, 'CargaRecaudacion', 'archivo-con-fecha-propia-se-pisa-con-fecha-sistema', 'FechaCarga is overwritten with PLA.FechaSistema', L(F, 'in.p_Archivo.get\\(0\\).FechaCarga = fechaSistema'),
     ecar(archivos=[archivo(FechaCarga='1999-01-01', NombreCargaMasiva='X.txt')], deps=[]), BASE_CR, secuencias={SEQM: 46}, tablas=[TCARGA])

# ------------------------------------------------------------------------------------------------ CargaMasivaRecaudacion
F = 'CargaMasivaRecaudacion.java'


def fila(folio, linea, monto=1000, cli_='9499210-9', emp_='76129263-3', regimen='APV-B', afiliado='DEP', periodo='2021-07', tipo_pago='NORMAL', banco_='CHILE', cuenta='111111'):
    return {'Fecha': '2021-12-07', 'TipoDeposito': 'TRA', 'FolioCartola': folio, 'FolioLineaCartola': linea, 'Banco': banco_, 'Cuenta': cuenta, 'Monto': monto, 'PeriodoPago': periodo,
            'TipoPago': tipo_pago, 'RUTEmpleador': emp_, 'RUTCliente': cli_, 'TipodeAfiliado': afiliado, 'Regimen': regimen}


def cmr(filas, archivos=None):
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_IdModulo': 'APV', 'p_NombreCarga': 'Carga_4.txt', 'p_Origen': 'RECAUDA',
            'p_Archivo': archivos if archivos is not None else [{'IdEmpresa': 1, 'IdCargaMasiva': 0, 'NombreCargaMasiva': 'Carga_4.txt', 'CantidadRegistros': len(filas), 'IdUsuario': 21,
                                                                 'IdTipoCarga': 2, 'Descripcion': 'Carga Recaudacion', 'FechaCarga': '2021-12-07T03:00:00.000Z', 'IdEstado': 'VIG'}],
            'p_Parametros': filas}


TCD = tabla(T_CARGADET, '"FolioCartola", "FolioLineaCartola", "RUTCliente"')

caso(140, 'CargaMasivaRecaudacion', 'carga-valida-guarda-archivo-y-detalle', 'new folios: one CargaMasiva row (date cut to 10 chars) and every detail row, success message',
     L(F, 'guardarCargaMasiva\\(in, salida, con\\)'), cmr([fila(1011, 115), fila(1011, 116, cli_='12969673-7'), fila(118, 119, regimen='APV-A', cli_='20231097-4')]), seed(),
     secuencias={SEQM: 1528}, tablas=[TCARGA, TCD])
caso(141, 'CargaMasivaRecaudacion', 'folio-existente-rechaza-y-no-inserta', 'a Folio/Linea already loaded: one ERROR per matching row, counter 1, nothing inserted',
     L(F, 'CARGA_RECAUDACION_EXITE_CARGA'), cmr([fila(1011, 115), fila(1011, 116)]),
     seed(ins(T_CARGADET, IdCargaMasiva=9, Fecha='2021-01-01', TipoDeposito='TRA', FolioCartola=1011, FolioLineaCartola=115, Banco='CHILE', Cuenta='1', Monto=1, PeriodoPago='2021-01',
              TipoPago='NORMAL', RUTEmpleador='1-9', RUTCliente='2-7', TipoDeAfiliado='DEP', Regimen='APV-B', MensajeValidacion='', EstadoRendicion='', EstadoProceso='')),
     secuencias={SEQM: 1529}, tablas=[TCARGA, TCD])
caso(142, 'CargaMasivaRecaudacion', 'dos-folios-existentes-dos-errores', 'two rows of the file already loaded: two ERROR messages (one per match)', L(F, 'listaDetalles.forEach'),
     cmr([fila(1011, 115), fila(1011, 116), fila(5, 5)]),
     seed(ins(T_CARGADET, IdCargaMasiva=9, FolioCartola=1011, FolioLineaCartola=115, RUTCliente='2-7'), ins(T_CARGADET, IdCargaMasiva=9, FolioCartola=1011, FolioLineaCartola=116, RUTCliente='3-5')),
     secuencias={SEQM: 1530}, tablas=[TCARGA, TCD])
caso(143, 'CargaMasivaRecaudacion', 'fecha-corta-lanza-StringIndexOutOfBounds', 'FechaCarga shorter than 10 characters: StringIndexOutOfBoundsException (substring), rolled back',
     L(F, 'archivo.FechaCarga= archivo.FechaCarga.substring\\(0,10\\)'),
     cmr([fila(1, 1)], archivos=[{'IdEmpresa': 1, 'NombreCargaMasiva': 'x.txt', 'CantidadRegistros': 1, 'IdUsuario': 21, 'IdTipoCarga': 2, 'Descripcion': '', 'FechaCarga': '2021-12', 'IdEstado': 'VIG'}]),
     seed(), secuencias={SEQM: 1531}, tablas=[TCARGA, TCD])
caso(144, 'CargaMasivaRecaudacion', 'fecha-nula-lanza-NullPointerException', 'FechaCarga missing: NullPointerException', L(F, 'archivo.FechaCarga= archivo.FechaCarga.substring\\(0,10\\)'),
     cmr([fila(1, 1)], archivos=[{'IdEmpresa': 1, 'NombreCargaMasiva': 'x.txt', 'CantidadRegistros': 1, 'IdUsuario': 21, 'IdTipoCarga': 2, 'IdEstado': 'VIG'}]),
     seed(), secuencias={SEQM: 1532}, tablas=[TCARGA, TCD])
caso(145, 'CargaMasivaRecaudacion', 'sin-archivo-lanza-IndexOutOfBounds', 'empty p_Archivo: the sequence block is empty and get(0) fails: IndexOutOfBoundsException', L(F, 'Long idCargaMasiva = idsCargaAchivo.get\\(0\\)'),
     cmr([fila(1, 1)], archivos=[]), seed(), secuencias={SEQM: 1533}, tablas=[TCARGA, TCD])
caso(146, 'CargaMasivaRecaudacion', 'sin-filas-solo-archivo', 'empty p_Parametros: only the file row is written, success message', L(F, 'in.p_Parametros.stream\\(\\)'),
     cmr([]), seed(), secuencias={SEQM: 1534}, tablas=[TCARGA, TCD])
caso(147, 'CargaMasivaRecaudacion', 'parametros-nulos-lanza-NullPointerException', 'p_Parametros null: NullPointerException', L(F, 'in.p_Parametros.stream\\(\\)'),
     {'p_IdEmpresa': 1, 'p_Archivo': [], 'p_Parametros': None}, seed(), secuencias={SEQM: 1535}, tablas=[TCARGA])
caso(148, 'CargaMasivaRecaudacion', 'dos-archivos-mismo-id', 'two file entries: both get the same IdCargaMasiva (first value of the block) and two rows are inserted',
     L(F, 'archivo.IdCargaMasiva =idCargaMasiva'),
     cmr([fila(1, 1)], archivos=[{'IdEmpresa': 1, 'NombreCargaMasiva': 'a.txt', 'CantidadRegistros': 1, 'IdUsuario': 21, 'IdTipoCarga': 2, 'Descripcion': 'A', 'FechaCarga': '2021-12-07', 'IdEstado': 'VIG'},
                                 {'IdEmpresa': 1, 'NombreCargaMasiva': 'b.txt', 'CantidadRegistros': 1, 'IdUsuario': 21, 'IdTipoCarga': 2, 'Descripcion': 'B', 'FechaCarga': '2021-12-08', 'IdEstado': 'VIG'}]),
     seed(), secuencias={SEQM: 1540}, tablas=[TCARGA, TCD])
