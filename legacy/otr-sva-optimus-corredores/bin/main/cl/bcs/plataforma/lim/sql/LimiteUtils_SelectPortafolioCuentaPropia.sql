SELECT
	Cliente."IdPersona"         AS "IdPersona"
	, Cliente."IdCliente"       AS "IdCliente"
	, Portafolio."IdPortafolio" AS "IdPortafolio"
FROM
	"BSCL.Portafolio.BD::POR.Portafolio" Portafolio
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" Cliente ON (Cliente."IdCliente" = Portafolio."IdCliente")
	INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" Socio ON (Socio."IdPersona" = Cliente."IdPersona")
	INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" Empresa ON (Empresa."IdSocioDeNegocio" = Socio."IdSocioDeNegocio")
WHERE
		Socio."Borrado" = 'N'
	AND Cliente."Borrado" = 'N'
	AND Empresa."IdEmpresa" = :IdEmpresa;
