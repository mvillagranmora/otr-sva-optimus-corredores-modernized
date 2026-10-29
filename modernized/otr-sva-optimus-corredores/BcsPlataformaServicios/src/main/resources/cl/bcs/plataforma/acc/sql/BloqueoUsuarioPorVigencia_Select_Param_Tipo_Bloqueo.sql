SELECT
	"IdTipoBloqueo",
	"NombreTipoBloqueo",
	"ComentarioPorDefecto",
	"IdAccion"
FROM "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"
WHERE "IdTipoBloqueo" = :IdTipoBloqueo;