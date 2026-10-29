SELECT
PERSONA."IdPersona"
,CASE PERSONA."Tipo" 
	WHEN 'N' THEN PERSONA."Nombres" ||' '|| PERSONA."ApellidoPaterno" || ' ' || PERSONA."ApellidoMaterno" 
	ELSE PERSONA."RazonSocial"
END AS "NombreCompleto"
,DOCUMENTO_IDENTIDAD."DocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD ON DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad" = PERSONA."IdDocumentoIdentidad"
WHERE PERSONA."IdPersona" IN (:IdPersona)