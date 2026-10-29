UPDATE REL
SET "IdEstadoAutorizacion" = :p_IdEstadoAutorizacion
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL
WHERE REL."IdRelacionPersona" = :p_IdRelacionPersona
