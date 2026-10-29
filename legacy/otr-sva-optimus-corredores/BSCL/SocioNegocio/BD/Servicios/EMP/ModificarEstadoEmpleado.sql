 SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::EMP.ModificarEstadoEmpleado";

CREATE PROCEDURE
-- 
--  Proposito: Servicio que obtiene y evalua el estado del empleado a modificar, para ser creado 
--  como una solicitud de Cambio.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 08-01-2016
--  
"BSCL.SocioNegocio.BD.Servicios::EMP.ModificarEstadoEmpleado" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdEmpleado								BIGINT,	
	IN  p_IdEstado									NVARCHAR(3),
	IN  p_IdTipo		 							NVARCHAR(40),
	IN  p_Fecha		 								NVARCHAR(27),
	IN  p_Comentario		 						NVARCHAR(255),
	
	-- Parametros salida del servicio publico
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
			DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
			DECLARE v_countEmpleado				 					BIGINT;
			DECLARE v_estadoAutorizacion							NVARCHAR(3);
			DECLARE v_RequiereAutorizacion							NVARCHAR(1);
			DECLARE v_secuencia_IdSolicitudCambio					BIGINT;
			DECLARE v_out_IdSecuencias table ("IdSecuencia" BIGINT);
			
			
		    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
			DECLARE v_fechaHoraSistema NVARCHAR(40);
			
			--------------------
		--Validaciones
		--------------------
		
		-- Existencia 		
		BEGIN
			v_IdEntidadOrigen:=CAST (p_IdEmpleado AS NVARCHAR(40));
			SELECT COUNT("IdEmpleado") INTO v_countEmpleado FROM "BSCL.SocioNegocio.BD::EMP.Empleado" WHERE "IdEmpleado"=v_IdEntidadOrigen;
			IF(v_countEmpleado=0) THEN
				p_out_ErroresDetalle =  select 'EMPLEADO_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
		END;		
		
		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			--Inicializacion de variables:
			v_secuencia_IdSolicitudCambio 	:= -1;
			
			v_estadoAutorizacion 			:= 'PEN';
			v_tipoEntidad					:= 'EMPLEADO-ESTADO';
			v_IdConcepto					:= 'CAEEMP';			
			p_out_RefId						:=0;
			
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			-- 			
			
			-- 1) OBTENCION DE VALORES SEGUN CAMPOS ENVIADOS DESDE UI
			-- (Se obtiene: Tipo de Dato y Valor Anterior del campo modificado
			
			--SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSecuenciaCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
			SELECT "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio".NEXTVAL into v_secuencia_IdSolicitudCambio from dummy;
			
						
			
IF (p_IdEstado = 'BLO') THEN
			V_RegistroSolicitudCambio =SELECT	
						v_secuencia_IdSolicitudCambio AS "IdSolicitudCambio",	
						 0 AS		"IdSecuenciaCambio", --- Se crea interno
						v_tipoEntidad	AS "IdTipoEntidad",	
						CAST (v_IdEntidadOrigen AS NVARCHAR(40))	AS "IdEntidadOrigen",
						MOD."COLUMN_NAME" 		AS "CampoModificado",
						MOD."DATA_TYPE_NAME" AS "TipoDato",
						(CASE MOD."COLUMN_NAME" WHEN 'IdEstado'			 THEN IFNULL(CAST (EMP."IdEstado" AS NVARCHAR(255)),'')
												WHEN 'IdTipoBloqueo'	 THEN IFNULL(CAST (EMP."IdTipoBloqueo" AS NVARCHAR(255)),'')
												WHEN 'FechaBloqueo'		 THEN IFNULL(CAST (EMP."FechaBloqueo" AS NVARCHAR(255)),'')
												WHEN 'ComentarioBloqueo' THEN IFNULL(CAST (EMP."ComentarioBloqueo" AS NVARCHAR(255)),'')
								END)    AS "ValorAntiguo",
						(CASE MOD."COLUMN_NAME" WHEN 'IdEstado'			THEN p_IdEstado
												WHEN 'IdTipoBloqueo'	THEN p_IdTipo
												WHEN 'FechaBloqueo'		THEN p_Fecha
												WHEN 'ComentarioBloqueo'		THEN p_Comentario
								END)    AS "ValorNuevo",								
						CURRENT_UTCTIMESTAMP AS "FechaSolicitud",--SE CREAN INTERNOS
						CURRENT_UTCTIMESTAMP AS "FechaRevision",--SE CREAN INTERNOS
						v_estadoAutorizacion AS "IdEstadoAutorizacion"
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP,
								(SELECT COL."DATA_TYPE_NAME",
										COL."COLUMN_NAME"
									FROM SYS.TABLE_COLUMNS COL
								WHERE COL."TABLE_NAME"='BSCL.SocioNegocio.BD::EMP.Empleado'
								AND COL."COLUMN_NAME"IN ('IdEstado','IdTipoBloqueo','FechaBloqueo','ComentarioBloqueo') AND COL.SCHEMA_NAME='BSCL') MOD
				WHERE  EMP."IdEmpleado" =v_IdEntidadOrigen;
ELSEIF (p_IdEstado = 'ACT') THEN
				V_RegistroSolicitudCambio =SELECT	
										v_secuencia_IdSolicitudCambio AS "IdSolicitudCambio",	
										 0 AS		"IdSecuenciaCambio", --- Se crea interno
										v_tipoEntidad	AS "IdTipoEntidad",	
										CAST (v_IdEntidadOrigen AS NVARCHAR(40))	AS "IdEntidadOrigen",
										MOD."COLUMN_NAME" 		AS "CampoModificado",
										MOD."DATA_TYPE_NAME" AS "TipoDato",
										(CASE MOD."COLUMN_NAME" WHEN 'IdEstado'			 	THEN IFNULL(CAST (EMP."IdEstado" AS NVARCHAR(255)),'')
																WHEN 'IdTipoDesbloqueo'	 	THEN IFNULL(CAST (EMP."IdTipoDesbloqueo" AS NVARCHAR(255)),'')
																WHEN 'FechaDesbloqueo'		THEN IFNULL(CAST (EMP."FechaDesbloqueo" AS NVARCHAR(255)),'')
																WHEN 'ComentarioDesbloqueo' THEN IFNULL(CAST (EMP."ComentarioDesbloqueo" AS NVARCHAR(255)),'')
												END)    AS "ValorAntiguo",
										(CASE MOD."COLUMN_NAME" WHEN 'IdEstado'				THEN p_IdEstado
																WHEN 'IdTipoDesbloqueo'		THEN p_IdTipo
																WHEN 'FechaDesbloqueo'		THEN p_Fecha
																WHEN 'ComentarioDesbloqueo'	THEN p_Comentario
												END)    AS "ValorNuevo",								
										CURRENT_UTCTIMESTAMP AS "FechaSolicitud",--SE CREAN INTERNOS
										CURRENT_UTCTIMESTAMP AS "FechaRevision",--SE CREAN INTERNOS
										v_estadoAutorizacion AS "IdEstadoAutorizacion"
								FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP,
												(SELECT COL."DATA_TYPE_NAME",
														COL."COLUMN_NAME"
													FROM SYS.TABLE_COLUMNS COL
												WHERE COL."TABLE_NAME"='BSCL.SocioNegocio.BD::EMP.Empleado'
												AND COL."COLUMN_NAME"IN ('IdEstado','IdTipoDesbloqueo','FechaDesbloqueo','ComentarioDesbloqueo') 
												AND COL.SCHEMA_NAME='BSCL') MOD
								WHERE  EMP."IdEmpleado" =v_IdEntidadOrigen;


	ELSE 
				p_out_ErroresContador := p_out_ErroresContador + 1;
				p_out_ErroresDetalle = select 'ESTADO_NO_DEFINIDO' as "Codigo",'ESTADO_NO_DEFINIDO' as "Descripcion" from dummy 
									   Union All 
		                               Select "Codigo", "Descripcion" From :p_out_ErroresDetalle; 
				SIGNAL SALIDA_CONTROLADA; 
			END IF;
		
			--select "DATA_TYPE_NAME","LENGTH","COLUMN_NAME",* from SYS.TABLE_COLUMNS WHERE "TABLE_NAME"='BSCL.SocioNegocio.BD::EMP.Cliente'
							
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
			call "BSCL.SocioNegocio.BD.Servicios::EMP.RequiereAutorizacionEmpleadoDatosGenerales" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:V_RegistroSolicitudCambio,
				:v_IdConcepto,
				:v_RequiereAutorizacion
			); */
			
		
		if v_RequiereAutorizacion='S' then
		------------------------------------------
		--Genera Evento de Autorizacion
		------------------------------------------
			DECLARE	 v_Glosa			NVARCHAR(520);
			declare v_out_idAuth 		bigint;
			declare v_out_AuthAdvertenciasContador 	int;
			declare v_out_AuthErroresContador 		int;
			v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
			v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
			
			SELECT "DocumentoIdentidad" || ' / ' || "NombreCompleto" into  v_Glosa 
			FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" WHERE "IdEmpleado" = p_IdEmpleado;
						
			CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
				p_IdEmpresa 				=> p_IdEmpresa,
				p_IdUsuario 				=> p_IdUsuario,
				p_IdModulo 					=> p_IdModulo,
				p_UsaReglasNegocio 			=> 'N',								
				p_IdTipoEvento 				=> v_IdConcepto,
				p_IdModuloSolicita 			=> p_IdModulo,
				p_IdUsuarioSolicita 		=> p_IdUsuario,
				p_Fecha 					=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'), 
				p_IdConcepto 				=> v_IdConcepto,
				p_NumeroOperacionConcepto 	=> CAST (p_IdEmpleado AS NVARCHAR(40)), --v_secuencia_IdSolicitudCambio,
				p_FechaOperacionConcepto 	=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'),
				p_IdFuncionario             => p_IdEmpleado,
				p_IdCliente 				=> p_IdEmpleado,
				p_Glosa 					=> v_Glosa,
				--salida
				p_out_IdEvento 				=> v_out_idAuth,
				p_out_AdvertenciasContador 	=> v_out_AuthAdvertenciasContador,
				p_out_AdvertenciasDetalle 	=> v_out_AuthAdvertenciasDetalle,
				p_out_ErroresContador 		=> v_out_AuthErroresContador,
				p_out_ErroresDetalle 		=> v_out_AuthErroresDetalle);				
				
			
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
				
				call  "BSCL.SocioNegocio.BD.Servicios::EMP.ConfirmarModificarEstadoEmpleado" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:p_UsaReglasNegocio,
				:p_IdEmpleado,
				'ING',
				V_out_AdvertenciasContador,
				v_out_AdvertenciasDetalle,	
				V_out_ErroresContador,
				V_out_ErroresDetalle
				);
				
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
		                                
		p_out_RefId:=v_secuencia_IdSolicitudCambio;				
		end if;
		
		


	END;
					
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;