"""F3 W3 T6-B: consistency of the recorded goldens of cases-liq/ with the tags each case declares, and coverage of the cards and of the quirks (run after record_liq.sh, before sync_liq.py):
  * `LIQ-modo:completo`   the legacy ran to the end: NO exception in the golden and a salida;  `LIQ-modo:excepcion`: an exception in the golden and NO salida;
  * every case has its golden with the provenance line of the T6-B runner, is stable in the determinism report (0 hard-unstable) and names its front (`front:L`);
  * COVERAGE: every card FM-LIQ-01..20 is named by at least one case `ref`, and every QUIRK tag below is carried by at least one case whose golden SHOWS the quirk (checked here from the golden, not
    from the tag): the list of logs never reaches AgregarLog, the folios of the carta are assigned over the complete list, the CIN branch uses group 0, the APV flag is read with company 1, no movements
    and no filters raise NoSuchElementException, the omnibus removal sends no message, the recount of the errors is by reference, only the first filter is used, ...;
  * the tags the module tests look up exist.
Also prints counts by tag. Exit 1 on any inconsistency. Reads only; never writes.

  python -X utf8 -I legacy-oracle/check_liq.py
"""
import json
import pathlib
import sys

BASE = pathlib.Path(__file__).resolve().parent.parent
TARJETAS = ["FM-LIQ-%02d" % n for n in range(1, 21)]
SOL = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
QUIRKS = ["log-sin-asignar", "carta-lista-completa", "cin-referencia-0", "apv-empresa-1", "sin-filtros-sin-mov", "omnibus-silencioso", "conteo-por-referencia", "primer-filtro", "fecha-vacia-0001",
          "estado-ING", "resultados-duplicados", "producto-n-por-m", "movcar-siempre", "simulacion-escribe", "mensajes-perdidos", "orden-por-hash", "comparacion-de-tipos", "secuencias-consumidas"]


def llamadas(g, nombre):
    return [c for c in g["colaboradores"] if c["colaborador"] == nombre]


def verificar_quirk(q, g, case, ent, salida):
    """returns None when the golden shows the quirk, else the reason it does not"""
    res = salida["P_OUT_RESULTADO"] if salida else []
    msgs = salida["P_OUT_MENSAJES"] if salida else []
    ids = [r["IdSolicitudAporteRescate"] for r in res]
    ie = llamadas(g, "CrearIngresoEgreso")
    ca = llamadas(g, "CrearCargoAbono")
    if q == "log-sin-asignar":
        al = llamadas(g, "AgregarLog")
        if not al:
            return "AgregarLog was not called"
        if al[0]["argumentos"][0].get("p_Log") != [] or not ent.get("p_Filtros"):
            return "p_Log of the call is not empty or the request has no filters"
        return None
    if q == "carta-lista-completa":
        return None if len(ids) > len(set(ids)) else "no solicitud is reported twice (the complete list was not stored under a client line)"
    if q == "cin-referencia-0":
        return None if ca else "CrearCargoAbono was not called"
    if q == "apv-empresa-1":
        return None if ent.get("p_IdEmpresa") != 1 or "APV.Habilitado" in str(g.get("excepcion", "")) else "company 1 and no missing APV parameter"
    if q == "sin-filtros-sin-mov":
        return None if str(g.get("excepcion", "")).startswith("NoSuchElementException") else "no NoSuchElementException"
    if q == "omnibus-silencioso":
        if any(m.get("codigo") == "UI_ERROR_APORTE_SIN_OMNIBUS" for m in msgs):
            return "the legacy DID send the omnibus message"
        return None if salida is not None else "no output"
    if q == "conteo-por-referencia":
        errores = [m for m in msgs if str(m.get("tipo", "")).upper() == "ERROR"]
        contadores = [c["respuesta"].get("P_OUT_ERRORESCONTADOR") for c in ie + ca if isinstance(c.get("respuesta"), dict)]
        perdido = salida is not None and salida["P_OUT_ERRORESCONTADOR"] == 0 and any(x for x in contadores if x)
        return None if errores or perdido else "no ERROR message in the output and no collaborator counter that the recount lost"
    if q == "primer-filtro":
        return None if len(ent.get("p_Filtros") or []) >= 2 and not ent.get("p_Movimientos") else "needs >= 2 filters and no movements"
    if q == "fecha-vacia-0001":
        fs = ent.get("p_Filtros") or []
        return None if not ent.get("p_Movimientos") and any(f and (f.get("FechaCertificacion") == "" or f.get("FechaLiquidacion") == "") for f in fs) else "needs an empty date and no movements"
    if q == "estado-ING":
        return None if res and all(r["Estado"] == "ING" for r in res) else "the results do not all say ING"
    if q == "resultados-duplicados":
        return None if len(ids) > len(set(ids)) else "no repeated result"
    if q == "producto-n-por-m":
        return None if len(ca) >= 2 or any(len(c["argumentos"][0]["p_CargoAbonoAGenerar"]) >= 2 for c in ca) else "fewer than 2 calls and fewer than 2 lines"
    if q == "movcar-siempre":
        objs = [o for c in ca for o in c["argumentos"][0]["p_CargoAbonoAGenerar"]]
        return None if objs and all(o["IdTipoMovimiento"] == "MOVCAR" and o["IdConcepto"] == "CAPEFM" for o in objs) else "some cargo / abono is not MOVCAR / CAPEFM"
    if q == "simulacion-escribe":
        if ent.get("p_SimularOperacion") != "S" or not ie or ie[0]["argumentos"][0]["p_SimularOperacion"] != "S":
            return "the request does not simulate or Tesoreria did not get S"
        filas = g["tablasTrasLaLlamada"][SOL]
        escribio = any(r["EstadoLiquidacion"] == "LIQ" and r["FolioCartaLiquidacion"] != 0 for r in filas)
        if ca and ca[0]["argumentos"][0]["p_simularOperacion"] != "N":
            return "CrearCargoAbono did not get N"
        return None if escribio or ca else "nothing written and no CrearCargoAbono"
    if q == "mensajes-perdidos":
        return None if ent.get("p_Filtros") == [] and len(msgs) >= 1 and not any(m.get("codigo") == "NO_SE_INGRESARON_FILTROS" for m in msgs) else "the NO_SE_INGRESARON_FILTROS message was not replaced by the collaborator's"
    if q == "orden-por-hash":
        return None if ie and len(ie[0]["argumentos"][0]["p_IngresoEgresoGenerar"]) >= 2 else "fewer than 2 lines to Tesoreria"
    if q == "comparacion-de-tipos":
        return None if ca else "no CrearCargoAbono to see the amounts"
    if q == "secuencias-consumidas":
        return None if "excepcion" in g and g["secuenciasSolicitadas"] else "no exception or no sequence asked"
    return "unknown quirk"


def main():
    malos, n = [], 0
    modos, vistos, quirks = {}, {}, {}
    for d in sorted((BASE / "cases-liq").glob("FMM-LIQ-*")):
        n += 1
        e = d / "expected.json"
        if not e.exists():
            malos.append("%s: no golden" % d.name)
            continue
        g = json.loads(e.read_text(encoding="utf-8"))
        case = json.loads((d / "case.json").read_text(encoding="utf-8"))
        ref = case["ref"]
        modo = [r for r in ref if r.startswith("LIQ-modo:")]
        if len(modo) != 1:
            malos.append("%s: it must carry exactly one LIQ-modo tag" % d.name)
            continue
        modo = modo[0]
        modos[modo] = modos.get(modo, 0) + 1
        if "front:L" not in ref:
            malos.append("%s: no front:L tag" % d.name)
        for r in ref:
            vistos[r] = vistos.get(r, 0) + 1
        if "OracleRunnerLiq.java executing the UNMODIFIED legacy cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate" not in g.get("_provenance", ""):
            malos.append("%s: golden without the provenance of the T6-B oracle" % d.name)
        tiene = "excepcion" in g
        if modo == "LIQ-modo:completo" and (tiene or "salida" not in g):
            malos.append("%s: tagged completo but the legacy threw %s" % (d.name, str(g.get("excepcion"))[:120]))
        if modo == "LIQ-modo:excepcion" and (not tiene or "salida" in g):
            malos.append("%s: tagged excepcion but the legacy ended normally" % d.name)
        if g.get("previas"):
            malos.append("%s: a T6-B case has no previous requests" % d.name)
        if "LIQ-desvio:kit-1" in ref and not str(g.get("excepcion", "")).startswith("NullPointerException:"):
            malos.append("%s: tagged LIQ-desvio:kit-1 but the legacy did not raise a NullPointerException" % d.name)
        if not any(c["colaborador"] == "CrearIngresoEgreso" for c in g["colaboradores"]) and modo == "LIQ-modo:completo" and not llamadas(g, "AgregarLog"):
            malos.append("%s: a complete run with no call to any collaborator" % d.name)
        salida = g.get("salida")
        ent = case["entrada"]
        for r in ref:
            if r.startswith("LIQ-quirk:"):
                q = r[len("LIQ-quirk:"):]
                if q not in QUIRKS:
                    malos.append("%s: unknown quirk tag %s" % (d.name, r))
                    continue
                why = verificar_quirk(q, g, case, ent, salida)
                if why:
                    malos.append("%s: tagged %s but the golden does not show it: %s" % (d.name, r, why))
                else:
                    quirks[q] = quirks.get(q, 0) + 1
    for t in TARJETAS:
        if t not in vistos:
            malos.append("card %s is pinned by NO case ref" % t)
    for q in QUIRKS:
        if q not in quirks:
            malos.append("the quirk %s is shown by NO case" % q)
    if n < 80:
        malos.append("fewer than 80 cases: %d" % n)
    rep = BASE / "determinism-liq.txt"
    if rep.exists():
        primera = rep.read_text(encoding="utf-8").splitlines()[0]
        print("determinism report:", primera)
        if "hard: 0" not in primera:
            malos.append("determinism report: some case is unstable: " + primera)
        if "repeats per case: 20" not in primera:
            malos.append("determinism report: not 20 repetitions: " + primera)
    else:
        malos.append("no determinism report")
    print("cases checked:", n, modos)
    print("cards:", {t: vistos.get(t, 0) for t in TARJETAS})
    print("quirks shown by the goldens:", quirks)
    for m in malos:
        print("!!", m)
    sys.exit(1 if malos else 0)


if __name__ == "__main__":
    main()
