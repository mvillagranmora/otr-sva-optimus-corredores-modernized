"""Helpers of the F3 W1 front 3 cases (ConsultarSolicitudesFFMM and its twin ConsultarSolicitudesFFMMWS, read only): case directories cases-sfm/FMM-SFM-nn_slug.

The case INPUTS (case.json, seed.sql), schema-sfm.sql and seed/base-sfm.sql are written here; the goldens (expected.json) are RECORDED by legacy-oracle/record_sfm.sh
from the unmodified legacy, never by this script. The cases live in their OWN directory (cases-sfm/, passed to the shared OracleRunner with -Doracle.cases) so
that record.sh / record_fsa.sh / record_rre.sh (which scan cases/) never run them and the shared runner stays untouched.

FAKE universe (on top of seed/base-fmm.sql, loaded through "esquemasExtra" = schema-sfm.sql + seed/base-sfm.sql):
  * the view FMM.ConsultarSolicitudesFFMM does not exist in the catalog CSVs (Q-F3A-2): it is a STAND-IN TABLE of the same name, one column per field the SQL selects
    or filters, with ASSUMED types taken from the pojo fields the legacy maps them to (text -> VARCHAR, Long -> BIGINT, BigDecimal -> DECIMAL(26,8),
    FechaIngresoLocal -> TIMESTAMP). FechaSolicitud / HoraSolicitud are TEXT ('yyyy-MM-dd' / 'HH:mm:ss', UTC): the legacy concatenates and parses them as text,
    which only works if the HANA view returns text (a DATE column would raise IllegalArgumentException in SQL.mapLenientHanaField).
  * TES.IngresoEgreso / TES.ComprobanteDetalleOrigen / TES.ComprobanteDetalle: stand-in tables with ONLY the columns the WS SQL reads (types as the Tesoreria stand-in).
  * base-sfm.sql: the two parameters the classes require (PLATAFORMA/PLA.FiltroAdministracionCarteraWS, FONDOMUTUO/WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS,
    both 'N') and the movement types INGGTA, APOREINV, RESREINV on top of the four of base-fmm.sql. PLA.SegundosZonaHoraria is -10800 (base-fmm.sql).
Default row: client 100, APO, APONORM, INGRESADO, solicitud n, FechaSolicitud 2026-03-02, HoraSolicitud 13:00:00 (UTC => local 10:00:00 with -10800), FolioContrato 0.
"""
import datetime
import json
import pathlib

BASE = pathlib.Path(__file__).resolve().parent
CASES = BASE / "cases-sfm"
VISTA_NOMBRE = "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesFFMM"
VISTA = '"%s"' % VISTA_NOMBRE
TES_IE = '"BSCL.Tesoreria.BD::TES.IngresoEgreso"'
TES_CDO = '"BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen"'
TES_CD = '"BSCL.Tesoreria.BD::TES.ComprobanteDetalle"'
CONFIG = '"BSCL.Plataforma.BD::PLA.Configuracion"'
PARAM = '"BSCL.FondoMutuo.BD::FMM.Parametro"'
TIPOMOV = '"BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo"'

S, L, D, T = "VARCHAR(500)", "BIGINT", "DECIMAL(26,8)", "TIMESTAMP"
SFM, WS = "ConsultarSolicitudesFFMM", "ConsultarSolicitudesFFMMWS"

# (column, H2 type, default). Order = the SELECT of the SQL, then the columns only the filter / the WS SQL read.
COLUMNAS = [
    ("IdCliente", L, 100), ("TipoSolicitud", S, "APO"), ("FechaConsulta", S, "2026-03-02"), ("Origen", S, "WEB"), ("NumSolicitud", L, 1),
    ("DetSolicitud", S, "detalle"), ("EstadoDetalle", S, "INGRESADO"), ("Rellamada", L, 0), ("OrigenSolicitud", S, "ORI"), ("FechaSolicitud", S, "2026-03-02"),
    ("HoraSolicitud", S, "13:00:00"), ("FechaCierre", S, ""), ("TipoTraspaso", S, ""), ("MotivoRechazo", S, ""), ("Folio", S, "F1"), ("FolioRelacionado", L, 0),
    ("RutCliente", S, "11111111-1"), ("CodFondoOrigen", L, 41), ("NombreFondoOrigen", S, "Fondo Uno"), ("SerieFondoOrigen", S, "FM001A"),
    ("RegimenFondoOrigen", S, "GEN"), ("MontoPesosOrigen", D, "1000"), ("MontoCuotasOrigen", D, "10"), ("ValorCuotaOrigen", D, "100"), ("CodFondoDestino", L, 0),
    ("NombreFondoDestino", S, ""), ("SerieFondoDestino", S, ""), ("RegimenFondoDestino", S, ""), ("MontoPesosDestino", D, "0"), ("MontoCuotasDestino", D, "0"),
    ("ValorCuotaDestino", D, "0"), ("MontoCuotasComision", D, "0"), ("PorcentajeOrigen", D, "100"), ("PorcentajeDestino", D, "0"),
    ("NombreCliente", S, "Cliente Ficticio Uno"), ("ClasificacionMLT", S, "N"), ("DescripcionTipoAhorro", S, "Ahorro"), ("DescripcionTipoCuenta", S, "Cuenta"),
    ("FechaValorCuota", S, "2026-03-03"), ("MontoCuotas", D, "10"), ("MontoPesos", D, "1000"), ("NombreAdministradora", S, "AGF1"), ("OrigenValorCuota", D, "100"),
    ("RegimenTributario", S, "GEN"), ("SerieFondo", S, "FM001A"), ("TipoAhorro", S, "A1"), ("TipoCuenta", S, "C1"), ("TipoOperacion", S, "NORMAL"),
    ("ValorCuota", D, "100"), ("TipoMovimiento", S, None), ("IdMatrizAgrupador", L, 0), ("EsADC", S, "N"), ("FechaCertificacion", S, "2026-03-04"),
    ("FechaLiquidacion", S, "2026-03-05"), ("FechaCurse", S, "2026-03-02"), ("FechaValorCuotaOrigen", S, "2026-03-03"), ("FechaValorCuotaDestino", S, ""),
    ("IdSerieFondo", S, "FM001A"), ("IdSerieFondoDestino", S, ""), ("IngresoAutomatico", S, "N"), ("FolioContrato", L, 0), ("Canal", S, "WEB"),
    ("DescripcionCanal", S, "Canal web"), ("IdMedioDePago", S, "CIN"), ("DescripcionMedioPago", S, "Cuenta inversion"), ("IdContrato", L, 0),
    ("FechaFinalReinversion", S, ""),
    ("IdIngresoEgreso", L, 0), ("FechaIngresoLocal", T, None),
]
NOMBRES = [c[0] for c in COLUMNAS]
OFFSET_DEFECTO = -10800   # PLA.SegundosZonaHoraria of seed/base-fmm.sql


def q(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "'S'" if v else "'N'"
    if isinstance(v, int):
        return str(v)
    return "'" + str(v).replace("'", "''") + "'"


def schema_sfm():
    """schema-sfm.sql: the stand-in of the view and the Tesoreria tables the WS SQL reads (H2 ONLY, never executed against HANA)."""
    s = ["-- GENERATED by make_cases_sfm.py (cases_sfm_lib.schema_sfm). H2 STAND-INS, ASSUMED types (Q-F3A-2: the view is not in the catalog CSVs).",
         "-- The view FMM.ConsultarSolicitudesFFMM is a TABLE here (one column per field the SQL selects/filters; text columns where the pojo field is a String).",
         'CREATE TABLE %s (' % VISTA]
    s.append(",\n".join('  "%s" %s' % (n, t) for n, t, _ in COLUMNAS))
    s.append(");")
    s.append('CREATE TABLE %s ("IdIngresoEgreso" BIGINT, "IdTipoOperacion" VARCHAR(500));' % TES_IE)
    s.append('CREATE TABLE %s ("IdComprobanteDetalle" BIGINT, "IdIngresoEgreso" BIGINT);' % TES_CDO)
    s.append('CREATE TABLE %s ("IdComprobanteDetalle" BIGINT, "IdComprobante" BIGINT, "IdEstado" VARCHAR(500));' % TES_CD)
    return "\n".join(s) + "\n"


def base_sfm():
    """seed/base-sfm.sql: parameters and movement types every FMM-SFM case needs on top of seed/base-fmm.sql (loaded through esquemasExtra)."""
    s = ["-- GENERATED by make_cases_sfm.py (cases_sfm_lib.base_sfm). FAKE rows on top of seed/base-fmm.sql."]
    s.append('INSERT INTO %s ("IdEmpresa", "CodigoParametro", "Valor") VALUES (1, \'PLA.FiltroAdministracionCarteraWS\', \'N\');' % CONFIG)
    s.append('INSERT INTO %s ("IdEmpresa", "NombreTipoParametro", "ValorParametro") VALUES (1, \'WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS\', \'N\');' % PARAM)
    for idt, ind, op in (("INGGTA", "APO", "APORFM"), ("APOREINV", "APO", "APORFM"), ("RESREINV", "RES", "RESCFM")):
        s.append('INSERT INTO %s ("IdTipoMovimientoFondoMutuo", "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion", "TieneCarga", "TieneIngresoManual", '
                 '"TieneCertificacion", "IdEstado", "GeneraOmnibus") VALUES (\'%s\', \'%s\', \'%s\', \'%s\', \'\', \'\', \'\', \'ACT\', \'\');' % (TIPOMOV, idt, idt, ind, op))
    return "\n".join(s) + "\n"


def _movimiento(tipo):
    return {"APO": "APONORM", "RES": "RESNORM", "REV": "APOREINV"}.get(tipo, "APONORM")


def _local(fecha, hora, offset):
    try:
        d = datetime.datetime.strptime("%s %s" % (fecha, hora), "%Y-%m-%d %H:%M:%S") + datetime.timedelta(seconds=offset)
        return d.strftime("%Y-%m-%d %H:%M:%S")
    except (ValueError, TypeError):
        return "2026-03-02 10:00:00"   # the text of the SFM columns is not a valid date/time (a case on purpose): the WS reads FechaIngresoLocal, so give it a valid one


def fila(ns, offset=OFFSET_DEFECTO, **ov):
    """INSERT of one row of the view stand-in. `ns` = NumSolicitud. TipoMovimiento and FechaIngresoLocal default from the type / the date and time (UTC + offset)."""
    for k in ov:
        if k not in NOMBRES:
            raise KeyError("view column unknown: " + k)
    v = {n: d for n, _, d in COLUMNAS}
    v["NumSolicitud"] = ns
    tipo = ov.get("TipoSolicitud", v["TipoSolicitud"])
    v["TipoMovimiento"] = _movimiento(tipo)
    v.update(ov)
    if "FechaIngresoLocal" not in ov:
        v["FechaIngresoLocal"] = _local(v["FechaSolicitud"], v["HoraSolicitud"], offset)
    return "INSERT INTO %s (%s) VALUES (%s);" % (VISTA, ", ".join('"%s"' % n for n in NOMBRES), ", ".join(q(v[n]) for n in NOMBRES))


def tes(idie=None, tipo="RESCFM", detalles=()):
    """Tesoreria rows for the WS join: an IngresoEgreso and its detalles [(IdComprobanteDetalle, IdComprobante, IdEstadoDetalle)] linked by ComprobanteDetalleOrigen."""
    s = []
    if idie is not None:
        s.append("INSERT INTO %s VALUES (%s, %s);" % (TES_IE, q(idie), q(tipo)))
    for cd, comp, est in detalles:
        s.append("INSERT INTO %s VALUES (%s, %s);" % (TES_CDO, q(cd), q(idie)))
        s.append("INSERT INTO %s VALUES (%s, %s, %s);" % (TES_CD, q(cd), q(comp), q(est)))
    return s


def config(codigo, valor):
    """Replaces a PLATAFORMA parameter (valor None deletes it: the legacy then raises)."""
    s = ['DELETE FROM %s WHERE "IdEmpresa" = 1 AND "CodigoParametro" = %s;' % (CONFIG, q(codigo))]
    if valor is not None:
        s.append('INSERT INTO %s ("IdEmpresa", "CodigoParametro", "Valor") VALUES (1, %s, %s);' % (CONFIG, q(codigo), q(valor)))
    return s


def parametro(nombre, valor):
    """Replaces a FONDOMUTUO parameter (FMM.Parametro); valor None deletes it."""
    s = ['DELETE FROM %s WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = %s;' % (PARAM, q(nombre))]
    if valor is not None:
        s.append('INSERT INTO %s ("IdEmpresa", "NombreTipoParametro", "ValorParametro") VALUES (1, %s, %s);' % (PARAM, q(nombre), q(valor)))
    return s


def entrada(**campos):
    """Request. A key given as None is written as an explicit JSON null (Gson then sets the field to null: NullPointerException in the legacy)."""
    d = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM"}
    d.update(campos)
    return d


TABLAS = [
    {"nombre": VISTA_NOMBRE, "orden": '"NumSolicitud", "TipoSolicitud", "EstadoDetalle", "FolioContrato", "FolioRelacionado", "Folio", "IdCliente", "IdIngresoEgreso"'},
    {"nombre": "BSCL.Plataforma.BD::PLA.Configuracion", "orden": '"IdEmpresa", "CodigoParametro"'},
    {"nombre": "BSCL.FondoMutuo.BD::FMM.Parametro", "orden": '"IdEmpresa", "NombreTipoParametro"'},
]

_n = {"n": 0}


def _escribir(slug, ref, servicio, ent, seed, extra):
    _n["n"] += 1
    cid = "FMM-SFM-%03d_%s" % (_n["n"], slug)
    doc = {"servicio": servicio, "ref": ref, "esquemasExtra": ["schema-sfm.sql", "seed/base-sfm.sql"], "entrada": ent, "tablas": TABLAS}
    doc.update(extra)
    d = CASES / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    texto = "-- case seed on top of seed/base-fmm.sql + schema-sfm.sql + seed/base-sfm.sql (fake data)\n" + ("\n".join(seed) if seed else "SELECT 1;") + "\n"
    (d / "seed.sql").write_text(texto, encoding="utf-8", newline="\n")
    return cid


def par(slug, ref, ent, filas=(), seed=(), solo=None, **extra):
    """One scenario = TWO cases with the same input and the same data: ConsultarSolicitudesFFMM (slug) and its twin ConsultarSolicitudesFFMMWS ("ws-" + slug).
    `filas` = INSERTs of the view (see fila); `seed` = any other statements (parameters, Tesoreria rows). solo=SFM / WS writes only that one."""
    datos = list(seed) + list(filas)
    ids = []
    if solo in (None, SFM):
        ids.append(_escribir(slug, "[SFM] " + ref, SFM, ent, datos, extra))
    if solo in (None, WS):
        ids.append(_escribir(slug if slug.startswith("ws-") else "ws-" + slug, "[WS] " + ref, WS, ent, datos, extra))
    return ids
