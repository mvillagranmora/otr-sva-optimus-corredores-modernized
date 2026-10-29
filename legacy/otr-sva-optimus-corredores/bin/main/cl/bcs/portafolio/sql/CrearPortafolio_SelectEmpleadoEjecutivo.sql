SELECT "IdEmpleadoEjecutivo","NombreCompleto"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
WHERE "IdCliente" = :p_IdCliente;