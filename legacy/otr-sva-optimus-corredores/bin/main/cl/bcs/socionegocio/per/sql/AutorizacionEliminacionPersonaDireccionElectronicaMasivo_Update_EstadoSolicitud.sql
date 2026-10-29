-- Actualizar estado de SolicitudCambio (AUT para autorizar, RZD para rechazar)
-- Usado por ambos: ConfirmarEliminar y RechazarEliminación
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = CAST(:IdDireccionElectronica AS NVARCHAR(40))
  AND "IdTipoEntidad" = 'DIRECCIONELECTRONICA-BORRAR'
  AND "IdEstadoAutorizacion" = 'PEN'
