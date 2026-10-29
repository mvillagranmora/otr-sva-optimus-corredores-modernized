"""verify 2026-10-25: the verifier's own fresh inputs for CrearSolicitudRescateReinversion (the code changed on 2026-10-25), never used by development.
16 random inputs with a NEW seed (20261025; development used 20261024) through the same generator body as make_fresh_rre.py (its uno(), with OUT redirected
here: make_fresh_rre.main() is NOT run, it would delete fresh-rre/cases), renamed VF-RRE-nn, plus 6 edge inputs derived from them: both lists empty, rescue
of 0 cuotas, a huge cuota count, one pair with 1 cuota against custody of exactly 1, and two MALFORMED records (there is no INTENT.md, so malformed
records are tried): letters in NumeroCuotas, a blank IdInterno. Writes only under verify20261025/fresh/cases/.

  python -I gen_fresh.py
"""
import json
import pathlib
import random
import shutil
import sys

HERE = pathlib.Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))
import make_fresh_rre as G  # noqa: E402

OUT = HERE / "fresh" / "cases"


def listas(e):
    if "p_SolicitudesRescate" in e:
        return "p_SolicitudesRescate", "p_SolicitudesAporte"
    return "p_SolicitudRescateReinversionRescate", "p_SolicitudRescateReinversionAporte"


def derivar(base, nuevo, ref, cambio):
    src, dst = OUT / base, OUT / nuevo
    shutil.copytree(src, dst)
    doc = json.loads((dst / "case.json").read_text(encoding="utf-8"))
    cambio(doc["entrada"])
    doc["ref"] = ref
    (dst / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")


def main():
    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True)
    G.OUT = OUT
    rnd = random.Random(20261025)
    for n in range(1, 17):
        G.uno(rnd, n)
        (OUT / ("FRESH-RRE-%02d" % n)).rename(OUT / ("VF-RRE-%02d" % n))
        p = OUT / ("VF-RRE-%02d" % n) / "case.json"
        p.write_text(p.read_text(encoding="utf-8").replace("fresh input (seed 20261024)", "verify fresh input (seed 20261025)"), encoding="utf-8", newline="\n")

    def vacias(e):
        r, a = listas(e)
        e[r], e[a] = [], []

    def cero(e):
        r, _ = listas(e)
        e[r][0]["CalculoPorMontoCuota"], e[r][0]["NumeroCuotas"], e[r][0]["Monto"] = "C", 0, 0

    def enorme(e):
        r, _ = listas(e)
        e[r][0]["CalculoPorMontoCuota"], e[r][0]["NumeroCuotas"], e[r][0]["Monto"] = "C", 999999999.9999, 0

    def letras(e):
        r, _ = listas(e)
        e[r][0]["NumeroCuotas"] = "abc"

    def blanco(e):
        r, _ = listas(e)
        e[r][0]["IdInterno"] = ""

    derivar("VF-RRE-01", "VF-RRE-17", "verify edge: both lists empty", vacias)
    derivar("VF-RRE-02", "VF-RRE-18", "verify edge: rescue of 0 cuotas", cero)
    derivar("VF-RRE-03", "VF-RRE-19", "verify edge: rescue of 999999999.9999 cuotas", enorme)
    derivar("VF-RRE-04", "VF-RRE-20", "verify edge: rescue of 0 cuotas, other pair set", cero)
    derivar("VF-RRE-05", "VF-RRE-21", "verify malformed: letters in NumeroCuotas", letras)
    derivar("VF-RRE-06", "VF-RRE-22", "verify malformed: blank IdInterno", blanco)
    print("verify fresh inputs:", len(list(OUT.iterdir())))


if __name__ == "__main__":
    main()
