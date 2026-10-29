"""F2 W1: the fresh Normal inputs (fresh/cases/FRESH-NOR-*) through the NEW Normal with G1 lifted (subject4a.sh -> tools-w1a/new-fresh/<id>.json) against the
dumps of the UNMODIFIED legacy recorded in step 5 (fresh/legacy/<id>/<id>.json). Same classification as compare_nor.py: same | approved-text (only the
exception text differs, same class; a driver SQLException subclass counts as SQLException) | DIFFERS. Writes tools-w1a/FRESH_NOR_W1A.json.

    py -3 tools-w1a/fresh_w1a.py
"""
import json
import pathlib
import sys

BASE = pathlib.Path(__file__).resolve().parent.parent


def norm(d, solo_clase):
    d = dict(d)
    d.pop("_provenance", None)
    if solo_clase and "excepcion" in d:
        c = d["excepcion"].split(":")[0]
        d["excepcion"] = "SQLException" if "SQL" in c else c
    return json.dumps(d, ensure_ascii=False, sort_keys=False)


def main():
    res, cuenta = [], {}
    for n in sorted((BASE / "tools-w1a" / "new-fresh").glob("FRESH-NOR-*.json")):
        cid = n.stem
        leg = json.loads((BASE / "fresh" / "legacy" / cid / (cid + ".json")).read_text(encoding="utf-8"))
        new = json.loads(n.read_text(encoding="utf-8"))
        if norm(leg, False) == norm(new, False):
            r = "same"
        elif norm(leg, True) == norm(new, True):
            r = "approved-text"
        elif leg.get("sesion", {}).get("tablasLocalesSinDrop") == ["#p_BloqueoSolicitudAporteRescate"] and \
                norm(dict(leg, sesion=dict(leg["sesion"], tablasLocalesSinDrop=[])), False) == norm(new, False):
            r = "approved-DEV-6-H1"   # step 6 (architecture review H1): the new code drops the leaked lock table; nothing else differs
        else:
            r = "DIFFERS"
        cuenta[r] = cuenta.get(r, 0) + 1
        res.append({"case": cid, "result": r, "legacy": leg.get("excepcion", leg.get("decisionServlet")), "new": new.get("excepcion", new.get("decisionServlet"))})
    out = {"summary": cuenta, "cases": res}
    (BASE / "tools-w1a" / "FRESH_NOR_W1A.json").write_text(json.dumps(out, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    print(cuenta)
    for x in res:
        if x["result"] != "same":
            print(x["case"], x["result"], str(x["legacy"])[:90], "<>", str(x["new"])[:90])
    sys.exit(1 if "DIFFERS" in cuenta else 0)


if __name__ == "__main__":
    main()
