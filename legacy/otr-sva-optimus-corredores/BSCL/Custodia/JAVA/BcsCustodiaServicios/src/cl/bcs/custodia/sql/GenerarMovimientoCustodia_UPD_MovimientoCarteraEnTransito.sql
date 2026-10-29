/********************************************************** 
Proposito	: Script para la actualización de montos valorizados en la tabla Transito
autor		: NRM
fecha		: 16/05/2017
***********************************************************/


UPDATE "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
SET
	"IdMovimientoCarteraEnTransitoRelacionado"	= COALESCE(:IdMovimientoCarteraEnTransitoRelacionado,"IdMovimientoCarteraEnTransitoRelacionado"),
	"FechaCertificacionRegistro"				= COALESCE(:FechaCertificacionRegistro,"FechaCertificacionRegistro"),
	"FechaAnulacionRegistro"					= COALESCE(:FechaAnulacionRegistro,"FechaAnulacionRegistro"),
	"IdUsuarioCertificacion"					= COALESCE(:IdUsuarioCertificacion,"IdUsuarioCertificacion"),
	"IdUsuarioAnulacion"						= COALESCE(:IdUsuarioAnulacion,"IdUsuarioAnulacion"),
	"IdEstado"									= COALESCE(:IdEstado,"IdEstado")
WHERE "IdMovimientoCarteraEnTransito" = :IdMovimientoCarteraEnTransito;
