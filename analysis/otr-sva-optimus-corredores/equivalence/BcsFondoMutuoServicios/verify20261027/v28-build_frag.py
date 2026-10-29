"""verify 2026-10-28 (v28): writes v28-frag-dev.json and v28-frag-fresh.json (lists of cases, paths RELATIVE TO EQ) from the texts Volcar28 wrote, the temporary check files
tmp/v28-check-dev.json / tmp/v28-check-fresh.json (paths relative to verify20261027/ for compare.py) and v28-fresh-status.json (which fresh inputs count: legacy output non empty and different
from EVERY development output of its front). No approval is written: cases with a known difference only get a "note". Run: python -I v28-build_frag.py
"""
import json
import pathlib

HERE = pathlib.Path(__file__).resolve().parent
EQ = HERE.parent
PREF = {"ffmm": "ffmm-", "cpfchain": "chain-"}
TIT = {"ffmm": "ConsultarCustodiaPortafolioFFMM (class alone)", "cpfchain": "chain ConsultarCustodiaPortafolioFondoMutuo + ConsultarCustodiaPortafolioFFMM"}

NOTA_KIT = ("DESVIO-KIT-1 NOT relaxed here: the legacy raises NullPointerException (ConfiguracionSistema of a null / absent / unknown company) and the new code the kit's "
            "ParametroNoEncontradoException (the module harness writes it as a plain 'Exception' with the message 'Parametro PLA.FechaSistema en modulo PLATAFORMA no encontrado'); the exception line is the "
            "only difference, output, tables, collaborator calls, session and servlet decision (ROLLBACK_HTTP500) are identical. The module's CasoFfmm.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION accepts either "
            "class for this slug; the verifier does not (the owner's decision is not in a signed record given to this verification): reported as a difference, not approved.")
NOTA_IDS = ("CHAIN only: the ids of the solicitudes (D-F3A-04, assigned by the legacy in parallelStream().forEach) are canonicalised on BOTH sides with the module test's own "
            "CadenaEquivalenciaTest.conIdsCanonicos (renaming by order of first appearance, cut at the first ConsultarEmpresa, which in the chain is FFMM's); the order of P_OUT_TBL is compared as written.")


def vacia(texto):
    """a legacy output with no data: no row in p_out_tbl / P_OUT_TBL and no exception"""
    s = texto.split("== salidaCruda\n", 1)[1].split("\n== ", 1)[0]
    exc = texto.rsplit("== excepcion\n", 1)[1].strip()
    return exc == "<none>" and (s.strip() in ("", '{"p_out_tbl":[]}', '{"P_OUT_TBL":[]}', "<absent>") or '"p_out_tbl":[]' in s.replace(" ", "") or '"P_OUT_TBL":[]' in s.replace(" ", ""))


def dev(front):
    cases = []
    base = HERE / "dev" / front
    for f in sorted((base / "legacy").glob("*.txt")):
        cid = f.stem
        c = {"id": PREF[front] + cid, "input": cid, "title": "%s development golden %s" % (TIT[front], cid),
             "legacy": "verify20261027/dev/%s/legacy/%s.txt" % (front, cid), "new": "verify20261027/dev/%s/new/%s.txt" % (front, cid), "mask": []}
        notas = []
        le = base / "legacy" / (cid + ".exc")
        if cid.split("_", 1)[1] in ("empresa-nula", "empresa-ausente", "empresa-sin-configuracion") and le.exists() and le.read_text(encoding="utf-8").startswith("NullPointerException"):
            notas.append(NOTA_KIT)
        if front == "cpfchain":
            notas.append(NOTA_IDS)
        if notas:
            c["note"] = " ".join(notas)
        cases.append(c)
    return cases


def fresh(front, status):
    cases = []
    devtexts = {p.read_text(encoding="utf-8") for p in (HERE / "dev" / front / "legacy").glob("*.txt")}
    for d in sorted((HERE / "fresh" / front / "cases").iterdir()):
        lp = HERE / "fresh" / front / "out" / "legacy" / (d.name + ".txt")
        ref = json.loads((d / "case.json").read_text(encoding="utf-8"))["ref"]
        st = {"id": PREF[front] + d.name, "front": front, "ref": ref}
        if not lp.exists():
            st["counts"] = False
            st["why"] = "the legacy oracle produced no dump"
            status.append(st)
            continue
        t = lp.read_text(encoding="utf-8")
        if vacia(t):
            st["counts"] = False
            st["why"] = "legacy output empty"
        elif t in devtexts:
            st["counts"] = False
            st["why"] = "legacy output equals a development output"
        else:
            st["counts"] = True
        status.append(st)
        c = {"id": PREF[front] + d.name, "input": d.name, "title": "%s fresh input %s: %s" % (TIT[front], d.name, ref),
             "legacy": "verify20261027/fresh/%s/out/legacy/%s.txt" % (front, d.name), "new": "verify20261027/fresh/%s/out/new/%s.txt" % (front, d.name), "mask": []}
        if front == "cpfchain":
            c["note"] = NOTA_IDS
        cases.append(c)
    return cases


def doc(cases, kind):
    return {"system": "otr-sva-optimus-corredores", "module": "BcsFondoMutuoServicios",
            "legacy": {"label": ("goldens of the module's test resources recorded from the unmodified legacy (legacy-oracle/record_ffmm.sh, record_cpfchain.sh)" if kind == "dev" else
                                 "unmodified legacy re-run by the verifier with record_ffmm.sh / record_cpfchain.sh (ORACLE_CASES / ORACLE_OUT / ORACLE_REPORT under verify20261027/fresh), JDK 25, UTC, 20 repetitions")
                      + "; canonical text by verify20261027/Volcar28.java",
                      "command": "record_<ffmm|cpfchain>.sh ; java Volcar28 %s <ffmm|cpfchain> ..." % kind},
            "new": {"label": "modernized BcsFondoMutuoServicios target/classes through the module's own harness (CasoFfmm / CasoCadena: real adapters), same H2 schema, seeds and scripted collaborators, JDK 25, UTC",
                    "command": "java -cp tmp/v28c;<surefire.test.class.path> Volcar28 %s <ffmm|cpfchain> ..." % kind},
            "cases": cases}


def main():
    status = []
    for kind in ("dev", "fresh"):
        cases = []
        for front in ("ffmm", "cpfchain"):
            cases += dev(front) if kind == "dev" else fresh(front, status)
        (HERE / ("v28-frag-%s.json" % kind)).write_text(json.dumps(cases, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
        chk = json.loads(json.dumps(doc(cases, kind)))
        for c in chk["cases"]:
            c["legacy"] = c["legacy"].replace("verify20261027/", "../", 1)
            c["new"] = c["new"].replace("verify20261027/", "../", 1)
        (HERE / "tmp" / ("v28-check-%s.json" % kind)).write_text(json.dumps(chk, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
        print(kind, len(cases))
    (HERE / "v28-fresh-status.json").write_text(json.dumps(status, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    for front in ("ffmm", "cpfchain"):
        s = [x for x in status if x["front"] == front]
        print(front, "counting:", sum(1 for x in s if x["counts"]), "of", len(s), [(x["id"], x["why"]) for x in s if not x["counts"]])


if __name__ == "__main__":
    main()
