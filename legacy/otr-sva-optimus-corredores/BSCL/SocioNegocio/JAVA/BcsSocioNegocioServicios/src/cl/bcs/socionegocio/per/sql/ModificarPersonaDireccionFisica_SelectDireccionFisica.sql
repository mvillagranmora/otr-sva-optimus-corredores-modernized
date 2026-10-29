SELECT
	DOC."IdDireccionFisica" AS "IdDireccionFisica",
	DOC."IdPersona" AS "IdPersona",
	COALESCE(CAST(DOC."Calle" AS NVARCHAR(255)),'') AS "Calle",
	COALESCE(CAST(DOC."Numero" AS NVARCHAR(255)),'') AS "Numero",
	COALESCE(CAST(DOC."Departamento" AS NVARCHAR(255)),'') AS "Departamento",
	COALESCE(CAST(DOC."Sector" AS NVARCHAR(255)),'') AS "Sector",
	COALESCE(CAST(DOC."CodigoPostal" AS NVARCHAR(255)),'') AS "CodigoPostal",
	COALESCE(CAST(DOC."IdCiudad" AS NVARCHAR(255)),'') AS "IdCiudad",
	COALESCE(CAST(DOC."IdDivisionPais" AS NVARCHAR(255)),'') AS "IdDivisionPais",
	COALESCE(CAST(DOC."CodigoProposito" AS NVARCHAR(255)),'') AS "CodigoProposito",
	COALESCE(CAST(DOC."Comentario" AS NVARCHAR(255)),'') AS "Comentario",
	COALESCE(CAST(DOC."IdEstado" AS NVARCHAR(255)),'') AS "IdEstado",
	COALESCE(CAST(DOC."CodigoCompuesto" AS NVARCHAR(255)),'') AS "CodigoCompuesto",
	COALESCE(PER."DocumentoIdentidad", '') AS "DocumentoIdentidadPersona",
	COALESCE(PER."NombreCompleto", '') AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" DOC
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."IdPersona" = DOC."IdPersona")
WHERE DOC."IdDireccionFisica" IN (:IdDireccionFisica);
