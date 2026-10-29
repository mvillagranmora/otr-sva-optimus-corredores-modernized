 
----------------------------------------------------
-- Obtiene mas datos de comprobantes a liquidar y los guarda en tabla global 
----------------------------------------------------

INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobante"
(
	"IdInterno",	
	"IdComprobanteInformado",
	"IdTipoMovimientoInformado",
	"IdComprobante",
	"IdTipoMovimiento",	
	"MontoComprobante",	
	"CantidadFormaDePago",
    "MontoTotalFormaDePago",
    "IdEstadoComprobante",
   	"AfectaCuentaInversion",
   	"IdPortafolioTitularPago",	
   	"IdConcepto",
   	"FechaMovimiento",
   	"IdPersona",	
	"NombrePersona",
	"TitularPago",
	"IdTipoPersonaTitularPago",
	"IdPersonaTitularPago",	
	"FechaLiquidacion",
	"IdCaja"
)
SELECT      TC."IdComprobante" AS "IdInterno"
		   ,TC."IdComprobante" AS "IdComprobanteInformado"
		   ,TC."IdTipoMovimiento" AS "IdTipoMovimientoInformado"
		   ,COALESCE(C."IdComprobante",0) AS "IdComprobante" 
		   ,COALESCE(C."IdTipoMovimiento",'') AS "IdTipoMovimiento"
		   ,COALESCE(C."Monto",0) AS "MontoComprobante"
		   ,COALESCE(TCFP."Count",0) AS "CantidadFormaDePago"
    	   ,COALESCE(TCFP."Monto",0) AS "MontoTotalFormaDePago"
    	   ,COALESCE(C."IdEstado",'') AS "IdEstadoComprobante"
    	   ,TC."AfectaCuentaInversion"
    	   ,COALESCE(C."IdPortafolioTitularPago",0) AS "IdPortafolioTitularPago" 
    	   ,COALESCE(C."IdConcepto",'') AS "IdConcepto" 
    	   ,COALESCE(C."FechaMovimiento",'0001-01-01') AS "FechaMovimiento" 
    	   ,COALESCE(C."IdPersona",0) AS "IdPersona" 
		   ,COALESCE(C."NombrePersona",'') AS "NombrePersona" 
		   ,COALESCE(C."TitularPago",'') AS "TitularPago" 
		   ,COALESCE(C."IdTipoPersonaTitularPago",'') AS "IdTipoPersonaTitularPago" 
		   ,COALESCE(C."IdPersonaTitularPago",0) AS "IdPersonaTitularPago"
		   ,COALESCE(C."FechaLiquidacion",:FechaMovimiento) AS "FechaLiquidacion"
		   ,COALESCE(C."IdCaja",0) AS "IdCaja" 
FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_ComprobantesALiquidar" AS TC
LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS C ON (C."IdComprobante" = TC."IdComprobante")
LEFT JOIN (SELECT "IdComprobante", 
	   					  COUNT("IdComprobanteFormaDePago") as "Count",
	   					  SUM("Monto") as "Monto"
	   			    FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
	   			   WHERE "IdEstado" NOT IN ('ANU','MOD')
	   			   GROUP BY "IdComprobante"
	   			 ) AS TCFP
	   		  ON TCFP."IdComprobante" = TC."IdComprobante";
	   		  
