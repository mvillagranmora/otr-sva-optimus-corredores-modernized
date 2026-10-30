"""Escribe ../fresh-cases.json (esquema de compare.py) a partir de fresh/legacy/<id>/expected.json y fresh/new/<id>/expected.json. Solo lista; no copia ni edita salidas.
Las rutas son relativas a la carpeta de fresh-cases.json (equivalence/BcsReplica/).

  py -3 -I build_fresh_cases.py
"""
import json
import pathlib

AQUI = pathlib.Path(__file__).resolve().parent
RAIZ = AQUI.parent  # equivalence/BcsReplica
ids = sorted(p.name for p in (AQUI / "legacy").iterdir() if p.is_dir())

MASCARA_PROVENANCE = {"regex": '  "_provenance": "[^\\n]*",\\n', "why": "la linea _provenance describe quien grabo el golden (el oraculo del legacy); la corrida nueva no la escribe. No es salida del producto"}

MASCARA_NPE = {"regex": '(?<="excepcion": "NullPointerException: )[^\n]*', "why": "mensaje de NullPointerException generado por la JVM (el legacy compilado sin -g nombra la variable <local4>/<parameter2>; la clase y el resto de la salida SI se comparan): texto del runtime, no salida del producto"}

casos = []
for i in ids:
    caso = json.loads((AQUI / "inputs" / i / "case.json").read_text(encoding="utf-8"))
    casos.append({
        "id": i,
        "input": i,
        "title": "%s %s (%s)" % (caso["servicio"], i.split("_", 1)[1], ",".join(caso.get("ref", []))),
        "legacy": "fresh/legacy/%s/expected.json" % i,
        "new": "fresh/new/%s/expected.json" % i,
        "mask": [MASCARA_PROVENANCE, MASCARA_NPE],
    })

salida = {
    "system": "otr-sva-optimus-corredores / BcsReplica",
    "legacy": {"label": "legacy cl.bcs.replica real (ReplicaUtils, IniciarReplica; EnviarTablaReplica e IniciarReplicaFull/Delta con una edicion de reloj) sobre H2 en memoria, JDK 25, UTC",
               "command": "legacy-oracle/record.sh REP-FRE con ORACLE_CASES=fresh/legacy ORACLE_REPORT=fresh/determinism-fresh.txt (20 corridas por caso)"},
    "new": {"label": "BcsReplica modernizado (src/main) sobre H2 en memoria, JDK 25, UTC, compuertas abiertas, colaboradores guionados",
            "command": "java cl.bcs.replica.caracterizacion.FrescoNuevo fresh/inputs fresh/new (fresh/driver/, usa CasoReplica.ejecutar de las pruebas)"},
    "cases": casos,
}
(RAIZ / "fresh-cases.json").write_text(json.dumps(salida, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
print("fresh-cases.json: %d casos" % len(casos))
