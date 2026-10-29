SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::EVT.ConfirmarModificacionPreferencias";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion al indicador de una entidad.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 07-04-2016
--  
"BSCL.Plataforma.BD.Servicios::EVT.ConfirmarModificacionPreferencias" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico		
	IN 	p_IdSolicitudCambio							TABLE ("IdSolicitudCambio" BIGINT),	
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
			DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
			DECLARE v_CountIndicadorModificado						BIGINT:=0;
			DECLARE v_CountIndicadorNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_countFechaVencimiento							BIGINT:=0;
			DECLARE v_IdEntidad										BIGINT:=-1;
			DECLARE v_TipoEntidad									NVARCHAR(3);
			DECLARE v_IdEstadoAutorizacion							NVARCHAR(3);
			
			DECLARE v_IdSolicitudCambio								BIGINT;
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			
			SELECT COUNT(1) INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
			WHERE "IdSolicitudCambio" IN (SELECT "IdSolicitudCambio" FROM :p_IdSolicitudCambio);
		
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
			
			DECLARE CURSOR c_solicitudes FOR SELECT "IdSolicitudCambio" FROM :p_IdSolicitudCambio;
			
			FOR cur_row as c_solicitudes DO
				v_IdSolicitudCambio := cur_row."IdSolicitudCambio";
			BEGIN	
				
				v_DatosSolicitudCambio = SELECT * FROM  "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" WHERE "IdSolicitudCambio" = v_IdSolicitudCambio;
			
				p_out_ErroresDetalle =  select "ValorNuevo" as "Codigo", "IdSolicitudCambio" as "Descripcion" from :v_DatosSolicitudCambio;
				
				SELECT COUNT(1) INTO v_countSolicitud FROM :v_DatosSolicitudCambio;
				IF :v_countSolicitud > 0 THEN 
				
					SELECT COALESCE(TO_INTEGER("ValorNuevo"),0) INTO v_IdEntidad 
					FROM :v_DatosSolicitudCambio
					WHERE "IdSolicitudCambio" = :v_IdSolicitudCambio and "CampoModificado" = 'IdEntidad';
	
					SELECT COALESCE("ValorNuevo",'') INTO v_TipoEntidad 
					FROM :v_DatosSolicitudCambio
					WHERE "IdSolicitudCambio" = :v_IdSolicitudCambio and "CampoModificado" = 'TipoEntidad';
					
					UPDATE pnot 
					SET	
						pnot."IdEstado" =  csc."ValorNuevo"
					FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" pnot
						INNER JOIN :v_DatosSolicitudCambio csc
						ON "IdSolicitudCambio" = v_IdSolicitudCambio
					WHERE TO_INTEGER("IdEntidadOrigen") = "IdConfiguracionEvento" and "IdEntidad" = v_IdEntidad AND "TipoEntidad" = v_TipoEntidad;
					
					UPDATE pnot 
					SET	
						pnot."FechaTerminoSuspension" = csc."ValorNuevo"
		 			FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" pnot
						INNER JOIN :v_DatosSolicitudCambio csc
						ON "IdSolicitudCambio" = v_IdSolicitudCambio
					WHERE TO_INTEGER("IdEntidadOrigen") = "IdEntidad" and "CampoModificado" = 'FechaTerminoSuspension' and pnot."TipoEntidad" = v_TipoEntidad;
					
					UPDATE pnot 
					SET	
		 				pnot."FechaInicioSuspension" = csc."ValorNuevo"
		 			FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" pnot
						INNER JOIN :v_DatosSolicitudCambio csc
						ON "IdSolicitudCambio" = v_IdSolicitudCambio
					WHERE TO_INTEGER("IdEntidadOrigen") = "IdEntidad" and "CampoModificado" = 'FechaInicioSuspension' and pnot."TipoEntidad" = v_TipoEntidad;
					
					UPDATE pnot 
					SET	
		  				pnot."SuspensionTemporal" =  csc."ValorNuevo"
					FROM "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" pnot
						INNER JOIN :v_DatosSolicitudCambio csc
						ON "IdSolicitudCambio" = v_IdSolicitudCambio
					WHERE TO_INTEGER("IdEntidadOrigen") = "IdEntidad" and "CampoModificado" = 'SuspensionTemporal' and pnot."TipoEntidad" = v_TipoEntidad;
					
					v_IdEstadoAutorizacion := 'AUT';
				END IF;
				
				--Se termina el flujo con la actualizacion del registro de solicitud.
				
				CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambio" (
					p_IdEmpresa				=>:p_IdEmpresa,
					p_IdUsuario				=>:p_IdUsuario,
					p_IdModulo				=>:p_IdModulo,
					p_IdSolicitudCambio		=>:v_IdSolicitudCambio,
					p_IdEstadoAutorizacion	=>:v_IdEstadoAutorizacion,
					p_out_AdvertenciasContador		=>:p_out_AdvertenciasContador,
					p_out_AdvertenciasDetalle		=>:p_out_AdvertenciasDetalle,	
					p_out_ErroresContador			=>:p_out_ErroresContador,
					p_out_ErroresDetalle			=>:p_out_ErroresDetalle
				);
			
			END;
			
			--LOG DE AUDITORIA (inicio)
			BEGIN 
				DECLARE v_DetalleLog 						TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
				DECLARE	v_TipoEntidadBase					NVARCHAR(40);
				DECLARE	v_IdEntidadBase						NVARCHAR(40);
				DECLARE v_fechaHoraSistema 					NVARCHAR(40);
				DECLARE v_NombreProceso						NVARCHAR(100):='Confirmar Cambio Preferencias Notificacion';
				DECLARE v_IdCliente							BIGINT:=0;
				DECLARE v_EstadoCliente						NVARCHAR(5):='';
				DECLARE	v_NombreCliente						NVARCHAR(100):='';
				DECLARE	v_IdDocumentoIdentidad				NVARCHAR(40):='';
				DECLARE	v_IdTipoDocumentoIdentidad			NVARCHAR(40):='';
				DECLARE v_IdPortafolio						BIGINT:=0;
				DECLARE v_EstadoPortafolio					NVARCHAR(5):='';
				--variables para ir a buscar Portafolio, si corresponde
				DECLARE v_datosEntidadOrigen 				TABLE ("IdEntidadOrigen" nvarchar(40), "Valor" nvarchar(40));
				DECLARE v_countdatosEntidadOrigen			BIGINT;
				DECLARE v_TipoEntidadOrigen					NVARCHAR(40):='';
				DECLARE v_IdEntidadOrigen					NVARCHAR(40):='0';
				
				v_TipoEntidadBase := 'NOTIFICADOR';

				--DATOS CABECERA LOG
				--PREPARANDONOS PARA ENCONTRAR PORTAFOLIO
				v_datosEntidadOrigen = (SELECT 
											"IdEntidadOrigen"		AS "IdEntidadOrigen",
											CASE 
												WHEN "ValorNuevo" = 'POR' OR "ValorNuevo" = 'PORTAFOLIO' THEN 'POR'
												WHEN "ValorNuevo" = 'CLI' OR "ValorNuevo" = 'PORTAFOLIO' THEN 'CLI'
												ELSE "ValorNuevo"
											END AS "Valor"
												FROM :v_DatosSolicitudCambio
												WHERE "IdSolicitudCambio" = :v_IdSolicitudCambio
														AND ("CampoModificado" = 'SuspensionTemporal' 
														OR (("ValorNuevo" = 'POR' OR "ValorNuevo" = 'PORTAFOLIO') OR ("ValorNuevo" = 'CLI' OR "ValorNuevo" = 'CLIENTE')))
										);
									
				SELECT COUNT(*) INTO v_countdatosEntidadOrigen FROM :v_datosEntidadOrigen;
				
				IF (v_countdatosEntidadOrigen = 2) THEN
					
					SELECT "Valor" INTO v_TipoEntidadOrigen  FROM :v_datosEntidadOrigen WHERE "Valor" IN ('POR','CLI');
					SELECT COALESCE("IdEntidadOrigen", 0) INTO v_IdEntidadOrigen FROM :v_datosEntidadOrigen WHERE "Valor" NOT IN ('POR','CLI');
					
					IF(v_TipoEntidadOrigen = 'CLI') THEN 
					
						SELECT  
							"CLI"."IdCliente",									
							COALESCE("CLI"."NombreCompleto",''),					
							"CLI"."DocumentoIdentidad",
							"CLI"."TipoDocumentoIdentidad",
							"CLI"."IdEstado"
						INTO 
							v_IdCliente,
							v_NombreCliente,
							v_IdDocumentoIdentidad,
							v_IdTipoDocumentoIdentidad,
							v_EstadoCliente
								FROM  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" "CLI" WHERE "IdCliente" = :v_IdEntidadOrigen;
							
						IF(v_IdCliente <> 0) THEN
						
							v_TipoEntidadBase:=v_TipoEntidadBase||'CLIENTE';	
						
						END IF;
					
					END IF;
				
					IF(v_TipoEntidadOrigen = 'POR') THEN 
					
						SELECT 
							"CLI"."IdCliente",									
							COALESCE("CLI"."NombreCompleto",''),					
							"CLI"."DocumentoIdentidad",
							"CLI"."TipoDocumentoIdentidad",
							"CLI"."IdEstado", 
							"PORT"."IdPortafolio",
							"PORT"."IdEstado"
						INTO 
							v_IdCliente,
							v_NombreCliente,
							v_IdDocumentoIdentidad,
							v_IdTipoDocumentoIdentidad,
							v_EstadoCliente,
							v_IdPortafolio,
							v_EstadoPortafolio
								FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" "PORT"
								LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" "CLI"
									ON ("PORT"."IdCliente" = "CLI"."IdCliente")
								WHERE "IdPortafolio" = :v_IdEntidadOrigen;
							
						IF(v_IdPortafolio <> 0) THEN
						
							v_TipoEntidadBase:=v_TipoEntidadBase||'PORTAFOLIO';	
						
						END IF;
					
					END IF;
				
				END IF;
			
				IF(v_IdEntidadOrigen <> 0 )  THEN 

					v_DetalleLog = (SELECT 
										DISTINCT 
										MOD."IdTipoEntidad"		 	AS "SubRegistro",
										CASE 
											WHEN COALESCE("CONFEVENT"."Descripcion", '') != '' THEN "CONFEVENT"."Descripcion"
											ELSE MOD."CampoModificado"	
										END AS "CampoRegistro",
										MOD."ValorAntiguo"  	 	AS "ValorAntiguo",
										MOD."ValorNuevo" 			AS "ValorNuevo"
											FROM :v_DatosSolicitudCambio MOD 
											INNER JOIN "BSCL.Plataforma.BD::EVT.PersonalizacionEvento" "EVEN"
												ON ("EVEN"."IdEntidad" = :v_IdEntidadOrigen AND MOD."IdEntidadOrigen" = "EVEN"."IdConfiguracionEvento")
											INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos" "CONFEVENT"
												ON("CONFEVENT"."IdEmpresa" = :p_IdEmpresa AND MOD."IdEntidadOrigen" = CAST("CONFEVENT"."IdConfiguracionEvento" AS NVARCHAR(40) ))
											WHERE MOD."CampoModificado" = 'IdEstado'
									);
					ELSE 
						
						v_DetalleLog = (Select MOD."IdTipoEntidad"		 as "SubRegistro",
											MOD."CampoModificado"	 as "CampoRegistro",
											MOD."ValorAntiguo"  	 as "ValorAntiguo",
											MOD."ValorNuevo" 		 as "ValorNuevo" 
												FROM :v_DatosSolicitudCambio MOD
										);
					
				END IF;
			
			
			
				
				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);

				--GRABAR LOG
				CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'MODIFICACION' 
					,p_IdModulo => 'EVT' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => v_TipoEntidadBase--'CLIENTE'
					,p_DetalleLog => :v_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => :v_IdEntidadBase
					,p_EstadoProceso => :p_IdEstadoAutorizacion
					,p_EstadoRegistro=> :p_IdEstadoAutorizacion
					,p_IdProceso => :v_IdSolicitudCambio
					,p_IdPortafolio => :v_IdPortafolio
					,p_EstadoPortafolio => :v_EstadoPortafolio
					,p_IdCliente => :v_IdCliente
					,p_NombreCliente => :v_NombreCliente
					,p_IdTipoDocumentoIdentidad => :v_IdTipoDocumentoIdentidad
					,p_IdDocumentoIdentidad => :v_IdDocumentoIdentidad
					,p_EstadoCliente => :v_EstadoCliente							
				);
			
			END;
			--LOG DE AUDITORIA (Fin)
		
			END FOR;
		END;
		--------------------
		--FIN SP 
		--------------------
		
	END;	
	
END;