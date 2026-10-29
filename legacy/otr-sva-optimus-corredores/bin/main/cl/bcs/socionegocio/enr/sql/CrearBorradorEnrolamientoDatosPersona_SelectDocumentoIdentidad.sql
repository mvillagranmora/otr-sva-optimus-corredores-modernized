SELECT 
"IdDocumentoIdentidad"
,"IdPersona"
,"IdTipoDocumentoIdentidad"
,"DocumentoIdentidad"
,"NumeroSerie"
,"FechaEmision"
,"FechaVencimiento"
,"Predeterminado"
,"IdTipoDocumentoIdentidadAsociado"
,"TipoPersona"
,"DocumentoIdentidadAsociado"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
WHERE "IdPersona" IN (:IdPersona) AND "Borrado" = 'N';