UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdTipoBloqueo" = :ValorNuevo,
	"FechaBloqueo" = CURRENT_UTCTIMESTAMP,
	"IdTipoDesbloqueo" = '',
	"FechaDesbloqueo" = '0001-01-01',
	"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdPortafolio" = :IdPortafolio
