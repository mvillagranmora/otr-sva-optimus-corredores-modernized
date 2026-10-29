-- Actualiza IdEstadoAutorizacion en SolicitudCambio (para AUT o RZD)
-- Concepto CAEEMP con IdTipoEntidad='EMPLEADO-ESTADO'
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdTipoEntidad" = 'EMPLEADO-ESTADO'
  AND "IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
  AND "IdEstadoAutorizacion" = 'PEN';
