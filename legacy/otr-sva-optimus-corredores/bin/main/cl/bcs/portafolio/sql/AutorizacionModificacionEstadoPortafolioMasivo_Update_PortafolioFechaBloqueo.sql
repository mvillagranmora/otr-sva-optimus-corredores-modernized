UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "FechaBloqueo" = CURRENT_UTCTIMESTAMP,
	"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdPortafolio" = :IdPortafolio
