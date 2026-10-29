-- Propaga cambios de estado del Cliente a Portafolio
UPDATE POR
SET
    POR."IdEstado" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCliente AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CLIENTE-ESTADO'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdEstado'),
        POR."IdEstado"),
    POR."IdTipoBloqueo" = CASE
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
        ELSE POR."IdTipoBloqueo"
    END,
    POR."IdTipoDesbloqueo" = CASE
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
        ELSE POR."IdTipoDesbloqueo"
    END,
    POR."FechaBloqueo" = CASE
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
        ELSE POR."FechaBloqueo"
    END,
    POR."FechaDesbloqueo" = CASE
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
        ELSE POR."FechaDesbloqueo"
    END,
    POR."ComentarioBloqueo" = CASE
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
        ELSE POR."ComentarioBloqueo"
    END,
    POR."ComentarioDesbloqueo" = CASE
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
        ELSE POR."ComentarioDesbloqueo"
    END,
    POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
WHERE POR."IdCliente" = :IdCliente;
