SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosComisiones"; 
GO

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosComisiones" AS

	SELECT 
		  MCCOB."IdCobro"
		, MVTOCMBTE."IdMovimientoComprobante"
	   	, MVTOCMBTE."IdEmpresa"
	   	, EMPRESA."Nombre" AS "NombreEmpresa" 
	   	, "IdConceptoMovimiento"
	   	, CONCEPTOMOV."Nombre" AS "NombreConceptoMovimiento"
	   	, "SecuenciaAgrupacion"
	   	, MVTOCMBTE."IdCliente"
	   	, MVTOCMBTE."DocumentoIdentidad"
	   	, MVTOCMBTE."TipoDocumentoIdentidad"       
	   	, "NombreLargoCliente"
	   	, "DireccionFacturacion"	     
	   	, MVTOCMBTE."IdPortafolio"
   	   	, PORTAFOLIO."CodigoPortafolio"
   	   	, PORTAFOLIO."Proposito"
   	   	, (PORTAFOLIO."CodigoPortafolio" ||' '|| PORTAFOLIO."Proposito") AS "NombrePortafolio"
       	, "IdEmpleadoDigitador"
       	, "NombreEmpleadoDigitador"
       	, "IdSucursalEmpleadoDigitador"
       	, "NombreSucursalEmpleadoDigitador"
       	, "IdEmpleadoEjecutivoPortafolio"
       	, "NombreEmpleadoEjecutivoPortafolio"
       	, "IdSucursalEmpleadoEjecutivoPortafolio"
       	, "NombreSucursalEmpleadoEjecutivoPortafolio"
	   	, "FolioCartera"	   
	   	, "IdInstrumento"
	   --"NemotecnicoInstrumento",
	   	, CASE 
	    	WHEN MVTOCMBTE."IdTipoInstrumento" IN ('PRT', 'EMP', 'SIM','RRV','RRF') THEN "NemotecnicoSubyacente" 
	        ELSE "NemotecnicoInstrumento"
	      END AS "NemotecnicoInstrumento"
	   	, MVTOCMBTE."IdTipoInstrumento"
	   	, "NombreTipoInstrumento"
	   	, "IdClasificacionTipoInstrumento"
	   	, "NombreClasificacionTipoInstrumento"	   
	   	, "IdInstrumentoSubyacente"
	   	, "NemotecnicoSubyacente"
	   	, "IdTipoInstrumentoSubyacente"
	   	, "NombreTipoInstrumentoSubyacente"	   
	   	, CASE 
	   		WHEN MVTOCMBTE."IdTipoInstrumento" IN ('PRT', 'EMP', 'SIM','RRV','RRF') THEN "IdInstrumentoSubyacente" 
	   		ELSE "IdInstrumento"
	   	  END AS "IdInstrumentoUnificado"		      
	   	, CASE 
	   	WHEN  MVTOCMBTE."IdTipoInstrumento" IN ('PRT', 'EMP', 'SIM','RRV','RRF') THEN "NemotecnicoSubyacente" 
	   		ELSE "NemotecnicoInstrumento"
	   	  END AS "NemotecnicoInstrumentoUnificado"
	   	, CASE 
	   		WHEN MVTOCMBTE."IdTipoInstrumento" IN ('PRT', 'EMP', 'SIM','RRV','RRF') THEN "IdTipoInstrumentoSubyacente" 
	   		ELSE MVTOCMBTE."IdTipoInstrumento"
	   	  END AS "IdTipoInstrumentoUnificado"
	   	, CASE 
	   		WHEN MVTOCMBTE."IdTipoInstrumento" IN ('PRT', 'EMP', 'SIM','RRV','RRF') THEN "NombreTipoInstrumentoSubyacente" 
	   		ELSE "NombreTipoInstrumento"
	      END AS "NombreTipoInstrumentoUnificado"
	   	, "IdEmisor"
	   	, "RazonSocialEmisor"
	   	, "FechaEmision"
	   	, "TasaEmision" 	        
	   	, MVTOCMBTE."IdTipoProducto"
	   	, TIPOPRODUCTO."Nombre" AS "NombreTipoProducto"
	   	, "CodigoTipoProducto" 
	   	, MVTOCMBTE."IdMoneda"
	   	, MONEDA."Nombre" AS "NombreMoneda"
	   	, MVTOCMBTE."IdMonedaSpot"
	   	, MVTOCMBTE."IdMonedaMovimientoComprobante" AS "IdMonedaMovimientoComprobante" 
       	, MonMvtoCmbte."Nombre" AS "NombreMonedaMovimientoComprobante"
	   	, MVTOCMBTE."IdMonedaLiquidacion"	   
	   	, MonLiq."Nombre" AS "NombreMonedaLiquidacion"
	   	, MVTOCMBTE."IdTipoComprobante"
	   	, TIPOCOMPROBANTE."Codigo" AS "CodigoTipoComprobante"
	   	, TIPOCOMPROBANTE."Nombre" AS "NombreTipoComprobante" 
	  	, "IdComprobante" 
	   	, "IdComprobanteReferencia" 
	   	, "FolioComprobanteReferencia"
	   	, "FechaComprobanteReferencia" 	    
	   	, MVTOCMBTE."IdTipoOperacion"
	   	, "CodigoTipoOperacion"
	   	, TIPOOPERACION."Nombre" AS "NombreTipoOperacion"
	   	, TIPOOPERACION."Accion" AS "AccionTipoOperacion" 	        
	   	, "IdMovimientoRelacionado" 	        
	   	, "IdMovimientoOdRelacionado"
	   	, MVTOCMBTE."IdBoveda"
	   	, BOVEDA."Codigo" AS "CodigoBoveda"
	   	, BOVEDA."Nombre" AS "NombreBoveda"
	   	, "IdCorredoraContraparte"
	   	, CASE 
	   	  	WHEN "IdCorredoraContraparte" = 0 THEN ''
	   	  	ELSE TO_NVARCHAR ("IdCorredoraContraparte")
	      END AS "IdCorredoraContraparteString"
	   	, MVTOCMBTE."IdCondicionLiquidacion" 
	   	, CONDICIONDELIQUIDACION."Codigo" AS "CodigoCondicionDeLiquidacion"
	   	, CONDICIONDELIQUIDACION."Nombre" AS "NombreCondicionDeLiquidacion"
	   	, "FechaLiquidacion" 
	   	, "IdBolsa"
	   	, "CodigoBolsa" 	        
	   	, MVTOCMBTE."IdTipoReajuste"
	   	, TIPOREAJUSTE."Nombre" AS "NombreTipoReajuste" 	        
	   	, MVTOCMBTE."IdTipoConvenio"
	   	, TIPOCONVENIO."Nombre" AS "NombreTipoConvenio"	         
	   	, MVTOCMBTE."IdFormaCustodia"
	   	, ConsFormCusOrd."Nombre" AS "NombreFormaCustodia"	   
	   	, MVTOCMBTE."IdEstadoFacturacion" 
	   	, ESTADOFACTURACION."IdEstado" AS "EstadoFacturacion"	        
	   	, ESTADOPLA."Nombre" AS "NombreEstadoFacturacion"	   
	   	, "IdOrden"
	   	, CASE 
	   	  	WHEN "IdOrden" = 0 THEN ''
	   	  	ELSE TO_NVARCHAR ("IdOrden")
	   	  END AS "IdOrdenString"
	   	, "IdAsignacion"
	   	, "IdAsignacionRef"
	   	, Canal."IdTipoCanalIngreso"
	   	, "CodigoCanalIngreso" 
	   	, Canal."Nombre" AS "NombreCanalIngreso"
	   	, "FechaOrden"
	   	, "IdTransaccion" 	      
       	, "FolioTransaccion"        
	   	, CASE
	     	WHEN MVTOCMBTE."IdTipoComprobante" = 'FOE' and "FechaTransaccion" = '0001-01-01' THEN CAST("FechaMovimientoComprobante" AS Date)
       		ELSE CAST("FechaTransaccion" AS Date) 
       	  END AS "FechaTransaccion"	   
	   	, "FechaTransaccion" AS "FechaTransaccionHora"
	   	, "FechaVencimiento"
	   	, "FechaParidad"
	   	, "ValorParidad"
	   	, "ValorTipoCambio"
	   	, "ValorCaptacionMonedaOrigen"
	   	, "FolioComprobante"	        
	   	, "Tasa"
	   	, MVTOCMBTE."TasaOperacion"  -- Para Venta corta
	   	, CASE 
	       	WHEN "EsFungible" = 'S' AND "IdClasificacionTipoInstrumento" = 'IF' THEN "ValorRescate" 
	   		ELSE "Cantidad" 
	   	  END AS "Cantidad"
	   	, "Precio"	 
	   	, COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), MVTOCMBTE."Monto") AS "Monto"
	   	, COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoMovimientoComprobante", FormMvtoCmbte."MontoDecimales", FormMvtoCmbte."MontoTrucaRedondea"), MVTOCMBTE."MontoMovimientoComprobante") AS "MontoMovimientoComprobante"	   
       	, COALESCE("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoLiquidacion", FormMvtoLiq."MontoDecimales", FormMvtoLiq."MontoTrucaRedondea"), MVTOCMBTE."MontoLiquidacion") AS "MontoLiquidacion"
	   	, "MontoDescuento"
	   	, "MontoInteres"
	   	, "Glosa"
	   	, "ValorRescate"
	   	, "TasaAgente"	        
	   	, "Spread"
	   	, "DiasPlazo"
	   	, CAST("FechaMovimientoComprobante" AS Date) AS "FechaMovimientoComprobante"
	   	, MVTOCMBTE."FechaIngresoRegistro"
	   	, MVTOCMBTE."FechaSistema" 
	   	, "EsComprobIndividual"
	   	, MVTOCMBTE."EsCuentaPropia"
	   	, "IndicadorOrdenDirecta"
	   	, "EsPrecioPrima"
	   	, "EsEnRueda"
	   	, "EsRuteo"
	   	, "EsIngresoAutomatico"
	   	, "EsAPlazo"
	   	, "ActualizarCustodia"
	   	, "ActualizarCuentaInversion"
	   	, "ActualizarTesoreria"
	   	, MVTOCMBTE."EsVentaCorta"
	   	, "PrecioAPlazo"
	   	, "MontoAPlazo"
	   	, MVTOCMBTE."IdUsuario"
	   	, MVTOCMBTE."IdUsuarioBloqueo"	   	   
	   	, CASE 
	     	WHEN MVTOCMBTE."IdUsuarioBloqueo" <> 0 THEN Usuario."CodigoUsuario"
	   		ELSE ''
	   	  END AS "CodigoUsuarioBloqueo"
	   	, MVTOCMBTE."IdCarga"
	   	, CONSCLI."IdPersona"
	   	, COALESCE(ConsIndPortConDerBcs."ValorIndicador",'N') AS "EsConvenioDerechBCS"
	   	, COALESCE(ConsIndPortConDerBec."ValorIndicador",'N') AS "EsConvenioDerechBEC"	   
	   	, COALESCE(TOTCOBROS."COMMVTO",0) AS "COMMVTO"
	   	, COALESCE(TOTCOBROS."DERMVTO",0) AS "DERMVTO"
	   	, COALESCE(TOTCOBROS."GASMVTO",0) AS "GASMVTO"
	   	, COALESCE(TOTCOBROS."IMPMVTO",0) AS "IMPMVTO"
	   	, COALESCE(TOTCOBROS."RETMVTO",0) AS "RETMVTO"
	   	, (COALESCE(TOTCOBROS."COMMVTO",0) + COALESCE(TOTCOBROS."DERMVTO",0) + COALESCE(TOTCOBROS."GASMVTO",0) + COALESCE(TOTCOBROS."IMPMVTO",0) + COALESCE(TOTCOBROS."RETMVTO",0)) AS "MontoCobro"
	   
	   	, "ComisionBVLMonedaCobro"
	   	, "ComisionSABMonedaCobro"
	   	, "ComisionSMVMonedaCobro"
	   	, "ComisionCavaliMonedaCobro"
	   	, "FondoGarantiaMonedaCobro"
	   	, "FondoLiquidacionMonedaCobro"
	   	, "OtrasComisionesMonedaCobro"
	   	, "ImpuestoIGVMonedaCobro"
	   
	   	, "ComisionBVLMonedaMovimiento"
	   	, "ComisionSABMonedaMovimiento"
	   	, "ComisionSMVMonedaMovimiento"
	   	, "ComisionCavaliMonedaMovimiento"
	   	, "FondoGarantiaMonedaMovimiento"
	   	, "FondoLiquidacionMonedaMovimiento"
	   	, "OtrasComisionesMonedaMovimiento"
	   	, "ImpuestoIGVMonedaMovimiento"
	   
	   	, "ComisionBVLMonedaLiquidacion"
	   	, "ComisionSABMonedaLiquidacion"
	   	, "ComisionSMVMonedaLiquidacion"
	   	, "ComisionCavaliMonedaLiquidacion"
	  	, "FondoGarantiaMonedaLiquidacion"
	   	, "FondoLiquidacionMonedaLiquidacion"
	   	, "OtrasComisionesMonedaLiquidacion"
	  	, "ImpuestoIGVMonedaLiquidacion"
	   
	   	, "NombreTipoCliente"
	   	, MVTOCMBTE."EsClienteFinal"
	   	, "CodigoBroker"
	   	, MVTOCMBTE."LiquidaBancoCustodio"
	   	, MVTOCMBTE."CodigoBancoCustodio"
	   	, MVTOCMBTE."CodigoBancoLiquidador"
	   
	   --CUENTA CUSTODIO
	   	, MVTOCMBTE."IdCuentaCustodioPortafolio"
	   	, "CuentaCustodioPortafolio"."NombreModeloCustodia" as "NombreModeloCustodia"
	   	, "CuentaCustodioPortafolio"."NombreTipoCustodia" as "NombreTipoCustodia"
	   	, "CuentaCustodioPortafolio"."NombreCompletoBancoCustodio" as "NombreCompletoBancoCustodio"
	   	, "CuentaCustodioPortafolio"."NombreCompletoBancoLiquidador" as "NombreCompletoBancoLiquidador"
			   
	   
	   
	   	, "ISIN"
	   	, ESTADOPLACAV."Nombre" AS "NombreEstadoCavali"
	   	, "ReajustesVAC"
	   	, "Intereses"
	   
	   	, COALESCE(TOTCOBROS."COMMVTOCMBTE",0) AS "COMMVTOCMBTE"
	   	, COALESCE(TOTCOBROS."DERMVTOCMBTE",0) AS "DERMVTOCMBTE"
	   	, COALESCE(TOTCOBROS."GASMVTOCMBTE",0) AS "GASMVTOCMBTE"
	   	, COALESCE(TOTCOBROS."IMPMVTOCMBTE",0) AS "IMPMVTOCMBTE"
	   	, COALESCE(TOTCOBROS."RETMVTOCMBTE",0) AS "RETMVTOCMBTE"
	   
	   	, (COALESCE(TOTCOBROSPERU."MontoMovimientoComprobanteCobrosTotal",0) + COALESCE(TOTCOBROS."COMMVTOCMBTE",0) + COALESCE(TOTCOBROS."DERMVTOCMBTE",0) + COALESCE(TOTCOBROS."GASMVTOCMBTE",0) + COALESCE(TOTCOBROS."IMPMVTOCMBTE",0) + COALESCE(TOTCOBROS."RETMVTOCMBTE",0)) AS "MontoMovimientoComprobanteCobro"
	   
	   	, COALESCE(TOTCOBROS."COMMVTOLIQ",0) AS "COMMVTOLIQ"
	   	, COALESCE(TOTCOBROS."DERMVTOLIQ",0) AS "DERMVTOLIQ"
	   	, COALESCE(TOTCOBROS."GASMVTOLIQ",0) AS "GASMVTOLIQ"
	   	, COALESCE(TOTCOBROS."IMPMVTOLIQ",0) AS "IMPMVTOLIQ"
	   	, COALESCE(TOTCOBROS."RETMVTOLIQ",0) AS "RETMVTOLIQ"
	   	, (COALESCE(TOTCOBROSPERU."MontoLiquidacionCobrosTotal",0) + COALESCE(TOTCOBROS."COMMVTOLIQ",0) + COALESCE(TOTCOBROS."DERMVTOLIQ",0) + COALESCE(TOTCOBROS."GASMVTOLIQ",0) + COALESCE(TOTCOBROS."IMPMVTOLIQ",0) +  COALESCE(TOTCOBROS."RETMVTOLIQ",0)) AS "MontoLiquidacionCobro"
	   
	   	   
	   	, COALESCE(TOTCOBROS."IdMonedaMovimientoComprobanteCobro",TOTCOBROSPERU."IdMonedaMovimientoComprobanteCobro",'') AS "IdMonedaMovimientoComprobanteCobro"	   
	   	, COALESCE(TOTCOBROS."IdMonedaLiquidacionCobro",TOTCOBROSPERU."IdMonedaLiquidacionCobro",'') AS "IdMonedaLiquidacionCobro"	
	   
	   	, CASE
		 	WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('POL') THEN MVTOCMBTE."Monto" + COALESCE(TOTCOBROSPERU."MontoMovimientoComprobanteCobrosTotal",0)
		 	WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE') THEN  
  	         (coalesce ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                    + (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                       COALESCE(TOTCOBROS."DERMVTO",0) + 
	                       COALESCE(TOTCOBROS."GASMVTO",0) + 
	                       COALESCE(TOTCOBROS."IMPMVTO",0) + 
	                       COALESCE(TOTCOBROS."RETMVTO",0))) 
	     	WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN  
	         (coalesce ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                    - (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                       COALESCE(TOTCOBROS."DERMVTO",0) + 
	                       COALESCE(TOTCOBROS."GASMVTO",0) + 
	                       COALESCE(TOTCOBROS."IMPMVTO",0) + 
	                       COALESCE(TOTCOBROS."RETMVTO",0))) 	                       	     
 	     	WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('POL') THEN MVTOCMBTE."Monto" - COALESCE(TOTCOBROSPERU."MontoMovimientoComprobanteCobrosTotal",0)
		 	WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE') THEN
 	         (coalesce ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                   - (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                      COALESCE(TOTCOBROS."DERMVTO",0) + 
 	                      COALESCE(TOTCOBROS."GASMVTO",0) + 
 	                      COALESCE(TOTCOBROS."IMPMVTO",0) + 
 	                      COALESCE(TOTCOBROS."RETMVTO",0))) 
         	WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN
             (coalesce ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), 0)
	                   + (COALESCE(TOTCOBROS."COMMVTO",0) + 
	                      COALESCE(TOTCOBROS."DERMVTO",0) + 
 	                      COALESCE(TOTCOBROS."GASMVTO",0) + 
 	                      COALESCE(TOTCOBROS."IMPMVTO",0) + 
 	                      COALESCE(TOTCOBROS."RETMVTO",0)))
 			ELSE 0
 	      END AS "MontoFinal"  --NO SIRVE, LOS COBROS PUEDEN ESTAR EN DIFERNTES MONEDAS	   
 	   
	    , CASE
	     	WHEN TIPOOPERACION."Accion" = 'COMPRA' AND PlaConf."Valor" = 'PE' AND MVTOCMBTE."IdTipoComprobante" IN ('POL', 'FOE', 'BOE') THEN MVTOCMBTE."MontoMovimientoComprobante" + COALESCE(TOTCOBROSPERU."MontoMovimientoComprobanteCobrosTotal",0)
   	   		WHEN TIPOOPERACION."Accion" = 'VENTA' AND PlaConf."Valor" = 'PE' AND MVTOCMBTE."IdTipoComprobante" IN ('POL', 'FOE', 'BOE') THEN MVTOCMBTE."MontoMovimientoComprobante" - COALESCE(TOTCOBROSPERU."MontoMovimientoComprobanteCobrosTotal",0)
	     	WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE') THEN  
  	         (coalesce ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoMovimientoComprobante", FormMvtoCmbte."MontoDecimales", FormMvtoCmbte."MontoTrucaRedondea"), 0)
  	                     + (COALESCE(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                        COALESCE(TOTCOBROS."DERMVTOCMBTE",0) + 
  	                        COALESCE(TOTCOBROS."GASMVTOCMBTE",0) + 
	                        COALESCE(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                        COALESCE(TOTCOBROS."RETMVTOCMBTE",0)))
 	     	WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN  
  	         (coalesce ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoMovimientoComprobante", FormMvtoCmbte."MontoDecimales", FormMvtoCmbte."MontoTrucaRedondea"), 0)
  	                     - (COALESCE(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                        COALESCE(TOTCOBROS."DERMVTOCMBTE",0) + 
  	                        COALESCE(TOTCOBROS."GASMVTOCMBTE",0) + 
	                        COALESCE(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                        COALESCE(TOTCOBROS."RETMVTOCMBTE",0)))
 	     	WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('NEO') THEN MVTOCMBTE."MontoMovimientoComprobante" - COALESCE(TOTCOBROSPERU."MontoMovimientoComprobanteCobrosTotal",0)
 		 	WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE') THEN
 	         (coalesce ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoMovimientoComprobante", FormMvtoCmbte."MontoDecimales", FormMvtoCmbte."MontoTrucaRedondea"), 0) 
  	                    - (COALESCE(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                       COALESCE(TOTCOBROS."DERMVTOCMBTE",0) + 
	                       COALESCE(TOTCOBROS."GASMVTOCMBTE",0) + 
	                       COALESCE(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                       COALESCE(TOTCOBROS."RETMVTOCMBTE",0)))
 	     	WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN
 	         (coalesce ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoMovimientoComprobante", FormMvtoCmbte."MontoDecimales", FormMvtoCmbte."MontoTrucaRedondea"), 0) 
  	                    + (COALESCE(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                       COALESCE(TOTCOBROS."DERMVTOCMBTE",0) + 
	                       COALESCE(TOTCOBROS."GASMVTOCMBTE",0) + 
	                       COALESCE(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                       COALESCE(TOTCOBROS."RETMVTOCMBTE",0)))
 			ELSE 0
 	   	  END AS "MontoMovimientoComprobanteFinal" 
 	   	    	   
 	   	, CASE 
 		 	WHEN TIPOOPERACION."Accion" = 'COMPRA' AND PlaConf."Valor" = 'PE' AND MVTOCMBTE."IdTipoComprobante" IN ('POL', 'FOE', 'BOE') THEN MVTOCMBTE."MontoLiquidacion" + COALESCE(TOTCOBROSPERU."MontoLiquidacionCobrosTotal",0)
 		 	WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE') THEN  
  	         (coalesce ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoLiquidacion", FormMvtoLiq."MontoDecimales", FormMvtoLiq."MontoTrucaRedondea"), 0) 
                        + (COALESCE(TOTCOBROS."COMMVTOLIQ",0) + 
                           COALESCE(TOTCOBROS."DERMVTOLIQ",0) + 
  	                       COALESCE(TOTCOBROS."GASMVTOLIQ",0) + 
  	                       COALESCE(TOTCOBROS."IMPMVTOLIQ",0) + 
  	                       COALESCE(TOTCOBROS."RETMVTOLIQ",0)))
  	     	WHEN TIPOOPERACION."Accion" = 'COMPRA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN                    
  	         (coalesce ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoLiquidacion", FormMvtoLiq."MontoDecimales", FormMvtoLiq."MontoTrucaRedondea"), 0) 
                        - (COALESCE(TOTCOBROS."COMMVTOLIQ",0) + 
                           COALESCE(TOTCOBROS."DERMVTOLIQ",0) + 
  	                       COALESCE(TOTCOBROS."GASMVTOLIQ",0) + 
  	                       COALESCE(TOTCOBROS."IMPMVTOLIQ",0) + 
  	                       COALESCE(TOTCOBROS."RETMVTOLIQ",0)))
  	     	WHEN TIPOOPERACION."Accion" = 'VENTA' AND PlaConf."Valor" = 'PE' AND MVTOCMBTE."IdTipoComprobante" IN ('NEO') THEN MVTOCMBTE."MontoLiquidacion" - COALESCE(TOTCOBROSPERU."MontoLiquidacionCobrosTotal",0)
  	     	WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE') THEN
 	         (coalesce ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoLiquidacion", FormMvtoLiq."MontoDecimales", FormMvtoLiq."MontoTrucaRedondea"), 0) 
 	                    - (COALESCE(TOTCOBROS."COMMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."DERMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."GASMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."IMPMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."RETMVTOLIQ",0)))
 	     	WHEN TIPOOPERACION."Accion" = 'VENTA' AND MVTOCMBTE."IdTipoComprobante" IN ('NCE', 'NCR', 'NEO') THEN                  
 	         (coalesce ("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"(MVTOCMBTE."MontoLiquidacion", FormMvtoLiq."MontoDecimales", FormMvtoLiq."MontoTrucaRedondea"), 0) 
 	                    + (COALESCE(TOTCOBROS."COMMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."DERMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."GASMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."IMPMVTOLIQ",0) + 
 	                       COALESCE(TOTCOBROS."RETMVTOLIQ",0)))
 			ELSE 0
 	   	  END AS "MontoLiquidacionFinal"
 	    	   
 	   	, CASE 
			WHEN "ActualizarTesoreria" = 'N' AND "ActualizarCuentaInversion" = 'S' THEN 'CUENTA INVERSION'
			WHEN "ActualizarTesoreria" = 'S' THEN MvtoCmbteForPag."NombreMedioDePago"
			ELSE ''
 	   	  END AS "ResumenMedioPago"
 	   	, MVTOCMBTE."CodigoIFRS"
 	   	, MVTOCMBTE."NemoSVS"
 	   	, MVTOCMBTE."MercadoSMV"
 	   	, MVTOCMBTE."ModalidadSMV"
 	   	, MVTOCMBTE."IdOperacion"
 	   	, MVTOCMBTE."FechaHoraMovimientoComprobante"
 	   
 	   --CORREDOR CORRESPONSAL
 	   	, CCPBCC."IdCorredorCorresponsal"
 	   	, COALESCE(CCPBCC."Codigo", '') AS "CodigoCorredorCorresponsal"
 	   	, COALESCE(MVTOCMBTE."IdTipoLiquidacionComisionFactura",'') AS "IdTipoLiquidacionComisionFactura"
 	   	, COALESCE(CTLF."TipoLiquidacion",'') AS "TipoLiquidacion"
 	   	, COALESCE("MVTOCMBTE"."ModPrefFact",'') AS "ModPrefFact"
 	   	, COALESCE("DetalleTipoIndicador"."DetalleIndicador",'') AS "ModPrefFactDescripcion"
	FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MVTOCMBTE
	   INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CONCEPTOMOV   ON CONCEPTOMOV."IdConcepto" = MVTOCMBTE."IdConceptoMovimiento"
	   INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS EMPRESA	  ON EMPRESA."IdEmpresa" = MVTOCMBTE."IdEmpresa"
	   LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CONSCLI ON CONSCLI."IdCliente" = MVTOCMBTE."IdCliente"   
	   LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" AS TIPOPRODUCTO    ON TIPOPRODUCTO."IdTipoProducto" = MVTOCMBTE."IdTipoProducto"
	   LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION      ON TIPOOPERACION."IdTipoOperacion" = MVTOCMBTE."IdTipoOperacion"
	   Left JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MONEDA 	    ON MONEDA."IdMoneda" = MVTOCMBTE."IdMoneda"   
	   INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MonMvtoCmbte  ON MonMvtoCmbte."IdMoneda" = MVTOCMBTE."IdMonedaMovimientoComprobante"   
	   INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MonLiq 	    ON MonLiq."IdMoneda" = MVTOCMBTE."IdMonedaLiquidacion"   
	   LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS TIPOCOMPROBANTE      ON TIPOCOMPROBANTE."IdTipoComprobante" = MVTOCMBTE."IdTipoComprobante"
	   LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS BOVEDA      ON BOVEDA."IdBoveda" = MVTOCMBTE."IdBoveda"
	   INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CONDICIONDELIQUIDACION      ON CONDICIONDELIQUIDACION."IdCondicionDeLiquidacion" = MVTOCMBTE."IdCondicionLiquidacion"
	   LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" AS TIPOREAJUSTE         ON TIPOREAJUSTE."IdTipoReajuste" = MVTOCMBTE."IdTipoReajuste"
	   INNER JOIN "BSCL.Facturacion.BD::FAC.TipoConvenio" AS TIPOCONVENIO  	  ON TIPOCONVENIO."IdTipoConvenio" = MVTOCMBTE."IdTipoConvenio"
	   INNER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS ESTADOFACTURACION     ON ESTADOFACTURACION."IdEstado" = MVTOCMBTE."IdEstadoFacturacion" AND Upper(EstadoFacturacion."NombreEntidad") = Upper('FAC.MovimientoComprobante')
	   INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTADOPLA      ON ESTADOPLA."IdEstado" = ESTADOFACTURACION."IdEstado"
	   
	   LEFT JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS ESTADOCAVALI     ON ESTADOCAVALI."IdEstado" = MVTOCMBTE."IdEstadoCavali" AND Upper(EstadoFacturacion."NombreEntidad") = Upper('FAC.MovimientoComprobanteCavali')
	   LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS ESTADOPLACAV ON ESTADOPLACAV."IdEstado" = ESTADOCAVALI."IdEstado"
	   
	   
	   LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosTotalizados" AS TOTCOBROS ON TOTCOBROS."IdMovimientoComprobante" = MVTOCMBTE."IdMovimientoComprobante"
	   LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosPeruTotalizados" AS TOTCOBROSPERU ON TOTCOBROSPERU."IdMovimientoComprobante" = MVTOCMBTE."IdMovimientoComprobante"
	   LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = MVTOCMBTE."IdPortafolio"
	   LEFT JOIN "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden" AS ConsFormCusOrd ON ConsFormCusOrd."IdTipoFormaCustodiaOrden" = MVTOCMBTE."IdFormaCustodia"
	   LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" AS "CuentaCustodioPortafolio" ON MVTOCMBTE."IdCuentaCustodioPortafolio" = "CuentaCustodioPortafolio"."IdCuentaCustodioPortafolio"
	   
	   INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS ConsIndPort 
	      ON ConsIndPort."IdCliente" = MVTOCMBTE."IdCliente" 
	     AND ConsIndPort."IdPortafolio" = MVTOCMBTE."IdPortafolio"  
	     AND ConsIndPort."IdUso" = 'PRE'
	     AND ConsIndPort."CodigoTipoIndicador" = 'GEN_OCCINV'
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
	   LEFT Join "BSCL.Plataforma.BD::ACC.Usuario" AS Usuario
	      ON Usuario."IdUsuario" = MVTOCMBTE."IdUsuarioBloqueo"      
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
	   LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" Canal
   		ON Canal."IdTipoCanalIngreso" = MVTOCMBTE."IdTipoCanalIngreso"
	   INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" AS MCCOB 
		ON MVTOCMBTE."IdMovimientoComprobante" = MCCOB."IdMovimientoComprobante" 
		AND MCCOB."IdCobro" IN ('COVA','COFI') 
		AND MCCOB."IdEstado"= 'VIG'
	   INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" AS COB 
		ON COB."IdCobro" = MCCOB."IdCobro";
	
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosComisiones" TO  BCS_DESARROLLADOR WITH GRANT OPTION;