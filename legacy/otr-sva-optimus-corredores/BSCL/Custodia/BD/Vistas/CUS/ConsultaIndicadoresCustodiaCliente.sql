SET SCHEMA "BSCL";
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaIndicadoresCustodiaCliente";
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaIndicadoresCustodiaCliente" 
AS

SELECT	"IdCliente",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CS' THEN "ValorIndicador" END),'S') AS "ContratoSimultanea",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CCGF' THEN "ValorIndicador" END),'S') AS "ContratoFuturos",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CVC' THEN "ValorIndicador" END),'S') AS "ContratoVentaCorta",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CC' THEN "ValorIndicador" END),'S') AS "ContratoCustodia",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CM' THEN "ValorIndicador" END),'S') AS "ContratoMercantil",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CE' THEN "ValorIndicador" END),'S') AS "ContratoEmprestitos",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CCGP' THEN "ValorIndicador" END),'S') AS "ContratoPactos",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CCF' THEN "ValorIndicador" END),'S') AS "ContratoForward"
		
FROM (

	SELECT IND."IdCliente",IND."CodigoTipoIndicador",IND."IdTipoIndicador",IND."IdIndicador",IND."ValorIndicador",DET."DetalleIndicador",DET."Codigo"
	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" DET 
		ON IND."IdTipoIndicador" = DET."IdTipoIndicador"
		AND IND."ValorIndicador" = CAST(DET."IdDetalleIndicador" AS NVARCHAR)
	WHERE "IdTipoEntidad" = 'CLIENTE'
)
GROUP BY "IdCliente"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultaIndicadoresCustodiaCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
