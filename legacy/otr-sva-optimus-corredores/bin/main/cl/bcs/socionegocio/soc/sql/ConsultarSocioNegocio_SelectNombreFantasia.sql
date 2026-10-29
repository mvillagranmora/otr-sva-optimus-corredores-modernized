SELECT 
NombreFantasia."IdNombreFantasia"
,NombreFantasia."DescripcionNombreFantasia" AS "Nombre"
FROM "BSCL.SocioNegocio.BD::PER.NombreFantasia" AS NombreFantasia
WHERE NombreFantasia."IdPersona" = :p_IdPersona