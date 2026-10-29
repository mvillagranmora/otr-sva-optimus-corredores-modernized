"""F3 W1 front 2: writes the INPUTS of the FMM-FMR cases (ConsultarFondoMutuoReinversion) and seed/base-fmr.sql. Deterministic. Removes every previous
cases-fmr/FMM-FMR-* directory first (a renumbered case must never keep a stale golden).

  py -3 -I make_cases_fmr.py            then  legacy-oracle/record_fmr.sh FMM-FMR   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_fmr_lib as L  # noqa: E402
import cases_fmr_a  # noqa: E402


def main():
    L.CASES.mkdir(exist_ok=True)
    for old in L.CASES.glob("FMM-FMR-*"):
        shutil.rmtree(old)
    (L.BASE / "seed" / "base-fmr.sql").write_text(L.base_fmr(), encoding="utf-8", newline="\n")
    cases_fmr_a.generar()
    print("FMM-FMR inputs written:", L._n["n"])


if __name__ == "__main__":
    main()
