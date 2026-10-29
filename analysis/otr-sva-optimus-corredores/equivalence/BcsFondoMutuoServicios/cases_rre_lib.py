"""Helpers of the F2 W1 front 2 cases (CrearSolicitudRescateReinversion, JavaCall 1436): case directories cases/FMM-RRE-nn_slug.

The case inputs (case.json, seed.sql) are written here; the goldens (expected.json) are RECORDED by legacy-oracle/record_rre.sh from the unmodified
legacy, never by this script. Every case carries "rescateReinversionReal": true (record.sh never runs it) and loads schema-rre.sql + seed/base-rre.sql.
FAKE universe on top of seed/base-fmm.sql (one client 100, portfolio 500, series FM001A national CLP instrument 900):
  FM002A national CLP, other issuer (AGF2), instrument 901        FMX01U foreign USD issuer EXT1, instrument 910
  FMX02U foreign USD, SAME issuer EXT1 (SWITCH), instrument 911   FMX03U foreign USD, issuer EXT2 (INTERNACIONAL), instrument 912
"""
import copy
import json
import pathlib
import re

BASE = pathlib.Path(__file__).resolve().parent
CASES = BASE / "cases"

SERIE = '"BSCL.FondoMutuo.BD::FMM.SerieDeFondo"'
FONDO = '"BSCL.FondoMutuo.BD::FMM.Fondo"'
REGSERIE = '"BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo"'
REGIMEN = '"BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"'
PARAM = '"BSCL.FondoMutuo.BD::FMM.Parametro"'
PLACONF = '"BSCL.Plataforma.BD::PLA.Configuracion"'
LOCK = '"BSCL.FondoMutuo.BD::FMM.ProcesarReinversionRescate_BloqueoReinversion"'
CONFREG = '"BSCL.FondoMutuo.BD::FMM.ConfiguracionRegimenFondoMutuo"'
MANDATO = '"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimenSerieDeFondo"'
PROPOSITO = '"BSCL.FondoMutuo.BD::FMM.ConfiguracionPropositoPortafolio"'
FECHAS = '"BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo"'

TABLAS = [
    {"nombre": "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate", "orden": "\"IdSolicitudAporteRescate\""},
    {"nombre": "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino", "orden": "\"IdReinversionSerieFondo\""},
    {"nombre": "BSCL.FondoMutuo.BD::FMM.ProcesarReinversionRescate_BloqueoReinversion", "orden": "\"IdBloqueaReinversion\", \"IdSerieFondo\""},
    {"nombre": "BSCL.FondoMutuo.BD::FMM.InterfazAgf", "orden": "\"IdInterfazAgf\""},
]

# series of the universe: id -> (instrument, fund, currency, issuer, foreign, price)
SERIES = {
    "FM001A": (900, "10", "CLP", "AGF1", "N", 1500.1234),
    "FM002A": (901, "11", "CLP", "AGF2", "N", 1200.5),
    "FMX01U": (910, "20", "USD", "EXT1", "S", 15.25),
    "FMX02U": (911, "21", "USD", "EXT1", "S", 22.75),
    "FMX03U": (912, "22", "USD", "EXT2", "S", 9.5),
}

RESCATE = {"IdInterno": 1, "IdCliente": 100, "IdPortafolio": 500, "IdSerieFondo": "FM001A", "CalculoPorMontoCuota": "C", "Monto": 0, "NumeroCuotas": 10,
           "IdMoneda": "CLP", "IdMedioDePago": "CIN", "SolicitudBatch": "N", "OrigenOperacion": "M", "Glosa": "", "IdAutorizadoOrigen": 0,
           "FolioContrato": 0, "IdRegimenTributario": "GEN"}
APORTE = {"IdInterno": 1, "IdCliente": 100, "IdPortafolio": 500, "IdSerieFondo": "FM002A", "IdEmisor": "AGF2", "Monto": 0, "NumeroCuotas": 0,
          "IndicadorMLT": "N", "IdMoneda": "CLP", "IdMedioDePago": "CIN", "IdRegimenTributario": "GEN", "Glosa": "", "FolioContrato": 0}


def sol(base, **kw):
    d = copy.deepcopy(base)
    d.update(kw)
    return d


def lit(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "TRUE" if v else "FALSE"
    if isinstance(v, (int, float)):
        return repr(v)
    return "'" + str(v).replace("'", "''") + "'"


def upd(tabla, sets, donde="1=1"):
    return "UPDATE %s SET %s WHERE %s;" % (tabla, ", ".join('"%s" = %s' % (k, lit(v)) for k, v in sets.items()), donde)


def ins(tabla, cols):
    return "INSERT INTO %s (%s) VALUES (%s);" % (tabla, ", ".join('"%s"' % k for k in cols), ", ".join(lit(v) for v in cols.values()))


def param(nombre, valor):
    return upd(PARAM, {"ValorParametro": valor}, "\"NombreTipoParametro\" = '%s'" % nombre)


_COLS = {}


def columnas(tabla):
    """Column names of a CREATE TABLE of schema-fmm.sql, in order (to clone a row with INSERT ... SELECT)."""
    if not _COLS:
        texto = (BASE / "schema-fmm.sql").read_text(encoding="utf-8")
        for m in re.finditer(r'CREATE TABLE ("[^"]+")\s*\((.*?)\n\);', texto, re.S):
            _COLS[m.group(1)] = re.findall(r'^\s*"([^"]+)"\s', m.group(2), re.M)
    return _COLS[tabla]


def clonar(tabla, clave, valor, **sobre):
    """INSERT INTO t SELECT (copy of the row t.clave = valor, with the given columns replaced by literals)."""
    cols = columnas(tabla)
    sel = ", ".join(lit(sobre[c]) if c in sobre else '"%s"' % c for c in cols)
    return "INSERT INTO %s (%s) SELECT %s FROM %s WHERE \"%s\" = %s;" % (tabla, ", ".join('"%s"' % c for c in cols), sel, tabla, clave, lit(valor))


DICCIONARIO = {
    "NO_SE_INGRESARON_SOLICITUDES_RESCATE": "No se ingresaron solicitudes de rescate",
    "ERROR_VALOR_CUOTA": "Error de valor cuota de ::IdSerieFondo::",
    "UI_ERROR_BLOQUEO_REINVERSION": "Error al bloquear la reinversion",
    "UI_ERROR_DELETE_BLOQUEO_REINVERSION": "Error al eliminar el bloqueo de reinversion",
    "UI_PATRIMONIO_MINIMO_INSUFICIENTE": "Patrimonio minimo insuficiente de ::IdInstrumento::",
    "UI_REINVERSION_1N_CREADA": "Reinversion ::secuencia:: creada, folio ::folioAgrupador::",
}


def base_rre():
    """seed/base-rre.sql: the FAKE rows every FMM-RRE case adds on top of seed/base-fmm.sql (loaded through esquemasExtra after schema-rre.sql)."""
    s = ["-- GENERATED by make_cases_rre.py (cases_rre_lib.base_rre). FAKE universe of the FMM-RRE cases, on top of seed/base-fmm.sql."]
    for sid, (ins_, fondo, mon, emisor, ext, precio) in SERIES.items():
        if sid != "FM001A":
            pais = 840 if ext == "S" else 56
            s.append(clonar(FONDO, "IdFondo", "10", IdFondo=fondo, IdEmisor=emisor, IdPaisEmisor=pais, IdPaisOrigen=pais, Nemotecnico=sid[:5],
                            NombreFondo="Fondo " + sid, FondoExtranjero=ext, IdMoneda=mon))
            s.append(clonar(SERIE, "IdSerieFondo", "FM001A", IdSerieFondo=sid, IdInstrumento=ins_, IdFondo=fondo, NombreSerieFondo="Serie " + sid,
                            DecimalesCuota=4 if mon == "CLP" else 4))
            s.append(clonar(REGSERIE, "IdSerieFondo", "FM001A", IdSerieFondo=sid))
            s.append(clonar('"BSCL.FondoMutuo.BD::FMM.ValorCuota"', "IdSerieFondo", "FM001A", IdSerieFondo=sid, ValorCuota=precio))
            s.append(clonar('"BSCL.FondoMutuo.BD::FMM.ValorTipoCambio"', "IdSerieFondo", "FM001A", IdSerieFondo=sid))
    for tm, ind, op in (("RESREINV", "RES", "RESCFM"), ("APOREINV", "APO", "APORFM")):
        s.append(ins('"BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo"', {"IdTipoMovimientoFondoMutuo": tm, "Descripcion": tm, "IndicadorAporteRescate": ind,
                 "IdTipoOperacion": op, "TieneCarga": "", "TieneIngresoManual": "", "TieneCertificacion": "", "IdEstado": "ACT", "GeneraOmnibus": ""}))
    for cod, txt in DICCIONARIO.items():
        s.append(ins('"BSCL.Plataforma.BD::PLA.Diccionario"', {"CodLenguaje": "es", "CodDiccionario": cod, "Descripcion": txt}))
    s.append(upd(SERIE, {"CambioFondo": "S"}))   # a reinversion is a change of fund: every series of the universe allows it (cases turn it off)
    return "\n".join(s) + "\n"


def precios(pares):
    """EntregarPreciosTasas: one price per IdRegistro (rescue = IdInterno, contribution = -IdInterno) of the series given in pares {id: serie}."""
    filas = []
    for reg, sid in pares.items():
        ins_, _f, mon, _e, _x, precio = SERIES[sid]
        filas.append({"IdRegistro": reg, "IdClasificacionTipoInstrumento": "FM", "IdTipoInstrumento": "FM", "IdInstrumento": ins_, "Nemotecnico": sid,
                      "Precio": precio, "CodigoFuente": "OTR", "FechaFuente": "2026-03-02 00:00:00.000", "IdMoneda": mon})
    return {"EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": filas}}}


def custodia(*filas):
    """ConsultarFlujoCustodia: (instrument, regime, quantity) rows of portfolio 500 / client 100."""
    salida = [{"IdCliente": 100, "IdPortafolio": 500, "IdInstrumento": i, "IdRegimenFondoMutuo": r, "Cantidad": q, "MontoAMercadoMonedaInstrumento": q * 1000}
              for (i, r, q) in filas]
    return {"ConsultarFlujoCustodia": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "p_out_FlujoTotalizado": salida}}}


def paridad(ids, factor=900):
    """CalcularParidad (scripted): trabajo 1 = one row per IdInterno (cross-currency amount), trabajo 2 = rows IdInterno*4 + s*1 (system currency),
    s*2 (UF amount) and s*3 (UF factor) as CalcularMontoNumeroCuota numbers them, for every id given (rescues positive, contributions negative)."""
    t1, t2 = [], []
    for i in ids:
        s = 1 if i > 0 else -1
        t1.append({"IdTrabajo": 1, "IdInterno": i, "FactorCambioLocal": factor, "FactorOrigenDestino": factor, "MontoDestino": abs(i) * 7000 + 0.25})
        t2.append({"IdTrabajo": 2, "IdInterno": i * 4 + s, "FactorCambioLocal": factor, "FactorOrigenDestino": factor, "MontoDestino": abs(i) * 10000 + 0.5})
        t2.append({"IdTrabajo": 2, "IdInterno": i * 4 + 2 * s, "MontoDestino": 38.5})
        t2.append({"IdTrabajo": 2, "IdInterno": i * 4 + 3 * s, "FactorOrigenDestino": 39000, "MontoDestino": 39000})
    return {"CalcularParidad": {"respuestas": [{"cuando": {"0.p_IdTrabajo": 1}, "respuesta": {"p_out_CalculosSalida": t1}},
                                               {"cuando": {"0.p_IdTrabajo": 2}, "respuesta": {"p_out_CalculosSalida": t2}}],
                                "defecto": {"p_out_CalculosSalida": []}}}


def colab(*dicts):
    out = {}
    for d in dicts:
        out.update(copy.deepcopy(d))
    return out


def caso(cid, ref, rescates, aportes, seed=None, colaboradores=None, servicio="CrearSolicitudRescateReinversion", sin_aportes=False, entrada_extra=None, **cab):
    if servicio == "CrearSolicitudRescateReinversion":
        entrada = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_SimularOperacion": "N", "p_UsaReglasNegocio": "S",
                   "p_IdTipoCanalIngreso": "AGE", "p_SolicitudesRescate": rescates}
        if not sin_aportes:
            entrada["p_SolicitudesAporte"] = aportes
    else:
        entrada = dict(entrada_extra or {})
    entrada.update(cab)
    doc = {"servicio": servicio, "ref": ref, "rescateReinversionReal": True, "esquemasExtra": ["schema-rre.sql", "seed/base-rre.sql"],
           "entrada": entrada, "tablas": TABLAS}
    if colaboradores:
        doc["colaboradores"] = colaboradores
    d = CASES / cid
    d.mkdir(parents=True, exist_ok=True)
    (d / "case.json").write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    texto = "-- case seed on top of seed/base-fmm.sql + seed/base-rre.sql (fake data)\n" + ("\n".join(seed) if seed else "SELECT 1;") + "\n"
    (d / "seed.sql").write_text(texto, encoding="utf-8", newline="\n")
