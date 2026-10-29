SET SCHEMA "BSCL";   
GO
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.DevengoForward";
GO
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.DevengoForward" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          	BIGINT,
    IN p_IdPais          		BIGINT,
    IN p_IdUsuario          	BIGINT,
    IN p_IdBolsa				NVARCHAR(3),
    IN p_IdSucursal				BIGINT,
    IN p_IdModulo           	NVARCHAR(3),
    IN p_UsaReglasNegocio		NVARCHAR(1),
    IN p_FechaProceso			NVARCHAR(10),
    OUT p_out_ErroresContador 	INT,   
	OUT p_out_ErroresDetalle 	TABLE( "Codigo" 		NVARCHAR(100)
	                                  ,"Descripcion" 	NVARCHAR(5000)
	                                 )
 )   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	   
BEGIN
	DECLARE v_IdGrupoLog BIGINT;
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE v_out_AdvertenciasDetalle  	TABLE ("FolioCartera" 			BIGINT
											 , "Codigo" 				NVARCHAR(100)
											 , "RequiereAutorizacion" 	NVARCHAR(1)
											 , "ValorParametro"	 		NVARCHAR(100)
											 ,"NombreParametro" 		NVARCHAR(40)
									  	);
	DECLARE v_out_ErroresDetalle  	   	TABLE ("FolioCartera" 			BIGINT
											 , "Codigo" 				NVARCHAR(100)
											 , "ValorParametro" 		NVARCHAR(100)
											 , "NombreParametro" 		NVARCHAR(40)
										);

	DECLARE v_VoucherDevengo			NVARCHAR(1);
	DECLARE v_DevengoDiasCorridos 		NVARCHAR(1);
	DECLARE v_FechaSistema				NVARCHAR(10);
	DECLARE v_FechaOperacionSecondate	SECONDDATE;
	DECLARE v_UltimoDiaMes				NVARCHAR(10);
	DECLARE v_UltimoDiaInhabil			NVARCHAR(1);
	DECLARE v_Habil						NVARCHAR(1);
	DECLARE v_FechaAnterior				NVARCHAR(10);
	DECLARE v_TABLE_OID_CAR				INT;
	DECLARE v_EsPrimerDiaHabil			NVARCHAR(1);
	DECLARE v_FechaPrimerDiaMes			NVARCHAR(10);
	DECLARE v_FechaFinMes 				NVARCHAR(10);
	DECLARE v_ndiadevatras				INT;
	DECLARE v_ndiadev 					INT;
	DECLARE v_Month						INT;
	
	/*Fecha de sistema*/
	DECLARE v_DolarPro				DECIMAL(26,8);
	DECLARE v_Euro					DECIMAL(26,8);
	DECLARE v_Uf					DECIMAL(26,8);
	DECLARE v_ParDolarPro			DECIMAL(26,8);
	DECLARE v_ParEuro				DECIMAL(26,8);
	DECLARE v_ParUf					DECIMAL(26,8);

	
	/*Fecha de sistema anterior*/
	DECLARE v_DolarProAnt			DECIMAL(26,8);
	DECLARE v_EuroAnt				DECIMAL(26,8);
	DECLARE v_UfAnt					DECIMAL(26,8);
	DECLARE v_ParDolarProAnt		DECIMAL(26,8);
	DECLARE v_ParEuroAnt			DECIMAL(26,8);
	DECLARE v_ParUfAnt				DECIMAL(26,8);

	
	/*Variables para el LOG*/
	DECLARE v_IdCliente					BIGINT;
	DECLARE v_NombreCliente				NVARCHAR(100);
    DECLARE v_IdTipoDocumentoIdentidad	NVARCHAR(40);
    DECLARE v_IdDocumentoIdentidad		NVARCHAR(40);
    DECLARE v_EstadoCliente				NVARCHAR(3);
    DECLARE v_DetalleLog 			    TABLE (
    										  "SubRegistro" 	NVARCHAR(40)
                                            , "CampoRegistro" 	NVARCHAR(40)
                                            , "ValorAntiguo" 	NVARCHAR(250)
                                            , "ValorNuevo" 		NVARCHAR(250)
                                        );
							
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		
		v_TablaCodigosMensajes = SELECT "Codigo" 			AS "CodigoMensaje",
								        '' 	AS "CodigoDato",
  								        '' 	AS "ValorDato" 
								 FROM :p_out_ErroresDetalle;
								 /*
								 UNION ALL
								 SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
								        "ValorParametro" 	AS "ValorDato" 
								 FROM :v_out_AdvertenciasDetalle;
		  						 */
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."Codigo"
									 , msj."Descripcion"
 								FROM :p_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";				 						 	
	END;
	
	p_out_ErroresContador		:= 0;
	
	BEGIN
	    
	    CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"( 
	    	:p_IdEmpresa
	        ,v_FechaOperacionSecondate 
	    );
      
		SELECT COUNT(TABLE_OID) 
		INTO v_TABLE_OID_CAR 
		FROM "SYS"."M_TEMPORARY_TABLES" 
		WHERE SCHEMA_NAME ='BSCL' 
		AND TABLE_NAME = '#CARTERADEVENGO'
		AND "CONNECTION_ID" = CURRENT_CONNECTION;
	
		IF (:v_TABLE_OID_CAR > 0) THEN
			DROP TABLE #CARTERADEVENGO;
		END IF;

		CREATE LOCAL TEMPORARY COLUMN TABLE #CARTERADEVENGO(
			  "FolioCartera"		BIGINT
		     ,"IdOperacion"			BIGINT
		     ,"MontoOperacion"		DECIMAL(26,8)
		     ,"MontoDiferido"		DECIMAL(26,8)
		     ,"TipoCambioMercado"	DECIMAL(26,8)
		     ,"TipoCambioCierre"	DECIMAL(26,8)
		     ,"TipoCambioCosto"		DECIMAL(26,8)
		     ,"MontoDevengo"		DECIMAL(26,8)
  		     ,"MontoDevengoDiario"	DECIMAL(26,8)
		); 
      
		SELECT COALESCE("Valor",'N')
		INTO v_DevengoDiasCorridos
		FROM "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa"
		WHERE "IdTipoProducto" = 'MEFW'
		AND "CodigoParametro" = 'DEVENGADIANOHABIL';
		
		
		SELECT "Valor" 
		INTO v_FechaSistema
		FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		WHERE "IdEmpresa" = :p_IdEmpresa 
		AND "CodigoParametro" = 'PLA.FechaSistema';            
									 
		IF (:v_DevengoDiasCorridos = 'S') THEN
			
			SELECT ADD_DAYS(TO_DATE(:v_FechaSistema)
			               , - DAYOFMONTH(TO_DATE(:v_FechaSistema))
			               ) 
			INTO v_UltimoDiaMes
			FROM DUMMY;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil" ( 	
				p_Fecha 	=> :p_FechaProceso,
				p_IdPais	=> :p_IdPais,
				p_out_Habil	=> v_Habil
			); 
			
			--Si el devengo se ejecuta el ultimo dia del mes y ademas el ultimo dia es inhabil   
			IF (:p_FechaProceso = :v_UltimoDiaMes AND :v_Habil = 'S') THEN
				v_UltimoDiaInhabil := 'S';
			END IF; 
			 
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaAnterior" (
 			p_Fecha   			=> :p_FechaProceso,
			p_NumeroDias 		=> 1,
			p_EsHabil  			=> 'S',
			p_IdPais  			=> :p_IdPais,
			p_out_FechaAnterior => v_FechaAnterior
		);
		
		SELECT CASE WHEN "Valor" = '' THEN
					'N'
			   ELSE COALESCE("Valor",'N')
			   END
		INTO v_VoucherDevengo
		FROM "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa"
		WHERE "IdTipoProducto" = 'MEFW'
		AND "CodigoParametro" = 'VOUCHERDEVENGO';

		IF (:v_VoucherDevengo = 'S') THEN    
		    
		    SELECT MONTHS_BETWEEN(TO_SECONDDATE(:v_FechaAnterior, 'YYYY-MM-DD')
								 ,TO_SECONDDATE(:p_FechaProceso, 'YYYY-MM-DD'))
			INTO v_month
			FROM DUMMY;
			
		    IF (v_month = 1) THEN  
				v_EsPrimerDiaHabil := 'S';
				
				SELECT SUBSTRING(TO_CHAR(:p_FechaProceso),1,7) || '-01'
				INTO v_FechaPrimerDiaMes
				FROM DUMMY;
				 
				SELECT ADD_DAYS(TO_DATE(:v_FechaPrimerDiaMes), -1)
				INTO v_FechaFinMes
				FROM DUMMY;
				
				SELECT DAYS_BETWEEN(TO_SECONDDATE(:p_FechaProceso, 'YYYY-MM-DD')
								   ,TO_SECONDDATE(:v_FechaFinMes, 'YYYY-MM-DD'))
				INTO v_ndiadevatras
				FROM DUMMY;
				
				v_ndiadev := v_ndiadevatras; 
		  	END IF;  
		END IF;  
		
		
		/*TIPO DE CAMBIO Y PARIDAD FECHA SISTEMA*/
/*    	SELECT COALESCE(USD."TipoCambioMercado",0)
    	     , COALESCE(USD."ParidadMercado",0)
    	     , COALESCE(EUR."TipoCambioMercado",0)
    	     , COALESCE(EUR."ParidadMercado",0)
    	     , COALESCE(UF."TipoCambioMercado",0)
    	     , COALESCE(UF."ParidadMercado",0)
    	INTO v_DolarPro
		   , v_ParDolarPro
    	   , v_Euro
    	   , v_ParEuro
    	   , v_Uf
		   , v_ParUf
  	    FROM "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" AS USD
  	       , "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" AS EUR
  	       , "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" AS UF
  	    WHERE USD."Fecha" = :v_FechaSistema
  	    AND USD."IdEstado" = 'ACT'
  	    AND USD."IdMoneda" = 'USD'
  	    AND EUR."Fecha" = :v_FechaSistema
  	    AND EUR."IdEstado" = 'ACT'
   	    AND EUR."IdMoneda" = 'EUR'
   	    AND UF."Fecha" = :v_FechaSistema
   	    AND UF."IdEstado" = 'ACT'
   	    AND UF."IdMoneda" = 'UF';
  	    
      

		SELECT COALESCE(USD."TipoCambioMercado",0)
    	     , COALESCE(USD."ParidadMercado",0)
    	     , COALESCE(EUR."TipoCambioMercado",0)
    	     , COALESCE(EUR."ParidadMercado",0)
    	     , COALESCE(UF."TipoCambioMercado",0)
    	     , COALESCE(UF."ParidadMercado",0)
    	INTO v_DolarProAnt
		   , v_ParDolarProAnt
    	   , v_EuroAnt
    	   , v_ParEuroAnt
    	   , v_UfAnt
		   , v_ParUfAnt
  	    FROM "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" AS USD
  	       , "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" AS EUR
  	       , "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" AS UF
  	    WHERE USD."Fecha" = :v_FechaAnterior
  	    AND USD."IdEstado" = 'ACT'
  	    AND USD."IdMoneda" = 'USD'
  	    AND EUR."Fecha" = :v_FechaAnterior
  	    AND EUR."IdEstado" = 'ACT'
   	    AND EUR."IdMoneda" = 'EUR'
   	    AND UF."Fecha" = :v_FechaAnterior
   	    AND UF."IdEstado" = 'ACT'
   	    AND UF."IdMoneda" = 'UF';
		*/
		
		INSERT INTO #CARTERADEVENGO
	    SELECT "FolioCartera"						
		     , "IdOperacion"
		     , "Saldo"											AS "MontoOperacion"
		     , "INGMontoDiferido"								AS "MontoDiferido"
		     , PARIDAD."TipoCambioMercado"					    AS "TipoCambioMercado"
		     , CAST("ValorTipoCambioCierre" AS DECIMAL(26,8))	AS "TipoCambioCierre"
		     , CAST("ValorTipoCambioCosto" AS DECIMAL(26,8))	AS "TipoCambioCosto"
		     

		     , CAST(((("ValorTipoCambioCierre" - "ValorTipoCambioCosto") * "Saldo") / CARTERA."PlazoEnDiasOperacion") AS DECIMAL(26,8)) AS "MontoDevengo"
   		     , CAST(((("ValorTipoCambioCierre" - "ValorTipoCambioCosto") * "Saldo") / DAYS_BETWEEN(TO_SECONDDATE(:v_FechaSistema, 'YYYY-MM-DD')
																								   ,TO_SECONDDATE(CARTERA."FechaLiquidacion", 'YYYY-MM-DD'))																
												   																	) AS DECIMAL(26,8)) AS "MontoDevengoDiario"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CARTERA
		INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MONEDA
				ON MONEDA."IdMoneda" = CARTERA."IdMonedaPrincipal"
			   AND MONEDA."IdEstado" = 'ACT'
		INNER JOIN "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" AS PARIDAD
				ON PARIDAD."IdMoneda" = CARTERA."IdMonedaPrincipal"
			   AND TO_CHAR(PARIDAD."Fecha",'YYYY-MM-DD') = TO_CHAR(CARTERA."FechaOperacion",'YYYY-MM-DD')
		LEFT JOIN "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" AS PARIDADANT
				ON PARIDADANT."IdMoneda" = CARTERA."IdMonedaPrincipal"
			   AND TO_CHAR(PARIDADANT."Fecha",'YYYY-MM-DD') = TO_CHAR(:v_FechaAnterior,'YYYY-MM-DD')
		WHERE "IdTipoProducto" = 'MEFW'
		AND (CARTERA."IdEstado" = 'VIG' AND "FechaVencimientoOperacion" > TO_DATE(:p_FechaProceso))
		OR	(CARTERA."IdEstado" IN ('VIG','LIQ') AND "FechaVencimientoOperacion" = TO_DATE(:p_FechaProceso));

		SELECT *
		FROM #CARTERADEVENGO;

		
		--DATOS DETALLE LOG
		v_DetalleLog = 
		(Select 'DVGFWD'	    	AS "SubRegistro",
				'DEVENGO FORWARD'  	AS "CampoRegistro",--MOD."IdLimite"			 		 as "CampoRegistro",
				''   				AS "ValorAntiguo",--MOD."IdEstadoAutorizacion"  	 as "ValorAntiguo",
				'Genero Devengo de Forward : ' || TO_VARCHAR(:v_FechaOperacionSecondate, 'YYYY-MM-DD')	AS "ValorNuevo"
		 FROM dummy
		);

		--GRABAR LOG
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			 p_IdTipoLog		 	=> 'DEVENGO' 
			,p_IdModulo 			=> :p_IdModulo 
			,p_IdUsuario 			=> :p_IdUsuario 
			,p_IdEmpresa 			=> :p_IdEmpresa 
			,p_FechaSistemaLog 		=> TO_VARCHAR (:v_FechaOperacionSecondate, 'YYYY-MM-DD')
			,p_IdUsuarioAtendido 	=> :p_IdUsuario
			,p_IdTipoRegistro 		=> 'FORWARD' --v_TipoEntidad--'CLIENTE'
			,p_DetalleLog 			=> :v_DetalleLog	-- hacia arriba obligatorios
			,p_NombreProceso 		=> 'Forward'-- hacia abajo opcionales
			,p_EstadoProceso 		=> 'VIG'
		);
		--LOG DE AUDITORIA (Fin)
		
	END;
END; 



GO