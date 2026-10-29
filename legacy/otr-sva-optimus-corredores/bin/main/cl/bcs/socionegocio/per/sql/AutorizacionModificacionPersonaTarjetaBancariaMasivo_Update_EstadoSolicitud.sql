-- Actualizar estado de SolicitudCambio (AUT para autorizar, RZD para rechazar)
-- Usado por ambos: ConfirmarModificar y RechazarAutorización
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = CAST(:IdTarjetaBancaria AS NVARCHAR(40))
  AND "IdTipoEntidad" = CASE 
        WHEN :IdConcepto = 'CAETJB' THEN 'TARJETABANCARIA-ESTADO'
        ELSE 'TARJETABANCARIA'
  END
  AND "IdEstadoAutorizacion" = 'PEN'
