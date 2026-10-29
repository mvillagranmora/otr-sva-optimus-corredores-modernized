SELECT
	PER."IdPersona" AS "IdPersona",
	CASE WHEN LENGTH(PER."NombreCompleto") > 100 THEN LEFT(PER."NombreCompleto", 100) ELSE PER."NombreCompleto" END AS "NombreCompleto",
	COALESCE(PER."DocumentoIdentidad", '') AS "DocumentoIdentidad",
	COALESCE(PER."TipoDocumentoIdentidad", '') AS "TipoDocumentoIdentidad",
	COALESCE(PERDIRECC."NombreCiudad", '') AS "Ciudad",
	COALESCE(PERDIRECC."NombrePais", '') AS "Pais",
	COALESCE(PERDIRECC."Numero", '') AS "Numero",
	COALESCE(PERDIRECC."Sector", '') AS "Sector",
	COALESCE(PERDIRECC."CodigoPostal", '') AS "CodigoPostal",
	COALESCE(PERDIRECC."DescripcionProposito", '') AS "Proposito",
	COALESCE(PERDIRECC."Comentario", '') AS "Comentario",
	COALESCE(PERDIRECC."NombreEstado", '') AS "Estado"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" PERDIRECC
	ON (PER."IdPersona" = PERDIRECC."IdPersona")
WHERE PERDIRECC."IdDireccionFisica" = :IdDireccionFisica
