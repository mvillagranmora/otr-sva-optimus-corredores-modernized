UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "ComentarioBloqueo" = '',
	"ComentarioDesbloqueo" = :ValorNuevo,
	"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdPortafolio" = :IdPortafolio
