"""ConsultarFlujoCustodiaFondoMutuo cases (FM-REQ-24, FM-REQ-25, D-REQ-9, D-REQ-10)."""
from cases3b_lib import caso
from cases3b_rows import fila, SOLICITUD
from cases3b_seeds import serie, serie_en_moneda, formato_moneda

S = "ConsultarFlujoCustodiaFondoMutuo"


def ent(instrumentos, carteras, fecha="2026-03-02"):
    return {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_IdPais": 56, "p_Instrumentos": instrumentos, "p_Carteras": carteras,
            "p_FechaSaldo": fecha, "p_DiasProyeccion": 0}


def ins(i, regimen="GEN"):
    return {"IdInstrumento": i, "IdRegimenFondoMutuo": regimen}


def car(c=100, p=500):
    return {"IdCliente": c, "IdPortafolio": p}


def cuota(*res):
    return {"CalcularMontoNumeroCuota": {"defecto": {"P_OUT_RESULTADO": [dict(IdInterno=i, Monto=m, TipoSolicitud="RES") for i, m in res]}}}


def flujo(*tot):
    return {"ConsultarFlujoCustodia": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "p_out_FlujoTotalizado": [
        {"IdCliente": c, "IdPortafolio": p, "IdInstrumento": i, "IdRegimenFondoMutuo": r, "Cantidad": q, "MontoAMercadoMonedaInstrumento": m}
        for (c, p, i, r, q, m) in tot]}}}


def merge(*ds):
    out = {}
    for d in ds:
        out.update(d)
    return out


def cases():
    yield caso("REQ-24-01_flujo-simple", S, "FM-REQ-24: una cartera, un instrumento: cantidad del flujo y monto valorizado por CalcularMontoNumeroCuota",
               ent([ins(900)], [car()]), colaboradores=cuota((1, 1500123.4)))
    yield caso("REQ-24-02_regimen-vacio-toma-el-primero", S, "FM-REQ-25 / D-REQ-9: regimen vacio coincide con el primer flujo; regimen DC-R con el suyo",
               ent([ins(900, ""), ins(900, "DC-R")], [car()]),
               colaboradores=merge(flujo((100, 500, 900, "GEN", 10, 100), (100, 500, 900, "DC-R", 20, 200)), cuota((1, 15), (2, 30))))
    yield caso("REQ-24-03_rescate-total-en-transito", S, "FM-REQ-24 / D-REQ-10: solicitud ING por total en transito deja cantidad y montos en 0, RescateTotal=S",
               ent([ins(900)], [car()]),
               [fila(SOLICITUD, IdSolicitudAporteRescate=1, IdCliente=100, IdPortafolio=500, IdSerieFondo="FM001A", IdEmpresa=1, Estado="ING",
                     CalculoPorMontoCuota="T", FolioOmnibus=0, IdRegimenTributario="GEN")],
               colaboradores=cuota())
    yield caso("REQ-24-04_total-en-transito-otro-regimen", S, "FM-REQ-24: el total en transito de otro regimen no cuenta",
               ent([ins(900, "DC-R")], [car()]),
               [fila(SOLICITUD, IdSolicitudAporteRescate=1, IdCliente=100, IdPortafolio=500, IdSerieFondo="FM001A", IdEmpresa=1, Estado="ING",
                     CalculoPorMontoCuota="T", FolioOmnibus=0, IdRegimenTributario="GEN")],
               colaboradores=merge(flujo((100, 500, 900, "DC-R", 5, 50)), cuota((1, 7))))
    yield caso("REQ-24-05_total-omnibus-o-estado-distinto-no-cuenta", S, "FM-REQ-24: solicitud en estado distinto de ING, o con folio omnibus, no cuenta",
               ent([ins(900)], [car()]),
               [fila(SOLICITUD, IdSolicitudAporteRescate=1, IdCliente=100, IdPortafolio=500, IdSerieFondo="FM001A", IdEmpresa=1, Estado="PEN",
                     CalculoPorMontoCuota="T", FolioOmnibus=0, IdRegimenTributario="GEN"),
                fila(SOLICITUD, IdSolicitudAporteRescate=2, IdCliente=100, IdPortafolio=500, IdSerieFondo="FM001A", IdEmpresa=1, Estado="ING",
                     CalculoPorMontoCuota="T", FolioOmnibus=9, IdRegimenTributario="GEN")],
               colaboradores=cuota((1, 1)))
    yield caso("REQ-24-06_sin-flujo", S, "FM-REQ-24: la custodia no trae el instrumento: cantidad 0, sin series a valorizar (la valorizacion igual se invoca)",
               ent([ins(900)], [car()]), colaboradores=merge(flujo(), cuota()))
    yield caso("REQ-24-07_producto-cartesiano", S, "FM-REQ-24: 2 carteras x 2 instrumentos: IdInterno 1..4 en orden cartera-instrumento",
               ent([ins(900), ins(901)], [car(100, 500), car(100, 501)]),
               [serie(IdSerieFondo="FM002A", IdInstrumento=901)],
               colaboradores=merge(flujo((100, 500, 900, "GEN", 1, 10), (100, 501, 901, "GEN", 2, 20)), cuota((1, 11), (4, 44))))
    yield caso("REQ-24-08_instrumento-sin-serie-sin-formato", S, "FM-REQ-24: instrumento sin serie => moneda '' => MonedaFormato no encuentra formato: excepcion",
               ent([ins(900), ins(555)], [car()]), colaboradores=cuota())
    yield caso("REQ-24-09_instrumento-cero-sin-filtro", S, "FM-REQ-24: IdInstrumento 0 viaja sin instrumento al filtro de custodia (null)",
               ent([ins(0)], [car()]), colaboradores=cuota())
    yield caso("REQ-24-10_resultado-de-cuota-ausente", S, "FM-REQ-24: sin resultado de valorizacion el MontoDisponible queda en 0",
               ent([ins(900)], [car()]), colaboradores=cuota((99, 5)))
    yield caso("REQ-24-11_serie-duplicada-por-instrumento", S, "FM-REQ-24: dos series del mismo instrumento: toMap falla (IllegalStateException)",
               ent([ins(900)], [car()]), [serie(IdSerieFondo="FM001B", IdInstrumento=900)], colaboradores=cuota())
    yield caso("REQ-24-12_custodia-falla", S, "FM-REQ-24: ConsultarFlujoCustodia falla: sale la excepcion",
               ent([ins(900)], [car()]), colaboradores={"ConsultarFlujoCustodia": {"respuestas": [{"error": "custodia caida"}]}})
    yield caso("REQ-24-13_instrumentos-repetidos", S, "FM-REQ-24: el mismo instrumento/regimen dos veces: un solo filtro a custodia, dos filas de salida",
               ent([ins(900), ins(900)], [car()]), colaboradores=cuota((1, 1), (2, 2)))
    yield caso("REQ-24-14_redondeo-del-monto-de-custodia", S, "FM-REQ-24: MontoDisponibleCustodia se formatea con MonedaFormato (decimales/redondeo de la moneda)",
               ent([ins(900)], [car()]), colaboradores=merge(flujo((100, 500, 900, "GEN", 3, 1500123.456789)), cuota((1, 2))))
    yield caso("REQ-24-15_sin-carteras", S, "FM-REQ-24: sin carteras: no hay filas, la custodia y la valorizacion se invocan igual",
               ent([ins(900)], []), colaboradores=cuota())
    yield caso("REQ-24-16_instrumento-nulo", S, "FM-REQ-24: IdInstrumento ausente: NullPointerException al armar los filtros",
               ent([{"IdRegimenFondoMutuo": "GEN"}], [car()]), colaboradores=cuota())
    for n, moneda, ti, to, dec, tr, nota in [
            (18, "EUR", "", "", 2, "T", "MonedaFormato T: trunca a 2 decimales (FLOOR)"),
            (19, "AAA", "", "OP", 0, "R", "MonedaFormato: sin fila exacta, usa la fila con el mismo tipo de instrumento ('')"),
            (20, "BBB", "FM", "OP", 1, "R", "MonedaFormato: solo coincide la moneda (tipo de instrumento distinto)"),
            (21, "CCC", "", "", 2, "X", "MonedaFormato: TrucaRedondea invalido => Exception")]:
        yield caso("REQ-24-%d_formato-%s" % (n, moneda.lower()), S, "FM-REQ-24: " + nota,
                   ent([ins(902 + n)], [car()]),
                   serie_en_moneda("FX%dA" % n, 902 + n, str(40 + n), moneda) + [formato_moneda(10 + n, moneda, ti, to, dec, tr)],
                   colaboradores=merge(flujo((100, 500, 902 + n, "GEN", 3, 1500123.456789)), cuota((1, 2))))
    yield caso("REQ-24-17_cartera-nula", S, "FM-REQ-24: IdCliente ausente en la cartera: NullPointerException al comparar el flujo",
               ent([ins(900)], [{"IdPortafolio": 500}]), colaboradores=cuota())
