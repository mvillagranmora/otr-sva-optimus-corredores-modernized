"""F3 W3 T3: writes the INPUTS of the FMM-CER T3 cases (CertificarSolicitudAporteRescate, fronts F-E cuenta de inversion, F-F Tesoreria and F-H Spot) into cases-cer-t3/ and seed/base-cer-t3.sql.
Deterministic. Removes every previous cases-cer-t3/FMM-CER-* directory first (a renumbered case must never keep a stale golden). Never touches cases-cer/ (T1) nor cases-cer-t2/ (T2).

  py -3 -I make_cases_cer_t3.py        then  legacy-oracle/record_cer_t3.sh FMM-CER   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_cer_t3 as T  # noqa: E402
import cases_cer_t3_e  # noqa: E402
import cases_cer_t3_f  # noqa: E402
import cases_cer_t3_h  # noqa: E402


def main():
    T.CASES_T3.mkdir(parents=True, exist_ok=True)
    for old in T.CASES_T3.glob("FMM-CER-*"):
        shutil.rmtree(old)
    T.escribir_base()
    cases_cer_t3_e.generar()
    cases_cer_t3_f.generar()
    cases_cer_t3_h.generar()
    cases_cer_t3_h.generar_extra()
    total = len(list(T.CASES_T3.glob("FMM-CER-*")))
    print("FMM-CER T3 inputs written: %d cases (FMM-CER-%03d .. FMM-CER-%03d), one golden per directory" % (total, T.PRIMER_NUMERO, T._n["n"]))


if __name__ == "__main__":
    main()
