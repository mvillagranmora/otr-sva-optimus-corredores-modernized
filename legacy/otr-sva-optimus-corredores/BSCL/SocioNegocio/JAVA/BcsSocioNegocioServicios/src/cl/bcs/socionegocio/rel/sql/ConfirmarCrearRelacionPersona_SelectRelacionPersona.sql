SELECT REL."IdPersonaOrigen", REL."IdPersonaDestino", REL."IdRelacionPersona"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL
WHERE REL."IdRelacionPersona" = :IdRelacionPersona;