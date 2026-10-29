"""Helpers of the F3 W1 front 8 cases (ConsultarCustodiaPortafolioFondoMutuo, CPF; read only): case directories cases-cpf/FMM-CPF-nnn_slug.

The case INPUTS (case.json, seed.sql), schema-cpf.sql and seed/base-cpf.sql are written here; the goldens (expected.json) are RECORDED by legacy-oracle/record_cpf.sh from the
unmodified legacy, never by this script. The cases live in their OWN directory (cases-cpf/, passed to the shared OracleRunner with -Doracle.cases): record.sh / record_fsa.sh /
record_rre.sh / record_sfm.sh / record_det.sh never run them and this script's cases never touch another front's goldens.

What CPF really executes (RULES_FONDOMUTUO_F3_A.md 8.2): four SQL of its own (SelectSolicitudes, SelectSolicitudesTotales, SelectSeries, SelectRegimen) over ~20 tables and
views of 8 modules, FOUR valuations with CalcularMontoNumeroCuota (REAL here, as in the DET oracle), one CalcularParidad (IdTrabajo 2), ObtenerDiaSiguiente (REAL, reads the
calendar) and ConsultarEmpresa. The oracle runs CPF + CalcularMontoNumeroCuota + ObtenerDiaSiguiente REAL; the REGISTERED DOUBLES (scripted per case, "colaboradores") are the
collaborators outside the module: Plataforma CalcularParidad, EntregarPreciosTasas, ConsultarEmpresa and the custody calculation ConsultarCustodiaPortafolioFFMM (another piece:
the custody library; its output is scripted with custodia() below).

schema-cpf.sql: H2 STAND-INS of what the four SQL join and the F1 schema lacks. Tables the F1 schema (schema-fmm.sql) already has are reused; the missing ones are created with
ONLY the columns the SQL reads; each column type is REAL when the catalog CSVs of hana_qa_results have it and ASSUMED (taken from the pojo field the legacy maps it to) otherwise;
the generated file tags every column. Two HANA functions are aliased (MAP, TO_DECIMAL: H2FnCpf).
"""
import csv
import json
import pathlib
import sys

BASE = pathlib.Path(__file__).resolve().parent
HANA = BASE.parent.parent / "hana_qa_results"
CASES = BASE / "cases-cpf"
SERVICIO = "ConsultarCustodiaPortafolioFondoMutuo"
sys.path.insert(0, str(BASE))
from cases3a_lib import clonar_serie, moneda_formato, fin_de_semana, feriado, config_plataforma, parametro  # noqa: E402,F401
from cases3b_rows import fila as _fila, fecha, RawSql  # noqa: E402,F401

SOLICITUD = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
PORTAFOLIO = "BSCL.Portafolio.BD::POR.Portafolio"
CONFIG = '"BSCL.Plataforma.BD::PLA.Configuracion"'
PARAM = '"BSCL.FondoMutuo.BD::FMM.Parametro"'

N = lambda n: "NVARCHAR(%d)" % n  # noqa: E731
LONG, DEC, DATE, TS = "BIGINT", "DECIMAL(26,8)", "DATE", "TIMESTAMP"

# NEW stand-ins: (table, [(column, fallback type)]). Views are H2 tables here.
NUEVAS = [
    ("BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente", [("IdCliente", LONG), ("DocumentoIdentidad", N(20)), ("RazonSocial", N(200))]),
    ("BSCL.SocioNegocio.BD::SOC.SocioDeNegocio", [("IdSocioDeNegocio", LONG), ("IdCliente", LONG)]),
    ("BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV", [("IdMatrizAgrupadorAPV", LONG), ("IdProductoAgrupador", LONG), ("EsADC", N(1))]),
    ("BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV", [("IdMatrizAgrupadorNOAPV", LONG), ("CodigoPortafolio", N(100)), ("EsADC", N(1))]),
    ("BSCL.APV.BD::APV.ProductoAgrupador", [("IdProductoAgrupador", LONG), ("NombreProductoAgrupador", N(100))]),
    ("BSCL.Mesa.BD::SPT.OperacionSpot", [("IdOperacionSpotOrigen", LONG), ("Estado", N(3)), ("MontoMonedaSecundaria", DEC)]),
    ("BSCL.Plataforma.BD::INS.Instrumento", [("IdInstrumento", LONG), ("IdTipoInstrumento", N(6))]),
    ("BSCL.Plataforma.BD::INS.TipoInstrumento", [("IdTipoInstrumento", N(6)), ("Nombre", N(100)), ("IdClasificacionTipoInstrumento", N(6))]),
    ("BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento", [("IdClasificacionTipoInstrumento", N(6)), ("Nombre", N(100))]),
]
# columns the SQL reads from tables the F1 schema already has but without them
AGREGADAS = [
    (PORTAFOLIO, "IdMatrizAgrupadorAPV", LONG),
    ("BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo", "Descripcion", N(100)),
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


def schema_cpf():
    """schema-cpf.sql: aliases + stand-in tables (H2 ONLY, never executed against HANA)."""
    s = ["-- GENERATED by make_cases_cpf.py (cases_cpf_lib.schema_cpf). H2 STAND-INS of what the four ConsultarCustodiaPortafolioFondoMutuo_*.sql join and the F1 schema lacks.",
         "-- Types: REAL when hana_qa_results/*columnas*.csv has the column, ASSUMED (pojo field type) otherwise. Views are tables here. No data: seed/base-cpf.sql and the cases fill them.",
         "-- HANA functions of those SQL that H2 lacks (class cl.bcs.oracle.H2FnCpf; TO_BIGINT comes from schema-session.sql):",
         'CREATE ALIAS MAP FOR "cl.bcs.oracle.H2FnCpf.map";',
         'CREATE ALIAS TO_DECIMAL FOR "cl.bcs.oracle.H2FnCpf.toDecimal";']
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
    if isinstance(v, float):
        return repr(v)
    return "'" + str(v).replace("'", "''") + "'"


def ins(tabla, **v):
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (tabla, ", ".join('"%s"' % k for k in v), ", ".join(q(x) for x in v.values()))


def upd(tabla, sets, donde):
    return 'UPDATE "%s" SET %s WHERE %s;' % (tabla, ", ".join('"%s" = %s' % (k, q(x)) for k, x in sets.items()), donde)


# ---- the FAKE universe -------------------------------------------------------------------------------------------------------------------------------
# instruments 900..905, one series each: FM001A (base, fund 10, CLP, issuer AGF1), FM002B (fund 11, CLP, AGF2, restricted rescue, 2 decimals), FM003U (fund 12, USD, EXT1, foreign),
# FM004C (fund 13, CLP, AGF1), FM005D (fund 14, CLP, AGF2), FM006E (fund 15, CLP, AGF1); only FM001A has a regime (GEN). Issuers AGF1 / AGF2 have a default identity document (and AGF2 also a
# non default one, which the SQL must ignore); EXT1 has no person (document NULL).
SERIES = {
    "FM001A": dict(inst=900, fondo="10"),
    "FM002B": dict(inst=901, fondo="11", f=dict(IdEmisor="AGF2", NombreFondo="Fondo Ficticio Dos"), s=dict(NombreSerieFondo="Serie B ficticia", CodigoSerie="B", DecimalesCuota=2,
                                                                                                      DecimalesValorCuota=2, IdFamiliaFondo="FAM2", RestringeRescate="S")),
    "FM003U": dict(inst=902, fondo="12", f=dict(IdEmisor="EXT1", NombreFondo="Fondo Ficticio Tres USD", IdMoneda="USD", FondoExtranjero="S"),
                   s=dict(NombreSerieFondo="Serie U ficticia", CodigoSerie="U", DecimalesCuota=2, DecimalesValorCuota=4, IdFamiliaFondo="FAM3")),
    "FM004C": dict(inst=903, fondo="13", f=dict(NombreFondo="Fondo Ficticio Cuatro"), s=dict(NombreSerieFondo="Serie C ficticia", CodigoSerie="C", IdFamiliaFondo="FAM1")),
    "FM005D": dict(inst=904, fondo="14", f=dict(IdEmisor="AGF2", NombreFondo="Fondo Ficticio Cinco"), s=dict(NombreSerieFondo="Serie D ficticia", CodigoSerie="D", IdFamiliaFondo="FAM2")),
    "FM006E": dict(inst=905, fondo="15", f=dict(NombreFondo="Fondo Ficticio Seis"), s=dict(NombreSerieFondo="Serie E ficticia", CodigoSerie="E", IdFamiliaFondo="FAM1")),
}
EMISOR = '"BSCL.Plataforma.BD::MER.Emisor"'
REGIMEN = '"BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"'
REGSERIE = '"BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo"'


def regimen(id_reg, descripcion, rescate_mandato="N", acepta_mandato=""):
    v = {"IdRegimenFondoMutuo": id_reg, "GrupoRegimen": "DC", "PropositoSerieRelacionado": "", "PermiteOtrosGrupos": "", "ValidaPortafolioCliente": "", "ValidaPortafolioAGF": "",
         "PermiteRescate": "S", "TipoCarteraNormativa": "", "PermiteCambioGrupo": "", "RescateConMandato": rescate_mandato, "AceptaMandatoEnReinversion": acepta_mandato,
         "PermiteAporte": "S", "PermiteTraspaso": "N", "ValidaMontoMinimoInstrumento": "S", "IdEstado": "ACT"}
    return ['INSERT INTO %s (%s) VALUES (%s);' % (REGIMEN, ", ".join('"%s"' % k for k in v), ", ".join(q(x) for x in v.values())),
            upd("BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo", {"Descripcion": descripcion}, '"IdRegimenFondoMutuo" = %s' % q(id_reg))]


def reg_serie(serie, reg, estado="ACT", empresa=1):
    return ins("BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo", IdSerieFondo=serie, IdRegimenFondoMutuo=reg, IdEmpresa=empresa, IdEstado=estado)


def base_cpf():
    """seed/base-cpf.sql: the FAKE rows every FMM-CPF case needs on top of seed/base-fmm.sql (loaded through esquemasExtra after schema-cpf.sql)."""
    s = ["-- GENERATED by make_cases_cpf.py (cases_cpf_lib.base_cpf). FAKE universe of the FMM-CPF cases, on top of seed/base-fmm.sql."]
    # parameters CPF reads and the F1 seed lacks (all of them are mandatory: a missing one makes the legacy raise)
    s.append(ins("BSCL.Plataforma.BD::PLA.Configuracion", IdEmpresa=1, CodigoParametro="PLA.CalculaValorCuotaDiaSiguiente", Valor="N"))
    s.append(ins("BSCL.FondoMutuo.BD::FMM.Parametro", IdEmpresa=1, NombreTipoParametro="WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS", ValorParametro="N"))
    # instruments, types, classification
    s.append(ins("BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento", IdClasificacionTipoInstrumento="FM", Nombre="Fondos Mutuos"))
    s.append(ins("BSCL.Plataforma.BD::INS.TipoInstrumento", IdTipoInstrumento="FM", Nombre="Fondo Mutuo", IdClasificacionTipoInstrumento="FM"))
    s.append(ins("BSCL.Plataforma.BD::INS.TipoInstrumento", IdTipoInstrumento="FMX", Nombre="Fondo Mutuo Extranjero", IdClasificacionTipoInstrumento="FM"))
    for i in (900, 901, 903, 904, 905):
        s.append(ins("BSCL.Plataforma.BD::INS.Instrumento", IdInstrumento=i, IdTipoInstrumento="FM"))
    s.append(ins("BSCL.Plataforma.BD::INS.Instrumento", IdInstrumento=902, IdTipoInstrumento="FMX"))
    # issuers: AGF1 -> person 1001 -> document 11 (default); AGF2 -> person 1002 -> documents 12 (default) and 13 (NOT default); EXT1 -> no row (document NULL)
    for emi, per, doc, rut in (("AGF1", 1001, 11, "76000001-1"), ("AGF2", 1002, 12, "76000002-2")):
        s.append(ins("BSCL.Plataforma.BD::MER.Emisor", IdEmisor=emi, IdPersona=per))
        s.append(ins("BSCL.SocioNegocio.BD::PER.DocumentoIdentidad", IdDocumentoIdentidad=doc, DocumentoIdentidad=rut, IdTipoDocumentoIdentidad="RUT", Predeterminado="S"))
        s.append(ins("BSCL.SocioNegocio.BD::PER.Persona", IdPersona=per, IdDocumentoIdentidad=doc))
    s.append(ins("BSCL.SocioNegocio.BD::PER.DocumentoIdentidad", IdDocumentoIdentidad=13, DocumentoIdentidad="76999999-9", IdTipoDocumentoIdentidad="RUT", Predeterminado="N"))
    s.append(ins("BSCL.SocioNegocio.BD::PER.Persona", IdPersona=1003, IdDocumentoIdentidad=13))
    s.append(ins("BSCL.Plataforma.BD::MER.Emisor", IdEmisor="AGF3", IdPersona=1003))      # AGF3: its only document is NOT the default one => NULL
    # base series FM001A: family and names (the F1 seed leaves them empty)
    s.append(upd("BSCL.FondoMutuo.BD::FMM.SerieDeFondo", {"IdFamiliaFondo": "FAM1", "NombreSerieFondo": "Serie A ficticia", "CodigoSerie": "A"}, '"IdSerieFondo" = \'FM001A\''))
    s.append(upd("BSCL.FondoMutuo.BD::FMM.Fondo", {"NombreFondo": "Fondo Ficticio Uno"}, '"IdFondo" = \'10\''))
    for sid, d in SERIES.items():
        if sid == "FM001A":
            continue
        fo = dict(d.get("f", {}))
        se = dict(d.get("s", {}))
        se["IdInstrumento"] = d["inst"]
        s += clonar_serie(sid, d["fondo"], fondo=fo, serie=se, tipo_cambio=(900, 890) if sid == "FM003U" else None)
    # regimes (GEN comes from seed/base-fmm.sql: RescateConMandato N, AceptaMandatoEnReinversion '')
    s.append(upd("BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo", {"Descripcion": "Regimen General"}, '"IdRegimenFondoMutuo" = \'GEN\''))
    s += regimen("BF", "Beneficio Fiscal", "N", "N")
    s += regimen("18Q", "Articulo 18 Q", "S", "")
    s += regimen("18T", "Articulo 18 T", "N", "S")
    s += regimen("57BIS", "Articulo 57 bis", "N", "N")
    # only FM001A has a regime in the base (GEN, from the F1 seed): FM002B..FM006E have NONE, so each case adds exactly the regimes it needs (SelectRegimen has no ORDER BY: the first one decides)
    # clients (view) and business partners
    for idc, doc, nom in ((100, "11111111-1", "Cliente Ficticio Uno"), (101, "22222222-2", "Cliente Ficticio Dos"), (102, "33333333-3", "Cliente Ficticio Tres")):
        s.append(ins("BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente", IdCliente=idc, DocumentoIdentidad=doc, RazonSocial=nom))
    s.append(ins("BSCL.SocioNegocio.BD::SOC.SocioDeNegocio", IdSocioDeNegocio=1100, IdCliente=100))
    s.append(ins("BSCL.SocioNegocio.BD::SOC.SocioDeNegocio", IdSocioDeNegocio=1101, IdCliente=101))
    # portfolios: 500 general client 100 (APV matrix 0, NOAPV matrix 8 by code PF500, ADC N); 501 APV client 100 (code APV-B, APV matrix 7: product 3, ADC S);
    # 502 client 101 (APV matrix 0, NOAPV matrix 9 by code PF502, ADC S); 503 client 101 (APV matrix NULL: no matrix at all)
    s.append(upd(PORTAFOLIO, {"IdMatrizAgrupadorAPV": 0}, '"IdPortafolio" = 500'))
    for idp, cod, idc, mat in ((501, "APV-B", 100, 7), (502, "PF502", 101, 0)):
        s.append(ins(PORTAFOLIO, IdPortafolio=idp, CodigoPortafolio=cod, IdCliente=idc, IdEstado="ACT", Proposito="PROPGEN", IdMatrizAgrupadorAPV=mat))
    s.append(ins(PORTAFOLIO, IdPortafolio=503, CodigoPortafolio="PF503", IdCliente=101, IdEstado="ACT", Proposito="PROPGEN"))
    s.append(ins("BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV", IdMatrizAgrupadorAPV=7, IdProductoAgrupador=3, EsADC="S"))
    s.append(ins("BSCL.APV.BD::APV.ProductoAgrupador", IdProductoAgrupador=3, NombreProductoAgrupador="Producto APV tres"))
    s.append(ins("BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV", IdMatrizAgrupadorNOAPV=8, CodigoPortafolio="PF500", EsADC="N"))
    s.append(ins("BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV", IdMatrizAgrupadorNOAPV=9, CodigoPortafolio="PF502", EsADC="S"))
    return "\n".join(s) + "\n"


# ---- case writer -------------------------------------------------------------------------------------------------------------------------------------
_n = {"n": 0}

TABLAS = [
    {"nombre": SOLICITUD, "orden": '"IdSolicitudAporteRescate"'},
    {"nombre": "BSCL.Plataforma.BD::PLA.Configuracion", "orden": '"IdEmpresa", "CodigoParametro"'},
]


def entrada(**campos):
    """Request with the three header fields; every other field is OMITTED unless given (the legacy pojo then keeps its defaults). A value `ABSENT` removes a header field."""
    d = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM"}
    d.update(campos)
    return {k: v for k, v in d.items() if v != "ABSENT"}


def caso(slug, ref, ent, filas=(), seed=(), colaboradores=None, **extra):
    """Writes cases-cpf/FMM-CPF-nnn_slug and returns the id. `seed` (parameters, series, side tables) is loaded BEFORE `filas` (the solicitud rows)."""
    datos = []
    for x in list(seed) + list(filas):
        datos += x if isinstance(x, (list, tuple)) else [x]
    _n["n"] += 1
    cid = "FMM-CPF-%03d_%s" % (_n["n"], slug)
    doc = {"servicio": SERVICIO, "ref": ref, "esquemasExtra": ["schema-cpf.sql", "seed/base-cpf.sql"], "entrada": ent, "tablas": TABLAS}
    doc.update(extra)
    # every case scripts the four collaborators outside the closure (a missing script FAILS loudly in the oracle): the defaults are "no custody", the standard price and a
    # system-currency value of 1 per quota; ConsultarEmpresa comes from seed/doubles-base.json (IdPais 56)
    col = dict(colaboradores or {})
    for k, v in (("ConsultarCustodiaPortafolioFFMM", sin_custodia()), ("EntregarPreciosTasas", precios()), ("CalcularParidad", paridad(final=1))):
        if k not in col:
            col.update(v)
    doc["colaboradores"] = col
    d = CASES / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    texto = "-- case seed on top of seed/base-fmm.sql + schema-cpf.sql + seed/base-cpf.sql (fake data)\n" + ("\n".join(datos) if datos else "SELECT 1;") + "\n"
    (d / "seed.sql").write_text(texto, encoding="utf-8", newline="\n")
    return cid


def config(codigo, valor):
    """Replaces a PLATAFORMA parameter (valor None deletes it)."""
    s = ['DELETE FROM %s WHERE "IdEmpresa" = 1 AND "CodigoParametro" = %s;' % (CONFIG, q(codigo))]
    if valor is not None:
        s.append('INSERT INTO %s ("IdEmpresa", "CodigoParametro", "Valor") VALUES (1, %s, %s);' % (CONFIG, q(codigo), q(valor)))
    return s


def fparam(nombre, valor):
    """Replaces a FONDOMUTUO parameter (valor None deletes it)."""
    s = ['DELETE FROM %s WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = %s;' % (PARAM, q(nombre))]
    if valor is not None:
        s.append('INSERT INTO %s ("IdEmpresa", "NombreTipoParametro", "ValorParametro") VALUES (1, %s, %s);' % (PARAM, q(nombre), q(valor)))
    return s


ING = RawSql("TIMESTAMP '2026-03-02 09:30:15'")


def sol(i, **ov):
    """A FMM.SolicitudAporteRescate row with EVERY column (type defaults) and the CPF defaults: APO, ING, by units ('C') 10, FM001A, client 100, portfolio 500, regime GEN."""
    v = dict(IdSolicitudAporteRescate=i, IdTipoCanalIngreso="OPT", IdCliente=100, IdPortafolio=500, TipoSolicitud="APO", IdSerieFondo="FM001A",
             IdTipoMovimientoFondoMutuo="APONORM", CalculoPorMontoCuota="C", FechaEmision=fecha("2026-03-02"), FechaAceptacion=fecha("2026-03-02"),
             FechaValorCuota=fecha("2026-03-03"), FechaCertificacion=fecha("2026-03-04"), FechaLiquidacion=fecha("2026-03-05"), Monto=0, NumeroCuotas=10,
             ValorCuota=0, IdMoneda="CLP", Estado="ING", EstadoLiquidacion="", IdMedioDePago="TRF", Glosa="glosa", IdEmpresa=1, IdUsuario=7, IdModulo="FMM",
             OrigenOperacion="M", SolicitudBatch="N", IdRegimenTributario="GEN", IdMonedaSistema="CLP", IdMonedaPago="CLP", IdEmpleadoEjecutivo=20,
             IndicadorMLT="N", Overnight="N", Switch="N", FechaIngresoRegistro=ING, IngresoAutomatico="N")
    v.update(ov)
    return _fila(SOLICITUD, **v)


def rescate(i, **ov):
    d = dict(TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM")
    d.update(ov)
    return sol(i, **d)


# ---- scripted collaborators --------------------------------------------------------------------------------------------------------------------------
PRECIO = 1500.1234
FUENTE = "2026-03-02 00:00:00.000"


def _ids(k=8):
    """Every IdInterno the five CalcularMontoNumeroCuota calls of a case may use: solicitudes +-(2..k+1), custody 1..k, guarantee -2..-2k, transit rescate -1..-k."""
    s = set()
    for j in range(1, k + 2):
        s.add(j)
        s.add(-j)
        s.add(-2 * j)
    s.discard(0)
    return sorted(s)


def precios(precio=PRECIO, fuente=FUENTE, k=8, por_id=None, instrumento=900):
    """EntregarPreciosTasas double: ONE canned answer with a price row for every IdRegistro the case may request (the legacy takes the ones it asked for). `por_id` {id: (precio, fuente)}
    overrides single ids; precio None => no rows at all (the valuation then answers ERROR_VALORCUOTA)."""
    if precio is None:
        return {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": []}}}
    filas = []
    for i in _ids(k):
        p, f = (por_id or {}).get(i, (precio, fuente))
        filas.append({"IdRegistro": i, "IdClasificacionTipoInstrumento": "FM", "Precio": p, "IdInstrumento": instrumento, "FechaFuente": f})
    return {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": filas}}}


def paridad(final=None, cuota=None, trabajo1=(), n=4):
    """CalcularParidad double. `final`: MontoDestino answered to CPF's own IdTrabajo 2 call for the IdInterno 1..n (the position of the saldo in the HashMap order); a dict {id: monto}
    answers single ids; a number answers all of them; None answers nothing. `cuota`: the CalcularMontoNumeroCuota answers (IdInterno*4+k) as a list of rows (see uf()); a row of
    `cuota` replaces a `final` row of the same IdInterno (the legacy List2Hash would raise on a repeated id). `trabajo1`: rows answered to IdTrabajo 1. ONE answer for every
    IdTrabajo 2 call (CalcularMontoNumeroCuota and CPF share it): rows are matched by IdInterno."""
    t2 = {}
    if isinstance(final, dict):
        for k, v in final.items():
            t2[k] = {"IdTrabajo": 2, "IdInterno": k, "MontoDestino": v}
    elif final is not None:
        for k in range(1, n + 1):
            t2[k] = {"IdTrabajo": 2, "IdInterno": k, "MontoDestino": final}
    for r in (cuota or []):
        t2[r["IdInterno"]] = r
    return {"CalcularParidad": {"respuestas": [{"cuando": {"0.p_IdTrabajo": 1}, "respuesta": {"p_out_CalculosSalida": list(trabajo1)}},
                                               {"cuando": {"0.p_IdTrabajo": 2}, "respuesta": {"p_out_CalculosSalida": list(t2.values())}}],
                                "defecto": {"p_out_CalculosSalida": []}}}


def uf(id_, monto_uf=3.85, valor_uf=39000, sistema=None):
    """The CalcularParidad(2) answers of CalcularMontoNumeroCuota for a valuation `id_` (positive or negative): (id*4+-2) amount in UF, (id*4+-3) UF value, optionally (id*4+-1) the
    system-currency amount (only read when the fund currency is not the system currency)."""
    s = 1 if id_ > 0 else -1
    r = [{"IdTrabajo": 2, "IdInterno": id_ * 4 + 2 * s, "MontoDestino": monto_uf}, {"IdTrabajo": 2, "IdInterno": id_ * 4 + 3 * s, "FactorOrigenDestino": valor_uf, "MontoDestino": valor_uf}]
    if sistema is not None:
        r.append({"IdTrabajo": 2, "IdInterno": id_ * 4 + 1 * s, "MontoDestino": sistema})
    return r


def conjunto(*dicts):
    r = {}
    for d in dicts:
        r.update(d)
    return r


def custodia(cliente=100, portafolio=500, instrumento=900, regimen="GEN", cantidad=100, garantia=0, boveda="COR", relacion="", ppp=1234.5678, ing_ppc=1111.1111,
             ing_monto=2222.2222, ing_moneda=3333.3333, producto="", adc="N", matriz=8, socio=1100, doc="11111111-1", razon="Cliente Ficticio Uno", codigo="PF500", rt="Regimen General",
             nemo="FM001A", **ov):
    """One SalidaCustodia of ConsultarCustodiaPortafolioFFMM (the fields CPF reads; every number given, `None` => JSON null)."""
    d = {"IdClasificacionTipoInstrumento": "FM", "NombreClasificacionTipoInstrumento": "Fondos Mutuos", "IdTipoInstrumento": "FM", "NombreTipoInstrumento": "Fondo Mutuo",
         "IdInstrumento": instrumento, "Nemotecnico": nemo, "IdEmisor": "AGF1", "IdSocioDeNegocio": socio, "IdCliente": cliente, "IdPortafolio": portafolio, "DocumentoIdentidad": doc,
         "RazonSocial": razon, "CodigoPortafolio": codigo, "IdBoveda": boveda, "Relacion": relacion, "IdRegimenFondoMutuo": regimen, "RegimenTributario": rt, "Producto": producto,
         "CantidadCustodia": cantidad, "CantidadGarantia": garantia, "CantidadPrecioPromedio": 0, "MontoPrecioPromedio": 0, "INGPrecioPromedioCompra": ing_ppc,
         "INGMontoPrecioPromedioCompra": ing_monto, "INGMontoPrecioPromedioMonedaInstrumento": ing_moneda, "TotalPosicion": 0, "IdMatrizAgrupador": matriz, "EsADC": adc,
         "PPPCustodia": ppp}
    d.update(ov)
    return d


def custodias(*filas):
    return {"ConsultarCustodiaPortafolioFFMM": {"defecto": {"p_out_tbl": list(filas)}}}


def sin_custodia():
    return custodias()
