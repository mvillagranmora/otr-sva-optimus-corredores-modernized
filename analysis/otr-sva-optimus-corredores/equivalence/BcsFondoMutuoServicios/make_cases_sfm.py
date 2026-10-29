"""F3 W1 front 3: writes the INPUTS of the FMM-SFM cases (ConsultarSolicitudesFFMM + ConsultarSolicitudesFFMMWS), schema-sfm.sql and seed/base-sfm.sql.
Deterministic. Removes every previous cases-sfm/FMM-SFM-* directory first (a renumbered case must never keep a stale golden).

  py -3 -I make_cases_sfm.py            then  legacy-oracle/record_sfm.sh FMM-SFM   (records expected.json from the unmodified legacy, 20 runs per case)
The goldens are never written here.
"""
import pathlib
import shutil
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_sfm_lib as L  # noqa: E402
import cases_sfm_a  # noqa: E402
import cases_sfm_b  # noqa: E402


def main():
    L.CASES.mkdir(parents=True, exist_ok=True)
    for old in L.CASES.glob("FMM-SFM-*"):
        shutil.rmtree(old)
    (L.BASE / "schema-sfm.sql").write_text(L.schema_sfm(), encoding="utf-8", newline="\n")
    (L.BASE / "seed" / "base-sfm.sql").write_text(L.base_sfm(), encoding="utf-8", newline="\n")
    cases_sfm_a.generar()
    cases_sfm_b.generar()
    print("FMM-SFM inputs written:", L._n["n"])


if __name__ == "__main__":
    main()
