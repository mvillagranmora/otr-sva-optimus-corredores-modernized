SET SCHEMA "BSCL";  
 
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarPosicionPorMoneda";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarPosicionPorMoneda"
    ( 
	-- Tabla Entrada
	IN p_IdMoneda NVARCHAR(3),
	IN p_IdEmpresa BIGINT,
	IN p_IdUsuario BIGINT,
	IN p_FechaOperacion DATE,
	IN p_TipoMesa NVARCHAR(6),			   
	--Salida   	--------------------------------------------------

	OUT p_out_posicionPorMonedas		 table ( 
												"IdMoneda"				NVARCHAR(3),
												"Compras"				DECIMAL(26,8),
												"Ventas"				DECIMAL(26,8),
												"PosicionInicial"		DECIMAL(26,8),
												"Posicion"				DECIMAL(26,8),
												"Paridad"				DECIMAL(26,8),
												"EquivalenteUSD"		DECIMAL(26,8)
										),
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

		DECLARE v_resultadoRegla 		NVARCHAR(40);
   		DECLARE p_posicionInicial  		DECIMAL(26,8);
   	 		
   		--****************************
		--*VALIDACION REGLAS BASICAS *
		--****************************
    
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,'MSP',:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
  		--**********************
		--*CONSULTAR OPERACIONES
		--**********************

		IF (:p_TipoMesa = 'TODOS') THEN
			v_Movimientos_Spot = SELECT * 
							 FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
							 WHERE ("Estado" = 'VIG'
							 OR "Estado" = 'PEL'
							 OR "Estado" = 'PDA')
							 AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399);
		ELSEIF (:p_TipoMesa = 'INTERB') THEN
			v_Movimientos_Spot = SELECT * 
							 FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
							 WHERE ("Estado" = 'VIG'
							 OR "Estado" = 'PEL'
							 OR "Estado" = 'PDA')
							 AND ("Nemotecnico" = 'INTERB'
							 OR "Nemotecnico" = 'ARBINT')
							 AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399);

		ELSE
			v_Movimientos_Spot = SELECT * 
							 FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot"
							 WHERE ("Estado" = 'VIG'
							 OR "Estado" = 'PEL'
							 OR "Estado" = 'PDA')
							 AND ("Nemotecnico" = 'DISTRI'
							 OR "Nemotecnico" = 'ARBDIS')
							 AND "FechaOperacion" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399);
		END IF;
		
  		 
  		 v_paridadTipoCambio_ = SELECT * 
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
  		 							
  		v_monedas_ = SELECT "IdMoneda" FROM "BSCL.Plataforma.BD::PLA.Moneda";
  				
  		v_posicionInicial_ = SELECT
							     * 
						     FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarPosicionInicial"
  							 WHERE "Estado" = 'ACT'
							 AND "IdEmpresa" = :p_IdEmpresa
							 AND "Fecha" BETWEEN TO_SECONDDATE(:p_FechaOperacion) AND ADD_SECONDS(TO_SECONDDATE(:p_FechaOperacion), 86399);
							 
  		v_consultarMoneda_ = SELECT 
  								 "IdMonedaPrincipal", "MultiplicaODivide"
  						     FROM "BSCL.Mesa.BD.Vistas::ConsultarMoneda" 
  						     WHERE "IdTipoProducto" = 'MEME'
  						     AND "IdEstado" = 'ACT'
  						     GROUP BY "IdMonedaPrincipal", "MultiplicaODivide";
  			
  		p_out_posicionPorMonedas =
  			SELECT
  				 cm."IdMonedaPrincipal" AS "IdMoneda"
  				,COALESCE(SUM(CASE WHEN ms."IdTipoOperacion" = 'COMSPT' THEN ms."MontoMonedaPrincipal" END),0)	AS "Compras"
  				,COALESCE(SUM(CASE WHEN ms."IdTipoOperacion" = 'VENSPT' THEN ms."MontoMonedaPrincipal" END),0)	AS "Ventas"
  				,CASE WHEN :p_TipoMesa = 'DISTRI' THEN 0 ELSE COALESCE(pi."PosicionInicial",0) END				AS "PosicionInicial"
  				,CASE WHEN SUM("MontoMonedaPrincipal") != 0 THEN
  					COALESCE(SUM(CASE WHEN ms."IdTipoOperacion" = 'COMSPT' THEN ms."MontoMonedaPrincipal" END),0) - 
  					COALESCE(SUM(CASE WHEN ms."IdTipoOperacion" = 'VENSPT' THEN ms."MontoMonedaPrincipal" END),0) + 
  					(CASE WHEN :p_TipoMesa = 'DISTRI' THEN 0 ELSE COALESCE(pi."PosicionInicial",0) END)
  				 ELSE 0 END																						AS "Posicion"
  				,COALESCE(ptc."ParidadMercado",0)																AS "Paridad"
  				,(COALESCE(SUM(CASE WHEN ms."IdTipoOperacion" = 'COMSPT' THEN ms."MontoMonedaPrincipal" END),0) - 
  				  COALESCE(SUM(CASE WHEN ms."IdTipoOperacion" = 'VENSPT' THEN ms."MontoMonedaPrincipal" END),0) + 
  				  (CASE WHEN :p_TipoMesa = 'DISTRI' THEN 0 ELSE COALESCE(pi."PosicionInicial",0) END)) * 
  				 (CASE WHEN cm."MultiplicaODivide" = 'D' AND COALESCE(ptc."ParidadMercado",0) <> 0 THEN 1/COALESCE(ptc."ParidadMercado",0)
  				 	   WHEN cm."MultiplicaODivide" = 'D' AND COALESCE(ptc."ParidadMercado",0) = 0 THEN 0
  				 	   ELSE COALESCE(ptc."ParidadMercado",0) END) AS "EquivalenteUSD"
  			FROM :v_consultarMoneda_ cm 
  			LEFT JOIN :v_paridadTipoCambio_ ptc
  				ON cm."IdMonedaPrincipal" = ptc."IdMoneda"
  			LEFT JOIN :v_Movimientos_Spot ms
  				ON cm."IdMonedaPrincipal" = ms."IdMonedaPrincipal"
  			LEFT JOIN :v_posicionInicial_ pi
  				ON cm."IdMonedaPrincipal" = pi."IdMoneda"
  			GROUP BY cm."IdMonedaPrincipal", cm."MultiplicaODivide", ptc."ParidadMercado", pi."PosicionInicial";
  		
  	END;
 END;