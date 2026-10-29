SELECT
	DF."IdDireccionFisica" AS "IdDireccionFisica",
	DF."IdPersona" AS "IdPersona",
	DF."Borrado" AS "Borrado",
	DF."Predeterminado" AS "Predeterminado",
	COALESCE(PER."DocumentoIdentidad", '') AS "DocumentoIdentidad",
	COALESCE(PER."NombreCompleto", '') AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" DF
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."IdPersona" = DF."IdPersona")
WHERE DF."IdDireccionFisica" IN (:IdDireccionFisica);
