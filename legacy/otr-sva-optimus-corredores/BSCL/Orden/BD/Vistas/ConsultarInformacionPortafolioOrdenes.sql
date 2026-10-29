SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarInformacionPortafolioOrdenes";
GO
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarInformacionPortafolioOrdenes" 
AS
WITH 
   IndicadorContratoCustodia AS (
		SELECT  ATR_CC."IdEntidad" AS "IdCliente",
				ATR_CC."ValorIndicador" AS "ContratoCustodia"
		FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC
		INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC 
				ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" 
				AND ATR_CC."IdTipoEntidad" = 'CLIENTE'
		WHERE TPO_CC."CodigoTipoIndicador" = 'CC'
)
SELECT	PORT."IdPortafolio",
		"IdDireccionNotificacion",
		"OperaCustodiaRV",
		"OperaCustodiaRFIF",
		"PermiteVenderNominalRV",
		"PermiteVenderNominalRFIF",
		"OperaCuentaInversion",
		"OperaLineaSobregiro",
		"MecanismoCaptacion",
		"IdLibroOrdenes",
		CLI."EsClienteFinal",
		BRO."IdPersona" AS "IdPersonaBrokerExtranjero",
		"CodigoIfrs",
		IND."ModificaTipoLiquidacion", 
		INDCUS."ContratoCustodia"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" PORT
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = PORT."IdCliente"
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarIndicadoresPortafolioOrdenes" IND ON PORT."IdPortafolio" = IND."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::PLA.BrokerExtranjero" BRO ON BRO."IdPersona" = CLI."IdPersona"
LEFT JOIN IndicadorContratoCustodia INDCUS ON INDCUS."IdCliente" = CLI."IdCliente"
;
GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarInformacionPortafolioOrdenes" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO