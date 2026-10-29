SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::CIR.ConfirmarModificarParametro";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion al parametro de la circularizacion.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 26-09-2016
--  
"BSCL.Normativa.BD.Servicios::CIR.ConfirmarModificarParametro" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	
	
	
	IN 	p_SolicitudCambio						TABLE ("IdSolicitudCambio"	BIGINT),	
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

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
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

	BEGIN	
		--------------------
		--Declare Variables
		--------------------
			DECLARE v_countSolicitud,v_IdParametro							BIGINT:=0;
			DECLARE v_countAutorizacionesPendientes					BIGINT;
			DECLARE v_IdEstadoAutorizacion_Calculado				NVARCHAR(3);
			DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'CIRCULARIZACION-PARAMETROS';
			DECLARE v_CountIndicadorModificado						BIGINT:=0;
			DECLARE v_CountIndicadorNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_countFechaVencimiento							BIGINT:=0;
			DECLARE v_Esquema,v_Version								NVARCHAR(10):='';
			DECLARE v_Periodo										NVARCHAR(6):='';
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			
			SELECT COUNT(1) INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_SolicitudCambio S ON S."IdSolicitudCambio" = SOL."IdSolicitudCambio";
		
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
			v_DatosSolicitudCambio = SELECT SOL.* FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_SolicitudCambio S ON S."IdSolicitudCambio" = SOL."IdSolicitudCambio";

			SELECT 	COUNT(1) INTO	v_CountIndicadorModificado FROM	:v_DatosSolicitudCambio;
			
			IF (v_CountIndicadorModificado > 0) 	 THEN
			
			--Identifica el periodo de la solicitud
		
				SELECT TOP 1 ATR."Periodo" INTO v_Periodo 
				FROM "BSCL.Normativa.BD::CIR.Parametro" ATR
				INNER JOIN :v_DatosSolicitudCambio MOD
				ON (ATR."IdParametro"			= cast (MOD."IdEntidadOrigen" as int));
			
			
				UPDATE  ATR
				SET	ATR."IdEstado" ='INA',
					ATR."FechaModificacion" = CURRENT_UTCTIMESTAMP,
					ATR."IdEstadoAutorizacion" =p_IdEstadoAutorizacion
				FROM "BSCL.Normativa.BD::CIR.Parametro" ATR
				WHERE 
   					    ATR."Periodo"			 	= v_Periodo								
					AND ATR."IdEstadoAutorizacion"	<> 'PEN'	
					AND ATR."Codigo" <>'RUTMAS';
					
				UPDATE  ATR
				SET ATR."IdEstado" ='ACT',
					ATR."Valor" = MOD."ValorNuevo",
					ATR."FechaModificacion" = CURRENT_UTCTIMESTAMP,
					ATR."IdEstadoAutorizacion" =p_IdEstadoAutorizacion
				FROM "BSCL.Normativa.BD::CIR.Parametro" ATR
				INNER JOIN :v_DatosSolicitudCambio MOD ON (ATR."IdParametro"= cast (MOD."IdEntidadOrigen" as int))
				INNER JOIN :p_SolicitudCambio SOL ON SOL."IdSolicitudCambio" = MOD."IdSolicitudCambio"		
				WHERE 
						MOD."IdTipoEntidad"			= v_TipoEntidad
					AND ATR."IdParametro"			= cast (MOD."IdEntidadOrigen" as int)
					AND ATR."Codigo" <>'RUTMAS';	
				
				
				---Se identifica si hay modificaciones a RUT desde masivo
						SELECT 	COUNT(1) INTO	v_CountIndicadorModificado FROM	:v_DatosSolicitudCambio  WHERE "CampoModificado" ='RUTMAS';
						
						IF (v_CountIndicadorModificado > 0) 	 THEN
						  --Obtine Id parametro
						 -- SELECT TOP 1 cast ("IdEntidadOrigen" as int)  AS  "IdParametro" INTO	v_IdParametro FROM	:v_DatosSolicitudCambio  WHERE "CampoModificado" ='RUTMAS';
						  
  						  --SELECT TOP 1 "Esquema","Version" INTO	v_Esquema,v_Version
  						  -- FROM	"BSCL.Normativa.BD::CIR.Parametro"  WHERE "Codigo" ='RUTMAS' AND "IdParametro"=v_IdParametro ;
  						   
  						   DELETE FROM "BSCL.Normativa.BD::CIR.Parametro" 
  						   WHERE  "Periodo"			 	= v_Periodo									
								AND "IdEstadoAutorizacion"	<> 'PEN'								
								AND "Codigo" ='RUTMAS';
  						   
							UPDATE  ATR
							SET
								ATR."FechaModificacion" = CURRENT_UTCTIMESTAMP,
								ATR."IdEstadoAutorizacion" =p_IdEstadoAutorizacion
							FROM "BSCL.Normativa.BD::CIR.Parametro" ATR
							WHERE   ATR."Periodo"			 	= v_Periodo
								AND ATR."IdEstadoAutorizacion"	= 'PEN'								
								AND ATR."Codigo" ='RUTMAS';
									
						END IF;		
				
	
			END IF;	
			
	
	
	
		
			--Se termina el flujo con la actualizacion del registro de solicitud.
			v_SolicitudCambio = SELECT "IdSolicitudCambio" FROM :v_DatosSolicitudCambio;
			v_EntidadOrigen = SELECT 0 as "IdEntidadOrigen", '' as "IdTipoEntidad" FROM dummy where 1=2;
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
				DECLARE	v_TipoEntidadBase	NVARCHAR(40);
				DECLARE	v_IdEntidadBase	NVARCHAR(40);
				DECLARE v_fechaHoraSistema NVARCHAR(40);
				DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Cambio valor Indicador';
				
				
				--DATOS CABECERA LOG
				SELECT TOP 1 "IdEntidadOrigen" INTO v_IdEntidadOrigen FROM :v_DatosSolicitudCambio;
				
				SELECT "IdParametro",
				CAST ("IdParametro" AS NVARCHAR(40))
				INTO v_TipoEntidadBase,
				v_IdEntidadBase
				FROM  "BSCL.Normativa.BD::CIR.Parametro"  WHERE "IdParametro"=cast (v_IdEntidadOrigen AS int);
				
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
					,p_IdTipoRegistro => v_TipoEntidadBase--'CLIENTE'
					,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => :v_IdEntidadBase
					,p_EstadoProceso => :p_IdEstadoAutorizacion
					,p_EstadoRegistro=> :p_IdEstadoAutorizacion
					--,p_IdTipoDocumentoIdentidad=> :v_IdTipoDocumentoIdentidad
					--,p_IdDocumentoIdentidad=> :v_IdDocumentoIdentidad					
					);
	
			END;
			--LOG DE AUDITORIA (Fin)			
			
					
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;