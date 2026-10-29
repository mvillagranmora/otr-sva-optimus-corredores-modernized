-- Actualizar estado de DireccionFisica cuando se rechaza (solo MODDIF)
UPDATE "BSCL.SocioNegocio.BD::PER.DireccionFisica"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdDireccionFisica" = :IdDireccionFisica
