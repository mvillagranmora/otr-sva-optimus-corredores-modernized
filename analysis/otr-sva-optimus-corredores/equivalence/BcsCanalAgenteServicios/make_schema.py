#!/usr/bin/env python3
"""BcsCanalAgenteServicios oracle: writes the H2 schemas from the REAL HANA QA catalog exported by tools/HanaCatalogoCanalAgente.java (catalog/canal_*.csv, read-only SELECT on SYS views).

  py -3 -I make_schema.py

schema-canalagente.sql  (the Java path, ConsultarHistorialMovimientosCuentaInversion): the 6 tables its SQL touches, with the REAL columns, types, NOT NULL, defaults and primary keys of HANA QA:
    POR.Portafolio, CLI.Cliente, PER.Persona, PER.DocumentoIdentidad, FAC.Comprobante and the work table ..._Proceso (a GLOBAL TEMPORARY ROW table with ON COMMIT DELETE ROWS in HANA; here a plain table:
    H2 cannot model the session isolation and the delete-on-commit, see ORACLE_NOTES.md).
schema-canalagente-prc.sql  (the BD/Servicios procedures, family PRC): the view BSCL.Portafolio.BD.Vistas::ConsultarPortafolio as a TABLE with the REAL columns of the view (ASSUMPTION: a view is not a table) and two stand-ins
    for the table variables `:v_out_CartolaDeMovimiento` and `:v_out_Clientes` that the procedures read (columns named by the procedure text; types ASSUMED from the Java classes of the cartola).
HANA NVARCHAR/VARCHAR/CHAR(n) become VARCHAR(n); SECONDDATE becomes TIMESTAMP(0); BLOB stays BLOB.
"""
import csv
import pathlib

BASE = pathlib.Path(__file__).resolve().parent
CAT = BASE / "catalog"

REALES = [
    "BSCL.Portafolio.BD::POR.Portafolio",
    "BSCL.SocioNegocio.BD::CLI.Cliente",
    "BSCL.SocioNegocio.BD::PER.Persona",
    "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad",
    "BSCL.Facturacion.BD::FAC.Comprobante",
    "BSCL.CanalAgente.BD::ConsultarHistorialMovimientosCuentaInversion_Proceso",
]
VISTA = "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"

# stand-ins of the table variables of the procedures: (column, H2 type). The names are those the procedure text selects; the types are ASSUMED (cartola Java class / procedure signature).
CARTOLA_ENTRADA = [("IdCargoAbonoMovimiento", "BIGINT"), ("NombreCliente", "VARCHAR(302)"), ("IdCliente", "BIGINT"), ("IdPortafolio", "BIGINT"), ("FechaSistema", "DATE"),
                   ("FechaLiquidacionOperacion", "DATE"), ("IdMoneda", "VARCHAR(3)"), ("IdConcepto", "VARCHAR(6)"), ("NombreConcepto", "VARCHAR(100)"), ("Cargo", "DECIMAL(26, 8)"),
                   ("Abono", "DECIMAL(26, 8)"), ("IdEstado", "VARCHAR(3)"), ("NombreEstado", "VARCHAR(100)"), ("NombreTipoComprobante", "VARCHAR(100)"), ("NombreTipoProducto", "VARCHAR(100)"),
                   ("NombreTipoOperacion", "VARCHAR(100)"), ("NombreTipoMovimiento", "VARCHAR(100)"), ("SaldoSimulado", "DECIMAL(26, 8)"), ("IdComprobante", "BIGINT"),
                   ("IdMatrizAgrupador", "BIGINT"), ("EsADC", "VARCHAR(1)")]
CLIENTES_ENTRADA = [("DocumentoIdentidad", "VARCHAR(40)"), ("NombreCompleto", "VARCHAR(5000)"), ("FechaNacimiento", "TIMESTAMP(0)"), ("FechaCreacion", "TIMESTAMP(0)"),
                    ("FechaDefuncion", "TIMESTAMP(0)"), ("IdTipoCliente", "VARCHAR(40)"), ("IdCliente", "BIGINT"), ("IdPortafolio", "BIGINT"), ("NombreTipoCliente", "VARCHAR(255)"),
                    ("IdTipoPersona", "VARCHAR(2)"), ("NombreTipoPersona", "VARCHAR(255)"), ("IdSegmentoCliente", "VARCHAR(40)"), ("NombreSegmentoCliente", "VARCHAR(255)"),
                    ("DescripcionSegmentoCliente", "VARCHAR(520)"), ("IdEmpleadoEjecutivo", "BIGINT"), ("CodigoEmpleadoEjecutivo", "VARCHAR(40)"), ("NombreEmpleadoEjecutivo", "VARCHAR(400)"),
                    ("IdCategoriaInversionista", "VARCHAR(40)"), ("NombreCategoriaInversionista", "VARCHAR(100)"), ("IdEstadoCliente", "VARCHAR(40)"), ("NombreEstadoCliente", "VARCHAR(100)"),
                    ("IdEstadoPortafolio", "VARCHAR(40)"), ("NombreEstadoPortafolio", "VARCHAR(100)"), ("CodigoPortafolio", "VARCHAR(100)"), ("DescripcionPortafolio", "VARCHAR(255)"),
                    ("PropositoPortafolio", "VARCHAR(520)"), ("IdTipoBloqueoPortafolio", "VARCHAR(40)"), ("NombreTipoBloqueoPortafolio", "VARCHAR(100)"), ("IdPerfilComercial", "VARCHAR(40)"),
                    ("IdPerfilProductos", "VARCHAR(40)"), ("IdPerfilCobros", "VARCHAR(40)"), ("IdPerfilLimites", "VARCHAR(40)"), ("SaldoFlujoCaja", "DECIMAL(26, 8)"),
                    ("IdMonedaFlujoCaja", "VARCHAR(3)"), ("SaldoFlujoCustodia", "DECIMAL(26, 8)"), ("IdInstrumento", "BIGINT"), ("Nemotecnico", "VARCHAR(40)")]


def tipo(t, largo, escala):
    t = t.upper()
    if t in ("NVARCHAR", "VARCHAR", "CHAR"):
        return "VARCHAR(%s)" % largo
    if t == "DECIMAL":
        return "DECIMAL(%s, %s)" % (largo, escala or 0)
    if t == "SECONDDATE":
        return "TIMESTAMP(0)"
    if t == "BLOB":
        return "BLOB"
    if t in ("TIMESTAMP", "BIGINT", "INTEGER", "TINYINT", "DOUBLE", "DATE"):
        return t
    raise ValueError("type not mapped: " + t)


def leer(nombre):
    with open(CAT / nombre, encoding="utf-8", newline="") as f:
        return list(csv.DictReader(f))


def main():
    columnas = {}
    for r in leer("canal_columnas_tablas.csv"):
        columnas.setdefault(r["TABLE_NAME"], []).append(r)
    claves = {}
    for r in leer("canal_claves.csv"):
        claves.setdefault(r["TABLE_NAME"], []).append((int(r["POSITION"]), r["COLUMN_NAME"]))
    vista = leer("canal_columnas_vistas.csv")

    def tabla(out, nombre, cols, pk, origen):
        lineas = []
        for c in cols:
            ddl = '    "%s" %s' % (c["COLUMN_NAME"], tipo(c["DATA_TYPE_NAME"], c["LENGTH"], c["SCALE"]))
            if c.get("GENERATION_TYPE") == "BY DEFAULT AS IDENTITY":
                ddl += " GENERATED BY DEFAULT AS IDENTITY"
            if c.get("DEFAULT_VALUE"):
                d = c["DEFAULT_VALUE"]
                ddl += " DEFAULT " + (d if d.upper().startswith("CURRENT") or d.lstrip("-").isdigit() else "'%s'" % d)
            if c.get("IS_NULLABLE") == "FALSE":
                ddl += " NOT NULL"
            lineas.append(ddl)
        if pk:
            lineas.append("    PRIMARY KEY (%s)" % ", ".join('"%s"' % x for _, x in sorted(pk)))
        out.append("-- %s" % origen)
        out.append('CREATE TABLE "%s" (' % nombre)
        out.append(",\n".join(lineas))
        out.append(");")
        out.append("")

    cab = "-- BcsCanalAgenteServicios oracle and tests: H2 schema generated by make_schema.py from catalog/canal_*.csv (real HANA QA catalog). Do not edit by hand."
    out = [cab, "CREATE SCHEMA IF NOT EXISTS BSCL;", "SET SCHEMA BSCL;", ""]
    for nombre in REALES:
        assert nombre in columnas, nombre
        origen = "HANA QA catalog" if "_Proceso" not in nombre else "HANA QA catalog (GLOBAL TEMPORARY ROW table, ON COMMIT DELETE ROWS in HANA; a plain table here)"
        tabla(out, nombre, columnas[nombre], claves.get(nombre), origen)
    (BASE / "schema-canalagente.sql").write_text("\n".join(out), encoding="utf-8", newline="\n")

    prc = [cab, "CREATE SCHEMA IF NOT EXISTS BSCL;", "SET SCHEMA BSCL;", ""]
    cols = [dict(r, IS_NULLABLE="TRUE", DEFAULT_VALUE="", GENERATION_TYPE="") for r in vista]
    for c, r in zip(cols, vista):
        c["COLUMN_NAME"] = r["COLUMN_NAME"]
    tabla(prc, VISTA, cols, None, "ASSUMED: the view modelled as a table with the REAL columns of the view (catalog/canal_columnas_vistas.csv); the case seeds its rows")
    for nombre, lista in (("BSCL.CanalAgente.PRC::CartolaEntrada", CARTOLA_ENTRADA), ("BSCL.CanalAgente.PRC::ClientesEntrada", CLIENTES_ENTRADA)):
        prc.append("-- ASSUMED: stand-in of the table variable the procedure reads (SQLScript table variables do not exist in H2)")
        prc.append('CREATE TABLE "%s" (' % nombre)
        prc.append(",\n".join('    "%s" %s' % (c, t) for c, t in lista))
        prc.append(");")
        prc.append("")
    (BASE / "schema-canalagente-prc.sql").write_text("\n".join(prc), encoding="utf-8", newline="\n")
    print("schema-canalagente.sql:", len(REALES), "real tables; schema-canalagente-prc.sql: 1 view stand-in + 2 table-variable stand-ins")


if __name__ == "__main__":
    main()
