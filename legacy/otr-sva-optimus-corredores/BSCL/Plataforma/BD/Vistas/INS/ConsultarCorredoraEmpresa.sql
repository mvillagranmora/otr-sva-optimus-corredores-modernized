SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" AS 
	SELECT 
			EMP."IdEmpresa",
			EMP."Codigo",
			EMP."Nombre",
			EMP."IdPais",
			EMP."IdSocioDeNegocio", 
			CORR."IdCorredora",
			CORRCOD."CodigoCorredora",
			CORRCOD."CodigoCorredoraFix",
			CORRCOD."IdBolsa",
			CORR."IdPersona"
	FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SOC on emp."IdSocioDeNegocio" = SOC."IdSocioDeNegocio" and SOC."IdEmpresa" = EMP."IdEmpresa"
	INNER JOIN "BSCL.Plataforma.BD::INS.Corredora" AS CORR on CORR."IdPersona" = soc."IdPersona"
	INNER JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" AS CORRCOD on CORR."IdCorredora" = CORRCOD."IdCorredora";
                      
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
