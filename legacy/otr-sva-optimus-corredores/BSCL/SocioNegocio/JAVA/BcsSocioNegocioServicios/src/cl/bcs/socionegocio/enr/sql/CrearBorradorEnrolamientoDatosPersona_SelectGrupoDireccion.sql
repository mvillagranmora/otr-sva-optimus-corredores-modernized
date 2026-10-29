SELECT 
"IdGrupoDireccion"
,"Grupo"
,"Comentario"
,"IdPersona"
FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccion"
WHERE "IdPersona" IN (:IdPersona) AND "Borrado" = 'N';