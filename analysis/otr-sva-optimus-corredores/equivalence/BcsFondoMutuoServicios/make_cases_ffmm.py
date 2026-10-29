"""F3 W1 front 9: writes the INPUTS of the FM-CPFF cases (ConsultarCustodiaPortafolioFFMM), schema-ffmm.sql and seed/base-ffmm.sql. Deterministic. Removes every previous
cases-ffmm/FM-CPFF-* directory first (a renumbered case must never keep a stale golden).

  py -3 -I make_cases_ffmm.py            then  legacy-oracle/record_ffmm.sh FM-CPFF   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_ffmm_lib as L  # noqa: E402
import cases_ffmm_a  # noqa: E402


def main():
    L.CASES.mkdir(parents=True, exist_ok=True)
    for old in L.CASES.glob("FM-CPFF-*"):
        shutil.rmtree(old)
    (L.BASE / "schema-ffmm.sql").write_text(L.schema_ffmm(), encoding="utf-8", newline="\n")
    (L.BASE / "seed" / "base-ffmm.sql").write_text(L.base_ffmm(), encoding="utf-8", newline="\n")
    cases_ffmm_a.generar()
    print("FM-CPFF inputs written:", L._n["n"])


if __name__ == "__main__":
    main()
