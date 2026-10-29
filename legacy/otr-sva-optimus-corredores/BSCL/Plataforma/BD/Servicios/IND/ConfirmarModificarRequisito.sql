SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::IND.ConfirmarModificarRequisito";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion al requisito de una entidad.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 13-04-2016
--  
"BSCL.Plataforma.BD.Servicios::IND.ConfirmarModificarRequisito" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	
	
	
	IN 	p_IdSolicitudCambio						BIGINT,	
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
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'REQUISITO';
			DECLARE v_CountIndicadorModificado						BIGINT:=0;
			DECLARE v_CountIndicadorNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			
			DECLARE v_IdEntidadOrigenAPV							NVARCHAR(40):='';
			DECLARE v_CountProposito								BIGINT:=0;
			DECLARE v_IdPortafolio									NVARCHAR(40):='';
			DECLARE v_IdTipoEntidad         						NVARCHAR(40):='';
			DECLARE v_TipoEntidadAPV								NVARCHAR(40):= 'INDICADORES';
			
			
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			
			SELECT COUNT(1) INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
			WHERE "IdSolicitudCambio" =p_IdSolicitudCambio;
		
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
			WHERE "IdSolicitudCambio" =p_IdSolicitudCambio;

			SELECT 	COUNT("IdSolicitudCambio") INTO	v_CountIndicadorModificado FROM	:v_DatosSolicitudCambio;
			
			IF (v_CountIndicadorModificado > 0) 	 THEN
			
				UPDATE  ATR
				SET
					ATR."ValorIndicador" = MOD."ValorNuevo",
					ATR."FechaModificacion" = CURRENT_UTCTIMESTAMP,
					ATR."IdEstadoAutorizacion" =p_IdEstadoAutorizacion
				FROM "BSCL.Plataforma.BD::IND.Indicador" ATR
						INNER JOIN :v_DatosSolicitudCambio MOD ON (ATR."IdIndicador"= cast (MOD."IdEntidadOrigen" as int))
				WHERE 
						MOD."IdTipoEntidad"			= v_TipoEntidad
					AND MOD."IdSolicitudCambio" 	= p_IdSolicitudCambio
					AND ATR."IdIndicador"			= cast (MOD."IdEntidadOrigen" as int);
					
					
				/* APV REPLICA*/
				
				SELECT TOP 1 "IdEntidadOrigen" 
				  INTO v_IdEntidadOrigenAPV 
				  FROM :v_DatosSolicitudCambio;
				
				SELECT "IdEntidad", "IdTipoEntidad" 
				  INTO v_IdPortafolio, v_IdTipoEntidad 
				  FROM "BSCL.Plataforma.BD::IND.Indicador"
				 WHERE "IdIndicador" = v_IdEntidadOrigenAPV;

				IF(:v_IdTipoEntidad = 'PORTAFOLIO') THEN
				 SELECT COUNT(Portafolio."IdPortafolio") 
				   INTO v_CountProposito
				   FROM "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
				  WHERE Portafolio."Proposito" = 'PROPOSITOAPV'
					AND Portafolio."IdPortafolio" = v_IdPortafolio;	
				END IF;

				IF(:v_CountProposito > 0) THEN								
				
					CALL "BSCL.APV.BD.Servicios::ReplicaPortafolioAPV" (
							:p_IdEmpresa,
							:p_IdUsuario,
							:p_IdModulo,
							:p_UsaReglasNegocio,
							v_IdPortafolio, 
							:p_IdSolicitudCambio,
							v_TipoEntidadAPV, 
							'U',
							p_out_AdvertenciasContador,
							p_out_AdvertenciasDetalle,
							p_out_ErroresContador,
							p_out_ErroresDetalle
						);
				
				END IF;
				 
				 /* FIN APV */
			END IF;	
			
	
	
			
			--Se termina el flujo con la actualizacion del registro de solicitud.

			CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambio" (
				p_IdEmpresa				=>:p_IdEmpresa,
				p_IdUsuario				=>:p_IdUsuario,
				p_IdModulo				=>:p_IdModulo,
				p_IdSolicitudCambio		=>:p_IdSolicitudCambio,
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
				DECLARE v_IdCliente					BIGINT:=0;
				DECLARE v_nombreCliente 			NVARCHAR(200):='';
				DECLARE v_IdDocumentoIdentidad		NVARCHAR(50):='';
				DECLARE v_TipoDocumentoIdentidad	NVARCHAR(10):='';
				DECLARE v_EstadoCliente				NVARCHAR(10):='';

				--DATOS CABECERA LOG
				SELECT TOP 1 "IdEntidadOrigen" INTO v_IdEntidadOrigen FROM :v_DatosSolicitudCambio;
				
				SELECT "IdTipoEntidad",
					CAST ("IdEntidad" AS NVARCHAR(40))
				INTO v_TipoEntidadBase,
					v_IdEntidadBase
						FROM "BSCL.Plataforma.BD::IND.Indicador" WHERE "IdIndicador"=cast (v_IdEntidadOrigen AS int);
				
				IF(v_TipoEntidadBase = 'CLIENTE' AND v_IdEntidadBase > 0) THEN 
					
					v_IdEntidadBase:= COALESCE(v_IdEntidadBase, '0');
					v_TipoEntidad:= v_TipoEntidad||v_TipoEntidadBase;
				
					SELECT 
						COALESCE("CLI"."IdCliente", 0)					AS "IdClien",
						COALESCE("CLI"."NombreCompleto", '')			AS "Nombre",
						COALESCE("CLI"."DocumentoIdentidad", '')		AS "DocumIdentidad",
						COALESCE("CLI"."TipoDocumentoIdentidad", '')	AS "TipoDocumento",
						COALESCE("CLI"."IdEstado", '')					AS "Estado"
					INTO
						v_IdCliente,
						v_nombreCliente,
						v_IdDocumentoIdentidad,
						v_TipoDocumentoIdentidad,
						v_EstadoCliente
							FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" "CLI"
							WHERE "CLI"."IdCliente" = :v_IdEntidadBase;
				END IF;

				--DATOS DETALLE LOG
				p_DetalleLog = (Select 
									MOD."IdTipoEntidad"		 							as "SubRegistro",
									COALESCE("TIPINDI"."NombreTipoIndicador", '') 		as "CampoRegistro",
									MOD."ValorAntiguo"  	 							as "ValorAntiguo",
									MOD."ValorNuevo" 		 							as "ValorNuevo" 
										FROM :v_DatosSolicitudCambio MOD
										LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicador" "TIPINDI"
											ON (COALESCE(MOD."CampoModificado", '') = CAST("TIPINDI"."IdTipoIndicador" AS NVARCHAR(40)))
								);
							
				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);

				--GRABAR LOG
				CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'MODIFICACION' 
					,p_IdModulo => 'SOC' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => :v_TipoEntidad--'CLIENTE'
					,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => :v_IdEntidadBase
					,p_EstadoProceso => :p_IdEstadoAutorizacion
					,p_EstadoRegistro=> :p_IdEstadoAutorizacion
					,p_IdCliente => :v_IdCliente
					,p_IdTipoDocumentoIdentidad=> :v_TipoDocumentoIdentidad
					,p_IdDocumentoIdentidad=> :v_IdDocumentoIdentidad
					,p_NombreCliente=> :v_nombreCliente
					,p_EstadoCliente => :v_EstadoCliente				
				);
	
			END;
			--LOG DE AUDITORIA (Fin)			

		--------------------
		--FIN SP 
		--------------------
	END;	
END;