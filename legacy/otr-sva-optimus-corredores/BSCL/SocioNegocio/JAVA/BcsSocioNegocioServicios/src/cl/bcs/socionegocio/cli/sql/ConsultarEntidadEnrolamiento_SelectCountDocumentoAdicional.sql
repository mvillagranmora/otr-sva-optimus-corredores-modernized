SELECT PERSONA."IdPersona"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PERSONA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCUMENTO_IDENTIDAD ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCUMENTO_IDENTIDAD_ADICIONAL ON PERSONA."IdDocumentoIdentidadAdicional" = DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
WHERE 
DOCUMENTO_IDENTIDAD_ADICIONAL."DocumentoIdentidad" = :DocumentoIdentidadAdicional 
AND DOCUMENTO_IDENTIDAD_ADICIONAL."IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidadAdicional
AND DOCUMENTO_IDENTIDAD."DocumentoIdentidad" <> :DocumentoIdentidad;