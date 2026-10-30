"""Escribe ../cases.json (esquema de compare.py) para los 264 casos de desarrollo: legacy = cases/<id>/expected.json (golden grabado; NO se toca), new = dev-new/<id>/expected.json (corrida del codigo nuevo con fresh/driver). Solo lista."""
import json, pathlib
R = pathlib.Path(__file__).resolve().parent.parent
ids = sorted(p.name for p in (R / "cases").iterdir() if p.is_dir())
M = {"regex": '  "_provenance": "[^\n]*",\n', "why": "la linea _provenance dice quien grabo la salida (oraculo del legacy o el driver nuevo); no es salida del producto"}
N = {"regex": '(?<="excepcion": "NullPointerException: )[^\n]*', "why": "mensaje de NullPointerException generado por la JVM (el legacy compilado sin -g nombra la variable <local4>/<parameter2>; la clase y el resto de la salida SI se comparan): texto del runtime, no salida del producto"}
c = [{"id": i, "input": i, "title": i, "legacy": "cases/%s/expected.json" % i, "new": "dev-new/%s/expected.json" % i, "mask": [M, N]} for i in ids]
d = {"system": "otr-sva-optimus-corredores / BcsReplica (casos de desarrollo)",
     "legacy": {"label": "golden grabado del legacy real sobre H2 (record.sh)", "command": "legacy-oracle/record.sh REP"},
     "new": {"label": "BcsReplica modernizado sobre H2", "command": "java cl.bcs.replica.caracterizacion.FrescoNuevo cases dev-new (fresh/driver)"}, "cases": c}
(R / "cases.json").write_text(json.dumps(d, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
print(len(c))
