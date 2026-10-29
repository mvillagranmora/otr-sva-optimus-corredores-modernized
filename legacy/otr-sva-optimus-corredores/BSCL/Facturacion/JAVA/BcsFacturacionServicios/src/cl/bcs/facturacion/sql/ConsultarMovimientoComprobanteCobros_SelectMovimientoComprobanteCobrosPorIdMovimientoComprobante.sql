SELECT 
	  MCCOB."IdCobro"
	, MCCOB."Porcentaje"
	, MCCOB."IdEstado"
	, COB."EsRecalculable"
	, MOV."IdMovimientoComprobante"
	, COBCLAS."CodigoCobroClasificacion"
	, COBCLAS."DescripcionCobroClasificacion"
	, COB."NombreCobro"
	, COB."FormaCobro"
	, (CASE
	   	  WHEN COB."FormaCobro" = 'V' THEN 'Variable'
	   	  WHEN COB."FormaCobro" = 'F' THEN 'Fijo'
	   END) AS "NombreFormaCobro"
	, MCCOB."IdMonedaCobro"
	, MCCOB."IdMonedaLiquidacionCobro"
	, MCCOB."IdMonedaMovimientoComprobanteCobro"
	, MONSIST."Codigo" AS "CodigoMonedaSistemaCobro" 
	, MONSIST."Nombre" AS "NombreMonedaSistemaCobro" 
	--, MONSIST."Decimales" AS "DecimalesMonedaSistemaCobro" 
	--, sum(TOTCOBROS."COMMVTOCMBTE") AS "MontoMovimientoComprobanteCobro"
	, MCCOB."MontoMovimientoComprobanteCobro"
	, MCCOB."IdMonedaLiquidacionCobro"
	, MONLIQ."Codigo" AS "CodigoMonedaLiquidacionCobro" 
	, MONLIQ."Nombre" AS "NombreMonedaLiquidacionCobro" 
	, MCCOB."MontoLiquidacionCobro" AS "MontoLiquidacionCobro"
	--, * 
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MOV
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" AS MCCOB 
ON MOV."IdMovimientoComprobante" = MCCOB."IdMovimientoComprobante" 
AND MCCOB."IdCobro" IN ('COVA','COFI') 
AND MCCOB."IdEstado"= 'VIG'
INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" AS COB 
ON COB."IdCobro" = MCCOB."IdCobro"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosTotalizados" AS TOTCOBROS 
ON TOTCOBROS."IdMovimientoComprobante" = MOV."IdMovimientoComprobante"
INNER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" As COBCLAS 
ON COB."IdCobroClasificacion" = COBCLAS."IdCobroClasificacion"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" As MONSIST
ON MONSIST."IdMoneda" = MCCOB."IdMonedaMovimientoComprobanteCobro" 
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" As MONLIQ 
ON MONLIQ."IdMoneda" = MCCOB."IdMonedaLiquidacionCobro"
WHERE MOV."IdMovimientoComprobante" IN (:IdsMovimientoComprobante);