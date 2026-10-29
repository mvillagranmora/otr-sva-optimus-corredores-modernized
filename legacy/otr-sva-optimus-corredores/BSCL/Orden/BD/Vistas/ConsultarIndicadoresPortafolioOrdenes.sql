SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarIndicadoresPortafolioOrdenes";
GO
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarIndicadoresPortafolioOrdenes"
AS 
SELECT	"IdPortafolio",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDMAILNTF' THEN "ValorIndicador" END),'N') AS "IdDireccionNotificacion",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDOCUSTRV' THEN "ValorIndicador" END),'N') AS "OperaCustodiaRV",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDOCUSTRF' THEN "ValorIndicador" END),'N') AS "OperaCustodiaRFIF",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDVNNOMRV' THEN "ValorIndicador" END),'N') AS "PermiteVenderNominalRV",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDVNNOMRF' THEN "ValorIndicador" END),'N') AS "PermiteVenderNominalRFIF",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_OCCINV' THEN "ValorIndicador" END),'N') AS "OperaCuentaInversion",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_SOBCTA' THEN "ValorIndicador" END),'N') AS "OperaLineaSobregiro",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDLIBRO' THEN "ValorIndicador" END),0) AS "IdLibroOrdenes",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDMODALIDAD' THEN "ValorIndicador" END),null) AS "MecanismoCaptacion",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_CODIFRS' THEN "ValorIndicador" END),null) AS "CodigoIfrs",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'FAC_MODFAC' THEN "CodigoValorIndicador" END),'MODPREFNORM') AS "ModificaTipoLiquidacion"
FROM (

	SELECT "IdPortafolio","CodigoTipoIndicador","IdIndicador","ValorIndicador","CodigoValorIndicador"
	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
) 
GROUP BY "IdPortafolio";
GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarIndicadoresPortafolioOrdenes" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO