SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::EMP.ConfirmarModificarEstadoEmpleadoMasivo";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion al estado del documentos de identidad.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 30-12-2015
--  
"BSCL.SocioNegocio.BD.Servicios::EMP.ConfirmarModificarEstadoEmpleadoMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	
	
	
	IN 	p_Empleado						TABLE ("IdPersona"	BIGINT),
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
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'EMPLEADO-ESTADO';
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_CountAtributoModificado						BIGINT:=0;
			DECLARE v_CountAtributoNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_IdEstado								BIGINT;
			
			
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			--v_IdEntidadOrigen:=CAST (p_IdEmpleado AS NVARCHAR(40));
			
			SELECT COUNT("IdSolicitudCambio") INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_Empleado P ON  CAST(P."IdPersona" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
				AND SOL."IdTipoEntidad" 		= v_TipoEntidad
                AND SOL."IdEstadoAutorizacion"	='PEN';
                
                IF(v_countSolicitud=0) THEN
				p_out_ErroresDetalle =  select 'SOLICITUD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
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
			v_DatosSolicitudCambio = SELECT * 
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_Empleado P ON  CAST(P."IdPersona" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
				AND SOL."IdTipoEntidad" 		= v_TipoEntidad
                AND SOL."IdEstadoAutorizacion"	='PEN';
			
			SELECT 	COUNT("IdSolicitudCambio") INTO	v_CountAtributoModificado FROM	:v_DatosSolicitudCambio;
			
			IF (v_CountAtributoModificado > 0) 	 THEN
			
					v_CampoModificado:='IdEstado';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdEstado > 0) THEN
		
						UPDATE  EMP
						SET
							EMP."IdEstado" = MOD."ValorNuevo",
							EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
								INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND EMP."IdEmpleado"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
					END IF;
					
					
					v_CampoModificado:='IdTipoBloqueo';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdEstado > 0) THEN
		
						UPDATE  EMP
						SET
							EMP."IdTipoBloqueo" = MOD."ValorNuevo",
							EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
								INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND EMP."IdEmpleado"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
					END IF;
					
					v_CampoModificado:='IdTipoDesbloqueo';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdEstado > 0) THEN
		
						UPDATE  EMP
						SET
							EMP."IdTipoDesbloqueo" = MOD."ValorNuevo",
							EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
								INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND EMP."IdEmpleado"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
					END IF;
					
					
					v_CampoModificado:='FechaBloqueo';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdEstado > 0) THEN
		
						UPDATE  EMP
						SET
							EMP."FechaBloqueo" = CURRENT_UTCTIMESTAMP,
							EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
								INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND EMP."IdEmpleado"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
					END IF;
					
					
					v_CampoModificado:='FechaDesbloqueo';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdEstado > 0) THEN
		
						UPDATE  EMP
						SET
							EMP."FechaDesbloqueo" = CURRENT_UTCTIMESTAMP,
							EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
								INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND EMP."IdEmpleado"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
					END IF;
					
					v_CampoModificado:='ComentarioBloqueo';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdEstado > 0) THEN
		
						UPDATE  EMP
						SET
							EMP."ComentarioBloqueo" = MOD."ValorNuevo",
							EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
								INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND EMP."IdEmpleado"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
					END IF;
					
					v_CampoModificado:='ComentarioDesbloqueo';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdEstado > 0) THEN
		
						UPDATE  EMP
						SET
							EMP."ComentarioDesbloqueo" = MOD."ValorNuevo",
							EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
								INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND EMP."IdEmpleado"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
							
					END IF;
					
					
			END IF;	
			
	
	
			v_SolicitudCambio = SELECT 0 as "IdSolicitudCambio" FROM dummy where 1=2;
			v_EntidadOrigen = SELECT "IdEntidadOrigen", "IdTipoEntidad" FROM :v_DatosSolicitudCambio;
			--Se termina el flujo con la actualizacion del registro de solicitud.

			CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambioMasivo" (
				p_IdEmpresa				=>:p_IdEmpresa,
				p_IdUsuario				=>:p_IdUsuario,
				p_IdModulo				=>:p_IdModulo,
				p_SolicitudCambio			=>:v_SolicitudCambio,
				p_EntidadOrigen			    =>:v_EntidadOrigen,
				p_IdEstadoAutorizacion	=>:p_IdEstadoAutorizacion,
				p_out_AdvertenciasContador		=>:p_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle		=>:p_out_AdvertenciasDetalle,	
				p_out_ErroresContador			=>:p_out_ErroresContador,
				p_out_ErroresDetalle			=>:p_out_ErroresDetalle				
				
			);

					--LOG DE AUDITORIA (inicio)
			BEGIN 
				--DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
				DECLARE	v_IdTipoDocumentoIdentidad	NVARCHAR(40);
				DECLARE	v_IdDocumentoIdentidad	NVARCHAR(40);
				DECLARE v_fechaHoraSistema NVARCHAR(40);
				DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Cambio de Estado';
				
				v_id_empleado = SELECT "IdPersona", "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL AS "IdLog" FROM :p_Empleado;
					
				call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);
				
				--DATOS DETALLE LOG
				v_DetalleLog = 
				(Select EMP."IdLog"			 AS "IdCabeceraLog",
						MOD."IdTipoEntidad"		 as "SubRegistro",
						MOD."CampoModificado"	 as "CampoRegistro",
						MOD."ValorAntiguo"  	 as "ValorAntiguo",
						MOD."ValorNuevo" 		 as "ValorNuevo" 
				FROM :v_DatosSolicitudCambio MOD
				JOIN :v_id_empleado AS EMP ON (EMP."IdPersona" = MOD."IdPersona"));
				
				v_log = SELECT P."IdLog" AS "IdCabeceraLog"
							    ,'MODIFICACION' AS "IdTipoLog"
							   ,'SOC' AS "IdModulo"
							   ,p_IdUsuario AS "IdUsuario"
							   ,p_IdEmpresa AS "IdEmpresa"
							   ,TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') AS "FechaSistemaLog"
							   ,p_IdUsuario AS "IdUsuarioAtendido"
							   ,0 AS "IdPersona"
							   ,0 AS "IdCliente"
							   ,EMP."IdEstado" AS "EstadoCliente"
							   ,EMP."NombreCompleto" AS "NombreCliente" 
							   ,EMP."TipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidad" 
							   ,EMP."DocumentoIdentidad" AS "IdDocumentoIdentidad"
							   ,0 AS "IdPortafolio"
							   ,'' AS "EstadoPortafolio"
							   ,v_TipoEntidad AS "IdTipoRegistro" 
							   ,P."IdPersona" AS "IdRegistro"
							   ,p_IdEstadoAutorizacion AS "EstadoRegistro"
							   ,'' AS "IdTipoOperacion" 
							   ,0 AS "Folio"
							   ,0 AS "Cantidad"
							   ,0 AS "PrecioTasa"
							   ,0 AS "Monto"
							   ,'' AS "IdMonedaOrigen"
							   ,'' AS "IdMonedaLiquidacion"
							   ,0 AS "TipoCambio"
							   ,'' AS "Nemo"
							   ,P."IdLog" AS "IdProceso"
							   ,p_IdEstadoAutorizacion AS "EstadoProceso"
							   ,v_NombreProceso AS "NombreProceso"
						FROM :v_id_empleado AS P
						JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMP ON EMP."IdPersona"=P."IdPersona";	   


					--GRABAR LOG
					CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo"(
						p_Log => :v_log,
						p_LogDetalle => :v_DetalleLog
					);
	
			END;
			--LOG DE AUDITORIA (Fin)			
			
			
			
			
			
			
			
						
		END;

		--------------------
		--FIN SP 
		--------------------
	END;	
END;