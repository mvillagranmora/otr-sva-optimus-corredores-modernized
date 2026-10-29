"""One line per FMM-RRE golden (or dump directory): decision, exception, counter, message codes, rows per table, collaborators, session. Reading aid only.

  py -3 -I tools-w1b-rre/resumen.py [prefix] [dumpDir]
"""
import json
import pathlib
import sys

BASE = pathlib.Path(__file__).resolve().parent.parent


def linea(nombre, g):
    s = g.get("salida") or {}
    msgs = ",".join("%s:%s" % (m.get("codigo"), m.get("id")) for m in s.get("P_OUT_MENSAJES", []))
    tablas = " ".join("%s=%d" % (k.split("::")[-1].replace("FMM.", "")[:14], len(v)) for k, v in g.get("tablas", {}).items())
    col = ",".join(c["colaborador"] for c in g.get("colaboradores", []))
    ses = g.get("sesion", {})
    return "%s | %s | %s | cnt=%s res=%s | %s | %s | %s | aut=%s loc=%s" % (
        nombre, g.get("decisionServlet"), (g.get("excepcion") or "")[:90], s.get("P_OUT_ERRORESCONTADOR"), len(s.get("P_OUT_RESULTADO", []) or []),
        msgs, tablas, col, ses.get("commitsAutonomos"), ses.get("tablasLocalesSinDrop"))


def main():
    pref = sys.argv[1] if len(sys.argv) > 1 else "FMM-RRE"
    if len(sys.argv) > 2:
        for p in sorted(pathlib.Path(sys.argv[2]).glob(pref + "*.json")):
            print(linea(p.stem, json.loads(p.read_text(encoding="utf-8"))))
        return
    for d in sorted((BASE / "cases").glob(pref + "*")):
        e = d / "expected.json"
        if e.exists():
            print(linea(d.name, json.loads(e.read_text(encoding="utf-8"))))


if __name__ == "__main__":
    main()
