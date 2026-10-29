"""verify 2026-10-27 (v27b): for each fresh input, is its legacy output non-empty and different from every development output (salidaCruda + exception) and from
the other fresh inputs of its front? Prints one line per input.   python v27b-check_fresh.py"""
import glob
import json
import os
import pathlib
import re

HERE = pathlib.Path(__file__).resolve().parent


def partes(t):
    m = re.search(r"== salidaCruda\n(.*?)\n== tablas", t, re.S)
    sal = m.group(1) if m else ""
    exc = t.split("== excepcion\n")[1].strip()
    return sal, exc


def exc_full(path):
    p = pathlib.Path(str(path)[:-4] + ".exc")
    return p.read_text(encoding="utf8") if p.exists() else ""


dev = {}
for fr in ("ifm", "sfm", "ws"):
    for f in glob.glob(str(HERE / "dev" / fr / "legacy" / "*.txt")):
        t = open(f, encoding="utf8").read()
        s, e = partes(t)
        dev.setdefault((s, exc_full(f)), []).append(os.path.basename(f))
res = {}
for fr in ("ifm", "sfm", "ws"):
    seen = {}
    for f in sorted(glob.glob(str(HERE / "fresh" / fr / "out" / "legacy" / "*.txt"))):
        t = open(f, encoding="utf8").read()
        s, e = partes(t)
        n = None
        if s and s != "<absent>":
            j = json.loads(s)
            n = len(j.get("FondosMutuos", j.get("P_OUT_SOLICITUDES", [])))
        key = (s, exc_full(f))
        nonempty = (n or 0) > 0 or e != "<none>"
        motivos = []
        if not nonempty:
            motivos.append("SALIDA VACIA")
        if key in dev:
            motivos.append("IGUAL A DEV " + dev[key][0])
        if key in seen:
            motivos.append("igual a fresca " + seen[key])
        seen.setdefault(key, os.path.basename(f))
        new = open(f.replace("legacy", "new"), encoding="utf8").read()
        if "errorDelArnes" in new:
            motivos.append("ERROR DEL ARNES")
        res[os.path.basename(f)] = motivos
        print(fr, os.path.basename(f), "filas=%s" % n, "exc=" + e[:30], "|", "; ".join(motivos) or "ok")
json.dump(res, open(HERE / "tmp" / "v27b-check.json", "w"), indent=1)
