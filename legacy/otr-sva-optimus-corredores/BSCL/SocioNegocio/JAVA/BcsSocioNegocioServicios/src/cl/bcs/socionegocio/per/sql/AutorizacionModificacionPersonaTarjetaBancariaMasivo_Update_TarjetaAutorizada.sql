-- Actualizar TarjetaBancaria aplicando cambios desde SolicitudCambio
-- Migración de ConfirmarModificarPersonaTarjetaBancariaMasivo.sql
-- 6 Campos: IdBanco, IdTipoTarjetaBancaria, NumeroTarjetaBancaria, FechaVencimiento, CodigoVerificacion, IdEstado
UPDATE "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" AS TGT
SET 
    "IdBanco" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdTarjetaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAETJB' THEN 'TARJETABANCARIA-ESTADO'
                ELSE 'TARJETABANCARIA'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdBanco'),
        TGT."IdBanco"),
    "IdTipoTarjetaBancaria" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdTarjetaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAETJB' THEN 'TARJETABANCARIA-ESTADO'
                ELSE 'TARJETABANCARIA'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdTipoTarjetaBancaria'),
        TGT."IdTipoTarjetaBancaria"),
    "NumeroTarjetaBancaria" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdTarjetaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAETJB' THEN 'TARJETABANCARIA-ESTADO'
                ELSE 'TARJETABANCARIA'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'NumeroTarjetaBancaria'),
        TGT."NumeroTarjetaBancaria"),
    "FechaVencimiento" = COALESCE(
        (SELECT TO_DATE(MAX("ValorNuevo")) FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdTarjetaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAETJB' THEN 'TARJETABANCARIA-ESTADO'
                ELSE 'TARJETABANCARIA'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'FechaVencimiento'),
        TGT."FechaVencimiento"),
    "CodigoVerificacion" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdTarjetaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAETJB' THEN 'TARJETABANCARIA-ESTADO'
                ELSE 'TARJETABANCARIA'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'CodigoVerificacion'),
        TGT."CodigoVerificacion"),
    "IdEstado" = COALESCE(
        (SELECT MAX("ValorNuevo") FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" 
         WHERE "IdEntidadOrigen" = CAST(:IdTarjetaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = CASE 
                WHEN :IdConcepto = 'CAETJB' THEN 'TARJETABANCARIA-ESTADO'
                ELSE 'TARJETABANCARIA'
           END
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdEstado'),
        TGT."IdEstado"),
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE TGT."IdTarjetaBancaria" = :IdTarjetaBancaria
