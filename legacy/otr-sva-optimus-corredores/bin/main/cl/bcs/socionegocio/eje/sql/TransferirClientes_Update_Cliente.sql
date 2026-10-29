UPDATE CLI
SET "IdPersonaEjecutivo" = :IdPersonaEjecutivoNuevo
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" C ON C."IdCliente" = CLI."IdCliente"
WHERE C."IdEmpleadoEjecutivo" = :IdEmpleadoEjecutivoAntiguo;
