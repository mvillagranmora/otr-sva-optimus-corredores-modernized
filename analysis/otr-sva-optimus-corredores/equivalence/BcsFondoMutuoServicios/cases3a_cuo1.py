"""FM-CUO cases, part 1 (rules 01-04, 07): parameters, price lookup, missing quota data, amount per quota, zero quota value."""
from cases3a_lib import *

USD = clonar_serie("FM002U", "20", fondo={"IdMoneda": "USD"}, serie={"IdInstrumento": 901, "DecimalesCuota": 2, "DecimalesValorCuota": 2}, tipo_cambio=(950, 940))
EUR = [moneda_formato("EUR", 2)] + clonar_serie("FM003E", "21", fondo={"IdMoneda": "EUR"}, serie={"IdInstrumento": 902}, tipo_cambio=(1000, 990))
SINTC = clonar_serie("FM005U", "23", fondo={"IdMoneda": "USD"}, serie={"IdInstrumento": 903, "DecimalesCuota": 2})
SINFONDO = clonar_serie("FM006X", "98", fondo=False, serie={"IdInstrumento": 904})
BRL = clonar_serie("FM004B", "22", fondo={"IdMoneda": "BRL"}, serie={"IdInstrumento": 905})
FECHA = "2026-03-02"


def ser(i=1, serie="FM001A", fecha=FECHA, modo="M", monto=150000, cuotas=None, mf="CLP", mi="CLP", tipo="APO", tfa="UF", porc=0, mov="", **x):
    d = {"IdInterno": i, "Fecha": fecha, "IdSerieFondo": serie, "CalculoPorMontoCuota": modo, "Monto": monto, "NumeroCuotas": cuotas, "IdMonedaFondo": mf,
         "IdMonedaInversion": mi, "TipoSolicitud": tipo, "TipoFactorAjuste": tfa, "IdEmpresa": 1, "Porcentaje": porc, "IdTipoMovimientoFondoMutuo": mov}
    d.update(x)
    return d


def cuo(cid, ref, series, seed=None, colab=None):
    caso(cid, "CalcularMontoNumeroCuota", ref, {"p_IdEmpresa": 1, "p_Series": series}, seed, colab)


def precios(*items, error=None):
    """items: (IdRegistro, Precio[, IdInstrumento[, FechaFuente]]); IdInstrumento None is filtered by the legacy."""
    filas = []
    for it in items:
        f = {"IdRegistro": it[0], "IdClasificacionTipoInstrumento": "FM", "Precio": it[1], "IdInstrumento": it[2] if len(it) > 2 else 900,
             "FechaFuente": it[3] if len(it) > 3 else "2026-03-02 00:00:00.000"}
        filas.append(f)
    return {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": filas}}}


def paridad(t1=(), t2=()):
    """CalcularParidad answers by IdTrabajo: t1 / t2 are lists of calculation dicts {IdInterno, FactorCambioLocal, FactorOrigenDestino, MontoDestino}."""
    return {"CalcularParidad": {"respuestas": [{"cuando": {"0.p_IdTrabajo": 1}, "respuesta": {"p_out_CalculosSalida": list(t1)}},
                                               {"cuando": {"0.p_IdTrabajo": 2}, "respuesta": {"p_out_CalculosSalida": list(t2)}}],
                                "defecto": {"p_out_CalculosSalida": []}}}


def conjunto(*dicts):
    r = {}
    for d in dicts:
        r.update(d)
    return r


def generar():
    cuo("FM-CUO-01a_sin-usa-dolar-observado", "FM-CUO-01: falta el parametro FONDOMUTUO/UsaDolarObservado => excepcion de ConfiguracionSistema", [ser()],
        parametro("UsaDolarObservado", None))
    cuo("FM-CUO-01b_sin-moneda-sistema", "FM-CUO-01: falta PLATAFORMA/PLA.MonedaSistema => excepcion de ConfiguracionSistema", [ser()], config_plataforma("PLA.MonedaSistema", None))
    cuo("FM-CUO-02a_precios-vacios", "FM-CUO-02/03: EntregarPreciosTasas sin filas => un ERROR_VALORCUOTA por serie, sin IdInterno", [ser(), ser(2, tipo="RES")], None, precios())
    cuo("FM-CUO-02b_precio-sin-instrumento", "FM-CUO-02: las filas sin IdInstrumento se descartan => mapa vacio", [ser()], None, precios((1, 1500.1234, None)))
    cuo("FM-CUO-02c_precio-duplicado", "FM-CUO-02 D-CUO-7: IdRegistro repetido => IllegalStateException de toMap", [ser(), ser(2)], None, precios((1, 1500.1234), (1, 1600)))
    cuo("FM-CUO-02d_ids-cero-y-negativo", "FM-CUO-02/08: IdInterno 0 busca el registro 0; IdInterno negativo usa el signo -1 en los ids de paridad",
        [ser(0), ser(-3, tipo="RES"), ser(5, mov="INGGTA")], None, precios((0, 1500.1234), (-3, 1400), (5, 1300)))
    cuo("FM-CUO-02e_idinterno-nulo", "FM-CUO-02: IdInterno nulo => NullPointerException al armar la consulta de precios", [ser(None)])
    cuo("FM-CUO-02f_fecha-con-hora", "FM-CUO-02: la fecha de la serie con hora (19 caracteres) se consulta como fecha", [ser(fecha="2026-03-02 10:30:00")])
    cuo("FM-CUO-02g_sin-pais-sistema", "FM-CUO-02: falta PLATAFORMA/PLA.PaisSistema => excepcion de ConfiguracionSistema", [ser()], config_plataforma("PLA.PaisSistema", None))
    cuo("FM-CUO-03a_serie-inexistente", "FM-CUO-03: serie que no existe: sin precios y sin datos de cuota => ERROR_VALORCUOTA por solicitud con IdInterno, sin llamar al servicio de precios",
        [ser(1, serie="NOEXISTE"), ser(2, serie="NOEXISTE")])
    cuo("FM-CUO-03b_serie-sin-fondo", "FM-CUO-03: serie sin fondo: hay serie pero no datos de cuota => ERROR_VALORCUOTA por solicitud (el servicio de precios si se llama)",
        [ser(1, serie="FM006X")], SINFONDO)
    cuo("FM-CUO-03c_mezcla-con-y-sin-datos", "FM-CUO-03: una serie con datos y otra sin ellos: la sin datos queda con valor cuota 0 (ERROR_VALOR_CUOTA)",
        [ser(1), ser(2, serie="FM006X", modo="C", cuotas=3)], SINFONDO, precios((1, 1500.1234), (2, 1000)))
    cuo("FM-CUO-03d_precio-nulo-fuente-nula", "FM-CUO-03: precio nulo (valor cuota 0) y fecha fuente nula (0001-01-01)", [ser(1, modo="C", cuotas=3)], None,
        {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": [{"IdRegistro": 1, "IdInstrumento": 900, "Precio": None, "FechaFuente": None}]}}})
    cuo("FM-CUO-04a_cuotas-dadas-modo-c", "FM-CUO-04/06: modo C con NumeroCuotas: MontoPorCuota = valor cuota * cuotas y es el Monto resultante", [ser(modo="C", monto=0, cuotas=10)])
    cuo("FM-CUO-04b_cuotas-nulas-monto-cero", "FM-CUO-04/06/09: modo C sin NumeroCuotas y Monto 0: cuotas = 0 => decimales+1 con HALF_UP (D-CUO-3)", [ser(modo="C", monto=0, cuotas=None)])
    cuo("FM-CUO-04c_cuotas-nulas-con-monto", "FM-CUO-04 D-CUO-2: modo C sin NumeroCuotas y Monto > 0 => NullPointerException", [ser(modo="C", monto=5000, cuotas=None)])
    cuo("FM-CUO-04d_modo-m-con-cuotas", "FM-CUO-04/06: modo M ignora las cuotas de entrada y las recalcula", [ser(modo="M", monto=150000, cuotas=3)])
    cuo("FM-CUO-04e_modo-total", "FM-CUO-04/06: modo T se trata como el modo C (distinto de M)", [ser(modo="T", monto=0, cuotas=5)])
    cuo("FM-CUO-04f_monto-por-cuota-cero-con-monto", "FM-CUO-06: modo C, cuotas 0 y Monto > 0: cuotas = Monto / valor cuota (HALF_DOWN) y Monto formateado", [ser(modo="C", monto=75000, cuotas=0)])
    cuo("FM-CUO-07a_valor-cuota-cero-se-omite", "FM-CUO-07 D-CUO-1: precio 0 (escala 0) es equals(ZERO) => se omite el calculo; modo C con cuotas 0 => division por cero al final",
        [ser(modo="C", monto=1000, cuotas=0)], None, precios((1, 0)))
    cuo("FM-CUO-07b_valor-cuota-cero-escala", "FM-CUO-07 D-CUO-1: precio 0.0000 (escala 4) NO es equals(ZERO) => divide por cero en modo M", [ser()], None, precios((1, 0.0000)))
    cuo("FM-CUO-07c_valor-cuota-cero-monto-cero", "FM-CUO-07 D-CUO-1: precio 0 con modo M y cuotas dadas: el resultado sale con ERROR_VALOR_CUOTA tras fallar la division",
        [ser(modo="M", monto=0, cuotas=0)], None, precios((1, 0)))
    cuo("FM-CUO-07d_valor-cuota-cero-sin-cuotas", "FM-CUO-07: precio 0 y NumeroCuotas nulo => NullPointerException", [ser(modo="M", cuotas=None)], None, precios((1, 0)))
    cuo("FM-CUO-07e_valor-cuota-cero-con-cuotas", "FM-CUO-07/10: precio 0 con cuotas distintas de 0: se omite el calculo y el resultado sale con ERROR_VALOR_CUOTA (CodigoMensaje)",
        [ser(modo="C", monto=1000, cuotas=5)], None, precios((1, 0)))
