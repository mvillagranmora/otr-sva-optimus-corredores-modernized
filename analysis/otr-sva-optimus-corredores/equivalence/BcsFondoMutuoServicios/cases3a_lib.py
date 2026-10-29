"""Helpers of the step-3A case generator (make_cases_3a.py): SQL seed fragments and the case writer. The cases are INPUTS (case.json + seed.sql);
the expected values are never written here: legacy-oracle/record3a.sh records expected.json by executing the unmodified legacy classes."""
import json
import pathlib

BASE = pathlib.Path(__file__).resolve().parent
OUT = BASE / "cases3a"

FONDO = '"BSCL.FondoMutuo.BD::FMM.Fondo"'
SERIE = '"BSCL.FondoMutuo.BD::FMM.SerieDeFondo"'
TIPOFONDO = '"BSCL.FondoMutuo.BD::FMM.TipoFondo"'
TIPOCAMBIO = '"BSCL.FondoMutuo.BD::FMM.ValorTipoCambio"'
CONFFECHAS = '"BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo"'
PARAM = '"BSCL.FondoMutuo.BD::FMM.Parametro"'
CONFIG = '"BSCL.Plataforma.BD::PLA.Configuracion"'
FERIADO = '"BSCL.Plataforma.BD::REG.DiaFeriado"'
MONEDAFMT = '"BSCL.Plataforma.BD::PLA.MonedaFormato"'
TRAMO = '"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTramoComision"'
COMMAX = '"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarComisionMaxima"'

_contador = {"n": 0}


def lit(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "TRUE" if v else "FALSE"
    if isinstance(v, (int, float)):
        return repr(v)
    s = str(v)
    if len(s) == 10 and s[4] == "-" and s[7] == "-" and s.replace("-", "").isdigit():
        return "DATE '%s'" % s
    return "'" + s.replace("'", "''") + "'"


def update(tabla, sets, donde):
    return "UPDATE %s SET %s WHERE %s;" % (tabla, ", ".join('"%s" = %s' % (k, lit(v)) for k, v in sets.items()), donde)


def insert(tabla, valores):
    return "INSERT INTO %s (%s) VALUES (%s);" % (tabla, ", ".join('"%s"' % k for k in valores), ", ".join(lit(v) for v in valores.values()))


def parametro(nombre, valor):
    """Replaces a FONDOMUTUO parameter (valor None deletes it: the legacy then raises or uses its default)."""
    s = ['DELETE FROM %s WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = %s;' % (PARAM, lit(nombre))]
    if valor is not None:
        s.append(insert(PARAM, {"IdEmpresa": 1, "NombreTipoParametro": nombre, "ValorParametro": valor}))
    return s


def config_plataforma(codigo, valor):
    s = ['DELETE FROM %s WHERE "IdEmpresa" = 1 AND "CodigoParametro" = %s;' % (CONFIG, lit(codigo))]
    if valor is not None:
        s.append(insert(CONFIG, {"IdEmpresa": 1, "CodigoParametro": codigo, "Valor": valor}))
    return s


def feriado(fecha, pais, estado="ACT"):
    _contador["n"] += 1
    return insert(FERIADO, {"IdFeriado": 5000 + _contador["n"], "Tipo": "FERIADO", "Fecha": fecha, "Dia": 0, "DescripcionDia": "feriado ficticio",
                            "AgnoCarga": int(fecha[:4]), "IdPais": pais, "IdEstado": estado})


def inhabil(dia, pais, estado="ACT"):
    _contador["n"] += 1
    return insert(FERIADO, {"IdFeriado": 5000 + _contador["n"], "Tipo": "INHABIL", "Fecha": "0001-01-01", "Dia": dia, "DescripcionDia": "dia inhabil",
                            "AgnoCarga": 2026, "IdPais": pais, "IdEstado": estado})


def fin_de_semana(*paises):
    """Saturday (5) and Sunday (6) are non working days of every given country (the optimus day: Monday 0)."""
    s = []
    for p in paises:
        s.append(inhabil(5, p))
        s.append(inhabil(6, p))
    return s


def cfg_borrar():
    return ['DELETE FROM %s WHERE "IdEmpresa" = 1;' % CONFFECHAS]


def cfg(tipo_fecha, dias, modalidad="UNITARIO", habil="S", origen="", debe="N", vcurse="N", vvc="N", vcert="N", vret="N", cl="N", ce="N", cf="N", cm="N",
        tipo_sol=None, estado=None, fext=None, estado_reg="ACT"):
    return insert(CONFFECHAS, {"IdEmpresa": 1, "IdModalidad": modalidad, "IdTipoFecha": tipo_fecha, "TipoSolicitud": tipo_sol, "EstadoFondo": estado,
                               "FondoExtranjero": fext, "Descripcion": tipo_fecha, "CantidadDias": dias, "EsHabil": habil, "IdTipoFechaOrigen": origen,
                               "DebeSerHabilFechaOrigen": debe, "ValutaCurse": vcurse, "ValutaValorCuota": vvc, "ValutaCertificacion": vcert,
                               "ValutaRetencion": vret, "CalendarioLocal": cl, "CalendarioEmisor": ce, "CalendarioFondo": cf, "CalendarioMoneda": cm,
                               "IdEstado": estado_reg})


def cfg_completa(modalidad="UNITARIO", dias=None, **flags):
    """The five date types of a modality: DIGITA 0, CURSE 0, VCUOTA 0, CERTIF 1, LIQUID 1 unless overridden in dias={'CERTIF': 2}."""
    d = {"DIGITA": 0, "CURSE": 0, "VCUOTA": 0, "CERTIF": 1, "LIQUID": 1}
    d.update(dias or {})
    orig = {"DIGITA": "", "CURSE": "DIGITA", "VCUOTA": "CURSE", "CERTIF": "CURSE", "LIQUID": "CURSE"}
    return [cfg(t, d[t], modalidad=modalidad, origen=orig[t], **flags.get(t, {})) for t in ("DIGITA", "CURSE", "VCUOTA", "CERTIF", "LIQUID")]


def tipo_fondo(id_tipo, money_market="N"):
    return insert(TIPOFONDO, {"IdTipoFondo": id_tipo, "NombreCorto": id_tipo, "Descripcion": "tipo " + id_tipo, "IndicadorMoneyMarket": money_market})


def clonar_serie(id_serie, id_fondo, fondo=None, serie=None, tipo_cambio=None):
    """A new series (and its fund) copied from FM001A / fund '10' with the given column overrides; optionally its observed-dollar rates."""
    fo = dict(fondo or {})
    fo["IdFondo"] = id_fondo
    se = dict(serie or {})
    se["IdSerieFondo"] = id_serie
    se["IdFondo"] = id_fondo
    s = []
    if fondo is not False:   # fondo=False: the series points to a fund that does not exist (the LEFT JOIN of the legacy SQL returns nulls)
        s += ['CREATE LOCAL TEMPORARY TABLE "tmp_fondo" AS SELECT * FROM %s WHERE "IdFondo" = \'10\';' % FONDO,
              update('"tmp_fondo"', fo, "1 = 1"),
              "INSERT INTO %s SELECT * FROM \"tmp_fondo\";" % FONDO,
              'DROP TABLE "tmp_fondo";']
    s += ['CREATE LOCAL TEMPORARY TABLE "tmp_serie" AS SELECT * FROM %s WHERE "IdSerieFondo" = \'FM001A\';' % SERIE,
          update('"tmp_serie"', se, "1 = 1"),
          "INSERT INTO %s SELECT * FROM \"tmp_serie\";" % SERIE,
          'DROP TABLE "tmp_serie";']
    if tipo_cambio is not None:
        s.append(insert(TIPOCAMBIO, {"IdSerieFondo": id_serie, "IdEmpresa": 1, "ValorDolarAporte": tipo_cambio[0], "ValorDolarRescate": tipo_cambio[1]}))
    return s


def moneda_formato(id_moneda, decimales=2, modo="R", id_formato=None):
    _contador["n"] += 1
    v = {"IdMonedaFormato": id_formato or 100 + _contador["n"], "IdMoneda": id_moneda, "IdUsoMonedaFormato": "GEN", "IdTipoInstrumento": "", "IdTipoOperacion": "",
         "PrecioDecimales": 4, "PrecioTrucaRedondea": "R", "TasaDecimales": 4, "TasaTrucaRedondea": "R", "CantidadDecimales": 4, "CantidadTrucaRedondea": "R",
         "MontoDecimales": decimales, "MontoTrucaRedondea": modo, "TipoCambioDecimales": 2, "TipoCambioTrucaRedondea": "R", "ParidadDecimales": 4,
         "ParidadTrucaRedondea": "R", "PuntosDecimales": 2, "PuntosTrucaRedondea": "R", "IdEstado": "ACT"}
    return insert(MONEDAFMT, v)


def tramo(id_tramo, id_serie, tipo, desde, hasta, porcentaje, id_comision=1, estado_tramo="ACT", estado_comision="ACT"):
    return [insert(TRAMO, {"IdTramo": id_tramo, "IdComision": id_comision, "DiasDesde": desde, "DiasHasta": hasta, "PorcentajeComision": porcentaje,
                           "EstadoTramo": estado_tramo, "IdSerieFondo": id_serie, "Descripcion": "tramo", "TipoComision": tipo,
                           "FechaVigenciaDesde": "2020-01-01", "FechaVigenciaHasta": "2099-12-31", "PorcentajeLiberado": 0,
                           "EstadoComision": estado_comision, "IdEmpresa": 1})]


def comision_maxima(id_serie, id_comision=1):
    return insert(COMMAX, {"IdSerieFondo": id_serie, "IdComision": id_comision})


def caso(cid, clase, ref, entrada, seed=None, colaboradores=None, reloj=None):
    d = OUT / cid
    d.mkdir(parents=True, exist_ok=True)
    c = {"clase": clase, "ref": ref, "entrada": entrada}
    if colaboradores:
        c["colaboradores"] = colaboradores
    if reloj:
        c["reloj"] = reloj
    (d / "case.json").write_text(json.dumps(c, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    sql = [x for x in (seed or []) if x]
    if sql:
        (d / "seed.sql").write_text("\n".join(sql) + "\n", encoding="utf-8", newline="\n")
