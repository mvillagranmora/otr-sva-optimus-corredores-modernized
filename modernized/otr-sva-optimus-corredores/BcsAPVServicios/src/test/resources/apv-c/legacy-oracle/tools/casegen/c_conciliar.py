"""Family C cases: ConciliarDeposito (APV-C-090 .. 109)."""
from fix import (T_ASIGN, T_DOC, T_RCLI, T_REC, T_REMP, T_V_CLIENTE, ins, persona, portafolio, rcli, rec, refdata_apv, remp, vigente)
from lib import L, caso, seed, tabla

F = 'ConciliarDeposito.java'
TREC = tabla(T_REC, '"IdRecaudacion"', columnas=['IdRecaudacion', 'MontoDeposito', 'IdEstado'])
TEMP = tabla(T_REMP, '"IdRecaudacionEmpleador"', columnas=['IdRecaudacionEmpleador', 'IdRecaudacion', 'MontoDepositoConvenido', 'MontoDepositoAPV', 'IdEstado'])
TCLI = tabla(T_RCLI, '"IdRecaudacionCliente"', columnas=['IdRecaudacionCliente', 'IdRecaudacion', 'IdRecaudacionEmpleador', 'IdCliente', 'IdRegimenTriburario', 'MontoDepositoConvenido', 'MontoDepositoAPV', 'IdEstado'])
TASIG = tabla(T_ASIGN, '"IdRecaudacionCliente"', enmascarar=['FechaCreacion', 'FechaModificacion'])


def ent(*ids, masivo='N', asigna='N', **kw):
    e = {'p_IdEmpresa': 1, 'p_IdUsuario': 9, 'p_IdModulo': 'APV', 'p_AsignacionAutomaticaContrato': asigna, 'p_EsProcesoMasivo': masivo, 'p_Parametros': [{'IdRecaudacion': i} for i in ids]}
    e.update(kw)
    return e


def doc_emp(idpersona=4400, doc='76129263-3'):
    return ins(T_DOC, DocumentoIdentidad=doc, IdDocumentoIdentidad=idpersona + 1, IdPersona=idpersona, Predeterminado='S')


def doc_cli(idcliente, doc):
    return ins(T_V_CLIENTE, IdCliente=idcliente, DocumentoIdentidad=doc)


def deposito(idrec=10, monto=1900, estado='ING'):
    return rec(idrec, estado, monto=monto)


MASK = ['*IdTrabajo']

caso(90, 'ConciliarDeposito', 'solo-clientes-cuadran-concilia', 'no employer, client details add up to the deposit: deposit and clients become CON, CONCILIADO_OK message, AgregarLog called',
     L(F, 'RC.IdEstado			= "CON";'), ent(10),
     seed(deposito(), rcli(30, 10, None, 'ING', apv=1000), rcli(31, 10, None, 'ING', apv=900, idcliente=7002), doc_cli(7001, '9499210-9'), doc_cli(7002, '12969673-7')),
     tablas=[TREC, TCLI], enmascarar=MASK)
caso(91, 'ConciliarDeposito', 'empleador-y-clientes-cuadran-concilia', 'employer + clients: deposit = employer total and APV/DC subtotals of employer and clients agree: all three levels become CON',
     L(F, 'int\\[\\] res3 = sql.batch'), ent(10),
     seed(deposito(monto=1900), remp(20, 10, 'ING', convenido=900, apv=1000), rcli(30, 10, 20, 'ING', apv=1000, regimen='APV'), rcli(31, 10, 20, 'ING', convenido=900, apv=0, regimen='DC', idcliente=7002),
          doc_emp(), doc_cli(7001, '9499210-9'), doc_cli(7002, '12969673-7')),
     tablas=[TREC, TEMP, TCLI], enmascarar=MASK)
caso(92, 'ConciliarDeposito', 'empleador-sin-clientes-error', 'employer but no client details: UI_DEPOSITO_CON_EMPLEADOR_SIN_CLIENTE (non-massive text), counter 1, no log',
     L(F, 'UI_DEPOSITO_CON_EMPLEADOR_SIN_CLIENTE"'), ent(10), seed(deposito(), remp(20, 10, 'ING', apv=1900), doc_emp()), tablas=[TREC, TEMP], enmascarar=MASK)
caso(93, 'ConciliarDeposito', 'empleador-sin-clientes-masivo', 'same case in massive mode: the _MASIVO message; logica forces its counter to 0 (so the massive log with the folio range and a random IdTrabajo, masked, IS written) but call() recomputes P_OUT_ERRORESCONTADOR from the messages',
     L(F, 'UI_DEPOSITO_CON_EMPLEADOR_SIN_CLIENTE_MASIVO'), ent(10, masivo='S'), seed(deposito(), remp(20, 10, 'ING', apv=1900), doc_emp()), tablas=[TREC, TEMP], enmascarar=MASK)
caso(94, 'ConciliarDeposito', 'empleador-no-cuadra-con-deposito', 'employer total differs from the deposit: UI_NO_CUADRA_DEPOSITO_CON_EMPLEADOR', L(F, 'UI_NO_CUADRA_DEPOSITO_CON_EMPLEADOR"'), ent(10),
     seed(deposito(monto=1900), remp(20, 10, 'ING', apv=1800), rcli(30, 10, 20, 'ING', apv=1800), doc_emp(), doc_cli(7001, '9499210-9')), tablas=[TREC, TEMP, TCLI], enmascarar=MASK)
caso(95, 'ConciliarDeposito', 'empleador-cuadra-pero-cliente-no-por-regimen', 'employer = deposit but the APV/DC split of the clients differs: UI_NO_CUADRAN_MONTOS_APV_DC_DE_EMPLEADOR_CLIENTE', L(F, 'UI_NO_CUADRAN_MONTOS_APV_DC_DE_EMPLEADOR_CLIENTE"'), ent(10),
     seed(deposito(monto=1900), remp(20, 10, 'ING', convenido=900, apv=1000), rcli(30, 10, 20, 'ING', apv=1900, regimen='APV'), doc_emp(), doc_cli(7001, '9499210-9')),
     tablas=[TREC, TEMP, TCLI], enmascarar=MASK)
caso(96, 'ConciliarDeposito', 'clientes-no-cuadran-con-deposito', 'no employer, clients do not add up to the deposit: UI_NO_CUADRA_DEPOSITO_CON_CLIENTE', L(F, 'UI_NO_CUADRA_DEPOSITO_CON_CLIENTE"'), ent(10),
     seed(deposito(monto=1900), rcli(30, 10, None, 'ING', apv=1000), doc_cli(7001, '9499210-9')), tablas=[TREC, TCLI], enmascarar=MASK)
caso(97, 'ConciliarDeposito', 'sin-empleador-ni-clientes', 'neither employer nor clients: UI_DEPOSITO_SIN_EMPLEADOR_NI_CLIENTES', L(F, 'UI_DEPOSITO_SIN_EMPLEADOR_NI_CLIENTES"'), ent(10),
     seed(deposito()), tablas=[TREC], enmascarar=MASK)
caso(98, 'ConciliarDeposito', 'estado-no-permitido', 'a deposit already DIS: UI_DEPOSITO_CON_ESTADO_NO_PERMITIDO with the folio', L(F, 'UI_DEPOSITO_CON_ESTADO_NO_PERMITIDO"'), ent(10),
     seed(deposito(estado='DIS'), rcli(30, 10, None, 'DIS', apv=1900)), tablas=[TREC, TCLI], enmascarar=MASK)
caso(99, 'ConciliarDeposito', 'deposito-inexistente', 'unknown deposit: code ERROR with the UI_SELECCIONE_DEPOSITO text', L(F, 'UI_SELECCIONE_DEPOSITO'), ent(99), seed(deposito()), tablas=[TREC], enmascarar=MASK)
caso(100, 'ConciliarDeposito', 'estado-nco-tambien-concilia', 'a deposit in NCO (not conciliated) can be conciliated', L(F, 'Estado.equals\\("NCO"\\)'), ent(10),
     seed(deposito(estado='NCO'), rcli(30, 10, None, 'NCO', apv=1900), doc_cli(7001, '9499210-9')), tablas=[TREC, TCLI], enmascarar=MASK)
caso(101, 'ConciliarDeposito', 'dos-depositos-uno-ok-uno-error-sin-log', 'two deposits, one conciliated and one in error: counter 1 and the log is NOT written',
     L(F, 'if\\(salida.P_OUT_ERRORESCONTADOR.compareTo\\(0L\\) == 0\\) \\{'), ent(10, 11),
     seed(deposito(10), deposito(11, 500), rcli(30, 10, None, 'ING', apv=1900), rcli(31, 11, None, 'ING', apv=400), doc_cli(7001, '9499210-9')), tablas=[TREC, TCLI], enmascarar=MASK)
caso(102, 'ConciliarDeposito', 'lista-vacia-no-escribe-log', 'empty list: counter 0 and no log call', L(F, 'if\\(!in.p_Parametros.isEmpty\\(\\)\\) \\{'), ent(), seed(deposito()), tablas=[TREC], enmascarar=MASK)
caso(103, 'ConciliarDeposito', 'masivo-con-errores-cuenta-cero-y-escribe-log-de-rango', 'massive mode with three errors: logica keeps its counter at 0 so the range log is written (folio 10 to 12 after sorting) while call() still reports 3 errors',
     L(F, 'Rango Conciliaci'), ent(12, 10, 11, masivo='S'), seed(deposito(10), deposito(11), deposito(12)), tablas=[TREC], enmascarar=MASK)
caso(104, 'ConciliarDeposito', 'id-nulo-lanza-NullPointerException', 'IdRecaudacion missing: NullPointerException (fila.IdRecaudacion.toString())', L(F, 'valoresDiccionario.put\\("FolioRecaudacion"'),
     {'p_IdEmpresa': 1, 'p_Parametros': [{}]}, seed(deposito()), tablas=[TREC], enmascarar=MASK)
caso(105, 'ConciliarDeposito', 'asignacion-automatica-sin-contrato-candidato', 'p_AsignacionAutomaticaContrato S but no candidate contract: conciliated, the search runs, nothing assigned',
     L(F, 'ObtieneAsignaContrato = AsignaContrato'), ent(10, asigna='S'),
     seed(refdata_apv(), deposito(), rcli(30, 10, None, 'ING', apv=1900, idcliente=701), doc_cli(701, '11111111-1'), persona(501, '11111111-1', idc=701), portafolio(901, 701)),
     tablas=[TREC, TCLI, TASIG], enmascarar=MASK)
caso(106, 'ConciliarDeposito', 'asignacion-automatica-con-contrato-unico', 'p_AsignacionAutomaticaContrato S with a single candidate contract: it is assigned (AsignarContratoARecaudacion runs for real, its timestamps are the wall clock: masked)',
     L(F, 'AsignarContratoARecaudacion AsignaContrato = new'), ent(10, asigna='S'),
     seed(refdata_apv(), deposito(), rcli(30, 10, None, 'ING', apv=1900, idcliente=701), doc_cli(701, '11111111-1'), persona(501, '11111111-1', idc=701), portafolio(901, 701), vigente(9001)),
     tablas=[TREC, TCLI, TASIG], enmascarar=MASK)
caso(107, 'ConciliarDeposito', 'montos-con-decimales-se-truncan', 'amounts are compared as longs: deposit 1900.50 vs client 1900.99 both truncate to 1900, so they match', L(F, 'longValue\\(\\)'), ent(10),
     seed(rec(10, 'ING', monto=1900.5), rcli(30, 10, None, 'ING', apv=1900.99), doc_cli(7001, '9499210-9')), tablas=[TREC, TCLI], enmascarar=MASK)
