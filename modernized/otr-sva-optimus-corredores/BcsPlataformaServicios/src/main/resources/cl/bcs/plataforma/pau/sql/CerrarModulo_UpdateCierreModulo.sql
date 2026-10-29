UPDATE "BSCL.Plataforma.BD::PAU.Modulo"
SET
	"IdEstado" = COALESCE(:Estado, "IdEstado"),
	"CodigoUsuario" = COALESCE(:CodigoUsuario, "CodigoUsuario"),
	"FechaUltimaActualizacion" = CURRENT_UTCTIMESTAMP
WHERE "CodigoModulo" = :CodigoModulo;