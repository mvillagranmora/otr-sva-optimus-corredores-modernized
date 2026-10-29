UPDATE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
SET "Predeterminado" = 'S'
,"IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdDocumentoIdentidad" = :IdDocumentoIdentidad;