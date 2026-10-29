SELECT
	 :NumeroOperacion AS "NumeroOperacion"
	,COALESCE(IND."ValorIndicador",'') AS "ValorIndicador"
	,COALESCE(IND."IdCliente",0) AS "IdCliente"
	,COALESCE(IND."IdPortafolio",0) AS "IdPortafolio"
	,COALESCE(PER."NombreCompleto",'') AS "NombreCompleto"
FROM DUMMY
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND
	ON (IND."ValorIndicador" IN (:ValorIndicador) AND
		IND."CodigoTipoIndicador" = :CodigoTipoIndicador AND
		IND."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
	ON (IND."IdPersona" = PER."IdPersona");
