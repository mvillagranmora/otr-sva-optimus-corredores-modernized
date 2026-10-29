SELECT "IdRelacionPersona"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" 
WHERE "IdPersonaOrigen" = :IdPersonaOrigen 
AND "IdTipoRelacion" = :IdTipoRelacion 
AND "IdEstado" = 'ACT'
AND "IdTipoRelacion" in ('AR1','AR2');