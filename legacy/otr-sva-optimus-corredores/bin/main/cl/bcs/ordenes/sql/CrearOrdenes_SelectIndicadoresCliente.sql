SELECT	"IdEmpresa","IdCliente",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CS' THEN "ValorIndicador" END),'S') AS "ContratoSimultanea",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CCG' THEN "ValorIndicador" END),'S') AS "ContratoCondicionesGenerales",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CCGF' THEN "ValorIndicador" END),'S') AS "ContratoFuturos",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CVC' THEN "ValorIndicador" END),'S') AS "ContratoVentaCortaRV",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CC' THEN "ValorIndicador" END),'S') AS "ContratoCustodia",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CM' THEN "ValorIndicador" END),'S') AS "ContratoMercantil",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CLASEORDEN' THEN "Codigo" END),'') AS "ClaseOrden",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'FOTOCOPIA' THEN "ValorIndicador" END),'') AS "FotocopiaCarnet",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ANTLEGAL' THEN "ValorIndicador" END),'') AS "AntecedentesLegales",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'PROCEDENC' THEN "ValorIndicador" END),'') AS "CodigoProcedenciaOrden",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'DOC_NEGOBA' THEN "ValorIndicador" END),'') AS "InstrumentoAcreditado",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'DOC_INSFIN' THEN "ValorIndicador" END),'') AS "ContratoNegociacionMVE",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'DOC_FNEMVE' THEN "ValorIndicador" END),'') AS "ContratoOpcionesExtranjeras",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'DCRSFAT' THEN "ValorIndicador" END),'S') AS "ContratoDeclaracionCRSFatca",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'INVCALIF' THEN "ValorIndicador" END),'N') AS "InversionistaCalificado",
		
		--OPCIONES
	    COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CCGO' THEN "ValorIndicador" END),'S') AS "ContratoOpciones",
	    COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'PORTAFOLIO_DERIVADOS' THEN "ValorIndicador" END),'') AS "PortafolioDerivados",
	    COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CVC' THEN "ValorIndicador" END),'S') AS "ContratoVentaCorta",
	    COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CE' THEN "ValorIndicador" END),'S') AS "ContratoEmprestitos",
	    COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'CCI' THEN "ValorIndicador" END),'S') AS "ContratoInternet"
FROM (

	SELECT IND."IdEmpresa",IND."IdCliente",IND."CodigoTipoIndicador",IND."IdTipoIndicador",IND."IdIndicador",IND."ValorIndicador",DET."DetalleIndicador",DET."Codigo"
	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" DET 
		ON IND."IdTipoIndicador" = DET."IdTipoIndicador"
		AND IND."ValorIndicador" = CAST(DET."IdDetalleIndicador" AS NVARCHAR) AND IND."IdEmpresa" = DET."IdEmpresa"
	WHERE "IdTipoEntidad" = 'CLIENTE' AND "IdCliente" IN (:IdsCliente)
)

GROUP BY "IdCliente","IdEmpresa";
