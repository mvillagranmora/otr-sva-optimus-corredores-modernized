-- Actualizar estado del Teléfono cuando se rechaza (solo MODTEL)
UPDATE "BSCL.SocioNegocio.BD::PER.Telefono"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdTelefono" = :IdTelefono
