SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::LIM.ConfirmarModificarLimite";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion a un limite.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 27-04-2016
--  
"BSCL.Plataforma.BD.Servicios::LIM.ConfirmarModificarLimite" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN  p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico

	IN 	p_Limite						TABLE ("IdLimite"	BIGINT),		
	IN  p_IdEstadoAutorizacion			NVARCHAR(3),
	
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
			DECLARE v_countSolicitud				BIGINT:=0;
			DECLARE v_countAutorizacionesPendientes	BIGINT;
			DECLARE v_IdEntidadOrigen				NVARCHAR(40);
			DECLARE v_TipoEntidad					NVARCHAR(40):= 'LIMITE';
			DECLARE v_CampoModificado				NVARCHAR(255):='';
			DECLARE v_CountAtributoModificado		BIGINT:=0;
			DECLARE v_CountAtributoNuevo			BIGINT:=0;
			DECLARE v_countProcesados				BIGINT:=0;
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_Campo					BIGINT;
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			--v_IdEntidadOrigen:=CAST (p_IdLimite AS NVARCHAR(40));
			SELECT COUNT("IdSolicitudCambio") INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_Limite P ON  CAST(P."IdLimite" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
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
			UPDATE LIM
			SET "IdEstadoAutorizacion" = p_IdEstadoAutorizacion
			FROM "BSCL.Plataforma.BD::LIM.Limite" LIM
				JOIN :p_Limite L ON L."IdLimite" = LIM."IdLimite"
			WHERE LIM."IdEstadoAutorizacion" = 'RZD';
									
			--DATOS MAESTROS DE LA ENTIDAD 
			v_DatosSolicitudCambio = SELECT * 
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_Limite P ON  CAST(P."IdLimite" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
				AND SOL."IdTipoEntidad" 		= v_TipoEntidad
                AND SOL."IdEstadoAutorizacion"	='PEN';
			
			SELECT COUNT("IdSolicitudCambio") INTO	v_CountAtributoModificado FROM :v_DatosSolicitudCambio;
			
			IF (v_CountAtributoModificado > 0) THEN
				
				v_CampoModificado:='Nombre';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE DOC
					SET
						DOC."Nombre" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='IdMesa';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdMesa" = (CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END),
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;

		
				v_CampoModificado:='IdCanalIngreso';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdCanalIngreso" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='IdTipoInstrumento';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdTipoInstrumento" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='IdTipoOperacion';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdTipoOperacion" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='IdTipoProducto';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdTipoProducto" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
					
		
				v_CampoModificado:='IdInstrumento';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdInstrumento" = (CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END),
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='UsaPlazo';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
				UPDATE  DOC
				SET
					DOC."UsaPlazo" = MOD."ValorNuevo",
					DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
									INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
				WHERE
					MOD."CampoModificado" 	= v_CampoModificado
					AND	MOD."IdTipoEntidad"	= v_TipoEntidad
					AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
				
				v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='UnidadPlazo';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."UnidadPlazo" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='Plazo';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."Plazo" = (CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END),
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
								

				v_CampoModificado:='IdTipoReajuste';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdTipoReajuste" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
							
							
				v_CampoModificado:='CodigoOperador';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."CodigoOperador" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='MontoTotal';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."MontoTotal" =(CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE CAST(MOD."ValorNuevo" AS DECIMAL (26,8)) END),
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
																	

				v_CampoModificado:='MontoIntraday';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."MontoIntraday" = (CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE CAST(MOD."ValorNuevo" AS DECIMAL (26,8)) END),
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='MontoOvernight';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."MontoOvernight" = (CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE CAST(MOD."ValorNuevo" AS DECIMAL (26,8)) END),
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='IdMoneda';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdMoneda" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='IdMonedaCalculo';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdMonedaCalculo" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='ClasificacionRiesgoMinima';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."ClasificacionRiesgoMinima" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='IdEstado';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdEstado" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='EsPersonalizada';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."EsPersonalizada" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='FechaVigencia';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."FechaVigencia" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='EsTotalizador';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."EsTotalizador" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='IdClasificacionRiesgo';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdClasificacionRiesgo" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='IdAgente';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdAgente" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='IdClasificacionTipoInstrumento';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdClasificacionTipoInstrumento" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='Porcentaje';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."Porcentaje" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='IdGrupoEmisor';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdGrupoEmisor" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
				v_CampoModificado:='IdEmisor';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."IdEmisor" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				v_CampoModificado:='IdTipoComportamiento';
                SELECT count("ValorNuevo") INTO v_count_Campo
                FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
                AND "IdTipoEntidad"= v_TipoEntidad;
                
                IF (v_count_Campo > 0) THEN
                    UPDATE  DOC
                    SET
                        DOC."IdTipoComportamiento" = MOD."ValorNuevo",
                        DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
                    FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
                    INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
                    WHERE 
                        MOD."CampoModificado"   = v_CampoModificado
                        AND MOD."IdTipoEntidad" = v_TipoEntidad
                        AND DOC."IdLimite"      = cast (MOD."IdEntidadOrigen" as int);
                        
                        v_countProcesados:=v_countProcesados + 1;
                END IF;
                
                v_CampoModificado:='MargenInferior';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."MargenInferior" =(CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE CAST(MOD."ValorNuevo" AS DECIMAL (26,8)) END),
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				v_CampoModificado:='MargenSuperior';
				SELECT count("ValorNuevo") INTO v_count_Campo
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_count_Campo > 0) THEN
					UPDATE  DOC
					SET
						DOC."MargenSuperior" =(CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE CAST(MOD."ValorNuevo" AS DECIMAL (26,8)) END),
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::LIM.Limite" DOC
					INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdLimite"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 	= v_CampoModificado
						AND	MOD."IdTipoEntidad"	= v_TipoEntidad
						AND DOC."IdLimite"		= cast (MOD."IdEntidadOrigen" as int);
						
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				
			END IF;	
			--Se termina el flujo con la actualizacion del registro de solicitud.
			v_SolicitudCambio = SELECT 0 as "IdSolicitudCambio" FROM dummy where 1=2;
			v_EntidadOrigen = SELECT "IdEntidadOrigen", "IdTipoEntidad" FROM :v_DatosSolicitudCambio;
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
						
		END;
		
		--LOG DE AUDITORIA (inicio)
		BEGIN 
			DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
			DECLARE v_fechaHoraSistema NVARCHAR(40);
			DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Solicitud de Cambio';
			
			--DATOS CABECERA LOG
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
				,p_IdModulo => p_IdModulo 
				,p_IdUsuario => p_IdUsuario 
				,p_IdEmpresa => p_IdEmpresa 
				,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
				,p_IdUsuarioAtendido => p_IdUsuario
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