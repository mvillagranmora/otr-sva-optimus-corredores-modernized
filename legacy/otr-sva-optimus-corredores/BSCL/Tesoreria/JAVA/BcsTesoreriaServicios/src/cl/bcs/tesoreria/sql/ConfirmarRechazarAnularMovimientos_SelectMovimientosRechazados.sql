SELECT RAMTE."IdIngresoEgreso"
FROM #RechazarAnularMovimientosTesoreria RAMTE
INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" IE ON (IE."IdIngresoEgreso" = RAMTE."IdIngresoEgreso")
INNER JOIN "BSCL.Plataforma.BD::AUT.Evento" EVE ON (EVE."IdEvento" = IE."IdEvento")
WHERE EVE."IdEstado" = 'RZD';