SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::EMP.ConfirmarModificarEmpleadoDatosGenerales";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion a un empleado.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 22-12-2015
--
--  Modificacion: JGARCES - BCS
--  Fecha: 02-02-2015
-- 	Descripcion: Se incluye utilización de plantilla de empleado.
--  
"BSCL.SocioNegocio.BD.Servicios::EMP.ConfirmarModificarEmpleadoDatosGenerales" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdEmpleado									BIGINT,	
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
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'EMPLEADO';
			DECLARE V_CampoModificado								NVARCHAR(255):='';
			DECLARE v_countProcesados								BIGINT:=0;
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_IdEstado			 					BIGINT;
			DECLARE v_count_CodigoEmpleado	 						BIGINT;
			DECLARE v_count_CodigoTipoCargoEmpleado 				BIGINT;
			DECLARE v_count_IdEmpleadoSupervisor					BIGINT;
			DECLARE v_count_IdSucursal								BIGINT;
			DECLARE v_count_UtilizaPlantillaEmpleado				BIGINT;
			DECLARE v_count_IdPlantillaEmpleado						BIGINT;
			DECLARE v_count_FechaInicioCargo						BIGINT;
			DECLARE v_count_FechaTerminoCargo						BIGINT;
			DECLARE v_count_DetalleCargo							BIGINT;			
			DECLARE v_count_IdDireccionElectronica					BIGINT;
			DECLARE v_count_RestringeCarteraCliente					BIGINT;
			DECLARE v_count_RestringeOrdenCliente                   BIGINT;
	
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.		
		BEGIN
			v_IdEntidadOrigen:=CAST (p_IdEmpleado AS NVARCHAR(40));
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
			
			V_CampoModificado:='IdEstado';
			SELECT count("ValorNuevo") INTO v_count_IdEstado
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_IdEstado > 0) THEN

				UPDATE  EMP
				SET
					EMP."IdEstado" = MOD."ValorNuevo",
					EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
						INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
	
			V_CampoModificado:='CodigoEmpleado';
			SELECT count("ValorNuevo") INTO v_count_CodigoEmpleado
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_CodigoEmpleado > 0) THEN

				UPDATE  EMP
				SET
					EMP."CodigoEmpleado" = MOD."ValorNuevo",
					EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
						INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
	
			V_CampoModificado:='IdDireccionElectronica';
			SELECT count("ValorNuevo") INTO v_count_IdDireccionElectronica
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_IdDireccionElectronica > 0) THEN

				UPDATE  EMP
				SET
					EMP."IdDireccionElectronica" = MOD."ValorNuevo",
					EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
						INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
			
	
			V_CampoModificado:='CodigoTipoCargoEmpleado';
			SELECT count("ValorNuevo") INTO v_count_CodigoTipoCargoEmpleado
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_CodigoTipoCargoEmpleado > 0) THEN

				UPDATE  EMP
				SET
					EMP."CodigoTipoCargoEmpleado" = MOD."ValorNuevo",
					EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
						INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
	
			V_CampoModificado:='IdEmpleadoSupervisor';
			SELECT count("ValorNuevo") INTO v_count_IdEmpleadoSupervisor
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_IdEmpleadoSupervisor > 0) THEN

				UPDATE  EMP
				SET
					EMP."IdEmpleadoSupervisor" = CASE WHEN MOD."ValorNuevo" = '' THEN NULL ELSE MOD."ValorNuevo" END,
					EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
						INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
	
			V_CampoModificado:='IdSucursal';
			SELECT count("ValorNuevo") INTO v_count_IdSucursal
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_IdSucursal > 0) THEN

				UPDATE  EMP
				SET
					EMP."IdSucursal" = CASE WHEN MOD."ValorNuevo"='' THEN NULL ELSE MOD."ValorNuevo" END,
					EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
						INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			
			V_CampoModificado:='UtilizaPlantillaEmpleado';
			SELECT count("ValorNuevo") INTO v_count_UtilizaPlantillaEmpleado
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_UtilizaPlantillaEmpleado > 0) THEN

				UPDATE  EMP
				SET
					EMP."UtilizaPlantillaEmpleado" = MOD."ValorNuevo",
					EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
						INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			
			V_CampoModificado:='IdPlantillaEmpleado';
			SELECT count("ValorNuevo") INTO v_count_IdPlantillaEmpleado
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_IdPlantillaEmpleado > 0) THEN

				UPDATE  EMP
				SET
					EMP."IdPlantillaEmpleado" = MOD."ValorNuevo",
					EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
						INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
			
	
			V_CampoModificado:='FechaInicioCargo';
			SELECT count("ValorNuevo") INTO v_count_FechaInicioCargo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_FechaInicioCargo > 0) THEN

				UPDATE  EMP
				SET
					EMP."FechaInicioCargo" = MOD."ValorNuevo",
					EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
						INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;


			V_CampoModificado:='FechaTerminoCargo';
			SELECT count("ValorNuevo") INTO v_count_FechaTerminoCargo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_FechaTerminoCargo > 0) THEN

				UPDATE  EMP
				SET
					EMP."FechaTerminoCargo" = MOD."ValorNuevo",
					EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
						INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
	
			V_CampoModificado:='DetalleCargo';
			SELECT count("ValorNuevo") INTO v_count_DetalleCargo
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_DetalleCargo > 0) THEN

				UPDATE  EMP
				SET
					EMP."DetalleCargo" = MOD."ValorNuevo",
					EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
						INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='CargoIndefinido';
			SELECT count("ValorNuevo") INTO v_count_UtilizaPlantillaEmpleado
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_UtilizaPlantillaEmpleado > 0) THEN

				UPDATE  EMP
				SET
					EMP."CargoIndefinido" = MOD."ValorNuevo",
					EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
						INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='RestringeCarteraCliente';
			SELECT count("ValorNuevo") INTO v_count_RestringeCarteraCliente
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
			AND "IdTipoEntidad"= v_TipoEntidad;
			
			IF (v_count_RestringeCarteraCliente > 0) THEN

				UPDATE  EMP
				SET
					EMP."RestringeCarteraCliente" = MOD."ValorNuevo",
					EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
						INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."CampoModificado" = V_CampoModificado
					AND	MOD."IdTipoEntidad"= v_TipoEntidad;
					v_countProcesados:=v_countProcesados + 1;
			END IF;
			
			V_CampoModificado:='RestringeOrdenCliente';
            SELECT count("ValorNuevo") INTO v_count_RestringeOrdenCliente
            FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = V_CampoModificado
            AND "IdTipoEntidad"= v_TipoEntidad;
            
            IF (v_count_RestringeOrdenCliente > 0) THEN

                UPDATE  EMP
                SET
                    EMP."RestringeOrdenCliente" = MOD."ValorNuevo",
                    EMP."FechaModificacion" = CURRENT_UTCTIMESTAMP
                FROM "BSCL.SocioNegocio.BD::EMP.Empleado" EMP
                        INNER JOIN :v_DatosSolicitudCambio MOD ON (EMP."IdEmpleado"= cast (MOD."IdEntidadOrigen" as int))
                WHERE 
                        MOD."CampoModificado" = V_CampoModificado
                    AND MOD."IdTipoEntidad"= v_TipoEntidad;
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
				FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" WHERE "IdEmpleado"=v_IdEntidadOrigen;
	
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
GO