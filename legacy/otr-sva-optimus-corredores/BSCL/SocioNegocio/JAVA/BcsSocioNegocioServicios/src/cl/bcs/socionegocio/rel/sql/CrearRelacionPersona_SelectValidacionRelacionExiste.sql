SELECT "IdRelacionPersona"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" 
WHERE "IdPersonaOrigen" = :IdPersonaOrigen 
AND "IdPersonaDestino" = :IdPersonaDestino 
AND "IdTipoRelacion" = :IdTipoRelacion;