  SELECT
	 MES."IdMesa"
    ,MES."IdEmpresa" 			
	,MES."IdCliente" 			
	,MES."IdPortafolio" 			
	,coalesce(CLI."DocumentoIdentidad",'') as "DocumentoIdentidad"
	,MES."Codigo"				
	,MES."Nombre" 				
	,MES."IdTipoProducto" 		
	,MES."IdEstadoMesa"			
	,MES."IdEstadoRegistro"		
	,MES."ValorRazonable"
	,IFNULL(POR."CodigoPropositoPortafolio",' - ') AS "CodigoPortafolio"
    FROM "BSCL.Mesa.BD::MES.Mesa" MES
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON (CLI."IdCliente" = MES."IdCliente")
	LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON (POR."IdPortafolio" = MES."IdPortafolio")
	WHERE MES."IdTipoProducto" =:IdTipoProducto ;