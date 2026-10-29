SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CargarEventoCapitalDesdeBursatil";
GO

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CargarEventoCapitalDesdeBursatil" (

	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
    IN p_IdUsuario				BIGINT,
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca
	IN p_Fecha 					NVARCHAR(10), 

	OUT p_out_EVCsCargados  	INT
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE v_errorcuenta INT DEFAULT 0;
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		p_out_EVCsCargados := 0;
	END;
	

	BEGIN
		--------------------
		--Variables
		--------------------
		DECLARE	v_countTransacciones,v_j, v_ultimoEVC, v_out_secuencia	BIGINT := 0;
		DECLARE v_InfContador, v_AdvContador, v_ErrContador BIGINT := 0;
		
		DECLARE v_arr_IdEventoCapital					BIGINT ARRAY;
		DECLARE v_arr_IdTipoEventoCapital				NVARCHAR(2) ARRAY;
		DECLARE v_arr_Secuencia							BIGINT ARRAY;
		DECLARE v_arr_NumeroDividendo					BIGINT ARRAY;
		DECLARE v_arr_IdClasificacionTipoInstrumento	NVARCHAR(3) ARRAY;
		DECLARE v_arr_IdTipoInstrumento					NVARCHAR(3) ARRAY;
		DECLARE v_arr_Nemo								NVARCHAR(40) ARRAY;
		DECLARE v_arr_Factor							DECIMAL(26,8) ARRAY;
		DECLARE v_arr_Paridad							DECIMAL(26,8) ARRAY;
		DECLARE v_arr_FechaLimite						DATE ARRAY;
		DECLARE v_arr_FechaPago							DATE ARRAY;
		DECLARE v_arr_IdMoneda							NVARCHAR(3) ARRAY;
		DECLARE v_arr_IdInstrumento						BIGINT ARRAY;
		DECLARE v_arr_IdInstrumentoNuevo				BIGINT ARRAY;
		DECLARE v_arr_NemoNuevo							NVARCHAR(40) ARRAY;
		DECLARE v_arr_FactorNuevo						DECIMAL(26,8) ARRAY;
		DECLARE v_arr_IdEstado							NVARCHAR(3) ARRAY;
		DECLARE v_arr_Indice							BIGINT ARRAY;
		
		
		DECLARE v_paso_IdEventoCapital					BIGINT;
		DECLARE v_paso_IdTipoEventoCapital				NVARCHAR(2);
		DECLARE v_paso_Secuencia						BIGINT;
		DECLARE v_paso_NumeroDividendo					BIGINT;
		DECLARE v_paso_IdClasificacionTipoInstrumento	NVARCHAR(3);
		DECLARE v_paso_IdTipoInstrumento				NVARCHAR(3);
		DECLARE v_paso_Nemo								NVARCHAR(40);
		DECLARE v_paso_Factor							DECIMAL(26,8);
		DECLARE v_paso_Paridad							DECIMAL(26,8);
		DECLARE v_paso_FechaLimite						DATE;
		DECLARE v_paso_FechaPago						DATE;
		DECLARE v_paso_IdMoneda							NVARCHAR(3);
		DECLARE v_paso_IdInstrumento					BIGINT;
		DECLARE v_paso_IdInstrumentoNuevo				BIGINT;
		DECLARE v_paso_NemoNuevo						NVARCHAR(40);
		DECLARE v_paso_FactorNuevo						DECIMAL(26,8);
		DECLARE v_paso_IdEstado							NVARCHAR(3);
		DECLARE v_paso_Indice							BIGINT;
		
		
		v_tbl = select DISTINCT bur.*
				FROM  "BSCL.Custodia.BD::CUS.EventoCapitalBursatil" AS BUR
				INNER JOIN (SELECT 	
							"ACF_NEMO", 
							"ACF_SEC_VC", 
							MAX(ACV_INDICE) AS "ACV_INDICE"
						FROM "BSCL.Custodia.BD::CUS.EventoCapitalBursatil" 
						GROUP BY "ACF_NEMO", "ACF_SEC_VC") 			AS 	TMP
						
					ON 	BUR."ACF_NEMO" 		= 	TMP."ACF_NEMO" 		AND 
						BUR."ACF_SEC_VC" 	= 	TMP."ACF_SEC_VC" 	AND
						BUR."ACV_INDICE" 	=  	TMP."ACV_INDICE"
				
				
				LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital"	ECV 
					ON (ECV."IdEmpresa"                     = p_IdEmpresa 						AND
						ECV."IdTipoEventoCapital" 			= BUR."ACF_TIPO_VC"					AND
						ECV."Secuencia"						= BUR."ACF_SEC_VC"					AND
						LTRIM(RTRIM(ECV."Nemo"))			= LTRIM(RTRIM(BUR."ACF_NEMO"))      AND 
						ECV."IdClasificacionTipoInstrumento"	= 	CASE BUR."ACF_MERCADO"
																		WHEN 'AC' THEN 'RV'
																		ELSE BUR."ACF_MERCADO" 
																	END	
					)
			WHERE
				ECV."IdEventoCapital" IS NULL  																							-- no existe en evento capital de optimus
				AND CASE WHEN IFNULL(bur."ACV_FEC_ACT_VC",'') <> '' THEN bur."ACV_FEC_ACT_VC" ELSE bur."ACF_FEC_ACT_VC" END = p_Fecha   -- se ha creado/modificado en la fecha de filtro    
				AND (bur."ACF_MONEDA" <> '' OR bur."ACV_MONEDA" <> '')																	-- MONEDA NO PUEDE SER BLANCO
				AND TRIM(bur."ACF_NEMO") <> ''																							-- NEMO NO PUEDE VENIR EN BLANCO
				AND (IFNULL(bur."ACF_FAC_ACC_NUE",0) > 0 OR IFNULL(bur."ACF_VAL_ACC",0) > 0) 											-- Debe traer algun valor
				AND	bur."ACF_NUM_DIVDO"		>	0;																						-- Debe tener un nro de dividendo
			
		
		
		--CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);

		--------------------------------------------------
		--Insert en evento de capital
		--------------------------------------------------
		
		SELECT MAX("IdEventoCapital") INTO v_ultimoEVC FROM "BSCL.Custodia.BD::CUS.EventoCapital";
				
		INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapital"("IdEventoCapital",
								"IdEmpresa",						"IdTipoEventoCapital",				"Secuencia",						"Descripcion", 
		"EsInscrito",			"IdClasificacionTipoInstrumento",	"IdTipoInstrumento",				"IdInstrumento",					"Nemo", 
		"Factor",				"Valor", 							"IdMoneda",							"IdInstrumentoNuevo",				"NemoNuevo",						"FactorNuevo", 
		"FechaActualizacion",	"FechaLimite",						"FechaPago",						"NumeroDividendo",					"IdTipoDividendo", 
		"PrecioAnterior",		"FechaHoraIngresoRegistro",			"FechaHoraUltimaActualizacion",		"TipoIngreso",						"IdUsuario", 
		"IdEstado",				"EmisorPagadorSorteoLetras",		"EmisorSorteoLetras",				"InstrumentoSorteoLetras", 
		"MonedaSorteoLetras",	"GlosaMonedaSorteoLetras",			"SorteadoSorteoLetras",				"CuentaDepositanteSorteoLetras",	"BloqueoSorteoLetras", 
		"APagarSorteoLetras",	"APagarPesosSorteoLetras",			"GlosaInstrumentoSorteoLetras",		"IdEmisor",							"Origen", 
		"Paridad",				"IdMonedaLiquidacion",				"IdEmisorNuevo",					"EstadoNotificacion",				"IdInstrumentoAnterior", 
		"Indice")
		SELECT "BSCL.Custodia.BD.Secuencias::EventoCapital".NEXTVAL,		-- "IdEventoCapital"    			,
			p_IdEmpresa,													-- "IdEmpresa"    				 	,
			Archivo."ACF_TIPO_VC",											-- "IdTipoEventoCapital"			,
			Archivo."ACF_SEC_VC",											-- "Secuencia"						,
			Archivo."ACF_DESCRIP_VC",										-- "Descripcion"					,
			'S',															-- "EsInscrito"						,
			Instrumento."IdClasificacionTipoInstrumento",					-- "IdClasificacionTipoInstrumento"	,
			Instrumento."IdTipoInstrumento",								-- "IdTipoInstrumento" 				,
			Instrumento."IdInstrumento",									-- "IdInstrumento" 					,
			Instrumento."Nemotecnico",										-- "Nemo"							,
			Archivo."ACF_FAC_ACC_NUE",										-- "Factor"							,
			IFNULL(Archivo."ACF_VAL_ACC",0),								-- "Valor"							,
			CASE 
				WHEN IFNULL(Archivo."ACV_MONEDA",'') <> '' AND IFNULL(Archivo."ACV_MONEDA",'') = '$' THEN 'CLP'
				WHEN IFNULL(Archivo."ACV_MONEDA",'') <> '' AND IFNULL(Archivo."ACV_MONEDA",'') = 'EURO' THEN 'EUR'
				WHEN IFNULL(Archivo."ACV_MONEDA",'') <> '' AND IFNULL(Archivo."ACV_MONEDA",'') = 'US$' THEN 'USD'
				WHEN IFNULL(Archivo."ACF_MONEDA",'') <> '' AND IFNULL(Archivo."ACF_MONEDA",'') = '$' THEN 'CLP'
				WHEN IFNULL(Archivo."ACF_MONEDA",'') <> '' AND IFNULL(Archivo."ACF_MONEDA",'') = 'EURO' THEN 'EUR'
				WHEN IFNULL(Archivo."ACF_MONEDA",'') <> '' AND IFNULL(Archivo."ACF_MONEDA",'') = 'US$' THEN 'USD'
				ELSE 'CLP'
			END, 															-- "IdMoneda"						,
			Instrumento2."IdInstrumento",									-- "IdInstrumentoNuev"				,
			Instrumento2."Nemotecnico",										-- "NemoNuevo"						,
			Archivo."ACF_FAC_ACC_NUE",										-- "FactorNuevo"					,
			Current_UTCTIMESTAMP,											-- "FechaActualizacion"				,
			TO_DATE(SUBSTRING (Archivo."ACF_FEC_LIM",1,4)||'-'||SUBSTRING (Archivo."ACF_FEC_LIM",6,2)||'-'||SUBSTRING (Archivo."ACF_FEC_LIM",9,2), 'YYYY-MM-DD'),	-- "FechaLimite"					,
			TO_DATE(SUBSTRING (Archivo."ACF_FEC_PAGO",1,4)||'-'||SUBSTRING (Archivo."ACF_FEC_PAGO",6,2)||'-'||SUBSTRING (Archivo."ACF_FEC_PAGO",9,2), 'YYYY-MM-DD'),	-- "FechaPago"						,
			Archivo."ACF_NUM_DIVDO",										-- "NumeroDividendo"				,
			IFNULL(Archivo."ACF_TIPO_DIV",''),								-- "IdTipoDividendo"				,
			IFNULL(Archivo."ACF_PRE_ANT_VC",0),								-- "PrecioAnterior"					,
			Current_UTCTIMESTAMP,											-- "FechaHoraIngresoRegistro"		,
			Current_UTCTIMESTAMP, 											-- "FechaHoraUltimaActualizacion"	,
			'C',															-- "TipoIngreso" 					,
			p_IdUsuario,													-- "IdUsuario" 						,
			'VIG',															-- "IdEstado"						,
			0,																-- "EmisorPagadorSorteoLetras"		,
			0,																-- "EmisorSorteoLetras"		,
			0,																-- "InstrumentoSorteoLetras"		,
			0,																-- "MonedaSorteoLetras"				,
			'',																-- "GlosaMonedaSorteoLetras"		,
			0,																-- "SorteadoSorteoLetras,
			0,																-- CuentaDepositanteSorteoLetras
			0,																-- BloqueoSorteoLetras
			0,																-- "APagarSorteoLetras,
			0,																-- APagarPesosSorteoLetras
			'',																-- GlosaInstrumentoSorteoLetras
			Emisor."IdEmisor",												-- id emisor
			'SIS',															-- "Origen"
			0, -- paridad
			CASE 
				WHEN IFNULL(Archivo."ACV_MONEDA",'') <> '' AND IFNULL(Archivo."ACV_MONEDA",'') = '$' THEN 'CLP'
				WHEN IFNULL(Archivo."ACV_MONEDA",'') <> '' AND IFNULL(Archivo."ACV_MONEDA",'') = 'EURO' THEN 'EUR'
				WHEN IFNULL(Archivo."ACV_MONEDA",'') <> '' AND IFNULL(Archivo."ACV_MONEDA",'') = 'US$' THEN 'USD'
				WHEN IFNULL(Archivo."ACF_MONEDA",'') <> '' AND IFNULL(Archivo."ACF_MONEDA",'') = '$' THEN 'CLP'
				WHEN IFNULL(Archivo."ACF_MONEDA",'') <> '' AND IFNULL(Archivo."ACF_MONEDA",'') = 'EURO' THEN 'EUR'
				WHEN IFNULL(Archivo."ACF_MONEDA",'') <> '' AND IFNULL(Archivo."ACF_MONEDA",'') = 'US$' THEN 'USD'
				ELSE 'CLP'
			END,                                                            -- IdMonedaLiquidacion, 
			'', 															-- idemisornuevo
			'N',
			0, 
			Archivo."ACV_INDICE"
		FROM :v_tbl AS Archivo
			INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 	AS Instrumento
				ON Trim(Archivo."ACF_NEMO") = Trim(Instrumento."Nemotecnico")
			INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 	AS Instrumento2
				ON Trim(Archivo."ACF_NEMO") = Trim(Instrumento2."Nemotecnico")
			INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 	AS Emisor
				ON Emisor."Nemotecnico" = Trim(Archivo."ACF_NEMO")

		WHERE
			(Archivo."ACF_MONEDA" <> '' OR Archivo."ACV_MONEDA" <> '')																			-- MONEDA NO PUEDE SER BLANCO
			AND TRIM(Archivo."ACF_NEMO") <> ''																									-- NEMO NO PUEDE VENIR EN BLANCO
			AND (IFNULL(Archivo."ACF_FAC_ACC_NUE",0) > 0 OR IFNULL(Archivo."ACF_VAL_ACC",0) > 0) 												-- Debe traer algun valor
			AND	Archivo."ACF_NUM_DIVDO"		>	0																								-- Debe tener un nro de dividendo
		;
		
		SELECT ::ROWCOUNT into p_out_EVCsCargados FROM DUMMY;
		
	END;
END; 
GO