"""F3 W3 T5 (CertificarSolicitudAporteRescate, front J the closing and the facade of the recursion): copies, byte for byte, the FMM-CER T5 cases (case.json, seed.sql, expected.json recorded by
record_cer_t5.sh) into the module test resources src/test/resources/fondomutuo-cer/cases-t5/, and the determinism report. There is NO new seed file (T5 loads the schema and the bases of T4-B:
schema-cer.sql and seed/base-cer*.sql are the ones of T1..T4-B and are NOT touched here: run sync_cer.py / sync_cer_t2.py ... for those). A case without golden is NOT copied (the module test
would fail). Nothing under legacy/ or src/main is touched.

  python -X utf8 -I legacy-oracle/sync_cer_t5.py                       writes into modernized/.../BcsFondoMutuoServicios
  python -X utf8 -I legacy-oracle/sync_cer_t5.py <module directory>    writes into ANOTHER copy of the module (validation outside the repo)
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
    (DEST / "cases-t5").mkdir(parents=True, exist_ok=True)
    if (BASE / "determinism-cer-t5.txt").exists():
        shutil.copyfile(BASE / "determinism-cer-t5.txt", DEST / "determinism-cer-t5.txt")
    for old in (DEST / "cases-t5").iterdir():
        if old.is_dir():
            shutil.rmtree(old)
    n = 0
    por_modo = {}
    for d in sorted((BASE / "cases-cer-t5").glob("FMM-CER-*")):
        if not (d / "expected.json").exists():
            print("no golden, skipped:", d.name)
            continue
        t = DEST / "cases-t5" / d.name
        t.mkdir()
        for f in ("case.json", "seed.sql", "expected.json"):
            if (d / f).exists():
                shutil.copyfile(d / f, t / f)
        n += 1
        ref = json.loads((d / "case.json").read_text(encoding="utf-8"))["ref"]
        modo = [r for r in ref if r.startswith("T5-modo:")][0]
        por_modo[modo] = por_modo.get(modo, 0) + 1
    print("FMM-CER T5 cases synced:", n, por_modo)


if __name__ == "__main__":
    main()
