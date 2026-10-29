UPDATE "BSCL.SocioNegocio.BD::CLI.Cliente"
SET
    "IdEstado" = 'BLO',
    "IdTipoBloqueo" = :IdTipoBloqueo,
    "ComentarioBloqueo" = :ComentarioBloqueo,
    "FechaBloqueo" = CURRENT_UTCTIMESTAMP,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdCliente" = :IdCliente;
