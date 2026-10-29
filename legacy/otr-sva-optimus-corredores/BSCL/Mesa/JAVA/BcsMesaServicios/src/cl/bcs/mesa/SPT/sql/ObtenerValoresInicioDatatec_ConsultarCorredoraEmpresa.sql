SELECT 
	 EMP."IdEmpresa"
	,EMP."IdSocioDeNegocio"
	,SOC."IdPersona"
	,IND."ValorIndicador" AS "CodigoDatatec"
	,EMP."Nombre" AS "NombreCompleto"
	FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC
	ON (EMP."IdEmpresa" = :IdEmpresa AND
		EMP."IdEmpresa" = SOC."IdEmpresa" AND
		EMP."IdSocioDeNegocio" = SOC."IdSocioDeNegocio" AND
		SOC."Borrado" = 'N')
INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIP
	ON (TIP."IdTipoEntidad" = 'CLIENTE' AND
		TIP."GrupoIndicador" = 'GENERALES' AND
		TIP."CodigoTipoIndicador" = 'CODIGO_DATATEC' AND
		TIP."IdEmpresa" = EMP."IdEmpresa")
INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" IND
	ON (TIP."IdTipoIndicador" = IND."IdTipoIndicador" AND
		IND."IdEntidad" = SOC."IdCliente");