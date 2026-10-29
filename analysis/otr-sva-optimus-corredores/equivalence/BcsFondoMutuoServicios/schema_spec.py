# Column spec of the H2 schema, DERIVED from the 36 in-scope SQL (+ the POJOs they fill, + the SQL of the real Plataforma kit classes the oracle runs).
# One string per object: "Column:T Column:T ...". T = L BIGINT | I INTEGER | D DECIMAL(26,8) | T TIMESTAMP | Dt DATE | S<n> NVARCHAR(n).
# TYPES HERE ARE ASSUMED (inferred from the Java POJO field types and from the three DDL texts in the SQL that carry real types: the lock table
# NVARCHAR(120)/BIGINT/NVARCHAR(40)/NVARCHAR(40)/NVARCHAR(3)/DECIMAL(26,8)/DECIMAL(26,8)/DATE). make_schema.py overrides every column found in the
# read-only HANA catalog exports (hana_qa_results/cat_columnas_*.csv and the future fmm_*.csv) and writes schema-types.sql with REAL/ASSUMED tags.
P = "BSCL.FondoMutuo.BD::"; PV = "BSCL.FondoMutuo.BD.Vistas::"; PL = "BSCL.Plataforma.BD::"; PLV = "BSCL.Plataforma.BD.Vistas::"
SN = "BSCL.SocioNegocio.BD::"; SNV = "BSCL.SocioNegocio.BD.Vistas::"; APV = "BSCL.APV.BD::"; APVV = "BSCL.APV.BD.Vistas::"
PO = "BSCL.Portafolio.BD::"; POV = "BSCL.Portafolio.BD.Vistas::"; CU = "BSCL.Custodia.BD::"; OR = "BSCL.Orden.BD::"

_ESTADOS = " ".join("Estado%s%s:S3" % (a, b) for a in ("Aporte", "Rescate", "Reinversion", "Liquidez") for b in ("Comercial", "Backoffice"))
_SOL = ("IdSolicitudAporteRescate:L IdTipoCanalIngreso:S3 FolioContrato:L IdCliente:L IdPortafolio:L TipoSolicitud:S3 IdSerieFondo:S40 IdTipoMovimientoFondoMutuo:S20 "
        "IdDividendo:L IdSerieFondoDestino:S40 IdEmisorDestino:S40 IdClienteDestino:L IdPortafolioDestino:L IdSerieFondoOrigen:S40 IdEmisorOrigen:S40 MontoOrigen:D "
        "IndicadorMLT:S1 Overnight:S1 IdSolicitudAporteRescateRelacionado:L IdTipoCartera:S3 CalculoPorMontoCuota:S1 FechaEmision:Dt FechaAceptacion:Dt FechaValorCuota:Dt "
        "FechaCertificacion:Dt FechaLiquidacion:Dt FechaAnulacion:Dt FechaModificacion:Dt FechaOriginalInversion:Dt FechaIngresoRegistro:T FechaAnulacionRegistro:T "
        "FechaAceptacionRegistro:T FechaCertificacionRegistro:T FechaLiquidacionRegistro:T FechaModificacionRegistro:T EsPorMontoLiquido:S1 Monto:D NumeroCuotas:D "
        "Porcentaje:D ValorCuota:D CostoUF:D CostoMonedaLocal:D ParidadFechaCertificacion:D ValorUFFechaInversion:D ParidadFechaInversion:D IdMoneda:S3 MontoComision:D "
        "ComisionInformada:S1 MontoComisionLocal:D MontoLiquido:D MontoLiquidoLocal:D MontoPago:D MontoPagoLocal:D MontoInversion:D NumeroCuotasInversion:D ValorCuotaInversion:D "
        "CostoMonedaLocalInversion:D Spread:D Estado:S3 EstadoLiquidacion:S3 MueveCta:S1 FolioCartaLiquidacion:L IdMedioDePago:S3 Glosa:S255 IdEmpresa:L IdUsuario:L IdModulo:S10 "
        "OrigenOperacion:S3 SolicitudBatch:S1 IdRegimenTributario:S40 TipoCarteraNormativa:S3 Tributacion:S3 IdRegimenTributarioDestino:S40 TipoCarteraNormativaDestino:S3 "
        "AdministracionLiquidez:S1 Visado:S1 IdPreferenciaPagoPortafolio:L IdAutorizadoOrigen:L IdAutorizadoDestino:L IdMonedaSistema:S3 MontoMonedaSistema:D Switch:S1 "
        "EstadoAnulacion:S3 FolioOmnibus:L FolioAgfOmnibus:L IdEmpleadoEjecutivo:L IdEmpleadoEjecutivoDestino:L IdMonedaPago:S3 IdIngresoEgreso:L IdOperacionSpot:L IdCartera:L "
        "IdAgrupador:L IngresoAutomatico:S1 TipoContratoOperacion:S3 FolioContratoOperacion:S40 RentabilidadReal:D IdOrigenRecaCliente:L FolioCarteraAsociadoComision:L SecuenciaMovimiento:L")

TABLES = {
 P + "FMM.SerieDeFondo": ("IdSerieFondo:S40 IdInstrumento:L IdFondo:L NombreSerieFondo:S100 CodigoSerie:S40 Alias:S40 DecimalesValorCuota:I DecimalesCuota:I MontoMinimo:D MontoMaximo:D "
    "DiasRetencionesAporte:L DiasRetencionesRescate:L DiasValutaCurseAporte:L DiasValutaCurseRescate:L DiasValutaValorCuotaAporte:L DiasValutaValorCuotaRescate:L "
    "DiasValutaCertificacionAporte:L DiasValutaCertificacionRescate:L ExcluyeGarantia:S1 CobraComision:S1 Diferido:S1 InstrumentoDCV:S1 Omnibus:S1 PatrimonioMinimo:D "
    "IdSerieFondoOrigen:S40 FechaIngresoRegistro:T IdEstado:S3 IdCliente:L IdPortafolio:L IdMedioDePagoAbono:S3 IdMedioDePagoCargo:S3 IdFamiliaFondo:L IdEmpresa:L CambioFondo:S1 "
    "DiasComision:L PorcentajeComision:D PorcentajeRemuneracion:D PorcentajeGastoOpera:D PorcentajeCostoAdm:D AportesAltos:D PatrimonioAltos:D EsAPV:S1 RestringeAporte:S1 "
    "RestringeRescate:S1 MontoPatrimonioEmisor:D"),
 P + "FMM.Fondo": ("IdFondo:L IdEmisor:S40 IdPaisEmisor:L Nemotecnico:S40 NombreFondo:S100 IdTipoFondo:S3 IdPaisOrigen:L CodigoSVS:S40 MoneyMarket:S1 FechaCreacion:Dt "
    "FechaIngresoRegistro:T FechaConstitucion:Dt FechaCierre:Dt Vigencia:S1 FondoExtranjero:S1 IdMoneda:S3 IdTipoInstrumento:S3 IdEmpresa:L IdCategoriaPerfilFondoMutuo:S3 "
    "UsaHorarioAporteDefault:S1 UsaHorarioRescateDefault:S1 UsaHorarioReinversionDefault:S1 UsaHorarioLiquidezDefault:S1 " + _ESTADOS + " BloqueoAporte:S1 BloqueoRescate:S1 "
    "BloqueoIndefinido:S1 FechaBloqueoDesde:Dt FechaBloqueoHasta:Dt FechaFondo:Dt AperturaAutomaticaFondo:S1 FondoNormativo:S1 IdTipoDocumentoIdentidadFondo:L "
    "DocumentoIdentidadFondo:S40 IdMercadoFondoMutuo:L"),
 P + "FMM.TipoFondo": "IdTipoFondo:S3 NombreCorto:S40 Descripcion:S100 IndicadorMoneyMarket:S1",
 P + "FMM.TipoMovimientoFondoMutuo": "IdTipoMovimientoFondoMutuo:S20 Descripcion:S100 IndicadorAporteRescate:S3 IdTipoOperacion:S3 TieneCarga:S1 TieneIngresoManual:S1 TieneCertificacion:S1 IdEstado:S3 GeneraOmnibus:S1",
 P + "FMM.RegimenSerieDeFondo": "IdSerieFondo:S40 IdRegimenFondoMutuo:S40 IdEmpresa:L IdEstado:S3",
 P + "FMM.RegimenFondoMutuo": ("IdRegimenFondoMutuo:S40 GrupoRegimen:S3 PropositoSerieRelacionado:S40 PermiteOtrosGrupos:S1 ValidaPortafolioCliente:S1 ValidaPortafolioAGF:S1 PermiteRescate:S1 "
    "TipoCarteraNormativa:S3 PermiteCambioGrupo:S1 RescateConMandato:S1 AceptaMandatoEnReinversion:S1 PermiteAporte:S1 PermiteTraspaso:S1 ValidaMontoMinimoInstrumento:S1 "
    "CertificaSinAsociado:S1 IdEstado:S3"),
 P + "FMM.ConfiguracionPropositoPortafolio": "IdRegimenFondoMutuo:S40 IdPropositoPortafolio:S40",
 P + "FMM.ConfiguracionFondoMutuo": "Key:S60 Value:S60",
 P + "FMM.ConfiguracionFechasFondoMutuo": ("IdEmpresa:L IdModalidad:S40 IdTipoFecha:S40 TipoSolicitud:S3 EstadoFondo:S3 FondoExtranjero:S1 Descripcion:S100 CantidadDias:L EsHabil:S1 "
    "IdTipoFechaOrigen:S40 DebeSerHabilFechaOrigen:S1 ValutaCurse:S1 ValutaValorCuota:S1 ValutaCertificacion:S1 ValutaRetencion:S1 CalendarioLocal:S1 CalendarioEmisor:S1 "
    "CalendarioFondo:S1 CalendarioMoneda:S1 IdEstado:S3"),
 P + "FMM.Parametro": "IdEmpresa:L NombreTipoParametro:S100 ValorParametro:S255",
 P + "FMM.ValorTipoCambio": "IdSerieFondo:S40 IdEmpresa:L ValorDolarAporte:D ValorDolarRescate:D",
 P + "FMM.ValorCuota": "IdSerieFondo:S40 ValorCuota:D",
 P + "FMM.Comision": "IdSerieFondo:S40 TipoComision:S3",
 P + "FMM.InterfazAgf": ("IdInterfazAgf:L DocumentoIdentidad:S40 CodigoPortafolio:S40 NombreCompleto:S255 IdEstado:S3 TipoRegistro:S3 FechaIngreso:Dt FechaIngresoRegistro:T IdEmpresa:L IdCliente:L "
    "IdPortafolio:L FechaLiquidacion:Dt FechaAceptacion:Dt TipoSolicitud:S3 IdEstadoSolicitud:S3 IdSerieFondo:S40 IdUsuario:L NumeroCuotas:D ValorCuota:D Monto:D CalculoPorMontoCuota:S1 "
    "IdMoneda:S3 PropositoSerie:S40 IdSerieFondoDestino:S40 IdEstadoSerieFondo:S3 IndicadorMLT:S1 IdSolicitudAporteRescate:L IdSolicitudAporteRescateRelacionado:L"),
 P + "FMM.SolicitudAporteRescate": _SOL,
 P + "FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate": "IdBloqueoSolicitudAporteRescate:S120 IdPortafolio:L IdSerieFondo:S40 IdRegimenTributario:S40 TipoSolicitud:S3 Monto:D NumeroCuotas:D FechaSistema:Dt",
 PV + "FMM.ConsultarSolicitudAporteRescate": _SOL + " EstadoSolicitud:S3 IdInstrumento:L IdTipoInstrumento:S3 IdEmisor:S40 IdMonedaFondo:S3 IdUsuario:L IdPersona:L ValorCuotaSolicitud:D "
    "CodigoPortafolio:S40 DocumentoIdentidad:S40 NombreCompleto:S255 IdEstadoSerieFondo:S3 IdFamiliaFondo:L PropositoPortafolio:S40 Estado:S3 IdMatrizAgrupador:L IdPersonaEmisorDestino:L",
 PV + "FMM.ConsultarTramoComision": ("IdTramo:L IdComision:L DiasDesde:L DiasHasta:L PorcentajeComision:D EstadoTramo:S3 IdSerieFondo:S40 Descripcion:S100 TipoComision:S3 "
    "FechaVigenciaDesde:Dt FechaVigenciaHasta:Dt PorcentajeLiberado:D EstadoComision:S3 IdEmpresa:L"),
 PV + "FMM.ConsultarComisionMaxima": "IdSerieFondo:S40 IdComision:L",
 PL + "PLA.Configuracion": "IdEmpresa:L CodigoParametro:S100 Valor:S255",
 PL + "PLA.Empresa": "IdEmpresa:L IdSocioDeNegocio:L",
 PL + "PLA.Modulo": "IdModulo:S10",
 PL + "PLA.Diccionario": "CodLenguaje:S10 CodDiccionario:S100 Descripcion:S500",
 PL + "PLA.Moneda": "IdMoneda:S3 IdPais:L",
 PL + "PLA.MonedaFormato": ("IdMonedaFormato:L IdMoneda:S3 IdUsoMonedaFormato:S20 IdTipoInstrumento:S3 IdTipoOperacion:S3 PrecioDecimales:I PrecioTrucaRedondea:S1 TasaDecimales:I TasaTrucaRedondea:S1 "
    "CantidadDecimales:I CantidadTrucaRedondea:S1 MontoDecimales:I MontoTrucaRedondea:S1 TipoCambioDecimales:I TipoCambioTrucaRedondea:S1 ParidadDecimales:I ParidadTrucaRedondea:S1 "
    "PuntosDecimales:I PuntosTrucaRedondea:S1 IdEstado:S3"),
 PL + "REG.DiaFeriado": "IdFeriado:L Tipo:S3 Fecha:Dt Dia:I DescripcionDia:S100 AgnoCarga:I IdPais:L IdEstado:S3",
 PL + "ACC.Usuario": "IdUsuario:L IdEmpresa:L IdEstado:S3 TipoEntidad:S3 IdEntidad:L",
 PL + "IND.TipoIndicador": "IdTipoIndicador:L IdTipoEntidad:S20 CodigoTipoIndicador:S20 IdEstado:S3",
 PL + "IND.Indicador": "IdEntidad:L IdTipoIndicador:L IdTipoEntidad:S20 ValorIndicador:S10",
 PL + "MER.Emisor": "IdEmisor:S40 IdPersona:L",
 PLV + "IND.ConsultarIndicadoresPortafolio": "IdCliente:L IdPortafolio:L ValorIndicador:S10 CodigoTipoIndicador:S20",
 PLV + "ACC.ConsultarRolesPerfilesUsuario": "IdPerfil:S60 IdUsuario:L",
 SN + "CLI.Cliente": "IdCliente:L IdPersona:L FechaCreacion:T IdPersonaEjecutivo:L",
 SN + "PER.Persona": "IdPersona:L IdDocumentoIdentidad:L",
 SN + "PER.DocumentoIdentidad": "IdDocumentoIdentidad:L DocumentoIdentidad:S40 IdTipoDocumentoIdentidad:S3 Predeterminado:S1",
 SN + "EMP.Empleado": "IdPersona:L IdEmpleado:L",
 SNV + "CLI.ConsultarClienteEstado": "IdCliente:L IdEstado:S3",
 SNV + "PER.ConsultarPersonaNombreCompleto": "IdPersona:L NombreCompleto:S255",
 SNV + "SOC.ConsultarSocioDeNegocio": "IdSocioDeNegocio:L IdContraparte:L IdCliente:L IdPersona:L",
 PO + "POR.Portafolio": "IdPortafolio:L CodigoPortafolio:S40 IdCliente:L IdEstado:S3 Proposito:S40",
 POV + "ConsultarPortafolio": "IdPortafolio:L IdMatrizAgrupadorAPV:L",
 APV + "REC.RegimenTributario": "IdRegimenTributario:S40 Codigo:S40",
 APVV + "REC.ConsultarRegimenTributario": "IdMatrizAgrupadorAPV:L IdRegimenTributario:S40",
 APV + "TRA.Traspaso": "IdTraspaso:L RazonRetiro:S3 IdPersona:L",
 APV + "TRA.OrdenTraspaso": "IdSolicitudAporteRescate:L IdTraspaso:L",
 CU + "CUS.Cartera": ("IdPortafolio:L IdInstrumento:L IdRegimenFondoMutuo:S40 IdEstado:S3 IdCliente:L IdEmisor:S40 IdMonedaInstrumento:S3 INGCantidad:D "
    "INGMontoMercadoActualMonedaInstrumento:D Nemotecnico:S40 FechaLiquidacion:Dt"),
 OR + "GBL.ValidaPatrimonioEmisor_Proceso_Paso": "IdCliente:L",
}
SEQUENCES = {"BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate": 1000, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato": 5000,
             "BSCL.FondoMutuo.BD.Secuencias::InterfazAgf": 300, "BSCL.Plataforma.BD.Secuencias::SecuenciaMovimiento": 9000}
