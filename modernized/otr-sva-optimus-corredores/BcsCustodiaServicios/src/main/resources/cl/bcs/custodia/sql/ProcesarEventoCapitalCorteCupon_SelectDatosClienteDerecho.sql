SELECT 
	CLI."IdCliente"
	,CLI."DocumentoIdentidad"
	,CLI."NombreCompleto" AS "NombreCliente"
	,SOC."IdSocioDeNegocio" AS "IdSocioNegocio"
	,EJE."IdEmpleadoEjecutivoPortafolio" AS "IdEmpleadoEjecutivoPortafolio"
	,EJE."CodigoEmpleado" AS "CodigoUsuarioAgente"
	,EJE."CodigoSucursal" AS "CodigoSucursal"
	,EJE."NombreCompletoEjecutivo" AS "NombreAgente"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SOC ON CLI."IdCliente" = SOC."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio" AS EJE ON EJE."IdPortafolio" = :IdPortafolio
WHERE CLI."IdCliente" = :IdCliente