SELECT 
SUM("COBORDEN"."MontoCobroCalculadoLiquidacion" ) AS "PorcentajeSuma"
FROM "BSCL.Orden.BD::ORD.Orden" AS ORDENN 
INNER JOIN "BSCL.Orden.BD::ORD.CobroOrden"AS "COBORDEN" ON "COBORDEN"."IdOrden" ="ORDENN"."IdOrden" 
WHERE "COBORDEN"."CodigoCobroClasificacionBase" <> 'COMISION'
AND "ORDENN" ."IdOrden" = :IdOrden