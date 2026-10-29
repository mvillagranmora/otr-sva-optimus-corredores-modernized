SELECT 
'S' AS "EntidadExiste"
, Enrolamiento."IdEnrolamiento" AS "IdEntidad"
FROM "BSCL.SocioNegocio.BD::ENR.Enrolamiento" AS Enrolamiento
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.DocumentoIdentidad" AS DocumentoIdentidad ON DocumentoIdentidad."IdDocumentoIdentidad" = Enrolamiento."IdDocumentoIdentidadAdicional"
WHERE DocumentoIdentidad."DocumentoIdentidad" = :DocumentoIdentidad AND DocumentoIdentidad."IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad;