UPDATE IE
SET 
	IE."IdEstado"				= 'ANU',
	IE."IdUsuarioAnulacion" 	= :p_IdUsuario,
	IE."FechaHoraAnulacion"		= CURRENT_UTCTIMESTAMP
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE,
	 #AutorizarAnularMovimientosTesoreria AS AAMTE 
WHERE IE."IdIngresoEgreso" = AAMTE."IdIngresoEgreso";