-- Actualizar estado de autorización de portafolio
UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdPortafolio" = :IdPortafolio
