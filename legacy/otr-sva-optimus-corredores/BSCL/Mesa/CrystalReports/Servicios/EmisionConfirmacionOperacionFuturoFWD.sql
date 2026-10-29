SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::EmisionConfirmacionOperacionFuturoFWD";

--
--  Proposito: servicio para consultar Emision de Papeleta FWD
--  Autor: EORTIZ - PRONET-IT
--  Fecha Creacion: /10/2016
--

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::EmisionConfirmacionOperacionFuturoFWD"(
	-- Parametros entrada
	IN p_IdEmpresa		BIGINT,
	IN p_FolioOperacion	BIGINT
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA
AS
BEGIN
	--------------------
	--Variables
	--------------------
	DECLARE v_FolioCartera	BIGINT;
	DECLARE varFECHA 		DATE;
	DECLARE varUF 			DECIMAL(26,8);
	DECLARE varDOLAR 		DECIMAL(26,8);
	DECLARE varIdOperacion  BIGINT;
	DECLARE varExisteOp		INT;
	DECLARE varIDESTADO		VARCHAR(3);
	DECLARE varESTADO		VARCHAR(10);
	
	
	v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;

	--Validar SI Existe Operacion
	SELECT count(1) INTO varExisteOp
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
	WHERE CC."IdTipoProducto"	= 'MEFW'
	  --AND CC."FolioCartera" 	= :p_FolioOperacion
	  AND CC."IdCartera"		= :p_FolioOperacion;

	--Se obtiene FolioCartera, FechaOperacion, Estado, IdOperacion
	SELECT
		 CC."FolioCartera"
		,coalesce(CC."FechaOperacion",'1900-01-01')
		,CC."IdEstado"
		,CC."NombreEstado"
		,coalesce(CC."IdOperacion",'0')
		INTO v_FolioCartera, varFECHA, varIDESTADO, varESTADO, varIdOperacion
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
	WHERE CC."IdTipoProducto"	= 'MEFW' 
	  --AND CC."FolioCartera" 	= :p_FolioOperacion
	  AND CC."IdCartera"		= :p_FolioOperacion;
	
	
	IF (varExisteOp != 0 AND varIDESTADO IN ('VIG','VCD','LIQ','ANT')) THEN
		--DOLAR
		v_TblUS = 
			SELECT 	PTC."TipoCambioMercado" AS "varDOLAR"
			FROM 	"BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
			WHERE 	PTC."Origen" = 'OBSERVADO'
			  		AND PTC."Fecha"	= :varFECHA
			  		AND PTC."IdMoneda"= 'DO';
		
		SELECT COUNT(1) INTO varDOLAR FROM :v_TblUS;
		
		IF varDOLAR > 0 THEN
			SELECT "varDOLAR" INTO varDOLAR FROM :v_TblUS;
		END IF;
		
		
		--UF
		v_TblUF =
			SELECT 	PTC."TipoCambioMercado" AS "varUF"
			FROM 	"BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
			WHERE 	PTC."Origen" = 'OBSERVADO'
		  			AND PTC."Fecha"	= :varFECHA
		  			AND PTC."IdMoneda"= 'UF';
		
		SELECT COUNT(1) INTO varUF FROM :v_TblUF;
		
		IF varUF > 0 THEN
			SELECT "varUF" INTO varUF FROM :v_TblUF;
		END IF;
		
		
		v_Confirmacion =
			SELECT
				 CC."FolioCartera" 				AS "NroOperacion"
				,CC."FechaOperacion"			AS "FechaInicio"
				,TO_VARCHAR(CC."FechaOperacion",'DD-MM-YYYY')	AS "FechaInicioFormato"
				,:varUF 						AS "UF_Inicio"  -- ValorUF dia operacion
				,:varDOLAR  					AS "Obs_Inicio" -- DolarObservado
				,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(:varDOLAR,'dx',4) AS "Obs_InicioStr"
				,CASE
					WHEN CC."IdTipoOperacion" = 'COMFWD' THEN 'COMPRA'
					WHEN CC."IdTipoOperacion" = 'VENFWD' THEN 'VENTA' END AS "TipoOperacion"
				,CC."IdMonedaPrincipal" 		AS "MonedaOperacion"
				,CC."MontoMonedaPrincipal" 		AS "MontoOperacion"
				,CASE
					WHEN CC."Nemotecnico" = 'ARBITR' THEN CC."ValorParidadCierre" 
					ELSE CC."ValorTipoCambioCierre"	END	AS "PrecioFuturo"--depende del instrumento
				,CC."MontoMonedaSecundaria"		AS "MontoFinal"
				,CC."PlazoEnDiasOperacion"		AS "Plazo"
				,CC."FechaVencimientoOperacion" AS "FechaVcto"
				,TO_VARCHAR(CC."FechaVencimientoOperacion",'DD-MM-YYYY')	AS "FechaVctoFormato"
				,PA."NombreParametro"			AS "Modalidad"
				,COALESCE(FPE."NombreFormaDePago",'') AS "NombreFormaDePagoEntregamos"
				,COALESCE(FPR."NombreFormaDePago", '') AS "NombreFormaDePagoRecibimos"
				,CC."IdMonedaSecundaria"		AS "MonedaConversion"
				,PA1."NombreParametro"			AS "Fixing"							
				,CLI."NombreCompleto" 			AS "NombreCliente"
				,CC."NombreUsuario"				AS "Operador"
				,COALESCE(CC."NombreBolsa", '') AS "Bolsa"
				,CC."NombreEmpresa"
				,CC."FechaLiquidacion"			AS "FechaLiquidacion"
				,TO_VARCHAR(FLIQ."FechaLiquidacion",'DD-MM-YYYY') AS "FechaLiquidacionFormato"
				,CC."FechaFixing"				AS "FechaFixing"
				,TO_VARCHAR(CC."FechaFixing",'DD-MM-YYYY')	AS "FechaFixingFormato"
				,CC."IdOperacion"				AS "FolioOperacion"
				,LOGO."LogoCorredora"
				,CASE 
					WHEN CC."Nemotecnico"= 'ARBITR' THEN MFP."ParidadDecimales"
					ELSE MFP."TipoCambioDecimales" END AS "ParidadDecimales"
 				,COALESCE(MFP."CantidadDecimales",4)    AS "CantidadDecimalesPrincipal"
                ,COALESCE(MFS."CantidadDecimales",4)    AS "CantidadDecimalesSecundaria"
                ,COALESCE(MFP."MontoDecimales",4)    AS "MontoDecimalesPrincipal"
                ,COALESCE(MFS."MontoDecimales",4)    AS "MontoDecimalesSecundaria"
                ,COALESCE(MFP."MontoDecimales",4)    AS "MontoDecimales"
                ,COALESCE(MFS."MontoDecimales",4)    AS "MontoDecimalesMonedaSecundaria"
                ,COALESCE(MFP."TipoCambioDecimales",4)    AS "TipoCambioDecimalesPrincipal"
                ,COALESCE(MFS."TipoCambioDecimales",4)    AS "TipoCambioDecimalesSecundaria"
				,''								AS "Observaciones"
				,COALESCE(OPE."IdMonedaCompensacion",'') AS "MonedaCompensacion"
			FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
				ON CLI."IdCliente" = CC."IdCliente"
			--LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR
				--ON DIR."IdPersona" = CLI."IdPersona"
			LEFT JOIN (
				 SELECT DISTINCT CP."IdOperacion",FP."Nombre" AS "NombreFormaDePago", CP."FechaLiquidacionComprobantePagoOperacion",CP."IdEmpresa"
				 FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CP
				 LEFT JOIN "BSCL.Mesa.BD::MES.FormaDePagoMesa" FP ON FP."IdFormaDePago" = cp."IdFormaDePago" AND fp."IdEstado"  = 'ACT'
				 WHERE CP."IdTipoProducto" = 'MEFW' AND CP."RecibePaga" = 'P'
			) AS FPE
				 ON (FPE."IdOperacion" = CC."IdCartera" AND FPE."IdEmpresa" = CC."IdEmpresa")
			LEFT JOIN (
				SELECT DISTINCT CP."IdOperacion",FP."Nombre" AS "NombreFormaDePago", CP."FechaLiquidacionComprobantePagoOperacion",CP."IdEmpresa"
				 FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CP
				 LEFT JOIN "BSCL.Mesa.BD::MES.FormaDePagoMesa" FP ON FP."IdFormaDePago" = cp."IdFormaDePago" AND fp."IdEstado"  = 'ACT'
				 WHERE CP."IdTipoProducto" = 'MEFW' AND CP."RecibePaga" = 'R'
			) AS FPR
			ON (FPR."IdOperacion" = CC."IdCartera" AND FPR."IdEmpresa" = CC."IdEmpresa")
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" US
				ON CC."IdUsuario" = US."IdUsuario"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" CI
				ON CC."IdInstrumento" = CI."IdInstrumento"
			LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" PA
				ON PA."IdParametro" = CC."IdModalidadCumplimiento"
			LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" PA1
				ON PA1."IdParametro" = CC."IdFixing"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MFP
				ON (MFP."IdUsoMonedaFormato" = 'FWD' AND MFP."IdMoneda" = CC."IdMonedaPrincipal" AND MFP."IdTipoOperacion" = CI."Nemotecnico")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MFS		
				ON 	(MFS."IdUsoMonedaFormato" = 'FWD' AND MFS."IdMoneda" = CC."IdMonedaSecundaria" AND MFS."IdTipoOperacion" = CI."Nemotecnico")
			INNER JOIN (
				SELECT
	                 CC."IdCartera"
	                ,CASE WHEN FPE."FechaLiquidacionComprobantePagoOperacion" > FPR."FechaLiquidacionComprobantePagoOperacion" THEN FPE."FechaLiquidacionComprobantePagoOperacion"
	                      WHEN FPE."FechaLiquidacionComprobantePagoOperacion" < FPR."FechaLiquidacionComprobantePagoOperacion" THEN FPR."FechaLiquidacionComprobantePagoOperacion"
	                      ELSE COALESCE(FPE."FechaLiquidacionComprobantePagoOperacion",FPR."FechaLiquidacionComprobantePagoOperacion") END AS "FechaLiquidacion"
	            FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CC
	            LEFT JOIN (
	                    SELECT DISTINCT "IdOperacion","FechaLiquidacionComprobantePagoOperacion","IdEmpresa"
	                    FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
	                    WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'P'
	                ) AS FPE
	                ON (FPE."IdOperacion" = CC."IdCartera" AND FPE."IdEmpresa" = CC."IdEmpresa")
	            LEFT JOIN (
	                    SELECT DISTINCT "IdOperacion","FechaLiquidacionComprobantePagoOperacion","IdEmpresa"
	                    FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
	                    WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'R'
	                ) AS FPR
	               ON (FPR."IdOperacion" = CC."IdCartera" AND FPR."IdEmpresa" = CC."IdEmpresa")
	            WHERE CC."IdTipoProducto"   = 'MEFW'
	              AND CC."IdCartera"        = :p_FolioOperacion
	              AND CC."IdEmpresa"        = :p_IdEmpresa
			) FLIQ 
                ON (CC."IdCartera" = FLIQ."IdCartera")
            LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" AS OPE ON CC."IdCartera" = OPE."IdCartera"
			CROSS JOIN :v_logo LOGO
			WHERE --CC."FolioCartera" 	= :p_FolioOperacion		-- Se reemplaza por la condicion siguiente
				  CC."IdCartera" 	= :p_FolioOperacion
			  AND CC."IdEstado"		IN ('VIG','VCD','LIQ','ANT')
			  AND CC."IdTipoProducto" = 'MEFW'
			  AND CC."IdEmpresa"	= :p_IdEmpresa
			/*GROUP BY CC."FolioCartera",CC."FechaOperacion",CC."IdTipoOperacion",
					 CC."IdMonedaPrincipal",CC."MontoMonedaPrincipal",CC."Nemotecnico",CC."ValorParidadCierre", 
					 CC."ValorTipoCambioCierre",CC."MontoMonedaSecundaria",CC."PlazoEnDiasOperacion",
					 CC."FechaVencimientoOperacion",PA."NombreParametro",FP."NombreMedioDePago",
					 PA1."NombreParametro",CLI."NombreCompleto",CC."IdMonedaSecundaria",
					 CC."NombreUsuario",CC."NombreBolsa",CC."FechaLiquidacion",CC."FechaFixing",CC."IdOperacion"*/
			;
	ELSE
		v_Confirmacion =
			SELECT
				:v_FolioCartera AS "NroOperacion"
				,''			AS "FechaInicio"
				,''			AS "FechaInicioFormato"
				,0			AS "UF_Inicio"  -- ValorUF dia operacion
				,0 			AS "Obs_Inicio" -- DolarObservado
				,'0'		AS "Obs_InicioStr"
				,'' 		AS "TipoOperacion"--formula
				,''			AS "MonedaOperacion"
				,0 			AS "MontoOperacion"
				,0 			AS "PrecioFuturo"
				,0			AS "MontoFinal"
				,0			AS "Plazo"
				,'' 		AS "FechaVcto"
				,''			AS "FechaVctoFormato"
				,0			AS "Modalidad"
				,'' 		AS "NombreFormaDePagoEntregamos"
				,''			AS "NombreFormaDePagoRecibimos"
				,''			AS "MonedaConversion"
				,0			AS "Fixing"
				,'' 		AS "NombreCliente"
				,''			AS "Operador"
				,''			AS "Bolsa"
				,''			AS "NombreEmpresa"
				,''			AS "FechaLiquidacion"
				,''			AS "FechaLiquidacionFormato"
				,''	 		AS "FechaFixing"
				,''	 		AS "FechaFixingFormato"
				,0			AS "FolioOperacion"
				,LOGO."LogoCorredora"
				,4 			AS "ParidadDecimales"
				,4			AS "CantidadDecimalesPrincipal"
				,4   		AS "CantidadDecimalesSecundaria"
				,4   		AS "MontoDecimalesPrincipal"
				,4   		AS "MontoDecimalesSecundaria"
				,4    		AS "MontoDecimales"
				,4          AS "MontoDecimalesMonedaSecundaria"
				,4   		AS "TipoCambioDecimalesPrincipal"
				,4   		AS "TipoCambioDecimalesSecundaria"
				,CASE
					WHEN varExisteOp =	0	THEN 'NO EXISTE OPERACIÓN' 
					WHEN varDolar = 0		THEN 'NO EXISTE DOLAR PARA LA FECHA DE OPERACION'
					WHEN varUF = 0			THEN 'NO EXISTE UF PARA LA FECHA DE OPERACION'
					WHEN varIDESTADO <> 'VIG' THEN 'ESTADO DE OPERACION '||:varESTADO||' ES DISTINTO A VIGENTE'
					ELSE 'ERROR AL OBTENER DATOS DE REPORTE CONFIRMACION' END AS "Observaciones"
				,''			AS "MonedaCompensacion"
			FROM DUMMY
			CROSS JOIN :v_logo LOGO;
	END IF;
	
	SELECT * FROM :v_Confirmacion;
	
 END;