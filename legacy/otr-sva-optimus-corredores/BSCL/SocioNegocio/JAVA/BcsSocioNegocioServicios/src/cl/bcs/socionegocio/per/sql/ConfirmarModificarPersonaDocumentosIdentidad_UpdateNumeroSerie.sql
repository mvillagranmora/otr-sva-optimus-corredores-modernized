UPDATE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
SET "NumeroSerie" = :p_ValorNuevo, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDocumentoIdentidad" = :p_IdDocumentoIdentidad
