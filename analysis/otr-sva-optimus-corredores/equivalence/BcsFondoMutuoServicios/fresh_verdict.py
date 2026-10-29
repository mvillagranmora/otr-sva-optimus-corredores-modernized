#!/usr/bin/env python3
"""Step 5: packs the dumps of the FRESH inputs (legacy and new, same OracleRunner) under fresh/legacy|new/<id>/<id>.json, writes fresh/fresh-cases.json
(schema of the skill's compare.py), runs that compare.py and classifies every difference. NOTHING is edited by hand: the dumps are copied byte for byte.

  py -3 fresh_verdict.py <legacyDumpDir> <newDumpDir> <g0DumpDir>

g0DumpDir = dump of the NEW Manejador with the F1 gate (-Doracle.listas=NORMAL): the cases whose request carries a non served list are listed a second time
as <id>@G0 (legacy output vs the 503) so that the approved DEV-F4 difference is shown, with the proof that the 503 wrote nothing.

Mask (only what legitimately varies): the "_provenance" text (names the subject that produced the dump). Nothing else.
Classification of a difference (pass 2, only for cases that differ in pass 1; each approval carries its reason in approvedDifference):
  DEV-F4  the new dump is a ServicioNoServidoException (gate closed) AND every table of the new dump is empty AND no collaborator was called that writes;
  text    both dumps hold an exception of the same class and the only difference is the exception TEXT (JVM helpful NPE names compiler locals; SQLException subclass);
  anything else stays "differs" = a FINDING for a person.
"""
import glob
import json
import os
import shutil
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
FRESH = os.path.join(HERE, "fresh")
SKILL = os.path.join(os.path.expanduser("~"), ".claude", "plugins", "cache", "claude-plugins-official", "code-modernization", "1.0.0", "scripts", "compare.py")
MASK = [{"regex": "\"_provenance\": \"(?:[^\"\\\\]|\\\\.)*\"", "why": "provenance text names the subject that produced the dump (legacy oracle vs new subject): not behaviour"}]
WRITERS = ("CrearIngresoEgreso", "CrearOperacionSpot", "AgregarLog", "InscribirProcesoBatch", "NotificarEstadoOrdenesOWP")


def pack(src_dir, side, ids):
    for cid in ids:
        if side == "new-g0" and not os.path.exists(os.path.join(src_dir, cid + ".json")):
            continue   # the G0 run only exists for the Manejador cases
        d = os.path.join(FRESH, side, cid)
        os.makedirs(d, exist_ok=True)
        shutil.copyfile(os.path.join(src_dir, cid + ".json"), os.path.join(d, cid + ".json"))


def entry(cid, title, newside, newid=None, extra=None):
    e = {"id": extra or cid, "input": cid, "title": title, "legacy": "legacy/%s/%s.json" % (cid, cid), "new": "%s/%s/%s.json" % (newside, newid or cid, newid or cid), "mask": MASK}
    return e


def load(path):
    return json.load(open(path, encoding="utf-8"))


def clase(exc):
    c = exc.split(":")[0]
    return "SQLException" if "SQL" in c else c


def classify(e, legacy, new):
    """returns the approvedDifference text or '' (a finding)"""
    if "excepcion" in new and new["excepcion"].startswith("ServicioNoServidoException"):
        vacias = all(len(v) == 0 for v in new["tablas"].values()) and not new["sesion"]["tablasLocalesSinDrop"]
        escritores = [c["colaborador"] for c in new["colaboradores"] if c["colaborador"] in WRITERS]
        if vacias and not escritores:
            return "DEV-F4 (approved by the user 2026-10-21): gate closed -> %s; the new dump has no request, no lock and no AGF row, no local table left and no write collaborator called" % new["excepcion"][:110]
        return ""
    if "excepcion" in legacy and "excepcion" in new and clase(legacy["excepcion"]) == clase(new["excepcion"]):
        a, b = dict(legacy), dict(new)
        for d in (a, b):
            d.pop("_provenance", None)
            d["excepcion"] = clase(d["excepcion"])
        if json.dumps(a, sort_keys=False) == json.dumps(b, sort_keys=False):
            return "exception TEXT only (class %s identical, every other field identical): JVM helpful NullPointerException/SQLException message of the JVM/driver (approved by the user)" % clase(new["excepcion"])
    return ""


def main():
    leg, new, g0 = sys.argv[1:4]
    ids = sorted(os.path.basename(p) for p in glob.glob(os.path.join(FRESH, "cases", "*")))
    for side, src in (("legacy", leg), ("new", new), ("new-g0", g0)):
        pack(src, side, ids)
    cases = []
    for cid in ids:
        title = load(os.path.join(FRESH, "cases", cid, "case.json"))["ref"]
        cases.append(entry(cid, title, "new"))
        ent = load(os.path.join(FRESH, "cases", cid, "case.json"))["entrada"]
        if cid.startswith("FRESH-MAN") and any(ent.get(k) for k in ent if k.startswith("p_Solicitud") and k != "p_SolicitudesAporteRescateNormal"):
            cases.append(entry(cid, title + " [F1 gate G0: only Normal lists served]", "new-g0", extra=cid + "@G0"))
    doc = {"system": "otr-sva-optimus-corredores / BcsFondoMutuoServicios solicitud",
           "legacy": {"label": "unmodified legacy cl.bcs.fondomutuo.* on H2 (OracleRunner, registered doubles)", "command": "legacy-oracle/record.sh with -Doracle.cases=fresh/cases"},
           "new": {"label": "new Normal (NormalSubject) / new Manejador (ManejadorNuevoSubject) over the same H2 and doubles", "command": "legacy-oracle/subject4a.sh, run4b.sh"},
           "cases": cases}
    p = os.path.join(FRESH, "fresh-cases.json")
    json.dump(doc, open(p, "w", encoding="utf-8", newline="\n"), indent=1, ensure_ascii=False)
    out1 = os.path.join(FRESH, "FRESH_EQUIVALENCE_pass1.json")
    subprocess.run([sys.executable, "-I", SKILL, p, "--out", out1, "--quiet"], check=False)
    r1 = {c["id"]: c for c in load(out1)["cases"]}
    for c in cases:
        if r1[c["id"]]["verdict"] == "differs":
            why = classify(c, load(os.path.join(FRESH, c["legacy"])), load(os.path.join(FRESH, c["new"])))
            if why:
                c["approvedDifference"] = why
    json.dump(doc, open(p, "w", encoding="utf-8", newline="\n"), indent=1, ensure_ascii=False)
    out2 = os.path.join(FRESH, "FRESH_EQUIVALENCE.json")
    rc = subprocess.run([sys.executable, "-I", SKILL, p, "--out", out2, "--quiet"], check=False).returncode
    t = load(out2)["totals"]
    print("pass1:", load(out1)["totals"])
    print("final:", t, "exit", rc)
    for c in load(out2)["cases"]:
        if c["verdict"] == "differs":
            print("FINDING", c["id"], c["reason"][:300])


if __name__ == "__main__":
    main()
