
-- Consulta comprobantes cobros facturados
 
SELECT
	 CMTECOB."IdComprobante"
	,COB."NombreCobro"
	,COB."FormaCobro"   -- F V
	,CASE 
	    WHEN COB."FormaCobro" = 'V' THEN 'Variable'
	    WHEN COB."FormaCobro" = 'F' THEN 'Fijo'
	    ELSE ''
	 END  AS "NombreFormaCobro"	
	,CMTECOB."IdCobro"
	,CMTECOB."IdMonedaCobro"
	,CMTECOB."Porcentaje" AS "ValorCobroFacturado"
	,CMTECOB."MontoCobro" AS "MontoCobroFacturado"

FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" AS CMTECOB
LEFT JOIN "BSCL.Plataforma.BD::COM.Cobro" AS COB ON (COB."IdCobro" = CMTECOB."IdCobro")
WHERE CMTECOB."IdComprobante"   IN (:IdComprobantes)
AND   CMTECOB."IdEstado"   = 'VIG';
