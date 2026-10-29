SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarOperaciones";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarOperaciones" AS

SELECT 

	MVTOCMBTE."FechaIngresoRegistro" 			AS "FechaIngresoRegistro",
	COALESCE(MVTOCMBTE."FolioTransaccion",'')  	AS "FolioTransaccion",
	COALESCE(MVTOCMBTE."IdTransaccion",0) 	  	AS "IdTransaccion",
	COALESCE(MVTOCMBTE."IdOrden",0) 			AS "IdOrden", 
	MVTOCMBTE."IdTipoComprobante" 				AS "IdTipoComprobante",
	COMP."FolioComprobante" 					AS "FolioComprobante",
	COALESCE(MVTOCMBTE."IdOrden",0)				AS "IdOperacion",
	COMP."IdComprobante"						AS "IdComprobante",
	MVTOCMBTE."IdCorredoraContraparte" 			AS "IdContraparte",
	0 											AS "IdOperadorContraparte" ,-- AS "NombreComprador", --Comprador
	0 											AS "IdOperadorCorredor",-- AS "NombreVendedor", -- Vendedor
	MVTOCMBTE."IdCliente" 						AS "IdCliente",
	MVTOCMBTE."IdPortafolio" 					AS "IdPortafolio",
	MVTOCMBTE."IdEmisor" 						AS "IdEmisor",
	(Case When MVTOCMBTE."IdTipoInstrumento" in ('PRT',
	        'EMP',
	        'SIM','RRV','RRF') 
	        then MVTOCMBTE."NemotecnicoSubyacente" 
	        Else MVTOCMBTE."NemotecnicoInstrumento" 
	  END) AS "Nemotecnico",
	COALESCE(MVTOCMBTE."NemotecnicoRelacionado",'') AS "RelacionInstrumento",
	CASE TIPOOPERACION."Accion" WHEN 'VENTA' THEN (MVTOCMBTE."Monto" - MVTOCMBTE."Spread")
		ELSE MVTOCMBTE."Monto" END 			AS "ValorPresenteCompraInst",
	MVTOCMBTE."FechaVencimiento" 				AS "FechaVencimiento",
	CASE COALESCE(MVTOCMBTE."IdClasificacionTipoInstrumento",'') WHEN 'IF' THEN MVTOCMBTE."ValorRescate" ELSE MVTOCMBTE."Cantidad" END 	AS "Cantidad",
	MVTOCMBTE."Precio" 						AS "Precio",
	MVTOCMBTE."Tasa" 							AS "Tasa",
	MVTOCMBTE."IdMoneda" 						AS "IdMoneda",
	MVTOCMBTE."Monto",
	TIPOOPERACION."Accion" 						as "AccionOperacion",
	CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN MVTOCMBTE."Monto"  ELSE 0 END AS "MontoCompra",
	CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN MVTOCMBTE."Monto"  ELSE 0 END AS "MontoVenta",
	CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN MVTOCMBTE."Tasa"  ELSE 0 END AS "TasaCompra",
	CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN MVTOCMBTE."Tasa"  ELSE 0 END AS "TasaVenta",
	TOTCOBROS."COMMVTOCMBTE" 					AS "Comision",
	TOTCOBROS."DERMVTOCMBTE" 					AS "Derecho",
	TOTCOBROS."GASMVTO" 						AS "Gasto",
	TOTCOBROS."IMPMVTO" 						AS "Impuesto",
	
	Case 
	     When TIPOOPERACION."Accion" = 'COMPRA' And MVTOCMBTE."IdTipoComprobante" in ('FAC', 'FAE', 'FOE','FOC', 'BOE') Then  
  	         (coalesce (MVTOCMBTE."MontoMovimientoComprobante", 0)+ 
  	         				(coalesce(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                        coalesce(TOTCOBROS."DERMVTOCMBTE",0) + 
  	                        coalesce(TOTCOBROS."GASMVTOCMBTE",0) + 
	                        coalesce(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                        coalesce(TOTCOBROS."RETMVTOCMBTE",0)))
 	     When TIPOOPERACION."Accion" = 'COMPRA' And MVTOCMBTE."IdTipoComprobante" in ('NCE', 'NCR', 'NEO') Then  
  	         (coalesce (MVTOCMBTE."MontoMovimientoComprobante", 0)
  	                     - (coalesce(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                        coalesce(TOTCOBROS."DERMVTOCMBTE",0) + 
  	                        coalesce(TOTCOBROS."GASMVTOCMBTE",0) + 
	                        coalesce(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                        coalesce(TOTCOBROS."RETMVTOCMBTE",0)))
		 else 0       	                        
 	 End As "TotalCompra",
 	 
 	 
 	 Case                       
 	     When TIPOOPERACION."Accion" = 'VENTA' And MVTOCMBTE."IdTipoComprobante" in ('FAC', 'FAE', 'FOE','FOC', 'BOE') Then
 	         (coalesce (MVTOCMBTE."MontoMovimientoComprobante", 0) 
  	                    - (coalesce(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                       coalesce(TOTCOBROS."DERMVTOCMBTE",0) + 
	                       coalesce(TOTCOBROS."GASMVTOCMBTE",0) + 
	                       coalesce(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                       coalesce(TOTCOBROS."RETMVTOCMBTE",0)))
 	     When TIPOOPERACION."Accion" = 'VENTA' And MVTOCMBTE."IdTipoComprobante" in ('NCE', 'NCR', 'NEO') Then
 	         (coalesce (MVTOCMBTE."MontoMovimientoComprobante", 0) 
  	                    + (coalesce(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                       coalesce(TOTCOBROS."DERMVTOCMBTE",0) + 
	                       coalesce(TOTCOBROS."GASMVTOCMBTE",0) + 
	                       coalesce(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                       coalesce(TOTCOBROS."RETMVTOCMBTE",0)))
 	    else 0                   
 	   End As "TotalVenta", 
	MVTOCMBTE."IdCondicionLiquidacion" 			AS "IdCondicionLiquidacion",
	COMP."IdComprobante" 						AS "IdDocumentoRelacion",
	'0001-01-01' AS "FechaVencimientoPacto",
	0 AS "PrecioPlazo",
	0 AS "TasaPlazo",
	'' AS "IdMonedaPlazo",
	0 AS "TotalCompraVencimiento",
	0 AS "TotalVentaVencimiento",
	COMP."IdBolsa" AS "IdBolsa",
	MVTOCMBTE."IdFormaCustodia" 				AS "Custodia",
	MVTOCMBTE."IdBoveda" 						AS "IdBoveda",
	0 AS "CantidadGarantia",
	'' AS "NemoSVS",
	MVTOCMBTE."Monto" AS "ValorPresente",
	0 AS "UtilidadPerdida",
	'0001-01-01' AS "FechaOrigenPacto",
	COALESCE(TIPOOPERACION."Nombre",'') AS "DescripcionOperacion",
	COMP."IdEstadoComprobante" AS "IdEstadoFactura",
	MVTOCMBTE."IdTipoCanalIngreso" AS "Canal",
	0 AS "ValorIVA",
	MVTOCMBTE."FolioComprobanteReferencia" AS "FolioComprobanteAsociado",
	MVTOCMBTE."IdTipoProducto",
	COALESCE(MVTOCMBTE."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
	COALESCE(MVTOCMBTE."IdTipoInstrumento",'') AS "IdTipoInstrumento",
	COALESCE(MVTOCMBTE."IdTipoOperacion",'') AS "IdTipoOperacion",
	MVTOCMBTE."IdInstrumento",
	CASE
		WHEN MVTOCMBTE."IndicadorOrdenDirecta" = '' THEN 'OOI'
		WHEN MVTOCMBTE."IndicadorOrdenDirecta" = 'OD' AND MVTOCMBTE."EsIngresoAutomatico" = 'S' THEN 'OCD'
		WHEN MVTOCMBTE."IndicadorOrdenDirecta" = 'OD' AND MVTOCMBTE."EsIngresoAutomatico" = 'N' THEN 'OSD'
		WHEN MVTOCMBTE."IndicadorOrdenDirecta" = 'OD' AND MVTOCMBTE."EsIngresoAutomatico" = '' THEN 'OSD'
		WHEN MVTOCMBTE."IndicadorOrdenDirecta" = 'OE' THEN 'OOE'
		ELSE 'OOI' END AS "ModalidadEjecucion",
	MVTOCMBTE."EsFungible" AS "EsFungible",
	MVTOCMBTE."IdMovimientoRelacionado" AS "IdMovimientoRelacionado",
	COMP."FechaComprobante" as "FechaTransaccion",
	COMP."FechaComprobante" as "FechaMovimiento",
	COALESCE(TRANS."CodigoOperadorContraparte",'') as "CodigoOperadorContraparte",
	COALESCE(TRANS."CodigoOperadorCorredor",'') as "CodigoOperadorCorredor",
	MVTOCMBTE."EsEnRueda" as "EsEnRueda",
	COMP."IdEstadoComprobante",
	MVTOCMBTE."IdMovimientoComprobante",
	MVTOCMBTE."IdMovimientoComprobante" AS "IdMovimientoComprobante2",
	MVTOCMBTE."EsVentaCorta",
	COMP."FechaComprobante" AS "FechaComprobante",
	IDENT."DocumentoIdentidad",
	CASE PER."Tipo" 
		WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
		ELSE PER."RazonSocial"
	END  as "NombreCompleto",
	/*IMPSURAOPT-378 INI para mostrar moneda y montos locales para los ETFs*/
	"IdMonedaMovimientoComprobante",
	"MontoMovimientoComprobante"
	/*IMPSURAOPT-378 FIN*/
	
	,TIPOOPERACION."IdTipoOperacionRelacionada"
	
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MVTOCMBTE
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COMP 
	ON MVTOCMBTE."IdComprobante" = COMP."IdComprobante"
	AND COMP."IdEstadoComprobante" in ('ENV')
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRANS 
	ON MVTOCMBTE."IdTransaccion" = TRANS."IdTransaccion"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosTotalizados" AS TOTCOBROS 	  
	ON TOTCOBROS."IdMovimientoComprobante" = MVTOCMBTE."IdMovimientoComprobante"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION
	ON TIPOOPERACION."IdTipoOperacion" = MVTOCMBTE."IdTipoOperacion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON (CLI."IdCliente" =  MVTOCMBTE."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER   ON (PER."IdPersona" = CLI."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
	
								
UNION ALL




SELECT 
	MVTOCMBTE."FechaIngresoRegistro" 			AS "FechaIngresoRegistro",
	COALESCE(MVTOCMBTE."FolioTransaccion",'')  	AS "FolioTransacion",
	COALESCE(MVTOCMBTE."IdTransaccion",0) 	  	AS "IdTransaccion",
	COALESCE(MVTOCMBTE."IdOrden",0) 			AS "IdOrden", 
	MVTOCMBTE."IdTipoComprobante" 				AS "IdTipoComprobante",
	COMP."FolioComprobante" 					AS "FolioComprobante",
	COALESCE(MVTOCMBTE."IdOrden",0)				AS "IdOperacion",
	COMP."IdComprobante"						AS "IdComprobante",
	MVTOCMBTE."IdCorredoraContraparte" 			AS "IdContraparte",
	0 											AS "IdOperadorContraparte" ,-- AS "NombreComprador", --Comprador
	0 											AS "IdOperadorCorredor",-- AS "NombreVendedor", -- Vendedor
	MVTOCMBTE."IdCliente" 						AS "IdCliente",
	MVTOCMBTE."IdPortafolio" 					AS "IdPortafolio",
	MVTOCMBTE."IdEmisor" 						AS "IdEmisor",
	(Case When MVTOCMBTE."IdTipoInstrumento" in ('PRT',
	        'EMP',
	        'SIM','RRV','RRF') 
	        then MVTOCMBTE."NemotecnicoSubyacente" 
	        Else MVTOCMBTE."NemotecnicoInstrumento" 
	  END) AS "Nemotecnico",
	COALESCE(MVTOCMBTE."NemotecnicoRelacionado",'') AS "RelacionInstrumento",
	CASE TIPOOPERACION."Accion" WHEN 'VENTA' THEN (MVTOCMBTE."Monto" - MVTOCMBTE."Spread")
		ELSE MVTOCMBTE."Monto" END 			AS "ValorPresenteCompraInst",
	MVTOCMBTE."FechaVencimiento" 				AS "FechaVencimiento",
	MVTOCMBTE."Cantidad" 						AS "Cantidad",
	MVTOCMBTE."Precio" 						AS "Precio",
	MVTOCMBTE."Tasa" 							AS "Tasa",
	MVTOCMBTE."IdMoneda" 						AS "IdMoneda",
	0											AS "Monto",
	TIPOOPERACION."Accion" 						as "AccionOperacion",
	CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN MVTOCMBTE."Monto"  ELSE 0 END AS "MontoCompra",
	CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN MVTOCMBTE."Monto"  ELSE 0 END AS "MontoVenta",
	CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN MVTOCMBTE."Tasa"  ELSE 0 END AS "TasaCompra",
	CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN MVTOCMBTE."Tasa"  ELSE 0 END AS "TasaVenta",
	0 					AS "Comision",
	0 					AS "Derecho",
	0 						AS "Gasto",
	0 						AS "Impuesto",

	Case 
	     When TIPOOPERACION."Accion" = 'COMPRA' And MVTOCMBTE."IdTipoComprobante" in ('FAC', 'FAE', 'FOE','FOC', 'BOE') Then  
  	         (coalesce (MVTOCMBTE."MontoMovimientoComprobante", 0)
  	                     + (coalesce(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                        coalesce(TOTCOBROS."DERMVTOCMBTE",0) + 
  	                        coalesce(TOTCOBROS."GASMVTOCMBTE",0) + 
	                        coalesce(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                        coalesce(TOTCOBROS."RETMVTOCMBTE",0)))
 	     When TIPOOPERACION."Accion" = 'COMPRA' And MVTOCMBTE."IdTipoComprobante" in ('NCE', 'NCR', 'NEO') Then  
  	         (coalesce (MVTOCMBTE."MontoMovimientoComprobante", 0)
  	                     - (coalesce(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                        coalesce(TOTCOBROS."DERMVTOCMBTE",0) + 
  	                        coalesce(TOTCOBROS."GASMVTOCMBTE",0) + 
	                        coalesce(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                        coalesce(TOTCOBROS."RETMVTOCMBTE",0)))
		 else 0       	                        
 	 End As "TotalCompra",
 	 Case                       
 	     When TIPOOPERACION."Accion" = 'VENTA' And MVTOCMBTE."IdTipoComprobante" in ('FAC', 'FAE', 'FOE','FOC', 'BOE') Then
 	         (coalesce (MVTOCMBTE."MontoMovimientoComprobante", 0) 
  	                    - (coalesce(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                       coalesce(TOTCOBROS."DERMVTOCMBTE",0) + 
	                       coalesce(TOTCOBROS."GASMVTOCMBTE",0) + 
	                       coalesce(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                       coalesce(TOTCOBROS."RETMVTOCMBTE",0)))
 	     When TIPOOPERACION."Accion" = 'VENTA' And MVTOCMBTE."IdTipoComprobante" in ('NCE', 'NCR', 'NEO') Then
 	         (coalesce (MVTOCMBTE."MontoMovimientoComprobante", 0) 
  	                    + (coalesce(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                       coalesce(TOTCOBROS."DERMVTOCMBTE",0) + 
	                       coalesce(TOTCOBROS."GASMVTOCMBTE",0) + 
	                       coalesce(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                       coalesce(TOTCOBROS."RETMVTOCMBTE",0)))
 	    else 0                   
 	   End As "TotalVenta", 
 	   
	MVTOCMBTE."IdCondicionLiquidacion" 			AS "IdCondicionLiquidacion",
	COMP."IdComprobante" 						AS "IdDocumentoRelacion",
	'0001-01-01' AS "FechaVencimientoPacto",
	0 AS "PrecioPlazo",
	0 AS "TasaPlazo",
	'' AS "IdMonedaPlazo",
	0 AS "TotalCompraVencimiento",
	0 AS "TotalVentaVencimiento",
	COMP."IdBolsa" AS "IdBolsa",
	MVTOCMBTE."IdFormaCustodia" 				AS "Custodia",
	MVTOCMBTE."IdBoveda" 						AS "IdBoveda",
	0 AS "CantidadGarantia",
	'' AS "NemoSVS",
	MVTOCMBTE."Monto" AS "ValorPresente",
	0 AS "UtilidadPerdida",
	'0001-01-01' AS "FechaOrigenPacto",
	COALESCE(TIPOOPERACION."Nombre",'') AS "DescripcionOperacion",
	COMP."IdEstadoComprobante" AS "IdEstadoFactura",
	MVTOCMBTE."IdTipoCanalIngreso" AS "Canal",
	0 AS "ValorIVA",
	MVTOCMBTE."FolioComprobanteReferencia" AS "FolioComprobanteAsociado",
	MVTOCMBTE."IdTipoProducto",
	COALESCE(MVTOCMBTE."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
	COALESCE(MVTOCMBTE."IdTipoInstrumento",'') AS "IdTipoInstrumento",
	COALESCE(MVTOCMBTE."IdTipoOperacion",'') AS "IdTipoOperacion",
	MVTOCMBTE."IdInstrumento",
	CASE
		WHEN MVTOCMBTE."IndicadorOrdenDirecta" = '' THEN 'OOI'
		WHEN MVTOCMBTE."IndicadorOrdenDirecta" = 'OD' AND MVTOCMBTE."EsIngresoAutomatico" = 'S' THEN 'OCD'
		WHEN MVTOCMBTE."IndicadorOrdenDirecta" = 'OD' AND MVTOCMBTE."EsIngresoAutomatico" = 'N' THEN 'OSD'
		WHEN MVTOCMBTE."IndicadorOrdenDirecta" = 'OD' AND MVTOCMBTE."EsIngresoAutomatico" = '' THEN 'OSD'
		WHEN MVTOCMBTE."IndicadorOrdenDirecta" = 'OE' THEN 'OOE'
		ELSE 'OOI' END AS "ModalidadEjecucion",
	MVTOCMBTE."EsFungible" AS "EsFungible",
	MVTOCMBTE."IdMovimientoRelacionado" AS "IdMovimientoRelacionado",
	COMP."FechaComprobante" as "FechaTransaccion",
	COMP."FechaComprobante" as "FechaMovimiento",
	COALESCE(TRANS."CodigoOperadorContraparte",'') as "CodigoOperadorContraparte",
	COALESCE(TRANS."CodigoOperadorCorredor",'') as "CodigoOperadorCorredor",
	MVTOCMBTE."EsEnRueda" as "EsEnRueda",
	COMP."IdEstadoComprobante",
	MVTOCMBTE."IdMovimientoComprobante",
	MVTOCMBTE."IdMovimientoComprobante" AS "IdMovimientoComprobante2",
	MVTOCMBTE."EsVentaCorta",
	COMP."FechaComprobante" as "FechaComprobante",
	IDENT."DocumentoIdentidad",
	CASE PER."Tipo" 
		WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
		ELSE PER."RazonSocial"
	END  as "NombreCompleto",
	/*IMPSURAOPT-378 INI para mostrar moneda y montos locales para los ETFs*/
	"IdMonedaMovimientoComprobante",
	"MontoMovimientoComprobante"
	/*IMPSURAOPT-378 FIN*/
	,TIPOOPERACION."IdTipoOperacionRelacionada"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MVTOCMBTE
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COMP 
	ON MVTOCMBTE."IdComprobante" = COMP."IdComprobante"
	AND COMP."IdEstadoComprobante" in ('ENV')
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRANS 
	ON MVTOCMBTE."IdTransaccion" = TRANS."IdTransaccion"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosTotalizados" AS TOTCOBROS 	  
	ON TOTCOBROS."IdMovimientoComprobante" = MVTOCMBTE."IdMovimientoComprobante"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION
	ON TIPOOPERACION."IdTipoOperacion" = MVTOCMBTE."IdTipoOperacion"
   
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON (CLI."IdCliente" = MVTOCMBTE."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER   ON (PER."IdPersona" = CLI."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad";

GO   
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarOperaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
