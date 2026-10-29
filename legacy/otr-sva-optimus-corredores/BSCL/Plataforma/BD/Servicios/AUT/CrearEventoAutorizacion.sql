SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion";
GO
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN  p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  		--ID del Modulo que lo invoca	
	IN  p_simularOperacion				NVARCHAR(1) DEFAULT 'N',	-- S/N se usa para simular operacion (algunos casos)
	IN  p_UsaReglasNegocio	 			NVARCHAR(1) DEFAULT 'N',	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
    IN p_IdTipoEvento               	NVARCHAR(6),              		 
    IN p_IdModuloSolicita           	NVARCHAR(3),				
    IN p_IdUsuarioSolicita          	BIGINT,              		
    IN p_Fecha                      	NVARCHAR(10), 				   
    IN p_IdTipoOperacion            	NVARCHAR(6) DEFAULT '', 
    IN p_IdConcepto						NVARCHAR(6) DEFAULT '', 
    IN p_NumeroOperacionConcepto    	NVARCHAR(40), 
    IN p_FechaOperacionConcepto     	NVARCHAR(10),
    IN p_IdCliente                  	BIGINT DEFAULT '0',  
    IN p_IdFuncionario              	BIGINT DEFAULT '0',  
    IN p_IdProveedor                	BIGINT DEFAULT '0', 
    IN p_IdTipoProducto					NVARCHAR(4) DEFAULT '',
	IN p_IdClasificacionTipoInstrumento	NVARCHAR(2) DEFAULT '',
	IN p_IdTipoInstrumento		    	NVARCHAR(3) DEFAULT '',
	IN p_IdInstrumento			    	BIGINT DEFAULT '0',
	IN p_Nemo							NVARCHAR(40) DEFAULT '',  
    IN p_Cantidad                   	BIGINT DEFAULT '0',
    IN p_Monto                      	DECIMAL(26,8) DEFAULT '0',
    IN p_Precio                     	DECIMAL(26,8) DEFAULT '0',
    IN p_Tasa                       	DECIMAL(26,8) DEFAULT '0',
    IN p_IdMoneda                   	NVARCHAR(3)   DEFAULT '',
    IN p_Glosa                      	NVARCHAR(260) DEFAULT '',
    IN p_IdCanal						NVARCHAR(3)   DEFAULT 'OPT',
    
		
	-- Parametros salida del servicio publico
	OUT p_out_IdEvento				BIGINT,	
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
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
	DECLARE v_SecuenciaEvento	BIGINT;	  
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
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 			 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			
			--Error INTerno no manejado  (El log sera captado por la capa de XS control)		
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
		DECLARE v_NombreTipoEvento 		NVARCHAR(100);
		DECLARE v_NombreTipoOperacion 	NVARCHAR(100);
		DECLARE v_NombreConcepto		NVARCHAR(100);
		DECLARE v_Count				 	INT:=0;
		
		-------------------------
		--Validaciones basicas 
		-------------------------
	
		--Validacion la existencia del Id del tipo de evento
		v_out_Resultado:='S'; v_CodigoError:=0; v_MensajeError:='';
		
			    	
		SELECT COUNT("IdTipoEvento") into v_Count FROM "BSCL.Plataforma.BD.Vistas::AUT.ConsultarTipoEvento" WHERE 
		"IdTipoEvento" = p_IdTipoEvento AND "IdEmpresa" = :p_IdEmpresa;

		IF v_Count=0 THEN
			p_out_ErroresDetalle =  SELECT 'TipoEvento' AS "Codigo",'NO_EXISTE_TIPOEVENTO' AS "Descripcion" FROM dummy;
		ELSE
			SELECT "Nombre" INTO v_NombreTipoEvento FROM "BSCL.Plataforma.BD::AUT.TipoEvento" WHERE "IdTipoEvento" 	= p_IdTipoEvento AND "IdEmpresa" = :p_IdEmpresa;
			v_Cantidad:= 0;
			--Valida si existen etapas configuradas para el tipo de evento
			SELECT COUNT(1) INTO v_Cantidad FROM "BSCL.Plataforma.BD::AUT.Etapa"
			WHERE 	"IdTipoEvento" 		= p_IdTipoEvento AND
					"IdEstado"			= 'VIG' AND "IdEmpresa" = :p_IdEmpresa;
					
			IF v_Cantidad = 0 THEN
				v_out_Resultado:='N';					
				v_MensajeError:='';
				v_hashmsg = SELECT 'NombreTipoEvento' AS "Codigo", v_NombreTipoEvento AS "Valor" FROM dummy;	
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('NO_EXISTE_ETAPAS_PARA_TIPOEVENTO', :v_hashmsg, :v_MensajeError);
				p_out_ErroresDetalle =  SELECT 'Etapas' AS "Codigo",:v_MensajeError AS "Descripcion" FROM dummy;
			ELSE
				v_Cantidad:= 0;		
				--Valida si existen autorizados para las etapas configuradas
				SELECT COUNT(1) INTO v_Cantidad 
				FROM "BSCL.Plataforma.BD::AUT.AutorizadoEtapa" AS AutorizadoEtapa
				INNER JOIN  "BSCL.Plataforma.BD::AUT.Etapa" AS Etapa ON
							Etapa."IdEtapa" = AutorizadoEtapa."IdEtapa" AND
							AutorizadoEtapa."IdEstado" = 'VIG' AND
							Etapa."IdEstado" = 'VIG' AND Etapa."IdEmpresa" = :p_IdEmpresa
						INNER JOIN "BSCL.Plataforma.BD::AUT.TipoEvento" AS TipoEvento ON
								TipoEvento."IdTipoEvento" = Etapa."IdTipoEvento" AND
								TipoEvento."IdTipoEvento" = p_IdTipoEvento AND
								TipoEvento."IdEstado" = 'VIG' AND TipoEvento."IdEmpresa" = :p_IdEmpresa;				
				IF v_Cantidad = 0 THEN
					v_out_Resultado:= 'N';					
					v_MensajeError:='';
					v_hashmsg = SELECT 'NombreTipoEvento' AS "Codigo", v_NombreTipoEvento AS "Valor" FROM dummy;	
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('NO_EXISTEN_AUTORIZADOS_PARA_ETAPAS', :v_hashmsg, :v_MensajeError);
					p_out_ErroresDetalle =  SELECT 'Autorizados' AS "Codigo",:v_MensajeError AS "Descripcion" FROM dummy;
				ELSE			
					IF TRIM(p_IdTipoOperacion) = '' AND TRIM(p_IdConcepto) = '' THEN
						v_out_Resultado := 'N';
						v_MensajeError  := ' '; 
						v_hashmsg = SELECT 'TipoOperacionConcepto' AS "Codigo", ' ' AS "Valor" FROM dummy;	 	
						CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('TIPO_OPERACION_COCEPTO_OBLIGATORIOS', :v_hashmsg, :v_MensajeError);
						p_out_ErroresDetalle =  SELECT 'TipoOperacionConcepto' AS "Codigo",:v_MensajeError AS "Descripcion" FROM dummy UNION ALL
												SELECT * FROM :p_out_ErroresDetalle;												 
					ELSE
						IF TRIM(p_IdTipoOperacion)<> '' THEN
							--Validacion la existencia del Id del tipo de operacion
							--v_out_Resultado:='S'; v_CodigoError:=0; v_MensajeError:='';								    	
							v_Count := 0;
							SELECT COUNT("IdTipoOperacion") INTO v_Count FROM "BSCL.Plataforma.BD::INS.TipoOperacion" WHERE "IdTipoOperacion" = p_IdTipoOperacion;			
							IF  v_Count = 0 THEN
								p_out_ErroresDetalle =  SELECT 'TipoOperacion' AS "Codigo",:v_MensajeError AS "Descripcion" FROM dummy;
							END IF;
						ELSE
						--Validacion la existencia del Id del concepto
							--v_out_Resultado:='S'; v_CodigoError:=0; v_MensajeError:='';								    		
							v_Count := 0;
							SELECT COUNT("IdConcepto") INTO v_Count FROM "BSCL.Plataforma.BD::PLA.Concepto" where  	"IdConcepto" = p_IdConcepto;
							IF  v_Count = 0 THEN
								p_out_ErroresDetalle =  SELECT 'Concepto' AS "Codigo",:v_MensajeError AS "Descripcion" FROM dummy;
							END IF;
						END IF;
					END IF;					
					IF (p_NumeroOperacionConcepto = '0' OR p_NumeroOperacionConcepto = '')  THEN
						v_out_Resultado := 'N';
						v_MensajeError  := ' '; 
						v_hashmsg = SELECT 'NumeroOperacion' AS "Codigo", ' ' AS "Valor" FROM dummy;	 	
						CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('NUMERO_OPERACION_NO_PUEDE_SER_CERO', :v_hashmsg, :v_MensajeError);
						p_out_ErroresDetalle =  SELECT 'NumeroOperacion' AS "Codigo",:v_MensajeError AS "Descripcion" FROM dummy UNION ALL
												SELECT * FROM :p_out_ErroresDetalle;
					END IF;
					
					IF "BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaOperacionConcepto)  = 'N' THEN
						v_out_Resultado := 'N';
						v_MensajeError  := '';
						v_hashmsg = SELECT 'FechaOperacion' AS "Codigo", p_FechaOperacionConcepto AS "Valor" FROM dummy;    	
						CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('FECHA_OPERACION_NO_VALIDA', :v_hashmsg, :v_MensajeError);
						p_out_ErroresDetalle =  SELECT 'FechaOperacion' AS "Codigo",:v_MensajeError AS "Descripcion" FROM dummy UNION ALL
												SELECT * FROM :p_out_ErroresDetalle;
					END IF;
					
					--Valida si existe un evento del mismo tipo para la operacion ingresada
					IF v_out_Resultado = 'S' THEN
						SELECT COUNT(1) INTO v_Cantidad FROM "BSCL.Plataforma.BD::AUT.Evento"
						WHERE 	"IdTipoEvento"				= p_IdTipoEvento			AND 
								"IdTipoOperacion" 			= p_IdTipoOperacion 		AND
								"IdConcepto"				= p_IdConcepto				AND
								"NumeroOperacionConcepto"	= p_NumeroOperacionConcepto AND
								"FechaOperacionConcepto"	= p_FechaOperacionConcepto	AND
								"IdEstado"			= 'VIG';
						IF v_Cantidad > 0 THEN
							SELECT "Nombre" INTO v_NombreConcepto FROM "BSCL.Plataforma.BD::PLA.Concepto" where "IdConcepto" = p_IdConcepto;
							v_out_Resultado  := 'N';
							v_MensajeError   := ' '; 
							v_hashmsg = SELECT 'NombreTipoOperacion' AS "Codigo", ' ' AS "Valor" FROM dummy UNION ALL 
					                	SELECT 'NombreConcepto' AS "Codigo", v_NombreConcepto AS "Valor" FROM dummy UNION ALL
					                	SELECT 'NumeroOperacionConcepto' AS "Codigo", ' ' AS "Valor" FROM dummy UNION ALL
					                	SELECT 'FechaOperacionConcepto' AS "Codigo", p_FechaOperacionConcepto AS "Valor" FROM dummy;	 	
	
							CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('OPERACION_CONCEPTO_YA_TIENE_EVENTO_INGRESADO', :v_hashmsg, :v_MensajeError);
							p_out_ErroresDetalle =  SELECT 'EventoAutorizacion' AS "Codigo", :v_MensajeError AS "Descripcion" FROM dummy UNION ALL
													SELECT * FROM :p_out_ErroresDetalle;
							SIGNAL SALIDA_CONTROLADA;
						END IF;
					END IF;		
				END IF;
			END IF;
		END IF;		

		--Si existe tipo evento, etapas y autorizados, continua validaciones
		IF v_out_Resultado = 'S' THEN
			--Valida existencia del modulo origen de la solcitud
			--v_out_Resultado:='N'; v_CodigoError:=0; v_MensajeError:='';
			
			v_Count := 0;												
			SELECT COUNT("IdModulo") into v_Count FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarModulo" WHERE 
			"IdModulo" = p_IdModuloSolicita;
																
			IF v_Count = 0 THEN
				p_out_ErroresDetalle =  SELECT 'Modulo' AS "Codigo",'MODULO_ORIGEN_NO_EXISTE' AS "Descripcion" FROM dummy UNION ALL
										SELECT * FROM :p_out_ErroresDetalle; 
			END IF;		
	
			--Valida existencia del usuario que solicita la autorizacion
			--v_out_Resultado:='N'; v_CodigoError:=0; v_MensajeError:='';
				    	
			v_Count := 0;	
			SELECT COUNT("IdUsuario") into v_Count FROM "BSCL.Plataforma.BD::ACC.Usuario" WHERE "IdUsuario" = p_IdUsuarioSolicita;
			
			IF v_Count = 0 THEN
				p_out_ErroresDetalle =  SELECT 'Usuario' AS "Codigo",'' AS "Descripcion" FROM dummy UNION ALL
										SELECT * FROM :p_out_ErroresDetalle; 
			END IF;		
	
			v_FechaValida := 'S';
			IF "BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_Fecha)  = 'N' THEN
				v_FechaValida := 'N'; 
			END IF;
	 
			IF 	v_FechaValida = 'N' THEN	
				v_MensajeError:='';
				v_hashmsg = SELECT 'Fecha' AS "Codigo", p_Fecha AS "Valor" FROM dummy;    	
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('FECHA_NO_VALIDA', :v_hashmsg, :v_MensajeError);
				p_out_ErroresDetalle =  SELECT 'Fecha' AS "Codigo",:v_MensajeError AS "Descripcion" FROM dummy UNION ALL
										SELECT * FROM :p_out_ErroresDetalle;
			END IF;
		
		END IF;

		--Si existen errores en las validaciones debe salir
		---------------------------------------------------
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
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
		
			--Se obtienen los parametros del tipo de evento
			SELECT  
				"HorasVigencia",
				"EtapasSecuenciales",
				"IdUsuarioSuperAutorizador",
				"IdRolSuperAutorizador"		
			INTO
				v_HorasVigencia, 				--DECIMAOL(5,2)
				v_EtapasSecuenciales,			--NVARCHAR(1) 	
				V_IdUsuarioSuperAutorizador,  	--BIGINT;
				V_IdRolSuperAutorizador    		--BIGINT; 	
			FROM "BSCL.Plataforma.BD::AUT.TipoEvento"
			WHERE 	"IdTipoEvento" 	= p_IdTipoEvento 	AND
					"IdEmpresa"	 	= p_IdEmpresa		AND
					"IdEstado"	 	= 'VIG';	

	      	--Se obtiene numero de secuencia que usaremos para el evento
			SELECT "BSCL.Plataforma.BD.Secuencias::AUTSecuenciasGenerales".NEXTVAL INTO v_SecuenciaEvento FROM DUMMY;
					
			--CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"( p_IdEmpresa, :v_FechaSistema ); 
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa, :v_FechaSistema);
			
			v_FechaSistema := SUBSTRING(v_FechaSistema,1,19);
			
			--SELECT ADD_SECONDS(TO_DATE(v_FechaSistema,'YYYY-MM-DD'), (HOUR(CURRENT_TIME)*3600) 
			--				   + (MINUTE(CURRENT_TIME)*60) + SECOND(CURRENT_TIME) + V_HorasVigencia * 3600) 
			--				   INTO v_FechaHoraVigencia FROM DUMMY;
			SELECT ADD_SECONDS(TO_SECONDDATE(v_FechaSistema,'YYYY-MM-DD HH24:MI:SS'), V_HorasVigencia * 3600) 
		    					   INTO v_FechaHoraVigencia FROM DUMMY;
						
			--v_FechaHoraResgistro := CURRENT_UTCTIMESTAMP; --"BSCL.Plataforma.BD.Utilitarios::ObtenerFechaLocal"(CURRENT_UTCTIMESTAMP, 'CL'); --p_FechaHoraResgistro
			v_FechaHoraResgistro := TO_SECONDDATE(:v_FechaSistema,'YYYY-MM-DD HH24:MI:SS');
			
			v_DetalleLog = Select '' as "SubRegistro", '' as "CampoRegistro", 
								 '' as "ValorAntiguo", '' as "ValorNuevo" from DUMMY WHERE 1=2;
								 
			

			INSERT INTO "BSCL.Plataforma.BD::AUT.Evento"
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
		       ,"IdEstado"
		       ,"IdCanal"
					)
		    VALUES (		
				 v_SecuenciaEvento
				,p_IdEmpresa       
				,p_IdTipoEvento               --BIGINT,              		 
				,p_IdModuloSolicita           --NVARCHAR(3),				
				,p_IdUsuarioSolicita          --BIGINT,              		
				,p_Fecha                      --
				,v_FechaHoraVigencia 		  --p_FechaHoraVigencia
				,v_FechaHoraResgistro 		  --p_FechaHoraResgistro
				,V_IdUsuarioSuperAutorizador  --BIGINT DEFAULT '0',  
				,V_IdRolSuperAutorizador      --BIGINT DEFAULT '0',
				,v_EtapasSecuenciales		  --NVARCHAR(1) DEFAULT 'S',      
				,p_IdTipoOperacion            --NVARCHAR(6) DEFAULT ' ',
				,p_IdConcepto				  --NVARCHAR(6) DEFAULT ' ',
				,p_NumeroOperacionConcepto    --BIGINT DEFAULT '0', 
				,p_FechaOperacionConcepto     --NVARCHAR(10) DEFAULT '0001-01-01',
				,p_IdCliente                  --BIGINT DEFAULT '0',  
				,p_IdFuncionario              --BIGINT DEFAULT '0',  
				,p_IdProveedor                --BIGINT DEFAULT '0', 
				,p_IdTipoProducto			  --NVARCHAR(4) DEFAULT ' ',
				,p_IdClasificacionTipoInstrumento  --NVARCHAR(2) DEFAULT ' ',
				,p_IdTipoInstrumento		  --NVARCHAR(3) DEFAULT ' ',
				,p_IdInstrumento			  --BIGINT DEFAULT '0',
				,p_Nemo						  --NVARCHAR(40) DEFAULT ' ',
				,p_Cantidad                   --BIGINT DEFAULT '0',
				,p_Monto                      --DECIMAL(26,8) DEFAULT '0',
				,p_Precio                     --DECIMAL(26,8) DEFAULT '0',
				,p_Tasa                       --DECIMAL(26,8) DEFAULT '0',
				,p_IdMoneda                   --NVARCHAR(3) DEFAULT ' ',
				,p_Glosa                      --NVARCHAR(260) DEFAULT ' ',
			    ,0							  --IdUsuarioCambioEstado	def.t_id not null Default '0'
	   		    ,0 							  --IdRolambioEstado	  	def.t_id not null Default '0'
       		    ,'0001-01-01'                 --def.t_fechaHora not null Default '0001-01-01'; 
				,'VIG'   					  --p_IdEstado --NVARCHAR(2),
				,p_IdCanal 
			    );
			    
			  v_DetalleLog = (Select "SubRegistro", "CampoRegistro", "ValorAntiguo", "ValorNuevo" from :v_DetalleLog
							UNION ALL
						Select 'Evento' as "SubRegistro", 'IdEvento' as "CampoRegistro", 
							'0' as "ValorAntiguo", TO_VARCHAR(:v_SecuenciaEvento) as "ValorNuevo" from DUMMY); 
			  			
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
				,v_SecuenciaEvento
				,"IdEtapa"             
				,"Codigo"		        
				,"Nombre"
				,"Orden"				
				,"TipoAutorizacion"       
				,"MinimoAutorizan"             
				,'VIG'                  
       		FROM
       			"BSCL.Plataforma.BD::AUT.Etapa"        
   			WHERE 
				"IdTipoEvento" 	= p_IdTipoEvento AND
				"IdEstado"		= 'VIG' AND "IdEmpresa" = :p_IdEmpresa;
				
			v_DetalleLog = (Select "SubRegistro", "CampoRegistro", "ValorAntiguo", "ValorNuevo" from :v_DetalleLog
							UNION ALL
							Select 'Etapa' as "SubRegistro", 'IdEtapa' as "CampoRegistro", 
							' ' as "ValorAntiguo", TO_VARCHAR("IdEtapa") as "ValorNuevo" FROM
				       			"BSCL.Plataforma.BD::AUT.Etapa"        
				   			WHERE 
								"IdTipoEvento" 	= p_IdTipoEvento AND
								"IdEstado"		= 'VIG' AND "IdEmpresa" = :p_IdEmpresa);
				
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
				INNER JOIN 	"BSCL.Plataforma.BD::AUT.AutorizadoEtapa" AS "AutorizadoEtapa" 
				ON	"AutorizadoEtapa"."IdEtapa" = "EtapaEvento"."IdEtapa" 
				AND "EtapaEvento"."IdEvento"	= v_SecuenciaEvento
				AND	"AutorizadoEtapa"."IdEstado" = 'VIG';
					
					
			
			v_DetalleLog = (Select "SubRegistro", "CampoRegistro", "ValorAntiguo", "ValorNuevo" from :v_DetalleLog
							UNION ALL
							Select 'Autorizador' as "SubRegistro", 'IdUsuario' as "CampoRegistro", 
							' ' as "ValorAntiguo", TO_VARCHAR("IdUsuario") as "ValorNuevo" 
							FROM  "BSCL.Plataforma.BD::AUT.EtapaEvento" AS "EtapaEvento"
							INNER JOIN 	"BSCL.Plataforma.BD::AUT.AutorizadoEtapa" AS "AutorizadoEtapa" 
							ON	"AutorizadoEtapa"."IdEtapa" = "EtapaEvento"."IdEtapa" AND
							"EtapaEvento"."IdEvento"	= v_SecuenciaEvento);		
						
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				p_IdTipoLog => 'INGRESO' 
				,p_IdModulo => 'AUT' 
				,p_IdEmpresa => p_IdEmpresa
				,p_IdUsuario => 3--p_IdUsuario 
				,p_FechaSistemaLog => '2016-02-16'--:v_FechaSistema
				,p_IdUsuarioAtendido => 1--p_IdUsuario
				,p_IdTipoRegistro => 'AUTORIZACION'
				,p_IdRegistro => 123--v_SecuenciaEvento
				,p_DetalleLog => :v_DetalleLog
				,p_NombreProceso => 'Creación Evento Autorización'
				);
		
		END;
		
		BEGIN 
			-- NOTIFICACIONES A AUTORIZADORES
			DECLARE v_out_Agregar_Evento TABLE("IdEvento" BIGINT,"IdSecuencia" BIGINT);
			
			--SE CREAN NOTIFACIONES SEGUN ETAPA	SE GUARDAN EN EVT.Evento	
			v_Eventos = 
			SELECT 
			'NOTIFICAR_EVENTO_AUTORIZACION' AS "CodigoTipoEvento", 
			CEE."IdEtapaEvento"  AS "IdSecuencia",  
			CURRENT_UTCTIMESTAMP AS "FechaCreacionSistema", 
			E."IdPersona" AS "IdPersonaRemitente", 
			E."IdPersona" AS "IdPersonaDestinatario", 
			0 as "IdPortafolio" 
			FROM
			"BSCL.Plataforma.BD.Vistas::AUT.ConsultarEvento" CE
			INNER JOIN "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapaEvento" CEE ON CEE."IdEtapaEvento" = CE."IdEtapaEvento" 
			INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" U ON U."IdUsuario" =:p_IdUsuario
			INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" E ON E."IdEmpleado" = U."IdEntidad" 
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG ON CCDG."IdEmpresa" = :p_IdEmpresa
			WHERE CE."IdEvento" IN (v_SecuenciaEvento);
			
			--SE GUARDAN EN EVT.EventoDirecciones
			v_IdDirecciones = SELECT 
			DISTINCT CASE WHEN COALESCE("IdDireccionElectronica",-1)>0 THEN "IdDireccionElectronica" ELSE CEMP."IdDireccionElectronicaPrincipal" END AS "IdDireccion",
			CEE."IdEtapaEvento" AS "IdSecuencia", 			
			"IdPersona" AS "IdPersona",
			'TO' AS "TipoDestinatario"
			FROM "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapa" ce
			INNER JOIN "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizadorEtapa" ea
			ON ce."IdEtapa" = ea."IdEtapa" 
			INNER JOIN "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEtapaEvento" CEE ON CEE."IdEtapa" = CE."IdEtapa" 
			INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" u ON ea."IdUsuario" = u."IdUsuario" 
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP
			ON CEMP."IdEmpleado" = u."IdEntidad" AND (COALESCE("IdDireccionElectronica",-1)>0 OR COALESCE("IdDireccionElectronicaPrincipal",-1)>0 )
			WHERE ce."IdTipoEvento" IN (:p_IdTipoEvento) AND cee."IdEvento" IN (v_SecuenciaEvento)
			AND ce."IdEstado" ='VIG' 
			AND ea."IdEstado" ='VIG' 
			AND u."IdEstado"  = 'ACT';

			
			v_ArchivosAdjuntos =
			SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;			
			
			--CAMPOS QUE SE GUARDAN EN TABLA EVT.eventoDetalle
			v_EventoDetalle = 
				SELECT
				"IdSecuencia" as "IdSecuencia",
				'AUTORIZACION' AS "Llave",  
				CA."Autorizacion" AS "Valor"
				FROM :v_Eventos v_ev
				JOIN "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEvento" CA ON  CA."IdEvento" = v_SecuenciaEvento AND v_ev."IdSecuencia"=CA."IdEtapaEvento"
				UNION
				SELECT "IdSecuencia" as "IdSecuencia", 
				'GLOSA' AS "Llave",  
				SUBSTRING(:p_Glosa,1,300) AS "Valor" 
				from :v_Eventos
				UNION
				SELECT "IdSecuencia", 
				'FECHAHORA' AS "Llave",  
				TO_NVARCHAR(ADD_SECONDS(v_FechaHoraResgistro,CONF."Valor"),'DD-MM-YYYY')||' a las '||TO_NVARCHAR(ADD_SECONDS(v_FechaHoraResgistro,CONF."Valor"),
				'HH24:MI:SS')||' hrs.' AS "Valor" 
				from :v_Eventos
				JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF ON  "CodigoParametro" LIKE '%PLA.SegundosZonaHoraria%'
				UNION
				SELECT "IdSecuencia", 
				'FECHA_VIGENCIA' AS "Llave",  
				TO_NVARCHAR(ADD_SECONDS(v_FechaHoraVigencia,CONF."Valor"),'DD-MM-YYYY HH24:MI:SS') AS "Valor" 			
				from :v_Eventos
				JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF ON  "CodigoParametro" LIKE '%PLA.SegundosZonaHoraria%'
				UNION
				SELECT "IdSecuencia" as "IdSecuencia",
				'USUARIO' AS "Llave",  
				U."CodigoUsuario"  AS "Valor" 
				FROM :v_Eventos v_ev
	            INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP ON  cemp."IdPersona"=v_ev."IdPersonaDestinatario"
	            INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" U ON  cemp."IdEmpleado"= U."IdEntidad" AND U."TipoEntidad" ='EMP' 
	            WHERE u."IdUsuario" = :p_IdUsuario
	            UNION
				SELECT "IdSecuencia", 
				'CORREDORA' AS "Llave",
	            CCDG."RazonSocial" as "Valor"
				FROM :v_Eventos
	            LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG 
	            ON CCDG."IdEmpresa" = :p_IdEmpresa
	            UNION
	            SELECT "IdSecuencia", 
				'ATENCIONCLIENTE' AS "Llave",
	            CCDG."Telefono"  as "Valor"
				FROM :v_Eventos
	            LEFT JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" CCDG 
	            ON CCDG."IdEmpresa" = :p_IdEmpresa
				UNION
				SELECT "IdSecuencia",
				'LOGO_PUBLICO' AS "Llave",
				"Valor" AS "Valor" 
				FROM :v_Eventos
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion"
				ON "CodigoParametro" LIKE '%PLA.UrlLogoCorredoraPublic%'
				UNION
				SELECT
				"IdSecuencia" as "IdSecuencia",
				'ID_EVENTO_AUT' AS "Llave",  
				CAST(CA."IdEvento" AS NVARCHAR)  AS "Valor"
				FROM :v_Eventos v_ev
				INNER JOIN "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEvento" CA ON  v_ev."IdSecuencia"=CA."IdEtapaEvento"
				UNION
				SELECT
				"IdSecuencia" as "IdSecuencia",
				'ID_ETAPAEVENTO_AUT' AS "Llave",  
				CAST("IdSecuencia" AS NVARCHAR) AS "Valor"
				FROM :v_Eventos v_ev;
				/*UNION
				SELECT
				"IdSecuencia" as "IdSecuencia"
				'ID_USUARIOS_DESTINO' AS "Llave",
				STRING_AGG(v_dic."IdUsuario",',') AS "Valor"
				FROM :v_IdDirecciones v_dic
				GROUP BY "IdSecuencia", 'ID_USUARIOS_DESTINO'*/
				
							  
				
			v_out_Agregar_Evento = SELECT 0 AS "IdEvento",0 as "IdSecuencia" FROM DUMMY WHERE 1=2;	
				
 				CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(
 					p_IdEmpresa => p_IdEmpresa,
 					p_IdUsuario => :p_IdUsuario,
 					p_IdModulo => 'PLA',
 					p_Eventos => :v_Eventos,
 					p_Valores => :v_EventoDetalle,
 					p_ArchivosAdjuntos => :v_ArchivosAdjuntos,
 					p_IdDirecciones => :v_IdDirecciones,
 					p_AplicaOrigenEvento =>'S',
 					p_out_IdEvento => :v_out_Agregar_Evento
 					);
			
			
		END;
			
		p_out_IdEvento := v_SecuenciaEvento;
	
	
				   
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

