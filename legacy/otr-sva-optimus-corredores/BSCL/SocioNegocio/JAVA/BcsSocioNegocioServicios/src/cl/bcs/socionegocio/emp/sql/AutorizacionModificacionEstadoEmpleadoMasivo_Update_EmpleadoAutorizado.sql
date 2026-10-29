-- Actualiza campos de estado en Empleado al autorizar (CAEEMP - cambio estado empleado)
UPDATE EMP
SET EMP."IdEstado" = COALESCE(
        (SELECT "ValorNuevo" 
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" SC
         WHERE SC."IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND SC."IdTipoEntidad" = 'EMPLEADO-ESTADO'
           AND SC."CampoModificado" = 'IdEstado'
           AND SC."IdEstadoAutorizacion" = 'PEN'),
        EMP."IdEstado"),
    EMP."IdTipoBloqueo" = COALESCE(
        (SELECT "ValorNuevo" 
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" SC
         WHERE SC."IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND SC."IdTipoEntidad" = 'EMPLEADO-ESTADO'
           AND SC."CampoModificado" = 'IdTipoBloqueo'
           AND SC."IdEstadoAutorizacion" = 'PEN'),
        EMP."IdTipoBloqueo"),
    EMP."IdTipoDesbloqueo" = COALESCE(
        (SELECT "ValorNuevo" 
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" SC
         WHERE SC."IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND SC."IdTipoEntidad" = 'EMPLEADO-ESTADO'
           AND SC."CampoModificado" = 'IdTipoDesbloqueo'
           AND SC."IdEstadoAutorizacion" = 'PEN'),
        EMP."IdTipoDesbloqueo"),
    EMP."FechaBloqueo" = CASE 
        WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" SC
                     WHERE SC."IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
                       AND SC."IdTipoEntidad" = 'EMPLEADO-ESTADO'
                       AND SC."CampoModificado" = 'FechaBloqueo'
                       AND SC."IdEstadoAutorizacion" = 'PEN')
        THEN CURRENT_UTCTIMESTAMP
        ELSE EMP."FechaBloqueo"
    END,
    EMP."FechaDesbloqueo" = CASE 
        WHEN EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" SC
                     WHERE SC."IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
                       AND SC."IdTipoEntidad" = 'EMPLEADO-ESTADO'
                       AND SC."CampoModificado" = 'FechaDesbloqueo'
                       AND SC."IdEstadoAutorizacion" = 'PEN')
        THEN CURRENT_UTCTIMESTAMP
        ELSE EMP."FechaDesbloqueo"
    END,
    EMP."ComentarioBloqueo" = COALESCE(
        (SELECT "ValorNuevo" 
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" SC
         WHERE SC."IdEntidadOrigen" = CAST(:IdEmpleado AS NVARCHAR(40))
           AND SC."IdTipoEntidad" = 'EMPLEADO-ESTADO'
           AND SC."CampoModificado" = 'ComentarioBloqueo'
           AND SC."IdEstadoAutorizacion" = 'PEN'),
        EMP."ComentarioBloqueo"),
    EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
WHERE EMP."IdEmpleado" = :IdEmpleado;
