"""After the shared oracle changes of F2 W1 front 2 (SessionConnection + ReinversionLock, OracleRunner guard, LegacySubject case): compares, byte for byte,
the dumps of a re-run of record.sh / record_w1b.sh into ORACLE_OUT (the goldens are never touched) with every existing golden they cover.

  py -3 -I tools-w1b-rre/regresion_goldens.py <outDir> [prefix]
"""
import pathlib
import sys

BASE = pathlib.Path(__file__).resolve().parent.parent


def main():
    out = pathlib.Path(sys.argv[1])
    pref = sys.argv[2] if len(sys.argv) > 2 else ""
    iguales, distintos, faltan = [], [], []
    for d in sorted((BASE / "cases").iterdir()):
        if not d.name.startswith(pref) or d.name.startswith("FMM-RRE") or not (d / "expected.json").exists():
            continue
        f = out / (d.name + ".json")
        if not f.exists():
            faltan.append(d.name)
        elif f.read_bytes() == (d / "expected.json").read_bytes():
            iguales.append(d.name)
        else:
            distintos.append(d.name)
    print("identical:", len(iguales), "different:", len(distintos), distintos, "not run:", len(faltan))


if __name__ == "__main__":
    main()
