-- Actualizar estado de SolicitudCambio (AUT para autorizar, RZD para rechazar)
-- Usado por ambos: ConfirmarModificar y RechazarAutorización
UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = CAST(:IdProveedor AS NVARCHAR(40))
  AND "IdTipoEntidad" = CASE 
        WHEN :IdConcepto = 'CAEPRO' THEN 'PROVEEDOR-ESTADO'
        ELSE 'PROVEEDOR'
  END
  AND "IdEstadoAutorizacion" = 'PEN'
