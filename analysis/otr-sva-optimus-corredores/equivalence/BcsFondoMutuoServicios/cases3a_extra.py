"""Extra FM-FEC / FM-CUO / FM-COM cases that close the branch coverage: calendar-country chain, state combinations, holidays by country, national-dollar
variants, EUR investment, null conversions, several custody rows, currency formats (MonedaFormato called directly)."""
from cases3a_lib import *
from cases3a_fec1 import sol, fec, PERMITE, WE56
from cases3a_fec2 import PAISES, cal
from cases3a_fec3 import EXT, SEM, cfg56
from cases3a_cuo1 import ser, cuo, precios, paridad, conjunto, EUR
from cases3a_com1 import lote, custodia, cu, com, TRAMOS, PARAM
from cases3a_com2 import P as PCOM, S as SCOM, PER


def generar_fec():
    todo = dict(cl="S", ce="S", cf="S", cm="S")
    for nombre, kw in (("j_emisor-cero", dict(pe=0, pf=76, pm=152)), ("k_emisor-y-fondo-cero", dict(pe=0, pf=0, pm=152)), ("l_fondo-y-moneda-cero", dict(pe=840, pf=0, pm=0))):
        fec("FM-FEC-06%s" % nombre, "FM-FEC-06: la cadena de pais de calculo salta los paises 0 (emisor, fondo, local, moneda, empresa); las claves del mapa pueden no existir (D-FEC-10)",
            [sol(1, ef="C", el="C", vcert=0, **kw)], PAISES + cal(**todo))
    base = cfg_borrar() + cfg_completa(dias={"CERTIF": 2, "LIQUID": 3}, **{t: dict(ce="S", cf="S") for t in ("DIGITA", "CURSE", "VCUOTA", "CERTIF", "LIQUID")})
    fec("FM-FEC-08g_estado-fondo-a-liquidacion-c", "FM-FEC-08: EstadoFondo=A pero EstadoLiquidacion=C usa los dias de configuracion",
        [sol(1, pe=840, pf=76, pm=76, ef="A", el="C")], fin_de_semana(56, 840, 76) + base)
    fec("FM-FEC-08h_estado-fondo-c-liquidacion-a", "FM-FEC-08: EstadoFondo=C y EstadoLiquidacion=A usa los dias de configuracion",
        [sol(1, pe=840, pf=76, pm=76, ef="C", el="A")], fin_de_semana(56, 840, 76) + base)
    ex = dict(serie="FMEXT1", pe=76, pf=840, pm=152, fext="S", ef="C", el="C")
    for pais in (840, 76, 152):
        fec("FM-FEC-10h%d_certificacion-feriado-pais-%d" % (pais, pais), "FM-FEC-10: la certificacion cae en feriado de uno solo de los paises de la serie (%d)" % pais,
            [sol(1, **ex)], EXT + SEM + fin_de_semana(76) + cfg56(1, 2) + [feriado("2026-03-03", pais)])
        fec("FM-FEC-10i%d_liquidacion-feriado-pais-%d" % (pais, pais), "FM-FEC-10: la liquidacion cae en feriado de uno solo de los paises (%d)" % pais,
            [sol(1, **ex)], EXT + SEM + fin_de_semana(76) + cfg56(1, 2) + [feriado("2026-03-04", pais)])
    fec("FM-FEC-10j_certificacion-feriado-empresa", "FM-FEC-10: la certificacion (0 dias, sin mover) cae en feriado de la empresa", [sol(1, **ex)], EXT + SEM + cfg56(0, 0) + [feriado("2026-03-02", 56)])
    fec("FM-FEC-10k_feriados-mismo-pais-distinta-fecha", "FM-FEC-10 D-FEC-3: dos feriados del mismo pais en fechas distintas no se deduplican entre si",
        [sol(1, **ex)], EXT + SEM + cfg56(1, 2) + [feriado("2026-03-03", 56), feriado("2026-03-04", 56)])
    fnd = clonar_serie("FMFND1", "50", fondo={"IdPaisEmisor": 56, "IdPaisOrigen": 56, "IdMoneda": "USD", "FondoExtranjero": "N"})
    fnd += clonar_serie("FMFND3", "52", fondo={"IdPaisEmisor": 56, "IdPaisOrigen": 56, "IdMoneda": "USD", "FondoExtranjero": "N", "MoneyMarket": "S"})
    fnd += clonar_serie("FMFND4", "53", fondo={"IdPaisEmisor": 56, "IdPaisOrigen": 56, "IdMoneda": "USD", "IdTipoFondo": "NOEXISTE"})
    s0 = dict(pe=56, pf=56, pm=840, ef="C", el="C", FondoNacionalDolar="S")
    fec("FM-FEC-12f_fnd-campos-nulos", "FM-FEC-12/04: fondo nacional dolar con estados, batch, fondo extranjero, fechas de origen y spot nulos y fila de configuracion sin origen: la copia de ProcesamientoFechas rellena los nulos",
        [sol(1, serie="FMFND4", tipo="APO", **dict(s0, ef=None, el=None, batch=None, fext=None, FechaCertificacionRescateOrigen=None, FechaLiquidacionRescateOrigen=None,
                                                  FechaValorCuotaRescateOrigen=None, RescateGeneraSpotCertificacion=None))],
        fnd + SEM + [feriado("2026-03-03", 840)] + cfg_borrar() + [cfg(t, d, origen=None) for t, d in (("DIGITA", 0), ("CURSE", 0), ("VCUOTA", 0), ("CERTIF", 1), ("LIQUID", 2))]
        + parametro("MODALIDAD_SOLICITUD_FECHA_APORTE_RESCATE", "UNITARIO") + parametro(PERMITE, "S"))
    fec("FM-FEC-12g_fnd-fondo-extranjero", "FM-FEC-12: fondo nacional dolar con pais del fondo y emisor distintos de la empresa (filtros de feriados)",
        [sol(1, serie="FMFND1", tipo="APO", **dict(s0, pf=840, pe=76))], fnd + SEM + fin_de_semana(76) + [feriado("2026-03-03", 76), feriado("2026-03-04", 840)] + cfg56(1, 2) + parametro(PERMITE, "N"))
    for nombre, pais in (("h_usa-posterior", 840), ("i_chile-posterior", 56)):
        fec("FM-FEC-12%s" % nombre, "FM-FEC-12: valor cuota de fondo nacional dolar: gana la fecha mayor entre el dia anterior habil de USA y del fondo (feriado en %d)" % pais,
            [sol(1, serie="FMFND3", tipo="APO", emision="2026-03-02", **s0)], fnd + SEM + [feriado("2026-02-27", pais)] + cfg56(1, 2) + parametro(PERMITE, "S"))
    fec("FM-FEC-12j_fnd-aporte-lunes-sin-permiso", "FM-FEC-12: aporte de un lunes, la serie money market (fondo) sin permiso del parametro: repaso de cuota por fondo nacional dolar no money market",
        [sol(1, serie="FMFND3", tipo="APO", emision="2026-03-02", **s0)], fnd + SEM + cfg56(1, 2) + parametro(PERMITE, "N"))
    fec("FM-FEC-12k_fnd-rescate-rf-martes", "FM-FEC-12: rescate RF un martes con feriados (la consulta de feriados se excluye)",
        [sol(1, serie="FMFND1", tipo="RES", emision="2026-03-03", IdMercadoFondoMutuo="RF", **s0)], fnd + SEM + [feriado("2026-03-04", 840)] + cfg56(1, 2) + parametro(PERMITE, "S"))


def generar_cuo():
    pe = paridad(t1=[{"IdInterno": 1, "FactorCambioLocal": 0.9, "FactorOrigenDestino": 0.9, "MontoDestino": 140.5}])
    for modo, extra in (("M", dict(monto=150000)), ("C", dict(monto=0, cuotas=10))):
        cuo("FM-CUO-05l_fondo-clp-inversion-eur-%s" % modo.lower(), "FM-CUO-05: fondo en moneda sistema e inversion en EUR (no USD): usa CalcularParidad", [ser(1, modo=modo, mi="EUR", **extra)],
            EUR, conjunto(precios((1, 1500.1234)), pe))
    cuo("FM-CUO-05m_fondo-clp-inversion-eur-sin-paridad", "FM-CUO-05: igual sin resultado de paridad", [ser(1, mi="EUR", monto=1000)], EUR, precios((1, 1500.1234)))
    cuo("FM-CUO-05n_lista-de-paridad-nula", "FM-CUO-05: CalcularParidad devuelve p_out_CalculosSalida nulo => Exception 'List must be not null'", [ser(1, mi="EUR", monto=1000)], EUR,
        conjunto(precios((1, 1500.1234)), {"CalcularParidad": {"defecto": {"p_out_CalculosSalida": None}}}))
    cuo("FM-CUO-05o_precio-de-otro-registro", "FM-CUO-05: hay precios pero no para el IdInterno de la solicitud: valor cuota 0", [ser(1, modo="C", monto=1000, cuotas=2)], None, precios((9, 100)))
    cuo("FM-CUO-08e_factores-nulos-explicitos", "FM-CUO-08: FactorOrigenDestino y MontoDestino nulos en la respuesta de paridad valen 0",
        [ser(1, mf="CLP", mi="CLP", monto=1000, tfa="USD")], None, conjunto(precios((1, 1500.1234)), paridad(t2=[{"IdInterno": 6, "MontoDestino": None}, {"IdInterno": 7, "FactorOrigenDestino": None, "MontoDestino": 3}])))
    cuo("FM-CUO-07f_valor-cuota-cero-escala-modo-c", "FM-CUO-07 D-CUO-1: modo C, cuotas 0 y precio 0.0000 (escala 4): no se omite y falla la division", [ser(1, modo="C", monto=1000, cuotas=0)], None,
        precios((1, 0.0000)))


def generar_com():
    P = PCOM
    com("FM-COM-02n_cantidad-negativa", "FM-COM-02: cantidad negativa: el primer lote aporta la cantidad y corta", [cu(cant=-5)], TRAMOS + SCOM, conjunto(custodia(lote(100)), P))
    L = custodia(lote(100, "2026-02-10", "2026-02-20"))
    for nombre, kw in (("a_cliente", dict(cli=101)), ("b_portafolio", dict(port=501)), ("c_instrumento", dict(inst=901))):
        lotes = custodia(lote(100, "2026-02-10", "2026-02-20"), lote(100, "2026-02-11", "2026-02-21", cli=kw.get("cli", 100), port=kw.get("port", 500), inst=kw.get("inst", 900)))
        com("FM-COM-02o%s_dos-solicitudes-distinto-%s" % (nombre[0], nombre[2:]), "FM-COM-02/04: dos solicitudes que difieren en %s: cada una solo usa sus lotes" % nombre[2:],
            [cu(1), cu(2, **kw)], TRAMOS + SCOM, conjunto(lotes, P))
    com("FM-COM-04r_mezcla-de-tramos-y-series", "FM-COM-04: tramos ENTRADA, SALIDA, PER, de otra serie y en distinto orden sobre las mismas solicitudes (aporte y rescate)",
        [cu(1, tipo="APO"), cu(2, tipo="RES")], TRAMOS + SCOM + PER + tramo(14, "FM002U", "ENTRADA", 0, 9999, 4.0) + tramo(15, "FM002U", "PER", 0, 9999, 4.0) + [comision_maxima("FM002U")],
        conjunto(L, precios((1, 1000), (2, 1000))))
    com("FM-COM-03f_precio-sin-instrumento", "FM-COM-03: el precio sin IdInstrumento se descarta", [cu()], TRAMOS + SCOM, conjunto(L, precios((1, 2000, None))))
    com("FM-COM-06i_lista-de-paridad-nula", "FM-COM-06: CalcularParidad devuelve p_out_CalculosSalida nulo => Exception 'List must be not null'", [cu()], TRAMOS + SCOM,
        conjunto(L, P, {"CalcularParidad": {"defecto": {"p_out_CalculosSalida": None}}}))


def formato(cid, ref, valor, tipo, moneda, ti=None, to=None, seed=None):
    e = {"valor": valor, "tipo": tipo, "moneda": moneda}
    if ti is not None:
        e["idTipoInstrumento"] = ti
    if to is not None:
        e["idTipoOperacion"] = to
    caso(cid, "MonedaFormato", ref, e, seed)


def generar_formato():
    fmt = '"BSCL.Plataforma.BD::PLA.MonedaFormato"'
    def fila(i, mon, ti, to, uso="GEN", **d):
        v = {"IdMonedaFormato": 300 + i, "IdMoneda": mon, "IdUsoMonedaFormato": uso, "IdTipoInstrumento": ti, "IdTipoOperacion": to, "PrecioDecimales": 2, "PrecioTrucaRedondea": "T",
             "TasaDecimales": 3, "TasaTrucaRedondea": "T", "CantidadDecimales": 1, "CantidadTrucaRedondea": "R", "MontoDecimales": 0, "MontoTrucaRedondea": "T", "TipoCambioDecimales": 5,
             "TipoCambioTrucaRedondea": "T", "ParidadDecimales": 6, "ParidadTrucaRedondea": "R", "PuntosDecimales": 1, "PuntosTrucaRedondea": "T", "IdEstado": "ACT"}
        v.update(d)
        return insert(fmt, v)
    seed = [fila(1, "JPY", "", ""), fila(2, "USD", "FM", "", MontoDecimales=3, MontoTrucaRedondea="T"), fila(3, "USD", "FM", "APORFM", MontoDecimales=4, MontoTrucaRedondea="R"),
            fila(4, "CHF", "", "", uso="OTR"), fila(5, "NOK", "", "", MontoTrucaRedondea="X")]
    v = 1234.56789
    for t in ("MONTO", "CANTIDAD", "PRECIO", "TASA", "PARIDAD", "TIPOCAMBIO", "PUNTOS"):
        formato("FM-CUO-11a_formato-%s" % t.lower(), "FM-CUO-05/06: MonedaFormato por tipo de valor %s (decimales y trunca/redondea de la fila GEN de JPY)" % t, v, t, "JPY", seed=seed)
    formato("FM-CUO-11b_formato-tipo-en-minusculas", "FM-CUO-05/06: el tipo de valor ignora mayusculas", v, "monto", "JPY", seed=seed)
    formato("FM-CUO-11c_formato-tipo-invalido", "FM-CUO-05/06: tipo de valor desconocido => Exception", v, "XYZ", "JPY", seed=seed)
    formato("FM-CUO-11d_formato-sin-moneda", "FM-CUO-05/06: moneda nula => Exception 'debe especificar moneda'", v, "MONTO", None, seed=seed)
    formato("FM-CUO-11e_formato-no-encontrado", "FM-CUO-05/06: moneda sin fila de uso GEN => Exception", v, "MONTO", "CHF", seed=seed)
    formato("FM-CUO-11f_formato-trunca-invalido", "FM-CUO-05/06: codigo de trunca/redondea distinto de R y T => Exception", v, "MONTO", "NOK", seed=seed)
    formato("FM-CUO-11g_formato-valor-nulo", "FM-CUO-05/06: valor nulo vale 0", None, "MONTO", "JPY", seed=seed)
    formato("FM-CUO-11h_formato-negativo-trunca-hacia-abajo", "FM-CUO-05/06: trunca (T) con FLOOR: -1234.5 => -1235", -1234.5, "MONTO", "JPY", seed=seed)
    formato("FM-CUO-11i_formato-por-instrumento", "FM-CUO-05/06: fila especifica por tipo de instrumento", v, "MONTO", "USD", ti="FM", to="", seed=seed)
    formato("FM-CUO-11j_formato-por-instrumento-y-operacion", "FM-CUO-05/06: fila especifica por tipo de instrumento y de operacion", v, "MONTO", "USD", ti="FM", to="APORFM", seed=seed)
    formato("FM-CUO-11k_formato-instrumento-sin-fila-cae-a-general", "FM-CUO-05/06: sin fila para el instrumento usa la general de la moneda", v, "MONTO", "USD", ti="ZZZ", to="ZZZ", seed=seed)


def generar():
    generar_fec()
    generar_cuo()
    generar_com()
    generar_formato()
