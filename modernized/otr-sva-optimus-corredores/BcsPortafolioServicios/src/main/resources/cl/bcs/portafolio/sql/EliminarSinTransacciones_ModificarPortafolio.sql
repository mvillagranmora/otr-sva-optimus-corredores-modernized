UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET 	"FechaModificacion" = :FechaModificacion,
		"ComentarioBloqueo" = :ComentarioBloqueo,
		"Borrado" 			= 'S',
		"CodigoPortafolio"  = :CodigoPortafolio,
		"IdEstado"			= 'BOR'
WHERE "IdPortafolio" 		= :IdPortafolio
