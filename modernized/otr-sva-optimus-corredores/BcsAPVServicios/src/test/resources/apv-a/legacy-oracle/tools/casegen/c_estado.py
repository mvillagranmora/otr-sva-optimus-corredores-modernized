"""Cases of CambiarEstadoContratosSAAPV (ids 250-309)."""
from lib import *  # noqa: F401,F403

K = 'CambiarEstadoContratosSAAPV'
KF = 'CambiarEstadoContratosSAAPV.java'
BASE = {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_IdModulo': 'APV'}


def e(opcion, filas, **kw):
    d = dict(BASE)
    d['p_Opcion'] = opcion
    d['p_ContratosSAAPV'] = filas
    d.update(kw)
    return d


def row(folio, idp=501, idc=701, idemp=0, fecha='2025-04-01', envio='', sol='PDS', est='REV', glosa=3, desc='MOTIVO'):
    return {'IdPersona': idp, 'IdCliente': idc, 'IdEmpleador': idemp, 'IdFolioContratoSAAPV': folio, 'FechaRevocacion': fecha,
            'IdEstadoEnvioNotificacion': envio, 'IdEstadoSolicitud': sol, 'IdEstado': est, 'IdGlosa': glosa, 'DescripcionGlosa': desc}


def mandato(idm, folio, estado='ACT'):
    return ins(T_MANDATO, IdMandatoPAC=idm, Estado=estado, FolioSAAPV=folio)


def traspaso(idt, idcon, est='PDI', mov='INGRESO', cierre='0001-01-01'):
    return ins(T_TRASPASO, IdTraspaso=idt, IdContratoSAAPV=idcon, IdEmpresa=1, TipoMovimiento=mov, IdEstado=est, CierreSolicitud=cierre,
               FechaIngreso='2025-03-01', FechaEgreso='0001-01-01', FechaModificacion='0001-01-01', FechaSuscripcion='2025-03-01')


PER = [persona(501, '11111111-1', idc=701)]
TK = [tabla(T_CONTRATO, '"IdContratoSAAPV"', COLS_CONTRATO_ESTADO),
      tabla(T_TRASPASO, '"IdTraspaso"', ['IdTraspaso', 'IdContratoSAAPV', 'IdEstado', 'CierreSolicitud'])]
AUT = {'PLATAFORMA/APV.AutorizaGestionContrato': 'S'}
ANU_OK = {'AnulacionMandatoPAC': {'P_OUT_ERRORESCONTADOR': 0, 'P_OUT_MENSAJES': [{'id': 0, 'tipo': 'OK', 'codigo': 'ANULACION_MANDATO_OK', 'descripcion': 'mandato anulado'}]}}
ANU_ERR = {'AnulacionMandatoPAC': {'P_OUT_ERRORESCONTADOR': 1, 'P_OUT_MENSAJES': [{'id': 0, 'tipo': 'ERROR', 'codigo': 'MANDATO_NO_ANULABLE', 'descripcion': 'no se puede anular'}]}}
REV_OK = {'RevocacionMandatoPAC': {'P_OUT_ERRORESCONTADOR': 0, 'P_OUT_MENSAJES': [{'id': 0, 'tipo': 'OK', 'codigo': 'REVOCACION_MANDATO_OK', 'descripcion': 'mandato revocado'}]}}
REV_ERR = {'RevocacionMandatoPAC': {'P_OUT_ERRORESCONTADOR': 2, 'P_OUT_MENSAJES': [{'id': 0, 'tipo': 'ERROR', 'codigo': 'MANDATO_NO_REVOCABLE', 'descripcion': 'no se puede revocar'}]}}

# ================================================================================================== authorization by blotter (APV.AutorizaGestionContrato = S)
LAUT = L(KF, 'envioBlotterAutorizacion.compareTo\\("S"\\) == 0', 'AgregarLogSAAPV\\(con, in, ValorAntiguoLog, in.p_ContratosSAAPV.get\\(0\\).IdEstadoSolicitud')
caso(250, K, 'blotter-revocacion-crea-evento-y-actualiza', 'S + REV: one authorization event (glosa with document, name, folio and description), the contract is updated and logged',
     LAUT, e('REV', [row('F-K1')]), seed(PER, contrato(9001, 'F-K1')), config=AUT, tablas=TK)
caso(251, K, 'blotter-cierre-persona-inexistente-glosa-solo-folio', 'S + CER with a person the query does not find: the glosa is the folio alone',
     L(KF, 'evento.Glosa = row.IdFolioContratoSAAPV'), e('CER', [row('F-K1', idp=999, est='CER')]), seed(PER, contrato(9001, 'F-K1')), config=AUT, tablas=TK)
caso(252, K, 'blotter-vigente-glosa-sin-descripcion', 'S + VIG: the glosa has no description (only REV and CER append it)', L(KF, 'in.p_Opcion.compareTo\\("REV"\\) == 0 \\|\\| in.p_Opcion.compareTo\\("CER"\\) == 0'),
     e('VIG', [row('F-K1', est='VIG', sol='PDA', envio='NEV')]), seed(PER, contrato(9001, 'F-K1', IdEstado='PDI')), config=AUT, tablas=TK)
caso(253, K, 'blotter-anular-usa-modo-COMPLE', 'S + ANU: any option other than REV/CER/VIG is mode COMPLE and the glosa has no description', L(KF, 'modo = "COMPLE"'),
     e('ANU', [row('F-K1', est='ANU')]), seed(PER, contrato(9001, 'F-K1')), config=AUT, tablas=TK)
caso(254, K, 'blotter-dos-contratos-dos-eventos', 'S + REV with two contracts: two events in one call, the log is written for the first one only', LAUT,
     e('REV', [row('F-K1'), row('F-K2')]), seed(PER, contrato(9001, 'F-K1'), contrato(9002, 'F-K2')), config=AUT, tablas=TK)
caso(255, K, 'blotter-lista-vacia-lanza-IndexOutOfBounds', 'S with an empty list: no event, then in.p_ContratosSAAPV.get(0) throws IndexOutOfBoundsException',
     L(KF, 'ConsultarEstadoGlosa ValorAntiguoLog = ConsultarEstadoGlosa\\(con, sql, in.p_ContratosSAAPV.get\\(0\\)'), e('REV', []), seed(PER, contrato(9001, 'F-K1')), config=AUT, tablas=TK)
caso(256, K, 'blotter-contrato-inexistente-no-actualiza', 'S + REV for a folio that does not exist: the event is still created, nothing is updated (old values unknown)',
     L(KF, 'ValorAntiguoLog = ConsultarEstadoGlosa'), e('REV', [row('NO-EXISTE')]), seed(PER, contrato(9001, 'F-K1')), config=AUT, tablas=TK)

# ================================================================================================== ANU
LANU = L(KF, 'if\\(in.p_Opcion.compareTo\\("ANU"\\) == 0\\)', 'ANULACION_CONTRATO_SAAPV_EXITOSA')
caso(260, K, 'anular-contrato-pendiente-de-ingreso', 'ANU of a PDI contract without traspaso or mandate: contract updated, annulment OK message, log', LANU,
     e('ANU', [row('F-K1', est='ANU', sol='ANU')]), seed(PER, contrato(9001, 'F-K1', IdEstado='PDI')), tablas=TK)
caso(261, K, 'anular-contrato-con-traspaso-de-ingreso', 'ANU of a PDI contract of type TRA with its INGRESO traspaso: the traspaso goes to ANU with CierreSolicitud = FechaRevocacion',
     L(KF, 'Traspaso.IdEstado = "ANU"'),
     e('ANU', [row('F-K1', est='ANU', sol='ANU', fecha='2025-03-20')]),
     seed(PER, contrato(9001, 'F-K1', IdEstado='PDI', CodigoTipoSolicitud='TRA'), traspaso(5001, 9001, est='PDI')), tablas=TK)
caso(262, K, 'anular-contrato-vigente-no-se-anula-ni-avisa', 'ANU of a contract that is not PDI, DesdePac N: silently skipped (no message, no update)',
     L(KF, '"PDI".equalsIgnoreCase\\(traspaso.IdEstado\\)\\)\\{'), e('ANU', [row('F-K1', est='ANU', sol='ANU')]), seed(PER, contrato(9001, 'F-K1', IdEstado='VIG')), tablas=TK)
caso(263, K, 'anular-desde-pac-contrato-no-pendiente-error', 'ANU with p_DesdePac S on a non-PDI contract: ERROR_ANULAR_CONTRATO "debe estar en estado Pendiente de Ingreso"',
     L(KF, 'ERROR_ANULAR_CONTRATO'), e('ANU', [row('F-K1', est='ANU', sol='ANU')], p_DesdePac='S'), seed(PER, contrato(9001, 'F-K1', IdEstado='VIG')), tablas=TK)
caso(264, K, 'anular-contrato-con-mandato-pac-lo-anula', 'ANU of a PDI contract with a PAC mandate: AnulacionMandatoPAC is called (module TES, DesdeSAAPV S) and its messages are appended',
     L(KF, 'AnulacionMandatoPAC anuMandato = new', 'out.P_OUT_MENSAJES.add\\(mensajeMandato\\)'),
     e('ANU', [row('F-K1', est='ANU', sol='ANU')]), seed(PER, contrato(9001, 'F-K1', IdEstado='PDI'), mandato(300, 'F-K1', 'ACT')),
     colaboradores=ANU_OK, tablas=TK)
caso(265, K, 'anular-mandato-con-error-retorna-sin-mas-mensajes', 'ANU with the mandate annulment answering errors: the output takes its counter and messages and returns (the contract was already updated)',
     L(KF, 'anuMandatoOut.P_OUT_ERRORESCONTADOR.longValue\\(\\) > 0L', 'return out;'),
     e('ANU', [row('F-K1', est='ANU', sol='ANU')]), seed(PER, contrato(9001, 'F-K1', IdEstado='PDI'), mandato(300, 'F-K1', 'ACT')),
     colaboradores=ANU_ERR, tablas=TK)
caso(266, K, 'anular-desde-pac-no-llama-al-mandato', 'ANU with p_DesdePac S: the PAC mandate service is not called', L(KF, 'if\\(!in.p_DesdePac.equals\\("S"\\)\\) \\{', 'AnulacionMandatoPAC anuMandato'),
     e('ANU', [row('F-K1', est='ANU', sol='ANU')], p_DesdePac='S'), seed(PER, contrato(9001, 'F-K1', IdEstado='PDI'), mandato(300, 'F-K1', 'ACT')), colaboradores=ANU_OK, tablas=TK)
caso(267, K, 'anular-dos-contratos-uno-pendiente-y-otro-vigente', 'ANU of two contracts, only the PDI one is annulled (and only it gets the OK message)', LANU,
     e('ANU', [row('F-K1', est='ANU', sol='ANU'), row('F-K2', est='ANU', sol='ANU')]),
     seed(PER, contrato(9001, 'F-K1', IdEstado='PDI'), contrato(9002, 'F-K2', IdEstado='VIG')), tablas=TK)
caso(268, K, 'anular-sin-contratos-coincidentes', 'ANU of a folio of another person: nothing matches, nothing is logged, no message', LANU,
     e('ANU', [row('F-K1', idp=502, est='ANU', sol='ANU')]), seed(PER, contrato(9001, 'F-K1', IdEstado='PDI')), tablas=TK)
caso(269, K, 'anular-con-desdepac-nulo-lanza-NPE', 'ANU with p_DesdePac null in the JSON: !in.p_DesdePac.equals("S") throws NullPointerException after the contract update',
     L(KF, 'if\\(!in.p_DesdePac.equals\\("S"\\)\\) \\{', 'AnulacionMandatoPAC anuMandato'),
     e('ANU', [row('F-K1', est='ANU', sol='ANU')], p_DesdePac=None), seed(PER, contrato(9001, 'F-K1', IdEstado='PDI')), tablas=TK)

# ================================================================================================== CER
LCER = L(KF, 'else if\\("CER".equals\\(in.p_Opcion\\)\\)\\{', 'ConsultarEstadoGlosa ValorAntiguoLog = ConsultarEstadoGlosa')
caso(280, K, 'cerrar-con-traspaso-pendiente-y-mandato', 'CER of a contract whose traspaso is PDI and has a PAC mandate: traspaso CER, mandate annulment called with Estado ANU, contract closed',
     LCER, e('CER', [row('F-K1', est='CER', sol='PDS', fecha='2025-03-25')]),
     seed(PER, contrato(9001, 'F-K1', IdEstado='VIG', CodigoTipoSolicitud='TRA'), traspaso(5001, 9001, est='PDI'), mandato(300, 'F-K1', 'ACT')),
     colaboradores=ANU_OK, tablas=TK)
caso(281, K, 'cerrar-sin-traspaso-ni-mandato', 'CER of a plain contract: the traspaso state is null, the contract is closed, no collaborator is called', LCER,
     e('CER', [row('F-K1', est='CER')]), seed(PER, contrato(9001, 'F-K1')), tablas=TK)
caso(282, K, 'cerrar-traspaso-sin-informacion-de-cliente-advierte', 'CER with a traspaso in TPD: warning ERROR_CERRAR_CONTRATO_SIN_INFO_CLIENTE (counter of warnings 1), the traspaso is closed too',
     L(KF, 'out.P_OUT_ADVERTENCIASCONTADOR \\+= 1'),
     e('CER', [row('F-K1', est='CER')]), seed(PER, contrato(9001, 'F-K1', CodigoTipoSolicitud='TRA'), traspaso(5001, 9001, est='TPD')), tablas=TK)
caso(283, K, 'cerrar-traspaso-en-otro-estado-se-ignora', 'CER with a traspaso already CER: the contract is NOT closed and nothing is reported', LCER,
     e('CER', [row('F-K1', est='CER')]), seed(PER, contrato(9001, 'F-K1', CodigoTipoSolicitud='TRA'), traspaso(5001, 9001, est='ING')), tablas=TK)
caso(284, K, 'cerrar-folio-inexistente-lanza-NPE', 'CER for a folio the query does not return: contratosTraspasos.IdEstado throws NullPointerException', L(KF, 'ConsultaContratoTraspaso.get\\(row.IdFolioContratoSAAPV\\);\\s*$'),
     e('CER', [row('NO-EXISTE', est='CER')]), seed(PER, contrato(9001, 'F-K1')), tablas=TK)
caso(285, K, 'cerrar-mandato-con-errores-suma-contadores', 'CER whose mandate annulment answers 1 error: messages are appended, the error counter is added, and the contract is still closed',
     L(KF, 'salidaAnularMandatoPAC.P_OUT_MENSAJES'),
     e('CER', [row('F-K1', est='CER')]), seed(PER, contrato(9001, 'F-K1'), mandato(300, 'F-K1', 'ACT')), colaboradores=ANU_ERR, tablas=TK)
caso(286, K, 'cerrar-dos-mandatos-del-mismo-folio-lanza-IllegalState', 'CER where two mandates share the folio: Collectors.toMap throws IllegalStateException (duplicate key)',
     L(KF, 'Collectors.toMap\\(traspaso'), e('CER', [row('F-K1', est='CER')]),
     seed(PER, contrato(9001, 'F-K1'), mandato(300, 'F-K1', 'ACT'), mandato(301, 'F-K1', 'INA')), tablas=TK)
caso(287, K, 'cerrar-dos-contratos', 'CER of two contracts in one call (two closings, one traspaso)', LCER,
     e('CER', [row('F-K1', est='CER'), row('F-K2', est='CER')]),
     seed(PER, contrato(9001, 'F-K1', CodigoTipoSolicitud='TRA'), contrato(9002, 'F-K2'), traspaso(5001, 9001, est='PDI')), tablas=TK)

# ================================================================================================== REV
LREV = L(KF, 'else if\\("REV".equals\\(in.p_Opcion\\)\\)\\{', 'REVOCACION_CONTRATO_SAAPV_EXITOSA')
caso(300, K, 'revocar-contrato-simple', 'REV of a plain contract: contract updated with the row state, OK message REVOCACION_CONTRATO_SAAPV_EXITOSA', LREV,
     e('REV', [row('F-K1', est='REV')]), seed(PER, contrato(9001, 'F-K1')), tablas=TK)
caso(301, K, 'revocar-con-traspaso-actualiza-el-traspaso-a-ANU', 'REV of a contract with a traspaso: the traspaso goes to ANU and a second log (with the mutated input) is written',
     L(KF, 'ValorAntiguoTraspasoLog = ConsultarEstadoTraspaso', 'AgregarLogSAAPV\\(con, inn, ValorAntiguoTraspasoLog, ContratosRevocar'),
     e('REV', [row('F-K1', est='REV')]), seed(PER, contrato(9001, 'F-K1', CodigoTipoSolicitud='TRA'), traspaso(5001, 9001, est='PDI')), tablas=TK)
caso(302, K, 'revocar-con-mandato-lo-revoca', 'REV with a PAC mandate: RevocacionMandatoPAC is called with the mandate id and state, its messages precede the OK message', L(KF, 'RevocacionMandatoPAC revMandato = new', 'REVOCACION_CONTRATO_SAAPV_EXITOSA'),
     e('REV', [row('F-K1', est='REV')]), seed(PER, contrato(9001, 'F-K1'), mandato(300, 'F-K1', 'ACT')), colaboradores=REV_OK, tablas=TK)
caso(303, K, 'revocar-mandato-con-errores-retorna', 'REV whose mandate revocation answers errors: the output is replaced by its counter and messages and returns without the OK message',
     L(KF, 'revMandatoOut.P_OUT_ERRORESCONTADOR.longValue\\(\\) > 0L', 'return out;'),
     e('REV', [row('F-K1', est='REV')]), seed(PER, contrato(9001, 'F-K1'), mandato(300, 'F-K1', 'ACT')), colaboradores=REV_ERR, tablas=TK)
caso(304, K, 'revocar-desde-pac-no-llama-al-mandato', 'REV with p_DesdePac S: no mandate service call', L(KF, 'if\\(!in.p_DesdePac.equals\\("S"\\)\\) \\{', 'RevocacionMandatoPAC revMandato'),
     e('REV', [row('F-K1', est='REV')], p_DesdePac='S'), seed(PER, contrato(9001, 'F-K1'), mandato(300, 'F-K1', 'ACT')), colaboradores=REV_OK, tablas=TK)
caso(305, K, 'revocar-folio-inexistente-lanza-NPE', 'REV for a folio the query does not return: contratosParaRevocar.FolioContratoSAAPV throws NullPointerException',
     L(KF, 'contratosParaRevocar.FolioContratoSAAPV.equalsIgnoreCase'), e('REV', [row('NO-EXISTE')]), seed(PER, contrato(9001, 'F-K1')), tablas=TK)

# ================================================================================================== VIG and the default branch
LVIG = L(KF, 'else if\\("VIG".equals\\(in.p_Opcion\\)\\)\\{', 'else \\{\\s*$')
caso(310, K, 'vigente-con-traspaso-lo-deja-pendiente-de-ingreso', 'VIG of a contract with a traspaso: contract updated, the traspaso goes to PDI', LVIG,
     e('VIG', [row('F-K1', est='VIG', sol='PDA', envio='NEV')]),
     seed(PER, contrato(9001, 'F-K1', IdEstado='PDI', CodigoTipoSolicitud='TRA'), traspaso(5001, 9001, est='ING')), tablas=TK)
caso(311, K, 'vigente-sin-traspaso', 'VIG of a plain contract: only the contract is updated, no message', LVIG,
     e('VIG', [row('F-K1', est='VIG', sol='PDA', envio='NEV')]), seed(PER, contrato(9001, 'F-K1', IdEstado='PDI')), tablas=TK)
caso(312, K, 'completar-usa-la-rama-por-defecto', 'COM (any other option): contract, glosa and log are updated, no message', L(KF, 'ConsultarEstadoGlosa ValorAntiguoLog = ConsultarEstadoGlosa\\(con, sql, in.p_ContratosSAAPV.get\\(0\\).IdFolioContratoSAAPV\\);\\s*$'),
     e('COM', [row('F-K1', est='COM', sol='PDA')]), seed(PER, contrato(9001, 'F-K1', IdEstado='VIG')), tablas=TK)
caso(313, K, 'opcion-nula-lanza-NPE', 'null option: in.p_Opcion.compareTo throws NullPointerException', L(KF, 'in.p_Opcion.compareTo\\("REV"\\) == 0\\)\\{'),
     e(None, [row('F-K1')]), seed(PER, contrato(9001, 'F-K1')), tablas=TK)
caso(314, K, 'glosa-por-defecto-cero-y-vacia', 'a row without glosa fields keeps the constructor defaults (IdGlosa 0, empty description)', L(KF, 'IdGlosa = 0L'),
     e('COM', [{'IdFolioContratoSAAPV': 'F-K1', 'IdEstado': 'COM', 'IdEstadoSolicitud': 'PDA', 'IdEstadoEnvioNotificacion': ''}]),
     seed(PER, contrato(9001, 'F-K1', IdGlosa=9, DescripcionGlosa='ANTES')), tablas=TK)
