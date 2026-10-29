UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "ComentarioBloqueo" = :ValorNuevo,
	"ComentarioDesbloqueo" = '',
	"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdPortafolio" = :IdPortafolio
