SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Custodia.BD.Servicios::CargarPosicionesArchivoL026";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::CargarPosicionesArchivoL026" (

	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
    IN p_IdUsuario				BIGINT 			default 0,
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca
	IN p_Fecha					DATE,

	-- Parametros entrada del servicio privado
	IN p_DataArchivo			TABLE (	"FECHA"									NVARCHAR(10),
										"CUENTA"								NVARCHAR(8),
										"INSTRUMENTO"							NVARCHAR(16),
										"TIPOINSTRUMENTO"						NVARCHAR(12),
										"MONEDA"								NVARCHAR(12),
										"EMISOR"								NVARCHAR(12),
										"POSICIONDISPONIBLE"					DECIMAL(26,8),
										"POSICIONDISPONIBLEVALORIZADAPARAAFP"	DECIMAL(26,8),
										"POSICIONBLOQUEADA"						DECIMAL(26,8),
										"POSICIONBLOQUEADAVALORIZADAPARAAFP"	DECIMAL(26,8),
										"RUTBENEFICIARIO"						NVARCHAR(12),
										"NOMBREBENEFICIARIO"					NVARCHAR(50),
										"MENSAJEVALIDACION" 					NVARCHAR(200)),

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_AdvertenciasDetalle  	 	 	table ("Codigo" nvarchar(41),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresContador  	 	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	 	table ("Codigo" nvarchar(41),"Descripcion" nvarchar(5000))
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
		DECLARE	v_FechaSistemaCreacion		TIMESTAMP;
		DECLARE	v_countTransacciones		BIGINT;
		DECLARE	v_countOrdenes				BIGINT;
		DECLARE	v_countClientes				BIGINT;
		DECLARE	v_countAsignacion			BIGINT;
		DECLARE v_ErroresContadorArchivo	BIGINT;
		DECLARE v_ErroresContadorCustodia	BIGINT;	
		DECLARE v_DCVCuentaPropia			NVARCHAR(40):='';
		DECLARE v_DCVCuentaTerceros			NVARCHAR(40):='';
		DECLARE v_DCVCuentaRelacionado		NVARCHAR(40):='';
		DECLARE v_DCVCuentaMandante			NVARCHAR(40):='';
	
		--------------------
		-- Validaciones
		-- Si se encuentra algún registro, no se puede cargar el archivo.
		--------------------

		BEGIN -- VALIDA ARCHIVO

			SELECT COUNT(1) INTO v_ErroresContadorArchivo
			FROM :p_DataArchivo			AS Archivo
			WHERE
			   TRIM(Archivo."INSTRUMENTO")			= 	''		-- NEMO NO PUEDE VENIR EN BLANCO
			OR TRIM(Archivo."CUENTA")				=	''		-- CUENTA NO PUEDE VENIR EN BLANCO
			OR TRIM(Archivo."MENSAJEVALIDACION")	!=	'OK';	-- VALIDACIÓN CON ERROR

			IF(v_ErroresContadorArchivo > 0) THEN
				p_out_ErroresContador = 10;
				p_out_ErroresDetalle = select 'ARCHIVO_L026_CON_ERRORES_REVISAR' as "Codigo",'ARCHIVO_L026_CON_ERRORES_REVISAR' as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			SELECT COUNT(1) INTO v_ErroresContadorCustodia
			FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia"	AS Custodia
			WHERE Custodia."FechaCuadraturaCustodia"	= p_Fecha
			  AND Custodia."IdEstado"					= 'VIG';

			IF(v_ErroresContadorCustodia < 1) THEN
				p_out_ErroresContador = 10;
				p_out_ErroresDetalle = select 'CARGA_DE_CUADRATURA_CUSTODIA_NO_REALIZADA' as "Codigo",'CARGA_DE_CUADRATURA_CUSTODIA_NO_REALIZADA' as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;

		END;

		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);

		--- OBTENEMOS CUENTAS CONFIGURADAS EN EL SISTEMA

		SELECT "Valor" INTO v_DCVCuentaPropia
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultaConfiguracionCustodia"
		WHERE "CodigoParametro" = 'CuentaDCVCuentaPropia' 
			AND "IdEmpresa" = :p_IdEmpresa;

		SELECT "Valor" INTO v_DCVCuentaTerceros
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultaConfiguracionCustodia"
		WHERE "CodigoParametro" = 'CuentaDCVCuentaTerceros' 
			AND "IdEmpresa" = :p_IdEmpresa;

		SELECT "Valor" INTO v_DCVCuentaRelacionado
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultaConfiguracionCustodia"
		WHERE "CodigoParametro" = 'CuentaDCVCuentaRelacionado' 
			AND "IdEmpresa" = :p_IdEmpresa;
	
		-----------------------------------------------------------------
		-- PREPARAMOS INFORMACIÓN PARA POSTERIORMENTE PODER ACTUALIZAR.
		-----------------------------------------------------------------
 		v_CargaDePosiciones = 
			SELECT 	Custodia."Nemotecnico" 				as Nemotecnico,
					Custodia."CuentaBoveda" 			as CuentaBoveda,
					Custodia."FechaCuadraturaCustodia" 	as "FechaCuadraturaCustodia",
					Archivo."RUTBENEFICIARIO"			as RutBeneficiario,
					Archivo."NOMBREBENEFICIARIO"		as NombreBeneficiario,
					SUM (CASE
	   					WHEN (Custodia."CuentaBoveda" = v_DCVCuentaPropia AND Archivo."POSICIONDISPONIBLE" > 0) 
	   						THEN Archivo."POSICIONDISPONIBLE"
	   					WHEN (Custodia."CuentaBoveda" = v_DCVCuentaTerceros AND Archivo."POSICIONDISPONIBLE" > 0) 
	   						THEN Archivo."POSICIONDISPONIBLE"
	   					WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRelacionado AND Archivo."POSICIONDISPONIBLE" > 0)
	   						THEN Archivo."POSICIONDISPONIBLE"
	   					WHEN (Custodia."CuentaBoveda" = (SELECT "Codigo" 
	   													FROM "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio"
														WHERE "Codigo" = Archivo."CUENTA") 
	   													AND Archivo."POSICIONDISPONIBLE" > 0)
	   						THEN Archivo."POSICIONDISPONIBLE"
	   					ELSE
	   						0
   					END) AS PosicionDisponible,
					SUM (CASE
						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaPropia AND Archivo."POSICIONDISPONIBLEVALORIZADAPARAAFP" > 0) 
							THEN Archivo."POSICIONDISPONIBLEVALORIZADAPARAAFP"
						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaTerceros AND Archivo."POSICIONDISPONIBLEVALORIZADAPARAAFP" > 0) 
							THEN Archivo."POSICIONDISPONIBLEVALORIZADAPARAAFP"
						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRelacionado AND Archivo."POSICIONDISPONIBLEVALORIZADAPARAAFP" > 0) 
							THEN Archivo."POSICIONDISPONIBLEVALORIZADAPARAAFP"
   						ELSE
   							0
   					END) AS PosicionDisponibleValorizadaParaAFP	,
					SUM (CASE
   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaPropia AND Archivo."POSICIONBLOQUEADA" > 0) 
   							THEN Archivo."POSICIONBLOQUEADA"
   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaTerceros AND Archivo."POSICIONBLOQUEADA" > 0) 
   							THEN Archivo."POSICIONBLOQUEADA"
   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRelacionado AND Archivo."POSICIONBLOQUEADA" > 0) 
   							THEN Archivo."POSICIONBLOQUEADA"
   						ELSE
   							0
   					END) AS PosicionBloqueada,
					SUM (CASE
   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaPropia AND Archivo."POSICIONBLOQUEADAVALORIZADAPARAAFP" > 0) 
   							THEN Archivo."POSICIONBLOQUEADAVALORIZADAPARAAFP"
   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaTerceros AND Archivo."POSICIONBLOQUEADAVALORIZADAPARAAFP" > 0) 
   							THEN Archivo."POSICIONBLOQUEADAVALORIZADAPARAAFP"
   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRelacionado AND Archivo."POSICIONBLOQUEADAVALORIZADAPARAAFP" > 0) 
   							THEN Archivo."POSICIONBLOQUEADAVALORIZADAPARAAFP"
   						ELSE
   							0
   					END) AS PosicionBloqueadaValorizadaParaAFP
   					
		FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia" AS Custodia
			INNER JOIN :p_DataArchivo 					AS Archivo
				ON RTRIM(Archivo."CUENTA")	= RTRIM(Custodia."CuentaBoveda")
			   AND Rtrim(Archivo."INSTRUMENTO")		= Custodia."Nemotecnico"
		WHERE Custodia."FechaCuadraturaCustodia"	= to_date(Archivo."FECHA")
		  AND Custodia."IdBoveda"					= 'DCV'
		  AND Custodia."IdEstado"					= 'VIG'
		 GROUP BY Custodia."Nemotecnico", Custodia."CuentaBoveda",Custodia."FechaCuadraturaCustodia",Archivo."RUTBENEFICIARIO",Archivo."NOMBREBENEFICIARIO";
		  
		-----------------------------------------------------------------
		-- ACTUALIZACIÓN DE POSICIONES EN TABLA DE CUADRATURA DE CUSTODIA.
		-----------------------------------------------------------------
		  
		UPDATE Custodia SET
			Custodia."PosicionBoveda"						= Posiciones.PosicionDisponible,
			Custodia."PosicionBovedaValorizadaParaAFP"		= Posiciones.PosicionDisponibleValorizadaParaAFP,
			Custodia."PosicionBloqueada"					= Posiciones.PosicionBloqueada,
			Custodia."PosicionBloqueadaValorizadaParaAFP"	= Posiciones.PosicionBloqueadaValorizadaParaAFP,
			Custodia."PosicionTotal"						= Posiciones.PosicionDisponible + Posiciones.PosicionBloqueada ,
			Custodia."Diferencia"							= Custodia."CantidadTotal" - (Posiciones.PosicionDisponible + Posiciones.PosicionBloqueada ),
			Custodia."RutBeneficiario"						= Posiciones.RutBeneficiario,
			Custodia."NombreBeneficiario"					= Posiciones.NombreBeneficiario
		FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia" AS Custodia
			INNER JOIN :v_CargaDePosiciones				AS Posiciones
				ON RTRIM(Posiciones.CuentaBoveda)		= RTRIM(Custodia."CuentaBoveda")
			   AND Rtrim(Posiciones.Nemotecnico)	= Custodia."Nemotecnico"
		WHERE Custodia."FechaCuadraturaCustodia"	= Posiciones."FechaCuadraturaCustodia"
		  AND Custodia."IdBoveda"					= 'DCV'
		  AND Custodia."IdEstado"					= 'VIG';

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