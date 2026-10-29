SELECT AAMTE."IdIngresoEgreso"
FROM #AutorizarAnularMovimientosTesoreria AAMTE
INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" IE ON (IE."IdIngresoEgreso" = AAMTE."IdIngresoEgreso"
AND IE."IdEstado" = 'ANU');