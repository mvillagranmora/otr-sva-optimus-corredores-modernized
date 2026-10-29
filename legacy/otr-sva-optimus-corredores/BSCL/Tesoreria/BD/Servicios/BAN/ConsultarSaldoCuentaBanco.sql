SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.BAN::ConsultarSaldoCuentaBanco";
GO
CREATE PROCEDURE 
--
--  Proposito: Consultar el saldo de las cuenta banco de la corredora
--  Autor: NRM - BCS
--  Fecha Creacion: 28/04/2016
--
"BSCL.Tesoreria.BD.Servicios.BAN::ConsultarSaldoCuentaBanco" (
	-- Parametros entrada comunes en todos los servicios publicos
    IN  p_IdEmpresa                     	BIGINT,
    IN  p_IdUsuario                     	BIGINT, 
    IN  p_IdModulo                      	NVARCHAR(3), 
	IN  p_UsaReglasNegocio	 		    	NVARCHAR(1)		DEFAULT 'S',	-- S/N se usa en caso de servicios anidados.  		


    -- Parametros entrada del servicio
	IN  p_FechaDesde  						NVARCHAR(10),
    IN  p_FechaHasta  						NVARCHAR(10),    
	IN  p_SoloMovimientosQueAfectanBanco   	NVARCHAR(1)		DEFAULT 'S',	-- S/N    -- Solo movimientos con banco y cuenta bancaria asignada
    IN	p_DatosAConsulta					TABLE("IdCuentaBanco"			BIGINT,
    											  "IdBanco"					BIGINT,
    											  "IdMoneda"				NVARCHAR(3)),
    											  
    -- Parametros salida del servicio publico
    OUT p_out_SaldoCuentaBanco      		TABLE("IdCuentaBanco"       	BIGINT,			--Tabla con proyección de Saldo
    											  "NombreCuentaBanco"		NVARCHAR(100),
	    										  "IdBanco"					BIGINT,
	    										  "NombreBanco"				NVARCHAR(100),  -- Alias Cuenta Banco
	    										  "NumeroCuentaBancaria"	NVARCHAR(100),
	    										  "IdMoneda"                NVARCHAR(3),
	    										  "NombreMoneda"			NVARCHAR(100),
	    										  "IdEmpresa"               BIGINT,
	    										  "NombreEmpresa"			NVARCHAR(100),
	                                              "FechaSaldo" 				NVARCHAR(10),
	                                              
	                                              "SaldoDisponibleInicial"	DECIMAL(26,8), --(Todo aquello que afecto saldo en fecha consulta)
	                                              "SaldoDisponibleIngreso"	DECIMAL(26,8),
	                                              "SaldoDisponibleEgreso"	DECIMAL(26,8),
	                                              "SaldoDisponible"         DECIMAL(26,8),
	                                              
	                                              "SaldoPendienteIngreso"	DECIMAL(26,8), --(Todo aquello que no afecto saldo y que es menos a fecha consulta)
	                                              "SaldoPendienteEgreso"	DECIMAL(26,8),
	                                              "SaldoPendiente"         	DECIMAL(26,8),
	                                              
	                                              "SaldoContableIngreso"	DECIMAL(26,8),
	                                              "SaldoContableEgreso"		DECIMAL(26,8),
	                                              "SaldoContable"           DECIMAL(26,8),
	                                              
												  "FechaT1"					DATE,
	                                              "SaldoIngresoT1"			DECIMAL(26,8),
	                                              "SaldoEgresoT1"			DECIMAL(26,8),
	                                              "SaldoT1"           		DECIMAL(26,8),
	                                              
												  "FechaT2"					DATE,
	                                              "SaldoIngresoT2"			DECIMAL(26,8),
	                                              "SaldoEgresoT2"			DECIMAL(26,8),
	                                              "SaldoT2"           		DECIMAL(26,8),
	                                              
                                                  "FechaT3"					DATE,
	                                              "SaldoIngresoT3"			DECIMAL(26,8),
	                                              "SaldoEgresoT3"			DECIMAL(26,8),
	                                              "SaldoT3"           		DECIMAL(26,8)), 
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador      	INT,
	OUT p_out_AdvertenciasDetalle       	TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador           	INT,
	OUT p_out_ErroresDetalle            	TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	READS SQL DATA AS
BEGIN
 
	--------------------
	--DECLARE Manejo de Errores 
	--------------------	
	DECLARE SALIDA_CONTROLADA 			CONDITION;
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				TABLE ("Codigo" nvarchar(100),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg 	 			NVARCHAR(5000);
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_resultadoRegla      		NVARCHAR(100);
	DECLARE v_ErroresDetalle 			TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));	
	 	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;		 					 	
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);

		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log ser? captado por la capa de XS control)		
			RESIGNAL;
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle		= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	p_out_SaldoCuentaBanco  	= SELECT 0 as "IdCuentaBanco",'' as "NombreCuentaBanco",0 as "IdBanco",'' as "NombreBanco",'' AS "NumeroCuentaBancaria",'' AS "IdMoneda",
										'' as "NombreMoneda",0 as "IdEmpresa",'' as "NombreEmpresa",'0001-01-01' as "FechaSaldo",
										 0 AS "SaldoDisponibleInicial",0 AS "SaldoDisponibleIngreso",0 AS "SaldoDisponibleEgreso",0 AS "SaldoDisponible",
										 0 AS "SaldoPendienteIngreso",0 AS "SaldoPendienteEgreso",0 AS "SaldoPendiente",
										 0 AS "SaldoContableIngreso",0 AS "SaldoContableEgreso",0 AS "SaldoContable",
										 '0001-01-01' AS "FechaT1",0 AS "SaldoIngresoT1",0 AS "SaldoEgresoT1",0 AS "SaldoT1",
										 '0001-01-01' AS "FechaT2",0 AS "SaldoIngresoT2",0 AS "SaldoEgresoT2",0 AS "SaldoT2",
										 '0001-01-01' AS "FechaT3",0 AS "SaldoIngresoT3",0 AS "SaldoEgresoT3",0 AS "SaldoT3"
								    FROM dummy where 1=2;
		

	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		--validacion invalidante configuración básica del sistema
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
	END;
	END IF;

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
		DECLARE v_FechaDesde		NVARCHAR(10);
		DECLARE v_FechaProyeccion	NVARCHAR(10);
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299		
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		---------------------------
		-- CARGA TABLAS BASE
		---------------------------
		v_TablaBaseCBan = 
		 SELECT  CB."IdCuentaBanco"
		 		,CBAN."NombreCuentaBancaria" AS "NombreCuentaBanco"
				,CB."IdBanco"
				,CBAN."NombreBanco"
				,CB."NumeroCuentaBancaria"
				,CB."IdMoneda"
				,CAST(CBAN."IdMoneda" ||' - '|| CBAN."NombreMoneda" as NVARCHAR(100)) AS "NombreMoneda"
				,CB."IdEmpresa"
				,CBAN."NombreEmpresa"
				,'0001-01-01' as "FechaSaldo"
				,(CASE WHEN CB."AfectaPosicionInicial" = 'S' THEN CB."SaldoInicial" ELSE 0 END)  as "Saldo"
		  FROM :p_DatosAConsulta as DC
		  LEFT JOIN "BSCL.Tesoreria.BD::TES.CuentaBanco" as CB 
		 		 ON CB."IdCuentaBanco" 	= COALESCE(DC."IdCuentaBanco",CB."IdCuentaBanco")
		 		AND CB."IdBanco"  		= COALESCE(DC."IdBanco",CB."IdBanco")
		 		AND CB."IdMoneda"		= COALESCE(DC."IdMoneda",CB."IdMoneda")
		 INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" as CBAN
		 		 ON CBAN."IdCuentaBanco" = CB."IdCuentaBanco"
		 WHERE CB."IdEmpresa" = :p_IdEmpresa;

		
		--) Completar rango fecha
		v_RangoFecha =
		SELECT GENERATED_PERIOD_START as "FechaSaldo"
		FROM SERIES_GENERATE_DATE('INTERVAL 1 DAY', :p_FechaDesde, ADD_DAYS(TO_DATE (:p_FechaHasta, 'YYYY-MM-DD'), 1)  )
		WHERE 1 = 1;


		v_RangoFechaCBan = 
		SELECT   A."IdCuentaBanco"
		 		,A."NombreCuentaBanco"
				,A."IdBanco"
				,A."NombreBanco"
				,A."NumeroCuentaBancaria"
				,A."IdMoneda"
				,A."NombreMoneda"
				,A."IdEmpresa"
				,A."NombreEmpresa"
                ,B."FechaSaldo"
				,A."Saldo" as "SaldoAperturaCuentaBanco"
		FROM :v_TablaBaseCBan as A
        LEFT JOIN :v_RangoFecha as B ON 1 = 1;
		
		
		
		-------------------------------------------
		-- OBTENER SALDO DISPONIBLE (Saldo real)
		-------------------------------------------
		
		--) Obtener Saldo disponible Inicial
		v_SaldoDisponibleDetalladoInicial =
		 SELECT  COUNT(VCFP."IdComprobanteFormaDePago") over (partition by VCFP."FechaAfectaSaldo" order by VCFP."FechaAfectaSaldo",VCFP."FechaHoraLiquidacion",VCFP."IdComprobanteFormaDePago"  asc rows between unbounded preceding and current row) as "Id"
				,VCFP."IdComprobanteFormaDePago"
				,VCFP."SaldoDisponible"
				,FechMax."IdCuentaBanco"
				,FechMax."FechaAfectaSaldoAnterior"
		   FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP
		  INNER JOIN (SELECT MAX(VCFP."FechaAfectaSaldo") as "FechaAfectaSaldoAnterior"
		  					,RF."IdCuentaBanco"
				 	   FROM :v_TablaBaseCBan AS RF
				  	   LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP ON VCFP."IdCuentaBancariaCorredora" = RF."IdCuentaBanco"
				 	  WHERE VCFP."FechaAfectaSaldo" < :p_FechaDesde
				 	  GROUP BY RF."IdCuentaBanco"
		  			 ) as FechMax 
		  		  ON VCFP."FechaAfectaSaldo" = FechMax."FechaAfectaSaldoAnterior" 
		  		 AND VCFP."IdCuentaBancariaCorredora" = FechMax."IdCuentaBanco"
		  		 AND VCFP."AfectoSaldo"	= 'S';
		  
		  
		v_SaldoDisponibleInicial =
		 SELECT  SC."IdCuentaBanco"
		 		,SC."FechaAfectaSaldoAnterior"
		 		,CAST(SC."SaldoDisponible" as decimal(26,8)) as "SaldoDisponible"
   		   FROM :v_SaldoDisponibleDetalladoInicial AS SC
   		  INNER JOIN (SELECT MAX("Id") as "MaxId", "IdCuentaBanco", "FechaAfectaSaldoAnterior" FROM :v_SaldoDisponibleDetalladoInicial GROUP BY "FechaAfectaSaldoAnterior", "IdCuentaBanco") AS SCMax
   		 		  ON SCMax."IdCuentaBanco" = SC."IdCuentaBanco" AND SCMax."FechaAfectaSaldoAnterior" = SC."FechaAfectaSaldoAnterior" AND SCMax."MaxId" = SC."Id";	


		--) Obtener Saldo Disponible
		v_SaldoDisponibleDetallado =
		 SELECT	 COUNT(RF."FechaSaldo") over (partition by RF."FechaSaldo" order by RF."FechaSaldo", VCFP."FechaHoraAfectaSaldo",VCFP."FechaHoraLiquidacion",VCFP."IdComprobanteFormaDePago"  asc rows between unbounded preceding and current row) as "Id"
		 		,VCFP."IdComprobanteFormaDePago"
		 		,RF."IdCuentaBanco"
		 		,RF."NombreCuentaBanco"
				,RF."IdBanco"
				,RF."NombreBanco"
				,RF."NumeroCuentaBancaria"
		 		,RF."IdMoneda"
		 		,RF."NombreMoneda"
		 		,RF."IdEmpresa"
		 		,RF."NombreEmpresa"
		 		,RF."FechaSaldo"
		 		,VCFP."FechaAfectaSaldo"
		 		,VCFP."FechaHoraAfectaSaldo"
		 		,COALESCE(SI."SaldoDisponible",0) as "SaldoDisponibleInicial"
		 		,COALESCE(SUM(VCFP."MontoEgreso") over (partition by RF."IdCuentaBanco" order by RF."FechaSaldo", VCFP."FechaHoraAfectaSaldo", VCFP."IdComprobanteFormaDePago" asc rows between unbounded preceding and current row),0) as "SaldoDisponibleEgreso"
		 		,COALESCE(SUM(VCFP."MontoIngreso") over (partition by RF."IdCuentaBanco" order by RF."FechaSaldo", VCFP."FechaHoraAfectaSaldo", VCFP."IdComprobanteFormaDePago" asc rows between unbounded preceding and current row),0) as "SaldoDisponibleIngreso"		 				 		
		 		,COALESCE(SI."SaldoDisponible",0) + COALESCE(SUM(VCFP."MontoConSigno") over (partition by RF."IdCuentaBanco" order by RF."FechaSaldo", VCFP."FechaHoraAfectaSaldo", VCFP."IdComprobanteFormaDePago" asc rows between unbounded preceding and current row),0) as "SaldoDisponible"
           FROM :v_RangoFechaCBan as RF
		   LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as VCFP 
		   		  ON VCFP."IdCuentaBancariaCorredora"	= RF."IdCuentaBanco"
		   		 AND VCFP."FechaAfectaSaldo"	= RF."FechaSaldo" 
		   		 AND VCFP."AfectoSaldo"			= 'S'
		   LEFT JOIN :v_SaldoDisponibleInicial as SI
		   		  ON SI."IdCuentaBanco" = RF."IdCuentaBanco";
		 

		v_SaldoDisponible = 
	     SELECT  SC."IdCuentaBanco"
		 		,SC."NombreCuentaBanco"
				,SC."IdBanco"
				,SC."NombreBanco"
				,SC."NumeroCuentaBancaria"
		 		,SC."IdMoneda"
		 		,SC."NombreMoneda"
		 		,SC."IdEmpresa"
		 		,SC."NombreEmpresa"
		 		,SC."FechaSaldo"
		 		,SC."SaldoDisponibleInicial"
		 		,SC."SaldoDisponibleEgreso"
		 		,SC."SaldoDisponibleIngreso"
		 		,CAST(SC."SaldoDisponible" as decimal(26,8)) as "SaldoDisponible"
   		   FROM :v_SaldoDisponibleDetallado AS SC
   		  INNER JOIN (SELECT MAX("Id") as "MaxId", "FechaSaldo", "IdCuentaBanco" FROM :v_SaldoDisponibleDetallado GROUP BY "FechaSaldo", "IdCuentaBanco") AS SCMax
   		 		  ON SCMax."IdCuentaBanco" = SC."IdCuentaBanco" AND SCMax."FechaSaldo" = SC."FechaSaldo" AND SCMax."MaxId" = SC."Id";		
		  --SELECT 'SaldoDisponible', * FROM :v_SaldoDisponible;   -- REVISAR



		-----------------------------------
		-- OBTENER SALDO PENDIENTE
		-----------------------------------


		--) Obtener Saldo pendiente(Movimientos y Comprobantes con FP)
		v_SaldoPendienteDetallado =
		 SELECT  COUNT(TB."FechaSaldo") over (partition by TB."FechaSaldo"  order by TB."FechaSaldo" asc rows between unbounded preceding and current row) as "Id"
		 		,TB."IdCuentaBanco"
		 		,TB."NombreCuentaBanco"
				,TB."IdBanco"
				,TB."NombreBanco"
				,TB."NumeroCuentaBancaria"
		 		,TB."IdMoneda"
		 		,TB."NombreMoneda"
		 		,TB."IdEmpresa"
		 		,TB."NombreEmpresa"
		 		,TB."FechaSaldo"		 		
		 		 
		 		,COALESCE(SUM(FP."MontoEgreso") over (partition by TB."IdCuentaBanco",TB."FechaSaldo"),0) AS "SaldoPendienteEgreso"
		 		,COALESCE(SUM(FP."MontoIngreso") over (partition by TB."IdCuentaBanco",TB."FechaSaldo"),0) AS "SaldoPendienteIngreso"
		 		,COALESCE(SUM(FP."MontoConSigno") over (partition by TB."IdCuentaBanco",TB."FechaSaldo"),0) AS "SaldoPendiente"
		 		 		 		 
           FROM :v_RangoFechaCBan AS TB
           LEFT JOIN 
           (
           		SELECT CFP."MontoEgreso", CFP."MontoIngreso", CFP."MontoConSigno", CFP."IdCuentaBancariaCorredora", CFP."FechaLiquidacion"
		   		FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" AS CFP		  		
		  		WHERE CFP."AfectoSaldo" = 'N'
				AND CFP."IdEstadoComprobante" <> 'ANU'

				UNION ALL

				SELECT IEFP."MontoEgreso", IEFP."MontoIngreso", IEFP."MontoConSigno", IEFP."IdCuentaBancariaCorredora", IEFP."FechaLiquidacion"
		        FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago" AS IEFP
				WHERE IEFP."IdEstadoIngresoEgreso" = 'ING'
				AND IEFP."IdEstado" <> 'ANU'
           ) AS FP ON (FP."IdCuentaBancariaCorredora" = TB."IdCuentaBanco" AND FP."FechaLiquidacion" <= TB."FechaSaldo");
				 
		 --SELECT 'SaldoPendienteDetallado:', * FROM :v_SaldoPendienteDetallado; -- REVISAR


		v_SaldoPendiente = 
	     SELECT  SC."IdCuentaBanco"
		 		,SC."NombreCuentaBanco"
				,SC."IdBanco"
				,SC."NombreBanco"
				,SC."NumeroCuentaBancaria"
		 		,SC."IdMoneda"
		 		,SC."NombreMoneda"
		 		,SC."IdEmpresa"
		 		,SC."NombreEmpresa"
		 		,SC."FechaSaldo"
		 		,SC."SaldoPendienteEgreso"
		 		,SC."SaldoPendienteIngreso"
		 		,SC."SaldoPendiente"
   		   FROM :v_SaldoPendienteDetallado AS SC
   		  INNER JOIN (SELECT MAX("Id") as "MaxId", "FechaSaldo", "IdCuentaBanco" FROM :v_SaldoPendienteDetallado GROUP BY "FechaSaldo", "IdCuentaBanco") AS SCMax
   		 		  ON SCMax."IdCuentaBanco" = SC."IdCuentaBanco" AND SCMax."FechaSaldo" = SC."FechaSaldo" AND SCMax."MaxId" = SC."Id";
				
		 --SELECT 'v_SaldoPendiente', * FROM :v_SaldoPendiente; -- REVISAR				


		-- Incluir movimientos sin banco o cuenta bancaria asignada(No Afectan Saldo Banco ya que no se sabe a que cuenta afectarán) 
		IF (:p_SoloMovimientosQueAfectanBanco = 'N') THEN

				--) Obtener Saldo pendiente(Movimientos y Comprobantes sin FP)
				v_SaldoPendienteDetalladoSinFP = 
				SELECT
				     ROW_NUMBER() over (partition by MSFP."FechaSaldo",MSFP."IdMoneda" order by MSFP."FechaSaldo" asc) as "Id"		     
					,-1 AS "IdCuentaBanco"	              -- -1 Sin definir
				    ,'SIN CUENTA DEFINIDA' AS "NombreCuentaBanco"
					,-1 AS "IdBanco"                      -- -1 Sin definir
					,'MOVIMIENTOS SIN BANCO DEFINIDO' AS "NombreBanco"
					,'' AS "NumeroCuentaBancaria"
					,MSFP."IdMoneda"
					,MSFP."NombreMoneda"
					,MSFP."IdEmpresa"
					,MSFP."NombreEmpresa"
					,MSFP."FechaSaldo"			
					,COALESCE(SUM(MSFP."MontoEgreso")   over (partition by MSFP."FechaSaldo",MSFP."IdMoneda"),0) AS "SaldoPendienteEgresoSinFP"
					,COALESCE(SUM(MSFP."MontoIngreso")  over (partition by MSFP."FechaSaldo",MSFP."IdMoneda"),0) AS "SaldoPendienteIngresoSinFP"
					,COALESCE(SUM(MSFP."MontoConSigno") over (partition by MSFP."FechaSaldo",MSFP."IdMoneda"),0) AS "SaldoPendienteSinFP"
				FROM
				(
					SELECT    -- Comprobantes sin FP
						 COM."IdMoneda", COM."NombreMoneda"
						,COM."IdEmpresa", COM."NombreEmpresa",TB."FechaSaldo"
						,COM."MontoEgreso",COM."MontoIngreso",COM."MontoConSigno"
					FROM :v_RangoFecha AS TB
					LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" AS COM  
						   ON (COM."FechaLiquidacion" <= TB."FechaSaldo"			   
						   AND NOT EXISTS (SELECT 1 FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP WHERE CFP."IdComprobante" = COM."IdComprobante" AND CFP."IdEstado" <> 'ANU')
					       AND COM."IdEstado" <> 'ANU'
					       AND COM."IdEmpresa" = :p_IdEmpresa)
		
					UNION ALL
			
					SELECT    -- Movimientos sin FP
						 IE."IdMoneda" ,IE."NombreMoneda"
						,IE."IdEmpresa"
						,IE."NombreEmpresa"
						,TB."FechaSaldo"
						,IE."MontoEgreso", IE."MontoIngreso", IE."MontoConSigno"
					FROM :v_RangoFecha AS TB
					LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" AS IE
						   ON (IE."FechaLiquidacion" <= TB."FechaSaldo"
						   AND NOT EXISTS (SELECT 1 FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS IEFP WHERE IEFP."IdIngresoEgreso" = IE."IdIngresoEgreso" AND IEFP."IdEstado" <> 'ANU')
					       AND IE."IdEstado" = 'ING'
					       AND IE."IdEmpresa" = :p_IdEmpresa)
				) AS MSFP;
				--SELECT 'SaldoPendienteDetalladoSinFP', * FROM :v_SaldoPendienteDetalladoSinFP;  -- REVISAR
		
		
				v_SaldoPendienteSinFP = 
			    SELECT   SC."IdCuentaBanco"
				 		,SC."NombreCuentaBanco"
						,SC."IdBanco"
						,SC."NombreBanco"
						,SC."NumeroCuentaBancaria"
				 		,SC."IdMoneda"
				 		,SC."NombreMoneda"
				 		,SC."IdEmpresa"
				 		,SC."NombreEmpresa"
				 		,SC."FechaSaldo"
				 		,SC."SaldoPendienteEgresoSinFP"
				 		,SC."SaldoPendienteIngresoSinFP"
				 		,SC."SaldoPendienteSinFP"			 		
		   		FROM :v_SaldoPendienteDetalladoSinFP AS SC
		   		INNER JOIN (SELECT MAX("Id") as "MaxId", "FechaSaldo", "IdMoneda" FROM :v_SaldoPendienteDetalladoSinFP GROUP BY "FechaSaldo", "IdMoneda") AS SCMax
		   		ON SCMax."MaxId" = SC."Id" AND SCMax."FechaSaldo" = SC."FechaSaldo" AND SCMax."IdMoneda" = SC."IdMoneda";
				--SELECT 'SaldoPendienteSinFP', * FROM :v_SaldoPendienteSinFP;  -- REVISAR

		END IF; -- IF (:p_SoloMovimientosQueAfectanBanco = 'N')

		
		---------------------------------------------------------------------
		-- OBTENER SALDO CONTABLE (Saldo disponible + Saldo Pendiente)
		---------------------------------------------------------------------
		v_SaldoContableDetallado =
		 SELECT	 COUNT(RF."FechaSaldo") over (partition by RF."FechaSaldo" order by RF."FechaSaldo" asc rows between unbounded preceding and current row) as "Id"
		 		,RF."IdCuentaBanco"
		 		,RF."NombreCuentaBanco"
				,RF."IdBanco"
				,RF."NombreBanco"
				,RF."NumeroCuentaBancaria"
		 		,RF."IdMoneda"
		 		,RF."NombreMoneda"
		 		,RF."IdEmpresa"
		 		,RF."NombreEmpresa"
		 		,RF."FechaSaldo"
		 		,COALESCE(SD."SaldoDisponibleEgreso",0) + COALESCE(SP."SaldoPendienteEgreso",0) as "SaldoContableEgreso"
		 		,COALESCE(SD."SaldoDisponibleIngreso",0) + COALESCE(SP."SaldoPendienteIngreso",0) as "SaldoContableIngreso"
		 		,COALESCE(SD."SaldoDisponible",0) + COALESCE(SP."SaldoPendiente",0) as "SaldoContable"
		  FROM :v_RangoFechaCBan as RF
		  INNER JOIN :v_SaldoDisponible as SD
		  		  ON SD."IdCuentaBanco" = RF."IdCuentaBanco"
		  		 AND SD."FechaSaldo" = RF."FechaSaldo"
		   LEFT JOIN :v_SaldoPendiente as SP
		   		  ON SP."IdCuentaBanco" = RF."IdCuentaBanco"
		   		 AND SP."FechaSaldo" = RF."FechaSaldo";
		
		
		
		v_SaldoContable = 
	     SELECT  SC."IdCuentaBanco"
		 		,SC."NombreCuentaBanco"
				,SC."IdBanco"
				,SC."NombreBanco"				
				,SC."NumeroCuentaBancaria"
		 		,SC."IdMoneda"
		 		,SC."NombreMoneda"
		 		,SC."IdEmpresa"
		 		,SC."NombreEmpresa"
		 		,SC."FechaSaldo"
		 		,SC."SaldoContableEgreso"
		 		,SC."SaldoContableIngreso"
		 		,SC."SaldoContable"
   		   FROM :v_SaldoContableDetallado AS SC
   		  INNER JOIN (SELECT MAX("Id") as "MaxId", "FechaSaldo", "IdCuentaBanco" FROM :v_SaldoContableDetallado GROUP BY "FechaSaldo", "IdCuentaBanco") AS SCMax
   		 		  ON SCMax."IdCuentaBanco" = SC."IdCuentaBanco" AND SCMax."FechaSaldo" = SC."FechaSaldo" AND SCMax."MaxId" = SC."Id";





		--------------------------------
		-- OBTENER SALDO > T0
		--------------------------------

		--) Completar rango fecha por proyección
		v_FechaDesde		:= ADD_DAYS(TO_DATE (:p_FechaDesde, 'YYYY-MM-DD'), 1);
		v_FechaProyeccion 	:= ADD_DAYS(TO_DATE (:p_FechaHasta, 'YYYY-MM-DD'), 5);

		v_RangoFechaProyeccion =
		SELECT 
			ROW_NUMBER() over (order by GENERATED_PERIOD_START asc) AS "Orden",
			GENERATED_PERIOD_START as "FechaSaldo"
		FROM SERIES_GENERATE_DATE('INTERVAL 1 DAY', :v_FechaDesde, ADD_DAYS(TO_DATE (:v_FechaProyeccion, 'YYYY-MM-DD'), 1)  )
		WHERE 1 = 1;		
		--SELECT 'v_RangoFechaProyeccion', * FROM :v_RangoFechaProyeccion; -- REVISAR
		
		
		v_RangoFechaProyeccionCBan = 
		 SELECT  A."IdCuentaBanco"
				,A."NombreCuentaBanco"
				,A."IdBanco"
				,A."NombreBanco"
				,A."NumeroCuentaBancaria"
				,A."IdMoneda"
				,A."NombreMoneda"
				,A."IdEmpresa"
				,A."NombreEmpresa"
                ,B."FechaSaldo"
                ,B."Orden"
		 FROM :v_TablaBaseCBan as A
         LEFT JOIN :v_RangoFechaProyeccion as B ON 1 = 1;		
		 --SELECT 'v_RangoFechaProyeccionCBan',* FROM :v_RangoFechaProyeccionCBan; -- REVISAR

		
		--) Obtener saldo > T0 (para movimientos y comprobantes con FP)
		v_SaldoValuta =
		 SELECT	 RF."IdCuentaBanco"
		 		,RF."NombreCuentaBanco"
				,RF."IdBanco"
				,RF."NombreBanco"
				,RF."NumeroCuentaBancaria"
		 		,RF."IdMoneda"
		 		,RF."NombreMoneda"
		 		,RF."IdEmpresa"
		 		,RF."NombreEmpresa"
		 		,RF."FechaSaldo"
		 		,RF."Orden"
		 		,COALESCE(SUM(CFP."MontoEgreso"),0) + COALESCE(SUM(IEFP."MontoEgreso"),0) as "SaldoValutaEgreso"
		 		,COALESCE(SUM(CFP."MontoIngreso"),0) + COALESCE(SUM(IEFP."MontoIngreso"),0) as "SaldoValutaIngreso"
		 		,COALESCE(SUM(CFP."MontoConSigno"),0) + COALESCE(SUM(IEFP."MontoConSigno"),0) as "SaldoValuta"
		   FROM :v_RangoFechaProyeccionCBan as RF
		   LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" as CFP
		   		  ON CFP."IdCuentaBancariaCorredora"	= RF."IdCuentaBanco"
		   		 AND ((CFP."FechaLiquidacion" = RF."FechaSaldo" AND CFP."FechaAfectaSaldo" = '0001-01-01') -- comprobantes que no se han liquidado
		   		 	OR
		   		 	 (CFP."FechaAfectaSaldo" = RF."FechaSaldo"))  -- comprobantes que se liquidaron, pero afecta saldo en fecha futura
		   		 --AND CFP."AfectoSaldo"		= 'N'
		   LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago" AS IEFP
			 	  ON IEFP."IdCuentaBancariaCorredora" = RF."IdCuentaBanco"
				 AND IEFP."FechaLiquidacion" = RF."FechaSaldo"				 
				 AND IEFP."IdEstadoIngresoEgreso" = 'ING'
		  GROUP BY RF."IdCuentaBanco",RF."NombreCuentaBanco",RF."IdBanco",RF."NombreBanco",RF."NumeroCuentaBancaria",RF."IdMoneda"
		  		,RF."NombreMoneda",RF."IdEmpresa",RF."NombreEmpresa",RF."FechaSaldo",RF."Orden";
		  --SELECT 'v_SaldoValuta', * FROM :v_SaldoValuta; -- REVISAR

				  
		v_SaldoLiquidacion = 
		  SELECT  "IdCuentaBanco", "NombreCuentaBanco","IdBanco", "NombreBanco", "NumeroCuentaBancaria"
		   		 ,"IdMoneda", "NombreMoneda","IdEmpresa","NombreEmpresa","FechaSaldo"
		   		 ,MAX(CASE WHEN "Orden"=1 THEN "FechaSaldoValuta" ELSE NULL END) AS "FechaT1"
		 		 ,MAX(CASE WHEN "Orden"=1 THEN "SaldoValutaEgreso" ELSE NULL END) AS "SaldoEgresoT1"
		 		 ,MAX(CASE WHEN "Orden"=1 THEN "SaldoValutaIngreso" ELSE NULL END) AS "SaldoIngresoT1"
		 		 ,MAX(CASE WHEN "Orden"=1 THEN "SaldoValuta" ELSE NULL END) AS "SaldoT1"		 		 
		 		 ,MAX(CASE WHEN "Orden"=2 THEN "FechaSaldoValuta" ELSE NULL END) AS "FechaT2"
		 		 ,MAX(CASE WHEN "Orden"=2 THEN "SaldoValutaEgreso" ELSE NULL END) AS "SaldoEgresoT2"
		 		 ,MAX(CASE WHEN "Orden"=2 THEN "SaldoValutaIngreso" ELSE NULL END) AS "SaldoIngresoT2"
		 		 ,MAX(CASE WHEN "Orden"=2 THEN "SaldoValuta" ELSE NULL END) AS "SaldoT2"		 		 
		 		 ,MAX(CASE WHEN "Orden"=3 THEN "FechaSaldoValuta" ELSE NULL END) AS "FechaT3"
		 		 ,MAX(CASE WHEN "Orden"=3 THEN "SaldoValutaEgreso" ELSE NULL END) AS "SaldoEgresoT3"
		 		 ,MAX(CASE WHEN "Orden"=3 THEN "SaldoValutaIngreso" ELSE NULL END) AS "SaldoIngresoT3"
		 		 ,MAX(CASE WHEN "Orden"=3 THEN "SaldoValuta" ELSE NULL END) AS "SaldoT3"
		 		 		 		 
		    FROM (SELECT  
		   				 SD."IdCuentaBanco"
				 		,SD."NombreCuentaBanco"
						,SD."IdBanco"
						,SD."NombreBanco"
						,SD."NumeroCuentaBancaria"
				 		,SD."IdMoneda"
				 		,SD."NombreMoneda"
				 		,SD."IdEmpresa"
				 		,SD."NombreEmpresa"
				 		,SD."FechaSaldo"
				 		,SV."FechaSaldo" as "FechaSaldoValuta"
				 		,SV."SaldoValutaEgreso"
				 		,SV."SaldoValutaIngreso"
				 		,SV."SaldoValuta"
				 		,SV."Orden"
				   FROM :v_SaldoDisponible as SD				   
				   LEFT JOIN :v_SaldoValuta as SV ON SV."IdCuentaBanco" = SD."IdCuentaBanco"
				  WHERE SV."FechaSaldo" > SD."FechaSaldo")
		   GROUP BY "IdCuentaBanco","NombreCuentaBanco","IdBanco","NombreBanco","NumeroCuentaBancaria","IdMoneda","NombreMoneda",
		   			"IdEmpresa","NombreEmpresa","FechaSaldo"
		   ORDER BY "IdCuentaBanco","FechaSaldo";
		   --SELECT 'v_SaldoLiquidacion', * FROM :v_SaldoLiquidacion; -- REVISAR



		-- Incluir movimientos sin banco o cuenta bancaria asignada(No Afectan Saldo Banco ya que no se sabe a que cuenta afectarán) 
		IF (:p_SoloMovimientosQueAfectanBanco = 'N') THEN
		
				--) Obtener saldo > T0 (para movimientos y comprobantes sin FP)
				v_SaldoValutaSinFP =
				SELECT
					 -1 AS "IdCuentaBanco"	              -- Sin definir
				    ,'SIN DEFINIR' AS "NombreCuentaBanco"
					,-1 AS "IdBanco"                      -- Sin definir
					,'SIN DEFINIR' AS "NombreBanco"
					,'' AS "NumeroCuentaBancaria"
					,MSFP."IdMoneda"
					,MSFP."NombreMoneda"
					,MSFP."IdEmpresa"
					,MSFP."NombreEmpresa"
					,RFP."FechaSaldo"
					,RFP."Orden"
					,COALESCE(SUM(MSFP."MontoEgreso"),0)   as "SaldoValutaEgreso"
					,COALESCE(SUM(MSFP."MontoIngreso"),0)  as "SaldoValutaIngreso"
					,COALESCE(SUM(MSFP."MontoConSigno"),0) as "SaldoValuta"			
				FROM :v_RangoFechaProyeccion AS RFP			
				LEFT JOIN
				(
					SELECT 
						 COM."IdMoneda", COM."NombreMoneda"
						,COM."IdEmpresa", COM."NombreEmpresa", COM."FechaLiquidacion"
						,COM."MontoEgreso" ,COM."MontoIngreso" ,COM."MontoConSigno"
					FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" AS COM 
					WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP WHERE CFP."IdComprobante" = COM."IdComprobante" AND CFP."IdEstado" <> 'ANU') -- sin FP
					AND COM."IdEstado" <> 'ANU'
					AND COM."IdEmpresa" = :p_IdEmpresa	
					UNION ALL			
					SELECT 
						 IE."IdMoneda" , IE."NombreMoneda"
						,IE."IdEmpresa", IE."NombreEmpresa", IE."FechaLiquidacion"
						,IE."MontoEgreso", IE."MontoIngreso", IE."MontoConSigno"
					FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" AS IE
					WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS IEFP WHERE IEFP."IdIngresoEgreso" = IE."IdIngresoEgreso" AND IEFP."IdEstado" <> 'ANU') -- sin FP
					      AND IE."IdEstado" = 'ING'
					      AND IE."IdEmpresa" = :p_IdEmpresa
				) AS MSFP		
				ON ( MSFP."FechaLiquidacion" = RFP."FechaSaldo")		
				GROUP BY MSFP."IdMoneda",MSFP."NombreMoneda",MSFP."IdEmpresa",MSFP."NombreEmpresa",RFP."FechaSaldo",RFP."Orden";
				-- SELECT 'SaldoValutaSinFP:', * FROM :v_SaldoValutaSinFP;   -- REVISAR
		
		
				v_SaldoLiquidacionSinFP = 
				  SELECT  "IdCuentaBanco", "NombreCuentaBanco","IdBanco", "NombreBanco", "NumeroCuentaBancaria"
				   		 ,"IdMoneda", "NombreMoneda","IdEmpresa","NombreEmpresa"
				   		 ,MAX(CASE WHEN "Orden"=1 THEN "FechaSaldoValuta" ELSE NULL END) AS "FechaT1"
				 		 ,MAX(CASE WHEN "Orden"=1 THEN "SaldoValutaEgreso" ELSE NULL END) AS "SaldoEgresoT1"
				 		 ,MAX(CASE WHEN "Orden"=1 THEN "SaldoValutaIngreso" ELSE NULL END) AS "SaldoIngresoT1"
				 		 ,MAX(CASE WHEN "Orden"=1 THEN "SaldoValuta" ELSE NULL END) AS "SaldoT1"
				 		 ,MAX(CASE WHEN "Orden"=2 THEN "FechaSaldoValuta" ELSE NULL END) AS "FechaT2"
				 		 ,MAX(CASE WHEN "Orden"=2 THEN "SaldoValutaEgreso" ELSE NULL END) AS "SaldoEgresoT2"
				 		 ,MAX(CASE WHEN "Orden"=2 THEN "SaldoValutaIngreso" ELSE NULL END) AS "SaldoIngresoT2"
				 		 ,MAX(CASE WHEN "Orden"=2 THEN "SaldoValuta" ELSE NULL END) AS "SaldoT2"
				 		 ,MAX(CASE WHEN "Orden"=3 THEN "FechaSaldoValuta" ELSE NULL END) AS "FechaT3"
				 		 ,MAX(CASE WHEN "Orden"=3 THEN "SaldoValutaEgreso" ELSE NULL END) AS "SaldoEgresoT3"
				 		 ,MAX(CASE WHEN "Orden"=3 THEN "SaldoValutaIngreso" ELSE NULL END) AS "SaldoIngresoT3"
				 		 ,MAX(CASE WHEN "Orden"=3 THEN "SaldoValuta" ELSE NULL END) AS "SaldoT3"		 		 
				    FROM (
				    		SELECT  
				   				 SV."IdCuentaBanco"
						 		,SV."NombreCuentaBanco"
								,SV."IdBanco"
								,SV."NombreBanco"
								,SV."NumeroCuentaBancaria"
						 		,SV."IdMoneda"
						 		,SV."NombreMoneda"
						 		,SV."IdEmpresa"
						 		,SV."NombreEmpresa"
						 		,SV."FechaSaldo"
						 		,SV."Orden"
						 		,SV."FechaSaldo" as "FechaSaldoValuta"
						 		,SV."SaldoValutaEgreso"
						 		,SV."SaldoValutaIngreso"
						 		,SV."SaldoValuta"
						   FROM :v_SaldoValutaSinFP as SV)				  
				   GROUP BY "IdCuentaBanco","NombreCuentaBanco","IdBanco","NombreBanco","NumeroCuentaBancaria","IdMoneda","NombreMoneda",
				   			"IdEmpresa","NombreEmpresa";
				   -- SELECT 'SaldoLiquidacionSinFP:', * FROM :v_SaldoLiquidacionSinFP;   -- REVISAR

			END IF; -- IF (:p_SoloMovimientosQueAfectanBanco = 'N')


		-------------------------------------------
		-- RETORNO INFORMACION SALDO BANCO
		-------------------------------------------
		p_out_SaldoCuentaBanco =             -- Movimientos con forma de pago
		 SELECT  SD."IdCuentaBanco"
		 		,SD."NombreCuentaBanco"
				,SD."IdBanco"
				,SD."NombreBanco"
				,SD."NumeroCuentaBancaria"
		 		,SD."IdMoneda"
		 		,SD."NombreMoneda"
		 		,SD."IdEmpresa"
		 		,SD."NombreEmpresa"
		 		,SD."FechaSaldo"
		 		,CAST(SD."SaldoDisponibleInicial" as DECIMAL(26,8)) as "SaldoDisponibleInicial"
		 		,CAST(SD."SaldoDisponibleIngreso" as DECIMAL(26,8)) as "SaldoDisponibleIngreso"
		 		,CAST(SD."SaldoDisponibleEgreso" as DECIMAL(26,8)) as "SaldoDisponibleEgreso"
		 		,CAST(SD."SaldoDisponible" as DECIMAL(26,8)) as "SaldoDisponible"
		 		,CAST(SP."SaldoPendienteIngreso" as DECIMAL(26,8)) as "SaldoPendienteIngreso"
		 		,CAST(SP."SaldoPendienteEgreso" as DECIMAL(26,8)) as "SaldoPendienteEgreso"
		 		,CAST(SP."SaldoPendiente" as DECIMAL(26,8)) as "SaldoPendiente"
		 		,CAST(SC."SaldoContableIngreso" as DECIMAL(26,8)) as "SaldoContableIngreso"
		 		,CAST(SC."SaldoContableEgreso" as DECIMAL(26,8)) as "SaldoContableEgreso"
		 		,CAST(SC."SaldoContable" as DECIMAL(26,8)) as "SaldoContable"		 		
		 		,"FechaT1"
		 		,CAST(SL."SaldoIngresoT1" as DECIMAL(26,8)) as "SaldoIngresoT1"
		 		,CAST(SL."SaldoEgresoT1" as DECIMAL(26,8)) as "SaldoEgresoT1"
		 		,CAST(SL."SaldoT1" as DECIMAL(26,8)) as "SaldoT1"
		 		,"FechaT2"
		 		,CAST(SL."SaldoIngresoT2" as DECIMAL(26,8)) as "SaldoIngresoT2"
		 		,CAST(SL."SaldoEgresoT2" as DECIMAL(26,8)) as "SaldoEgresoT2"
		 		,CAST(SL."SaldoT2" as DECIMAL(26,8)) as "SaldoT2"
		 		,"FechaT3"
		 		,CAST(SL."SaldoIngresoT3" as DECIMAL(26,8)) as "SaldoIngresoT3"
		 		,CAST(SL."SaldoEgresoT3" as DECIMAL(26,8)) as "SaldoEgresoT3"
		 		,CAST(SL."SaldoT3" as DECIMAL(26,8)) as "SaldoT3"
		  FROM :v_SaldoDisponible AS SD
		  LEFT JOIN :v_SaldoPendiente AS SP ON SP."IdCuentaBanco" = SD."IdCuentaBanco" AND SP."FechaSaldo" = SD."FechaSaldo"
		  LEFT JOIN :v_SaldoContable AS SC ON SC."IdCuentaBanco" = SD."IdCuentaBanco" AND SC."FechaSaldo" = SD."FechaSaldo"
		  LEFT JOIN :v_SaldoLiquidacion AS SL ON SL."IdCuentaBanco" = SD."IdCuentaBanco" AND SL."FechaSaldo" = SD."FechaSaldo";



		-- Incluir movimientos sin banco o cuenta bancaria asignada(No Afectan Saldo Banco ya que no se sabe a que cuenta afectarán) 
		IF (:p_SoloMovimientosQueAfectanBanco = 'N') THEN
	
			  p_out_SaldoCuentaBanco = 
			  SELECT * FROM :p_out_SaldoCuentaBanco UNION ALL	
			  SELECT  SP."IdCuentaBanco"           -- Movimientos sin forma de pago
			 		,SP."NombreCuentaBanco"
					,SP."IdBanco"
					,SP."NombreBanco"					
					,SP."NumeroCuentaBancaria"
			 		,SP."IdMoneda"
			 		,SP."NombreMoneda"
			 		,SP."IdEmpresa"
			 		,SP."NombreEmpresa"
			 		,SP."FechaSaldo"		 		
			 		,0 As "SaldoDisponibleInicial"
			 		,0 As "SaldoDisponibleIngreso"
			 		,0 As "SaldoDisponibleEgreso"
			 		,0 As "SaldoDisponible"
			 		,COALESCE(CAST(SP."SaldoPendienteIngresoSinFP" as DECIMAL(26,8)),0) As "SaldoPendienteIngreso"
			 		,COALESCE(CAST(SP."SaldoPendienteEgresoSinFP" as DECIMAL(26,8)),0) As "SaldoPendienteEgreso"
			 		,COALESCE(CAST(SP."SaldoPendienteSinFP" as DECIMAL(26,8)),0) As "SaldoPendiente"
			 		,COALESCE(CAST(SP."SaldoPendienteIngresoSinFP" as DECIMAL(26,8)),0) as "SaldoContableIngreso"
			 		,COALESCE(CAST(SP."SaldoPendienteEgresoSinFP" as DECIMAL(26,8)),0) as "SaldoContableEgreso"
			 		,COALESCE(CAST(SP."SaldoPendienteSinFP" as DECIMAL(26,8)),0) as "SaldoContable"
			 		,SL."FechaT1"
			 		,COALESCE(CAST(SL."SaldoIngresoT1" as DECIMAL(26,8)),0) as "SaldoIngresoT1"
			 		,COALESCE(CAST(SL."SaldoEgresoT1" as DECIMAL(26,8)),0) as "SaldoEgresoT1"
			 		,COALESCE(CAST(SL."SaldoT1" as DECIMAL(26,8)),0) as "SaldoT1"
			 		,SL."FechaT2"
			 		,COALESCE(CAST(SL."SaldoIngresoT2" as DECIMAL(26,8)),0) as "SaldoIngresoT2"
			 		,COALESCE(CAST(SL."SaldoEgresoT2" as DECIMAL(26,8)),0) as "SaldoEgresoT2"
			 		,COALESCE(CAST(SL."SaldoT2" as DECIMAL(26,8)),0) as "SaldoT2"
			 		,SL."FechaT3"
			 		,COALESCE(CAST(SL."SaldoIngresoT3" as DECIMAL(26,8)),0) as "SaldoIngresoT3"
			 		,COALESCE(CAST(SL."SaldoEgresoT3" as DECIMAL(26,8)),0) as "SaldoEgresoT3"
			 		,COALESCE(CAST(SL."SaldoT3" as DECIMAL(26,8)),0) as "SaldoT3"
			  FROM :v_SaldoPendienteSinFP AS SP
	  		  INNER JOIN :p_DatosAConsulta as DC ON (DC."IdMoneda" = SP."IdMoneda" OR COALESCE(DC."IdMoneda",'')='')
			  LEFT JOIN :v_SaldoLiquidacionSinFP AS SL ON (SL."IdMoneda" = SP."IdMoneda");		  
		  		
		END IF; -- IF (:p_SoloMovimientosQueAfectanBanco = 'N')

		
	END;
END;
