UPDATE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
SET "IdEstado" = :p_ValorNuevo, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDocumentoIdentidad" = :p_IdDocumentoIdentidad
