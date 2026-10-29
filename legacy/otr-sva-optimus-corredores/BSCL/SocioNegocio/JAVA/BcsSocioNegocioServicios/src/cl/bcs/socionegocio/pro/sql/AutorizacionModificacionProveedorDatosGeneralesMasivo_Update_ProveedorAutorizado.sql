-- Actualizar Proveedor aplicando cambios desde SolicitudCambio
-- Migración de ConfirmarModificarProveedorDatosGeneralesMasivo.sql
-- Campos: IdEstado, IdAreaProveedor
UPDATE "BSCL.SocioNegocio.BD::PRO.Proveedor" AS TGT
SET 
    "IdEstado" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdProveedor AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEPRO' THEN 'PROVEEDOR-ESTADO'
                ELSE 'PROVEEDOR'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdEstado'),
        TGT."IdEstado"),
    "IdAreaProveedor" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdProveedor AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAEPRO' THEN 'PROVEEDOR-ESTADO'
                ELSE 'PROVEEDOR'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdAreaProveedor'),
        TGT."IdAreaProveedor"),
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE TGT."IdProveedor" = :IdProveedor
