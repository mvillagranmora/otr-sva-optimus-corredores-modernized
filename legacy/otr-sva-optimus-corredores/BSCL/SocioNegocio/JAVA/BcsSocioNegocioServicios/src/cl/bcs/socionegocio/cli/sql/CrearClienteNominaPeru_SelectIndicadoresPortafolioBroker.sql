SELECT 
	TPO."CodigoTipoIndicador",
	"BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL AS "IdIndicador",
	Cliente."IdPortafolioDefecto" AS "IdEntidad",
	ATR."IdTipoEntidad",
	ATR."IdTipoIndicador",
	ATR."ModeloCliente",
	ATR."IdEmpresa",
	CASE TPO."CodigoTipoIndicador"
		WHEN 'CAR_DIRELE' THEN TO_NVARCHAR(Cliente."IdDireccionElectronicaPrincipal")
		WHEN 'CAR_DIRFIS' THEN TO_NVARCHAR(Cliente."IdDireccionFisicaPrincipal")
		WHEN 'FAC_DIRELE' THEN TO_NVARCHAR(Cliente."IdDireccionElectronicaPrincipal")
		WHEN 'FAC_DIRFIS' THEN TO_NVARCHAR(Cliente."IdDireccionFisicaPrincipal")
		ELSE ATR."ValorIndicador" 
	END AS "ValorIndicador",
	ATR."IdEstadoAutorizacion"
FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO
INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON TPO."IdTipoIndicador" = ATR."IdTipoIndicador" AND TPO."IdTipoEntidad" = ATR."IdTipoEntidad" AND TPO."IdEmpresa" = ATR."IdEmpresa"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ClienteBroker ON ClienteBroker."IdPersona" = :IdPersonaBroker
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente ON Cliente."IdCliente" = :IdCliente
WHERE ATR."IdTipoEntidad" = 'PORTAFOLIO' AND ATR."IdEntidad" = ClienteBroker."IdPortafolioDefecto";