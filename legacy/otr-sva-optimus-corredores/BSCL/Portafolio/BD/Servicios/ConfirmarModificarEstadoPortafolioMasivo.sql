SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::ConfirmarModificarEstadoPortafolioMasivo";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion al estado de portafolio
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: JGARCES - BCS
--  Fecha Creacion: 12/02/2016
--  
"BSCL.Portafolio.BD.Servicios::ConfirmarModificarEstadoPortafolioMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	
	IN 	p_Portafolio								TABLE ("IdPortafolio"	BIGINT),	
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
			DECLARE v_IdEstadoAutorizacion_Calculado				NVARCHAR(3);
			DECLARE v_IdEntidadOrigen								NVARCHAR(40);
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'PORTAFOLIO-ESTADO';
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_CountAtributoModificado						BIGINT:=0;
			DECLARE v_CountAtributoNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_IdEstado								BIGINT;
		
			--PRONETIT
			DECLARE v_countIdPortafolio INT:=0;
			DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        	DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
   
        	DECLARE v_IdEntidad				BIGINT:= 0;
			DECLARE v_TipoEntidadReplica	NVARCHAR(50):= 'PORTAFOLIO';
			DECLARE v_TipoOperacion			NVARCHAR(1):= 'U';
			
			DECLARE v_CountPortafolio								BIGINT;
			DECLARE v_CantRegistrosPortafolio 						BIGINT;
			DECLARE v_IdPortafolio									BIGINT;
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			--v_IdEntidadOrigen:=CAST (p_IdPortafolio AS NVARCHAR(40));
			
			SELECT COUNT("IdSolicitudCambio") INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_Portafolio P ON  CAST(P."IdPortafolio" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
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
			JOIN :p_Portafolio P ON  CAST(P."IdPortafolio" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
				AND SOL."IdTipoEntidad" 		= v_TipoEntidad
                AND SOL."IdEstadoAutorizacion"	='PEN';
			
			SELECT 	COUNT("IdSolicitudCambio") INTO	v_CountAtributoModificado FROM	:v_DatosSolicitudCambio;
			
			IF (v_CountAtributoModificado > 0) 	 THEN
			
				v_CampoModificado:='IdEstado';
				SELECT count("ValorNuevo") INTO v_count_IdEstado
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				IF (v_count_IdEstado > 0) THEN
					UPDATE  POR
					SET
						POR."IdEstado" = MOD."ValorNuevo",
						POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						
					FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
							INNER JOIN :v_DatosSolicitudCambio MOD ON (POR."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 		= v_CampoModificado
						AND MOD."IdTipoEntidad"		= v_TipoEntidad
						AND POR."IdPortafolio"		= cast (MOD."IdEntidadOrigen" as int);
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				--#################	
				--     BLOQUEO	
				--#################		
				v_CampoModificado:='IdTipoBloqueo';
				SELECT count("ValorNuevo") INTO v_count_IdEstado
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				IF (v_count_IdEstado > 0) THEN
					UPDATE  POR
					SET
						POR."IdTipoBloqueo" = MOD."ValorNuevo",
						POR."FechaBloqueo" = CURRENT_UTCTIMESTAMP,
						POR."IdTipoDesbloqueo" = '',
						POR."FechaDesbloqueo" = '0001-01-01',
						POR."FechaDesbloqueoTemporal" = '0001-01-01',
						POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						
					FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
							INNER JOIN :v_DatosSolicitudCambio MOD ON (POR."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 		= v_CampoModificado
						AND MOD."IdTipoEntidad"		= v_TipoEntidad
						AND POR."IdPortafolio"		= cast (MOD."IdEntidadOrigen" as int);
						v_countProcesados:=v_countProcesados + 1;
				END IF;			
												
				v_CampoModificado:='FechaBloqueo';
				SELECT count("ValorNuevo") INTO v_count_IdEstado
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				IF (v_count_IdEstado > 0) THEN
					UPDATE  POR
					SET
						POR."FechaBloqueo" = CURRENT_UTCTIMESTAMP,
						POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						
					FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
							INNER JOIN :v_DatosSolicitudCambio MOD ON (POR."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 		= v_CampoModificado
						AND MOD."IdTipoEntidad"		= v_TipoEntidad
						AND POR."IdPortafolio"		= cast (MOD."IdEntidadOrigen" as int);
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				v_CampoModificado:='ComentarioBloqueo';
				SELECT count("ValorNuevo") INTO v_count_IdEstado
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				IF (v_count_IdEstado > 0) THEN
					UPDATE  POR
					SET
						POR."ComentarioBloqueo" = MOD."ValorNuevo",
						POR."ComentarioDesbloqueo" = '',
						POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						
					FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
							INNER JOIN :v_DatosSolicitudCambio MOD ON (POR."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 		= v_CampoModificado
						AND MOD."IdTipoEntidad"		= v_TipoEntidad
						AND POR."IdPortafolio"		= cast (MOD."IdEntidadOrigen" as int);
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				v_CampoModificado:='FechaBloqueoTemporal';
				SELECT count(*) INTO v_count_IdEstado
				FROM :v_DatosSolicitudCambio 
				WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				IF (v_count_IdEstado > 0) THEN
    			UPDATE POR
    			SET
        			POR."FechaBloqueoTemporal" = COALESCE(TO_SECONDDATE(NULLIF(TRIM(MOD."ValorNuevo"),'')),TO_SECONDDATE('0001-01-01 00:00:00')),
        			POR."FechaModificacion" = CURRENT_UTCTIMESTAMP,
        			POR."IdTipoDesbloqueo" = '',
        			POR."ComentarioDesbloqueo" = '',
        			POR."FechaDesbloqueo" = '0001-01-01'
    			FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
        			INNER JOIN :v_DatosSolicitudCambio MOD ON (POR."IdPortafolio" = cast(MOD."IdEntidadOrigen" as int))
    			WHERE
        		MOD."CampoModificado" = v_CampoModificado
        		AND MOD."IdTipoEntidad" = v_TipoEntidad
        		AND POR."IdPortafolio" = cast(MOD."IdEntidadOrigen" as int);
    			v_countProcesados := v_countProcesados + 1;
				END IF;			
				
				--#################	
				--     DESBLOQUEO	
				--#################	
				v_CampoModificado:='IdTipoDesbloqueo';
				SELECT count("ValorNuevo") INTO v_count_IdEstado
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				IF (v_count_IdEstado > 0) THEN
					UPDATE  POR
					SET
						POR."IdTipoDesbloqueo" = MOD."ValorNuevo",
						POR."FechaDesbloqueo" = CURRENT_UTCTIMESTAMP,
						POR."IdTipoBloqueo" = '',
						POR."FechaBloqueo" = '0001-01-01',
						POR."FechaBloqueoTemporal" = '0001-01-01',
						POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						
					FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
							INNER JOIN :v_DatosSolicitudCambio MOD ON (POR."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 		= v_CampoModificado
						AND MOD."IdTipoEntidad"		= v_TipoEntidad
						AND POR."IdPortafolio"		= cast (MOD."IdEntidadOrigen" as int);
						v_countProcesados:=v_countProcesados + 1;
				END IF;			
												
				v_CampoModificado:='FechaDesbloqueo';
				SELECT count("ValorNuevo") INTO v_count_IdEstado
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				IF (v_count_IdEstado > 0) THEN
					UPDATE  POR
					SET
						POR."FechaDesbloqueo" = CURRENT_UTCTIMESTAMP,
						POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						
					FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
							INNER JOIN :v_DatosSolicitudCambio MOD ON (POR."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 		= v_CampoModificado
						AND MOD."IdTipoEntidad"		= v_TipoEntidad
						AND POR."IdPortafolio"		= cast (MOD."IdEntidadOrigen" as int);
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				v_CampoModificado:='ComentarioDesbloqueo';
				SELECT count("ValorNuevo") INTO v_count_IdEstado
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				IF (v_count_IdEstado > 0) THEN
					UPDATE  POR
					SET
						POR."ComentarioBloqueo" = '',
						POR."ComentarioDesbloqueo" = MOD."ValorNuevo",
						POR."FechaModificacion" = CURRENT_UTCTIMESTAMP
						
					FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
							INNER JOIN :v_DatosSolicitudCambio MOD ON (POR."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
					WHERE 
						MOD."CampoModificado" 		= v_CampoModificado
						AND MOD."IdTipoEntidad"		= v_TipoEntidad
						AND POR."IdPortafolio"		= cast (MOD."IdEntidadOrigen" as int);
						v_countProcesados:=v_countProcesados + 1;
				END IF;
				
				v_CampoModificado:='FechaDesbloqueoTemporal';
				SELECT count(*) INTO v_count_IdEstado
				FROM :v_DatosSolicitudCambio 
				WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				IF (v_count_IdEstado > 0) THEN
    			UPDATE POR
    			SET
        				POR."FechaDesbloqueoTemporal" = COALESCE(TO_SECONDDATE(NULLIF(TRIM(MOD."ValorNuevo"),'')),TO_SECONDDATE('0001-01-01 00:00:00')),        					
            			POR."IdTipoBloqueo" = '',
            			POR."ComentarioBloqueo" = '',
            			POR."FechaBloqueo" = '0001-01-01',
        				POR."FechaModificacion" = CURRENT_UTCTIMESTAMP,
        				POR."FechaBloqueoTemporal" = '0001-01-01'       				      				
    			FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
        			INNER JOIN :v_DatosSolicitudCambio MOD ON (POR."IdPortafolio" = cast(MOD."IdEntidadOrigen" as int))         
    			WHERE
        			MOD."CampoModificado" = v_CampoModificado
        			AND MOD."IdTipoEntidad" = v_TipoEntidad
        			AND POR."IdPortafolio" = cast(MOD."IdEntidadOrigen" as int);
    				v_countProcesados := v_countProcesados + 1;
				END IF;
				
				--PRONETIT
				
		            v_CountPortafolio:= 1;
				
		            SELECT COUNT("IdPortafolio") INTO v_CantRegistrosPortafolio FROM :p_Portafolio;
				
					WHILE :v_CountPortafolio <= :v_CantRegistrosPortafolio DO
		
						v_IdPortafolio 	:= 0;
						
						v_PortafolioRecorrido = SELECT 
						 						ROW_NUMBER() OVER(ORDER BY POR."IdPortafolio") 	AS "Id"
												,POR."IdPortafolio"								AS "IdPortafolio" 	 
											FROM :p_Portafolio AS POR;
												
						SELECT POR."IdPortafolio" INTO v_IdPortafolio
						FROM :v_PortafolioRecorrido AS POR
						WHERE "Id" = :v_CountPortafolio;
						
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
									v_IdPortafolio,
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
									
						v_CountPortafolio:= :v_CountPortafolio + 1;					
						
					END WHILE;
				
			END IF;	

			
			--Se termina el flujo con la actualizacion del registro de solicitud.
			
			v_SolicitudCambio = SELECT "IdSolicitudCambio" FROM :v_DatosSolicitudCambio;
			v_EntidadOrigen = SELECT 0 as "IdEntidadOrigen", '' as "IdTipoEntidad" FROM dummy where 1=2;

			CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambioMasivo" (
				p_IdEmpresa					=>:p_IdEmpresa,
				p_IdUsuario					=>:p_IdUsuario,
				p_IdModulo					=>:p_IdModulo,
				p_SolicitudCambio			=>:v_SolicitudCambio,
				p_EntidadOrigen			    =>:v_EntidadOrigen,
				p_IdEstadoAutorizacion		=>:p_IdEstadoAutorizacion,
				p_out_AdvertenciasContador	=>:p_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle	=>:p_out_AdvertenciasDetalle,	
				p_out_ErroresContador		=>:p_out_ErroresContador,
				p_out_ErroresDetalle		=>:p_out_ErroresDetalle	
			);
					

			--LOG DE AUDITORIA (inicio)
			BEGIN 
				DECLARE v_fechaHoraSistema NVARCHAR(40);
				DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Cambio de Estado';
				
				
				--DATOS CABECERA LOG
				call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);
				
				v_id_portafolio = SELECT "IdPortafolio", "BSCL"."BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL AS "IdLog" FROM :p_Portafolio;			
				
				--DATOS DETALLE LOG
				v_DetalleLog = 
				(Select POR."IdLog"			 AS "IdCabeceraLog",
						MOD."IdTipoEntidad"	 as "SubRegistro",
						MOD."CampoModificado"	 as "CampoRegistro",
						MOD."ValorAntiguo"  	 as "ValorAntiguo",
						MOD."ValorNuevo" 		 as "ValorNuevo" 
				FROM :v_DatosSolicitudCambio MOD
				JOIN :v_id_portafolio AS POR ON (POR."IdPortafolio" = MOD."IdPortafolio"));
			
				
				v_log = SELECT P."IdLog" AS "IdCabeceraLog"
							   ,'MODIFICACION' AS "IdTipoLog"
							   ,'POR' AS "IdModulo"
							   ,p_IdUsuario AS "IdUsuario"
							   ,p_IdEmpresa AS "IdEmpresa"
							   ,TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') AS "FechaSistemaLog"
							   ,p_IdUsuario AS "IdUsuarioAtendido"
							   ,0 AS "IdPersona"
							   ,cast(0 as INTEGER) AS "IdCliente"
							   ,cast(POR."IdEstado" as VARCHAR(3)) AS "EstadoCliente"
							   ,'' AS "NombreCliente" 
							   ,'' AS "IdTipoDocumentoIdentidad" 
							   ,'' AS "IdDocumentoIdentidad"
							   ,0 AS "IdPortafolio"
							   ,'' AS "EstadoPortafolio"
							   ,'PORTAFOLIO-ESTADO' AS "IdTipoRegistro" 
							   ,P."IdPortafolio" AS "IdRegistro"
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
						FROM :v_id_portafolio AS P
						JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON POR."IdPortafolio" = P."IdPortafolio";	   
	
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
