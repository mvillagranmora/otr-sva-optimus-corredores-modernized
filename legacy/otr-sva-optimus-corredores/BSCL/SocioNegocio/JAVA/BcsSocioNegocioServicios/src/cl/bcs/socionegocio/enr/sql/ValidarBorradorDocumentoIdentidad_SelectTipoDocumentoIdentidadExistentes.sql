SELECT
DocumentoIdentidad."IdDocumentoIdentidad"
,DocumentoIdentidad."IdTipoDocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::ENR.DocumentoIdentidad" as DocumentoIdentidad 
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.CrearBorradorDocumentoIdentidad" CrearBorradorDocumentoIdentidad 
	ON CrearBorradorDocumentoIdentidad."IdEnrolamiento" = DocumentoIdentidad."IdEnrolamiento" AND CrearBorradorDocumentoIdentidad."IdTipoDocumentoIdentidad" =  DocumentoIdentidad."IdTipoDocumentoIdentidad"
WHERE CrearBorradorDocumentoIdentidad."IdTrabajo" = :IdTrabajo AND CrearBorradorDocumentoIdentidad."IdDocumentoIdentidad" != DocumentoIdentidad."IdDocumentoIdentidad"