SELECT
ENROLAMIENTO."IdEnrolamiento"
,ENROLAMIENTO."IdDireccionFisicaPrincipal"
,ENROLAMIENTO."IdDireccionElectronicaPrincipal"
,ENROLAMIENTO."IdTelefonoPrincipal"
,ENROLAMIENTO."IdDocumentoIdentidad"
,ENROLAMIENTO."IdDocumentoIdentidadAdicional"
,ENROLAMIENTO."CLI_IdTipoCliente" AS "IdTipoCliente"
,CASE WHEN FATCA."IdEnrolamiento" IS NULL THEN 'N' ELSE 'S' END AS "TieneFatca" 
FROM "BSCL.SocioNegocio.BD::ENR.Enrolamiento" ENROLAMIENTO
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.DocumentoIdentidad" DOCUMENTO_IDENTIDAD ON DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad" = ENROLAMIENTO."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::ENR.Fatca" FATCA ON ENROLAMIENTO."IdEnrolamiento" = FATCA."IdEnrolamiento"
WHERE ENROLAMIENTO."IdTipoEntidad" = :IdTipoEntidad
AND DOCUMENTO_IDENTIDAD."IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad
AND DOCUMENTO_IDENTIDAD."DocumentoIdentidad" = :DocumentoIdentidad;