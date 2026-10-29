-- Actualiza el estado de SolicitudCambio (AUT para autorizar, RZD para rechazar)
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = CAST(:IdPersona AS NVARCHAR(40))
  AND "IdTipoEntidad" = 'PERSONA-ESTADO'
  AND "IdEstadoAutorizacion" = 'PEN';
