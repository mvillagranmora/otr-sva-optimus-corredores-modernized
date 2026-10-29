SELECT 
COUNT ("IdCobroOrden") AS "existen"
FROM "BSCL.Orden.BD::ORD.CobroOrden"AS "COBORDEN"  
WHERE "COBORDEN"."CodigoCobroClasificacionBase" = 'COMISION'
AND "COBORDEN"."IdOrden"=:IdOrden
AND "COBORDEN"."IdCobro"=:IdCobro
;