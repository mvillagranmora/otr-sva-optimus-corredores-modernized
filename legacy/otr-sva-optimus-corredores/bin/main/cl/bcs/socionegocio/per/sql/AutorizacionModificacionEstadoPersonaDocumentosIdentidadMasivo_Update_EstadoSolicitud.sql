-- Actualiza el estado de SolicitudCambio según el tipo de concepto
-- CAEDOC => DOCUMENTOIDENTIDAD-ESTADO
-- MODDOC/NUEDOC => DOCUMENTOIDENTIDAD
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = CAST(:IdDocumentoIdentidad AS NVARCHAR(40))
  AND "IdTipoEntidad" = CASE
      WHEN :IdConcepto = 'CAEDOC' THEN 'DOCUMENTOIDENTIDAD-ESTADO'
      ELSE 'DOCUMENTOIDENTIDAD'
  END
  AND "IdEstadoAutorizacion" = 'PEN';
