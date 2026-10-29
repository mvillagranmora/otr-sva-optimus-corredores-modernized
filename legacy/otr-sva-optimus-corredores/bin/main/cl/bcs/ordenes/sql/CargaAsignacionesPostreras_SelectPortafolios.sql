SELECT cli."TipoDocumentoIdentidad",cli."DocumentoIdentidad",port."IdPortafolio",cli."IdCliente",port."CodigoPortafolio" 
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on port."IdCliente" = cli."IdCliente"
WHERE cli."DocumentoIdentidad" in (:documentosIdentidad) and cli."IdEmpresa" = :idEmpresa