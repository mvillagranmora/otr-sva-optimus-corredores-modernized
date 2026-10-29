SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::ConfirmarModificarPreferenciaPagoMasivo";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion a las preferencias de pago.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 01-04-2016
--  
"BSCL.Portafolio.BD.Servicios::ConfirmarModificarPreferenciaPagoMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	
	IN 	p_PreferenciaPagoPortafolio					TABLE ("IdPreferenciaPagoPortafolio"	BIGINT),	
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
			call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
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
			DECLARE v_IdEntidadOrigen								NVARCHAR(40);
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'PREFERENCIAPAGO';
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_CountAtributoModificado						BIGINT:=0;
			DECLARE v_CountAtributoNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_IdPreferenciaPago 						BIGINT;
			DECLARE v_count_IdPersonaRelacionada					BIGINT;
			DECLARE v_count_IdCuentaBancaria						BIGINT;
			DECLARE v_count_IdTarjetaBancaria						BIGINT;
			DECLARE v_count_IdEstado								BIGINT;
			DECLARE v_count_PorDefecto								BIGINT;
			
			--PRONETIT
			DECLARE v_countIdPortafolio INT:=0;
			DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        	DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
   
        	DECLARE v_IdEntidad				BIGINT:= 0;
			DECLARE v_TipoEntidadReplica	NVARCHAR(50):= 'PREFERENCIASDEPAGO';
			DECLARE v_TipoOperacion			NVARCHAR(1):= 'U';
			
			DECLARE v_CountPreferencia								BIGINT;
			DECLARE v_CantRegistrosPreferencia 						BIGINT;
			DECLARE v_IdPortafolio									BIGINT;
			DECLARE v_IdTipoOperacion 								NVARCHAR(6);
			DECLARE v_IdMoneda										NVARCHAR(3);
			DECLARE v_EsDefecto										NVARCHAR(1);
		

		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			--v_IdEntidadOrigen:=CAST (p_IdPreferenciaPagoPortafolio AS NVARCHAR(40));
			
			SELECT COUNT("IdSolicitudCambio") INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_PreferenciaPagoPortafolio P ON  CAST(P."IdPreferenciaPagoPortafolio" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
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
									
			--DATOS MAESTROS DE LA ENTIDAD 
			v_DatosSolicitudCambio = SELECT * 
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_PreferenciaPagoPortafolio P ON  CAST(P."IdPreferenciaPagoPortafolio" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
				AND SOL."IdTipoEntidad" 		= v_TipoEntidad
                AND SOL."IdEstadoAutorizacion"	='PEN';
			
			SELECT 	COUNT("IdSolicitudCambio") INTO	v_CountAtributoModificado FROM	:v_DatosSolicitudCambio;
			
			IF (v_CountAtributoModificado > 0) 	 THEN
		
					v_CampoModificado:='IdPreferenciaPago';
					SELECT count("ValorNuevo") INTO v_count_IdPreferenciaPago
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdPreferenciaPago > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPreferenciaPago" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPreferenciaPagoPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPreferenciaPagoPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;

					v_CampoModificado:='IdPersonaRelacionada';
					SELECT count("ValorNuevo") INTO v_count_IdPersonaRelacionada
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdPersonaRelacionada > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPersonaRelacionada" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPreferenciaPagoPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPreferenciaPagoPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;

					v_CampoModificado:='IdCuentaBancaria';
					SELECT count("ValorNuevo") INTO v_count_IdCuentaBancaria
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdCuentaBancaria > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdCuentaBancaria" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPreferenciaPagoPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPreferenciaPagoPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;


					v_CampoModificado:='IdTarjetaBancaria';
					SELECT count("ValorNuevo") INTO v_count_IdTarjetaBancaria
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdTarjetaBancaria > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdTarjetaBancaria" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPreferenciaPagoPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPreferenciaPagoPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
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
							
						FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPreferenciaPagoPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPreferenciaPagoPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='PorDefecto';
					SELECT count("ValorNuevo") INTO v_count_PorDefecto
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_PorDefecto > 0) THEN

						SELECT TOP 1 DOC."IdPortafolio" , PP."IdTipoOperacion", PP."IdMoneda", MOD."ValorNuevo" INTO v_IdPortafolio, v_IdTipoOperacion, v_IdMoneda, v_EsDefecto
							FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC
							INNER JOIN "BSCL.Plataforma.BD::PAG.PreferenciaPago" PP ON DOC."IdPreferenciaPago" = PP."IdPreferenciaPago" AND PP."IdEmpresa" = DOC."IdEmpresa" 
							INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPreferenciaPagoPortafolio"= cast (MOD."IdEntidadOrigen" as int))
							WHERE 
									MOD."CampoModificado" 		= v_CampoModificado
								AND	MOD."IdTipoEntidad"			= v_TipoEntidad
								AND DOC."IdPreferenciaPagoPortafolio"	= cast (MOD."IdEntidadOrigen" as int) AND DOC."IdEmpresa" = p_IdEmpresa;
						
						IF v_EsDefecto = 'S' THEN
							--QUITAR MARCA POR DEFECTO A TODOS LOS INDICADORES DEL PORTAFOLIO.
							UPDATE  DOC
							SET  DOC."PorDefecto" = 'N',
		 						 DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC,
								 "BSCL.Plataforma.BD::PAG.PreferenciaPago" PP,
								 :v_DatosSolicitudCambio MOD
							WHERE DOC."IdPreferenciaPago" = PP."IdPreferenciaPago"  
							AND DOC."IdPortafolio"        = v_IdPortafolio
							AND PP."IdEmpresa"            = DOC."IdEmpresa" 
							AND PP."IdEmpresa"            = p_IdEmpresa
							AND PP."IdTipoOperacion"      = v_IdTipoOperacion
							AND PP."IdMoneda" 			  = v_IdMoneda
							AND DOC."IdPreferenciaPagoPortafolio" != cast (MOD."IdEntidadOrigen" as int);			
						END IF;
  					
		
						UPDATE  DOC
						SET
							DOC."PorDefecto" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPreferenciaPagoPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPreferenciaPagoPortafolio"	= cast (MOD."IdEntidadOrigen" as int) AND DOC."IdEmpresa" = p_IdEmpresa;
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					
					--PRONETIT
	                
		            v_CountPreferencia:= 1;
					SELECT COUNT("IdPreferenciaPagoPortafolio") INTO v_CantRegistrosPreferencia FROM :p_PreferenciaPagoPortafolio;
										
					v_PreferenciaRecorrido = SELECT 
						 						ROW_NUMBER() OVER(ORDER BY PRE."IdPreferenciaPagoPortafolio") 	AS "Id"
												,PRE."IdPreferenciaPagoPortafolio" 							AS "IdPreferenciaPagoPortafolio" 	 
											FROM :p_PreferenciaPagoPortafolio AS PRE;
					
					
					WHILE :v_CountPreferencia <= :v_CantRegistrosPreferencia DO
		
						v_IdPortafolio 	:= 0;
						v_IdEntidad		:= 0;
										
						SELECT PRE."IdPreferenciaPagoPortafolio" INTO v_IdEntidad
						FROM :v_PreferenciaRecorrido AS PRE
						WHERE "Id" = :v_CountPreferencia;
						
						SELECT PRE."IdPortafolio" INTO v_IdPortafolio
						FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" AS PRE
						WHERE "IdPreferenciaPagoPortafolio" = :v_IdEntidad;
						
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
									
						v_CountPreferencia:= :v_CountPreferencia + 1;					
						
					END WHILE;
		
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
				DECLARE v_IdCliente		BIGINT;
				DECLARE v_EstadoCliente	NVARCHAR(3);
				DECLARE	v_NombreCliente	NVARCHAR(100);
				DECLARE	v_IdTipoDocumentoIdentidad	NVARCHAR(40);
				DECLARE	v_IdDocumentoIdentidad	NVARCHAR(40);				
				DECLARE v_fechaHoraSistema NVARCHAR(40);
				DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Solicitud de Cambio';
				
				DECLARE CURSOR c_IdPreferenciaPagoPortafolio FOR
					SELECT "IdPreferenciaPagoPortafolio" 
					  FROM :p_PreferenciaPagoPortafolio;
					 
				--DATOS CABECERA LOG
				call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => :p_IdEmpresa,
					p_out_FechaHoraSistema => :v_fechaHoraSistema
				);					 

				-- recorre tabla p_PreferenciaPagoPortafolio
				FOR cur_row AS c_IdPreferenciaPagoPortafolio DO
					SELECT "IdPortafolio" INTO v_IdPortafolio 
					  FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" 
					 WHERE "IdPreferenciaPagoPortafolio" = cur_row."IdPreferenciaPagoPortafolio"; 
					
					SELECT CLI."IdCliente"
						   ,CLI."NombreCompleto"
						   ,CLI."TipoDocumentoIdentidad"
						   ,CLI."DocumentoIdentidad"
						   ,CLI."IdEstado"
					INTO v_IdCliente,
						 v_NombreCliente,
						 v_IdTipoDocumentoIdentidad,
						 v_IdDocumentoIdentidad,
						 v_EstadoCliente					   
					  FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
					  INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = POR."IdCliente"
					  WHERE POR."IdPortafolio" = :v_IdPortafolio;					
					
					--DATOS DETALLE LOG
					p_DetalleLog = 
					(Select MOD."IdTipoEntidad"		 as "SubRegistro",
							MOD."CampoModificado"	 as "CampoRegistro",
							MOD."ValorAntiguo"  	 as "ValorAntiguo",
							MOD."ValorNuevo" 		 as "ValorNuevo" 
					FROM :v_DatosSolicitudCambio MOD
					WHERE MOD."IdPreferenciaPagoPortafolio" = cur_row."IdPreferenciaPagoPortafolio");				

					--GRABAR LOG
					CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'MODIFICACION' 
					,p_IdModulo => 'SOC' 
					,p_IdUsuario => :p_IdUsuario 
					,p_IdEmpresa => :p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => :p_IdUsuario
					,p_IdPortafolio => :v_IdPortafolio
					,p_IdTipoRegistro => :v_TipoEntidad--'PREFERENCIAPAGO'
					,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => cur_row."IdPreferenciaPagoPortafolio"
					,p_EstadoProceso => :p_IdEstadoAutorizacion
					,p_EstadoRegistro=> :p_IdEstadoAutorizacion
					,p_EstadoCliente=> :v_EstadoCliente --Cliente
					,p_IdCliente => :v_IdCliente
					,p_NombreCliente=> :v_NombreCliente
					,p_IdTipoDocumentoIdentidad=> :v_IdTipoDocumentoIdentidad
					,p_IdDocumentoIdentidad=> :v_IdDocumentoIdentidad						
					);					
				END FOR;					 

			END;
			--LOG DE AUDITORIA (Fin)			
			
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
GO