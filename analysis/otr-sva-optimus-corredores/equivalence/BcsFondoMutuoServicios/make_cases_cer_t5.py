"""F3 W3 T5: writes the INPUTS of the FMM-CER T5 cases (CertificarSolicitudAporteRescate, front J the closing and the facade of the recursion; each case is a COMPLETE run A..J) into
cases-cer-t5/. Deterministic. Removes every previous cases-cer-t5/FMM-CER-* directory first (a renumbered case must never keep a stale golden). Never touches cases-cer/ (T1), cases-cer-t2/ (T2),
cases-cer-t3/ (T3) nor cases-cer-t4/ (T4-B). There is NO new seed file: the cases load the schema and the bases of T4-B.

  python -X utf8 make_cases_cer_t5.py        then  legacy-oracle/record_cer_t5.sh FMM-CER   (records expected.json from the legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_cer_t5 as T  # noqa: E402
import cases_cer_t5_a  # noqa: E402

MODULOS = [cases_cer_t5_a]
try:
    import cases_cer_t5_b  # noqa: E402
    MODULOS.append(cases_cer_t5_b)
except ImportError:
    pass
try:
    import cases_cer_t5_c  # noqa: E402
    MODULOS.append(cases_cer_t5_c)
except ImportError:
    pass
try:
    import cases_cer_t5_d  # noqa: E402
    MODULOS.append(cases_cer_t5_d)
except ImportError:
    pass


def main():
    T.CASES_T5.mkdir(parents=True, exist_ok=True)
    for old in T.CASES_T5.glob("FMM-CER-*"):
        shutil.rmtree(old)
    for m in MODULOS:
        m.generar()
    total = len(list(T.CASES_T5.glob("FMM-CER-*")))
    print("FMM-CER T5 inputs written: %d cases (FMM-CER-%03d .. FMM-CER-%03d), one golden per directory" % (total, T.PRIMER_NUMERO, T._n["n"]))


if __name__ == "__main__":
    main()
