"""BcsCanalAgenteServicios oracle: writes the INPUTS of every case into cases/ (case.json + seed.sql). Deterministic. Removes every previous cases/CAN-* directory first (a renumbered case never keeps a
stale golden). The goldens (expected.json) are never written here: legacy-oracle/record.sh records them from the unmodified legacy.

  py -3 -I make_schema.py ; py -3 -I make_cases.py        then  legacy-oracle/record.sh CAN        (20 runs per case)
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_can_cal  # noqa: E402
import cases_can_lib as L  # noqa: E402
import cases_can_log  # noqa: E402
import cases_can_prc  # noqa: E402


def main():
    L.CASES.mkdir(exist_ok=True)
    for old in L.CASES.glob("*"):
        if old.is_dir():
            shutil.rmtree(old)
    ids = cases_can_log.generar() + cases_can_cal.generar() + cases_can_prc.generar()
    assert len(ids) == len(set(ids))
    por_familia = {}
    for i in ids:
        f = i.split("-")[1]
        por_familia[f] = por_familia.get(f, 0) + 1
    print("BcsCanalAgenteServicios inputs written: %d cases %s" % (len(ids), por_familia))


if __name__ == "__main__":
    main()
