SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Custodia.BD.Servicios::EVC.CargarSorteoLetrasDesdeArchivoL043";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CargarSorteoLetrasDesdeArchivoL043" (

	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
    IN p_IdUsuario				BIGINT 			default 0,
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_DataArchivo			TABLE (	"DEPOSITANTE"		DECIMAL(10,0),
										"MONEDA"			DECIMAL(2,0),
										"GLOSAMONEDA"		NVARCHAR(10),
										"EMISORPAGADOR"		DECIMAL(5,0),
										"EMISOR"			DECIMAL(5,0),
										"INSTRUMENTO"		NVARCHAR(2),
										"GLOSAINSTRUMENTO"	NVARCHAR(10),
										"EVENTO"			NVARCHAR(8),
										"NEMO"				NVARCHAR(12),
										"BLOQUEO"			DECIMAL(6,0),
										"SORTEADO"			DECIMAL(12,4),
										"APAGAR"			DECIMAL(12,4),
										"APAGARPESOS"		DECIMAL(12,0),
										"MENSAJEVALIDACION" NVARCHAR(200)),

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
				Archivo.MONEDA		<=	0	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR TRIM(Archivo.NEMO)	= 	''	-- NEMO NO PUEDE VENIR EN BLANCO
			OR Archivo.SORTEADO		<=	0	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR Archivo.APAGAR		<=	0	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO
			OR Archivo.APAGARPESOS	<=	0;	-- MONEDA NO PUEDE SER INFERIOR O IGUAL A CERO

			IF(p_out_ErroresContador > 0) THEN
				p_out_ErroresDetalle = select 'ARCHIVO_SORTEO_LETRAS_CON_ERRORES_REVISAR' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;

		END;


		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);

		--------------------------------------------------
		--Insert de Sorteos de letras en evento de capital
		--------------------------------------------------

		INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapital"
			    (
				"IdEventoCapital"    				,
				"IdEmpresa"    				 		,
				"IdTipoEventoCapital"				,
				"Secuencia"							,
				"Descripcion"						,
				"EsInscrito"						,
				"IdClasificacionTipoInstrumento"	,
				"IdTipoInstrumento" 				,
				"IdInstrumento" 					,
				"Nemo"								,
				"Factor"							,
				"Valor"								,
				"IdMoneda"							,
				"IdInstrumentoNuevo"				,
				"NemoNuevo"							,
				"FactorNuevo"						,
				"FechaActualizacion"				,
				"FechaLimite"						,
				"FechaPago"							,
				"NumeroDividendo"					,
				"IdTipoDividendo"					,
				"PrecioAnterior"					,
				"FechaHoraIngresoRegistro"			,
				"FechaHoraUltimaActualizacion"		,
				"TipoIngreso" 						,
				"IdUsuario" 						,
				"CuentaDepositanteSorteoLetras"		,
				"MonedaSorteoLetras"				,
				"GlosaMonedaSorteoLetras"			,
				"EmisorPagadorSorteoLetras"			,
				"EmisorSorteoLetras"				,
				"InstrumentoSorteoLetras"			,
				"GlosaInstrumentoSorteoLetras"		,
				"BloqueoSorteoLetras"				,
				"SorteadoSorteoLetras"				,
				"APagarSorteoLetras"				,
				"APagarPesosSorteoLetras"			,
				"IdEstado" 							,
				"Origen")
	   	 	Select 	
				"BSCL.Custodia.BD.Secuencias::EventoCapital".NEXTVAL,			-- "IdEventoCapital"    			,
				p_IdEmpresa,													-- "IdEmpresa"    				 	,
				'SL',															-- "IdTipoEventoCapital"			,
				"BSCL.Custodia.BD.Secuencias::EventoCapitalSecuencia".NEXTVAL,	-- "Secuencia"						,
				'Sorteo de Letras DCV '|| Archivo."NEMO",						-- "Descripcion"					,
				'N',															-- "EsInscrito"						,
				Instrumento."IdClasificacionTipoInstrumento",					-- "IdClasificacionTipoInstrumento"	,
				Instrumento."IdTipoInstrumento",								-- "IdTipoInstrumento" 				,
				Instrumento."IdInstrumento",									-- "IdInstrumento" 					,
				Instrumento."Nemotecnico",										-- "Nemo"							,
				0,																-- "Factor"							,
				0,																-- "Valor"							,
				'CLP',															-- "IdMoneda"						,
				0,																-- "IdInstrumentoNuevo"				,
				'',																-- "NemoNuevo"						,
				0,																-- "FactorNuevo"					,
				'0001-01-01',													-- "FechaActualizacion"				,
				TO_DATE(SUBSTRING (Archivo."EVENTO",1,4)||'-'||SUBSTRING (Archivo."EVENTO",5,2)||'-'||SUBSTRING (Archivo."EVENTO",7,2), 'YYYY-MM-DD'),	-- "FechaLimite"					,
				TO_DATE(SUBSTRING (Archivo."EVENTO",1,4)||'-'||SUBSTRING (Archivo."EVENTO",5,2)||'-'||SUBSTRING (Archivo."EVENTO",7,2), 'YYYY-MM-DD'),	-- "FechaPago"						,
				0,																-- "NumeroDividendo"				,
				'',																-- "IdTipoDividendo"				,
				0,																-- "PrecioAnterior"					,
				Current_UTCTIMESTAMP,											-- "FechaHoraIngresoRegistro"		,
				Current_UTCTIMESTAMP, 											-- "FechaHoraUltimaActualizacion"	,
				'S',															-- "TipoIngreso" 					,
				p_IdUsuario,													-- "IdUsuario" 						,
				Archivo."DEPOSITANTE",											-- "CuentaDepositanteSorteoLetras"	,
				Archivo."MONEDA",												-- "MonedaSorteoLetras"				,
				Archivo."GLOSAMONEDA",											-- "GlosaMonedaSorteoLetras"		,
				Archivo."EMISORPAGADOR",										-- "EmisorPagadorSorteoLetras"		,
				Archivo."EMISOR",												-- "EmisorSorteoLetras"				,
				Archivo."INSTRUMENTO",											-- "InstrumentoSorteoLetras"		,
				Archivo."GLOSAINSTRUMENTO",										-- "GlosaInstrumentoSorteoLetras"	,
				Archivo."BLOQUEO",												-- "BloqueoSorteoLetras"			,
				Archivo."SORTEADO",												-- "SorteadoSorteoLetras"			,
				Archivo."APAGAR",												-- "APagarSorteoLetras"				,
				Archivo."APAGARPESOS",											-- "APagarPesosSorteoLetras"		,
				'VIG',															-- "IdEstado"						,
				'MAN'															-- "Origen"
			FROM :p_DataArchivo														AS Archivo
				INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 	AS Instrumento
					ON Trim(Archivo."NEMO") = Trim(Instrumento."Nemotecnico");


		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------

		--------------------
		--FIN SP 
		--------------------
	END;
END;
GO