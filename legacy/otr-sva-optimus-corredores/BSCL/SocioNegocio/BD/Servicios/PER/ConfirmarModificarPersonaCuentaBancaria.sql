SET SCHEMA "BSCL"; 

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarPersonaCuentaBancaria";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion a las cuentas de una persona.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 17-12-2015
--  
"BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarPersonaCuentaBancaria" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN 	p_IdCuentaBancaria							BIGINT,	
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
			DECLARE v_IdEntidadOrigen								NVARCHAR(40);
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'CUENTABANCARIA';
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_CountAtributoModificado						BIGINT:=0;
			DECLARE v_CountAtributoNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_IdBanco			 						BIGINT;
			DECLARE v_count_IdTipoCuentaBancaria					BIGINT;
			DECLARE v_count_IdMoneda								BIGINT;
			DECLARE v_count_NumeroCuentaBancaria					BIGINT;
			DECLARE v_count_IdEstado								BIGINT;
			DECLARE v_count_CodigoCuentaInterbancario			    BIGINT;
			DECLARE v_count_CodigoAba		    					BIGINT;
			DECLARE v_count_OtroCodigoBancoExtranjero		    	BIGINT;
			DECLARE v_count_CodigoIban		    					BIGINT;
			DECLARE v_count_OtroCodigoCuentaExtranjero		    	BIGINT;
			DECLARE v_count_EsPac									BIGINT;
			DECLARE v_count_EsFatca									BIGINT;
					
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			v_IdEntidadOrigen:=CAST (p_IdCuentaBancaria AS NVARCHAR(40));
			
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
			
			SELECT 	COUNT("IdSolicitudCambio") INTO	v_CountAtributoModificado FROM	:v_DatosSolicitudCambio;
			
			IF (v_CountAtributoModificado > 0) 	 THEN
			
			
			
					v_CampoModificado:='IdBanco';
					SELECT count("ValorNuevo") INTO v_count_IdBanco
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdBanco > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdBanco" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdPlaza';
					SELECT count("ValorNuevo") INTO v_count_IdBanco
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdBanco > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPlaza" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					
					v_CampoModificado:='IdPlaza';
					SELECT count("ValorNuevo") INTO v_count_IdBanco
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdBanco > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPlaza" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;

					v_CampoModificado:='IdTipoCuentaBancaria';
					SELECT count("ValorNuevo") INTO v_count_IdTipoCuentaBancaria
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdTipoCuentaBancaria > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdTipoCuentaBancaria" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;

					v_CampoModificado:='IdMoneda';
					SELECT count("ValorNuevo") INTO v_count_IdMoneda
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdMoneda > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdMoneda" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;

					v_CampoModificado:='NumeroCuentaBancaria';
					SELECT count("ValorNuevo") INTO v_count_NumeroCuentaBancaria
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_NumeroCuentaBancaria > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."NumeroCuentaBancaria" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;

					v_CampoModificado:='IdEstado';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdEstado > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdEstado" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdTipoIdentificadorCuenta';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdEstado > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdTipoIdentificadorCuenta" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdentificadorCuenta';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdEstado > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdentificadorCuenta" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdPersonaRelacionada';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdEstado > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPersonaRelacionada" = TO_BIGINT(MOD."ValorNuevo"),
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					
					v_CampoModificado:='CodigoCuentaInterbancario';
					SELECT count("ValorNuevo") INTO v_count_CodigoCuentaInterbancario
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_CodigoCuentaInterbancario > 0) THEN		
						UPDATE  DOC
						SET
							DOC."CodigoCuentaInterbancario" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;			
					
					
					
					v_CampoModificado:='CodigoAba';
					SELECT count("ValorNuevo") INTO v_count_CodigoAba
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_CodigoAba > 0) THEN		
						UPDATE  DOC
						SET
							DOC."CodigoAba" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;			
					
					
					
					v_CampoModificado:='OtroCodigoBancoExtranjero';
					SELECT count("ValorNuevo") INTO v_count_OtroCodigoBancoExtranjero
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_OtroCodigoBancoExtranjero > 0) THEN		
						UPDATE  DOC
						SET
							DOC."OtroCodigoBancoExtranjero" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;			
					

					
					v_CampoModificado:='CodigoIban';
					SELECT count("ValorNuevo") INTO v_count_CodigoIban
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_CodigoIban > 0) THEN		
						UPDATE  DOC
						SET
							DOC."CodigoIban" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;			
					
					
					v_CampoModificado:='OtroCodigoCuentaExtranjero';
					SELECT count("ValorNuevo") INTO v_count_OtroCodigoCuentaExtranjero
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_OtroCodigoCuentaExtranjero > 0) THEN		
						UPDATE  DOC
						SET
							DOC."OtroCodigoCuentaExtranjero" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;								
					
					v_CampoModificado:='EsPac';
					SELECT count("ValorNuevo") INTO v_count_EsPac
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_EsPac > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."EsPac" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					v_CampoModificado:='EsFatca';
					SELECT count("ValorNuevo") INTO v_count_EsFatca
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_EsFatca > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."EsFatca" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdCuentaBancaria"		= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
	
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
						
		END;
			--LOG DE AUDITORIA (inicio)
			BEGIN 
				DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
				DECLARE v_fechaHoraSistema NVARCHAR(40);
				DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Solicitud de Cambio';
				DECLARE v_id_persona BIGINT;
				DECLARE v_nombre_persona NVARCHAR(200)='';
				DECLARE v_documento_identidad NVARCHAR(40)='';
				DECLARE v_tipo_documento_identidad NVARCHAR(40)='';
				
				--DATOS CABECERA LOG
				call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);
				
				--DATOS DETALLE LOG
				p_DetalleLog = 
				(Select 
					MOD."IdTipoEntidad"		 as "SubRegistro",
					MOD."CampoModificado"	 as "CampoRegistro",
					CASE 
						WHEN MOD."CampoModificado" = 'IdBanco' AND CAST("BANCANT"."IdBanco" AS NVARCHAR) = COALESCE (MOD."ValorAntiguo", '') THEN "BANCANT"."NombreBanco" 
						WHEN MOD."CampoModificado" = 'IdPlaza' AND CAST("PLAZANT"."IdPlaza" AS NVARCHAR) = COALESCE(MOD."ValorAntiguo", '') THEN "PLAZANT"."NombrePlaza" 
						WHEN MOD."CampoModificado" = 'IdMoneda'AND ("MONEANT"."IdMoneda" = COALESCE(MOD."ValorAntiguo", '') OR "MONEANT"."Codigo" = COALESCE(MOD."ValorAntiguo", '')) THEN "MONEANT"."Nombre" 
						WHEN MOD."CampoModificado" = 'IdTipoCuentaBancaria' AND "TICUANT"."IdTipoCuentaBancaria" = COALESCE(MOD."ValorAntiguo", '') THEN "TICUANT"."NombreTipoCuentaBancaria" 
						ELSE MOD."ValorAntiguo"
					END AS "ValorAntiguo",
					CASE 
						WHEN MOD."CampoModificado" = 'IdBanco' AND CAST("BANCNUE"."IdBanco" AS NVARCHAR) = COALESCE (MOD."ValorNuevo", '') THEN "BANCNUE"."NombreBanco" 
						WHEN MOD."CampoModificado" = 'IdPlaza' AND CAST("PLAZNUE"."IdPlaza" AS NVARCHAR) = COALESCE(MOD."ValorNuevo", '') THEN "PLAZNUE"."NombrePlaza" 
						WHEN MOD."CampoModificado" = 'IdMoneda' AND ("MONENUE"."IdMoneda" = COALESCE(MOD."ValorNuevo", '') OR "MONENUE"."Codigo" = COALESCE(MOD."ValorNuevo", '')) THEN "MONENUE"."Nombre" 
						WHEN MOD."CampoModificado" = 'IdTipoCuentaBancaria' AND "TICUNUE"."IdTipoCuentaBancaria" = COALESCE(MOD."ValorNuevo", '') THEN "TICUNUE"."NombreTipoCuentaBancaria"
						ELSE MOD."ValorNuevo"
					END AS "ValorNuevo" 
						FROM :v_DatosSolicitudCambio MOD
						LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco"					AS "BANCANT"   	ON (CAST("BANCANT"."IdBanco" AS NVARCHAR) = COALESCE(MOD."ValorAntiguo", ''))
						LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco"					AS "BANCNUE"	ON (CAST("BANCNUE"."IdBanco" AS NVARCHAR) = COALESCE(MOD."ValorNuevo", ''))
						LEFT JOIN "BSCL.Plataforma.BD::BAN.Plaza"					AS "PLAZANT"	ON (CAST("PLAZANT"."IdPlaza" AS NVARCHAR) = COALESCE(MOD."ValorAntiguo", ''))
						LEFT JOIN "BSCL.Plataforma.BD::BAN.Plaza"					AS "PLAZNUE"	ON (CAST("PLAZNUE"."IdPlaza" AS NVARCHAR) = COALESCE(MOD."ValorNuevo", ''))
						LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda"					AS "MONEANT"	ON ("MONEANT"."IdMoneda" = COALESCE(MOD."ValorAntiguo", '') 
																										OR "MONEANT"."Codigo" = COALESCE(MOD."ValorAntiguo", ''))
						LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda"					AS "MONENUE"	ON ("MONENUE"."IdMoneda" = COALESCE(MOD."ValorNuevo", '') 
																										OR "MONENUE"."Codigo" = COALESCE(MOD."ValorNuevo", ''))
						LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" 		AS "TICUANT"	ON("TICUANT"."IdTipoCuentaBancaria" = COALESCE(MOD."ValorAntiguo", ''))
						LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" 		AS "TICUNUE"	ON("TICUNUE"."IdTipoCuentaBancaria" = COALESCE(MOD."ValorNuevo", ''))
				); 
				
				--GRABAR LOG
					SELECT "IdPersona" INTO v_id_persona FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" WHERE "IdCuentaBancaria" = :p_IdCuentaBancaria;
				
					SELECT 
						CASE WHEN LENGTH("NOM"."NombreCompleto") > 100 THEN LEFT(("NOM"."NombreCompleto"),100) ELSE "NOM"."NombreCompleto" END AS "Nombres",
						"di"."DocumentoIdentidad" AS "DocumentoIdentidad",
						"di"."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidad"
						INTO v_nombre_persona, v_documento_identidad, v_tipo_documento_identidad
							FROM "BSCL.SocioNegocio.BD::PER.Persona" "p"
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" "NOM" ON ("p"."IdPersona" = "NOM"."IdPersona")
							LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" "di" ON ("p"."IdDocumentoIdentidad" = "di"."IdDocumentoIdentidad")
								WHERE "p"."IdPersona" = v_id_persona;

					CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'MODIFICACION' 
					,p_IdModulo => 'SOC' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdPersona => :v_id_persona
					,p_NombreCliente => :v_nombre_persona
					,p_IdDocumentoIdentidad => :v_documento_identidad
					,p_IdTipoDocumentoIdentidad => :v_tipo_documento_identidad
					,p_IdTipoRegistro => v_TipoEntidad--'CLIENTE'
					,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => :v_IdEntidadOrigen
					,p_EstadoProceso => :p_IdEstadoAutorizacion
					);
	
			END;
			--LOG DE AUDITORIA (Fin)				
		--------------------
		--FIN SP 
		--------------------
	END;	
END;