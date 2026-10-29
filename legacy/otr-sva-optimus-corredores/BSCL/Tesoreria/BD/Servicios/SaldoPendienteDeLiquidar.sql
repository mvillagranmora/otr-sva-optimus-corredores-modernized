SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios::SaldoPendienteDeLiquidar";

CREATE PROCEDURE 
--  
--  Proposito: Consultar el saldo de los movimientos más los comprobantes que se encuentran pendientes de liquidar
--  Autor: - BCS 
--  Fecha Creacion: 17/08/2016
-- 
"BSCL.Tesoreria.BD.Servicios::SaldoPendienteDeLiquidar" ( 
	-- Parametros entrada comunes en todos los servicios publicos
    IN  p_IdEmpresa                     BIGINT,
        
    -- Parametros entrada del servicio publico
	IN  p_FechaDesde  					NVARCHAR(10),
    IN  p_FechaHasta  					NVARCHAR(10),
	IN  p_SociosAConsultar				TABLE	("IdPersona"	BIGINT,	--valor no puede venir en null
												 "IdPortafolio"	BIGINT,	--viene nulo cuando no corresponde a un cliente
											  	 "IdMoneda"		NVARCHAR(3)),	--valor puede venir en nulo										 
	
    OUT p_out_SaldoPorFechaLiquidacion	TABLE	("IdPersona"	BIGINT,
    											 "IdCliente"	BIGINT, 
                                              	 "IdPortafolio"	BIGINT,  
                                              	 "FechaSaldo"	NVARCHAR(10),
                                              	 "IdMoneda"		NVARCHAR(3),
                                              	 "SaldoMovIng"	DECIMAL(26,8),
                                              	 "SaldoMovEgr"	DECIMAL(26,8),
                                              	 "Saldo"		DECIMAL(26,8)), --Tabla con proyección de Saldo
	OUT p_out_ErroresContador           INT,
	OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	READS SQL DATA AS
BEGIN
 
	--------------------
	--DECLARE Manejo de Errores 
	--------------------	
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_errorExt	 				NVARCHAR(40);        	
	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_hash,:v_erroroutmsg);		
		p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado  (El log ser? captado por la capa de XS control)		
			RESIGNAL;
		END IF;
	END;
	
	p_out_ErroresContador			:= 0;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy WHERE 1=2;
	p_out_SaldoPorFechaLiquidacion	= SELECT 0 as "IdPersona",0 as "IdCliente",0 as "IdPortafolio",'0001-01-01' as "FechaSaldo",
											'' as "IdMoneda",0 as "SaldoMovIng",0 as "SaldoMovEgr",0 as "Saldo" FROM dummy WHERE 1=2;
	
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;


		----------------------
		-- Cargar Tabla base
		----------------------
		v_TablaBase = 
		SELECT PER."IdPersona",
			   CLI."IdCliente",
			   POR."IdPortafolio",
			   MON."IdMoneda"
		  FROM :p_SociosAConsultar AS SC
		 INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER
		 		 ON PER."IdPersona" = SC."IdPersona"
		  LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
		 		 ON CLI."IdPersona" = PER."IdPersona"
		  LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
		  		 ON POR."IdCliente" = CLI."IdCliente"
		  		AND POR."IdPortafolio" = COALESCE(SC."IdPortafolio",POR."IdPortafolio")
		  LEFT JOIN "BSCL.Tesoreria.BD::TES.MonedaTesoreria" AS MON
		  		 ON MON."IdMoneda" = COALESCE(SC."IdMoneda",MON."IdMoneda");
		
		--------------------------
		-- Completar rango fecha
		--------------------------
		v_RangoFecha = 
		SELECT 	A."IdPersona",
				A."IdCliente", 
				A."IdPortafolio", 
				A."IdMoneda",
				GENERATED_PERIOD_START  as "Fecha"
		  FROM :v_TablaBase as A
		  LEFT JOIN SERIES_GENERATE_DATE('INTERVAL 1 DAY', :p_FechaDesde, ADD_DAYS(TO_DATE(:p_FechaHasta,'YYYY-MM-DD'), 1) ) as B ON 1 = 1;
		
		------------------------------------
		--Obtener Saldo Movimientos
		------------------------------------
		v_TablaMovimientos = 
		 SELECT  B."FechaLiquidacion"
		 		,B."FechaMovimiento"
				,B."IdPersonaTitularPago"	as "IdPersona"
				,B."IdPortafolioTitularPago" as "IdPortafolio"
				,B."IdMoneda"
				,SUM(CASE WHEN COALESCE(C."IdIngresoEgresoFormaDePago",0) = 0 
					   		THEN (CASE WHEN B."IdTipoMovimiento" = 'MOVING' THEN B."Monto" ELSE 0 END)
					   		ELSE (CASE WHEN B."IdTipoMovimiento" = 'MOVING' THEN C."Monto" ELSE 0 END)
				   END) AS "MontoMOVING"
				,SUM(CASE WHEN COALESCE(C."IdIngresoEgresoFormaDePago",0) = 0 
					   		THEN (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN B."Monto" ELSE 0 END)
					   		ELSE (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN C."Monto" ELSE 0 END)
				   END) AS "MontoMOVEGR"
				,SUM(CASE WHEN COALESCE(C."IdIngresoEgresoFormaDePago",0) = 0 
					   		THEN (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN -1*B."Monto" ELSE B."Monto" END)
					   		ELSE (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN -1*C."Monto" ELSE C."Monto" END)
				   END) AS "Monto"					
		   FROM :v_TablaBase AS A
		  INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" as B
				  ON B."IdPersonaTitularPago" 		= A."IdPersona"
				 AND B."IdPortafolioTitularPago" 	= COALESCE(A."IdPortafolio",B."IdPortafolioTitularPago")
				 AND B."IdMoneda" 					= A."IdMoneda"
				 AND B."FechaLiquidacion" 			<= :p_FechaHasta
				 AND B."IdEstado" 					= 'ING'
		   LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS C
				  ON B."IdIngresoEgreso" 	= C."IdIngresoEgreso"
				 AND C."IdEstado" 			= 'ING'
		  GROUP BY B."FechaLiquidacion",B."FechaMovimiento",B."IdPersonaTitularPago",B."IdPortafolioTitularPago",B."IdMoneda";
				
		 v_SaldoInicialMovimientos =
		 SELECT  "IdPersona"
		 		,"IdPortafolio"
				,"IdMoneda"
				,SUM("MontoMOVING") As "TotalMontoIngresoAnterior"
				,SUM("MontoMOVEGR") As "TotalMontoEgresoAnterior"				
				,SUM("Monto") As "TotalMontoAnterior"
		   FROM :v_TablaMovimientos
		  WHERE "FechaLiquidacion" < :p_FechaDesde
          GROUP BY "IdPersona","IdPortafolio", "IdMoneda";
		
		v_SaldoMovimiento =
		SELECT   RF."Fecha"
				,RF."IdPersona"
				,RF."IdCliente"
				,RF."IdPortafolio"
				,RF."IdMoneda"
				,COALESCE(SI."TotalMontoIngresoAnterior",0) + 
				 COALESCE(SUM(MOV."MontoMOVING") over  (PARTITION BY RF."IdPersona",RF."IdPortafolio",RF."IdMoneda"
				 		 						  		ORDER BY RF."Fecha" ROWS BETWEEN unbounded preceding and CURRENT ROW),0) 
				 as "SaldoMovIng"
				,COALESCE(SI."TotalMontoEgresoAnterior",0) + 
				 COALESCE(SUM(MOV."MontoMOVEGR") over  (PARTITION BY RF."IdPersona",RF."IdPortafolio",RF."IdMoneda"
				 		 						  		ORDER BY RF."Fecha" ROWS BETWEEN unbounded preceding and CURRENT ROW),0) 
				 as "SaldoMovEgr"
				,COALESCE(SI."TotalMontoAnterior",0) + 
				 COALESCE(SUM(MOV."Monto") over  (PARTITION BY RF."IdPersona",RF."IdPortafolio",RF."IdMoneda"
				 		 						  ORDER BY RF."Fecha" ROWS BETWEEN unbounded preceding and CURRENT ROW),0) 
				 as "Saldo"
	      FROM :v_RangoFecha as RF
	      LEFT JOIN :v_TablaMovimientos as MOV
				 ON RF."IdPortafolio" = MOV."IdPortafolio" 
				AND RF."IdMoneda"     = MOV."IdMoneda"
				AND RF."Fecha"        = MOV."FechaLiquidacion"
		  LEFT JOIN :v_SaldoInicialMovimientos AS SI 
		  		 ON SI."IdPortafolio"	= RF."IdPortafolio"
			    AND SI."IdMoneda" 		= RF."IdMoneda";

		------------------------------------
		--Obtener Saldo Comprobantes
		------------------------------------
		v_TablaComprobantes = 
		 SELECT  B."FechaLiquidacion"
		 		,B."FechaMovimiento"
				,B."IdPersonaTitularPago"	as "IdPersona"
				,B."IdPortafolioTitularPago" as "IdPortafolio"
				,B."IdMoneda"
				,SUM(CASE WHEN COALESCE(C."IdComprobanteFormaDePago",0) = 0 
					   THEN (CASE WHEN B."IdTipoMovimiento" = 'MOVING' THEN B."Monto" ELSE 0 END)
					   ELSE (CASE WHEN B."IdTipoMovimiento" = 'MOVING' THEN C."Monto" ELSE 0 END)
				   END) AS "MontoMOVING"
				,SUM(CASE WHEN COALESCE(C."IdComprobanteFormaDePago",0) = 0 
					   THEN (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN B."Monto" ELSE 0 END)
					   ELSE (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN C."Monto" ELSE 0 END)
				   END) AS "MontoMOVEGR"
				,SUM(CASE WHEN COALESCE(C."IdComprobanteFormaDePago",0) = 0 
					   THEN (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN -1*B."Monto" ELSE B."Monto" END)
					   ELSE (CASE WHEN B."IdTipoMovimiento" = 'MOVEGR' THEN -1*C."Monto" ELSE C."Monto" END)
				   END) AS "Monto"					
		   FROM :v_TablaBase AS A
		  INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as B 
				  ON B."IdPersonaTitularPago" 		= A."IdPersona"
				 AND B."IdPortafolioTitularPago"	= COALESCE(A."IdPortafolio",B."IdPortafolioTitularPago")
				 AND B."IdMoneda" 					= A."IdMoneda"
				 AND B."FechaLiquidacion" 			<= :p_FechaHasta
				 AND B."IdEstado" 					IN ('ING','GIR','GIP','LIP')
		   LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS C
				  ON B."IdComprobante" 			= C."IdComprobante"
				 AND C."IdComprobantePartida" 	= 0
				 AND C."IdEstado" 				IN ('ING','NDP')
		  GROUP BY B."FechaLiquidacion",B."FechaMovimiento",B."IdPersonaTitularPago",B."IdPortafolioTitularPago",B."IdMoneda";
		 		 
		v_SaldoInicialComprobantes =
		 SELECT  "IdPersona"
		 		,"IdPortafolio"
				,"IdMoneda"
				,SUM("MontoMOVING") As "TotalMontoIngresoAnterior"
				,SUM("MontoMOVEGR") As "TotalMontoEgresoAnterior"				
				,SUM("Monto") As "TotalMontoAnterior"
		   FROM :v_TablaComprobantes
		  WHERE "FechaLiquidacion" < :p_FechaDesde
          GROUP BY "IdPersona","IdPortafolio", "IdMoneda";
          
		v_SaldoComprobante =
		SELECT   RF."Fecha"
				,RF."IdPersona"
				,RF."IdCliente"
				,RF."IdPortafolio"
				,RF."IdMoneda"
				,COALESCE(SI."TotalMontoIngresoAnterior",0) + 
				 COALESCE(SUM(COM."MontoMOVING") over  (PARTITION BY RF."IdPersona",RF."IdPortafolio",RF."IdMoneda"
				 		 						  		ORDER BY RF."Fecha" ROWS BETWEEN unbounded preceding and CURRENT ROW),0) 
				 as "SaldoMovIng"
				,COALESCE(SI."TotalMontoEgresoAnterior",0) + 
				 COALESCE(SUM(COM."MontoMOVEGR") over  (PARTITION BY RF."IdPersona",RF."IdPortafolio",RF."IdMoneda"
				 		 						  		ORDER BY RF."Fecha" ROWS BETWEEN unbounded preceding and CURRENT ROW),0) 
				 as "SaldoMovEgr"
				,COALESCE(SI."TotalMontoAnterior",0) + 
				 COALESCE(SUM(COM."Monto") over  (PARTITION BY RF."IdPersona",RF."IdPortafolio",RF."IdMoneda"
				 		 						  ORDER BY RF."Fecha" ROWS BETWEEN unbounded preceding and CURRENT ROW),0) 
				 as "Saldo"
	      FROM :v_RangoFecha as RF
	      LEFT JOIN :v_TablaComprobantes as COM
				 ON RF."IdPortafolio" = COM."IdPortafolio" 
				AND RF."IdMoneda"     = COM."IdMoneda"
				AND RF."Fecha"        = COM."FechaLiquidacion"
		  LEFT JOIN :v_SaldoInicialComprobantes AS SI 
		  		 ON SI."IdPortafolio"	= RF."IdPortafolio"
			    AND SI."IdMoneda" 		= RF."IdMoneda";          
				
		------------------------------------------------------
		--Retorna informacion del saldo  
		------------------------------------------------------
		p_out_SaldoPorFechaLiquidacion =
	     SELECT  RF."IdPersona"
				,RF."IdCliente"
              	,RF."IdPortafolio"
              	,RF."Fecha" as "FechaSaldo"
              	,COALESCE(RF."IdMoneda",SCFL."IdMoneda",SMFL."IdMoneda") as "IdMoneda"
              	,CAST((COALESCE(SCFL."SaldoMovIng",0) + COALESCE(SMFL."SaldoMovIng",0)) as DECIMAL(26,8)) AS "SaldoMovIng"
              	,CAST((COALESCE(SCFL."SaldoMovEgr",0) + COALESCE(SMFL."SaldoMovEgr",0)) as DECIMAL(26,8)) AS "SaldoMovEgr"
              	,CAST((COALESCE(SCFL."Saldo",0) + COALESCE(SMFL."Saldo",0)) as DECIMAL(26,8)) AS "Saldo"
   		   FROM :v_RangoFecha AS RF   		   
   		   LEFT JOIN :v_SaldoMovimiento as  SCFL
   		   		  ON SCFL."Fecha" 			= RF."Fecha"
				 AND SCFL."IdPersona"		= RF."IdPersona"
				 AND SCFL."IdPortafolio"	= RF."IdPortafolio"
		  		 AND SCFL."IdMoneda"		= RF."IdMoneda"
		   LEFT JOIN :v_SaldoComprobante as SMFL
		   		  ON SMFL."Fecha"			= RF."Fecha" 
				 AND SMFL."IdPersona"		= RF."IdPersona"
				 AND SMFL."IdPortafolio"	= RF."IdPortafolio"
		  		 AND SMFL."IdMoneda"		= RF."IdMoneda";
	END;
END;
