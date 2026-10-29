"""Family C cases: CambiarEstadoRecaudacion (420-429), CrearAnotacionRecaudacion (430-439), ModificarAnotacionRecaudacion (440-449),
EliminarRegistrosRecaudacion (450-459), AsignarDepositoEmpleador (460-469), ModificarBonificacion (350-359)."""
from fix import T_ANOT, T_INVBON, T_REC, T_RCLI, T_REMP, rcli, rec, remp
from lib import L, caso, ins, seed, tabla

TREC = tabla(T_REC, '"IdRecaudacion"', columnas=['IdRecaudacion', 'IdEmpresa', 'IdEstado'])
TEMP = tabla(T_REMP, '"IdRecaudacionEmpleador"', columnas=['IdRecaudacionEmpleador', 'IdRecaudacion', 'IdEmpresa', 'IdEstado'])
TCLI = tabla(T_RCLI, '"IdRecaudacionCliente"', columnas=['IdRecaudacionCliente', 'IdRecaudacion', 'IdRecaudacionEmpleador', 'IdEmpresa', 'IdEstado'])

# ------------------------------------------------------------------------------------------------ CambiarEstadoRecaudacion
F = 'CambiarEstadoRecaudacion.java'


def ce(*filas):
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_Parametros': [
        {'FolioRecaudacion': f, 'IdRecaudacionEmpleador': e, 'IdRecaudacionCliente': c, 'IdEstado': st} for (f, e, c, st) in filas]}


BASE_CE = seed(rec(10, 'ING'), rec(11, 'ING'), rec(12, 'ING', idempresa=2), remp(20, 10), remp(21, 11), rcli(30, 10, 20), rcli(31, 11, 21))

caso(420, 'CambiarEstadoRecaudacion', 'solo-deposito', 'FolioRecaudacion > 0 only: the deposit state changes, the details do not',
     L(F, 'if\\(fila.FolioRecaudacion > 0\\)'), ce((10, 0, 0, 'CON')), BASE_CE, tablas=[TREC, TEMP, TCLI])
caso(421, 'CambiarEstadoRecaudacion', 'solo-detalle-empleador', 'IdRecaudacionEmpleador > 0 only', L(F, 'if\\(fila.IdRecaudacionEmpleador > 0\\)'),
     ce((0, 20, 0, 'CON')), BASE_CE, tablas=[TREC, TEMP, TCLI])
caso(422, 'CambiarEstadoRecaudacion', 'solo-detalle-cliente', 'IdRecaudacionCliente > 0 only', L(F, 'if\\(fila.IdRecaudacionCliente > 0\\)'),
     ce((0, 0, 30, 'REZ')), BASE_CE, tablas=[TREC, TEMP, TCLI])
caso(423, 'CambiarEstadoRecaudacion', 'los-tres-a-la-vez', 'deposit, employer detail and client detail in one row', L(F, 'for \\(ParametrosRecaudacionEstado fila'),
     ce((10, 20, 30, 'DIS')), BASE_CE, tablas=[TREC, TEMP, TCLI])
caso(424, 'CambiarEstadoRecaudacion', 'ceros-no-actualizan-y-responde-ok', 'all ids 0: nothing updated, OK message anyway', L(F, 'msg.codigo = "OK"'),
     ce((0, 0, 0, 'CON')), BASE_CE, tablas=[TREC, TEMP, TCLI])
caso(425, 'CambiarEstadoRecaudacion', 'lista-vacia-responde-ok', 'empty list: OK message', L(F, 'out.P_OUT_MENSAJES.add\\(msg\\)'),
     {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_Parametros': []}, BASE_CE, tablas=[TREC])
caso(426, 'CambiarEstadoRecaudacion', 'folio-nulo-lanza-NullPointerException', 'FolioRecaudacion missing: unboxing null at the first comparison',
     L(F, 'if\\(fila.FolioRecaudacion > 0\\)'),
     {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_Parametros': [{'IdRecaudacionEmpleador': 0, 'IdRecaudacionCliente': 0, 'IdEstado': 'CON'}]},
     BASE_CE, tablas=[TREC])
caso(427, 'CambiarEstadoRecaudacion', 'lista-nula-lanza-NullPointerException', 'p_Parametros null: NullPointerException', L(F, 'for \\(ParametrosRecaudacionEstado fila'),
     {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_Parametros': None}, BASE_CE)
caso(428, 'CambiarEstadoRecaudacion', 'dos-filas', 'two rows are applied in order', L(F, 'for \\(ParametrosRecaudacionEstado fila'),
     ce((10, 20, 30, 'CON'), (11, 21, 31, 'REZ')), BASE_CE, tablas=[TREC, TEMP, TCLI])
caso(429, 'CambiarEstadoRecaudacion', 'otra-empresa-no-se-actualiza', 'the deposit of another empresa is not touched (IdEmpresa in the WHERE)', L(F, 'Rec_CE.IdEmpresa'),
     ce((12, 0, 0, 'CON')), BASE_CE, tablas=[TREC])

# ------------------------------------------------------------------------------------------------ CrearAnotacionRecaudacion
F = 'CrearAnotacionRecaudacion.java'
SEQA = 'BSCL.APV.BD.Secuencias::REC.IdAnotacion'
TANOT = tabla(T_ANOT, '"IdAnotacion"', enmascarar=['FechaAnotacion', 'FechaIngreso'])


def an(**kw):
    e = {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_IdRecaudacion': 10, 'p_Titulo': 'TITULO', 'p_Detalle': 'DETALLE', 'p_IdMotivo': 'MOT1',
         'p_IdAmbito': 'AMB1', 'p_IdRelevancia': 'ALTA', 'p_FechaDesde': '2025-03-01', 'p_FechaHasta': '2025-03-31'}
    e.update(kw)
    return e


caso(430, 'CrearAnotacionRecaudacion', 'inserta-y-devuelve-id-de-secuencia', 'one annotation: id from the sequence, OK message, P_OUT_REFID = the id',
     L(F, 'sql.batch\\(con,SQL_INSERT_ANOTACION'), an(), seed(rec(10)), secuencias={SEQA: 3000}, tablas=[TANOT])
caso(431, 'CrearAnotacionRecaudacion', 'motivo-se-llena-con-el-ambito', 'defect: IdMotivoAnotacion takes p_IdAmbito, p_IdMotivo is ignored',
     L(F, 'An_insert.IdMotivo'), an(p_IdMotivo='SOLO-MOTIVO', p_IdAmbito='AMBITO-X'), seed(rec(10)), secuencias={SEQA: 3010}, tablas=[TANOT])
caso(432, 'CrearAnotacionRecaudacion', 'entrada-vacia-inserta-fila-con-nulos', 'no field informed: a row with the sequence id, today and NULLs is inserted',
     L(F, 'An_insert.IdAnotacion'), {}, seed(rec(10)), secuencias={SEQA: 3020}, tablas=[TANOT])
caso(433, 'CrearAnotacionRecaudacion', 'dos-llamadas-consecutivas-usan-ids-distintos', 'the sequence value is requested once per call (3030 then the next call would be 3031): one call pins one value',
     L(F, 'getSecuencia'), an(p_IdRecaudacion=11, p_Titulo='OTRO'), seed(rec(10), rec(11)), secuencias={SEQA: 3030}, tablas=[TANOT])

# ------------------------------------------------------------------------------------------------ ModificarAnotacionRecaudacion
F = 'ModificarAnotacionRecaudacion.java'
TANOT2 = tabla(T_ANOT, '"IdAnotacion"', enmascarar=['FechaAnotacion', 'FechaIngreso'])
ANOT_BASE = ins(T_ANOT, IdAnotacion=60, IdRecaudacion=10, FechaAnotacion='2025-03-02', IdMotivoAnotacion='M0', Titulo='T0', Detalle='D0', FechaIngreso='2025-03-02',
                FechaCreacion='2025-03-02 08:00:00', FechaModificacion='2025-03-02 08:00:00', IdUsuarioCreador=9, IdRelevanciaAnotacion='BAJA', IdAmbitoAnotacion='A0',
                ValidoDesde='2025-03-01', ValidoHasta='2025-03-31')


def anm(**kw):
    e = {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_IdAnotacion': 60, 'p_Titulo': 'NUEVO', 'p_Detalle': 'NUEVO DETALLE', 'p_IdMotivo': 'MOT1',
         'p_IdAmbito': 'AMB1', 'p_IdRelevancia': 'ALTA', 'p_FechaDesde': '2025-04-01', 'p_FechaHasta': '2025-04-30'}
    e.update(kw)
    return e


caso(440, 'ModificarAnotacionRecaudacion', 'actualiza-y-refid-es-id-de-secuencia', 'defect: P_OUT_REFID is a NEW sequence value, not the annotation id; FechaModificacion is the database clock',
     L(F, 'out.P_OUT_REFID = idsRecaudacion.get\\(0\\)'), anm(), seed(ANOT_BASE), secuencias={'BSCL.APV.BD.Secuencias::REC.IdAnotacion': 3100}, tablas=[TANOT2])
caso(441, 'ModificarAnotacionRecaudacion', 'anotacion-inexistente-responde-ok', 'unknown annotation: nothing updated, OK message anyway',
     L(F, 'msg.codigo = "OK"'), anm(p_IdAnotacion=999), seed(ANOT_BASE), secuencias={'BSCL.APV.BD.Secuencias::REC.IdAnotacion': 3110}, tablas=[TANOT2])
caso(442, 'ModificarAnotacionRecaudacion', 'motivo-toma-el-ambito', 'defect: IdMotivoAnotacion takes p_IdAmbito', L(F, 'An_insert.IdMotivo'),
     anm(p_IdMotivo='SOLO-MOTIVO', p_IdAmbito='AMBITO-X'), seed(ANOT_BASE), secuencias={'BSCL.APV.BD.Secuencias::REC.IdAnotacion': 3120}, tablas=[TANOT2])
caso(443, 'ModificarAnotacionRecaudacion', 'campos-nulos-pisan-los-existentes', 'fields not sent become NULL (no COALESCE in the UPDATE); the key is p_IdAnotacion',
     L(F, 'sql.batch\\(con,SQL_UPDATE_ANOTACION'), {'p_IdEmpresa': 1, 'p_IdAnotacion': 60}, seed(ANOT_BASE), secuencias={'BSCL.APV.BD.Secuencias::REC.IdAnotacion': 3130},
     tablas=[TANOT2])

# ------------------------------------------------------------------------------------------------ EliminarRegistrosRecaudacion
F = 'EliminarRegistrosRecaudacion.java'
BASE_EL = seed(rec(10, 'CON'), remp(20, 10, 'CON'), remp(21, 10, 'CON'), rcli(30, 10, 20, 'CON'), rcli(31, 10, 21, 'CON'), rcli(32, 10, 21, 'CON'),
               rcli(33, 10, 21, 'CON', idempresa=2))


def el(tipo, emp=0, cli=0, rec_=10):
    return {'IdEmpresa': 1, 'IdRecaudacion': rec_, 'IdRecaudacionEmpleador': emp, 'IdRecaudacionCliente': cli, 'TipoPersonaEliminar': tipo}


caso(450, 'EliminarRegistrosRecaudacion', 'empleador-con-un-cliente-dos-mensajes', 'type E: the employer detail and its single client detail are deleted: two OK messages, deposit back to ING',
     L(F, 'if\\(in_s.TipoPersonaEliminar.equals\\("E"\\)\\)'), el('E', emp=20), BASE_EL, tablas=[TREC, TEMP, TCLI])
caso(451, 'EliminarRegistrosRecaudacion', 'empleador-con-dos-clientes-sin-mensaje-de-clientes', 'type E with two client details: the batch count is 2, so the client message is NOT added (res[0]==1)',
     L(F, 'UI_DETALLE_CLIENTE_ASOCIADO_ELIMINADO'), el('E', emp=21), BASE_EL, tablas=[TREC, TEMP, TCLI])
caso(452, 'EliminarRegistrosRecaudacion', 'cliente-un-registro', 'type C: one client detail deleted, message, the employer detail goes back to ING and the deposit too',
     L(F, 'UI_DETALLE_CLIENTE_ELIMINADO'), el('C', emp=20, cli=30), BASE_EL, tablas=[TREC, TEMP, TCLI])
caso(453, 'EliminarRegistrosRecaudacion', 'cliente-sin-coincidencia', 'type C with no matching client: no message; the deposit goes back to ING but the employer detail is NOT reset (no client was deleted)',
     L(F, 'resCliente = sql.batch\\(con,SQL_ELIMINA_REGISTRO_CLIENTE,List_in,ParametrosEntradaServicio.class\\);\\s*$'), el('C', emp=20, cli=999), BASE_EL,
     tablas=[TREC, TEMP, TCLI])
caso(454, 'EliminarRegistrosRecaudacion', 'cliente-cero-borra-todos-los-del-empleador', 'type C with IdRecaudacionCliente 0: every client of that employer is deleted (2 rows: no message, the employer detail is NOT reset)',
     L(F, 'IdRecaudacionEmpleador = in.IdRecaudacionEmpleador'), el('C', emp=21, cli=0), BASE_EL, tablas=[TREC, TEMP, TCLI])
caso(455, 'EliminarRegistrosRecaudacion', 'tipo-nulo-lanza-NullPointerException', 'TipoPersonaEliminar missing: NullPointerException', L(F, 'in_s.TipoPersonaEliminar.equals'),
     {'IdEmpresa': 1, 'IdRecaudacion': 10}, BASE_EL, tablas=[TREC, TCLI])
caso(456, 'EliminarRegistrosRecaudacion', 'tipo-distinto-de-E-se-trata-como-cliente', 'any type other than E (here X) deletes the client detail', L(F, '}else\\{\\s*$'),
     el('X', emp=20, cli=30), BASE_EL, tablas=[TREC, TEMP, TCLI])
caso(457, 'EliminarRegistrosRecaudacion', 'recaudacion-nula-lanza-NullPointerException', 'IdRecaudacion missing: the deletes match nothing and CambiarEstadoRecaudacion unboxes null (rolled back)',
     L(F, 'RecaudacionEstado.FolioRecaudacion = in.IdRecaudacion'), {'IdEmpresa': 1, 'IdRecaudacionEmpleador': 0, 'IdRecaudacionCliente': 30, 'TipoPersonaEliminar': 'C'},
     BASE_EL, tablas=[TREC, TCLI])
caso(458, 'EliminarRegistrosRecaudacion', 'otra-empresa-no-se-elimina', 'the client detail of another empresa is not deleted', L(F, 'SQL_ELIMINA_REGISTRO_CLIENTE'),
     {'IdEmpresa': 1, 'IdRecaudacion': 10, 'IdRecaudacionEmpleador': 21, 'IdRecaudacionCliente': 33, 'TipoPersonaEliminar': 'C'}, BASE_EL, tablas=[TREC, TCLI])

# ------------------------------------------------------------------------------------------------ AsignarDepositoEmpleador
F = 'AsignarDepositoEmpleador.java'


def ad(*filas):
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_Parametros': [
        {'IdRecaudacion': r, 'MontoDeposito': md, 'IdRecaudacionEmpleador': e, 'MontoEmpleador': me} for (r, md, e, me) in filas]}


BASE_AD = seed(rec(10, 'CON', monto=5000), remp(20, 10, 'ING', convenido=0, apv=1000), remp(21, 0, 'ING', convenido=0, apv=1500))
TEMPD = tabla(T_REMP, '"IdRecaudacionEmpleador"', columnas=['IdRecaudacionEmpleador', 'IdRecaudacion', 'MontoDepositoAPV', 'MontoDepositoConvenido'])

caso(460, 'AsignarDepositoEmpleador', 'asignacion-exitosa', 'deposit 5000 with 1000 assigned and an employer of 1500: assigned, message id 1', L(F, 'msg.descripcion = "Asignaci'),
     ad((10, 5000, 21, 1500)), BASE_AD, tablas=[TEMPD])
caso(461, 'AsignarDepositoEmpleador', 'excede-el-saldo-del-deposito', 'assigned + employer > deposit: message id 3, nothing updated', L(F, 'msg.id = 3L'),
     ad((10, 5000, 21, 4500)), BASE_AD, tablas=[TEMPD])
caso(462, 'AsignarDepositoEmpleador', 'deposito-ya-completo', 'deposit already fully assigned (deposit - assigned <= 0): message id 4', L(F, 'msg.id = 4L'),
     ad((10, 1000, 21, 1)), BASE_AD, tablas=[TEMPD])
caso(463, 'AsignarDepositoEmpleador', 'empleador-inexistente-problemas', 'the UPDATE matches no employer detail: message id 2 (Problemas al Conciliar)', L(F, 'msg.id = 2L'),
     ad((10, 5000, 999, 100)), BASE_AD, tablas=[TEMPD])
caso(464, 'AsignarDepositoEmpleador', 'dos-filas-comparten-una-instancia-de-mensaje', 'defect: one MensajeSalida instance is reused: an OK followed by an error leaves BOTH list entries as the error',
     L(F, 'MensajeSalida               msg             = new MensajeSalida'), ad((10, 5000, 21, 1500), (10, 5000, 21, 9000)), BASE_AD, tablas=[TEMPD])
caso(465, 'AsignarDepositoEmpleador', 'lista-vacia', 'empty list: no messages, counter 0', L(F, 'for\\(AsignarDepositoEmpleador.DepositoEmpleador fila'),
     {'p_IdEmpresa': 1, 'p_Parametros': []}, BASE_AD, tablas=[TEMPD])
caso(466, 'AsignarDepositoEmpleador', 'monto-justo-hasta-el-limite', 'assigned + employer == deposit is allowed (<=)', L(F, 'totalDouble \\+ fila.MontoEmpleador <= fila.MontoDeposito'),
     ad((10, 5000, 21, 4000)), BASE_AD, tablas=[TEMPD])
caso(467, 'AsignarDepositoEmpleador', 'deposito-sin-empleadores-total-cero', 'deposit without employers: total 0 (COALESCE), a single aggregate row', L(F, 'totalDepositoEmpleador.get\\(0\\).Total'),
     ad((77, 100, 21, 50)), BASE_AD, tablas=[TEMPD])

# ------------------------------------------------------------------------------------------------ ModificarBonificacion
F = 'ModificarBonificacion.java'
TBON = tabla(T_INVBON, '"IdBonificacion"')
BASE_MB = seed(ins(T_INVBON, IdBonificacion=1, MontoCalculado=100), ins(T_INVBON, IdBonificacion=2, MontoCalculado=200))


def mb(*filas):
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_Parametros': [{'IdBonificacion': i, 'MontoABonificar': m} for (i, m) in filas]}


caso(350, 'ModificarBonificacion', 'actualiza-monto-calculado', 'one bonificacion: MontoCalculado updated, message OK, counter 0', L(F, 'msg.descripcion = "Bonificaci.n Modificada"'),
     mb((1, 1234.5678)), BASE_MB, tablas=[TBON])
caso(351, 'ModificarBonificacion', 'inexistente-error', 'unknown bonificacion: message ERROR (tipo APV, so not counted by getNumeroErrores), counter 1', L(F, 'Error al Modificar'),
     mb((99, 10)), BASE_MB, tablas=[TBON])
caso(352, 'ModificarBonificacion', 'ok-y-error-comparten-mensaje', 'defect: one MensajeSalida instance is reused: OK then error leaves both entries as the error; counter is the last row',
     L(F, 'MensajeSalida	msg	= new MensajeSalida'), mb((1, 10), (99, 20)), BASE_MB, tablas=[TBON])
caso(353, 'ModificarBonificacion', 'lista-vacia-sin-contador', 'empty list: no message and the counter stays null (omitted from the JSON)', L(F, 'for\\(Bonificacion fila : in.p_Parametros\\)'),
     mb(), BASE_MB, tablas=[TBON])
caso(354, 'ModificarBonificacion', 'monto-nulo-pone-nulo', 'MontoABonificar missing: MontoCalculado becomes NULL', L(F, 'Bon.MontoABonificar'),
     {'p_IdEmpresa': 1, 'p_Parametros': [{'IdBonificacion': 2}]}, BASE_MB, tablas=[TBON])
caso(355, 'ModificarBonificacion', 'dos-filas-ok', 'two valid rows: both updated, two identical OK messages', L(F, 'salida.P_OUT_ERRORESCONTADOR = 0L'),
     mb((1, 11), (2, 22)), BASE_MB, tablas=[TBON])
