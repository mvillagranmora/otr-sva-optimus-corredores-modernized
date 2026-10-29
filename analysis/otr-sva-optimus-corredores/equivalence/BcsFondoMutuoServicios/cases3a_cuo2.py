"""FM-CUO cases, part 2 (rules 05, 06, 08, 09): investment currency different from the fund currency, same currency, system currency / UF / factor, final number of quotas."""
from cases3a_lib import *
from cases3a_cuo1 import USD, EUR, BRL, SINTC, ser, cuo, precios, paridad, conjunto

PU = precios((1, 25.5), (2, 25.5), (3, 25.5))        # price of the USD fund series (IdRegistro = IdInterno)
OBS_S = parametro("UsaDolarObservado", "S")


def generar():
    # --- FM-CUO-05: diferido, UsaDolarObservado = N
    for modo, extra in (("M", dict(monto=150000)), ("C", dict(monto=0, cuotas=10))):
        for tipo in ("APO", "RES"):
            cuo("FM-CUO-05a_fondo-usd-inversion-clp-%s-%s" % (modo.lower(), tipo.lower()),
                "FM-CUO-05: fondo en USD e inversion en CLP con UsaDolarObservado=N: paridad = tipo de cambio de la serie (%s de aporte / rescate)" % tipo,
                [ser(1, serie="FM002U", modo=modo, mf="USD", mi="CLP", tipo=tipo, **extra)], USD, PU)
        cuo("FM-CUO-05b_fondo-clp-inversion-usd-%s" % modo.lower(), "FM-CUO-05: fondo en moneda sistema e inversion en USD: paridad = tipo de cambio de la serie (inverso)",
            [ser(1, modo=modo, mf="CLP", mi="USD", **extra)])
    pa = paridad(t1=[{"IdInterno": 1, "FactorCambioLocal": 1.25, "FactorOrigenDestino": 1.25, "MontoDestino": 120000.5}])
    for modo, extra in (("M", dict(monto=150000)), ("C", dict(monto=0, cuotas=10))):
        cuo("FM-CUO-05c_otra-moneda-con-paridad-%s" % modo.lower(), "FM-CUO-05: fondo en EUR e inversion en CLP: usa CalcularParidad (hay resultado)",
            [ser(1, serie="FM003E", modo=modo, mf="EUR", mi="CLP", **extra)], EUR, conjunto(precios((1, 33.3)), pa))
    cuo("FM-CUO-05d_otra-moneda-sin-paridad", "FM-CUO-05: fondo en EUR e inversion en CLP sin resultado de paridad: monto destino 0, paridad -1",
        [ser(1, serie="FM003E", mf="EUR", mi="CLP", monto=1000)], EUR, precios((1, 33.3)))
    cuo("FM-CUO-05e_dolar-observado-s-con-paridad", "FM-CUO-05: UsaDolarObservado=S usa siempre CalcularParidad (aunque el fondo sea USD)",
        [ser(1, serie="FM002U", mf="USD", mi="CLP", monto=150000)], USD + OBS_S, conjunto(PU, pa))
    cuo("FM-CUO-05f_dolar-observado-s-sin-paridad", "FM-CUO-05: UsaDolarObservado=S sin resultado de paridad", [ser(1, serie="FM002U", mf="USD", mi="CLP", monto=150000)],
        USD + OBS_S, PU)
    cuo("FM-CUO-05g_dolar-observado-s-modo-c", "FM-CUO-05: UsaDolarObservado=S modo C con paridad", [ser(1, serie="FM002U", modo="C", monto=0, cuotas=4, mf="USD", mi="CLP")],
        USD + OBS_S, conjunto(PU, pa))
    cuo("FM-CUO-05h_tipo-cambio-cero", "FM-CUO-05: serie USD sin tipo de cambio (COALESCE 0) => division por cero", [ser(1, serie="FM005U", mf="USD", mi="CLP", monto=150000)], SINTC, PU)
    cuo("FM-CUO-05i_tipo-cambio-cero-modo-c", "FM-CUO-05: serie USD sin tipo de cambio en modo C: el producto da 0 y no falla",
        [ser(1, serie="FM005U", modo="C", monto=0, cuotas=3, mf="USD", mi="CLP")], SINTC, PU)
    cuo("FM-CUO-05j_moneda-sin-formato", "FM-CUO-05/06: moneda del fondo sin fila en PLA.MonedaFormato => Exception 'formato no encontrado'",
        [ser(1, serie="FM004B", mf="BRL", mi="BRL", monto=1000)], BRL, precios((1, 10)))
    cuo("FM-CUO-05k_inversion-nula", "FM-CUO-05: IdMonedaInversion nula => NullPointerException", [ser(1, mi=None)])
    # --- FM-CUO-06: misma moneda
    cuo("FM-CUO-06a_misma-moneda-modo-m", "FM-CUO-06: misma moneda modo M: cuotas = Monto / valor cuota (HALF_DOWN), paridad 1", [ser(1, monto=150000), ser(2, tipo="RES", monto=99999.99)],
        None, precios((1, 1500.1234), (2, 1500.1234)))
    cuo("FM-CUO-06b_misma-moneda-redondeo", "FM-CUO-06 D-CUO-3: monto que no divide exacto: redondeo HALF_DOWN a los decimales de la serie", [ser(1, monto=1000)], None, precios((1, 3)))
    cuo("FM-CUO-06c_dos-solicitudes-misma-serie", "FM-CUO-06: dos solicitudes de la misma serie comparten la fila de cuota (se pisa el valor cuota con cada precio)",
        [ser(1, monto=1000), ser(2, monto=1000)], None, precios((1, 3), (2, 7)))
    # --- FM-CUO-08: moneda sistema / UF / factor
    pt2 = [{"IdInterno": 5, "FactorOrigenDestino": 1, "MontoDestino": 140.1234}, {"IdInterno": 6, "FactorOrigenDestino": 1, "MontoDestino": 37.5},
           {"IdInterno": 7, "FactorOrigenDestino": 38500.55, "MontoDestino": 38500.55}]
    cuo("FM-CUO-08a_fondo-usd-todos-los-calculos", "FM-CUO-08: fondo en USD (moneda sistema CLP): paridad a moneda sistema, a UF y factor UF->sistema (ids IdInterno*4+1..3)",
        [ser(1, serie="FM002U", mf="USD", mi="USD", monto=1000)], USD, conjunto(PU, paridad(t2=pt2)))
    cuo("FM-CUO-08b_fondo-usd-sin-calculos", "FM-CUO-08: sin resultados de paridad: factor, costo y monto en moneda sistema valen 0", [ser(1, serie="FM002U", mf="USD", mi="USD", monto=1000)],
        USD, PU)
    cuo("FM-CUO-08c_fondo-usd-negativo", "FM-CUO-08: IdInterno negativo usa los ids IdInterno*4-1..3",
        [ser(-1, serie="FM002U", mf="USD", mi="USD", monto=1000)], USD, conjunto(precios((-1, 25.5)), paridad(t2=[{"IdInterno": -5, "MontoDestino": 140.1234}, {"IdInterno": -6, "MontoDestino": 9}, {"IdInterno": -7, "FactorOrigenDestino": 7}])))
    cuo("FM-CUO-08d_factor-nulo", "FM-CUO-08: el resultado de UF sin FactorOrigenDestino conserva el 0 por defecto del legacy",
        [ser(1, serie="FM002U", mf="USD", mi="USD", monto=1000)], USD, conjunto(PU, paridad(t2=[{"IdInterno": 7, "MontoDestino": 5}, {"IdInterno": 6}])))
    # --- FM-CUO-09: final number of quotas
    cuo("FM-CUO-09a_reinversion-cuotas-cero", "FM-CUO-09 D-CUO-3: RESREINV con cuotas 0: decimales + 1 y HALF_UP",
        [ser(1, modo="C", monto=1000, cuotas=0, mov="RESREINV", tipo="RES")], None, precios((1, 3)))
    cuo("FM-CUO-09b_reinversion-otro-id", "FM-CUO-09: la regla RESREINV solo toca el resultado de su IdInterno",
        [ser(1, modo="C", monto=1000, cuotas=0, mov="RESREINV", tipo="RES"), ser(2, modo="C", monto=1000, cuotas=0, tipo="RES")], None, precios((1, 3), (2, 3)))
    cuo("FM-CUO-09c_reinversion-con-cuotas", "FM-CUO-09: RESREINV con cuotas distintas de 0 no cambia el redondeo", [ser(1, modo="C", monto=0, cuotas=4, mov="RESREINV", tipo="RES")])
    # exact halves: HALF_DOWN and HALF_UP give different quotas (D-CUO-3); 1 / 32 = 0.03125 (4 decimals), 1 / 8 = 0.125 (2 decimals), 1 / 64 = 0.015625 (5 decimals)
    cuo("FM-CUO-06d_mitad-exacta-modo-m", "FM-CUO-06 D-CUO-3: misma moneda modo M con cociente 0.03125 a 4 decimales: HALF_DOWN da 0.0312", [ser(1, monto=1)], None, precios((1, 32)))
    cuo("FM-CUO-06e_mitad-exacta-modo-c", "FM-CUO-06 D-CUO-3: modo C con cuotas 0 y Monto 1: HALF_DOWN da 0.0312", [ser(1, modo="C", monto=1, cuotas=0)], None, precios((1, 32)))
    cuo("FM-CUO-05p_mitad-exacta-diferido", "FM-CUO-05 D-CUO-3: fondo USD con tipo de cambio 1 y cociente 0.125 a 2 decimales: HALF_DOWN da 0.12",
        [ser(1, serie="FM007U", mf="USD", mi="CLP", monto=1)], clonar_serie("FM007U", "24", fondo={"IdMoneda": "USD"}, serie={"IdInstrumento": 906, "DecimalesCuota": 2}, tipo_cambio=(1, 1)),
        precios((1, 8)))
    cuo("FM-CUO-09d_mitad-exacta-mas-un-decimal", "FM-CUO-09 D-CUO-3: cuotas 0 con RESREINV: +1 decimal y HALF_UP sobre 0.015625 da 0.01563", [ser(1, modo="C", monto=1, cuotas=0, mov="RESREINV", tipo="RES")],
        None, precios((1, 64)))
    cuo("FM-CUO-04g_cuotas-con-mas-decimales", "FM-CUO-04/09 D-CUO-3: NumeroCuotas de entrada 0.12345 se redondea HALF_DOWN a los decimales de la serie (0.1234)", [ser(1, modo="C", monto=0, cuotas=0.12345)])
    cuo("FM-CUO-10a_estado-ok", "FM-CUO-10: resultado OK con valor cuota > 0 y fecha fuente del precio", [ser(1, monto=100000, porc=2.5)], None, precios((1, 2000.5, 900, "2026-03-01 00:00:00.000")))
