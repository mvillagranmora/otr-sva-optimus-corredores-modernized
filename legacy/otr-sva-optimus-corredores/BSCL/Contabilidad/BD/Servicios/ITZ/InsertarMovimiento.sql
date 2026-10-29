SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.InsertarMovimiento";

CREATE PROCEDURE 
-- 
--  Proposito: Inserta Registro
--	
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 02/11/2016
--  Id Servicio = 12049
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.InsertarMovimiento" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),
    ----------------------------------------------------------------------------------------------------------------------
	-- Parametros para el Movimiento -------------
	----------------------------------------------------------------------------------------------------------------------
	IN p_IdTipoMovimiento			NVARCHAR(10),
	IN p_Numero						BIGINT,
	IN p_Linea      				BIGINT,
	IN p_Fecha						NVARCHAR(10),
	IN p_Rut						NVARCHAR(40),
	IN p_Cuenta						BIGINT,
	IN p_Ejercicio					BIGINT,
	IN p_Periodo					NVARCHAR(7),
	IN p_IdCentralizacion			BIGINT,
	IN p_Monto						DECIMAL(18,2),
	IN p_Estado						NVARCHAR(1),
	----------------------------------------------------------------------------------------------------------------------
	-- Parametros para el detalle del movimiento ---------------------
    ----------------------------------------------------------------------------------------------------------------------
	IN p_TabMovimientoDato	TABLE(
								  "IdTipoDatoEvento"		NVARCHAR(40),
								  "ValorVarchar"			NVARCHAR(60),
								  "ValorDecimal"			DECIMAL(18,4),
								  "Estado"					NVARCHAR(1)
								  ),
    ----------------------------------------------------------------------------------------------------------------------
	-- Parametros de Salida
	OUT p_out_IdMovimiento			BIGINT,
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
 )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL"
	AS

BEGIN
	--DECLARE v_IdGrupoLog BIGINT;
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100), "Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  				NVARCHAR(520);	        	

	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 as "IdInterno",'' as "Codigo",'' as "RequiereAutorizacion",'' as "ValorParametro",'' as  "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 as "IdInterno",'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" FROM DUMMY WHERE 1=2;

	DECLARE v_count_operaciones			BIGINT;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador 
		  FROM :v_out_ErroresDetalle;
		
		SELECT COUNT(1) INTO p_out_AdvertenciasContador 
		  FROM :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes = SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
  								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_ErroresDetalle
								 UNION ALL
								 SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
								        "ValorParametro" 	AS "ValorDato" 
								   FROM :v_out_AdvertenciasDetalle;

		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									   errores."Codigo",
									   msj."Descripcion"
 								  FROM :v_out_ErroresDetalle errores INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								  														errores."Codigo" = msj."Codigo";

		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									       adv."Codigo",
 									       adv."RequiereAutorizacion",
 									       msj."Descripcion"
 								    FROM :v_out_AdvertenciasDetalle adv INNER JOIN :v_out_ErroresDetalleMensajes msj on 
 								    													adv."Codigo" = msj."Codigo";
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio      
    	--PARAMETROS REGLAS NEGOCIO
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_IdMovimiento				BIGINT;
		DECLARE v_MovimientoCentralizado	BIGINT;

		--*******************************
		--*VALIDACION REGLAS BASICAS *
		--*******************************
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa
		                                                                 ,:p_IdUsuario
		                                                                 ,:p_IdModulo
		                                                                 ,:v_out_ErroresDetalle_Basicos
		                                                                 ,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 	AS "IdInterno",
			                              '' 	AS "Codigo",
			                              '' 	AS "ValorParametro",
			                              '' 	AS "NombreParametro"
			                         FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		IF :p_Monto <> 0 THEN
			-- Valida que no se encuentra centralizado
			SELECT COUNT(1)
			  INTO v_MovimientoCentralizado
			  FROM "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_ItzMovimientoCentralizado
			 WHERE CTB_ItzMovimientoCentralizado."IdEmpresa"				= :p_IdEmpresa
			   AND CTB_ItzMovimientoCentralizado."Fecha"					= :p_Fecha
			   AND CTB_ItzMovimientoCentralizado."IdTipoMovimiento"			= :p_IdTipoMovimiento
			   AND CTB_ItzMovimientoCentralizado."Numero"					= :p_Numero
			   AND TRIM(CTB_ItzMovimientoCentralizado."DocumentoIdentidad")	= TRIM(:p_Rut)
			   AND CTB_ItzMovimientoCentralizado."Cuenta"					= :p_Cuenta
			   AND CTB_ItzMovimientoCentralizado."Estado"					= 'V';
	
			IF :v_MovimientoCentralizado = 0 THEN
				-- Obtiene Id del Movimiento ---
				SELECT "BSCL.Contabilidad.BD.Secuencias::IdMovimiento".NEXTVAL
				  INTO p_out_IdMovimiento
				  FROM DUMMY;
	
				-- Inserta el Movimiento -----------------------------------
				INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" (
																	"IdMovimiento",
																	"IdTipoMovimiento",
																	"Numero",
																	"Linea",
																	"Fecha",
																	"DocumentoIdentidad",
																	"Cuenta",
																	"IdEmpresa",
																	"Ejercicio",
																	"Periodo",
																	"IdCentralizacion",
																	"Monto",
																	"Estado"
																	) VALUES (
																	:p_out_IdMovimiento,
																	:p_IdTipoMovimiento,
																	:p_Numero,
																	:p_Linea,
																	:p_Fecha,
																	:p_Rut,
																	:p_Cuenta,
																	:p_IdEmpresa,
																	:p_Ejercicio,
																	:p_Periodo,
																	:p_IdCentralizacion,
																	:p_Monto,
																	:p_Estado
																	);
			
				--------------------------------------------------------------------------------------------------------------
				----------------         INSERTA MovimientoDato      ---------------------------------------------------------
				--------------------------------------------------------------------------------------------------------------
				INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato"	(
																				"IdMovimiento",
																				"IdTipoDatoEvento",
																				"ValorVarchar",
																				"ValorDecimal",
																				"Estado"
																				)
								SELECT :p_out_IdMovimiento,
									   "IdTipoDatoEvento",
									   "ValorVarchar",
									   "ValorDecimal",
									   "Estado"
		   						  FROM :p_TabMovimientoDato;
			END IF;
		END IF;
	END;
END;
