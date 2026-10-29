"""F3 W1 front 3: writes the INPUTS of the FMM-IFM cases (ConsultarInstrumentosFondosMutuos) and seed/base-ifm.sql. Deterministic. Removes every previous
cases-ifm/FMM-IFM-* directory first (a renumbered case must never keep a stale golden).

  py -3 -I make_cases_ifm.py            then  legacy-oracle/record_ifm.sh FMM-IFM   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_ifm_lib as L  # noqa: E402
import cases_ifm_a  # noqa: E402


def main():
    L.CASES.mkdir(exist_ok=True)
    for old in L.CASES.glob("FMM-IFM-*"):
        shutil.rmtree(old)
    (L.BASE / "seed" / "base-ifm.sql").write_text(L.base_ifm(), encoding="utf-8", newline="\n")
    cases_ifm_a.generar()
    print("FMM-IFM inputs written:", L._n["n"])


if __name__ == "__main__":
    main()
