SELECT
"IdPortafolio",
SUM(CASE WHEN "IdClasificacionTipoInstrumento"='IF' THEN "INGValorRescate" ELSE "INGCantidad" END) AS "CantidadDisponible"
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE 
"IdPortafolio" IN (:IdPortafolio) 
AND "FechaOperacion" <= :FechaHasta
AND "IdEstado" = 'VIG'
GROUP BY "IdPortafolio"