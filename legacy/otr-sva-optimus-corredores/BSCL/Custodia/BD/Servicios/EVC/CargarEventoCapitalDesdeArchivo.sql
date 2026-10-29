SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Custodia.BD.Servicios::EVC.CargarEventoCapitalDesdeArchivo";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CargarEventoCapitalDesdeArchivo" (

	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
    IN p_IdUsuario				BIGINT 			default 0,
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_DataArchivo			TABLE (	"MERCADO"                  NVARCHAR(5),
										"NEMOTECNICO"              NVARCHAR(12),
										"NEMOTECNICODIV"           NVARCHAR(12),
										"TIPOEVENTO"               NVARCHAR(2),
										"SECUENCIA"                DECIMAL(8,0),
										"NUMERODIVIDENDO"          DECIMAL(8,0),
										"FECHALIMITE"              NVARCHAR(10),
										"FECHAPAGO"                NVARCHAR(10),
										"INDICADORINSCRITO"        NVARCHAR(1),
										"FACTORPAGO"               DECIMAL(17,9),
										"MONEDAPAGO"               NVARCHAR(3),
										"FACTORSUSCRIPCION"        DECIMAL(17,9),
										"DESCRIPCION"              NVARCHAR(60),
										"MENSAJEVALIDACION" 		NVARCHAR(200)),
	-- Salidas para mensajes de advertencias y errores

	OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_AdvertenciasDetalle  	 	 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresContador  	 	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	 	table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador
		from :p_out_AdvertenciasDetalle;
	END;

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;
			select count(1) into p_out_ErroresContador
			from :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)
			RESIGNAL;
		END IF;
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;

	BEGIN
		--------------------
		--Variables
		--------------------
		DECLARE	v_FechaSistemaCreacion	TIMESTAMP;
		DECLARE	v_countTransacciones	BIGINT;
		DECLARE	v_countOrdenes			BIGINT;
		DECLARE	v_countClientes			BIGINT;
		DECLARE	v_countAsignacion		BIGINT;

		--------------------
		-- Validaciones
		-- Si se encuentra algún registro, no se puede cargar el archivo.
		--------------------
/*
		p_OUT_DataArchivo = select * FROM :p_DataArchivo			AS Archivo
			WHERE
				Archivo.MONEDA		<=	0	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR TRIM(Archivo.NEMO)	= 	''	-- NEMO NO PUEDE VENIR EN BLANCO
			OR Archivo.SORTEADO	<=	0	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR Archivo.APAGAR		<=	0	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR Archivo.APAGARPESOS	<=	0;

*/
		BEGIN
			SELECT COUNT(1) INTO p_out_ErroresContador
			FROM :p_DataArchivo			AS Archivo
			WHERE
				Archivo.MONEDAPAGO			=	''	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR TRIM(Archivo.NEMOTECNICO)	= 	''	-- NEMO NO PUEDE VENIR EN BLANCO
			OR Archivo.FACTORPAGO			<=	0	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR Archivo.NUMERODIVIDENDO		<=	0;	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO

			IF(p_out_ErroresContador > 0) THEN
				p_out_ErroresDetalle = select 'ARCHIVO_SORTEO_LETRAS_CON_ERRORES_REVISAR' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;

		END;


		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);

		--------------------------------------------------
		--Insert de Sorteos de letras en evento de capital
		--------------------------------------------------
		INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapital"("IdEventoCapital",
									"IdEmpresa",						"IdTipoEventoCapital",				"Secuencia",						"Descripcion", 
			"EsInscrito",			"IdClasificacionTipoInstrumento",	"IdTipoInstrumento",				"IdInstrumento",					"Nemo", 
			"Factor",				"IdMoneda",							"IdInstrumentoNuevo",				"NemoNuevo",						"FactorNuevo", 
			"FechaActualizacion",	"FechaLimite",						"FechaPago",						"NumeroDividendo",					"IdTipoDividendo", 
			"PrecioAnterior",		"FechaHoraIngresoRegistro",			"FechaHoraUltimaActualizacion",		"TipoIngreso",						"IdUsuario", 
			"IdEstado",				"Valor",							"EmisorPagadorSorteoLetras",		"EmisorSorteoLetras",				"InstrumentoSorteoLetras", 
			"MonedaSorteoLetras",	"GlosaMonedaSorteoLetras",			"SorteadoSorteoLetras",				"CuentaDepositanteSorteoLetras",	"BloqueoSorteoLetras", 
			"APagarSorteoLetras",	"APagarPesosSorteoLetras",			"GlosaInstrumentoSorteoLetras",		"IdEmisor",							"Origen", 
			"Paridad",				"IdMonedaLiquidacion",				"IdEmisorNuevo",					"EstadoNotificacion",				"IdInstrumentoAnterior", 
			"Indice")
	   	 	SELECT "BSCL.Custodia.BD.Secuencias::EventoCapital".NEXTVAL,			-- "IdEventoCapital"    			,
				p_IdEmpresa,													-- "IdEmpresa"    				 	,
				Archivo."TIPOEVENTO",											-- "IdTipoEventoCapital"			,
				Archivo."SECUENCIA",											-- "Secuencia"						,
				Archivo."DESCRIPCION",											-- "Descripcion"					,
				Archivo."INDICADORINSCRITO",										-- "EsInscrito"						,
				Instrumento."IdClasificacionTipoInstrumento",					-- "IdClasificacionTipoInstrumento"	,
				Instrumento."IdTipoInstrumento",								-- "IdTipoInstrumento" 				,
				Instrumento."IdInstrumento",									-- "IdInstrumento" 					,
				Instrumento."Nemotecnico",										-- "Nemo"							,
				Archivo."FACTORPAGO",											-- "Factor"							,
				Archivo."MONEDAPAGO",											-- "IdMoneda"						,
				Instrumento2."IdInstrumento",									-- "IdInstrumentoNuev"				,
				Instrumento2."Nemotecnico",										-- "NemoNuevo"						,
				0,																-- "FactorNuevo"					,
				'0001-01-01',													-- "FechaActualizacion"				,
				TO_DATE(SUBSTRING (Archivo."FECHALIMITE",1,4)||'-'||SUBSTRING (Archivo."FECHALIMITE",6,2)||'-'||SUBSTRING (Archivo."FECHALIMITE",9,2), 'YYYY-MM-DD'),	-- "FechaLimite"					,
				TO_DATE(SUBSTRING (Archivo."FECHAPAGO",1,4)||'-'||SUBSTRING (Archivo."FECHAPAGO",6,2)||'-'||SUBSTRING (Archivo."FECHAPAGO",9,2), 'YYYY-MM-DD'),	-- "FechaPago"						,
				Archivo."NUMERODIVIDENDO",										-- "NumeroDividendo"				,
				'',																-- "IdTipoDividendo"				,
				0,																-- "PrecioAnterior"					,
				Current_UTCTIMESTAMP,											-- "FechaHoraIngresoRegistro"		,
				Current_UTCTIMESTAMP, 											-- "FechaHoraUltimaActualizacion"	,
				'S',															-- "TipoIngreso" 					,
				p_IdUsuario,													-- "IdUsuario" 						,
				'VIG',															-- "IdEstado"						,
				0,																-- "Valor"							,
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
				'MAN',															-- "Origen"
				0, -- paridad
				Archivo."MONEDAPAGO", 
				'', -- idemisornuevo
				'N',
				0, 
				0
			FROM :p_DataArchivo														AS Archivo
				INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 	AS Instrumento
					ON Trim(Archivo."NEMOTECNICO") = Trim(Instrumento."Nemotecnico")
				INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 	AS Instrumento2
					ON Trim(Archivo."NEMOTECNICO") = Trim(Instrumento2."Nemotecnico")
				inner JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 	AS Emisor
					ON Emisor."Nemotecnico" = Trim(Archivo."NEMOTECNICO");
	END;
END;
GO