-- Actualizar estado de SolicitudCambio a Rechazado
-- Para MODDOC: TipoEntidad = DOCUMENTOIDENTIDAD
-- Para CAEDOC: TipoEntidad = DOCUMENTOIDENTIDAD-ESTADO
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = CAST(:IdDocumentoIdentidad AS NVARCHAR(40))
  AND "IdTipoEntidad" = CASE WHEN :IdConcepto = 'CAEDOC' THEN 'DOCUMENTOIDENTIDAD-ESTADO' ELSE 'DOCUMENTOIDENTIDAD' END
  AND "IdEstadoAutorizacion" = 'PEN'
