-- Actualiza DocumentoIdentidad con cambios pendientes de estado (CAEDOC)
UPDATE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS TGT
SET
    "IdEstado" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdDocumentoIdentidad AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'DOCUMENTOIDENTIDAD-ESTADO'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdEstado'),
        TGT."IdEstado"),
    "FechaModificacion" = CASE
        WHEN EXISTS (
            SELECT 1
            FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
            WHERE "IdEntidadOrigen" = CAST(:IdDocumentoIdentidad AS NVARCHAR(40))
              AND "IdTipoEntidad" = 'DOCUMENTOIDENTIDAD-ESTADO'
              AND "IdEstadoAutorizacion" = 'PEN'
              AND "CampoModificado" = 'IdEstado'
        )
        THEN CURRENT_UTCTIMESTAMP
        ELSE TGT."FechaModificacion"
    END
WHERE TGT."IdDocumentoIdentidad" = :IdDocumentoIdentidad;
