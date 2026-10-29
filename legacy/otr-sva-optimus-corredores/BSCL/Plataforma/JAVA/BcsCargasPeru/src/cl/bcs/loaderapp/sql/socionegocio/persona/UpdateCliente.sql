UPDATE C
SET
 C."IdSucursalPreferencia" = :IdSucursal,
 C."IdEstado" = :IdEstadoCliente,
 C."FechaBloqueo" = TO_DATE(:FechaBloqueo, 'DD-MM-YYYY'),
 C."IdTipoBloqueo" = :IdTipoBloqueo,
 C."ComentarioBloqueo" = :ComentarioBloqueo
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" C
WHERE C."IdCliente" = :IdCliente