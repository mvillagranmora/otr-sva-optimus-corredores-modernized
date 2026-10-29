SELECT
	TELPERS."IdPersona" AS "IdPersona",
	COALESCE(PAIS."NombrePais", '') AS "Pais",
	COALESCE(TELPERS."Numero", '') AS "NumeroTelefono",
	COALESCE(TELPERS."NombreTipoTelefono", '') AS "TipoTelefono",
	COALESCE(TELPERS."Anexo", '') AS "Anexo",
	COALESCE(TELPERS."DescripcionProposito", '') AS "Proposito",
	COALESCE(TELPERS."Comentario", '') AS "Comentario",
	COALESCE(TELPERS."NombreEstado", '') AS "Estado"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TELPERS
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS ON (COALESCE(TELPERS."IdPais", 0) = PAIS."IdPais")
WHERE TELPERS."IdTelefono" = :p_IdTelefono;
