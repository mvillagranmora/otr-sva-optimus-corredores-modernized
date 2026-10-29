SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClientePivot";
GO
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClientePivot"  AS
SELECT	"IdCliente",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CS' THEN "ValorIndicador" END),'S') AS "ContratoSimultanea",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CCGF' THEN "ValorIndicador" END),'S') AS "ContratoFuturos",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CVC' THEN "ValorIndicador" END),'S') AS "ContratoVentaCorta",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CVCRF' THEN "ValorIndicador" END),'S') AS "ContratoVentaCortaRF",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CC' THEN "ValorIndicador" END),'S') AS "ContratoCustodia",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CM' THEN "ValorIndicador" END),'S') AS "ContratoMercantil"
FROM (

	SELECT "IdCliente","CodigoTipoIndicador","IdIndicador","ValorIndicador"
	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"
	WHERE "IdTipoEntidad" = 'CLIENTE' and "IdUso" = 'REQ'
) 
GROUP BY "IdCliente";
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClientePivot" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
