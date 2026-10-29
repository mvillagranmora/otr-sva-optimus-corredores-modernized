UPDATE "BSCL.SocioNegocio.BD::PER.DireccionElectronica"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
    "IdEstado" = CASE 
        WHEN :IdEstado IS NOT NULL THEN :IdEstado
        ELSE "IdEstado"
    END
WHERE "IdDireccionElectronica" = :IdDireccionElectronica
