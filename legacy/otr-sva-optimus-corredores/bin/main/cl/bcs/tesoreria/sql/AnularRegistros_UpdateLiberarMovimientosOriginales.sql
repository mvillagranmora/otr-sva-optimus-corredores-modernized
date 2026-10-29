UPDATE IE
	SET "IdEstado" = CASE WHEN :p_IdModulo != 'MRF' THEN 'ING' ELSE 'ANU' END , 
	"IdUsuarioEmision"	= 0,
	"FechaHoraEmision"	= '0001-01-01 00:00:00'
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE
INNER JOIN #AnularRegistros_ComprobantesAnular AS CA ON (IE."IdIngresoEgreso" = CA."IdIngresoEgresoOrigen");