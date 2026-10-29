UPDATE IEFP
SET IEFP."IdEstado" = 'ANU'
FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS IEFP
INNER JOIN #AutorizarAnularMovimientosTesoreria AS AAMTE
ON (IEFP."IdIngresoEgreso" = AAMTE."IdIngresoEgreso");