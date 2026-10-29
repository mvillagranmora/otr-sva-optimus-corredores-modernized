SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarRelacionCorredora";
GO

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarRelacionCorredora" AS
SELECT
	CLI."IdPersona" AS "IdPersonaDestino"
FROM
	"BSCL.SocioNegocio.BD::CLI.Cliente" CLI
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON
	IND."IdEntidad" = CLI."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO ON
	IND."IdTipoIndicador" = TPO."IdTipoIndicador"
WHERE
	TPO."IdTipoEntidad" = 'CLIENTE'
	AND TPO."CodigoTipoIndicador" = 'RELCORR'
	AND IND."ValorIndicador" = 'S'


WITH READ ONLY;	 	
GO

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarRelacionCorredora" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO