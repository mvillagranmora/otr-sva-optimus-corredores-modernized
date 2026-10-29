SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacionMasivo";
GO
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacionMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN  p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  		--ID del Modulo que lo invoca	
	IN  p_simularOperacion				NVARCHAR(1) DEFAULT 'N',	-- S/N se usa para simular operacion (algunos casos)
	IN  p_UsaReglasNegocio	 			NVARCHAR(1) DEFAULT 'N',	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN p_tablaEventos 		TABLE(  "IdTipoEvento"               	NVARCHAR(6),              		 
								    "IdModuloSolicita"           	NVARCHAR(3),				
								    "IdUsuarioSolicita"          	BIGINT,              		
								    "Fecha"                      	NVARCHAR(10), 				   
								    "IdTipoOperacion"            	NVARCHAR(6),
								    "IdConcepto"						NVARCHAR(6),
								    "NumeroOperacionConcepto"    	NVARCHAR(40), 
								    "FechaOperacionConcepto"     	NVARCHAR(10),
								    "IdCliente"                  	BIGINT,
								    "IdFuncionario"              	BIGINT,
								    "IdProveedor"                	BIGINT,
								    "IdTipoProducto"					NVARCHAR(4),
									"IdClasificacionTipoInstrumento"	NVARCHAR(2),
									"IdTipoInstrumento"		    	NVARCHAR(3),
									"IdInstrumento"			    	BIGINT,
									"Nemo"							NVARCHAR(40),
								    "Cantidad"                   	BIGINT,
								    "Monto"                      	DECIMAL(26,8),
								    "Precio"                     	DECIMAL(26,8),
								    "Tasa"                       	DECIMAL(26,8),
								    "IdMoneda"                   	NVARCHAR(3),
								    "Glosa"                      	NVARCHAR(500)  ,
								    "IdUnion"                       BIGINT       
								    ),
								    
    IN p_tablaDetalle       TABLE(  "IdUnion"                       BIGINT      ,        --**CAMPOS NUEVOS     
								    "NombreCampo"           	    NVARCHAR(100),		
								    "TipoDato"           	        NVARCHAR(50),		
								    "Valor"          	            NVARCHAR(300),
								    "Clasificacion"                 NVARCHAR(50)),
								   
	-- Parametros salida del servicio publico
	OUT p_out_IdEvento table ("IdEvento" BIGINT,"NumeroOperacionConcepto" 	NVARCHAR(40)),
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(600)),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	

	DECLARE v_errorhashmsgtable TABLE ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg 		NVARCHAR(520);
	--DECLARE v_SecuenciaEvento	BIGINT;	  
	DECLARE v_out_Resultado 	NVARCHAR(1);
    DECLARE v_out_Mensaje 		NVARCHAR(520);
	
	DECLARE	v_HorasVigencia				DECIMAL(5,2);
	DECLARE	v_EtapasSecuenciales		NVARCHAR(1);	
	DECLARE	V_IdUsuarioSuperAutorizador BIGINT;
	DECLARE	V_IdRolSuperAutorizador    	BIGINT; 
	
	DECLARE v_CodigoError 	INT;
    DECLARE v_MensajeError 	NVARCHAR(1000);	
    DECLARE v_FechaValida	CHAR(1);

	DECLARE v_FechaHoraVigencia 	DATETIME;
	DECLARE v_FechaHoraResgistro	DATETIME;
	DECLARE v_FechaSistema			NVARCHAR(40);
	
	DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));

	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			p_out_ErroresDetalle = SELECT '' AS "Codigo", ::SQL_ERROR_MESSAGE AS "Descripcion" FROM dummy;
			--Error INTerno no manejado  (El log ser� captado por la capa de XS control)		
			--RESIGNAL; v_MensajeError
		END IF;		
	END;
		
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_Cantidad 				INT;
		DECLARE v_CantidadDetalle 		INT;
		DECLARE v_NombreTipoEvento 		NVARCHAR(100);
		DECLARE v_NombreTipoOperacion 	NVARCHAR(100);
		DECLARE v_NombreConcepto		NVARCHAR(100);
		
		-------------------------
		--Validaciones b�sicas 
		-------------------------
	
		--Validacion la existencia del Id del tipo de evento
		v_out_Resultado:='S'; v_CodigoError:=0; v_MensajeError:='';
		
		/*Incluir aqui validaciones */	
		v_Cantidad:= 0;
		
		SELECT COUNT(1) INTO v_Cantidad FROM :p_tablaEventos EVE LEFT JOIN "BSCL.Plataforma.BD.Vistas::AUT.ConsultarTipoEvento" TIP
		ON (EVE."IdTipoEvento"=TIP."IdTipoEvento")
		WHERE TIP."IdTipoEvento" IS NULL AND "IdEmpresa" = :p_IdEmpresa;
		
		IF v_Cantidad > 0 THEN
				v_MensajeError:='';
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('NO_EXISTE_TIPOEVENTO', :v_hash, :v_MensajeError);
				p_out_ErroresDetalle =  SELECT 'NO_EXISTE_TIPOEVENTO' AS "Codigo",:v_MensajeError AS "Descripcion"
										 FROM :p_tablaEventos EVE LEFT JOIN "BSCL.Plataforma.BD.Vistas::AUT.ConsultarTipoEvento" TIP
										ON (EVE."IdTipoEvento"=TIP."IdTipoEvento")
										WHERE TIP."IdTipoEvento" IS NULL AND "IdEmpresa" = :p_IdEmpresa UNION ALL SELECT * FROM :p_out_ErroresDetalle;
		END IF;
		v_Cantidad:= 0; 
		SELECT COUNT(1) INTO v_Cantidad FROM :p_tablaEventos EVE LEFT JOIN "BSCL.Plataforma.BD::AUT.Etapa" TIP
		ON (EVE."IdTipoEvento"=TIP."IdTipoEvento" AND TIP."IdEstado"= 'VIG')
		WHERE TIP."IdTipoEvento" IS NULL AND TIP."IdEmpresa" = :p_IdEmpresa;
		
		IF v_Cantidad > 0 THEN
				v_MensajeError:='';
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('NO_EXISTE_ETAPAS_PARA_TIPOEVENTO', :v_hash, :v_MensajeError);
				p_out_ErroresDetalle =  SELECT 'NO_EXISTE_ETAPAS_PARA_TIPOEVENTO' AS "Codigo",:v_MensajeError AS "Descripcion"
										FROM :p_tablaEventos EVE LEFT JOIN "BSCL.Plataforma.BD::AUT.Etapa" TIP
										ON (EVE."IdTipoEvento"=TIP."IdTipoEvento" AND TIP."IdEstado"= 'VIG' AND TIP."IdEmpresa" = :p_IdEmpresa)
										WHERE TIP."IdTipoEvento" IS NULL UNION ALL SELECT * FROM :p_out_ErroresDetalle;
		END IF;
		
		v_Cantidad:= 0;
		
		SELECT COUNT(1) INTO v_Cantidad FROM :p_tablaEventos EVE LEFT JOIN 
		(SELECT ETA."IdTipoEvento" FROM "BSCL.Plataforma.BD::AUT.Etapa" ETA
			INNER JOIN "BSCL.Plataforma.BD::AUT.AutorizadoEtapa" AUT ON (ETA."IdEtapa" = AUT."IdEtapa"
								 AND AUT."IdEstado" = 'VIG'
								 AND ETA."IdEstado" = 'VIG' )
		 WHERE ETA."IdEmpresa" = :p_IdEmpresa
		 ) AS  TIP
		ON (EVE."IdTipoEvento"=TIP."IdTipoEvento" )
		WHERE TIP."IdTipoEvento" IS NULL;
		
		IF v_Cantidad > 0 THEN
				v_MensajeError:='';
				v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('NO_EXISTEN_AUTORIZADOS_PARA_ETAPAS', :v_hash, :v_MensajeError);
				p_out_ErroresDetalle =  SELECT 'NO_EXISTEN_AUTORIZADOS_PARA_ETAPAS' AS "Codigo",:v_MensajeError AS "Descripcion"
										 FROM :p_tablaEventos EVE LEFT JOIN 
										(SELECT ETA."IdTipoEvento" FROM "BSCL.Plataforma.BD::AUT.Etapa" ETA
											INNER JOIN "BSCL.Plataforma.BD::AUT.AutorizadoEtapa" AUT ON (ETA."IdEtapa" = AUT."IdEtapa"
																 AND AUT."IdEstado" = 'VIG'
																 AND ETA."IdEstado" = 'VIG' )
										WHERE ETA."IdEmpresa" = :p_IdEmpresa						 
										 ) AS  TIP
										ON (EVE."IdTipoEvento"=TIP."IdTipoEvento" )
										WHERE TIP."IdTipoEvento" IS NULL UNION ALL SELECT * FROM :p_out_ErroresDetalle;
		END IF;
		
		--Valida si existen eventos del mismo tipo para las operaciones ingresadas
		v_Cantidad:= 0;
		
		SELECT COUNT(1) INTO v_Cantidad FROM :p_tablaEventos EVE 
		WHERE EXISTS (
			SELECT 1 FROM "BSCL.Plataforma.BD::AUT.Evento" EV 
			WHERE EV."IdTipoEvento" = EVE."IdTipoEvento"
			AND COALESCE(EV."IdTipoOperacion",'') = COALESCE(EVE."IdTipoOperacion",'')
			AND COALESCE(EV."IdConcepto",'') = COALESCE(EVE."IdConcepto",'')
			AND COALESCE(EV."NumeroOperacionConcepto",'') = COALESCE(EVE."NumeroOperacionConcepto",'')
			AND COALESCE(EV."FechaOperacionConcepto",'') = COALESCE(EVE."FechaOperacionConcepto",'')
			AND EV."IdEstado" = 'VIG'
		);
		
		IF v_Cantidad > 0 THEN
			SELECT "Nombre" INTO v_NombreConcepto 
			FROM "BSCL.Plataforma.BD::PLA.Concepto" 
			WHERE "IdConcepto" = (
				SELECT TOP 1 EVE."IdConcepto" 
				FROM :p_tablaEventos EVE
				WHERE EXISTS (
					SELECT 1 FROM "BSCL.Plataforma.BD::AUT.Evento" EV 
					WHERE EV."IdTipoEvento" = EVE."IdTipoEvento"
					AND COALESCE(EV."IdTipoOperacion",'') = COALESCE(EVE."IdTipoOperacion",'')
					AND COALESCE(EV."IdConcepto",'') = COALESCE(EVE."IdConcepto",'')
					AND COALESCE(EV."NumeroOperacionConcepto",'') = COALESCE(EVE."NumeroOperacionConcepto",'')
					AND COALESCE(EV."FechaOperacionConcepto",'') = COALESCE(EVE."FechaOperacionConcepto",'')
					AND EV."IdEstado" = 'VIG'
				)
			);

			v_MensajeError   := ' '; 
			v_hash = SELECT 'NombreTipoOperacion' AS "Codigo", ' ' AS "Valor" FROM dummy UNION ALL 
					SELECT 'NombreConcepto' AS "Codigo", COALESCE(v_NombreConcepto, ' ') AS "Valor" FROM dummy UNION ALL
					SELECT 'NumeroOperacionConcepto' AS "Codigo", ' ' AS "Valor" FROM dummy UNION ALL
					SELECT 'FechaOperacionConcepto' AS "Codigo", (
						SELECT TOP 1 EVE."FechaOperacionConcepto" 
						FROM :p_tablaEventos EVE
						WHERE EXISTS (
							SELECT 1 FROM "BSCL.Plataforma.BD::AUT.Evento" EV 
							WHERE EV."IdTipoEvento" = EVE."IdTipoEvento"
							AND COALESCE(EV."IdTipoOperacion",'') = COALESCE(EVE."IdTipoOperacion",'')
							AND COALESCE(EV."IdConcepto",'') = COALESCE(EVE."IdConcepto",'')
							AND COALESCE(EV."NumeroOperacionConcepto",'') = COALESCE(EVE."NumeroOperacionConcepto",'')
							AND COALESCE(EV."FechaOperacionConcepto",'') = COALESCE(EVE."FechaOperacionConcepto",'')
							AND EV."IdEstado" = 'VIG'
						)
					) AS "Valor" FROM dummy;

			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('OPERACION_CONCEPTO_YA_TIENE_EVENTO_INGRESADO', :v_hash, :v_MensajeError);
			p_out_ErroresDetalle =  SELECT 'EventoAutorizacion' AS "Codigo", :v_MensajeError AS "Descripcion" FROM dummy UNION ALL
									SELECT * FROM :p_out_ErroresDetalle;
		END IF;

		--Si existen errores en las validaciones debe salir
		---------------------------------------------------
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
			/* CONTINUA SOLO CON LOS EVENTOS SIN PROBLEMA */
			--p_tablaEventos=SELECT EV.* FROM :p_tablaEventos EV WHERE NOT EXISTS (SELECT 1 FROM  :p_out_ErroresDetalle DET WHERE DET."Codigo"= EV."IdTipoEvento");
		END IF;		
		
		
		
		------------------------
		--Validaciones via Motor (solo complejas)
		------------------------		
		--
	
		--------------------
		--Reglas Negocio 
		--------------------		
		--
					
		--------------------
		--Negocio Aqui
		--------------------
		BEGIN
		
					
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(:p_IdEmpresa, :v_FechaSistema);
			
			v_FechaSistema := SUBSTRING(v_FechaSistema,1,19);
			
		/*
			SELECT ADD_SECONDS(TO_SECONDDATE(v_FechaSistema,'YYYY-MM-DD HH24:MI:SS'), V_HorasVigencia * 3600) 
		    					   INTO v_FechaHoraVigencia FROM DUMMY;
			*/			
			v_FechaHoraResgistro := TO_SECONDDATE(:v_FechaSistema,'YYYY-MM-DD HH24:MI:SS');
			
			v_DetalleLog = Select '' as "SubRegistro", '' as "CampoRegistro", 
								 '' as "ValorAntiguo", '' as "ValorNuevo" from DUMMY WHERE 1=2;
								 
			
			
			v_TabEventos= SELECT "BSCL.Plataforma.BD.Secuencias::AUTSecuenciasGenerales".NEXTVAL AS "IdEvento"      
							       ,p_IdEmpresa AS "IdEmpresa"              
							       ,EVE."IdTipoEvento"            
							       ,EVE."IdModuloSolicita" AS "IdModulo"               
							       ,EVE."IdUsuarioSolicita" AS "IdUsuario"              
							       ,EVE."Fecha" 
							       ,EVE."IdUnion"
							       ,ADD_SECONDS(TO_SECONDDATE(v_FechaSistema,'YYYY-MM-DD HH24:MI:SS'), TP."HorasVigencia" * 3600) AS "FechaHoraVigencia"          
							       ,v_FechaHoraResgistro AS "FechaHoraRegistro"      
							       ,TP."IdUsuarioSuperAutorizador" 
								   ,TP."IdRolSuperAutorizador"
								   ,TP."EtapasSecuenciales"    
							       ,COALESCE(EVE."IdTipoOperacion",'') AS "IdTipoOperacion"
							       ,COALESCE(EVE."IdConcepto",'')	   AS "IdConcepto"       
							       ,COALESCE(EVE."NumeroOperacionConcepto",'')  AS "NumeroOperacionConcepto"      
							       ,COALESCE(EVE."FechaOperacionConcepto",'')	 AS "FechaOperacionConcepto"        
							       ,COALESCE(EVE."IdCliente",0)              AS "IdCliente"
							       ,COALESCE(EVE."IdFuncionario",0)   AS "IdFuncionario"           
							       ,COALESCE(EVE."IdProveedor",0)   AS "IdProveedor"         
							       ,COALESCE(EVE."IdTipoProducto",'')	AS "IdTipoProducto"				  
								   ,COALESCE(EVE."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento"			 
								   ,COALESCE(EVE."IdTipoInstrumento",'') AS "IdTipoInstrumento"			
								   ,COALESCE(EVE."IdInstrumento",0)		AS "IdInstrumento"	  
								   ,COALESCE(EVE."Nemo",'')		AS "Nemo"			  
							       ,COALESCE(EVE."Cantidad",0)  AS "Cantidad"                
							       ,COALESCE(EVE."Monto",0)       AS "Monto"             
							       ,COALESCE(EVE."Precio",0)  AS "Precio"                 
							       ,COALESCE(EVE."Tasa",0)      AS "Tasa"               
							       ,COALESCE(EVE."IdMoneda",'')  AS "IdMoneda"                
							       ,COALESCE(EVE."Glosa",'') AS "Glosa"
							       ,0 AS "IdUsuarioCambioEstado"
						   		   ,0 AS "IdRolCambioEstado"
					       		   ,'0001-01-01' AS "FechaHoraCambioEstado"                 
							       ,'VIG' AS "IdEstado"  
								FROM :p_tablaEventos EVE INNER JOIN  "BSCL.Plataforma.BD::AUT.TipoEvento" TP
								ON (EVE."IdTipoEvento"= TP."IdTipoEvento"
									AND TP."IdEmpresa"= p_IdEmpresa
									AND TP."IdEstado"	= 'VIG');
			
							
			SELECT "IdEvento"       
		       ,"IdEmpresa"              
		       ,"IdTipoEvento"            
		       ,"IdModulo"               
		       ,"IdUsuario"              
		       ,"Fecha"                                 
		       ,"FechaHoraVigencia"          
		       ,"FechaHoraRegistro"      
		       ,"IdUsuarioSuperAutorizador"
			   ,"IdRolSuperAutorizador"
			   ,"EtapasSecuenciales"    
		       ,"IdTipoOperacion"
		       ,"IdConcepto"	          
		       ,"NumeroOperacionConcepto"        
		       ,"FechaOperacionConcepto"         
		       ,"IdCliente"              
		       ,"IdFuncionario"          
		       ,"IdProveedor"            
		       ,"IdTipoProducto"				  
			   ,"IdClasificacionTipoInstrumento"		 
			   ,"IdTipoInstrumento"		
			   ,"IdInstrumento"			  
			   ,"Nemo"					  
		       ,"Cantidad"               
		       ,"Monto"                  
		       ,"Precio"                 
		       ,"Tasa"                   
		       ,"IdMoneda"                 
		       ,"Glosa"
		       ,"IdUsuarioCambioEstado"
	   		   ,"IdRolCambioEstado"
       		   ,"FechaHoraCambioEstado"                 
		       ,"IdEstado"   
			FROM :v_TabEventos
			INTO "BSCL.Plataforma.BD::AUT.Evento"
		    (
				"IdEvento"       
		       ,"IdEmpresa"              
		       ,"IdTipoEvento"            
		       ,"IdModulo"               
		       ,"IdUsuario"              
		       ,"Fecha"                                 
		       ,"FechaHoraVigencia"          
		       ,"FechaHoraRegistro"      
		       ,"IdUsuarioSuperAutorizador"
			   ,"IdRolSuperAutorizador"
			   ,"EtapasSecuenciales"    
		       ,"IdTipoOperacion"
		       ,"IdConcepto"	          
		       ,"NumeroOperacionConcepto"        
		       ,"FechaOperacionConcepto"         
		       ,"IdCliente"              
		       ,"IdFuncionario"          
		       ,"IdProveedor"            
		       ,"IdTipoProducto"				  
			   ,"IdClasificacionTipoInstrumento"		 
			   ,"IdTipoInstrumento"		
			   ,"IdInstrumento"			  
			   ,"Nemo"					  
		       ,"Cantidad"               
		       ,"Monto"                  
		       ,"Precio"                 
		       ,"Tasa"                   
		       ,"IdMoneda"                 
		       ,"Glosa"
		       ,"IdUsuarioCambioEstado"
	   		   ,"IdRolCambioEstado"
       		   ,"FechaHoraCambioEstado"                 
		       ,"IdEstado");
		       
		       
		    ------------------------------------------------------------------------------------------------------
		    -- AGREGAMOS EL DETALLE DE LAS AUTORIZACIONES, SOLO SI TIENE DATOS LA TABLA (p_tablaDetalle)  
		    -- AE 12 04 19
		    
		    v_CantidadDetalle := 0;
			SELECT COUNT(1) INTO v_CantidadDetalle FROM :p_tablaDetalle;
	
		    IF v_CantidadDetalle > 0 THEN
		    
				v_TabEventosDetalle =  SELECT  "BSCL.Plataforma.BD.Secuencias::AUT.IdEventoDetalle".NEXTVAL AS "IdEventoDetalle" , 
												EVE."IdEvento"   AS "IdEvento" ,
												EVEDET."NombreCampo",
												EVEDET."TipoDato",
												EVEDET."Valor",
												EVEDET."Clasificacion"
				                        FROM :v_TabEventos EVE INNER JOIN :p_tablaDetalle EVEDET
				                             ON (EVE."IdUnion" = EVEDET."IdUnion");
                
                SELECT "IdEventoDetalle"	
				  ,"IdEvento"	
				  ,"NombreCampo"
				  ,"TipoDato"
				  ,"Valor"
				  ,"Clasificacion"	
				FROM :v_TabEventosDetalle
				INTO "BSCL.Plataforma.BD::AUT.EventoDetalle"
			    (  "IdEventoDetalle"	
				  ,"IdEvento"	
				  ,"NombreCampo"
				  ,"TipoDato"
				  ,"Valor"
				  ,"Clasificacion"	                                
			    );
		       
             END IF;
			------------------------------------------------------------------------------------------------------
			
		 	p_out_IdEvento =   SELECT "IdEvento" as "IdEvento","NumeroOperacionConcepto" AS "NumeroOperacionConcepto"  FROM :v_TabEventos;
			    
			  v_DetalleLog = (Select "SubRegistro", "CampoRegistro", "ValorAntiguo", "ValorNuevo" from :v_DetalleLog
							UNION ALL
						Select 'Evento' as "SubRegistro", 'IdEvento' as "CampoRegistro", 
							'0' as "ValorAntiguo", TO_VARCHAR("IdEvento") as "ValorNuevo" from :v_TabEventos); 
			  			
			--Se crean las etapas del evento    
			INSERT INTO "BSCL.Plataforma.BD::AUT.EtapaEvento" (
				"IdEtapaEvento"         --: def.t_id not null; 
				,"IdEvento"             --: def.t_id not null;       //[FK]AUT.EtapaEvento;IdEvento;AUT.Evento;IdEvento// 
				,"IdEtapa"
				,"Codigo"		        --: def.t_codigo not null;
				,"Nombre"				--: def.t_nombre not null;
				,"Orden"
				,"TipoAutorizacion"     --: String(1) not null Default 'T'; //(T)Deben autorizar todos (U)Basta solo uno (M)Un minimo de  
				,"MinimoAutorizan"      --: Integer not null Default '0';   //Si TipoAutorizacion = 'M' este campo debe ser distinto de cero       
				,"IdEstado"             --: def.t_id_alpha3 not null;       //[FK]AUT.AutorizadoEtapaEvento;IdEstado;PLA.Estado;IdEstado//
				)
			SELECT   
				"BSCL.Plataforma.BD.Secuencias::AUTSecuenciasGenerales".NEXTVAL 
				,EV."IdEvento"
				,ET."IdEtapa"             
				,ET."Codigo"		        
				,ET."Nombre"
				,ET."Orden"				
				,ET."TipoAutorizacion"       
				,ET."MinimoAutorizan"             
				,'VIG'                  
       		FROM
       			"BSCL.Plataforma.BD::AUT.Etapa" ET  INNER JOIN  :v_TabEventos EV 
       			ON (ET."IdTipoEvento" =EV."IdTipoEvento" )
   			WHERE 	ET."IdEstado"= 'VIG' AND ET."IdEmpresa" = :p_IdEmpresa;
				
				
				
			v_DetalleLog = (Select "SubRegistro", "CampoRegistro", "ValorAntiguo", "ValorNuevo" from :v_DetalleLog
							UNION ALL
							Select 'Etapa' as "SubRegistro", 'IdEtapa' as "CampoRegistro", 
							' ' as "ValorAntiguo", TO_VARCHAR(ET."IdEtapa") as "ValorNuevo" FROM
				       			"BSCL.Plataforma.BD::AUT.Etapa" ET INNER JOIN   :v_TabEventos EV 
				       			ON (ET."IdTipoEvento"=EV."IdTipoEvento")    
				   			WHERE 
							--	ET."IdTipoEvento" 	= p_IdTipoEvento AND
								ET."IdEstado"		= 'VIG' AND ET."IdEmpresa" = :p_IdEmpresa);
				
			--Se crean los autorizados de las etapas	
			INSERT INTO "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento" (
			    "IdAutorizadoEtapaEvento"      --: def.t_id not null; 
		       ,"IdEtapaEvento"                --: def.t_id not null;                          //[FK]AUT.AutorizadoEtapaEvento;IdEtapaEvento;AUT.EtapaEvento;IdEtapaEvento//
		       ,"IdUsuario"                    --: def.t_id not null Default '0';              //Usuario autorizador de la etapa
		       ,"IdRol"                        --: def.t_id not null Default '0';              //Rol autorizador de la etapa
		       ,"IdUsuarioReemplaza"           --: def.t_id not null Default '0';              //Usuario autorizador de la etapa en reemplazo de
		       ,"ReemplazaDesde"               --: def.t_fecha not null Default '0001-01-01';
		       ,"ReemplazaHasta"               --: def.t_fecha not null Default '0001-01-01';
		       ,"Observaciones"                --: def.t_glosa not null Default '';
		       ,"FechaHoraCambioEstado"        --: def.t_fecha not null Default '0001-01-01';
		       ,"IdEstado"   		
				)
			SELECT 
				"BSCL.Plataforma.BD.Secuencias::AUTSecuenciasGenerales".NEXTVAL
		   		,"EtapaEvento"."IdEtapaEvento"   
				,"AutorizadoEtapa"."IdUsuario"	
				,"AutorizadoEtapa"."IdRol"
				,"AutorizadoEtapa"."IdUsuarioReemplaza"
				,"AutorizadoEtapa"."ReemplazaDesde"
				,"AutorizadoEtapa"."ReemplazaHasta"
				,' '
				,'0001-01-01 00:00'
				,'VIG'
			FROM  "BSCL.Plataforma.BD::AUT.EtapaEvento" AS "EtapaEvento"
				INNER JOIN 	:v_TabEventos  AS "TabEventos"
				ON ("EtapaEvento"."IdEvento"="TabEventos"."IdEvento")
				INNER JOIN 	"BSCL.Plataforma.BD::AUT.AutorizadoEtapa" AS "AutorizadoEtapa" 
				ON	("AutorizadoEtapa"."IdEtapa" = "EtapaEvento"."IdEtapa" )
			WHERE "AutorizadoEtapa"."IdEstado" = 'VIG'; 
				---AND				"EtapaEvento"."IdEvento"	= v_SecuenciaEvento);
			
			v_DetalleLog = (Select "SubRegistro", "CampoRegistro", "ValorAntiguo", "ValorNuevo" from :v_DetalleLog
							UNION ALL
							Select 'Autorizador' as "SubRegistro", 'IdUsuario' as "CampoRegistro", 
							' ' as "ValorAntiguo", TO_VARCHAR("AutorizadoEtapa"."IdUsuario") as "ValorNuevo" 
							FROM  "BSCL.Plataforma.BD::AUT.EtapaEvento" AS "EtapaEvento"
							INNER JOIN 	:v_TabEventos  AS "TabEventos"
							ON ("EtapaEvento"."IdEvento"="TabEventos"."IdEvento")
							INNER JOIN 	"BSCL.Plataforma.BD::AUT.AutorizadoEtapa" AS "AutorizadoEtapa" 
							ON	"AutorizadoEtapa"."IdEtapa" = "EtapaEvento"."IdEtapa"); 
							--AND"EtapaEvento"."IdEvento"	= v_SecuenciaEvento);		
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				p_IdTipoLog => 'INGRESO' 
				,p_IdModulo => 'AUT' 
				,p_IdEmpresa => 1--p_IdEmpresa
				,p_IdUsuario => 3--p_IdUsuario 
				,p_FechaSistemaLog => '2016-02-16'--:v_FechaSistema
				,p_IdUsuarioAtendido => 1--p_IdUsuario
				,p_IdTipoRegistro => 'AUTORIZACION'
				,p_IdRegistro => 123--v_SecuenciaEvento
				,p_DetalleLog => :v_DetalleLog
				,p_NombreProceso => 'Creacion Evento Autorizacion'
			);
		
		END;
			
	
	 	
				   
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
