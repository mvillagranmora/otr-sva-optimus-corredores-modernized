-- Marcar DireccionElectronica como borrada aplicando cambios desde SolicitudCambio
-- Migración de ConfirmarEliminarPersonaDireccionElectronica.sql
-- Campo: Borrado (marca como 'S' para eliminación lógica)
UPDATE "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS TGT
SET 
    "Borrado" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdDireccionElectronica AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'DIRECCIONELECTRONICA-BORRAR'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'Borrado'),
        TGT."Borrado"),
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE TGT."IdDireccionElectronica" = :IdDireccionElectronica
