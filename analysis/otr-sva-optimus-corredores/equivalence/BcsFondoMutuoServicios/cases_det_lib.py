"""Helpers of the F3 W1 front 4 cases (ConsultarDetalleSolicitud, read only): case directories cases-det/FMM-DET-nnn_slug.

The case INPUTS (case.json, seed.sql), schema-det.sql and seed/base-det.sql are written here; the goldens (expected.json) are RECORDED by legacy-oracle/record_det.sh from
the unmodified legacy, never by this script. The cases live in their OWN directory (cases-det/, passed to the shared OracleRunner with -Doracle.cases): record.sh /
record_fsa.sh / record_rre.sh / record_sfm.sh never run them.

What DET really executes: it has no SQL of its own (ConsultarDetalleSolicitud_SelectSolcitudes.sql is on disk and NOT loaded by any class, D-F3A-24); it delegates to
ConsultarSolicitudAporteRescateUI, which runs ConsultarSolicitudAporteRescateUI_SelectSolicitudes.sql (270 lines, ~25 tables and views from 8 modules), values the
solicitudes in state ING / PEN with CalcularMontoNumeroCuota and computes the APV retention. The oracle runs ALL THREE real classes; only Plataforma CalcularParidad and
EntregarPreciosTasas (the two collaborators of CalcularMontoNumeroCuota outside the module) are registered doubles, scripted per case ("colaboradores").

schema-det.sql: H2 STAND-INS. Tables that the F1 schema (schema-fmm.sql) already has are reused (SolicitudAporteRescate with its REAL types, TipoMovimientoFondoMutuo,
SerieDeFondo, Fondo, RegimenFondoMutuo, ACC.Usuario, POR.Portafolio, APV TRA.OrdenTraspaso / TRA.Traspaso); the ones it lacks are created with ONLY the columns the SQL reads;
each column type is REAL when the catalog CSVs of hana_qa_results have it and ASSUMED (taken from the pojo field the legacy maps it to) otherwise; the generated file tags
every column. Missing columns of existing tables are added with ALTER TABLE ... ADD COLUMN IF NOT EXISTS. Two HANA functions are aliased (MAP, TO_DECIMAL: H2FnDet).
"""
import csv
import json
import pathlib

BASE = pathlib.Path(__file__).resolve().parent
HANA = BASE.parent.parent / "hana_qa_results"
CASES = BASE / "cases-det"
SERVICIO = "ConsultarDetalleSolicitud"

SOLICITUD = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
PORTAFOLIO = "BSCL.Portafolio.BD::POR.Portafolio"
CONFIG = '"BSCL.Plataforma.BD::PLA.Configuracion"'
PARAM = '"BSCL.FondoMutuo.BD::FMM.Parametro"'
VALORCUOTA = '"BSCL.FondoMutuo.BD::FMM.ValorCuota"'
TIPOCAMBIO = '"BSCL.FondoMutuo.BD::FMM.ValorTipoCambio"'

N = lambda n: "NVARCHAR(%d)" % n  # noqa: E731
LONG, DEC, DATE, TS = "BIGINT", "DECIMAL(26,8)", "DATE", "TIMESTAMP"

# NEW stand-ins: (table, kind, [(column, fallback type)]). kind = TABLE | VIEW (H2: both are tables).
NUEVAS = [
    ("BSCL.Plataforma.BD::PLA.Estado", [("IdEstado", N(3)), ("Nombre", N(100))]),
    ("BSCL.Plataforma.BD::PAG.MedioDePago", [("IdMedioDePago", N(3)), ("Nombre", N(100))]),
    ("BSCL.Plataforma.BD::INS.TipoOperacion", [("IdTipoOperacion", N(6)), ("Nombre", N(100))]),
    ("BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV", [("IdMatrizAgrupadorAPV", LONG), ("IdProductoAgrupador", LONG), ("EsADC", N(1))]),
    ("BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV", [("IdMatrizAgrupadorNOAPV", LONG), ("CodigoPortafolio", N(100)), ("EsADC", N(1))]),
    ("BSCL.APV.BD::APV.ProductoAgrupador", [("IdProductoAgrupador", LONG), ("NombreProductoAgrupador", N(100))]),
    ("BSCL.APV.BD::REC.Cliente", [("IdRecaudacionCliente", LONG), ("IdRecaudacionEmpleador", LONG)]),
    ("BSCL.APV.BD::REC.Empleador", [("IdRecaudacionEmpleador", LONG), ("PeriodoPago", N(20))]),
    ("BSCL.APV.BD::SCS.EntidadRecaudadora", [("IdEntidadRecaudadora", LONG), ("Nombre", N(100))]),
    ("BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente", [("IdCliente", LONG), ("DocumentoIdentidad", N(20)), ("NombreCompleto", N(200)), ("Nombres", N(100)),
                                                           ("ApellidoPaterno", N(100)), ("ApellidoMaterno", N(100))]),
    ("BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado", [("IdEmpleado", LONG), ("NombreCompleto", N(200))]),
    ("BSCL.FondoMutuo.BD::FMM.OmnibusDetalle", [("IdOmnibusDetalle", LONG), ("IdOmnibus", LONG)]),
    ("BSCL.FondoMutuo.BD::TRA.Traspaso", [("IdTraspaso", LONG), ("IdTipoMovimientoTraspaso", N(10)), ("IdEntidadRecaudadora", LONG)]),
    ("BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino", [("IdSolicitudAporteRescate", LONG), ("IdTraspaso", LONG)]),
    ("BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen", [("IdSolicitudAporteRescate", LONG), ("IdTraspaso", LONG)]),
    ("BSCL.Facturacion.BD::FAC.MovimientoComprobante", [("IdTransaccion", LONG), ("FolioTransaccion", N(40)), ("IdTipoCanalIngreso", N(3)), ("IdComprobante", LONG),
                                                        ("IdTipoProducto", N(4))]),
    ("BSCL.Mesa.BD::SPT.OperacionSpot", [("IdOperacionSpotOrigen", LONG), ("Estado", N(3)), ("IdOperacionSpot", LONG), ("EstadoConfirmacion", N(3)),
                                         ("IdTipoOperacion", N(6)), ("FechaLiquidacion", DATE), ("MontoMonedaSecundaria", DEC)]),
    ("BSCL.Tesoreria.BD::TES.Comprobante", [("IdComprobante", LONG)]),
    ("BSCL.Tesoreria.BD::TES.ComprobanteDetalle", [("IdComprobanteDetalle", LONG), ("IdComprobante", LONG), ("IdTipoComprobanteOrigen", N(3)), ("IdComprobanteOrigen", LONG),
                                                   ("IdEstado", N(3))]),
    ("BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago", [("IdComprobante", LONG), ("IdEstado", N(3)), ("IdMedioDePago", N(3)), ("NombreMedioDePago", N(100))]),
    ("BSCL.Tesoreria.BD::TES.IngresoEgreso", [("IdIngresoEgreso", LONG), ("IdEstado", N(3)), ("FechaLiquidacion", DATE), ("IdComprobanteOrigen", LONG),
                                              ("IdModulo", N(3)), ("IdTipoProducto", N(4)), ("IdOperacion", LONG)]),
]
# columns the SQL reads from tables the F1 schema already has but without them
AGREGADAS = [
    (PORTAFOLIO, "DescripcionPortafolio", N(100)), (PORTAFOLIO, "IdMatrizAgrupadorAPV", LONG),
    ("BSCL.APV.BD::TRA.Traspaso", "TipoMovimiento", N(10)), ("BSCL.APV.BD::TRA.Traspaso", "NombreEntidad", N(100)),
    (SOLICITUD, "Modificaciones", N(1)), ("BSCL.Plataforma.BD::ACC.Usuario", "CodigoUsuario", N(40)),
    ("BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo", "Descripcion", N(100)), ("BSCL.FondoMutuo.BD::FMM.SerieDeFondo", "ISIN", N(40)),
]

_cat = {}


def _catalogo():
    if _cat:
        return _cat
    for f in sorted(HANA.glob("*columnas*.csv")):
        with open(f, encoding="utf-8", newline="") as fh:
            for r in csv.DictReader(fh):
                t = r.get("TABLE_NAME") or r.get("VIEW_NAME")
                if t and r.get("COLUMN_NAME") and r.get("DATA_TYPE_NAME"):
                    _cat.setdefault((t, r["COLUMN_NAME"]), (r["DATA_TYPE_NAME"], r.get("LENGTH") or "", r.get("SCALE") or "", f.name))
    return _cat


def _tipo_real(tabla, col):
    c = _catalogo().get((tabla, col))
    if not c:
        return None
    n, ln, sc, fuente = c
    if n in ("NVARCHAR", "VARCHAR"):
        return "NVARCHAR(%s)" % ln, fuente
    if n == "DECIMAL":
        return "DECIMAL(%s,%s)" % (ln, sc or 0), fuente
    if n in ("BIGINT", "INTEGER", "DATE"):
        return n, fuente
    if n in ("TIMESTAMP", "SECONDDATE"):
        return "TIMESTAMP", fuente
    return None


def _col(tabla, col, fallback):
    r = _tipo_real(tabla, col)
    if r:
        return '  "%s" %s' % (col, r[0]), "REAL (%s)" % r[1]
    return '  "%s" %s' % (col, fallback), "ASSUMED"


def schema_det():
    """schema-det.sql: aliases + stand-in tables (H2 ONLY, never executed against HANA)."""
    s = ["-- GENERATED by make_cases_det.py (cases_det_lib.schema_det). H2 STAND-INS of what ConsultarSolicitudAporteRescateUI_SelectSolicitudes.sql joins and the F1 schema lacks.",
         "-- Types: REAL when hana_qa_results/*columnas*.csv has the column, ASSUMED (pojo field type) otherwise. Views are tables here. No data: seed/base-det.sql and the cases fill them.",
         '-- HANA functions of that SQL that H2 lacks (class cl.bcs.oracle.H2FnDet; TO_BIGINT and TO_NVARCHAR come from schema-session.sql):',
         'CREATE ALIAS MAP FOR "cl.bcs.oracle.H2FnDet.map";',
         'CREATE ALIAS TO_DECIMAL FOR "cl.bcs.oracle.H2FnDet.toDecimal";']
    for tabla, cols in NUEVAS:
        lineas, tags = [], []
        for c, fb in cols:
            d, t = _col(tabla, c, fb)
            lineas.append(d)
            tags.append("%s: %s" % (c, t))
        s.append("-- %s | %s" % (tabla, "; ".join(tags)))
        s.append('CREATE TABLE "%s" (\n%s\n);' % (tabla, ",\n".join(lineas)))
    s.append("-- columns the F1 schema lacks on tables it already has")
    for tabla, c, fb in AGREGADAS:
        d, t = _col(tabla, c, fb)
        s.append('ALTER TABLE "%s" ADD COLUMN IF NOT EXISTS %s; -- %s' % (tabla, d.strip(), t))
    return "\n".join(s) + "\n"


def q(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "'S'" if v else "'N'"
    if isinstance(v, int):
        return str(v)
    return "'" + str(v).replace("'", "''") + "'"


def ins(tabla, **v):
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (tabla, ", ".join('"%s"' % k for k in v), ", ".join(q(x) for x in v.values()))


def upd(tabla, sets, donde):
    return 'UPDATE "%s" SET %s WHERE %s;' % (tabla, ", ".join('"%s" = %s' % (k, q(x)) for k, x in sets.items()), donde)


def base_det():
    """seed/base-det.sql: the FAKE rows every FMM-DET case needs on top of seed/base-fmm.sql (loaded through esquemasExtra after schema-det.sql)."""
    s = ["-- GENERATED by make_cases_det.py (cases_det_lib.base_det). FAKE universe of the FMM-DET cases, on top of seed/base-fmm.sql."]
    for ide, nom in (("ING", "Ingresado"), ("PEN", "Pendiente"), ("CER", "Certificado"), ("LIQ", "Liquidado"), ("ANU", "Anulado"), ("ACT", "Activo"), ("CNF", "Confirmado"),
                     ("PRO", "Procesado"), ("MOD", "Modificado")):
        s.append(ins("BSCL.Plataforma.BD::PLA.Estado", IdEstado=ide, Nombre=nom))
    for idm, nom in (("CIN", "Cuenta de inversion"), ("TRF", "Transferencia"), ("CHQ", "Cheque")):
        s.append(ins("BSCL.Plataforma.BD::PAG.MedioDePago", IdMedioDePago=idm, Nombre=nom))
    s.append(ins("BSCL.Plataforma.BD::INS.TipoOperacion", IdTipoOperacion="CPUSD", Nombre="Compra dolar"))
    s.append(ins("BSCL.Plataforma.BD::INS.TipoOperacion", IdTipoOperacion="VTUSD", Nombre="Venta dolar"))
    # clients (view) and executive
    for idc, doc, nom, nombres, ap, am in ((100, "11111111-1", "Cliente Ficticio Uno", "Cliente", "Ficticio", "Uno"), (101, "22222222-2", "Cliente Ficticio Dos", "Cliente", "Ficticio", "Dos"),
                                           (200, "33333333-3", "Cliente Destino", "Cliente", "Destino", "Tres")):
        s.append(ins("BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente", IdCliente=idc, DocumentoIdentidad=doc, NombreCompleto=nom, Nombres=nombres, ApellidoPaterno=ap, ApellidoMaterno=am))
    s.append(ins("BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado", IdEmpleado=20, NombreCompleto="Ejecutivo Ficticio"))
    s.append(upd("BSCL.Plataforma.BD::ACC.Usuario", {"CodigoUsuario": "usr07"}, '"IdUsuario" = 7'))
    # portfolios: 500 general (matrix NOAPV), 501 APV (code APV-B), 502 APV (code APV-A), 503 client 101, 504 destination client 200 APV matrix
    s.append(upd(PORTAFOLIO, {"DescripcionPortafolio": "Portafolio general", "IdMatrizAgrupadorAPV": 0}, '"IdPortafolio" = 500'))
    for idp, cod, idc, prop, desc, mat in ((501, "APV-B", 100, "PROPOSITOAPV", "Portafolio APV B", 7), (502, "APV-A", 100, "PROPOSITOAPV", "Portafolio APV A", 0),
                                           (503, "PF503", 101, "PROPGEN", "Portafolio cliente dos", 0), (504, "PF504", 200, "PROPOSITOAPV", "Portafolio destino", 7)):
        s.append(ins(PORTAFOLIO, IdPortafolio=idp, CodigoPortafolio=cod, IdCliente=idc, IdEstado="ACT", Proposito=prop, DescripcionPortafolio=desc, IdMatrizAgrupadorAPV=mat))
    s.append(ins("BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV", IdMatrizAgrupadorAPV=7, IdProductoAgrupador=3, EsADC="S"))
    s.append(ins("BSCL.APV.BD::APV.ProductoAgrupador", IdProductoAgrupador=3, NombreProductoAgrupador="Producto APV tres"))
    s.append(ins("BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV", IdMatrizAgrupadorNOAPV=8, CodigoPortafolio="PF500", EsADC="N"))
    s.append(ins("BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV", IdMatrizAgrupadorNOAPV=9, CodigoPortafolio="APV-A", EsADC="S"))
    s.append(ins("BSCL.FondoMutuo.BD::FMM.OmnibusDetalle", IdOmnibusDetalle=9001, IdOmnibus=77))
    s.append(ins("BSCL.APV.BD::SCS.EntidadRecaudadora", IdEntidadRecaudadora=1, Nombre="Entidad Recaudadora Uno"))
    # an observed-dollar rate and a quota value for FM001A come from seed/base-fmm.sql (ValorCuota 1500.1234, rates 900 / 890)
    # regime descriptions
    return "\n".join(s) + "\n"


_n = {"n": 0}

TABLAS = [
    {"nombre": SOLICITUD, "orden": '"IdSolicitudAporteRescate"'},
    {"nombre": "BSCL.FondoMutuo.BD::FMM.ValorCuota", "orden": '"IdSerieFondo"'},
    {"nombre": "BSCL.Plataforma.BD::PLA.Configuracion", "orden": '"IdEmpresa", "CodigoParametro"'},
]


def entrada(id_=1, **campos):
    """Request; `id_` None = key written as an explicit JSON null, the string "ABSENT" = key omitted."""
    d = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM"}
    if id_ != "ABSENT":
        d["p_SolicitudesAporteRescate"] = id_
    d.update(campos)
    return d


def caso(slug, ref, ent, filas=(), seed=(), colaboradores=None, **extra):
    """Writes cases-det/FMM-DET-nnn_slug and returns the id. `seed` (parameters, series, side tables) is loaded BEFORE `filas` (the solicitud rows)."""
    datos = list(seed) + list(filas)
    _n["n"] += 1
    cid = "FMM-DET-%03d_%s" % (_n["n"], slug)
    doc = {"servicio": SERVICIO, "ref": ref, "esquemasExtra": ["schema-det.sql", "seed/base-det.sql"], "entrada": ent, "tablas": TABLAS}
    doc.update(extra)
    if colaboradores:
        doc["colaboradores"] = colaboradores
    d = CASES / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    texto = "-- case seed on top of seed/base-fmm.sql + schema-det.sql + seed/base-det.sql (fake data)\n" + ("\n".join(datos) if datos else "SELECT 1;") + "\n"
    (d / "seed.sql").write_text(texto, encoding="utf-8", newline="\n")
    return cid


def config(codigo, valor):
    """Replaces a PLATAFORMA parameter (valor None deletes it)."""
    s = ['DELETE FROM %s WHERE "IdEmpresa" = 1 AND "CodigoParametro" = %s;' % (CONFIG, q(codigo))]
    if valor is not None:
        s.append('INSERT INTO %s ("IdEmpresa", "CodigoParametro", "Valor") VALUES (1, %s, %s);' % (CONFIG, q(codigo), q(valor)))
    return s


def precios(*items):
    """EntregarPreciosTasas double: items (IdRegistro, Precio[, IdInstrumento[, FechaFuente]]); IdRegistro = IdSolicitudAporteRescate (IdInterno of the valuation)."""
    filas = []
    for it in items:
        filas.append({"IdRegistro": it[0], "IdClasificacionTipoInstrumento": "FM", "Precio": it[1], "IdInstrumento": it[2] if len(it) > 2 else 900,
                      "FechaFuente": it[3] if len(it) > 3 else "2026-03-02 00:00:00.000"})
    return {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": filas}}}


def paridad(t1=(), t2=()):
    """CalcularParidad double answering by IdTrabajo (1 = deferred amount, 2 = system currency / UF): lists of {IdInterno, FactorCambioLocal, FactorOrigenDestino, MontoDestino}."""
    return {"CalcularParidad": {"respuestas": [{"cuando": {"0.p_IdTrabajo": 1}, "respuesta": {"p_out_CalculosSalida": list(t1)}},
                                               {"cuando": {"0.p_IdTrabajo": 2}, "respuesta": {"p_out_CalculosSalida": list(t2)}}],
                                "defecto": {"p_out_CalculosSalida": []}}}


def conjunto(*dicts):
    r = {}
    for d in dicts:
        r.update(d)
    return r


def uf(id_, monto_uf, valor_uf=39000, sistema=None):
    """The three CalcularParidad(2) answers of CalcularMontoNumeroCuota for a CLP-fund solicitud `id_`: (id*4 + 2) amount in UF, (id*4 + 3) UF value, optionally (id*4 + 1) system amount."""
    r = [{"IdInterno": id_ * 4 + 2, "MontoDestino": monto_uf}, {"IdInterno": id_ * 4 + 3, "FactorOrigenDestino": valor_uf, "MontoDestino": valor_uf}]
    if sistema is not None:
        r.append({"IdInterno": id_ * 4 + 1, "MontoDestino": sistema})
    return r
