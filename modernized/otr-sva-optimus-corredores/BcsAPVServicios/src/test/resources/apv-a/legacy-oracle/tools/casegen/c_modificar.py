"""Cases of ModificarContratoSAAPV (ids 500-549)."""
from lib import *  # noqa: F401,F403
from c_grabar import par, SEQ, MASCARA, sp_ok, SP_PORTAFOLIO, _NULO, err, PARTES as PARTES_G  # noqa: F401

M = 'ModificarContratoSAAPV'
MF = 'ModificarContratoSAAPV.java'

COLS = ['IdContratoSAAPV', 'FolioContratoSAAPV', 'IdPersona', 'IdCliente', 'IdEmpleador', 'IdEstado', 'IdEstadoSolicitud', 'IdEstadoEnvioNotificacion', 'NombrePlan',
        'FechaSuscripcion', 'FechaCreacion', 'FechaAprobacion', 'IdMedioRecaudacionFormaPago', 'Trabajador', 'Folio', 'TipoTraspaso']
TM = [tabla(T_CONTRATO, '"IdContratoSAAPV"', COLS), tabla(T_PORCON, '"IdPortafolioContratoSAAPV"'),
      tabla(T_DISTR, '"IdDistribucionInversion"', excluir=['IdDistribucionInversion'])]
TM_TRA = [tabla(T_CONTRATO, '"IdContratoSAAPV"', COLS), tabla(T_TRASPASO, '"IdTraspaso"'), tabla(T_DETORIGEN, '"IdDetalleOrigenTraspaso"'), tabla(T_PORCON, '"IdPortafolioContratoSAAPV"')]
SEQM = {'BSCL.APV.BD.Secuencias::TRA.IdTraspaso': 6000, 'BSCL.APV.BD.Secuencias::TRA.IdDetalleOrigenTraspaso': 6500}


def pmod(**kw):
    d = dict(IdContratoSAAPV=9001, FolioContratoSAAPV='F-M1', Folio='F-M1')
    d.update(kw)
    d.setdefault('IdEstado', 'PDI')
    return par(**d)


def pw(**kw):
    d = {'CodigoAgrupador': 'GRUPO 1', 'IdMatrizAgrupador': 1, 'OrigenAhorro': 'APV', 'IdPortafolio': 901, 'IdPropositoPortafolio': 'PROPOSITOAPV', 'MesInicio': '2025-03',
         'MesTermino': '9999-01', 'MontoPesos': 100000, 'MontoUF': 0, 'Pago': 'IND', 'PagoNombre': 'INDEFINIDO', 'Porcentaje': 100, 'Producto': 'APV FONDOS MUTUOS',
         'Regimen': 'APV-A', 'IdContratoSAAPVOrigen': 9001}
    d.update(kw)
    return d


def dm(**kw):
    d = {'IdContratoSAAPV': 9001, 'FolioContratoSAAPV': 'F-M1', 'IdInstrumento': 1021327, 'Nemotecnico': '00391CCLP', 'MontoPesos': 100000, 'Porcentaje': 100, 'IdEstado': 'VIG'}
    d.update(kw)
    return d


def do(**kw):
    d = {'IdDetalleOrigenTraspaso': 0, 'IdTraspaso': 0, 'CodigoTipoAhorro': 'APV', 'IdEntidadRecaudadora': 831, 'NombreEntidad': 'AFP UNO', 'DocumentoEntidad': '76240079-0',
         'Fondo': 'A', 'MontoTraspaso': 5000000, 'UFTraspaso': 0, 'PorcentajeTraspaso': 100, 'IdEstado': 'VIG', 'IdInstrumento': 0, 'IdPortafolio': 0,
         'CodigoRegimenTributario': '0', 'IdContratoSAAPVOrigen': 9001}
    d.update(kw)
    return d


def em(parametros, pws=None, dis=None, dos=None, tipo='M'):
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_IdModulo': 'APV', 'p_Tipo': tipo, 'p_Parametros': parametros,
            'p_DistribucionInversion': [dm()] if dis is None else dis, 'p_PortafoliosModificados': [pw()] if pws is None else pws, 'p_DetalleOrigenTraspaso': dos or []}


def pp(idpc=8801, idpo=901, idc=9001, **kw):
    return porcon(idpc, idpo, idc, **kw)


def vieja(idc=9001, estado='PDI', folio='F-M1', **kw):
    return contrato(idc, folio, IdEstado=estado, **kw)


DIS_OLD = ins(T_DISTR, IdDistribucionInversion=1, IdContratoSAAPV=9001, FolioContratoSAAPV='F-M1', IdInstrumento=1, Nemotecnico='VIEJA', MontoPesos=5, Porcentaje=100, IdEstado='VIG')
BASEM = [PARTES_G, vieja(), pp(), DIS_OLD]
COLM = {'SalidaProcedimiento': {SP_PORTAFOLIO: sp_ok(901)}}

# ================================================================================================ the common modification
LM = L(MF, r'sql.batch\(con, SQL_MODIFICAR_CONTRATOSAAPV', r'Valido datos minimos del contrato')
caso(500, M, 'modifica-contrato-pendiente-reemplaza-distribucion-y-portafolios', 'PDI contract: updated, old distribution deleted, state to VIG through CambiarEstadoContratosSAAPV, new distribution and the portfolio link re-created',
     LM, em([pmod()]), seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM, enmascarar_colaboradores=MASCARA)
caso(501, M, 'contrato-vigente-tambien-reemplaza-la-distribucion', 'a VIG contract (not PDI/PEN): the distribution is deleted by the else branch and re-created', L(MF, r'ELIMINAR_DISTRIBUCION_OK'),
     em([pmod()]), seed(PARTES_G, vieja(estado='VIG'), pp(), DIS_OLD), colaboradores=COLM, secuencias=SEQM, tablas=TM, enmascarar_colaboradores=MASCARA)
caso(502, M, 'contrato-pen-se-trata-como-pendiente', 'a PEN contract is handled like PDI', L(MF, r'linea.IdEstado.compareTo\("PEN"\) == 0'),
     em([pmod()]), seed(PARTES_G, vieja(estado='PEN'), pp(), DIS_OLD), colaboradores=COLM, secuencias=SEQM, tablas=TM, enmascarar_colaboradores=MASCARA)
caso(503, M, 'forma-de-pago-ausente-es-error-y-retorna', 'IdMedioRecaudacionFormaPago null and TipoMovimiento INGRESO: ERROR_FORMA_PAGO_NO_EXISTE (no id), returns before updating',
     L(MF, r'ERROR_FORMA_PAGO_NO_EXISTE'), em([pmod(IdMedioRecaudacionFormaPago=_NULO, TipoMovimiento='INGRESO')]), seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM,
     enmascarar_colaboradores=MASCARA)
caso(504, M, 'datos-minimos-incompletos-deja-advertencias', 'missing mandatory data (no address): "Contrato queda Pendiente de Ingreso" and one warning per missing item (id 4), no VIG change', L(MF, r'Contrato queda Pendiente de Ingreso'),
     em([pmod(DireccionCliente='', NumeroDireccionCliente='')]), seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM, enmascarar_colaboradores=MASCARA)
caso(505, M, 'fechas-vacias-se-guardan-como-1900', 'empty dates are written as 1900-01-01', L(MF, r'FechaSuscripcion.isEmpty\(\)\?"1900-01-01"'),
     em([pmod(FechaSuscripcion='', FechaCreacion='', FechaAprobacion='', FechaNotificacion='', FechaRecepcion='')]), seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM,
     enmascarar_colaboradores=MASCARA)
caso(506, M, 'fecha-nula-lanza-NPE', 'FechaSuscripcion null: fila.FechaSuscripcion.isEmpty() throws NullPointerException', L(MF, r'FechaSuscripcion.isEmpty\(\)\?"1900-01-01"'),
     em([pmod(FechaSuscripcion=_NULO)]), seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM, enmascarar_colaboradores=MASCARA)
caso(507, M, 'id-de-contrato-nulo-lanza-NPE', 'IdContratoSAAPV null: IdContratoSAAPVBorrar.longValue() throws NullPointerException', L(MF, r'IdContratoSAAPVBorrar.longValue\(\) != filaValidaPortafolio'),
     em([pmod(IdContratoSAAPV=_NULO)]), seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM, enmascarar_colaboradores=MASCARA)
caso(508, M, 'folio-vacio-toma-el-folio-anterior', 'FolioContratoSAAPV "" takes Folio (and the other way round)', L(MF, r'if\("".equalsIgnoreCase\(fila.FolioContratoSAAPV\)\)'),
     em([pmod(FolioContratoSAAPV='', Folio='F-M1')]), seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM, enmascarar_colaboradores=MASCARA)
caso(509, M, 'contrato-inexistente-no-actualiza-pero-avisa-ok', 'the contract id does not exist: the UPDATE touches nothing, the OK message is still produced and the rest continues', L(MF, r'MODIFICACION_CONTRATO_OK'),
     em([pmod(IdContratoSAAPV=9999)], pws=[], dis=[]), seed(PARTES_G, vieja()), colaboradores=COLM, secuencias=SEQM, tablas=TM, enmascarar_colaboradores=MASCARA)

# ================================================================================================ log (AgregarLogSAAPV)
caso(510, M, 'sin-detalle-de-origen-no-se-escribe-el-log', 'with no p_DetalleOrigenTraspaso rows the log builder throws IndexOutOfBounds inside its try (swallowed): AgregarLog is never called (legacy defect)',
     L(MF, r'in.p_DetalleOrigenTraspaso.get\(0\).IdTraspaso!=null'), em([pmod()], dos=[]), seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM[:1], enmascarar_colaboradores=MASCARA)
caso(511, M, 'con-detalle-de-origen-se-escribe-el-log', 'with one p_DetalleOrigenTraspaso row the modification log is written (distribution and portfolio details included)', L(MF, r'agregarLogService.logica\(con, entradaAgregarLog\)'),
     em([pmod()], dos=[do()]), seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM[:1], enmascarar_colaboradores=MASCARA)

# ================================================================================================ distribution
LDIS = L(MF, r'if\(in.p_DistribucionInversion.size\(\) > 0\)\{', r'Problema al crear Distribucion')
caso(512, M, 'distribucion-de-otro-contrato-se-descarta-y-nula-se-completa', 'distribution rows of another contract are ignored; a row without IdContratoSAAPV is completed with the contract id', LDIS,
     em([pmod()], dis=[dm(), dm(IdContratoSAAPV=7777, Nemotecnico='OTRO'), {'FolioContratoSAAPV': 'F-M1', 'IdInstrumento': 2, 'Nemotecnico': 'SIN-ID', 'MontoPesos': 1, 'Porcentaje': 1, 'IdEstado': 'VIG'}]),
     seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM, enmascarar_colaboradores=MASCARA)
caso(513, M, 'todas-las-distribuciones-ajenas-insertan-la-lista-original', 'when no row belongs to the contract the ORIGINAL list is inserted (distribucionInversionList.size() > 0 ? ... : in.p_DistribucionInversion)', LDIS,
     em([pmod()], dis=[dm(IdContratoSAAPV=7777, Nemotecnico='AJENA')]), seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM, enmascarar_colaboradores=MASCARA)
caso(514, M, 'sin-distribucion-no-inserta', 'no distribution rows: the old one is deleted and nothing is inserted', LDIS, em([pmod()], dis=[]), seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM,
     enmascarar_colaboradores=MASCARA)

# ================================================================================================ portfolios
LPOR = L(MF, r'if\(in.p_PortafoliosModificados.size\(\) > 0 && !fila.CodigoTipoSolicitud.equals\("TRA"\)\)\{', r'else if\(fila.TipoMovimiento.equals\("EGRESO"\)\)')
caso(515, M, 'portafolio-nuevo-se-crea-y-se-vincula', 'a portfolio row with IdPortafolio 0: the CrearPortafolio procedure is called, CREACION_PORTAFOLIO_OK with the reference as id, then the link is created', LPOR,
     em([pmod()], pws=[pw(IdPortafolio=0)]), seed(PARTES_G, vieja(), pp(), DIS_OLD), colaboradores={'SalidaProcedimiento': {SP_PORTAFOLIO: sp_ok(905)}}, secuencias=SEQM, tablas=TM,
     enmascarar_colaboradores=MASCARA)
caso(516, M, 'portafolio-nuevo-con-errores-no-se-vincula', 'the portfolio procedure answers an error: nothing is reported for it and no link is created',
     L(MF, r'SalidaCliente.addError\(errorDetalle.Codigo, errorDetalle.Descripcion\)'),
     em([pmod()], pws=[pw(IdPortafolio=0, Regimen='APV-B')]), seed(PARTES_G, vieja(), pp(), DIS_OLD),
     colaboradores={'SalidaProcedimiento': {SP_PORTAFOLIO: {'p_out_RefId': 0, 'p_out_AdvertenciasContador': 0, 'p_out_ErroresContador': 1,
                                                          'p_out_ErroresDetalle': [err('PORTAFOLIO_NO_CREADO')], 'p_out_AdvertenciasDetalle': []}}},
     secuencias=SEQM, tablas=TM, enmascarar_colaboradores=MASCARA)
caso(517, M, 'portafolio-de-otro-contrato-no-se-vincula', 'a portfolio row whose IdContratoSAAPVOrigen is another contract is not linked (and the validation skips it)', LPOR,
     em([pmod()], pws=[pw(), pw(IdPortafolio=902, IdContratoSAAPVOrigen=7777)]), seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM, enmascarar_colaboradores=MASCARA)

# ================================================================================================ traspaso
LTRA = L(MF, r'if\(fila.CodigoTipoSolicitud.equals\("TRA"\)\)\{', r'FIN TRASPASO')
TRA = dict(CodigoTipoSolicitud='TRA', TipoMovimiento='INGRESO', TipoTraspaso='TOT', NombreEntidadOrigen='AFP UNO', RutEntidadOrigen='76240079-0', TipoDeAhorro='APV', IdPersonaOrigen=61704)
TRA_SEED = [PARTES_G, vieja(CodigoTipoSolicitud='TRA'), ins(T_TRASPASO, IdTraspaso=5001, IdContratoSAAPV=9001, IdEmpresa=1, TipoMovimiento='INGRESO', IdEstado='PDI',
                                                          FechaIngreso='2025-03-01', FechaEgreso='0001-01-01', FechaModificacion='0001-01-01', FechaSuscripcion='2025-03-01', CierreSolicitud='0001-01-01'),
            ins(T_DETORIGEN, IdDetalleOrigenTraspaso=5101, IdTraspaso=5001, CodigoTipoAhorro='APV', NombreEntidad='VIEJA', DocumentoEntidad='1', Fondo='A', MontoTraspaso=1,
                UFTraspaso=0, PorcentajeTraspaso=100, IdEstado='VIG', IdInstrumento=0, IdPortafolio=0, CodigoRegimenTributario='0'),
            ins(T_DETTRASPASO, IdTraspaso=5001), pp()]
caso(518, M, 'traspaso-pendiente-se-reemplaza-con-aprobacion-automatica', 'TRA PDI contract: old traspaso and origin detail deleted, a new traspaso (sequence 6000) with its origin rows is created and goes ING (ApruebaTraspasoAutomatico S)',
     LTRA, em([pmod(**TRA)], dos=[do()], dis=[], pws=[]), seed(TRA_SEED), colaboradores=COLM, secuencias=SEQM, tablas=TM_TRA, enmascarar_colaboradores=MASCARA)
caso(519, M, 'traspaso-pendiente-sin-aprobacion-automatica-pide-autorizacion', 'the same with APV.ApruebaTraspasoAutomatico N: traspaso PDA and an INGTRA authorization event', L(MF, r'ApruebaTraspasoAutomatico.equals\("N"\)'),
     em([pmod(**TRA)], dos=[do()], dis=[], pws=[]), seed(TRA_SEED), config={'APV/APV.ApruebaTraspasoAutomatico': 'N'}, colaboradores=COLM, secuencias=SEQM, tablas=TM_TRA,
     enmascarar_colaboradores=MASCARA)
caso(520, M, 'traspaso-detalle-de-otro-contrato-no-se-crea', 'origin rows of another contract are skipped: the new traspaso has no origin rows', L(MF, r'IdContratoSAAPVBorrar.longValue\(\) != row.IdContratoSAAPVOrigen.longValue\(\)\) continue;'),
     em([pmod(**TRA)], dos=[do(IdContratoSAAPVOrigen=7777)], dis=[], pws=[]), seed(TRA_SEED), colaboradores=COLM, secuencias=SEQM, tablas=TM_TRA, enmascarar_colaboradores=MASCARA)
caso(521, M, 'traspaso-de-contrato-vigente-no-se-toca', 'a TRA contract that is already VIG: the traspaso branch (PDI/PEN only) does not run', L(MF, r'linea.IdEstado.compareTo\("PDI"\) == 0'),
     em([pmod(**TRA)], dos=[do()], dis=[], pws=[]), seed(PARTES_G, vieja(estado='VIG', CodigoTipoSolicitud='TRA'), TRA_SEED[2:], ), colaboradores=COLM, secuencias=SEQM, tablas=TM_TRA,
     enmascarar_colaboradores=MASCARA)

# ================================================================================================ egreso
LEGR = L(MF, r'else if\(fila.TipoMovimiento.equals\("EGRESO"\)\)\{', r'in.p_DetalleOrigenTraspaso tiene los nuevos')
EGR = dict(CodigoTipoSolicitud='TRA', TipoMovimiento='EGRESO', TipoTraspaso='TOT', NombreEntidadOrigen='AFP DOS', RutEntidadOrigen='76240079-0', TipoDeAhorro='APV',
           IdMedioRecaudacionFormaPago=_NULO)
EGR_SEED = [PARTES_G, vieja(CodigoTipoSolicitud='TRA'),
            ins(T_TRASPASO, IdTraspaso=5001, IdContratoSAAPV=0, IdEmpresa=1, TipoMovimiento='EGRESO', IdEstado='PDI', FechaIngreso='2025-03-01', FechaEgreso='2025-03-01',
                FechaModificacion='0001-01-01', FechaSuscripcion='2025-03-01', CierreSolicitud='0001-01-01', NombreEntidad='AFP UNO', DocumentoEntidad='1', CodigoTipoTraspaso='TOT'),
            ins(T_DETORIGEN, IdDetalleOrigenTraspaso=5101, IdTraspaso=5001, CodigoTipoAhorro='APV', NombreEntidad='VIEJA', DocumentoEntidad='1', Fondo='A', MontoTraspaso=1, UFTraspaso=0,
                PorcentajeTraspaso=100, IdEstado='VIG', IdInstrumento=0, IdPortafolio=0, CodigoRegimenTributario='0')]
caso(522, M, 'egreso-reemplaza-el-detalle-y-actualiza-el-traspaso', 'TipoMovimiento EGRESO (no forma de pago required): the origin details of the traspaso are replaced and the traspaso entity data is updated (FechaModificacion = system date)',
     LEGR, em([pmod(**EGR)], dos=[do(IdTraspaso=5001)], dis=[], pws=[]), seed(EGR_SEED), colaboradores=COLM, secuencias=SEQM, tablas=TM_TRA, enmascarar_colaboradores=MASCARA)
caso(523, M, 'egreso-sin-detalle-lanza-IndexOutOfBounds', 'EGRESO without p_DetalleOrigenTraspaso: in.p_DetalleOrigenTraspaso.get(0) throws IndexOutOfBoundsException', L(MF, r'paramConsultaTraspasoEgresos.put\("IdTraspaso"'),
     em([pmod(**EGR)], dos=[], dis=[], pws=[]), seed(EGR_SEED), colaboradores=COLM, secuencias=SEQM, tablas=TM_TRA, enmascarar_colaboradores=MASCARA)

# ================================================================================================ several contracts / empty
caso(524, M, 'dos-contratos-se-procesan-en-orden', 'two contracts in one call: both updated, each with its own messages', LM,
     em([pmod(), pmod(IdContratoSAAPV=9002, FolioContratoSAAPV='F-M2', Folio='F-M2')], pws=[pw(), pw(IdContratoSAAPVOrigen=9002, IdPortafolio=902)], dis=[dm(), dm(IdContratoSAAPV=9002, FolioContratoSAAPV='F-M2')]),
     seed(PARTES_G, portafolio(902, 701), vieja(), vieja(9002, folio='F-M2'), pp(), pp(8802, 902, 9002), DIS_OLD), colaboradores=COLM, secuencias=SEQM, tablas=TM, enmascarar_colaboradores=MASCARA)
caso(525, M, 'lista-vacia-no-hace-nada', 'no contracts: empty output', L(MF, r'for\(ParametrosContrato fila : in.p_Parametros\)'), em([], dis=[], pws=[]), seed(BASEM), colaboradores=COLM, secuencias=SEQM, tablas=TM)
