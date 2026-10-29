
SELECT 
	CM."IdCargaMasiva",
	:FechaProceso AS "FechaProceso",  -- si carga es procesada con exito se registra esta fecha
	CM."IdEstado"
FROM "BSCL.Orden.BD::CAR.CargaMasiva" AS CM
WHERE CM."IdCargaMasiva" IN (:IdsCargasMasivas);
