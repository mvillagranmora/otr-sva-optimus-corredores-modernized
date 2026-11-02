"""F3 W3 T6-A: consistency of the recorded goldens of cases-cerpf/ with the tags each case declares, and coverage of the cards (run after record_cerpf.sh, before sync_cerpf.py):
  * `PF-modo:completo`   the legacy ran to the end: NO exception in the golden and a salida;
  * `PF-modo:excepcion`  the legacy threw somewhere in the run: an exception in the golden and NO salida;
  * every case has its golden with the provenance line of the T6-A runner, appears stable in the determinism report (0 hard-unstable) and names its front (`front:X`);
  * `PF-seleccion:cero|uno|varias`: the record of the call to Certificar (`CertificarPorFiltros.certificar`) carries 0 / 1 / 2+ ids in p_Solicitudes (a case in which the legacy throws BEFORE
    calling Certificar has no record and is not tagged); `PF-duplicados`: the ids carry a repeated one; `PF-sin-filtros`: the entry has no filter;
  * the header the class copies is the one of the request (p_IdEmpresa, p_IdUsuario, p_IdModulo, p_SimularOperacion, p_UsaReglasNegocio, p_FechaCertificacion, p_IdTipoCanalIngreso) and the two
    fields it does NOT copy keep the defaults of Certificar (p_NotificaOWP "S", p_CertificacionAporteSwitchSimulado "N"): FM-CXF-05;
  * COVERAGE: every card FM-CXF-01..06 and every matrix case C01..C05 (C06 is the job and has no case here) is named by at least one case `ref`; every filter field has a `PF-filtro:<campo>` case;
    the tags the module tests look up exist.
Also prints counts by tag. Exit 1 on any inconsistency. Reads only; never writes.

  python -X utf8 -I legacy-oracle/check_cerpf.py
"""
import json
import pathlib
import sys

BASE = pathlib.Path(__file__).resolve().parent.parent
TARJETAS = ["FM-CXF-%02d" % n for n in range(1, 6)] + ["FM-CXF-02", "FM-CER-02"]
MATRIZ = ["matriz:C%02d" % n for n in range(1, 6)]
CAMPOS = ["IdEmisor", "IdMoneda", "IdSerieFondo", "Nacional", "TipoSolicitud", "IdTipoMovimientoFondoMutuo", "MoneyMarket", "IdCarga"]
TAGS = ["PF-desvio:kit-1", "PF-seleccion:cero", "PF-seleccion:uno", "PF-seleccion:varias", "PF-duplicados", "PF-sin-filtros", "PF-nulo", "PF-modo:completo", "PF-modo:excepcion"]
PF = "CertificarPorFiltros.certificar"
CABECERA = ["p_IdEmpresa", "p_IdUsuario", "p_IdModulo", "p_SimularOperacion", "p_UsaReglasNegocio", "p_FechaCertificacion", "p_IdTipoCanalIngreso"]


def main():
    malos, n = [], 0
    modos, vistos, tags = {}, {}, {}
    for d in sorted((BASE / "cases-cerpf").glob("FMM-CERPF-*")):
        n += 1
        e = d / "expected.json"
        if not e.exists():
            malos.append("%s: no golden" % d.name)
            continue
        g = json.loads(e.read_text(encoding="utf-8"))
        case = json.loads((d / "case.json").read_text(encoding="utf-8"))
        ref = case["ref"]
        modo = [r for r in ref if r.startswith("PF-modo:")]
        if len(modo) != 1:
            malos.append("%s: it must carry exactly one PF-modo tag" % d.name)
            continue
        modo = modo[0]
        modos[modo] = modos.get(modo, 0) + 1
        if "front:X" not in ref:
            malos.append("%s: no front:X tag" % d.name)
        for r in ref:
            vistos[r] = vistos.get(r, 0) + 1
            if r.startswith("PF-") and not r.startswith("PF-modo"):
                tags[r] = tags.get(r, 0) + 1
        if "OracleRunnerCerPf.java executing the legacy cl.bcs.fondomutuo.CertificarPorFiltrosSolicitudAporteRescate" not in g.get("_provenance", ""):
            malos.append("%s: golden without the provenance of the T6-A oracle" % d.name)
        tiene = "excepcion" in g
        if modo == "PF-modo:completo" and (tiene or "salida" not in g):
            malos.append("%s: tagged completo but the legacy threw %s" % (d.name, str(g.get("excepcion"))[:120]))
        if modo == "PF-modo:excepcion" and (not tiene or "salida" in g):
            malos.append("%s: tagged excepcion but the legacy ended normally" % d.name)
        if g.get("previas"):
            malos.append("%s: a T6-A case has no previous requests" % d.name)
        if "PF-desvio:kit-1" in ref and not str(g.get("excepcion", "")).startswith("NullPointerException:"):
            malos.append("%s: tagged PF-desvio:kit-1 but the legacy did not raise a NullPointerException (%s)" % (d.name, str(g.get("excepcion"))[:80]))
        llamadas = [c for c in g["colaboradores"] if c["colaborador"] == PF]
        ent = case["entrada"]
        if llamadas:
            if len(llamadas) != 1:
                malos.append("%s: more than one call to Certificar (%d)" % (d.name, len(llamadas)))
            a = llamadas[0]["argumentos"][0]
            ids = [s["IdSolicitudAporteRescate"] for s in a["p_Solicitudes"]]
            for c in CABECERA:
                if a.get(c) != ent.get(c):
                    malos.append("%s: header %s is %r in the call to Certificar and %r in the request" % (d.name, c, a.get(c), ent.get(c)))
            if a.get("p_NotificaOWP") != "S" or a.get("p_CertificacionAporteSwitchSimulado") != "N":
                malos.append("%s: the fields the class does not copy must keep the defaults of Certificar" % d.name)
            n_ids = len(ids)
            for t, ok in (("PF-seleccion:cero", n_ids == 0), ("PF-seleccion:uno", n_ids == 1), ("PF-seleccion:varias", n_ids >= 2), ("PF-duplicados", len(set(ids)) < n_ids)):
                if t in ref and not ok:
                    malos.append("%s: tagged %s but Certificar received %s" % (d.name, t, ids))
            if "PF-duplicados" not in ref and len(set(ids)) < n_ids:
                malos.append("%s: Certificar received a repeated id %s and the case is not tagged PF-duplicados" % (d.name, ids))
            if "PF-sin-filtros" in ref and ent.get("p_Filtros") not in ([], None) and "p_Filtros" in ent:
                malos.append("%s: tagged PF-sin-filtros but it has filters" % d.name)
        else:
            if any(t.startswith("PF-seleccion") or t == "PF-duplicados" for t in ref):
                malos.append("%s: the legacy never called Certificar and the case carries a PF-seleccion / PF-duplicados tag" % d.name)
            if modo != "PF-modo:excepcion":
                malos.append("%s: no call to Certificar in a case that ended normally" % d.name)
    for t in TARJETAS + MATRIZ:
        if t not in vistos:
            malos.append("card / matrix case %s is pinned by NO case ref" % t)
    for c in CAMPOS:
        if "PF-filtro:" + c not in vistos:
            malos.append("filter field %s has no PF-filtro case" % c)
    for t in TAGS:
        if t not in vistos:
            malos.append("the tag %s that the module tests look up is carried by NO case" % t)
    rep = BASE / "determinism-cerpf.txt"
    if rep.exists():
        primera = rep.read_text(encoding="utf-8").splitlines()[0]
        print("determinism report:", primera)
        if "hard: 0" not in primera:
            malos.append("determinism report: some case is unstable: " + primera)
    else:
        malos.append("no determinism report")
    print("cases checked:", n, modos)
    print("tags:", tags)
    print("refs:", {t: vistos.get(t, 0) for t in TARJETAS + MATRIZ})
    for m in malos:
        print("!!", m)
    sys.exit(1 if malos else 0)


if __name__ == "__main__":
    main()
