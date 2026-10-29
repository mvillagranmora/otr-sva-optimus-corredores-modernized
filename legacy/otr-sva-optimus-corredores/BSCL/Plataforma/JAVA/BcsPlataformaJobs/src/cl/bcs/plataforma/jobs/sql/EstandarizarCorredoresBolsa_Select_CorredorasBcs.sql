SELECT 
	COR."IdCorredora", 
	COR."IdPersona", 
	COR."CodigoInscripcionRegulador", 
	COR."IdEstado", 
	COR."Borrado", 
	COR."NombreCorto",
	PER."RazonSocial"
FROM "BSCL.Plataforma.BD::INS.Corredora" COR
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON COR."IdPersona" = PER."IdPersona"
WHERE COR."IdEstado" = 'ACT'
ORDER BY TRIM("NombreCorto") ASC