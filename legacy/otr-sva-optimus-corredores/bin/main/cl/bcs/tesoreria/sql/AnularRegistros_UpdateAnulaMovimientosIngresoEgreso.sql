UPDATE IE
SET 
	"IdEstado"				= 'ANU',
	"IdUsuarioAnulacion" 	= :p_IdUsuario,
	"FechaHoraAnulacion"	= CURRENT_UTCTIMESTAMP
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE
INNER JOIN  #AnularRegistros_p_RegistrosAProcesar AS RP ON (IE."IdIngresoEgreso" = RP."Id")
WHERE RP."Entidad" = 'TES.IngresoEgreso';