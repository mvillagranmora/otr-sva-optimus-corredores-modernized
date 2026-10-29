SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarPersonaFatca";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion de datos FATCA de una Persona.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: JG - BCS
--  Fecha Creacion: 26-04-2016
--  
"BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarPersonaFatca" (
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
				 SOL."IdEntidadOrigen"  	= v_IdEntidadOrigen
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
			WHERE "IdEntidadOrigen"  		= v_IdEntidadOrigen
				AND "IdTipoEntidad" 		= v_TipoEntidad
  				AND "IdEstadoAutorizacion"	='PEN';
			
			
			--ACTUALIZACION DE VALORES

			V_CampoModificado:='IdClasificacionFatca';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."IdClasificacionFatca" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			
			V_CampoModificado:='IdClasificacionEmpresaFatca';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."IdClasificacionEmpresaFatca" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			
			V_CampoModificado:='NumeroGIIN';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."NumeroGIIN" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			
			V_CampoModificado:='FechaFirmaFormulario';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."FechaFirmaFormulario" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			
			V_CampoModificado:='AutorizacionInformarOrganismo';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."AutorizacionInformarOrganismo" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='NACEEUU';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."NACEEUU" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='NACEEUUExplicacion';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."NACEEUUExplicacion" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='EntidadFinanciera';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."EntidadFinanciera" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='TipoInstitucionFinanciera';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."TipoInstitucionFinanciera" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='EntidadGestionada';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."EntidadGestionada" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='IngresosPasivos';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."IngresosPasivos" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='GenerarIngresosPasivos';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."GenerarIngresosPasivos" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='CategoriaEntidad';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."CategoriaEntidad" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='GIINEntidad';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."GIINEntidad" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
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

				UPDATE  FAT
				SET
					FAT."RazonSocial" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='FechaLiquidacion';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."FechaLiquidacion" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='NombreMercado';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."NombreMercado" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='FechaConstitucion';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."FechaConstitucion" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='ObligacionesFiscales';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."ObligacionesFiscales" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='ObligacionesFiscalesExplicacion';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."ObligacionesFiscalesExplicacion" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='IndividuoControl';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."IndividuoControl" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='ExcepcionResidente';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."ExcepcionResidente" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='ParticipacionSociedadesUSA';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."ParticipacionSociedadesUSA" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='OrdenPermanente';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."OrdenPermanente" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			
			V_CampoModificado:='ExcepcionNacimiento';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."ExcepcionNacimiento" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			
			V_CampoModificado:='ExcepcionDomicilio';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."ExcepcionDomicilio" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			
			V_CampoModificado:='ExcepcionTelefono';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."ExcepcionTelefono" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			
			V_CampoModificado:='ExcepcionOrdenPermanente';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."ExcepcionOrdenPermanente" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			
			V_CampoModificado:='ExcepcionFirma';
			SELECT count("ValorNuevo") INTO v_count_Tipo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_Tipo > 0) THEN

				UPDATE  FAT
				SET
					FAT."ExcepcionFirma" = MOD."ValorNuevo",
					FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM 
					"BSCL.SocioNegocio.BD::PER.Fatca" FAT
					INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
						AND	MOD."IdTipoEntidad"= v_TipoEntidad;
				v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='IdPaisNacionalidad1';
            SELECT count("ValorNuevo") INTO v_count_Tipo
            FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
            AND "IdTipoEntidad"= v_TipoEntidad;
            
            IF (v_count_Tipo > 0) THEN

                UPDATE  PER
                SET
                    PER."IdPaisNacionalidadPrincipal" = MOD."ValorNuevo",
                    PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
                FROM 
                    "BSCL.SocioNegocio.BD::PER.Persona" PER
                    INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
                WHERE 
                        MOD."CampoModificado" = V_CampoModificado
                        AND MOD."IdTipoEntidad"= v_TipoEntidad;
                v_countProcesados:=v_countProcesados + 1;
            END IF;
            
            V_CampoModificado:='IdPaisNacionalidad2';
            SELECT count("ValorNuevo") INTO v_count_Tipo
            FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
            AND "IdTipoEntidad"= v_TipoEntidad;
            
            IF (v_count_Tipo > 0) THEN

                UPDATE  PER
                SET
                    PER."IdPaisNacionalidadSecundaria" = MOD."ValorNuevo",
                    PER."FechaModificacion" = CURRENT_UTCTIMESTAMP
                FROM 
                    "BSCL.SocioNegocio.BD::PER.Persona" PER
                    INNER JOIN :v_DatosSolicitudCambio MOD ON (PER."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
                WHERE 
                        MOD."CampoModificado" = V_CampoModificado
                        AND MOD."IdTipoEntidad"= v_TipoEntidad;
                v_countProcesados:=v_countProcesados + 1;
            END IF;
			
														
			--Se termina el flujo con la actualizacion del registro de solicitud.

			CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambio" (
				p_IdEmpresa					=>:p_IdEmpresa,
				p_IdUsuario					=>:p_IdUsuario,
				p_IdModulo					=>:p_IdModulo,
				p_IdEntidadOrigen			=>:v_IdEntidadOrigen,
				p_IdTipoEntidad				=>:v_TipoEntidad,
				p_IdEstadoAutorizacion		=>:p_IdEstadoAutorizacion,
				p_out_AdvertenciasContador	=>:p_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle	=>:p_out_AdvertenciasDetalle,	
				p_out_ErroresContador		=>:p_out_ErroresContador,
				p_out_ErroresDetalle		=>:p_out_ErroresDetalle	
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
						MOD."ValorAntiguo"  	 as "ValorAntiguo",
						MOD."ValorNuevo" 		 as "ValorNuevo" 
				FROM :v_DatosSolicitudCambio MOD);

				--GRABAR LOG

					CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
						 p_IdTipoLog 				=> 'MODIFICACION' 
						,p_IdModulo 				=> 'SOC' 
						,p_IdUsuario 				=> p_IdUsuario 
						,p_IdEmpresa 				=> p_IdEmpresa 
						,p_FechaSistemaLog 			=> TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
						,p_IdUsuarioAtendido 		=> p_IdUsuario
						,p_IdPersona 				=> p_IdPersona
						,p_IdTipoRegistro 			=> v_TipoEntidad--'CLIENTE'
						,p_DetalleLog 				=> :p_DetalleLog	-- hacia arriba obligatorios
						,p_NombreProceso 			=> :v_NombreProceso-- hacia abajo opcionales
						,p_IdRegistro 				=> :v_IdEntidadOrigen
						,p_EstadoProceso 			=> :p_IdEstadoAutorizacion
						,p_EstadoRegistro			=> :p_IdEstadoAutorizacion
						,p_IdTipoDocumentoIdentidad	=> :v_IdTipoDocumentoIdentidad
						,p_IdDocumentoIdentidad		=> :v_IdDocumentoIdentidad					
					);
	
			END;
			--LOG DE AUDITORIA (Fin)			
					
		END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;