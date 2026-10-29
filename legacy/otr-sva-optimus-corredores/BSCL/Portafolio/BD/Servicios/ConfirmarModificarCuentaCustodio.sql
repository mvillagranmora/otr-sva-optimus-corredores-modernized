SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::ConfirmarModificarCuentaCustodio";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion al portafolio de un cliente.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 07-09-2016
--  
"BSCL.Portafolio.BD.Servicios::ConfirmarModificarCuentaCustodio" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico

	IN 	p_SolicitudesCambio							TABLE("IdSecuenciaCambio" BIGINT),	
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
			DECLARE v_IdEntidadOrigen								NVARCHAR(40) DEFAULT '';
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'PORTAFOLIO';
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_CountAtributoModificado						BIGINT:=0;
			DECLARE v_CountAtributoNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_Mod				 						BIGINT:=0;
			
			--PRONETIT
			DECLARE v_countIdPortafolio INT:=0;
			DECLARE v_out_AdvertenciasContador, v_out_ErroresContador 	INT;
		    DECLARE v_out_ErroresDetalle           						table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        	DECLARE v_out_AdvertenciasDetalle      						table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
   
        	DECLARE v_IdEntidad											BIGINT:= 0;
			DECLARE v_TipoEntidadReplica								NVARCHAR(50):= 'CUENTACUSTODIO';
			DECLARE v_TipoOperacion										NVARCHAR(1):= 'U';
			
			DECLARE v_CountCuentaCustodio								BIGINT;
			DECLARE v_CantRegistrosCuentaCustodio						BIGINT;
			DECLARE v_IdPortafolio										BIGINT;
			
			DECLARE v_tablaCuentasCustodio 								TABLE("IdCuentaCustodioPortafolio" BIGINT, "IdPortafolio" BIGINT);
			
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	

		--select * FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" ;
		BEGIN
			--SELECT "IdSolicitudCambio" INTO v_IdEntidadOrigen FROM :p_SolicitudesCambio;
			--v_IdEntidadOrigen:= CAST (p_secuencia_IdSolicitudCambio AS NVARCHAR(40));
			
			SELECT COUNT(1) INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" csc
			INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  csc."IdSolicitudCambio";
		
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
			v_DatosSolicitudCambio	=SELECT csc.* 
									 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" csc
									 INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  csc."IdSolicitudCambio";
			
			SELECT 	COUNT("IdSolicitudCambio") INTO	v_CountAtributoModificado FROM :v_DatosSolicitudCambio;
			
			IF (v_CountAtributoModificado > 0) 	 THEN
					select * from "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio";
			
					v_CampoModificado:='Codigo';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio dsc
					INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  dsc."IdSolicitudCambio"
					WHERE "CampoModificado" = v_CampoModificado;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  ccp
						SET
							ccp."Codigo" = MOD."ValorNuevo",
							ccp."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ccp
								INNER JOIN :v_DatosSolicitudCambio MOD ON (ccp."IdCuentaCustodioPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND ccp."IdCuentaCustodioPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdEstado';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio dsc
					INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  dsc."IdSolicitudCambio"
					WHERE "CampoModificado" = v_CampoModificado;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  ccp
						SET
							ccp."IdEstado" = MOD."ValorNuevo",
							ccp."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ccp
								INNER JOIN :v_DatosSolicitudCambio MOD ON (ccp."IdCuentaCustodioPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND ccp."IdCuentaCustodioPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdTipoCuentaCustodia';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio dsc
					INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  dsc."IdSolicitudCambio"
					WHERE "CampoModificado" = v_CampoModificado;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  ccp
						SET
							ccp."IdTipoCuentaCustodia" = MOD."ValorNuevo",
							ccp."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ccp
								INNER JOIN :v_DatosSolicitudCambio MOD ON (ccp."IdCuentaCustodioPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND ccp."IdCuentaCustodioPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdTipoCustodia';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio dsc
					INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  dsc."IdSolicitudCambio"
					WHERE "CampoModificado" = v_CampoModificado;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  ccp
						SET
							ccp."IdTipoCustodia" = MOD."ValorNuevo",
							ccp."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ccp
								INNER JOIN :v_DatosSolicitudCambio MOD ON (ccp."IdCuentaCustodioPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND ccp."IdCuentaCustodioPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdModeloCustodia';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio dsc
					INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  dsc."IdSolicitudCambio"
					WHERE "CampoModificado" = v_CampoModificado;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  ccp
						SET
							ccp."IdModeloCustodia" = MOD."ValorNuevo",
							ccp."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ccp
								INNER JOIN :v_DatosSolicitudCambio MOD ON (ccp."IdCuentaCustodioPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND ccp."IdCuentaCustodioPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdPersonaBancoCustodio';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio dsc
					INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  dsc."IdSolicitudCambio"
					WHERE "CampoModificado" = v_CampoModificado;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  ccp
						SET
							ccp."IdPersonaBancoCustodio" = MOD."ValorNuevo",
							ccp."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ccp
								INNER JOIN :v_DatosSolicitudCambio MOD ON (ccp."IdCuentaCustodioPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND ccp."IdCuentaCustodioPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdPersonaBancoLiquidador';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio dsc
					INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  dsc."IdSolicitudCambio"
					WHERE "CampoModificado" = v_CampoModificado;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  ccp
						SET
							ccp."IdPersonaBancoLiquidador" = MOD."ValorNuevo",
							ccp."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ccp
								INNER JOIN :v_DatosSolicitudCambio MOD ON (ccp."IdCuentaCustodioPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND ccp."IdCuentaCustodioPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='LiquidaDividendos';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio dsc
					INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  dsc."IdSolicitudCambio"
					WHERE "CampoModificado" = v_CampoModificado;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  ccp
						SET
							ccp."LiquidaDividendos" = MOD."ValorNuevo",
							ccp."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ccp
								INNER JOIN :v_DatosSolicitudCambio MOD ON (ccp."IdCuentaCustodioPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND ccp."IdCuentaCustodioPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='LiquidaCupones';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio dsc
					INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  dsc."IdSolicitudCambio"
					WHERE "CampoModificado" = v_CampoModificado;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  ccp
						SET
							ccp."LiquidaCupones" = MOD."ValorNuevo",
							ccp."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ccp
								INNER JOIN :v_DatosSolicitudCambio MOD ON (ccp."IdCuentaCustodioPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND ccp."IdCuentaCustodioPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='LiquidaVencimientos';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio dsc
					INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  dsc."IdSolicitudCambio"
					WHERE "CampoModificado" = v_CampoModificado;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  ccp
						SET
							ccp."LiquidaVencimientos" = MOD."ValorNuevo",
							ccp."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ccp
								INNER JOIN :v_DatosSolicitudCambio MOD ON (ccp."IdCuentaCustodioPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND ccp."IdCuentaCustodioPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='LiquidaPrepagos';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio dsc
					INNER JOIN :p_SolicitudesCambio sc ON sc."IdSecuenciaCambio" =  dsc."IdSolicitudCambio"
					WHERE "CampoModificado" = v_CampoModificado;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  ccp
						SET
							ccp."LiquidaPrepagos" = MOD."ValorNuevo",
							ccp."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" ccp
								INNER JOIN :v_DatosSolicitudCambio MOD ON (ccp."IdCuentaCustodioPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND ccp."IdCuentaCustodioPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					
			--PRONETIT
	                
		    v_CountCuentaCustodio:= 1;
		    
		    
		   	v_tablaCuentasCustodio = 	SELECT CCP."IdCuentaCustodioPortafolio" AS "IdCuentaCustodioPortafolio", CCP."IdPortafolio" AS "IdPortafolio"
		    							FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCP
		    							INNER JOIN :v_DatosSolicitudCambio AS MOD ON (CCP."IdCuentaCustodioPortafolio"= cast (MOD."IdEntidadOrigen" as int));
		    
			SELECT COUNT("IdCuentaCustodioPortafolio") INTO v_CantRegistrosCuentaCustodio FROM :v_tablaCuentasCustodio;
								
			v_CuentaCustodioRecorrido = SELECT 
				 						ROW_NUMBER() OVER(ORDER BY CU."IdCuentaCustodioPortafolio") 	AS "Id"
										,CU."IdCuentaCustodioPortafolio" 							AS "IdCuentaCustodioPortafolio" 	 
									FROM :v_tablaCuentasCustodio AS CU;
			
			
			WHILE :v_CountCuentaCustodio <= :v_CantRegistrosCuentaCustodio DO
		
				v_IdPortafolio 	:= 0;
				v_IdEntidad		:= 0;
										
				SELECT CU."IdCuentaCustodioPortafolio" INTO v_IdEntidad
				FROM :v_CuentaCustodioRecorrido AS CU
				WHERE "Id" = :v_CountCuentaCustodio;
				
				SELECT CU."IdPortafolio" INTO v_IdPortafolio
				FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CU
				WHERE "IdCuentaCustodioPortafolio" = :v_IdEntidad;
						
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
									
				v_CountCuentaCustodio:= :v_CountCuentaCustodio + 1;					
						
			END WHILE;
				
					
			END IF;	
			--Se termina el flujo con la actualizacion del registro de solicitud.
			v_entidadOrigen = SELECT "IdEntidadOrigen", "IdTipoEntidad" FROM :v_DatosSolicitudCambio;
			v_solicitudes = select "IdSecuenciaCambio" AS "IdSolicitudCambio" FROM :p_SolicitudesCambio;
			
			CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambioMasivo" (
				p_IdEmpresa				=>:p_IdEmpresa,
				p_IdUsuario				=>:p_IdUsuario,
				p_IdModulo				=>:p_IdModulo,
				p_SolicitudCambio		=>:v_solicitudes,
				p_EntidadOrigen			=>:v_entidadOrigen,
				p_out_AdvertenciasContador		=>:p_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle		=>:p_out_AdvertenciasDetalle,	
				p_out_ErroresContador			=>:p_out_ErroresContador,
				p_out_ErroresDetalle			=>:p_out_ErroresDetalle	
			);
						
		END;
			--LOG DE AUDITORIA (inicio)
			BEGIN 
				DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
				DECLARE v_fechaHoraSistema NVARCHAR(40);
				DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Solicitud de Cambio';
				
				
				--DATOS CABECERA LOG
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
					,p_IdModulo => p_IdModulo
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => v_TipoEntidad--'CLIENTE'
					,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => :v_IdEntidadOrigen
					,p_EstadoProceso => :p_IdEstadoAutorizacion
					);
	
			END;
			--LOG DE AUDITORIA (Fin)			
			
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;