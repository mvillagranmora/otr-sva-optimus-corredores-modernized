SET SCHEMA "BSCL";   

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.GraboCalceOperaciones";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.GraboCalceOperaciones" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          	BIGINT,
    IN p_IdPais          		BIGINT,
    IN p_IdUsuario          	BIGINT,
    IN p_IdModulo           	NVARCHAR(3),
    IN p_UsaReglasNegocio		NVARCHAR(1),
	IN p_tablaCalce				TABLE(
									"FolioInterno"			BIGINT
								   ,"IdOperacion"			BIGINT
								   ,"FolioCartera"			BIGINT
								   ,"IdTipoOperacion"		NVARCHAR(6)
								   ,"IdMonedaPrincipal"		NVARCHAR(3)
								   ,"IdTipoInstrumento"		NVARCHAR(3)
								   ,"IdInstrumento"			NVARCHAR(6)
								   ,"FechaVencimiento"		DATE
								   ,"MontoOperacion"		DECIMAL(26,8)
								   ,"MontoDisponible"		DECIMAL(26,8)
	   							   ,"MontoCalce"			DECIMAL(26,8)
								),						
	-- Parametros de Salida
	OUT p_out_ErroresContador 	INT,   
	OUT p_out_ErroresDetalle 	TABLE ("IdInterno" 		BIGINT
	                                  ,"Codigo" 		NVARCHAR(100)
	                                  ,"Descripcion" 	NVARCHAR(5000))
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
	        	
	DECLARE v_out_ErroresDetalle  	TABLE ("IdInterno" 			BIGINT
										  ,"Codigo" 			NVARCHAR(100)
										  ,"ValorParametro" 	NVARCHAR(100)
										  ,"NombreParametro" 	NVARCHAR(40)
										  )
									DEFAULT SELECT 0 	AS "IdInterno" 			
										  	     , ''	AS "Codigo" 			
										         , '' 	AS "ValorParametro" 	
										         , '' 	AS "NombreParametro" 	
										    FROM DUMMY;
										    
	DECLARE v_fechaHoraSistema		NVARCHAR(40);
	DECLARE v_Operaciones_Calzadas	BIGINT;

	DECLARE v_tablaReservaCartera 	TABLE(
										"IdOperacion"			BIGINT,
    									"IdRegistro"			BIGINT,
    									"IdReserva"				BIGINT,
    									"IdConcepto"			NVARCHAR(6),	-- INGRES / RETRES / INGDET / RETDET / LIMDET
    									"FolioOperacion"		BIGINT,
    									"FolioCartera" 			BIGINT, 
    									"FolioCarteraCustodia" 	BIGINT,     									
    									"IdTipoProducto"		NVARCHAR(4),
    									"Cantidad" 				DECIMAL(26,8),
    									"Tasa" 					DECIMAL(26,8),
    									"TasaMercado"           DECIMAL(26,8),
     									"Precio"    DECIMAL(26,8),
     									"PrecioMercado"         DECIMAL(26,8),
    									"ValorRescate"			DECIMAL(26,8),
    									"MontoValorizado"		DECIMAL(26,8),
    									"IdClasificacionTipoInstrumento" NVARCHAR(3),
    									"IdTipoInstrumento"		NVARCHAR(3),
    									"IdInstrumento"			BIGINT,
    									"IdTipoReajuste"		NVARCHAR(3),
    									"IdEmisor"				NVARCHAR(40),
    									"CorteMinimo"			DECIMAL(26,8),
    									"FechaVencimiento"		DATE,
    									"EsFungible"			NVARCHAR(1),
    									"Glosa"					NVARCHAR(260),
    									"IdBolsa"				NVARCHAR(3),
										"IdBoveda"				NVARCHAR(3),	
										"CodigoIFRS"			NVARCHAR(40)
									);
	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :v_out_ErroresDetalle;	
		
		v_TablaCodigosMensajes = SELECT "Codigo" 			AS "CodigoMensaje",
								        "NombreParametro" 	AS "CodigoDato",
  								        "ValorParametro" 	AS "ValorDato" 
								 FROM :v_out_ErroresDetalle;
		  
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (
			:v_TablaCodigosMensajes
		    ,v_out_ErroresDetalleMensajes
		);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
									   errores."Codigo",
									   msj."Descripcion"
 								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj 
										ON errores."Codigo" = msj."Codigo";
	END;
	
	p_out_ErroresContador		:= 0;
	
	                            
	v_out_ErroresDetalle = SELECT 0 	AS "IdInterno" 
	                            , '' 	AS "Codigo"
	                            , '' 	AS "ValorParametro"
	                            , '' 	AS "NombreParametro" 
	                       FROM dummy 
	                       WHERE 1=2;

	--------------------
	--Negocio Aqui
	--------------------
	BEGIN -- proceso de negocio      
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
			p_IdEmpresa => :p_IdEmpresa,
			p_out_FechaHoraSistema => v_fechaHoraSistema
		);
        /**********************************************************************************************/
		/******************************** Ingreso Operaciones Calzadas ********************************/
		/**********************************************************************************************/		
        SELECT COUNT(1) AS "Count"
        INTO v_Operaciones_Calzadas
        FROM :p_tablaCalce;

        IF(:v_Operaciones_Calzadas > 0) THEN
        
         
        	INSERT INTO "BSCL.Custodia.BD::PAR.IngresoReserva_IN"
        						SELECT "FolioInterno"			AS "IdOperacion"	
    									 , 0 						AS "IdRegistro"		
    									 , 0						AS "IdReserva"			
    									 , 'INGRES'					AS "IdConcepto"
    									 , 0						AS "FolioOperacion"
    									 , CUS."IdOperacion"		AS "FolioCartera"
     									 , CUS."FolioCartera"		AS "FolioCarteraCustodia"
    									 , 'MEFW'					AS "IdTipoProducto"	
    									 , FWDCALCE."MontoCalce" 	AS "Cantidad" 			
    									 , 0 						AS "Tasa" 				
    									 , 0						AS "TasaMercado"
    									 , 0						AS "Precio"
    									 , 0						AS "PrecioMercado"
    									 , 0						AS "ValorRescate"			
    									 , FWDCALCE."MontoCalce"	AS "MontoValorizado"	
    									 , 'DV'						AS "IdClasificacionTipoInstrumento"
								     	 , FWDCALCE."IdTipoInstrumento"	AS "IdTipoInstrumento"
    									 , FWDCALCE."IdInstrumento"	AS "IdInstrumento"
    									 , ''						AS "IdTipoReajuste"	
    									 , ''						AS "IdEmisor"
    									 , 0						AS "CorteMinimo"			
    									 , "FechaVencimiento"	    AS "FechaVencimiento"
    									 , 'N'						AS "EsFungible"		
    									 , ''						AS "Glosa"
    									 , 0						AS "IdBolsa"
    									 , 0						AS "IdBoveda"
    									 , 0						AS "CodigoIFRS"					
        							FROM :p_tablaCalce AS FWDCALCE
        							INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CUS  
									 		ON CUS."FolioCartera" = FWDCALCE."FolioCartera"
									 	   AND CUS."IdInstrumento" = FWDCALCE."IdInstrumento"
									 	   AND CUS."IdTipoProducto" = 'MEFW' AND CUS."IdEstado"!='MOD'
        							UNION ALL
									SELECT CUS."IdOperacion"	
    									 , 0 						AS "IdRegistro"		
    									 , 0						AS "IdReserva"			
    									 , 'INGRES'					AS "IdConcepto"		--NVARCHAR(6),	-- INGRES / RETRES / INGDET / RETDET / LIMDET
    									 , 0						AS "FolioOperacion"	
    									 , "FolioInterno"			AS "FolioCartera"
    									 , CUS."FolioCartera"		AS "FolioCarteraCustodia" 		
    									 , 'MEFW'					AS "IdTipoProducto"	
    									 , FWDCALCE."MontoCalce" 	AS "Cantidad" 			
    									 , 0 						AS "Tasa" 				
    									 , 0						AS "TasaMercado"
    									 , 0						AS "Precio"
    									 , 0						AS "PrecioMercado"
    									 , 0						AS "ValorRescate"			    									 	
    									 , FWDCALCE."MontoCalce"	AS "MontoValorizado"	
    									 , 'DV'						AS "IdClasificacionTipoInstrumento"
    									 , CUS."IdTipoInstrumento"	AS "IdTipoInstrumento"
    									 , CUS."IdInstrumento"		AS "IdInstrumento"
    									 , ''						AS "IdTipoReajuste"	
    									 , ''						AS "IdEmisor"
    									 , 0						AS "CorteMinimo"			
    									 , CUS."FechaLiquidacion"	AS "FechaVencimiento"	
    									 , 'N'						AS "EsFungible"		
    									 , ''						AS "Glosa"
    									 , 0						AS "IdBolsa"
    									 , 0						AS "IdBoveda"
    									 , 0						AS "CodigoIFRS"					
        							FROM :p_tablaCalce AS FWDCALCE
									INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" AS CUS  
									 		ON CUS."FolioCartera" = FWDCALCE."FolioCartera"
									 	   AND CUS."IdInstrumento" = FWDCALCE."IdInstrumento"
									 	   AND CUS."IdTipoProducto" = 'MEFW' AND CUS."IdEstado"!='MOD';
			v_tablaReservaCartera = select * from "BSCL.Custodia.BD::PAR.IngresoReserva_IN";							
        	CALL "BSCL.Custodia.BD.Servicios::IngresoReserva" (
			      p_IdEmpresa 				=> :p_IdEmpresa
			    , p_IdUsuario 				=> :p_IdUsuario
			    , p_IdModulo 				=> :p_IdModulo
			    , p_simularOperacion		=> 'N'	
				, p_UsaReglasNegocio		=> 'N'	
				, p_IdPais 					=> :p_IdPais
			    
			    --* VARIABLES DEL PROCESO OBLIGATORIAS
				, p_FechaOperacion			=> :v_fechaHoraSistema
			    , p_TblCarteraReserva		=> :v_tablaReservaCartera
				--, p_out_IdReserva    		=> p_out_IdReserva
				, p_out_ErroresContador		=> p_out_ErroresContador
				--, p_out_ErroresDetalle		=> p_out_ErroresDetalleRes
			);
			
			
			
			--LOG DE AUDITORIA (Inicio)
			BEGIN
				DECLARE p_IdLog BIGINT;
				DECLARE CURSOR cur FOR SELECT "FolioCartera","MontoCalce" FROM :p_tablaCalce;			
				DECLARE v_LogDetalle TABLE (
					"IdCabeceraLog"    BIGINT,
					"SubRegistro"     NVARCHAR(40),
					"CampoRegistro"    NVARCHAR(500),
					"ValorAntiguo"    NVARCHAR(250),
					"ValorNuevo"     NVARCHAR(250)
				);
				SELECT "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL INTO p_IdLog FROM dummy;	
					
				v_log = SELECT :p_IdLog AS "IdCabeceraLog"
								,'INGRESO' AS "IdTipoLog"
								,:p_IdModulo AS "IdModulo"
								,:p_IdUsuario AS "IdUsuario"
								,:p_IdEmpresa AS "IdEmpresa"
								,TO_VARCHAR (COALESCE(v_fechaHoraSistema, '0001-01-01'), 'YYYY-MM-DD') as "FechaSistemaLog"
								,:p_IdUsuario AS "IdUsuarioAtendido"
								,0  AS "IdPersona"
								,0  AS "IdCliente"
								,'' AS "EstadoCliente"
								,'' AS "NombreCliente"
								,'' AS "IdTipoDocumentoIdentidad"
								,'' AS "IdDocumentoIdentidad"
								,0  AS "IdPortafolio"
								,'' AS "EstadoPortafolio"
								,'CALCE_DE_OPERACIONES' AS "IdTipoRegistro"
								,'' AS "IdRegistro"
								,'' AS "EstadoRegistro"
								,'' AS "IdTipoOperacion"
								,0 AS "Folio"
								,0 AS "Cantidad"
								,0 AS "PrecioTasa"
								,0 AS "Monto"
								,'' AS "IdMonedaOrigen"
								,'' AS "IdMonedaLiquidacion"
								,'' AS "TipoCambio"
								,'Forward' AS "Nemo"
								,0  AS "IdProceso" 
								,'' AS "EstadoProceso"
								,'' AS "NombreProceso"
						FROM DUMMY;

				FOR fila_cursor AS cur
				DO
					INSERT INTO :v_LogDetalle ("IdCabeceraLog","SubRegistro","CampoRegistro","ValorAntiguo","ValorNuevo")
					Values(
							:p_IdLog 
							,'Folio: ' || COALESCE(TO_VARCHAR(fila_cursor."FolioCartera"), '')
							,'MontoCalce:'
							,''
							,COALESCE(TO_VARCHAR(fila_cursor."MontoCalce"), '')
					);
				END FOR;		
							
				CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
			END;	
			--LOG DE AUDITORIA (Fin)
			
			/*
	        SELECT COUNT(1) AS "Count"
			INTO p_out_ErroresContador	
	        FROM :p_out_ErroresDetalleRes;
				                   							
			IF (:p_out_ErroresContador > 0) THEN
				p_out_ErroresDetalle = SELECT "IdRegistro" AS "IdInterno"
			                                , "Codigo"
                                            , "Descripcion"
								   	   FROM :p_out_ErroresDetalleRes;

				SIGNAL SALIDA_CONTROLADA;
			END IF;
			*/
        END IF;
        
        
		/**********************************************************************************************/
		/********************************  Cambio de Estado PEN a PDA  ********************************/
		/**********************************************************************************************/		
    	/*
        v_TblNuevoEstado= SELECT "IdOperacion"	AS "IdOperacion"
							   , 'MEFW'			AS "IdTipoProducto"
						       , 'PDA' 			AS "IdNuevoEstado"
						  FROM :p_out_RegistroEvaluacionLimite
					   	  WHERE "IdEstado" = 'PEN'
					   	    AND "IdEstadoAutorizacion" = 'PEN';
	   
		CALL "BSCL.Custodia.BD.Servicios::CambiarEstadoOperacion" (
		     p_IdEmpresa 		=> :p_IdEmpresa     
		    ,p_IdUsuario 		=> :p_IdUsuario     
		    ,p_IdModulo 		=> :p_IdModulo    	
		    ,p_simularOperacion => 'N'  			
		    ,p_UsaReglasNegocio => 'N'  			
		    ,p_IdPais 			=> :p_IdPais        
			,p_TblNuevoEstado 	=> :v_TblNuevoEstado
		    ,p_out_TblResultado => p_out_TblResultado
		);	
		*/	
			
			
		--) Validar errores
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle; 
		IF (:p_out_ErroresContador > 0) THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
	END;
END; 