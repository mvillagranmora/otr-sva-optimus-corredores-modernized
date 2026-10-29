"""Helpers of the F3 W3 T1 cases of CertificarSolicitudAporteRescate (CER; front F-A header/validation FM-CER-01..14 and front F-B amounts FM-CER-15..20 and 23): case
directories cases-cer/FMM-CER-nnn_slug.

The case INPUTS (case.json, seed.sql), schema-cer.sql, seed/base-cer.sql and seed/doubles-cer.json are written here; the goldens (expected.json) are RECORDED by
legacy-oracle/record_cer.sh from the UNMODIFIED legacy CertificarSolicitudAporteRescate, never by this script. The cases live in their OWN directory (cases-cer/, passed with
-Doracle.cases): no other record script can run them or touch their goldens.

TWO KINDS OF CASE (see the "costura" in API_ASSUMED.md of the module tests):
  * TERMINAL: the legacy ends in F-A (requirements error, empty list, duplicate ids, no solicitud found, none valid = early exit FM-CER-14) or throws an exception there. The WHOLE
    legacy outcome is the golden (salida, tables before / after the call / after the host decision, collaborator calls, sequences, session).
  * CONTINUES: at least one solicitud survives F-A. Every such case is recorded TWICE with the SAME seed and input:
      - `<slug>`          (modo completo): the benign collaborator doubles (seed/doubles-cer.json + per-case answers built here) let the legacy run to the end; the whole run is the
                          golden (full output, the UPDATE to CRT, every collaborator entry): later fronts (T2..T5) will compare complete runs against it, T1 compares the projection of
                          what F-A and F-B decided (EstadoProceso / Codigo of every solicitud, the amounts written by the UPDATE after FM-CER-23, the order of the aportes / rescates
                          seen by the collaborators, the effective date, the pairs sent to SaldoFlujoCaja);
      - `<slug>-parada`   (modo parada): the first collaborator that F-C / F-D reach (SaldoFlujoCaja, CalculaCustodiaRescate, ProcesarCustodiaFondoMutuo) is scripted to FAIL with
                          "PARADA_T1": the legacy stops right there; the golden holds the collaborator calls up to that point and the tables BEFORE any write. This is what T1 compares
                          to the new code stopped at the seam (the FasesPosteriores double).

What the oracle runs REAL: CertificarSolicitudAporteRescate, AgregarLogCertificacionAporteRescate, CalcularMontoNumeroCuota, CalcularComisiones, RevisarRequisitosBasicos,
ConfiguracionSistema, Diccionario, MensajeSalida, MonedaFormato, SQL / NamedParameterStatement, Fecha and the SQL files of the class. REGISTERED DOUBLES: everything listed in
record_cer.sh.

schema-cer.sql (H2 STAND-INS, never run against HANA): FMM.SolicitudAporteRescate (60 columns) and the view FMM.ConsultarSolicitudAporteRescate already exist in schema-fmm.sql with
types taken from the earlier catalogs; CATALOGO_HANA_W3.txt (HANA QA, read only, 2026-10-29) is compared with them column by column HERE (cases_cer_lib.verificar_catalogo) and the
columns it lists are marked REAL in the generated schema-cer.sql.
"""
import json
import pathlib
import re

BASE = pathlib.Path(__file__).resolve().parent
ANALISIS = BASE.parent.parent
CATALOGO = ANALISIS / "CATALOGO_HANA_W3.txt"
CASES = BASE / "cases-cer"
SERVICIO = "CertificarSolicitudAporteRescate"

T_SOL = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
V_SOL = "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate"
T_LOG = "BSCL.FondoMutuo.BD::FMM.LogCertificacionAporteRescate"
SEQ_LOG = "BSCL.FondoMutuo.BD.Secuencias::LogCertificacionAporteRescate"
CONFIG = '"BSCL.Plataforma.BD::PLA.Configuracion"'
PARAM = '"BSCL.FondoMutuo.BD::FMM.Parametro"'
ESTADO = "BSCL.Plataforma.BD::PLA.Estado"
COMPROB = "BSCL.Tesoreria.BD.Vistas::ConsultarPorComprobanteOrigenLiquidado"
INDIC_VISTA = "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarIndicadoresPortafolioFondoMutuo"
IND_INDICADOR = "BSCL.Plataforma.BD::IND.Indicador"
IND_TIPO = "BSCL.Plataforma.BD::IND.TipoIndicador"
TRA_APV = "BSCL.APV.BD::TRA.Traspaso"
TRA_ORDEN = "BSCL.APV.BD::TRA.OrdenTraspaso"
TRA_FM = "BSCL.FondoMutuo.BD::TRA.Traspaso"
TRA_DET = "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen"
ENT_REC = "BSCL.APV.BD::SCS.EntidadRecaudadora"
PERSONA = "BSCL.SocioNegocio.BD::PER.Persona"
DOCIDENT = "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
CUENTA = "BSCL.SocioNegocio.BD::PER.CuentaBancaria"
TIPOCTA = "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria"
SERIE = "BSCL.FondoMutuo.BD::FMM.SerieDeFondo"
FONDO = "BSCL.FondoMutuo.BD::FMM.Fondo"
TIPOMOV = "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo"
REGIMEN = "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
COMISION = "BSCL.FondoMutuo.BD::FMM.Comision"
VALORTC = "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio"
MONEDAFMT = "BSCL.Plataforma.BD::PLA.MonedaFormato"
DICC = "BSCL.Plataforma.BD::PLA.Diccionario"

ABSENT = object()     # key omitted from the JSON
NULL = None           # explicit JSON null


class Num:
    """A JSON number written EXACTLY as given (trailing zeros, exponent): Gson reads it with its text."""

    def __init__(self, texto):
        self.texto = texto


def dumps(doc):
    def default(o):
        if isinstance(o, Num):
            return "@@NUM:%s@@" % o.texto
        raise TypeError(repr(o))
    s = json.dumps(doc, indent=2, ensure_ascii=False, default=default)
    return re.sub(r'"@@NUM:([^"]*)@@"', r"\1", s) + "\n"


# ----------------------------------------------------------------------------------------------------------------------------------------------------
# catalog (CATALOGO_HANA_W3.txt) against the H2 stand-ins
# ----------------------------------------------------------------------------------------------------------------------------------------------------
def leer_catalogo():
    sec, tabla, vista, claves, secuencias = None, {}, {}, [], []
    for linea in CATALOGO.read_text(encoding="utf-8").splitlines():
        if linea.startswith("== Q-W0B-14 columnas de la tabla"):
            sec = "t"
        elif linea.startswith("== Q-W0B-14 columnas de la vista"):
            sec = "v"
        elif linea.startswith("== Q-W0B-14 claves"):
            sec = "k"
        elif linea.startswith("== Q-W0B-14 secuencias"):
            sec = "s"
        elif linea.startswith("=="):
            sec = None
        elif sec in ("t", "v") and "|" in linea and not linea.startswith("COLUMN_NAME"):
            c, t, ln, sc, nul = [x.strip() for x in linea.split("|")]
            (tabla if sec == "t" else vista)[c] = (t, ln, sc, nul == "TRUE")
        elif sec == "k" and "|" in linea and not linea.startswith("CONSTRAINT_NAME"):
            claves.append([x.strip() for x in linea.split("|")])
        elif sec == "s" and "|" in linea and not linea.startswith("SEQUENCE_NAME"):
            secuencias.append([x.strip() for x in linea.split("|")])
    return tabla, vista, claves, secuencias


def _tipo_catalogo(t, ln, sc):
    if t == "NVARCHAR":
        return "NVARCHAR(%s)" % ln
    if t == "DECIMAL":
        return "DECIMAL(%s,%s)" % (ln, sc)
    return t


def _dominios():
    dom = {}
    for m in re.finditer(r'CREATE DOMAIN "([^"]+)" AS ([^;]+);', (BASE / "schema-types.sql").read_text(encoding="utf-8")):
        dom[m.group(1)] = m.group(2).strip().replace(" ", "")
    return dom


def verificar_catalogo():
    """Compares the 60 + 60 columns of CATALOGO_HANA_W3.txt with the H2 domains of schema-types.sql. Returns (lines of the report, list of columns that need a type change)."""
    tabla, vista, claves, secuencias = leer_catalogo()
    dom = _dominios()
    informe, cambios = [], []
    for nombre, cols, pref, tabla_h2 in (("FMM.SolicitudAporteRescate", tabla, T_SOL + ".", T_SOL), ("FMM.ConsultarSolicitudAporteRescate", vista, V_SOL + ".", V_SOL)):
        iguales, distintos, faltan, nulos = [], [], [], []
        nn_h2 = {n: nn for n, tp, nn in _columnas(tabla_h2)}
        for c, (t, ln, sc, nul) in cols.items():
            d = dom.get(pref + c)
            if d is None:
                faltan.append(c)
            elif d == _tipo_catalogo(t, ln, sc).replace(" ", ""):
                iguales.append(c)
            else:
                distintos.append((c, d, _tipo_catalogo(t, ln, sc)))
            if c in nn_h2 and nn_h2[c] == nul:   # the catalog says nullable (TRUE) and the stand-in says NOT NULL, or the other way round
                nulos.append(c)
        informe.append("%s: %d columns in the catalog, %d with the SAME type in the H2 stand-in, %d with another type, %d missing in H2, %d with another nullability" % (nombre, len(cols), len(iguales), len(distintos), len(faltan), len(nulos)))
        informe.append("   REAL columns (type verified): " + ", ".join(iguales))
        if nulos:
            informe.append("   nullability differs: " + ", ".join(nulos))
        for c, d, r in distintos:
            informe.append("   type differs: %s H2=%s HANA=%s%s" % (c, d, r, "  (HANA DECIMAL(34) without scale = floating decimal: H2 DECFLOAT(34))" if "DECIMAL(34" in r else ""))
            cambios.append((nombre, c, d, r))
        if faltan:
            informe.append("   in the catalog but not in the H2 stand-in: " + ", ".join(faltan))
    return informe, cambios


def schema_cer():
    informe, cambios = verificar_catalogo()
    tabla, vista, claves, secuencias = leer_catalogo()
    s = ["-- GENERATED by make_cases_cer.py (cases_cer_lib.schema_cer). H2 STAND-INS of what CertificarSolicitudAporteRescate reads / writes in F-A and F-B and the F1 schema lacks.",
         "-- H2 ONLY, never HANA. REAL = verified against CATALOGO_HANA_W3.txt (HANA QA, read-only SELECT, 2026-10-29); ASSUMED = in no catalog.",
         "-- Conformance of the F1 stand-ins with the catalog (computed when this file was generated):"]
    s += ["--   " + l for l in informe]
    s.append("-- PRIMARY KEY of FMM.SolicitudAporteRescate in HANA QA: " + "; ".join("%s (%s)" % (k[1], k[0]) for k in claves) + ". The H2 stand-in (F1) has no key; the cases never repeat an id in the table.")
    s.append("-- NOTE: the catalog lists 60 columns for the table and 60 for the view, in position order; columns the SQL of this class certainly uses (FolioContrato, IdTipoCanalIngreso, Switch,")
    s.append("--       FechaValorCuota, EstadoSolicitud, IdMonedaFondo, IdOmnibus ...) are NOT in the listing: the catalog query looks cut at 60 rows (question P-1 of the report).")
    s.append("-- the view column IdOmnibus is read by SelectSolicitudes and is in no catalog: ASSUMED nullable BIGINT (Q-W0B-9: null or 0 when the solicitud has no omnibus?)")
    s.append('ALTER TABLE "%s" ADD COLUMN IF NOT EXISTS "IdOmnibus" BIGINT;' % V_SOL)
    for nombre, c, d, r in cambios:
        if nombre.endswith("ConsultarSolicitudAporteRescate") and "DECIMAL(34" in r:
            s.append('-- %s.%s: REAL type is HANA DECIMAL(34) without scale (floating); the F1 stand-in says DECIMAL(34,0), which would round every amount to an integer' % (nombre, c))
            s.append('ALTER TABLE "%s" ALTER COLUMN "%s" DECFLOAT(34);' % (V_SOL, c))
    s.append("-- columns that later phases of the legacy read (the `completo` variant of a case runs to the end): FMM.TipoMovimientoFondoMutuo.GeneraCargoAbono (default 'N'), COM.Cobro (IVA)")
    s.append('ALTER TABLE "%s" ADD COLUMN IF NOT EXISTS "GeneraCargoAbono" NVARCHAR(1) DEFAULT \'N\'; -- ASSUMED' % TIPOMOV)
    s.append('CREATE TABLE "BSCL.Plataforma.BD::COM.Cobro" ("IdCobro" NVARCHAR(40), "ValorCobro" DECIMAL(26,8)); -- ASSUMED')
    s.append("-- FMM.ReinversionSerieFondoDestino (read by crearAporteReinversion, FM-CER-52..55, for EVERY rescate of the `completo` run): same columns as schema-rre.sql (REAL, f2_columnas_reinversion.csv)")
    s.append('CREATE TABLE IF NOT EXISTS "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" ("IdReinversionSerieFondo" BIGINT NOT NULL, "IdSolicitudAporteRescate" BIGINT NOT NULL, '
             '"IdSerieFondoDestino" NVARCHAR(40) NOT NULL, "Porcentaje" DECIMAL(26,8) NOT NULL, "IdRegimenTributario" NVARCHAR(40) NOT NULL, "IdCarga" BIGINT DEFAULT 0 NOT NULL, '
             '"Switch" NVARCHAR(1) NOT NULL, "PorcentajeReal" DECIMAL(26,8) NOT NULL, "FechaFinalReinversion" DATE NOT NULL);')
    s.append("-- PLA.Estado (catalog of the states the output prints: PEN RZD APD ADV CRT ING ACP ...)")
    s.append('CREATE TABLE IF NOT EXISTS "%s" ("IdEstado" NVARCHAR(3), "Nombre" NVARCHAR(100)); -- ASSUMED' % ESTADO)
    s.append("-- Tesoreria view ConsultarPorComprobanteOrigenLiquidado (FM-CER-10): ASSUMED columns, the ones the SELECT reads")
    s.append('CREATE TABLE "%s" ("IdComprobanteOrigen" BIGINT, "IdComprobante" BIGINT, "IdTipoComprobanteOrigen" NVARCHAR(3), "IdEstado" NVARCHAR(3));' % COMPROB)
    s.append("-- view FMM.ConsultarIndicadoresPortafolioFondoMutuo (FM-CER-15): ASSUMED columns, the ones the SELECT reads")
    s.append('CREATE TABLE "%s" ("IdPortafolio" BIGINT, "OperaLineaSobregiro" NVARCHAR(1));' % INDIC_VISTA)
    s.append("-- APV / FondoMutuo traspaso tables and the persons of TGR / SII (FM-CER-04, FM-CER-13): columns added to the F1 / DET stand-ins")
    s.append('ALTER TABLE "%s" ADD COLUMN IF NOT EXISTS "DocumentoEntidad" NVARCHAR(40);' % TRA_APV)
    s.append('CREATE TABLE IF NOT EXISTS "%s" ("IdTraspaso" BIGINT, "IdTipoMovimientoTraspaso" NVARCHAR(10), "IdEntidadRecaudadora" BIGINT);' % TRA_FM)
    s.append('CREATE TABLE IF NOT EXISTS "%s" ("IdSolicitudAporteRescate" BIGINT, "IdTraspaso" BIGINT);' % TRA_DET)
    s.append('CREATE TABLE IF NOT EXISTS "%s" ("IdEntidadRecaudadora" BIGINT, "Nombre" NVARCHAR(100));' % ENT_REC)
    s.append('ALTER TABLE "%s" ADD COLUMN IF NOT EXISTS "IdPersona" BIGINT;' % ENT_REC)
    s.append('CREATE TABLE IF NOT EXISTS "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" ("IdPersona" BIGINT, "NombreCompleto" NVARCHAR(200), "DocumentoIdentidad" NVARCHAR(20));')
    s.append('CREATE TABLE IF NOT EXISTS "%s" ("IdCuentaBancaria" BIGINT, "IdTipoCuentaBancaria" NVARCHAR(40), "NumeroCuentaBancaria" NVARCHAR(40), "IdBanco" BIGINT, "IdPersona" BIGINT);' % CUENTA)
    s.append('ALTER TABLE "%s" ADD COLUMN IF NOT EXISTS "IdEstado" NVARCHAR(3);' % CUENTA)
    s.append('ALTER TABLE "%s" ADD COLUMN IF NOT EXISTS "IdMoneda" NVARCHAR(3);' % CUENTA)
    s.append('CREATE TABLE IF NOT EXISTS "%s" ("IdTipoCuentaBancaria" NVARCHAR(40), "NombreTipoCuentaBancaria" NVARCHAR(100));' % TIPOCTA)
    s.append("-- FMM.LogCertificacionAporteRescate (FM-CER-14, FM-CER-66): 22 columns of the INSERT, types ASSUMED (the catalog of W3 has no entry for it)")
    s.append('CREATE TABLE "%s" ("IdLogCertificacionAporteRescate" BIGINT NOT NULL, "IdSolicitudAporteRescate" BIGINT NOT NULL, "Codigo" NVARCHAR(100), "IdEstado" NVARCHAR(3), '
             '"TipoSolicitud" NVARCHAR(3), "IdTipoMovimientoFondoMutuo" NVARCHAR(10), "IdUsuario" BIGINT, "IdEmpresa" BIGINT, "FechaIngresoRegistro" TIMESTAMP, "FechaSistema" NVARCHAR(10), '
             '"IdSerieFondo" NVARCHAR(40), "IdEmisor" NVARCHAR(40), "Monto" DECIMAL(26,8), "IdMoneda" NVARCHAR(3), "MontoOriginal" DECIMAL(26,8), "TipoCambio" DECIMAL(26,8), '
             '"IdMonedaOriginal" NVARCHAR(3), "ValorCuota" DECIMAL(26,8), "NumeroCuotas" DECIMAL(26,8), "IdCliente" BIGINT, "IdPortafolio" BIGINT, "Simulacion" NVARCHAR(1), PRIMARY KEY ("IdLogCertificacionAporteRescate"));' % T_LOG)
    seq = [x for x in secuencias if x[0].endswith("::LogCertificacionAporteRescate")]
    inicio = seq[0][1] if seq else "1"
    s.append("-- sequences of the HANA catalog (REAL): LogCertificacionAporteRescate start %s min 1 inc 1 no cycle cache 1; FolioCartaLiquidacionAporteRescate, SecuenciaComprobanteLiquidacion "
             "(start 1; Liquidacion, not used by T1) and SolicitudAporteRescate (start 11413788, already in schema-fmm.sql)" % inicio)
    s.append('CREATE SEQUENCE "%s" START WITH %s MINVALUE 1 INCREMENT BY 1 NO CYCLE;' % (SEQ_LOG, inicio))
    s.append('CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::FolioCartaLiquidacionAporteRescate" START WITH 1 MINVALUE 1 INCREMENT BY 1 NO CYCLE;')
    s.append('CREATE SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::SecuenciaComprobanteLiquidacion" START WITH 1 MINVALUE 1 INCREMENT BY 1 NO CYCLE;')
    return "\n".join(s) + "\n"


# ----------------------------------------------------------------------------------------------------------------------------------------------------
# seed helpers
# ----------------------------------------------------------------------------------------------------------------------------------------------------
def q(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "'S'" if v else "'N'"
    if isinstance(v, (int, float)):
        return str(v)
    if isinstance(v, str) and v.startswith("@"):   # raw SQL literal (e.g. @TIMESTAMP '...')
        return v[1:]
    return "'" + str(v).replace("'", "''") + "'"


def ins(tabla, **v):
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (tabla, ", ".join('"%s"' % k for k in v), ", ".join(q(x) for x in v.values()))


def config(codigo, valor, empresa=1):
    """Replaces a PLATAFORMA parameter of PLA.Configuracion (valor None deletes it)."""
    s = ['DELETE FROM %s WHERE "IdEmpresa" = %d AND "CodigoParametro" = %s;' % (CONFIG, empresa, q(codigo))]
    if valor is not None:
        s.append('INSERT INTO %s ("IdEmpresa", "CodigoParametro", "Valor") VALUES (%d, %s, %s);' % (CONFIG, empresa, q(codigo), q(valor)))
    return s


def param(nombre, valor, empresa=1):
    """Replaces a FONDOMUTUO parameter of FMM.Parametro (valor None deletes it)."""
    s = ['DELETE FROM %s WHERE "IdEmpresa" = %d AND "NombreTipoParametro" = %s;' % (PARAM, empresa, q(nombre))]
    if valor is not None:
        s.append('INSERT INTO %s ("IdEmpresa", "NombreTipoParametro", "ValorParametro") VALUES (%d, %s, %s);' % (PARAM, empresa, q(nombre), q(valor)))
    return s


def _columnas(tabla):
    """[(column, type text, not null)] of a stand-in table of schema-fmm.sql, with the type taken from the domain of schema-types.sql"""
    fmm = (BASE / "schema-fmm.sql").read_text(encoding="utf-8")
    dom = {}
    for m in re.finditer(r'CREATE DOMAIN "([^"]+)" AS ([^;]+);', (BASE / "schema-types.sql").read_text(encoding="utf-8")):
        dom[m.group(1)] = m.group(2).strip()
    ini = fmm.index('CREATE TABLE "%s"' % tabla)
    fin = fmm.index("\n);", ini)
    cols = []
    for m in re.finditer(r'^\s+"([^"]+)" "([^"]+)"( NOT NULL)?', fmm[ini:fin], re.M):
        cols.append((m.group(1), dom[m.group(2)], bool(m.group(3))))
    return cols


def _defecto(tipo):
    t = tipo.upper()
    if t.startswith("NVARCHAR") or t.startswith("VARCHAR"):
        return "''"
    if t == "DATE":
        return "DATE '0001-01-01'"
    if t == "TIMESTAMP":
        return "TIMESTAMP '0001-01-01 00:00:00'"
    return "0"


_COLS = {}


def cols(tabla):
    if tabla not in _COLS:
        _COLS[tabla] = _columnas(tabla)
    return _COLS[tabla]


def _lit(v):
    if isinstance(v, Num):
        return v.texto
    if v is None:
        return "NULL"
    if isinstance(v, str) and v.startswith("@"):
        return v[1:]
    if isinstance(v, str) and re.fullmatch(r"\d{4}-\d{2}-\d{2}", v):
        return "DATE '%s'" % v
    if isinstance(v, str) and re.fullmatch(r"\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}", v):
        return "TIMESTAMP '%s'" % v
    return q(v)


# values every solicitud of the cases has unless the case says otherwise (FAKE data; the ids of the base universe of seed/base-fmm.sql + seed/base-cer.sql)
BASE_SOL = dict(
    IdTipoCanalIngreso="OPT", IdCliente=100, IdPortafolio=500, IdSerieFondo="FM001A", IdDividendo=0, IdSerieFondoDestino="", IdEmisorDestino="", IdClienteDestino=0,
    IdPortafolioDestino=0, IdSerieFondoOrigen="", IdEmisorOrigen="", MontoOrigen=0, IndicadorMLT="N", Overnight="N", IdSolicitudAporteRescateRelacionado=0, IdTipoCartera="",
    CalculoPorMontoCuota="M", FechaEmision="2026-03-02", FechaAceptacion="2026-03-02", FechaValorCuota="2026-03-02", FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03",
    FechaAnulacion="0001-01-01", FechaOriginalInversion="0001-01-01", FechaIngresoRegistro="2026-03-01 09:00:00", FechaModificacion="0001-01-01",
    FechaAnulacionRegistro="0001-01-01 00:00:00", FechaAceptacionRegistro="2026-03-01 09:30:00", FechaCertificacionRegistro="0001-01-01 00:00:00",
    FechaLiquidacionRegistro="0001-01-01 00:00:00", FechaModificacionRegistro="0001-01-01 00:00:00", EsPorMontoLiquido="N", Monto=1000000, NumeroCuotas=0, Porcentaje=0,
    ValorCuota=0, CostoUF=0, CostoMonedaLocal=1000000, ParidadFechaCertificacion=0, ValorUFFechaInversion=0, ParidadFechaInversion=0, IdMoneda="CLP", MontoComision=0,
    ComisionInformada="N", MontoComisionLocal=0, MontoLiquido=0, MontoLiquidoLocal=0, MontoPago=0, MontoPagoLocal=0, MontoInversion=0, NumeroCuotasInversion=0,
    ValorCuotaInversion=0, CostoMonedaLocalInversion=0, Spread=0, EstadoLiquidacion="ING", MueveCta="N", FolioCartaLiquidacion=0, IdMedioDePago="CIN", Glosa="",
    IdEmpresa=1, IdUsuario=7, IdModulo="FMM", OrigenOperacion="M", SolicitudBatch="N", IdRegimenTributario="GEN", TipoCarteraNormativa="", Tributacion="",
    IdRegimenTributarioDestino="", TipoCarteraNormativaDestino="", AdministracionLiquidez="N", Visado="N", IdPreferenciaPagoPortafolio=0, IdAutorizadoOrigen=0,
    IdAutorizadoDestino=0, IdMonedaSistema="CLP", MontoMonedaSistema=0, Switch="N", EstadoAnulacion="", FolioOmnibus=0, FolioAgfOmnibus=0, IdEmpleadoEjecutivo=20,
    IdEmpleadoEjecutivoDestino=0, IdMonedaPago="CLP", IdIngresoEgreso=0, IdOperacionSpot=0, IdCartera=0, IdAgrupador=0, IngresoAutomatico="N", TipoContratoOperacion="",
    FolioContratoOperacion="", RentabilidadReal=0, IdOrigenRecaCliente=0, FolioContrato=0, SecuenciaMovimiento=0,
    # columns of the VIEW only (the view derives them from the joins of HANA; here the case writes them)
    IdInstrumento=900, IdTipoInstrumento="FM", IdEmisor="AGF1", IdMonedaFondo="CLP", IdPersona=1000, ValorCuotaSolicitud=0, CodigoPortafolio="PF500",
    DocumentoIdentidad="11111111-1", NombreCompleto="Cliente Ficticio Uno", IdEstadoSerieFondo="ACT", IdFamiliaFondo="", PropositoPortafolio="PROPGEN", IdOmnibus=0,
)

TIPO_MOV = {"APO": "APONORM", "RES": "RESNORM"}


class Sol(list):
    """The SQL statements of ONE solicitud plus `spec`, the dict of its column values (the generator reads it to script the benign answers of the collaborators)."""
    spec = None


def sol(id_, tipo="RES", estado="ING", **c):
    """The two rows of ONE solicitud: the stand-in TABLE FMM.SolicitudAporteRescate and the stand-in VIEW FMM.ConsultarSolicitudAporteRescate (HANA derives the view from the table
    and the joins; in H2 both are written by the case with the SAME values). `estado` is the state of the table (Estado) and of the view (EstadoSolicitud). A keyword names a column of
    either; a value None writes NULL; a value '@...' is a raw SQL literal."""
    v = dict(BASE_SOL)
    v["IdSolicitudAporteRescate"] = id_
    v["TipoSolicitud"] = tipo
    v["IdTipoMovimientoFondoMutuo"] = TIPO_MOV.get(tipo, "RESNORM")
    v["Estado"] = estado
    v["EstadoSolicitud"] = estado
    v.update(c)
    out = Sol()
    out.spec = dict(v)
    for tabla in (T_SOL, V_SOL):
        fila = {}
        for n, t, nn in cols(tabla) + ([("IdOmnibus", "BIGINT", False)] if tabla == V_SOL else []):
            if n in v and not (v[n] is None and nn):    # a NULL only where the real column is nullable (e.g. the view's Monto / NumeroCuotas; the table's are NOT NULL)
                fila[n] = _lit(v[n])
            elif nn:
                fila[n] = _defecto(t)
            else:
                fila[n] = "NULL"
        out.append('INSERT INTO "%s" (%s) VALUES (%s);' % (tabla, ", ".join('"%s"' % n for n in fila), ", ".join(fila.values())))
    return out


def aporte(id_, **c):
    return sol(id_, "APO", **c)


def rescate(id_, **c):
    return sol(id_, "RES", **c)


def solo_tabla(id_, tipo="RES", estado="CRT", **c):
    """Only the TABLE row (the solicitud a case reaches through the LEFT JOIN `sr`: the related solicitud that is not itself certified in the request)."""
    return [x for x in sol(id_, tipo, estado, **c) if x.startswith('INSERT INTO "%s"' % T_SOL)]   # a plain list: it is NOT a solicitud of the request (no spec)


def indicador_portafolio(portafolio, sobregiro):
    return ins(INDIC_VISTA, IdPortafolio=portafolio, OperaLineaSobregiro=sobregiro)


def opera_saldo_negativo(portafolio, valor, id_tipo=900):
    """IND.Indicador with the GEN_OCSNEG type (FM-CER-20, CELLINCECT-763)"""
    s = []
    if portafolio == "tipo":
        return [ins(IND_TIPO, IdTipoIndicador=id_tipo, IdTipoEntidad="POR", CodigoTipoIndicador="GEN_OCSNEG", IdEstado="ACT")]
    s.append(ins(IND_INDICADOR, IdEntidad=portafolio, IdTipoIndicador=id_tipo, IdTipoEntidad="POR", ValorIndicador=valor))
    return s


def comprobante_no_liquidado(id_solicitud, comprobante=7001, estado="PEN", origen="FMM"):
    return ins(COMPROB, IdComprobanteOrigen=id_solicitud, IdComprobante=comprobante, IdTipoComprobanteOrigen=origen, IdEstado=estado)


def persona_con_documento(id_persona, id_doc, documento, predeterminado="S"):
    return [ins(DOCIDENT, IdDocumentoIdentidad=id_doc, DocumentoIdentidad=documento, IdTipoDocumentoIdentidad="RUT", Predeterminado=predeterminado),
            ins(PERSONA, IdPersona=id_persona, IdDocumentoIdentidad=id_doc)]


def estado_pla(id_, nombre):
    return ins(ESTADO, IdEstado=id_, Nombre=nombre)


# ----------------------------------------------------------------------------------------------------------------------------------------------------
# collaborator scripts (answers of the registered doubles)
# ----------------------------------------------------------------------------------------------------------------------------------------------------
def precio(id_registro, valor=1500.1234, fecha="2026-03-02 00:00:00.000", instrumento=900, moneda="CLP", nemo="FM001A"):
    """One row of the EntregarPreciosTasas answer (IdRegistro = IdSolicitudAporteRescate of F-A and IdInterno of CalcularMontoNumeroCuota). fecha None writes a null FechaFuente;
    instrumento None writes a null IdInstrumento (the legacy drops that row)."""
    return {"IdRegistro": id_registro, "IdClasificacionTipoInstrumento": "FM", "IdTipoInstrumento": "FM", "IdInstrumento": instrumento, "Nemotecnico": nemo, "Precio": valor,
            "CodigoFuente": "OTR", "FechaFuente": fecha, "IdMoneda": moneda}


def precios(*filas):
    return {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": list(filas)}}}


def falla(colaborador, texto="PARADA_T1"):
    return {colaborador: {"respuestas": [{"error": texto}]}}


# first collaborator that F-C / F-D reach: scripted to fail in the `parada` variant of a case that continues
PARADA = ("SaldoFlujoCaja", "CalculaCustodiaRescate", "ProcesarCustodiaFondoMutuo")


def saldo_flujo(*filas):
    """SaldoFlujoCaja answer: rows {IdPortafolio, IdMoneda, FechaSaldo, Saldo}"""
    return {"SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": list(filas)}}}


def _partir(valores):
    """splits the VALUES (...) text of an INSERT of seed/base-fmm.sql on the commas outside quotes"""
    out, cur, comilla = [], "", False
    for ch in valores:
        if ch == "'":
            comilla = not comilla
        if ch == "," and not comilla:
            out.append(cur.strip())
            cur = ""
        else:
            cur += ch
    out.append(cur.strip())
    return out


def _fila_base(tabla):
    """(columns, literal values) of the FIRST INSERT of `tabla` in seed/base-fmm.sql"""
    for linea in (BASE / "seed" / "base-fmm.sql").read_text(encoding="utf-8").splitlines():
        if linea.startswith('INSERT INTO "%s" ' % tabla):
            m = re.match(r'INSERT INTO "[^"]+" \((.*?)\) VALUES \((.*)\);$', linea)
            cs = [c.strip().strip('"') for c in m.group(1).split(",")]
            return cs, _partir(m.group(2))
    raise KeyError(tabla)


def clonar(tabla, **cambios):
    """INSERT of a copy of the base row of `tabla` with the given columns replaced (a value '@...' is written as a raw SQL literal)"""
    cs, vals = _fila_base(tabla)
    fila = dict(zip(cs, vals))
    for k, v in cambios.items():
        fila[k] = q(v)
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (tabla, ", ".join('"%s"' % c for c in cs), ", ".join(fila[c] for c in cs))


def tipo_movimiento(id_, indicador, generaomnibus=""):
    return ins(TIPOMOV, IdTipoMovimientoFondoMutuo=id_, Descripcion=id_, IndicadorAporteRescate=indicador, IdTipoOperacion="RESCFM" if indicador == "RES" else "APORFM",
               TieneCarga="", TieneIngresoManual="", TieneCertificacion="", IdEstado="ACT", GeneraOmnibus=generaomnibus)


def base_cer():
    """seed/base-cer.sql: the FAKE rows every FMM-CER case needs on top of seed/base-fmm.sql (loaded through esquemasExtra after schema-cer.sql)."""
    s = ["-- GENERATED by make_cases_cer.py (cases_cer_lib.base_cer). FAKE universe of the FMM-CER cases, on top of seed/base-fmm.sql (company 1, user 7, FechaSistema 2026-03-02, series FM001A).",
         "-- company 2: present with a business partner and user 8 but with NO configuration rows at all (DESVIO-KIT-1); user 9 is inactive."]
    s.append(ins("BSCL.Plataforma.BD::PLA.Empresa", IdEmpresa=2, IdSocioDeNegocio=10))
    s.append(ins("BSCL.Plataforma.BD::ACC.Usuario", IdUsuario=8, IdEmpresa=2, IdEstado="ACT", TipoEntidad="EMP", IdEntidad=70))
    s.append(ins("BSCL.Plataforma.BD::ACC.Usuario", IdUsuario=9, IdEmpresa=1, IdEstado="INA", TipoEntidad="EMP", IdEntidad=70))
    s.append("-- funds / series: FM001A (CLP, 4 decimals of quota), FM001B (2 decimals of value), FM002A (USD fund, 6 / 4), FM003A (0 decimals); family ids are fake")
    s.append(clonar(FONDO, IdFondo="20", IdEmisor="AGF2", Nemotecnico="FM002", NombreFondo="Fondo Ficticio Dos USD", IdMoneda="USD", IdPaisEmisor=840, IdPaisOrigen=840))
    for ids, instr, fondo, dec in (("FM001B", 901, "10", 2), ("FM002A", 903, "20", 6), ("FM003A", 904, "10", 0)):
        s.append(clonar(SERIE, IdSerieFondo=ids, IdInstrumento=instr, IdFondo=fondo, NombreSerieFondo="Serie " + ids, CodigoSerie=ids[-1], DecimalesValorCuota=dec))
    s.append(ins(VALORTC, IdSerieFondo="FM002A", IdEmpresa=1, ValorDolarAporte=910, ValorDolarRescate=905))
    s.append(ins(VALORTC, IdSerieFondo="FM001B", IdEmpresa=1, ValorDolarAporte=0, ValorDolarRescate=0))
    s.append("-- FMM parameters of Certificar (FM-CER-03): the base file has CALCULAR_COMISION_CERTIFICACION and RESCATE_GENERA_VENTA_SPOT_DOLAR; the others are added here")
    for nombre, valor in (("TipoComprobanteFacturaNoApv", "FFM"), ("TipoComprobanteFacturaApv", "FOE"), ("VALIDAR_CERTIFICACION", "N"), ("RESCATE_COMISION_COMPROBANTE_AUTOMATICO", "N")):
        s.append(ins("BSCL.FondoMutuo.BD::FMM.Parametro", IdEmpresa=1, NombreTipoParametro=nombre, ValorParametro=valor))
    s.append("-- movement types (GeneraOmnibus 'S' only for the two fake types APOOMNI / RESOMNI that the omnibus cases use)")
    for t, ind, om in (("APOREINV", "APO", ""), ("RESREINV", "RES", ""), ("RESCNN", "RES", ""), ("RESCOM", "RES", ""), ("RESTRAPOR", "RES", ""), ("APOOMNI", "APO", "S"), ("RESOMNI", "RES", "S")):
        s.append(tipo_movimiento(t, ind, om))
    s.append("-- regimes: REGSA certifies WITHOUT a certified associated solicitud ('N'), REGSS with ('S'); GEN (base) has CertificaSinAsociado NULL")
    s.append(clonar(REGIMEN, IdRegimenFondoMutuo="REGSA"))
    s.append(clonar(REGIMEN, IdRegimenFondoMutuo="REGSS"))
    s.append("UPDATE \"%s\" SET \"CertificaSinAsociado\" = 'N' WHERE \"IdRegimenFondoMutuo\" = 'REGSA';" % REGIMEN)
    s.append("UPDATE \"%s\" SET \"CertificaSinAsociado\" = 'S' WHERE \"IdRegimenFondoMutuo\" = 'REGSS';" % REGIMEN)
    s.append("-- states the output prints (NombreEstado of ResultadoCertificacion); IMPORTANT: the states of the case must exist or procesarSalida throws NullPointerException (FM-CER-67)")
    for i, n in (("ING", "Ingresada"), ("ACP", "Aceptada"), ("PEN", "Pendiente"), ("ADV", "Advertencia"), ("RZD", "Rechazada"), ("APD", "Aprobada"), ("CRT", "Certificada"),
                 ("LIQ", "Liquidada"), ("ANU", "Anulada")):
        s.append(estado_pla(i, n))
    s.append("-- currencies: EUR (fake format row: 3 decimals of amount, TRUNCATE) and USD / CLP of the base file")
    s.append(ins("BSCL.Plataforma.BD::PLA.Moneda", IdMoneda="EUR", IdPais=250))
    s.append(clonar(MONEDAFMT, IdMonedaFormato=3, IdMoneda="EUR", MontoDecimales=3, MontoTrucaRedondea="T"))
    s.append("-- persons of TGR and SII (FM-CER-04, FM-CER-13): FAKE documents (not the real ones of those institutions) configured through APV.DocumentoIdentidadPersonaTGR / ...SII")
    s += persona_con_documento(3001, 301, "99999991-1")
    s += persona_con_documento(3002, 302, "99999992-2")
    s += config("APV.DocumentoIdentidadPersonaTGR", "99999991-1")
    s += config("APV.DocumentoIdentidadPersonaSII", "99999992-2")
    s.append("-- IVA of the commission (later phases read it with cobros.get(0): without the row the `completo` run of a rescate would end in IndexOutOfBoundsException)")
    s.append(ins("BSCL.Plataforma.BD::COM.Cobro", IdCobro="IVA", ValorCobro=19))
    s.append("-- GEN_OCSNEG indicator type (FM-CER-20); the indicator rows are per case")
    s.append(ins(IND_TIPO, IdTipoIndicador=900, IdTipoEntidad="POR", CodigoTipoIndicador="GEN_OCSNEG", IdEstado="ACT"))
    s.append("-- dictionary: the codes of the output. The ones NOT listed here come out as '<codigo> sin traduccion' (kit behaviour)")
    for cod, txt in (("NO_SE_INGRESARON_SOLICITUDES", "No se ingresaron solicitudes"), ("SOLICITUD_DUPLICADA", "Solicitud duplicada"),
                     ("UI_NO_EXISTEN_SOLICITUDES_CUMPLAN_FILTROS", "No existen solicitudes que cumplan los filtros"),
                     ("UI_ESTADO_INCORRECTO_CERTIFICACION", "Estado incorrecto para certificar"),
                     ("UI_FECHA_INVALIDA_CERTIFICACION_FONDO_MUTUO", "Fecha de certificacion invalida"), ("UI_NO_EXISTE_VALOR_CUOTA", "No existe valor cuota cargado"),
                     ("UI_DOCUMENTO_ASOCIADO_NO_LIQUIDADO", "Documento ::IdComprobante:: asociado no liquidado (solicitud ::IdSolicitudAporteRescate::)"),
                     ("UI_VALIDACION_LIQUIDACION_FONDO_MUTUO", "El aporte debe estar liquidado"),
                     ("UI_SOLICITUD_ASOCIADA_NO_CERTIFICADA", "Solicitud asociada ::IdSolicitudAporteRescateRelacionado:: no certificada ni liquidada"),
                     ("UI_NO_EXISTE_TGR_CONFIGURADA", "No existe TGR configurada"), ("ERROR_CALCULO_MONTO_CUOTA", "Error al calcular monto y numero de cuotas"),
                     ("UI_EXITO", "Certificada ::DocumentoIdentidad:: ::CodigoPortafolio::"), ("UI_SALDO_INSUFICIENTE_FLUJO_CAJA", "Saldo insuficiente en cuenta de inversion"),
                     ("USUARIO_NO_EXISTE", "Usuario no existe"), ("MODULO_NO_EXISTE", "Modulo no existe"), ("EMPRESA_NO_EXISTE", "Empresa no existe"),
                     ("USUARIO_NO_VIGENTE", "Usuario no vigente")):
        s.append(ins(DICC, CodLenguaje="es", CodDiccionario=cod, Descripcion=txt))
    return "\n".join(s) + "\n"
