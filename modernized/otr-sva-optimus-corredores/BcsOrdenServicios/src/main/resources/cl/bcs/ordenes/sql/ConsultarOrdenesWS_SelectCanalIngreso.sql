SELECT 
"IdTipoCanalIngreso" 
, "Nombre" 
FROM BSCL."BSCL.Plataforma.BD::PLA.TipoCanalIngreso"
WHERE "IdEstado" = :IdEstado
AND "PermiteIngresoOrdenes" = 'S';