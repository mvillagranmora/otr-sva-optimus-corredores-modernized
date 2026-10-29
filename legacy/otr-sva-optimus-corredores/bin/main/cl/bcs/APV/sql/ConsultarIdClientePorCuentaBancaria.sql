SELECT TOP 1 
	CLI."IdCliente"
FROM "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" AS CB
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" AS CPB
ON CB."IdBanco" = CPB."IdBanco"
INNER JOIN "BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV" AS CGC
ON CPB."IdPersona" = CGC."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
ON CPB."IdPersona" = CLI."IdPersona"
WHERE CB."CodigoBanco" = :p_CodigoBanco
AND CPB."NumeroCuentaBancaria" = :p_NumeroCuentaBancaria
AND CGC."IdEstado" = 'VIG';
