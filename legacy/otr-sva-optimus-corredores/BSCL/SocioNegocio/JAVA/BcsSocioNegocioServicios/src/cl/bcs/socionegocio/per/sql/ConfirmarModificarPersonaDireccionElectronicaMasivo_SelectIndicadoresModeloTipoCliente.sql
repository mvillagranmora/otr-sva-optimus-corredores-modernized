SELECT I."IdIndicador",
I."IdTipoIndicador",
I."IdEntidad",
I."ValorIndicador"
FROM "BSCL.Plataforma.BD::IND.Indicador" I
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = I."IdEntidad"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = POR."IdCliente"
WHERE I."IdTipoEntidad" = 'PORTAFOLIO'
	AND POR."IdEstado" = 'ACT'
	AND CLI."IdCliente" IN (:IdCliente)
	AND I."IdTipoIndicador" IN (:IdTipoIndicador);