SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.EliminarPreComprobante";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Eliminación de Registro
--	
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 17/11/2016
--  Id Servicio = 12056
--  
"BSCL.Contabilidad.BD.Servicios::ITZ.EliminarPreComprobante" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),
    --------------------------------------------------------------------------------------------
	IN p_IdPreComprobante			BIGINT,
	IN p_IdPreComprobanteAsiento	BIGINT,
	IN p_IdEvento					BIGINT,
	IN p_Origen						NVARCHAR(2),
	--------------------------------------- Parametros de Salida -------------------------------------------------------------------------
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
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 

	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", 'N' AS "RequiereAutorizacion", '' AS "ValorParametro",'' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"ValorParametro" NVARCHAR(100),"NombreParametro" NVARCHAR(40)) DEFAULT 
												SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY WHERE 1=2;

	--DECLARE v_count_operaciones			BIGINT;

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		-------------------------------------------------------------------------------------------------------------------
		---------------------------------------------------------------------------------------------------

		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador 
		  FROM :v_out_ErroresDetalle;

		-------------------------------------------------------------------------------------------------------------------
		-------------------------------------------------------------------------------------------------------------------
		
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
		DECLARE v_Estado 					NVARCHAR(1);
    	--PARAMETROS REGLAS NEGOCIO
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_IdTipoContabilidad		BIGINT;
		DECLARE v_IdDefinicionAsiento		BIGINT;
		DECLARE v_CantidadCondicionados		BIGINT;
		DECLARE v_EstadoPreComprobante		NVARCHAR(1);

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
		
		
		
		
		--Si se desea eliminar el PreComprobante
		--IF @pOrigen = 'PR' and @Estado <> 'C' BEGIN
		IF :p_Origen = 'PR' THEN
		
			SELECT "Estado"
			INTO v_EstadoPreComprobante
			FROM "BSCL.Contabilidad.BD::ITZ.PreComprobante"
			WHERE "IdPreComprobante" =  :p_IdPreComprobante;		
			
/*			IF :v_EstadoPreComprobante = 'C'   THEN
				v_out_ErroresDetalle = SELECT 0 	AS "IdInterno",
				                              'PRECOMPROBANTE_YA_ESTA_CONTABILIZADO' 	AS "Codigo",
				                              '' 	AS "ValorParametro",
				                              '' 	AS "NombreParametro"
				                         FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			
			END IF;*/
			
			IF :v_EstadoPreComprobante = 'A'   THEN
				v_out_ErroresDetalle = SELECT 0 	AS "IdInterno",
				                              'PRECOMPROBANTE_YA_ESTA_ANULADO' 	AS "Codigo",
				                              '' 	AS "ValorParametro",
				                              '' 	AS "NombreParametro"
				                         FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			
			END IF;
		
			UPDATE "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento
			   SET "Estado" = 'A'
			  FROM "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento
			  			LEFT OUTER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento ON
								CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento" = CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento"
			 WHERE CTB_itzPreComprobanteAsiento."IdPreComprobante" = :p_IdPreComprobante;

			--------------------------------------------------
			-- Anula Asiento ---------------------------------
			--------------------------------------------------
			UPDATE "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento"
			   SET "Estado" = 'A'
			 WHERE "IdPreComprobante" 	= :p_IdPreComprobante;
		
			--------------------------------------------------
			-- Anula PreComprobante --------------------------
			--------------------------------------------------
			UPDATE "BSCL.Contabilidad.BD::ITZ.PreComprobante"
			   SET "Estado" = 'A'
			 WHERE "IdPreComprobante" 	= :p_IdPreComprobante;
		END IF;
		
		IF :p_Origen = 'AS' THEN
			-- Solo Puede eliminar los asientos de un Estado "I"
			SELECT "Estado"
			  INTO v_Estado
			  FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento"
			 WHERE "IdPreComprobanteAsiento" 	= :p_IdPreComprobanteAsiento;

			--------------------------------------------------
			-- Puede eliminar los asientos en Estado "I" -----
			--------------------------------------------------
			IF :v_Estado = 'I' THEN
				----------------------------------------------------
				-- Elimina relación Evento PreComprobante Asiento --
				----------------------------------------------------
				DELETE FROM "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento"
				 WHERE "IdPreComprobanteAsiento" 	= :p_IdPreComprobanteAsiento
				   AND "Estado" 					= 'I';
				--------------------------------------------------
				-- Elimina PreComprobante Asiento ----------------
				--------------------------------------------------
				DELETE FROM "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento"
				 WHERE "IdPreComprobanteAsiento" 	= :p_IdPreComprobanteAsiento
				   AND "Estado" 					= 'I';
			END IF;
		END IF;
		
		IF :p_Origen = 'DE' THEN
			SELECT "Estado"
			  INTO v_Estado
			  FROM "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento"
			 WHERE "IdPreComprobanteAsiento" 	= :p_IdPreComprobanteAsiento
			   AND "IdEvento" 					= :p_IdEvento;

			--------------------------------------------------
			-- Puede eliminar los asientos en Estado "I" -----
			--------------------------------------------------
			IF :v_Estado = 'I' THEN
				----------------------------------------------------
				-- Elimina relación Evento PreComprobante Asiento --
				----------------------------------------------------
				DELETE FROM "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento"
				 WHERE "IdPreComprobanteAsiento" 	= :p_IdPreComprobanteAsiento
				   AND "IdEvento" 					= :p_IdEvento
				   AND "Estado" 					= 'I';
			END IF;
		END IF;
	END;
END;
