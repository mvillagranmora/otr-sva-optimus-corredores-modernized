SELECT
	DF."IdDireccionFisica" AS "IdDireccionFisica",
	DF."IdPersona" AS "IdPersona",
	COALESCE(DF."Calle", '') AS "Calle",
	COALESCE(DF."Numero", '') AS "Numero",
	COALESCE(CIU."NombreCiudad", '') AS "NombreCiudad"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" DF
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIU ON (COALESCE(DF."IdCiudad", 0) = COALESCE(CIU."IdCiudad", 0))
WHERE DF."IdDireccionFisica" IN (:IdDireccionFisica);
