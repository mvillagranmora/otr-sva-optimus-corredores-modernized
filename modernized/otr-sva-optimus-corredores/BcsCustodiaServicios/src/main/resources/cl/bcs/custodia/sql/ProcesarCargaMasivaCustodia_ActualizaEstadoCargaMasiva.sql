UPDATE CM
SET  CM."IdEstado"     = 'PRO'
	,CM."FechaProceso" = CAST(:Fecha AS SECONDDATE)
FROM "BSCL.Custodia.BD::CUS.CargaMasiva" AS CM
WHERE CM."IdCargaMasiva" = :IdCargaMasiva;