SELECT "IdNombreFantasia" AS "IdNombreFantasia", "IdPersona" AS "IdPersona",
	COALESCE("DescripcionNombreFantasia",'') AS "DescripcionNombreFantasia",
	COALESCE("IdEstado",'') AS "IdEstado"
FROM "BSCL.SocioNegocio.BD::PER.NombreFantasia"
WHERE "IdNombreFantasia" = :p_IdNombreFantasia AND "IdPersona" = :p_IdPersona
