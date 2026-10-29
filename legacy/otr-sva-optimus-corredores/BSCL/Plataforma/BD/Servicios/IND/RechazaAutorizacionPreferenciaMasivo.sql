SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::IND.RechazaAutorizacionPreferenciaMasivo";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para Rechaza de forma masiva la modificacion de Preferencias.
--  Invocado desde el modulo de autorizaciones.
--  Replica del SP RechazaAutorizacionPreferencia
--  Autor: JVILLARROEL - GAT-BLAC
--  Fecha Creacion: 28-06-2018
--  
"BSCL.Plataforma.BD.Servicios::IND.RechazaAutorizacionPreferenciaMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN 	p_SolicitudCambio							TABLE ("IdSolicitudCambio" BIGINT),		
	IN  p_IdConcepto	       						NVARCHAR(6),
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
		    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
			
			DECLARE v_countSolicitud								BIGINT;
			DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'PREFERENCIA';
			DECLARE v_IdEstadoAutorizacion							NVARCHAR(3):='RZD';
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.		
		BEGIN
			v_DatosSolicitud = SELECT SOL.* FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_SolicitudCambio S ON S."IdSolicitudCambio" = SOL."IdSolicitudCambio";

			SELECT COUNT(SOL."IdSolicitudCambio") INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_SolicitudCambio S ON S."IdSolicitudCambio" = SOL."IdSolicitudCambio";
			
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
			--Se termina el flujo con la actualizacion del registro de solicitud.
			v_SolicitudCambio = SELECT "IdSolicitudCambio" FROM :v_DatosSolicitud;
			v_EntidadOrigen = SELECT "IdEntidadOrigen", "IdTipoEntidad" FROM :v_DatosSolicitud;
			CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambioMasivo" (
				p_IdEmpresa				=>:p_IdEmpresa,
				p_IdUsuario				=>:p_IdUsuario,
				p_IdModulo				=>:p_IdModulo,
				p_SolicitudCambio			=>:v_SolicitudCambio,
				p_EntidadOrigen			    =>:v_EntidadOrigen,
				p_IdEstadoAutorizacion	=>:v_IdEstadoAutorizacion,
				p_out_AdvertenciasContador		=>:v_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle		=>:v_out_AdvertenciasDetalle,	
				p_out_ErroresContador			=>:v_out_ErroresContador,
				p_out_ErroresDetalle			=>:v_out_ErroresDetalle
			);
			p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContador;	
		  	p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
		                                        From :v_out_AdvertenciasDetalle Union All 
		                                        Select "Codigo","RequiereAutorizacion", "Descripcion"
		                                        From :p_out_AdvertenciasDetalle;  
		                                          
		                                          
		    p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador;
		                                         
		    p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
                                   From :v_out_ErroresDetalle Union All 
                                   Select "Codigo", "Descripcion" 
                                   From :p_out_ErroresDetalle;
		                                     
			if 	p_out_ErroresContador > 0 then
				SIGNAL SALIDA_CONTROLADA;
			end if;
			
			v_DatosSolicitud = SELECT SOL.* FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_SolicitudCambio S ON S."IdSolicitudCambio" = SOL."IdSolicitudCambio";
				
			UPDATE  ATR
			SET
				ATR."IdEstadoAutorizacion" =v_IdEstadoAutorizacion
			FROM "BSCL.Plataforma.BD::IND.Indicador" ATR
					INNER JOIN :v_DatosSolicitud MOD ON (ATR."IdIndicador"= cast (MOD."IdEntidadOrigen" as int))
			WHERE 
					MOD."IdTipoEntidad"			= v_TipoEntidad
				--AND MOD."IdSolicitudCambio" 	= p_IdSolicitudCambio
				AND ATR."IdIndicador"			= cast (MOD."IdEntidadOrigen" as int);			
		END;
		
		--LOG DE AUDITORIA (inicio)
		BEGIN 
			DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
			DECLARE	v_TipoEntidadBase	NVARCHAR(40);
			DECLARE	v_IdEntidadBase	NVARCHAR(40);
			DECLARE v_fechaHoraSistema NVARCHAR(40);
			DECLARE v_NombreProceso	NVARCHAR(100):='Rechazar Cambio valor Indicador';
			
			
			--DATOS CABECERA LOG
			SELECT TOP 1 "IdEntidadOrigen" INTO v_IdEntidadOrigen FROM :v_DatosSolicitud;
			
			SELECT "IdTipoEntidad",
			CAST ("IdEntidad" AS NVARCHAR(40))
			INTO v_TipoEntidadBase,
			v_IdEntidadBase
			FROM "BSCL.Plataforma.BD::IND.Indicador" WHERE "IdIndicador"=cast (v_IdEntidadOrigen AS int);
			
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			
			--DATOS DETALLE LOG
			p_DetalleLog = 
			Select MOD."IdTipoEntidad"		 as "SubRegistro",
					MOD."CampoModificado" || '|' || TIPIND."NombreTipoIndicador" as "CampoRegistro",
					MOD."ValorAntiguo"  	 as "ValorAntiguo",
					MOD."ValorNuevo" 		 as "ValorNuevo" 
			FROM :v_DatosSolicitud MOD
			JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIPIND ON (TIPIND."IdTipoIndicador" = MOD."CampoModificado");
			
			--GRABAR LOG
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				p_IdTipoLog => 'MODIFICACION' 
				,p_IdModulo => 'PLA' 
				,p_IdUsuario => p_IdUsuario 
				,p_IdEmpresa => p_IdEmpresa 
				,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
				,p_IdUsuarioAtendido => p_IdUsuario
				,p_IdTipoRegistro => v_TipoEntidadBase--'CLIENTE'
				,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
				,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
				,p_IdRegistro => :v_IdEntidadBase
				,p_EstadoProceso => :v_IdEstadoAutorizacion
				,p_EstadoRegistro=> :v_IdEstadoAutorizacion
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