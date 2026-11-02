"""F3 W3 T6-A: writes the INPUTS of the FMM-CERPF cases (CertificarPorFiltrosSolicitudAporteRescate) into cases-cerpf/. Deterministic. Removes every previous cases-cerpf/FMM-CERPF-* directory first
(a renumbered case must never keep a stale golden). Never touches cases-cer-t5/ nor any other case directory.

  python -X utf8 make_cases_cerpf.py        then  legacy-oracle/record_cerpf.sh FMM-CERPF   (records expected.json from the legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_cerpf as T  # noqa: E402


def main():
    T.CASES_PF.mkdir(parents=True, exist_ok=True)
    for old in T.CASES_PF.glob("FMM-CERPF-*"):
        shutil.rmtree(old)
    T.generar()
    total = len(list(T.CASES_PF.glob("FMM-CERPF-*")))
    print("FMM-CERPF inputs written: %d cases (FMM-CERPF-%03d .. FMM-CERPF-%03d), one golden per directory" % (total, T.PRIMER_NUMERO, T._n["n"]))


if __name__ == "__main__":
    main()
