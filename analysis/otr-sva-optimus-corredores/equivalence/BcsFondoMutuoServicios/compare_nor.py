#!/usr/bin/env python3
"""Step 4A: compares the goldens of Normal (cases/FMM-NOR-*/expected.json, recorded from the legacy) with the dump of the NEW implementation
(<dir>/<case>.json from legacy-oracle/subject4a.sh) with the strict comparator of compare.py (whole dump, key order included), then classifies each case:

  same                 identical dump
  approved-DEV-F4      the new module answers 503 of a closed gate (G1/G2: F2 classes) where the legacy ran: listed in APROBADAS
  approved-text        only the TEXT of the exception differs (same class): the JVM "helpful NullPointerException" names compiler locals
  DIFFERS              anything else (exit code 1)

  py -3 -I compare_nor.py --new <dumpDir> [--out EQUIVALENCE_NOR.json]
"""
import argparse
import json
import os
import sys

import compare

APROBADAS = {"FMM-NOR-06_aporte-medio-no-cin-tesoreria": "G1", "FMM-NOR-07_aporte-usd-genera-spot": "G2",
             "FMM-NOR-52_dos-aportes-tra-tesoreria": "G1"}


def sin_texto_de_excepcion(texto):
    d = json.loads(texto)
    d.pop("_provenance", None)
    if "excepcion" in d:
        clase = d["excepcion"].split(":")[0]
        # the legacy SQL class rethrows the driver failure as a plain java.sql.SQLException (with the query text); the new SqlNombrado lets the H2 subclass through
        d["excepcion"] = "SQLException" if "SQL" in clase else clase
    return json.dumps(d, ensure_ascii=False, indent=1)


def clasificar(cid, golden, nuevo):
    if compare.same(golden, nuevo):
        return "same", ""
    n = json.loads(nuevo)
    if cid in APROBADAS and n.get("excepcion", "").startswith("ServicioNoServidoException"):
        return "approved-DEV-F4", "gate " + APROBADAS[cid] + ": " + n["excepcion"]
    if "excepcion" in json.loads(golden) and sin_texto_de_excepcion(golden) == sin_texto_de_excepcion(nuevo):
        return "approved-text", json.loads(golden)["excepcion"][:80] + " <> " + n["excepcion"][:80]
    return "DIFFERS", ""


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--new", required=True)
    ap.add_argument("--out")
    a = ap.parse_args()
    goldens = compare.load_goldens("FMM-NOR-")
    filas, malos = [], 0
    for cid, g in goldens.items():
        p = os.path.join(a.new, cid + ".json")
        if not os.path.exists(p):
            filas.append({"case": cid, "result": "MISSING"})
            malos += 1
            continue
        res, nota = clasificar(cid, g, open(p, encoding="utf-8").read())
        malos += res == "DIFFERS"
        filas.append({"case": cid, "result": res, "note": nota})
    resumen = {}
    for f in filas:
        resumen[f["result"]] = resumen.get(f["result"], 0) + 1
    doc = {"comparator": "compare.py strict (whole dump) + classification", "summary": resumen, "cases": filas}
    print(json.dumps({"summary": resumen}, indent=1))
    for f in filas:
        if f["result"] != "same":
            print(f["case"], f["result"], f.get("note", ""))
    if a.out:
        open(a.out, "w", encoding="utf-8", newline="\n").write(json.dumps(doc, indent=1, ensure_ascii=False) + "\n")
    sys.exit(1 if malos else 0)


if __name__ == "__main__":
    main()
