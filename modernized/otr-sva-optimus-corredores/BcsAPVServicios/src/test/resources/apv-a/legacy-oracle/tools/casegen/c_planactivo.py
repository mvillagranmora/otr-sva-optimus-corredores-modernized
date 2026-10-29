"""Cases of CambiarAPlanActivoContratoSAAPV (ids 330-344) and CambiarAPlanActivoContratoSAAPVWrapper (ids 345-359)."""
from lib import *  # noqa: F401,F403

P = 'CambiarAPlanActivoContratoSAAPV'
PF = 'CambiarAPlanActivoContratoSAAPV.java'
W = 'CambiarAPlanActivoContratoSAAPVWrapper'
WF = 'CambiarAPlanActivoContratoSAAPVWrapper.java'
BASE = {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_IdModulo': 'APV'}
T_EMP = 'BSCL.SocioNegocio.BD::EMP.Empleado'
T_GBL = 'BSCL.APV.BD::GBL.PortafoliosCambioPlanActivo'


def e(contratos, **kw):
    d = dict(BASE)
    d['p_ContratoCambioPlanIn'] = contratos
    d.update(kw)
    return d


def cc(idc=9001, idpo=901, idp=501, idcli=701, folio='F-P1'):
    return {'IdPersona': idp, 'IdCliente': idcli, 'IdPortafolio': idpo, 'IdContratoSAAPV': idc, 'FolioContratoSAAPV': folio}


def perfil_apv(activo='PERFIL ACTIVO', abierto='PERFIL BASE', cat='MODERADO'):
    return ins(T_PERFIL, IdPerfilComercialAPV=1, IdEmpresa=1, IdCategoriaInversionista=cat, IdPerfilComercialAbierto=abierto, IdPerfilComercialActivo=activo, IdEstado='VIG')


DIS = ins(T_DISTR, IdDistribucionInversion=1, IdContratoSAAPV=9001, FolioContratoSAAPV='F-P1', IdInstrumento=1021327, Nemotecnico='00391CCLP', MontoPesos=100000, Porcentaje=100, IdEstado='VIG')
BASEP = [persona(501, '11111111-1', idc=701, ejecutivo=335), ins(T_EMP, IdEmpleado=335, IdPersona=335, Borrado='N'), portafolio(901, 701, matriz=1), porcon(8801, 901, 9001),
         contrato(9001, 'F-P1', IdEstado='VIG', IdPlan=1), DIS]
TP = [tabla(T_PORTAFOLIO, '"IdPortafolio"', ['IdPortafolio', 'IdMatrizAgrupadorAPV', 'IdEstado']), tabla(T_GBL, '"IdPortafolio"'),
      tabla(T_CONTRATO, '"IdContratoSAAPV"', ['IdContratoSAAPV', 'FolioContratoSAAPV', 'IdPlan', 'IdEstado', 'IdEstadoSolicitud', 'IdEstadoEnvioNotificacion', 'IdPerfilComercial', 'NombrePlan'])]
MPOR_OK = {'ModificarPortafolio': {'P_OUT_ERRORESCONTADOR': 0}}
SEQP = {'BSCL.APV.BD.Secuencias::TRA.IdTraspaso': 6000, 'BSCL.APV.BD.Secuencias::TRA.IdDetalleOrigenTraspaso': 6500}

# ===================================================================================================== CambiarAPlanActivoContratoSAAPV
caso(330, P, 'sin-contratos-es-error', 'no contracts: the modelos-comerciales warning and NO_SE_INGRESARON_CONTRATOS (error), returns at once', L(PF, r'NO_SE_INGRESARON_CONTRATOS'),
     e([]), seed(BASEP), tablas=TP)
LP = L(PF, r'if\(idsContrato.size\(\) > 0\) \{', r'out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores')
caso(331, P, 'cambia-el-contrato-vigente-a-plan-activo', 'a VIG contract of plan 1: ModificarContratoSAAPV runs with the data read from the tables, ModificarPortafolio is called with the active commercial profile, the portfolio matrix moves 1 -> 7',
     LP, e([cc()]), seed(BASEP, perfil_apv()), colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP)
caso(332, P, 'sin-perfil-activo-no-llama-a-modificar-portafolio', 'no PerfilComercialAPV row for the client category: ModificarPortafolio is skipped, the matrix still moves', L(PF, r'idPerfilComercialAPV == null \|\| ""'),
     e([cc()]), seed(BASEP), colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP)
caso(333, P, 'modificar-portafolio-con-error-y-advertencia', 'ModificarPortafolio answers an error and a warning: messages "CONTRATO F-P1: Modificar Portafolio: ..." with the contract id',
     L(PF, r'ModificarPortafolio.ErrorDetalle errorDetalle'),
     e([cc()]), seed(BASEP, perfil_apv()),
     colaboradores={'ModificarPortafolio': {'P_OUT_ERRORESCONTADOR': 1, 'P_OUT_ERRORESDETALLE': [{'Codigo': 'PERFIL_INVALIDO', 'Descripcion': 'perfil no valido'}],
                                            'P_OUT_ADVERTENCIASDETALLE': [{'Codigo': 'PERFIL_AVISO', 'RequiereAutorizacion': 'N', 'Descripcion': 'aviso del perfil'}]}},
     secuencias=SEQP, tablas=TP)
caso(334, P, 'contrato-no-vigente-no-se-lee-pero-el-plan-del-portafolio-cambia', 'a contract that is not VIG is not selected by any query: ModificarContratoSAAPV receives empty lists, yet the portfolio plan update still runs',
     L(PF, r'if\(lstPortafolios.size\(\) > 0\)\{'), e([cc()]), seed(BASEP[:4], contrato(9001, 'F-P1', IdEstado='CER', IdPlan=1), DIS, perfil_apv()),
     colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP)
caso(335, P, 'con-autorizacion-por-blotter-pasa-IdEstadoSolicitud-PDA', 'APV.AutorizaGestionContrato S: the contract is processed with IdEstadoSolicitud PDA and the authorization event of the VIG change is created',
     L(PF, r'"S".equals\(autorizaGestionContrato\) \? "PDA"'), e([cc()]), seed(BASEP, perfil_apv()), config={'PLATAFORMA/APV.AutorizaGestionContrato': 'S'},
     colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP)
caso(336, P, 'dos-contratos-de-dos-clientes', 'two contracts: both are modified, one ModificarPortafolio call per distinct portfolio, plan change message "Cambio de Plan a 2 Portafolios OK."', LP,
     e([cc(), cc(9002, 902, 502, 702, 'F-P2')]),
     seed(BASEP, perfil_apv(), persona(502, '22222222-2', idc=702, ejecutivo=335), portafolio(902, 702, matriz=2), porcon(8802, 902, 9002), contrato(9002, 'F-P2', IdEstado='VIG', IdPlan=1, IdPersona=502, IdCliente=702,
                                                                                                                                                 DocumentoIdentidadCliente='22222222-2')),
     colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP)
caso(337, P, 'mapa-de-matrices-del-plan-y-matriz-fuera-de-la-lista', 'portfolio matrices 1,2,3,19,4,5,6 map to 7,8,9,20,10,11,12 (other values stay); here matrix 3 maps to 9 and matrix 7 (not in the list) stays; the contract has two portfolios', L(PF, r'sql.batch\(con, SQL_MODIFICAR_PLAN_PORTAFOLIOS'),
     e([cc(9001, 903), cc(9001, 904)]),
     seed(BASEP[:2], portafolio(903, 701, matriz=3), portafolio(904, 701, matriz=7), porcon(8803, 903, 9001), porcon(8804, 904, 9001), BASEP[4:], perfil_apv()),
     colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP)
caso(338, P, 'contrato-con-traspaso-de-ingreso-pendiente', 'a TRA contract with its traspaso and origin details: Modificar replaces the traspaso (PDI) with new ids', L(PF, r'datosDetalleOrigenTraspasoList = sql.query'),
     e([cc()]),
     seed(BASEP[:4], contrato(9001, 'F-P1', IdEstado='VIG', IdPlan=1, CodigoTipoSolicitud='TRA'), DIS, perfil_apv(),
          ins(T_TRASPASO, IdTraspaso=5001, IdContratoSAAPV=9001, IdEmpresa=1, TipoMovimiento='INGRESO', IdEstado='PDI', FechaIngreso='2025-03-01', FechaEgreso='0001-01-01',
              FechaModificacion='0001-01-01', FechaSuscripcion='2025-03-01', CierreSolicitud='0001-01-01', IdPersona=61704),
          ins(T_DETORIGEN, IdDetalleOrigenTraspaso=5101, IdTraspaso=5001, CodigoTipoAhorro='APV', IdEntidadRecaudadora=831, NombreEntidad='AFP UNO', DocumentoEntidad='76240079-0', Fondo='A',
              MontoTraspaso=5000000, UFTraspaso=0, PorcentajeTraspaso=100, IdEstado='VIG', IdInstrumento=0, IdPortafolio=0, CodigoRegimenTributario='0')),
     colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP + [tabla(T_TRASPASO, '"IdTraspaso"'), tabla(T_DETORIGEN, '"IdDetalleOrigenTraspaso"')])
caso(339, P, 'portafolio-nulo-en-la-entrada-lo-guarda-como-nulo', 'a row without IdPortafolio: the batch binds NULL to the global table and the matrix update affects nothing for it', L(PF, r'lstPortafolios.add\(portafolio\)'),
     e([{'IdPersona': 501, 'IdCliente': 701, 'IdContratoSAAPV': 9001, 'FolioContratoSAAPV': 'F-P1'}]), seed(BASEP, perfil_apv()), colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP)
caso(340, P, 'lista-nula-lanza-NPE', 'null contract list: .size() throws NullPointerException after the first warning was added (call rethrows)', L(PF, r'in.p_ContratoCambioPlanIn.size\(\) == 0'),
     e(None), seed(BASEP), tablas=TP)

# ===================================================================================================== Wrapper
LW = L(WF, r'if\("S".equalsIgnoreCase\(cambioPlanRequiereAutorizacion\)\) \{', r'EVENTO_AUTORIZACION_CREADO')
TPW = [tabla(T_PORTAFOLIO, '"IdPortafolio"', ['IdPortafolio', 'IdMatrizAgrupadorAPV'])]


def wc(idc=9001, idpo=901, idp=501, idcli=701, folio='F-P1', rut='11111111-1', reg='APV-A'):
    return {'IdPersona': idp, 'IdCliente': idcli, 'RutCliente': rut, 'IdPortafolio': idpo, 'RegimenTributario': reg, 'IdContratoSAAPV': idc, 'FolioContratoSAAPV': folio}


WAUT = {'APV/APV.CambioPlanRequiereAutorizacion': 'S'}
caso(345, W, 'con-autorizacion-crea-un-evento-por-cliente', 'APV.CambioPlanRequiereAutorizacion S: one MOPLAC event per client with a glosa "RutCliente: ... Portafolios APV: regimen, regimen" and a detail row per contract; message EVENTO_AUTORIZACION_CREADO',
     LW, e([wc(), wc(9002, 902, folio='F-P2', reg='APV-B')]), seed(BASEP), config=WAUT, tablas=TPW)
caso(346, W, 'dos-clientes-dos-eventos', 'two clients: two events (IdUnion 1 and 2, in client id order) and a warning counter of 2', LW,
     e([wc(), wc(9002, 902, 502, 702, 'F-P2', '22222222-2', 'APV-B')]), seed(BASEP), config=WAUT, tablas=TPW)
caso(347, W, 'evento-con-errores-copia-los-errores', 'CrearEventoAutorizacionMasivo answers errors: ERROR messages carry the error counter as id, the output counter is set, no creation message', L(WF, r'salida.P_OUT_ERRORESCONTADOR > 0'),
     e([wc()]), seed(BASEP), config=WAUT,
     colaboradores={'CrearEventoAutorizacionMasivo': {'P_OUT_ERRORESCONTADOR': 2, 'P_OUT_ERRORESDETALLE': [{'Codigo': 'EVT_ERROR1', 'Descripcion': 'primer error'}, {'Codigo': 'EVT_ERROR2', 'Descripcion': 'segundo error'}]}},
     tablas=TPW)
caso(348, W, 'con-autorizacion-sin-contratos-no-crea-evento', 'S with an empty list: no event, empty output', L(WF, r'if \(tablaEventos.size\(\) > 0\)'), e([]), seed(BASEP), config=WAUT, tablas=TPW)
caso(349, W, 'persona-nula-lanza-NPE', 'S with a contract without IdPersona: contratoIn.IdPersona.toString() throws NullPointerException', L(WF, r'contratoIn.IdPersona.toString\(\)'),
     e([{'IdCliente': 701, 'RutCliente': '11111111-1', 'IdPortafolio': 901, 'RegimenTributario': 'APV-A', 'IdContratoSAAPV': 9001, 'FolioContratoSAAPV': 'F-P1'}]), seed(BASEP), config=WAUT, tablas=TPW)
caso(350, W, 'sin-autorizacion-delega-en-cambiar-a-plan-activo', 'CambioPlanRequiereAutorizacion N: the call is delegated to CambiarAPlanActivoContratoSAAPV and its messages/counters are returned (advertencias 0)',
     L(WF, r'cambiarAPlanActivoContratoSAAPV.logica\(con, entradaConfirmar\)'), e([wc()]), seed(BASEP, perfil_apv()), colaboradores=MPOR_OK, secuencias=SEQP, tablas=TPW)
