"""Seed rows (INSERT text) for the step 3B cases: same column sets and values as seed/base-fmm.sql, with overrides."""
from cases3b_lib import sql

FONDO = dict(IdFondo="10", IdEmisor="AGF1", IdPaisEmisor=56, Nemotecnico="FM001", NombreFondo="Fondo Ficticio Uno", IdTipoFondo="ACC", IdPaisOrigen=56,
             CodigoSVS="", MoneyMarket="N", FechaCreacion="DATE", FechaIngresoRegistro="TS", FechaConstitucion="DATE", FechaCierre="DATE", Vigencia="",
             FondoExtranjero="N", IdMoneda="CLP", IdTipoInstrumento="FM", IdEmpresa=1, IdCategoriaPerfilFondoMutuo="", UsaHorarioAporteDefault="",
             UsaHorarioRescateDefault="", UsaHorarioReinversionDefault="", UsaHorarioLiquidezDefault="", EstadoAporteComercial="A",
             EstadoAporteBackoffice="A", EstadoRescateComercial="A", EstadoRescateBackoffice="A", EstadoReinversionComercial="A",
             EstadoReinversionBackoffice="A", EstadoLiquidezComercial="A", EstadoLiquidezBackoffice="A", BloqueoAporte="N", BloqueoRescate="N",
             BloqueoIndefinido="N", FechaBloqueoDesde="DATE", FechaBloqueoHasta="DATE", FechaFondo="2026-03-02", AperturaAutomaticaFondo="",
             FondoNormativo="", IdTipoDocumentoIdentidadFondo="", DocumentoIdentidadFondo="", IdMercadoFondoMutuo="0")

SERIE = dict(IdSerieFondo="FM001A", IdInstrumento=900, IdFondo="10", NombreSerieFondo="Serie A ficticia", CodigoSerie="A", Alias="", DecimalesValorCuota=4,
             DecimalesCuota=4, MontoMinimo=0, MontoMaximo=0, DiasRetencionesAporte=0, DiasRetencionesRescate=0, DiasValutaCurseAporte=0,
             DiasValutaCurseRescate=0, DiasValutaValorCuotaAporte=0, DiasValutaValorCuotaRescate=0, DiasValutaCertificacionAporte=0,
             DiasValutaCertificacionRescate=1, ExcluyeGarantia="", CobraComision="", Diferido="N", InstrumentoDCV="", Omnibus="", PatrimonioMinimo=0,
             IdSerieFondoOrigen="", FechaIngresoRegistro="TS", IdEstado="ACT", IdCliente=0, IdPortafolio=0, IdMedioDePagoAbono="", IdMedioDePagoCargo="",
             IdFamiliaFondo="", IdEmpresa=1, CambioFondo="", DiasComision=0, PorcentajeComision=0, PorcentajeRemuneracion=0, PorcentajeGastoOpera=0,
             PorcentajeCostoAdm=0, PatrimonioAltos="N", EsAPV="N", RestringeAporte="N", RestringeRescate="N", MontoPatrimonioEmisor=0)


class Raw(str):
    """a value written as is (DATE '...' / TIMESTAMP '...')"""


def _fix(d):
    out = {}
    for k, v in d.items():
        if v == "DATE":
            out[k] = Raw("DATE '0001-01-01'")
        elif v == "TS":
            out[k] = Raw("TIMESTAMP '2026-03-02 00:00:00'")
        else:
            out[k] = v
    return out


def _insert(table, cols):
    names = ", ".join('"%s"' % k for k in cols)
    vals = ", ".join(v if isinstance(v, Raw) else _lit(v) for v in cols.values())
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (table, names, vals)


def _lit(v):
    if v is None:
        return "NULL"
    if isinstance(v, (int, float)):
        return repr(v)
    return "'" + str(v).replace("'", "''") + "'"


def fondo(**ov):
    d = dict(FONDO)
    d.update(ov)
    return _insert("BSCL.FondoMutuo.BD::FMM.Fondo", _fix(d))


def serie(**ov):
    d = dict(SERIE)
    d.update(ov)
    return _insert("BSCL.FondoMutuo.BD::FMM.SerieDeFondo", _fix(d))


def cartera(cliente, emisor, moneda, cantidad, monto, nemo="FM001A", instr=900, portafolio=500, estado="VIG", fecha="2026-03-01", regimen=""):
    return sql("BSCL.Custodia.BD::CUS.Cartera", IdPortafolio=portafolio, IdInstrumento=instr, IdRegimenFondoMutuo=regimen, IdEstado=estado, IdCliente=cliente,
               IdEmisor=emisor, IdMonedaInstrumento=moneda, INGCantidad=cantidad, INGMontoMercadoActualMonedaInstrumento=monto, Nemotecnico=nemo,
               FechaLiquidacion=fecha)


def valor_cuota(serie_id, valor):
    return sql("BSCL.FondoMutuo.BD::FMM.ValorCuota", IdSerieFondo=serie_id, ValorCuota=valor)


def formato_moneda(id_, moneda, tipo_instrumento, tipo_operacion, decimales, trunca):
    """a PLA.MonedaFormato row (MONTO columns are the ones the cases read; the others like the CLP row)"""
    return sql("BSCL.Plataforma.BD::PLA.MonedaFormato", IdMonedaFormato=id_, IdMoneda=moneda, IdUsoMonedaFormato="GEN", IdTipoInstrumento=tipo_instrumento,
               IdTipoOperacion=tipo_operacion, PrecioDecimales=4, PrecioTrucaRedondea="R", TasaDecimales=4, TasaTrucaRedondea="R", CantidadDecimales=4,
               CantidadTrucaRedondea="R", MontoDecimales=decimales, MontoTrucaRedondea=trunca, TipoCambioDecimales=2, TipoCambioTrucaRedondea="R",
               ParidadDecimales=4, ParidadTrucaRedondea="R", PuntosDecimales=2, PuntosTrucaRedondea="R", IdEstado="ACT")


def serie_en_moneda(serie_id, instrumento, fondo_id, moneda):
    """a fondo + serie quoted in the given currency (issuer AGF1)"""
    return [fondo(IdFondo=fondo_id, IdMoneda=moneda, Nemotecnico="FX" + fondo_id), serie(IdSerieFondo=serie_id, IdInstrumento=instrumento, IdFondo=fondo_id)]


def regimen(id_, grupo):
    """a RegimenFondoMutuo of the given group, active, linked to the series FM001A"""
    return ['INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" ("IdRegimenFondoMutuo", "GrupoRegimen", "PropositoSerieRelacionado", '
            '"PermiteOtrosGrupos", "ValidaPortafolioCliente", "ValidaPortafolioAGF", "PermiteRescate", "TipoCarteraNormativa", "PermiteCambioGrupo", '
            '"RescateConMandato", "AceptaMandatoEnReinversion", "PermiteAporte", "PermiteTraspaso", "ValidaMontoMinimoInstrumento", "IdEstado") '
            "SELECT '%s', '%s', \"PropositoSerieRelacionado\", \"PermiteOtrosGrupos\", \"ValidaPortafolioCliente\", \"ValidaPortafolioAGF\", "
            '"PermiteRescate", "TipoCarteraNormativa", "PermiteCambioGrupo", "RescateConMandato", "AceptaMandatoEnReinversion", "PermiteAporte", '
            '"PermiteTraspaso", "ValidaMontoMinimoInstrumento", "IdEstado" FROM "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" '
            "WHERE \"IdRegimenFondoMutuo\" = 'GEN';" % (id_, grupo),
            sql("BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo", IdSerieFondo="FM001A", IdRegimenFondoMutuo=id_, IdEmpresa=1, IdEstado="ACT")]
