SELECT 
	CILI."ConceptoPH" AS "Concepto",
	CILI."Movimiento" AS "TipoMovimiento",
	ifnull(sum(T."Monto"), 0) AS "MontoDecimal",
	'CLP' AS "Moneda",
	TO_NVARCHAR("FechaLiquidacion", 'YYYYMMDD') AS "FechaLiquidacion",
	'04' AS "TipoCliente",
	'00' AS "EstadoOperacion"
FROM "BSCL.Orden.BD::ORD.Transaccion" T
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIP ON TIP."IdTipoOperacion" = T."IdTipoOperacion" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.CodigoInterfazLiquidezIntradia" CILI 
ON CILI."CodigoOperacion" = 'TELREN' AND CILI."IdTipoMovimiento" = TIP."Accion"
WHERE "IdSistemaBursatil" = 'BCSTELERENTA' AND "FechaLiquidacion" = :FechaProceso
AND "IdMoneda" = 'CLP' AND T."IdEmpresa" = :IdEmpresa AND T."IdCondicionDeLiquidacion" = 'T0'
GROUP BY T."IdTipoOperacion", CILI."Movimiento", "FechaLiquidacion",CILI."ConceptoPH"
ORDER BY "FechaLiquidacion";
