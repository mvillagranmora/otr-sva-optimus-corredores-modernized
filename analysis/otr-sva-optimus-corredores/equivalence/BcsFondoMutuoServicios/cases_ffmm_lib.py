"""Helpers of the F3 W1 front 9 cases (ConsultarCustodiaPortafolioFFMM, FFMM; read only): case directories cases-ffmm/FM-CPFF-Cnn_slug.

The case INPUTS (case.json, seed.sql), schema-ffmm.sql and seed/base-ffmm.sql are written here; the goldens (expected.json) are RECORDED by legacy-oracle/record_ffmm.sh from the
unmodified legacy, never by this script. The cases live in their OWN directory (cases-ffmm/), passed to the shared OracleRunner with -Doracle.cases: no other record script runs them
and this script never touches another front's goldens.

What FFMM really executes (RULES_FONDOMUTUO_F3_C.md 8.2): ONE of two custody SELECT (vigente / historico), ONE of two guarantee SELECT, the persons-related SELECT, over ~20 tables and
views of 8 modules, ConsultarEmpresa (always, first), ConfiguracionSistema (PLA.FechaSistema) and then an in-memory aggregation in HashMaps. The oracle runs the REAL class on H2; the ONLY
registered double is ConsultarEmpresa (Plataforma, outside the module), scripted per case ("colaboradores"); ConfiguracionSistema reads PLA.Configuracion from H2 (REAL).

schema-ffmm.sql: H2 STAND-INS. The F1 schema (schema-fmm.sql) already has a REDUCED CUS.Cartera and views of SOC / ConsultarPortafolio: CUS.Cartera is DROPPED and re-created here with the
columns the five SQL read (types REAL from hana_qa_results/cat_columnas_tablas.csv); HIS.Cartera, GTA.Garantia and HIS.Garantia are created with the types the owner verified in HANA QA
(read only, 2026-10-28: CUS/GTA all NOT NULL, amounts DECIMAL(26,8), IdBoveda / IdTipoInstrumento NVARCHAR(3), ids BIGINT; HIS.* the same columns almost all NULLABLE plus FechaRespaldo
TIMESTAMP(27,7)); every other table / view is tagged REAL when the catalog CSV has it and ASSUMED otherwise. One HANA function is aliased (MAP: H2FnFfmm).
"""
import json
import pathlib
import sys
from decimal import Decimal

BASE = pathlib.Path(__file__).resolve().parent
CASES = BASE / "cases-ffmm"
SERVICIO = "ConsultarCustodiaPortafolioFFMM"
sys.path.insert(0, str(BASE))
from cases_cpf_lib import SERIES, regimen, sol, clonar_serie, RawSql, fecha  # noqa: E402,F401

CUS = "BSCL.Custodia.BD::CUS.Cartera"
HISC = "BSCL.Custodia.BD::HIS.Cartera"
GTA = "BSCL.Custodia.BD::GTA.Garantia"
HISG = "BSCL.Custodia.BD::HIS.Garantia"
CLI = "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
SOCT = "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"
SOCV = "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"
REL = "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona"
INSV = "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"
INST = "BSCL.Plataforma.BD::INS.Instrumento"
PORT = "BSCL.Portafolio.BD::POR.Portafolio"
PORV = "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"
MAPV = "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"
MNOAPV = "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV"
PROD = "BSCL.APV.BD::APV.ProductoAgrupador"
CONFIG = "BSCL.Plataforma.BD::PLA.Configuracion"
SOLICITUD = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
SERIE = "BSCL.FondoMutuo.BD::FMM.SerieDeFondo"

N = lambda s: RawSql(str(s))  # noqa: E731  (a numeric / raw SQL literal)
D = lambda s: RawSql("DATE '%s'" % s)  # noqa: E731
TS = lambda s: RawSql("TIMESTAMP '%s'" % s)  # noqa: E731


def lit(v):
    if v is None:
        return "NULL"
    if isinstance(v, RawSql):
        return str(v)
    if isinstance(v, bool):
        return "'S'" if v else "'N'"
    if isinstance(v, (int, Decimal)):
        return str(v)
    if isinstance(v, float):
        return repr(v)
    return "'" + str(v).replace("'", "''") + "'"


def ins(tabla, **v):
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (tabla, ", ".join('"%s"' % k for k in v), ", ".join(lit(x) for x in v.values()))


def upd(tabla, sets, donde):
    return 'UPDATE "%s" SET %s WHERE %s;' % (tabla, ", ".join('"%s" = %s' % (k, lit(x)) for k, x in sets.items()), donde)


# ---- the custody tables ------------------------------------------------------------------------------------------------------------------------------
# (column, type, REAL tag). CUS.Cartera: the types of hana_qa_results/cat_columnas_tablas.csv (all NOT NULL there but IdRegimenFondoMutuo, which the CSV says is NULLABLE: see Q-F3C-10).
CARTERA = [
    ("IdCartera", "BIGINT"), ("FolioCartera", "BIGINT"), ("IdEmpresa", "BIGINT"), ("IdCliente", "BIGINT"), ("IdPortafolio", "BIGINT"), ("IdEstado", "NVARCHAR(3)"),
    ("IdClasificacionTipoInstrumento", "NVARCHAR(3)"), ("IdTipoInstrumento", "NVARCHAR(3)"), ("IdInstrumento", "BIGINT"), ("Nemotecnico", "NVARCHAR(40)"), ("IdEmisor", "NVARCHAR(40)"),
    ("IdBoveda", "NVARCHAR(3)"), ("IdRegimenFondoMutuo", "NVARCHAR(40)"), ("PrecioCompra", "DECIMAL(26,8)"), ("INGFechaOperacion", "DATE"), ("INGFechaLiquidacion", "DATE"),
    ("INGCantidad", "DECIMAL(26,8)"), ("INGMontoATasaCompraActual", "DECIMAL(26,8)"), ("INGMontoCompraActualMonedaInstrumento", "DECIMAL(26,8)"), ("EGRFechaOperacion", "DATE"),
    ("EGRFechaLiquidacion", "DATE"), ("EGRMonto", "DECIMAL(26,8)"),
]
GARANTIA = [
    ("IdCarteraOrigen", "BIGINT"), ("IdEmpresa", "BIGINT"), ("IdCliente", "BIGINT"), ("IdPortafolio", "BIGINT"), ("IdEstado", "NVARCHAR(3)"), ("IdClasificacionTipoInstrumento", "NVARCHAR(3)"),
    ("IdTipoInstrumento", "NVARCHAR(3)"), ("IdInstrumento", "BIGINT"), ("Nemotecnico", "NVARCHAR(40)"), ("IdEmisor", "NVARCHAR(40)"), ("INGFechaLiquidacion", "DATE"),
    ("INGCantidad", "DECIMAL(26,8)"), ("EGRFechaLiquidacion", "DATE"),
]
TABLAS_CUSTODIA = [(CUS, CARTERA, False, "REAL (cat_columnas_tablas.csv; owner HANA QA 2026-10-28; IdRegimenFondoMutuo is NULLABLE in the CSV while the owner note says every column is NOT NULL: Q-F3C-10)"), (HISC, CARTERA + [("FechaRespaldo", "TIMESTAMP(7)")], True, "REAL (owner HANA QA 2026-10-28: nullable, FechaRespaldo TIMESTAMP(27,7))"),
                   (GTA, GARANTIA, False, "REAL (owner HANA QA 2026-10-28)"), (HISG, GARANTIA + [("FechaRespaldo", "TIMESTAMP(7)")], True, "REAL (owner HANA QA 2026-10-28: nullable, FechaRespaldo TIMESTAMP(27,7))")]
NULABLE_SIEMPRE = {(CUS, "IdRegimenFondoMutuo")}   # cat_columnas_tablas.csv: IS_NULLABLE TRUE (the owner's note says all NOT NULL: contradiction, question Q-F3C-10)


def ddl_custodia():
    s = []
    for tabla, cols, nulable, tag in TABLAS_CUSTODIA:
        lineas = []
        for c, t in cols:
            nul = nulable or (tabla, c) in NULABLE_SIEMPRE
            lineas.append('  "%s" %s%s' % (c, t, "" if nul else " NOT NULL"))
        s.append("-- %s | %s" % (tabla, tag))
        s.append('CREATE TABLE "%s" (\n%s\n);' % (tabla, ",\n".join(lineas)))
    return s


# ---- other stand-ins (name, [(column, type)], tag) ----------------------------------------------------------------------------------------------------
NUEVAS = [
    (CLI, [("IdCliente", "BIGINT"), ("IdPersona", "BIGINT"), ("DocumentoIdentidad", "NVARCHAR(40)"), ("RazonSocial", "NVARCHAR(200)"), ("IdEmpresa", "BIGINT")], "ASSUMED (view not in any catalog CSV; IdPersona and IdEmpresa are read by the five SQL)"),
    (SOCT, [("IdSocioDeNegocio", "BIGINT"), ("IdPersona", "BIGINT"), ("IdCliente", "BIGINT")], "ASSUMED (table not in any catalog CSV)"),
    (MAPV, [("IdMatrizAgrupadorAPV", "BIGINT"), ("IdProductoAgrupador", "BIGINT"), ("EsADC", "NVARCHAR(1)")], "ASSUMED"),
    (MNOAPV, [("IdMatrizAgrupadorNOAPV", "BIGINT"), ("CodigoPortafolio", "NVARCHAR(100)"), ("EsADC", "NVARCHAR(1)")], "ASSUMED"),
    (PROD, [("IdProductoAgrupador", "BIGINT"), ("NombreProductoAgrupador", "NVARCHAR(100)")], "ASSUMED"),
    (INST, [("IdInstrumento", "BIGINT"), ("IdTipoInstrumento", "NVARCHAR(3)")], "ASSUMED"),
    ("BSCL.Plataforma.BD::INS.TipoInstrumento", [("IdTipoInstrumento", "NVARCHAR(3)"), ("Nombre", "NVARCHAR(100)"), ("IdClasificacionTipoInstrumento", "NVARCHAR(3)")], "REAL (cat_columnas_tablas.csv)"),
    ("BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento", [("IdClasificacionTipoInstrumento", "NVARCHAR(3)"), ("Nombre", "NVARCHAR(100)")], "REAL (cat_columnas_tablas.csv)"),
    (INSV, [("IdInstrumento", "BIGINT"), ("IdClasificacionTipoInstrumento", "NVARCHAR(3)"), ("NombreClasificacionTipoInstrumento", "NVARCHAR(100)"), ("IdTipoInstrumento", "NVARCHAR(3)"),
            ("NombreTipoInstrumento", "NVARCHAR(100)"), ("IdEmisor", "NVARCHAR(40)")], "ASSUMED (view not in any catalog CSV)"),
    (REL, [("IdPersonaOrigen", "BIGINT"), ("IdPersonaDestino", "BIGINT"), ("IdEstado", "NVARCHAR(3)")], "ASSUMED (view not in any catalog CSV)"),
]
AGREGADAS = [
    (PORT, "IdMatrizAgrupadorAPV", "BIGINT", "REAL (cat_columnas_tablas.csv)"),
    ("BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo", "Descripcion", "NVARCHAR(100)", "REAL (fmm_columnas_tablas.csv)"),
    (SOCV, "IdEmpresa", "BIGINT", "REAL (cat_columnas_vistas.csv)"),
    (PORV, "CodigoPortafolio", "NVARCHAR(100)", "REAL (cat_columnas_vistas.csv)"),
]


def schema_ffmm():
    s = ["-- GENERATED by make_cases_ffmm.py (cases_ffmm_lib.schema_ffmm). H2 STAND-INS of what the five ConsultarCustodiaPortafolioFFMM_*.sql read and the F1 schema lacks.",
         "-- Types: REAL when a catalog CSV of hana_qa_results or the owner's HANA QA read-only check (2026-10-28) has them, ASSUMED otherwise. Views are tables here. No data: seed/base-ffmm.sql and the cases fill them.",
         "-- HANA function of those SQL that H2 lacks (class cl.bcs.oracle.H2FnFfmm):",
         'CREATE ALIAS MAP FOR "cl.bcs.oracle.H2FnFfmm.map";',
         "-- the F1 schema holds a REDUCED CUS.Cartera (11 columns, no rows, no constraint): replaced by the full set of columns the SQL read",
         'DROP TABLE "%s";' % CUS]
    s += ddl_custodia()
    for tabla, cols, tag in NUEVAS:
        s.append("-- %s | %s" % (tabla, tag))
        s.append('CREATE TABLE "%s" (\n%s\n);' % (tabla, ",\n".join('  "%s" %s' % c for c in cols)))
    s.append("-- columns the F1 schema lacks on tables it already has")
    for tabla, c, t, tag in AGREGADAS:
        s.append('ALTER TABLE "%s" ADD COLUMN IF NOT EXISTS "%s" %s; -- %s' % (tabla, c, t, tag))
    return "\n".join(s) + "\n"


# ---- row builders ----------------------------------------------------------------------------------------------------------------------------------------
def cartera(id, tabla=CUS, **ov):
    """A CUS.Cartera / HIS.Cartera row with EVERY modelled column: a 100 quota open position of FM001A, client 100, portfolio 500, regime GEN, vault COR, in force since 2026-01-12
    (INGFechaLiquidacion) with no egress ('0001-01-01'), price 9800, purchase value 980000 (instrument currency) and 990000 (rate): `id` is IdCartera and, unless given, FolioCartera."""
    v = dict(IdCartera=id, IdEmpresa=1, IdCliente=100, IdPortafolio=500, IdEstado="VIG", IdClasificacionTipoInstrumento="FM", IdTipoInstrumento="FFM", IdInstrumento=900,
             Nemotecnico="FM001A", IdEmisor="AGF1", IdBoveda="COR", IdRegimenFondoMutuo="GEN", PrecioCompra=N("9800"), INGFechaOperacion=D("2026-01-10"), INGFechaLiquidacion=D("2026-01-12"),
             INGCantidad=N("100"), INGMontoATasaCompraActual=N("990000"), INGMontoCompraActualMonedaInstrumento=N("980000"), EGRFechaOperacion=D("0001-01-01"),
             EGRFechaLiquidacion=D("0001-01-01"), EGRMonto=N("0"))
    v.update(ov)
    if "FolioCartera" not in v:
        v["FolioCartera"] = v["IdCartera"]
    if tabla == HISC and "FechaRespaldo" not in v:
        v["FechaRespaldo"] = TS("2026-03-09 00:00:00")
    cols = [c for c, _ in CARTERA] + (["FechaRespaldo"] if tabla == HISC else [])
    return ins(tabla, **{c: v[c] for c in cols})


def garantia(tabla=GTA, origen=1, **ov):
    """A GTA.Garantia / HIS.Garantia row: 10 quotas of FM001A of client 100, portfolio 500, whose origin cartera is `origen` (IdCartera), in force since 2026-01-12, no egress."""
    v = dict(IdCarteraOrigen=origen, IdEmpresa=1, IdCliente=100, IdPortafolio=500, IdEstado="VIG", IdClasificacionTipoInstrumento="FM", IdTipoInstrumento="FFM", IdInstrumento=900,
             Nemotecnico="FM001A", IdEmisor="AGF1", INGFechaLiquidacion=D("2026-01-12"), INGCantidad=N("10"), EGRFechaLiquidacion=D("0001-01-01"))
    v.update(ov)
    if tabla == HISG and "FechaRespaldo" not in v:
        v["FechaRespaldo"] = TS("2026-03-09 00:00:00")
    cols = [c for c, _ in GARANTIA] + (["FechaRespaldo"] if tabla == HISG else [])
    return ins(tabla, **{c: v[c] for c in cols})


def his(id, **ov):
    return cartera(id, HISC, **ov)


def hgar(origen=1, **ov):
    return garantia(HISG, origen=origen, **ov)


def relacion(origen, destino, estado="ACT"):
    return ins(REL, IdPersonaOrigen=origen, IdPersonaDestino=destino, IdEstado=estado)


def cliente(idc, persona, doc, razon, empresa=1):
    return ins(CLI, IdCliente=idc, IdPersona=persona, DocumentoIdentidad=doc, RazonSocial=razon, IdEmpresa=empresa)


def socio(ids, persona, idc):
    return ins(SOCT, IdSocioDeNegocio=ids, IdPersona=persona, IdCliente=idc)


# ---- the FAKE universe ------------------------------------------------------------------------------------------------------------------------------------
# company 1 (client 99 = the corredora, person 1: ConsultarEmpresa double), clients 100 (person 7100, document 11111111-1), 101 (7101, related to the company), 102 (7102); business partners 1100 / 1101 /
# 1102 / 1099 (the person is the join key of the custody SQL); portfolios 500 (client 100, code PF500, APV matrix 0, NOAPV matrix 8 ADC N), 501 (client 100, APV-B, APV matrix 7: product 3, ADC S),
# 502 (client 101, PF502, NOAPV 9 ADC S), 503 (client 101, PF503, APV matrix NULL), 504 (client 99, PF504), 505 (client 102, PF505); instruments 900 .. 905 (series FM001A .. FM006E, families FAM1 /
# FAM2 / FAM3: the SAME series of the CPF front), all of type FFM, class FM; regimes GEN, BF, 18Q, 18T, 57BIS. FechaSistema 2026-03-10.
PORTAFOLIOS = [(500, "PF500", 100, 0), (501, "APV-B", 100, 7), (502, "PF502", 101, 0), (503, "PF503", 101, None), (504, "PF504", 99, 0), (505, "PF505", 102, 0)]
CLIENTES = [(99, 1, "76000000-0", "Corredora Ficticia", 1099), (100, 7100, "11111111-1", "Cliente Ficticio Uno", 1100), (101, 7101, "22222222-2", "Cliente Ficticio Dos", 1101),
            (102, 7102, "33333333-3", "Cliente Ficticio Tres", 1102)]
INSTRUMENTOS = {900: ("AGF1", "FM001A"), 901: ("AGF2", "FM002B"), 902: ("EXT1", "FM003U"), 903: ("AGF1", "FM004C"), 904: ("AGF2", "FM005D"), 905: ("AGF1", "FM006E")}


def base_universo(fecha_sistema="2026-03-10"):
    s = []
    s.append(upd("BSCL.Plataforma.BD::PLA.Configuracion", {"Valor": fecha_sistema}, '"IdEmpresa" = 1 AND "CodigoParametro" = \'PLA.FechaSistema\''))
    s.append(ins("BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento", IdClasificacionTipoInstrumento="FM", Nombre="Fondos Mutuos"))
    s.append(ins("BSCL.Plataforma.BD::INS.TipoInstrumento", IdTipoInstrumento="FFM", Nombre="Fondo Mutuo", IdClasificacionTipoInstrumento="FM"))
    for i, (emi, nemo) in INSTRUMENTOS.items():
        s.append(ins(INST, IdInstrumento=i, IdTipoInstrumento="FFM"))
        s.append(ins(INSV, IdInstrumento=i, IdClasificacionTipoInstrumento="FM", NombreClasificacionTipoInstrumento="Fondos Mutuos", IdTipoInstrumento="FFM", NombreTipoInstrumento="Fondo Mutuo", IdEmisor=emi))
    # series (same universe as the CPF front): FM001A is the F1 base series; the others are cloned from it
    s.append(upd(SERIE, {"IdFamiliaFondo": "FAM1", "NombreSerieFondo": "Serie A ficticia", "CodigoSerie": "A"}, '"IdSerieFondo" = \'FM001A\''))
    s.append(upd("BSCL.FondoMutuo.BD::FMM.Fondo", {"NombreFondo": "Fondo Ficticio Uno"}, '"IdFondo" = \'10\''))
    for sid, d in SERIES.items():
        if sid == "FM001A":
            continue
        fo = dict(d.get("f", {}))
        se = dict(d.get("s", {}))
        se["IdInstrumento"] = d["inst"]
        s += clonar_serie(sid, d["fondo"], fondo=fo, serie=se)
    s.append(upd("BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo", {"Descripcion": "Regimen General"}, '"IdRegimenFondoMutuo" = \'GEN\''))
    s += regimen("BF", "Beneficio Fiscal", "N", "N")
    s += regimen("18Q", "Articulo 18 Q", "S", "")
    s += regimen("18T", "Articulo 18 T", "N", "S")
    s += regimen("57BIS", "Articulo 57 bis", "N", "N")
    for idc, per, doc, razon, ids in CLIENTES:
        s.append(cliente(idc, per, doc, razon))
        s.append(socio(ids, per, idc))
        s.append(ins(SOCV, IdSocioDeNegocio=ids, IdPersona=per, IdCliente=idc, IdEmpresa=1))
    # portfolios (the F1 base has 500 / client 100 / PF500; its view row is added here with the code)
    s.append(upd(PORT, {"IdMatrizAgrupadorAPV": 0}, '"IdPortafolio" = 500'))
    for idp, cod, idc, mat in PORTAFOLIOS:
        if idp != 500:
            v = dict(IdPortafolio=idp, CodigoPortafolio=cod, IdCliente=idc, IdEstado="ACT", Proposito="PROPGEN")
            if mat is not None:
                v["IdMatrizAgrupadorAPV"] = mat
            s.append(ins(PORT, **v))
        s.append(ins(PORV, IdPortafolio=idp, IdMatrizAgrupadorAPV=mat, CodigoPortafolio=cod) if mat is not None else ins(PORV, IdPortafolio=idp, CodigoPortafolio=cod))
    s.append(ins(MAPV, IdMatrizAgrupadorAPV=7, IdProductoAgrupador=3, EsADC="S"))
    s.append(ins(PROD, IdProductoAgrupador=3, NombreProductoAgrupador="Producto APV tres"))
    s.append(ins(MNOAPV, IdMatrizAgrupadorNOAPV=8, CodigoPortafolio="PF500", EsADC="N"))
    s.append(ins(MNOAPV, IdMatrizAgrupadorNOAPV=9, CodigoPortafolio="PF502", EsADC="S"))
    # persons related to the company (person 1): 7101 active, 7102 inactive
    s.append(relacion(1, 7101, "ACT"))
    s.append(relacion(1, 7102, "INA"))
    return s


def base_ffmm():
    return "-- GENERATED by make_cases_ffmm.py (cases_ffmm_lib.base_ffmm). FAKE universe of the FM-CPFF cases, on top of seed/base-fmm.sql.\n" + "\n".join(base_universo()) + "\n"


# ---- case writer -------------------------------------------------------------------------------------------------------------------------------------
_n = {"n": 0}

TABLAS = [
    {"nombre": CUS, "orden": '"IdCartera"'},
    {"nombre": CONFIG, "orden": '"IdEmpresa", "CodigoParametro"'},
]
ESQUEMAS = ["schema-ffmm.sql", "seed/base-ffmm.sql"]


def entrada(**campos):
    """Request with the three header fields and the date 2026-03-10 (= PLA.FechaSistema): every other field is OMITTED unless given (the legacy pojo then keeps its defaults). `ABSENT` removes a field."""
    d = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_FechaConsulta": "2026-03-10"}
    d.update(campos)
    return {k: v for k, v in d.items() if v != "ABSENT"}


def empresa(persona=1, cliente_id=99):
    """ConsultarEmpresa double: the company's person and client (client 99 = the corredora: the 'CPRO' relation)."""
    return {"ConsultarEmpresa": {"defecto": {"IdEmpresa": 1, "Codigo": "BCS", "Nombre": "Corredora Ficticia", "IdEstado": "ACT", "IdPais": 56, "IdSocioDeNegocio": 10, "IdPersona": persona,
                                             "IdCliente": cliente_id}}}


def caso(num, slug, ref, ent, seed=(), colaboradores=None, **extra):
    """Writes cases-ffmm/FM-CPFF-Cnn_slug and returns the id. `seed` is the case seed (loaded after seed/base-ffmm.sql)."""
    datos = []
    for x in list(seed):
        datos += x if isinstance(x, (list, tuple)) else [x]
    _n["n"] += 1
    cid = "FM-CPFF-C%s_%s" % (num if isinstance(num, str) else "%02d" % num, slug)
    doc = {"servicio": SERVICIO, "ref": ref, "esquemasExtra": ESQUEMAS, "entrada": ent, "tablas": TABLAS}
    doc.update(extra)
    col = dict(colaboradores or {})
    if "ConsultarEmpresa" not in col:
        col.update(empresa())
    doc["colaboradores"] = col
    d = CASES / cid
    if d.exists():
        raise SystemExit("duplicate case id " + cid)
    d.mkdir(parents=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    texto = "-- case seed on top of seed/base-fmm.sql + schema-ffmm.sql + seed/base-ffmm.sql (fake data)\n" + ("\n".join(datos) if datos else "SELECT 1;") + "\n"
    (d / "seed.sql").write_text(texto, encoding="utf-8", newline="\n")
    return cid


def config(codigo, valor):
    """Replaces a PLATAFORMA parameter (valor None deletes it)."""
    s = ['DELETE FROM "%s" WHERE "IdEmpresa" = 1 AND "CodigoParametro" = %s;' % (CONFIG, lit(codigo))]
    if valor is not None:
        s.append('INSERT INTO "%s" ("IdEmpresa", "CodigoParametro", "Valor") VALUES (1, %s, %s);' % (CONFIG, lit(codigo), lit(valor)))
    return s


def solicitud(i, **ov):
    """A FMM.SolicitudAporteRescate row in transit (ING) accepted 2026-03-05, APO, FM001A, client 100, portfolio 500, regime GEN, ValorCuota 1000 and Monto 50000."""
    d = dict(FechaAceptacion=fecha("2026-03-05"), ValorCuota=1000, Monto=50000)
    d.update(ov)
    return sol(i, **d)
