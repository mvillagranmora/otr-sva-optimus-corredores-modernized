SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarPersonaFatcaMasivo";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion de datos FATCA de una Persona.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: JG - BCS
--  Fecha Creacion: 26-04-2016
--  
"BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarPersonaFatcaMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_Persona								TABLE ("IdPersona"	BIGINT),	
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
			p_out_ErroresDetalle = SELECT '' AS "Codigo", ::SQL_ERROR_MESSAGE AS "Descripcion" FROM dummy;
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
			--v_IdEntidadOrigen:=CAST (p_IdPersona AS NVARCHAR(40));

			SELECT COUNT("IdSolicitudCambio") INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_Persona P ON  CAST(P."IdPersona" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
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
			v_DatosSolicitudCambio = SELECT * 
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_Persona P ON  CAST(P."IdPersona" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
				AND SOL."IdTipoEntidad" 		= v_TipoEntidad
                AND SOL."IdEstadoAutorizacion"	='PEN';
			
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

                UPDATE  FAT
                SET
                    FAT."IdPaisNacionalidadPrincipal" = MOD."ValorNuevo",
                    FAT."FechaModificacion" = CURRENT_UTCTIMESTAMP
                FROM 
                    "BSCL.SocioNegocio.BD::PER.Persona" FAT
                    INNER JOIN :v_DatosSolicitudCambio MOD ON (FAT."IdPersona"= cast (MOD."IdEntidadOrigen" as int))
                WHERE 
                        MOD."CampoModificado" = V_CampoModificado
                        AND MOD."IdTipoEntidad"= v_TipoEntidad;
                v_countProcesados:=v_countProcesados + 1;
            END IF;
			
			
			
														
			--Se termina el flujo con la actualizacion del registro de solicitud.

			v_SolicitudCambio = SELECT 0 as "IdSolicitudCambio" FROM dummy where 1=2;
			v_EntidadOrigen = SELECT "IdEntidadOrigen", "IdTipoEntidad" FROM :v_DatosSolicitudCambio;
			
			CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambioMasivo" (
				p_IdEmpresa					=>:p_IdEmpresa,
				p_IdUsuario					=>:p_IdUsuario,
				p_IdModulo					=>:p_IdModulo,
				p_SolicitudCambio			=>:v_SolicitudCambio,
				p_EntidadOrigen			    =>:v_EntidadOrigen,
				--p_TipoEntidad				=>:v_TipoEntidad,
				p_IdEstadoAutorizacion		=>:p_IdEstadoAutorizacion,
				p_out_AdvertenciasContador	=>:p_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle	=>:p_out_AdvertenciasDetalle,	
				p_out_ErroresContador		=>:p_out_ErroresContador,
				p_out_ErroresDetalle		=>:p_out_ErroresDetalle	
			);
						
			--LOG DE AUDITORIA (inicio)
			BEGIN 
				DECLARE v_fechaHoraSistema NVARCHAR(40);
				DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Solicitud de Cambio';
				
				call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);
				
				v_id_persona = SELECT "IdPersona", "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL AS "IdLog" FROM :p_Persona;
				
				--DATOS DETALLE LOG
				v_DetalleLog = 
				(Select MOD."IdTipoEntidad"		 as "SubRegistro",
						MOD."CampoModificado"	 as "CampoRegistro",
						MOD."ValorAntiguo"  	 as "ValorAntiguo",
						MOD."ValorNuevo" 		 as "ValorNuevo" 
				FROM :v_DatosSolicitudCambio MOD
				INNER JOIN :v_id_persona AS PER ON (PER."IdPersona" = cast(MOD."IdEntidadOrigen" as int)));

				v_log =  SELECT P."IdLog" AS "IdCabeceraLog"
							   ,'MODIFICACION' AS "IdTipoLog"
							   ,'SOC' AS "IdModulo"
							   ,p_IdUsuario AS "IdUsuario"
							   ,p_IdEmpresa AS "IdEmpresa"
							   ,TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') AS "FechaSistemaLog"
							   ,p_IdUsuario AS "IdUsuarioAtendido"
							   ,PER."IdPersona"
							   ,0 AS "IdCliente"
							   ,'' AS "EstadoCliente"
							   ,'' AS "NombreCliente" 
							   ,PER."TipoDocumentoIdentidad" as "IdTipoDocumentoIdentidad"
							   ,PER."IdDocumentoIdentidad"
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
						 FROM :v_id_persona AS P   
 						 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."IdPersona" = P."IdPersona");

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