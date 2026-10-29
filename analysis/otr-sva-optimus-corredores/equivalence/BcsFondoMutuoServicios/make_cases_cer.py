"""F3 W3 T1: writes the INPUTS of the FMM-CER cases (CertificarSolicitudAporteRescate, fronts F-A and F-B), schema-cer.sql and seed/base-cer.sql. Deterministic. Removes every previous
cases-cer/FMM-CER-* directory first (a renumbered case must never keep a stale golden).

  py -3 -I make_cases_cer.py            then  legacy-oracle/record_cer.sh FMM-CER   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_cer_lib as L  # noqa: E402
import cases_cer_caso as C  # noqa: E402
import cases_cer_a  # noqa: E402
import cases_cer_b  # noqa: E402


def main():
    L.CASES.mkdir(parents=True, exist_ok=True)
    for old in L.CASES.glob("FMM-CER-*"):
        shutil.rmtree(old)
    informe, _ = L.verificar_catalogo()
    for linea in informe:
        print("catalog:", linea)
    (L.BASE / "schema-cer.sql").write_text(L.schema_cer(), encoding="utf-8", newline="\n")
    (L.BASE / "seed" / "base-cer.sql").write_text(L.base_cer(), encoding="utf-8", newline="\n")
    cases_cer_a.generar()
    cases_cer_b.generar()
    total = len(list(L.CASES.glob("FMM-CER-*")))
    print("FMM-CER inputs written: %d base cases, %d directories (goldens to record: one per directory)" % (C._n["n"], total))


if __name__ == "__main__":
    main()
