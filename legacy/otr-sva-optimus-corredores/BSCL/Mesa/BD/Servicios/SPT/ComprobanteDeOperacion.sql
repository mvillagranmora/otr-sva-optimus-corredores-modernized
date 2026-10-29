		
SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::SPT.ComprobanteDeOperacion";

CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  
"BSCL.Mesa.BD.Servicios::SPT.ComprobanteDeOperacion" ( 
	--Entrada
	IN p_FechaOperacion DATE,
	IN p_IdEmpresa BIGINT,
    IN p_IdUsuario BIGINT,
    IN p_IdMoneda VARCHAR(3),
    IN p_Resultados	TABLE	(
    							"Campo"					VARCHAR(100)
    						),
    												
    OUT p_out_ResultadoOperacionesInterbancarios	TABLE  (
    														"FolioOperacion"			BIGINT,
    														"MontoCompra" 				DECIMAL(26,8),
    														"PrecioCompra" 				DECIMAL(26,8),
    														"MontoVenta" 				DECIMAL(26,8),
    														"PrecioVenta" 				DECIMAL(26,8)
    												),
    
    OUT p_out_PosicionInicial						TABLE  (
    														"PosicionCompra" 			DECIMAL(26,8),
    														"TipoCambioCompra"			DECIMAL(26,8),
    														"PosicionVenta" 			DECIMAL(26,8),
    														"TipoCambioVenta"			DECIMAL(26,8)
    												),	
    OUT p_out_ResultadoPosicionInicial  			DECIMAL(26,8),
    OUT p_out_MontosInterbancarios					TABLE  (
    														"MontoCompraInterbancario" 	DECIMAL(26,8),
    														"MontoVentaInterbancario" 	DECIMAL(26,8)
    												),
    OUT p_out_MontosConsolidados					TABLE  (
    														"MontoCompraConsolidado" 	DECIMAL(26,8),
    														"MontoVentaConsolidado" 	DECIMAL(26,8)
    												),
    OUT p_out_PreciosPromedios						TABLE  (
    														"PrecioCompraInterbancario" DECIMAL(26,8),
    														"PrecioVentaInterbancario" 	DECIMAL(26,8),
    														"PrecioCompraConsolidado" 	DECIMAL(26,8),
    														"PrecioVentaConsolidado" 	DECIMAL(26,8)
    												),
    												
    OUT p_out_ResultadosTotales						TABLE  (
    														"Campo"						VARCHAR(100),
   															"Valor" 					DECIMAL(26,8)    												
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
	DECLARE v_out_AdvertenciasDetalle	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	DECLARE v_out_ErroresDetalle		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
										DEFAULT SELECT 0 AS "IdInterno", '' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY WHERE 1=2;
	
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

		DECLARE v_resultadoRegla 					NVARCHAR(40);
		DECLARE v_PosicionInicial  					DECIMAL(26,8);
		DECLARE v_TipoCambioInicial  				DECIMAL(26,8);
		DECLARE p_Resultado  						BIGINT;
		DECLARE v_countPosicion 					BIGINT;
		DECLARE v_countConfigMesa 			  		BIGINT;
		DECLARE v_posicionInicialValorizada	  		DECIMAL(26,8);
 		DECLARE v_countMovimientos		 	  		BIGINT;
   		DECLARE v_countMonedasPosicionInicial		BIGINT;
   		DECLARE v_monedasPosicionInicialValorizada 	DECIMAL(26,8);
   		DECLARE v_monedasPosicionInicial			DECIMAL(26,8);   		
   		DECLARE v_tablaPosicionInicial 		  		table (
   														"IdPosicionInicial"	BIGINT
												 		, "IdEmpresa"			BIGINT
												 		, "Fecha" 				SECONDDATE
												 		, "IdMoneda"			NVARCHAR(3)	
												 		, "TipoCambioInicial"	DECIMAL(26,8)
												 		, "CierreIngresado"		DECIMAL(26,8)
												 		, "PosicionInicial"		DECIMAL(26,8)
														, "TipoInstrumento"		NVARCHAR(50)
												 		, "IdUsuario"			NVARCHAR(50)
												 		, "Estado"				NVARCHAR(3)
												 		, "ResultadoPosicionInicial"  	DECIMAL(26,8)
												 	 );
		
    
		
		--****************************
		--*VALIDACION REGLAS BASICAS *
		--****************************
    
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,'MSP',:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		
		
		v_Movimientos_Spot = SELECT * 
							 FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
							 WHERE ("Estado" = 'VIG'
							 OR "Estado" = 'PEL'
							 OR "Estado" = 'PDA')
							 AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399);

							 
		--*******************************
		--*RESULTADOS INTERBANCARIOS*
		--*******************************
		p_out_ResultadoOperacionesInterbancarios = SELECT 
										"IdOperacion" AS "FolioOperacion",
										CASE WHEN "IdTipoOperacion" = 'COMSPT' THEN COALESCE(SUM(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END), 0) ELSE 0 END as "MontoCompra",
										CASE WHEN "IdTipoOperacion" = 'COMSPT' THEN COALESCE("TipoCambioCierre", 0) ELSE 0 END as "PrecioCompra",
										CASE WHEN "IdTipoOperacion" = 'VENSPT' THEN COALESCE(SUM(CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END), 0) ELSE 0 END as "MontoVenta",
										CASE WHEN "IdTipoOperacion" = 'VENSPT' THEN COALESCE("TipoCambioCierre", 0) ELSE 0 END as "PrecioVenta"
									 FROM :v_Movimientos_Spot
									 WHERE "Nemotecnico" = 'INTERB' 
									 GROUP BY "IdOperacion", "TipoCambioCierre", "IdTipoOperacion";
									 
									 
		--***************************
		--*MONEDAS POSICION INICIAL *
		--***************************	

  		v_ConsultarMonedasPosicionInicial = SELECT 
  												* 
  											FROM "BSCL.Mesa.BD.Vistas::ConsultarMonedasPosicionInicial"
											WHERE "Estado" = 'ACT'
											AND "IdEmpresa" = :p_IdEmpresa
											AND "Fecha" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399);
											
		SELECT COUNT(*) INTO v_countMonedasPosicionInicial  FROM :v_ConsultarMonedasPosicionInicial;
		
		IF (v_countMonedasPosicionInicial > 0) THEN
		
			v_ParidadTipoCambio = SELECT * 
		 						   FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS ptc
		 						   INNER JOIN (
								   		SELECT MAX(p."IdParidadYTipoCambio") AS "IdParidad"
								   		FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" p
								   		WHERE p."Fecha" BETWEEN TO_SECONDDATE(:p_FechaOperacion) 
			              		   		AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399) 
								   		AND p."IdEstado" = 'ACT'
								   		GROUP BY p."IdMoneda"
            					 	) as table1
        							ON table1."IdParidad" = ptc."IdParidadYTipoCambio"            
									WHERE ptc."Fecha" BETWEEN TO_SECONDDATE(:p_FechaOperacion) 
        							AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399) 
									AND ptc."IdEstado" = 'ACT';
			
			SELECT 
				SUM(CASE WHEN mpi."IdMoneda" = 'USD' THEN mpi."PosicionInicial" ELSE (mpi."PosicionInicial" * ptc."ParidadMercado") END) into v_PosicionInicial 
				FROM :v_ConsultarMonedasPosicionInicial mpi
				INNER JOIN :v_ParidadTipoCambio ptc
				ON mpi."IdMoneda" = ptc."IdMoneda";
				
			SELECT 
				SUM((CASE WHEN mpi."IdMoneda" = 'USD' THEN mpi."PosicionInicial" ELSE (mpi."PosicionInicial" * ptc."ParidadMercado") END)* mpi."TipoCambioInicial") into v_monedasPosicionInicialValorizada 
				FROM :v_ConsultarMonedasPosicionInicial mpi
				INNER JOIN :v_ParidadTipoCambio ptc
				ON mpi."IdMoneda" = ptc."IdMoneda";
				
			SELECT 
				SUM("ResultadoPosicionInicial") into p_out_ResultadoPosicionInicial 
				FROM :v_ConsultarMonedasPosicionInicial mpi
				INNER JOIN :v_ParidadTipoCambio ptc
				ON mpi."IdMoneda" = ptc."IdMoneda";
				
		ELSE
			SELECT
				0 into v_PosicionInicial
			FROM DUMMY;
			
			SELECT
				0 into v_monedasPosicionInicialValorizada
			FROM DUMMY;
			
			SELECT
				0 into p_out_ResultadoPosicionInicial
			FROM DUMMY;
		END IF;
		
		
		--*******************
		--*POSICION INICIAL *
		--*******************					
		
		v_tablaPosicionInicial = SELECT 
									*
								 FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarPosicionInicial"
								 WHERE "IdEmpresa" = :p_IdEmpresa
								 AND "Estado" = 'ACT'
								 AND "Fecha" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399);
		
		
		SELECT COUNT(*) INTO v_countPosicion  FROM :v_tablaPosicionInicial;
		
		IF (v_countPosicion > 0) THEN
			SELECT 
				SUM(CASE WHEN "IdMoneda" = 'USD' THEN "TipoCambioInicial" ELSE 0 END) into  v_TipoCambioInicial
		FROM :v_tablaPosicionInicial;
		
		p_out_PosicionInicial = SELECT 
									CASE WHEN v_PosicionInicial > 0 THEN v_PosicionInicial ELSE 0 END AS "PosicionCompra",
									CASE WHEN v_PosicionInicial > 0 THEN v_TipoCambioInicial ELSE 0 END AS "TipoCambioCompra",
									CASE WHEN v_PosicionInicial < 0 THEN -1 * v_PosicionInicial ELSE 0 END AS "PosicionVenta",
									CASE WHEN v_PosicionInicial < 0 THEN v_TipoCambioInicial ELSE 0 END AS "TipoCambioVenta"
								FROM :v_tablaPosicionInicial;
		
		ELSE
			p_out_PosicionInicial = SELECT
										0 AS "PosicionCompra",
										0 AS "TipoCambioCompra",
										0 AS "PosicionVenta",
										0 AS "TipoCambioVenta"
									FROM DUMMY;
									
			SELECT 
				0 INTO v_PosicionInicial
			FROM DUMMY;			
			
			SELECT 
				0 INTO p_out_ResultadoPosicionInicial
			FROM DUMMY;			
			
		END IF;
			
			
		--**********************************
		--*PRECIOS PROMEDIO DE OPERACIONES *
		--**********************************

		SELECT COUNT(*) INTO v_countMovimientos  FROM :v_Movimientos_Spot;
		
		IF (v_countMovimientos > 0) THEN
			
			p_out_PreciosPromedios = SELECT 
									 COALESCE(NULLIF(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' AND "Nemotecnico" = 'INTERB' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "ResultadoMonedaLocalConsolidado" ELSE "MontoMonedaSecundaria" END) END), 0) / COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' AND "Nemotecnico" = 'INTERB' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) END), 0), 0) AS "PrecioCompraInterbancario" 
									,COALESCE(NULLIF(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' AND "Nemotecnico" = 'INTERB' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "ResultadoMonedaLocalConsolidado" ELSE "MontoMonedaSecundaria" END) END), 0) / COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' AND "Nemotecnico" = 'INTERB' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) END), 0), 0) AS "PrecioVentaInterbancario"
									,COALESCE(NULLIF((COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' AND "Nemotecnico" = 'INTERB' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "ResultadoMonedaLocalConsolidado" ELSE "MontoMonedaSecundaria" END) END), 0) + COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' AND "Nemotecnico" = 'DISTRI' THEN "ResultadoMonedaLocalConsolidado" END), 0) + (CASE WHEN v_PosicionInicial > 0 THEN v_monedasPosicionInicialValorizada ELSE 0 END)), 0) / NULLIF((COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' AND "Nemotecnico" = 'INTERB' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) END), 0) + COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' AND "Nemotecnico" = 'DISTRI' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END ) END), 0) + (CASE WHEN v_PosicionInicial > 0 THEN v_PosicionInicial ELSE 0 END)), 0), 0) AS "PrecioCompraConsolidado"
									,COALESCE(NULLIF((COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' AND "Nemotecnico" = 'INTERB' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "ResultadoMonedaLocalConsolidado" ELSE "MontoMonedaSecundaria" END) END), 0) + COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' AND "Nemotecnico" = 'DISTRI' THEN "ResultadoMonedaLocalConsolidado" END), 0) + (CASE WHEN v_PosicionInicial < 0 THEN v_monedasPosicionInicialValorizada * -1 ELSE 0 END)), 0) / NULLIF((COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' AND "Nemotecnico" = 'INTERB' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) END), 0) + COALESCE(SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' AND "Nemotecnico" = 'DISTRI' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END ) END), 0) + (CASE WHEN v_PosicionInicial < 0 THEN (v_PosicionInicial * -1) ELSE 0 END)),0), 0) AS "PrecioVentaConsolidado"
								FROM :v_Movimientos_Spot;
								
			
		ELSE
			p_out_PreciosPromedios = SELECT 
									 0 as "PrecioCompraInterbancario" 
									,0 as "PrecioVentaInterbancario"
									,CASE WHEN v_PosicionInicial > 0 THEN v_TipoCambioInicial ELSE 0 END as "PrecioCompraConsolidado"
									,CASE WHEN v_PosicionInicial < 0 THEN v_TipoCambioInicial ELSE 0 END as "PrecioVentaConsolidado"
								FROM DUMMY;
		END IF;
		

		--***************************************
		--*MONTOS INTERBANCARIOS DE OPERACIONES *
		--***************************************

		p_out_MontosInterbancarios = SELECT 
										 	COALESCE(SUM("MontoCompra"), 0) as "MontoCompraInterbancario" 
											,COALESCE(SUM("MontoVenta"), 0) as "MontoVentaInterbancario" 
										FROM :p_out_ResultadoOperacionesInterbancarios;
								
		--***************************************
		--*RESULTADO DE OPERACIONES CONSOLIDADO *
		--***************************************	
		p_out_MontosConsolidados = SELECT 
										 CASE WHEN v_PosicionInicial >= 0 THEN COALESCE(SUM("MontoCompra"), 0) + v_PosicionInicial ELSE COALESCE(SUM("MontoCompra"), 0) END as "MontoCompraConsolidado" 
										,CASE WHEN v_PosicionInicial < 0 THEN COALESCE(SUM("MontoVenta"), 0) + (-1 * v_PosicionInicial) ELSE COALESCE(SUM("MontoVenta"), 0) END as "MontoVentaConsolidado" 
										FROM
									(SELECT 
										COALESCE(SUM("MontoCompra"), 0)as "MontoCompra",
										COALESCE(SUM("MontoVenta"), 0) as "MontoVenta"
									FROM :p_out_ResultadoOperacionesInterbancarios
									UNION
									SELECT
										SUM(CASE WHEN "IdTipoOperacion" = 'COMSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END) as "MontoCompra",
										SUM(CASE WHEN "IdTipoOperacion" = 'VENSPT' THEN (CASE WHEN "IdMonedaPrincipal" != "IdMonedaEquivalente" AND "IdMonedaSecundaria" = "IdMonedaLocal" THEN "MontoMonedaPrincipalEquivalente" ELSE "MontoMonedaPrincipal" END) ELSE 0 END) as "MontoVenta"
									FROM :v_Movimientos_Spot
									WHERE "Nemotecnico" = 'DISTRI');
		

		--***********************************
		--*RESULTADO DE OPERACIONES TOTALES *
		--***********************************

		CALL "BSCL.Mesa.BD.Servicios::ConsultarConfiguracionMesa"(1,'MEME', 'CODRESULTADO', p_out_datosMesa);
		
		SELECT COUNT(*) INTO v_countConfigMesa  FROM :p_out_datosMesa;
		
		IF (v_countConfigMesa > 0) THEN
			SELECT "Valor" into p_resultado from :p_out_datosMesa;
		ELSE
			SELECT
				0 INTO p_resultado
			FROM DUMMY;
		END IF;
		
		CALL "BSCL.Mesa.BD.Servicios::SPT.ConsultarResultadosTotalesSpot"(
		 p_FechaOperacion => :p_FechaOperacion, 
		 p_IdEmpresa => :p_IdEmpresa, 
		 p_IdUsuario => :p_IdUsuario, 
		 p_IdMoneda => :p_IdMoneda,
		 p_Resultado => :p_Resultado, 
		 p_Resultados => :p_Resultados, 
	 	 p_out_ResultadoOperaciones => p_out_ResultadosTotales, 
		 p_out_AdvertenciasContador => p_out_AdvertenciasContador, 
		 p_out_AdvertenciasDetalle => p_out_AdvertenciasDetalle, 
		 p_out_ErroresContador => p_out_ErroresContador, 
		 p_out_ErroresDetalle => p_out_ErroresDetalle);

	END;
END;

