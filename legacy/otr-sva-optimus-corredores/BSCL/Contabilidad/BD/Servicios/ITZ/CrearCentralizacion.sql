SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.CrearCentralizacion";

CREATE PROCEDURE 
-- 
--  Proposito: Creación de Registro
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 01/11/2016
--  Id Servicio = 12042
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.CrearCentralizacion" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
	IN p_IdEmpresa          		BIGINT,
	IN p_IdUsuario          		BIGINT,
	IN p_IdModulo           		NVARCHAR(3),
	IN p_UsaReglasNegocio			NVARCHAR(1),
	--------------------------------------------------------------------------------------
	IN p_IdTipoCentralizacion		BIGINT,
	--IN p_IdEmpresa					BIGINT,
	--IN p_FechaProceso				NVARCHAR(10),
	--IN p_HoraProceso   			NVARCHAR(8),
	IN p_FechaContable 				NVARCHAR(10),
	IN p_Glosa 						NVARCHAR(50),
	IN p_CodigoUsuario 				NVARCHAR(40),
	IN p_Estado 					NVARCHAR(1),
	IN p_TipoGeneracion 			NVARCHAR(1),
	-- Parametro de Salida con ID generado
	OUT p_out_IdCentralizacion  	BIGINT,
	-- Parametros de Salida
	OUT p_out_AdvertenciasContador INT,
	OUT p_out_AdvertenciasDetalle TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador INT,
	OUT p_out_ErroresDetalle TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
	)   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN
	DECLARE v_IdGrupoLog 	BIGINT;
	DECLARE v_FechaProceso 	NVARCHAR(10);
    DECLARE v_HoraProceso 	NVARCHAR(8);

	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 

	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", 'N' AS "RequiereAutorizacion", '' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	
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
		DECLARE v_CuentaVigente				BIGINT;
		DECLARE v_resultadoRegla 			NVARCHAR(40);
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
		
		v_FechaProceso := CAST(NOW() AS NVARCHAR(10));
		v_HoraProceso := TRIM(SUBSTR(CAST(NOW() AS NVARCHAR(19)), 12, 9));

		SELECT COUNT(*)
		  INTO v_CuentaVigente
		  FROM "BSCL.Contabilidad.BD::ITZ.Centralizacion"
		 WHERE "IdTipoCentralizacion" = :p_IdTipoCentralizacion 
		   AND "FechaContable" = :p_FechaContable
		   AND "Estado" = 'V';

		SELECT "BSCL.Contabilidad.BD.Secuencias::IdCentralizacion".NEXTVAL
		  INTO p_out_IdCentralizacion
		  FROM DUMMY;

		IF :v_CuentaVigente > 0 THEN 
			INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion"(
																	"IdCentralizacion",
																	"IdTipoCentralizacion", 
																	"IdEmpresa", 
																	"FechaProceso", 
																	"HoraProceso", 
																	"FechaContable", 
																	"Glosa", 
																	"CodigoUsuario", 
																	"TipoGeneracion", 
																	"Estado") 
															VALUES (
																	:p_out_IdCentralizacion,
																	:p_IdTipoCentralizacion,
																	:p_IdEmpresa,
																	:v_FechaProceso,
																	:v_HoraProceso,
																	:p_FechaContable,
																	:p_Glosa,
																	:p_CodigoUsuario,
																	:p_TipoGeneracion,
																	:p_Estado
																	);
		END IF;
	END;
END;