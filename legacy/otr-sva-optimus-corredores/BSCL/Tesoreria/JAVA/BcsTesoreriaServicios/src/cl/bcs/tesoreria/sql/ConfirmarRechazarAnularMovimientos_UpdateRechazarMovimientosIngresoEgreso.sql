UPDATE IE
SET 
	IE."IdEvento" = 0
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" IE
INNER JOIN #RechazarAnularMovimientosTesoreria RAMTE ON (RAMTE."IdIngresoEgreso" = IE."IdIngresoEgreso");