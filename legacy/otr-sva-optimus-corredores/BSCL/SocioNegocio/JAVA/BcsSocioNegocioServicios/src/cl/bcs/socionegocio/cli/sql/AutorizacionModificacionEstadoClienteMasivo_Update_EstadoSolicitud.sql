-- Actualiza estado de SolicitudCambio (AUT/RZD)
-- Para CAECLI usa CLIENTE-ESTADO; para otros conceptos usa CLIENTE
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
  AND "IdTipoEntidad" = CASE
      WHEN :IdConcepto = 'CAECLI' THEN 'CLIENTE-ESTADO'
      ELSE 'CLIENTE'
  END
  AND "IdEstadoAutorizacion" = 'PEN';
