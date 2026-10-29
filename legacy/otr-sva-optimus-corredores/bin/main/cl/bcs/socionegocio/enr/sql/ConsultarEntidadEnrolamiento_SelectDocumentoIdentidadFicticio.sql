SELECT "DocumentoIdentidadAsociado"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 
WHERE 
"IdTipoDocumentoIdentidadAsociado" = 'FIC' 
AND "DocumentoIdentidadAsociado" = TO_NVARCHAR(:DocumentoIdentidadFicticio)