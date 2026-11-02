"""F3 W3 T6-B: writes the INPUTS of the FMM-LIQ cases (LiquidacionSolicitudAporteRescate) into cases-liq/. Deterministic. Removes every previous cases-liq/FMM-LIQ-* directory first
(a renumbered case must never keep a stale golden). Never touches any other case directory.

  python -X utf8 make_cases_liq.py        then  legacy-oracle/record_liq.sh FMM-LIQ   (records expected.json from the legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_liq as T  # noqa: E402
import cases_liq_lib as LL  # noqa: E402


def main():
    LL.CASES_LIQ.mkdir(parents=True, exist_ok=True)
    for old in LL.CASES_LIQ.glob("FMM-LIQ-*"):
        shutil.rmtree(old)
    T.generar()
    total = len(list(LL.CASES_LIQ.glob("FMM-LIQ-*")))
    print("FMM-LIQ inputs written: %d cases (FMM-LIQ-%03d .. FMM-LIQ-%03d), one golden per directory" % (total, LL.PRIMER_NUMERO, LL._n["n"]))


if __name__ == "__main__":
    main()
