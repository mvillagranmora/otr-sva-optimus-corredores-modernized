SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarPersonaDatosGenerales";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion a una Persona.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 07-12-2015
--  
--  Proposito: Se incorporan definiciones Optimus Perú
--  Autor: JGARCES - BCS
--  Fecha Modificacion: 14/03/2017
--
"BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarPersonaDatosGenerales" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdPersona									BIGINT,	
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
			DECLARE v_countSolicitud								BIGINT;
			DECLARE v_countAutorizacionesPendientes					BIGINT;
			DECLARE v_IdEstadoAutorizacion_Calculado				NVARCHAR(3);
			DECLARE v_IdEntidadOrigen								NVARCHAR(40);
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'PERSONA';
			DECLARE V_CampoModificado								NVARCHAR(255):='';
			DECLARE v_countProcesados								BIGINT:=0;
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_Tipo			 						BIGINT;

						
	
			
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.		
		BEGIN
			v_IdEntidadOrigen:=CAST (p_IdPersona AS NVARCHAR(40));
			SELECT COUNT("IdSolicitudCambio") INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			WHERE
			-- SOL."IdSolicitudCambio" = :p_IdSolicitudCambio
				 SOL."IdEntidadOrigen"  	=	v_IdEntidadOrigen
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

									
			--DATOS MAESTROS DE LA ENTIDAD
			v_DatosSolicitudCambio = SELECT * FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
			WHERE "IdEntidadOrigen"  	= v_IdEntidadOrigen
			 AND "IdTipoEntidad" 		= v_TipoEntidad
  			 AND "IdEstadoAutorizacion"	='PEN';
			
			--ACTUALIZACION DE VALORES
			
			V_CampoModificado:='Tipo';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."Tipo" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
	
			V_CampoModificado:='Nombres';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."Nombres" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
					


			V_CampoModificado:='ApellidoPaterno';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."ApellidoPaterno" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;
						
			V_CampoModificado:='ApellidoMaterno';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."ApellidoMaterno" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;

			V_CampoModificado:='RazonSocial';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."RazonSocial" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
					INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;			

			V_CampoModificado:='IdGiroPrincipal';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."IdGiroPrincipal" = CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END,
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;		

			V_CampoModificado:='IdPaisNacimiento';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."IdPaisNacimiento" = CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END,
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	
	
			
			V_CampoModificado:='IdPaisResidencia';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."IdPaisResidencia" = CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END,
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	
					

			V_CampoModificado:='FechaNacimiento';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."FechaNacimiento" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	

			V_CampoModificado:='FechaDefuncion';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN
			
				UPDATE  PER
				SET
					PER."FechaDefuncion" = MOD."ValorNuevo",
					PER."IdEstado" = 'BLO',
					PER."IdTipoBloqueo" = 'BMUERT',
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	
								

			V_CampoModificado:='Sexo';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."Sexo" = CASE WHEN MOD."ValorNuevo"='' THEN NULL ELSE MOD."ValorNuevo" END,
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	
				
			V_CampoModificado:='EsExtranjero';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."EsExtranjero" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;			
			
			V_CampoModificado:='EstadoCivil';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."EstadoCivil" = CASE WHEN MOD."ValorNuevo"='' THEN NULL ELSE MOD."ValorNuevo" END,
					PER."FechaInicioConyuge"  = (CASE WHEN MOD."ValorNuevo"='CA' THEN CURRENT_UTCTIMESTAMP ELSE PER."FechaInicioConyuge" END),					
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;			
						
	
			V_CampoModificado:='RegimenConyugal';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."RegimenConyugal" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;				
			
			V_CampoModificado:='Cargo';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."Cargo" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;					
			

			V_CampoModificado:='Empleador';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."Empleador" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	
	
			V_CampoModificado:='IdProfesion';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."IdProfesion" = CASE WHEN MOD."ValorNuevo"='' THEN NULL ELSE MOD."ValorNuevo" END,
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;		
			
			V_CampoModificado:='FechaInicioActividad';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."FechaInicioActividad" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;		
						

			V_CampoModificado:='Alias';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."Alias" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;		
		
			V_CampoModificado:='IdPersonaConyuge';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."IdPersonaConyuge" =(CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END),
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;		
			
			V_CampoModificado:='IdPersonaRepresentante';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."IdPersonaRepresentante" = (CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END),
					PER."FechaInicioRepresentante"  = (CASE WHEN MOD."ValorNuevo"<>'' THEN CURRENT_UTCTIMESTAMP ELSE PER."FechaInicioRepresentante" END),					
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;		
			
			V_CampoModificado:='IdPaisNacionalidadPrincipal';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
						
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."IdPaisNacionalidadPrincipal" = (CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END),
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	
			
			V_CampoModificado:='GlobalLegalEntity';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
						
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."GlobalLegalEntity" = (CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END),
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	

			V_CampoModificado:='IdPersonaEmpleador';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
						
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."IdPersonaEmpleador" = (CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END),
					PER."FechaInicioActividad"  = (CASE WHEN MOD."ValorNuevo"<>'' THEN CURRENT_UTCTIMESTAMP ELSE PER."FechaInicioActividad" END),					
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	
								
			V_CampoModificado:='CodigoTipoCargoEmpleado';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
						
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."CodigoTipoCargoEmpleado" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	
			
			V_CampoModificado:='DetalleCargo';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
						
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."DetalleCargo" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	
			
			V_CampoModificado:='IdNombreFantasiaPrincipal';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
						
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."IdNombreFantasiaPrincipal" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;		
			
			V_CampoModificado:='IdRangoPatrimonio';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;
						
			IF (v_count_Tipo > 0) THEN

				UPDATE  PER
				SET
					PER."IdRangoPatrimonio" = CASE WHEN MOD."ValorNuevo" = '' THEN 0 ELSE MOD."ValorNuevo" END,
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;
			
			
			-- PERU --			
			V_CampoModificado:='IdGradoInstruccion';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;	
			IF (v_count_Tipo > 0) THEN
				UPDATE  PER
				SET
					PER."IdGradoInstruccion" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	
			
			--
			V_CampoModificado:='AgnosServicio';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;	
			IF (v_count_Tipo > 0) THEN
				UPDATE  PER
				SET
					PER."AgnosServicio" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	
			--
			V_CampoModificado:='IngresoPromedioMensual';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;	
			IF (v_count_Tipo > 0) THEN
				UPDATE  PER
				SET
					PER."IngresoPromedioMensual" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	
			--
			V_CampoModificado:='IdPaisResidenciaSecundaria';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;	
			IF (v_count_Tipo > 0) THEN
				UPDATE  PER
				SET
					PER."IdPaisResidenciaSecundaria" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;	
			--
			V_CampoModificado:='IdPaisNacionalidadSecundaria';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;	
			IF (v_count_Tipo > 0) THEN
				UPDATE  PER
				SET
					PER."IdPaisNacionalidadSecundaria" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;	
			--
			V_CampoModificado:='IdGiroCIIU';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
						AND "IdTipoEntidad"= v_TipoEntidad;	
			IF (v_count_Tipo > 0) THEN
				UPDATE  PER
				SET
					PER."IdGiroCIIU" = MOD."ValorNuevo",
					PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
					
				FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;					
			END IF;
												
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
				DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Solicitud de Cambio';
				
				
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
						MOD."ValorAntiguo" || '|'|| coalesce(TIPIND2."Nombre",'') as "ValorAntiguo",
						MOD."ValorNuevo"   || '|'|| coalesce(TIPIND."Nombre",'') as "ValorNuevo" 
				FROM :v_DatosSolicitudCambio MOD
				LEFT JOIN "BSCL.Plataforma.BD::REG.Profesion" AS TIPIND
				ON (TO_CHAR(TIPIND."IdProfesion") = MOD."ValorNuevo" and MOD."CampoModificado" = 'IdProfesion' )
				LEFT JOIN "BSCL.Plataforma.BD::REG.Profesion" AS TIPIND2
				ON (TO_CHAR(TIPIND2."IdProfesion") = MOD."ValorAntiguo" and MOD."CampoModificado" = 'IdProfesion' )
				); 

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