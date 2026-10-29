"""FMM-RRE-53..66: coverage of the remaining branches (dates of the input, back-office profile, taxation groups, nulls, amount limits)."""
import cases_rre_lib as L
from cases_rre_lib import RESCATE, APORTE, sol
from cases_rre_b import par, regimen, BATCH_FECHAS

NAC = par("FM001A", "FM002A")
SIN_PERFIL = [L.ins('"BSCL.Plataforma.BD::ACC.Usuario"', {"IdUsuario": 8, "IdEmpresa": 1, "IdEstado": "ACT", "TipoEntidad": "EMP", "IdEntidad": 80})]


def escribir():
    c = L.caso
    c("FMM-RRE-53_fecha-valor-cuota-10", "FM-REI-01/13/18: FechaValorCuota yyyy-MM-dd (GsonDateDeserializer de 10): cuota y fila con esa fecha",
      [sol(RESCATE, FechaValorCuota="2026-03-03")], [sol(APORTE)], colaboradores=NAC)
    c("FMM-RRE-54_fecha-valor-cuota-19", "FM-REI-01: FechaValorCuota yyyy-MM-dd HH:mm:ss (19)", [sol(RESCATE, FechaValorCuota="2026-03-03 10:11:12")],
      [sol(APORTE)], colaboradores=NAC)
    c("FMM-RRE-55_fecha-valor-cuota-ilegible", "FM-REI-01: FechaValorCuota ilegible: el deserializador del legacy devuelve null", [sol(RESCATE, FechaValorCuota="ayer")],
      [sol(APORTE)], colaboradores=NAC)
    c("FMM-RRE-56_sin-perfil-todo-batch-backoffice", "FM-REI-05/08/20: sin perfil pero todas batch: pasa con estados backoffice", [sol(RESCATE, SolicitudBatch="S")],
      [sol(APORTE)], colaboradores=NAC, p_IdUsuario=8, seed=SIN_PERFIL + BATCH_FECHAS + [L.param("RespetaValutasCargaMasiva", "S")])
    tres = L.colab(L.precios({1: "FM001A", 2: "FM001A", 3: "FM001A", -1: "FM002A", -2: "FM002A", -3: "FM002A"}), L.custodia((900, "R108", 3000), (900, "R107", 3000), (900, "R57", 3000)),
                   L.paridad([1, 2, 3, -1, -2, -3]))
    c("FMM-RRE-57_tributacion-art108-art107-57bis", "FM-REI-06, D-NOR-9: tres regimenes ART108/ART107/57BIS: el legacy revienta antes "
      "('key field is not unique' de un List2Hash del universo de prueba); se fija tal cual (la tributacion queda sin caso)",
      [sol(RESCATE, IdInterno=i, IdAgrupador=i, IdRegimenTributario=r) for i, r in ((1, "R108"), (2, "R107"), (3, "R57"))],
      [sol(APORTE, IdInterno=i, IdAgrupador=i, IdRegimenTributario=r) for i, r in ((1, "R108"), (2, "R107"), (3, "R57"))], colaboradores=tres,
      seed=regimen("R108", "ART108") + regimen("R107", "ART107") + regimen("R57", "57BIS"))
    c("FMM-RRE-58_regimen-nulo", "FM-REI-09/20/28: IdRegimenTributario nulo en rescate y aporte: clave de bloqueo con '' y sin regimen (EsRegimenValido N)",
      [sol(RESCATE, IdRegimenTributario=None)], [sol(APORTE, IdRegimenTributario=None)], colaboradores=NAC)
    c("FMM-RRE-59_portafolio-inexistente", "FM-REI-18/20/37, D-NOR-9: portafolio sin fila (cliente nulo): el legacy revienta con "
      "'key field is not unique' DESPUES del bloqueo (queda)", [sol(RESCATE, IdPortafolio=501)],
      [sol(APORTE, IdPortafolio=501)], colaboradores=NAC)
    c("FMM-RRE-60_serie-rescate-inexistente", "FM-REI-22/37, D-NOR-9: serie de rescate inexistente: NPE (serie.IdMoneda) sin bloqueo para ese rescate",
      [sol(RESCATE, IdSerieFondo="NOEXISTE")], [sol(APORTE)], colaboradores=NAC)
    c("FMM-RRE-61_monto-maximo-destino", "FM-REI-21: MontoMaximo del destino menor que el aporte: EsMontoMaximo N", [sol(RESCATE)], [sol(APORTE)], colaboradores=NAC,
      seed=[L.upd(L.SERIE, {"MontoMaximo": 100}, "\"IdSerieFondo\" = 'FM002A'")])
    c("FMM-RRE-62_monto-maximo-agrupado-web", "FM-REI-21, D-F2-15: canal WEB y MontoMaximo: se mide la suma", [sol(RESCATE)], [sol(APORTE)], colaboradores=NAC,
      p_IdTipoCanalIngreso="WEB", seed=[L.upd(L.SERIE, {"MontoMaximo": 100, "MontoMinimo": 1}, "\"IdSerieFondo\" = 'FM002A'")])
    c("FMM-RRE-63_patrimonio-altos-destino-usd", "FM-REI-22: destino no CLP con PatrimonioAltos S: el acumulado se ASIGNA (no suma)",
      [sol(RESCATE, IdSerieFondo="FMX01U", IdMoneda="USD")], [sol(APORTE, IdSerieFondo="FMX02U", IdEmisor="EXT1", IdMoneda="USD")],
      colaboradores=par("FMX01U", "FMX02U"), seed=[L.upd(L.SERIE, {"PatrimonioAltos": "S"}, "\"IdSerieFondo\" = 'FMX02U'")])
    c("FMM-RRE-64_mlt-con-regimenes-distinto-grupo", "FM-REI-21, D-F2-14: aporte MLT con regimen de otro GrupoRegimen y configurado",
      [sol(RESCATE)], [sol(APORTE, IndicadorMLT="S", IdRegimenTributario="APVX")], colaboradores=NAC,
      seed=regimen("APVX", "APV") + [L.ins(L.CONFREG, {"IdRegimenFondoMutuoOrigen": "GEN", "IdRegimenFondoMutuoDestino": "APVX"})])
    c("FMM-RRE-65_calculo-por-monto", "FM-REI-09/13: rescate por monto (M): la clave del bloqueo usa el Monto", [sol(RESCATE, CalculoPorMontoCuota="M",
                                                                                                                   Monto=15001.5, NumeroCuotas=0)],
      [sol(APORTE)], colaboradores=NAC)
    c("FMM-RRE-66_bf-aporte-cuotas-cero", "FM-REI-19, D-F2-17: BF con el aporte calculado en 0 cuotas (porcentaje 0)", [sol(RESCATE, IdRegimenTributario="BF")],
      [sol(APORTE, IdRegimenTributario="BF", Porcentaje=0)], colaboradores=NAC, seed=regimen("BF"))
    dos = L.colab(L.precios({1: "FM001A", 2: "FM001A", -1: "FM002A", -2: "FM002A"}), L.custodia((900, "GEN", 1000)), L.paridad([1, 2, -1, -2]))
    c("FMM-RRE-67_montos-agrupados-mismo-grupo-age", "FM-REI-17/21, D-F2-15 (canario 7): canal AGE, dos aportes del MISMO agrupador 1 a la misma serie: se "
      "escriben juntos con la clave {serie,1} pero se leen con el IdAgrupador NUEVO: no se suman y el minimo rechaza",
      [sol(RESCATE, IdAgrupador=1), sol(RESCATE, IdInterno=2, NumeroCuotas=5, IdAgrupador=1)],
      [sol(APORTE, IdAgrupador=1), sol(APORTE, IdInterno=2, IdAgrupador=1)], colaboradores=dos,
      seed=[L.upd(L.SERIE, {"MontoMinimo": 20000}, "\"IdSerieFondo\" = 'FM002A'")])
    c("FMM-RRE-68_custodia-insuficiente-las-dos-break", "FM-REI-29, D-F2-8 (mutacion de goldens 2 / canario 8): fuera de CAR y SIN custodia en las dos: "
      "el break deja la segunda sin validar (un solo mensaje; con CAR serian dos, golden 42)",
      [sol(RESCATE, IdAgrupador=1, NumeroCuotas=5000), sol(RESCATE, IdInterno=2, NumeroCuotas=6000, IdAgrupador=2)],
      [sol(APORTE, IdAgrupador=1), sol(APORTE, IdInterno=2, IdAgrupador=2)], colaboradores=dos)
    return 16
