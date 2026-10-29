"""F3 W1 front 4: writes the INPUTS of the FMM-VDO cases (ConsultarValorDolarFondoMutuo) and seed/base-vdo.sql. Deterministic. Removes every previous
cases-vdo/FMM-VDO-* directory first (a renumbered case must never keep a stale golden).

  py -3 -I make_cases_vdo.py            then  legacy-oracle/record_vdo.sh FMM-VDO   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_vdo_lib as L  # noqa: E402
import cases_vdo_a  # noqa: E402


def main():
    L.CASES.mkdir(exist_ok=True)
    for old in L.CASES.glob("FMM-VDO-*"):
        shutil.rmtree(old)
    (L.BASE / "seed" / "base-vdo.sql").write_text(L.base_vdo(), encoding="utf-8", newline="\n")
    cases_vdo_a.generar()
    print("FMM-VDO inputs written:", L._n["n"])


if __name__ == "__main__":
    main()
