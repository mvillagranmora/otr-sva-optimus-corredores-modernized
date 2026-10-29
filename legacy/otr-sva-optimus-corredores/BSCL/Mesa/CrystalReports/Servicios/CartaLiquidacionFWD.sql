SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::CartaLiquidacionFWD";
GO
-- 
--  Proposito: servicio para generación de reporte Carta de Liquidación de blotter Vencimiento FWD
--  Fecha Creacion: 25/05/2017
--  

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::CartaLiquidacionFWD" (
	IN p_IdEmpresa		BIGINT,
    IN p_IdPais			BIGINT,
    IN p_Fecha			DATE,
    IN p_IdCartera		BIGINT,
    IN p_IdOperacion	BIGINT,
    IN p_FolioOperacion	BIGINT,
    IN p_FechaOperacion	DATE,
    IN p_IdEstado		NVARCHAR(3)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE v_logo TABLE("LogoCorredora" BLOB);
	DECLARE v_countOperaciones 	BIGINT := 0;
	
	DECLARE v_OperacionesSalida TABLE(
		 "Dia"							NVARCHAR(2)
		,"Mes"							NVARCHAR(2)
		,"Agno"							NVARCHAR(4)
		,"FechaSistemaFormato"			NVARCHAR(10)
		,"IdOperacion"					BIGINT
		,"NombreEmpresa"				NVARCHAR(100)
		,"NombreCliente"				NVARCHAR(100)
		,"TipoOperacion"				NVARCHAR(100)
		,"Nemotecnico"					NVARCHAR(100)
		,"FechaOperacion"				DATE
		,"FechaOperacionFormato"		NVARCHAR(10)
		,"IdMonedaPrincipal"			NVARCHAR(3)
		,"IdMonedaSecundaria"			NVARCHAR(3)
		,"NombreCampoMonto"				NVARCHAR(30)
		,"MontoMonedaPrincipal"			DECIMAL(26,8)
		,"MontoMonedaSecundaria"		DECIMAL(26,8)
		,"NombreCampoTCFWD"				NVARCHAR(30)
		,"ValorCampoTCFWD"				DECIMAL(26,8)
		,"TCForward"					DECIMAL(26,8)
		,"Spread"						DECIMAL(26,8)
		,"PrecioReferenciaDO"			DECIMAL(26,8)
		,"PrecioReferencia"				DECIMAL(26,8)
		,"ModalidadLiquidacion"			NVARCHAR(100)
		,"NombreMedioDePagoEntregamos"	NVARCHAR(100)
		,"NombreMedioDePagoRecibimos"	NVARCHAR(100)
		,"ModalidadLiquidacionValor"	NVARCHAR(100)
		,"MontoRecibimos"				DECIMAL(26,8)
		,"MontoPagamos"					DECIMAL(26,8)
		,"IdMonedaLiquidacion"			NVARCHAR(3)
		,"MontoSaldo1"					DECIMAL(26,8)
		,"AFavor1"						NVARCHAR(100)
		,"IdMoneda1"					NVARCHAR(3)
		,"MontoSaldo2"					DECIMAL(26,8)
		,"AFavor2"						NVARCHAR(100)
		,"IdMoneda2"					NVARCHAR(3)
		,"FechaRecibimos"				NVARCHAR(10)
		,"FechaEntregamos"				NVARCHAR(10)
		,"FechaVencimientoFormato"		NVARCHAR(10)
		,"MontoDecimalesPrincipal"		BIGINT
		,"MontoDecimalesSecundaria"		BIGINT
		,"MontoDecimalesLiquidacion"	BIGINT
		,"MontoDecimalesMoneda1"		BIGINT
		,"MontoDecimalesMoneda2"		BIGINT
		,"MontoDecimalesDO"			BIGINT
		,"MontoDecimalesUF"				BIGINT
		,"TCDecimalesUF"				BIGINT
		,"TipoVencimiento"				NVARCHAR(20)
		,"LogoCorredora"				BLOB
		,"MensajeError"					NVARCHAR(100)
		,"MonedaCompensacion"			NVARCHAR(3)
	);
	
	v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
	
	v_FormaDePagoEntregamos =
		SELECT
			 "IdOperacion"
			,"IdMoneda"
			,STRING_AGG("NombreFormaDePago",', ') AS "NombreFormaDePago"
		FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
		WHERE "IdOperacion" = :p_IdCartera
		  AND "IdTipoProducto" = 'MEFW'
		  AND "RecibePaga" = 'P'
		  AND "IdEmpresa" = :p_IdEmpresa
		GROUP BY "IdOperacion", "IdMoneda";
	
	--SELECT 'v_FormaDePagoEntregamos', * FROM :v_FormaDePagoEntregamos;
	
	v_FormaDePagoRecibimos =
		SELECT
			 "IdOperacion"
			,"IdMoneda"
			,STRING_AGG("NombreFormaDePago",', ') AS "NombreFormaDePago"
		FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
		WHERE "IdOperacion" = :p_IdCartera
		  AND "IdTipoProducto" = 'MEFW'
		  AND "RecibePaga" = 'R'
		  AND "IdEmpresa" = :p_IdEmpresa
		GROUP BY "IdOperacion", "IdMoneda";
	
	--SELECT 'v_FormaDePagoRecibimos', * FROM :v_FormaDePagoRecibimos;
	
	
	v_OperacionesSalida = 
		SELECT
			 TO_VARCHAR(p_Fecha,'DD')										AS "Dia"
			,TO_VARCHAR(p_Fecha,'MM')										AS "Mes"
			,TO_VARCHAR(p_Fecha,'YYYY')										AS "Agno"
			--,TO_VARCHAR(p_Fecha,'DD-MM-YYYY')								AS "FechaSistemaFormato"
			,TO_VARCHAR(FWD."FechaFiltroGrillaVcto",'DD-MM-YYYY')			AS "FechaSistemaFormato"
			,:p_IdOperacion													AS "IdOperacion"
			,FWD."NombreEmpresa"
			,FWD."NombreCliente"
			,FWD."TipoOperacion"
			,FWD."Nemotecnico"
			,FWD."FechaOperacion"
			,TO_VARCHAR(FWD."FechaOperacion",'DD-MM-YYYY')					AS "FechaOperacionFormato"
			,FWD."IdMonedaPrincipal"
			,FWD."IdMonedaSecundaria"
			,CASE
				WHEN FWD."IdTipoOperacion" = 'COMFWD' THEN 'Monto Compra'
				WHEN FWD."IdTipoOperacion" = 'VENFWD' THEN 'Monto Venta'
				ELSE 'Monto' END											AS "NombreCampoMonto"
			,FWD."MontoMonedaPrincipal"
			,FWD."MontoMonedaSecundaria"
			,CASE
				WHEN FWD."Nemotecnico" = 'ARBITR' THEN 'PARIDAD CIERRE' 
				ELSE 'TC CIERRE' END										AS "NombreCampoTCFWD"
			,CASE
				WHEN FWD."Nemotecnico" = 'ARBITR' THEN FWD."ParidadCierre" 
				ELSE FWD."TipoCambioCierre" END								AS "ValorCampoTCFWD"
			,FWD."TasaForward"												AS "TCForward"
			,0																AS "Spread"
			,PARDO."TipoCambioMercado"										AS "PrecioReferenciaDO"
			,CASE
				WHEN FWD."PrecioVencimiento" <> 0 THEN FWD."PrecioVencimiento"
				WHEN FWD."ParidadVencimiento" <> 0 THEN FWD."ParidadVencimiento"
				ELSE 0 END													AS "PrecioReferencia"
			,FWD."ModalidadLiquidacion"
			--,FWD."NombreMedioDePagoEntregamos"
			--,COALESCE(FPE."NombreFormaDePago",'')							AS "NombreMedioDePagoEntregamos"
			,CASE
				WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' THEN
					CASE WHEN FWD."IdMonedaPrincipal" = FPE."IdMoneda" THEN COALESCE(FPE."NombreFormaDePago",'')
						 WHEN FWD."IdMonedaPrincipal" = FPR."IdMoneda" THEN COALESCE(FPR."NombreFormaDePago",'')
						 ELSE '' END
				WHEN FWD."IdMonedaPrincipal" = FWD."IdMonedaLiquidacion" OR (FWD."Nemotecnico" = 'ARBITR' AND FWD."IdMonedaSecundaria" = FWD."IdMonedaLiquidacion") THEN
					CASE WHEN FWD."IdMonedaPrincipal" = FPE."IdMoneda" THEN COALESCE(FPE."NombreFormaDePago",'')
						 WHEN FWD."IdMonedaPrincipal" = FPR."IdMoneda" THEN COALESCE(FPR."NombreFormaDePago",'')
						 ELSE '' END
				ELSE '' END													AS "NombreMedioDePagoEntregamos"
			--,FWD."NombreMedioDePagoRecibimos"
			--,COALESCE(FPR."NombreFormaDePago",'')							AS "NombreMedioDePagoRecibimos"
			,CASE
				WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' THEN
					CASE WHEN FWD."IdMonedaSecundaria" = FPE."IdMoneda" THEN COALESCE(FPE."NombreFormaDePago",'')
						 WHEN FWD."IdMonedaSecundaria" = FPR."IdMoneda" THEN COALESCE(FPR."NombreFormaDePago",'')
						 ELSE '' END
				WHEN FWD."IdMonedaLiquidacion" = 'CLP' THEN
					CASE WHEN FWD."IdMonedaPrincipal" = FPE."IdMoneda" THEN COALESCE(FPE."NombreFormaDePago",'')
						 WHEN FWD."IdMonedaPrincipal" = FPR."IdMoneda" THEN COALESCE(FPR."NombreFormaDePago",'')
						 ELSE '' END
				ELSE '' END													AS "NombreMedioDePagoRecibimos"
			,FWD."ModalidadLiquidacionValor"
			,FWD."MontoRecibimos"
			,FWD."MontoPagamos"
			,FWD."IdMonedaLiquidacion"
			,CASE
				WHEN FWD."ModalidadLiquidacionValor" = 'COMPENSADA' THEN
					CASE
						WHEN FWD."IdMonedaLiquidacion" = 'CLP' THEN
						CASE 
							WHEN FWD."IdEstado" = 'LIQ' AND FWD."MontoRecibimos" <> 0 THEN FWD."MontoRecibimos"
							WHEN FWD."IdEstado" = 'LIQ' AND FWD."MontoPagamos"   <> 0 THEN FWD."MontoPagamos"
							WHEN FWD."IdEstado" IN ('VCD','ANT') AND FWD."MontoRecibimos" <> 0 THEN FWD."MontoRecibimos"
							WHEN FWD."IdEstado" IN ('VCD','ANT') AND FWD."MontoPagamos" <> 0 THEN FWD."MontoPagamos"
						END 
						ELSE 
							CASE
								WHEN FWD."IdEstado" = 'LIQ' THEN 
									CASE 
										WHEN FWD."MontoRecibimos" <> 0 THEN FWD."MontoRecibimos" 
										WHEN FWD."MontoPagamos"   <> 0 THEN FWD."MontoPagamos"
									END
								WHEN FWD."IdEstado" IN ('VCD','ANT')  THEN 
									CASE 
										WHEN FWD."PrecioVencimiento" <> 0 AND FWD."MontoRecibimos" <> 0 THEN FWD."MontoRecibimos" / FWD."PrecioVencimiento"
										WHEN FWD."PrecioVencimiento" <> 0 AND FWD."MontoPagamos"   <> 0 THEN FWD."MontoPagamos" / FWD."PrecioVencimiento"
										WHEN FWD."ParidadVencimiento" <> 0 AND FWD."MontoRecibimos" <> 0 THEN FWD."MontoRecibimos" / FWD."ParidadVencimiento"
										WHEN FWD."ParidadVencimiento" <> 0 AND FWD."MontoPagamos"   <> 0 THEN FWD."MontoPagamos" / FWD."ParidadVencimiento"
									END
							END
						END
				WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'COMFWD' THEN FWD."MontoRecibimos"
				WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'VENFWD' THEN FWD."MontoPagamos"
				ELSE 0 END													AS "MontoSaldo1"
			,CASE
				WHEN FWD."ModalidadLiquidacionValor" = 'COMPENSADA' AND FWD."MontoRecibimos" <> 0 THEN FWD."NombreEmpresa"
				WHEN FWD."ModalidadLiquidacionValor" = 'COMPENSADA' AND FWD."MontoPagamos"   <> 0 THEN FWD."NombreCliente"
				WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'COMFWD' THEN FWD."NombreEmpresa"
				WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'VENFWD' THEN FWD."NombreCliente"
				ELSE '' END													AS "AFavor1"
			,CASE
				WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' THEN FWD."IdMonedaPrincipal"
				ELSE FWD."IdMonedaLiquidacion" END							AS "IdMoneda1"
			,CASE
				WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'COMFWD' THEN FWD."MontoPagamos"
				WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'VENFWD' THEN FWD."MontoRecibimos"
				ELSE 0 END													AS "MontoSaldo2"
			,CASE
				WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'COMFWD' THEN FWD."NombreCliente"
				WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'VENFWD' THEN FWD."NombreEmpresa"
				ELSE '' END													AS "AFavor2"
			,CASE
				WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' THEN FWD."IdMonedaSecundaria"
				ELSE FWD."IdMonedaLiquidacion" END							AS "IdMoneda2"
			,CASE
				WHEN FWD."MontoRecibimos" <> 0 THEN TO_VARCHAR(FWD."FechaFiltroGrillaVcto",'DD-MM-YYYY')
				ELSE '' END													AS "FechaRecibimos"
			,CASE
				WHEN FWD."MontoPagamos" <> 0 THEN TO_VARCHAR(FWD."FechaFiltroGrillaVcto",'DD-MM-YYYY')
				ELSE '' END													AS "FechaEntregamos"
			,TO_VARCHAR(FWD."FechaFiltroGrillaVcto",'DD-MM-YYYY')			AS "FechaVencimientoFormato"
			,COALESCE(MONPRI."MontoDecimales",4)							AS "MontoDecimalesPrincipal"
			,COALESCE(MONSEC."MontoDecimales",4)							AS "MontoDecimalesSecundaria"
			,COALESCE(MONLIQ."MontoDecimales",4)							AS "MontoDecimalesLiquidacion"
			,COALESCE(MONLIQ1."MontoDecimales",4)							AS "MontoDecimalesMoneda1"
			,COALESCE(MONLIQ2."MontoDecimales",4)							AS "MontoDecimalesMoneda2"
			,COALESCE(MONDO."MontoDecimales",4)								AS "MontoDecimalesDO"
			,COALESCE(MONUF."MontoDecimales",4)								AS "MontoDecimalesUF"
			,COALESCE(MONUF."TipoCambioDecimales",4)						AS "TCDecimalesUF"
			,FWD."TipoVencimiento"											AS "TipoVencimiento"
			,LOGO."LogoCorredora"											
			,'DATOS_OK'														AS "MensajeError"
			,COALESCE(FWD."IdMonedaCompensacion",'')						AS "MonedaCompensacion"
		FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDVencimiento" FWD
		LEFT JOIN :v_FormaDePagoEntregamos FPE
			ON (FWD."IdCartera" = FPE."IdOperacion")
		LEFT JOIN :v_FormaDePagoRecibimos FPR
			ON (FWD."IdCartera" = FPR."IdOperacion")
		LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARDO
			ON (PARDO."IdMoneda" = 'USD' AND PARDO."Fecha" = FWD."FechaVencimiento")
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONPRI
			ON (MONPRI."IdUsoMonedaFormato" = 'FWD' AND MONPRI."IdMoneda" = FWD."IdMonedaPrincipal") AND MONPRI."IdTipoOperacion" = ''
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONSEC
			ON (MONSEC."IdUsoMonedaFormato" = 'FWD' AND MONSEC."IdMoneda" = FWD."IdMonedaSecundaria") AND MONSEC."IdTipoOperacion" = ''
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONLIQ
			ON (MONLIQ."IdUsoMonedaFormato" = 'FWD' AND MONLIQ."IdMoneda" = FWD."IdMonedaLiquidacion") AND MONLIQ."IdTipoOperacion" = ''
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONLIQ1
			ON (MONLIQ1."IdUsoMonedaFormato" = 'FWD' AND 
				MONLIQ1."IdMoneda" = CASE WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' THEN FWD."IdMonedaPrincipal"
										  ELSE FWD."IdMonedaLiquidacion" END) AND MONLIQ1."IdTipoOperacion" = ''
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONLIQ2
			ON (MONLIQ2."IdUsoMonedaFormato" = 'FWD' AND 
				MONLIQ2."IdMoneda" = CASE WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' THEN FWD."IdMonedaSecundaria"
										  ELSE FWD."IdMonedaLiquidacion" END) AND MONLIQ2."IdTipoOperacion" = ''
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONDO
			ON (MONDO."IdUsoMonedaFormato" = 'FWD' AND MONDO."IdMoneda" = 'DO') AND MONDO."IdTipoOperacion" = ''
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONUF
			ON (MONUF."IdUsoMonedaFormato" = 'FWD' AND MONUF."IdMoneda" = 'UF') AND MONUF."IdTipoOperacion" = ''
		LEFT JOIN :v_logo LOGO
			ON (LOGO."LogoCorredora" IS NOT NULL)
		WHERE FWD."IdCartera"		= :p_IdCartera
		  AND FWD."IdOperacion"		= :p_IdOperacion
		  AND FWD."FolioOperacion"	= :p_FolioOperacion
		  AND FWD."FechaOperacion"	= TO_DATE(:p_FechaOperacion,'YYYY-MM-DD')
		  AND FWD."IdEstado"		= :p_IdEstado
		  AND FWD."IdEstado" IN ('VCD','LIQ');
	
	
	SELECT COUNT(*) INTO v_countOperaciones FROM :v_OperacionesSalida;
	
	IF (:v_countOperaciones = 0) THEN
		v_OperacionesSalida =
			SELECT 
				 ''					AS "Dia"
				,''					AS "Mes"
				,''					AS "Agno"
				,''					AS "FechaSistemaFormato"
				,:p_IdOperacion				AS "IdOperacion"
				,''					AS "NombreEmpresa"
				,''					AS "NombreCliente"
				,''					AS "TipoOperacion"
				,''					AS "Nemotecnico"
				,''					AS "FechaOperacion"
				,''					AS "FechaOperacionFormato"
				,''					AS "IdMonedaPrincipal"
				,''					AS "IdMonedaSecundaria"
				,''					AS "NombreCampoMonto"
				,0					AS "MontoMonedaPrincipal"
				,0					AS "MontoMonedaSecundaria"
				,''					AS "NombreCampoTCFWD"
				,0					AS "ValorCampoTCFWD"
				,0					AS "TCForward"
				,0					AS "Spread"
				,0					AS "PrecioReferenciaDO"
				,0					AS "PrecioReferencia"
				,0					AS "ModalidadLiquidacion"
				,''					AS "NombreMedioDePagoEntregamos"
				,''					AS "NombreMedioDePagoRecibimos"
				,''					AS "ModalidadLiquidacionValor"
				,0					AS "MontoRecibimos"
				,0					AS "MontoPagamos"
				,''					AS "IdMonedaLiquidacion"
				,0					AS "MontoSaldo1"
				,''					AS "AFavor1"
				,''					AS "IdMoneda1"
				,0					AS "MontoSaldo2"
				,''					AS "AFavor2"
				,''					AS "IdMoneda2"
				,''					AS "FechaRecibimos"
				,''					AS "FechaEntregamos"
				,''					AS "FechaVencimientoFormato"
				,4					AS "MontoDecimalesPrincipal"
				,4					AS "MontoDecimalesSecundaria"
				,4					AS "MontoDecimalesLiquidacion"
				,4					AS "MontoDecimalesMoneda1"
				,4					AS "MontoDecimalesMoneda2"
				,4					AS "MontoDecimalesDO"
				,4					AS "MontoDecimalesUF"
				,8					AS "TCDecimalesUF"
				,'' 					AS "TipoVencimiento"
				,LOGO."LogoCorredora"			AS "LogoCorredora"
				,'ERROR :  No se encontraron datos.' 	AS "MensajeError"
				,''					AS "MonedaCompensacion"
			FROM DUMMY
			INNER JOIN :v_logo LOGO
				ON (LOGO."LogoCorredora" IS NOT NULL);
	END IF;
	
	--Salida del SP
	SELECT * FROM :v_OperacionesSalida;
	
END;
GO