"""F2 W1 front 2: writes the INPUTS of the FMM-RRE cases (CrearSolicitudRescateReinversion) and seed/base-rre.sql. Deterministic.

  py -3 -I make_cases_rre.py            then  legacy-oracle/record_rre.sh FMM-RRE   (records expected.json from the unmodified legacy)
The goldens are never written here.
"""
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_rre_lib as L  # noqa: E402
import cases_rre_a  # noqa: E402
import cases_rre_b  # noqa: E402
import cases_rre_c  # noqa: E402
import cases_rre_d  # noqa: E402


def main():
    (L.BASE / "seed" / "base-rre.sql").write_text(L.base_rre(), encoding="utf-8", newline="\n")
    n = cases_rre_a.escribir() + cases_rre_b.escribir() + cases_rre_c.escribir() + cases_rre_d.escribir()
    print("FMM-RRE inputs written:", n)


if __name__ == "__main__":
    main()
