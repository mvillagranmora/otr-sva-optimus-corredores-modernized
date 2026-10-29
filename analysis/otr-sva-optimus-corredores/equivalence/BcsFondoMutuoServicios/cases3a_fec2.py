"""FM-FEC cases, part 2 (rules 06-09): calendar country, calculation order, two calendars for certification/liquidation, quota-value date."""
from cases3a_lib import *
from cases3a_fec1 import sol, fec, PERMITE, s_mm

PAISES = fin_de_semana(56, 840, 76, 152) + [feriado("2026-03-03", 840), feriado("2026-03-04", 76), feriado("2026-03-05", 152)]
TODOS = ("DIGITA", "CURSE", "VCUOTA", "CERTIF", "LIQUID")


def cal(**f):
    """configuration of the five date types with the calendar flags f (cl, ce, cf, cm) on every row; certification 2 days, liquidation 3 (config days)."""
    return cfg_borrar() + cfg_completa(dias={"CERTIF": 2, "LIQUID": 3}, **{t: dict(f) for t in TODOS})


def generar():
    ext = dict(pe=840, pf=76, pm=152, ef="C", el="C", vcert=0)
    for nombre, flags in (("a_todos", dict(cl="S", ce="S", cf="S", cm="S")), ("b_solo-fondo", dict(cf="S")), ("c_solo-emisor", dict(ce="S")),
                          ("d_solo-local", dict(cl="S")), ("e_solo-moneda", dict(cm="S")), ("f_ninguno", {}),
                          ("h_emisor-y-fondo", dict(ce="S", cf="S")), ("i_emisor-fondo-moneda", dict(ce="S", cf="S", cm="S"))):
        fec("FM-FEC-06%s" % nombre, "FM-FEC-06: pais del calendario por banderas (%s): primera pasada emisor-fondo-local-moneda-empresa, segunda fondo-emisor-..." % nombre,
            [sol(1, **ext), sol(2, tipo="APO", **ext)], PAISES + cal(**flags))
    fec("FM-FEC-06g_paises-cero", "FM-FEC-06: con banderas S y paises 0 (nulos) el calendario cae al de la empresa",
        [sol(1, pe=None, pf=None, pm=None, ef="C", el="C")], PAISES + cal(cl="S", ce="S", cf="S", cm="S"))
    orden = cfg_borrar() + cfg_completa(dias={"DIGITA": 1, "CURSE": 2, "VCUOTA": 1, "CERTIF": 2, "LIQUID": 3})
    fec("FM-FEC-07a_orden-de-calculo", "FM-FEC-07: digitacion desde la emision, curse desde la digitacion, el resto desde el curse (viernes, fin de semana y feriado)",
        [sol(1, emision="2026-03-06", vcert=0)], fin_de_semana(56) + [feriado("2026-03-10", 56)] + orden)
    fec("FM-FEC-07b_falta-digitacion", "FM-FEC-07 D-FEC-2: sin fila DIGITA => NullPointerException", [sol(1)],
        cfg_borrar() + [cfg("CURSE", 0), cfg("VCUOTA", 0), cfg("CERTIF", 1), cfg("LIQUID", 1)])
    fec("FM-FEC-07c_falta-liquidacion", "FM-FEC-07 D-FEC-2: sin fila LIQUID => NullPointerException", [sol(1)],
        cfg_borrar() + [cfg("DIGITA", 0), cfg("CURSE", 0), cfg("VCUOTA", 0), cfg("CERTIF", 1)])
    # FM-FEC-08: certification / liquidation with two calendars (emisor 840 != fondo 76)
    dos = lambda **x: cfg_borrar() + cfg_completa(dias={"CERTIF": 0, "LIQUID": 0}, **{t: dict(ce="S", cf="S", **x.get(t, {})) for t in TODOS})
    v = dict(CERTIF=dict(vcert="S"), LIQUID=dict(vret="S"))
    sem = fin_de_semana(56, 840, 76)
    for nombre, hol, ef in (("a_serie-fondo-posterior", [feriado("2026-03-04", 76)], "A"), ("b_serie-emisor-posterior", [feriado("2026-03-04", 840)], "A"),
                            ("c_config-fondo-posterior", [feriado("2026-03-04", 76)], "C"), ("d_config-emisor-posterior", [feriado("2026-03-04", 840)], "C"),
                            ("e_mismo-dia", [], "A")):
        c = dos(**v) if ef == "A" else cfg_borrar() + cfg_completa(dias={"CERTIF": 2, "LIQUID": 3}, **{t: dict(ce="S", cf="S") for t in TODOS})
        fec("FM-FEC-08%s" % nombre, "FM-FEC-08: certificacion y liquidacion con calendario del emisor y del fondo: gana la fecha mayor (%s)" % nombre,
            [sol(1, pe=840, pf=76, pm=76, ef=ef, el=ef, vcert=2, ret=3)], sem + hol + c)
    fec("FM-FEC-08f_clave-no-coincide", "FM-FEC-08/10 D-FEC-4,10: valutas de la serie distintas a los dias de configuracion => la clave del mapa no existe => NullPointerException",
        [sol(1, pe=840, pf=76, pm=76, ef="A", el="A", vcert=2, ret=3)], sem + cfg_borrar() + cfg_completa(dias={"CERTIF": 1, "LIQUID": 1}, **{t: dict(ce="S", cf="S") for t in TODOS}))
    # FM-FEC-09: quota-value date
    pm = s_mm("S")
    fec("FM-FEC-09a_aporte-curse-menos-un-dia", "FM-FEC-09: PermiteDiaNoHabilFechaValorCuota: aporte => curse - 1 dia (lunes y martes)",
        [sol(1, serie="FMMM01", tipo="APO", emision="2026-03-02"), sol(2, serie="FMMM01", tipo="APO", emision="2026-03-03")], pm)
    fec("FM-FEC-09b_rescate-certificacion-menos-un-dia", "FM-FEC-09: PermiteDiaNoHabilFechaValorCuota: rescate => certificacion - 1 dia",
        [sol(1, serie="FMMM01", emision="2026-03-05", vcert=2)], pm)
    vc = lambda dias, **f: cfg_borrar() + cfg_completa(dias={"VCUOTA": dias}, **{"VCUOTA": dict(ce="S", cf="S", vvc="S", **f)})
    fec("FM-FEC-09c_emisor-distinto-fondo-mayor", "FM-FEC-09: sin permiso, emisor != fondo: gana la fecha mayor de los dos calendarios (emisor posterior)",
        [sol(1, pe=840, pf=76, pm=76, vvc=2)], sem + [feriado("2026-03-03", 840)] + vc(0))
    fec("FM-FEC-09d_emisor-distinto-fondo-menor", "FM-FEC-09: sin permiso, emisor != fondo (fondo posterior)",
        [sol(1, pe=840, pf=76, pm=76, vvc=2)], sem + [feriado("2026-03-03", 76)] + vc(0))
    fec("FM-FEC-09e_emisor-igual-fondo", "FM-FEC-09: sin permiso y mismo pais: calculo normal", [sol(1, vvc=1)], fin_de_semana(56) + vc(0))
    fec("FM-FEC-09f_clave-valor-cuota-no-coincide", "FM-FEC-09 D-FEC-10: NumeroValutaValorCuota distinto de los dias de configuracion con emisor != fondo => NullPointerException",
        [sol(1, pe=840, pf=76, pm=76, vvc=0)], sem + vc(1))
