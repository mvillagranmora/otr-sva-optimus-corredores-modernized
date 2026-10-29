-- Actualizar estado de SolicitudCambio a Rechazado (solo MODDIF)
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = CAST(:IdDireccionFisica AS NVARCHAR(40))
  AND "IdTipoEntidad" = 'DIRECCIONFISICA'
  AND "IdEstadoAutorizacion" = 'PEN'
