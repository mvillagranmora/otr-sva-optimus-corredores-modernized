SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarEstadoPersona";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion al estado de la persona.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 30-12-2015
--  
"BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarEstadoPersona" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN 	p_IdPersona						BIGINT,	
	IN  p_IdEstadoAutorizacion						NVARCHAR(3),
	
	-- Parametros salida del servicio publico
	--OUT p_out_RefId 								BIGINT,
			
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	
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
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  			nvarchar(5000);	        	
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
			DECLARE v_countSolicitud								BIGINT:=0;
			DECLARE v_countAutorizacionesPendientes					BIGINT;
			DECLARE v_IdEstadoAutorizacion_Calculado				NVARCHAR(3);
			DECLARE v_IdEntidadOrigen								NVARCHAR(40);
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'PERSONA-ESTADO';
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_CountAtributoModificado						BIGINT:=0;
			DECLARE v_CountAtributoNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_IdEstado								BIGINT;
			DECLARE v_count_IdTipoBloqueo							BIGINT;
			DECLARE v_count_FechaBloqueo							BIGINT;
			DECLARE v_count_ComentarioBloqueo						BIGINT;
			
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			v_IdEntidadOrigen:=CAST (p_IdPersona AS NVARCHAR(40));
			
			SELECT COUNT(1) INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
			WHERE "IdEntidadOrigen"  	= v_IdEntidadOrigen
			 AND "IdTipoEntidad" 		= v_TipoEntidad
  			 AND "IdEstadoAutorizacion"	='PEN';
		
			IF (v_countSolicitud = 0) THEN
				p_out_ErroresDetalle =  select 'SOLICITUD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				--p_out_ErroresDetalle =  select cast(v_countSolicitud as nvarchar(40)) as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
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
			

			--ACTUALIZACION DE VALORES (* ENTIDAD EXISTENTE *)
									
			--DATOS MAESTROS DE LA ENTIDAD 
			v_DatosSolicitudCambio = SELECT * FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
			WHERE "IdEntidadOrigen"  	= v_IdEntidadOrigen
			 AND "IdTipoEntidad" 		= v_TipoEntidad
  			 AND "IdEstadoAutorizacion"	='PEN';
			
			--SELECT 	COUNT("IdSolicitudCambio") INTO	v_CountAtributoModificado FROM	:v_DatosSolicitudCambio;
			
			--IF (v_CountAtributoModificado > 0) 	 THEN
			
					v_CampoModificado:='IdEstado';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdEstado > 0) THEN
		
						UPDATE  PER
						SET
							PER."IdEstado" = MOD."ValorNuevo",
							PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
								INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND PER."IdPersona"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
						UPDATE CLI
						SET
							CLI."IdEstado" = MOD."ValorNuevo",
							CLI."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int)) AND CLI."IdEstado" != MOD."ValorNuevo"
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
						
						UPDATE POR
						SET
							POR."IdEstado" = MOD."ValorNuevo",
							POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
								INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					
					END IF;
					
					
					
					v_CampoModificado:='IdTipoBloqueo';
					SELECT count("ValorNuevo") INTO v_count_IdTipoBloqueo
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdTipoBloqueo > 0) THEN
		
						UPDATE  PER
						SET
							PER."IdTipoBloqueo" = CASE WHEN MOD."ValorNuevo"='' THEN NULL ELSE MOD."ValorNuevo" END,
							PER."IdTipoDesbloqueo"='',
							PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
								INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND PER."IdPersona"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
						UPDATE  CLI
						SET
							CLI."IdTipoBloqueo" = CASE WHEN MOD."ValorNuevo"='' THEN NULL ELSE MOD."ValorNuevo" END,
							CLI."IdTipoDesbloqueo"='',
							CLI."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int)) AND (CLI."IdTipoBloqueo" IS NULL OR CLI."IdTipoBloqueo" != MOD."ValorNuevo")
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
						
						UPDATE POR
						SET
							POR."IdTipoBloqueo" = MOD."ValorNuevo",
							POR."IdTipoDesbloqueo" = '',
							POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
								INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					
					END IF;
					
					v_CampoModificado:='IdTipoDesbloqueo';
					SELECT count("ValorNuevo") INTO v_count_IdTipoBloqueo
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdTipoBloqueo > 0) THEN
		
						UPDATE  PER
						SET
							PER."IdTipoDesbloqueo" = CASE WHEN MOD."ValorNuevo"='' THEN NULL ELSE MOD."ValorNuevo" END,
							PER."IdTipoBloqueo" = '',
							PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
								INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND PER."IdPersona"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
						UPDATE  CLI
						SET
							CLI."IdTipoDesbloqueo" = CASE WHEN MOD."ValorNuevo"='' THEN NULL ELSE MOD."ValorNuevo" END,
							CLI."IdTipoBloqueo"='',
							CLI."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int)) AND CLI."IdTipoDesbloqueo" != MOD."ValorNuevo"
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
						
						UPDATE POR
						SET
							POR."IdTipoBloqueo" = '',
							POR."IdTipoDesbloqueo" = MOD."ValorNuevo",
							POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
								INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					
					END IF;
					
					
					
					v_CampoModificado:='FechaBloqueo';
					SELECT count("ValorNuevo") INTO v_count_FechaBloqueo
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_FechaBloqueo > 0) THEN
		
						UPDATE  PER
						SET
							PER."FechaBloqueo" = CURRENT_UTCTIMESTAMP,
							PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
								INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND PER."IdPersona"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
						UPDATE  CLI
						SET
							CLI."FechaBloqueo" = CURRENT_UTCTIMESTAMP,
							CLI."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int)) AND CLI."FechaBloqueo" != MOD."ValorNuevo"
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
						
						UPDATE POR
						SET
							POR."FechaBloqueo" = CURRENT_UTCTIMESTAMP,
							POR."FechaDesbloqueo" = '0001-01-01',
							POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
								INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					
					END IF;
					
					v_CampoModificado:='FechaDesbloqueo';
					SELECT count("ValorNuevo") INTO v_count_FechaBloqueo
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_FechaBloqueo > 0) THEN
		
						UPDATE  PER
						SET
							PER."FechaDesbloqueo" = CURRENT_UTCTIMESTAMP,
							PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
								INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND PER."IdPersona"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
						UPDATE  CLI
						SET
							CLI."FechaDesbloqueo" = CURRENT_UTCTIMESTAMP,
							CLI."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int)) AND CLI."FechaDesbloqueo" != MOD."ValorNuevo"
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
						
						UPDATE POR
						SET
							POR."FechaBloqueo" = '0001-01-01',
							POR."FechaDesbloqueo" = CURRENT_UTCTIMESTAMP,
							POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
								INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					
					END IF;
					
					
					v_CampoModificado:='ComentarioBloqueo';
					SELECT count("ValorNuevo") INTO v_count_ComentarioBloqueo
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_ComentarioBloqueo > 0) THEN
		
						UPDATE  PER
						SET
							PER."ComentarioBloqueo" = MOD."ValorNuevo",
							PER."ComentarioDesbloqueo" = '',
							PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
								INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND PER."IdPersona"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
						UPDATE  CLI
						SET
							CLI."ComentarioBloqueo" = MOD."ValorNuevo",
							CLI."ComentarioDesbloqueo" = '',
							CLI."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int)) AND CLI."ComentarioBloqueo" != MOD."ValorNuevo"
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
						
						UPDATE POR
						SET
							POR."ComentarioBloqueo" = MOD."ValorNuevo",
							POR."ComentarioDesbloqueo" = '',
							POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
								INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					
					END IF;
					
					
					v_CampoModificado:='ComentarioDesbloqueo';
					SELECT count("ValorNuevo") INTO v_count_ComentarioBloqueo
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_ComentarioBloqueo > 0) THEN
		
						UPDATE  PER
						SET
							PER."ComentarioDesbloqueo" = MOD."ValorNuevo",
							PER."ComentarioBloqueo"='',
							PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
								INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND PER."IdPersona"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
						UPDATE  CLI
						SET
							CLI."ComentarioDesbloqueo" = MOD."ValorNuevo",
							CLI."ComentarioBloqueo"='',
							CLI."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int)) AND CLI."ComentarioDesbloqueo" != MOD."ValorNuevo"
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
						
						UPDATE POR
						SET
							POR."ComentarioBloqueo" = '',
							POR."ComentarioDesbloqueo" = MOD."ValorNuevo",
							POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
								INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON POR."IdCliente" = CLI."IdCliente"
								INNER JOIN :v_DatosSolicitudCambio MOD ON (CLI."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" = V_CampoModificado
							AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					
					END IF;
					
			--END IF;	
			
	
	
			
			--Se termina el flujo con la actualizacion del registro de solicitud.

			CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambio" (
				p_IdEmpresa				=>:p_IdEmpresa,
				p_IdUsuario				=>:p_IdUsuario,
				p_IdModulo				=>:p_IdModulo,
				p_IdEntidadOrigen		=>:v_IdEntidadOrigen,
				p_IdTipoEntidad			=>:v_TipoEntidad,
				p_IdEstadoAutorizacion	=>:p_IdEstadoAutorizacion,
				p_out_AdvertenciasContador		=>:p_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle		=>:p_out_AdvertenciasDetalle,	
				p_out_ErroresContador			=>:p_out_ErroresContador,
				p_out_ErroresDetalle			=>:p_out_ErroresDetalle	
			);

	
			--LOG DE AUDITORIA (inicio)
			BEGIN 
				DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
				DECLARE	v_IdTipoDocumentoIdentidad	NVARCHAR(40);
				DECLARE	v_IdDocumentoIdentidad	NVARCHAR(40);
				DECLARE v_fechaHoraSistema NVARCHAR(40);
				DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Cambio de Estado';
				
				
				--DATOS CABECERA LOG
	
				SELECT "TipoDocumentoIdentidad",
				"DocumentoIdentidad"
				INTO v_IdTipoDocumentoIdentidad,
				v_IdDocumentoIdentidad
				FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" WHERE "IdPersona"=v_IdEntidadOrigen;
	
				call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);
				
				--DATOS DETALLE LOG
				p_DetalleLog = 
				(Select MOD."IdTipoEntidad"		 as "SubRegistro",
						MOD."CampoModificado"	 as "CampoRegistro",
						MOD."ValorAntiguo"  	 as "ValorAntiguo",
						MOD."ValorNuevo" 		 as "ValorNuevo" 
				FROM :v_DatosSolicitudCambio MOD);
				
				--GRABAR LOG
					CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'MODIFICACION' 
					,p_IdModulo => 'SOC' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdPersona => :p_IdPersona
					,p_IdTipoRegistro => v_TipoEntidad--'CLIENTE'
					,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => :v_IdEntidadOrigen
					,p_EstadoProceso => :p_IdEstadoAutorizacion
					,p_EstadoRegistro=> :p_IdEstadoAutorizacion
					,p_IdTipoDocumentoIdentidad=> :v_IdTipoDocumentoIdentidad
					,p_IdDocumentoIdentidad=> :v_IdDocumentoIdentidad					
					);
	
			END;
			--LOG DE AUDITORIA (Fin)							
		END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;