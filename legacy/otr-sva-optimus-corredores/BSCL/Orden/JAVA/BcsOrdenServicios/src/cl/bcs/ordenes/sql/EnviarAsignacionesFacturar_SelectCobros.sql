SELECT COB."IdAsignacion",COB."IdCobro", COB."IdMoneda" as "IdMonedaCobro",
COALESCE(COB."MontoCobroCalculado",0) AS "MontoCobro",
COALESCE(COB."MontoCobroCalculadoLocal",0) AS "MontoMovimientoComprobanteCobro", 
COALESCE(COB."MontoCobroCalculadoLiquidacion",0) AS  "MontoLiquidacionCobro",
"ValorCobroFinal" AS "Porcentaje"  
FROM "BSCL.Orden.BD::ORD.CobroAsignacion" COB
WHERE COB."IdAsignacion" IN (:idsAsignacion);