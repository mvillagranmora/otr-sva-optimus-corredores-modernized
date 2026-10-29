SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::MarketData_CrearTransacciones";
GO
CREATE PROCEDURE
-- 
--  Proposito: crear transacciones que llegan por market data
--  Autor: TEAM ORDENES, AW YEAH
--  Fecha Creacion: 07/01/2016
--  
"BSCL.Orden.BD.Servicios::MarketData_CrearTransacciones" ( 
    IN p_IdEmpresa				BIGINT,
    IN p_IdUsuario				BIGINT,
    IN p_SegundosZonaHoraria	BIGINT,
	OUT p_out_InformacionContador int,
	OUT p_out_AdvertenciasContador int,
	OUT p_out_ErroresContador int
    
	)
	
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN

	--MANEJO DE ERRORES
	DECLARE v_Errores "BSCL.Orden.BD.Tipos::SalidaDetalle" DEFAULT SELECT 0 as "IdInterno",0 as "IdOperacion",'' as "Resultado",'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2" FROM DUMMY WHERE 1=2;
	
	DECLARE v_out_InformacionContador	INT;
	DECLARE v_out_AdvertenciasContador	INT;
	DECLARE v_out_ErroresContador		INT;
	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle					=> :v_Errores,
			
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> v_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> v_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> v_out_ErroresDetalle);
		
		INSERT INTO "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_InformacionDetalle"
		("IdInterno","IdOperacion","Codigo","Descripcion")
		SELECT "IdInterno","IdOperacion","Codigo","Descripcion" FROM :v_out_InformacionDetalle;
		
		INSERT INTO "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_AdvertenciasDetalle"
		("IdInterno","IdOperacion","Codigo","RequiereAutorizacion","Descripcion")
		SELECT "IdInterno","IdOperacion","Codigo","RequiereAutorizacion","Descripcion" FROM :v_out_AdvertenciasDetalle;
		
		INSERT INTO "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_ErroresDetalle"
		("IdInterno","IdOperacion","Codigo","Descripcion")
		SELECT "IdInterno","IdOperacion","Codigo","Descripcion" FROM :v_out_ErroresDetalle;
		
		DROP TABLE #TRANMD;
	END;
	
	BEGIN
	
		DECLARE v_CodigoCorredoraBolsaActual 		NVARCHAR(40);
		DECLARE v_CodigoCorredoraBolsaFixActual 	NVARCHAR(40);
		DECLARE v_IdCorredoraActual			 		BIGINT;
		
		DECLARE v_IdLibroOrdenesDefecto			BIGINT;
		DECLARE v_IdPaisSistema					BIGINT;
		DECLARE v_out_AdvertenciasContador 		INT;
		DECLARE v_out_ErroresContador 			INT;
		DECLARE v_IdBolsa						NVARCHAR(3) = 'BCS';
		
		DECLARE v_Transacciones TABLE(
			"IdInterno"	 						BIGINT,
			"FolioTransaccion"					NVARCHAR(40),
			"FechaCreacion"						NVARCHAR(10),
			"HoraCreacion"						NVARCHAR(10),
			"FechaEmision"						NVARCHAR(10),
			"Plazo"								BIGINT,
			"EsCuentaPropia"					NVARCHAR(1),
			"EsFungible"						NVARCHAR(1),
			"EsVentaCorta"						NVARCHAR(1),
			"CodigoOperadorContraparte"			NVARCHAR(40),
			"CodigoOperadorCorredor"			NVARCHAR(40),
			"IdSistemaBursatil"					NVARCHAR(40),
			"IdEmisor"							NVARCHAR(40),
			"Cantidad"							DECIMAL(26,8),	--NOMINALES EN RF, CORTES EN IF, 
			"Monto"								DECIMAL(26,8),
			
			"Precio"							DECIMAL(26,8),
			"Tasa"								DECIMAL(26,8), --SIMULTANEA, RENTA FIJA, IF, FUTURO BCU
			"MontoPlazo"						DECIMAL(26,8),
			"PrecioPlazo"						DECIMAL(26,8),
			"ValorRescate"						DECIMAL(26,8),
			"IdTipoReajuste"					NVARCHAR(3),
			"PagaderoEn"						NVARCHAR(3),
			"IdEmpleado"						BIGINT,
			"IdTipoOperacion"					NVARCHAR(6),
			"IdCondicionDeLiquidacionContado"	NVARCHAR(2),
			"FechaLiquidacionContado"			NVARCHAR(10),
			"FechaVencimiento"					NVARCHAR(10),
			"IdCondicionDeLiquidacionPlazo"		NVARCHAR(2),
			"FechaLiquidacionPlazo"				NVARCHAR(10),
			"IdMonedaLiquidacion"				NVARCHAR(3),
			"IdInstrumento"						BIGINT,
			"IdCorredorContraparte"				BIGINT,
			"IdBolsa"							NVARCHAR(3),
			"IdLibroOrdenes"					BIGINT,
			"IdInstrumentoSubyacente"			BIGINT,
			"EsFueraRueda"						NVARCHAR(1),
			"IndicadorDCV"						NVARCHAR(1),
			"EsBilateral"						NVARCHAR(1),
			"MarcaNegociacion"					NVARCHAR(40),
			"TipoCambioCorresponsal"			DECIMAL(26,8),
			"IdCorredorCorresponsal"			BIGINT,
			"MontoLiquidacionCorresponsal"		DECIMAL(26,8),
			"IdMonedaLiquidacionCorresponsal"	NVARCHAR(3),
			"FolioTransaccionPlazo"				NVARCHAR(40),
			"MejorPrecioCompra"					DECIMAL(26,8),
			"MejorPrecioVenta"					DECIMAL(26,8),
			"NemotecnicoEspecificoBolson" 		NVARCHAR(100)
			);
		
		CREATE LOCAL TEMPORARY TABLE #TRANMD (
			"IdInterno"							BIGINT,			--LLAVE OPTIMUS
			"tradeTime"							NVARCHAR(30),	--LLAVE MARKET DATA
			"tradeID" 							NVARCHAR(20),	--LLAVE MARKET DATA
			"Folio" 							NVARCHAR(40),--
			"HoraSistemaCreacion"				TIME,--
			"FechaCreacion"						NVARCHAR(10),--
			"FechaLiquidacion"					NVARCHAR(10),--
			"FechaVencimiento"					NVARCHAR(10) NOT NULL DEFAULT '0001-01-01',--
			"IdEmisor"							NVARCHAR(40),--
			"IdSistemaBursatil"					NVARCHAR(40),
			"Cantidad"							DECIMAL(26,8),--
			"Tasa"								DECIMAL(26,8),
			"Monto"								DECIMAL(26,8),--
			"Plazo"								INT 			NOT NULL DEFAULT 0,--
			"ValorRescate"						DECIMAL(26,8) 	NOT NULL DEFAULT 0,--
			"EsFungible"						NVARCHAR(1) NOT NULL DEFAULT 'N',
			"Precio"							DECIMAL(26,8),--
			"IdTipoOperacionCompra"				NVARCHAR(6),--
			"IdTipoOperacionVenta"				NVARCHAR(6),--
			"IdTipoReajuste"					NVARCHAR(3) NOT NULL DEFAULT '',--
			"PagaderoEn"						NVARCHAR(3) NULL,--
			"IdCondicionDeLiquidacion"			NVARCHAR(2),--
			"IdBolsa"							NVARCHAR(3),--
			"IdInstrumento"						BIGINT,--
			"IdTipoInstrumento"					NVARCHAR(3),--
			"IdMonedaLiquidacion"				NVARCHAR(3),--
			"EsVentaCorta"						NVARCHAR(1),--
			"CodigoOperadorCompra" 				NVARCHAR(40),--
			"CodigoOperadorVenta" 				NVARCHAR(40),--
			"IdLibroOrdenesCompra"				BIGINT,--
			"IdLibroOrdenesVenta"				BIGINT,--
			"MarcaNegociacion"					NVARCHAR(40),--
			"EsFueraRueda"						NVARCHAR(1),--
			"IndicadorDCV"						NVARCHAR(1),--
			"IdCorredoraCompra"					BIGINT,--
			"IdCorredoraVenta"					BIGINT,--
			"CodigoCorredoraCompra"				NVARCHAR(40),--
			"CodigoCorredoraVenta"				NVARCHAR(40),--
			"MejorPrecioCompra"					DECIMAL(26,8),
			"MejorPrecioVenta"					DECIMAL(26,8),
			"NemotecnicoEspecificoBolson"		NVARCHAR(100) NOT NULL DEFAULT '',
			"DiasLiquidacion"					BIGINT DEFAULT 0,
			"MontoPlazo"						DECIMAL(26,8) NOT NULL DEFAULT 0,
			"PrecioPlazo"						DECIMAL(26,8) NOT NULL DEFAULT 0,
			"IdInstrumentoSubyacente"			BIGINT NOT NULL DEFAULT 0,
			"IdInstrumentoSimultaneaCompra"		BIGINT,
			"IdInstrumentoSimultaneaVenta"		BIGINT,
			"EsPagareCentral"					NVARCHAR(1) NOT NULL DEFAULT 'N'
		);
		
		IF (:p_IdEmpresa IS NULL) THEN
			v_Errores = SELECT "IdInterno",0 as "IdOperacion",'RECHAZO' as "Resultado",'FALTA_PARAMETRO_EMPRESA' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2" 
						FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRV_Entrada_Entrada" UNION ALL
						SELECT "IdInterno",0 as "IdOperacion",'RECHAZO' as "Resultado",'FALTA_PARAMETRO_EMPRESA' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2" 
						FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRF_Entrada_Entrada" UNION ALL
						SELECT "IdInterno",0 as "IdOperacion",'RECHAZO' as "Resultado",'FALTA_PARAMETRO_EMPRESA' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2" 
						FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesIF_Entrada_Entrada";
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		IF (:v_IdBolsa IS NULL) THEN
			v_Errores = SELECT "IdInterno",0 as "IdOperacion",'RECHAZO' as "Resultado",'FALTA_PARAMETRO_BOLSA' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2" 
						FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRV_Entrada_Entrada" UNION ALL
						SELECT "IdInterno",0 as "IdOperacion",'RECHAZO' as "Resultado",'FALTA_PARAMETRO_BOLSA' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2" 
						FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRF_Entrada_Entrada" UNION ALL
						SELECT "IdInterno",0 as "IdOperacion",'RECHAZO' as "Resultado",'FALTA_PARAMETRO_BOLSA' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2" 
						FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesIF_Entrada_Entrada";
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		--OBTIENE CODIGO CORREDORA ACTUAL
		v_Parametros = SELECT corr."IdCorredora",corrcod."CodigoCorredora",corrcod."CodigoCorredoraFix"
						FROM "BSCL.Plataforma.BD::PLA.Empresa" emp
						INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" soc on emp."IdSocioDeNegocio" = soc."IdSocioDeNegocio" and soc."IdEmpresa" = :p_IdEmpresa
						INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per on per."IdPersona" = soc."IdPersona"
						INNER JOIN "BSCL.Plataforma.BD::INS.Corredora" corr on corr."IdPersona" = soc."IdPersona"
						INNER JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrcod on corr."IdCorredora" = corrcod."IdCorredora" and "IdBolsa" = :v_IdBolsa;
						
		IF (IS_EMPTY(:v_Parametros)) THEN
			v_Errores = SELECT "IdInterno",0 as "IdOperacion",'RECHAZO' as "Resultado",'NO_SE_PUDO_OBTENER_CONFIGURACION_CORREDORA' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2" 
						FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRV_Entrada_Entrada" UNION ALL
						SELECT "IdInterno",0 as "IdOperacion",'RECHAZO' as "Resultado",'NO_SE_PUDO_OBTENER_CONFIGURACION_CORREDORA' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2" 
						FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRF_Entrada_Entrada" UNION ALL
						SELECT "IdInterno",0 as "IdOperacion",'RECHAZO' as "Resultado",'NO_SE_PUDO_OBTENER_CONFIGURACION_CORREDORA' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2" 
						FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesIF_Entrada_Entrada";
			SIGNAL SALIDA_CONTROLADA;
			
		END IF;
		
		v_LibroDefecto = SELECT "IdLibroOrdenes" FROM "BSCL.Orden.BD::ORD.LibroOrdenes" WHERE "LibroPorDefecto" = 'S' AND "IdEmpresa" = p_IdEmpresa;
		IF (IS_EMPTY(:v_LibroDefecto)) THEN
			v_Errores = SELECT "IdInterno",0 as "IdOperacion",'RECHAZO' as "Resultado",'NO_SE_PUDO_OBTENER_CONFIGURACION_LIBRO_ORDENES' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2" 
						FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRV_Entrada_Entrada";
			SIGNAL SALIDA_CONTROLADA;
			
		END IF;
		
		SELECT "IdCorredora","CodigoCorredora","CodigoCorredoraFix" INTO v_IdCorredoraActual,v_CodigoCorredoraBolsaActual,v_CodigoCorredoraBolsaFixActual FROM :v_Parametros;
		SELECT TOP 1 "IdLibroOrdenes" into v_IdLibroOrdenesDefecto FROM :v_LibroDefecto;
		SELECT "IdPais" INTO v_IdPaisSistema FROM "BSCL.Plataforma.BD::PLA.Empresa" WHERE "IdEmpresa" = :p_IdEmpresa;	
		
		--RF
		INSERT INTO #TRANMD("IdInterno","tradeTime","tradeID","Folio",
			"HoraSistemaCreacion","FechaCreacion","FechaVencimiento",
			"FechaLiquidacion","IdEmisor","IdSistemaBursatil",
			"Cantidad","Monto","Precio",
			"IdCondicionDeLiquidacion","Tasa",
			"IdBolsa","IdInstrumento","IdTipoInstrumento","IdMonedaLiquidacion","EsVentaCorta",
			"CodigoOperadorCompra","CodigoOperadorVenta","MarcaNegociacion","EsFueraRueda","IndicadorDCV",
			"IdCorredoraCompra","IdCorredoraVenta","MejorPrecioCompra","MejorPrecioVenta","CodigoCorredoraCompra","CodigoCorredoraVenta","NemotecnicoEspecificoBolson")			
		SELECT ent."IdInterno","tradeTime","tradeID",ent."tradeID" as "Folio", 
			TO_TIME(ADD_SECONDS(TO_SECONDDATE("tradeTime",'YYYYMMDDHH24MISSFF7'),-1*0/*:p_SegundosZonaHoraria*/)) as "HoraSistemaCreacion", --MD MANDA LAS FECHAS EN UTC
			TO_NVARCHAR(TO_DATE(ADD_SECONDS(TO_SECONDDATE("tradeTime",'YYYYMMDDHH24MISSFF7'),-1*0/*:p_SegundosZonaHoraria*/)),'YYYY-MM-DD') as "FechaCreacion",
			
			CASE 
				WHEN ent."symbol" LIKE 'CERO%' OR ent."symbol" LIKE 'XERO%' OR ent."symbol" LIKE 'ZERO%' 
					THEN TO_NVARCHAR(TO_DATE(LEFT(RIGHT(ent."symbol",6),4) || '20' ||  RIGHT(RIGHT(ent."symbol",6),2),'DDMMYYYY'),'YYYY-MM-DD')
					--TODO AGREGAR LOS BR CUANDO SE MIGRE A JAVA, AUNQUE NO SE TRANSEN EN HT
				ELSE '0001-01-01' END AS "FechaVencimiento",
			
			
			TO_NVARCHAR(TO_DATE(ent."settlDate",'YYYYMMDD')) as "FechaLiquidacion",
			emi."IdEmisor",CASE LEFT(ent."auctionId",1) WHEN 'H' THEN 'BCSREMATEHOLANDES' WHEN 'T' THEN 'BCSTELERENTA' WHEN 'E' THEN 'BCSREMATEELECTRONICO' ELSE NULL END AS "IdSistemaBursatil",
			ent."quantity" as "Cantidad",ent."currencySettlementAmount" as "Monto",ent."price" as "Precio",
			
			CASE RIGHT(LEFT("tradeCondition",2),1) WHEN ' ' THEN 'T2' WHEN 'M' THEN 'T1' WHEN 'H' THEN 'T0' WHEN '3' THEN 'T3' WHEN '5' THEN 'T5' ELSE null END AS "IdCondicionDeLiquidacion",
			ent."tir"*CASE ent."signTir" WHEN '+' THEN 1 WHEN '-' THEN -1 ELSE 0 END as "Tasa",
			:v_IdBolsa as "IdBolsa",ins."IdInstrumento",ins."IdTipoInstrumento",ins."IdMoneda" as "IdMonedaLiquidacion",
			CASE RIGHT(LEFT("tradeCondition",4),1) WHEN ' ' THEN 'N' WHEN 'V' THEN 'S' ELSE 'N' END AS "EsVentaCorta",
			
			CASE WHEN ent."buyExecutingFirm" != ent."buyEnteringFirm" AND RTRIM(ent."buyEnteringFirm") != '' THEN ent."buyEnteringFirm" ELSE ent."traderBuy" END AS "CodigoOperadorCompra",
			CASE WHEN ent."sellExecutingFirm" != ent."sellEnteringFirm" AND RTRIM(ent."sellEnteringFirm") != '' THEN ent."sellEnteringFirm" ELSE ent."traderSell" END AS "CodigoOperadorVenta",
			
			'' as "MarcaNegociacion",'N' AS "EsFueraRueda",ent."dcvTitle" as "IndicadorDCV",
			corrCompra."IdCorredora" as "IdCorredoraCompra",
			corrVenta."IdCorredora" as "IdCorredoraVenta",
			0 AS "MejorPrecioCompra",0 as "MejorPrecioVenta",
			ent."buyExecutingFirm" AS "CodigoCorredoraCompra",ent."sellExecutingFirm" AS "CodigoCorredoraVenta",ent."symbol" AS "NemotecnicoEspecificoBolson"
			FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRF_Entrada_Entrada" ent
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = 
																					CASE WHEN ent."symbol" LIKE 'CERO%' THEN 'CERO'
																						WHEN ent."symbol" LIKE 'ZERO%' THEN 'ZERO'
																						WHEN ent."symbol" LIKE 'XERO%' THEN 'XERO' ELSE ent."symbol" END
			LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" emi on emi."IdEmisor" = ins."IdEmisor"

			LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrCompraCodigo on corrCompraCodigo."IdBolsa" = :v_IdBolsa and corrCompraCodigo."CodigoCorredoraFix" = ent."buyExecutingFirm"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrCompra on corrCompra."IdCorredora" = corrCompraCodigo."IdCorredora"
			
			LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrVentaCodigo on corrVentaCodigo."IdBolsa" = :v_IdBolsa and corrVentaCodigo."CodigoCorredoraFix" = ent."sellExecutingFirm"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrVenta on corrVenta."IdCorredora" = corrVentaCodigo."IdCorredora"
			;
		
		--RV	
		INSERT INTO #TRANMD("IdInterno","tradeTime","tradeID","Folio",
			"HoraSistemaCreacion","FechaCreacion",
			"FechaLiquidacion","IdEmisor","IdSistemaBursatil",
			"Cantidad","Monto","Precio",
			"IdCondicionDeLiquidacion","Tasa",
			"IdBolsa","IdInstrumento","IdTipoInstrumento","IdMonedaLiquidacion","EsVentaCorta",
			"CodigoOperadorCompra","CodigoOperadorVenta","MarcaNegociacion","EsFueraRueda","IndicadorDCV",
			"IdCorredoraCompra","IdCorredoraVenta","CodigoCorredoraCompra","CodigoCorredoraVenta","MejorPrecioCompra","MejorPrecioVenta")
		SELECT ent."IdInterno","tradeTime","tradeID",ent."tradeID" as "Folio", 
			TO_TIME(ADD_SECONDS(TO_SECONDDATE("tradeTime",'YYYYMMDDHH24MISSFF7'),-1*0/*:p_SegundosZonaHoraria*/)) as "HoraSistemaCreacion",
			TO_NVARCHAR(TO_DATE(ADD_SECONDS(TO_SECONDDATE("tradeTime",'YYYYMMDDHH24MISSFF7'),-1*0/*:p_SegundosZonaHoraria*/)),'YYYY-MM-DD') as "FechaCreacion",
			'0001-01-01' as "FechaLiquidacion",
			emi."IdEmisor",'BCSTELEPREGON' AS "IdSistemaBursatil",
			ent."quantity" as "Cantidad",ent."amount" as "Monto",ent."price" as "Precio",
			CASE LTRIM(RTRIM(ent."settlementCondition")) WHEN 'T+3' THEN 'T3' WHEN 'CN' THEN 'T2' WHEN '' THEN 'T2' WHEN 'PM' THEN 'T1' WHEN 'PH' THEN 'T0' END as "IdCondicionDeLiquidacion",
			0 as "Tasa",
			:v_IdBolsa as "IdBolsa",ins."IdInstrumento",ins."IdTipoInstrumento",ins."IdMoneda" as "IdMonedaLiquidacion",CASE ent."shortSell" WHEN 'YES' THEN 'S' WHEN 'NO' THEN 'N' END AS "EsVentaCorta",
			
			CASE WHEN ent."buyerBroker" != ent."buyEnteringFirm" AND RTRIM(ent."buyEnteringFirm") != '' THEN ent."buyEnteringFirm" ELSE ent."buyEnteringTrader" END AS "CodigoOperadorCompra",
			CASE WHEN ent."sellerBroker" != ent."sellEnteringFirm" AND RTRIM(ent."sellEnteringFirm") != '' THEN ent."sellEnteringFirm" ELSE ent."sellEnteringTrader" END AS "CodigoOperadorVenta",
			
			
			'' as "MarcaNegociacion",'N' AS "EsFueraRueda",'N' as "IndicadorDCV",
			corrCompra."IdCorredora" as "IdCorredoraCompra",
			corrVenta."IdCorredora" as "IdCorredoraVenta",
			
			
			
			ent."buyerBroker" AS "CodigoCorredoraCompra",ent."sellerBroker" AS "CodigoCorredoraVenta",
			ent."priceBuyingPrice" AS "MejorPrecioCompra",ent."priceSellingPrice" as "MejorPrecioVenta"
			FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesRV_Entrada_Entrada" ent
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = ent."instrument"
			LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" emi on emi."IdEmisor" = ins."IdEmisor"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoIns on tipoIns."IdTipoInstrumento" = ins."IdTipoInstrumento"
			
			LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrCompraCodigo on corrCompraCodigo."IdBolsa" = :v_IdBolsa and corrCompraCodigo."CodigoCorredoraFix" = ent."buyerBroker"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrCompra on corrCompra."IdCorredora" = corrCompraCodigo."IdCorredora"
			
			LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrVentaCodigo on corrVentaCodigo."IdBolsa" = :v_IdBolsa and corrVentaCodigo."CodigoCorredoraFix" = ent."sellerBroker"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrVenta on corrVenta."IdCorredora" = corrVentaCodigo."IdCorredora"
			
			;	
		
		
		
		
		
		
		
		
		--SIM
		INSERT INTO #TRANMD("IdInterno","Folio",
			"HoraSistemaCreacion","FechaCreacion",
			"FechaLiquidacion","IdEmisor","IdSistemaBursatil",
			"Cantidad","Monto","Precio","PrecioPlazo","Plazo","MontoPlazo","IdInstrumentoSubyacente",
			"IdCondicionDeLiquidacion","Tasa","FechaVencimiento",
			"IdBolsa","IdInstrumento","IdTipoInstrumento","IdMonedaLiquidacion","EsVentaCorta",
			"CodigoOperadorCompra","CodigoOperadorVenta","MarcaNegociacion","EsFueraRueda","IndicadorDCV",
			"IdCorredoraCompra","IdCorredoraVenta","CodigoCorredoraCompra","CodigoCorredoraVenta","MejorPrecioCompra","MejorPrecioVenta")
		SELECT ent."IdInterno",ent."folio" as "Folio", 
			TO_TIME(ADD_SECONDS(TO_SECONDDATE("transactionDay" || "transactionTime",'YYYYMMDDHH24:MI:SS'),-1*0/*:p_SegundosZonaHoraria*/)) as "HoraSistemaCreacion",
			TO_NVARCHAR(TO_DATE("transactionDay",'YYYYMMDD'),'YYYY-MM-DD') as "FechaCreacion",
			'0001-01-01' as "FechaLiquidacion",
			emi."IdEmisor",'BCSREMATESERIALIZADO' AS "IdSistemaBursatil",
			ent."qty" as "Cantidad",ent."amount" as "Monto",ent."spotPrice" as "Precio",ent."forwardPrice" AS "PrecioPlazo",0 as "Plazo", ent."forwardPrice" * ent."qty" as "MontoPlazo",
			ins."IdInstrumento" AS "IdInstrumentoSubyacente",
			CASE LTRIM(RTRIM(ent."settlementType")) WHEN 'T+3' THEN 'T3' WHEN 'CN' THEN 'T2' WHEN '' THEN 'T2' WHEN 'PM' THEN 'T1' WHEN 'PH' THEN 'T0' END as "IdCondicionDeLiquidacion",
			ent."pubRate" as "Tasa", TO_NVARCHAR(TO_DATE(ent."expirationDate",'YYYYMMDD'),'YYYY-MM-DD') AS "FechaVencimiento",
			:v_IdBolsa as "IdBolsa",null AS "IdInstrumento",'SIM' AS "IdTipoInstrumento",'' as "IdMonedaLiquidacion",'N' AS "EsVentaCorta",
			
			CASE WHEN ent."buyExecutingFirm" != ent."buyEnteringFirm" AND RTRIM(ent."buyEnteringFirm") != '' THEN ent."buyEnteringFirm" ELSE ent."buyEnteringTrader" END AS "CodigoOperadorCompra",
			CASE WHEN ent."sellExecutingFirm" != ent."sellEnteringFirm" AND RTRIM(ent."sellEnteringFirm") != '' THEN ent."sellEnteringFirm" ELSE ent."sellEnteringTrader" END AS "CodigoOperadorVenta",
			
			
			'' as "MarcaNegociacion",'N' AS "EsFueraRueda",'N' as "IndicadorDCV",
			corrCompra."IdCorredora" as "IdCorredoraCompra",
			corrVenta."IdCorredora" as "IdCorredoraVenta",
			
			
			
			ent."buyExecutingFirm" AS "CodigoCorredoraCompra",ent."sellExecutingFirm" AS "CodigoCorredoraVenta",
			0 AS "MejorPrecioCompra",0 AS "MejorPrecioVenta"
			FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesSIM_Entrada_Entrada" ent
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = ent."symbol"
			LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" emi on emi."IdEmisor" = ins."IdEmisor"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoIns on tipoIns."IdTipoInstrumento" = ins."IdTipoInstrumento"
			
			LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrCompraCodigo on corrCompraCodigo."IdBolsa" = :v_IdBolsa and corrCompraCodigo."CodigoCorredoraFix" = ent."buyExecutingFirm"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrCompra on corrCompra."IdCorredora" = corrCompraCodigo."IdCorredora"
			
			LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrVentaCodigo on corrVentaCodigo."IdBolsa" = :v_IdBolsa and corrVentaCodigo."CodigoCorredoraFix" = ent."sellExecutingFirm"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrVenta on corrVenta."IdCorredora" = corrVentaCodigo."IdCorredora"
			
			;	
		
		
		UPDATE tran 
		SET "DiasLiquidacion" = "PlazoEnDias"
		FROM #TRANMD tran
		LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" liq on liq."IdCondicionDeLiquidacion" = tran."IdCondicionDeLiquidacion";
		
		v_fechasLiquidacionIN =	SELECT distinct "FechaCreacion" as "FechaInicial","DiasLiquidacion" as "NumeroDias"
								FROM #TRANMD tran;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguienteMasivo"(:v_IdPaisSistema,'S',:v_fechasLiquidacionIN,:v_fechasLiquidacionOUT);
		
		UPDATE tran 
		SET "FechaLiquidacion" = COALESCE(fec_liq."FechaCalculada",'0001-01-01')
		FROM #TRANMD tran
		INNER JOIN :v_fechasLiquidacionOUT fec_liq ON fec_liq."FechaInicial" = tran."FechaCreacion" and fec_liq."NumeroDias" = tran."DiasLiquidacion"
		WHERE tran."FechaLiquidacion" = '0001-01-01';
		
		
		
		v_entrada = SELECT "IdInterno","FechaLiquidacion" aS "FechaDesde","FechaVencimiento" as "FechaHasta",:v_IdPaisSistema as "IdPais",'N' as "SoloDiasHabiles" 
					FROM #TRANMD WHERE "IdTipoInstrumento" = 'SIM'; 
		
		--SELECT 'v_entrada',* FROM :v_entrada;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerPlazoMasivo" ( 	
			p_tbl			=> :v_entrada,
			p_out_Plazos 	=> v_out_Plazos);
		
		--select 'v_out_Plazos',* from :v_out_Plazos;
		
		UPDATE TRAN
		SET "Plazo" = outt."Plazo"
		FROM #TRANMD TRAN
		INNER JOIN :v_out_Plazos outt on TRAN."IdInterno" = outt."IdInterno";
		
		
		--IF
		INSERT INTO #TRANMD("IdInterno","tradeTime","tradeID","Folio",
			"HoraSistemaCreacion","FechaCreacion",
			"FechaLiquidacion","FechaVencimiento","IdEmisor","IdSistemaBursatil",
			"Cantidad","Monto","Precio","ValorRescate","IdTipoReajuste","PagaderoEn",
			"IdCondicionDeLiquidacion",
			"Tasa","Plazo","EsFungible",
			"IdBolsa","IdInstrumento","IdTipoInstrumento","IdMonedaLiquidacion","EsVentaCorta",
			"CodigoOperadorCompra","CodigoOperadorVenta","MarcaNegociacion","EsFueraRueda","IndicadorDCV",
			"IdCorredoraCompra","IdCorredoraVenta","MejorPrecioCompra","MejorPrecioVenta","CodigoCorredoraCompra","CodigoCorredoraVenta","EsPagareCentral")			
		SELECT ent."IdInterno","TRADETIME","TRADEID",ent."TRADEID" as "Folio", 
			TO_TIME(ADD_SECONDS(TO_SECONDDATE("TRADETIME",'YYYYMMDDHH24MISSFF7'),-1*0/*:p_SegundosZonaHoraria*/)) as "HoraSistemaCreacion",
			TO_NVARCHAR(TO_DATE(ADD_SECONDS(TO_SECONDDATE("TRADETIME",'YYYYMMDDHH24MISSFF7'),-1*0/*:p_SegundosZonaHoraria*/)),'YYYY-MM-DD') as "FechaCreacion",
			TO_NVARCHAR(TO_DATE(ent."SETTLDATE",'YYYYMMDD')) as "FechaLiquidacion",TO_NVARCHAR(TO_DATE(ent."EXPIRETIME",'YYYYMMDD')) as "FechaVencimiento",
			ent."ISSUER" AS "IdEmisor",CASE LEFT(ent."AUCTIONID",1) WHEN 'H' THEN 'BCSREMATEHOLANDES' WHEN 'T' THEN 'BCSTELERENTA' WHEN 'E' THEN 'BCSREMATEELECTRONICO' ELSE NULL END AS "IdSistemaBursatil",
			1 as "Cantidad",ent."LOCALAMOUNT" as "Monto",0 as "Precio",ent."RESCUEVALUE" as "ValorRescate",
			
			CASE ent."RESETTYPE" WHEN 'NR' THEN 'CLP' WHEN 'UD' THEN 'USD' WHEN 'US' THEN 'USD' WHEN 'IC' THEN 'ICP' WHEN 'UF' THEN 'UF' WHEN 'ES' THEN 'EUR' WHEN 'EU' THEN 'EUR' END AS "IdTipoReajuste",
			CASE ent."RESETTYPE" WHEN 'NR' THEN 'CLP' WHEN 'UD' THEN 'USD' WHEN 'US' THEN 'CLP' WHEN 'IC' THEN 'CLP' WHEN 'UF' THEN 'CLP' WHEN 'ES' THEN 'CLP' WHEN 'EU' THEN 'EUR' END AS "PagaderoEn",
			
			CASE RIGHT(LEFT("TRADECONDITION",2),1) WHEN ' ' THEN 'T2' WHEN 'M' THEN 'T1' WHEN 'H' THEN 'T0' WHEN '3' THEN 'T3' WHEN '5' THEN 'T5' ELSE null END AS "IdCondicionDeLiquidacion",
			ent."RATE"*CASE ent."SIGNRATE" WHEN '+' THEN 1 WHEN '-' THEN -1 ELSE 0 END as "Tasa",ent."TERM" as "Plazo",CASE ent."SYMBOL" WHEN 'PRBC' THEN 'N' WHEN 'PDBC' THEN 'N' ELSE 'S' END AS "EsFungible",
			:v_IdBolsa as "IdBolsa",ins."IdInstrumento",tipoIns."IdTipoInstrumento",ins."IdMoneda" as "IdMonedaLiquidacion",
			CASE RIGHT(LEFT("TRADECONDITION",4),1) WHEN ' ' THEN 'N' WHEN 'V' THEN 'S' ELSE 'N' END AS "EsVentaCorta",
			
			CASE WHEN ent."BUYEXECUTINGFIRM" != ent."BUYENTERINGFIRM" AND RTRIM(ent."BUYENTERINGFIRM") != '' THEN ent."BUYENTERINGFIRM" ELSE ent."TRADERBUY" END AS "CodigoOperadorCompra",
			CASE WHEN ent."SELLEXECUTINGFIRM" != ent."SELLENTERINGFIRM" AND RTRIM(ent."SELLENTERINGFIRM") != '' THEN ent."SELLENTERINGFIRM" ELSE ent."TRADERSELL" END AS "CodigoOperadorVenta",
			
			
			'' as "MarcaNegociacion",'N' AS "EsFueraRueda",ent."DCVTITLE" as "IndicadorDCV",
			corrCompra."IdCorredora" as "IdCorredoraCompra",
			corrVenta."IdCorredora" as "IdCorredoraVenta",
			
			
			0 AS "MejorPrecioCompra",0 as "MejorPrecioVenta",
			ent."BUYEXECUTINGFIRM" AS "CodigoCorredoraCompra",ent."SELLEXECUTINGFIRM" AS "CodigoCorredoraVenta",
			CASE ent."SYMBOL" WHEN 'PRBC' THEN 'S' WHEN 'PDBC' THEN 'S' ELSE 'N' END AS "EsPagareCentral"
			FROM "BSCL.Orden.BD::ORD.MarketData_CrearTransaccionesIF_Entrada_Entrada" ent
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."Nemotecnico" = ent."SYMBOL"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoIns on tipoIns."IdTipoInstrumento" = ins."IdTipoInstrumento"			
			
			LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrCompraCodigo on corrCompraCodigo."IdBolsa" = :v_IdBolsa and corrCompraCodigo."CodigoCorredoraFix" = ent."BUYEXECUTINGFIRM"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrCompra on corrCompra."IdCorredora" = corrCompraCodigo."IdCorredora"
			
			LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" corrVentaCodigo on corrVentaCodigo."IdBolsa" = :v_IdBolsa and corrVentaCodigo."CodigoCorredoraFix" = ent."SELLEXECUTINGFIRM"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" corrVenta on corrVenta."IdCorredora" = corrVentaCodigo."IdCorredora"
			
			;	
			
		UPDATE TRAN
		SET "Cantidad" = "ValorRescate" / reaj."CorteMinimo"
		FROM #TRANMD TRAN
		INNER JOIN "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste" reaj on reaj."IdInstrumento" = TRAN."IdInstrumento" AND reaj."IdTipoReajuste" = TRAN."IdTipoReajuste"
		WHERE TRAN."EsPagareCentral" = 'S';
		
		UPDATE TRAN
		SET "IdTipoOperacionCompra" = tipoOpeCompra."IdTipoOperacion",
		"IdTipoOperacionVenta" = tipoOpeVenta."IdTipoOperacion",
		"IdInstrumentoSimultaneaCompra" = CASE TRAN."IdTipoInstrumento" WHEN 'SIM' THEN tipoOpeInsCompra."IdInstrumento" ELSE NULL END,
		"IdInstrumentoSimultaneaVenta" = CASE TRAN."IdTipoInstrumento" WHEN 'SIM' THEN tipoOpeInsVenta."IdInstrumento" ELSE NULL END

		FROM #TRANMD TRAN
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" tipoOpeInsCompra on tipoOpeInsCompra."IdTipoInstrumento" = TRAN."IdTipoInstrumento"
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoOpeCompra on tipoOpeInsCompra."IdTipoOperacion" =  tipoOpeCompra."IdTipoOperacion" AND tipoOpeCompra."AccionOrden" = 'COMPRA'
			
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" tipoOpeInsVenta on tipoOpeInsVenta."IdTipoInstrumento" = TRAN."IdTipoInstrumento"
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoOpeVenta on tipoOpeInsVenta."IdTipoOperacion" =  tipoOpeVenta."IdTipoOperacion" AND tipoOpeVenta."AccionOrden" = 'VENTA';
		
		
		
		
		--------------VALIDACIONES-------------------
		v_TransaccionesYaProcesadas =	SELECT ent."IdInterno",count("IdTransaccion") as "CantidadTransacciones"
									  	FROM #TRANMD ent
										LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" tran on tran."Folio" = ent."tradeID" and tran."FechaSistemaCreacion" = ent."FechaCreacion"
										GROUP BY ent."IdInterno";
		
		--select 'v_TransaccionesYaProcesadas',* from :v_TransaccionesYaProcesadas;
		
		v_val = SELECT ent."IdInterno",
						--'OK' AS "CORREDORA_VENDE_NO_VALIDA",   
						CASE WHEN ent."IdCorredoraVenta" IS NULL THEN 'RECHAZO' ELSE 'OK' END  AS "CORREDORA_VENDE_NO_VALIDA",
						
						--'OK' AS "CORREDORA_COMPRA_NO_VALIDA",   
						CASE WHEN ent."IdCorredoraCompra" IS NULL THEN 'RECHAZO' ELSE 'OK' END AS "CORREDORA_COMPRA_NO_VALIDA",
						
						CASE WHEN ent."CodigoCorredoraCompra" != :v_CodigoCorredoraBolsaFixActual AND ent."CodigoCorredoraVenta" != :v_CodigoCorredoraBolsaFixActual THEN 'RECHAZO' ELSE 'OK' END AS "TRANSACCION_OTRAS_CORREDORAS",
						CASE WHEN TTT."CantidadTransacciones" > 0 THEN 'RECHAZO' ELSE 'OK' END AS "TRANSACCION_YA_PROCESADA",
						CASE WHEN ent."IdInstrumento" IS NULL AND ent."IdInstrumentoSimultaneaCompra" IS NULL AND "IdInstrumentoSimultaneaVenta" IS NULL 
							THEN 'RECHAZO' ELSE 'OK' END AS "INSTRUMENTO_NO_EXISTE"
						
				FROM #TRANMD ent
				INNER JOIN :v_TransaccionesYaProcesadas TTT ON ent."IdInterno" = TTT."IdInterno";
		
		--SELECT 'v_val',* FROM :v_val;
		
		v_TablaRequisitos = 
			SELECT "IdInterno","Resultado","CodigoValidacion"
			FROM (
				SELECT * ,
				
				COALESCE(
				"1" ||"CORREDORA_VENDE_NO_VALIDA",	"2" ||"CORREDORA_COMPRA_NO_VALIDA",	"3" ||"TRANSACCION_OTRAS_CORREDORAS",	"4" ||"TRANSACCION_YA_PROCESADA","5" ||"INSTRUMENTO_NO_EXISTE"
				)  	AS "Resultado",
				
				COALESCE(
				"1" ||'CORREDORA_VENDE_NO_VALIDA',	"2" ||'CORREDORA_COMPRA_NO_VALIDA',	"3" ||'TRANSACCION_OTRAS_CORREDORAS',	"4" ||'TRANSACCION_YA_PROCESADA',"5" ||'INSTRUMENTO_NO_EXISTE'
				)  AS "CodigoValidacion" 
				FROM :v_val,"BSCL.Plataforma.BD::PLA.Identidad"
			) WHERE "Resultado" IS NOT NULL;
		
		--SELECT 'v_TablaRequisitos',* FROM :v_TablaRequisitos;
		
		v_Errores =	SELECT tb."IdInterno", tb."IdInterno" as "IdOperacion","Resultado" ,
							CASE "CodigoValidacion" 
								WHEN 'CORREDORA_VENDE_NO_VALIDA' 		THEN 'CORREDORA_VENDE_NO_VALIDA'			--01
								WHEN 'CORREDORA_COMPRA_NO_VALIDA' 		THEN 'CORREDORA_COMPRA_NO_VALIDA'
								WHEN 'TRANSACCION_OTRAS_CORREDORAS' 	THEN 'TRANSACCION_OTRAS_CORREDORAS'
								WHEN 'TRANSACCION_YA_PROCESADA' 		THEN 'TRANSACCION_YA_PROCESADA'
								WHEN 'INSTRUMENTO_NO_EXISTE' 			THEN 'INSTRUMENTO_NO_EXISTE'
							END AS "Codigo",
							CASE "CodigoValidacion" 
								WHEN 'CORREDORA_VENDE_NO_VALIDA' 		THEN 'CodigoCorredora'							--01
								WHEN 'CORREDORA_COMPRA_NO_VALIDA' 		THEN 'CodigoCorredora'
								WHEN 'TRANSACCION_OTRAS_CORREDORAS' 	THEN 'CodigosCorredoras'
								WHEN 'TRANSACCION_YA_PROCESADA' 		THEN 'IdInterno'
								WHEN 'INSTRUMENTO_NO_EXISTE' 			THEN 'IdInterno'
							END AS "CodigoDato1",
							CASE "CodigoValidacion" 
								WHEN 'CORREDORA_VENDE_NO_VALIDA' 		THEN CAST(ent."CodigoCorredoraVenta" AS NVARCHAR(100))		--01
								WHEN 'CORREDORA_COMPRA_NO_VALIDA' 		THEN CAST(ent."CodigoCorredoraCompra" AS NVARCHAR(100))
								WHEN 'TRANSACCION_OTRAS_CORREDORAS' 	THEN CAST('C:' || ent."CodigoCorredoraCompra" || '-V:' || ent."CodigoCorredoraVenta" AS NVARCHAR(100))
								WHEN 'TRANSACCION_YA_PROCESADA' 		THEN CAST(tb."IdInterno" AS NVARCHAR(100))
								WHEN 'INSTRUMENTO_NO_EXISTE' 			THEN CAST(tb."IdInterno" AS NVARCHAR(100))
							END AS "ValorDato1",
							'' as "CodigoDato2",
							'' as "ValorDato2"
						FROM :v_TablaRequisitos tb
						INNER JOIN #TRANMD ent on tb."IdInterno" = ent."IdInterno"
						ORDER BY tb."IdInterno";	
		
		--SELECT 'v_Errores',* FROM :v_Errores;
		--FIN VALIDACIONES
		v_ErroresResumen = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:v_Errores);
		
		v_Libros = SELECT row_number() over (PARTITION BY "CodigoOperador" ORDER BY OPE."IdLibroOrdenes") AS "Prioridad",OPE."IdLibroOrdenesOperador",OPE."IdLibroOrdenes",OPE."CodigoOperador"
							FROM "BSCL.Orden.BD::ORD.LibroOrdenesOperador" OPE
							INNER JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" LIB on OPE."IdLibroOrdenes" = LIB."IdLibroOrdenes" WHERE LIB."IdEstado" = 'ABT' AND OPE."IdEstado" = 'ACT';
							
		
		--SETEO DE LIBRO
		v_LibrosCompra = SELECT "CodigoOperadorCompra",libroope."IdLibroOrdenes" AS "IdLibroOrdenesCompra"
						FROM #TRANMD TRAN
						LEFT JOIN :v_Libros as libroope ON libroope."CodigoOperador" = TRAN."CodigoOperadorCompra"
						WHERE ("Prioridad" = 1 OR libroope."CodigoOperador" IS NULL);
		
		v_LibrosVenta = SELECT "CodigoOperadorVenta",libroope."IdLibroOrdenes" AS "IdLibroOrdenesVenta"
						FROM #TRANMD TRAN
						LEFT JOIN :v_Libros as libroope ON libroope."CodigoOperador" = TRAN."CodigoOperadorVenta"
						WHERE ("Prioridad" = 1 OR libroope."CodigoOperador" IS NULL);
		
		
		UPDATE TRAN
		SET 
			"IdLibroOrdenesCompra" = COALESCE(librosCompra."IdLibroOrdenesCompra",:v_IdLibroOrdenesDefecto),
			"IdLibroOrdenesVenta" = COALESCE(librosVenta."IdLibroOrdenesVenta",:v_IdLibroOrdenesDefecto)
		FROM #TRANMD TRAN
		LEFT JOIN :v_LibrosCompra librosCompra ON librosCompra."CodigoOperadorCompra" = TRAN."CodigoOperadorCompra"
		LEFT JOIN :v_LibrosVenta librosVenta ON librosVenta."CodigoOperadorVenta" = TRAN."CodigoOperadorVenta";
		
		SELECT 'TRANS FINALES',* FROM #TRANMD;
		
		v_Transacciones = SELECT TRAN."IdInterno", 
								"Folio" AS "FolioTransaccion", 
								"FechaCreacion",
								"HoraSistemaCreacion" AS "HoraCreacion", 
								'0001-01-01' AS "FechaEmision",
								"Plazo",
								'N' AS "EsCuentaPropia",
								"EsFungible",
								"EsVentaCorta",
								CASE v_IdCorredoraActual WHEN "IdCorredoraCompra" THEN "CodigoOperadorVenta" WHEN "IdCorredoraVenta" THEN "CodigoOperadorCompra" END AS "CodigoOperadorContraparte",
								CASE v_IdCorredoraActual WHEN "IdCorredoraCompra" THEN "CodigoOperadorCompra" WHEN "IdCorredoraVenta" THEN "CodigoOperadorVenta" END AS "CodigoOperadorCorredor",
								"IdSistemaBursatil",
								"IdEmisor",
								"Cantidad",
								"Monto",
								"Precio",
								"Tasa",
								"MontoPlazo",
								"PrecioPlazo",
								"ValorRescate",
								"IdTipoReajuste",
								"PagaderoEn",
								0 AS "IdEmpleado",
								CASE v_IdCorredoraActual WHEN "IdCorredoraCompra" THEN "IdTipoOperacionCompra" WHEN "IdCorredoraVenta" THEN "IdTipoOperacionVenta" END AS "IdTipoOperacion",
								TRAN."IdCondicionDeLiquidacion" AS "IdCondicionDeLiquidacionContado",
								"FechaLiquidacion" AS "FechaLiquidacionContado",
								"FechaVencimiento",
								CASE WHEN "IdTipoInstrumento" = 'SIM' THEN 'TP' ELSE '' END AS "IdCondicionDeLiquidacionPlazo",
								CASE WHEN "IdTipoInstrumento" = 'SIM' THEN "FechaVencimiento" ELSE '0001-01-01' END AS "FechaLiquidacionPlazo",
								"IdMonedaLiquidacion",
								CASE WHEN "IdTipoInstrumento" = 'SIM' THEN
									CASE v_IdCorredoraActual WHEN "IdCorredoraCompra" THEN "IdInstrumentoSimultaneaCompra" WHEN "IdCorredoraVenta" THEN "IdInstrumentoSimultaneaVenta" END
									ELSE "IdInstrumento" 
								END AS "IdInstrumento",
								CASE v_IdCorredoraActual WHEN "IdCorredoraCompra" THEN COALESCE("IdCorredoraVenta",0) WHEN "IdCorredoraVenta" THEN COALESCE("IdCorredoraCompra",0) END AS "IdCorredorContraparte",
								"IdBolsa",
								CASE v_IdCorredoraActual WHEN "IdCorredoraCompra" THEN "IdLibroOrdenesCompra" WHEN "IdCorredoraVenta" THEN "IdLibroOrdenesVenta" END AS "IdLibroOrdenes",
								"IdInstrumentoSubyacente",
								"EsFueraRueda",
								"IndicadorDCV",
								'N' AS "EsBilateral",
								"MarcaNegociacion",
								0 AS "TipoCambioCorresponsal",
								0 AS "IdCorredorCorresponsal",
								0 AS "MontoLiquidacionCorresponsal",
								'' AS "IdMonedaLiquidacionCorresponsal",
								CASE "IdTipoInstrumento" WHEN 'SIM' THEN TO_BIGINT("Folio") + 1 || '' ELSE '' end AS "FolioTransaccionPlazo",
								"MejorPrecioCompra",
								"MejorPrecioVenta",
								"NemotecnicoEspecificoBolson"
								FROM #TRANMD TRAN
								INNER JOIN :v_ErroresResumen res on res."IdInterno" = TRAN."IdInterno" AND res."Resultado" != 'RECHAZO';
		
		IF (IS_EMPTY(:v_Transacciones)) THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT 'v_Transacciones',* FROM :v_Transacciones;
		--SIGNAL SALIDA_CONTROLADA;
		CALL "BSCL.Orden.BD.Servicios::CrearTransacciones" ( 
		    p_IdEmpresa					=> :p_IdEmpresa,
		    p_IdUsuario					=> :p_IdUsuario,
		    p_IdModulo					=> 'ORD',
		    p_simularOperacion			=> 'N',
			p_UsaReglasNegocio			=> 'S',
			p_Transacciones				=> :v_Transacciones,
			p_out_Resultado         	=> v_out_Resultado,
			p_out_Errores	         	=> v_out_Errores_CrearTransacciones,
			p_out_InformacionContador	=> v_out_InformacionContador,
			p_out_InformacionDetalle	=> v_out_InformacionDetalle,
			p_out_AdvertenciasContador	=> v_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle	=> v_out_AdvertenciasDetalle,
			p_out_ErroresContador 		=> v_out_ErroresContador,
			p_out_ErroresDetalle 		=> v_out_ErroresDetalle);
		
		--END
		INSERT INTO "BSCL.Orden.BD::ORD.MarketData_CrearTransacciones_Salida_Resultado"
		("IdInterno","IdTransaccion")
		SELECT "IdInterno","IdTransaccion" FROM :v_out_Resultado;
		v_Errores = SELECT * FROM :v_Errores UNION ALL SELECT * FROM :v_out_Errores_CrearTransacciones;
		
		SIGNAL SALIDA_CONTROLADA;
		
	END;
	
END;
GO
