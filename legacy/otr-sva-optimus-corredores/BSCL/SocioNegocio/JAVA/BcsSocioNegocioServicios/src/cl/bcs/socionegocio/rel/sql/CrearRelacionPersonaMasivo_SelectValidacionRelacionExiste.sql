SELECT 
"IdRelacionPersona"
,"IdPersonaDestino"
,"IdTipoRelacion"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" 
WHERE "IdPersonaOrigen" IN (:IdPersonaOrigen); 