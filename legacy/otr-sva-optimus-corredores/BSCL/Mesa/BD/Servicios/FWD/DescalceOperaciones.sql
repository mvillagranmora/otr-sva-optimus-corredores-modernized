SET SCHEMA "BSCL";   

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.DescalceOperaciones";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.DescalceOperaciones" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          	BIGINT,
    IN p_IdPais          		BIGINT,
    IN p_IdUsuario          	BIGINT,
    IN p_IdModulo           	NVARCHAR(3),
    IN p_UsaReglasNegocio		NVARCHAR(1),
	IN p_tablaDescalce			TABLE(
									"FolioInterno"			BIGINT
								   ,"FolioCartera"			BIGINT
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
		/******************************** Elimino Operaciones Calzadas ********************************/
		/**********************************************************************************************/		
        SELECT COUNT(1) AS "Count"
        INTO v_Operaciones_Calzadas
        FROM :p_tablaDescalce;

        IF(:v_Operaciones_Calzadas > 0) THEN

        	DELETE FROM "BSCL.Custodia.BD::CUS.Reserva" Reserva
			WHERE Reserva."IdTipoProducto" = 'MEFW'
			  AND (Reserva."IdOperacion" , Reserva."FolioCarteraOrigen") IN (
																		SELECT DES."FolioInterno" AS "IdOperacion"
																		     , DES."FolioCartera" AS "FolioCarteraOrigen" 
																		FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES
																		INNER JOIN :p_tablaDescalce AS DES
																		        ON RES."IdOperacion" = DES."FolioInterno"
																		       AND RES."FolioCarteraOrigen" = DES."FolioCartera"
																		WHERE RES."IdTipoProducto" = 'MEFW'
																		union all
																		SELECT DES."FolioCartera" AS "IdOperacion"
																		     , DES."FolioInterno" AS "FolioCarteraOrigen"
																		FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES
																		INNER JOIN :p_tablaDescalce AS DES
																		        ON RES."IdOperacion" = DES."FolioCartera"
																		       AND RES."FolioCarteraOrigen" = DES."FolioInterno"
																		WHERE RES."IdTipoProducto" = 'MEFW'
																	);
        
        	/*
        	v_tablaReservaCartera = SELECT "FolioInterno"			AS "IdOperacion"	
    									 , 0 						AS "IdRegistro"		
    									 , 0						AS "IdReserva"			
    									 , 'INGRES'					AS "IdConcepto"
    									 , 0						AS "FolioOperacion"
    									 , CUS."IdOperacion"		AS "FolioCartera"
    									 , 'MEFW'					AS "IdTipoProducto"	
    									 , FWDCALCE."MontoCalce" 	AS "Cantidad" 			
    									 , 0 						AS "Tasa" 				
    									 , 0						AS "Precio"			
    									 , FWDCALCE."MontoCalce"	AS "MontoValorizado"	
    									 , 'DV'						AS "IdClasificacionTipoInstrumento"
    									 , FWDCALCE."IdInstrumento"	AS "IdInstrumento"
    									 , ''						AS "IdTipoReajuste"	
    									 , ''						AS "IdEmisor"			
    									 , "FechaVencimiento"	    AS "FechaVencimiento"
    									 , 'N'						AS "EsFungible"		
    									 , ''						AS "Glosa"				
        							FROM :p_tablaCalce AS FWDCALCE
        							INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera3" AS CUS  
									 		ON CUS."IdOperacion" = FWDCALCE."FolioCartera"
									 	   AND CUS."IdInstrumento" = FWDCALCE."IdInstrumento"
									 	   AND CUS."IdTipoProducto" = 'MEFW'
        							UNION ALL
									SELECT CUS."IdOperacion"	
    									 , 0 						AS "IdRegistro"		
    									 , 0						AS "IdReserva"			
    									 , 'INGRES'					AS "IdConcepto"		--NVARCHAR(6),	-- INGRES / RETRES / INGDET / RETDET / LIMDET
    									 , 0						AS "FolioOperacion"	
    									 , "FolioInterno"			AS "FolioCartera" 		
    									 , 'MEFW'					AS "IdTipoProducto"	
    									 , FWDCALCE."MontoCalce" 	AS "Cantidad" 			
    									 , 0 						AS "Tasa" 				
    									 , 0						AS "Precio"			
    									 , FWDCALCE."MontoCalce"	AS "MontoValorizado"	
    									 , 'DV'						AS "IdClasificacionTipoInstrumento"
    									 , CUS."IdInstrumento"		
    									 , ''						AS "IdTipoReajuste"	
    									 , ''						AS "IdEmisor"			
    									 , CUS."FechaLiquidacion"	AS "FechaVencimiento"	
    									 , 'N'						AS "EsFungible"		
    									 , ''						AS "Glosa"				
        							FROM :p_tablaCalce AS FWDCALCE
									INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera3" AS CUS  
									 		ON CUS."IdOperacion" = FWDCALCE."FolioCartera"
									 	   AND CUS."IdInstrumento" = FWDCALCE."IdInstrumento"
									 	   AND CUS."IdTipoProducto" = 'MEFW'; 
												
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
				, p_out_IdReserva    		=> p_out_IdReserva
				, p_out_ErroresContador		=> p_out_ErroresContador
				, p_out_ErroresDetalle		=> p_out_ErroresDetalleRes
			);
			*/
			
				
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
			
		--) Validar errores
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle; 
		IF (:p_out_ErroresContador > 0) THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
	END;
END; 
