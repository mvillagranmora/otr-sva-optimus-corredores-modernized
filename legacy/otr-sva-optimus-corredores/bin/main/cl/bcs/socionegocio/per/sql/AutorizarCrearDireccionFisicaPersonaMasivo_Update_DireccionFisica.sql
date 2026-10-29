UPDATE "BSCL.SocioNegocio.BD::PER.DireccionFisica"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
    "IdEstado" = CASE 
        WHEN :IdEstado IS NOT NULL THEN :IdEstado
        ELSE "IdEstado"
    END
WHERE "IdDireccionFisica" = :IdDireccionFisica
