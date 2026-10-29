SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::EmisionCarteraFWDBasilea";

--
--  Proposito: servicio para consultar Emision Cartera FWD Basilea
--  Autor: EORTIZ - PRONET-IT
--  Fecha Creacion: /10/2016
--

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::EmisionCarteraFWDBasilea"(
	-- Parametros entrada
	IN p_IdEmpresa				BIGINT,
	IN p_FechaOperacion			DATE,
	IN p_Usuario				VARCHAR(100)
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
	DECLARE v_Salida TABLE (	"FolioOperacion"			BIGINT,
								"FolioCartera"			    BIGINT,
								"NombreInstrumento"			NVARCHAR(100),
								"RutCliente"				NVARCHAR(40),
								"NombreCliente"				NVARCHAR(100),
								"FechaInicio"				DATE,
								"FechaInicioFormato"		NVARCHAR(10),
								"FechaVencimiento"			DATE,
								"FechaVencimientoFormato"	NVARCHAR(10),
								"PlazoOperacion"			BIGINT,
								"IdMonedaPrincipal"			NVARCHAR(3),
								"IdMonedaSecundaria"		NVARCHAR(3),
								"MontoNocional"				DECIMAL(26,8),
								"MontoNocionalCLP"	    	DECIMAL(26,8),
								"TipoCambioFWD"				DECIMAL(26,10),
								"PlazoRem"					BIGINT,
								"MontoValorRazonableActivo"	DECIMAL(26,8),
								"MontoValorRazonablePasivo"	DECIMAL(26,8),
								"ValorRazonable"			DECIMAL(26,8),
								"TasaValorRazonableActivo"	DECIMAL(26,8),
								"TasaValorRazonablePasivo"	DECIMAL(26,8),
								"TipoOperacion"				NVARCHAR(6),
								"Factor"					DECIMAL(26,8),
								"FechaConsulta"				DATE,
								"FechaConsultaFormato"		NVARCHAR(10),
								"FechaImpresion"			NVARCHAR(10),
								"HoraImpresion"				NVARCHAR(10),
								"DO"						DECIMAL(26,8),
								"UF"						DECIMAL(26,8),
								"PrecioSpotCompra"			DECIMAL(26,8),
								"PrecioSpotVenta"			DECIMAL(26,8),
								"PrecioSpotPromedio"		DECIMAL(26,8),
								"USD"						DECIMAL(26,8),
								"Usuario"					NVARCHAR(100),
								"MontoDecimalesPrincipal"	INTEGER,
								"MontoDecimalesSecundaria"	INTEGER,
								"MontoDecimalesDO"			INTEGER,
								"MontoDecimalesUF"			INTEGER,
								"TCDecimalesUF"				INTEGER,
								"MontoDecimalesUSD"			INTEGER,
								"TasaDecimales"				INTEGER,
								"LogoCorredora"				BLOB,
								"MensajeError"				NVARCHAR(100),
								"TipoCambioPar"             NVARCHAR(100),
								"TipoCambioVal"             DECIMAL(26,8),
								"CodigoPortafolio"          NVARCHAR(100),
								"ModalidadLiquidacion"		NVARCHAR(100),
								"MonedaCompensacion"		NVARCHAR(3),
								"MontoMatriz"				DECIMAL(26,8),
								"EquivalenteCredito"		DECIMAL(26,8)
								);
	
	DECLARE varUF 			BIGINT;
	DECLARE varDOLAR 		BIGINT;
	DECLARE varExisteDolar 	INT;
	DECLARE varExisteUf		INT;
	DECLARE v_count			BIGINT;
	
	DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
	DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
	DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
	DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
	DECLARE v_FechaHoraUTC   		NVARCHAR(40);
	DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
	DECLARE v_FechaSistema			NVARCHAR(10);
	
	-- Se obtiene la misma FechaHoraSistema que se muestra en la UI
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> p_IdEmpresa,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
	);
	
	
	v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
	
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		IF(p_FechaOperacion = v_FechaSistema) THEN
			v_Cartera =
			SELECT
				 MF."IdOperacion" 								AS "FolioOperacion"
				,CAR."IdCartera"								AS "FolioCartera"
				,MF."NombreInstrumento"	||' '|| (CASE WHEN MF."IdMonedaPrincipal" = MF."IdMonedaSecundaria" THEN MF."IdMonedaPrincipal"
							  ELSE MF."IdMonedaPrincipal" || '/' || MF."IdMonedaSecundaria" END)  AS "NombreInstrumento"
				,CASE WHEN COALESCE(MF."RutCliente",'') <> '' AND MF."TipoDocumentoIdentidad" = 'R' THEN
					"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(MF."RutCliente",'-'),'i',0)||
					'-'||SUBSTR_AFTER(MF."RutCliente",'-')
				  ELSE COALESCE(MF."RutCliente",'-') END		AS "RutCliente"
				,MF."NombreCliente"								AS "NombreCliente"
				,MF."FechaOperacion"							AS "FechaInicio"
				,TO_VARCHAR(MF."FechaOperacion",'DD-MM-YYYY')	AS "FechaInicioFormato"
				,MF."FechaVencimiento"							AS "FechaVencimiento"
				,TO_VARCHAR(MF."FechaVencimiento",'DD-MM-YYYY')	AS "FechaVencimientoFormato"
				,MF."Plazo"										AS "PlazoOperacion"
				,MF."IdMonedaPrincipal"							AS "IdMonedaPrincipal"
				,MF."IdMonedaSecundaria"						AS "IdMonedaSecundaria"
				,MF."MontoMonedaPrincipal"						AS "MontoNocional"
				,(CASE WHEN MF."IdMonedaPrincipal"='CLP' THEN MF."MontoMonedaPrincipal" 
					   WHEN MF."IdMonedaSecundaria"='CLP' THEN MF."MontoMonedaSecundaria" 
					   WHEN (MF."IdMonedaPrincipal"!='CLP' AND MF."IdMonedaSecundaria"!='CLP') AND MF."Nemotecnico" = 'ARBITR' THEN COALESCE((MF."MontoMonedaPrincipal"	* MF."ParidadCierre") * PTCUFOPE."TipoCambioMercado", 0) 
					   WHEN (MF."IdMonedaPrincipal"!='CLP' AND MF."IdMonedaSecundaria"!='CLP') AND MF."Nemotecnico" <> 'ARBITR' THEN COALESCE((MF."MontoMonedaPrincipal"	* MF."TipoCambioCierre") * PTCUFOPE."TipoCambioMercado", 0) END) AS "MontoNocionalCLP"
				,CASE WHEN MF."Nemotecnico" = 'ARBITR' THEN MF."ParidadCierre" ELSE MF."TipoCambioCierre" END AS "TipoCambioFWD"
				,MF."PlazoResidual"								AS "PlazoRem"
				,MF."MontoValorRazonableActivo"					AS "MontoValorRazonableActivo"
				,MF."MontoValorRazonablePasivo"			 		AS "MontoValorRazonablePasivo"
				,MF."MontoValorRazonableActivo" - "MontoValorRazonablePasivo" AS "ValorRazonable"
				,MF."TasaValorRazonableActivo"					AS "TasaValorRazonableActivo"
				,MF."TasaValorRazonablePasivo" 			 		AS "TasaValorRazonablePasivo"
				,CASE WHEN MF."IdTipoOperacion" = 'COMFWD' THEN 'COMPRA' ELSE 'VENTA' END AS "TipoOperacion"		
				,(	SELECT Canasta."Factor"
					FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" AS Canasta
					INNER JOIN (
							SELECT CCM."IdCanasta", MIN(CFC."Hasta") AS "MIN", MAX(CFC_MAYOR."Hasta") AS "MAX"
							FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarCanastaMoneda" CCM
							LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" CFC
								ON (CFC."IdEmpresa" = CCM."IdEmpresa" AND
									CFC."IdCanasta" = CCM."IdCanasta" AND 
									CFC."Hasta" >= MF."PlazoResidual")
							LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" CFC_MAYOR
								ON (CFC_MAYOR."IdEmpresa" = CCM."IdEmpresa" AND
									CFC_MAYOR."IdCanasta" = CCM."IdCanasta")
							WHERE CCM."IdEmpresa"= :p_IdEmpresa 
							  AND CCM."IdMoneda" = MF."IdMonedaPrincipal"
							GROUP BY CCM."IdCanasta"
							) AS FactorCorrespondiente
						ON (COALESCE(FactorCorrespondiente."MIN",FactorCorrespondiente."MAX") = Canasta."Hasta" AND 
							Canasta."IdEmpresa"	= :p_IdEmpresa AND 
							Canasta."IdCanasta"	= FactorCorrespondiente."IdCanasta")
					)											AS "Factor"
				,:p_FechaOperacion								AS "FechaConsulta"
				,TO_VARCHAR(:p_FechaOperacion,'DD-MM-YYYY')			AS "FechaConsultaFormato"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaImpresion"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraImpresion"
				,COALESCE(PTCDO."TipoCambioMercado",0)			AS "DO"	 	
				,COALESCE(PTCUF."TipoCambioMercado",0)			AS "UF"
				,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."PrecioSpotCompra",0)	ELSE COALESCE(PTCDO."PrecioSpotCompra",0) 	END AS "PrecioSpotCompra"
				,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."PrecioSpotVenta",0) 	ELSE COALESCE(PTCDO."PrecioSpotVenta",0) 	END AS "PrecioSpotVenta"
				,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."PrecioSpotPromedio",0) 	ELSE COALESCE(PTCDO."PrecioSpotPromedio",0) END AS "PrecioSpotPromedio"
			    ,COALESCE(PTCUSD."TipoCambioMercado",0)			AS "USD"
				,USU."CodigoUsuario"							AS "Usuario"
				,COALESCE(MONPRI."MontoDecimales",4)			AS "MontoDecimalesPrincipal"
				,COALESCE(MONSEC."MontoDecimales",4)			AS "MontoDecimalesSecundaria"
				,COALESCE(MONDO."MontoDecimales",4)				AS "MontoDecimalesDO"
				,COALESCE(MONUF."MontoDecimales",4)				AS "MontoDecimalesUF"
				,COALESCE(MONUF."TipoCambioDecimales",4)		AS "TCDecimalesUF"
				,COALESCE(MONUSD."MontoDecimales",4)			AS "MontoDecimalesUSD"
				,COALESCE(MONPRI."TasaDecimales",8)				AS "TasaDecimales"
				,LOGO."LogoCorredora"
				,''												AS "MensajeError"
				,CASE WHEN CONF."Valor" = 'S' THEN 'USD' ELSE 'DO' END AS "TipoCambioPar"	 
				,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."TipoCambioMercado",0) ELSE COALESCE(PTCDO."TipoCambioMercado",0) END AS "TipoCambioVal"
				,MF."CodigoPropositoPortafolio"                 AS "CodigoPortafolio"
				,PARAM."NombreParametro" AS "ModalidadLiquidacion"
				,COALESCE(MF."IdMonedaCompensacion",'') AS "MonedaCompensacion"
			FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" MF
			LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON MF."IdCartera"=CAR."IdCartera"
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU
				ON (USU."IdUsuario" = :p_Usuario)
			LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCDO
				ON (PTCDO."Origen"	= 'OBSERVADO' AND 
					PTCDO."Fecha"	= :p_FechaOperacion AND 
					PTCDO."IdMoneda"= 'DO')
			LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCUF
				ON (PTCUF."Origen"	= 'OBSERVADO' AND 
					PTCUF."Fecha"	= :p_FechaOperacion AND 
					PTCUF."IdMoneda"= 'UF')
			LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCUSD
				ON (PTCUSD."Origen"	= 'OBSERVADO' AND 
					PTCUSD."Fecha"	= :p_FechaOperacion AND 
					PTCUSD."IdMoneda"= 'USD')
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONPRI
				ON (MONPRI."IdUsoMonedaFormato" = 'FWD' AND MONPRI."IdMoneda" = MF."IdMonedaPrincipal" AND  MONPRI."IdTipoInstrumento" = MF."IdTipoInstrumento")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONSEC
				ON (MONSEC."IdUsoMonedaFormato" = 'FWD' AND MONSEC."IdMoneda" = MF."IdMonedaSecundaria" AND  MONSEC."IdTipoInstrumento" = MF."IdTipoInstrumento")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONDO
				ON (MONDO."IdUsoMonedaFormato" = 'FWD' AND MONDO."IdMoneda" = 'DO' AND  MONDO."IdTipoInstrumento" = MF."IdTipoInstrumento")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONUF
				ON (MONUF."IdUsoMonedaFormato" = 'FWD' AND MONUF."IdMoneda" = 'UF' AND  MONUF."IdTipoInstrumento" = MF."IdTipoInstrumento")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONUSD
				ON (MONUSD."IdUsoMonedaFormato" = 'FWD' AND MONUSD."IdMoneda" = 'USD' AND  MONUSD."IdTipoInstrumento" = MF."IdTipoInstrumento")
			LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONF
				ON (CONF."CodigoParametro" = 'TIPO_CAMBIO_VALORIZA_BASILEA')
			LEFT JOIN :v_logo LOGO
				ON (LOGO."LogoCorredora" IS NOT NULL)
			LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCUFOPE
				ON (PTCUFOPE."Origen"	= 'OBSERVADO' AND 
					PTCUFOPE."Fecha"	= MF."FechaOperacion" AND 
					PTCUFOPE."IdMoneda"= 'UF')
			INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PARAM 
				ON (PARAM."IdParametro" = CAR."IdModalidadCumplimiento" AND PARAM."NombreTipoParametro" = 'ModalidadLiquidacion' AND PARAM."IdEmpresa" = CAR."IdEmpresa")
			WHERE MF."Estado"='VIG'
			  AND MF."FechaOperacion" <= :p_FechaOperacion
			  AND MF."FechaVencimiento" > :p_FechaOperacion;
		ELSE
			v_Cartera =
			SELECT
				 MF."IdOperacion" 								AS "FolioOperacion"
				,CAR."IdCartera"								AS "FolioCartera"
				,MF."NombreInstrumento"	||' '|| (CASE WHEN MF."IdMonedaPrincipal" = MF."IdMonedaSecundaria" THEN MF."IdMonedaPrincipal"
							  ELSE MF."IdMonedaPrincipal" || '/' || MF."IdMonedaSecundaria" END)  AS "NombreInstrumento"
				,CASE WHEN COALESCE(MF."RutCliente",'') <> '' AND MF."TipoDocumentoIdentidad" = 'R' THEN
					"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(MF."RutCliente",'-'),'i',0)||
					'-'||SUBSTR_AFTER(MF."RutCliente",'-')
				  ELSE COALESCE(MF."RutCliente",'-') END		AS "RutCliente"
				,MF."NombreCliente"								AS "NombreCliente"
				,MF."FechaOperacion"							AS "FechaInicio"
				,TO_VARCHAR(MF."FechaOperacion",'DD-MM-YYYY')	AS "FechaInicioFormato"
				,MF."FechaVencimiento"							AS "FechaVencimiento"
				,TO_VARCHAR(MF."FechaVencimiento",'DD-MM-YYYY')	AS "FechaVencimientoFormato"
				,MF."Plazo"										AS "PlazoOperacion"
				,MF."IdMonedaPrincipal"							AS "IdMonedaPrincipal"
				,MF."IdMonedaSecundaria"						AS "IdMonedaSecundaria"
				,MF."MontoMonedaPrincipal"						AS "MontoNocional"
				,(CASE WHEN MF."IdMonedaPrincipal"='CLP' THEN MF."MontoMonedaPrincipal" 
					   WHEN MF."IdMonedaSecundaria"='CLP' THEN MF."MontoMonedaSecundaria" 
					   WHEN (MF."IdMonedaPrincipal"!='CLP' AND MF."IdMonedaSecundaria"!='CLP') AND MF."Nemotecnico" = 'ARBITR' THEN COALESCE((MF."MontoMonedaPrincipal"	* MF."ParidadCierre") * PTCUFOPE."TipoCambioMercado", 0) 
					   WHEN (MF."IdMonedaPrincipal"!='CLP' AND MF."IdMonedaSecundaria"!='CLP') AND MF."Nemotecnico" <> 'ARBITR' THEN COALESCE((MF."MontoMonedaPrincipal"	* MF."TipoCambioCierre") * PTCUFOPE."TipoCambioMercado", 0) END) AS "MontoNocionalCLP"				,CASE WHEN MF."Nemotecnico" = 'ARBITR' THEN MF."ParidadCierre" ELSE MF."TipoCambioCierre" END AS "TipoCambioFWD"
				,DAYS_BETWEEN (
                    TO_SECONDDATE(:p_FechaOperacion,'YYYY-MM-DD'),
                    TO_SECONDDATE((CASE WHEN MF."IdEstado" = 'ANT' OR 
                                            (MF."IdEstado" = 'VCD' AND MF."EGRIdTipoOperacion" = 'ANTFWD') THEN MF."FechaAnticipo" 
                                        ELSE MF."FechaVencimiento" END), 'YYYY-MM-DD')
                    ) 											AS "PlazoRem"
				,MF."MontoValorRazonableActivo"					AS "MontoValorRazonableActivo"
				,MF."MontoValorRazonablePasivo"			 		AS "MontoValorRazonablePasivo"
				,MF."MontoValorRazonableActivo" - "MontoValorRazonablePasivo" AS "ValorRazonable"
				,MF."TasaValorRazonableActivo"					AS "TasaValorRazonableActivo"
				,MF."TasaValorRazonablePasivo" 			 		AS "TasaValorRazonablePasivo"
				,CASE WHEN MF."IdTipoOperacion" = 'COMFWD' THEN 'COMPRA' ELSE 'VENTA' END AS "TipoOperacion"		
				,(	SELECT Canasta."Factor"
					FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" AS Canasta
					INNER JOIN (
							SELECT CCM."IdCanasta", MIN(CFC."Hasta") AS "MIN", MAX(CFC_MAYOR."Hasta") AS "MAX"
							FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarCanastaMoneda" CCM
							LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" CFC
								ON (CFC."IdEmpresa" = CCM."IdEmpresa" AND
									CFC."IdCanasta" = CCM."IdCanasta" AND 
									CFC."Hasta" >= MF."PlazoResidual")
							LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" CFC_MAYOR
								ON (CFC_MAYOR."IdEmpresa" = CCM."IdEmpresa" AND
									CFC_MAYOR."IdCanasta" = CCM."IdCanasta")
							WHERE CCM."IdEmpresa"= :p_IdEmpresa 
							  AND CCM."IdMoneda" = MF."IdMonedaPrincipal"
							GROUP BY CCM."IdCanasta"
							) AS FactorCorrespondiente
						ON (COALESCE(FactorCorrespondiente."MIN",FactorCorrespondiente."MAX") = Canasta."Hasta" AND 
							Canasta."IdEmpresa"	= :p_IdEmpresa AND 
							Canasta."IdCanasta"	= FactorCorrespondiente."IdCanasta")
					)											AS "Factor"
				,:p_FechaOperacion								AS "FechaConsulta"
				,TO_VARCHAR(:p_FechaOperacion,'DD-MM-YYYY')			AS "FechaConsultaFormato"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaImpresion"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraImpresion"
				,COALESCE(PTCDO."TipoCambioMercado",0)			AS "DO"
				,COALESCE(PTCUF."TipoCambioMercado",0)			AS "UF"
				,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."PrecioSpotCompra",0)	ELSE COALESCE(PTCDO."PrecioSpotCompra",0) 	END AS "PrecioSpotCompra"
				,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."PrecioSpotVenta",0) 	ELSE COALESCE(PTCDO."PrecioSpotVenta",0) 	END AS "PrecioSpotVenta"
				,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."PrecioSpotPromedio",0) 	ELSE COALESCE(PTCDO."PrecioSpotPromedio",0) END AS "PrecioSpotPromedio"
			    ,COALESCE(PTCUSD."TipoCambioMercado",0)			AS "USD"
				,USU."CodigoUsuario"							AS "Usuario"
				,COALESCE(MONPRI."MontoDecimales",4)			AS "MontoDecimalesPrincipal"
				,COALESCE(MONSEC."MontoDecimales",4)			AS "MontoDecimalesSecundaria"
				,COALESCE(MONDO."MontoDecimales",4)				AS "MontoDecimalesDO"
				,COALESCE(MONUF."MontoDecimales",4)				AS "MontoDecimalesUF"
				,COALESCE(MONUF."TipoCambioDecimales",4)		AS "TCDecimalesUF"
				,COALESCE(MONUSD."MontoDecimales",4)			AS "MontoDecimalesUSD"
				,COALESCE(MONPRI."TasaDecimales",8)				AS "TasaDecimales"
				,LOGO."LogoCorredora"
				,''												AS "MensajeError"
				,CASE WHEN CONF."Valor" = 'S' THEN 'USD' ELSE 'DO' END AS "TipoCambioPar"	
				,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."TipoCambioMercado",0) ELSE COALESCE(PTCDO."TipoCambioMercado",0) END AS "TipoCambioVal"	 
				,MF."CodigoPropositoPortafolio"                 AS "CodigoPortafolio"
				,PARAM."NombreParametro" AS "ModalidadLiquidacion"
				,COALESCE(MF."IdMonedaCompensacion", '') AS "MonedaCompensacion"
			FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDHistorico" MF
			LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CAR ON MF."IdCartera"= CAR."IdCartera" AND CAR."FechaRespaldo" =  :p_FechaOperacion
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU
				ON (USU."IdUsuario" = :p_Usuario)
			LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCDO
				ON (PTCDO."Origen"	= 'OBSERVADO' AND 
					PTCDO."Fecha"	= :p_FechaOperacion AND 
					PTCDO."IdMoneda"= 'DO')
			LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCUF
				ON (PTCUF."Origen"	= 'OBSERVADO' AND 
					PTCUF."Fecha"	= :p_FechaOperacion AND 
					PTCUF."IdMoneda"= 'UF')
			LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCUSD
				ON (PTCUSD."Origen"	= 'OBSERVADO' AND 
					PTCUSD."Fecha"	= :p_FechaOperacion AND 
					PTCUSD."IdMoneda"= 'USD')
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONPRI
				ON (MONPRI."IdUsoMonedaFormato" = 'FWD' AND MONPRI."IdMoneda" = MF."IdMonedaPrincipal" AND  MONPRI."IdTipoInstrumento" = MF."IdTipoInstrumento")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONSEC
				ON (MONSEC."IdUsoMonedaFormato" = 'FWD' AND MONSEC."IdMoneda" = MF."IdMonedaSecundaria" AND  MONSEC."IdTipoInstrumento" = MF."IdTipoInstrumento")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONDO
				ON (MONDO."IdUsoMonedaFormato" = 'FWD' AND MONDO."IdMoneda" = 'DO' AND  MONDO."IdTipoInstrumento" = MF."IdTipoInstrumento")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONUF
				ON (MONUF."IdUsoMonedaFormato" = 'FWD' AND MONUF."IdMoneda" = 'UF' AND  MONUF."IdTipoInstrumento" = MF."IdTipoInstrumento")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONUSD
				ON (MONUSD."IdUsoMonedaFormato" = 'FWD' AND MONUSD."IdMoneda" = 'USD' AND  MONUSD."IdTipoInstrumento" = MF."IdTipoInstrumento")
			LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONF
				ON (CONF."CodigoParametro" = 'TIPO_CAMBIO_VALORIZA_BASILEA')		
			LEFT JOIN :v_logo LOGO
				ON (LOGO."LogoCorredora" IS NOT NULL)
			LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCUFOPE
				ON (PTCUFOPE."Origen"	= 'OBSERVADO' AND 
					PTCUFOPE."Fecha"	= MF."FechaOperacion" AND 
					PTCUFOPE."IdMoneda"= 'UF')
			INNER JOIN "BSCL.Mesa.BD::MES.Parametro" AS PARAM 
				ON (PARAM."IdParametro" = CAR."IdModalidadCumplimiento" AND PARAM."NombreTipoParametro" = 'ModalidadLiquidacion' AND PARAM."IdEmpresa" = CAR."IdEmpresa")
			WHERE MF."Estado"='VIG'
			  AND TO_DATE(MF."FechaRespaldo") = :p_FechaOperacion
			  AND MF."FechaOperacion" <= :p_FechaOperacion
			  AND MF."FechaVencimiento" > :p_FechaOperacion;
		END IF;
		
		
		v_Salida =
			SELECT
				 CAR.*
				,CASE WHEN CAR."TipoCambioPar" = 'DO' 
					THEN COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."DO" / 100,0)
					ELSE 				
						CASE WHEN CAR."TipoOperacion" = 'COMPRA'
							THEN 
								CASE WHEN CAR."PrecioSpotCompra" > 0
									THEN 
										COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotCompra" / 100,0)
					  				ELSE 
					  					COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotPromedio" / 100,0) 
					  				END
							ELSE  
								CASE WHEN CAR."PrecioSpotVenta" > 0
									THEN 
										COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotVenta" / 100,0)
					  				ELSE 
					  					COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotPromedio" / 100,0) 
					  				END
					  		END
					END AS "MontoMatriz" 
				,CASE WHEN CAR."TipoOperacion" = 'COMPRA'
						THEN 
							CASE WHEN CAR."ValorRazonable" >= 0 
								THEN 
									CASE WHEN CAR."TipoCambioPar" = 'DO' 
									THEN 
										COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."DO" / 100,0) + CAR."ValorRazonable"
									ELSE  
										CASE WHEN CAR."PrecioSpotCompra" > 0
											THEN 
												COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotCompra" / 100,0) + CAR."ValorRazonable"
											ELSE 
												COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotPromedio" / 100,0) + CAR."ValorRazonable"
											END
									END
								ELSE 
									CASE WHEN CAR."TipoCambioPar" = 'DO' 
									THEN 
										COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."DO" / 100,0)
									ELSE 
										CASE WHEN CAR."PrecioSpotCompra" > 0
											THEN 
												COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotCompra" / 100,0) 
											ELSE 
												COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotPromedio" / 100,0)
											END
									END
								END
						ELSE 
					CASE WHEN CAR."ValorRazonable" >= 0 
						THEN 
							CASE WHEN CAR."TipoCambioPar" = 'DO' 
								THEN 
									COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."DO" / 100,0) + CAR."ValorRazonable"
								ELSE
									CASE WHEN CAR."PrecioSpotVenta" > 0
										THEN 
											COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotVenta" / 100,0) + CAR."ValorRazonable"
										ELSE 
											COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotPromedio" / 100,0) + CAR."ValorRazonable"
										END
								END
						ELSE 
							CASE WHEN CAR."TipoCambioPar" = 'DO' 
								THEN 
									COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."DO" / 100,0)
								ELSE
									CASE WHEN CAR."PrecioSpotVenta" > 0
										THEN 
											COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotVenta" / 100,0) 
										ELSE 
											COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotPromedio" / 100,0) 
										END 
								END
						END
				END AS "EquivalenteCredito"
			FROM :v_Cartera CAR;
	
	SELECT COUNT(1) INTO v_count FROM :v_Cartera;
	IF (v_count = 0) THEN
		v_Cartera =
			SELECT
				 0 		AS "FolioOperacion"
				,0 		AS "FolioCartera"
				,'' 	AS "NombreInstrumento"
				,''		AS "RutCliente"
				,''  	AS "NombreCliente"
				,''		AS "FechaInicio"
				,''		AS "FechaInicioFormato"
				,''		AS "FechaVencimiento"
				,''		AS "FechaVencimientoFormato"
				,0		AS "PlazoOperacion"
				,''		AS "IdMonedaPrincipal"
				,''		AS "IdMonedaSecundaria"
				,0 		AS "MontoNocional"
				,0 		AS "MontoNocionalCLP"
				,0		AS "TipoCambioFWD"
				,0		AS "PlazoRem"
				,0 		AS "MontoValorRazonableActivo"
				,0 		AS "MontoValorRazonablePasivo"
				,0		AS "ValorRazonable"
				,0		AS "TasaValorRazonableActivo"
				,0  	AS "TasaValorRazonablePasivo"
				,''		AS "TipoOperacion"
				,0		AS "Factor"
				,:p_FechaOperacion									AS "FechaConsulta"
				,TO_VARCHAR(:p_FechaOperacion,'DD-MM-YYYY')			AS "FechaConsultaFormato"
				,TO_VARCHAR(:v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaImpresion"
				,TO_VARCHAR(:v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraImpresion"
				,COALESCE(PTCDO."TipoCambioMercado",0)			AS "DO"
				,COALESCE(PTCUF."TipoCambioMercado",0)			AS "UF"
				,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."PrecioSpotCompra",0)	ELSE COALESCE(PTCDO."PrecioSpotCompra",0) 	END AS "PrecioSpotCompra"
				,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."PrecioSpotVenta",0) 	ELSE COALESCE(PTCDO."PrecioSpotVenta",0) 	END AS "PrecioSpotVenta"
				,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."PrecioSpotPromedio",0) 	ELSE COALESCE(PTCDO."PrecioSpotPromedio",0) END AS "PrecioSpotPromedio"
			    ,COALESCE(PTCUSD."TipoCambioMercado",0)			AS "USD"
				,USU."CodigoUsuario"							AS "Usuario"
				,4												AS "MontoDecimalesPrincipal"
				,4												AS "MontoDecimalesSecundaria"
				,COALESCE(MONDO."MontoDecimales",4)				AS "MontoDecimalesDO"
				,COALESCE(MONUF."MontoDecimales",4)				AS "MontoDecimalesUF"
				,COALESCE(MONUF."TipoCambioDecimales",4)		AS "TCDecimalesUF"
				,COALESCE(MONUSD."MontoDecimales",4)			AS "MontoDecimalesUSD"
				,8												AS "TasaDecimales"
				,LOGO."LogoCorredora"
				,'ERROR : NO SE ENCONTRARON DATOS PARA REPORTE BASILEA.' AS "MensajeError"
				,CASE WHEN CONF."Valor" = 'S' THEN 'USD' ELSE 'DO' END AS "TipoCambioPar"	
				,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."TipoCambioMercado",0) ELSE COALESCE(PTCDO."TipoCambioMercado",0) END AS "TipoCambioVal"	 
				,'' AS "CodigoPortafolio" 
				,'' AS "ModalidadLiquidacion"
				,'' AS "MonedaCompensacion"
			FROM DUMMY
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU
				ON (USU."IdUsuario" = :p_Usuario)
			LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCDO
				ON (PTCDO."Origen"	= 'OBSERVADO' AND 
					TO_DATE(PTCDO."Fecha")	= TO_DATE(:p_FechaOperacion) AND 
					PTCDO."IdMoneda"= 'DO')
			LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCUF
				ON (PTCUF."Origen"	= 'OBSERVADO' AND 
					PTCUF."Fecha"	= :p_FechaOperacion AND 
					PTCUF."IdMoneda"= 'UF')
			LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCUSD
				ON (PTCUSD."Origen"	= 'OBSERVADO' AND 
					TO_DATE(PTCUSD."Fecha")	= TO_DATE(:p_FechaOperacion) AND 
					PTCUSD."IdMoneda"= 'USD')
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONDO
				ON (MONDO."IdUsoMonedaFormato" = 'FWD' AND MONDO."IdMoneda" = 'DO')
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONUF
				ON (MONUF."IdUsoMonedaFormato" = 'FWD' AND MONUF."IdMoneda" = 'UF')
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONUSD
				ON (MONUSD."IdUsoMonedaFormato" = 'FWD' AND MONUSD."IdMoneda" = 'USD')
			LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONF
				ON (CONF."CodigoParametro" = 'TIPO_CAMBIO_VALORIZA_BASILEA')
			LEFT JOIN :v_logo LOGO
				ON (LOGO."LogoCorredora" IS NOT NULL);
		
		v_Salida =
			SELECT
				 CAR.*
				,0	AS "MontoMatriz"
				,0	AS "EquivalenteCredito"
			FROM :v_Cartera CAR
			ORDER BY "FolioOperacion";
		
	END IF;
	
	--Salida del SP
	SELECT * FROM :v_Salida;
		
END;
