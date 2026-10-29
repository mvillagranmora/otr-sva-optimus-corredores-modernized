"""F3 W1 front 9 part B: writes the INPUTS of the FM-CHAIN cases (CPF over the REAL FFMM), schema-cpfchain.sql and seed/base-cpfchain.sql. Deterministic. Removes every previous
cases-cpfchain/FM-CHAIN-* directory first (a renumbered case must never keep a stale golden). It NEVER writes schema-cpf.sql, seed/base-cpf.sql or anything of cases-cpf/ (the CPF front).

  py -3 -I make_cases_cpfchain.py            then  legacy-oracle/record_cpfchain.sh FM-CHAIN   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_cpfchain_lib as L  # noqa: E402
import cases_cpfchain_a  # noqa: E402


def main():
    L.CASES.mkdir(parents=True, exist_ok=True)
    for old in L.CASES.glob("FM-CHAIN-*"):
        shutil.rmtree(old)
    (L.BASE / "schema-cpfchain.sql").write_text(L.schema_cpfchain(), encoding="utf-8", newline="\n")
    (L.BASE / "seed" / "base-cpfchain.sql").write_text(L.base_cpfchain(), encoding="utf-8", newline="\n")
    cases_cpfchain_a.generar()
    print("FM-CHAIN inputs written:", L._n["n"])


if __name__ == "__main__":
    main()
