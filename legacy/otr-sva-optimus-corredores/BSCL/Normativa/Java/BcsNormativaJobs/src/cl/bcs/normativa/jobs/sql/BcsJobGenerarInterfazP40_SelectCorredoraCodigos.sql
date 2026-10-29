
SELECT 
			CORRCOD."CodigoCorredora" as "CodigoCorredora"
	FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SOC on emp."IdSocioDeNegocio" = SOC."IdSocioDeNegocio" and SOC."IdEmpresa" = EMP."IdEmpresa"
	INNER JOIN "BSCL.Plataforma.BD::INS.Corredora" AS CORR on CORR."IdPersona" = soc."IdPersona"
	INNER JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" AS CORRCOD on CORR."IdCorredora" = CORRCOD."IdCorredora"
	WHERE EMP."IdEmpresa" = :IdEmpresa

