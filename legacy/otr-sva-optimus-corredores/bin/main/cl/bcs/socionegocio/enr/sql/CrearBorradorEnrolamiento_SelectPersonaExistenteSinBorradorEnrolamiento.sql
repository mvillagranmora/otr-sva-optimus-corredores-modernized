SELECT
PERSONA."IdPersona"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PERSONA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCUMENTO_IDENTIDAD
	ON (PERSONA."IdPersona" = DOCUMENTO_IDENTIDAD."IdPersona"
	AND PERSONA."IdDocumentoIdentidad" = DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarEnrolamiento" ENROLAMIENTO 
	ON (ENROLAMIENTO."DocumentoIdentidad" =  DOCUMENTO_IDENTIDAD."DocumentoIdentidad" 
	AND ENROLAMIENTO."IdTipoDocumentoIdentidad" = DOCUMENTO_IDENTIDAD."IdTipoDocumentoIdentidad"
	AND ENROLAMIENTO."IdTipoEntidad" = :IdTipoEntidad)
WHERE 
DOCUMENTO_IDENTIDAD."IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad 
AND DOCUMENTO_IDENTIDAD."DocumentoIdentidad" = :DocumentoIdentidad
AND ENROLAMIENTO."IdEnrolamiento" IS NULL;