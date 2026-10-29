"""FM-FEC cases, part 3 (rules 10-12): re-pass for foreign series, re-pass of the certification of a spot-generating redemption, national-dollar funds."""
from cases3a_lib import *
from cases3a_fec1 import sol, fec, PERMITE

EXT = clonar_serie("FMEXT1", "40", fondo={"IdPaisEmisor": 840, "IdPaisOrigen": 840, "FondoExtranjero": "S", "IdMoneda": "USD"})
SEM = fin_de_semana(56, 840, 152)


def cfg56(cert, liq, vcuota=0):
    return cfg_borrar() + cfg_completa(dias={"CERTIF": cert, "LIQUID": liq, "VCUOTA": vcuota})


def generar():
    e = dict(serie="FMEXT1", pe=840, pf=840, pm=840, fext="S")
    base = EXT + SEM + cfg56(1, 2)
    fec("FM-FEC-10a_certificacion-en-feriado-extranjero", "FM-FEC-10: la certificacion cae en feriado del fondo/emisor => 1 dia habil siguiente en todos los paises (D-FEC-5)",
        [sol(1, **e)], base + [feriado("2026-03-03", 840)])
    fec("FM-FEC-10b_liquidacion-en-feriado-de-moneda", "FM-FEC-10: la liquidacion cae en feriado de la moneda => se recalcula con los 4 paises",
        [sol(1, **dict(e, pm=152))], base + [feriado("2026-03-04", 152)])
    fec("FM-FEC-10c_sin-feriados-no-consulta", "FM-FEC-10: serie extranjera sin fechas en feriado => no hay recalculo", [sol(1, **e)], base)
    fec("FM-FEC-10d_solo-fondo-extranjero-no-aplica", "FM-FEC-10: el resultado solo se aplica si el emisor de la serie no es el pais de la empresa",
        [sol(1, pf=840, pe=56, pm=56)], SEM + cfg56(1, 2) + [feriado("2026-03-03", 840)])
    fec("FM-FEC-10e_feriado-duplicado-pais-chico", "FM-FEC-10 D-FEC-3: feriados repetidos del pais 56 (Long cacheado) se deduplican con ==",
        [sol(1, **e)], base + [feriado("2026-03-03", 56), feriado("2026-03-03", 56)])
    fec("FM-FEC-10f_feriado-duplicado-pais-grande", "FM-FEC-10 D-FEC-3: feriados repetidos del pais 840 (Long no cacheado) no se deduplican => IllegalStateException de toMap",
        [sol(1, **e)], base + [feriado("2026-03-03", 840), feriado("2026-03-03", 840)])
    fec("FM-FEC-10g_mezcla-nacional-y-extranjera", "FM-FEC-10: solo las solicitudes de serie extranjera entran al recalculo",
        [sol(1), sol(2, **e)], base + [feriado("2026-03-03", 840)])
    s = dict(e, tipo="RES", RescateGeneraSpotCertificacion="S")
    c = lambda a, b: EXT + SEM + cfg56(a, b)
    fec("FM-FEC-11a_spot-certificacion-antes-de-liquidacion", "FM-FEC-11: certificacion anterior al dia habil previo a la liquidacion y emision+2 dias habiles alcanza => ese dia",
        [sol(1, **s)], c(1, 3))
    fec("FM-FEC-11b_spot-certificacion-igual-liquidacion", "FM-FEC-11: certificacion igual a liquidacion y emision+2 no alcanza => la liquidacion", [sol(1, **s)], c(1, 1))
    fec("FM-FEC-11c_spot-certificacion-posterior", "FM-FEC-11: certificacion posterior al dia previo a la liquidacion => sin cambio", [sol(1, **s)], c(4, 3))
    fec("FM-FEC-11d_sin-spot", "FM-FEC-11: RescateGeneraSpotCertificacion=N no entra", [sol(1, **dict(s, RescateGeneraSpotCertificacion="N"))], c(1, 3))
    fec("FM-FEC-11e_spot-serie-nacional", "FM-FEC-11: serie nacional (pais fondo = empresa y emisor = empresa) no entra", [sol(1, RescateGeneraSpotCertificacion="S")], c(1, 3))
    fec("FM-FEC-11f_dos-solicitudes-spot", "FM-FEC-11: varias solicitudes, una con spot y otra sin", [sol(1, **s), sol(2, **dict(s, RescateGeneraSpotCertificacion="N"))], c(1, 3))
    fnd = clonar_serie("FMFND1", "50", fondo={"IdPaisEmisor": 56, "IdPaisOrigen": 56, "IdMoneda": "USD", "FondoExtranjero": "N"})
    fnd += clonar_serie("FMFND2", "51", fondo={"IdPaisEmisor": 56, "IdPaisOrigen": 56, "IdMoneda": "USD", "FondoExtranjero": "N", "MoneyMarket": "S", "IdTipoFondo": "MMK"})
    fnd.insert(0, tipo_fondo("MMK", "S"))
    hol = [feriado("2026-03-03", 840), feriado("2026-03-04", 840), feriado("2026-03-05", 56), feriado("2026-03-09", 840)]
    n = 0
    for tipo in ("APO", "RES"):
        for merc in ("", "RF"):
            for mm in (False, True):
                for par in ("N", "S"):
                    n += 1
                    fec("FM-FEC-12a%02d_fnd-%s-%s-%s-param%s" % (n, tipo.lower(), merc.lower() or "norf", "mm" if mm else "nomm", par),
                        "FM-FEC-12: fondo nacional dolar %s mercado '%s' %s parametro=%s (lunes: diaHabil/soloCaeDiaHabil de ObtenerDiaAnterior)" % (tipo, merc, "money market" if mm else "no money market", par),
                        [sol(1, serie="FMFND2" if mm else "FMFND1", tipo=tipo, pe=56, pf=56, pm=840, ef="C", el="C", FondoNacionalDolar="S", IdMercadoFondoMutuo=merc, emision="2026-03-02")],
                        fnd + SEM + hol + cfg56(1, 2, 0) + parametro(PERMITE, par))
    for tipo in ("APO", "RES"):
        for mm in (False, True):
            fec("FM-FEC-12b_fnd-%s-%s-martes" % (tipo.lower(), "mm" if mm else "nomm"), "FM-FEC-12: fondo nacional dolar un martes (dia no lunes)",
                [sol(1, serie="FMFND2" if mm else "FMFND1", tipo=tipo, pe=56, pf=56, pm=840, ef="C", el="C", FondoNacionalDolar="S", emision="2026-03-03")],
                fnd + SEM + hol + cfg56(1, 2, 0) + parametro(PERMITE, "S"))
    fec("FM-FEC-12c_fnd-sin-feriados", "FM-FEC-12: fondo nacional dolar sin feriados en las fechas (no hay recalculo de curse/certificacion/liquidacion)",
        [sol(1, serie="FMFND1", pe=56, pf=56, pm=840, ef="C", el="C", FondoNacionalDolar="S", emision="2026-03-02")], fnd + SEM + cfg56(1, 2, 0) + parametro(PERMITE, "S"))
    fec("FM-FEC-12d_fnd-feriado-duplicado", "FM-FEC-12 D-FEC-3: feriados repetidos sin deduplicar => IllegalStateException de toMap",
        [sol(1, serie="FMFND1", pe=56, pf=56, pm=840, ef="C", el="C", FondoNacionalDolar="S")], fnd + SEM + [feriado("2026-03-03", 840), feriado("2026-03-03", 840)] + cfg56(1, 2, 0))
    fec("FM-FEC-12e_fnd-curse-en-feriado", "FM-FEC-12: el curse cae en feriado de la moneda => se recalcula con ObtenerDiaHabilMultiplesPaises",
        [sol(1, serie="FMFND1", pe=56, pf=56, pm=840, ef="C", el="C", FondoNacionalDolar="S", emision="2026-03-02", tipo="APO")],
        fnd + SEM + [feriado("2026-03-02", 840)] + cfg56(1, 2, 0) + parametro(PERMITE, "N"))
