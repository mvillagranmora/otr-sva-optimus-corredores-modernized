-- Actualizar estado de SolicitudCambio a Rechazado
-- Solo para MODTEL (modificaciones)
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = CAST(:IdTelefono AS NVARCHAR(40))
  AND "IdTipoEntidad" = 'TELEFONO'
  AND "IdEstadoAutorizacion" = 'PEN'
