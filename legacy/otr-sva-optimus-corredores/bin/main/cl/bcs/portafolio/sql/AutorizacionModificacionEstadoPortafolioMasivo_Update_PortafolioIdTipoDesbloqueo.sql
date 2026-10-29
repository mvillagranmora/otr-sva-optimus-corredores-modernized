UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdTipoDesbloqueo" = :ValorNuevo,
	"FechaDesbloqueo" = CURRENT_UTCTIMESTAMP,
	"IdTipoBloqueo" = '',
	"FechaBloqueo" = '0001-01-01',
	"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdPortafolio" = :IdPortafolio
