SELECT "IdEnrolamiento"
FROM "BSCL.SocioNegocio.BD::SOC.Enrolamiento"
WHERE "IdTipoEntidad"           = :p_llave_IdTipoEntidad
AND "IdTipoDocumentoIdentidad"  = :p_llave_IdTipoDocumentoIdentidad
AND "DocumentoIdentidad"        = :p_llave_DocumentoIdentidad;