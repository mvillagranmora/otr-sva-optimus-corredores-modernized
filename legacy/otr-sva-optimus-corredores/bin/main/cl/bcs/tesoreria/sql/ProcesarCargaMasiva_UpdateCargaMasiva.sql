		
-- Actualiza cargas masivas validas y procesadas
UPDATE CM
SET 
	 CM."IdEstado"     = 'PRO'
	,CM."FechaProceso" = CAST(:FechaProceso AS SECONDDATE)
	,CM."IdMoneda"     = :IdMoneda
	,CM."IdConcepto"   = :IdConcepto
FROM "BSCL.Tesoreria.BD::CAR.CargaMasiva" AS CM
INNER JOIN "BSCL.Tesoreria.BD::GBL.ProcesarCargaMasiva_Proceso" AS PROC ON (PROC."IdCargaMasiva" = CM."IdCargaMasiva")
WHERE PROC."IdCargaMasiva" = :IdCargaMasiva;  -- Solo las cargas masivas validas
