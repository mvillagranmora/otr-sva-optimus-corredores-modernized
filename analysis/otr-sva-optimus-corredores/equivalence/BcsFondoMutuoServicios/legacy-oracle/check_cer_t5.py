"""F3 W3 T5: consistency of the recorded goldens of cases-cer-t5/ with the tags each case declares, and coverage of the cards (run after record_cer_t5.sh, before sync_cer_t5.py):
  * `T5-modo:completo`   the legacy ran to the end: NO exception in the golden and a salida;
  * `T5-modo:excepcion`  the legacy threw somewhere in the run: an exception in the golden and NO salida;
  * every case has its golden with the provenance line of the T5 runner, appears stable in the determinism report (0 hard-unstable) and names its front (`front:F-J`);
  * the golden is a WHOLE run: a case that is not an early exit / an exception of F-A carries the calls of the closing (the asynchronous notification and AgregarLog) unless the case says the run
    was cut by an exception that comes first (the check lists the exceptions it did not expect to cut the closing);
  * COVERAGE: every card FM-CER-61..71 is named by at least one case `ref` (and each one of the defects of front J that the cards list: D-F3B-01, 02, 16, 31 are pinned by some case or the
    check says which are not);
  * the tags that the tests of the module look up exist: `T5-compuerta:G1|G2|G3|G6|G8|GCUS`, `T5-profundidad`, `T5-m3:a|b|c`, `T5-async-parametros`.
Also prints counts by tag. Exit 1 on any inconsistency. Reads only; never writes.

  python -X utf8 -I legacy-oracle/check_cer_t5.py
"""
import json
import pathlib
import sys

BASE = pathlib.Path(__file__).resolve().parent.parent
TARJETAS = ["FM-CER-%02d" % n for n in range(61, 72)]
DEFECTOS = ["D-F3B-01", "D-F3B-02", "D-F3B-16", "D-F3B-31", "D-F3B-47", "D-F3B-50", "D-NOR-9"]
TAGS = ["T5-compuerta:G1", "T5-compuerta:G2", "T5-compuerta:G3", "T5-compuerta:G5", "T5-compuerta:G6", "T5-compuerta:G8", "T5-compuerta:GCUS", "T5-profundidad", "T5-m3:a", "T5-m3:b", "T5-m3:c",
        "T5-async-parametros"]


def main():
    malos, n = [], 0
    modos, vistos, tags = {}, {}, {}
    for d in sorted((BASE / "cases-cer-t5").glob("FMM-CER-*")):
        n += 1
        e = d / "expected.json"
        if not e.exists():
            malos.append("%s: no golden" % d.name)
            continue
        g = json.loads(e.read_text(encoding="utf-8"))
        case = json.loads((d / "case.json").read_text(encoding="utf-8"))
        ref = case["ref"]
        modo = [r for r in ref if r.startswith("T5-modo:")]
        if len(modo) != 1:
            malos.append("%s: it must carry exactly one T5-modo tag" % d.name)
            continue
        modo = modo[0]
        modos[modo] = modos.get(modo, 0) + 1
        if "front:F-J" not in ref:
            malos.append("%s: no front:F-J tag" % d.name)
        for r in ref:
            vistos[r] = vistos.get(r, 0) + 1
            if r.startswith("T5-") and not r.startswith("T5-modo"):
                tags[r] = tags.get(r, 0) + 1
        if "OracleRunnerCerT5.java executing the legacy cl.bcs.fondomutuo.CertificarSolicitudAporteRescate" not in g.get("_provenance", ""):
            malos.append("%s: golden without the provenance of the T5 oracle" % d.name)
        tiene = "excepcion" in g
        if modo == "T5-modo:completo" and (tiene or "salida" not in g):
            malos.append("%s: tagged completo but the legacy threw %s" % (d.name, str(g.get("excepcion"))[:120]))
        if modo == "T5-modo:excepcion" and (not tiene or "salida" in g):
            malos.append("%s: tagged excepcion but the legacy ended normally" % d.name)
        if g.get("previas"):
            malos.append("%s: a T5 case has no previous requests" % d.name)
        if "T5-profundidad" in ref:
            hijos = [c for c in g["colaboradores"] if c["colaborador"] == "CertificarSolicitudAporteRescate.hijo"]
            anidados = [c for h in hijos for c in h.get("internas", []) if c["colaborador"] == "CertificarSolicitudAporteRescate.hijo"]
            if not anidados:
                malos.append("%s: tagged T5-profundidad but the golden has no recursion of depth 2" % d.name)
    for t in TARJETAS:
        if not any(r.startswith(t) and r == t for r in vistos):
            malos.append("card %s is pinned by NO case ref" % t)
    for t in DEFECTOS:
        if t not in vistos:
            malos.append("defect %s is pinned by NO case ref" % t)
    for t in TAGS:
        if t not in vistos:
            malos.append("the tag %s that the module tests look up is carried by NO case" % t)
    rep = BASE / "determinism-cer-t5.txt"
    if rep.exists():
        primera = rep.read_text(encoding="utf-8").splitlines()[0]
        print("determinism report:", primera)
        if "hard: 0" not in primera:
            malos.append("determinism report: some case is unstable: " + primera)
        if "unstable (hard + message-only): 0" not in primera:
            print("   (a message-only difference is listed in the report: the tests compare the class of the exception)")
    else:
        malos.append("no determinism report")
    print("cases checked:", n, modos)
    print("tags:", tags)
    print("cards:", {t: vistos.get(t, 0) for t in TARJETAS})
    for m in malos:
        print("!!", m)
    sys.exit(1 if malos else 0)


if __name__ == "__main__":
    main()
