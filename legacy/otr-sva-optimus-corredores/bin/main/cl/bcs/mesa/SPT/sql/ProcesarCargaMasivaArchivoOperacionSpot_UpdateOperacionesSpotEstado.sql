UPDATE "BSCL.Mesa.BD::CAR.CargaMasivaSpot" 
SET "IdEstado" = (:IdEstado)
	,"FechaProceso" = (:FechaProceso)
WHERE "IdCargaMasiva" IN (:IdCargaMasiva);