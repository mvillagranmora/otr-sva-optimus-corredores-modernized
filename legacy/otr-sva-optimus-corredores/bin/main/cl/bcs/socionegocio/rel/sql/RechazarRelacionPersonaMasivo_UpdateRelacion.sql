UPDATE REL 
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL
SET REL."IdEstadoAutorizacion" = :p_IdEstadoAutorizacion,
    REL."IdEstado" = CASE 
        WHEN :p_IdEstado IS NOT NULL THEN :p_IdEstado
        ELSE REL."IdEstado"
    END
WHERE REL."IdRelacionPersona" = :p_IdRelacionPersona
