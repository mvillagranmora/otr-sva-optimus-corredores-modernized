UPDATE "BSCL.SocioNegocio.BD::ENR.DocumentoIdentidad"
SET "IdTipoDocumentoIdentidad" = COALESCE(:IdTipoDocumentoIdentidad,"IdTipoDocumentoIdentidad")
,"DocumentoIdentidad" = COALESCE(:DocumentoIdentidad,"DocumentoIdentidad")
,"NumeroSerie" = COALESCE(:NumeroSerie,"NumeroSerie")
,"FechaEmision" = COALESCE(:FechaEmision,"FechaEmision")
,"FechaVencimiento" = COALESCE(:FechaVencimiento,"FechaVencimiento")
,"Predeterminado" = COALESCE(:Predeterminado,"Predeterminado")
,"IdTipoDocumentoIdentidadAsociado" = COALESCE(:IdTipoDocumentoIdentidadAsociado,"IdTipoDocumentoIdentidadAsociado")
,"TipoPersona" = COALESCE(:TipoPersona,"TipoPersona")
,"DocumentoIdentidadAsociado" = COALESCE(:DocumentoIdentidadAsociado,"DocumentoIdentidadAsociado")
WHERE "IdDocumentoIdentidad" = :IdDocumentoIdentidad;