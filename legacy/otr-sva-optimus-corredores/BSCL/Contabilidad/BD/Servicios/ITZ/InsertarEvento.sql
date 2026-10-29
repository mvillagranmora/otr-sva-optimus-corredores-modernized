SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.InsertarEvento";

CREATE PROCEDURE 
-- 
--  Proposito: Inserción Registro
--			   
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 02/11/2016
--  Id Servicio = 12048
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.InsertarEvento" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),
    --------------------------------------------------------------------------------------------   
    IN p_IdTipoEvento  				NVARCHAR(40),
	IN p_IdCentralizacion 			BIGINT,
	IN p_IdMovimiento  				BIGINT,
    IN p_FechaContable 				NVARCHAR(10),
    IN p_Monto   					DECIMAL(18,2),
    IN p_CodigoMoneda  				NVARCHAR(3),
    --IN p_FechaGeneracion 			NVARCHAR(10),
    --IN p_HoraGeneracion 		    NVARCHAR(8),
    IN p_Estado						NVARCHAR(1),
	--------------------------------------- Parametros de Salida -------------------------------------------------------------------------
	OUT p_out_IdEvento				BIGINT, 
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
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		
		DECLARE v_ExisteEvento 				BIGINT;
		DECLARE v_GlosaError 				NVARCHAR(40);

		DECLARE v_FechaGeneracion 			NVARCHAR(10);
		DECLARE v_HoraGeneracion			NVARCHAR(8);
		
		
		v_ExisteEvento := 0;
		
		v_FechaGeneracion := CAST(NOW() AS NVARCHAR(10));
		v_HoraGeneracion := CAST(NOW() AS NVARCHAR(8));

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
		
		SELECT COUNT(1)
		  INTO v_ExisteEvento
		  FROM "BSCL.Contabilidad.BD::ITZ.Evento"
		 WHERE "IdCentralizacion" 	= :p_IdCentralizacion
		   AND "IdMovimiento"  		= :p_IdMovimiento
		   AND "FechaContable"  	= :p_FechaContable
		   AND "IdTipoEvento"  		= :p_IdTipoEvento
		   AND "Estado"    			= 'V';
    
		IF :v_ExisteEvento = 0 THEN
			IF :p_Monto <> 0 THEN
				-- Obtiene Id del Movimiento ---
				SELECT "BSCL.Contabilidad.BD.Secuencias::IdEvento".NEXTVAL
				  INTO p_out_IdEvento
				  FROM DUMMY;
				
				INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" 
						(
						"IdEvento",
						"IdTipoEvento",
						"IdEmpresa", 
						"IdCentralizacion", 
						"IdMovimiento", 
						"FechaContable", 
						"Monto", 
						"CodigoMoneda", 
						"CodigoUsuario", 
						"FechaProceso", 
						"HoraProceso", 
						"Estado"
						) VALUES (
						:p_out_IdEvento,
					 	:p_IdTipoEvento,
					 	:p_IdEmpresa,
					 	:p_IdCentralizacion,
					 	:p_IdMovimiento,
					 	:p_FechaContable,
					 	ABS(:p_Monto),
					 	:p_CodigoMoneda,
					 	:p_IdUsuario,
					 	:v_FechaGeneracion,
					 	:v_HoraGeneracion,
					 	:p_Estado
						);
			END IF;
		END IF;
	END;
END;
