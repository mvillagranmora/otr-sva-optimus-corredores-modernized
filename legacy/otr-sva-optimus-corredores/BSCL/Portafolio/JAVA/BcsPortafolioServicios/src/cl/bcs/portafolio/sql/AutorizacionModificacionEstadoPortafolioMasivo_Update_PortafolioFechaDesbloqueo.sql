UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "FechaDesbloqueo" = CURRENT_UTCTIMESTAMP,
	"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdPortafolio" = :IdPortafolio
