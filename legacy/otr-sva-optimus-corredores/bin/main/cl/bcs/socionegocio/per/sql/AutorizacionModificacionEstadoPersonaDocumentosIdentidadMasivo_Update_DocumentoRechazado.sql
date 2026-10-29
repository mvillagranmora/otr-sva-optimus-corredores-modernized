-- Rechazo de documento: replica lógica del SP RechazaAutorizacionPersonaDocumentosIdentidadMasivo
UPDATE DOC
SET
    "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
    "IdEstado" = CASE WHEN :IdConcepto = 'NUEDOC' THEN 'INA' ELSE "IdEstado" END,
    "Borrado" = CASE WHEN :IdConcepto = 'NUEDOC' THEN 'S' ELSE 'N' END
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC
WHERE DOC."IdDocumentoIdentidad" = :IdDocumentoIdentidad;
