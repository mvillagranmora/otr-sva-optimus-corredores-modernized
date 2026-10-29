SELECT COUNT(1) AS "Contador"
FROM "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
WHERE "IdTipoEntidad" = :IdTipoEntidad
AND "IdTipoDocumentoIdentidad" = :IdTipoDocumentoIdentidad
AND "DocumentoIdentidad" = :DocumentoIdentidad
AND "Tipo" = :Tipo
