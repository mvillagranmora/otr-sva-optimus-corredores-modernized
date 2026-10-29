"""F3 W1 front 1: writes the INPUTS of the FMM-FSA cases (ConsultarFechaSolicitudAporteRescate) and seed/base-fsa.sql. Deterministic. Removes every previous
cases/FMM-FSA-* directory first (a renumbered case must never keep a stale golden).

  py -3 -I make_cases_fsa.py            then  legacy-oracle/record_fsa.sh FMM-FSA   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_fsa_lib as L  # noqa: E402
import cases_fsa_a  # noqa: E402


def main():
    for old in L.CASES.glob("FMM-FSA-*"):
        shutil.rmtree(old)
    (L.BASE / "seed" / "base-fsa.sql").write_text(L.base_fsa(), encoding="utf-8", newline="\n")
    cases_fsa_a.generar()
    print("FMM-FSA inputs written:", L._n["n"])


if __name__ == "__main__":
    main()
