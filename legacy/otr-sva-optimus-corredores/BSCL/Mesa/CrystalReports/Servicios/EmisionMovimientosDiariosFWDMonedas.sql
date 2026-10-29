SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::EmisionMovimientosDiariosFWDMonedas";

--
--  Proposito: servicio para consultar Emision Movimientos Diario FWD Monedas
--  Autor: EORTIZ - PRONET-IT
--  Fecha Creacion: /10/2016
--

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::EmisionMovimientosDiariosFWDMonedas"(
	-- Parametros entrada 
	IN p_IdEmpresa		BIGINT,
	IN p_FechaOperacion	DATE,
	IN p_IdUsuario		BIGINT
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
	DECLARE v_logo TABLE("LogoCorredora" BLOB);
	DECLARE v_ExisteDolar		BIGINT := 0;
	DECLARE v_countOperaciones 	BIGINT := 0;
	DECLARE v_MensajeError		NVARCHAR(100) := '';
	
	DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
	DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
	DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
	DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
	DECLARE v_FechaHoraUTC   		NVARCHAR(40);
	DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
	
	DECLARE v_Movimientos TABLE(
		 "FolioOperacion"				BIGINT
		,"NombreInstrumento"			NVARCHAR(100)
		,"RutCliente"					NVARCHAR(40)
		,"Portafolio"					NVARCHAR(200)
		,"NombreCliente"				NVARCHAR(100)
		,"FechaInicio"					DATE
		,"FechaInicioFormato"			NVARCHAR(10)
		,"HoraInicio"					NVARCHAR(5)
		,"FechaVencimiento"				DATE
		,"FechaVencimientoFormato"		NVARCHAR(10)
		,"PlazoOperacion"				BIGINT
		,"IdMonedaPrincipal"			NVARCHAR(3)
		,"IdMonedaSecundaria"			NVARCHAR(3)
		,"Monto"						DECIMAL(26,8)
		,"TipoCambioFWD"				DECIMAL(26,8)
		,"PlazoRem"						BIGINT
		,"MontoValorRazonableActivo"	DECIMAL(26,8)
		,"MontoValorRazonablePasivo"	DECIMAL(26,8)
		,"TasaValorRazonableActivo"		DECIMAL(26,8)
		,"TasaValorRazonablePasivo"		DECIMAL(26,8)
		,"TipoOperacion"				NVARCHAR(6)
		,"FechaConsulta"				DATE
		,"FechaConsultaFormato"			NVARCHAR(10)
		,"DolarObservado"				DECIMAL(26,8)
		,"Usuario"						NVARCHAR(100)
		,"Estado"						NVARCHAR(3)
		,"ModalidadPago"				NVARCHAR(100)
		,"PrecioSpotCompra"				DECIMAL(26,8)
		,"PrecioSpotVenta"				DECIMAL(26,8)
		,"PrecioSpotPromedio"			DECIMAL(26,8)
		,"LogoCorredora"				BLOB
		,"FechaSistemaLOCAL"			NVARCHAR(10)
		,"HoraSistemaLOCAL"				NVARCHAR(5)
		,"MontoDecimalesPrincipal"		BIGINT
		,"MontoDecimalesSecundaria"		BIGINT
		,"MensajeError"					NVARCHAR(100)
	);
	
	v_logo =
		SELECT TOP 1 "Binario" AS "LogoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
	
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
	
	
	v_Movimientos =
		SELECT
			 MF."FolioOperacion"
			,MF."NombreInstrumento"
			,MF."RutCliente"
			,MF."CodigoPropositoPortafolio"	AS "Portafolio"
			,MF."NombreCliente"
			,MF."FechaOperacion"			AS "FechaInicio"
			,TO_VARCHAR(MF."FechaOperacion",'DD-MM-YYYY')	AS "FechaInicioFormato"
			,MF."HoraOperacion"				AS "HoraInicio"	
			,MF."FechaVencimiento"
			,TO_VARCHAR(MF."FechaVencimiento",'DD-MM-YYYY')	AS "FechaVencimientoFormato"
			,MF."Plazo"						AS "PlazoOperacion"
			,MF."IdMonedaPrincipal"
			,MF."IdMonedaSecundaria"
			,MF."MontoMonedaPrincipal"		AS "Monto"
			,CASE WHEN MF."Nemotecnico" = 'ARBITR' THEN MF."ParidadCierre" ELSE MF."TipoCambioCierre" END AS "TipoCambioFWD"
			,MF."PlazoResidual"				AS "PlazoRem"
			,MF."MontoValorRazonableActivo"
			,MF."MontoValorRazonablePasivo"
			,MF."TasaValorRazonableActivo"
			,MF."TasaValorRazonablePasivo"
			,CASE
				WHEN MF."IdTipoOperacion" = 'COMFWD' THEN 'COMPRA' 
				WHEN MF."IdTipoOperacion" = 'VENFWD' THEN 'VENTA' END AS "TipoOperacion"
			,:p_FechaOperacion				AS "FechaConsulta"
			,TO_VARCHAR(:p_FechaOperacion,'DD-MM-YYYY')	AS "FechaConsultaFormato"
			,COALESCE(PTC."TipoCambioMercado",0)		AS "DolarObservado"
			,USU."CodigoUsuario"						AS "Usuario"
			,MF."Estado"
			,MF."ModalidadLiquidacion"					AS "ModalidadPago"
			,COALESCE(PAR."PrecioSpotCompra",0)			AS "PrecioSpotCompra"
		    ,COALESCE(PAR."PrecioSpotVenta",0)			AS "PrecioSpotVenta"
		    ,COALESCE(PAR."PrecioSpotPromedio",0)		AS "PrecioSpotPromedio"
		    ,LOGO."LogoCorredora"
			--,NULL AS "LogoCorredora"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
			,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
			,COALESCE(MONPRI."MontoDecimales",4)			AS "MontoDecimalesPrincipal"
			,COALESCE(MONSEC."MontoDecimales",4)			AS "MontoDecimalesSecundaria"
			,'SIN_ERRORES'								AS "MensajeError"
		FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" MF
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU
			ON USU."IdUsuario" = :p_IdUsuario
		LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR
			ON (PAR."IdMoneda" = MF."IdMonedaPrincipal" AND PAR."Fecha" = :p_FechaOperacion)
		LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
			ON (PTC."Origen" = 'OBSERVADO' AND PTC."Fecha" = :p_FechaOperacion AND PTC."IdMoneda"= 'USD')
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONPRI
			ON (MONPRI."IdUsoMonedaFormato" = 'FWD' AND MONPRI."IdMoneda" = MF."IdMonedaPrincipal" AND MONPRI."IdTipoOperacion" = MF."Nemotecnico")
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONSEC
			ON (MONSEC."IdUsoMonedaFormato" = 'FWD' AND MONSEC."IdMoneda" = MF."IdMonedaSecundaria" AND MONSEC."IdTipoOperacion" = MF."Nemotecnico")
		LEFT JOIN :v_logo LOGO
			ON (LOGO."LogoCorredora" IS NOT NULL)
		WHERE MF."Estado"='VIG'
		  AND MF."FechaOperacion" = :p_FechaOperacion
		/*GROUP BY
			MF."FolioOperacion",MF."NombreInstrumento",MF."RutCliente",MF."CodigoPropositoPortafolio",MF."NombreCliente",
			MF."FechaOperacion",MF."HoraOperacion",MF."FechaVencimiento",MF."Plazo",MF."IdMonedaPrincipal",MF."IdMonedaSecundaria",
			MF."MontoMonedaPrincipal",MF."Nemotecnico",MF."ParidadCierre",MF."TipoCambioCierre",MF."PlazoResidual",MF."MontoValorRazonableActivo",
			MF."MontoValorRazonablePasivo",MF."TasaValorRazonableActivo",MF."TasaValorRazonablePasivo",MF."IdTipoOperacion",
			PTC."TipoCambioMercado",USU."CodigoUsuario",MF."Estado",MF."ModalidadLiquidacion",PAR."PrecioSpotCompra",PAR."PrecioSpotVenta",
			PAR."PrecioSpotPromedio"*/
			--,"LogoCorredora"
		;
	
	
	SELECT COUNT(1) INTO v_ExisteDolar
	FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
	WHERE PTC."Origen"	= 'OBSERVADO' 
	  AND PTC."Fecha"	= :p_FechaOperacion
	  AND PTC."IdMoneda"= 'USD';
	
	SELECT COUNT(1) INTO v_countOperaciones
	FROM :v_Movimientos;
	
	IF (v_ExisteDolar = 0 AND v_countOperaciones = 0) THEN
		v_MensajeError := 'NO_EXISTE_VALOR_DOLAR_REFERENCIA_Y_NO_EXISTEN_OPERACIONES';
	ELSEIF (v_ExisteDolar = 0) THEN
		v_MensajeError := 'NO_EXISTE_VALOR_DOLAR_REFERENCIA';
	ELSEIF (v_countOperaciones = 0) THEN
		v_MensajeError := 'NO_EXISTEN_OPERACIONES';
	END IF;
		
	IF (v_ExisteDolar = 0 OR v_countOperaciones = 0) THEN
		v_Movimientos =
			SELECT
				 0 		AS "FolioOperacion"
				,'' 	AS "NombreInstrumento"
				,''		AS "RutCliente"
				,''		AS "Portafolio"
				,''  	AS "NombreCliente"
				,''		AS "FechaInicio"
				,''		AS "FechaInicioFormato"
				,''		AS "HoraInicio"	
				,''		AS "FechaVencimiento"
				,''		AS "FechaVencimientoFormato"
				,0		AS "PlazoOperacion"
				,''		AS "IdMonedaPrincipal"
				,''		AS "IdMonedaSecundaria"
				,0 		AS "Monto"
				,0		AS "TipoCambioFWD"
				,0		AS "PlazoRem"
				,0 		AS "MontoValorRazonableActivo"
				,0 		AS "MontoValorRazonablePasivo"
				,0		AS "TasaValorRazonableActivo"
				,0  	AS "TasaValorRazonablePasivo"
				,''		AS "TipoOperacion"
				,''		AS "FechaConsulta"
				,''		AS "FechaConsultaFormato"
				,0		AS "DolarObservado"
				,''		AS "Usuario"
				,''		AS "Estado"
				,'' 	AS "ModalidadPago"	
				,0		AS "PrecioSpotCompra"
				,0		AS "PrecioSpotVenta"
				,0		AS "PrecioSpotPromedio"
				,LOGO."LogoCorredora"	AS "LogoCorredora"
				,'' 	AS "FechaSistemaLOCAL"
				,'' 	AS "HoraSistemaLOCAL"
				,4		AS "MontoDecimalesPrincipal"
				,4		AS "MontoDecimalesSecundaria"
				,v_MensajeError	AS "MensajeError"
			FROM DUMMY
			LEFT JOIN :v_logo LOGO
				ON (LOGO."LogoCorredora" IS NOT NULL)
			;
	END IF;
	
	
	--Salida del SP
	SELECT * FROM :v_Movimientos;
	
END;
