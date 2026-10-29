"""verify 2026-10-27 (v27b): writes v27b-frag-dev.json and v27b-frag-fresh.json (lists of {"id","input","title","legacy","new","mask":[]}, paths relative to EQ) and the temporary
cases files under tmp/ (paths relative to tmp/, for compare.py --allow-outside). No mask, no approval. A "note" is written only to say WHY a known difference exists (DESVIO-KIT-1),
never to approve it.
    python v27b-build_frag.py"""
import json
import pathlib

HERE = pathlib.Path(__file__).resolve().parent
EQ = HERE.parent
DESVIO = ("empresa-nula", "empresa-sin-configuracion", "empresa-ausente")
NOTA_DESVIO = ("DESVIO-KIT-1, NOT approved and NOT relaxed here: legacy raises NullPointerException (ConfiguracionSistema.getParametroConfiguracionString on an unknown/null company: "
               "HashMap.get(...) is null); the new code raises the kit's ParametroNoEncontradoException, which the harness writes as 'Exception: Parametro PLA.FiltroAdministracionCarteraWS "
               "en modulo PLATAFORMA no encontrado'. Only the exception line (== excepcion) differs; salidaCruda, tablas, colaboradores, sesion and decisionServlet are identical. "
               "The module's SfmEquivalenciaTest relaxes this for the slugs empresa-nula / empresa-sin-configuracion / empresa-ausente; this check does not")


def nota_dev(front, cid):
    if front in ("sfm", "ws"):
        slug = cid.split("_", 1)[1]
        if slug.startswith("ws-"):
            slug = slug[3:]
        if slug in DESVIO:
            return NOTA_DESVIO
    return None


def frag(zona):
    out = []
    titulos = {"ifm": "ConsultarInstrumentosFondosMutuos", "sfm": "ConsultarSolicitudesFFMM", "ws": "ConsultarSolicitudesFFMMWS"}
    for fr in ("ifm", "sfm", "ws"):
        if zona == "dev":
            base = HERE / "dev" / fr
            ids = sorted(p.stem for p in (base / "legacy").glob("*.txt"))
            rel = "verify20261027/dev/%s" % fr
        else:
            base = HERE / "fresh" / fr / "out"
            ids = sorted(p.stem for p in (base / "legacy").glob("*.txt"))
            rel = "verify20261027/fresh/%s/out" % fr
        for cid in ids:
            c = {"id": ("%s-%s" % (fr, cid)) if zona == "dev" else cid, "input": cid,
                 "title": "%s %s %s" % (titulos[fr], "golden" if zona == "dev" else "fresh input", cid),
                 "legacy": "%s/legacy/%s.txt" % (rel, cid), "new": "%s/new/%s.txt" % (rel, cid), "mask": []}
            if zona == "dev":
                n = nota_dev(fr, cid)
                if n:
                    c["note"] = n
            else:
                ref = json.loads((HERE / "fresh" / fr / "cases" / cid / "case.json").read_text(encoding="utf8"))["ref"]
                c["title"] += ": " + ref
            out.append(c)
    return out


def main():
    for zona in ("dev", "fresh"):
        cases = frag(zona)
        (HERE / ("v27b-frag-%s.json" % zona)).write_text(json.dumps(cases, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
        doc = {"system": "otr-sva-optimus-corredores", "module": "BcsFondoMutuoServicios",
               "legacy": {"label": "unmodified legacy cl.bcs.fondomutuo ConsultarInstrumentosFondosMutuos / ConsultarSolicitudesFFMM / ConsultarSolicitudesFFMMWS on in-memory H2 MODE=LEGACY (record_ifm.sh / record_sfm.sh), JDK 25, UTC"},
               "new": {"label": "modernized module through its test harness (CasoIfm / CasoSfm, profile ABIERTA), same schema and seeds; canonical text by Volcar27b"},
               "cases": []}
        for c in cases:
            d = dict(c)
            for k in ("legacy", "new"):
                d[k] = "../" + c[k][len("verify20261027/"):]
            doc["cases"].append(d)
        (HERE / "tmp" / ("v27b-cases-%s.json" % zona)).write_text(json.dumps(doc, indent=1, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
        print(zona, len(cases))


if __name__ == "__main__":
    main()
