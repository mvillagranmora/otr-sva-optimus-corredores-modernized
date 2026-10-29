SELECT 
	 POR."IdPortafolio"
	,CLI."IdCliente"
	,IND."NombreValorIndicador" AS "MonedaPeferenciaPago"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = POR."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = POR."IdPortafolio" AND IND."CodigoTipoIndicador" = 'DIV_DOM'
WHERE POR."IdPortafolio" IN (:IdsPortafolios)