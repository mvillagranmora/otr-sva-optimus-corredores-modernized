UPDATE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
SET "IdTipoDocumentoIdentidad" = :p_ValorNuevo, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDocumentoIdentidad" = :p_IdDocumentoIdentidad
