package cl.bcs.tesoreria.pojo;

public class ConstantesTesoreria {
	public final String SI = "S";
	public final String NO = "N";
	
	public final String TipoSolicitudAporte = "APO";
	public final String TipoSolicitudRescate = "RES";
	
	public final String Dolar = "USD";
	public final String Pesos = "CLP";
	
	public final String CalculoPorCuota = "C";
	public final String CalculoPorMonto = "M";
	public final String CalculoPorTotal = "T";
	public final String CalculoPorPorcentaje = "P";
	
	public final String EstadoActivo = "ACT";
	public final String EstadoPendiente = "PEN";
	public final String EstadoRechazado = "RZD";
	public final String EstadoCertificado = "CRT";
	public final String EstadoIngresado = "ING";
	public final String EstadoLiquidado = "LIQ";
	public final String EstadoAceptado = "ACP";
	public final String EstadoAprobado = "APD";
	public final String EstadoAdvertencia = "ADV";
	public final String EstadoError = "ERR";
	public final String EstadoAnulado = "ANU";
	public final String EstadoRechazadoTesoreria = "RECH";
	public final String EstadoIngresadoTesoreria = "ING";
	public final String EstadoVigente = "VIG";
	public final String EstadoProcesado = "PRO";
	public final String EstadoRES = "RES";
	
	public final String Abierto = "A";
	public final String Cerrado = "C";
	
	public final String Reinversion = "RESREINV";
	public final String Transferencia = "RESTRANSF";
	public final String Traspaso = "RESTRASP";
	public final String CambioSerie = "RESCAM";
	public final String IngresoTitulo = "INGTIT";
	public final String RetiroTitulo = "RETTIT";
	public final String AporteFondoMutuo = "APONORM";
	public final String RescateFondoMutuo = "RESNORM";
	public final String AporteRescateProgramado = "APORESPRO";
	public final String AporteOvernight = "APOOVER";
	public final String AporteReinversion = "APOREINV";
	public final String TipoOperacionAporteFondoMutuo = "APORFM";
	public final String TipoOperacionRescateFondoMutuo = "RESCFM";
	public final String TipoOperacionAnulacionAporteFondoMutuo = "ANUAPO";
	public final String TipoOperacionAnulacionRescateFondoMutuo = "ANURES";
	
	
	public final String PerfilComercial = "PERFIL_COMERCIAL";
	public final String PerfilBackOffice = "PERFIL_BACKOFFICE";
	
	public final String Retiro = "RETIRO";
	public final String Ingreso = "INGRESO";
	
	public final String TipoOperacion = "INFM";
	public final String TipoProducto = "INFM";
	
	public final String ConceptoAporteTransferencia = "APOTRN";
	public final String ConceptoRescateTransferencia = "RESTRN";
	public final String ConceptoAporteTraspaso = "APOTRS";
	public final String ConceptoRescateTraspaso = "RESTRS";
	public final String ConceptoRescateCambioSerie = "RESCAM";
	public final String ConceptoAporteCambioSerie = "APORFM";
	public final String ConceptoRetiroTitulo = "RETIFM";
	public final String ConceptoIngresoTitulo = "INTIFM";
	public final String ConceptoAporteNormal = "APORFM";
	public final String ConceptoRescateNormal = "RESCFM";
	public final String ConceptoAporteReinversion = "APOREI";
	public final String ConceptoAporteDividendo = "APODIV";
	public final String ConceptoAporteOvernight="APOOVE";
	public final String ConceptoAporteRescateProgramado="APOPRO";
	public final String ConceptoAporteNormalDolar = "APODOL";
	public final String ConceptoRescateNormalDolar = "RESDOL";
	public final String ConceptoAnulacionAporte = "ANUAPO";
	public final String ConceptoAnulacionRescate = "ANURES";
	
	public final String ConceptoTraspasoIngreso = "TRAING";  //conceptos
	
	
	
	public final String Transito = "TRANSITO";
	public final String Cartera = "CARTERA";
	
	public final String TipoComprobanteCustodia = "FFMM";
	
	public final String BCS = "BCS";
	
	public final String UF = "UF";
	
	public final String COR = "COR";
	public final String OK = "OK";
	
	public final String OrigenManual = "M";
	
	public final String TipoRegistroParticipe = "PA";
	public final String TipoRegistroTraspaso = "TR";
	public final String TipoRegistroAporteRescate = "AR";
	
	public final String CUS = "CUS";
	
	public final String ModuloFondoMutuo = "FMM";
	public final String TipoComprobanteTesoreria = "FMM";
	public final String TipoComprobanteCuentaInversion = "FFM";
	
	public final String ComisionSalida = "SALIDA";
	public final String ComisionEntrada = "ENTRADA";
	
	public final String UsaCuentaInversion = "GEN_OCCINV";
	
	public final String FechaDefecto = "0001-01-01";
	
	public final String TributacionAPV = "ModeloRetiroAPV";
	public final String TributacionART108 = "MayorMenorValor";
	public final String TributacionART107 = "Beneficio107LIR";
	public final String Tributacion57BIS = "Beneficio57BIS";
	public final String TributacionDefecto = "MayorMenorValor";
	
	public final String CuentaInversion = "CIN";
	
	public final String UsoFlujoCajaIngreso = "INGRESO_APORTE_FMM";
	public final String UsoFlujoCajaCertificacion = "CERTIFICACION_APORTE_FMM";
	
	public final String IngresoTesoreria = "MOVING";
	public final String EgresoTesoreria = "MOVEGR";
	public final String TipoPersonaCliente = "CLI";
	
	
	public final String TipoPersonaCorredor = "CNT";
	public final String TipoPersonaContraparte = "CNT";
	
	
	public final String T0 = "T0";
	
	public final String TipoInstrumento = "FMM";
	
	public final String MODCAJ = "MODCAJ";
	public final String MODDEP = "MODDEP";
	
	public final String NumeroReferenciaTesoreria = "NumeroReferencia";
	public final String IdIngresoEgresoTesoreria = "IdIngresoEgreso";
	
	public final String CargoCuentaInversion = "MOVCAR";
	public final String AbonoCuentaInversion = "MOVABN";
	
	public final String ModuloSpot = "SPT";
	public final String SpotCompra = "COMSPT";
	public final String SpotVenta = "VENSPT";
	public final String SpotProducto = "MEME";
	public final String SpotTipoComprobanteFacturacion ="FAE";
	public final String SpotBenOur="BEN";
	public final String SpotEstadoPendiente = "PEN";
	public final String SpotEstadoRechazado = "RECH";
	public final String SpotCalculaMargen="N";
	public final String SpotError="ERROR";
	public final String SpotTipoInstrumento="SPT";
	public final String SpotNemo="DISTRI";
	public final Long SpotCodigoComercio=(long)10005;
	public final String SpotTipoMovimientoIngreso="MOVING";
	public final String SpotTipoMovimientoEgreso="MOVEGR";
	public final String SpotformaDePagoEntregamos="VENSPT_CIN_CLP_0";
	public final String SpotformaDePagoRecibimos="COMSPT_CIN_USD_0";
	
	public final String TraspasoEstadoIngresado = "ING";
	public final String TraspasoEstadoVigente = "VIG";
	public final String TraspasoEstadoPendiente = "PEN";
	public final String TraspasoMovimientoIngreso = "TRAING";
	public final String TraspasoMovimientoEgreso = "TRAEGR";
	public final String TraspasoDetalleActivo = "ACT";
	public final String TraspasoDetalleInactivo = "INA";
		
	public final String TraspasoRetencion = "TRASPASO_RETENCION";
	
	public final String ModuloTesoreria = "TES";
	public final String ModuloCtaInversion = "CTA";
	
	public final String MedioPagoTransferencia = "TRA";
	
	
	public final String RescateComision = "RESCOM";
	public final String AporteDividendo = "APODIV";
	public final String APV = "APV";
		
	public final String COCAPV = "COCAPV";
	public final String COAFFM = "COAFFM";
	public final String SCV = "SCV";
	
	public final String CanalAgente = "AGE";
	
	
	public final String ABNCSC = "ABNCSC";
	
	public final String ACSCIR = "ACSCIR";
	public final String ACSCER = "ACSCER";
	public final String ACSCID = "ACSCID";
	
	public final String ABNAPV = "ABNAPV";
	
	public final String AAPVIR = "AAPVIR";
	public final String AAPVER = "AAPVER";
	public final String AAPVID = "AAPVID";
	
	public final String ABPACR = "ABPACR";
	public final String EGPACR = "EGPACR";
	
	public final String ABNAPV_DESCRIPTION = "Abono APV";
}
