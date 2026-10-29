#!/usr/bin/env python3
"""Verdict legacy goldens vs the NEW ConsultarEstadoModulo, with APPROVED differences (approved_differences.json).

  py -3 -I compare_new.py --new <dumpDir> [--out EQUIVALENCE_NEW.json]

Uses compare.py unchanged (canonical text comparison, self check). A case is "same" when the canonical dumps are identical, "approved" when it is
not identical but the ONLY differences are the masked fields of an approved entry AND that entry's mustHold conditions hold on the new dump,
and "differs" otherwise (also "missing"). Goldens are read-only. Exit 0 only if every case is same or approved.
"""
import argparse
import importlib.util
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
spec = importlib.util.spec_from_file_location("compare_legacy", os.path.join(HERE, "compare.py"))
cmp = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cmp)


def masked(text, fields):
    d = json.loads(text)
    d.pop("_provenance", None)
    for f in fields:
        if f in d:
            d[f] = "<MASKED>"
    return json.dumps(d, ensure_ascii=False, indent=1)


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    ap = argparse.ArgumentParser()
    ap.add_argument("--new", required=True)
    ap.add_argument("--out")
    ap.add_argument("--label", default="new")
    a = ap.parse_args()
    approved = {e["case"].split("_")[0]: e for e in json.load(open(os.path.join(HERE, "approved_differences.json"), encoding="utf-8"))["approved"]}
    goldens = cmp.load_goldens("PLA-ESM")
    sc = cmp.self_check(goldens)
    print("comparator self check:", "PASSED" if sc["passed"] else "FAILED")
    if not sc["passed"]:
        return 2
    tot = {"cases": len(goldens), "same": 0, "approved": 0, "differs": 0, "missing": 0}
    cases = []
    for cid, g in goldens.items():
        key = cid.split("_")[0]
        p = os.path.join(a.new, cid + ".json")
        e = {"id": cid}
        if not os.path.exists(p):
            e.update(verdict="missing", reason="no dump " + p)
            tot["missing"] += 1
        else:
            n = open(p, encoding="utf-8").read()
            if cmp.same(g, n):
                e.update(verdict="same", reason="identical")
                tot["same"] += 1
            elif key in approved:
                ap_ = approved[key]
                fields = [ap_["field"]]
                nd = json.loads(n)
                must = ap_.get("mustHold", {})
                ok = masked(g, fields) == masked(n, fields)
                if must.get("newHasExcepcion"):
                    ok = ok and "excepcion" in nd
                txt = str(nd.get("excepcion", ""))
                ok = ok and not any(s in txt for s in must.get("newExcepcionMustNotContain", []))
                if ok:
                    e.update(verdict="approved", approval=ap_["approval"], reason=ap_["reason"], masked=fields)
                    tot["approved"] += 1
                else:
                    e.update(verdict="differs", reason="approved difference does not hold for " + key)
                    tot["differs"] += 1
            else:
                e.update(verdict="differs", reason="not identical and no approved difference")
                tot["differs"] += 1
        cases.append(e)
    verdict = {"system": "otr-sva-optimus-corredores", "module": "BcsPlataformaServicios", "class": "cl.bcs.plataforma.cre.ConsultarEstadoModulo",
               "subject": a.label, "comparatorSelfCheck": sc, "totals": tot, "cases": cases}
    if a.out:
        with open(a.out, "w", encoding="utf-8", newline="\n") as f:
            json.dump(verdict, f, indent=2, ensure_ascii=False)
            f.write("\n")
    print("totals:", json.dumps(tot))
    for c in cases:
        if c["verdict"] != "same":
            print(" ", c["verdict"].upper(), c["id"], "-", c.get("approval", c["reason"])[:120])
    return 0 if tot["same"] + tot["approved"] == tot["cases"] else 1


if __name__ == "__main__":
    sys.exit(main())
