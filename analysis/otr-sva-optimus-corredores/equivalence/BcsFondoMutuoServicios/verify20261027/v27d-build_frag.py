"""verify 2026-10-27 (v27d): writes v27d-frag-dev.json and v27d-frag-fresh.json (lists of cases, paths RELATIVE TO EQ) from the texts Volcar27d wrote, the temporary check files
v27d-check-dev.json / v27d-check-fresh.json (same cases, paths relative to verify20261027/ so that compare.py, which refuses paths outside its cases folder, can run) and
v27d-fresh-status.json (counted / leftOut). No approval is written: cases with a known difference only get a "note". Run: python -I v27d-build_frag.py
A fresh input counts only when its legacy output is non-empty AND differs from every legacy output of the development cases.
"""
import json
import pathlib

HERE = pathlib.Path(__file__).resolve().parent
TIT = "CargarValorCuota (CVC, W2, writes)"
NOTA_KIT = ("DESVIO-KIT-1 NOT relaxed here: company with NO configuration rows. The legacy raises NullPointerException (ConfiguracionSistema, HashMap.get(...) is null) and the new code "
            "the kit's ParametroNoEncontradoException (the module harness writes it as a plain 'Exception' with the message 'Parametro PLA.FechaSistema en modulo PLATAFORMA no encontrado'); "
            "the exception line is the only difference (output, tables before / after the call / after the decision, AgregarLog calls, sequences, session and servlet decision ROLLBACK_HTTP500 "
            "are identical). The module's CasoCvc.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION accepts either class for this slug; the verifier does not, and no signed approval was given to this verification.")


def dev():
    cases = []
    for f in sorted((HERE / "dev/cvc/legacy").glob("*.txt")):
        cid = f.stem
        c = {"id": "cvc-" + cid, "input": cid, "title": "%s development golden %s" % (TIT, cid),
             "legacy": "verify20261027/dev/cvc/legacy/%s.txt" % cid, "new": "verify20261027/dev/cvc/new/%s.txt" % cid, "mask": []}
        if (HERE / "dev/cvc/legacy" / (cid + ".exc")).exists() and (HERE / "dev/cvc/legacy" / (cid + ".exc")).read_text(encoding="utf-8").startswith("NullPointerException") \
                and (HERE / "dev/cvc/new" / (cid + ".exc")).exists() and (HERE / "dev/cvc/new" / (cid + ".exc")).read_text(encoding="utf-8").startswith("Exception: Parametro ") \
                and cid.endswith("empresa-sin-configuracion"):
            c["note"] = NOTA_KIT
        cases.append(c)
    return cases


def fresh():
    devtexts = {f.read_text(encoding="utf-8") for f in (HERE / "dev/cvc/legacy").glob("*.txt")}
    devnew = {f.read_text(encoding="utf-8") for f in (HERE / "dev/cvc/new").glob("*.txt")}
    cases, left = [], []
    for d in sorted((HERE / "fresh/cvc/cases").iterdir()):
        lf = HERE / "fresh/cvc/out/legacy" / (d.name + ".txt")
        ref = json.loads((d / "case.json").read_text(encoding="utf-8"))["ref"][0]
        if not lf.exists():
            left.append({"id": d.name, "ref": ref, "why": "the unmodified legacy produced no stable dump (see determinism-fresh.txt / oracle-run.log)"})
            continue
        t = lf.read_text(encoding="utf-8")
        if not t.strip():
            left.append({"id": d.name, "ref": ref, "why": "empty legacy output"})
            continue
        if t in devtexts:
            left.append({"id": d.name, "ref": ref, "why": "legacy output identical to a development output"})
            continue
        cases.append({"id": d.name, "input": d.name, "title": "%s fresh input: %s" % (TIT, ref),
                      "legacy": "verify20261027/fresh/cvc/out/legacy/%s.txt" % d.name, "new": "verify20261027/fresh/cvc/out/new/%s.txt" % d.name, "mask": []})
    return cases, left


def doc(cases, kind):
    return {"system": "otr-sva-optimus-corredores", "module": "BcsFondoMutuoServicios",
            "legacy": {"label": ("goldens of the module's test resources recorded from the unmodified legacy (legacy-oracle/record_cvc.sh)" if kind == "dev" else
                                 "unmodified legacy re-run by the verifier with record_cvc.sh (ORACLE_CASES / ORACLE_OUT / ORACLE_REPORT under verify20261027/fresh/cvc), JDK 25, UTC, 20 repetitions")
                      + "; canonical text by verify20261027/Volcar27d.java",
                      "command": "record_cvc.sh ; java Volcar27d %s ..." % kind},
            "new": {"label": "modernized BcsFondoMutuoServicios target/classes through the module's own harness (CasoCvc, profile ABIERTA), same H2 schema, seeds and scripted AgregarLog, JDK 25, UTC",
                    "command": "java -cp tmp/vd;<surefire.test.class.path> Volcar27d %s ..." % kind},
            "cases": cases}


def main():
    fr, left = fresh()
    for kind, cases in (("dev", dev()), ("fresh", fr)):
        (HERE / ("v27d-frag-%s.json" % kind)).write_text(json.dumps(cases, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
        chk = json.loads(json.dumps(doc(cases, kind)))
        for c in chk["cases"]:
            c["legacy"] = c["legacy"].replace("verify20261027/", "", 1)
            c["new"] = c["new"].replace("verify20261027/", "", 1)
        (HERE / ("v27d-check-%s.json" % kind)).write_text(json.dumps(chk, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
        print(kind, len(cases))
    (HERE / "v27d-fresh-status.json").write_text(json.dumps({"counted": len(fr), "leftOut": left}, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    print("leftOut", left)


if __name__ == "__main__":
    main()
