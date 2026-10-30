"""BcsReplica oracle: consistency of the recorded goldens (run after legacy-oracle/record.sh, before sync_replica.py). Reads only; exit 1 on any inconsistency.
  * every case has its golden, with the provenance of the oracle, and the determinism report says 0 unstable cases;
  * every id a case cites (B-REP / D-REP) is in reglas_rep.py and every id of the catalog is cited (D-REP-22 is documentation only);
  * every collaborator the golden recorded was scripted by the case (no "SIN RESPUESTA CONFIGURADA"), no exception is an IllegalStateException of the oracle itself;
  * the cases that tag a legacy defect really show it in the golden (spot checks, see ESPERADOS).

  py -3 -I check_replica.py
"""
import json
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import reglas_rep  # noqa: E402

BASE = pathlib.Path(__file__).resolve().parent
CASES = BASE / "cases"
SIN_CASO = {"D-REP-22"}


def main():
    malos, n, citados = [], 0, set()
    familias = {}
    for d in sorted(CASES.glob("REP-*")):
        n += 1
        familias[d.name.split("-")[1]] = familias.get(d.name.split("-")[1], 0) + 1
        e = d / "expected.json"
        if not e.exists():
            malos.append("%s: no golden" % d.name)
            continue
        g = json.loads(e.read_text(encoding="utf-8"))
        c = json.loads((d / "case.json").read_text(encoding="utf-8"))
        if "executing the legacy cl.bcs.replica." not in g.get("_provenance", ""):
            malos.append("%s: golden without the provenance of the oracle" % d.name)
        for r in c["ref"]:
            citados.add(r)
            if r not in reglas_rep.REGLAS and r not in reglas_rep.DEFECTOS:
                malos.append("%s: cites %s which is not in reglas_rep.py" % (d.name, r))
        for col in g["colaboradores"]:
            if col.get("respuesta") == "SIN RESPUESTA CONFIGURADA":
                malos.append("%s: collaborator %s.%s had no scripted answer" % (d.name, col["colaborador"], col["metodo"]))
        ex = g.get("excepcion", "")
        if ex.startswith("IllegalStateException") or ex.startswith("IllegalArgumentException"):
            malos.append("%s: the oracle itself failed: %s" % (d.name, ex.split("\n")[0][:160]))
        if not c.get("tablas") and "ReplicaUtils.call" not in c["servicio"]:
            malos.append("%s: dumps no table" % d.name)
    for r in list(reglas_rep.REGLAS) + list(reglas_rep.DEFECTOS):
        if r not in citados and r not in SIN_CASO:
            malos.append("%s is in reglas_rep.py but no case cites it" % r)
    rep = BASE / "determinism.txt"
    if rep.exists():
        primera = rep.read_text(encoding="utf-8").splitlines()[0]
        print("determinism report:", primera)
        if "hard: 0" not in primera or "unstable: 0" not in primera:
            malos.append("determinism report: some case is unstable: " + primera)
    else:
        malos.append("no determinism.txt (run record.sh with ORACLE_REPEAT=20)")
    print("cases checked:", n, familias)
    for m in malos:
        print("!!", m)
    sys.exit(1 if malos else 0)


if __name__ == "__main__":
    main()
