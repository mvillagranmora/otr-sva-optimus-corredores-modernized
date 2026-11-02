"""Helpers of the F3 W3 T4-A cases (CrearSolicitudAporteReinversion, "AR": the class Certificar calls from crearAporteReinversion): case directories cases-csr/FMM-CSR-nnn_slug.

The case INPUTS (case.json, seed.sql), seed/base-csr.sql and schema-csr.sql are written/kept here; the goldens (expected.json) are RECORDED by legacy-oracle/record_csr.sh from the
unmodified legacy (20 runs per case, 0 unstable), never by this script. Every case loads schema-csr.sql + seed/base-csr.sql through "esquemasExtra".

FAKE universe on top of seed/base-fmm.sql (client 100, portfolio 500 = code PF500, user 7 = PERFIL_COMERCIAL, system currency CLP, additional currency USD, country 56, clock 2026-03-02 Monday):
  series (instrument, fund, issuer, foreign, fund country, issuer country, currency)      days of CONTRIBUTION (retention, valuta cuota, certification, curse)
  FM001A (900, 10, AGF1, N, 56, 56, CLP)  base series                                      0 0 0 0
  FM002A (901, 11, AGF2, N, 56, 56, CLP)                                                   1 2 1 0
  FMX01U (910, 20, EXT1, S, 840, 840, USD)  foreign                                        1 1 2 0
  FMX02U (911, 21, EXT1, S, 840, 840, USD)  foreign, SAME issuer as FMX01U (SWITCH)        1 1 2 0
  FMX03U (912, 22, EXT2, S, 840, 840, USD)  foreign, other issuer (INTERNACIONAL)          1 1 2 0
  FMN01U (913, 23, AGF3, N, 56, 56, USD)    national dollar (EmisorFondoNacionalDolar S)   0 1 0 0
  FMN02U (914, 24, AGF3, N, 56, 56, USD)    national dollar, other fund (same issuer)      0 1 0 0
  FMN03U (915, 25, AGF3, N, 56, 56, USD)    national dollar WITH curse days (VALUTASNACUSD) 0 1 0 2
Regimes (all of them valid for every series, proposito PROPGEN): GEN (group DC, base), 18Q (DC), APVA (group APV), A108 (ART108), A107 (ART107), R57B (57BIS), NOAP (PermiteAporte N).
Portfolios of client 100 (all with the indicator GEN_OCCINV = S): 500 PF500 (base), 501 APV-A, 502 BF, 503 DC-DL, 504 DC-AL, 505 DC-R, 506 apv-a (lower case), 507 PFNEW (no indicator).
Related rescue 9001 (RESREINV, regime GEN) and 9002 (regime 18Q) are seeded in FMM.SolicitudAporteRescate; reinversion rows 1..3 (FMM.ReinversionSerieFondoDestino) point to them.
"""
import copy
import json
import pathlib
import re
import sys

BASE = pathlib.Path(__file__).resolve().parent
sys.path.insert(0, str(BASE))
import cases3a_lib as C3  # noqa: E402
from cases3a_lib import (lit, update, insert, parametro, config_plataforma, feriado, fin_de_semana, cfg_borrar, cfg, clonar_serie)  # noqa: E402,F401

CASES = BASE / "cases-csr"
PRIMER_NUMERO = 1
TABLAS_GRANDES = [{"nombre": "CSR.ConteoSolicitudes", "orden": "\"Filas\""}, {"nombre": "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino", "orden": "\"IdReinversionSerieFondo\""}]
_n = {"n": PRIMER_NUMERO - 1}

SERIE = '"BSCL.FondoMutuo.BD::FMM.SerieDeFondo"'
FONDO = '"BSCL.FondoMutuo.BD::FMM.Fondo"'
REGSERIE = '"BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo"'
REGIMEN = '"BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"'
PARAM = '"BSCL.FondoMutuo.BD::FMM.Parametro"'
PLACONF = '"BSCL.Plataforma.BD::PLA.Configuracion"'
SOLICITUD = '"BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"'
REINV = '"BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino"'
PROPOSITO = '"BSCL.FondoMutuo.BD::FMM.ConfiguracionPropositoPortafolio"'
PORTAFOLIO = '"BSCL.Portafolio.BD::POR.Portafolio"'
IND = '"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"'
INDFM = '"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarIndicadoresPortafolioFondoMutuo"'
TIPOMOV = '"BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo"'
DICC = '"BSCL.Plataforma.BD::PLA.Diccionario"'
USUARIO = '"BSCL.Plataforma.BD::ACC.Usuario"'
CONFFECHAS = C3.CONFFECHAS

TABLAS = [
    {"nombre": "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate", "orden": "\"IdSolicitudAporteRescate\""},
    {"nombre": "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino", "orden": "\"IdReinversionSerieFondo\""},
    {"nombre": "BSCL.FondoMutuo.BD::FMM.InterfazAgf", "orden": "\"IdInterfazAgf\""},
]

# id -> (instrument, fund, issuer, foreign, fund country, issuer country, currency, (aporte ret, vc, cert, curse), price)
SERIES = {
    "FM002A": (901, "11", "AGF2", "N", 56, 56, "CLP", (1, 2, 1, 0), 1200.5),
    "FMX01U": (910, "20", "EXT1", "S", 840, 840, "USD", (1, 1, 2, 0), 15.25),
    "FMX02U": (911, "21", "EXT1", "S", 840, 840, "USD", (1, 1, 2, 0), 22.75),
    "FMX03U": (912, "22", "EXT2", "S", 840, 840, "USD", (1, 1, 2, 0), 9.5),
    "FMN01U": (913, "23", "AGF3", "N", 56, 56, "USD", (0, 1, 0, 0), 30.5),
    "FMN02U": (914, "24", "AGF3", "N", 56, 56, "USD", (0, 1, 0, 0), 31.25),
    "FMN03U": (915, "25", "AGF3", "N", 56, 56, "USD", (0, 1, 0, 2), 29.75),
}
PRECIO_BASE = ("FM001A", 900, "CLP", 1500.1234)
TODAS = ["FM001A"] + list(SERIES)

# regime id -> (group, PermiteAporte)
REGIMENES = {"18Q": ("DC", "S"), "APVA": ("APV", "S"), "A108": ("ART108", "S"), "A107": ("ART107", "S"), "R57B": ("57BIS", "S"), "NOAP": ("DC", "N")}
# portfolio -> code
PORTAFOLIOS = {501: "APV-A", 502: "BF", 503: "DC-DL", 504: "DC-AL", 505: "DC-R", 506: "apv-a", 507: "PFNEW"}

APORTE = {"IdInterno": 1, "IdCliente": 100, "IdPortafolio": 500, "IdSerieFondoOrigen": "FM001A", "IdEmisorOrigen": "AGF1", "CostoUF": 0.5, "FechaOrigenInversion": "2026-01-15",
          "MontoOrigen": 100000, "IndicadorMLT": "N", "IdSerieFondo": "FM002A", "Monto": 100000, "NumeroCuotas": 0, "IdMoneda": "CLP", "IdMedioDePago": "CIN", "Visado": "S",
          "SolicitudBatch": "N", "OrigenOperacion": "M", "IdRegimenTributario": "GEN", "Glosa": "reinversion", "IdAutorizadoOrigen": 0, "FolioContrato": 0,
          "IdSolicitudAporteRescateRelacionado": 9001, "FechaEmision": "2026-03-02", "Porcentaje": 100, "IdAgrupador": 50, "Switch": "N"}

# what Certificar sends (CertificarSolicitudAporteRescate.java:2385-2442, 2464-2520): the full list of fields of the first branch, nothing else
APORTE_CERTIFICAR = {"IdInterno": 1, "IdCliente": 100, "IdPortafolio": 500, "IdSerieFondoOrigen": "FM001A", "IdEmisorOrigen": "AGF1", "CostoUF": 0.25, "FechaOrigenInversion": "2026-02-27",
                     "MontoOrigen": 100000, "Monto": 100000, "IndicadorMLT": "N", "IdSerieFondo": "FM002A", "NumeroCuotas": 0, "IdMoneda": "CLP", "IdMedioDePago": "CIN",
                     "Visado": "S", "SolicitudBatch": "N", "OrigenOperacion": "M", "IdRegimenTributario": "GEN", "Glosa": "", "IdAutorizadoOrigen": 0, "FolioContrato": 7000,
                     "FechaEmision": "2026-03-02", "IdSolicitudAporteRescateRelacionado": 9001, "IdAgrupador": 50, "Switch": "N", "FechaCertificacionRescateOrigen": "2026-03-02",
                     "FechaLiquidacionRescateOrigen": "2026-03-02", "FechaValorCuotaRescateOrigen": "2026-03-02", "Canal": "AGE", "IngresoAutomatico": "N", "MontoTotalInversion": 100000}


def sol(base, **kw):
    d = copy.deepcopy(base)
    for k, v in kw.items():
        if v is KEY_AUSENTE:
            d.pop(k, None)
        else:
            d[k] = v
    return d


class _Ausente:
    def __repr__(self):
        return "KEY_AUSENTE"


KEY_AUSENTE = _Ausente()


def ins(tabla, cols):
    return insert(tabla, cols)


def upd(tabla, sets, donde="1=1"):
    return update(tabla, sets, donde)


def param(nombre, valor):
    return parametro(nombre, valor)


# ---------------------------------------------------------------------------------------------------------------------------------------------------------- schema helpers
_COLS = {}
_DOMINIOS = {}


def columnas(tabla):
    """[(name, domain)] of a CREATE TABLE of schema-fmm.sql, in order."""
    if not _COLS:
        texto = (BASE / "schema-fmm.sql").read_text(encoding="utf-8")
        for m in re.finditer(r'CREATE TABLE ("[^"]+")\s*\((.*?)\n\);', texto, re.S):
            _COLS[m.group(1)] = re.findall(r'^\s*"([^"]+)"\s+("[^"]+"|[A-Z]+(?:\(\d+(?:,\d+)?\))?)', m.group(2), re.M)
    return _COLS[tabla]


def dominios():
    if not _DOMINIOS:
        texto = (BASE / "schema-types.sql").read_text(encoding="utf-8")
        for m in re.finditer(r'CREATE DOMAIN ("[^"]+") AS ([A-Z]+)(\(\d+(?:,\d+)?\))?;', texto):
            _DOMINIOS[m.group(1)] = m.group(2)
    return _DOMINIOS


def valor_defecto(dom):
    tipo = dominios().get(dom, dom)
    if tipo in ("NVARCHAR", "VARCHAR", "NCHAR"):
        return ""
    if tipo == "DATE":
        return "0001-01-01"
    if tipo == "TIMESTAMP":
        return "0001-01-01 00:00:00"
    return 0


VISTA_SOLICITUD = '"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate"'


def fila_solicitud(tabla=None, **sobre):
    """INSERT INTO FMM.SolicitudAporteRescate (or the given table) with EVERY column: the default of its type (text '', number 0, date 0001-01-01) unless overridden (no defaults)."""
    tabla = tabla or SOLICITUD
    cols = columnas(tabla)
    vals = []
    for nombre, dom in cols:
        v = sobre[nombre] if nombre in sobre else valor_defecto(dom)
        if dominios().get(dom, dom) == "TIMESTAMP" and isinstance(v, str):
            vals.append("TIMESTAMP '%s'" % v)
        elif dominios().get(dom, dom) == "DATE" and isinstance(v, str):
            vals.append("DATE '%s'" % v)
        else:
            vals.append(lit(v))
    return "INSERT INTO %s (%s) VALUES (%s);" % (tabla, ", ".join('"%s"' % c for c, _ in cols), ", ".join(vals))


def rescate_relacionado(idr=9001, regimen="GEN", **sobre):
    d = {"IdSolicitudAporteRescate": idr, "IdTipoCanalIngreso": "AGE", "TipoSolicitud": "RES", "IdTipoMovimientoFondoMutuo": "RESREINV", "IdRegimenTributario": regimen,
         "IdCliente": 100, "IdPortafolio": 500, "IdSerieFondo": "FM001A", "IdEmpresa": 1, "IdUsuario": 7, "IdModulo": "FMM", "Estado": "CER", "IdMoneda": "CLP"}
    d.update(sobre)
    return fila_solicitud(**d)


def reinversion(idr, rescate, serie, regimen="GEN", final="2026-03-02", porcentaje=100, switch="N"):
    return ins(REINV, {"IdReinversionSerieFondo": idr, "IdSolicitudAporteRescate": rescate, "IdSerieFondoDestino": serie, "Porcentaje": porcentaje, "IdRegimenTributario": regimen,
                       "IdCarga": 0, "Switch": switch, "PorcentajeReal": porcentaje, "FechaFinalReinversion": final})


def clonar(tabla, clave, valor, **sobre):
    """INSERT INTO t SELECT (copy of the row t.clave = valor, with the given columns replaced by literals)."""
    cols = [c for c, _ in columnas(tabla)]
    sel = ", ".join(lit(sobre[c]) if c in sobre else '"%s"' % c for c in cols)
    return "INSERT INTO %s (%s) SELECT %s FROM %s WHERE \"%s\" = %s;" % (tabla, ", ".join('"%s"' % c for c in cols), sel, tabla, clave, lit(valor))


DICCIONARIO = {
    "NO_SE_INGRESARON_SOLICITUDES_APORTE": "No se ingresaron solicitudes de aporte",
    "UI_PATRIMONIO_MINIMO_INSUFICIENTE": "Patrimonio minimo insuficiente",
    "UI_SALDO_INSUFICIENTE_FLUJO_CAJA": "Saldo insuficiente en el flujo de caja",
    "NO_SE_INGRESARON_SOLICITUDES": "No se ingresaron solicitudes",
    "USUARIO_NO_EXISTE": "El usuario no existe",
    "USUARIO_NO_VIGENTE": "El usuario no esta vigente",
    "MODULO_NO_EXISTE": "El modulo no existe",
    "EMPRESA_NO_EXISTE": "La empresa no existe",
}


def base_csr():
    """seed/base-csr.sql: the FAKE rows every FMM-CSR case adds on top of seed/base-fmm.sql (loaded through esquemasExtra after schema-csr.sql)."""
    s = ["-- GENERATED by make_cases_csr.py (cases_csr_lib.base_csr). FAKE universe of the FMM-CSR cases, on top of seed/base-fmm.sql."]
    s.append(ins(PARAM, {"IdEmpresa": 1, "NombreTipoParametro": "APORTE_GENERA_COMPRA_SPOT_DOLAR", "ValorParametro": "N"}))
    # calendar: Saturday and Sunday are non working days of 56 and 840; 2026-03-05 (Thursday) is a holiday of 56
    s += fin_de_semana(56, 840)
    s.append(feriado("2026-03-05", 56))
    # date configuration: the five date types of the UNITARIO modality with every valuta and calendar flag open (so the days of the series reach the dates), and the BATCH copy
    s += cfg_borrar()
    orig = {"DIGITA": "", "CURSE": "DIGITA", "VCUOTA": "CURSE", "CERTIF": "CURSE", "LIQUID": "CURSE"}
    flags = {"DIGITA": {}, "CURSE": {"vcurse": "S"}, "VCUOTA": {"vvc": "S"}, "CERTIF": {"vcert": "S"}, "LIQUID": {"vret": "S"}}
    for modalidad in ("UNITARIO", "BATCH"):
        for t in ("DIGITA", "CURSE", "VCUOTA", "CERTIF", "LIQUID"):
            s.append(cfg(t, 0, modalidad=modalidad, origen=orig[t], ce="S", cf="S", cm="S", **flags[t]))
    for sid, (ins_, fondo, emisor, ext, pf, pe, mon, apo, precio) in SERIES.items():
        fo = {"IdEmisor": emisor, "IdPaisEmisor": pe, "IdPaisOrigen": pf, "Nemotecnico": sid[:5], "NombreFondo": "Fondo " + sid, "FondoExtranjero": ext, "IdMoneda": mon}
        se = {"IdInstrumento": ins_, "NombreSerieFondo": "Serie " + sid, "DiasRetencionesAporte": apo[0], "DiasValutaValorCuotaAporte": apo[1], "DiasValutaCertificacionAporte": apo[2],
              "DiasValutaCurseAporte": apo[3], "CambioFondo": "S"}
        s += clonar_serie(sid, fondo, fondo=fo, serie=se)
        s.append(clonar('"BSCL.FondoMutuo.BD::FMM.ValorCuota"', "IdSerieFondo", "FM001A", IdSerieFondo=sid, ValorCuota=precio))
        s.append(clonar('"BSCL.FondoMutuo.BD::FMM.ValorTipoCambio"', "IdSerieFondo", "FM001A", IdSerieFondo=sid))
    s.append(upd(SERIE, {"CambioFondo": "S"}, "\"IdSerieFondo\" = 'FM001A'"))
    # regimes, valid for every series and for the proposito of the portfolios
    for rid, (grupo, permite) in REGIMENES.items():
        s.append(clonar(REGIMEN, "IdRegimenFondoMutuo", "GEN", IdRegimenFondoMutuo=rid, GrupoRegimen=grupo, PermiteAporte=permite))
        s.append(ins(PROPOSITO, {"IdRegimenFondoMutuo": rid, "IdPropositoPortafolio": "PROPGEN"}))
    for sid in TODAS:
        for rid in (list(REGIMENES) if sid == "FM001A" else ["GEN"] + list(REGIMENES)):
            s.append(ins(REGSERIE, {"IdSerieFondo": sid, "IdRegimenFondoMutuo": rid, "IdEmpresa": 1, "IdEstado": "ACT"}))
    # portfolios of the client
    for pid, codigo in PORTAFOLIOS.items():
        s.append(ins(PORTAFOLIO, {"IdPortafolio": pid, "CodigoPortafolio": codigo, "IdCliente": 100, "IdEstado": "ACT", "Proposito": "PROPGEN"}))
        if pid != 507:
            s.append(ins(IND, {"IdCliente": 100, "IdPortafolio": pid, "ValorIndicador": "S", "CodigoTipoIndicador": "GEN_OCCINV"}))
    for tm, ind, op in (("APOREINV", "APO", "APORFM"), ("RESREINV", "RES", "RESCFM")):
        s.append(ins(TIPOMOV, {"IdTipoMovimientoFondoMutuo": tm, "Descripcion": tm, "IndicadorAporteRescate": ind, "IdTipoOperacion": op, "TieneCarga": "", "TieneIngresoManual": "",
                               "TieneCertificacion": "", "IdEstado": "ACT", "GeneraOmnibus": ""}))
    for cod, txt in DICCIONARIO.items():
        s.append(ins(DICC, {"CodLenguaje": "es", "CodDiccionario": cod, "Descripcion": txt}))
    # users: 8 back office profile, 9 without profile
    s.append(ins('"BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"', {"Key": "PERFIL_BACKOFFICE", "Value": "6"}))
    for uid in (8, 9):
        s.append(ins(USUARIO, {"IdUsuario": uid, "IdEmpresa": 1, "IdEstado": "ACT", "TipoEntidad": "EMP", "IdEntidad": 70 + uid}))
    s.append(ins('"BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario"', {"IdPerfil": 6, "IdUsuario": 8}))
    # the related rescues and the destination rows of the reinversion (the AR reads them; they are NOT written by the cases that do not say so)
    s.append(rescate_relacionado(9001, "GEN"))
    s.append(rescate_relacionado(9002, "18Q"))
    s.append(reinversion(1, 9001, "FM002A", "GEN"))
    s.append(reinversion(2, 9001, "FMX01U", "GEN"))
    s.append(reinversion(3, 9002, "FM002A", "18Q"))
    return "\n".join(s) + "\n"


# ---------------------------------------------------------------------------------------------------------------------------------------------------------- collaborators
def precios(pares):
    """EntregarPreciosTasas: one price per IdRegistro of the series given in pares {IdRegistro: serie} (CalcularMontoNumeroCuota)."""
    filas = []
    for reg, sid in pares.items():
        if sid == "FM001A":
            _s, ins_, mon, precio = PRECIO_BASE
        else:
            ins_, _f, _e, _x, _pf, _pe, mon, _a, precio = SERIES[sid]
        filas.append({"IdRegistro": reg, "IdClasificacionTipoInstrumento": "FM", "IdTipoInstrumento": "FM", "IdInstrumento": ins_, "Nemotecnico": sid, "Precio": precio,
                      "CodigoFuente": "OTR", "FechaFuente": "2026-03-02 00:00:00.000", "IdMoneda": mon})
    return {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": filas}}}


def custodia(*filas):
    """ConsultarFlujoCustodia: (instrument, regime, quantity) rows of portfolio 500 / client 100."""
    salida = [{"IdCliente": 100, "IdPortafolio": 500, "IdInstrumento": i, "IdRegimenFondoMutuo": r, "Cantidad": q, "MontoAMercadoMonedaInstrumento": q * 1000} for (i, r, q) in filas]
    return {"ConsultarFlujoCustodia": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "p_out_FlujoTotalizado": salida}}}


def paridad(ids, factor=900, monto_total=None):
    """CalcularParidad (scripted): trabajo 1 = one row per IdInterno (MontoDestino = id * 7000.25 unless monto_total {id: value}), trabajo 2 = rows IdInterno*4 + 1 (system currency),
    +2 (UF amount) and +3 (UF factor) as CalcularMontoNumeroCuota numbers them, for every id given."""
    t1, t2 = [], []
    for i in ids:
        mt = (monto_total or {}).get(i, i * 7000 + 0.25)
        t1.append({"IdTrabajo": 1, "IdInterno": i, "FactorCambioLocal": factor, "FactorOrigenDestino": factor, "MontoDestino": mt})
        t2.append({"IdTrabajo": 2, "IdInterno": i * 4 + 1, "FactorCambioLocal": factor, "FactorOrigenDestino": factor, "MontoDestino": i * 10000 + 0.5})
        t2.append({"IdTrabajo": 2, "IdInterno": i * 4 + 2, "MontoDestino": 38.5})
        t2.append({"IdTrabajo": 2, "IdInterno": i * 4 + 3, "FactorOrigenDestino": 39000, "MontoDestino": 39000})
    return {"CalcularParidad": {"respuestas": [{"cuando": {"0.p_IdTrabajo": 1}, "respuesta": {"p_out_CalculosSalida": t1}},
                                               {"cuando": {"0.p_IdTrabajo": 2}, "respuesta": {"p_out_CalculosSalida": t2}}],
                                "defecto": {"p_out_CalculosSalida": []}}}


def saldo(monto, portafolio=500, moneda="CLP", fecha="2026-03-02"):
    """SaldoFlujoCaja: one balance row (the legacy compares portfolio, currency and FechaSaldo = FechaCertificacion of the request)."""
    return {"SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": [{"IdPortafolio": portafolio, "IdMoneda": moneda, "FechaSaldo": fecha, "Saldo": monto}]}}}


def saldos(monto=50000000, portafolios=(500, 501, 502, 503, 504, 505, 506, 507), monedas=("CLP", "USD"), dias=range(2, 10)):
    """SaldoFlujoCaja: a balance row per portfolio, currency and day 2026-03-02..09 (the legacy keeps the LAST row that matches portfolio, currency and FechaSaldo = FechaCertificacion)."""
    filas = [{"IdPortafolio": p, "IdMoneda": m, "FechaSaldo": "2026-03-%02d" % d, "Saldo": monto} for p in portafolios for m in monedas for d in dias]
    return {"SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": filas}}}


def productos(portafolios=(500, 501, 502, 503, 504, 505, 506, 507)):
    """ConsultarProductosPortafolio: every portfolio of the universe operates the product INFM (the requirement FM-REQ of the closure)."""
    filas = [{"IdPortafolio": p, "IdPerfilProductos": "PERF1", "IdEstadoPerfilProducto": "ACT", "IdEstadoDetallePerfilProducto": "ACT", "IdTipoProducto": "INFM", "IdTipoOperacion": "0",
              "IdTipoCanalIngreso": "OPT"} for p in portafolios]
    return {"ConsultarProductosPortafolio": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "p_out_Mensajes": [], "p_out_PortafolioProductos": filas}}}


def colab(*dicts):
    out = {}
    for d in dicts:
        out.update(copy.deepcopy(d))
    return out


def ids_paridad(solicitudes):
    return [s["IdInterno"] for s in solicitudes]


def precios_de(solicitudes):
    """The price table of the series of the contributions: registro = IdInterno."""
    return precios({s["IdInterno"]: s["IdSerieFondo"] for s in solicitudes if s.get("IdSerieFondo") in TODAS})


BENIGNOS = {
    "ProcesarIngresoEgresoTesoreria": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_MENSAJES": [], "P_OUT_INGRESOEGRESOGENERADO": [], "P_OUT_RESULTADOPROCESO": []}},
    "ProcesarIngresoOperacionSpot": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_MENSAJES": [], "P_OUT_IDOPERACIONSPOT": []}},
}


def base_colab(solicitudes, **kw):
    """The collaborators every real case needs: price (registro = IdInterno), custody 1000 of instrument 900 / 901 regime GEN, parity of every id, and the benign answers of the two
    FondoMutuo helpers that the AR reaches behind ports (Tesoreria, Spot: empty results)."""
    return colab(precios_de(solicitudes), custodia((900, "GEN", 1000), (901, "GEN", 1000)), paridad(ids_paridad(solicitudes), **kw), BENIGNOS, productos(), saldos())


def caso(slug, ref, aportes, seed=None, colaboradores=None, entrada_extra=None, entrada=None, tablas=None, **cab):
    """Writes cases-csr/FMM-CSR-nnn_slug (nnn = order of the call) and returns the id. cab = keys of the request (p_IdUsuario ...) and, when it is one of
    reloj / sinColaboradores, of the case header."""
    _n["n"] += 1
    cid = "FMM-CSR-%03d_%s" % (_n["n"], slug)
    if entrada is None:
        entrada = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_SimularOperacion": "N", "p_UsaReglasNegocio": "S", "p_IdTipoCanalIngreso": "AGE",
                   "p_SolicitudesAporte": aportes}
    entrada = dict(entrada)
    for k, v in list(cab.items()):
        if k in ("reloj",):
            continue
        if v is KEY_AUSENTE:
            entrada.pop(k, None)
        else:
            entrada[k] = v
    if entrada_extra:
        entrada.update(entrada_extra)
    doc = {"servicio": "CrearSolicitudAporteReinversion", "ref": ref, "esquemasExtra": ["schema-csr.sql", "seed/base-csr.sql"], "entrada": entrada, "tablas": tablas or TABLAS}
    if "reloj" in cab:
        doc["reloj"] = cab["reloj"]
    if colaboradores:
        doc["colaboradores"] = colaboradores
    d = CASES / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    texto = "-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)\n" + ("\n".join(x for x in seed if x) if seed else "SELECT 1;") + "\n"
    (d / "seed.sql").write_text(texto, encoding="utf-8", newline="\n")
    return cid
