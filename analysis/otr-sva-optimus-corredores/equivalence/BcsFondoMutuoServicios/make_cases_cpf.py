"""F3 W1 front 8: writes the INPUTS of the FMM-CPF cases (ConsultarCustodiaPortafolioFondoMutuo), schema-cpf.sql and seed/base-cpf.sql. Deterministic. Removes every previous
cases-cpf/FMM-CPF-* directory first (a renumbered case must never keep a stale golden).

  py -3 -I make_cases_cpf.py            then  legacy-oracle/record_cpf.sh FMM-CPF   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_cpf_lib as L  # noqa: E402
import cases_cpf_a  # noqa: E402


def main():
    L.CASES.mkdir(parents=True, exist_ok=True)
    for old in L.CASES.glob("FMM-CPF-*"):
        shutil.rmtree(old)
    (L.BASE / "schema-cpf.sql").write_text(L.schema_cpf(), encoding="utf-8", newline="\n")
    (L.BASE / "seed" / "base-cpf.sql").write_text(L.base_cpf(), encoding="utf-8", newline="\n")
    cases_cpf_a.generar()
    print("FMM-CPF inputs written:", L._n["n"])


if __name__ == "__main__":
    main()
