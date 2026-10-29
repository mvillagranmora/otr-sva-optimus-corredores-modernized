SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.CI1720.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.CI1720.ObtenerTotales"
(
	--------------------- PARAMETROS COMUNES.
	IN p_IdEmpresa 		BIGINT,
	IN p_IdUsuario		BIGINT,
	IN p_IdModulo		NVARCHAR(3),
	
	--------------------- PARAMETROS DE ENTRADA PROPIOS.
	IN p_IdDocumento 	BIGINT,
	
	-- Filtro del RUT del cliente.
    --IN p_RutCliente 	NVARCHAR(42) DEFAULT NULL,
	
	-- Filtro del nemotecnico.
    IN p_Nemotecnico 		NVARCHAR(20) DEFAULT NULL,
    IN p_NombreEmisor		NVARCHAR(30) DEFAULT NULL,
    IN p_TipoInstrumento	NVARCHAR(10) DEFAULT NULL,
    
    IN p_CantidadUnidadesNominal_Desde	DECIMAL(15,4) DEFAULT NULL,
    IN p_CantidadUnidadesNominal_Hasta	DECIMAL(15,4) DEFAULT NULL,
    
    IN p_TipoUnidades		NVARCHAR(15) DEFAULT NULL,
    
    IN p_Valorizacion_Desde			BIGINT DEFAULT NULL,
    IN p_Valorizacion_Hasta			BIGINT DEFAULT NULL,
    
    IN p_CarteraPropia_Desde		DECIMAL(15,4) DEFAULT NULL,
    IN p_CarteraPropia_Hasta		DECIMAL(15,4) DEFAULT NULL,
	
	IN p_CarteraTerceros_Desde		DECIMAL(15,4) DEFAULT NULL,
	IN p_CarteraTerceros_Hasta		DECIMAL(15,4) DEFAULT NULL,
	
	IN p_CustodiaPropiaDCV_Desde	DECIMAL(15,4) DEFAULT NULL,
	IN p_CustodiaPropiaDCV_Hasta	DECIMAL(15,4) DEFAULT NULL,
	
	IN p_CustodiaTercerosDCV_Desde	DECIMAL(15,4) DEFAULT NULL,
	IN p_CustodiaTercerosDCV_Hasta	DECIMAL(15,4) DEFAULT NULL,
	
	IN p_CustodiaSociedadEmisora_Desde		DECIMAL(15,4) DEFAULT NULL,
	IN p_CustodiaSociedadEmisora_Hasta		DECIMAL(15,4) DEFAULT NULL,
	
	IN p_CustodiaIntermediario_Desde		DECIMAL(15,4) DEFAULT NULL,
	IN p_CustodiaIntermediario_Hasta		DECIMAL(15,4) DEFAULT NULL,
	
	IN p_CustodiaBolsas_Desde				DECIMAL(15,4) DEFAULT NULL,
	IN p_CustodiaBolsas_Hasta				DECIMAL(15,4) DEFAULT NULL,
	
	IN p_CustodiaOtros_Desde				DECIMAL(15,4) DEFAULT NULL,
	IN p_CustodiaOtros_Hasta				DECIMAL(15,4) DEFAULT NULL,
	
	OUT p_out_tbl table (
		"TYPE" 						NVARCHAR (100),
		"COUNT"						BIGINT,
		"CantidadUnidadesNominal"	DECIMAL(19,4),
		"Valorizacion"				BIGINT,
		"CarteraPropia"				DECIMAL(19,4),
		"CustodiaTerceros"			DECIMAL(19,4),
		"CustodiaPropiaDCV"			DECIMAL(19,4),
		"CustodiaTercerosDCV"		DECIMAL(19,4),
		"CustodiaSociedadEmisora"	DECIMAL(19,4),
		"CustodiaIntermediario"		DECIMAL(19,4),
		"CustodiaBolsas"			DECIMAL(19,4),
		"CustodiaOtros"				DECIMAL(19,4)
	)
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL"

AS
BEGIN
	
	p_out_tbl = SELECT
					'TOTAL' AS "TYPE",
					COUNT(*) AS "COUNT",
					IFNULL(SUM("CantidadUnidadesNominal"), 0) AS "CantidadUnidadesNominal",
					IFNULL(SUM("Valorizacion"), 0) AS "Valorizacion",
					IFNULL(SUM("CarteraPropia"), 0) AS "CarteraPropia",
					IFNULL(SUM("CustodiaTerceros"), 0) AS "CustodiaTerceros",
					IFNULL(SUM("CustodiaPropiaDCV"), 0) AS "CustodiaPropiaDCV",
					IFNULL(SUM("CustodiaTercerosDCV"), 0) AS "CustodiaTercerosDCV",
					IFNULL(SUM("CustodiaSociedadEmisora"), 0) AS "CustodiaSociedadEmisora",
					IFNULL(SUM("CustodiaIntermediario"), 0) AS "CustodiaIntermediario",
					IFNULL(SUM("CustodiaBolsas"), 0) AS "CustodiaBolsas",
					IFNULL(SUM("CustodiaOtros"), 0) AS "CustodiaOtros"
					FROM "BSCL.Normativa.BD.Vistas::NOR.CI1720.Datos"
					WHERE "IdDocumento" = p_IdDocumento
				UNION ALL
				SELECT
					'FILTRO' AS "TYPE",
					COUNT(*) AS "COUNT",
					IFNULL(SUM("CantidadUnidadesNominal"), 0) AS "CantidadUnidadesNominal",
					IFNULL(SUM("Valorizacion"), 0) AS "Valorizacion",
					IFNULL(SUM("CarteraPropia"), 0) AS "CarteraPropia",
					IFNULL(SUM("CustodiaTerceros"), 0) AS "CustodiaTerceros",
					IFNULL(SUM("CustodiaPropiaDCV"), 0) AS "CustodiaPropiaDCV",
					IFNULL(SUM("CustodiaTercerosDCV"), 0) AS "CustodiaTercerosDCV",
					IFNULL(SUM("CustodiaSociedadEmisora"), 0) AS "CustodiaSociedadEmisora",
					IFNULL(SUM("CustodiaIntermediario"), 0) AS "CustodiaIntermediario",
					IFNULL(SUM("CustodiaBolsas"), 0) AS "CustodiaBolsas",
					IFNULL(SUM("CustodiaOtros"), 0) AS "CustodiaOtros"
					FROM "BSCL.Normativa.BD.Vistas::NOR.CI1720.Datos"
					WHERE "IdDocumento" = p_IdDocumento
					--AND ("RutCliente" LIKE p_RutCliente OR p_RutCliente IS NULL)
					AND ("Nemotecnico" LIKE p_Nemotecnico OR p_Nemotecnico IS NULL)
					AND ("NombreEmisor" LIKE p_NombreEmisor OR p_NombreEmisor IS NULL)
					AND ("TipoInstrumento" LIKE p_TipoInstrumento OR p_TipoInstrumento IS NULL)
					AND ("CantidadUnidadesNominal" >= p_Valorizacion_Desde OR p_Valorizacion_Desde IS NULL)
					AND ("CantidadUnidadesNominal" <= p_Valorizacion_Hasta OR p_Valorizacion_Hasta IS NULL)
					AND ("TipoUnidades" LIKE p_TipoUnidades OR p_TipoUnidades IS NULL)
					AND ("Valorizacion" >= p_Valorizacion_Desde OR p_Valorizacion_Desde IS NULL)
					AND ("Valorizacion" <= p_Valorizacion_Hasta OR p_Valorizacion_Hasta IS NULL)
					AND ("CarteraPropia" >= p_CarteraPropia_Desde OR p_CarteraPropia_Desde IS NULL)
					AND ("CarteraPropia" <= p_CarteraPropia_Hasta OR p_CarteraPropia_Hasta IS NULL)
					AND ("CustodiaPropiaDCV" >= p_CustodiaPropiaDCV_Desde OR p_CustodiaPropiaDCV_Desde IS NULL)
					AND ("CustodiaPropiaDCV" <= p_CustodiaPropiaDCV_Hasta OR p_CustodiaPropiaDCV_Hasta IS NULL)
					AND ("CustodiaTercerosDCV" >= p_CustodiaTercerosDCV_Desde OR p_CustodiaTercerosDCV_Desde IS NULL)
					AND ("CustodiaTercerosDCV" <= p_CustodiaTercerosDCV_Hasta OR p_CustodiaTercerosDCV_Hasta IS NULL)
					AND ("CustodiaSociedadEmisora" >= p_CustodiaSociedadEmisora_Desde OR p_CustodiaSociedadEmisora_Desde IS NULL)
					AND ("CustodiaSociedadEmisora" <= p_CustodiaSociedadEmisora_Hasta OR p_CustodiaSociedadEmisora_Hasta IS NULL)
					AND ("CustodiaIntermediario" >= p_CustodiaIntermediario_Desde OR p_CustodiaIntermediario_Desde IS NULL)
					AND ("CustodiaIntermediario" <= p_CustodiaIntermediario_Hasta OR p_CustodiaIntermediario_Hasta IS NULL)
					AND ("CustodiaBolsas" >= p_CustodiaBolsas_Desde OR p_CustodiaBolsas_Desde IS NULL)
					AND ("CustodiaBolsas" <= p_CustodiaBolsas_Hasta OR p_CustodiaBolsas_Hasta IS NULL)
					AND ("CustodiaOtros" >= p_CustodiaOtros_Desde OR p_CustodiaOtros_Desde IS NULL)
					AND ("CustodiaOtros" <= p_CustodiaOtros_Hasta OR p_CustodiaOtros_Hasta IS NULL);
					
END
