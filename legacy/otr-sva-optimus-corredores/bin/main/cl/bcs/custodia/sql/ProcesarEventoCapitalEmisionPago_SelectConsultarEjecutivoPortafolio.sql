SELECT 	EJE."IdPortafolio"											AS "IdPortafolio",
		COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0)				AS "IdEmpleadoEjecutivoPortafolio",						
		COALESCE(EJE."CodigoEmpleado",'')							AS "CodigoUsuarioAgente",
		COALESCE(EJE."NombreCompletoEjecutivo",'')					AS "NombreAgente",
		COALESCE(EJE."CodigoSucursal",'')							AS "CodigoSucursal"
FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE
WHERE EJE."IdPortafolio" IN (:IdPortafolio)