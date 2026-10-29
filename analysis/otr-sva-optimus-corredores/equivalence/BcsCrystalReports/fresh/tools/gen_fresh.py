"""Generates fresh/input/<Dominio>_<Plantilla>/{spec.json,data.json}: NEW templates (none of the 20 development ones) and
boundary-value fake rows. usage: py -3 -I gen_fresh.py <BcsCrystalReports module dir> <out input dir>
Reads (read-only): <module>/target/probe_all.json (Crystal API dump of tables/fields/subreports/pictures/formulas) and the
.sql beside the templates (lengths). Uses the development gen_sql.py (copied here unchanged)."""
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import fresh_values as fv   # noqa: E402
import gen_sql              # noqa: E402

FORMATS = ["pdf", "excel", "word", "rtf", "txt"]
# (Dominio, plantilla, filas). Orden y CuentaInversion solo tienen sus 2 plantillas ya usadas en desarrollo.
CASES = [
    ("APV", "Templates/NOTIFICACIONEMPLEADORSAAPV.rpt", 4),
    ("Contabilidad", "Templates/InformeOperacionesPactosConDetalle.rpt", 7),
    ("Contabilidad", "Templates/INFORME_CONTROL_FACTURACION_OPERACIONES_RESUMEN.rpt", 3),
    ("Custodia", "Templates/TRASPASO_ENTRE_CORREDORES.rpt", 5),
    ("Custodia", "Templates/INFORME_SIMULTANEAS_VALORIZADA.rpt", 4),
    ("Custodia", "Templates/COMPROBANTEINGRESORETIROGARANTIA.rpt", 2),
    ("Facturacion", "Templates/InformeGananciaCapital.rpt", 6),
    ("FondoMutuo", "Templates/template_cuadratura_liquidacion.rpt", 8),
    ("FondoMutuo", "Templates/template_registro_participes_ffmm.rpt", 40),
    ("Mesa", "Templates/MSP_INFORME_OPERACIONES_NO_FACTURABLES.rpt", 6),
    ("Mesa", "Templates/OperacionesSobre500000.rpt", 5),
    ("Normativa", "Templates/CI63Carta.rpt", 2),
    ("Normativa", "Templates/Anexo4.rpt", 12),
    # REJECTED (legacy engine cannot render them with string-typed dates; see input-rejected/ and fresh-cases.json leftOut):
    #   ("Mesa", "Templates/MRF_QUINCENAL_DE_PACTOS.rpt", 2)   -> "Failed to regenerate records"
    #   ("Normativa", "Templates/CI63Transacciones.rpt", 12)   -> Exception in formula '{@Mes}' at 'Month ({Comando.FechaOperacion})'
    ("Normativa", "Templates/CIRCERTIFICADOCUSTODIA_SURA.rpt", 3),
    ("Tesoreria", "Templates/CartaInstruccionPago.rpt", 4),
    ("Tesoreria", "Templates/MovimientosTesoreria.rpt", 10),
]


def dash_fields(formulas):
    return set(m.group(1) for f in formulas.values() for m in re.finditer(r'Split\(\{\w+\.(\w+)\}\s*,\s*"-"\)', f))


def rows_for(mod, table, domain, n, dashes=()):
    sqlp = gen_sql.find_sql(os.path.join(mod, "templates"), domain, table.get("command"))
    cols = gen_sql.columns(sqlp)
    schema, rows = {}, [dict() for _ in range(n)]
    for name, jrc in table["fields"].items():
        for i in range(n):
            v, st = fv.synth(name, jrc, cols.get(name), i, name in dashes)
            rows[i][name] = v
            schema[name] = st
    return schema, rows, os.path.basename(sqlp) if sqlp else None


def main(mod, out):
    probe = {o["rpt"]: o for o in json.load(open(os.path.join(mod, "target", "probe_all.json"), encoding="utf-8"))}
    for domain, rel, n in CASES:
        name = domain + "_" + re.sub(r"[^A-Za-z0-9_]", "_", os.path.splitext(os.path.basename(rel))[0])
        key = "templates/%s/%s" % (domain, rel)
        o = probe[key]
        schema, rows, sqlf = rows_for(mod, o["tables"][0], domain, n, dash_fields(o.get("formulas", {})))
        data = {"schema": schema, "rows": rows}
        if o["subreports"]:
            data["subreports"] = {}
            for sn, tabs in o["subreports"].items():
                s2, r2, _ = rows_for(mod, tabs[0], domain, 3)
                data["subreports"][sn] = {"schema": s2, "rows": r2}
        params = []
        for pic in sorted(o["pictures"], key=lambda p: p.lower().startswith("pdf417")):
            if pic.lower().startswith("pdf417"):
                params.append({"type": "pdf417", "name": pic, "data": "BCS|FOLIO-9876543|20240229|USD|-1500.50"})
            else:
                params.append({"type": "image", "name": pic, "data": fv.png_b64(150, 60, 17)})
        spec = {"template": key, "formats": FORMATS, "params": params, "sql": sqlf,
                "note": "FRESH input (boundary values), template not used by development"}
        d = os.path.join(out, name)
        os.makedirs(d, exist_ok=True)
        for fn, obj in (("spec.json", spec), ("data.json", data)):
            with open(os.path.join(d, fn), "w", encoding="utf-8", newline="\n") as fh:
                json.dump(obj, fh, ensure_ascii=False, indent=1)
        print(name, "rows=%d fields=%d subs=%s params=%s" % (n, len(schema), list(o["subreports"]), [p["name"] for p in params]))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
