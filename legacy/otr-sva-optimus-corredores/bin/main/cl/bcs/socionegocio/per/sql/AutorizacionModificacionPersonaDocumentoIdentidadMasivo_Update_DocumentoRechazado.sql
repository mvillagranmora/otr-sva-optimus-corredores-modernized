-- Actualizar estado del DocumentoIdentidad cuando se rechaza (solo MODDOC/CAEDOC)
UPDATE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdDocumentoIdentidad" = :IdDocumentoIdentidad
