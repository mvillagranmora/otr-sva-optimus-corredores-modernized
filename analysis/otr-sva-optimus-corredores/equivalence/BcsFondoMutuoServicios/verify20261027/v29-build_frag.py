"""verify 2026-10-29 (v29): writes v29-frag-fresh.json (list of cases, paths RELATIVE TO EQ), tmp/v29nor/check-fresh.json (same cases in the cases.json shape of compare.py, paths relative to tmp/v29nor/),
v29-fresh-status.json (which fresh inputs count: legacy output non empty and different from EVERY development output of Normal and from the earlier fresh Normal outputs) and
v29-gates-summary.json (new side under the Compuertas variants). No approval is written: the cases with a known difference only get a "note".  python -I v29-build_frag.py
"""
import json
import pathlib

HERE = pathlib.Path(__file__).resolve().parent
OUT = HERE / "fresh" / "nor" / "out"
REF = HERE / "tmp" / "v29nor" / "ref"

NOTAS = {
    "VF-NOR-11": ("G2 (Spot) closed in the new module: the module raises ServicioNoServidoException (ROLLBACK_503, the router repeats the request in TomEE) where the legacy, run by the oracle, reaches "
                  "the Mesa query and raises SQLException (the oracle H2 has no MES.Mesa table); same gate behaviour as the development case FMM-NOR-07 (NormalCompuertasYDefectosTest). Output, tables, "
                  "collaborator calls and session are the same up to the gate; only the exception class and the servlet decision differ. NOT approved: reported as a difference."),
    "VF-NOR-36": ("Only the session section differs: sesion.tablasLocalesSinDrop = ['#p_BloqueoSolicitudAporteRescate'] in the legacy and [] in the new code. The request has two identical solicitudes "
                  "(same portfolio, series, regime, type, amount and quantity, different IdInterno), so the legacy lock procedure fails on the duplicate key AFTER CREATE LOCAL TEMPORARY TABLE and leaves the "
                  "session-local table undropped; BloqueoSolicitudJdbc.crear drops it on failure (architecture review H1, covered by BloqueoSolicitudFalloTest). Output, tables, calls, decision are identical. "
                  "NOT approved here: reported as a difference."),
}


def vacia(t):
    s = t.split("== salidaCruda\n", 1)[1].split("\n== ", 1)[0].replace(" ", "")
    exc = t.rsplit("== excepcion\n", 1)[1].strip()
    return exc == "<none>" and '"P_OUT_RESULTADO":[]' in s and '"P_OUT_MENSAJES":[]' in s


def main():
    dev = {p.read_text(encoding="utf-8") for p in (REF / "dev").glob("*.txt")}
    prev = {p.read_text(encoding="utf-8") for p in (REF / "prev").glob("*.txt")}
    status, cases = [], []
    for d in sorted((HERE / "fresh" / "nor" / "cases").iterdir()):
        ref = json.loads((d / "case.json").read_text(encoding="utf-8"))["ref"]
        lp, np_ = OUT / "legacy" / (d.name + ".txt"), OUT / "new" / (d.name + ".txt")
        st = {"id": d.name, "ref": ref}
        if not lp.exists():
            st.update(counts=False, why="the legacy oracle produced no dump")
            status.append(st)
            continue
        t = lp.read_text(encoding="utf-8")
        if vacia(t):
            st.update(counts=False, why="legacy output empty")
        elif t in dev:
            st.update(counts=False, why="legacy output equals a development output")
        elif t in prev:
            st.update(counts=False, why="legacy output equals an output of an earlier fresh Normal input (FRESH-NOR-*)")
        else:
            st["counts"] = True
        st["equal"] = t == np_.read_text(encoding="utf-8")
        status.append(st)
        c = {"id": d.name, "input": d.name, "title": "CrearSolicitudAporteRescateNormal fresh input %s: %s" % (d.name, ref),
             "legacy": "verify20261027/fresh/nor/out/legacy/%s.txt" % d.name, "new": "verify20261027/fresh/nor/out/new/%s.txt" % d.name, "mask": []}
        if d.name in NOTAS:
            c["note"] = NOTAS[d.name]
        cases.append(c)
    (HERE / "v29-frag-fresh.json").write_text(json.dumps(cases, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    (HERE / "v29-fresh-status.json").write_text(json.dumps(status, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    chk = {"system": "otr-sva-optimus-corredores", "module": "BcsFondoMutuoServicios",
           "legacy": {"label": "unmodified legacy CrearSolicitudAporteRescateNormal (v29-record_nor.sh = record.sh + ORACLE_CASES/REPEAT/REPORT), JDK 25, UTC, 20 repetitions", "command": "v29-run_oracle_nor.sh"},
           "new": {"label": "modernized BcsFondoMutuoServicios target/classes through CasoNormal (profile ABIERTAS), same H2 schema, seeds and scripted collaborators", "command": "java Volcar29 fresh"},
           "cases": [dict(c, legacy=c["legacy"].replace("verify20261027/", "../../", 1), new=c["new"].replace("verify20261027/", "../../", 1)) for c in cases]}
    (HERE / "tmp" / "v29nor" / "check-fresh.json").write_text(json.dumps(chk, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    # Compuertas variants: new side only, against the CasoNormal run
    summ = {}
    for v in ("harness", "cn1", "cn0", "c0n1", "cn1ce1", "cn0ce1"):
        dif = [p.stem for p in sorted((HERE / "fresh" / "nor" / "gates" / v).glob("*.txt")) if p.read_text(encoding="utf-8") != (OUT / "new" / p.name).read_text(encoding="utf-8")]
        summ[v] = {"cases": len(list((HERE / "fresh" / "nor" / "gates" / v).glob("*.txt"))), "differ_from_CasoNormal_run": dif}
    (HERE / "v29-gates-summary.json").write_text(json.dumps(summ, indent=1) + "\n", encoding="utf-8", newline="\n")
    n = sum(1 for s in status if s["counts"])
    print("inputs", len(status), "count", n, "equal", sum(1 for s in status if s.get("equal")), "differ", [s["id"] for s in status if "equal" in s and not s["equal"]])
    print("not counting:", [(s["id"], s["why"]) for s in status if not s["counts"]])


if __name__ == "__main__":
    main()
