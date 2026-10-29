"""verify 2026-10-27: writes frag-dev-<f>.json and frag-fresh-<f>.json (lists of {id,input,title,legacy,new,mask[,note]}, paths relative to equivalence/BcsFondoMutuoServicios)
from the canonical texts written by Volcar27 under dev/<f>/{legacy,new} and fresh/<f>/out/{legacy,new}. Nothing is approved here: a "note" only names a known difference.

  python -I build_frags.py
"""
import json, pathlib, re
HERE = pathlib.Path(__file__).resolve().parent
RES = HERE.parents[4] / "modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios/src/test/resources"
SERV = {"fsa": "ConsultarFechaSolicitudAporteRescate", "vdo": "ConsultarValorDolarFondoMutuo", "fmr": "ConsultarFondoMutuoReinversion"}
KIT = ("DESVIO-KIT-1: legacy NullPointerException (ConfiguracionSistema, per-company map null), new ParametroNoEncontradoException (kit port ConfiguracionSistemaJdbc); "
       "VdoEquivalenciaTest relaxes the class for this slug, this check does not; message, tables, collaborators and decision otherwise equal. NOT approved here")
MSG = ("legacy plain Exception, new ParametroNoEncontradoException (a subclass thrown by the product); the message text is identical ('Parametro ... no encontrado') and the "
       "VdoEquivalenciaTest passes because for a legacy plain Exception it compares only the message, not the class; exception class differs under a strict class check. "
       "Same shape as FMM-RRE-19 (approved by a person in verify 2026-10-26); NOT approved here")


def rel(*p):
    return "verify20261027/" + "/".join(p)


def exc_and_out(txt):
    exc = txt.split("== excepcion\n")[1].strip()
    out = re.search(r"== salidaCruda\n(.*)\n", txt).group(1)
    return exc, out


dev_texts = {}
for f in SERV:
    dev_texts[f] = {p.name: p.read_bytes() for p in (HERE / "dev" / f / "legacy").glob("*.txt")}
all_dev = {b for f in dev_texts for b in dev_texts[f].values()}
resumen = {}
for f in SERV:
    cases = []
    for p in sorted((HERE / "dev" / f / "legacy").glob("*.txt")):
        cid = p.stem
        ref = json.loads((RES / ("fondomutuo-" + f) / "cases" / cid / "case.json").read_text(encoding="utf-8"))["ref"]
        c = {"id": cid, "input": cid, "title": "%s golden %s: %s" % (SERV[f], cid, ref), "legacy": rel("dev", f, "legacy", cid + ".txt"), "new": rel("dev", f, "new", cid + ".txt"), "mask": []}
        a, b = p.read_bytes(), (HERE / "dev" / f / "new" / (cid + ".txt")).read_bytes()
        if a != b:
            slug = cid.split("_", 1)[1]
            c["note"] = KIT if slug in ("empresa-sin-configuracion", "empresa-ausente", "tabla-empresa-2-sin-configuracion") else MSG
        cases.append(c)
    (HERE / ("frag-dev-%s.json" % f)).write_text(json.dumps(cases, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    fr = []
    cuenta = 0
    for p in sorted((HERE / "fresh" / f / "out" / "legacy").glob("*.txt")):
        cid = p.stem
        d = json.loads((HERE / "fresh" / f / "cases" / cid / "case.json").read_text(encoding="utf-8"))
        a, b = p.read_bytes(), (HERE / "fresh" / f / "out" / "new" / (cid + ".txt")).read_bytes()
        exc, out = exc_and_out(a.decode())
        notas = []
        vacia = out == "" or re.fullmatch(r'\{"P_OUT_LISTA":\[\]\}', out) is not None
        if a in all_dev:
            notas.append("does not count as fresh coverage: its canonical output equals that of a development case")
        elif vacia:
            notas.append("does not count as fresh coverage: empty output")
        else:
            cuenta += 1
        if a != b:
            notas.append(KIT if exc == "NullPointerException" and f == "vdo" else "see diff")
        c = {"id": cid, "input": cid, "title": "%s fresh %s: %s" % (SERV[f], cid, d["ref"]), "legacy": rel("fresh", f, "out", "legacy", cid + ".txt"),
             "new": rel("fresh", f, "out", "new", cid + ".txt"), "mask": []}
        if notas:
            c["note"] = "; ".join(notas)
        fr.append(c)
    (HERE / ("frag-fresh-%s.json" % f)).write_text(json.dumps(fr, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    resumen[f] = (len(cases), len(fr), cuenta)
print(resumen)
