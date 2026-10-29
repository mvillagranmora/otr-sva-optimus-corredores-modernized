"""F3 W1 front 4: writes the INPUTS of the FMM-DET cases (ConsultarDetalleSolicitud), schema-det.sql and seed/base-det.sql. Deterministic. Removes every previous
cases-det/FMM-DET-* directory first (a renumbered case must never keep a stale golden).

  py -3 -I make_cases_det.py            then  legacy-oracle/record_det.sh FMM-DET   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_det_lib as L  # noqa: E402
import cases_det_a  # noqa: E402


def main():
    L.CASES.mkdir(parents=True, exist_ok=True)
    for old in L.CASES.glob("FMM-DET-*"):
        shutil.rmtree(old)
    (L.BASE / "schema-det.sql").write_text(L.schema_det(), encoding="utf-8", newline="\n")
    (L.BASE / "seed" / "base-det.sql").write_text(L.base_det(), encoding="utf-8", newline="\n")
    cases_det_a.generar()
    print("FMM-DET inputs written:", L._n["n"])


if __name__ == "__main__":
    main()
