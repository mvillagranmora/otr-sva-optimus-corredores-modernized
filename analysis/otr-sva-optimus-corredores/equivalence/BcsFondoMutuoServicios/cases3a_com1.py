"""FM-COM cases, part 1 (rules 01-03): parameters, FIFO custody lots, quota value used for the commission."""
from cases3a_lib import *
from cases3a_cuo1 import precios, paridad, conjunto

PARAM = "CALCULAR_COMISION_VALOR_CUOTA_ACTUAL"
TRAMOS = (tramo(1, "FM001A", "ENTRADA", 0, 30, 1.5) + tramo(2, "FM001A", "SALIDA", 0, 30, 2.0) + tramo(3, "FM001A", "SALIDA", 31, 90, 1.0)
          + [comision_maxima("FM001A")])


def lote(cant, origen="2026-02-10", op="2026-02-09", concepto="APORFM", precio=1500, cli=100, port=500, inst=900):
    return {"IdCliente": cli, "IdPortafolio": port, "IdInstrumento": inst, "Cantidad": cant, "PrecioCompra": precio, "IdConcepto": concepto,
            "FechaOrigenCompra": origen + " 00:00:00.000", "FechaOperacion": None if op is None else op + " 00:00:00.000"}


def custodia(*lotes, error=None):
    if error:
        return {"ConsultarFlujoCustodia": {"respuestas": [{"error": error}]}}
    return {"ConsultarFlujoCustodia": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "p_out_FlujoDetallado": list(lotes)}}}


def cu(i=1, serie="FM001A", inst=900, cli=100, port=500, cant=100, mf="CLP", mo="CLP", tipo="RES", mov="RESNORM", fvc="2026-03-02", fcurse="2026-03-02", es="N", **x):
    d = {"IdInterno": i, "IdSerieFondo": serie, "IdInstrumento": inst, "IdCliente": cli, "IdPortafolio": port, "Cantidad": cant, "IdMonedaFondo": mf,
         "IdMonedaOperacion": mo, "TipoSolicitud": tipo, "IdTipoMovimientoFondoMutuo": mov, "FechaValorCuota": fvc, "FechaCurse": fcurse, "EsTraspaso": es}
    d.update(x)
    return d


def com(cid, ref, datos, seed=None, colab=None):
    caso(cid, "CalcularComisiones", ref, {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_SimularOperacion": "S", "p_UsaReglasNegocio": "S",
                                          "p_DatosCustodia": datos}, seed, colab)


def generar():
    L1 = custodia(lote(100, precio=1500))
    P2000 = precios((1, 2000.5))
    com("FM-COM-01a_parametro-ausente-usa-precio-actual", "FM-COM-01/03: sin CALCULAR_COMISION_VALOR_CUOTA_ACTUAL el legacy usa S: el rescate comisiona con el precio vigente",
        [cu()], TRAMOS + parametro(PARAM, None), conjunto(L1, P2000))
    com("FM-COM-01b_parametro-n-rescate-usa-precio-del-lote", "FM-COM-01/03: parametro N: el RESCATE usa el precio de compra del lote", [cu()], TRAMOS + parametro(PARAM, "N"), conjunto(L1, P2000))
    com("FM-COM-01c_parametro-n-aporte-usa-precio-actual", "FM-COM-01/03: parametro N: el APORTE sigue usando el precio vigente", [cu(tipo="APO", fcurse="2026-02-20")], TRAMOS + parametro(PARAM, "N"),
        conjunto(L1, P2000))
    com("FM-COM-01d_parametro-s", "FM-COM-01/03: parametro S explicito", [cu()], TRAMOS + parametro(PARAM, "S"), conjunto(L1, P2000))
    com("FM-COM-01e_sin-fecha-sistema", "FM-COM-01: falta PLATAFORMA/PLA.FechaSistema => excepcion de ConfiguracionSistema", [cu()], config_plataforma("PLA.FechaSistema", None),
        conjunto(L1, P2000))
    com("FM-COM-01f_sin-moneda-sistema", "FM-COM-01: falta PLA.MonedaSistema (se lee despues de los precios) => excepcion", [cu()], TRAMOS + config_plataforma("PLA.MonedaSistema", None),
        conjunto(L1, P2000))
    com("FM-COM-01g_sin-pais-sistema", "FM-COM-01: falta PLA.PaisSistema (al pedir los precios) => excepcion", [cu()], TRAMOS + config_plataforma("PLA.PaisSistema", None), conjunto(L1, P2000))
    com("FM-COM-01h_fecha-sistema-con-hora", "FM-COM-01: PLA.FechaSistema de 19 caracteres se lee como fecha", [cu()], TRAMOS + config_plataforma("PLA.FechaSistema", "2026-03-02 08:00:00"),
        conjunto(L1, P2000))
    lotes3 = custodia(lote(100, "2026-02-20"), lote(50, "2026-02-05"), lote(80, "2026-02-12"))
    P = precios((1, 1000), (2, 1000), (3, 1000))
    com("FM-COM-02a_consume-lotes-fifo", "FM-COM-02: los lotes se ordenan por FechaOrigenCompra ascendente y el rescate consume 50 + 70", [cu(cant=120)], TRAMOS, conjunto(lotes3, P))
    com("FM-COM-02b_cantidad-mayor-al-total", "FM-COM-02: la cantidad supera todos los lotes: consume todos y queda saldo en la entrada", [cu(cant=500)], TRAMOS, conjunto(lotes3, P))
    com("FM-COM-02c_cantidad-igual-al-lote", "FM-COM-02: cantidad igual al primer lote: lo consume entero y corta", [cu(cant=50)], TRAMOS, conjunto(lotes3, P))
    com("FM-COM-02d_cantidad-cero", "FM-COM-02: cantidad 0: el primer lote aporta 0 y corta", [cu(cant=0)], TRAMOS, conjunto(lotes3, P))
    com("FM-COM-02e_lotes-de-otro-cliente", "FM-COM-02: los lotes de otro cliente/portafolio/instrumento no cuentan",
        [cu(cant=10)], TRAMOS, conjunto(custodia(lote(100, cli=101), lote(100, port=501), lote(100, inst=901), lote(40, "2026-02-11")), P))
    com("FM-COM-02f_aporte-tambien-consume-lotes", "FM-COM-02 D-COM-6: un aporte consume los lotes de custodia como un rescate (comision ENTRADA)",
        [cu(tipo="APO", cant=60, fcurse="2026-02-20")], TRAMOS, conjunto(lotes3, P))
    com("FM-COM-02g_dos-solicitudes-mismos-lotes", "FM-COM-02: cada solicitud recorre los mismos lotes (el detalle no se descuenta entre solicitudes)", [cu(1, cant=60), cu(2, cant=60)], TRAMOS,
        conjunto(lotes3, P))
    com("FM-COM-02h_sin-lotes", "FM-COM-02: sin lotes de custodia la comision es 0", [cu()], TRAMOS, conjunto(custodia(), P))
    com("FM-COM-02i_instrumento-cero-filtra-nulo", "FM-COM-02: IdInstrumento 0 pide todos los instrumentos (filtro nulo); los lotes se cruzan por instrumento 0",
        [cu(inst=0)], TRAMOS, conjunto(custodia(lote(10, inst=0)), P))
    com("FM-COM-02j_fechas-de-lote-iguales", "FM-COM-02: lotes con la misma fecha de origen conservan el orden de entrada",
        [cu(cant=150)], TRAMOS, conjunto(custodia(lote(100, "2026-02-10", precio=1400), lote(100, "2026-02-10", precio=1600)), P))
    com("FM-COM-02k_error-de-custodia", "FM-COM-02: el servicio de custodia falla => la excepcion se propaga", [cu()], TRAMOS, custodia(error="custodia no disponible"))
    com("FM-COM-02l_empresa-inexistente", "FM-COM-02: ConsultarEmpresa falla => excepcion", [cu()], TRAMOS, {"ConsultarEmpresa": {"respuestas": [{"error": "Empresa no existe"}]}})
    com("FM-COM-02m_instrumento-nulo", "FM-COM-02: IdInstrumento nulo => NullPointerException", [cu(inst=None)], TRAMOS, conjunto(L1, P))
    com("FM-COM-03a_sin-precio", "FM-COM-03: sin precio para el IdInterno la comision se calcula con valor cuota 0", [cu()], TRAMOS + parametro(PARAM, "S"), conjunto(L1, precios((9, 1000))))
    com("FM-COM-03b_precio-nulo", "FM-COM-03: precio nulo vale 0", [cu()], TRAMOS + parametro(PARAM, "S"), conjunto(L1, {"EntregarPreciosTasas": {"defecto": {
        "P_OUT_PRECIOSTASAS": [{"IdRegistro": 1, "IdInstrumento": 900, "Precio": None}]}}}))
    com("FM-COM-03c_precio-duplicado", "FM-COM-03: IdRegistro repetido en los precios => IllegalStateException de toMap", [cu(1), cu(2)], TRAMOS, conjunto(L1, precios((1, 10), (1, 11))))
    com("FM-COM-03d_fecha-valor-cuota-invalida", "FM-COM-03: FechaValorCuota vacia => DateTimeParseException al pedir precios", [cu(fvc="")], TRAMOS, conjunto(L1, P2000))
    com("FM-COM-03e_serie-inexistente", "FM-COM-03/04: serie inexistente: sin datos de serie (EsAPV N, tipo de instrumento vacio) y sin tramos", [cu(serie="NOEXISTE")], TRAMOS,
        conjunto(L1, P2000))
