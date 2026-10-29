"""Cases of the small family A classes: CambiarEstadoEnvioNotificacionContratoSAAPV, ObtenerFolioSuscripcion, AsociarPerfilesAPV,
ModificarRelacionLaboral, ConsultarPreferenciaPagoPortafolio."""
from lib import *  # noqa: F401,F403

BASE = {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_IdModulo': 'APV'}


def e(**kw):
    d = dict(BASE)
    d.update(kw)
    return d


# ===================================================================================================== CambiarEstadoEnvioNotificacionContratoSAAPV
N = 'CambiarEstadoEnvioNotificacionContratoSAAPV'
NF = 'CambiarEstadoEnvioNotificacionContratoSAAPV.java'
TN = [tabla(T_CONTRATO, '"IdContratoSAAPV"', ['IdContratoSAAPV', 'FolioContratoSAAPV', 'IdEstado', 'IdEstadoEnvioNotificacion'])]

caso(1, N, 'un-contrato-queda-enviado', 'one contract: IdEstadoEnvioNotificacion is forced to ENV whatever the input says', L(NF, 'List<Params> params = new', 'sql.batch'),
     e(p_Opcion='VIG', p_ContratosSAAPV=[{'IdFolioContratoSAAPV': 'F-N1', 'IdEstadoEnvioNotificacion': 'NEV'}]),
     seed(contrato(9001, 'F-N1'), contrato(9002, 'F-N2')), tablas=TN)
caso(2, N, 'dos-contratos-quedan-enviados', 'two contracts are updated in one batch; a third one not listed keeps NEV', L(NF, 'List<Params> params = new', 'sql.batch'),
     e(p_Opcion='VIG', p_ContratosSAAPV=[{'IdFolioContratoSAAPV': 'F-N1'}, {'IdFolioContratoSAAPV': 'F-N2'}]),
     seed(contrato(9001, 'F-N1'), contrato(9002, 'F-N2'), contrato(9003, 'F-N3')), tablas=TN)
caso(3, N, 'lista-vacia-no-actualiza', 'empty list: nothing is updated and the output has no messages', L(NF, 'List<Params> params = new', 'sql.batch'),
     e(p_Opcion='VIG', p_ContratosSAAPV=[]), seed(contrato(9001, 'F-N1')), tablas=TN)
caso(4, N, 'folio-inexistente-sin-mensaje', 'a folio that does not exist updates nothing and reports nothing', L(NF, 'sql.batch'),
     e(p_Opcion='VIG', p_ContratosSAAPV=[{'IdFolioContratoSAAPV': 'NO-EXISTE'}]), seed(contrato(9001, 'F-N1')), tablas=TN)
caso(5, N, 'folio-nulo-no-actualiza', 'a row without folio binds NULL: no contract matches', L(NF, 'p.FolioContratoSAAPV = row'),
     e(p_Opcion='VIG', p_ContratosSAAPV=[{'IdEstadoEnvioNotificacion': 'NEV'}]), seed(contrato(9001, 'F-N1')), tablas=TN)
caso(6, N, 'lista-nula-lanza-NPE', 'JSON null list: the for loop throws NullPointerException (legacy behaviour)', L(NF, 'for \(ContratosSAAPV row'),
     e(p_Opcion='VIG', p_ContratosSAAPV=None), seed(contrato(9001, 'F-N1')), tablas=TN)
caso(7, N, 'mismo-folio-repetido', 'the same folio twice is updated twice (same final state)', L(NF, 'for \(ContratosSAAPV row', 'sql.batch'),
     e(p_Opcion='VIG', p_ContratosSAAPV=[{'IdFolioContratoSAAPV': 'F-N1'}, {'IdFolioContratoSAAPV': 'F-N1'}]), seed(contrato(9001, 'F-N1')), tablas=TN)

# ===================================================================================================== ObtenerFolioSuscripcion
F = 'ObtenerFolioSuscripcion'
FF = 'ObtenerFolioSuscripcion.java'
TF = [tabla(T_FOLIO, '"Canal"')]

caso(10, F, 'canal-nuevo-crea-folio-uno', 'a channel without counter gets folio 1 inserted and answers W1', L(FF, 'List<InsertUpdateFolioSuscripcion>', 'salida.p_Out_ContadorError = 0L', ),
     e(p_Canal='W'), '', tablas=TF)
caso(11, F, 'canal-existente-llama-al-procedimiento', 'an existing counter calls the stored procedure with Folio+1 (recorded, not executed) and answers W6',
     L(FF, 'new StoredProcedure', 'p_Out_FolioSuscripcion = '), e(p_Canal='W'), seed(ins(T_FOLIO, Canal='W', Folio=5)), tablas=TF)
caso(12, F, 'canal-vacio-error', 'empty channel: NO_SE_INGRESO_UN_CANAL with counter 1', L(FF, 'NO_SE_INGRESO_UN_CANAL', 'p_Out_Mensajes = mensajesErrores'), e(p_Canal=''), '', tablas=TF)
caso(13, F, 'canal-ausente-lanza-NPE', 'channel not informed: in.p_Canal.isEmpty() throws NullPointerException', L(FF, 'in.p_Canal.isEmpty'),
     e(), '', tablas=TF)
caso(14, F, 'canal-distingue-mayusculas', 'channel lookup is case sensitive: "w" is a new channel', L(FF, 'in.p_Canal.isEmpty', 'salida.p_Out_ContadorError = 0L'),
     e(p_Canal='w'), seed(ins(T_FOLIO, Canal='W', Folio=5)), tablas=TF)
caso(15, F, 'canal-largo-concatena-folio', 'folio text is Canal + (Folio+1): multi-letter channel BIZ with folio 99 answers BIZ100', L(FF, 'p_Out_FolioSuscripcion = SelectFolioSuscripcion'),
     e(p_Canal='BIZ'), seed(ins(T_FOLIO, Canal='BIZ', Folio=99)), tablas=TF)

# ===================================================================================================== AsociarPerfilesAPV
A = 'AsociarPerfilesAPV'
AF = 'AsociarPerfilesAPV.java'
TA = [tabla(T_PERFIL, '"IdPerfilComercialAPV", "IdPerfilComercialAbierto"')]


def perfil(i, cat, ab, ac, estado='VIG', emp=1):
    return ins(T_PERFIL, IdPerfilComercialAPV=i, IdEmpresa=emp, IdCategoriaInversionista=cat, IdPerfilComercialAbierto=ab,
               IdPerfilComercialActivo=ac, IdEstado=estado)


def p(cat, ab, ac):
    return {'IdCategoriaInversionista': cat, 'IdPerfilComercialAbierto': ab, 'IdPerfilComercialActivo': ac}


caso(20, A, 'lista-vacia-error', 'no profiles: NO_SE_INGRESARON_PARAMETROS', L(AF, 'in.p_AsociarPerfilesAPVIn.size\(\) == 0', 'NO_SE_INGRESARON_PARAMETROS'), e(p_AsociarPerfilesAPVIn=[]), '', tablas=TA)
caso(21, A, 'categoria-vacia-error', 'empty category: CATEGORIA_INVERSIONISTA_VACIO', L(AF, 'CATEGORIA_INVERSIONISTA_VACIO'), e(p_AsociarPerfilesAPVIn=[p('', 'AB', 'AC')]), '',
     tablas=TA)
caso(22, A, 'perfil-abierto-vacio-error', 'empty open profile: PERFIL_COMERCIAL_ABIERTO_VACIO', L(AF, 'PERFIL_COMERCIAL_ABIERTO_VACIO'),
     e(p_AsociarPerfilesAPVIn=[p('MODERADO', '', 'AC')]), '', tablas=TA)
caso(23, A, 'perfil-activo-vacio-error', 'empty active profile: PERFIL_COMERCIAL_ACTIVO_VACIO', L(AF, 'PERFIL_COMERCIAL_ACTIVO_VACIO'),
     e(p_AsociarPerfilesAPVIn=[p('MODERADO', 'AB', '')]), '', tablas=TA)
caso(24, A, 'categoria-nula-error', 'null category is treated as empty', L(AF, 'CATEGORIA_INVERSIONISTA_VACIO'),
     e(p_AsociarPerfilesAPVIn=[{'IdPerfilComercialAbierto': 'AB', 'IdPerfilComercialActivo': 'AC'}]), '', tablas=TA)
caso(25, A, 'un-perfil-valido-inserta', 'one valid profile is inserted with the next sequence value 500, VIG', L(AF, 'perfilComercialAPVList.size\(\) > 0', 'sql.batch\(con,SQL_INSERT_PERFIL'),
     e(p_AsociarPerfilesAPVIn=[p('MODERADO', 'AB', 'AC')]), '', secuencias={'BSCL.APV.BD.Secuencia::APV.IdPerfilComercialAPV': 500}, tablas=TA)
caso(26, A, 'dos-perfiles-comparten-el-mismo-id', 'two profiles get the SAME id: the loop never increments i (legacy defect)', L(AF, 'int i = 0;', 'sql.batch\(con,SQL_INSERT_PERFIL'),
     e(p_AsociarPerfilesAPVIn=[p('MODERADO', 'AB1', 'AC1'), p('AGRESIVO', 'AB2', 'AC2')]), '',
     secuencias={'BSCL.APV.BD.Secuencia::APV.IdPerfilComercialAPV': 500}, tablas=TA)
caso(27, A, 'perfil-repetido-se-inserta-una-vez', 'the same triple twice is inserted once', L(AF, 'hash = perfil.IdCategoriaInversionista', 'perfilComercialAPVList.add'),
     e(p_AsociarPerfilesAPVIn=[p('MODERADO', 'AB', 'AC'), p('MODERADO', 'AB', 'AC')]), '',
     secuencias={'BSCL.APV.BD.Secuencia::APV.IdPerfilComercialAPV': 500}, tablas=TA)
caso(28, A, 'abierto-ya-asociado-a-la-categoria', 'an open profile already associated to the category: error PLAN_ABIERTO_ASOCIADO_CATEGORIA_INVERSIONISTA_EXISTE '
     '(and PLAN_ABIERTO_ASOCIADO_PLAN_ACTIVO_EXISTE when the pair repeats), nothing inserted', L(AF, 'validaciones.size\(\) > 0', 'return out;'),
     e(p_AsociarPerfilesAPVIn=[p('MODERADO', 'AB', 'AC')]), seed(perfil(1, 'MODERADO', 'AB', 'AC')),
     secuencias={'BSCL.APV.BD.Secuencia::APV.IdPerfilComercialAPV': 500}, tablas=TA)
caso(29, A, 'abierto-ya-asociado-con-otro-activo', 'open profile associated to another active one: only the first validation fires', L(AF, 'validaciones.size\(\) > 0', 'return out;'),
     e(p_AsociarPerfilesAPVIn=[p('MODERADO', 'AB', 'AC2')]), seed(perfil(1, 'MODERADO', 'AB', 'AC')),
     secuencias={'BSCL.APV.BD.Secuencia::APV.IdPerfilComercialAPV': 500}, tablas=TA)
caso(30, A, 'activo-ya-es-abierto-de-otro', 'the requested pair is the reverse of an existing one (active as open and open as active): PLAN_ACTIVO_ASOCIADO_PLAN_ABIERTO_EXISTE',
     L(AF, 'validaciones.size\(\) > 0', 'return out;'), e(p_AsociarPerfilesAPVIn=[p('MODERADO', 'AC', 'AB')]), seed(perfil(1, 'MODERADO', 'AB', 'AC')),
     secuencias={'BSCL.APV.BD.Secuencia::APV.IdPerfilComercialAPV': 500}, tablas=TA)
caso(31, A, 'registro-inactivo-u-otra-empresa-no-valida', 'an inactive row (INA) and a row of another empresa do not trigger the validations: inserted',
     L(AF, 'perfilComercialAPVList.size\(\) > 0', 'sql.batch\(con,SQL_INSERT_PERFIL'), e(p_AsociarPerfilesAPVIn=[p('MODERADO', 'AB', 'AC')]),
     seed(perfil(1, 'MODERADO', 'AB', 'AC', estado='INA'), perfil(2, 'MODERADO', 'AB', 'AC', emp=2)),
     secuencias={'BSCL.APV.BD.Secuencia::APV.IdPerfilComercialAPV': 500}, tablas=TA)
caso(32, A, 'lista-nula-lanza-NPE', 'JSON null list: in.p_AsociarPerfilesAPVIn.size() throws NullPointerException (legacy behaviour)', L(AF, 'in.p_AsociarPerfilesAPVIn.size'),
     e(p_AsociarPerfilesAPVIn=None), '', tablas=TA)

# ===================================================================================================== ModificarRelacionLaboral
R = 'ModificarRelacionLaboral'
RF = 'ModificarRelacionLaboral.java'
TR = [tabla(T_RELLAB, '"IdPersona", "IdEmpleador"')]


def rl(per, emp, cargo='JEFCUS', ingreso=1500000.5, ini='2025-01-01', ret='0001-01-01', estado='VIG'):
    return {'IdPersona': per, 'IdEmpleador': emp, 'CodigoTipoCargoEmpleado': cargo, 'IngresoMensual': ingreso, 'FechaIngresoCargo': ini,
            'FechaRetiroCargo': ret, 'IdEstado': estado}


caso(40, R, 'inserta-la-relacion', 'no requirement errors: the labour relation row is inserted', L(RF, 'basicos.logica', 'sql.batch'),
     e(p_RelacionLaboral=[rl(501, 801)]), '', tablas=TR)
caso(41, R, 'requisitos-con-error-no-inserta', 'RevisarRequisitosBasicos answers an ERROR: it is copied to the output and nothing is inserted', L(RF, 'getNumeroErrores\(salidaBasicos', 'return out;'),
     e(p_RelacionLaboral=[rl(501, 801)]), '',
     colaboradores={'requisitosBasicos': [{'id': 1, 'tipo': 'ERROR', 'codigo': 'USUARIO_NO_EXISTE', 'descripcion': 'Usuario no existe'}]}, tablas=TR)
caso(42, R, 'requisitos-con-advertencia-inserta', 'only an ADVERTENCIA from the requirements: not counted as error, the row is inserted and the message is dropped', L(RF, 'getNumeroErrores\(salidaBasicos', 'return out;'),
     e(p_RelacionLaboral=[rl(501, 801)]), '',
     colaboradores={'requisitosBasicos': [{'id': 1, 'tipo': 'ADVERTENCIA', 'codigo': 'AVISO', 'descripcion': 'Aviso'}]}, tablas=TR)
caso(43, R, 'lista-vacia-no-inserta', 'empty list: the batch executes nothing', L(RF, 'sql.batch'), e(p_RelacionLaboral=[]), '', tablas=TR)
caso(44, R, 'dos-relaciones', 'two relations in one batch', L(RF, 'sql.batch'), e(p_RelacionLaboral=[rl(501, 801), rl(502, 801, cargo='GERENT')]), '',
     tablas=TR)
caso(45, R, 'valores-nulos-se-insertan-nulos', 'null income and dates are inserted as NULL', L(RF, 'sql.batch'),
     e(p_RelacionLaboral=[{'IdPersona': 501, 'IdEmpleador': 801, 'CodigoTipoCargoEmpleado': 'JEFCUS', 'IdEstado': 'VIG'}]), '', tablas=TR)
caso(46, R, 'lista-nula-lanza-SQLException', 'JSON null list: SQL.batch rejects null parameters ("Null parameters...")', L(RF, 'sql.batch'),
     e(p_RelacionLaboral=None), '', tablas=TR)

# ===================================================================================================== ConsultarPreferenciaPagoPortafolio
P = 'ConsultarPreferenciaPagoPortafolio'
PF = 'ConsultarPreferenciaPagoPortafolio.java'
V_PREF = 'BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio'


def pref(i, port, medio='CHE', op='VENACN', estado='ACTIVO'):
    return ins(V_PREF, IdPreferenciaPagoPortafolio=i, IdPortafolio=port, IdMedioDePago=medio, IdTipoOperacion=op, Estado=estado)


UNI = [persona(501, '11111111-1', idc=701), persona(502, '22222222-2', idc=702), portafolio(901, 701), portafolio(902, 701), portafolio(903, 701, estado='INA'), portafolio(904, 702)]
caso(50, P, 'devuelve-las-preferencias-del-cliente', 'two active CHE/VENACN preferences of the client are returned', L(PF, 'preferenciasPagoPortafolio = sql.query', 'salida.P_OUT_ERRORESCONTADOR = 0L'),
     e(p_IdCliente=701), seed(UNI, pref(11, 901), pref(12, 902)), sin_orden=['P_OUT_PREFERENCIA_PAGO_PORTAFOLIO'])
caso(51, P, 'cliente-sin-preferencias', 'no preferences: empty list and the OK message', L(PF, 'preferenciasPagoPortafolio = sql.query', 'salida.P_OUT_ERRORESCONTADOR = 0L'), e(p_IdCliente=701), seed(UNI))
caso(52, P, 'filtra-medio-operacion-estado-y-portafolio-inactivo', 'preferences with other medio, other operation, INACTIVO state or an INA portfolio are excluded',
     L(PF, 'preferenciasPagoPortafolio = sql.query', 'salida.P_OUT_ERRORESCONTADOR = 0L'), e(p_IdCliente=701),
     seed(UNI, pref(11, 901), pref(12, 902, medio='TRA'), pref(13, 902, op='COMACN'), pref(14, 901, estado='INACTIVO'), pref(15, 903)))
caso(53, P, 'cliente-ausente-no-devuelve-nada', 'client not informed binds NULL: no rows', L(PF, 'preferenciasPagoPortafolio = sql.query', 'salida.P_OUT_ERRORESCONTADOR = 0L'), e(), seed(UNI, pref(11, 901)))
caso(54, P, 'solo-las-del-cliente-pedido', 'preferences of another client are not returned', L(PF, 'preferenciasPagoPortafolio = sql.query', 'salida.P_OUT_ERRORESCONTADOR = 0L'), e(p_IdCliente=702),
     seed(UNI, pref(11, 901), pref(12, 904)))
