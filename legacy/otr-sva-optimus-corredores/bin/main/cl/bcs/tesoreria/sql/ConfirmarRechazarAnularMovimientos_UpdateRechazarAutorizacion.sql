UPDATE EVE
SET 
	EVE."IdEstado"				= 'RZD',
	EVE."IdUsuarioCambioEstado" = :p_IdUsuario,
	EVE."FechaHoraCambioEstado"	= CURRENT_UTCTIMESTAMP
FROM "BSCL.Plataforma.BD::AUT.Evento" AS EVE
INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" IE ON (IE."IdEvento" = EVE."IdEvento")
INNER JOIN #RechazarAnularMovimientosTesoreria RAMTE ON (RAMTE."IdIngresoEgreso" = IE."IdIngresoEgreso");