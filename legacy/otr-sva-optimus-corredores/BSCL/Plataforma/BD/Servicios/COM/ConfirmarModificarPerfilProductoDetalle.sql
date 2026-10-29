SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ConfirmarModificarPerfilProductoDetalle";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion al perfil de productos detalle.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 30-03-2016
--  
"BSCL.Plataforma.BD.Servicios::COM.ConfirmarModificarPerfilProductoDetalle" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	
	
	
	IN 	p_IdDetallePerfilProductos						BIGINT,	
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
			DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'PERFILPRODUCTODETALLE';
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_CountAtributoModificado						BIGINT:=0;
			DECLARE v_CountAtributoNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_IdTipoProducto					 		BIGINT;
			DECLARE v_count_IdTipoOperacion 						BIGINT;
			DECLARE v_count_IdCanalIngreso 							BIGINT;
			DECLARE v_count_IdMoneda								BIGINT;
			DECLARE v_count_IdGrupoActivos							BIGINT;
			DECLARE v_count_IdEstado								BIGINT;
			--LOG									
			DECLARE p_sysDate 			NVARCHAR(20);
			DECLARE p_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));

		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			v_IdEntidadOrigen:=CAST (p_IdDetallePerfilProductos AS NVARCHAR(40));
			
			SELECT COUNT(1) INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
			WHERE "IdEntidadOrigen"  	= v_IdEntidadOrigen
			 AND "IdTipoEntidad" 		= v_TipoEntidad
  			 AND "IdEstadoAutorizacion"	='PEN';
		
			IF (v_countSolicitud = 0) THEN
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
			v_DatosSolicitudCambio = SELECT * FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
			WHERE "IdEntidadOrigen"  	= v_IdEntidadOrigen
			 AND "IdTipoEntidad" 		= v_TipoEntidad
  			 AND "IdEstadoAutorizacion"	='PEN';
			
			SELECT 	COUNT("IdSolicitudCambio") INTO	v_CountAtributoModificado FROM	:v_DatosSolicitudCambio;
			
			IF (v_CountAtributoModificado > 0) 	 THEN
					--obtener valores antiguos para el log.
					v_valores = 
						SELECT DOC.* FROM "BSCL.Plataforma.BD::COM.DetallePerfilProductos" DOC 
						INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdDetallePerfilProductos"= MOD."IdEntidadOrigen");
						
					v_valores_antiguos = 
					SELECT DISTINCT "IdDetallePerfilProductos" AS "IdSecuencia", "Llave", "Valor"
					FROM (
						SELECT * ,
						COALESCE(
						"1" ||"IdDetallePerfilProductos", "2" ||"IdPerfilProductos", "3" ||"IdTipoProducto", "4" ||"IdTipoOperacion", "5" ||"IdCanalIngreso", "6" ||"IdMoneda",
						"7" ||"FechaCreacion", "8" ||"IdEstado", "9" ||"IdEmpresa", "10" ||"IdGrupoActivos", 
						"11" ||"FechaIngreso", "12" ||"FechaModificacion", "13" || "IdEstadoAutorizacion", "14" || "IdUsuarioCreador",
						"15" || "PermiteRuteo"
						) AS "Valor",
						        
						COALESCE(
						"1" ||'IdDetallePerfilProductos', "2" ||'IdPerfilProductos', "3" ||'IdTipoProducto', "4" ||'IdTipoOperacion', "5" ||'IdCanalIngreso', "6" ||'IdMoneda', 
						"7" ||'FechaCreacion', "8" ||'IdEstado', "9" ||'IdEmpresa', "10" ||'IdGrupoActivos', 
						"11" ||'FechaIngreso', "12" ||'FechaModificacion', "13" || 'IdEstadoAutorizacion', "14" || 'IdUsuarioCreador',
						"15" || "PermiteRuteo"
						) AS "Llave" 
						FROM :v_valores,"BSCL.Plataforma.BD::PLA.Identidad"
					) WHERE  "Valor" IS NOT NULL;
			
			
					v_CampoModificado:='IdTipoProducto';
					SELECT count("ValorNuevo") INTO v_count_IdTipoProducto
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdTipoProducto > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdTipoProducto" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Plataforma.BD::COM.DetallePerfilProductos" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdDetallePerfilProductos"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdDetallePerfilProductos"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;

					v_CampoModificado:='IdTipoOperacion';
					SELECT count("ValorNuevo") INTO v_count_IdTipoOperacion
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdTipoOperacion > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdTipoOperacion" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Plataforma.BD::COM.DetallePerfilProductos" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdDetallePerfilProductos"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdDetallePerfilProductos"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;

					v_CampoModificado:='IdCanalIngreso';
					SELECT count("ValorNuevo") INTO v_count_IdCanalIngreso
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdCanalIngreso > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdCanalIngreso" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Plataforma.BD::COM.DetallePerfilProductos" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdDetallePerfilProductos"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdDetallePerfilProductos"	= cast (MOD."IdEntidadOrigen" as int);
							
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
							
						FROM "BSCL.Plataforma.BD::COM.DetallePerfilProductos" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdDetallePerfilProductos"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdDetallePerfilProductos"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;

					v_CampoModificado:='IdGrupoActivos';
					SELECT count("ValorNuevo") INTO v_count_IdGrupoActivos
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdGrupoActivos > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdGrupoActivos" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Plataforma.BD::COM.DetallePerfilProductos" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdDetallePerfilProductos"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdDetallePerfilProductos"	= cast (MOD."IdEntidadOrigen" as int);
							
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
							
						FROM "BSCL.Plataforma.BD::COM.DetallePerfilProductos" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdDetallePerfilProductos"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdDetallePerfilProductos"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					v_CampoModificado:='PermiteRuteo';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					IF (v_count_IdEstado > 0) THEN
						UPDATE  DOC
						SET
							DOC."PermiteRuteo" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.Plataforma.BD::COM.DetallePerfilProductos" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdDetallePerfilProductos"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdDetallePerfilProductos"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					BEGIN
					--LOG
					--REEMPLAZAR POR LOG MASIVO							
						p_DetalleLog =  
						SELECT 
							va."IdSecuencia" as "SubRegistro", 
							va."Llave" as "CampoRegistro", 
							TO_VARCHAR(va."Valor") as "ValorAntiguo", 
							TO_VARCHAR(dsc."ValorNuevo") as "ValorNuevo" 
						FROM :v_valores_antiguos va
						INNER JOIN :v_DatosSolicitudCambio dsc on va."IdSecuencia" = dsc."IdEntidadOrigen" AND va."Llave" =dsc."CampoModificado" AND dsc."IdTipoEntidad" = v_TipoEntidad;
						
						SELECT CURRENT_DATE into p_sysDate from DUMMY;
					
						CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
							p_IdTipoLog => 'ACTUALIZACION' 
							,p_IdModulo => 'PLA' 
							,p_IdUsuario => p_IdUsuario 
							,p_IdEmpresa => p_IdEmpresa 
							,p_FechaSistemaLog => TO_VARCHAR (p_sysDate, 'YYYY-MM-DD')
							,p_IdUsuarioAtendido => p_IdUsuario
							,p_IdTipoRegistro => 'DETALLE PERFIL PRODUCTOS'
							,p_DetalleLog => :p_DetalleLog
						);
					
					END;
	
		
		
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

		--------------------
		--FIN SP 
		--------------------
	END;	
END;