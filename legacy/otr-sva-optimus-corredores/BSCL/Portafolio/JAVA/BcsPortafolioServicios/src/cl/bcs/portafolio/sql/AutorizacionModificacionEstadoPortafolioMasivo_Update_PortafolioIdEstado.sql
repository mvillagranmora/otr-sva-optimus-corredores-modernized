UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdEstado" = :ValorNuevo,
	"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdPortafolio" = :IdPortafolio
