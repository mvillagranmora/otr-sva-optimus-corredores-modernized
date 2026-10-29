SELECT
	DE."IdDireccionElectronica" AS "IdDireccionElectronica",
	DE."IdPersona" AS "IdPersona",
	DE."Borrado" AS "Borrado",
	DE."Predeterminado" AS "Predeterminado",
	COALESCE(PER."DocumentoIdentidad", '') AS "DocumentoIdentidad",
	COALESCE(PER."NombreCompleto", '') AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DE
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."IdPersona" = DE."IdPersona")
WHERE DE."IdDireccionElectronica" IN (:IdDireccionElectronica);
