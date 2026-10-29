"""Generates src/test/resources/crystal/equiv/<Dominio>_<Plantilla>/{spec.json,data.json} (fake, deterministic rows).

Prereq: probe.sh target/probe_all.json templates/**/*.rpt   (Crystal API dump of tables/fields/subreports/pictures)
usage : py -3 -I gen_cases.py [case-name-substring ...]
The field list comes from the .rpt itself (tables of the Crystal database controller); types and lengths are refined from
the .sql that the template's CALL names (gen_sql.py). To change the selection edit cases.py."""
import json
import os
import re
import sys

import gen_sql
import gen_values
from cases import CASES

HERE = os.path.dirname(os.path.abspath(__file__))
MOD = os.path.abspath(os.path.join(HERE, "..", "..", ".."))
OUT = os.path.join(MOD, "src", "test", "resources", "crystal", "equiv")
FORMATS = ["pdf", "excel", "word", "rtf", "txt"]


def dash_fields(formulas):
    """Fields a formula Split()s on "-" (e.g. FechaString): they need d-M-yyyy text, not our default dd/MM/yyyy."""
    return set(m.group(1) for f in formulas.values() for m in re.finditer(r'Split\(\{\w+\.(\w+)\}\s*,\s*"-"\)', f))


def rows_for(table, domain, n, dashes=()):
    sqlp = gen_sql.find_sql(os.path.join(MOD, "templates"), domain, table.get("command"))
    cols = gen_sql.columns(sqlp)
    schema, rows = {}, [dict() for _ in range(n)]
    hit = 0
    for name, jrc in table["fields"].items():
        info = cols.get(name)
        hit += 1 if info else 0
        for i in range(n):
            v, st = gen_values.synth(name, jrc, info, i)
            if name in dashes:
                v = "%d-%d-2025" % (5 + i, 3 + i)   # not dd-MM-yyyy: the PDF print-date mask must never see data
            rows[i][name] = v
            schema[name] = st
    return schema, rows, os.path.basename(sqlp) if sqlp else None, hit, len(table["fields"])


def main():
    probe = {o["rpt"]: o for o in json.load(open(os.path.join(MOD, "target", "probe_all.json"), encoding="utf-8"))}
    flt = sys.argv[1:]
    for domain, rel, nrows in CASES:
        name = domain + "_" + re.sub(r"[^A-Za-z0-9_]", "_", os.path.splitext(os.path.basename(rel))[0])
        if flt and not any(f in name for f in flt):
            continue
        key = "templates/%s/%s" % (domain, rel)
        o = probe[key]
        schema, rows, sqlf, hit, tot = rows_for(o["tables"][0], domain, nrows, dash_fields(o.get("formulas", {})))
        data = {"schema": schema, "rows": rows}
        if o["subreports"]:
            data["subreports"] = {}
            for sn, tabs in o["subreports"].items():
                s2, r2, _sq, _h, _t = rows_for(tabs[0], domain, 2)
                data["subreports"][sn] = {"schema": s2, "rows": r2}
        params = []
        for pic in sorted(o["pictures"], key=lambda p: p.lower().startswith("pdf417")):
            if pic.lower().startswith("pdf417"):
                params.append({"type": "pdf417", "name": pic, "data": "BCS|FOLIO-000123|20250310|CLP|1234567"})
            else:
                params.append({"type": "image", "name": pic, "data": gen_values.png_b64()})
        spec = {"template": key, "formats": FORMATS, "params": params, "sql": sqlf,
                "note": "fields from the .rpt (Crystal API); %d of %d typed from the .sql" % (hit, tot)}
        d = os.path.join(OUT, name)
        os.makedirs(d, exist_ok=True)
        for fn, obj in (("spec.json", spec), ("data.json", data)):
            with open(os.path.join(d, fn), "w", encoding="utf-8", newline="\n") as fh:
                json.dump(obj, fh, ensure_ascii=False, indent=1)
        print("%-45s rows=%d fields=%d sql=%s typed-from-sql=%d/%d subs=%s params=%s" % (
            name, nrows, tot, sqlf, hit, tot, list(o["subreports"]), [p["name"] for p in params]))


if __name__ == "__main__":
    main()
