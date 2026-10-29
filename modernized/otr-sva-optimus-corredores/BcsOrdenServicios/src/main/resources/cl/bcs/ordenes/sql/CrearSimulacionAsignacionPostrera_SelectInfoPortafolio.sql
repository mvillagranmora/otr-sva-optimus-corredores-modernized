SELECT	
	 por."IdPortafolio"
	,p_por."Cantidad"
	,p_por."Porcentaje"
	,por."CodigoPortafolio"
	,cli."IdCliente"
	,cli."NombreCompleto"
	,cli."DocumentoIdentidad"
										
FROM "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Portafolios" as p_por
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio"  as por ON (por."IdPortafolio" = p_por."IdPortafolio")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as cli ON (por."IdCliente" = cli."IdCliente");