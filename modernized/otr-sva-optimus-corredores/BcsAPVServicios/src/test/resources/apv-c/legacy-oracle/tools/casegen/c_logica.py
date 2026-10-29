"""Family C cases of the TYPED entry point ValidarMandatosPAC.logica(Connection, SalidaRecaudacionTraspaso, Long IdEmpresa, Long IdUsuario), ids 480-509.

The legacy Tesoreria job BcsJobProcesarDistribucion calls this method IN PROCESS with its own connection (the JavaCall entry point call() returns null and is pinned by
APV-C-411). The method stores the outcome of the PAC distribution in BSCL.Tesoreria.BD::PAC.LogMandatoPAC through ValidarMandatosPAC_INSERT_LogMandatoPAC.sql, which is an
UPDATE (despite the file name): it only changes rows that already exist for (IdMandatoPAC, FechaSistema). Cases use "metodo": "logica"; the oracle runner decodes
entrada.entrada with the legacy Gson into SalidaRecaudacionTraspaso and calls logica(conn, entrada, idEmpresa, idUsuario).
The column FechaProceso is CURRENT_TIMESTAMP (run day): the dump does not print it, it prints whether it was left alone (SIN_CAMBIO) or rewritten (ACTUALIZADA).
"""
from lib import *  # noqa: F401,F403

H = 'ValidarMandatosPAC.java'
T_LOG = 'BSCL.Tesoreria.BD::PAC.LogMandatoPAC'
SIN = '2024-01-01'
FS = '2025-04-01'

TLOG = tabla(T_LOG, '"IdMandatoPAC", "FechaSistema", "Usuario", "Glosa", "EstadoProcesamiento", "PreRendicion"',
             columnas=['IdMandatoPAC', 'FechaSistema', 'Usuario', 'EstadoProcesamiento', 'PreRendicion', 'Glosa'],
             expresiones={'FechaProceso': 'CASE WHEN "FechaProceso" IS NULL THEN \'NULA\' WHEN "FechaProceso" = DATE \'%s\' THEN \'SIN_CAMBIO\' ELSE \'ACTUALIZADA\' END' % SIN})


def fila(idm, fecha=FS, usuario='9', estado='ORI', pre='N', glosa='previo'):
    """A LogMandatoPAC row as the distribution leaves it before the job stores the outcome (what the UPDATE finds)."""
    return ins(T_LOG, IdMandatoPAC=idm, FechaSistema=fecha, FechaProceso=SIN, Usuario=usuario, EstadoProcesamiento=estado, PreRendicion=pre, Glosa=glosa)


def reg(idm, serv=None, estado=None, **kw):
    """RegistroRecaudacion as the legacy Gson reads it: absent keys keep the constructor defaults (IdMandatoPAC 0, texts ''); only these three are used by logica."""
    d = {'IdMandatoPAC': idm}
    if serv is not None:
        d['IdentServicio'] = serv
    if estado is not None:
        d['EstadoRendicion'] = estado
    d.update(kw)
    return d


def ent(a_procesar=None, no_procesadas=None, procesadas=None, emp=1, usu=21, **extra):
    """case entrada: {"entrada": typed SalidaRecaudacionTraspaso fields, "idEmpresa", "idUsuario"}; a list left as None is absent (constructor default: empty list)."""
    e = {}
    if a_procesar is not None:
        e['P_OUT_SOLICITUDES_A_PROCESAR'] = a_procesar
    if no_procesadas is not None:
        e['P_OUT_SOLICITUDES_NO_PROCESADAS'] = no_procesadas
    if procesadas is not None:
        e['P_OUT_SOLICITUDES_PROCESADAS'] = procesadas
    e.update(extra)
    return {'entrada': e, 'idEmpresa': emp, 'idUsuario': usu}


def lg(num, slug, titulo, pina, entrada, *filas, **kw):
    tablas = kw.pop('tablas', [TLOG])
    return caso(num, 'ValidarMandatosPAC', slug, titulo, pina, entrada, seed(*filas), tablas=tablas, metodo='logica', **kw)


LLOGICA = L(H, r'public void logica\(Connection con')
LOK = L(H, r'InsertarMandatosProcesados\(con, sql, solAGuardarOK')
LNOK = L(H, r'InsertarMandatosNoProcesados\(con, sql, solAGuardarNOK')
LHASH = L(H, r'hashNoPro = entrada')
LFILT = L(H, r'solAGuardarOK 	= entrada')
LUSU = L(H, r'np.Usuario				= IdUsuario.toString\(\)')
LFECHA = L(H, r'Date fechaSistema')

lg(480, 'logica-sin-solicitudes-no-escribe', 'logica with no request at all: nothing is written, no exception (the output of the void method is null)', LLOGICA,
   ent(), fila(1), fila(2))
lg(481, 'logica-una-solicitud-a-procesar-queda-PRO', 'one request to process, none rejected: its LogMandatoPAC row becomes PRO / PreRendicion S / Glosa "Procesado." / Usuario = the user id / FechaProceso rewritten', LOK,
   ent([reg(1, 'S1')]), fila(1), fila(2))
lg(482, 'logica-varias-solicitudes-a-procesar-todas-PRO', 'three requests to process: the three rows become PRO, the fourth row of the table is not touched', LOK,
   ent([reg(1, 'S1'), reg(2, 'S2'), reg(3, 'S3')]), fila(1), fila(2), fila(3), fila(4))
lg(483, 'logica-no-procesada-queda-PEN-con-su-estado-de-rendicion', 'one rejected request: the row becomes PEN / PreRendicion S and the Glosa is the EstadoRendicion of the request', LNOK,
   ent(no_procesadas=[reg(2, 'S2', 'RECHAZADO POR BANCO')]), fila(1), fila(2))
lg(484, 'logica-mixta-procesadas-y-no-procesadas', 'requests 1 2 3 to process and 2 4 rejected: 1 and 3 become PRO, 2 and 4 PEN (2 is both: rejected wins)', LFILT,
   ent([reg(1, 'S1'), reg(2, 'S2'), reg(3, 'S3')], [reg(2, 'S2', 'SIN FONDOS'), reg(4, 'S4', 'CUENTA CERRADA')]), fila(1), fila(2), fila(3), fila(4))
lg(485, 'logica-todas-las-a-procesar-estan-rechazadas', 'every request to process is also in the rejected list: no PRO batch at all, only the PEN rows are written', LFILT,
   ent([reg(1), reg(2)], [reg(1, estado='MANDATO VENCIDO'), reg(2, estado='MANDATO VENCIDO')]), fila(1), fila(2))
lg(486, 'logica-lista-procesadas-se-ignora', 'P_OUT_SOLICITUDES_PROCESADAS (filled) is never read: only A_PROCESAR and NO_PROCESADAS drive the writes', LLOGICA,
   ent(procesadas=[reg(1, 'S1'), reg(2, 'S2')]), fila(1), fila(2))
lg(487, 'logica-sin-fila-previa-no-inserta', 'the statement is an UPDATE: a request with no LogMandatoPAC row writes nothing (no row is inserted)', LOK,
   ent([reg(7, 'S7')], [reg(8, 'S8', 'X')]), fila(1))
lg(488, 'logica-fila-de-otra-fecha-no-se-actualiza', 'the UPDATE also filters FechaSistema = the system date: a row of another date is left as it was', LFECHA,
   ent([reg(1)], [reg(2, estado='R')]), fila(1, fecha='2025-03-31'), fila(1), fila(2, fecha='2025-04-02'), fila(2))
lg(489, 'logica-filas-duplicadas-se-actualizan-todas', 'two rows with the same (IdMandatoPAC, FechaSistema): the UPDATE rewrites both', LOK,
   ent([reg(1)]), fila(1, glosa='a'), fila(1, glosa='b'))
lg(490, 'logica-mandato-repetido-en-la-lista', 'a mandate twice in A_PROCESAR and twice in NO_PROCESADAS: each element is a batch row, the last write of the same row wins', LOK,
   ent([reg(1), reg(1), reg(3)], [reg(2, estado='A'), reg(2, estado='B')]), fila(1), fila(2), fila(3))
lg(491, 'logica-estado-de-rendicion-nulo-y-ausente', 'EstadoRendicion absent keeps the constructor default "" (Glosa ""), explicit null gives a NULL Glosa', LNOK,
   ent(no_procesadas=[reg(1), reg(2, estado=None, EstadoRendicion=None)]), fila(1), fila(2))
lg(492, 'logica-glosa-con-texto-largo-y-acentos', 'Glosa is the EstadoRendicion text as is: accents, quotes and a long text', LNOK,
   ent(no_procesadas=[reg(1, 'S1', 'Rendición rechazada: "cuenta" inválida / sin fondos ñandú ' + 'x' * 120)]), fila(1))
lg(493, 'logica-usuario-y-empresa-distintos', 'IdUsuario 987654321 is stored as its text in Usuario for both PRO and PEN rows; IdEmpresa 7 only selects the configuration', LUSU,
   ent([reg(1)], [reg(2, estado='R')], emp=7, usu=987654321), fila(1), fila(2))
lg(494, 'logica-usuario-nulo-con-solicitudes-lanza-NPE', 'IdUsuario null with a request to write: IdUsuario.toString() throws NullPointerException before any statement runs', LUSU,
   ent([reg(1)], [reg(2, estado='R')], usu=None), fila(1), fila(2))
lg(495, 'logica-usuario-nulo-sin-solicitudes-no-falla', 'IdUsuario null with empty lists: IdUsuario is never dereferenced, nothing happens', LUSU,
   ent(usu=None), fila(1))
lg(496, 'logica-empresa-nula-no-falla', 'IdEmpresa null: the configuration lookup does not need it, the PRO row is written', LFECHA,
   ent([reg(1)], emp=None), fila(1))
lg(497, 'logica-fecha-sistema-con-hora', 'PLA.FechaSistema with time (19 characters) is read with the date-time format: the date part filters the rows', LFECHA,
   ent([reg(1)]), fila(1), config={'PLATAFORMA/PLA.FechaSistema': '2025-04-01 08:30:00'})
lg(498, 'logica-sin-parametro-fecha-sistema-lanza-Exception', 'PLA.FechaSistema not configured: java.lang.Exception "Parametro ... no encontrado" before reading the lists', LFECHA,
   ent([reg(1)]), fila(1), config={'PLATAFORMA/PLA.FechaSistema': None})
lg(499, 'logica-fecha-sistema-invalida-lanza-DateTimeParseException', 'PLA.FechaSistema "01/04/2025": Fecha.toDate throws DateTimeParseException', LFECHA,
   ent([reg(1)]), fila(1), config={'PLATAFORMA/PLA.FechaSistema': '01/04/2025'})
lg(500, 'logica-no-procesadas-nula-lanza-NPE', 'NO_PROCESADAS explicitly null: parallelStream() on null throws NullPointerException; nothing written', LHASH,
   ent([reg(1)], None, P_OUT_SOLICITUDES_NO_PROCESADAS=None), fila(1))
lg(501, 'logica-a-procesar-nula-lanza-NPE', 'A_PROCESAR explicitly null: NullPointerException after the rejected list was grouped; nothing written', LFILT,
   ent(None, [reg(2, estado='R')], P_OUT_SOLICITUDES_A_PROCESAR=None), fila(1), fila(2))
lg(502, 'logica-elemento-nulo-en-no-procesadas-lanza-NPE', 'a null element in NO_PROCESADAS: the grouping lambda throws NullPointerException', LHASH,
   ent([reg(1)], [None, reg(2, estado='R')]), fila(1), fila(2))
lg(503, 'logica-id-nulo-en-no-procesadas-lanza-NPE', 'a rejected request with IdMandatoPAC null: groupingBy refuses a null key (NullPointerException); nothing written', LHASH,
   ent([reg(1)], [reg(None, estado='R')]), fila(1))
lg(504, 'logica-id-nulo-en-a-procesar-no-escribe-nada-util', 'a request to process with IdMandatoPAC null passes the filter (the map accepts a null key lookup) and is written with a NULL id: no row matches', LFILT,
   ent([reg(None), reg(1)]), fila(1))
lg(505, 'logica-id-ausente-usa-el-valor-por-defecto-cero', 'IdMandatoPAC absent keeps the constructor default 0: the row of mandate 0 is the one updated', LFILT,
   ent([{'IdentServicio': 'S0'}]), fila(0), fila(1))
lg(506, 'logica-id-no-numerico-lanza-JsonSyntaxException', 'the typed input is read with Gson: a non numeric IdMandatoPAC fails to decode (JsonSyntaxException) before logica runs', LLOGICA,
   ent([reg('abc')]), fila(1))
lg(507, 'logica-tabla-inexistente-lanza-SQLException', 'the LogMandatoPAC table does not exist: the first batch throws SQLException (a plain one, as SQL.batch rethrows)', LOK,
   ent([reg(1)], [reg(2, estado='R')]), 'DROP TABLE "%s";' % T_LOG, tablas=[])
lg(508, 'logica-muchas-solicitudes-en-lote', 'forty PRO and twenty PEN requests: every row of both batches is written, the untouched ones stay', LOK,
   ent([reg(i, 'S%d' % i) for i in range(1, 41)], [reg(i, 'S%d' % i, 'RECHAZO %d' % i) for i in range(41, 61)]), [fila(i) for i in range(1, 66)])
lg(509, 'logica-campos-extra-de-la-salida-se-ignoran', 'the other fields of SalidaRecaudacionTraspaso (counter, messages, result, other fields of the records) are read but never used', LLOGICA,
   ent([reg(1, 'S1', IdTraspaso=5, MontoDeposito=1500.5, FechaDeposito='Apr 1, 2025')],[reg(2, 'S2', 'R', IdCliente=3)],
       P_OUT_ERRORESCONTADOR=4, P_OUT_MENSAJES=[{'id': 1, 'tipo': 'ERROR', 'codigo': 'X', 'descripcion': 'x'}],
       P_OUT_RESULTADO=[{'IdSolicitud': 10}]), fila(1), fila(2))
