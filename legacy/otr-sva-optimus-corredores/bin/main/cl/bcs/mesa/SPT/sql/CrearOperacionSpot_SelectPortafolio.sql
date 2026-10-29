SELECT 
	cli."NombreCompleto",
	port."IdPortafolio",
	port."CodigoPortafolio",
	cli."IdCliente",
	cli."IdEstado" AS "IdEstadoCliente",
	port."IdEstado" AS "IdEstadoPortafolio",
	cli."DocumentoIdentidad",
	cli."IdEmpleadoEjecutivo",
	cli."TipoDocumentoIdentidad",
	port."Proposito",
	cli."IdPersona"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on port."IdCliente" = cli."IdCliente"
WHERE port."IdCliente" IN (:IdCliente) AND port."IdPortafolio" IN (:IdPortafolio)