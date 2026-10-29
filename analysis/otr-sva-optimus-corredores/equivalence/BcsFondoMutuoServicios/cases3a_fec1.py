"""FM-FEC cases, part 1 (rules 01-07): preparation, Switch, money-market flag, configuration selection, valuta, calendar country, calculation order."""
from cases3a_lib import *

WE56 = fin_de_semana(56)
PERMITE = "PERMITE_DIA_NO_HABIL_FECHA_VALOR_CUOTA_MONEY_MARKET"
MODALIDAD = "MODALIDAD_SOLICITUD_FECHA_APORTE_RESCATE"


def sol(i=1, serie="FM001A", emision="2026-03-02", ef="A", el="A", ret=0, vvc=0, vcert=1, vcurse=0, batch="N", tipo="RES", fext="N", pf=56, pe=56, pm=56, **x):
    d = {"IdInterno": i, "IdSerieFondo": serie, "FechaEmision": emision, "EstadoFondo": ef, "EstadoLiquidacion": el, "NumeroDiasRetencion": ret,
         "NumeroValutaValorCuota": vvc, "NumeroValutaCertificacion": vcert, "NumeroValutaCurse": vcurse, "SolicitudBatch": batch, "TipoSolicitud": tipo,
         "FondoExtranjero": fext, "IdPaisFondo": pf, "IdPaisEmisor": pe, "IdPaisMoneda": pm}
    d.update(x)
    return d


def fec(cid, ref, sols, seed=None):
    caso(cid, "CalcularFechaSolicitudAporteRescate", ref, {"p_IdEmpresa": 1, "p_IdPais": 56, "p_SolicitudAporteRescate": sols}, seed)


def s_mm(param="S"):
    """money-market series FMMM01, RF series FMRF01, plain series FM001A; PERMITE_DIA_NO_HABIL = param."""
    return (clonar_serie("FMMM01", "30", fondo={"MoneyMarket": "S"}) + clonar_serie("FMRF01", "31", fondo={"IdMercadoFondoMutuo": "RF"})
            + parametro(PERMITE, param) + WE56)


def generar():
    fec("FM-FEC-01a_nulos-a-cero", "FM-FEC-01: valutas, retencion y paises nulos valen 0; IdPaisLocal := IdPaisEmisor (D-FEC-1)",
        [sol(1, ret=None, vvc=None, vcert=None, vcurse=None, pf=None, pe=None, pm=None)], WE56)
    fec("FM-FEC-01b_parametro-dia-no-habil-ausente", "FM-FEC-01/03: sin el parametro PERMITE_DIA_NO_HABIL... el legacy usa N", [sol(1, tipo="APO")],
        parametro(PERMITE, None) + WE56)
    fec("FM-FEC-01c_serie-inexistente", "FM-FEC-01/03 D-FEC-2: la serie no existe => NullPointerException", [sol(1, serie="NOEXISTE")])
    fec("FM-FEC-01d_serie-sin-fondo", "FM-FEC-01 D-FEC-2: serie cuyo fondo no existe => IdPaisEmisor nulo se desempaqueta (NullPointerException)",
        [sol(1, serie="SINFONDO")], clonar_serie("SINFONDO", "99", fondo=False))
    fec("FM-FEC-01e_fecha-emision-invalida", "FM-FEC-07: FechaEmision vacia => DateTimeParseException al calcular", [sol(1, emision="")], WE56)
    fec("FM-FEC-01f_fecha-emision-con-hora", "FM-FEC-07/13: FechaEmision de 19 caracteres (yyyy-MM-dd HH:mm:ss) se lee como fecha", [sol(1, emision="2026-03-05 10:20:30")], WE56)
    fec("FM-FEC-02a_switch-origen", "FM-FEC-02: Switch=S toma las fechas del rescate de origen; primero van los Switch y despues los demas",
        [sol(1), sol(2, Switch="S", FechaCertificacionRescateOrigen="2026-03-04", FechaLiquidacionRescateOrigen="2026-03-05",
                     FechaValorCuotaRescateOrigen="2026-03-03")], WE56)
    fec("FM-FEC-02b_switch-invalido-se-pierde", "FM-FEC-02 D-FEC-2: Switch distinto de S/N o nulo => la solicitud no sale en el resultado",
        [sol(1, Switch="X"), sol(2, Switch=None), sol(3)], WE56)
    fec("FM-FEC-02c_solo-switch-sin-configuracion", "FM-FEC-02: solo Switch=S no lee la configuracion de fechas", [sol(1, Switch="S")], cfg_borrar())
    fec("FM-FEC-03a_tipo-solicitud-invalido", "FM-FEC-03: tipo distinto de APO/RES => Exception 'Tipo Solicitud no valida.'", [sol(1, tipo="OTR")], WE56)
    d = "2026-03-02"
    fec("FM-FEC-03b_aporte-money-market-permite", "FM-FEC-03/09: aporte de serie money market con el parametro en S: valor cuota = curse - 1 dia",
        [sol(1, serie="FMMM01", tipo="APO", emision="2026-03-03")], s_mm("S"))
    fec("FM-FEC-03c_rescate-money-market-permite", "FM-FEC-03/09: rescate money market con el parametro en S: valor cuota = certificacion - 1 dia",
        [sol(1, serie="FMMM01", emision="2026-03-03")], s_mm("S"))
    fec("FM-FEC-03d_rescate-renta-fija-permite", "FM-FEC-03: rescate de mercado RF cuenta como money market", [sol(1, serie="FMRF01", emision="2026-03-03")], s_mm("S"))
    fec("FM-FEC-03e_aporte-renta-fija-no", "FM-FEC-03: el aporte solo mira la bandera money market (RF no)", [sol(1, serie="FMRF01", tipo="APO")], s_mm("S"))
    fec("FM-FEC-03f_money-market-con-parametro-n", "FM-FEC-03: money market con el parametro en N no permite dia no habil",
        [sol(1, serie="FMMM01", tipo="APO"), sol(2, serie="FMMM01")], s_mm("N"))
    base = cfg_borrar() + cfg_completa() + cfg_completa("BATCH", dias={"CERTIF": 3, "LIQUID": 3})
    fec("FM-FEC-04a_modalidad-nula-batch-s", "FM-FEC-04: parametro de modalidad vacio: SolicitudBatch=S usa las filas BATCH, N las UNITARIO",
        [sol(1, batch="S"), sol(2, batch="N")], base + WE56)
    fec("FM-FEC-04b_modalidad-por-parametro", "FM-FEC-04: parametro BATCH: filas BATCH aunque SolicitudBatch sea N", [sol(1, batch="N")],
        base + parametro(MODALIDAD, "BATCH") + WE56)
    fec("FM-FEC-04c_modalidad-sin-filas", "FM-FEC-04 D-FEC-2: parametro sin filas => falta configuracion => NullPointerException", [sol(1)],
        base + parametro(MODALIDAD, "ZZZ") + WE56)
    fec("FM-FEC-04d_modalidad-sin-parametro", "FM-FEC-04: parametro ausente: el legacy usa vacio (UNITARIO / BATCH segun SolicitudBatch)", [sol(1, batch="S")],
        base + parametro(MODALIDAD, None) + WE56)
    fec("FM-FEC-04e_filtros-tipo-estado-extranjero", "FM-FEC-04: las filas con TipoSolicitud/EstadoFondo/FondoExtranjero solo aplican si coinciden (ignora mayusculas)",
        [sol(1, tipo="APO"), sol(2, tipo="RES", ef="C"), sol(3, tipo="RES", fext="S"), sol(4, tipo="APO", ef="c", fext="s")],
        cfg_borrar() + cfg_completa() + [cfg("CERTIF", 3, origen="CURSE", tipo_sol="apo"), cfg("LIQUID", 4, origen="CURSE", estado="C"),
                                          cfg("CERTIF", 2, origen="CURSE", fext="S", tipo_sol="RES")] + WE56)
    fec("FM-FEC-04f_ultima-fila-pisa", "FM-FEC-04 D-FEC-2: varias filas validas del mismo tipo de fecha => la ultima pisa; las INA no cuentan",
        [sol(1)], cfg_borrar() + cfg_completa() + [cfg("CERTIF", 4, origen="CURSE"), cfg("CERTIF", 9, origen="CURSE", estado_reg="INA")] + WE56)
    fec("FM-FEC-04g_falta-tipo-de-fecha", "FM-FEC-04/07 D-FEC-2: sin fila CERTIF => NullPointerException al calcular",
        [sol(1)], cfg_borrar() + [cfg("DIGITA", 0), cfg("CURSE", 0, origen="DIGITA"), cfg("VCUOTA", 0, origen="CURSE"), cfg("LIQUID", 1, origen="CURSE")])
    fec("FM-FEC-04h_solicitud-batch-nulo", "FM-FEC-04: modalidad vacia y SolicitudBatch nulo => NullPointerException", [sol(1, batch=None)], WE56)
    flags = {t: dict(vcurse="S", vvc="S", vcert="S", vret="S") for t in ("DIGITA", "CURSE", "VCUOTA", "CERTIF", "LIQUID")}
    fec("FM-FEC-05a_valuta-total-todas", "FM-FEC-05: la valuta suma CantidadDias + curse + certificacion + retencion + valor cuota segun las banderas S",
        [sol(1, vcurse=1, vcert=2, ret=3, vvc=1)], cfg_borrar() + cfg_completa(dias={"DIGITA": 1, "CURSE": 0, "VCUOTA": 0, "CERTIF": 0, "LIQUID": 0}, **flags) + WE56)
    fec("FM-FEC-05b_valuta-solo-retencion", "FM-FEC-05: solo la fila LIQUID suma la retencion; las demas banderas en N",
        [sol(1, vcurse=5, vcert=5, ret=3, vvc=5)], cfg_borrar() + cfg_completa(dias={"CERTIF": 0, "LIQUID": 0}, LIQUID=dict(vret="S"), CERTIF=dict(vcert="S")) + WE56)
