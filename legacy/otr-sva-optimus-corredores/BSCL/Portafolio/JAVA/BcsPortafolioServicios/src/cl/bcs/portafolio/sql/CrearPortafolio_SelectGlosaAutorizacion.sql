SELECT "Tipo", "DocumentoIdentidad", "RazonSocial", "Nombres", "ApellidoPaterno", "ApellidoMaterno"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 
WHERE "IdCliente" = :IdCliente;