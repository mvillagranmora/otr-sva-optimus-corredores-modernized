-- Actualizar estado de SolicitudCambio (AUT para autorizar, RZD para rechazar)
-- Usado por ambos: ConfirmarModificar y RechazarAutorización
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
  AND "IdTipoEntidad" = CASE 
        WHEN :IdConcepto = 'CAEEMP' THEN 'EMPLEADO-ESTADO'
        ELSE 'EMPLEADO'
  END
  AND "IdEstadoAutorizacion" = 'PEN'
