UPDATE CM
SET  CM."IdEstado"     = 'PRO'
	,CM."FechaProceso" = CAST(:Fecha AS SECONDDATE)
FROM "BSCL.Plataforma.BD::PLA.CargaMasiva" AS CM
WHERE CM."IdCargaMasiva" = :IdCargaMasiva;
