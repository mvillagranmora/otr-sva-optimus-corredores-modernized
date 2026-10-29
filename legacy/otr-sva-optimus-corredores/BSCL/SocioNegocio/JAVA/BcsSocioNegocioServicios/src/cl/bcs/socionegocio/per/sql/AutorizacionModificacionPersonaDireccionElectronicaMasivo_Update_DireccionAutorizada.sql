-- Actualizar DireccionElectronica aplicando cambios desde SolicitudCambio
-- Migración de ConfirmarModificarPersonaDireccionElectronicaMasivo.sql
-- 4 Campos: Direccion, CodigoProposito, Comentario, IdEstado
UPDATE "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS TGT
SET 
    "Direccion" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdDireccionElectronica AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'DIRECCIONELECTRONICA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'Direccion'),
        TGT."Direccion"),
    "CodigoProposito" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdDireccionElectronica AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'DIRECCIONELECTRONICA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'CodigoProposito'),
        TGT."CodigoProposito"),
    "Comentario" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdDireccionElectronica AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'DIRECCIONELECTRONICA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'Comentario'),
        TGT."Comentario"),
    "IdEstado" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdDireccionElectronica AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'DIRECCIONELECTRONICA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdEstado'),
        TGT."IdEstado"),
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE TGT."IdDireccionElectronica" = :IdDireccionElectronica
