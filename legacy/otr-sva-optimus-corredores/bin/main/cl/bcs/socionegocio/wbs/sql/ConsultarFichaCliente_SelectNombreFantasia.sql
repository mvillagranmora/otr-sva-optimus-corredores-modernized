SELECT 
NombreFantasia."IdPersona"
,COALESCE(NombreFantasia."IdNombreFantasia",0) AS "IdNombreFantasia"
,COALESCE(NombreFantasia."DescripcionNombreFantasia",'') AS "Nombre"
FROM "BSCL.SocioNegocio.BD::PER.NombreFantasia" AS NombreFantasia
WHERE NombreFantasia."IdPersona" IN (:p_IdPersonas)