SELECT
'S' AS "PersonaExiste"
,"IdPersona"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PERSONA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCUMENTO_IDENTIDAD_ADICIONAL ON PERSONA."IdDocumentoIdentidadAdicional" = DOCUMENTO_IDENTIDAD_ADICIONAL."IdDocumentoIdentidad"
WHERE
DOCUMENTO_IDENTIDAD_ADICIONAL."DocumentoIdentidad" = :DocumentoIdentidad
AND DOCUMENTO_IDENTIDAD_ADICIONAL."IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad
AND PERSONA."Tipo" = (CASE WHEN :CodigoPais = 'PE' THEN :IdTipoPersona ELSE "Tipo" END);