#!/usr/bin/env python3
"""Compares the goldens (cases/<case>/expected.json, recorded from the legacy) with a dump directory (<dir>/<case>.json) produced by
another subject (the new implementation, or a mutant) with the same OracleRunner and dump format.

  py -3 -I compare.py --new <dumpDir> [--out EQUIVALENCE.json] [--self-check] [--prefix FMM-]

Comparison = canonical text of the whole dump without "_provenance" (key ORDER is kept: the order of the keys of the output JSON is part of
the contract). Exit code 0 only when every golden case has a dump and is identical; 1 otherwise; 2 when the comparator self check fails.
--self-check first proves that the comparator can fail: every golden is compared with a copy of itself changed by ONE byte and must be
reported different (same idea as the self check of the earlier modules).
"""
import argparse
import glob
import hashlib
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))


def canon(text):
    d = json.loads(text)
    d.pop("_provenance", None)
    return json.dumps(d, ensure_ascii=False, indent=1)


def sha(s):
    return hashlib.sha256(s.encode("utf-8")).hexdigest()


def load_goldens(prefix):
    out = {}
    for p in sorted(glob.glob(os.path.join(HERE, "cases", prefix + "*", "expected.json"))):
        out[os.path.basename(os.path.dirname(p))] = open(p, encoding="utf-8").read()
    return out


def same(golden_text, other_text):
    """THE comparison used for every case (and by the self check). Unparseable text counts as different."""
    try:
        return canon(golden_text) == canon(other_text)
    except ValueError:
        return False


def self_check(goldens):
    bad = []
    for cid, text in goldens.items():
        # positive control (a golden equals itself, also with different formatting) and negative control (one byte changed)
        if not same(text, text) or not same(text, json.dumps(json.loads(text))):
            bad.append(cid + " (identical copy reported different)")
        raw = text
        i = len(raw) // 2
        while i < len(raw) and not raw[i].isalnum():
            i += 1
        mutated = raw[:i] + ("1" if raw[i] != "1" else "2") + raw[i + 1:]
        if same(text, mutated):
            bad.append(cid + " (one-byte change not noticed)")
    return {"passed": not bad, "cases": len(goldens), "notDetected": bad,
            "detail": "each golden equals itself and, compared with a copy changed by one byte, is reported as different" if not bad
            else "comparator failure: " + ", ".join(bad)}


def main():
    sys.stdout.reconfigure(encoding="utf-8")   # -I ignores PYTHONIOENCODING; the reasons quote accented module codes
    ap = argparse.ArgumentParser()
    ap.add_argument("--new", required=True, help="directory with <case>.json dumps of the subject under comparison")
    ap.add_argument("--out", help="write the verdict JSON here")
    ap.add_argument("--self-check", action="store_true")
    ap.add_argument("--prefix", default="FMM-")
    ap.add_argument("--label", default="new")
    a = ap.parse_args()

    goldens = load_goldens(a.prefix)
    if not goldens:
        print("no golden cases found", file=sys.stderr)
        return 2
    sc = None
    if a.self_check:
        sc = self_check(goldens)
        print("self check:", "PASSED" if sc["passed"] else "FAILED", "-", sc["detail"])
        if not sc["passed"]:
            return 2

    cases, tot = [], {"cases": len(goldens), "same": 0, "differs": 0, "missing": 0}
    for cid, gtext in goldens.items():
        g = canon(gtext)
        p = os.path.join(a.new, cid + ".json")
        entry = {"id": cid, "legacySha256": sha(g)}
        if not os.path.exists(p):
            entry.update(verdict="missing", reason="no dump " + p)
            tot["missing"] += 1
        else:
            ntext = open(p, encoding="utf-8").read()
            try:
                n = canon(ntext)
            except ValueError:
                n = "<unparseable dump>"
            entry["newSha256"] = sha(n)
            if same(gtext, ntext):
                entry.update(verdict="same", reason="identical, %d bytes" % len(g.encode("utf-8")))
                tot["same"] += 1
            else:
                gl, nl = g.splitlines(), n.splitlines()
                k = next((i for i in range(min(len(gl), len(nl))) if gl[i] != nl[i]), min(len(gl), len(nl)))
                entry.update(verdict="differs", reason="first difference at canonical line %d: golden %r vs %s %r" % (
                    k + 1, gl[k].strip() if k < len(gl) else "<end>", a.label, nl[k].strip() if k < len(nl) else "<end>"))
                tot["differs"] += 1
        cases.append(entry)

    verdict = {"system": "otr-sva-optimus-corredores", "module": "BcsFondoMutuoServicios", "class": "cl.bcs.fondomutuo.ManejadorSolicitudAporteRescate + CrearSolicitudAporteRescateNormal",
               "subject": a.label, "selfCheck": sc, "totals": tot, "cases": cases}
    if a.out:
        with open(a.out, "w", encoding="utf-8", newline="\n") as f:
            json.dump(verdict, f, indent=2, ensure_ascii=False)
            f.write("\n")
    print("totals:", json.dumps(tot))
    for c in cases:
        if c["verdict"] != "same":
            print(" ", c["verdict"].upper(), c["id"], "-", c["reason"][:160])
    return 0 if tot["same"] == tot["cases"] else 1


if __name__ == "__main__":
    sys.exit(main())
