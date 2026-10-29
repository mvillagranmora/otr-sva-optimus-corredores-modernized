SELECT 
"IdRelacionPersona"
,"IdPersonaDestino"
,"IdTipoRelacion"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" 
WHERE "IdPersonaOrigen" IN (:IdPersonaOrigen)
AND "IdTipoRelacion" IN ('AR1','AR2');