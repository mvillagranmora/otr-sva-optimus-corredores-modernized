SELECT 
Relacion."IdTipoRelacion"
,TipoRelacion."NombreTipoRelacion" AS "TipoRelacion"
,Relacion."IdPersonaOrigen"
,Relacion."IdPersonaDestino"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" Relacion
INNER JOIN "BSCL.SocioNegocio.BD::REL.TipoRelacion" AS TipoRelacion 
ON TipoRelacion."IdEmpresa" = :p_IdEmpresa AND TipoRelacion."IdTipoRelacion" = Relacion."IdTipoRelacion"
WHERE Relacion."IdPersonaOrigen" = :p_IdPersona