		
SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::SPT.ConsultarResultadosTotalesSpot";

CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  
"BSCL.Mesa.BD.Servicios::SPT.ConsultarResultadosTotalesSpot" ( 
	--Entrada
	IN p_FechaOperacion DATE,
	IN p_IdEmpresa BIGINT,
    IN p_IdUsuario BIGINT,
    IN p_IdMoneda VARCHAR(3),
    IN p_Resultado BIGINT,
    IN p_Resultados	TABLE	(
    							"Campo"					VARCHAR(100)
    						),											
    OUT p_out_ResultadoOperaciones			TABLE  (
    												"Campo"			VARCHAR(100),
    												"Valor" 		DECIMAL(26,8)    												
    												),
	--Salida
   	 -- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
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
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE v_out_AdvertenciasDetalle  TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
												DEFAULT SELECT  0  AS "IdInterno"
													,'' AS "Codigo"
													,'' AS "RequiereAutorizacion"
													,'' AS "ValorParametro"
													,'' AS "NombreParametro"
													FROM DUMMY
													WHERE 1=2;
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
												DEFAULT SELECT 0  AS "IdInterno"
														   ,'' AS "Codigo"
														   ,'' AS "ValorParametro"
														   ,'' AS "NombreParametro"
													FROM DUMMY
													WHERE 1=2;
	
	DECLARE v_count_operaciones				BIGINT;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
								"NombreParametro" as "CodigoDato",
								"ValorParametro" as "ValorDato" 
								FROM :v_out_ErroresDetalle
								UNION ALL
								SELECT "Codigo" AS "CodigoMensaje",
								"NombreParametro" as "CodigoDato",
								"ValorParametro" as "ValorDato" 
								FROM :v_out_AdvertenciasDetalle;
		  
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									errores."Codigo",
									msj."Descripcion"
								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									adv."Codigo",
									adv."RequiereAutorizacion",
									msj."Descripcion"
								FROM :v_out_AdvertenciasDetalle adv
								INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
				 						 	
	END;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	
    BEGIN

		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_resultadoPosicionInicial 	DECIMAL(26,8);
		DECLARE v_distribucion	 			DECIMAL(26,8);
		DECLARE v_countPosicionInicial		BIGINT;
		--*******************************
		--*VALIDACION REGLAS BASICAS *
		--*******************************
    
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,'MSP',:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		v_ConsultarMonedasPosicionInicial = SELECT 
  												* 
  											FROM "BSCL.Mesa.BD.Vistas::ConsultarMonedasPosicionInicial"
											WHERE "Estado" = 'ACT'
											AND "IdEmpresa" = :p_IdEmpresa
											AND "Fecha" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399);
											
		SELECT COUNT(*) INTO v_countPosicionInicial  FROM :v_ConsultarMonedasPosicionInicial;
		
		IF (v_countPosicionInicial > 0) THEN
		
				SELECT 
					SUM("ResultadoPosicionInicial") INTO v_resultadoPosicionInicial
				FROM :v_ConsultarMonedasPosicionInicial;
			
		ELSE
			
			SELECT
				0 INTO v_resultadoPosicionInicial
			FROM DUMMY;	
			
		END IF;		
						    
		SELECT 
			COALESCE(SUM("Margen"), 0) into v_distribucion
		FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" 
		WHERE "Nemotecnico" = 'DISTRI'
		AND "Estado" = 'VIG'
		AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399);
		
		p_out_ResultadoOperaciones = 
									 SELECT 
									 	CASE WHEN re."Campo" = 'TODOS' OR re."Campo" = 'POSINI' THEN 'POSINI' END as "Campo",
									 	:v_resultadoPosicionInicial as "Valor"
									 FROM DUMMY
									 INNER JOIN :p_Resultados re 
									 ON re."Campo" = 'TODOS' OR re."Campo" = 'POSINI'
									 UNION ALL
									 SELECT
										cm."Nemotecnico" as "Campo",
										CASE WHEN :p_Resultado = 1 THEN SUM(cm."ResultadoMonedaLocalPuntas") 
											 WHEN :p_Resultado = 2 THEN SUM(cm."ResultadoMonedaLocalBidAsk")
											 WHEN :p_Resultado = 3 THEN SUM(cm."ResultadoMonedaLocalDO")
											 WHEN :p_Resultado = 4 THEN SUM(cm."ResultadoMonedaLocalTCSpot")
											 WHEN :p_Resultado = 5 THEN SUM(cm."ResultadoMonedaLocalTCCierreMesa")
											 ELSE 0
										END as "Valor"
									 FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" cm
									 INNER JOIN :p_Resultados re 
									 ON re."Campo" = 'TODOS' OR re."Campo" = 'INTERB' 
									 WHERE cm."Nemotecnico" = 'INTERB'
									 AND "Estado" = 'VIG'
									 AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
									 GROUP BY cm."Nemotecnico"
									 UNION ALL
									 SELECT
										cm."Nemotecnico" as "Campo",
										:v_distribucion as "Valor"
									 FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" cm
									 INNER JOIN :p_Resultados re 
									 ON re."Campo" = 'TODOS' OR re."Campo" = 'DISTRI' 
									 WHERE cm."Nemotecnico" = 'DISTRI'
									 AND "Estado" = 'VIG'
									 AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
									 GROUP BY cm."Nemotecnico"
									 UNION ALL
									 SELECT
										cm."Nemotecnico" as "Campo",
										CASE WHEN :p_Resultado = 1 THEN SUM(cm."ResultadoMonedaLocalPuntas") 
											 WHEN :p_Resultado = 2 THEN SUM(cm."ResultadoMonedaLocalBidAsk")
											 WHEN :p_Resultado = 3 THEN SUM(cm."ResultadoMonedaLocalDO")
											 WHEN :p_Resultado = 4 THEN SUM(cm."ResultadoMonedaLocalTCSpot")
											 WHEN :p_Resultado = 5 THEN SUM(cm."ResultadoMonedaLocalTCCierreMesa")
											 ELSE 0
										END as "Valor"
									 FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" cm
									 INNER JOIN :p_Resultados re 
									 ON re."Campo" = 'TODOS' OR re."Campo" = 'ARBINT' 
									 WHERE cm."Nemotecnico" = 'ARBINT'
									 AND "Estado" = 'VIG'
									 AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
									 GROUP BY cm."Nemotecnico"
									 UNION ALL
									 SELECT
										cm."Nemotecnico" as "Campo",
										CASE WHEN :p_Resultado = 1 THEN SUM(cm."ResultadoMonedaLocalPuntas") 
											 WHEN :p_Resultado = 2 THEN SUM(cm."ResultadoMonedaLocalBidAsk")
											 WHEN :p_Resultado = 3 THEN SUM(cm."ResultadoMonedaLocalDO")
											 WHEN :p_Resultado = 4 THEN SUM(cm."ResultadoMonedaLocalTCSpot")
											 WHEN :p_Resultado = 5 THEN SUM(cm."ResultadoMonedaLocalTCCierreMesa")
											 ELSE 0
										END as "Valor"
									 FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" cm
									 INNER JOIN :p_Resultados re 
									 ON re."Campo" = 'TODOS' OR re."Campo" = 'ARBDIS' 
									 WHERE cm."Nemotecnico" = 'ARBDIS'
									 AND "Estado" = 'VIG'
									 AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
									 GROUP BY cm."Nemotecnico"
									 UNION ALL
									 SELECT
									 	CASE WHEN re."Campo" = 'TODOS' OR re."Campo" = 'TOTAL' THEN 'TOTAL' END as "Campo",
										CASE WHEN :p_Resultado = 1 THEN :v_resultadoPosicionInicial + :v_distribucion + SUM(CASE WHEN cm."Nemotecnico" = 'DISTRI' THEN 0 ELSE cm."ResultadoMonedaLocalPuntas" END)
											 WHEN :p_Resultado = 2 THEN :v_resultadoPosicionInicial + :v_distribucion + SUM(CASE WHEN cm."Nemotecnico" = 'DISTRI' THEN 0 ELSE cm."ResultadoMonedaLocalBidAsk" END)
											 WHEN :p_Resultado = 3 THEN :v_resultadoPosicionInicial + :v_distribucion + SUM(CASE WHEN cm."Nemotecnico" = 'DISTRI' THEN 0 ELSE cm."ResultadoMonedaLocalDO" END)
											 WHEN :p_Resultado = 4 THEN :v_resultadoPosicionInicial + :v_distribucion + SUM(CASE WHEN cm."Nemotecnico" = 'DISTRI' THEN 0 ELSE cm."ResultadoMonedaLocalTCSpot" END)
											 WHEN :p_Resultado = 5 THEN :v_resultadoPosicionInicial + :v_distribucion + SUM(CASE WHEN cm."Nemotecnico" = 'DISTRI' THEN 0 ELSE cm."ResultadoMonedaLocalTCCierreMesa" END)
										ELSE 0
										END
										as "Valor"		 
									 FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" cm
									 INNER JOIN :p_Resultados re 
									 ON  re."Campo" = 'TODOS' OR re."Campo" = 'TOTAL'
									 WHERE "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399)
									 AND cm."Estado" = 'VIG'
									 GROUP BY re."Campo";
									 
									 
				
	END;
END;


