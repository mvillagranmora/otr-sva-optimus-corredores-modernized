"""Cases of BusquedaContratoSAAPVRecaudacion (ids 60-89), AsignarContratoARecaudacion (90-119) and ValidarYAsignarContratosARecaudaciones (120-149)."""
from lib import *  # noqa: F401,F403

BASE = {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_IdModulo': 'APV'}
T_REC = 'BSCL.APV.BD::REC.Recaudacion'
T_RECCLI = 'BSCL.APV.BD::REC.Cliente'
T_RECEMP = 'BSCL.APV.BD::REC.Empleador'
V_RECDIS = 'BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido'


def e(**kw):
    d = dict(BASE)
    d.update(kw)
    return d


def rec(idr=10, idrc=11, idcli=701, regimen='APV', idre=None, estado='REZ'):
    """REC.Recaudacion + REC.Cliente (+ REC.Empleador when idre)."""
    out = [ins(T_REC, IdRecaudacion=idr), ins(T_RECCLI, IdRecaudacionCliente=idrc, IdRecaudacion=idr, IdRecaudacionEmpleador=idre, IdCliente=idcli, IdEstado=estado,
                                              IdRegimenTriburario=regimen)]
    if idre:
        out.append(ins(T_RECEMP, IdRecaudacionEmpleador=idre))
    return out


def vigente(idc=9001, idcli=701, medio=1, idpo=901, origen='APV', estado='VIG', folio=None, idpc=None):
    return [contrato(idc, folio or 'F-B%d' % idc, IdEstado=estado, IdCliente=idcli, IdMedioRecaudacionFormaPago=medio), porcon(idpc or 8000 + idc, idpo, idc, origen=origen)]


PORT = [persona(501, '11111111-1', idc=701), persona(502, '22222222-2', idc=702), portafolio(901, 701, matriz=1), portafolio(902, 701, matriz=2), portafolio(903, 702, matriz=1)]

# =============================================================================================================== BusquedaContratoSAAPVRecaudacion
B = 'BusquedaContratoSAAPVRecaudacion'
BF = 'BusquedaContratoSAAPVRecaudacion.java'
LB = L(BF, r'List<Recaudacion> listadoRecaudaciones 	= obtenerRecaudaciones', r'return out;')
caso(60, B, 'sin-recaudaciones-es-error', 'empty id list: UI_NO_HAY_RECAUDACIONES (no translation: the dictionary receives null values)', L(BF, r'UI_NO_HAY_RECAUDACIONES'),
     e(p_IdsRecaudacion=[]), seed(PORT))
caso(61, B, 'apv-directo-asigna-el-contrato-unico', 'an APV recaudacion of a client without employer (DIREMP) and one APV contract with medio DIREMP: the contract is assigned', LB,
     e(p_IdsRecaudacion=[10]), seed(PORT, rec(), vigente(9001)), sin_orden=['P_OUT_RESULTADO'])
caso(62, B, 'dc-asigna-por-tipo-de-contrato', 'a DC recaudacion matches the contract by origin of the saving (DC)', L(BF, r'recaudacion.IdRegimenTributario.equalsIgnoreCase\("APV"\)'),
     e(p_IdsRecaudacion=[10]), seed(PORT, rec(regimen='DC'), vigente(9001, origen='DC')), sin_orden=['P_OUT_RESULTADO'])
caso(63, B, 'otro-regimen-asigna-por-codigo-del-regimen', 'a recaudacion with regimen APV-A (neither APV nor DC) matches by the regimen code of the portfolio matrix', L(BF, r'item.IdRegimenTributario.equalsIgnoreCase\(recaudacion.IdRegimenTributario\)'),
     e(p_IdsRecaudacion=[10]), seed(PORT, rec(regimen='APV-A'), vigente(9001)), sin_orden=['P_OUT_RESULTADO'])
caso(64, B, 'dos-contratos-candidatos-no-asigna', 'two contracts match: none is assigned (IdContratoSAAPV 0)', L(BF, r'contratosCliente.size\(\) > 1 \|\| contratosCliente.size\(\) == 0'),
     e(p_IdsRecaudacion=[10]), seed(PORT, rec(), vigente(9001), vigente(9002, idpo=902)), sin_orden=['P_OUT_RESULTADO'])
caso(65, B, 'ningun-contrato-candidato-no-asigna', 'no contract of the client: IdContratoSAAPV 0', L(BF, r'new ContratoSAAPV\(\) : contratosCliente.get\(0\)'),
     e(p_IdsRecaudacion=[10]), seed(PORT, rec()), sin_orden=['P_OUT_RESULTADO'])
caso(66, B, 'recaudacion-por-empleador-solo-con-medio-ATREMP', 'a recaudacion with an employer receipt (ATREMP) matches only a contract whose medio is ATREMP (id 2)', L(BF, r'item.CodigoMedioRecaudacion.equalsIgnoreCase\(recaudacion.DetalleRecaudacion\)'),
     e(p_IdsRecaudacion=[10]), seed(PORT, rec(idre=55), vigente(9001, medio=1), vigente(9002, medio=2, idpo=902)), sin_orden=['P_OUT_RESULTADO'])
caso(67, B, 'contrato-con-medio-PAC-se-excluye', 'a contract whose medio is id 3 (PAC) is excluded by the query', L(BF, r'obtenerContratos\(con, idsClientes\)'),
     e(p_IdsRecaudacion=[10]), seed(PORT, rec(), vigente(9001, medio=3)), sin_orden=['P_OUT_RESULTADO'])
caso(68, B, 'contrato-no-vigente-se-excluye', 'a contract that is not VIGENTE (state REV) is excluded', L(BF, r'obtenerContratos\(con, idsClientes\)'),
     e(p_IdsRecaudacion=[10]), seed(PORT, rec(), vigente(9001, estado='REV')), sin_orden=['P_OUT_RESULTADO'])
caso(69, B, 'portafolio-inactivo-o-de-otro-proposito-se-excluye', 'a contract whose portfolio is INA or has another purpose is excluded', L(BF, r'obtenerContratos\(con, idsClientes\)'),
     e(p_IdsRecaudacion=[10]), seed(PORT[:2], portafolio(902, 701, matriz=2, estado='INA'), portafolio(903, 701, matriz=1, proposito='OTRO'), rec(), vigente(9001, idpo=902), vigente(9002, idpo=903)),
     sin_orden=['P_OUT_RESULTADO'])
caso(70, B, 'dos-recaudaciones-de-dos-clientes', 'two recaudaciones of two clients: each gets its own contract', LB,
     e(p_IdsRecaudacion=[10, 20]), seed(PORT, rec(), rec(idr=20, idrc=21, idcli=702), vigente(9001), vigente(9002, idcli=702, idpo=903)), sin_orden=['P_OUT_RESULTADO'])
caso(71, B, 'recaudacion-sin-cliente-lanza-NPE', 'a recaudacion without client rows: IdRegimenTributario is null and equalsIgnoreCase throws NullPointerException', L(BF, r'recaudacion.IdRegimenTributario.equalsIgnoreCase\("APV"\)'),
     e(p_IdsRecaudacion=[10]), seed(PORT, ins(T_REC, IdRecaudacion=10), vigente(9001)))
caso(72, B, 'ids-nulos-lanza-NPE', 'p_IdsRecaudacion not informed: in.p_IdsRecaudacion.size() throws NullPointerException', L(BF, r'in.p_IdsRecaudacion.size\(\) == 0'), e(), seed(PORT))
caso(73, B, 'recaudacion-inexistente-devuelve-lista-vacia', 'a recaudacion id that does not exist: no recaudaciones, no contracts, empty result', L(BF, r'obtenerRecaudaciones\(con, in.p_IdsRecaudacion\)'),
     e(p_IdsRecaudacion=[999]), seed(PORT, rec(), vigente(9001)), sin_orden=['P_OUT_RESULTADO'])

# =============================================================================================================== AsignarContratoARecaudacion
A = 'AsignarContratoARecaudacion'
AF = 'AsignarContratoARecaudacion.java'
T_RECCLI_COLS = [tabla(T_RECCLI, '"IdRecaudacionCliente"', ['IdRecaudacionCliente', 'IdRecaudacion', 'IdCliente', 'IdRegimenTriburario'])]
T_ASIG_D = tabla(T_ASIGN, '"IdRecaudacionCliente"', enmascarar=['FechaCreacion', 'FechaModificacion'])


def pa(idrc=11, idc=9001, idr=10):
    return {'IdRecaudacionCliente': idrc, 'IdContratoSAAPV': idc, 'IdRecaudacion': idr}


def asig_old(idrc=11, idc=9001, hace_un_ano=True):
    f = "'2020-01-15 10:00:00'" if hace_un_ano else 'CURRENT_TIMESTAMP'
    return 'INSERT INTO "%s" ("IdRecaudacionCliente", "IdContratoSAAPV", "FechaCreacion", "FechaModificacion") VALUES (%d, %d, %s, %s);' % (T_ASIGN, idrc, idc, f, f)


UNI = [PORT, rec(), vigente(9001)]
LA = L(AF, r'for \(ParametrosAsignacionContratoRecaudacion fila : in.p_Parametros\)', r'out.P_OUT_MENSAJES.addAll\(mensajes\)')
caso(90, A, 'parametros-nulos-lanzan-NPE', 'p_Parametros not informed: the for loop throws NullPointerException', L(AF, r'for \(ParametrosAsignacionContratoRecaudacion fila : in.p_Parametros\)'), e(), seed(UNI),
     tablas=[T_ASIG_D])
caso(91, A, 'lista-vacia-no-escribe-ni-informa', 'empty list: the contracts query uses IN (NULL), nothing is written, no messages', L(AF, r'hashListadoIdsContratos.put'), e(p_Parametros=[]), seed(UNI), tablas=[T_ASIG_D])
caso(92, A, 'asignacion-nueva-inserta-y-ratifica-el-regimen', 'a new assignment: inserted (timestamps are the wall clock, masked), the regimen of the contract is written to REC.Cliente and UI_ASIGNACION_EXITOSA is reported with the IdRecaudacionCliente',
     LA, e(p_Parametros=[pa()]), seed(UNI), tablas=[T_ASIG_D] + T_RECCLI_COLS, sin_orden=['P_OUT_MENSAJES'])
caso(93, A, 'asignacion-existente-se-actualiza', 'an existing assignment of that recaudacion cliente: it is updated (new contract, new FechaModificacion)', L(AF, r'Update_Asignacion.add\(Param_Asignacion\)'),
     e(p_Parametros=[pa(idc=9002)]), seed(UNI, vigente(9002, idpo=902), asig_old(11, 9001)), tablas=[T_ASIG_D] + T_RECCLI_COLS, sin_orden=['P_OUT_MENSAJES'])
caso(94, A, 'si-hay-actualizaciones-no-se-insertan-las-nuevas', 'one existing and one new assignment: only the UPDATE batch runs (the INSERT is in an else-if): the new assignment is lost (legacy defect)', L(AF, r'if\(Update_Asignacion.size\(\)>0\) \{', r'else if\(Insert_Asignacion.size\(\)>0\)'),
     e(p_Parametros=[pa(11, 9001, 10), pa(21, 9002, 20)]), seed(UNI, rec(idr=20, idrc=21, idcli=701), vigente(9002, idpo=902), asig_old(11, 9001)), tablas=[T_ASIG_D] + T_RECCLI_COLS,
     sin_orden=['P_OUT_MENSAJES'])
caso(95, A, 'simulacion-contrato-ya-asignado-este-mes', 'simulation S, the same contract was assigned this month (REAL clock): nothing is written and the message is UI_CONTRATO_YA_ASIGNADO', L(AF, r'tipoMensajesSalida = "ContratoAsignado"'),
     e(p_Simulacion='S', p_Parametros=[pa()]), seed(UNI, asig_old(11, 9001, hace_un_ano=False)), reloj_real=True, tablas=[T_ASIG_D] + T_RECCLI_COLS, sin_orden=['P_OUT_MENSAJES'])
caso(96, A, 'simulacion-asignacion-de-otro-mes-se-procede', 'simulation S with an assignment from another month: the update goes ahead', L(AF, r'MesActual == mesSystema'),
     e(p_Simulacion='S', p_Parametros=[pa()]), seed(UNI, asig_old(11, 9001, hace_un_ano=True)), reloj_real=True, tablas=[T_ASIG_D] + T_RECCLI_COLS, sin_orden=['P_OUT_MENSAJES'])
caso(97, A, 'simulacion-sin-asignacion-previa-inserta', 'simulation S without a previous assignment: inserted', L(AF, r'!"S".equals\(in.p_Simulacion\) \|\| "".equals\(tipoMensajesSalida\)'),
     e(p_Simulacion='S', p_Parametros=[pa()]), seed(UNI), reloj_real=True, tablas=[T_ASIG_D] + T_RECCLI_COLS, sin_orden=['P_OUT_MENSAJES'])
caso(98, A, 'origen-traspaso-no-ratifica-el-regimen', 'p_Origen TRASPA: the traspaso detail is read, the recaudacion cliente is NOT updated (IdRecaudacionCliente stays 0) and the message id is 0', L(AF, r'"TRASPA".equalsIgnoreCase\(in.p_Origen\)\)\{'),
     e(p_Origen='TRASPA', p_Parametros=[pa()]),
     seed(UNI, ins(T_TRASPASO, IdTraspaso=5001, IdContratoSAAPV=9001, IdEmpresa=1, TipoMovimiento='INGRESO', IdEstado='PDI'), ins(T_DETTRASPASO, IdTraspaso=5001)),
     tablas=[T_ASIG_D] + T_RECCLI_COLS, sin_orden=['P_OUT_MENSAJES'])
caso(99, A, 'contrato-sin-id-se-trata-como-cero', 'IdContratoSAAPV null becomes 0: the assignment is written with contract 0 and no contract matches the query (no message)', L(AF, r'fila.IdContratoSAAPV = 0L'),
     e(p_Parametros=[{'IdRecaudacionCliente': 11, 'IdRecaudacion': 10}]), seed(UNI), tablas=[T_ASIG_D] + T_RECCLI_COLS)
caso(100, A, 'portafolio-inactivo-no-genera-mensaje', 'the contract portfolio is INA: the contracts query excludes it, so the assignment is written but nothing is reported', L(AF, r'listadoContratosRatificarRegimen = sql.query'),
     e(p_Parametros=[pa()]), seed(PORT[:2], portafolio(901, 701, matriz=1, estado='INA'), rec(), vigente(9001)), tablas=[T_ASIG_D] + T_RECCLI_COLS)
caso(101, A, 'dos-contratos-dos-mensajes', 'two parameters with two contracts of two portfolios: two inserts and two messages', LA,
     e(p_Parametros=[pa(11, 9001, 10), pa(21, 9002, 20)]), seed(UNI, rec(idr=20, idrc=21, idcli=701), vigente(9002, idpo=902)), tablas=[T_ASIG_D] + T_RECCLI_COLS, sin_orden=['P_OUT_MENSAJES'])

# =============================================================================================================== ValidarYAsignarContratosARecaudaciones
Y = 'ValidarYAsignarContratosARecaudaciones'
YF = 'ValidarYAsignarContratosARecaudaciones.java'


def rez(idrc=11, folio=10, rutc='11111111-1', monto=100000, tipo='APV', regimen='APV-A', rute='', fecha='2025-03-15', estado='REZ', idcli=701):
    return ins(V_RECDIS, FolioRecaudacion=folio, IdRecaudacionCliente=idrc, IdRecaudacionEmpleador=0, RutEmpleador=rute, RecMonto=monto, TipoRegimen=tipo, IdCliente=idcli,
               Regimen=regimen, RutCliente=rutc, CodigoEstado=estado, FechaDeposito=fecha, MontoRecaudacion=monto)


def fil(**kw):
    d = {'p_FechaDesde': '2025-03-01', 'p_FechaHasta': '2025-03-31', 'p_RutCliente': '', 'p_RutEmpleador': '', 'p_FolioRecaudacion': 0}
    d.update(kw)
    return d


def ey(filtros=None, **kw):
    d = dict(BASE)
    d['p_FiltrosRecaudacion'] = filtros if filtros is not None else fil()
    d.update(kw)
    return d


YT = [T_ASIG_D]
LY = L(YF, r'private SalidaValidarYAsignarContratosARecaudaciones logica', r'return salida;')
caso(120, Y, 'sin-rezagos-no-hay-nada-que-asignar', 'no recaudacion in rezago (REZ) in the date range: empty answer (the temp table is still created, filled with nothing and dropped)', LY,
     ey(), seed(UNI, rez(fecha='2025-05-01')), tablas=YT)
caso(121, Y, 'un-rezago-con-contrato-se-asigna', 'one rezago whose recaudacion has a single candidate contract: the assignment is inserted and UI_ASIGNAR_CONTRATO_A_RECAUDACION is reported (the folio of the contract is the text "null": FolioContratoSAAPV is never filled by the search)',
     L(YF, r'UI_ASIGNAR_CONTRATO_A_RECAUDACION'), ey(), seed(UNI, rez()), tablas=YT)
caso(122, Y, 'un-rezago-sin-contrato-es-advertencia', 'one rezago whose recaudacion has no candidate contract: UI_ERROR_ASIGNAR_CONTRATO_RECAUDACION warning with counter 1', L(YF, r'UI_ERROR_ASIGNAR_CONTRATO_RECAUDACION'),
     ey(), seed(PORT, rec(), rez()), tablas=YT)
caso(123, Y, 'rezago-sin-datos-de-recaudacion-lanza-NPE', 'a rezago view row with no matching REC.Recaudacion/Cliente rows: the search returns no entry, IdContratoSAAPV stays null and the unboxing throws NullPointerException', L(YF, r'recaudacion.IdContratoSAAPV > 0'),
     ey(), seed(UNI[:1], rez(), vigente(9001)), tablas=YT)
caso(124, Y, 'filtra-por-folio-de-recaudacion', 'p_FolioRecaudacion > 0 keeps only that folio', L(YF, r'filtros.p_FolioRecaudacion > 0'),
     ey(fil(p_FolioRecaudacion=10)), seed(UNI, rez(), rez(idrc=21, folio=20)), tablas=YT)
caso(125, Y, 'filtra-por-rut-del-cliente', 'p_RutCliente keeps only the recaudaciones of that RUT', L(YF, r'rec.RutCliente.equals\(filtros.p_RutCliente\)'),
     ey(fil(p_RutCliente='22222222-2')), seed(PORT, rec(), rec(idr=20, idrc=21, idcli=702), vigente(9001), vigente(9002, idcli=702, idpo=903), rez(), rez(idrc=21, folio=20, rutc='22222222-2', idcli=702)), tablas=YT)
caso(126, Y, 'filtra-por-rut-del-empleador', 'p_RutEmpleador keeps only the recaudaciones of that employer RUT', L(YF, r'rec.RutEmpleador.equals\(filtros.p_RutEmpleador\)'),
     ey(fil(p_RutEmpleador='76000000-0')), seed(UNI, rez(rute='76000000-0'), rez(idrc=21, folio=20, rute='')), tablas=YT)
caso(127, Y, 'rezago-ya-asignado-se-actualiza', 'the recaudacion cliente already has an assignment: the UPDATE branch of asignarContratosARecaudacion is used', L(YF, r'updatesAsignacion.add\(Param_Asignacion\)'),
     ey(), seed(UNI, rez(), asig_old(11, 9999)), tablas=YT)
caso(128, Y, 'dos-rezagos-fallan-al-borrar-la-tabla-temporal-dos-veces', 'two rezagos: the temp table is dropped once per row (batch of the DROP) so the second DROP fails with an SQLException (HANA rejects it too: legacy defect)', L(YF, r'sql.batch\(conn, SQL_DROP_TABLA_TEMPORAL'),
     ey(), seed(UNI, rec(idr=20, idrc=21, idcli=701), vigente(9002, idpo=902), rez(), rez(idrc=21, folio=20)), tablas=YT)
