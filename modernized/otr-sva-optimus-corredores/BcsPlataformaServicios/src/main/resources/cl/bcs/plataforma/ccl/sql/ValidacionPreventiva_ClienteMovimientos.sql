SELECT CIN."IdCliente" AS "Valor5" 
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarMovimientosCuentaInversion" CIN
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CIN."IdCliente" = CLI."IdCliente"
WHERE CLI."IdCliente" IS NULL;
