SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarValoresGlosaDisponibles";

CREATE PROCEDURE
--	
--  Proposito: Vista
--	
--  Empresa: Ingeniería ProNet IT SpA.
--  Autor: Hans Muller :: HMR
--  Fecha Creacion: 01/11/2016
--  Id Servicio = 12040
--	
"BSCL.Contabilidad.BD.Servicios::ITZ.ConsultarValoresGlosaDisponibles" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
    IN p_UsaReglasNegocio			NVARCHAR(1),
    --------------------------------------------------------------------------------
	IN p_IdTipoContabilidad			BIGINT,
	IN p_IdDefinicionAsiento		BIGINT,
	IN p_TipoConsulta				NVARCHAR(1),
	-- Se agrega tabla resultado donde se devuelven los resultados
	-- Parametros salida del servicio publico
	OUT p_out_TabValoresGlosaDisponibles TABLE (
												"GlosaCampo"				NVARCHAR(120),
												"NombreCampo"				NVARCHAR(40),
												"Orden"						BIGINT,
												"Origen"					NVARCHAR(3),
												"IdDefinicionAsiento"		BIGINT,
												"IdTipoContabilidad"		BIGINT,
												"TipoDato"					NVARCHAR(1)
                                     			),
	-- Parametros de Salida
	OUT p_out_AdvertenciasContador INT,
	OUT p_out_AdvertenciasDetalle TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador INT,
	OUT p_out_ErroresDetalle TABLE ("IdInterno" BIGINT, "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY
	)
      LANGUAGE SQLSCRIPT
      SQL SECURITY INVOKER
      DEFAULT SCHEMA "BSCL"
      --READS SQL DATA
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
		DROP TABLE #TabGlosaMovimientos;
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio      
    	--PARAMETROS REGLAS NEGOCIO
		DECLARE v_resultadoRegla 			NVARCHAR(40);

		CREATE LOCAL TEMPORARY TABLE #TabGlosaMovimientos(
															"NombreCampo"				NVARCHAR(30),
															"Orden"						BIGINT,
															"Origen"					NVARCHAR(3),
															"IdDefinicionAsiento"		BIGINT,
															"IdTipoContabilidad"		BIGINT,
															"TipoDato"					NVARCHAR(1)
														  );
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



		INSERT INTO #TabGlosaMovimientos (
						"NombreCampo", 
						"Orden", 
						"Origen", 
						"IdDefinicionAsiento", 
						"IdTipoContabilidad", 
						"TipoDato"
					 ) 
					  SELECT 'Numero' 				AS "NombreCampo",
					 		 0 						AS "Orden",
					 		 'MOV' 					AS "Origen",
					 		 :p_IdDefinicionAsiento AS "IdDefinicionAsiento",
					 		 :p_IdTipoContabilidad 	AS "IdTipoContabilidad",
					 		 'D' 					AS "TipoDato"
					 	FROM DUMMY
					  UNION ALL
					  SELECT 'Linea' 				AS "NombreCampo",
					 		 0 						AS "Orden",
					 		 'MOV' 					AS "Origen",
					 		 :p_IdDefinicionAsiento AS "IdDefinicionAsiento",
					 		 :p_IdTipoContabilidad 	AS "IdTipoContabilidad",
					 		 'D' 					AS "TipoDato"
					 	FROM DUMMY
					  UNION ALL
					  SELECT 'Fecha' 				AS "NombreCampo",
					  		 0 						AS "Orden",
					  		 'MOV' 					AS "Origen",
					  		 :p_IdDefinicionAsiento AS "IdDefinicionAsiento",
					  		 :p_IdTipoContabilidad 	AS "IdTipoContabilidad",
					  		 'V' 					AS "TipoDato"
					    FROM DUMMY
					  UNION ALL
					  SELECT 'Rut' 					AS "NombreCampo",
							 0 						AS "Orden",
							 'MOV' 					AS "Origen",
							 :p_IdDefinicionAsiento AS "IdDefinicionAsiento",
							 :p_IdTipoContabilidad 	AS "IdTipoContabilidad",
							 'V' 					AS "TipoDato"
						FROM DUMMY
					  UNION ALL
					  SELECT 'Cuenta' 				AS "NombreCampo",
							 0 						AS "Orden",
							 'MOV' 					AS "Origen",
							 :p_IdDefinicionAsiento AS "IdDefinicionAsiento",
							 :p_IdTipoContabilidad 	AS "IdTipoContabilidad",
							 'D' 					AS "TipoDato"
						FROM DUMMY
					  UNION ALL
					  SELECT 'Ejercicio' 			AS "NombreCampo",
							 0 						AS "Orden",
							 'MOV' 					AS "Origen",
							 :p_IdDefinicionAsiento AS "IdDefinicionAsiento",
							 :p_IdTipoContabilidad 	AS "IdTipoContabilidad",
							 'D' 					AS "TipoDato"
						FROM DUMMY
					  UNION ALL
					  SELECT 'Periodo' 				AS "NombreCampo",
							 0 						AS "Orden",
							 'MOV' 					AS "Origen",
							 :p_IdDefinicionAsiento AS "IdDefinicionAsiento",
							 :p_IdTipoContabilidad 	AS "IdTipoContabilidad",
							 'V' 					AS "TipoDato"
						FROM DUMMY
					  UNION ALL
					  SELECT 'Monto' 				AS "NombreCampo",
							 0 						AS "Orden",
							 'MOV' 					AS "Origen",
							 :p_IdDefinicionAsiento AS "IdDefinicionAsiento",
							 :p_IdTipoContabilidad 	AS "IdTipoContabilidad",
							 'D' 					AS "TipoDato"
						FROM DUMMY;

		IF :p_TipoConsulta = 'C' THEN
			p_out_TabValoresGlosaDisponibles =
				SELECT DISTINCT '' 												AS "GlosaCampo",
					   CTB_itzEventoTipoDatoMovimientoTipo."IdTipoDatoEvento" 	AS "NombreCampo",
					   0 														AS "Orden",
					   'DAT' 													AS "Origen",
					   0 														AS "IdDefinicionAsiento",
					   :p_IdTipoContabilidad 									AS "IdTipoContabilidad",
					   CTB_itzEventoTipoDato."TipoDato" 						AS "TipoDato"
				  FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" AS CTB_itzContabilidadTipoCentralizacionTipo
					   INNER JOIN "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo" AS CTB_itzMovimientoTipoCentralizacionTipo ON
							CTB_itzMovimientoTipoCentralizacionTipo."IdTipoCentralizacion" = CTB_itzContabilidadTipoCentralizacionTipo."IdTipoCentralizacion"
					   INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo" AS CTB_itzEventoTipoDatoMovimientoTipo ON
							CTB_itzEventoTipoDatoMovimientoTipo."IdTipoMovimiento" = CTB_itzMovimientoTipoCentralizacionTipo."IdTipoMovimiento"
					   INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" AS CTB_itzEventoTipoDato ON
							CTB_itzEventoTipoDato."IdTipoDatoEvento" = CTB_itzEventoTipoDatoMovimientoTipo."IdTipoDatoEvento"
				 WHERE CTB_itzContabilidadTipoCentralizacionTipo."IdTipoContabilidad" = :p_IdTipoContabilidad
				   AND CTB_itzEventoTipoDatoMovimientoTipo."IdTipoDatoEvento" NOT IN (SELECT "IdTipoDatoEvento"
																						FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoGlosa" AS CTB_itzContabilidadTipoGlosa
																					   WHERE CTB_itzContabilidadTipoGlosa."IdTipoContabilidad" = :p_IdTipoContabilidad
																					   AND CTB_itzContabilidadTipoGlosa."Estado" = 'V')
			 UNION ALL 
				SELECT '' 								AS "GlosaCampo",
					   TabFijos."NombreCampo" 			AS "NombreCampo",
					   0 								AS "Orden",
					   TabFijos."Origen" 				AS "Origen",
					   TabFijos."IdDefinicionAsiento" 	AS "IdDefinicionAsiento",
					   TabFijos."IdTipoContabilidad"	AS "IdTipoContabilidad",
					   TabFijos."TipoDato" 				AS "TipoDato"
				  FROM #TabGlosaMovimientos AS TabFijos
				 WHERE TabFijos."NombreCampo" NOT IN (SELECT "NombreCampoMovimiento"
														FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoGlosa" AS CTB_itzContabilidadTipoGlosa
													   WHERE CTB_itzContabilidadTipoGlosa."IdTipoContabilidad" = :p_IdTipoContabilidad
													   AND CTB_itzContabilidadTipoGlosa."Estado" = 'V')
			  ORDER BY "Origen", "NombreCampo" ASC;
		END IF;

		IF :p_TipoConsulta = 'A' THEN
			p_out_TabValoresGlosaDisponibles =
				SELECT DISTINCT '' 												AS "GlosaCampo",
					   CTB_itzEventoTipoDatoMovimientoTipo."IdTipoDatoEvento" 	AS "NombreCampo",
					   0 														AS "Orden",
					   'DAT' 													AS "Origen",
					   0 														AS "IdDefinicionAsiento",
					   :p_IdTipoContabilidad 									AS "IdTipoContabilidad",
					   CTB_itzEventoTipoDato."TipoDato" 						AS "TipoDato"
				  FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" AS CTB_itzContabilidadTipoCentralizacionTipo
					   INNER JOIN "BSCL.Contabilidad.BD::ITZ.MovimientoTipoCentralizacionTipo" AS CTB_itzMovimientoTipoCentralizacionTipo ON
							CTB_itzMovimientoTipoCentralizacionTipo."IdTipoCentralizacion" = CTB_itzContabilidadTipoCentralizacionTipo."IdTipoCentralizacion"
					   INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoMovimientoTipo" AS CTB_itzEventoTipoDatoMovimientoTipo ON
							CTB_itzEventoTipoDatoMovimientoTipo."IdTipoMovimiento" = CTB_itzMovimientoTipoCentralizacionTipo."IdTipoMovimiento"
					   INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" AS CTB_itzEventoTipoDato ON
							CTB_itzEventoTipoDato."IdTipoDatoEvento" = CTB_itzEventoTipoDatoMovimientoTipo."IdTipoDatoEvento"
				 WHERE CTB_itzContabilidadTipoCentralizacionTipo."IdTipoContabilidad" = (SELECT TOP 1 CTB_itzContabilidadTipo."IdTipoContabilidad"
																						   FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" AS CTB_itzContabilidadTipo
																								INNER JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" AS CTB_itzContabilidadDefinicionAsiento ON
																									 CTB_itzContabilidadDefinicionAsiento."IdTipoContabilidad" = CTB_itzContabilidadTipo."IdTipoContabilidad"
																						  WHERE CTB_itzContabilidadDefinicionAsiento."IdDefinicionAsiento" = :p_IdDefinicionAsiento)
				   AND CTB_itzEventoTipoDatoMovimientoTipo."IdTipoDatoEvento" NOT IN (SELECT "IdTipoDatoEvento"
																						FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoGlosa" AS CTB_itzContabilidadTipoGlosa
																					   WHERE CTB_itzContabilidadTipoGlosa."IdTipoContabilidad" = :p_IdTipoContabilidad
																					   AND CTB_itzContabilidadTipoGlosa."Estado" = 'V'
																					   AND CTB_itzContabilidadTipoGlosa."IdDefinicionAsiento" = :p_IdDefinicionAsiento);
			/*
			 UNION ALL
				SELECT '' AS "GlosaCampo",
					   TabFijos."NombreCampo" AS "NombreCampo",
					   0 AS "Orden",
					   TabFijos."Origen" AS "Origen",
					   TabFijos."IdDefinicionAsiento" AS "IdDefinicionAsiento",
					   TabFijos."IdTipoContabilidad" AS "IdTipoContabilidad",
					   TabFijos."TipoDato" AS "TipoDato"
				  FROM #TabGlosaMovimientos AS TabFijos
				 WHERE TabFijos."IdDefinicionAsiento" = :p_IdDefinicionAsiento
				   AND TabFijos."NombreCampo" NOT IN (SELECT "NombreCampoMovimiento"
														FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoGlosa" AS CTB_itzContabilidadTipoGlosa
													   WHERE CTB_itzContabilidadTipoGlosa."IdDefinicionAsiento" = :p_IdDefinicionAsiento AND CTB_itzContabilidadTipoGlosa."Origen" = 'DAT')
			  ORDER BY "Origen", "NombreCampo" ASC;
			  */
			  
		END IF;

		DROP TABLE #TabGlosaMovimientos;

	END;
END;
