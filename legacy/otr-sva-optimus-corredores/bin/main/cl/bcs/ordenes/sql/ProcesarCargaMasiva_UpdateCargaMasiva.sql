
UPDATE CM
SET 
	 CM."IdEstado"     = 'PRO'
	,CM."FechaProceso" = CAST(:FechaProceso AS SECONDDATE)
FROM "BSCL.Orden.BD::CAR.CargaMasiva" AS CM
WHERE CM."IdCargaMasiva" = :IdCargaMasiva;