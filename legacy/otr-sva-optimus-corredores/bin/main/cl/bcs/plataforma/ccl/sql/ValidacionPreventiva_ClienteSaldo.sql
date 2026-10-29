SELECT S."IdCliente" AS "Valor5"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarSaldoFechaEfectivaAnalitica" S
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON S."IdCliente" = CLI."IdCliente"
WHERE CLI."IdCliente" IS NULL;