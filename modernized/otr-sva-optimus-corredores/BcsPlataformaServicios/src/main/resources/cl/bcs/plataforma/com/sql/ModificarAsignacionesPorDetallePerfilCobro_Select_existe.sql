SELECT 
COUNT ("IdAsignacion") AS "existen"
FROM "BSCL.Orden.BD::ORD.CobroAsignacion"
WHERE "CodigoCobroClasificacionBase" = 'COMISION'
AND "IdCobro" =:IdCobro
AND "IdAsignacion"= :IdAsignacion ;
