"""Writes seed/base-fmm.sql and seed/doubles-base.json: the FAKE universe shared by every case (invented ids, names and amounts; nothing from the real
system): empresa 1 (pais 56), usuario 7 with the comercial profile, one cliente 100 / portafolio 500 with a custodia of 1000 cuotas, one CLP fondo
10 with serie FM001A (instrumento 900), regimen GEN, calendar rules for the five valuta dates, a CLP formato, parameters of the FONDOMUTUO and
PLATAFORMA modules, and the dictionary texts. Everything here is a SEED (input); expected outputs are only ever recorded by the legacy."""
import json
import os

from seedlib import HERE, ins

OUT = []


def a(short, **v):
    OUT.append(ins(short, **v))


def config():
    for k, v in [("PLA.LocalizacionSistema", "es"), ("PLA.FechaSistema", "2026-03-02"), ("PLA.MonedaSistema", "CLP"), ("PLA.PaisSistema", "56"),
                 ("PLA.MonedaAdicional", "USD"), ("PLA.Moneda", "CLP"), ("PLA.NotificarEstadoOrdenOWP", "N"), ("PLA.SegundosZonaHoraria", "-10800"),
                 ("PLA.HorasOperaCINClienteNuevoBloqueado", "24")]:
        a("PLA.Configuracion", IdEmpresa=1, CodigoParametro=k, Valor=v)
    for k, v in [("CALCULAR_COMISION_CERTIFICACION", "N"), ("RespetaValutasCargaMasiva", "N"), ("RESCATE_GENERA_VENTA_SPOT_DOLAR", "N"),
                 ("UsaDolarObservado", "N"), ("MODALIDAD_SOLICITUD_FECHA_APORTE_RESCATE", ""), ("PERMITE_DIA_NO_HABIL_FECHA_VALOR_CUOTA_MONEY_MARKET", "N"),
                 ("VALIDAR_REINVERSION_DISTINTAS_FAMILIAS", "N"), ("UsaReplicaSigaFondoMutuo", "N"), ("CALCULAR_COMISION_VALOR_CUOTA_ACTUAL", "N"),
                 ("MontoMinimoTotalInversion", "0"), ("MontoMaximoTotalInversion", "999999999999"), ("MontoMinimoAportesAltos", "0"),
                 ("MontoMaximoAportesAltos", "999999999999")]:
        a("FMM.Parametro", IdEmpresa=1, NombreTipoParametro=k, ValorParametro=v)


def plataforma():
    a("PLA.Modulo", IdModulo="FMM")
    a("PLA.Empresa", IdEmpresa=1, IdSocioDeNegocio=10)
    a("SOC.ConsultarSocioDeNegocio", IdSocioDeNegocio=10, IdContraparte=5, IdCliente=99, IdPersona=1)
    a("ACC.Usuario", IdUsuario=7, IdEmpresa=1, IdEstado="ACT", TipoEntidad="EMP", IdEntidad=70)
    a("FMM.ConfiguracionFondoMutuo", Key="PERFIL_COMERCIAL", Value="5")
    a("ACC.ConsultarRolesPerfilesUsuario", IdPerfil=5, IdUsuario=7)
    for code, txt in [("UI_ERROR_BLOQUEO_SOLICITUD", "Solicitud duplicada"), ("NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE", "No se ingresaron solicitudes"),
                      ("UI_ERROR_FM_CLIENTE_SIN_CUSTODIA", "Cliente sin custodia de ::Nemotecnico:: retiro ::CantidadRetiro:: custodia ::CantidadCustodia::"),
                      ("ID_INTERNO_NO_CERO", "El id interno no puede ser cero"), ("CORRELATIVO_INCORRECTO", "Correlativo incorrecto"),
                      ("PERFIL_INCORRECTO", "Perfil incorrecto")]:
        a("PLA.Diccionario", CodLenguaje="es", CodDiccionario=code, Descripcion=txt)
    for m, p in [("CLP", 56), ("USD", 840)]:
        a("PLA.Moneda", IdMoneda=m, IdPais=p)
    for i, m in enumerate(["CLP", "USD"], 1):
        a("PLA.MonedaFormato", IdMonedaFormato=i, IdMoneda=m, IdUsoMonedaFormato="GEN", IdTipoInstrumento="", IdTipoOperacion="", PrecioDecimales=4, PrecioTrucaRedondea="R",
          TasaDecimales=4, TasaTrucaRedondea="R", CantidadDecimales=4, CantidadTrucaRedondea="R", MontoDecimales=0 if m == "CLP" else 2, MontoTrucaRedondea="R",
          TipoCambioDecimales=2, TipoCambioTrucaRedondea="R", ParidadDecimales=4, ParidadTrucaRedondea="R", PuntosDecimales=2, PuntosTrucaRedondea="R", IdEstado="ACT")


def fondo():
    a("FMM.TipoFondo", IdTipoFondo="ACC", NombreCorto="Accionario", Descripcion="Fondo accionario ficticio", IndicadorMoneyMarket="N")
    est = {"Estado%s%s" % (x, y): "A" for x in ("Aporte", "Rescate", "Reinversion", "Liquidez") for y in ("Comercial", "Backoffice")}
    a("FMM.Fondo", IdFondo=10, IdEmisor="AGF1", IdPaisEmisor=56, Nemotecnico="FM001", NombreFondo="Fondo Ficticio Uno", IdTipoFondo="ACC", IdPaisOrigen=56,
      MoneyMarket="N", FondoExtranjero="N", IdMoneda="CLP", IdTipoInstrumento="FM", IdEmpresa=1, BloqueoAporte="N", BloqueoRescate="N", BloqueoIndefinido="N",
      FechaFondo="2026-03-02", IdMercadoFondoMutuo=0, **est)
    a("FMM.SerieDeFondo", IdSerieFondo="FM001A", IdInstrumento=900, IdFondo=10, NombreSerieFondo="Serie A ficticia", CodigoSerie="A", DecimalesValorCuota=4,
      DecimalesCuota=4, MontoMinimo=0, MontoMaximo=0, DiasRetencionesAporte=0, DiasRetencionesRescate=0, DiasValutaCurseAporte=0, DiasValutaCurseRescate=0,
      DiasValutaValorCuotaAporte=0, DiasValutaValorCuotaRescate=0, DiasValutaCertificacionAporte=0, DiasValutaCertificacionRescate=1, Diferido="N",
      PatrimonioMinimo=0, PatrimonioAltos="N", IdEstado="ACT", IdEmpresa=1, EsAPV="N", RestringeAporte="N", RestringeRescate="N", MontoPatrimonioEmisor=0)
    a("FMM.RegimenFondoMutuo", IdRegimenFondoMutuo="GEN", GrupoRegimen="DC", PermiteRescate="S", PermiteAporte="S", PermiteTraspaso="N", ValidaMontoMinimoInstrumento="S",
      RescateConMandato="N", IdEstado="ACT")
    a("FMM.RegimenSerieDeFondo", IdSerieFondo="FM001A", IdRegimenFondoMutuo="GEN", IdEmpresa=1, IdEstado="ACT")
    a("FMM.ConfiguracionPropositoPortafolio", IdRegimenFondoMutuo="GEN", IdPropositoPortafolio="PROPGEN")
    for t, ind, op in [("RESNORM", "RES", "RESCFM"), ("APONORM", "APO", "APORFM"), ("RESTRASP", "RES", "RESCFM"), ("APOTRASP", "APO", "APORFM")]:
        a("FMM.TipoMovimientoFondoMutuo", IdTipoMovimientoFondoMutuo=t, Descripcion=t, IndicadorAporteRescate=ind, IdTipoOperacion=op, IdEstado="ACT")
    a("FMM.ValorTipoCambio", IdSerieFondo="FM001A", IdEmpresa=1, ValorDolarAporte=900, ValorDolarRescate=890)
    a("FMM.ValorCuota", IdSerieFondo="FM001A", ValorCuota=1500.1234)
    for tf, dias, org, hab in [("DIGITA", 0, "", "S"), ("CURSE", 0, "DIGITA", "S"), ("VCUOTA", 0, "CURSE", "S"), ("CERTIF", 1, "CURSE", "S"), ("LIQUID", 1, "CURSE", "S")]:
        a("FMM.ConfiguracionFechasFondoMutuo", IdEmpresa=1, IdModalidad="UNITARIO", IdTipoFecha=tf, CantidadDias=dias, EsHabil=hab, IdTipoFechaOrigen=org,
          DebeSerHabilFechaOrigen="N", ValutaCurse="N", ValutaValorCuota="N", ValutaCertificacion="S" if tf == "CERTIF" else "N", ValutaRetencion="N",
          CalendarioLocal="N", CalendarioEmisor="N", CalendarioFondo="N", CalendarioMoneda="N", IdEstado="ACT", Descripcion=tf)


def cliente():
    a("PER.DocumentoIdentidad", IdDocumentoIdentidad=1, DocumentoIdentidad="11111111-1", IdTipoDocumentoIdentidad="RUT", Predeterminado="S")
    a("PER.Persona", IdPersona=1000, IdDocumentoIdentidad=1)
    a("PER.ConsultarPersonaNombreCompleto", IdPersona=1000, NombreCompleto="Cliente Ficticio Uno")
    a("EMP.Empleado", IdPersona=2000, IdEmpleado=20)
    a("CLI.Cliente", IdCliente=100, IdPersona=1000, FechaCreacion="2020-01-01 00:00:00", IdPersonaEjecutivo=2000)
    a("CLI.ConsultarClienteEstado", IdCliente=100, IdEstado="ACT")
    a("POR.Portafolio", IdPortafolio=500, CodigoPortafolio="PF500", IdCliente=100, IdEstado="ACT", Proposito="PROPGEN")
    a("IND.ConsultarIndicadoresPortafolio", IdCliente=100, IdPortafolio=500, ValorIndicador="S", CodigoTipoIndicador="GEN_OCCINV")


DOUBLES = {
    "ConsultarEmpresa": {"defecto": {"IdEmpresa": 1, "Codigo": "BCS", "Nombre": "Corredora Ficticia", "IdEstado": "ACT", "IdPais": 56, "IdSocioDeNegocio": 10, "IdCliente": 99}},
    "CalcularParidad": {"respuestas": [
        {"cuando": {"0.p_IdTrabajo": 1}, "respuesta": {"p_out_CalculosSalida": [{"IdTrabajo": 1, "IdInterno": 1, "FactorCambioLocal": 1, "FactorOrigenDestino": 1, "MontoDestino": 15001}]}},
        {"cuando": {"0.p_IdTrabajo": 2}, "respuesta": {"p_out_CalculosSalida": [
            {"IdTrabajo": 2, "IdInterno": 6, "MontoDestino": 38.5}, {"IdTrabajo": 2, "IdInterno": 7, "FactorOrigenDestino": 39000, "MontoDestino": 39000}]}}],
        "defecto": {"p_out_CalculosSalida": []}},
    "EntregarPreciosTasas": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": [{"IdRegistro": 1, "IdClasificacionTipoInstrumento": "FM", "IdTipoInstrumento": "FM",
        "IdInstrumento": 900, "Nemotecnico": "FM001A", "Precio": 1500.1234, "CodigoFuente": "OTR", "FechaFuente": "2026-03-02 00:00:00.000", "IdMoneda": "CLP"}]}},
    "ConsultarFlujoCustodia": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "p_out_FlujoTotalizado": [{"IdCliente": 100, "IdPortafolio": 500, "IdInstrumento": 900,
        "IdRegimenFondoMutuo": "GEN", "Cantidad": 1000, "MontoAMercadoMonedaInstrumento": 1500123}]}},
    "SaldoFlujoCaja": {"defecto": {"P_OUT_FLUJOCAJA": []}},
    "AgregarLog": {"defecto": {}},
    "InscribirProcesoBatch": {"defecto": {}},
    "ConsultarProductosPortafolio": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "p_out_Mensajes": [], "p_out_PortafolioProductos": [{"IdPortafolio": 500, "IdPerfilProductos": "PERF1", "IdEstadoPerfilProducto": "ACT", "IdEstadoDetallePerfilProducto": "ACT", "IdTipoProducto": "INFM", "IdTipoOperacion": "0", "IdTipoCanalIngreso": "OPT"}]}},
}


def main():
    for f in (config, plataforma, fondo, cliente):
        OUT.append("-- " + f.__name__)
        f()
    with open(os.path.join(HERE, "seed", "base-fmm.sql"), "w", encoding="utf-8", newline="\n") as fh:
        fh.write("-- GENERATED by make_base.py. FAKE universe shared by all cases (see make_base.py).\n" + "\n".join(OUT) + "\n")
    with open(os.path.join(HERE, "seed", "doubles-base.json"), "w", encoding="utf-8", newline="\n") as fh:
        json.dump(DOUBLES, fh, indent=1)
        fh.write("\n")
    print("base seed: %d statements" % len([x for x in OUT if x.startswith("INSERT")]))


if __name__ == "__main__":
    main()
