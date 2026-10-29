"""F3 W3 T2: writes the INPUTS of the FMM-CER T2 cases (CertificarSolicitudAporteRescate, fronts F-C aportes and F-D rescates) into cases-cer-t2/ and seed/base-cer-t2.sql.
Deterministic. Removes every previous cases-cer-t2/FMM-CER-* directory first (a renumbered case must never keep a stale golden). Never touches cases-cer/ (T1).

  py -3 -I make_cases_cer_t2.py        then  legacy-oracle/record_cer_t2.sh FMM-CER   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_cer_t2 as T  # noqa: E402
import cases_cer_t2_c  # noqa: E402
import cases_cer_t2_d  # noqa: E402


def main():
    T.CASES_T2.mkdir(parents=True, exist_ok=True)
    for old in T.CASES_T2.glob("FMM-CER-*"):
        shutil.rmtree(old)
    T.escribir_base()
    cases_cer_t2_c.generar()
    cases_cer_t2_d.generar()
    total = len(list(T.CASES_T2.glob("FMM-CER-*")))
    print("FMM-CER T2 inputs written: %d cases (FMM-CER-%03d .. FMM-CER-%03d), one golden per directory" % (total, T.PRIMER_NUMERO, T._n["n"]))


if __name__ == "__main__":
    main()
