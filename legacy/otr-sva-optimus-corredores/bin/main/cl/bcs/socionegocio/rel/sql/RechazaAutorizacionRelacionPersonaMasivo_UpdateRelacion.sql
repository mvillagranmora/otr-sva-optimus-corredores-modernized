UPDATE "BSCL.SocioNegocio.BD::REL.RelacionPersona"
SET "IdEstadoAutorizacion" = :p_IdEstadoAutorizacion,
    "IdEstado" = CASE 
        WHEN :p_IdEstado IS NOT NULL THEN :p_IdEstado
        ELSE "IdEstado"
    END
WHERE "IdRelacionPersona" = :p_IdRelacionPersona
