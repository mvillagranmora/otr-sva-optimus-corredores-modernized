SELECT
	dir."IdDireccionFisica",
	mail."IdDireccionElectronica"
FROM
	"BSCL.SocioNegocio.BD::CLI.Cliente" cli
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" dir ON dir."IdPersona" = cli."IdPersona"
	AND dir."Predeterminado" = 'S'
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" mail ON mail."IdPersona" = cli."IdPersona"
	AND dir."Predeterminado" = 'S' 
WHERE
	"IdCliente" = :IdCliente;