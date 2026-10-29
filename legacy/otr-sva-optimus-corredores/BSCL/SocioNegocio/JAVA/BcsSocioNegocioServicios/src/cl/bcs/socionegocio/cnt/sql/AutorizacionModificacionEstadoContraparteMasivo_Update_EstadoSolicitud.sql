-- Actualiza estado de SolicitudCambio para contraparte (AUT/RZD)
-- Para CAECNT usa CONTRAPARTE-ESTADO; para otros conceptos mantiene CONTRAPARTE
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
  AND "IdTipoEntidad" = CASE
      WHEN :IdConcepto = 'CAECNT' THEN 'CONTRAPARTE-ESTADO'
      ELSE 'CONTRAPARTE'
  END
  AND "IdEstadoAutorizacion" = 'PEN';
