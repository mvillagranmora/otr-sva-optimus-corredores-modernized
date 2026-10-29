-- Actualiza Cliente desde SolicitudCambio (CLIENTE-ESTADO)
UPDATE "BSCL.SocioNegocio.BD::CLI.Cliente" AS TGT
SET
    "IdEstado" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdEstado'),
        TGT."IdEstado"),
    "IdTipoBloqueo" = CASE
        WHEN EXISTS (
            SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
            WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
              AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
              AND "IdEstadoAutorizacion" = 'PEN'
              AND "CampoModificado" = 'IdTipoBloqueo'
        )
        THEN COALESCE((SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
                       WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
                         AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
                         AND "IdEstadoAutorizacion" = 'PEN'
                         AND "CampoModificado" = 'IdTipoBloqueo'), '')
        WHEN EXISTS (
            SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
            WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
              AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
              AND "IdEstadoAutorizacion" = 'PEN'
              AND "CampoModificado" = 'IdTipoDesbloqueo'
        )
        THEN ''
        ELSE TGT."IdTipoBloqueo"
    END,
    "IdTipoDesbloqueo" = CASE
        WHEN EXISTS (
            SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
            WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
              AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
              AND "IdEstadoAutorizacion" = 'PEN'
              AND "CampoModificado" = 'IdTipoDesbloqueo'
        )
        THEN COALESCE((SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
                       WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
                         AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
                         AND "IdEstadoAutorizacion" = 'PEN'
                         AND "CampoModificado" = 'IdTipoDesbloqueo'), '')
        WHEN EXISTS (
            SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
            WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
              AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
              AND "IdEstadoAutorizacion" = 'PEN'
              AND "CampoModificado" = 'IdTipoBloqueo'
        )
        THEN ''
        ELSE TGT."IdTipoDesbloqueo"
    END,
    "FechaBloqueo" = CASE
        WHEN EXISTS (
            SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
            WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
              AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
              AND "IdEstadoAutorizacion" = 'PEN'
              AND "CampoModificado" = 'FechaBloqueo'
        )
        THEN CURRENT_UTCTIMESTAMP
        WHEN EXISTS (
            SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
            WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
              AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
              AND "IdEstadoAutorizacion" = 'PEN'
              AND "CampoModificado" = 'FechaDesbloqueo'
        )
        THEN TO_DATE('0001-01-01', 'YYYY-MM-DD')
        ELSE TGT."FechaBloqueo"
    END,
    "FechaDesbloqueo" = CASE
        WHEN EXISTS (
            SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
            WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
              AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
              AND "IdEstadoAutorizacion" = 'PEN'
              AND "CampoModificado" = 'FechaDesbloqueo'
        )
        THEN CURRENT_UTCTIMESTAMP
        WHEN EXISTS (
            SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
            WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
              AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
              AND "IdEstadoAutorizacion" = 'PEN'
              AND "CampoModificado" = 'FechaBloqueo'
        )
        THEN TO_DATE('0001-01-01', 'YYYY-MM-DD')
        ELSE TGT."FechaDesbloqueo"
    END,
    "ComentarioBloqueo" = CASE
        WHEN EXISTS (
            SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
            WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
              AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
              AND "IdEstadoAutorizacion" = 'PEN'
              AND "CampoModificado" = 'ComentarioBloqueo'
        )
        THEN COALESCE((SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
                       WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
                         AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
                         AND "IdEstadoAutorizacion" = 'PEN'
                         AND "CampoModificado" = 'ComentarioBloqueo'), '')
        WHEN EXISTS (
            SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
            WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
              AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
              AND "IdEstadoAutorizacion" = 'PEN'
              AND "CampoModificado" = 'ComentarioDesbloqueo'
        )
        THEN ''
        ELSE TGT."ComentarioBloqueo"
    END,
    "ComentarioDesbloqueo" = CASE
        WHEN EXISTS (
            SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
            WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
              AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
              AND "IdEstadoAutorizacion" = 'PEN'
              AND "CampoModificado" = 'ComentarioDesbloqueo'
        )
        THEN COALESCE((SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
                       WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
                         AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
                         AND "IdEstadoAutorizacion" = 'PEN'
                         AND "CampoModificado" = 'ComentarioDesbloqueo'), '')
        WHEN EXISTS (
            SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
            WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
              AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
              AND "IdEstadoAutorizacion" = 'PEN'
              AND "CampoModificado" = 'ComentarioBloqueo'
        )
        THEN ''
        ELSE TGT."ComentarioDesbloqueo"
    END,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE TGT."IdCliente" = :IdCliente;
