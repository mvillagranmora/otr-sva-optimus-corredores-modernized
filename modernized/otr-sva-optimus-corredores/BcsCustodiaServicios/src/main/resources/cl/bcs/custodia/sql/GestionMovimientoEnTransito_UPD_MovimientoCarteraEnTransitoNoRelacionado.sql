/********************************************************** 
Proposito	: Script para la actualización de montos valorizados en la tabla Transito
autor		: NRM
fecha		: 20/05/2017
***********************************************************/


UPDATE "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
SET
	"FechaCertificacionRegistro"	= CASE WHEN COALESCE(:IdUsuarioCertificacion,0) > 0 THEN CURRENT_UTCTIMESTAMP ELSE "FechaCertificacionRegistro" END, 	
	"IdUsuarioCertificacion" 		= COALESCE(:IdUsuarioCertificacion, "IdUsuarioCertificacion"),
	"FechaAnulacionRegistro" 		= CASE WHEN COALESCE(:IdUsuarioAnulacion,0) > 0 THEN CURRENT_UTCTIMESTAMP ELSE "FechaAnulacionRegistro" END,
	"IdUsuarioAnulacion" 			= COALESCE(:IdUsuarioAnulacion, "IdUsuarioAnulacion"),
	"IdEstado"						= COALESCE(:IdEstado, "IdEstado")
WHERE "IdMovimientoCarteraEnTransito" = :IdMovimientoCarteraEnTransito;