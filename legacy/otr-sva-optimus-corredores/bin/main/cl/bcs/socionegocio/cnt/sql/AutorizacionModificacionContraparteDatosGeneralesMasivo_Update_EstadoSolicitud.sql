-- Actualizar estado de SolicitudCambio (AUT para autorizar, RZD para rechazar)
-- Usado por ambos: ConfirmarModificar y RechazarAutorización
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
  AND "IdTipoEntidad" = CASE 
        WHEN :IdConcepto = 'CAECNT' THEN 'CONTRAPARTE-ESTADO'
        ELSE 'CONTRAPARTE'
  END
  AND "IdEstadoAutorizacion" = 'PEN'
