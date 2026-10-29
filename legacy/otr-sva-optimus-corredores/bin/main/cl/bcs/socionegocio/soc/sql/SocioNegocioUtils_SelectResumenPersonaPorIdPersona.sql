SELECT PER."IdPersona"
	 , CASE
		   WHEN PER."Tipo" = 'J' THEN PER."RazonSocial"
		   ELSE PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
	END                          AS "NombreCompleto"
	 , PDOC."DocumentoIdentidad" AS "DocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
	 LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" PDOC ON (PDOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
WHERE PER."IdPersona" IN (:IdsPersona);