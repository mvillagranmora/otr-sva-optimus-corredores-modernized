SELECT "IdEnrolamiento"
FROM "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
WHERE "IdTipoEntidad" = :IdTipoEntidad
AND "IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad
AND "DocumentoIdentidad" = :DocumentoIdentidad
