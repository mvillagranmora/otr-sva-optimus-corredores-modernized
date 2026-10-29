SELECT FECHA_ULTIMA_MODIFICACION."IdCliente"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarFechaUltimaModificacionClientePAUBloqueoClientes" AS FECHA_ULTIMA_MODIFICACION
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE ON CLIENTE."IdCliente" = FECHA_ULTIMA_MODIFICACION."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
WHERE FECHA_ULTIMA_MODIFICACION."FechaUltimaModificacion" <= :FechaLimiteActuliazacionDatos AND CLIENTE."IdEstado" != 'BLO'
AND PORTAFOLIO."AreaCliente" = :AreaClienteParametro
GROUP BY(FECHA_ULTIMA_MODIFICACION."IdCliente")