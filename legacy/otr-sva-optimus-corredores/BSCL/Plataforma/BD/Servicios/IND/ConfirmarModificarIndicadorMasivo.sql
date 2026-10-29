SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::IND.ConfirmarModificarIndicadorMasivo";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion al indicador de una entidad.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 07-04-2016
--  
"BSCL.Plataforma.BD.Servicios::IND.ConfirmarModificarIndicadorMasivo" (
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
			DECLARE v_IdPortafolio									NVARCHAR(40):='';
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'INDICADOR';
			DECLARE v_CountIndicadorModificado						BIGINT:=0;
			DECLARE v_CountIndicadorNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_countFechaVencimiento							BIGINT:=0;
			DECLARE v_CountProposito								BIGINT:=0;

			--PRONETIT
			DECLARE v_countIdPortafolio INT:=0;
			DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        	DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
   
        	DECLARE v_IdEntidad						BIGINT:= 0;
			DECLARE v_TipoEntidadReplica			NVARCHAR(50):= 'INDICADORES';
			DECLARE v_TipoOperacion					NVARCHAR(1):= 'U';
			
			DECLARE v_CountIndicadores				BIGINT;
			DECLARE v_CantRegistrosIndicadores 		BIGINT;
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

			SELECT 	COUNT("IdSolicitudCambio") INTO	v_CountIndicadorModificado FROM	:v_DatosSolicitudCambio;
			
			IF (v_CountIndicadorModificado > 0) 	 THEN
			
				UPDATE ATR
				SET
					ATR."ValorIndicador" = MOD."ValorNuevo",
					ATR."FechaModificacion" = CURRENT_UTCTIMESTAMP,
					ATR."IdEstadoAutorizacion" = p_IdEstadoAutorizacion
				FROM "BSCL.Plataforma.BD::IND.Indicador" ATR
				INNER JOIN :v_DatosSolicitudCambio MOD ON (ATR."IdIndicador"= cast (MOD."IdEntidadOrigen" as int))
				INNER JOIN :p_SolicitudCambio SOL ON SOL."IdSolicitudCambio" = MOD."IdSolicitudCambio"		
				WHERE 
					MOD."IdTipoEntidad"			= v_TipoEntidad
					--AND MOD."IdSolicitudCambio" 	= p_IdSolicitudCambio
					AND ATR."IdIndicador"			= cast (MOD."IdEntidadOrigen" as int);
					
				
				v_CampoModificado:='FechaVencimiento';
				SELECT count("ValorNuevo") INTO v_countFechaVencimiento
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_countFechaVencimiento > 0) THEN
					UPDATE  DOC
					SET
						DOC."FechaVencimiento" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::IND.Indicador" DOC
						INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdIndicador"= cast (MOD."IdEntidadOrigen" as int))	
						INNER JOIN :p_SolicitudCambio SOL ON SOL."IdSolicitudCambio" = MOD."IdSolicitudCambio"				
					WHERE 
						MOD."IdTipoEntidad"			= v_TipoEntidad
						--AND MOD."IdSolicitudCambio" = p_IdSolicitudCambio
						AND DOC."IdIndicador"		= cast (MOD."IdEntidadOrigen" as int);
				END IF;
					
				
				--PRONETIT
				/* APV REPLICA*/
			                
	            v_CountIndicadores:= 1;
				SELECT COUNT("IdSolicitudCambio") INTO v_CantRegistrosIndicadores FROM :p_SolicitudCambio;
									
				v_IndicadoresRecorrido = 	SELECT 
					 							ROW_NUMBER() OVER(ORDER BY SOL."IdSolicitudCambio") 	AS "Id"
												,SOL."IdSolicitudCambio" 								AS "IdSolicitudCambio" 	 
											FROM :p_SolicitudCambio AS SOL;
				
				
				WHILE :v_CountIndicadores <= :v_CantRegistrosIndicadores DO
	
					v_IdPortafolio 	:= 0;
					v_IdEntidad		:= 0;
									
					SELECT INDR."IdSolicitudCambio" INTO v_IdEntidad
					FROM :v_IndicadoresRecorrido AS INDR
					WHERE "Id" = :v_CountIndicadores;
					
					SELECT TOP 1
						IND."IdEntidad" INTO v_IdPortafolio
					FROM (	SELECT 
								* 
							FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
							WHERE "IdSolicitudCambio" = v_IdEntidad) AS CSC
					INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IND
					ON CSC."IdEntidadOrigen" = CAST (IND."IdIndicador" AS NVARCHAR(100));
	
					
					IF(v_IdPortafolio != 0) THEN
						
						SELECT COUNT("IdPortafolio") INTO v_countIdPortafolio
						FROM "BSCL.Portafolio.BD::POR.Portafolio"
						WHERE "IdPortafolio" = v_IdPortafolio
						AND "Proposito" = 'PROPOSITOAPV';
						
						IF(v_countIdPortafolio > 0)THEN
							CALL "BSCL.APV.BD.Servicios::ReplicaPortafolioAPV" (
								:p_IdEmpresa,
								:p_IdUsuario,
								:p_IdModulo,
								:p_UsaReglasNegocio,
								v_IdPortafolio, 
								v_IdEntidad,
								v_TipoEntidadReplica, 
								v_TipoOperacion,
								v_out_AdvertenciasContador,
								v_out_AdvertenciasDetalle,
								v_out_ErroresContador,
								v_out_ErroresDetalle
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
				                
				            IF	p_out_ErroresContador > 0 then
								SIGNAL SALIDA_CONTROLADA;
						  	END if;
			            END IF;      
											
					END IF;
								
					v_CountIndicadores := :v_CountIndicadores + 1;					
					
				END WHILE;
				
				 /* FIN APV */
				 
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
				DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(500), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
				DECLARE	v_TipoEntidadBase	NVARCHAR(40);
				DECLARE	v_IdEntidadBase	NVARCHAR(40);
				DECLARE v_fechaHoraSistema NVARCHAR(40);
				DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Cambio valor Indicador';
				DECLARE v_IdCliente							BIGINT:=0;
				DECLARE v_EstadoCliente						NVARCHAR(5):='';
				DECLARE	v_NombreCliente						NVARCHAR(100):='';
				DECLARE	v_IdDocumentoIdentidad				NVARCHAR(40):='';
				DECLARE	v_IdTipoDocumentoIdentidad			NVARCHAR(40):='';
				DECLARE v_IdPortafolio						BIGINT:=0;
				DECLARE v_EstadoPortafolio					NVARCHAR(5):='';
				DECLARE v_IdPersonaEmpleado							BIGINT:=0;
				DECLARE v_IdPersonaCliente							BIGINT:=0;
				DECLARE v_IdClientePortafolio						BIGINT:=0;
				DECLARE v_IdPersonaClientePortafolio				BIGINT:=0;
				DECLARE v_IdEntidadBaseTemporal						BIGINT:=0;
				
				--DATOS CABECERA LOG
				SELECT TOP 1 "IdEntidadOrigen" INTO v_IdEntidadOrigen FROM :v_DatosSolicitudCambio;
				
				SELECT COALESCE("IdTipoEntidad", ''),
				COALESCE(CAST ("IdEntidad" AS NVARCHAR(40)), '')
				INTO v_TipoEntidadBase,
				v_IdEntidadBase
				FROM "BSCL.Plataforma.BD::IND.Indicador" WHERE "IdIndicador"=cast (v_IdEntidadOrigen AS int);
				
				SELECT COALESCE("IdEntidad", 0) 
				INTO v_IdEntidadBaseTemporal
				FROM "BSCL.Plataforma.BD::IND.Indicador" WHERE "IdIndicador"=cast (v_IdEntidadOrigen AS int);
				
				IF(v_TipoEntidadBase = 'PERSONA')THEN
						
					v_TipoEntidadBase:='INDICADOR'||v_TipoEntidadBase;
					
					SELECT
						CASE
							COALESCE("PER"."Tipo", '') WHEN 'N'
							THEN COALESCE("PER"."Nombres", '') || ' ' || COALESCE("PER"."ApellidoPaterno", '') || ' ' || COALESCE("PER"."ApellidoMaterno", '')
							ELSE COALESCE("PER"."RazonSocial" ,'')
						END AS "NombreCompleto"
					INTO
						v_NombreCliente
					FROM "BSCL.SocioNegocio.BD::PER.Persona" AS "PER"
					WHERE "PER"."IdPersona" = :v_IdEntidadBaseTemporal;
					
					SELECT
						COALESCE("DOC"."DocumentoIdentidad", ''),
						COALESCE("DOC"."IdTipoDocumentoIdentidad", '')
					INTO
						v_IdDocumentoIdentidad,
						v_IdTipoDocumentoIdentidad
					FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC"
					WHERE "DOC"."IdPersona" = :v_IdEntidadBaseTemporal;
				
				END IF;
				
				IF(v_TipoEntidadBase = 'CLIENTE') THEN
				
					v_TipoEntidadBase:='INDICADOR'||v_TipoEntidadBase;
					
					SELECT
						COALESCE("CLI"."IdCliente", 0),
						COALESCE("CLI"."IdPersona", 0),
						COALESCE("CLI"."IdEstado", '')
					INTO
						v_IdCliente,
						v_IdPersonaCliente,
						v_EstadoCliente
					FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"
					WHERE "CLI"."IdCliente" = :v_IdEntidadBaseTemporal;
					
					SELECT
						CASE
							COALESCE("PER"."Tipo", '') WHEN 'N'
							THEN COALESCE("PER"."Nombres", '') || ' ' || COALESCE("PER"."ApellidoPaterno", '') || ' ' || COALESCE("PER"."ApellidoMaterno", '')
							ELSE COALESCE("PER"."RazonSocial" ,'')
						END AS "NombreCompleto"
					INTO
						v_NombreCliente
					FROM "BSCL.SocioNegocio.BD::PER.Persona" AS "PER"
					WHERE "PER"."IdPersona" = :v_IdPersonaCliente;
					
					SELECT 
						COALESCE("DOC"."DocumentoIdentidad", ''),
						COALESCE("DOC"."IdTipoDocumentoIdentidad", '')
					INTO 	
						v_IdDocumentoIdentidad,
						v_IdTipoDocumentoIdentidad
					FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC"
					WHERE "DOC"."IdPersona" = :v_IdPersonaCliente;
					
				END IF;
					
				IF(v_TipoEntidadBase = 'EMPLEADO')THEN
					
					v_TipoEntidadBase:='INDICADOR'||v_TipoEntidadBase;
					
					SELECT
						COALESCE("EMP"."IdPersona", 0)
					INTO
						v_IdPersonaEmpleado
					FROM "BSCL.SocioNegocio.BD::EMP.Empleado" AS "EMP"
					WHERE "EMP"."IdEmpleado" = :v_IdEntidadBaseTemporal;
				
					SELECT
						CASE
							COALESCE("PER"."Tipo", '') WHEN 'N'
							THEN COALESCE("PER"."Nombres", '') || ' ' || COALESCE("PER"."ApellidoPaterno", '') || ' ' || COALESCE("PER"."ApellidoMaterno", '')
							ELSE COALESCE("PER"."RazonSocial" ,'')
						END AS "NombreCompleto"
					INTO 
						v_NombreCliente
					FROM "BSCL.SocioNegocio.BD::PER.Persona" AS "PER"
					WHERE "PER"."IdPersona" = :v_IdPersonaEmpleado;
					
					SELECT 
						COALESCE("DOC"."DocumentoIdentidad", ''),
						COALESCE("DOC"."IdTipoDocumentoIdentidad", '')
					INTO 	
						v_IdDocumentoIdentidad,
						v_IdTipoDocumentoIdentidad
					FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC"
					WHERE "DOC"."IdPersona" = :v_IdPersonaEmpleado;
						
				END IF;
				
				IF(v_TipoEntidadBase = 'PORTAFOLIO')THEN
					
					v_TipoEntidadBase:='INDICADOR'||v_TipoEntidadBase;
					
					SELECT
						COALESCE("PORT"."IdCliente", 0),
						COALESCE("PORT"."IdPortafolio", 0),
						COALESCE("PORT"."IdEstado", '')
					INTO
						v_IdClientePortafolio,
						v_IdPortafolio,
						v_EstadoPortafolio
					FROM "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT"
					WHERE "PORT"."IdPortafolio" = :v_IdEntidadBaseTemporal;
					
					SELECT
						COALESCE("CLI"."IdCliente", 0),
						COALESCE("CLI"."IdPersona", 0),
						COALESCE("CLI"."IdEstado", '')
					INTO
						v_IdCliente,
						v_IdPersonaClientePortafolio,
						v_EstadoCliente
					FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"
					WHERE "CLI"."IdCliente" = :v_IdClientePortafolio;
					
					SELECT
						CASE
							COALESCE("PER"."Tipo", '') WHEN 'N'
							THEN COALESCE("PER"."Nombres", '') || ' ' || COALESCE("PER"."ApellidoPaterno", '') || ' ' || COALESCE("PER"."ApellidoMaterno", '')
							ELSE COALESCE("PER"."RazonSocial" ,'')
						END AS "NombreCompleto"
					INTO
						v_NombreCliente
					FROM "BSCL.SocioNegocio.BD::PER.Persona" AS "PER"
					WHERE "PER"."IdPersona" = :v_IdPersonaClientePortafolio;
					
					SELECT
						COALESCE("DOC"."DocumentoIdentidad", ''),
						COALESCE("DOC"."IdTipoDocumentoIdentidad", '')
					INTO
						v_IdDocumentoIdentidad,
						v_IdTipoDocumentoIdentidad
					FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC"
					WHERE "DOC"."IdPersona" = :v_IdPersonaClientePortafolio;
					
				END IF;
				
				call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);
				
				--DATOS DETALLE LOG   --

				p_DetalleLog = 
				Select MOD."IdTipoEntidad"		 as "SubRegistro",
						MOD."CampoModificado" || '|' || TIPIND."NombreTipoIndicador" as "CampoRegistro",
						MOD."ValorAntiguo"  	 as "ValorAntiguo",
						MOD."ValorNuevo" 		 as "ValorNuevo" 
				FROM :v_DatosSolicitudCambio MOD
				JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIPIND ON (TIPIND."IdTipoIndicador" = MOD."CampoModificado");
				

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
			
					
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;