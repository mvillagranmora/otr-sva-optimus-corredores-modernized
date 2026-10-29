"""Builds cases.json for compare.py from the facets written by `mvn test -Dequivalence.out=<dir>/out`.

usage: py -3 make_cases.py <equivalence/BcsCrystalReports dir>      (expects <dir>/out/legacy/<case>/<fmt>.txt and out/new/...)
then : py -3 <plugin>/scripts/compare.py <dir>/cases.json
"""
import json
import os
import sys

FORMATS = ["pdf", "excel", "word", "rtf", "txt"]


def main(base, out="out", out_file="cases.json", rest=False):
    leg = os.path.join(base, out, "legacy")
    cases = []
    n = 0
    for case in sorted(os.listdir(leg)):
        for fmt in FORMATS:
            if not os.path.exists(os.path.join(leg, case, fmt + ".txt")):
                continue
            n += 1
            cases.append({"id": "%05d" % n, "input": case.split("_")[0], "title": "crystal %s [%s]" % (case, fmt),
                          "legacy": "%s/legacy/%s/%s.txt" % (out, case, fmt), "new": "%s/new/%s/%s.txt" % (out, case, fmt),
                          "mask": []})
    doc = {
        "system": "otr-sva-optimus-corredores",
        "module": "BcsCrystalReports",
        "legacy": {
            "label": "facets of the goldens recorded by EXECUTING the unmodified legacy cl.bcs.plataforma.report.api.ReportDocument "
                     "(+ BcsUtils ListMapResultSet), Crystal JRC 14.1, JDK 11, headless, H2 stand-in for HANA, fake rows",
            "command": "src/test/legacy-recorder/record.sh (README.md in src/test/java/cl/bcs/reports/equiv)"},
        "new": {
            "label": "sidecar cl.bcs.reports.report.ReportDocument (via POST /reports/generate where the contract allows, "
                     "else the engine API as the legacy servlet calls it), same JVM flags, template bytes and rows",
            "command": "mvn -o -f modernized/otr-sva-optimus-corredores/BcsCrystalReports/pom.xml test -Dequivalence.out=<dir>/out"},
        "note": "Each case is the canonical facet of one rendered format: masked length, masked SHA-256 and the extracted text "
                "(PDF: page count + Tj text). Masks are applied inside the facet (PDF /Title uuid, print date/time text, font-subset "
                "streams/widths/ToUnicode/lengths/xref; xls print date/time doubles; RTF \\creatim; literal dates of the recording "
                "day and today; times like 12:32PM). Not covered: HANA data, Java 8 runtime, Linux fonts, the other ~130 templates.",
        "cases": cases,
    }
    if rest:
        doc["new"] = {
            "label": "sidecar through the NEW HTTP contract: POST /report with the legacy servlet's JSON body (all 21 cases, multi-row, "
                     "typed fields, subreports, images, pdf417), X-Internal-Token, same JVM flags, template bytes and rows",
            "command": "mvn -o -f modernized/otr-sva-optimus-corredores/BcsCrystalReports/pom.xml test -Dtest=EquivalenceRestTest "
                       "-Dequivalence.out=<dir>/out-rest"}
    with open(os.path.join(base, out_file), "w", encoding="utf-8", newline="\n") as fh:
        json.dump(doc, fh, ensure_ascii=False, indent=2)
    print("%s: %d cases" % (out_file, len(cases)))


if __name__ == "__main__":
    main(*sys.argv[1:4], rest=len(sys.argv) > 3)
