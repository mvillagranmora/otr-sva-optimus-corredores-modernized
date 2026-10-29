SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Custodia.BD.Servicios::CargarPosicionesOtrosSaldos";
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::CargarPosicionesOtrosSaldos" (

	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
    IN p_IdUsuario				BIGINT 			default 0,
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca
--	IN p_Fecha					NVARCHAR(10) default '0001-01-01',
	IN p_Fecha					DATE,

	-- Parametros entrada del servicio privado
	IN p_DataArchivo			TABLE (	"BOVEDA"						NVARCHAR(3),
										"INSTRUMENTO"					NVARCHAR(13),
										"POSICIONDISPONIBLE"			DECIMAL(26,8),
										"MENSAJEVALIDACION" 			NVARCHAR(200)),

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
		DECLARE v_DCVCuentaTerceros			NVARCHAR(40):='';
		DECLARE v_FechaSistema				NVARCHAR(20);

		--------------------
		-- Validaciones
		-- Si se encuentra algún registro, no se puede cargar el archivo.
		--------------------

		BEGIN -- VALIDA ARCHIVO

			SELECT COUNT(1) INTO v_ErroresContadorArchivo
			FROM :p_DataArchivo			AS Archivo
			WHERE
			   TRIM(Archivo."INSTRUMENTO")			= 	''		-- NEMO NO PUEDE VENIR EN BLANCO
			OR TRIM(Archivo."BOVEDA")				=	''		-- BOVEDA NO PUEDE VENIR EN BLANCO
			OR Archivo."POSICIONDISPONIBLE"			=	0		-- NO PUEDE VENIR SIN POSICIÓN
			OR TRIM(Archivo."MENSAJEVALIDACION")	!=	'OK';	-- VALIDACIÓN CON ERROR


			IF(v_ErroresContadorArchivo > 0) THEN
				p_out_ErroresContador = 10;
				p_out_ErroresDetalle = select 'ARCHIVO_OTROS_SALDOS_CON_ERRORES_REVISAR' as "Codigo",'ARCHIVO_OTROS_SALDOS_CON_ERRORES_REVISAR' as "Descripcion" from dummy;
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

		SELECT "Valor" INTO v_DCVCuentaTerceros
				FROM "BSCL.Custodia.BD::CUS.Configuracion"
				WHERE "CodigoParametro" = 'CuentaDCVCuentaTerceros'
			  	AND "IdEmpresa" = p_IdEmpresa; 
	
		-----------------------------------------------------------------
		-- PREPARAMOS INFORMACIÓN PARA POSTERIORMENTE PODER ACTUALIZAR.
		-----------------------------------------------------------------
		v_CargaDePosiciones = 
			SELECT 	Custodia."Nemotecnico" 				as Nemotecnico,
					Custodia."IdBoveda"		 			as IdBoveda,
					Custodia."FechaCuadraturaCustodia" 	as FechaCuadraturaCustodia,
					Archivo."POSICIONDISPONIBLE"		AS PosicionBoveda
		FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia" AS Custodia
			INNER JOIN :p_DataArchivo 					AS Archivo
				ON Archivo."BOVEDA"				= Custodia."IdBoveda"
			   AND Rtrim(Archivo."INSTRUMENTO")	= Custodia."Nemotecnico"
		WHERE Custodia."FechaCuadraturaCustodia"	= p_Fecha
		  AND Custodia."IdBoveda"					!= 'DCV'
		  AND Custodia."IdEstado"					= 'VIG';
		  
		  	v_CargaActualizada = SELECT CASE "BOVEDA"
										  	WHEN 'DRE' THEN 'CIA'
										  	WHEN 'SCR' THEN 'CIA'
										  	WHEN 'PAS' THEN 'CIA'
										  	WHEN 'DCV' THEN 'CIA'
										  	WHEN 'MILA' THEN 'CIA'
										  	WHEN 'COR'	THEN 'CIA'
										ELSE "BOVEDA"
										END	AS "IdBoveda"
									  , "INSTRUMENTO" AS "Nemotecnico"
									  , SUM(COALESCE("POSICIONDISPONIBLE",0)) AS "PosicionBoveda"
								 FROM :p_DataArchivo
								 GROUP BY "BOVEDA","INSTRUMENTO"; 
											 
			v_CargaParaActualizar = SELECT "IdBoveda"
									   , "Nemotecnico"
									   , "PosicionBoveda"
								    FROM :v_CargaActualizada AS Carga
								    WHERE (Carga."IdBoveda",Carga."Nemotecnico")IN (	SELECT "IdBoveda","Nemotecnico"
												   										FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia"
												   										WHERE "FechaCuadraturaCustodia" = p_Fecha
												   										AND "IdEstado"		= 'VIG'
			  																			AND "TipoCuenta" 	= 'TER'
												   									);								 
								 
								 
			v_CargaParaIngresar = SELECT "IdBoveda"
									   , "Nemotecnico"
									   , "PosicionBoveda"
								  FROM :v_CargaActualizada AS Carga
								  WHERE (Carga."IdBoveda",Carga."Nemotecnico") NOT IN (	SELECT "IdBoveda","Nemotecnico"
												   										FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia"
												   										WHERE "FechaCuadraturaCustodia" = p_Fecha
												   										AND "IdEstado"		= 'VIG'
			  																			AND "TipoCuenta" 	= 'TER'
			  																			);  	

			 		  
		-----------------------------------------------------------------
		-- ACTUALIZACIÓN DE POSICIONES EN TABLA DE CUADRATURA DE CUSTODIA.
		-----------------------------------------------------------------
		
		  UPDATE Custodia SET
				Custodia."PosicionBoveda"		= Archivo."PosicionBoveda",
				Custodia."PosicionTotal"		= (Archivo."PosicionBoveda" + Custodia."PosicionBloqueada" + Custodia."PosicionReventa" + Custodia."PosicionRecompra"),
				Custodia."Diferencia"			= Custodia."CantidadTotal" - (Archivo."PosicionBoveda" + Custodia."PosicionBloqueada" + Custodia."PosicionReventa" + Custodia."PosicionRecompra")			  
			FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia" AS Custodia
			INNER JOIN :v_CargaParaActualizar 					AS Archivo 
					ON Archivo."IdBoveda" 			= Custodia."IdBoveda" 
				   AND Archivo."Nemotecnico" 		= Custodia."Nemotecnico"
			WHERE Custodia."IdEstado"					= 'VIG'
			  AND Custodia."TipoCuenta" 				= 'TER'
			  AND Custodia."FechaCuadraturaCustodia"	= p_Fecha;
			 		  
		------------------------------------------------------------------
		-- INGRESO LOS REGISTROS QUE NO EXISTEN EN LA CUADRATURA
		------------------------------------------------------------------		  
		INSERT INTO "BSCL.Custodia.BD::CUS.CuadraturaCustodia"(
				 "IdCuadraturaCustodia"
				,"IdBoveda"
			    ,"IdInstrumento"
				,"IdTipoInstrumento"
				,"IdClasificacionTipoInstrumento"
				,"Nemotecnico"
				,"PosicionBoveda"
				,"FechaCuadraturaCustodia"
				,"TipoCuenta"
				,"IdEstado"
				,"IdUsuario"
				,"CuentaBoveda"
				,"FechaModificacion"
				,"NombreBeneficiario"
				,"RutBeneficiario"
				,"PosicionTotal"
				,"Diferencia"
			)
			SELECT "BSCL.Custodia.BD.Secuencias::CuadraturaCustodia".NEXTVAL AS "IdCuadraturaCustodia"
			     , Carga."IdBoveda"
				 , COALESCE(Instrumento."IdInstrumento",0)
				 , COALESCE(Instrumento."IdTipoInstrumento",'')
				 , COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'')
			     , Carga."Nemotecnico"
			     , CASE WHEN Carga."IdBoveda" IN ('CIA','BCS','BOV','BEC','COR') THEN
   				   		Carga."PosicionBoveda"
   				   ELSE 0
   				   END
			     , :p_Fecha
			     , 'TER'
			     , 'VIG'
			     , :p_IdUsuario
			     , v_DCVCuentaTerceros
			     , v_FechaSistemaCreacion
			     ,''
			     ,''
			     ,Carga."PosicionBoveda"
			     ,-(Carga."PosicionBoveda")
			FROM :v_CargaParaIngresar AS Carga
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
					ON Instrumento."Nemotecnico" = Carga."Nemotecnico"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
					ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento";  
		--------------------
		-- Updaate post ingreso para las diferencias de las cuadraturas ya ingresadas
		--------------------		
			UPDATE Custodia SET
				Custodia."Diferencia"			= Custodia."CantidadTotal" - (Archivo."PosicionBoveda" + Custodia."PosicionBloqueada" + Custodia."PosicionReventa" + Custodia."PosicionRecompra")			  
			FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia" AS Custodia
			INNER JOIN :v_CargaParaIngresar 					AS Archivo 
					ON Archivo."IdBoveda" 			= Custodia."IdBoveda" 
				   AND Archivo."Nemotecnico" 		= Custodia."Nemotecnico"
			WHERE Custodia."IdEstado"					= 'VIG'
			  AND Custodia."TipoCuenta" 				= 'TER'
			  AND Custodia."FechaCuadraturaCustodia"	= p_Fecha;
		--------------------
		--Lógica de Negocio 
		--------------------

		--------------------
		--FIN SP 
		--------------------
	END;
END;