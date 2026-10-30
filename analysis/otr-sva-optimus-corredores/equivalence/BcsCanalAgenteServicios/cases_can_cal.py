"""BcsCanalAgenteServicios oracle: inputs of the cases of the JavaCall entry ConsultarHistorialMovimientosCuentaInversion.call (family CAL): JSON in, JSON out."""
from cases_can_lib import *  # noqa: F401,F403

C = "ConsultarHistorial.call"
TP = [dump_proc()]


def base(movs, **kw):
    return cartola(resp(movs, **kw))


def generar():
    g = Gen("CAL")
    M = mundo()
    j = entrada_h

    g.caso("json-completo-devuelve-el-contrato-con-nulos-explicitos", C, j(), ["B-CA-11", "B-CA-03", "D-CAN-02", "D-CAN-03"], base([mov(1, 5001), mov(2, None)]), tablas=TP, seed=M)
    g.caso("numeros-como-texto-se-aceptan", C, {"p_IdEmpresa": "1", "p_IdUsuario": "1646", "p_IdModulo": "CTA", "p_IdPortafolio": "100", "p_FechaDesde": "2026-02-01", "p_FechaHasta": "2026-02-28",
                                               "p_IdMoneda": "CLP", "p_IdMatrizAgrupador": "0"}, ["B-CA-11", "D-CAN-10", "B-CA-05"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("enteros-con-punto-cero-se-aceptan", C, j(p_IdPortafolio=100.0, p_IdUsuario=1646.0), ["D-CAN-10"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("entero-con-decimales-reales-lanza-error-de-gson", C, j(p_IdPortafolio=100.5), ["D-CAN-10"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("portafolio-no-numerico-lanza-error-de-gson", C, j(p_IdPortafolio="abc"), ["D-CAN-10"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("matriz-texto-vacio-lanza-error-de-gson", C, j(p_IdMatrizAgrupador=""), ["D-CAN-10"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("campos-desconocidos-se-ignoran", C, j(p_Otro="x", extra={"a": 1}), ["B-CA-11", "D-CAN-10"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("nombre-de-campo-en-minusculas-queda-nulo-y-falla-el-portafolio", C, {"p_idportafolio": 100, "p_IdEmpresa": 1}, ["D-CAN-10", "D-CAN-01"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("json-vacio-falla-por-portafolio-nulo", C, {}, ["D-CAN-01", "D-CAN-09"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("cliente-no-encontrado-lanza-nullpointer-tambien-por-json", C, j(999), ["D-CAN-01"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("nulos-explicitos-en-los-opcionales", C, j(p_IdMatrizAgrupador=None, p_EsADC=None), ["B-CA-05"], base([mov(1, 5001)]), tablas=TP, seed=M)
    g.caso("opcionales-presentes-viajan-a-la-cartola", C, j(p_IdMatrizAgrupador=7, p_EsADC="S"), ["B-CA-05", "B-CA-02"], base([mov(1, 5001, IdMatrizAgrupador=7, EsADC="S")]), tablas=TP, seed=M)
    g.caso("fechas-con-hora-en-la-cartola-salen-con-milisegundos", C, j(), ["B-CA-11", "B-CA-06"],
           base([mov(1, 5001, FechaSistema="2026-02-03 10:15:00", FechaLiquidacionOperacion="2026-02-05 23:59:59.123")]), tablas=TP, seed=M)
    g.caso("fecha-nula-en-la-cartola-sale-nula", C, j(), ["B-CA-11", "B-CA-06"], base([mov(1, 5001, FechaSistema=None, FechaLiquidacionOperacion=None)]), tablas=TP, seed=M)
    g.caso("cartola-con-errores-y-sin-movimientos-por-json", C, j(), ["D-CAN-04", "B-CA-08"], base([], contador=1, errores=[{"Codigo": "E1", "Descripcion": "x"}]), tablas=TP, seed=M)
    g.caso("cartola-sin-movimientos-devuelve-listas-vacias", C, j(), ["B-CA-08", "B-CA-11"], base([]), tablas=TP, seed=M)
    g.caso("falla-de-la-base-se-propaga-por-json", C, j(), ["D-CAN-05", "B-CA-03"], base([mov(1, 5001)]), tablas=TP, seed=M, fallos=[fallo("TO_VARCHAR", "no se puede leer el comprobante")])
    g.caso("cartola-lanza-error-se-propaga-por-json", C, j(), ["B-CA-02"], cartola(error="Error en la cartola"), tablas=TP, seed=M)
    g.caso("caracteres-especiales-y-unicode-en-los-nombres", C, j(), ["B-CA-06", "B-CA-11"],
           base([mov(1, 5001, NombreCliente="Ñandú \"Pérez\" & Cía <ltda>", NombreConcepto="Abono áéí – señal", NombreEstado="Vigente\ttab")]), tablas=TP, seed=M)
    return g.ids
