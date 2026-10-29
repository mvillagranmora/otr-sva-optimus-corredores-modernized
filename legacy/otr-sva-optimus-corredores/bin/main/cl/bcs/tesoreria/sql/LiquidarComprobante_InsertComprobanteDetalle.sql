
------------------------------------------------------------------------
-- Obtiene y guarda mas datos de detalle de comprobantes a liquidar 
------------------------------------------------------------------------


INSERT INTO "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobanteDetalle"
(
	"IdInterno",
	"IdComprobanteInformado",	
	"IdComprobanteDetalle",
	"IdTipoMovimiento",
	"IdTipoComprobanteOrigen",
	"SecuenciaRelacionada",
	"PendienteDeLiquidar",
	"IdConcepto",
	"Monto"
)
SELECT   TC."IdComprobante" AS "IdInterno"
	   	,TC."IdComprobante" AS "IdComprobanteInformado"
	   	,COALESCE(CD."IdComprobanteDetalle", 0) AS "IdComprobanteDetalle" 
	   	,COALESCE(CD."IdTipoMovimiento",'') AS "IdTipoMovimiento" 
	   	,COALESCE(SREL."IdTipoComprobanteOrigen",'') AS "IdTipoComprobanteOrigen" 
	   	,COALESCE(SREL."SecuenciaRelacionada",0) AS "SecuenciaRelacionada"
	   	,COALESCE(SREL."PendienteDeLiquidar",0) AS "PendienteDeLiquidar"
	   	,COALESCE(CD."IdConcepto",'') AS "IdConcepto" 
	   	,COALESCE(CD."Monto",0) AS "Monto"
   	
  
  FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobante" AS TC
  LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" as CD 
   		 ON CD."IdComprobante" = TC."IdComprobante"
  LEFT JOIN (SELECT "IdComprobante","IdTipoComprobanteOrigen","SecuenciaRelacionada",
   			         SUM(CASE WHEN "IdEstado" = 'LIQ' THEN 0 ELSE 1 END) as "PendienteDeLiquidar"
			 FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" 
			 WHERE "IdTipoMovimiento" = 'MOVING'
			 AND "IdEstado" NOT IN ('ANU','MOD')		     
			 GROUP BY "IdComprobante","IdTipoComprobanteOrigen","SecuenciaRelacionada"
   			) AS SREL
   		   ON SREL."IdComprobante" = TC."IdComprobante"
   		  AND SREL."IdTipoComprobanteOrigen" = CD."IdTipoComprobanteOrigen" 
   		  AND SREL."SecuenciaRelacionada" = CD."SecuenciaRelacionada";
