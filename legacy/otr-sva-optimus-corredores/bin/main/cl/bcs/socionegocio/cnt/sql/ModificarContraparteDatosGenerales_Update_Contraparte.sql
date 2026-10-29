-- Actualizar Contraparte aplicando cambios desde SolicitudCambio
-- Migración de ConfirmarModificarContraparteDatosGenerales.sql
-- 9 Campos: IdEstado, CodigoInscripcion, Otros, Depositos, Bolsa, Corredora, CamaraDeCompensacion, Banco, Boveda, Emisor
UPDATE "BSCL.SocioNegocio.BD::CNT.Contraparte" AS TGT
SET 
    "IdEstado" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdSolicitudCambio" = :IdSolicitudCambio
           AND "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CONTRAPARTE'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdEstado'),
        TGT."IdEstado"),
    "CodigoInscripcion" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdSolicitudCambio" = :IdSolicitudCambio
           AND "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CONTRAPARTE'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'CodigoInscripcion'),
        TGT."CodigoInscripcion"),
    "Otros" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdSolicitudCambio" = :IdSolicitudCambio
           AND "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CONTRAPARTE'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'Otros'),
        TGT."Otros"),
    "Depositos" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdSolicitudCambio" = :IdSolicitudCambio
           AND "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CONTRAPARTE'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'Depositos'),
        TGT."Depositos"),
    "Bolsa" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdSolicitudCambio" = :IdSolicitudCambio
           AND "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CONTRAPARTE'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'Bolsa'),
        TGT."Bolsa"),
    "Corredora" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdSolicitudCambio" = :IdSolicitudCambio
           AND "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CONTRAPARTE'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'Corredora'),
        TGT."Corredora"),
    "CamaraDeCompensacion" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdSolicitudCambio" = :IdSolicitudCambio
           AND "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CONTRAPARTE'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'CamaraDeCompensacion'),
        TGT."CamaraDeCompensacion"),
    "Banco" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdSolicitudCambio" = :IdSolicitudCambio
           AND "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CONTRAPARTE'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'Banco'),
        TGT."Banco"),
    "Boveda" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdSolicitudCambio" = :IdSolicitudCambio
           AND "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CONTRAPARTE'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'Boveda'),
        TGT."Boveda"),
    "Emisor" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdSolicitudCambio" = :IdSolicitudCambio
           AND "IdEntidadOrigen" = CAST(:IdContraparte AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CONTRAPARTE'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'Emisor'),
        TGT."Emisor"),
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE TGT."IdContraparte" = :IdContraparte
