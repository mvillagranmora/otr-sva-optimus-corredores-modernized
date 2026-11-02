"""F3 W3 T4-A: writes the INPUTS of the FMM-CSR cases (CrearSolicitudAporteReinversion, the class Certificar calls from crearAporteReinversion) into cases-csr/ and seed/base-csr.sql.
Deterministic. Removes every previous cases-csr/FMM-CSR-* directory first (a renumbered case must never keep a stale golden). Never touches any other cases directory.

  py -3 -I make_cases_csr.py        then  legacy-oracle/record_csr.sh FMM-CSR   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_csr_lib as L  # noqa: E402
import cases_csr_a  # noqa: E402
import cases_csr_b  # noqa: E402
import cases_csr_c  # noqa: E402
import cases_csr_d  # noqa: E402
import cases_csr_e  # noqa: E402


def main():
    L.CASES.mkdir(parents=True, exist_ok=True)
    for old in L.CASES.glob("FMM-CSR-*"):
        shutil.rmtree(old)
    (L.BASE / "seed" / "base-csr.sql").write_text(L.base_csr(), encoding="utf-8", newline="\n")
    cases_csr_a.generar()
    cases_csr_b.generar()
    cases_csr_c.generar()
    cases_csr_d.generar()
    cases_csr_e.generar()
    total = len(list(L.CASES.glob("FMM-CSR-*")))
    print("FMM-CSR inputs written: %d cases (FMM-CSR-%03d .. FMM-CSR-%03d), one golden per directory" % (total, L.PRIMER_NUMERO, L._n["n"]))


if __name__ == "__main__":
    main()
