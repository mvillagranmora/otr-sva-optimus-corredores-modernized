SELECT
"IdTipoRelacion", 
"NombreTipoRelacion"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion"
WHERE "IdTipoRelacion" in (:IdsTipoRelacion);