UPDATE CM
SET CM."IdEstado"		= 'PRO',
	CM."FechaProceso"	= CAST(:FechaProceso AS SECONDDATE)
FROM "BSCL.SocioNegocio.BD::CAR.CargaMasiva" AS CM
WHERE CM."IdCargaMasiva" IN (:IdCargaMasiva);