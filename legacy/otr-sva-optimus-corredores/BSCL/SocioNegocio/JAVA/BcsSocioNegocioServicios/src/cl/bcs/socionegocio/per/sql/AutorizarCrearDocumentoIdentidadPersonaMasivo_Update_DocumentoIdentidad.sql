UPDATE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
    "IdEstado" = CASE 
        WHEN :IdEstado IS NOT NULL THEN :IdEstado
        ELSE "IdEstado"
    END,
    "Borrado" = CASE 
        WHEN :Borrado IS NOT NULL THEN :Borrado
        ELSE "Borrado"
    END
WHERE "IdDocumentoIdentidad" = :IdDocumentoIdentidad
