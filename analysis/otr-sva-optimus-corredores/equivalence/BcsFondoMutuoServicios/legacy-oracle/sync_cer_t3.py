"""F3 W3 T3 (CertificarSolicitudAporteRescate, fronts F-E cuenta de inversion, F-F Tesoreria and F-H Spot): copies, byte for byte, the FMM-CER T3 cases (case.json, seed.sql, expected.json recorded by
record_cer_t3.sh) and seed/base-cer-t3.sql into the module test resources src/test/resources/fondomutuo-cer/ (cases go to cases-t3/; schema-cer.sql, seed/base-cer.sql and
seed/doubles-cer.json are the ones of T1 and are NOT touched here: run sync_cer.py for those). A case without golden is NOT copied (the module test would fail).
Nothing under legacy/ or src/main is touched.

  py -3 -I legacy-oracle/sync_cer_t3.py                       writes into modernized/.../BcsFondoMutuoServicios
  py -3 -I legacy-oracle/sync_cer_t3.py <module directory>    writes into ANOTHER copy of the module (validation outside the repo)
"""
import json
import pathlib
import shutil
import sys

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
MOD = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else WS / "modernized" / "otr-sva-optimus-corredores" / "BcsFondoMutuoServicios"
DEST = MOD / "src" / "test" / "resources" / "fondomutuo-cer"


def main():
    (DEST / "cases-t3").mkdir(parents=True, exist_ok=True)
    (DEST / "seed").mkdir(parents=True, exist_ok=True)
    shutil.copyfile(BASE / "seed" / "base-cer-t3.sql", DEST / "seed" / "base-cer-t3.sql")
    if (BASE / "determinism-cer-t3.txt").exists():
        shutil.copyfile(BASE / "determinism-cer-t3.txt", DEST / "determinism-cer-t3.txt")
    for old in (DEST / "cases-t3").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = 0
    por_modo = {}
    for d in sorted((BASE / "cases-cer-t3").glob("FMM-CER-*")):
        if not (d / "expected.json").exists():
            print("no golden, skipped:", d.name)
            continue
        t = DEST / "cases-t3" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
        ref = json.loads((d / "case.json").read_text(encoding="utf-8"))["ref"]
        modo = [r for r in ref if r.startswith("T3-costura:")][0]
        por_modo[modo] = por_modo.get(modo, 0) + 1
    print("FMM-CER T3 cases synced:", n, por_modo)


if __name__ == "__main__":
    main()
