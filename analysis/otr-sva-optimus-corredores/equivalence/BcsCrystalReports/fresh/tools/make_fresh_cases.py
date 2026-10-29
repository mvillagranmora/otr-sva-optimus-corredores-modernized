"""Builds ../../fresh-cases.json for compare.py from fresh/legacy/<case>/<fmt>.txt and fresh/new/<case>/<fmt>.txt, and checks the
freshness rule: every legacy facet is non-empty and its masked-sha256 / text differs from EVERY development facet
(out/legacy/*/*.txt of the 20 development cases). usage: py -3 -I make_fresh_cases.py <equivalence/BcsCrystalReports dir>"""
import json
import os
import re
import sys

FORMATS = ["pdf", "excel", "word", "rtf", "txt"]


def body(path):
    s = open(path, encoding="utf-8").read()
    return re.sub(r"^case: .*\n", "", s, count=1)     # the facet header carries the case name: compare the rest


def main(base):
    dev = set()
    for root, _d, files in os.walk(os.path.join(base, "out", "legacy")):
        for f in files:
            dev.add(body(os.path.join(root, f)))
    leg = os.path.join(base, "fresh", "legacy")
    cases, n, notfresh = [], 0, []
    for case in sorted(os.listdir(leg)):
        for fmt in FORMATS:
            p = os.path.join(leg, case, fmt + ".txt")
            if not os.path.exists(p):
                continue
            b = body(p)
            if len(b.strip()) < 40 or b in dev:
                notfresh.append("%s/%s" % (case, fmt))
            n += 1
            cases.append({"id": "F%03d" % n, "input": case, "title": "crystal fresh %s [%s]" % (case, fmt),
                          "legacy": "fresh/legacy/%s/%s.txt" % (case, fmt), "new": "fresh/new/%s/%s.txt" % (case, fmt),
                          "mask": []})
    doc = {"system": "otr-sva-optimus-corredores", "module": "BcsCrystalReports",
           "legacy": {"label": "FRESH inputs (boundary values, templates not used by development) through the unmodified legacy "
                               "cl.bcs.plataforma.report.api.ReportDocument, JDK 11, 2 passes >= 65 s apart (self-deterministic modulo masks)",
                      "command": "fresh/tools/gen_fresh.py; record.sh with EQUIV_DIR=<scratch copy of fresh/input>"},
           "new": {"label": "sidecar through POST /report (EquivalenceRestTest, X-Internal-Token), same template bytes and rows",
                   "command": "mvn -o test -Dtest=EquivalenceRestTest -Dequivalence.cases=<fresh input+goldens> -Dequivalence.out=<dir>"},
           "cases": cases}
    if os.path.exists(os.path.join(base, "fresh", "leftOut.json")):
        doc["leftOut"] = json.load(open(os.path.join(base, "fresh", "leftOut.json"), encoding="utf-8"))
    with open(os.path.join(base, "fresh-cases.json"), "w", encoding="utf-8", newline="\n") as fh:
        json.dump(doc, fh, ensure_ascii=False, indent=2)
    print("fresh-cases.json: %d case-formats; not fresh/empty: %s" % (n, notfresh))


if __name__ == "__main__":
    main(sys.argv[1])
