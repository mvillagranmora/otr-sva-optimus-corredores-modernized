"""FM-FEC cases, part 4: the date helpers ObtenerDiaSiguiente / ObtenerDiaAnterior / ObtenerDiaHabilMultiplesPaises called directly (ids FM-FEC-Dnn):
the arithmetic that step 2 deferred. Real legacy classes record the goldens."""
from cases3a_lib import *

SEM = fin_de_semana(56, 840)
HOL = [feriado("2026-03-04", 56), feriado("2026-03-11", 840)]


def sig(cid, ref, fechas, seed=None, **top):
    e = {"p_idPais": 56, "p_diaHabil": "S", "p_soloCaeDiaHabil": "N", "p_consideraHabilAnterior": "N", "p_fechas": fechas}
    e.update(top)
    caso(cid, "ObtenerDiaSiguiente", ref, e, seed)


def ant(cid, ref, fechas, seed=None, **top):
    e = {"idPais": 56, "diaHabil": "S", "soloCaeDiaHabil": "N", "excluirConsultarDiaFeriado": False, "fechas": fechas}
    e.update(top)
    caso(cid, "ObtenerDiaAnterior", ref, e, seed)


def mul(cid, ref, fechas, seed=None, **top):
    e = {"p_diaHabil": "S", "p_soloCaeDiaHabil": "N", "p_fechas": fechas}
    e.update(top)
    caso(cid, "ObtenerDiaHabilMultiplesPaises", ref, e, seed)


def fs(f, n, pais=None, i=0):
    return {"fechaInicial": f, "numeroDias": n, "pais": pais, "IdInterno": i}


def fm(f, n, paises, i=0):
    return {"idInterno": i, "fechaInicial": f, "numeroDias": n, "paises": paises}


def generar():
    sig("FM-FEC-D01a_siguiente-dias-habiles", "ObtenerDiaSiguiente: suma dias habiles saltando fin de semana y feriados del pais de cada fecha (el pais nulo toma p_idPais)",
        [fs("2026-03-02", 3, None, 1), fs("2026-03-06", 2, 840, 2), fs("2026-03-10", 1, 840, 3)], SEM + HOL)
    sig("FM-FEC-D01b_siguiente-sin-dias", "ObtenerDiaSiguiente: numeroDias nulo o menor que 1 devuelve la fecha tal cual", [fs("2026-03-07", None, 56), fs("2026-03-07", 0, 56)], SEM)
    sig("FM-FEC-D01c_siguiente-solo-cae-dia-habil-adelante", "ObtenerDiaSiguiente: p_diaHabil=N y p_soloCaeDiaHabil=S mueve un sabado/feriado al siguiente dia habil",
        [fs("2026-03-07", 1, 56), fs("2026-03-04", 0, 56), fs("2026-03-03", 1, 56)], SEM + HOL, p_diaHabil="N", p_soloCaeDiaHabil="S")
    sig("FM-FEC-D01d_siguiente-solo-cae-dia-habil-atras", "ObtenerDiaSiguiente: con p_consideraHabilAnterior=S el dia no habil retrocede al habil anterior",
        [fs("2026-03-08", 1, 56), fs("2026-03-04", 0, 56), fs("2026-03-05", 1, 56)], SEM + HOL, p_diaHabil="N", p_soloCaeDiaHabil="S", p_consideraHabilAnterior="S")
    sig("FM-FEC-D01e_siguiente-sin-ajuste", "ObtenerDiaSiguiente: p_diaHabil=N sin p_soloCaeDiaHabil (nulo => N) cuenta dias corridos y no ajusta",
        [fs("2026-03-04", 3, 56)], SEM + HOL, p_diaHabil="N", p_soloCaeDiaHabil=None)
    sig("FM-FEC-D01f_siguiente-dia-habil-nulo", "ObtenerDiaSiguiente: p_diaHabil nulo => NullPointerException", [fs("2026-03-04", 1, 56)], SEM, p_diaHabil=None)
    sig("FM-FEC-D01g_siguiente-fecha-invalida", "ObtenerDiaSiguiente: fecha vacia => DateTimeParseException", [fs("", 1, 56)], SEM)
    sig("FM-FEC-D01h_siguiente-pais-sin-feriados", "ObtenerDiaSiguiente: pais sin filas de calendario => ningun dia es inhabil", [fs("2026-03-06", 2, 999)], SEM)
    ant("FM-FEC-D02a_anterior-dias-habiles", "ObtenerDiaAnterior: resta dias habiles saltando fin de semana y feriados del pais",
        [{"fechaInicial": "2026-03-09", "numeroDias": 1}, {"fechaInicial": "2026-03-05", "numeroDias": 2}], SEM + HOL)
    ant("FM-FEC-D02b_anterior-excluye-feriados", "ObtenerDiaAnterior: excluirConsultarDiaFeriado ignora los FERIADO (solo cuentan los dias de semana inhabiles)",
        [{"fechaInicial": "2026-03-05", "numeroDias": 1}, {"fechaInicial": "2026-03-09", "numeroDias": 1}], SEM + HOL, excluirConsultarDiaFeriado=True)
    ant("FM-FEC-D02c_anterior-sin-dias", "ObtenerDiaAnterior: numeroDias nulo o 0 deja 0001-01-01 (fecha de arranque del legacy) en el calculo",
        [{"fechaInicial": "2026-03-09", "numeroDias": 0}, {"fechaInicial": "2026-03-09", "numeroDias": None}], SEM)
    ant("FM-FEC-D02d_anterior-dia-habil-n-solo-cae-s", "ObtenerDiaAnterior: diaHabil=N con soloCaeDiaHabil=S: resta dias corridos y luego avanza al dia habil",
        [{"fechaInicial": "2026-03-09", "numeroDias": 1}, {"fechaInicial": "2026-03-05", "numeroDias": 1}], SEM + HOL, diaHabil="N", soloCaeDiaHabil="S")
    ant("FM-FEC-D02e_anterior-n-solo-cae-s-sin-feriados", "ObtenerDiaAnterior: lo mismo ignorando los FERIADO",
        [{"fechaInicial": "2026-03-05", "numeroDias": 1}], SEM + HOL, diaHabil="N", soloCaeDiaHabil="S", excluirConsultarDiaFeriado=True)
    ant("FM-FEC-D02f_anterior-sin-ajuste", "ObtenerDiaAnterior: diaHabil=N con soloCaeDiaHabil=N cuenta dias corridos", [{"fechaInicial": "2026-03-09", "numeroDias": 2}], SEM,
        diaHabil="N")
    ant("FM-FEC-D02g_anterior-pais-nulo", "ObtenerDiaAnterior: idPais nulo => sin filas de calendario", [{"fechaInicial": "2026-03-09", "numeroDias": 1}], SEM, idPais=None)
    ant("FM-FEC-D02h_anterior-dia-habil-nulo", "ObtenerDiaAnterior: diaHabil nulo => NullPointerException", [{"fechaInicial": "2026-03-09", "numeroDias": 1}], SEM, diaHabil=None)
    mul("FM-FEC-D03a_multiples-dias-habiles", "ObtenerDiaHabilMultiplesPaises: un dia es inhabil si lo es en cualquiera de los paises de la fecha",
        [fm("2026-03-02", 2, [56, 840], 1), fm("2026-03-10", 1, [840], 2), fm("2026-03-03", 1, [56], 3)], SEM + HOL)
    mul("FM-FEC-D03b_multiples-sin-dias", "ObtenerDiaHabilMultiplesPaises: numeroDias nulo o <= 0 devuelve la fecha (aunque sea feriado)",
        [fm("2026-03-04", 0, [56], 1), fm("2026-03-04", None, [56], 2), fm("2026-03-04", -1, [56], 3)], SEM + HOL)
    mul("FM-FEC-D03c_multiples-solo-cae-dia-habil", "ObtenerDiaHabilMultiplesPaises: diaHabil=N con soloCaeDiaHabil=S avanza al siguiente dia habil de todos los paises",
        [fm("2026-03-07", 0, [56, 840], 1), fm("2026-03-06", 1, [56], 2), fm("2026-03-13", 1, [56, 840], 3), fm("2026-03-02", 1, [56], 4)], SEM + HOL,
        p_diaHabil="N", p_soloCaeDiaHabil="S")
    # D-FEC-11 (new): with p_diaHabil=N and p_soloCaeDiaHabil=S the legacy checks the STALE text out.fechaCalculada: a result that is a FERIADO never leaves the loop
    # (infinite loop). Unreachable from CalcularFechaSolicitudAporteRescate (it always passes p_diaHabil=S). No golden can be recorded for it; see TRANSFORMATION_NOTES.
    mul("FM-FEC-D03d_multiples-sin-ajuste", "ObtenerDiaHabilMultiplesPaises: diaHabil=N sin soloCaeDiaHabil (nulo => N)", [fm("2026-03-04", 2, [56], 1)], SEM + HOL,
        p_diaHabil="N", p_soloCaeDiaHabil=None)
    mul("FM-FEC-D03e_multiples-dia-habil-nulo", "ObtenerDiaHabilMultiplesPaises: p_diaHabil nulo => NullPointerException", [fm("2026-03-04", 1, [56], 1)], SEM, p_diaHabil=None)
    mul("FM-FEC-D03f_multiples-sin-paises", "ObtenerDiaHabilMultiplesPaises: lista de paises vacia: nada es inhabil", [fm("2026-03-06", 2, [], 1)], SEM)
