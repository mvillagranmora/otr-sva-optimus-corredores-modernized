SET SCHEMA "BSCL";
GO
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarIndicadoresClienteOrdenes";
GO
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarIndicadoresClienteOrdenes"
AS 
SELECT	"IdEmpresa","IdCliente",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CS' THEN "ValorIndicador" END),'S') AS "ContratoSimultanea",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CCG' THEN "ValorIndicador" END),'S') AS "ContratoCondicionesGenerales",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CCGF' THEN "ValorIndicador" END),'S') AS "ContratoFuturos",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CVC' THEN "ValorIndicador" END),'S') AS "ContratoVentaCorta",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CVCRF' THEN "ValorIndicador" END),'S') AS "ContratoVentaCortaRF",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CC' THEN "ValorIndicador" END),'S') AS "ContratoCustodia",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CM' THEN "ValorIndicador" END),'S') AS "ContratoMercantil",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CLASEORDEN' THEN "Codigo" END),'') AS "ClaseOrden",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'FOTOCOPIA' THEN "ValorIndicador" END),'') AS "FotocopiaCarnet",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ANTLEGAL' THEN "ValorIndicador" END),'') AS "AntecedentesLegales"
FROM (

	SELECT IND."IdEmpresa",IND."IdCliente",IND."CodigoTipoIndicador",IND."IdTipoIndicador",IND."IdIndicador",IND."ValorIndicador",DET."DetalleIndicador",DET."Codigo"
	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" DET 
		ON IND."IdTipoIndicador" = DET."IdTipoIndicador"
		AND IND."ValorIndicador" = CAST(DET."IdDetalleIndicador" AS NVARCHAR)
	WHERE "IdTipoEntidad" = 'CLIENTE'
)

GROUP BY "IdCliente","IdEmpresa";
GO

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarIndicadoresClienteOrdenes" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
