"""F3 W2: writes the INPUTS of the FMM-CVC cases (CargarValorCuota), schema-cvc.sql and seed/base-cvc.sql. Deterministic. Removes every previous cases-cvc/FMM-CVC-* directory
first (a renumbered case must never keep a stale golden).

  py -3 -I make_cases_cvc.py            then  legacy-oracle/record_cvc.sh FMM-CVC   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_cvc_lib as L  # noqa: E402
import cases_cvc_a  # noqa: E402
import cases_cvc_b  # noqa: E402


def main():
    L.CASES.mkdir(parents=True, exist_ok=True)
    for old in L.CASES.glob("FMM-CVC-*"):
        shutil.rmtree(old)
    (L.BASE / "schema-cvc.sql").write_text(L.schema_cvc(), encoding="utf-8", newline="\n")
    (L.BASE / "seed" / "base-cvc.sql").write_text(L.base_cvc(), encoding="utf-8", newline="\n")
    cases_cvc_a.generar()
    cases_cvc_b.generar()
    print("FMM-CVC inputs written:", L._n["n"])


if __name__ == "__main__":
    main()
