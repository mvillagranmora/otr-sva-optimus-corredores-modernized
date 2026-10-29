SELECT COALESCE(CLI."TipoEnrolamiento", '') AS "TipoEnrolamiento" ,
	CASE WHEN PER."Tipo" = 'N'
		THEN 'NATURAL'
		ELSE 'JURIDICA' 
	END AS "Tipo"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS PER
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE PER."IdPersona" = :IdPersona;
