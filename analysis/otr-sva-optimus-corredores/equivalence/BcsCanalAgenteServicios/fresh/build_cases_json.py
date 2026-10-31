"""BcsCanalAgenteServicios, verificacion independiente 2026-10-30: escribe cases.json (casos de desarrollo) y fresh-cases.json (entradas frescas) en el esquema de compare.py. SOLO LISTA: no copia ni edita salidas,
no borra nada (a diferencia de make_cases.py, que borra cases/ y por eso no se ejecuto).
 - cases.json:       legacy = cases/<id>/expected.json (el golden que grabo el oraculo del desarrollo ejecutando la clase legacy REAL; no se toca)
                     new    = dev-new/<id>/expected.json (corrida del codigo nuevo con fresh/driver FrescoNuevo sobre las MISMAS entradas)
 - fresh-cases.json: legacy = fresh/legacy/<id>/expected.json (grabado por el verificador con legacy-oracle/record.sh sobre entradas propias)
                     new    = fresh/new/<id>/expected.json
Las rutas son relativas a la carpeta de los .json (equivalence/BcsCanalAgenteServicios/).

  py -3 -I fresh/build_cases_json.py
"""
import json
import pathlib

AQUI = pathlib.Path(__file__).resolve().parent
R = AQUI.parent

MASCARA_PROVENANCE = {"regex": '  "_provenance": "[^\\n]*",\\n', "why": "la linea _provenance dice quien grabo la salida (oraculo del legacy o el driver nuevo); no es salida del producto"}
MASCARA_NPE = {"regex": '(?<="excepcion": "NullPointerException: )[^\n]*', "why": "mensaje de NullPointerException generado por la JVM (el legacy compilado nombra la variable/metodo de la JVM de su compilacion; la clase y el resto de la salida SI se comparan): texto del runtime, no salida del producto"}
MASCARA_VARIABLE_NPE = {"regex": r'(?<= because \\")[^"\\\n]*(?=\\" is null)', "why": "nombre de la variable en el mensaje helpful-NPE de la JVM incrustado en 'Map Object failed' (el legacy compilado sin -g dice <local12>, el codigo nuevo dice obj): texto del runtime, no salida del producto. La parte 'Parameters:' que sigue NO se enmascara"}
MASCARAS = [MASCARA_PROVENANCE, MASCARA_NPE, MASCARA_VARIABLE_NPE]

LEG = {"label": "legacy cl.bcs.canalagente.ConsultarHistorialMovimientosCuentaInversion REAL (compilado desde legacy/, sin edicion) con BcsUtils SQL/NamedParameterStatement/JavaCall reales, sobre H2 en memoria MODE=LEGACY, JDK 25, UTC; colaborador ConsultarCartolaDeMovimiento = doble registrado",
       "command": "legacy-oracle/record.sh CAN (goldens de desarrollo, 20 corridas por caso); entradas frescas: ORACLE_CASES=fresh/legacy legacy-oracle/record.sh CAN-FRG"}
NEW = {"label": "BcsCanalAgenteServicios modernizado (src/main) sobre H2 en memoria, JDK 25, UTC, compuerta abierta, colaborador guionado",
       "command": "java cl.bcs.canalagente.caracterizacion.FrescoNuevo <entradas> <salida> (fresh/driver/, usa CasoCanal.ejecutar de las pruebas); desarrollo: cases -> dev-new; frescas: fresh/inputs -> fresh/new"}


def escribe(nombre, sistema, casos):
    salida = {"system": sistema, "legacy": LEG, "new": NEW, "cases": casos}
    (R / nombre).write_text(json.dumps(salida, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    print("%s: %d casos" % (nombre, len(casos)))


dev = sorted(p.name for p in (R / "cases").iterdir() if p.is_dir())
escribe("cases.json", "otr-sva-optimus-corredores / BcsCanalAgenteServicios (casos de desarrollo)",
        [{"id": i, "input": i, "title": i, "legacy": "cases/%s/expected.json" % i, "new": "dev-new/%s/expected.json" % i, "mask": MASCARAS} for i in dev])

fre_dir = AQUI / "legacy"
if fre_dir.exists():
    ids = sorted(p.name for p in fre_dir.iterdir() if p.is_dir())
    casos = []
    for i in ids:
        caso = json.loads((AQUI / "inputs" / i / "case.json").read_text(encoding="utf-8"))
        casos.append({"id": i, "input": i, "title": "%s %s (%s)" % (caso["servicio"], i.split("_", 1)[1], ",".join(caso.get("ref", []))),
                      "legacy": "fresh/legacy/%s/expected.json" % i, "new": "fresh/new/%s/expected.json" % i, "mask": MASCARAS})
    escribe("fresh-cases.json", "otr-sva-optimus-corredores / BcsCanalAgenteServicios (entradas frescas del verificador)", casos)
