"""Cases of the authorization hook AutorizacionCambiarAPlanActivoContratoSAAPV (BcsAutorizaciones), ids 526-540.

The legacy platform calls the hook IN PROCESS (ModificarEstadoEventoAutorizacionMasivo.procesarAutorizacionJava: Class.forName(JavaAutorizacion).newInstance(),
then autorizarEvento / rechazarEvento(conn, EntradaAutorizacion)), so the cases use "metodo": "autorizarEvento" | "rechazarEvento": the oracle runner decodes
`entrada` (the legacy JSON of EntradaAutorizacion: p_IdEmpresa, p_IdUsuario, p_IdModulo, p_Lista[Evento]) with the legacy Gson and encodes the SalidaAutorizacion.
"""
from lib import *  # noqa: F401,F403
from c_planactivo import BASEP, MPOR_OK, SEQP, TP, cc, perfil_apv  # noqa: F401

H = 'AutorizacionCambiarAPlanActivoContratoSAAPV'
HF = 'AutorizacionCambiarAPlanActivoContratoSAAPV.java'
T_DETEVT = 'BSCL.Plataforma.BD::AUT.EventoDetalle'


def evt(idevento, idcliente, **kw):
    d = {'IdEvento': idevento, 'IdEmpresa': 1, 'IdModulo': 'APV', 'IdUsuario': 21, 'IdCliente': idcliente}
    d.update(kw)
    return d


def aut(eventos, **kw):
    d = {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_IdModulo': 'APV', 'p_Lista': eventos}
    d.update(kw)
    return d


def det(idevento, valor):
    """AUT.EventoDetalle row as SolicitarCambioAPlanActivo writes it: Valor = IdPersona;IdCliente;IdPortafolio;IdContratoSAAPV;folio."""
    return ins(T_DETEVT, IdEvento=idevento, NombreCampo='Datos', TipoDato='STRING', Valor=valor, Clasificacion='1')


V1 = '501;701;901;9001;F-P1'
LAUT = L(HF, r'for\(MensajeSalida salida : salidaConfirmacion.P_OUT_MENSAJES\)', r'P_OUT_ERRORESCONTADOR = 1L')
LEXI = L(HF, r'hashSalidaConfirmar.containsKey\(id\)')
LELSE = L(HF, r's.P_OUT_ERRORESCONTADOR = salidaConfirmacion.P_OUT_ERRORESCONTADOR')

caso(526, H, 'exito-cuando-el-cliente-del-evento-es-el-numero-de-portafolios', 'one event whose IdCliente (1) equals the number of portfolios of the plan change (the id of the OK message MODIFICA_PLAN_PORTAFOLIO_OK): the contract is changed, the output is OPERACION_AUTORIZADA with resultado EXITO',
     LEXI, aut([evt(5001, 1)]), seed(BASEP, perfil_apv(), det(5001, V1)), colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP, metodo='autorizarEvento')
caso(527, H, 'cliente-real-no-coincide-y-devuelve-los-mensajes-del-cambio-de-plan', 'IdCliente 701 is not the id of any OK message: the plan change is applied but the output is its message list and its counter, with no resultado (legacy defect: the id compared is the client, the OK message carries the portfolio count)',
     LELSE, aut([evt(5001, 701)]), seed(BASEP, perfil_apv(), det(5001, V1)), colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP, metodo='autorizarEvento')
caso(528, H, 'dos-detalles-del-evento-dos-contratos-y-exito-con-cliente-2', 'two detail rows of the same event: two contracts are changed together (two portfolios, OK message id 2) and the event with IdCliente 2 succeeds',
     LAUT, aut([evt(5001, 2)]),
     seed(BASEP, perfil_apv(), persona(502, '22222222-2', idc=702, ejecutivo=335), portafolio(902, 702, matriz=2), porcon(8802, 902, 9002),
          contrato(9002, 'F-P2', IdEstado='VIG', IdPlan=1, IdPersona=502, IdCliente=702, DocumentoIdentidadCliente='22222222-2'),
          det(5001, V1), det(5001, '502;702;902;9002;F-P2')),
     colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP, metodo='autorizarEvento')
caso(529, H, 'evento-sin-detalle-el-contador-queda-en-cero-con-un-error', 'the event has no detail rows: the plan change receives no contract, answers NO_SE_INGRESARON_CONTRATOS (error) with its counter still 0, and the output copies both',
     LELSE, aut([evt(5002, 701)]), seed(BASEP, perfil_apv(), det(5001, V1)), colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP, metodo='autorizarEvento')
caso(530, H, 'error-al-modificar-portafolio-se-copia-con-su-contador', 'ModificarPortafolio answers an error: the plan change counts it, the event does not succeed (client 701) and the output carries the error messages and the counter 1',
     LELSE, aut([evt(5001, 701)]), seed(BASEP, perfil_apv(), det(5001, V1)),
     colaboradores={'ModificarPortafolio': {'P_OUT_ERRORESCONTADOR': 1, 'P_OUT_ERRORESDETALLE': [{'Codigo': 'PERFIL_INVALIDO', 'Descripcion': 'perfil no valido'}]}},
     secuencias=SEQP, tablas=TP, metodo='autorizarEvento')
caso(531, H, 'exito-oculta-los-errores-del-cambio-de-plan', 'IdCliente 1 with an error of ModificarPortafolio: the OK message of the portfolio update still exists, so the event is EXITO with counter 0 and the errors of the plan change are not reported (legacy defect kept)',
     LEXI, aut([evt(5001, 1)]), seed(BASEP, perfil_apv(), det(5001, V1)),
     colaboradores={'ModificarPortafolio': {'P_OUT_ERRORESCONTADOR': 1, 'P_OUT_ERRORESDETALLE': [{'Codigo': 'PERFIL_INVALIDO', 'Descripcion': 'perfil no valido'}]}},
     secuencias=SEQP, tablas=TP, metodo='autorizarEvento')
caso(532, H, 'cliente-nulo-se-compara-con-el-id-cero', 'an event without IdCliente is compared with id 0: no message has id 0 with type OK, so the output is the message list of the plan change',
     LEXI, aut([evt(5001, None)]), seed(BASEP, perfil_apv(), det(5001, V1)), colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP, metodo='autorizarEvento')
caso(533, H, 'lista-de-eventos-vacia-lanza-IndexOutOfBounds', 'an empty p_Lista: the detail query binds NULL and finds nothing, then p_Lista.get(0) throws IndexOutOfBoundsException (outside the protected block)',
     L(HF, r'entradaConfirmar.p_IdEmpresa = entrada.p_Lista.get\(0\).IdEmpresa'), aut([]), seed(BASEP, perfil_apv(), det(5001, V1)), colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP, metodo='autorizarEvento')
caso(534, H, 'dos-eventos-la-consulta-es-invalida-y-lanza-NPE', 'two events: the list parameter expands to "= ?,?" (syntax error), the SQLException is swallowed, the result stays null and the loop throws NullPointerException; nothing is changed',
     L(HF, r'catch \(SQLException e\)'), aut([evt(5001, 1), evt(5003, 1)]), seed(BASEP, perfil_apv(), det(5001, V1), det(5003, V1)), colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP, metodo='autorizarEvento')
caso(535, H, 'valor-del-detalle-no-numerico-lanza-NumberFormatException', 'a detail whose IdPersona part is not a number: Long.parseLong throws NumberFormatException before the protected block',
     L(HF, r'Long.parseLong\(parts\[0\]\)'), aut([evt(5001, 1)]), seed(BASEP, perfil_apv(), det(5001, 'abc;701;901;9001;F-P1')), colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP, metodo='autorizarEvento')
caso(536, H, 'valor-del-detalle-con-menos-de-cuatro-partes-lanza-ArrayIndexOutOfBounds', 'a detail with three parts: parts[3] throws ArrayIndexOutOfBoundsException',
     L(HF, r'Long.parseLong\(parts\[3\]\)'), aut([evt(5001, 1)]), seed(BASEP, perfil_apv(), det(5001, '501;701;901')), colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP, metodo='autorizarEvento')
caso(537, H, 'valor-del-detalle-nulo-lanza-NPE', 'a detail with a NULL value: Valor.split throws NullPointerException',
     L(HF, r'detalleEvento.Valor.split'), aut([evt(5001, 1)]), seed(BASEP, perfil_apv(), ins(T_DETEVT, IdEvento=5001, NombreCampo='Datos', TipoDato='STRING', Valor=None, Clasificacion='1')),
     colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP, metodo='autorizarEvento')
caso(538, H, 'el-cambio-de-plan-lanza-excepcion-y-queda-contador-uno', 'the contract table has the contract twice: the plan change throws (key field is not unique), the hook swallows it and answers counter 1 with no messages',
     L(HF, r'catch \(Exception e1\)'), aut([evt(5001, 1)]), seed(BASEP, perfil_apv(), contrato(9001, 'F-P1', IdEstado='VIG', IdPlan=1), det(5001, V1)),
     colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP, metodo='autorizarEvento')
caso(539, H, 'rechazar-no-hace-nada', 'the rejection of a plan change has no effect: empty output, no table touched',
     L(HF, r'public SalidaAutorizacion rechazarEvento'), aut([evt(5001, 701)]), seed(BASEP, perfil_apv(), det(5001, V1)), colaboradores=MPOR_OK, secuencias=SEQP, tablas=TP, metodo='rechazarEvento')
