SELECT
	ORD."IdOrden" 						AS "IdOrden",
	ASIGN."IdAsignacion"				AS "IdAsignacion",
	ORD."IdMonedaOrden"					AS "IdMonedaOrden",
	ORD."IdMonedaLiquidacionOrden" 		AS "IdMonedaLiquidacionOrden",
	ORD."MontoOrden" 					AS "MontoOrden",
	ORD."MontoLocal"					AS "MontoLocal",
	ORD."MontoLiquidacion"				AS "MontoLiquidacion",
	ASIGN."TipoCambio"					AS "TipoCambio"
	
FROM "BSCL.Orden.BD::ORD.Orden" ORD
INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" ASIGN ON ASIGN."IdOrden" = ORD."IdOrden"
WHERE ORD."IdOrden" IN (:IdsOrden)
AND ASIGN."IdAsignacion" IN (:IdsAsignacion);