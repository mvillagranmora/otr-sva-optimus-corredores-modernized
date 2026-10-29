-- Actualizar estado de SolicitudCambio a Confirmado (ING para ingresado/confirmado)
-- Usado por ConfirmarModificarContraparteDatosGenerales cuando no requiere autorización
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdSolicitudCambio" = :IdSolicitudCambio
  AND "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
  AND "IdTipoEntidad" = 'CONTRAPARTE'
  AND "IdEstadoAutorizacion" = 'PEN'
