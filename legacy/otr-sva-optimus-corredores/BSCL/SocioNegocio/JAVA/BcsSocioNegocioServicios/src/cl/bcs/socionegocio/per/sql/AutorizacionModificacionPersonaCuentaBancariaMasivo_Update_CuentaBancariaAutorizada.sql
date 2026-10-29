-- Actualizar CuentaBancaria aplicando cambios desde SolicitudCambio
-- Migración de ConfirmarModificarPersonaCuentaBancariaMasivo.sql

UPDATE "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS TGT
SET
    "IdBanco" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdBanco'),
        TGT."IdBanco"),

    "IdTipoCuentaBancaria" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdTipoCuentaBancaria'),
        TGT."IdTipoCuentaBancaria"),

    "IdMoneda" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdMoneda'),
        TGT."IdMoneda"),

    "NumeroCuentaBancaria" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'NumeroCuentaBancaria'),
        TGT."NumeroCuentaBancaria"),

    "IdEstado" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdEstado'),
        TGT."IdEstado"),

    "IdPlaza" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdPlaza'),
        TGT."IdPlaza"),

    "IdTipoIdentificadorCuenta" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdTipoIdentificadorCuenta'),
        TGT."IdTipoIdentificadorCuenta"),

    "IdentificadorCuenta" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'IdentificadorCuenta'),
        TGT."IdentificadorCuenta"),

    "CodigoCuentaInterbancario" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'CodigoCuentaInterbancario'),
        TGT."CodigoCuentaInterbancario"),

    "CodigoAba" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'CodigoAba'),
        TGT."CodigoAba"),

    "OtroCodigoBancoExtranjero" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'OtroCodigoBancoExtranjero'),
        TGT."OtroCodigoBancoExtranjero"),

    "CodigoIban" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'CodigoIban'),
        TGT."CodigoIban"),

    "OtroCodigoCuentaExtranjero" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'OtroCodigoCuentaExtranjero'),
        TGT."OtroCodigoCuentaExtranjero"),

    "EsPac" = COALESCE(
        (SELECT MAX("ValorNuevo")
         FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
         WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
           AND "IdTipoEntidad" = 'CUENTABANCARIA'
           AND "IdEstadoAutorizacion" = 'PEN'
           AND "CampoModificado" = 'EsPac'),
        TGT."EsPac"),

    "Predeterminado" = COALESCE(
        (SELECT MAX("ValorNuevo")
        FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
        WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
        AND "IdTipoEntidad" = 'CUENTABANCARIA'
        AND "IdEstadoAutorizacion" = 'PEN'
        AND "CampoModificado" = 'Predeterminado'),
        TGT."Predeterminado"),
    
    "IdPersonaRelacionada" = COALESCE(
        (SELECT CAST(MAX("ValorNuevo") AS BIGINT)
        FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
        WHERE "IdEntidadOrigen" = CAST(:IdCuentaBancaria AS NVARCHAR(40))
        AND "IdTipoEntidad" = 'CUENTABANCARIA'
        AND "IdEstadoAutorizacion" = 'PEN'
        AND "CampoModificado" = 'IdPersonaRelacionada'),
        TGT."IdPersonaRelacionada"),

    "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP

WHERE TGT."IdCuentaBancaria" = :IdCuentaBancaria;