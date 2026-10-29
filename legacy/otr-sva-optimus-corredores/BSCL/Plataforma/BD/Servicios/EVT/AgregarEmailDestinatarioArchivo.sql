SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.AgregarEmailDestinatarioArchivo";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para Agrega correos-destinatarios(según rol)-archivos previo a su envío
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::EVT.AgregarEmailDestinatarioArchivo"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
  								 	OUT p_out_IdArchivo	table("IdArchivoAdjunto" BIGINT, "Archivo" BLOB, "Nombre" NVARCHAR(200),"IdEmail" BIGINT),
 								    OUT p_out_MensajeSalida			NVARCHAR(520)
 								 	/*OUT p_Mails table("IdEnvio" BIGINT,"MensajeCuerpo" NCLOB,"Remitente" NVARCHAR(200),"Asunto" NVARCHAR(540)),
   									OUT p_Destinatarios	table ("IdEnvio" BIGINT,"TipoDestinatario" NVARCHAR(5), "Destinatario" NVARCHAR(200)),
  							    	OUT p_ArchivoAdjunto table ("IdEnvio" BIGINT, "Archivo" BLOB,"Nombre" NVARCHAR(200))
  								 	 */
  								 	 
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 

        DECLARE v_count BIGINT;
        DECLARE v_countIdConf BIGINT;
        DECLARE v_countIdConfReenviar BIGINT;
        DECLARE v_countIdConfRecordatorio BIGINT; 
        DECLARE v_countRe BIGINT;
        DECLARE out_mail BIGINT;
        DECLARE out_dest BIGINT;
        DECLARE v_countRecor BIGINT;
        DECLARE v_emisor NVARCHAR(520);
        
        --------------------------------
        --CAso para envios manuales
		 ---- Agrupados
		--- no agrupados

        --------------------------------
		--caso para envios inmediatos

        --------------------------------
		--caso para envios por HORA
        
        SELECT "Valor" INTO v_emisor FROM "BSCL.Plataforma.BD::PLA.Configuracion" where "CodigoParametro" = 'PLA.EmisorNotificacion' AND "IdEmpresa" = p_IdEmpresa;
        
        --Estas tres consultas se utilizan solo para determinar si hay notificaciones que actualizar al final del SP
        SELECT DISTINCT COUNT(1) INTO v_countIdConf
 		FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion" CN
 		WHERE (("FechaDespachoOrginal" < CURRENT_UTCTIMESTAMP and ("EnviadoSrv" = 'N' and "TiempoDespachoSrv" != 'M' ) ) OR "EnviarSrv" = 'S' );

		select distinct  COUNT(1) into v_countIdConfRecordatorio
 		FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion"
 		where "EnviadoSrv" = 'N' and "RecordarDespacho" = 'S' 
 		AND "FechaDespachoRecordatorio" != TO_SECONDDATE('0001-01-01 0:00:00','YYYY-MM-DD HH24:MI:SS') 
 		and "FechaDespachoRecordatorio" < CURRENT_UTCTIMESTAMP and "RecordatorioEnviado" = 'N';
 				
 		select distinct  COUNT(1) into v_countIdConfReenviar
 		FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion"
 		where "ReEnviarSrv" = 'S';
 		
 				
 		--Obtener las configuraciones de las notificaciones a procesar.		
 		p_IdConfiguracion = 
 			select distinct "IdConfiguracionEvento", "IdNotificacion"
 			FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion" CN
 			where (("FechaDespachoOrginal" < CURRENT_UTCTIMESTAMP and ("EnviadoSrv" = 'N' and "TiempoDespachoSrv" != 'M' ) ) or "EnviarSrv" = 'S' ) AND ("FormaDespachoSrv" != 'U');
 				
 		p_IdConfiguracionRecordatorio = 
 			select distinct "IdConfiguracionEvento" 
 			FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion"
 			where "EnviadoSrv" = 'N' and "RecordarDespacho" = 'S' 
 			AND "FechaDespachoRecordatorio" != TO_SECONDDATE('0001-01-01 0:00:00','YYYY-MM-DD HH24:MI:SS') 
 			and "FechaDespachoRecordatorio" < CURRENT_UTCTIMESTAMP and "RecordatorioEnviado" = 'N' AND ("FormaDespachoSrv" != 'U');
 			
 		p_IdConfiguracionReEnviar = 
 			select distinct "IdConfiguracionEvento", "DireccionAlternativa"
 			FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion"
 			where "ReEnviarSrv" = 'S' AND ("FormaDespachoSrv" != 'U');
 		
 		--¿Por que no utilizo los contadores anteriores? (solo cambia el select en FormaDespachoSrv!=U)
 		SELECT count(1) into v_count   FROM :p_IdConfiguracion;
 		SELECT count(1) into v_countRe FROM :p_IdConfiguracionReEnviar;
 		SELECT COUNT(1) into v_countRecor FROM :p_IdConfiguracionRecordatorio;
 		
 		IF :v_count > 0 or :v_countRe > 0 or :v_countRecor > 0 THEN

		--Obtener los correos a enviar dependiendo si el destinatario es:
		--cliente , se validan las configuraciones de su portafolio
		--empleado, no tiene configuracion adicional, solo se valida que el correo corresponda a la persona del empleado
		v_mail_Enviar = 
				SELECT "IdEnvio", "MensajeCuerpo", "Remitente", "Asunto", "Destinatario"
				FROM
				(
					SELECT DISTINCT
						CASE WHEN CE."Agrupa" = 'N' THEN E."IdEvento" ELSE N."IdNotificacion" END AS "IdEnvio",
						to_char(E."MensajeBody") as "MensajeCuerpo", 
						'emisor' as "Remitente",
						P."Asunto",
						CPDE."Direccion" AS "Destinatario",
						CASE
							WHEN CE."PermitePersonalizar" = 'N' THEN 'S' --VER SI ES MEJOR PONER OR 'TIPOENTIDAD'='EMP'
							ELSE
								CASE WHEN COALESCE(CCEP."IdConfiguracionEvento",0)=0 THEN 'S' -- se puede personalizar, pero no se ha personalizado el cliente.
								ELSE
									CASE WHEN COALESCE(CCEP."IdEntidad",0)=0 THEN 'N'
									ELSE
										CASE WHEN CCEP."EstadoConfiguracionPortafolio" = 'INA' THEN 'N'
										ELSE
											CASE WHEN CCEP."SuspensionTemporal" = 'N' THEN 'S'
											ELSE
												CASE WHEN CURRENT_UTCTIMESTAMP BETWEEN CCEP."FechaInicioSuspension" AND CCEP."FechaTerminoSuspension" THEN 'N'
												ELSE 'S'
						END END END END END END AS "Valido"

					FROM "BSCL.Plataforma.BD::EVT.Notificacion" N
					INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON ND."IdNotificacion" = N."IdNotificacion" AND N."Enviado"='N'
					INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE ON CE."IdConfiguracionEvento" = N."IdConfiguracionEvento"
					INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" E ON ND."IdEvento" = E."IdEvento"  AND E."MensajeBody" is not null
					INNER JOIN "BSCL.Plataforma.BD::EVT.EventoDirecciones" ED ON ND."IdEvento" = ED."IdEvento"
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" =  ED."IdDireccionElectronica" 
					INNER JOIN "BSCL.Plataforma.BD::EVT.Plantilla" P ON P."IdPlantilla" = CE."IdPlantilla"

					LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoEntidad" CCEP ON (CCEP."TipoEntidad" ='POR' OR CCEP."TipoEntidad" ='CLI') AND CCEP."IdConfiguracionEvento" = N."IdConfiguracionEvento"
				)WHERE "Valido" = 'S';
		
		v_mailReEnviar = SELECT "IdEnvio", "MensajeCuerpo", "Remitente", "Asunto", "Destinatario"
				FROM
				(
					SELECT DISTINCT
						CASE WHEN CE."Agrupa" = 'N' THEN E."IdEvento" ELSE N."IdNotificacion" END AS "IdEnvio",
						to_char(E."MensajeBody") as "MensajeCuerpo", 
						v_emisor as "Remitente",
						P."Asunto",
						CPDE."Direccion" AS "Destinatario",
						CASE
							WHEN CE."PermitePersonalizar" = 'N' THEN 'S' --VER SI ES MEJOR PONER OR 'TIPOENTIDAD'='EMP'
							ELSE
								CASE WHEN COALESCE(CCEP."IdConfiguracionEvento",0)=0 THEN 'S' -- se puede personalizar, pero no se ha personalizado el cliente.
								ELSE
									CASE WHEN COALESCE(CCEP."IdEntidad",0)=0 THEN 'N'
									ELSE
										CASE WHEN CCEP."EstadoConfiguracionPortafolio" = 'INA' THEN 'N'
										ELSE
											CASE WHEN CCEP."SuspensionTemporal" = 'N' THEN 'S'
											ELSE
												CASE WHEN CURRENT_UTCTIMESTAMP BETWEEN CCEP."FechaInicioSuspension" AND CCEP."FechaTerminoSuspension" THEN 'N'
												ELSE 'S'
						END END END END END END AS "Valido"

					FROM "BSCL.Plataforma.BD::EVT.Notificacion" N
					INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON ND."IdNotificacion" = N."IdNotificacion" AND N."ReEnviar"='S' AND N."ReEnviado" = 'N'
					INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE ON CE."IdConfiguracionEvento" = N."IdConfiguracionEvento"
					INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" E ON ND."IdEvento" = E."IdEvento"  AND E."MensajeBody" is not null
					INNER JOIN "BSCL.Plataforma.BD::EVT.EventoDirecciones" ED ON ND."IdEvento" = ED."IdEvento"
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" =  ED."IdDireccionElectronica" 
					INNER JOIN "BSCL.Plataforma.BD::EVT.Plantilla" P ON P."IdPlantilla" = CE."IdPlantilla"

					LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoEntidad" CCEP ON (CCEP."TipoEntidad" ='POR' OR CCEP."TipoEntidad" ='CLI') AND CCEP."IdConfiguracionEvento" = N."IdConfiguracionEvento"
				)WHERE "Valido" = 'S';
		--falta corregir		
		v_mail_Recordar = 
	  		select distinct p_conf."IdConfiguracionEvento" as "IdEnvio",to_char(cmd."MensajeRecordatorio") as "MensajeCuerpo",
	  			v_emisor as "Remitente",'Recordatorio : '||"Asunto" as "Asunto",cmprdest."Direccion" as "Destinatario"
	  			FROM :p_IdConfiguracionRecordatorio  p_conf
		  		left join "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoPorConfiguracion" cmd on cmd."IdConfiguracionEvento" = p_conf."IdConfiguracionEvento"
		  		INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEvento" evt ON EVT."IdEvento" = cmd."IdEvento"
		  		LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos" cce ON cce."IdConfiguracionEvento" = p_conf."IdConfiguracionEvento"
		  		LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" cmprdest ON cmprdest."IdPersona" =  EVT."IdPersonaDestinatario"
		  		LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarPlantillas" cp ON cp."IdPlantilla" = cce."IdPlantilla"
		  		where cmd."IdConfiguracionEvento" = p_conf."IdConfiguracionEvento"   AND cmd."MensajeBody" is not null;
		
 	
	  	v_mail = 
		  	SELECT  DISTINCT "IdEnvio", "MensajeCuerpo", "Remitente","Asunto" FROM :v_mail_Enviar
		  		UNION ALL 
		  	SELECT  DISTINCT "IdEnvio", "MensajeCuerpo", "Remitente","Asunto" FROM :v_mailReEnviar
		  		UNION ALL 
		  	SELECT  DISTINCT "IdEnvio", "MensajeCuerpo", "Remitente","Asunto" FROM :v_mail_Recordar;
	  	
	  	
		v_destinatario_enviar = 
	  		select DISTINCT 
	  		CASE WHEN CCE."Agrupa" = 'N' THEN ND."IdEvento" ELSE N."IdNotificacion" END AS "IdEnvio",
	  		'TO' as "TipoDestinatario", 
	  		CPDE."Direccion" as "Destinatario"
			FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion" N
			INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON ND."IdNotificacion" = N."IdNotificacion"
			INNER JOIN "BSCL.Plataforma.BD::EVT.EventoDirecciones" ED ON ND."IdEvento" = ED."IdEvento"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" =  ED."IdDireccionElectronica" 
			INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos" CCE ON CCE."IdConfiguracionEvento" = N."IdConfiguracionEvento"
			where (("FechaDespachoOrginal" < CURRENT_UTCTIMESTAMP and ("EnviadoSrv" = 'N' and "TiempoDespachoSrv" != 'M' ) ) or "EnviarSrv" = 'S' ) AND ("FormaDespachoSrv" != 'U') AND "ExcluyeDestinatario" = 'N';
	  		
	  	v_destinatario_reenviar = 
	  		SELECT DISTINCT 
		  		CASE WHEN CCE."Agrupa" = 'N' THEN ND."IdEvento" ELSE N."IdNotificacion" END AS "IdEnvio",
		  		'TO' AS "TipoDestinatario", 
		  		CASE WHEN COALESCE(N."DireccionAlternativa",'')='' THEN CPDE."Direccion" ELSE N."DireccionAlternativa" END AS "Destinatario"
			FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion" N
			INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON ND."IdNotificacion" = N."IdNotificacion"
			INNER JOIN "BSCL.Plataforma.BD::EVT.EventoDirecciones" ED ON ND."IdEvento" = ED."IdEvento"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" =  ED."IdDireccionElectronica" 
			INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos" CCE ON CCE."IdConfiguracionEvento" = N."IdConfiguracionEvento"
			WHERE N."ReEnviarSrv" = 'S' AND N."ReEnviado" = 'UI_NO';

		--NO COMPROBADO	
	  	v_destinatario_recordar = 
	  			select distinct p_conf."IdConfiguracionEvento" as "IdEnvio" , 'TO' as "TipoDestinatario" ,cmprdest."Direccion" as "Destinatario"
	  			FROM :p_IdConfiguracionRecordatorio  p_conf
		  		left join "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoPorConfiguracion" cmd
		  		on cmd."IdConfiguracionEvento" = p_conf."IdConfiguracionEvento"
		  		INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEvento" evt
				ON EVT."IdEvento" = cmd."IdEvento"
		  		LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos" cce
		  		ON cce."IdConfiguracionEvento" = p_conf."IdConfiguracionEvento"
 				LEFT JOIN "BSCL.Plataforma.BD::EVT.EventoDirecciones" evtdir
 				ON evtdir."IdEvento" = cmd."IdEvento"
 				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" cmprdest
		  		ON cmprdest."IdDireccionElectronica" =  evtdir."IdDireccionElectronica" 
 		  		where cmd."IdConfiguracionEvento" = p_conf."IdConfiguracionEvento" and "ExcluyeDestinatario" = 'N';

 	  	 
 	  	 v_destinatariosEventoReenviar_b = 
					SELECT distinct conf."IdConfiguracionEvento" as "IdEnvio", 'TO' as "TipoDestinatario", "DireccionAlternativa" as "Destinatario"
				  	 FROM :p_IdConfiguracionReEnviar conf
					where  "DireccionAlternativa" is not null;
		
 	  	v_todos_reenviar = 
	  	SELECT distinct conf."IdConfiguracionEvento" as "IdEnvio", 'CC' as "TipoDestinatario", "Direccion" as "Destinatario"
	  	 FROM :p_IdConfiguracionReEnviar conf
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" ced
	  	 ON ced."IdConfiguracionEvento" = conf."IdConfiguracionEvento"
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cte
	  	 ON ced."CodigoTipoEvento" = cte."CodigoTipoEvento"
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento"  cute
	  	 ON cute."IdConfiguracionTipoEvento" = cte."IdConfiguracionTipoEvento" 
	  	 LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" 
	  	 ON 1 = 1
	  	WHERE "NotificaTodosUsuarios" = 'S' and "Direccion" is not null and length(trim("Direccion")) > 0 and "DireccionAlternativa" is null;
	  	
	  	v_porRol_reenviar = 
		  	SELECT distinct conf."IdConfiguracionEvento" as "IdEnvio", 'CC' as "TipoDestinatario", "Direccion" as "Destinatario"
		  	 FROM :p_IdConfiguracionReEnviar conf
		  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" ced
		  	 ON ced."IdConfiguracionEvento" = conf."IdConfiguracionEvento"
		  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cte
		  	 ON ced."CodigoTipoEvento" = cte."CodigoTipoEvento"
		  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento"  cute
		  	 ON cute."IdConfiguracionTipoEvento" = cte."IdConfiguracionTipoEvento" 
		  	 LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" 
		  	 ON "IdRol" = "IdUsuarioRol"
		  	WHERE "NotificaTodosUsuarios" = 'N' and "DireccionAlternativa" is null and "Direccion" is not null and length(trim("Direccion")) > 0 and "IdRol" = "IdUsuarioRol" ;
	  	
	  	v_copiasPorConf_reenviar = 
	  		SELECT DISTINCT pconf."IdConfiguracionEvento" as "IdEnvio", 'CC' as "TipoDestinatario", "Correo" as "Destinatario"
	  	 	 FROM :p_IdConfiguracionReEnviar pconf
	  	 	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCopiasCorreo" cc
	  	 	 ON cc."IdConfiguracionEvento" = pconf."IdConfiguracionEvento"
	  	 	 where "DireccionAlternativa" is null and "Correo" is not null;
	  	
	  	v_reenviarRemitente = 
	  	SELECT distinct conf."IdConfiguracionEvento" as "IdEnvio", 'TO' as "TipoDestinatario", "DireccionAlternativa" as "Destinatario"
	  	 FROM :p_IdConfiguracionReEnviar conf
	  	 INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" ced
	  	 ON ced."IdConfiguracionEvento" = conf."IdConfiguracionEvento"
	  	 INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" evt
	  	 ON evt."IdConfiguracionEvento" = ced."IdConfiguracionEvento"
 	  	 INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cte
	  	 ON evt."CodigoTipoEvento" = cte."CodigoTipoEvento"
	  	 INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" cmpr
	  	 ON cmpr."IdUsuario" = ced."IdUsuario"
 	  	 WHERE "CopiarRemitente" = 'S';
	  	
	  	
 	  	
	  	

		
		 
 	  	v_todos = 
	  	SELECT distinct conf."IdConfiguracionEvento" as "IdEnvio", 'CC' as "TipoDestinatario", "Direccion" as "Destinatario"
	  	 FROM :p_IdConfiguracion conf
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" ced
	  	 ON ced."IdConfiguracionEvento" = conf."IdConfiguracionEvento"
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cte
	  	 ON ced."CodigoTipoEvento" = cte."CodigoTipoEvento"
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento"  cute
	  	 ON cute."IdConfiguracionTipoEvento" = cte."IdConfiguracionTipoEvento" 
	  	 LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" 
	  	 ON 1=1
	  	WHERE "NotificaTodosUsuarios" = 'S' and "Direccion" is not null and length(trim("Direccion")) > 0;
	  	
	  	v_porRol = 
	  	SELECT distinct conf."IdConfiguracionEvento" as "IdEnvio", 'CC' as "TipoDestinatario", "Direccion" as "Destinatario"
	  	 FROM :p_IdConfiguracion conf
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" ced
	  	 ON ced."IdConfiguracionEvento" = conf."IdConfiguracionEvento"
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cte
	  	 ON ced."CodigoTipoEvento" = cte."CodigoTipoEvento"
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento"  cute
	  	 ON cute."IdConfiguracionTipoEvento" = cte."IdConfiguracionTipoEvento" 
	  	 LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" 
	  	 ON "IdRol" = "IdUsuarioRol"
	  	WHERE "NotificaTodosUsuarios" = 'N' and "Direccion" is not null and length(trim("Direccion")) > 0 and "IdRol" = "IdUsuarioRol";
	  	
	  	v_copiasPorConf = 
	  		SELECT DISTINCT 
	  		CASE WHEN cce."Agrupa" = 'N' THEN nd."IdEvento" ELSE pconf."IdNotificacion" END AS "IdEnvio", 
	  		'CC' as "TipoDestinatario", 
	  		"Correo" as "Destinatario"
	  	 	 FROM :p_IdConfiguracion pconf
	 	 	 INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" nd ON nd."IdNotificacion" = pconf."IdNotificacion" 
	  	 	 INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCopiasCorreo" cc ON cc."IdConfiguracionEvento" = pconf."IdConfiguracionEvento" 
	  	 	 INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos" cce ON cce."IdConfiguracionEvento" = pconf."IdConfiguracionEvento" 
	  	 	 WHERE "Correo" IS NOT NULL AND "CopiasAdicionales" = 'S';
	  	 	
	  	v_enviarRemitente = 
	  	SELECT distinct conf."IdConfiguracionEvento" as "IdEnvio", 'CC' as "TipoDestinatario", cmpr."Direccion" as "Destinatario"
	  	 FROM :p_IdConfiguracion conf
	  	 INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos" ce
	  	 ON ce."IdConfiguracionEvento" = conf."IdConfiguracionEvento" 
	  	 INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoPorConfiguracion" cepc
	  	 ON cepc."IdConfiguracionEvento" = ce."IdConfiguracionEvento"
 	  	 INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEvento" cte
	  	 ON cepc."IdEvento" = cte."IdEvento"
	  	 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" cmpr
	  	 ON cmpr."IdPersona" = cte."IdPersonaRemitente"
 	  	 WHERE "CopiarRemitente" = 'S'; 
 	  	 
 	  	 

		
		 
 	  	v_todosRecordar = 
	  	SELECT distinct conf."IdConfiguracionEvento" as "IdEnvio", 'CC' as "TipoDestinatario", "Direccion" as "Destinatario"
	  	 FROM :p_IdConfiguracion conf
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" ced
	  	 ON ced."IdConfiguracionEvento" = conf."IdConfiguracionEvento"
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cte
	  	 ON ced."CodigoTipoEvento" = cte."CodigoTipoEvento"
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento"  cute
	  	 ON cute."IdConfiguracionTipoEvento" = cte."IdConfiguracionTipoEvento" 
	  	 LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" 
	  	 ON 1=1
	  	WHERE "NotificaTodosUsuarios" = 'S' and "Direccion" is not null and length(trim("Direccion")) > 0;
	  	
	  	v_porRolRecordar = 
	  	SELECT distinct conf."IdConfiguracionEvento" as "IdEnvio", 'CC' as "TipoDestinatario", "Direccion" as "Destinatario"
	  	 FROM :p_IdConfiguracionRecordatorio conf
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" ced
	  	 ON ced."IdConfiguracionEvento" = conf."IdConfiguracionEvento"
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cte
	  	 ON ced."CodigoTipoEvento" = cte."CodigoTipoEvento"
	  	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento"  cute
	  	 ON cute."IdConfiguracionTipoEvento" = cte."IdConfiguracionTipoEvento" 
	  	 LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" 
	  	 ON "IdRol" = "IdUsuarioRol"
	  	WHERE "NotificaTodosUsuarios" = 'N' and "Direccion" is not null and length(trim("Direccion")) > 0 and "IdRol" = "IdUsuarioRol";
	  	
	  	v_copiasPorConfRecordar = 
	  		SELECT DISTINCT pconf."IdConfiguracionEvento" as "IdEnvio", 'CC' as "TipoDestinatario", "Correo" as "Destinatario"
	  	 	 FROM :p_IdConfiguracionRecordatorio pconf
	  	 	 LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCopiasCorreo" cc
	  	 	 ON cc."IdConfiguracionEvento" = pconf."IdConfiguracionEvento"
	  	 	 INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos" cce
	  	 	 ON cce."IdConfiguracionEvento" = pconf."IdConfiguracionEvento" where "Correo" is not null and "CopiasAdicionales" = 'S';
	  	 	
	  	 	/*
	  		v_enviarRemitenteRecordar = 
	  		SELECT distinct conf."IdConfiguracionEvento" as "IdEnvio", 'CC' as "TipoDestinatario", cmpr."Direccion" as "Destinatario"
	  		 FROM :p_IdConfiguracionRecordatorio conf
	  		 INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" ce
	  		 ON ce."IdConfiguracionEvento" = conf."IdConfiguracionEvento" 
	  		 INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" ced
	  		 ON ced."IdConfiguracionEvento" = conf."IdConfiguracionEvento"
 	  		 INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" cte
	  		 ON ced."CodigoTipoEvento" = cte."CodigoTipoEvento"
	  		 INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" cmpr
	  		 ON cmpr."IdUsuario" = ce."IdUsuario"
 	  		WHERE "CopiarRemitente" = 'S'; */
		  	
 
		v_destinatarios = 
		
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_destinatario_enviar
			UNION ALL
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_destinatario_reenviar
			UNION ALL
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_copiasPorConf
			/*
			UNION ALL
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_destinatario_recordar
			UNION ALL
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_porRol
			UNION
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_todos
			UNION ALL
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_copiasPorConfRecordar
			UNION ALL
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_porRolRecordar
			UNION
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_todosRecordar
			UNION ALL
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_copiasPorConf_reenviar
			UNION ALL
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_porRol_reenviar
			UNION
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_todos_reenviar
			UNION ALL
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_destinatariosEventoReenviar_b
			UNION ALL 
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_enviarRemitente
			UNION ALL 
			SELECT  "IdEnvio","TipoDestinatario","Destinatario" FROM :v_reenviarRemitente
			*/;
		
  		v_archivosAdjuntos =  
		 	  select ids."IdConfiguracionEvento" as "IdEnvio","Archivo","NombreArchivo" as "Nombre" 
		 	  from  :p_IdConfiguracion ids
				  INNER JOIN  "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoDetalles" cepc ON ids."IdConfiguracionEvento" = cepc."IdConfiguracionEvento" 
				  INNER JOIN  "BSCL.Plataforma.BD::EVT.Evento" evt ON cepc."CodigoTipoEvento" = evt."CodigoTipoEvento"
				  INNER JOIN "BSCL.Plataforma.BD::EVT.EventoArchivoAdjunto" eaa ON eaa."IdEvento" = evt."IdEvento"
			  UNION ALL 	
		  	  select ids."IdConfiguracionEvento" as "IdEnvio","Archivo","NombreArchivo" as "Nombre" 
		  	  from :p_IdConfiguracion ids
				  INNER JOIN  "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoDetalles" cepc ON ids."IdConfiguracionEvento" = cepc."IdConfiguracionEvento" 
				  INNER JOIN  "BSCL.Plataforma.BD::EVT.Evento" evt ON cepc."CodigoTipoEvento" = evt."CodigoTipoEvento"
				  INNER JOIN "BSCL.Plataforma.BD::EVT.EventoArchivoAdjunto" eaa ON eaa."IdEvento" = evt."IdEvento";
 		 		
		 							
  			--DEBUG
  			
 			call "BSCL.Plataforma.BD.Servicios::EVT.AgregarEnvioMail"(p_IdEmpresa,p_IdUsuario,p_IdModulo,:v_mail,:v_destinatarios,:v_archivosAdjuntos, p_idsArchivosAdjuntos,p_out_MensajeSalida);
		 	
			select count(1) INTO out_mail from :v_mail;
			select count(1) INTO out_dest from :v_destinatarios;
  		 	p_out_IdArchivo = select "IdArchivoAdjunto","Archivo","Nombre","IdEmail" FROM :p_idsArchivosAdjuntos;
 			
		ELSE
			p_out_MensajeSalida ='else';
		 	p_out_IdArchivo =
  		 select -1 as "IdArchivoAdjunto", '' as "Archivo", '' as "Nombre", -1 as "IdEmail" FROM dummy;
 
		
		END IF;	
		
  			--DEBUG
		IF :v_countIdConf > 0 OR :v_countIdConfReenviar > 0 OR :v_countIdConfRecordatorio > 0 THEN
			
 			CALL "BSCL.Plataforma.BD.Servicios::EVT.ActualizarNotificacion"(p_IdEmpresa,p_IdUsuario,p_IdModulo,p_out_MensajeSalida);
 			
			p_out_MensajeSalida := 'MAIL :::' || out_mail || 'Dest :::' || out_dest || ' msg salida act';-- ||p_out_MensajeSalida ;
			
		END IF;
 
 		
 
END;