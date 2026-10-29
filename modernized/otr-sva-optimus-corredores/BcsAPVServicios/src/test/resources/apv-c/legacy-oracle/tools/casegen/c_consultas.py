"""Family C cases: ConsultarTotalesDistribucionRecaudacion, ConsultarTotalesOrigenPorDeposito (APV-C-470 .. 479)."""
from lib import L, caso, ins, seed, tabla

T_ESTADO = 'BSCL.APV.BD::APV.Estado'
T_CRD = 'BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido'

F = 'ConsultarTotalesDistribucionRecaudacion.java'


def estados():
    return [ins(T_ESTADO, IdEstado=i, Codigo=c, Nombre=n, NombreEntidad='RECAUDACION')
            for i, (c, n) in enumerate([('ING', 'INGRESADO'), ('CON', 'CONCILIADO'), ('DIS', 'DISTRIBUIDO'), ('REZ', 'REZAGO'), ('REC', 'RECUPERADO')], start=1)] + \
           [ins(T_ESTADO, IdEstado=9, Codigo='ACT', Nombre='ACTIVO', NombreEntidad='CLIENTE')]


def crd(folio, estado, fecha, rec_total, dis_total=0, regimen='APV', rec_monto=None, dis_dc=0, dis_apv=0):
    return ins(T_CRD, FolioRecaudacion=folio, CodigoEstado=estado, NombreEstado=estado, FechaDeposito=fecha, RecTotal=rec_total, DisTotal=dis_total,
               TipoRegimen=regimen, RecMonto=rec_total if rec_monto is None else rec_monto, DisMontoDC=dis_dc, DisMontoAPV=dis_apv)


caso(470, 'ConsultarTotalesDistribucionRecaudacion', 'sin-registros-solo-estados', 'no deposits in range: every state counts 0, totals 0',
     L(F, 'out.p_Totales = sql.query', 'out.p_Resumen = sql.query'),
     {'p_FechaDesde': '2025-03-01', 'p_FechaHasta': '2025-03-31'}, seed(estados()), sin_orden=['p_Totales', 'p_Resumen'])

caso(471, 'ConsultarTotalesDistribucionRecaudacion', 'mezcla-de-estados-y-regimenes', 'deposits in several states and regimes inside the range',
     L(F, 'out.p_Totales = sql.query', 'out.p_Resumen = sql.query'),
     {'p_FechaDesde': '2025-03-01', 'p_FechaHasta': '2025-03-31'},
     seed(estados(), crd(1, 'CON', '2025-03-05', 1000, regimen='APV'), crd(2, 'CON', '2025-03-06', 500, regimen='DC'),
          crd(3, 'DIS', '2025-03-07', 2000, 1990, regimen='APV', dis_apv=1990), crd(4, 'DIS', '2025-03-08', 800, 800, regimen='DC', dis_dc=800),
          crd(5, 'REZ', '2025-03-09', 300, regimen='APV'), crd(6, 'REC', '2025-03-10', 100, regimen='DC')), sin_orden=['p_Totales', 'p_Resumen'])

caso(472, 'ConsultarTotalesDistribucionRecaudacion', 'fechas-fuera-de-rango-no-cuentan', 'deposits outside the date range are not counted (BETWEEN is inclusive)',
     L(F, 'p_FechaDesde', 'p_FechaHasta'),
     {'p_FechaDesde': '2025-03-05', 'p_FechaHasta': '2025-03-06'},
     seed(estados(), crd(1, 'CON', '2025-03-04', 111), crd(2, 'CON', '2025-03-05', 222), crd(3, 'CON', '2025-03-06', 333), crd(4, 'CON', '2025-03-07', 444)), sin_orden=['p_Totales', 'p_Resumen'])

caso(473, 'ConsultarTotalesDistribucionRecaudacion', 'fechas-nulas-no-devuelve-filas-de-datos', 'null dates: BETWEEN NULL matches nothing, still one row per state',
     L(F, 'param.put\\("p_FechaDesde"'),
     {}, seed(estados(), crd(1, 'CON', '2025-03-04', 111)), sin_orden=['p_Totales', 'p_Resumen'])

# ------------------------------------------------------------------------------------------------ ConsultarTotalesOrigenPorDeposito (stored procedure, scripted)
F = 'ConsultarTotalesOrigenPorDeposito.java'
SP = 'BSCL.APV.BD.Servicios::APV.ConsultarTotalesOrigenPorDeposito'


def sp(**kw):
    return {'StoredProcedure': {SP: {'salidas': [kw]}}}


def eto(rec=10):
    e = {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV'}
    if rec is not None:
        e['p_IdRecaudacion'] = rec
    return e


caso(474, 'ConsultarTotalesOrigenPorDeposito', 'sin-errores-devuelve-totales', 'the procedure reports no errors: its totals table is returned', L(F, 'out.P_TOTALES_ORIGEN =  \(List'),
     eto(), colaboradores=sp(p_out_ErroresContador=0, p_out_AdvertenciasContador=0, p_out_TotalesOrigen=[{'Origen': 'CAJA', 'Total': 1500.5}, {'Origen': 'BANCO', 'Total': 300}]))
caso(475, 'ConsultarTotalesOrigenPorDeposito', 'errores-un-solo-mensaje-reutilizado', 'procedure errors: ONE MensajeSalida instance is reused for every error and warning (all entries equal the last), tipo never set so the counter is 0',
     L(F, 'MensajeSalida mensajeSalida = new MensajeSalida'),
     eto(), colaboradores=sp(p_out_ErroresContador=2, p_out_AdvertenciasContador=1,
                             p_out_ErroresDetalle=[{'Codigo': 'E1', 'Descripcion': 'primer error'}, {'Codigo': 'E2', 'Descripcion': 'segundo error'}],
                             p_out_AdvertenciasDetalle=[{'Codigo': 'W1', 'RequiereAutorizacion': 'N', 'Descripcion': 'una advertencia'}]))
caso(476, 'ConsultarTotalesOrigenPorDeposito', 'contador-de-errores-ausente-lanza-NullPointerException', 'the procedure leaves p_out_ErroresContador unset: NullPointerException', L(F, 'resultSP.get\("p_out_ErroresContador"\)'),
     eto(), colaboradores=sp(p_out_AdvertenciasContador=0))
caso(477, 'ConsultarTotalesOrigenPorDeposito', 'el-procedimiento-falla-lanza-SQLException', 'the procedure call itself fails: SQLException', L(F, 'sql.call\(con'),
     eto(), colaboradores={'StoredProcedure': {SP: {'lanza': 'procedure failed'}}})
caso(478, 'ConsultarTotalesOrigenPorDeposito', 'sin-tabla-de-totales-queda-nula', 'no errors and the totals table not returned: P_TOTALES_ORIGEN becomes null (omitted from the JSON)', L(F, 'out.P_TOTALES_ORIGEN =  \(List'),
     eto(), colaboradores=sp(p_out_ErroresContador=0))
caso(479, 'ConsultarTotalesOrigenPorDeposito', 'recaudacion-nula-llega-nula-al-procedimiento', 'p_IdRecaudacion missing: the procedure receives a null IdRecaudacion', L(F, 'paramsIn.put\("IdRecaudacion"'),
     eto(rec=None), colaboradores=sp(p_out_ErroresContador=0, p_out_TotalesOrigen=[]))
