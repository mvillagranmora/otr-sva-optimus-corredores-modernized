"""Family C cases: ProcesarDistribucion (APV-C-001 .. 049)."""
from fix import (T_ANOT, T_CONTRATO, T_ESTTRANS, T_RCLI, T_REC, T_REMP, T_SOLAPO, T_CFGAPV, T_V_FONDO, T_INS, ins, msg, rcli, rec, remp)
from lib import L, caso, seed, tabla
from pd import (cliente_sn, contrato_y_distribucion, crd, cuota, escenario_ok, gest, asig, sc_calcular_cuotas, sc_cargo_abono, sc_precio, sc_ordenes, sc_solicitud, sc_traspaso_ok,
                sol_resultado, transiciones)

F = 'ProcesarDistribucion.java'
SEQ = {'BSCL.FondoMutuo.BD.Secuencias::FolioContrato': 5000, 'BSCL.APV.BD.Secuencia::APV.IdEstadoTransicion': 7000}
MASK = ['*IdTrabajo']
TREC = tabla(T_REC, '"IdRecaudacion"', columnas=['IdRecaudacion', 'IdEstado'])
TEMP = tabla(T_REMP, '"IdRecaudacionEmpleador"', columnas=['IdRecaudacionEmpleador', 'IdRecaudacion', 'IdEstado'])
TCLI = tabla(T_RCLI, '"IdRecaudacionCliente"', columnas=['IdRecaudacionCliente', 'IdRecaudacion', 'IdRecaudacionEmpleador', 'IdContratoDistribucion', 'IdEstado'])
TCON = tabla(T_CONTRATO, '"IdContratoSAAPV"', columnas=['IdContratoSAAPV', 'IdEstado'])
TET = tabla(T_ESTTRANS, '"IdEstadoTransicion"', enmascarar=['FechaTransicion'])
TABLAS = [TREC, TEMP, TCLI, TCON, TET]


def ent(opcion='DIS', ids=(10,), **kw):
    e = {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_Opcion': opcion, 'p_Parametros': [{'IdRecaudacion': i} for i in ids]}
    e.update(kw)
    return e


FFM_OK = {}
FFM_OK.update(sc_calcular_cuotas(cuota(1, 100000, 98.5)))
FFM_OK.update(sc_solicitud(resultados=[sol_resultado(1, 3001, 5000)]))
FFM_OK.update(sc_cargo_abono())
FFM_OK.update(sc_traspaso_ok(2))

# ------------------------------------------------------------------------------------------------ early exits
caso(1, 'ProcesarDistribucion', 'sin-registros-para-distribuir', 'the distribution view has no CON/REZ rows for the ids: NO_SE_ENCONTRARON_REGISTROS_PARA_DISTRIBUIR (empty value map) and nothing else',
     L(F, 'NO_SE_ENCONTRARON_REGISTROS_PARA_DISTRIBUIR'), ent(), seed(escenario_ok(estado='ING')), secuencias=SEQ, tablas=TABLAS, enmascarar=MASK)
caso(2, 'ProcesarDistribucion', 'sin-parametros-lanza-IndexOutOfBounds', 'empty p_Parametros: in.p_Parametros.get(0) throws IndexOutOfBoundsException (after the FolioContrato sequence was requested)',
     L(F, 'in.p_Parametros.get\\(0\\).IdRecaudacion == 0'), ent(ids=()), seed(escenario_ok()), secuencias=SEQ, tablas=TABLAS, enmascarar=MASK)
caso(3, 'ProcesarDistribucion', 'sin-parametro-abre-registros-lanza', 'PLATAFORMA/APV.AbreRegistrosTesoreria missing: ConfiguracionSistema throws Exception before anything else', L(F, 'AbreRegistrosTesoreria = ConfiguracionSistema'),
     ent(), seed(escenario_ok()), secuencias=SEQ, config={'PLATAFORMA/APV.AbreRegistrosTesoreria': None}, tablas=TABLAS, enmascarar=MASK)
caso(4, 'ProcesarDistribucion', 'opcion-nula-lanza-NullPointerException', 'p_Opcion missing with rows found: NullPointerException at in.p_Opcion.equals("RECTRA")', L(F, 'in.p_Opcion.equals\\("RECTRA"\\)'),
     {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_Parametros': [{'IdRecaudacion': 10}]}, seed(escenario_ok()), secuencias=SEQ, tablas=TABLAS, enmascarar=MASK)

# ------------------------------------------------------------------------------------------------ happy path with a fund distribution
caso(20, 'ProcesarDistribucion', 'distribuye-un-fondo-mutuo-completo', 'one valid recaudacion distributed 100% to a fund: FFMM request built, cuenta inversion abono/cargo, traspaso DIS, states DIS and the final OK message with the FFMM result',
     L(F, 'PROCESO_DISTRIBUCION_COMPLETADO'), ent(), seed(escenario_ok()), secuencias=SEQ, colaboradores=FFM_OK, tablas=TABLAS, enmascarar=MASK)
