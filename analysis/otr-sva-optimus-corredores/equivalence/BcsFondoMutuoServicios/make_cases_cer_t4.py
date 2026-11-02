"""F3 W3 T4-B: writes the INPUTS of the FMM-CER T4 cases (CertificarSolicitudAporteRescate, fronts F-G Facturacion and F-I Reinversion y switch) into cases-cer-t4/ and seed/base-cer-t4.sql.
Deterministic. Removes every previous cases-cer-t4/FMM-CER-* directory first (a renumbered case must never keep a stale golden). Never touches cases-cer/ (T1), cases-cer-t2/ (T2) nor
cases-cer-t3/ (T3).

  py -3 -I make_cases_cer_t4.py        then  legacy-oracle/record_cer_t4.sh FMM-CER   (records expected.json from the legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_cer_t4 as T  # noqa: E402
import cases_cer_t4_g  # noqa: E402
import cases_cer_t4_i  # noqa: E402


def main():
    T.CASES_T4.mkdir(parents=True, exist_ok=True)
    for old in T.CASES_T4.glob("FMM-CER-*"):
        shutil.rmtree(old)
    T.escribir_base()
    cases_cer_t4_g.generar()
    cases_cer_t4_i.generar()
    total = len(list(T.CASES_T4.glob("FMM-CER-*")))
    print("FMM-CER T4 inputs written: %d cases (FMM-CER-%03d .. FMM-CER-%03d), one golden per directory" % (total, T.PRIMER_NUMERO, T._n["n"]))


if __name__ == "__main__":
    main()
