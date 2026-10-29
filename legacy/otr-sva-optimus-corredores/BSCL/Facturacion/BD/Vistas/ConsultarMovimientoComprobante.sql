SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante"; 
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" AS
SELECT MVTOCMBTE."IdMovimientoComprobante",
	   MVTOCMBTE."IdEmpresa",
	   EMPRESA."Nombre" AS "NombreEmpresa", 
	   MVTOCMBTE."IdConceptoMovimiento",
	   CONCEPTOMOV."Nombre" AS "NombreConceptoMovimiento", 
	   MVTOCMBTE."SecuenciaAgrupacion", 
	   MVTOCMBTE."IdCliente",
	   MVTOCMBTE."DocumentoIdentidad",
	   MVTOCMBTE."TipoDocumentoIdentidad",        
	   MVTOCMBTE."NombreLargoCliente",
	   MVTOCMBTE."DireccionFacturacion",	     
	   MVTOCMBTE."IdPortafolio", 
   	   PORTAFOLIO."CodigoPortafolio",
   	   PORTAFOLIO."Proposito",
   	   (PORTAFOLIO."CodigoPortafolio" ||' '|| PORTAFOLIO."Proposito") AS "NombrePortafolio",
       MVTOCMBTE."IdEmpleadoDigitador",
       MVTOCMBTE."NombreEmpleadoDigitador",
       MVTOCMBTE."IdSucursalEmpleadoDigitador",
       MVTOCMBTE."NombreSucursalEmpleadoDigitador",
       MVTOCMBTE."IdEmpleadoEjecutivoPortafolio",
       MVTOCMBTE."NombreEmpleadoEjecutivoPortafolio",
       MVTOCMBTE."IdSucursalEmpleadoEjecutivoPortafolio",
       MVTOCMBTE."NombreSucursalEmpleadoEjecutivoPortafolio",
	   MVTOCMBTE."FolioCartera",	   
	   MVTOCMBTE."IdInstrumento",
	   TRANSACCION."CodigoOperadorCorredor" AS "CodigoOperador", 
	   --"NemotecnicoInstrumento",
	   CASE 
	        WHEN MVTOCMBTE."IdTipoInstrumento" IN ('PRT', 'EMP', 'SIM','RRV','RRF') THEN MVTOCMBTE."NemotecnicoSubyacente" 
	        ELSE MVTOCMBTE."NemotecnicoInstrumento"
	        END AS "NemotecnicoInstrumento",
	   MVTOCMBTE."IdTipoInstrumento",
	   MVTOCMBTE."NombreTipoInstrumento",
	   MVTOCMBTE."IdClasificacionTipoInstrumento",
	   MVTOCMBTE."NombreClasificacionTipoInstrumento",	   
	   MVTOCMBTE."IdInstrumentoSubyacente",
	   MVTOCMBTE."NemotecnicoSubyacente",
	   MVTOCMBTE."IdTipoInstrumentoSubyacente",
	   MVTOCMBTE."NombreTipoInstrumentoSubyacente",	   
	   CASE 
	   		WHEN MVTOCMBTE."IdTipoInstrumento" IN ('PRT', 'EMP', 'SIM','RRV','RRF') THEN MVTOCMBTE."IdInstrumentoSubyacente" 
	   		ELSE MVTOCMBTE."IdInstrumento"
	   END AS "IdInstrumentoUnificado",		      
	   CASE 
	   		WHEN MVTOCMBTE."IdTipoInstrumento" IN ('PRT', 'EMP', 'SIM','RRV','RRF') THEN MVTOCMBTE."NemotecnicoSubyacente" 
	   		ELSE MVTOCMBTE."NemotecnicoInstrumento"
	   END AS "NemotecnicoInstrumentoUnificado",
	   CASE 
	   		WHEN MVTOCMBTE."IdTipoInstrumento" IN ('PRT', 'EMP', 'SIM','RRV','RRF') THEN MVTOCMBTE."IdTipoInstrumentoSubyacente" 
	   		ELSE MVTOCMBTE."IdTipoInstrumento"
	   END AS "IdTipoInstrumentoUnificado",
	   CASE 
	   		WHEN MVTOCMBTE."IdTipoInstrumento" IN ('PRT', 'EMP', 'SIM','RRV','RRF') THEN MVTOCMBTE."NombreTipoInstrumentoSubyacente" 
	   		ELSE MVTOCMBTE."NombreTipoInstrumento"
	   END AS "NombreTipoInstrumentoUnificado",
	   MVTOCMBTE."IdEmisor",
	   MVTOCMBTE."RazonSocialEmisor",
	   MVTOCMBTE."FechaEmision",
	   MVTOCMBTE."TasaEmision", 	        
	   MVTOCMBTE."IdTipoProducto",
	   TIPOPRODUCTO."Nombre" AS "NombreTipoProducto",
	   MVTOCMBTE."CodigoTipoProducto", 
	   MVTOCMBTE."IdMoneda",
	   MONEDA."Nombre" AS "NombreMoneda",
	   MVTOCMBTE."IdMonedaSpot",
	   MVTOCMBTE."IdMonedaMovimientoComprobante" AS "IdMonedaMovimientoComprobante", 
       MonMvtoCmbte."Nombre" AS "NombreMonedaMovimientoComprobante",
	   MVTOCMBTE."IdMonedaLiquidacion",	   
	   MonLiq."Nombre" AS "NombreMonedaLiquidacion",
	   MVTOCMBTE."IdTipoComprobante",
	   TIPOCOMPROBANTE."Codigo" AS "CodigoTipoComprobante",
	   TIPOCOMPROBANTE."Nombre" AS "NombreTipoComprobante", 
	   MVTOCMBTE."IdComprobante", 
	   MVTOCMBTE."IdComprobanteReferencia", 
	   MVTOCMBTE."FolioComprobanteReferencia", 
	   MVTOCMBTE."FechaComprobanteReferencia", 	    
	   MVTOCMBTE."IdTipoOperacion",
	   MVTOCMBTE."CodigoTipoOperacion",

		CASE 
			WHEN (MVTOCMBTE."EsVentaCorta" = 'S' AND MVTOCMBTE."IdClasificacionTipoInstrumento" = 'RV') THEN 'INTERMEDIACION VENTA CORTA RENTA VARIABLE'
			WHEN (MVTOCMBTE."EsVentaCorta" = 'S' AND MVTOCMBTE."IdClasificacionTipoInstrumento" = 'RF') THEN 'INTERMEDIACION VENTA CORTA RENTA FIJA'
			ELSE TIPOOPERACION."Nombre"
		END AS "NombreTipoOperacion",

	   TIPOOPERACION."Accion" AS "AccionTipoOperacion", 	        
	   MVTOCMBTE."IdMovimientoRelacionado", 	        
	   MVTOCMBTE."IdMovimientoOdRelacionado",
	   MVTOCMBTE."IdBoveda",
	   BOVEDA."Codigo" AS "CodigoBoveda",
	   BOVEDA."Nombre" AS "NombreBoveda",
	   MVTOCMBTE."IdCorredoraContraparte",
	   CASE 
	   	  WHEN MVTOCMBTE."IdCorredoraContraparte" = 0 THEN ''
	   	  ELSE TO_NVARCHAR (MVTOCMBTE."IdCorredoraContraparte")
	   END AS "IdCorredoraContraparteString",
	   MVTOCMBTE."IdCondicionLiquidacion", 
	   CONDICIONDELIQUIDACION."Codigo" AS "CodigoCondicionDeLiquidacion",
	   CONDICIONDELIQUIDACION."Nombre" AS "NombreCondicionDeLiquidacion",
	   MVTOCMBTE."FechaLiquidacion", 
	   MVTOCMBTE."IdBolsa",
	   MVTOCMBTE."CodigoBolsa", 	        
	   MVTOCMBTE."IdTipoReajuste",
	   TIPOREAJUSTE."Nombre" AS "NombreTipoReajuste", 	        
	   MVTOCMBTE."IdTipoConvenio",
	   TIPOCONVENIO."Nombre" AS "NombreTipoConvenio",	         
	   MVTOCMBTE."IdFormaCustodia",
	   ConsFormCusOrd."Nombre" AS "NombreFormaCustodia",	   
	   MVTOCMBTE."IdEstadoFacturacion", 
	   ESTADOFACTURACION."IdEstado" AS "EstadoFacturacion",	        
	   ESTADOPLA."Nombre" AS "NombreEstadoFacturacion",	   
	   MVTOCMBTE."IdOrden",
	   CASE 
	   	  WHEN MVTOCMBTE."IdOrden" = 0 THEN ''
	   	  ELSE TO_NVARCHAR (MVTOCMBTE."IdOrden")
	   END AS "IdOrdenString",
	   MVTOCMBTE."IdAsignacion",
	   MVTOCMBTE."IdAsignacionRef",
	   MVTOCMBTE."IdTipoCanalIngreso",
	   MVTOCMBTE."CodigoCanalIngreso", 
	   MVTOCMBTE."FechaOrden",
   MVTOCMBTE."IdTransaccion",
       MVTOCMBTE."FolioTransaccion",        
	   CASE
	     WHEN MVTOCMBTE."IdTipoComprobante" = 'FOE' and MVTOCMBTE."FechaTransaccion" = '0001-01-01' THEN CAST(MVTOCMBTE."FechaMovimientoComprobante" AS DATE)
       ELSE CAST(MVTOCMBTE."FechaTransaccion" AS DATE) 
       END AS "FechaTransaccion",	   
	   MVTOCMBTE."FechaTransaccion" AS "FechaTransaccionHora",
	   MVTOCMBTE."FechaVencimiento",
	   MVTOCMBTE."FechaParidad",
	   MVTOCMBTE."ValorParidad",
	   MVTOCMBTE."ValorTipoCambio",
	   MVTOCMBTE."ValorCaptacionMonedaOrigen",
	   MVTOCMBTE."FolioComprobante",	        
	   MVTOCMBTE."Tasa",
	   MVTOCMBTE."TasaOperacion",  -- Para Venta corta

	   CASE 
	       WHEN MVTOCMBTE."EsFungible" = 'S' AND MVTOCMBTE."IdClasificacionTipoInstrumento" = 'IF' THEN MVTOCMBTE."ValorRescate" 
	   ELSE MVTOCMBTE."Cantidad" 
	   END AS "Cantidad",
	   MVTOCMBTE."Precio",	 
	   COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), MVTOCMBTE."Monto") AS "Monto",
	   COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoMovimientoComprobante", FormMvtoCmbte."MontoDecimales", FormMvtoCmbte."MontoTrucaRedondea"), MVTOCMBTE."MontoMovimientoComprobante") AS "MontoMovimientoComprobante",	   
       COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoLiquidacion", FormMvtoLiq."MontoDecimales", FormMvtoLiq."MontoTrucaRedondea"), MVTOCMBTE."MontoLiquidacion") AS "MontoLiquidacion",
	   MVTOCMBTE."MontoDescuento",
	   MVTOCMBTE."MontoInteres",
	   MVTOCMBTE."Glosa",
	   MVTOCMBTE."ValorRescate",
	   MVTOCMBTE."TasaAgente",	        
	   MVTOCMBTE."Spread",
	   MVTOCMBTE."DiasPlazo",
	   CAST(MVTOCMBTE."FechaMovimientoComprobante" AS DATE) AS "FechaMovimientoComprobante",
	   MVTOCMBTE."FechaIngresoRegistro",
	   MVTOCMBTE."FechaSistema", 
	   MVTOCMBTE."EsComprobIndividual",
	   MVTOCMBTE."EsCuentaPropia",
	   MVTOCMBTE."IndicadorOrdenDirecta",
	   MVTOCMBTE."EsPrecioPrima",
	   MVTOCMBTE."EsEnRueda",
	   MVTOCMBTE."EsRuteo",
	   MVTOCMBTE."EsIngresoAutomatico",
	   MVTOCMBTE."EsAPlazo",
	   MVTOCMBTE."ActualizarCustodia",
	   MVTOCMBTE."ActualizarCuentaInversion",
	   MVTOCMBTE."ActualizarTesoreria",
	   MVTOCMBTE."EsVentaCorta",
	   MVTOCMBTE."PrecioAPlazo",
	   MVTOCMBTE."MontoAPlazo",
	   MVTOCMBTE."IdUsuario",
	   MVTOCMBTE."IdUsuarioBloqueo",	   	   
	   CASE 
	     WHEN MVTOCMBTE."IdUsuarioBloqueo" <> 0 THEN USUARIO."CodigoUsuario"
	   ELSE ''
	   END AS "CodigoUsuarioBloqueo",
	   MVTOCMBTE."IdCarga",
	   CONSCLI."IdPersona",
	   COALESCE(ConsIndPortConDerBcs."ValorIndicador",'N') AS "EsConvenioDerechBCS",
	   COALESCE(ConsIndPortConDerBec."ValorIndicador",'N') AS "EsConvenioDerechBEC",	   
	   COALESCE(TOTCOBROS."COMMVTO",0) AS "COMMVTO",
	   COALESCE(TOTCOBROS."DERMVTO",0) AS "DERMVTO",
	   COALESCE(TOTCOBROS."GASMVTO",0) AS "GASMVTO",
	   COALESCE(TOTCOBROS."IMPMVTO",0) AS "IMPMVTO",
	   COALESCE(TOTCOBROS."RETMVTO",0) AS "RETMVTO",
	   (COALESCE(TOTCOBROS."COMMVTO",0) + COALESCE(TOTCOBROS."DERMVTO",0) + COALESCE(TOTCOBROS."GASMVTO",0) + COALESCE(TOTCOBROS."IMPMVTO",0) + COALESCE(TOTCOBROS."RETMVTO",0)) AS "MontoCobro",
	   
	   "ComisionBVLMonedaCobro",
	   "ComisionSABMonedaCobro",
	   "ComisionSMVMonedaCobro",
	   "ComisionCavaliMonedaCobro",
	   "FondoGarantiaMonedaCobro",
	   "FondoLiquidacionMonedaCobro",
	   "OtrasComisionesMonedaCobro",
	   "ImpuestoIGVMonedaCobro",
	   
	   "ComisionBVLMonedaMovimiento",
	   "ComisionSABMonedaMovimiento",
	   "ComisionSMVMonedaMovimiento",
	   "ComisionCavaliMonedaMovimiento",
	   "FondoGarantiaMonedaMovimiento",
	   "FondoLiquidacionMonedaMovimiento",
	   "OtrasComisionesMonedaMovimiento",
	   "ImpuestoIGVMonedaMovimiento",
	   
	   "ComisionBVLMonedaLiquidacion",
	   "ComisionSABMonedaLiquidacion",
	   "ComisionSMVMonedaLiquidacion",
	   "ComisionCavaliMonedaLiquidacion",
	   "FondoGarantiaMonedaLiquidacion",
	   "FondoLiquidacionMonedaLiquidacion",
	   "OtrasComisionesMonedaLiquidacion",
	   "ImpuestoIGVMonedaLiquidacion",
	   
	   MVTOCMBTE."NombreTipoCliente",
	   MVTOCMBTE."EsClienteFinal",
	   MVTOCMBTE."CodigoBroker",
	   MVTOCMBTE."LiquidaBancoCustodio",
	   MVTOCMBTE."CodigoBancoCustodio",
	   	MVTOCMBTE."CodigoBancoLiquidador",
	   
	   --CUENTA CUSTODIO
	   MVTOCMBTE."IdCuentaCustodioPortafolio",
	   "CuentaCustodioPortafolio"."NombreModeloCustodia" as "NombreModeloCustodia",
	   "CuentaCustodioPortafolio"."NombreTipoCustodia" as "NombreTipoCustodia",
	   "CuentaCustodioPortafolio"."NombreCompletoBancoCustodio" as "NombreCompletoBancoCustodio",
	   "CuentaCustodioPortafolio"."NombreCompletoBancoLiquidador" as "NombreCompletoBancoLiquidador",
			   
	   
	   
	   MVTOCMBTE."ISIN",
	   ESTADOPLACAV."Nombre" AS "NombreEstadoCavali",
	   MVTOCMBTE."ReajustesVAC",
	   MVTOCMBTE."Intereses",
	   
	   COALESCE(TOTCOBROS."COMMVTOCMBTE",0) AS "COMMVTOCMBTE",
	   COALESCE(TOTCOBROS."DERMVTOCMBTE",0) AS "DERMVTOCMBTE",
	   COALESCE(TOTCOBROS."GASMVTOCMBTE",0) AS "GASMVTOCMBTE",
	   COALESCE(TOTCOBROS."IMPMVTOCMBTE",0) AS "IMPMVTOCMBTE",
	   COALESCE(TOTCOBROS."RETMVTOCMBTE",0) AS "RETMVTOCMBTE",
	   
	   (COALESCE(TOTCOBROSPERU."MontoMovimientoComprobanteCobrosTotal",0) + COALESCE(TOTCOBROS."COMMVTOCMBTE",0) + COALESCE(TOTCOBROS."DERMVTOCMBTE",0) + COALESCE(TOTCOBROS."GASMVTOCMBTE",0) + COALESCE(TOTCOBROS."IMPMVTOCMBTE",0) + COALESCE(TOTCOBROS."RETMVTOCMBTE",0)) AS "MontoMovimientoComprobanteCobro",
	   
	   COALESCE(TOTCOBROS."COMMVTOLIQ",0) AS "COMMVTOLIQ",
	   COALESCE(TOTCOBROS."DERMVTOLIQ",0) AS "DERMVTOLIQ",
	   COALESCE(TOTCOBROS."GASMVTOLIQ",0) AS "GASMVTOLIQ",
	   COALESCE(TOTCOBROS."IMPMVTOLIQ",0) AS "IMPMVTOLIQ",
	   COALESCE(TOTCOBROS."RETMVTOLIQ",0) AS "RETMVTOLIQ",
	   (COALESCE(TOTCOBROSPERU."MontoLiquidacionCobrosTotal",0) + COALESCE(TOTCOBROS."COMMVTOLIQ",0) + COALESCE(TOTCOBROS."DERMVTOLIQ",0) + COALESCE(TOTCOBROS."GASMVTOLIQ",0) + COALESCE(TOTCOBROS."IMPMVTOLIQ",0) +  COALESCE(TOTCOBROS."RETMVTOLIQ",0)) AS "MontoLiquidacionCobro",
	   
	   	   
	   COALESCE(TOTCOBROS."IdMonedaMovimientoComprobanteCobro",TOTCOBROSPERU."IdMonedaMovimientoComprobanteCobro",'') AS "IdMonedaMovimientoComprobanteCobro",	   
	   COALESCE(TOTCOBROS."IdMonedaLiquidacionCobro",TOTCOBROSPERU."IdMonedaLiquidacionCobro",'') AS "IdMonedaLiquidacionCobro",	
	   
	   CASE
		 WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('POL') THEN MVTOCMBTE."Monto" + COALESCE(TOTCOBROSPERU."MontoMovimientoComprobanteCobrosTotal",0)
		 WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'BEE') THEN  
  	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                    + (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                       COALESCE(TOTCOBROS."DERMVTO",0) + 
	                       COALESCE(TOTCOBROS."GASMVTO",0) + 
	                       COALESCE(TOTCOBROS."IMPMVTO",0) + 
	                       COALESCE(TOTCOBROS."RETMVTO",0)))
	     WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') AND MVTOCMBTE."IdTipoOperacion" = 'ANVOTC' THEN  
	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                    + (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                       COALESCE(TOTCOBROS."DERMVTO",0) + 
	                       COALESCE(TOTCOBROS."GASMVTO",0) + 
	                       COALESCE(TOTCOBROS."IMPMVTO",0) + 
	                       COALESCE(TOTCOBROS."RETMVTO",0)))
 	     WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'BEE') AND MVTOCMBTE."IdTipoOperacion" ='VENOTC' THEN
 	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                   + (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                      COALESCE(TOTCOBROS."DERMVTO",0) + 
 	                      COALESCE(TOTCOBROS."GASMVTO",0) + 
 	                      COALESCE(TOTCOBROS."IMPMVTO",0) + 
 	                      COALESCE(TOTCOBROS."RETMVTO",0)))	        
	     WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN  
	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                    - (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                       COALESCE(TOTCOBROS."DERMVTO",0) + 
	                       COALESCE(TOTCOBROS."GASMVTO",0) + 
	                       COALESCE(TOTCOBROS."IMPMVTO",0) + 
	                       COALESCE(TOTCOBROS."RETMVTO",0))) 	                       	     
 	     WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('POL') THEN MVTOCMBTE."Monto" - COALESCE(TOTCOBROSPERU."MontoMovimientoComprobanteCobrosTotal",0)
		 WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'BEE') THEN
 	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                   - (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                      COALESCE(TOTCOBROS."DERMVTO",0) + 
 	                      COALESCE(TOTCOBROS."GASMVTO",0) + 
 	                      COALESCE(TOTCOBROS."IMPMVTO",0) + 
 	                      COALESCE(TOTCOBROS."RETMVTO",0))) 
         WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN
             (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                   + (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                      COALESCE(TOTCOBROS."DERMVTO",0) + 
 	                      COALESCE(TOTCOBROS."GASMVTO",0) + 
 	                      COALESCE(TOTCOBROS."IMPMVTO",0) + 
 	                      COALESCE(TOTCOBROS."RETMVTO",0)))
 		ELSE 0
 	   END AS "MontoFinal",  --NO SIRVE, LOS COBROS PUEDEN ESTAR EN DIFERNTES MONEDAS	   
 	   
	   CASE
	     WHEN TIPOOPERACION."Accion" = 'COMPRA' AND PlaConf."Valor" = 'PE' AND MVTOCMBTE."IdTipoComprobante" IN ('POL', 'FOE', 'BOE') THEN MVTOCMBTE."MontoMovimientoComprobante" + COALESCE(TOTCOBROSPERU."MontoMovimientoComprobanteCobrosTotal",0)
   	     WHEN TIPOOPERACION."Accion" = 'VENTA' AND PlaConf."Valor" = 'PE' AND MVTOCMBTE."IdTipoComprobante" IN ('POL', 'FOE', 'BOE') THEN MVTOCMBTE."MontoMovimientoComprobante" - COALESCE(TOTCOBROSPERU."MontoMovimientoComprobanteCobrosTotal",0)
	     WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'BEE') THEN  
  	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoMovimientoComprobante", FormMvtoCmbte."MontoDecimales", FormMvtoCmbte."MontoTrucaRedondea"), 0)
  	                     + (COALESCE(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                        COALESCE(TOTCOBROS."DERMVTOCMBTE",0) + 
  	                        COALESCE(TOTCOBROS."GASMVTOCMBTE",0) + 
	                        COALESCE(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                        COALESCE(TOTCOBROS."RETMVTOCMBTE",0)))
	     WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') AND MVTOCMBTE."IdTipoOperacion" = 'ANVOTC' THEN  
	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                    + (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                       COALESCE(TOTCOBROS."DERMVTO",0) + 
	                       COALESCE(TOTCOBROS."GASMVTO",0) + 
	                       COALESCE(TOTCOBROS."IMPMVTO",0) + 
	                       COALESCE(TOTCOBROS."RETMVTO",0)))
 	     WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'BEE') AND MVTOCMBTE."IdTipoOperacion" ='VENOTC' THEN
 	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                   + (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                      COALESCE(TOTCOBROS."DERMVTO",0) + 
 	                      COALESCE(TOTCOBROS."GASMVTO",0) + 
 	                      COALESCE(TOTCOBROS."IMPMVTO",0) + 
 	                      COALESCE(TOTCOBROS."RETMVTO",0)))	        							
 	     WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN  
  	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoMovimientoComprobante", FormMvtoCmbte."MontoDecimales", FormMvtoCmbte."MontoTrucaRedondea"), 0)
  	                     - (COALESCE(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                        COALESCE(TOTCOBROS."DERMVTOCMBTE",0) + 
  	                        COALESCE(TOTCOBROS."GASMVTOCMBTE",0) + 
	                        COALESCE(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                        COALESCE(TOTCOBROS."RETMVTOCMBTE",0)))
 	     WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('NEO') THEN MVTOCMBTE."MontoMovimientoComprobante" - COALESCE(TOTCOBROSPERU."MontoMovimientoComprobanteCobrosTotal",0)
 		 WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'BEE') THEN
 	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoMovimientoComprobante", FormMvtoCmbte."MontoDecimales", FormMvtoCmbte."MontoTrucaRedondea"), 0) 
  	                    - (COALESCE(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                       COALESCE(TOTCOBROS."DERMVTOCMBTE",0) + 
	                       COALESCE(TOTCOBROS."GASMVTOCMBTE",0) + 
	                       COALESCE(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                       COALESCE(TOTCOBROS."RETMVTOCMBTE",0)))
 	     WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN
 	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoMovimientoComprobante", FormMvtoCmbte."MontoDecimales", FormMvtoCmbte."MontoTrucaRedondea"), 0) 
  	                    + (COALESCE(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                       COALESCE(TOTCOBROS."DERMVTOCMBTE",0) + 
	                       COALESCE(TOTCOBROS."GASMVTOCMBTE",0) + 
	                       COALESCE(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                       COALESCE(TOTCOBROS."RETMVTOCMBTE",0)))
 		ELSE 0
 	   END AS "MontoMovimientoComprobanteFinal", 
 	   	    	   
 	   CASE 
 		 WHEN TIPOOPERACION."Accion" = 'COMPRA' AND PlaConf."Valor" = 'PE' AND MVTOCMBTE."IdTipoComprobante" IN ('POL', 'FOE', 'BOE') THEN MVTOCMBTE."MontoLiquidacion" + COALESCE(TOTCOBROSPERU."MontoLiquidacionCobrosTotal",0)
 		 WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'BEE') THEN  
  	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoLiquidacion", FormMvtoLiq."MontoDecimales", FormMvtoLiq."MontoTrucaRedondea"), 0) 
                        + (COALESCE(TOTCOBROS."COMMVTOLIQ",0) + 
                           COALESCE(TOTCOBROS."DERMVTOLIQ",0) + 
  	                       COALESCE(TOTCOBROS."GASMVTOLIQ",0) + 
  	                       COALESCE(TOTCOBROS."IMPMVTOLIQ",0) + 
  	                       COALESCE(TOTCOBROS."RETMVTOLIQ",0)))
	     WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') AND MVTOCMBTE."IdTipoOperacion" = 'ANVOTC' THEN  
	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                    + (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                       COALESCE(TOTCOBROS."DERMVTO",0) + 
	                       COALESCE(TOTCOBROS."GASMVTO",0) + 
	                       COALESCE(TOTCOBROS."IMPMVTO",0) + 
	                       COALESCE(TOTCOBROS."RETMVTO",0)))
 	     WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'BEE') AND MVTOCMBTE."IdTipoOperacion" ='VENOTC' THEN
 	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                   + (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                      COALESCE(TOTCOBROS."DERMVTO",0) + 
 	                      COALESCE(TOTCOBROS."GASMVTO",0) + 
 	                      COALESCE(TOTCOBROS."IMPMVTO",0) + 
 	                      COALESCE(TOTCOBROS."RETMVTO",0)))	        						   
  	     WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN                    
  	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoLiquidacion", FormMvtoLiq."MontoDecimales", FormMvtoLiq."MontoTrucaRedondea"), 0) 
                        - (COALESCE(TOTCOBROS."COMMVTOLIQ",0) + 
                           COALESCE(TOTCOBROS."DERMVTOLIQ",0) + 
  	                       COALESCE(TOTCOBROS."GASMVTOLIQ",0) + 
  	                       COALESCE(TOTCOBROS."IMPMVTOLIQ",0) + 
  	                       COALESCE(TOTCOBROS."RETMVTOLIQ",0)))
  	     WHEN TIPOOPERACION."Accion" = 'VENTA' AND PlaConf."Valor" = 'PE' AND MVTOCMBTE."IdTipoComprobante" IN ('NEO') THEN MVTOCMBTE."MontoLiquidacion" - COALESCE(TOTCOBROSPERU."MontoLiquidacionCobrosTotal",0)
  	     WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'BEE') THEN
 	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoLiquidacion", FormMvtoLiq."MontoDecimales", FormMvtoLiq."MontoTrucaRedondea"), 0) 
 	                    - (COALESCE(TOTCOBROS."COMMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."DERMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."GASMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."IMPMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."RETMVTOLIQ",0)))
 	     WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN                  
 	         (COALESCE ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoLiquidacion", FormMvtoLiq."MontoDecimales", FormMvtoLiq."MontoTrucaRedondea"), 0) 
 	                    + (COALESCE(TOTCOBROS."COMMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."DERMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."GASMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."IMPMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."RETMVTOLIQ",0)))
 		ELSE 0
 	   END AS "MontoLiquidacionFinal",
 	    	   
 	   CASE 
			WHEN MVTOCMBTE."ActualizarTesoreria" = 'N' AND MVTOCMBTE."ActualizarCuentaInversion" = 'S' THEN 'CUENTA INVERSION'
			WHEN MVTOCMBTE."ActualizarTesoreria" = 'S' THEN MvtoCmbteForPag."NombreMedioDePago"
			ELSE ''
 	   END AS "ResumenMedioPago",
 	   MVTOCMBTE."CodigoIFRS",
 	   MVTOCMBTE."NemoSVS",
 	   MVTOCMBTE."MercadoSMV",
 	   MVTOCMBTE."ModalidadSMV",
 	   MVTOCMBTE."IdOperacion",
 	   MVTOCMBTE."FechaHoraMovimientoComprobante",
 	   MCREF."Referencia",
 	   REFDOC."Descripcion" AS "DescripcionReferencia",
 	   REFDOC."TipoReferencia",
 	   ASIG."IdOperacionSpot" AS "FolioSpot",
 	   FACSPOT."IdComprobante" AS "SecuenciaComprobanteSpot", 
	   COALESCE(FACSPOT."IdEstadoFacturacion",SPOT."Estado")  AS "EstadoSpot",
 	   
 	   --CORREDOR CORRESPONSAL
 	   CCPBCC."IdCorredorCorresponsal",
 	   COALESCE(CCPBCC."Codigo", '') AS "CodigoCorredorCorresponsal",
 	   COALESCE(MVTOCMBTE."IdTipoLiquidacionComisionFactura",'') AS "IdTipoLiquidacionComisionFactura",
 	   COALESCE(CTLF."TipoLiquidacion",'') AS "TipoLiquidacion",
 	   COALESCE("MVTOCMBTE"."ModPrefFact",'') AS "ModPrefFact",
 	   COALESCE("DetalleTipoIndicador"."DetalleIndicador",'') AS "ModPrefFactDescripcion",
	   ASIG."IdAsignacionMasiva" AS "IdAsignacionMasiva",
	   TA."Nombre" AS "TipoAsignacion",
	   TRANSACCION."MarcaAsignacion"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MVTOCMBTE

  LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" ASIG 	ON (MVTOCMBTE."IdAsignacion" = ASIG."IdAsignacion")	
  LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot"  SPOT     ON (ASIG."IdOperacionSpot" = SPOT."IdOperacionSpot")    
  LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" FACSPOT ON ( SPOT."IdComprobanteFacturacion" = FACSPOT."IdMovimientoComprobante")  

  LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS TRANSACCION  ON TRANSACCION."IdTransaccion" = MVTOCMBTE."IdTransaccion"
  
   INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CONCEPTOMOV   ON CONCEPTOMOV."IdConcepto" = MVTOCMBTE."IdConceptoMovimiento"
   INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS EMPRESA	  ON EMPRESA."IdEmpresa" = MVTOCMBTE."IdEmpresa"
   LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CONSCLI ON CONSCLI."IdCliente" = MVTOCMBTE."IdCliente"   
   LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS TIPOPRODUCTO    ON TIPOPRODUCTO."IdTipoProducto" = MVTOCMBTE."IdTipoProducto"
   LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION      ON TIPOOPERACION."IdTipoOperacion" = MVTOCMBTE."IdTipoOperacion"
   LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MONEDA 	    ON MONEDA."IdMoneda" = MVTOCMBTE."IdMoneda"   
   INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MonMvtoCmbte  ON MonMvtoCmbte."IdMoneda" = MVTOCMBTE."IdMonedaMovimientoComprobante"   
   INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MonLiq 	    ON MonLiq."IdMoneda" = MVTOCMBTE."IdMonedaLiquidacion"   
   LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS TIPOCOMPROBANTE      ON TIPOCOMPROBANTE."IdTipoComprobante" = MVTOCMBTE."IdTipoComprobante"
   LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS BOVEDA      ON BOVEDA."IdBoveda" = MVTOCMBTE."IdBoveda"
   INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CONDICIONDELIQUIDACION      ON CONDICIONDELIQUIDACION."IdCondicionDeLiquidacion" = MVTOCMBTE."IdCondicionLiquidacion"
   LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" AS TIPOREAJUSTE         ON TIPOREAJUSTE."IdTipoReajuste" = MVTOCMBTE."IdTipoReajuste"
   INNER JOIN "BSCL.Facturacion.BD::FAC.TipoConvenio" AS TIPOCONVENIO  	  ON TIPOCONVENIO."IdTipoConvenio" = MVTOCMBTE."IdTipoConvenio"
   INNER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS ESTADOFACTURACION     ON ESTADOFACTURACION."IdEstado" = MVTOCMBTE."IdEstadoFacturacion" AND UPPER(EstadoFacturacion."NombreEntidad") = UPPER('FAC.MovimientoComprobante')
   INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTADOPLA      ON ESTADOPLA."IdEstado" = ESTADOFACTURACION."IdEstado"
   
   LEFT JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS ESTADOCAVALI     ON ESTADOCAVALI."IdEstado" = MVTOCMBTE."IdEstadoCavali" AND UPPER(EstadoFacturacion."NombreEntidad") = UPPER('FAC.MovimientoComprobanteCavali')
   LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTADOPLACAV ON ESTADOPLACAV."IdEstado" = ESTADOCAVALI."IdEstado"
   
   
   LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosTotalizados" AS TOTCOBROS ON TOTCOBROS."IdMovimientoComprobante" = MVTOCMBTE."IdMovimientoComprobante"
   LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosPeruTotalizados" AS TOTCOBROSPERU ON TOTCOBROSPERU."IdMovimientoComprobante" = MVTOCMBTE."IdMovimientoComprobante"
   LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = MVTOCMBTE."IdPortafolio"
   LEFT JOIN "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden" AS ConsFormCusOrd ON ConsFormCusOrd."IdTipoFormaCustodiaOrden" = MVTOCMBTE."IdFormaCustodia"
   LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" AS "CuentaCustodioPortafolio" ON MVTOCMBTE."IdCuentaCustodioPortafolio" = "CuentaCustodioPortafolio"."IdCuentaCustodioPortafolio"
   
   INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS CONSINDPORT 
      ON CONSINDPORT."IdCliente" = MVTOCMBTE."IdCliente" 
     AND CONSINDPORT."IdPortafolio" = MVTOCMBTE."IdPortafolio"  
     AND CONSINDPORT."IdUso" = 'PRE'
     AND CONSINDPORT."CodigoTipoIndicador" = 'GEN_OCCINV'
   LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS ConsIndPortConDerBcs
      ON ConsIndPortConDerBcs."IdCliente" = MVTOCMBTE."IdCliente" 
     AND ConsIndPortConDerBcs."IdPortafolio" = MVTOCMBTE."IdPortafolio" 
     AND ConsIndPortConDerBcs."CodigoTipoIndicador" = 'FAC_CDRBCS'
   LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS ConsIndPortConDerBec
      ON ConsIndPortConDerBec."IdCliente" = MVTOCMBTE."IdCliente" 
     AND ConsIndPortConDerBec."IdPortafolio" = MVTOCMBTE."IdPortafolio" 
     AND ConsIndPortConDerBec."CodigoTipoIndicador" = 'FAC_CDRBEC' 
   LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS PlaConf
      ON PlaConf."IdEmpresa" = MVTOCMBTE."IdEmpresa"
     AND PlaConf."CodigoParametro" = 'PLA.LocalizacionSistema'
      
   LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteUnaFormaDePago" AS MvtoCmbteForPag ON MvtoCmbteForPag."IdMovimientoComprobante" = MVTOCMBTE."IdMovimientoComprobante"        
   LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USUARIO
      ON USUARIO."IdUsuario" = MVTOCMBTE."IdUsuarioBloqueo"      
   LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS FormMvto 
      ON FormMvto."IdMoneda" = MVTOCMBTE."IdMoneda"
     AND FormMvto."IdTipoOperacion" = ''
     AND FormMvto."IdTipoInstrumento" = ''
     AND FormMvto."IdUsoMonedaFormato" = 'GEN'
   LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS FormMvtoCmbte 
      ON FormMvtoCmbte."IdMoneda" = MVTOCMBTE."IdMonedaMovimientoComprobante"
     AND FormMvtoCmbte."IdTipoOperacion" = ''
     AND FormMvtoCmbte."IdTipoInstrumento" = ''
     AND FormMvtoCmbte."IdUsoMonedaFormato" = 'GEN'  
   LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS FormMvtoLiq
      ON FormMvtoLiq."IdMoneda" = MVTOCMBTE."IdMonedaLiquidacion"
     AND FormMvtoLiq."IdTipoOperacion" = ''
     AND FormMvtoLiq."IdTipoInstrumento" = ''
     AND FormMvtoLiq."IdUsoMonedaFormato" = 'GEN'
   LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarProductoBovedaCorredorCorresponsal" AS CCPBCC
   	ON CCPBCC."IdTipoProducto"  = MVTOCMBTE."IdTipoProducto"
   		AND CCPBCC."IdBoveda" = MVTOCMBTE."IdBoveda"
   LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarTipoLiquidacionFactura" AS CTLF
  	ON CTLF."IdTipoLiquidacionComisionFactura" = MVTOCMBTE."IdTipoLiquidacionComisionFactura"
  		AND CTLF."IdEstado" = 'ACT'
   LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS "DetalleTipoIndicador" 
    ON "DetalleTipoIndicador"."CodigoTipoIndicador" = 'FAC_MODFAC' 
    	AND "DetalleTipoIndicador"."Codigo" = MVTOCMBTE."ModPrefFact" 
    	AND "DetalleTipoIndicador"."IdEmpresa" = MVTOCMBTE."IdEmpresa"
   LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteReferencia" AS MCREF
    ON MVTOCMBTE."IdMovimientoComprobante" = CASE WHEN (MCREF."IdMovimientoComprobante" = '' OR MCREF."IdMovimientoComprobante" IS NULL) THEN '0' ELSE MCREF."IdMovimientoComprobante" END
   LEFT JOIN "BSCL.Facturacion.BD::FAC.ReferenciaDocumento" AS REFDOC
    ON MCREF."TipoReferencia" = REFDOC."TipoReferencia"
   LEFT JOIN "BSCL.Orden.BD::ORD.TipoAsignacion" AS TA ON TA."IdTipoAsignacion" = ASIG."IdTipoAsignacion";
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" TO  BCS_DESARROLLADOR WITH GRANT OPTION;