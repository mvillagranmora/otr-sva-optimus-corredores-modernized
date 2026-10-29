SELECT
	T."IdTelefono" AS "IdTelefono",
	T."IdPersona" AS "IdPersona",
	COALESCE(T."Numero", '') AS "Numero"
FROM "BSCL.SocioNegocio.BD::PER.Telefono" T
WHERE T."IdTelefono" IN (:IdTelefono);
