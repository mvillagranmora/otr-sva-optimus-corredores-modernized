"""FM-COM cases, part 2 (rules 04-06): entry/exit commission by bracket, quarterly commission, currency conversions."""
from cases3a_lib import *
from cases3a_cuo1 import precios, paridad, conjunto
from cases3a_com1 import TRAMOS, lote, custodia, cu, com, PARAM

P = precios((1, 1000), (2, 1000))
S = parametro(PARAM, "S")
PER = tramo(10, "FM001A", "PER", 0, 9999, 1.2)
OTRO = tramo(11, "FM002U", "SALIDA", 0, 9999, 5.0) + tramo(12, "FM001A", "SALIDA", 0, 9999, 7.0, id_comision=2, estado_tramo="INA") + [comision_maxima("FM002U")]


def generar():
    # FM-COM-04: ENTRADA / SALIDA brackets. FechaOperacion 2026-02-20 -> curse 2026-03-02 = 10 days; 2026-01-15 -> 46; 2025-06-01 -> 274
    for nombre, op, dias in (("a_primer-tramo", "2026-02-20", 10), ("b_segundo-tramo", "2026-01-15", 46), ("c_fuera-de-tramos", "2025-06-01", 274),
                             ("d_limite-superior", "2026-01-31", 30), ("e_limite-inferior-siguiente", "2026-01-30", 31), ("f_dias-negativos", "2026-03-20", -18)):
        com("FM-COM-04%s" % nombre, "FM-COM-04: comision de salida por tramo segun los dias entre la operacion del lote y el curse (%d dias)" % dias, [cu(fcurse="2026-03-02")],
            TRAMOS + S + OTRO, conjunto(custodia(lote(100, "2026-02-01", op)), P))
    lotes = custodia(lote(100, "2026-02-10", "2026-02-20"))
    com("FM-COM-04g_aporte-entrada", "FM-COM-04: aporte => tramos ENTRADA", [cu(tipo="APO")], TRAMOS + S, conjunto(lotes, P))
    com("FM-COM-04h_curse-vacio-usa-fecha-sistema", "FM-COM-04: sin FechaCurse los dias van de la fecha de origen del lote a la fecha de sistema", [cu(fcurse="")], TRAMOS + S, conjunto(lotes, P))
    com("FM-COM-04i_curse-nulo-usa-fecha-sistema", "FM-COM-04: FechaCurse nula: igual que vacia", [cu(fcurse=None)], TRAMOS + S, conjunto(lotes, P))
    com("FM-COM-04j_aporte-reinversion", "FM-COM-04: concepto APOREI: los dias van de la fecha de origen del lote al curse", [cu(tipo="APO")], TRAMOS + S,
        conjunto(custodia(lote(100, "2026-02-25", "2026-01-01", "APOREI")), P))
    com("FM-COM-04k_aporte-reinversion-sin-curse", "FM-COM-04: concepto APOREI sin FechaCurse => IllegalArgumentException de Date.valueOf", [cu(tipo="APO", fcurse=None)], TRAMOS + S,
        conjunto(custodia(lote(100, "2026-02-25", "2026-01-01", "APOREI")), P))
    com("FM-COM-04l_movimiento-reinversion", "FM-COM-04: movimiento RESREINV no cobra comision", [cu(mov="RESREINV")], TRAMOS + S, conjunto(lotes, P))
    com("FM-COM-04m_serie-apv", "FM-COM-04: serie APV no cobra comision", [cu()], TRAMOS + S + [update('"BSCL.FondoMutuo.BD::FMM.SerieDeFondo"', {"EsAPV": "S"}, "\"IdSerieFondo\" = 'FM001A'")],
        conjunto(lotes, P))
    com("FM-COM-04n_lote-sin-fecha-operacion", "FM-COM-04: lote sin FechaOperacion con FechaCurse => NullPointerException", [cu()], TRAMOS + S, conjunto(custodia(lote(100, "2026-02-10", None)), P))
    com("FM-COM-04o_curse-invalido", "FM-COM-04: FechaCurse con formato invalido => IllegalArgumentException", [cu(fcurse="02/03/2026")], TRAMOS + S, conjunto(lotes, P))
    com("FM-COM-04p_tipo-solicitud-nulo", "FM-COM-04: parametro N y TipoSolicitud nulo => NullPointerException", [cu(tipo=None)], TRAMOS + parametro(PARAM, "N"), conjunto(lotes, P))
    com("FM-COM-04q_flujo-es-el-ultimo-tramo", "FM-COM-04: Flujo = dias del ultimo tramo evaluado", [cu()], TRAMOS + S, conjunto(custodia(lote(30, "2026-02-01", "2026-02-20"), lote(70, "2026-02-02", "2026-01-15")), P))
    # FM-COM-05: quarterly commission (PER)
    ref = "FM-COM-05: comision trimestral PER (D-COM-2: 1.19 binario, base 365; D-COM-3,4)"
    com("FM-COM-05a_mismo-trimestre", ref + " lote del mismo trimestre: dias entre ingreso y fecha de sistema", [cu()], TRAMOS + S + PER, conjunto(custodia(lote(100, "2026-02-10")), P))
    for nombre, f in (("b_trimestre-anterior-q4", "2025-11-20"), ("c_trimestre-anterior-q2", "2025-05-10"), ("d_trimestre-anterior-q3", "2025-08-15"),
                      ("e_bisiesto-q1", "2024-02-10")):
        com("FM-COM-05%s" % nombre, ref + " lote de otro trimestre: se cobra la duracion de su trimestre (%s)" % f, [cu()], TRAMOS + S + PER, conjunto(custodia(lote(100, f)), P))
    com("FM-COM-05f_dos-lotes-contador-llega-a-cero", ref + " dos lotes de la solicitud: Comision = suma de cantidades * valor cuota solo al llegar el contador a 0",
        [cu(cant=150)], TRAMOS + S + PER, conjunto(custodia(lote(100, "2026-02-10"), lote(100, "2026-01-05")), P))
    com("FM-COM-05g_dos-solicitudes-contador-no-llega-a-cero", ref + " D-COM-3: con varias solicitudes el contador (total de lotes) no llega a 0 y la Comision queda en 0",
        [cu(1, cant=60), cu(2, cant=60)], TRAMOS + S + PER, conjunto(custodia(lote(100, "2026-02-10")), P))
    com("FM-COM-05h_ingreso-posterior-a-fecha-sistema", ref + " lote posterior a la fecha de sistema en otro trimestre: dias negativos", [cu()], TRAMOS + S + PER,
        conjunto(custodia(lote(100, "2026-04-10")), P))
    com("FM-COM-05i_per-y-salida", ref + " la misma solicitud con tramos SALIDA y PER", [cu()], TRAMOS + S + PER, conjunto(custodia(lote(100, "2026-02-10", "2026-02-20")), P))
    com("FM-COM-05j_per-de-otra-serie", ref + " tramo PER de otra serie no cuenta", [cu()], TRAMOS + S + tramo(13, "FM002U", "PER", 0, 9999, 3.0) + [comision_maxima("FM002U")],
        conjunto(custodia(lote(100, "2026-02-10")), P))
    # FM-COM-06: conversions
    cal = [{"IdInterno": 6, "MontoDestino": 11.5}, {"IdInterno": 7, "MontoDestino": 12.5}, {"IdInterno": 8, "MontoDestino": 0.0011}, {"IdInterno": 9, "MontoDestino": 0.0012}]
    com("FM-COM-06a_conversiones-completas", "FM-COM-06: las 4 conversiones (ids IdInterno*5+1..4) con resultado", [cu()], TRAMOS + S, conjunto(custodia(lote(100)), P, paridad(t1=[{"IdInterno": 6, "MontoDestino": 11.5},
        {"IdInterno": 7, "MontoDestino": 12.5}, {"IdInterno": 8, "MontoDestino": 0.0011}, {"IdInterno": 9, "MontoDestino": 0.0012}])))
    com("FM-COM-06b_sin-conversiones-valen-uno", "FM-COM-06 D-COM-5: sin resultados de paridad, comision local/sistema y pivotes valen 1", [cu()], TRAMOS + S, conjunto(custodia(lote(100)), P))
    com("FM-COM-06c_conversiones-parciales", "FM-COM-06 D-COM-5: solo algunas conversiones tienen resultado (las otras valen 1)", [cu()], TRAMOS + S,
        conjunto(custodia(lote(100)), P, paridad(t1=[{"IdInterno": 7, "MontoDestino": 12.5}, {"IdInterno": 9, "MontoDestino": 0.0012}])))
    com("FM-COM-06d_dos-solicitudes-y-monedas", "FM-COM-06: dos solicitudes con monedas de fondo/operacion distintas", [cu(1, mf="USD", mo="CLP"), cu(2, mf="CLP", mo="USD", tipo="APO")], TRAMOS + S,
        conjunto(custodia(lote(100)), P, paridad(t1=[{"IdInterno": 6, "MontoDestino": 1}, {"IdInterno": 11, "MontoDestino": 2}])))
    com("FM-COM-06e_sin-solicitudes", "FM-COM-06: sin solicitudes no hay llamada de paridad", [], TRAMOS + S, conjunto(custodia(lote(100)), P))
    com("FM-COM-06f_paridad-con-id-repetido", "FM-COM-06: el resultado de paridad repite IdInterno => Exception 'key field is not unique'", [cu()], TRAMOS + S,
        conjunto(custodia(lote(100)), P, paridad(t1=[{"IdInterno": 6, "MontoDestino": 1}, {"IdInterno": 6, "MontoDestino": 2}])))
    com("FM-COM-06g_idinterno-nulo", "FM-COM-06: IdInterno nulo => NullPointerException", [cu(None)], TRAMOS + S, conjunto(custodia(lote(100)), P))
    com("FM-COM-06h_error-de-paridad", "FM-COM-06: el servicio de paridad falla => excepcion", [cu()], TRAMOS + S, conjunto(custodia(lote(100)), P, {"CalcularParidad": {"respuestas": [{"error": "paridad no disponible"}]}}))
