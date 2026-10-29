SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.AgruparNotificaciones";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio encargado de Agregar notificaciones o Agregar detalle de notificaciones según corresponda
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.AgruparNotificaciones"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo          			NVARCHAR(3),
	                                
  									
  									out p_msg_out 					NVARCHAR(520),
  									OUT p_IdEventoAgrupado			TABLE ("IdEventoAgrupado" BIGINT,"IdConfiguracionEvento" BIGINT),
  									OUT p_IdEventoNoAgrupado  		TABLE("IdEventoNoAgrupado" BIGINT, "IdConfiguracionEvento" BIGINT),
  									OUT p_Notificacion 				TABLE("IdNotificacion" BIGINT)
  									
 	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 
	DECLARE contadorAgrupados BIGINT;
	DECLARE contadorNoAgrupados BIGINT;
	DECLARE contadorPendientes BIGINT;
	DECLARE contadorNuevos BIGINT;	
	
	DECLARE contadorDetalleAgrupados BIGINT;
	DECLARE contadorNuevosAgrupados BIGINT;
	DECLARE v_NoAgrupaDetalle TABLE("IdNotificacion" BIGINT, "IdEvento" BIGINT); 
	DECLARE v_NuevosDetalle TABLE("IdNotificacion" BIGINT, "IdEvento" BIGINT); 
	DECLARE p_idsNotificacion_nuevos TABLE("IdNotificacion" BIGINT, "IdEvento" BIGINT); 
	DECLARE p_idsNotificacion TABLE("IdNotificacion" BIGINT, "IdEvento" BIGINT, "IdConfiguracionEvento" BIGINT); 
	p_idsNotificacion_nuevos = SELECT -1 AS "IdNotificacion", -1 AS "IdEvento"  FROM DUMMY where 1=2;
	p_idsNotificacion = SELECT -1 AS "IdNotificacion", -1 AS "IdEvento", -1 AS "IdConfiguracionEvento" FROM DUMMY where 1=2;

	--Eventos agrupados que deban generar notificaciones
	v_eventosAgrupar = 
	SELECT DISTINCT CENP."IdConfiguracionEvento",CENP."Agrupa",CENP."Procesado",CENP."IdEvento", CENP."IdPersonaDestinatario",
		CASE WHEN COALESCE(CCEP."EstadoConfiguracionPortafolio",'')= '' THEN 'S' 
			ELSE  CASE WHEN CCEP."EstadoConfiguracionPortafolio" = 'ACT' THEN 'S' ELSE 'N' END
		END AS "GenerarNotificacion"
		FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventosNoProcesados" CENP
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN ON CSN."IdPersona" = CENP."IdPersonaDestinatario"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoEntidad" CCEP ON CCEP."TipoEntidad" = 'POR' AND CCEP."IdEntidad" = CENP."IdPortafolio" AND CCEP."IdConfiguracionEvento" = CENP."IdConfiguracionEvento" AND CENP."PermitePersonalizar" = 'S'
	WHERE CENP."TipoEntidadDestino" = 'POR' and CENP."Procesado" = 'N' AND CENP."Agrupa" = 'S' AND CENP."IdEstado" = 'ACT' and CENP."IdEstadoDetalle" = 'ACT'
	UNION ALL
	SELECT DISTINCT CENP."IdConfiguracionEvento",CENP."Agrupa",CENP."Procesado",CENP."IdEvento", CENP."IdPersonaDestinatario",
		CASE WHEN COALESCE(CCEP."EstadoConfiguracionPortafolio",'')= '' THEN 'S' 
			ELSE  CASE WHEN CCEP."EstadoConfiguracionPortafolio" = 'ACT' THEN 'S' ELSE 'N' END
		END AS "GenerarNotificacion"
		FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventosNoProcesados" CENP
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN ON CSN."IdPersona" = CENP."IdPersonaDestinatario"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoEntidad" CCEP ON CCEP."TipoEntidad" = 'CLI' AND CCEP."IdEntidad" = CSN."IdCliente" AND CCEP."IdConfiguracionEvento" = CENP."IdConfiguracionEvento" AND CENP."PermitePersonalizar" = 'S'
	WHERE CENP."TipoEntidadDestino" = 'CLI' and CENP."Procesado" = 'N' AND CENP."Agrupa" = 'S' AND CENP."IdEstado" = 'ACT' and CENP."IdEstadoDetalle" = 'ACT'
	UNION ALL
	SELECT DISTINCT CENP."IdConfiguracionEvento", CENP."Agrupa", CENP."Procesado", CENP."IdEvento", CENP."IdPersonaDestinatario", 'S' AS "GenerarNotificacion"
		FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventosNoProcesados" CENP
		INNER JOIN  "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN ON CSN."IdPersona" = CENP."IdPersonaDestinatario" 
	WHERE CENP."TipoEntidadDestino" = 'EMP' and CENP."Procesado" = 'N' AND CENP."Agrupa" = 'S' AND CENP."IdEstado" = 'ACT' and CENP."IdEstadoDetalle" = 'ACT';

	--Eventos NO agrupados que deban generar notificaciones
	v_eventosNoAgrupar =
	SELECT DISTINCT CENP."IdConfiguracionEvento",CENP."Agrupa",CENP."Procesado",CENP."IdEvento", 
		CASE WHEN COALESCE(CCEP."EstadoConfiguracionPortafolio",'')= '' THEN 'S' 
			ELSE  CASE WHEN CCEP."EstadoConfiguracionPortafolio" = 'ACT' THEN 'S' ELSE 'N' END
		END AS "GenerarNotificacion"
		FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventosNoProcesados" CENP
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN ON CSN."IdPersona" = CENP."IdPersonaDestinatario"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoEntidad" CCEP ON CCEP."TipoEntidad" = 'POR' AND CCEP."IdEntidad" = CENP."IdPortafolio" AND CCEP."IdConfiguracionEvento" = CENP."IdConfiguracionEvento" AND CENP."PermitePersonalizar" = 'S'
	WHERE CENP."TipoEntidadDestino" = 'POR' and CENP."Procesado" = 'N' AND CENP."Agrupa" = 'N' AND CENP."IdEstado" = 'ACT' and CENP."IdEstadoDetalle" = 'ACT'
	UNION ALL
	SELECT DISTINCT CENP."IdConfiguracionEvento",CENP."Agrupa",CENP."Procesado",CENP."IdEvento", 
		CASE WHEN COALESCE(CCEP."EstadoConfiguracionPortafolio",'')= '' THEN 'S' 
			ELSE  CASE WHEN CCEP."EstadoConfiguracionPortafolio" = 'ACT' THEN 'S' ELSE 'N' END
		END AS "GenerarNotificacion"
		FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventosNoProcesados" CENP
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN ON CSN."IdPersona" = CENP."IdPersonaDestinatario"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoEntidad" CCEP ON CCEP."TipoEntidad" = 'CLI' AND CCEP."IdEntidad" = CSN."IdCliente" AND CCEP."IdConfiguracionEvento" = CENP."IdConfiguracionEvento" AND CENP."PermitePersonalizar" = 'S'
	WHERE CENP."TipoEntidadDestino" = 'CLI' and CENP."Procesado" = 'N' AND CENP."Agrupa" = 'N' AND CENP."IdEstado" = 'ACT' and CENP."IdEstadoDetalle" = 'ACT'
	UNION ALL
	SELECT DISTINCT CENP."IdConfiguracionEvento", CENP."Agrupa", CENP."Procesado", CENP."IdEvento", 'S' AS "GenerarNotificacion"
		FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventosNoProcesados" CENP
		INNER JOIN  "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN ON csn."IdPersona" = cenp."IdPersonaDestinatario" 
	WHERE CENP."TipoEntidadDestino" = 'EMP' and CENP."Procesado" = 'N' AND CENP."Agrupa" = 'N' AND CENP."IdEstado" = 'ACT' and CENP."IdEstadoDetalle" = 'ACT';

	--Actualizar los eventos agrupados y no agrupados como ignorados en la tabla evento.
	UPDATE "BSCL.Plataforma.BD::EVT.Evento" E
	SET E."Procesado" = 'I'
	FROM :v_eventosNoAgrupar NA, "BSCL.Plataforma.BD::EVT.Evento" E
	WHERE NA."GenerarNotificacion" = 'N' AND NA."IdEvento" = E."IdEvento";
	
	UPDATE "BSCL.Plataforma.BD::EVT.Evento" E
	SET E."Procesado" = 'I'
	FROM :v_eventosAgrupar A, "BSCL.Plataforma.BD::EVT.Evento" E
	WHERE A."GenerarNotificacion" = 'N' AND A."IdEvento" = E."IdEvento";

	--Obtener solo los eventos a generar notificaciones.
	v_NoAgrupar = SELECT "IdConfiguracionEvento", "Agrupa", "Procesado", "IdEvento" FROM :v_eventosNoAgrupar WHERE "GenerarNotificacion" = 'S';
	v_Agrupar = SELECT "IdConfiguracionEvento", "Agrupa", "Procesado", "IdEvento", "IdPersonaDestinatario" FROM :v_eventosAgrupar WHERE "GenerarNotificacion" = 'S';

	--Generar variables de salida con los eventos a procesar.				
	p_IdEventoAgrupado =  SELECT "IdEvento" as "IdEventoAgrupado","IdConfiguracionEvento" FROM :v_Agrupar;
	p_IdEventoNoAgrupado = 	SELECT  "IdEvento" as "IdEventoNoAgrupado","IdConfiguracionEvento" FROM :v_NoAgrupar;
	
	SELECT COUNT(1) INTO contadorAgrupados FROM :v_Agrupar;
	SELECT COUNT(1) INTO contadorNoAgrupados FROM :v_NoAgrupar;
	
	IF :contadorAgrupados > 0 THEN

		v_notificaciones_des_agrupadas = 
		select 
			RANK() OVER (PARTITION BY a."IdPersonaDestinatario", a."IdConfiguracionEvento" ORDER BY a."IdEvento") AS "SEC", 
			a."IdConfiguracionEvento", 
			a."Agrupa", 
			a."Procesado", 
			a."IdEvento", 
			a."IdPersonaDestinatario"
		FROM :v_Agrupar a;
		
		v_notificaciones_pendientes = 
		SELECT DISTINCT N."IdNotificacion", N."IdConfiguracionEvento", E."IdPersonaDestinatario"
		FROM "BSCL.Plataforma.BD::EVT.Notificacion" N
		INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON ND."IdNotificacion" = N."IdNotificacion"
		INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" E ON E."IdEvento" = ND."IdEvento"
		WHERE "Enviado" = 'N';
		
		v_notificaciones_a_procesar = 
		SELECT nda."SEC", COALESCE(np."IdNotificacion",0)as "IdNotificacion", nda."IdConfiguracionEvento", nda."IdEvento", nda."IdPersonaDestinatario"
		FROM :v_notificaciones_des_agrupadas nda
		LEFT JOIN :v_notificaciones_pendientes np ON np."IdConfiguracionEvento" = nda."IdConfiguracionEvento" and np."IdPersonaDestinatario" = nda."IdPersonaDestinatario";

		--Si existen notificaciones pendientes para un tipo de notificacion.
		
		SELECT COUNT(*) INTO contadorPendientes from :v_notificaciones_a_procesar where "IdNotificacion" <> 0;
		IF :contadorPendientes > 0 THEN
			v_detalle_notificaciones = select n."IdNotificacion", n."IdEvento" from :v_notificaciones_a_procesar n  where "IdNotificacion" <> 0;
			CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarNotificacionDetalle"(p_IdEmpresa,p_IdUsuario,p_IdModulo,:v_detalle_notificaciones, p_msg_out);
			v_notificaciones_a_procesar = SELECT * from :v_notificaciones_a_procesar where "IdNotificacion" <> 0;
			--obtener el id de las notificaciones con nuevo detalle.
			p_idsNotificacion_nuevos = SELECT "IdNotificacion", "IdEvento" FROM :v_detalle_notificaciones;
		END IF;
		
		SELECT COUNT(*) INTO contadorNuevos from :v_notificaciones_a_procesar where "IdNotificacion" = 0;
		IF :contadorNuevos > 0 THEN
			--Ingresar solo una notificacion por cada evento - configuracion - destinatario.
			v_notificaciones = 	
			SELECT 
				nap."IdConfiguracionEvento", 
				nap."IdEvento" 
			FROM :v_notificaciones_a_procesar nap 
			WHERE nap."SEC" = 1;
			CALL"BSCL.Plataforma.BD.Servicios::EVT.AgregarNotificacion"(p_IdEmpresa,p_IdUsuario,p_IdModulo,:v_notificaciones, p_msg_out,:p_idsNotificacion);
			
			v_notificaciones_destinatario = 
			select 
				idn."IdNotificacion", 
				idn."IdConfiguracionEvento", 
				idn."IdEvento", 
				nda."IdPersonaDestinatario"
			FROM :v_notificaciones_des_agrupadas nda
			INNER JOIN :p_idsNotificacion idn ON idn."IdEvento" = nda."IdEvento"
			WHERE "SEC" = 1;
			
			v_nuevos_detalle = 
			select 
				nd."IdNotificacion", 
				nap."IdEvento" 
			FROM :v_notificaciones_a_procesar nap
			INNER JOIN :v_notificaciones_destinatario nd ON nd."IdConfiguracionEvento" = nap."IdConfiguracionEvento"
			INNER JOIN :v_notificaciones_des_agrupadas nda ON nda."IdEvento" = nd."IdEvento"
			WHERE nda."IdPersonaDestinatario" = nap."IdPersonaDestinatario" AND nap."SEC" <>1
			UNION ALL SELECT "IdNotificacion", "IdEvento" FROM :p_idsNotificacion;
			
			CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarNotificacionDetalle"(p_IdEmpresa,p_IdUsuario,p_IdModulo,:v_nuevos_detalle, p_msg_out);
		END IF;
	END IF;
	
 	IF :contadorNoAgrupados > 0 THEN
 	
 		v_NuevosNoAgrupa = 
		SELECT DISTINCT "IdConfiguracionEvento","IdEvento" FROM :v_NoAgrupar ;
		
 		CALL"BSCL.Plataforma.BD.Servicios::EVT.AgregarNotificacion"(p_IdEmpresa,p_IdUsuario,p_IdModulo,:v_NuevosNoAgrupa, p_msg_out,:p_idsNotificacion);
 		
 		v_NoAgrupaDetalle = 
		SELECT "IdNotificacion" ,"IdEvento" 
		FROM :p_idsNotificacion;
		
		CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarNotificacionDetalle"(p_IdEmpresa,p_IdUsuario,p_IdModulo,:v_NoAgrupaDetalle, p_msg_out);
  	END IF;
  	
    --obtener todas las notificaciones que no hayan sido enviadas  	
  	p_Notificacion = SELECT "IdNotificacion" FROM "BSCL.Plataforma.BD::EVT.Notificacion" WHERE "Enviado" = 'N';
	p_msg_out := 'OK';

END;