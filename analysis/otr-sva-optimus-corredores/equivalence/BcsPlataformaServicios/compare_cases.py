#!/usr/bin/env python3
"""Adds the compare.py fields to cases.json (the oracle inventory), keeping every inventory field as it is:
per case "legacy" (the golden cases/<id>/expected.json), "new" (out/new/<id>.json, the NewSubject dump), a mask for the
harness _provenance label, and for PLA-ESM-29 the difference the user approved (DEV-A3, approved_differences.json).
Idempotent. Run from anywhere: py -3 -I compare_cases.py
The new side is produced by:
  ORACLE_SUBJECT=cl.bcs.oracle.NewSubject ORACLE_OUT=<this dir>/out/new ORACLE_EXTRA_SOURCES=new-subject/cl/bcs/oracle/NewSubject.java \
  ORACLE_EXTRA_CP=modernized/otr-sva-optimus-corredores/BcsPlataformaServicios/target/classes bash legacy-oracle/record.sh"""
import json
import os

HERE = os.path.dirname(os.path.abspath(__file__))
P = os.path.join(HERE, "cases.json")
doc = json.load(open(P, encoding="utf-8"))
approved = {e["case"].split("_")[0]: e for e in json.load(open(os.path.join(HERE, "approved_differences.json"), encoding="utf-8"))["approved"]}
MASK = [{"regex": "\"_provenance\": \"[^\"\\n]*\"", "why": "harness label naming the subject that ran (legacy golden vs new dump), not product output"}]

doc["legacy"] = {"label": "goldens recorded by executing the unmodified legacy cl.bcs.plataforma.cre.ConsultarEstadoModulo (legacy-oracle/record.sh + OracleRunner, H2 MODE=LEGACY, JVM UTC)",
                 "command": "bash legacy-oracle/record.sh  (writes cases/<id>/expected.json)"}
doc["new"] = {"label": "modernized BcsPlataformaServicios ConsultarEstadoModuloHandler called through new-subject/NewSubject on the same seeded H2",
              "command": "ORACLE_SUBJECT=cl.bcs.oracle.NewSubject ORACLE_OUT=out/new ORACLE_EXTRA_SOURCES=new-subject/cl/bcs/oracle/NewSubject.java ORACLE_EXTRA_CP=<module>/target/classes bash legacy-oracle/record.sh"}
for c in doc["cases"]:
    cid = c["id"]
    c["legacy"] = "cases/%s/expected.json" % cid
    c["new"] = "out/new/%s.json" % cid
    c["mask"] = MASK
    a = approved.get(cid.split("_")[0])
    if a:
        c["approvedDifference"] = "%s: %s Must still hold (checked by compare_new.py): %s" % (a["approval"], a["reason"], json.dumps(a["mustHold"]))
with open(P, "w", encoding="utf-8", newline="\n") as fh:
    json.dump(doc, fh, indent=2, ensure_ascii=False)
    fh.write("\n")
print(len(doc["cases"]), "cases;", sum(1 for c in doc["cases"] if "approvedDifference" in c), "approved")
