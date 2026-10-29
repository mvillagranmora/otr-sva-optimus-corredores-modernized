UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
	"IdEstado" = :IdEstado
WHERE "IdPortafolio" = :IdPortafolio
