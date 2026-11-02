"""F3 W3 T4-A: consistency check of the FMM-CSR matrix (cases-csr/) and of its goldens. Exit code 0 = consistent.

  py -3 -I legacy-oracle/check_csr.py [--resources]      (--resources checks the copy in the module test resources too)
Checks: every case has case.json, seed.sql and a golden (expected.json or expected.json.gz); the golden was recorded by the oracle (provenance) and no collaborator answered "SIN RESPUESTA
CONFIGURADA"; every FM-REI-41..68 card and every defect named in RULES_FONDOMUTUO_F2_REINVERSION.md is pinned by at least one case `ref`; the stability report says 20 runs, 0 hard;
the sizes of the goldens (a plain golden over 1 MB must be gzipped). Prints the coverage table.
"""
import gzip
import json
import pathlib
import re
import sys

BASE = pathlib.Path(__file__).resolve().parent.parent
WS = BASE.parents[3]
RES = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios" / "src" / "test" / "resources" / "fondomutuo-csr"
TARJETAS = ["FM-REI-%d" % n for n in range(41, 69)]
DEFECTOS = ["D-F2-4", "D-F2-8", "D-F2-11", "D-F2-12", "D-F2-13", "D-F2-18", "D-F2-19", "D-F2-20", "D-F2-21", "D-NOR-5", "D-NOR-9", "D-NOR-16", "D-SQL-1", "D-CUO", "D-REQ-1", "D-REQ-6",
            "D-REQ-7", "D-REQ-9", "D-TES-1", "D-TES-2"] + ["D-CSR-%02d" % n for n in range(1, 13)]


def leer_golden(d):
    if (d / "expected.json").exists():
        return json.loads((d / "expected.json").read_text(encoding="utf-8"))
    if (d / "expected.json.gz").exists():
        with gzip.open(d / "expected.json.gz", "rt", encoding="utf-8") as f:
            return json.load(f)
    return None


def revisar(raiz: pathlib.Path, nombre: str, errores: list):
    casos = sorted(p for p in raiz.glob("FMM-CSR-*") if p.is_dir())
    refs = {}
    for d in casos:
        for f in ("case.json", "seed.sql"):
            if not (d / f).exists():
                errores.append("%s: %s missing in %s" % (nombre, f, d.name))
        g = leer_golden(d)
        if g is None:
            errores.append("%s: no golden in %s" % (nombre, d.name))
            continue
        if "Recorded by legacy-oracle" not in g.get("_provenance", ""):
            errores.append("%s: golden without oracle provenance in %s" % (nombre, d.name))
        texto = json.dumps(g.get("colaboradores", []))
        if "SIN RESPUESTA CONFIGURADA" in texto:
            errores.append("%s: a collaborator had no scripted answer in %s" % (nombre, d.name))
        if (d / "expected.json").exists() and (d / "expected.json").stat().st_size > 1024 * 1024:
            errores.append("%s: plain golden over 1 MB in %s (gzip it with sync_csr.py)" % (nombre, d.name))
        ref = json.loads((d / "case.json").read_text(encoding="utf-8")).get("ref", "")
        refs[d.name] = ref
    return casos, refs


def main():
    errores = []
    casos, refs = revisar(BASE / "cases-csr", "analysis", errores)
    if "--resources" in sys.argv:
        c2, _ = revisar(RES / "cases", "resources", errores)
        if len(c2) != len(casos):
            errores.append("resources have %d cases, analysis %d" % (len(c2), len(casos)))
    todo = " ".join(refs.values())
    print("cases: %d" % len(casos))
    print("card coverage (cases per card):")
    for t in TARJETAS:
        n = len(re.findall(r"(?<![\d])%s(?!\d)" % re.escape(t), todo)) + len(re.findall(r"FM-REI-(?:\d+/)*%s(?!\d)" % t.split("-")[-1], todo)) // 2
        # a card is named as FM-REI-nn or inside a list FM-REI-41/52/62: count the cases that name it in either form
        cuantos = sum(1 for r in refs.values() if re.search(r"FM-REI-([\d/]+)", r) and t.split("-")[-1] in re.search(r"FM-REI-([\d/]+)", r).group(1).split("/") or re.search(r"(?<!\d)%s(?!\d)" % re.escape(t), r))
        print("  %-10s %3d" % (t, cuantos))
        if cuantos == 0:
            errores.append("card %s is pinned by no case" % t)
    print("defect coverage:")
    for d in DEFECTOS:
        cuantos = sum(1 for r in refs.values() if d in r)
        print("  %-10s %3d" % (d, cuantos))
        if cuantos == 0:
            errores.append("defect %s is pinned by no case" % d)
    informe = BASE / "determinism-csr.txt"
    if not informe.exists():
        errores.append("determinism-csr.txt missing")
    else:
        m = re.match(r"cases executed: (\d+), repeats per case: (\d+), unstable \(hard \+ message-only\): (\d+), hard: (\d+)", informe.read_text(encoding="utf-8").splitlines()[0])
        if not m:
            errores.append("determinism-csr.txt: unexpected format")
        else:
            if int(m.group(1)) != len(casos):
                errores.append("determinism-csr.txt covers %s cases, the matrix has %d" % (m.group(1), len(casos)))
            if int(m.group(2)) != 20:
                errores.append("determinism-csr.txt: %s runs per case, 20 expected" % m.group(2))
            if int(m.group(4)) != 0:
                errores.append("determinism-csr.txt: %s unstable cases" % m.group(4))
    for e in errores:
        print("ERROR:", e)
    print("OK" if not errores else "%d error(s)" % len(errores))
    sys.exit(1 if errores else 0)


if __name__ == "__main__":
    main()
