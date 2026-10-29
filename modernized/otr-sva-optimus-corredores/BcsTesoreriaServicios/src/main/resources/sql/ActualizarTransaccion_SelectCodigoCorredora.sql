SELECT
	Corredora."NombreCorto",
	Corredora."IdCorredora",
	CodigoCorredora."CodigoCorredora",
	CodigoCorredora."IdBolsa"
FROM
	"BSCL.Plataforma.BD::PLA.Empresa" AS Empresa
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SocioNegocio ON
	Empresa."IdSocioDeNegocio" = SocioNegocio."IdSocioDeNegocio"
	AND SocioNegocio."IdEmpresa" = Empresa."IdEmpresa"
INNER JOIN "BSCL.Plataforma.BD::INS.Corredora" AS Corredora ON
	Corredora."IdPersona" = SocioNegocio."IdPersona"
INNER JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" AS CodigoCorredora ON
	Corredora."IdCorredora" = CodigoCorredora."IdCorredora"
WHERE SocioNegocio."Borrado" = 'N' AND CodigoCorredora."IdBolsa" = 'BCS';