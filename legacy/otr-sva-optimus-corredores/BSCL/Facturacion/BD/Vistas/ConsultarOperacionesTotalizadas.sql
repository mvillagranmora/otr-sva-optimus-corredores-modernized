SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarOperacionesTotalizadas";
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarOperacionesTotalizadas" AS

SELECT 
	"FechaIngresoRegistro",
	"FolioTransaccion",
	"IdTransaccion",
	"IdOrden",
	"IdTipoComprobante",
	"FolioComprobante",
	"IdOperacion",
	"IdComprobante",
	"IdContraparte",
	"IdOperadorContraparte" ,
	"IdOperadorCorredor",
	"IdCliente",
	"IdPortafolio",
	"IdEmisor",
	"Nemotecnico",
	"RelacionInstrumento",
	"ValorPresenteCompraInst",
	"FechaVencimiento",
	"Cantidad",
	"Precio",
	"Tasa",
	"IdMoneda",
	"Monto",
	"AccionOperacion",
	"MontoCompra",
	"MontoVenta",
	"TasaCompra",
	"TasaVenta",
	CASE WHEN RANK = 1 THEN RESULTADO."Comision" ELSE 0 END AS "Comision",
	CASE WHEN RANK = 1 THEN RESULTADO."Derecho" ELSE 0 END AS "Derecho",
	CASE WHEN RANK = 1 THEN RESULTADO."Gasto" ELSE 0 END AS "Gasto",
	CASE WHEN RANK = 1 THEN RESULTADO."Impuesto" ELSE 0 END AS "Impuesto",
	Case 
		WHEN "AccionOperacion" = 'COMPRA' And "IdTipoComprobante" in ('FAC', 'FAE', 'FOE','FOC', 'BOE') Then 
	     	CASE WHEN RANK = 1 THEN RESULTADO."TotalCompra1" ELSE 0 END
 	     WHEN "AccionOperacion" = 'COMPRA' And "IdTipoComprobante" in ('NCE', 'NCR', 'NEO') Then RESULTADO."TotalCompra2"       
		 else 0       	                        
 	 End As "TotalCompra",
	
 	 Case                       
 		 When "AccionOperacion" = 'VENTA' And "IdTipoComprobante" in ('FAC', 'FAE', 'FOE','FOC', 'BOE') THEN
 			CASE WHEN RANK = 1 THEN RESULTADO."TotalVenta1" ELSE 0 END                      
 		 When "AccionOperacion" = 'VENTA' And "IdTipoComprobante" in ('NCE', 'NCR', 'NEO') THEN RESULTADO."TotalVenta2"
 		 else 0
 	End As "TotalVenta",
	"IdCondicionLiquidacion",
	"IdDocumentoRelacion",
	"FechaVencimientoPacto",
	"PrecioPlazo",
	"TasaPlazo",
	"IdMonedaPlazo",
	"TotalCompraVencimiento",
	"TotalVentaVencimiento",
	"IdBolsa",
	"Custodia",
	"IdBoveda",
	"CantidadGarantia",
	"NemoSVS",
	"ValorPresente",
	"UtilidadPerdida",
	"FechaOrigenPacto",
	"DescripcionOperacion",
	"IdEstadoFactura",
	"Canal",
	"ValorIVA",
	"FolioComprobanteAsociado",
	"IdTipoProducto",
	"IdClasificacionTipoInstrumento",
	"IdTipoInstrumento",
	"IdTipoOperacion",
	"IdInstrumento",
	"ModalidadEjecucion",
	"EsFungible",
	"IdMovimientoRelacionado",
	"FechaTransaccion",
	"FechaMovimiento",
	"CodigoOperadorContraparte",
	"CodigoOperadorCorredor",
	"EsEnRueda",
	"IdEstadoComprobante",
	"IdMovimientoComprobante",
	"IdMovimientoComprobante2",
	"EsVentaCorta",
	"FechaComprobante",
	"DocumentoIdentidad",
	"IdComprobanteReferencia",
	"NombreCompleto",
	"IdMonedaMovimientoComprobante",
	"MontoMovimientoComprobante",
	"IdTipoOperacionRelacionada",
	"IdSistemaBursatil",
	"InstrumentoNegociacion",
	"SecuenciaMovimiento",
	"TipoFechaTransaccion"
FROM 
(SELECT 
	RANK() over (partition by  COMP."IdComprobante" order by COMP."IdComprobante",MVTOCMBTE."FolioTransaccion" desc) as RANK,
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
	CASE WHEN MVTOCMBTE."IdClasificacionTipoInstrumento" = 'SP' THEN MVTOCMBTE."FechaLiquidacion"
	ELSE MVTOCMBTE."FechaVencimiento" END AS "FechaVencimiento",
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
	TOTCOBROS."COMMVTOCMBTE" AS "Comision",
	TOTCOBROS."DERMVTOCMBTE" AS "Derecho",
	TOTCOBROS."GASMVTO" AS "Gasto",
	TOTCOBROS."IMPMVTO" AS "Impuesto",
	Case 
		When TIPOOPERACION."Accion" = 'COMPRA' And MVTOCMBTE."IdTipoComprobante" in ('FAC', 'FAE', 'FOE','FOC', 'BOE') Then COMP."MontoLiquidacionFinal"
 	End As "TotalCompra1",
 	Case 
		When TIPOOPERACION."Accion" = 'COMPRA' And MVTOCMBTE."IdTipoComprobante" in ('NCE', 'NCR', 'NEO') Then  
  	         (coalesce (MVTOCMBTE."MontoMovimientoComprobante", 0)
  	                     - (coalesce(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                        coalesce(TOTCOBROS."DERMVTOCMBTE",0) + 
  	                        coalesce(TOTCOBROS."GASMVTOCMBTE",0) + 
	                        coalesce(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                        coalesce(TOTCOBROS."RETMVTOCMBTE",0))) 	   
 	End As "TotalCompra2",
 	Case                       
 		When TIPOOPERACION."Accion" = 'VENTA' And MVTOCMBTE."IdTipoComprobante" in ('FAC', 'FAE', 'FOE','FOC', 'BOE') THEN COMP."MontoLiquidacionFinal" 		
 	End As "TotalVenta1",   
	Case                       
 		When TIPOOPERACION."Accion" = 'VENTA' And MVTOCMBTE."IdTipoComprobante" in ('NCE', 'NCR', 'NEO') Then
 	         (coalesce (MVTOCMBTE."MontoMovimientoComprobante", 0) 
  	                    + (coalesce(TOTCOBROS."COMMVTOCMBTE",0) + 
  	                       coalesce(TOTCOBROS."DERMVTOCMBTE",0) + 
	                       coalesce(TOTCOBROS."GASMVTOCMBTE",0) + 
	                       coalesce(TOTCOBROS."IMPMVTOCMBTE",0) + 
 	                       coalesce(TOTCOBROS."RETMVTOCMBTE",0)))  	    
 	End As "TotalVenta2", 
	MVTOCMBTE."IdCondicionLiquidacion" 			AS "IdCondicionLiquidacion",
	COMP."IdComprobante" 						AS "IdDocumentoRelacion",
	CASE WHEN MVTOCMBTE."IdClasificacionTipoInstrumento" = 'RF' THEN MVTOCMBTE."FechaVencimiento" 
	ELSE '0001-01-01' END AS "FechaVencimientoPacto",
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
	COMP."FolioComprobanteReferencia" AS "FolioComprobanteAsociado",
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
	COALESCE(TRANS."FechaOperacion", COMP."FechaComprobante") as "FechaTransaccion",
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
	COMP."IdComprobanteReferencia",
	CASE PER."Tipo" 
		WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
		ELSE PER."RazonSocial"
	END  as "NombreCompleto",
	/*IMPSURAOPT-378 INI para mostrar moneda y montos locales para los ETFs*/
	"IdMonedaMovimientoComprobante",
	"MontoMovimientoComprobante"
	/*IMPSURAOPT-378 FIN*/
	
	,TIPOOPERACION."IdTipoOperacionRelacionada",
	TRANS."IdSistemaBursatil",
	COALESCE(INS."Nemotecnico", '') AS "InstrumentoNegociacion",
	COALESCE(MVTOCMBTE."SecuenciaMovimiento",0) AS "SecuenciaMovimiento",
	CASE
		WHEN TRANS."FechaOperacion" IS NOT NULL THEN 'T'
		ELSE 'C'
	END AS "TipoFechaTransaccion"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MVTOCMBTE
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COMP 
	ON MVTOCMBTE."IdComprobante" = COMP."IdComprobante"
	AND COMP."IdEstadoComprobante" <> 'ANU'
LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRANS 
	ON MVTOCMBTE."IdTransaccion" = TRANS."IdTransaccion"
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORD 
	ON MVTOCMBTE."IdOrden" = ORD."IdOrden"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS 
	ON INS."IdInstrumento" = ORD."IdInstrumentoNegociacion"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosTotalizados" AS TOTCOBROS
    ON TOTCOBROS."IdComprobante" = COMP."IdComprobante"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION
	ON TIPOOPERACION."IdTipoOperacion" = MVTOCMBTE."IdTipoOperacion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON (CLI."IdCliente" =  MVTOCMBTE."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER   ON (PER."IdPersona" = CLI."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
) AS RESULTADO

GO   
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarOperacionesTotalizadas" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
