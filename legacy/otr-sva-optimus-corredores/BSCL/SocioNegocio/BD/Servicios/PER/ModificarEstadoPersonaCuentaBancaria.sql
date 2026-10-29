SET SCHEMA "BSCL"; 

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.ModificarEstadoPersonaCuentaBancaria";

CREATE PROCEDURE
-- 
--  Proposito: Servicio que obtiene y evalua el estado de la cuenta bancaria, 
--  para ser creado como una solicitud de Cambio.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 13-01-2016
--  
"BSCL.SocioNegocio.BD.Servicios::PER.ModificarEstadoPersonaCuentaBancaria" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
	IN  p_IdPersona									BIGINT DEFAULT 0,	  
	
	-- Parametros entrada del servicio publico
	IN  p_IdCuentaBancaria				    	BIGINT,	
	IN  p_IdEstado									NVARCHAR(3),
	IN  p_Comentario		 						NVARCHAR(255),
	/*
	IN  p_RegistroCambio		  TABLE("CampoModificado"		NVARCHAR(255), -- parametro estandar
											"ValorNuevo"			NVARCHAR(255)),-- parametro estandar
	*/
	-- Parametros salida del servicio publico
--	OUT p_out_IdSecuencias table ("IdSecuencia" BIGINT)
	OUT p_out_RefId 					BIGINT,	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  			nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA

	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
			DECLARE v_tipoEntidad									NVARCHAR(40);
			DECLARE v_IdConcepto	       							NVARCHAR(6);
			DECLARE v_Evento      									BIGINT:=1;
			DECLARE v_TolalEventos						      		BIGINT:=0;	
			DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
			DECLARE v_IdEntidadBase									BIGINT:=0;
			DECLARE v_countEntidad			   					    BIGINT;
			DECLARE v_estadoAutorizacion							NVARCHAR(3);
			DECLARE v_RequiereAutorizacion							NVARCHAR(1);
			DECLARE v_secuencia_IdSolicitudCambio					BIGINT;
			DECLARE v_out_IdSecuencias 								table ("IdSecuencia" BIGINT);
			
			
		    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
			
			DECLARE v_countDocumentoPrincipal  					    BIGINT;
			DECLARE v_fechaHoraSistema NVARCHAR(40);
			--------------------
		--Validaciones
		--------------------
		
		-- Existencia 		
		BEGIN
			v_IdEntidadOrigen:=CAST (p_IdCuentaBancaria AS NVARCHAR(40));
			
			SELECT COUNT(1) INTO v_countEntidad FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" WHERE "IdCuentaBancaria"=v_IdEntidadOrigen;
			
			IF(v_countEntidad=0) THEN
				p_out_ErroresDetalle =  select 'ENTIDAD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
				ELSE
					SELECT "IdPersona" INTO v_IdEntidadBase FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" WHERE "IdCuentaBancaria"=p_IdCuentaBancaria;
			END IF;
		END;		
		
		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			v_IdEntidadOrigen:=CAST (p_IdCuentaBancaria AS NVARCHAR(40));
			SELECT COUNT(1) INTO v_countEntidad FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" WHERE "IdCuentaBancaria"=v_IdEntidadOrigen and "Predeterminado" ='S' and p_IdEstado = 'INA';
			IF(v_countEntidad=1) THEN
				p_out_ErroresDetalle =  select 'UI_ERROR_BLOQUEAR_CUENTA_PREDETERMINADA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
				ELSE
					SELECT "IdPersona" INTO v_IdEntidadBase FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" WHERE "IdCuentaBancaria"=p_IdCuentaBancaria;
			END IF;
		END;
		BEGIN
			--Inicializacion de variables:
			v_secuencia_IdSolicitudCambio 	:= -1;
			
			v_estadoAutorizacion 			:= 'PEN';
			v_tipoEntidad					:= 'CUENTABANCARIA-ESTADO';
			v_IdConcepto					:= 'CAECTB';
			p_out_RefId						:=	0;
			
			
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);			
			-- 1) OBTENCION DE VALORES SEGUN CAMPOS ENVIADOS DESDE UI
			-- (Se obtiene: Tipo de Dato y Valor Anterior del campo modificado
			
			
			SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
		
		
			V_RegistroSolicitudCambio =SELECT	
						v_secuencia_IdSolicitudCambio AS "IdSolicitudCambio",	
						 0 AS		"IdSecuenciaCambio", --- Se crea interno
						v_tipoEntidad	AS "IdTipoEntidad",	
						v_IdEntidadOrigen	AS "IdEntidadOrigen",
						MOD."COLUMN_NAME" 		AS "CampoModificado",
						MOD."DATA_TYPE_NAME" AS "TipoDato",
						(CASE MOD."COLUMN_NAME" WHEN 'IdEstado'		THEN IFNULL(CAST (CTB."IdEstado" AS NVARCHAR(255)),'')
												WHEN 'Comentario'	THEN ''
								END)    AS "ValorAntiguo",
						(CASE MOD."COLUMN_NAME" WHEN 'IdEstado'		THEN p_IdEstado
												WHEN 'Comentario'	THEN p_Comentario
								END)    AS "ValorNuevo",								
						CURRENT_UTCTIMESTAMP AS "FechaSolicitud",--SE CREAN INTERNOS
						CURRENT_UTCTIMESTAMP AS "FechaRevision",--SE CREAN INTERNOS
						v_estadoAutorizacion AS "IdEstadoAutorizacion"
				FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" CTB,
								(SELECT COL."DATA_TYPE_NAME",
										COL."COLUMN_NAME"
									FROM SYS.TABLE_COLUMNS COL
								WHERE COL."TABLE_NAME"='BSCL.SocioNegocio.BD::PER.CuentaBancaria'
								AND COL."COLUMN_NAME"= 'IdEstado'  AND COL.SCHEMA_NAME='BSCL'
								UNION ALL SELECT 'NVARCHAR' AS "DATA_TYPE_NAME",
												 'Comentario' AS  "COLUMN_NAME" FROM dummy) MOD
				WHERE  CTB."IdCuentaBancaria" =v_IdEntidadOrigen;




							
			-- 2) ENVIO A SERVICIO CREACION SOLICITUD DE CAMBIO

			CALL  "BSCL.Plataforma.BD.Servicios::PLA.CrearRegistroSolicitudCambio"
			(
				:V_RegistroSolicitudCambio,
				v_out_IdSecuencias
				
			);
											
						
							
			-- 2) DETERMINA SI LA MODIFICACION  REQUIERE DE UNA AUTORIZACION PREVIA
					
			------------------------------------------
			--Pregunta Si requiere autorizacion
			------------------------------------------
			
			call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:V_RegistroSolicitudCambio,
				:v_IdConcepto,
				:v_RequiereAutorizacion
			);	
			/* 
			call "BSCL.SocioNegocio.BD.Servicios::PER.RequiereAutorizacionPersonaCuentaBancaria" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:V_RegistroSolicitudCambio,
				:v_IdConcepto,--Segun el concepto
				:v_RequiereAutorizacion
			); */
			
				
		if v_RequiereAutorizacion ='S' and p_UsaReglasNegocio != 'N' then
		------------------------------------------
		--Genera Evento de Autorizacion
		------------------------------------------
			DECLARE	 v_Glosa		NVARCHAR(255);
			declare v_out_idAuth 		bigint;
			declare v_out_AuthAdvertenciasContador 	int;
			declare v_out_AuthErroresContador 		int;
			v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
			v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
			
			SELECT "DocumentoIdentidad" || ' / ' || "NombreCompleto" || ' / IdCta: ' || p_IdCuentaBancaria  into  v_Glosa 
			FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" WHERE "IdPersona" = v_IdEntidadBase;
					
			
					CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
						p_IdEmpresa 				=> p_IdEmpresa,
						p_IdUsuario 				=> p_IdUsuario,
						p_IdModulo 					=> p_IdModulo,
						p_UsaReglasNegocio 			=> 'N',								
						p_IdTipoEvento 				=> v_IdConcepto,--'MODCTB',
						p_IdModuloSolicita 			=> p_IdModulo,
						p_IdUsuarioSolicita 		=> p_IdUsuario,
						p_Fecha 					=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'), 
						p_IdConcepto 				=> v_IdConcepto,
						p_NumeroOperacionConcepto 	=>  v_IdEntidadOrigen,--v_secuencia_IdSolicitudCambio,
						p_FechaOperacionConcepto 	=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'),
						p_IdCliente 				=> v_IdEntidadBase, --v_IdCliente,
						p_Glosa 					=> v_Glosa,
						--salida
						p_out_IdEvento 				=> v_out_idAuth,
						p_out_AdvertenciasContador 	=> v_out_AuthAdvertenciasContador,
						p_out_AdvertenciasDetalle 	=>  v_out_AuthAdvertenciasDetalle,
						p_out_ErroresContador 		=>  v_out_AuthErroresContador,
						p_out_ErroresDetalle 		=>  v_out_AuthErroresDetalle);		
			
					 p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AuthAdvertenciasContador;	
		  			 
					 p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
		                                          From :v_out_AuthAdvertenciasDetalle Union All 
		                                          Select "Codigo","RequiereAutorizacion", "Descripcion"
		                                          From :p_out_AdvertenciasDetalle;  
		                                          
		                                          
		              p_out_ErroresContador := p_out_ErroresContador + v_out_AuthErroresContador;
		                                         
		              p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
		                                     From :v_out_AuthErroresDetalle Union All 
		                                     Select "Codigo", "Descripcion" 
		                                     From :p_out_ErroresDetalle; 
		                    
                                     
			  if 	p_out_ErroresContador > 0 then
						SIGNAL SALIDA_CONTROLADA;
						
					else
						--Mensaje de Salida
						p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;	
						v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
						CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION',:v_hash,:v_erroroutmsg);
						p_out_AdvertenciasDetalle =  select 'REQUIERE_AUTORIZACION' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
												     select * from :p_out_AdvertenciasDetalle;
						SIGNAL SALIDA_CONTROLADA;
			  end if;								
			                            
				
				
		else
		
			------------------------------------------
			-- Se confirma la modificacion y se realizan los cambios
			------------------------------------------
					
				call  "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarEstadoPersonaCuentaBancaria" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:p_UsaReglasNegocio,
				:v_IdEntidadOrigen,
				'ING',
				V_out_AdvertenciasContador,
				v_out_AdvertenciasDetalle,	
				V_out_ErroresContador,
				V_out_ErroresDetalle
				);
				
				-- Se realiza insert en tabla LogGenerarContratos para un posterior procesamiento y comprobacion de datos obligatorios que pudiera gatillar la regeneracion de sus documentos.		
				INSERT INTO "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
				("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
				SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 'CUENTA_BANCARIA_', 'CAMBIOESTADOBLOQUEO', :p_IdEmpresa, 0, :p_IdPersona, :p_IdUsuario, LEFT(:v_fechaHoraSistema,10) , 0, 'PEN', 'Persona - ModificarEstadoPersonaCuentaBancaria.sql'
				FROM dummy;
  			 p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContador;	
  			 
			 p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
                                          From :v_out_AdvertenciasDetalle Union All 
                                          Select "Codigo","RequiereAutorizacion", "Descripcion"
                                          From :p_out_AdvertenciasDetalle;  
                                          
                                          
              p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador;
                                         
              p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
                                     From :v_out_ErroresDetalle Union All 
                                     Select "Codigo", "Descripcion" 
                                     From :p_out_ErroresDetalle; 
                
                                     
                                     
			  if 	p_out_ErroresContador > 0 then
						SIGNAL SALIDA_CONTROLADA;
			  end if;								
		                                
						
		end if;
		
		
		p_out_RefId:=v_secuencia_IdSolicitudCambio;

	END;
					
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;