"""F3 W3 T6-B (LiquidacionSolicitudAporteRescate, cards FM-LIQ-01..20): the building blocks of the case writer cases_liq.py (case directories cases-liq/FMM-LIQ-nnn_slug).

Same method as T1..T6-A: the case INPUTS (case.json, seed.sql) are written by cases_liq.py; the goldens (expected.json) are RECORDED by legacy-oracle/record_liq.sh from the UNMODIFIED legacy class
`cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate` (H2 MODE=LEGACY, the three collaborators outside the module are registered doubles that record every call, 20 runs per case; an unstable case gets
NO golden). NEVER derived from reading the code. All data is fake (ids of the base universe of seed/base-fmm.sql, persons 7001.., portfolios 8001.., amounts invented).

This module only holds helpers: the solicitud rows (table + view, with the view columns Liquidacion reads), the parameters / modalities / withholding configuration, the scripted answers of the
collaborators and the case writer itself.
"""
import json
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import cases_cer_lib as L  # noqa: E402
from cases_cer_lib import ABSENT, Num  # noqa: E402,F401

CASES_LIQ = L.BASE / "cases-liq"
SERVICIO = "LiquidacionSolicitudAporteRescate"
PRIMER_NUMERO = 1
_n = {"n": PRIMER_NUMERO - 1}
ESQUEMAS = ["schema-liq.sql", "seed/base-liq.sql"]

T_SOL = L.T_SOL
V_SOL = L.V_SOL
T_MOD = "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago"
T_RET = "BSCL.APV.BD::TRA.ConfiguracionRetencionTraspaso"
T_PARAM = "BSCL.FondoMutuo.BD::FMM.Parametro"
T_CONF = "BSCL.Plataforma.BD::PLA.Configuracion"
T_DIC = "BSCL.Plataforma.BD::PLA.Diccionario"
TABLAS = [{"nombre": T_SOL, "orden": '"IdSolicitudAporteRescate"',
           "columnas": ["IdSolicitudAporteRescate", "IdEmpresa", "TipoSolicitud", "IdTipoMovimientoFondoMutuo", "Estado", "EstadoLiquidacion", "FolioCartaLiquidacion", "FechaLiquidacion",
                        "FechaLiquidacionRegistro", "Monto", "MontoComision", "IdMedioDePago"]}]

# the columns of the VIEW the two selects read that are not in the F1 stand-in (schema-liq.sql adds them) and their defaults: an AGF (person 7001, portfolio 8001) paid / charged by transfer
VISTA_EXTRA = dict(IdPersonaAGF=7001, IdPortafolioAGF=8001, IdMedioDePagoAbonoAGF="TRA", IdMedioDePagoCargoAGF="TRA", IdMatrizAgrupadorAPV=None, IdOmnibus=1)
# the defaults of a solicitud ready to be liquidated (certified, state of liquidation ING, 1.000.000 CLP, paid by transfer, client 1000 / portfolio 500 as in the base universe)
BASE_LIQ = dict(Estado="CRT", EstadoLiquidacion="ING", IdMedioDePago="TRA", Monto=1000000, MontoComision=0, MontoPago=0, MontoPagoLocal=0, MontoComisionLocal=0, CostoMonedaLocal=1000000,
                IdMoneda="CLP", IdMonedaFondo="CLP", IdSerieFondo="FM001A", FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03", IdPersona=1000, CodigoPortafolio="PF500")

TIPOS_MOV = {"APO": "APONORM", "RES": "RESNORM"}


class Fila(list):
    """The SQL statements of ONE solicitud plus `spec`, the dict of its values (the writer reads it only to describe the case)."""
    spec = None


def _lit(v):
    return L._lit(v)


def sol(id_, tipo="APO", mov=None, **c):
    """The rows of ONE solicitud in the stand-in TABLE and in the stand-in VIEW (the case writes both, as HANA derives the view from the table), then the UPDATE of the view columns Liquidacion
    reads (VISTA_EXTRA). A keyword names a column of the table / view or one of VISTA_EXTRA; None writes NULL; a value '@...' is a raw SQL literal. `mov` is IdTipoMovimientoFondoMutuo."""
    extra = dict(VISTA_EXTRA)
    for k in list(c):
        if k in VISTA_EXTRA:
            extra[k] = c.pop(k)
    v = dict(BASE_LIQ)
    v.update(c)
    estado = v.pop("Estado")
    base = L.sol(id_, tipo, estado, IdTipoMovimientoFondoMutuo=(mov or TIPOS_MOV[tipo]), **v)
    out = Fila(base)
    sets = ", ".join('"%s" = %s' % (k, _lit(x)) for k, x in extra.items())
    out.append('UPDATE "%s" SET %s WHERE "IdSolicitudAporteRescate" = %d;' % (V_SOL, sets, id_))
    out.spec = dict(v)
    out.spec.update(extra)
    out.spec.update(IdSolicitudAporteRescate=id_, TipoSolicitud=tipo, IdTipoMovimientoFondoMutuo=(mov or TIPOS_MOV[tipo]))
    return out


def aporte(id_, **c):
    return sol(id_, "APO", **c)


def rescate(id_, **c):
    return sol(id_, "RES", **c)


def aplanar(seed):
    out, specs = [], []
    for it in seed:
        if isinstance(it, Fila):
            specs.append(it.spec)
            out.extend(it)
        elif isinstance(it, (list, tuple)):
            o, s = aplanar(it)
            out.extend(o)
            specs.extend(s)
        else:
            out.append(it)
    return out, specs


def param(nombre, valor, empresa=1):
    """replaces a parameter of FMM.Parametro (valor None deletes it)"""
    s = ['DELETE FROM "%s" WHERE "IdEmpresa" = %d AND "NombreTipoParametro" = %s;' % (T_PARAM, empresa, L.q(nombre))]
    if valor is not None:
        s.append('INSERT INTO "%s" ("IdEmpresa", "NombreTipoParametro", "ValorParametro") VALUES (%d, %s, %s);' % (T_PARAM, empresa, L.q(nombre), L.q(valor)))
    return s


def config(codigo, valor, empresa=1):
    """replaces a parameter of PLA.Configuracion (valor None deletes it)"""
    return L.config(codigo, valor, empresa)


def cta_inv(valor, empresa=1):
    return param("LiquidacionCuentaInversionCliente", valor, empresa)


def omnibus(valor, empresa=1):
    return param("VALIDAR_LIQUIDACION_OMNIBUS", valor, empresa)


def apv(valor, empresa=1):
    return config("APV.Habilitado", valor, empresa)


def modalidades(*pares):
    """replaces the whole table of media of payment: pares = ("TRA", "MODDEP"), ..."""
    s = ['DELETE FROM "%s";' % T_MOD]
    for medio, mod in pares:
        s.append('INSERT INTO "%s" ("IdMedioDePago", "IdModalidadDePago", "IdEstado") VALUES (%s, %s, \'ACT\');' % (T_MOD, L.q(medio), L.q(mod)))
    return s


def retencion(*filas):
    """replaces the withholding configuration: filas = dict(pct=15, retiro="RES", regimen=None, tope=0, persona=0, estado="VIG")"""
    s = ['DELETE FROM "%s";' % T_RET]
    for f in filas:
        d = dict(pct=0, retiro="RES", regimen=None, tope=0, persona=0, estado="VIG")
        d.update(f)
        s.append('INSERT INTO "%s" ("PorcentajeRetencion", "MontoTopeRetencion", "CodigoRetiro", "IdPersonaRetencion", "IdRegimenTributario", "IdEstado") VALUES (%s, %s, %s, %s, %s, %s);'
                 % (T_RET, _lit(d["pct"]), _lit(d["tope"]), L.q(d["retiro"]), _lit(d["persona"]), _lit(d["regimen"]), L.q(d["estado"])))
    return s


def empresa_dos():
    """the rows company 2 needs to be a company of the module: its date of the system, its language (the dictionary), its two parameters of Liquidacion. NOT APV.Habilitado (the class reads company 1)"""
    return (config("PLA.FechaSistema", "2026-03-02", 2) + config("PLA.LocalizacionSistema", "es", 2) + param("LiquidacionCuentaInversionCliente", "N", 2) + param("VALIDAR_LIQUIDACION_OMNIBUS", "N", 2))


# ------------------------------------------------------------------------------------------------------------------------------------------------------------
# the request
# ------------------------------------------------------------------------------------------------------------------------------------------------------------
def filtro(**c):
    """a filter. Default: all texts empty, IdInterno 0. A value ABSENT removes the key (the field reads null); None writes JSON null."""
    d = {"IdInterno": 0, "IdEmisor": "", "TipoSolicitud": "", "IdEstado": "", "FechaCertificacion": "", "FechaLiquidacion": "", "IdSerieFondo": ""}
    d.update(c)
    return {k: v for k, v in d.items() if v is not ABSENT}


def entrada(movs=(), filtros=(), empresa=1, usuario=7, modulo="FMM", sim="N", reglas="S", **campos):
    """The request. `movs`: ids (list) or ABSENT / None for the field itself; `filtros`: list of filters or ABSENT / None. Extra keywords replace / add fields; ABSENT removes the key."""
    d = {"p_IdEmpresa": empresa, "p_IdUsuario": usuario, "p_IdModulo": modulo, "p_SimularOperacion": sim, "p_UsaReglasNegocio": reglas}
    if movs is None:
        d["p_Movimientos"] = None
    elif movs is not ABSENT:
        d["p_Movimientos"] = [{"IdMovimiento": m} for m in movs]
    if filtros is None:
        d["p_Filtros"] = None
    elif filtros is not ABSENT:
        d["p_Filtros"] = list(filtros)
    d.update(campos)
    return {k: v for k, v in d.items() if v is not ABSENT}


# ------------------------------------------------------------------------------------------------------------------------------------------------------------
# the scripted collaborators (registered doubles: answers by "respuestas" / "defecto", every call is recorded in the golden)
# ------------------------------------------------------------------------------------------------------------------------------------------------------------
def msg(tipo, codigo, descripcion="", id_=0):
    return {"id": id_, "tipo": tipo, "codigo": codigo, "descripcion": descripcion}


def ie_ok(*refs, contador=0, mensajes=(), generados=ABSENT, resultado=()):
    """CrearIngresoEgreso answers: the movements generated for the NumeroReferencia given (default: none), counter `contador` (None writes null), messages."""
    gen = [{"NumeroReferencia": r, "IdIngresoEgreso": 9000 + r, "IdTipoMovimiento": "MOVEGR"} for r in refs] if generados is ABSENT else generados
    return {"CrearIngresoEgreso": {"defecto": {"P_OUT_ERRORESCONTADOR": contador, "P_OUT_MENSAJES": list(mensajes), "P_OUT_INGRESOEGRESOGENERADO": gen, "P_OUT_RESULTADOPROCESO": list(resultado)}}}


def ca_ok(n=1, contador=0, mensajes=(), generados=ABSENT):
    """CrearCargoAbono answers: `n` generated charges / credits (default 1), counter, messages."""
    gen = [{"NumeroCargoAbono": k + 1, "IdCargoAbonoMovimiento": 5000 + k, "IdTipoMovimiento": "MOVCAR"} for k in range(n)] if generados is ABSENT else generados
    return {"CrearCargoAbono": {"defecto": {"P_OUT_ERRORESCONTADOR": contador, "P_OUT_MENSAJES": list(mensajes), "P_OUT_CARGOABONOGENERADO": gen}}}


def falla(colaborador, texto):
    """the collaborator throws a plain Exception(texto)"""
    return {colaborador: {"respuestas": [{"error": texto}]}}


def agregar_log(contador=0, mensajes=()):
    return {"AgregarLog": {"defecto": {"P_OUT_ERRORESCONTADOR": contador, "P_OUT_MENSAJES": list(mensajes)}}}


# ------------------------------------------------------------------------------------------------------------------------------------------------------------
# the case writer
# ------------------------------------------------------------------------------------------------------------------------------------------------------------
def _escribir(cid, doc, seed_texto):
    d = CASES_LIQ / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(L.dumps(doc), encoding="utf-8", newline="\n")
    (d / "seed.sql").write_text(seed_texto, encoding="utf-8", newline="\n")


def caso(slug, ref, ent, seed=(), colaboradores=None, excepcion=False, tags=(), **extra):
    """Writes cases-liq/FMM-LIQ-nnn_slug and returns its id. `ref`: the rule cards (FM-LIQ-nn) and quirk tags the case pins. `excepcion=True`: the legacy is expected to throw somewhere in the run (the
    golden carries `excepcion`); otherwise the run must end normally. The default answers are: Tesoreria generated NOTHING (counter 0), CuentaInversion generated ONE movement, AgregarLog ok."""
    _n["n"] += 1
    sql, specs = aplanar(seed)
    colab = {}
    colab.update(ie_ok())
    colab.update(ca_ok(1))
    colab.update(agregar_log())
    colab.update(colaboradores or {})
    cid = "FMM-LIQ-%03d_%s" % (_n["n"], slug)
    refs = list(ref) + ["front:L", "LIQ-modo:excepcion" if excepcion else "LIQ-modo:completo"] + list(tags)
    doc = {"servicio": SERVICIO, "ref": refs, "esquemasExtra": ESQUEMAS, "entrada": ent, "tablas": TABLAS, "colaboradores": colab}
    doc.update(extra)
    texto = "-- case seed on top of schema-types / schema-fmm / seed/base-fmm.sql + schema-liq.sql + seed/base-liq.sql (fake data)\n" + ("\n".join(sql) if sql else "SELECT 1;") + "\n"
    _escribir(cid, doc, texto)
    return cid
