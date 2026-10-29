SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CierreBloqueoAutomaticoFondos";

CREATE PROCEDURE
--  Proposito: Cierre y Bloqueo Automatico de Fondos
--  Autor: Gat-Blac 
--  Fecha Creacion: 17/11/2016
"BSCL.FondoMutuo.BD.Servicios::CierreBloqueoAutomaticoFondos" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario								BIGINT,
    OUT p_out_Resultado 						TABLE(
    												"IdFondo"						NVARCHAR(40),
    												"EstadoAporteComercial"			NVARCHAR(1),
    												"EstadoRescateComercial"		NVARCHAR(1),
    												"EstadoReinversionComercial"	NVARCHAR(1),
    												"EstadoLiquidezComercial"		NVARCHAR(1),
    												"EstadoAporteBackoffice"		NVARCHAR(1),
    												"EstadoRescateBackoffice"		NVARCHAR(1),
    												"EstadoReinversionBackoffice"	NVARCHAR(1),
    												"EstadoLiquidezBackoffice"		NVARCHAR(1),
    												"BloqueoAporte"					NVARCHAR(1),
    												"BloqueoRescate"				NVARCHAR(1)
    												),
    OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
	)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			RESIGNAL;
		END;
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
		BEGIN
			select count(1) into p_out_ErroresContador 
			from :v_out_ErroresDetalle;
			select count(1) into p_out_AdvertenciasContador 
			from :v_out_AdvertenciasDetalle;
			
			v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_ErroresDetalle
									UNION ALL
									SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_AdvertenciasDetalle;
									
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdInterno",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdInterno",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo"; 
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
			DECLARE v_FechaSistema							NVARCHAR(10);
			DECLARE v_count_advertencias_agf				BIGINT;
			DECLARE v_count_errores_agf						BIGINT;
			DECLARE v_count_bloqueo							BIGINT;
			DECLARE v_hora_cierre_aporte_comercial			TIME;
			DECLARE v_hora_cierre_aporte_backoffice			TIME;
			DECLARE v_hora_cierre_rescate_comercial			TIME;
			DECLARE v_hora_cierre_rescate_backoffice		TIME;
			DECLARE v_hora_cierre_reinversion_comercial		TIME;
			DECLARE v_hora_cierre_reinversion_backoffice	TIME;
			DECLARE v_hora_cierre_liquidacion_comercial		TIME;
			DECLARE v_hora_cierre_liquidacion_backoffice	TIME;
			DECLARE v_FechaSistemaUTC						NVARCHAR(40);
			DECLARE v_FechaSistemaLOCAL						NVARCHAR(40);
			DECLARE v_FechaHoraSistemaUTC					NVARCHAR(40);
			DECLARE v_FechaHoraSistemaLOCAL					NVARCHAR(40);
			DECLARE v_FechaHoraUTC							NVARCHAR(40);
			DECLARE v_FechaHoraLOCAL						NVARCHAR(40);
			DECLARE v_HoraSistema							TIME;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CIERRE BLOQUEO AUTOMATICA FONDOS','INICIO','INICIO');
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
				p_IdEmpresa => :p_IdEmpresa,
				p_out_FechaSistemaUTC => :v_FechaSistemaUTC, 
				p_out_FechaSistemaLOCAL => :v_FechaSistemaLOCAL, 
				p_out_FechaHoraSistemaUTC => :v_FechaHoraSistemaUTC, 
				p_out_FechaHoraSistemaLOCAL => :v_FechaHoraSistemaLOCAL, 
				p_out_FechaHoraUTC => :v_FechaHoraUTC, 
				p_out_FechaHoraLOCAL => :v_FechaHoraLOCAL
			);
			
			SELECT TO_TIME(:v_FechaHoraLOCAL) into v_HoraSistema
			FROM DUMMY;
			
			v_fondos_estado_actual = SELECT "IdFondo",
											"EstadoAporteComercial",
											"EstadoAporteBackoffice",
											"EstadoRescateComercial",
											"EstadoRescateBackoffice",
											"EstadoReinversionComercial",
											"EstadoReinversionBackoffice",
											"EstadoLiquidezComercial",
											"EstadoLiquidezBackoffice",
											"IdEmpresa"
									 FROM  "BSCL.FondoMutuo.BD::FMM.Fondo"
									 WHERE 'A' IN ("EstadoAporteComercial",
											"EstadoAporteBackoffice",
											"EstadoRescateComercial",
											"EstadoRescateBackoffice",
											"EstadoReinversionComercial",
											"EstadoReinversionBackoffice",
											"EstadoLiquidezComercial",
											"EstadoLiquidezBackoffice");
			
			/*Se desbloquen los fondo que estan bloqueados que se deben desbloquear por fecha*/
			
			v_fondos_bloqueo = SELECT "IdFondo"
								   FROM "BSCL.FondoMutuo.BD::FMM.Fondo"
								   WHERE ("BloqueoAporte" = 'N' OR "BloqueoRescate" = 'N') AND "FechaBloqueoDesde" >= :v_FechaSistema AND "BloqueoIndefinido" = 'N';
			
			SELECT COUNT(1) into v_count_bloqueo
			FROM :v_fondos_bloqueo;
			
			IF 	v_count_bloqueo > 0 THEN
				
				UPDATE FONDOS
				SET "BloqueoAporte" = 'S',
					"BloqueoRescate" = 'S',
					"UltimoFechaHoraBloqueo" = CURRENT_UTCTIMESTAMP
				FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDOS
				WHERE ("BloqueoAporte" = 'N' OR "BloqueoRescate" = 'N') AND "FechaBloqueoDesde" >= :v_FechaSistema AND "BloqueoIndefinido" = 'N';
				
				CALL "BSCL.FondoMutuo.BD.Servicios::CrearRegistroBloqueoDesbloqueoFondoAgf"(
					p_IdEmpresa => :p_IdEmpresa,
					p_IdUsuario => :p_IdUsuario,
					p_IdModulo => 'FMM',
					p_EsManual => 'N',
					p_Fondo => :v_fondos_bloqueo,
					p_out_Resultado => :v_out_resultado_agf,
					p_out_AdvertenciasContador => :v_count_advertencias_agf,
					p_out_AdvertenciasDetalle => :v_advertencias_agf,
					p_out_ErroresContador => :v_count_errores_agf,
					p_out_ErroresDetalle => :v_errores_agf
				);
				
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", "Codigo", '' AS "ValorParametro", '' AS "NombreParametro"
							   FROM :v_errores_agf
							   UNION ALL SELECT * FROM :v_out_ErroresDetalle;
							   
			END IF;
						
			SELECT "Value" into v_hora_cierre_aporte_comercial
			FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"
			WHERE "Key" = 'HORA_CIERRE_APORTE_COMERCIAL';
			
			SELECT "Value" into v_hora_cierre_aporte_backoffice
			FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"
			WHERE "Key" = 'HORA_CIERRE_APORTE_BACKOFFICE';
			
			SELECT "Value" into v_hora_cierre_rescate_comercial
			FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"
			WHERE "Key" = 'HORA_CIERRE_RESCATE_COMERCIAL';
			
			SELECT "Value" into v_hora_cierre_rescate_backoffice
			FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"
			WHERE "Key" = 'HORA_CIERRE_RESCATE_BACKOFFICE';
			
			SELECT "Value" into v_hora_cierre_reinversion_comercial
			FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"
			WHERE "Key" = 'HORA_CIERRE_REINVERSION_COMERCIAL';
			
			SELECT "Value" into v_hora_cierre_reinversion_backoffice
			FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"
			WHERE "Key" = 'HORA_CIERRE_REINVERSION_BACKOFFICE';
			
			SELECT "Value" into v_hora_cierre_liquidacion_comercial
			FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"
			WHERE "Key" = 'HORA_CIERRE_LIQUIDEZ_COMERCIAL';
			
			SELECT "Value" into v_hora_cierre_liquidacion_backoffice
			FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"
			WHERE "Key" = 'HORA_CIERRE_LIQUIDEZ_BACKOFFICE';
			
			/**
			*	HORARIOS DEFAULT
			**/
			
			/*Actualizando Aporte Comercial DEFAULT*/
			UPDATE FONDO
			SET "EstadoAporteComercial" = 'C',
				"UltimoFechaHoraCierreAporteComercial" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioAporteDefault" = 'S' AND :v_HoraSistema >= :v_hora_cierre_aporte_comercial AND FONDO."EstadoAporteComercial" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Aporte Backoffice DEFAULT*/
			UPDATE FONDO
			SET "EstadoAporteBackoffice" = 'C',
				"UltimoFechaHoraCierreAporteBackOffice" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioAporteDefault" = 'S' AND :v_HoraSistema >= :v_hora_cierre_aporte_backoffice AND FONDO."EstadoAporteBackoffice" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Rescate Comercial DEFAULT*/
			UPDATE FONDO
			SET "EstadoRescateComercial" = 'C',
				"UltimoFechaHoraCierreRescateComercial" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioRescateDefault" = 'S' AND :v_HoraSistema >= :v_hora_cierre_rescate_comercial AND FONDO."EstadoRescateComercial" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Rescate Backoffice DEFAULT*/
			UPDATE FONDO
			SET "EstadoRescateBackoffice" = 'C',
				"UltimoFechaHoraCierreRescateBackOffice" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioRescateDefault" = 'S' AND :v_HoraSistema >= :v_hora_cierre_rescate_backoffice AND FONDO."EstadoRescateBackoffice" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Reinversion Comercial DEFAULT*/
			UPDATE FONDO
			SET "EstadoReinversionComercial" = 'C',
				"UltimoFechaHoraCierreReinversionComercial" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO 
			WHERE FONDO."UsaHorarioReinversionDefault" = 'S' AND :v_HoraSistema >= :v_hora_cierre_reinversion_comercial AND FONDO."EstadoReinversionComercial" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Reinversion Backoffice DEFAULT*/
			UPDATE FONDO
			SET "EstadoReinversionBackoffice" = 'C',
				"UltimoFechaHoraCierreReinversionBackOffice" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioReinversionDefault" = 'S' AND :v_HoraSistema >= :v_hora_cierre_reinversion_backoffice AND FONDO."EstadoReinversionBackoffice" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Reinversion Comercial DEFAULT*/
			UPDATE FONDO
			SET "EstadoLiquidezComercial" = 'C',
				"UltimoFechaHoraCierreLiquidezComercial" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioLiquidezDefault" = 'S' AND :v_HoraSistema >= :v_hora_cierre_liquidacion_comercial AND FONDO."EstadoLiquidezComercial" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Reinversion Backoffice DEFAULT*/
			UPDATE FONDO
			SET "EstadoLiquidezBackoffice" = 'C',
				"UltimoFechaHoraCierreLiquidezBackOffice" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioLiquidezDefault" = 'S' AND :v_HoraSistema >= :v_hora_cierre_liquidacion_backoffice AND FONDO."EstadoLiquidezBackoffice" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/**
			*	HORARIOS PERSONALIZADOS
			**/
			
			/*Actualizando Aporte Comercial DEFAULT*/
			UPDATE FONDO
			SET "EstadoAporteComercial" = 'C',
				"UltimoFechaHoraCierreAporteComercial" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioAporteDefault" = 'N' AND :v_HoraSistema >= "HoraCierreAporteComercial" AND FONDO."EstadoAporteComercial" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Aporte Backoffice DEFAULT*/
			UPDATE FONDO
			SET "EstadoAporteBackoffice" = 'C',
				"UltimoFechaHoraCierreAporteBackOffice" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioAporteDefault" = 'N' AND :v_HoraSistema >= "HoraCierreAporteBackoffice" AND FONDO."EstadoAporteBackoffice" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Rescate Comercial DEFAULT*/
			UPDATE FONDO
			SET "EstadoRescateComercial" = 'C',
				"UltimoFechaHoraCierreRescateComercial" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioRescateDefault" = 'N' AND :v_HoraSistema >=  "HoraCierreRescateComercial" AND FONDO."EstadoRescateComercial" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Rescate Backoffice DEFAULT*/
			UPDATE FONDO
			SET "EstadoRescateBackoffice" = 'C',
				"UltimoFechaHoraCierreRescateBackOffice" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioRescateDefault" = 'N' AND :v_HoraSistema >= "HoraCierreRescateBackoffice" AND FONDO."EstadoRescateBackoffice" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Reinversion Comercial DEFAULT*/
			UPDATE FONDO
			SET "EstadoReinversionComercial" = 'C',
				"UltimoFechaHoraCierreReinversionComercial" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioReinversionDefault" = 'N' AND :v_HoraSistema >= "HoraCierreReinversionComercial" AND FONDO."EstadoReinversionComercial" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Reinversion Backoffice DEFAULT*/
			UPDATE FONDO
			SET "EstadoReinversionBackoffice" = 'C',
				"UltimoFechaHoraCierreReinversionBackOffice" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioReinversionDefault" = 'N' AND :v_HoraSistema >= "HoraCierreReinversionBackoffice" AND FONDO."EstadoReinversionBackoffice" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Reinversion Comercial DEFAULT*/
			UPDATE FONDO
			SET "EstadoLiquidezComercial" = 'C',
				"UltimoFechaHoraCierreLiquidezComercial" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioLiquidezDefault" = 'N' AND :v_HoraSistema >= "HoraCierreLiquidezComercial" AND FONDO."EstadoLiquidezComercial" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			/*Actualizando Reinversion Backoffice DEFAULT*/
			UPDATE FONDO
			SET "EstadoLiquidezBackoffice" = 'C',
				"UltimoFechaHoraCierreLiquidezBackOffice" = CURRENT_UTCTIMESTAMP
			FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO
			WHERE FONDO."UsaHorarioLiquidezDefault" = 'N' AND :v_HoraSistema >= "HoraCierreLiquidezBackoffice" AND FONDO."EstadoLiquidezBackoffice" = 'A' AND :p_IdEmpresa = FONDO."IdEmpresa";
			
			v_fondos_cambiados = SELECT FONDO_ANTERIOR."IdFondo"
								 FROM :v_fondos_estado_actual AS FONDO_ANTERIOR
								 INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO_ACTUAL ON (FONDO_ANTERIOR."IdFondo" = FONDO_ACTUAL."IdFondo" 
								 																AND FONDO_ANTERIOR."IdEmpresa" = FONDO_ACTUAL."IdEmpresa")
								 WHERE 	  FONDO_ANTERIOR."EstadoAporteComercial" 		!= FONDO_ACTUAL."EstadoAporteComercial"
								 	   OR FONDO_ANTERIOR."EstadoAporteBackoffice" 		!= FONDO_ACTUAL."EstadoAporteBackoffice"
								 	   OR FONDO_ANTERIOR."EstadoRescateComercial" 		!= FONDO_ACTUAL."EstadoRescateComercial"
								 	   OR FONDO_ANTERIOR."EstadoRescateBackoffice" 		!= FONDO_ACTUAL."EstadoRescateBackoffice"
								 	   OR FONDO_ANTERIOR."EstadoReinversionComercial" 	!= FONDO_ACTUAL."EstadoReinversionComercial"
								 	   OR FONDO_ANTERIOR."EstadoReinversionBackoffice" 	!= FONDO_ACTUAL."EstadoReinversionBackoffice"
								 	   OR FONDO_ANTERIOR."EstadoLiquidezComercial" 		!= FONDO_ACTUAL."EstadoLiquidezComercial"
								 	   OR FONDO_ANTERIOR."EstadoLiquidezBackoffice" 	!= FONDO_ACTUAL."EstadoLiquidezBackoffice";
			
			
			
			v_log = SELECT 
							0 AS "IdCabeceraLog"
							,'MODIFICACION' AS "IdTipoLog"
							,'FMM' AS "IdModulo"
							,:p_IdUsuario AS "IdUsuario"
							,:p_IdEmpresa AS "IdEmpresa"
							,:v_FechaSistema AS "FechaSistemaLog"
							,:p_IdUsuario AS "IdUsuarioAtendido"
							,0  AS "IdPersona"
							,0  AS "IdCliente"
							,'' AS "EstadoCliente"
							,'' AS "NombreCliente"
							,'' AS "IdTipoDocumentoIdentidad"
							,'' AS "IdDocumentoIdentidad"
							,0 AS "IdPortafolio"
							,'' AS "EstadoPortafolio"
							,'CIERRE_BLOQUEO_AUTOMATICO_FMM' AS "IdTipoRegistro"
							,0 AS "IdRegistro"
							,'' AS "EstadoRegistro"
							,'' AS "IdTipoOperacion"
							,0 AS "Folio"
							,0 AS "Cantidad"
							,0 AS "PrecioTasa"
							,0 AS "Monto"
							,'' AS "IdMonedaOrigen"
							,'' AS "IdMonedaLiquidacion"
							,'' AS "TipoCambio"
							,"IdFondo" AS "Nemo"
							,0 AS "IdProceso"
							,'' AS "EstadoProceso"
							,'' AS "NombreProceso"
					FROM :v_fondos_cambiados;
		
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
			
			p_out_Resultado = SELECT "IdFondo",
									 "EstadoAporteComercial",
									 "EstadoRescateComercial",
									 "EstadoReinversionComercial",
									 "EstadoLiquidezComercial",
									 "EstadoAporteBackoffice",
									 "EstadoRescateBackoffice",
									 "EstadoReinversionBackoffice",
									 "EstadoLiquidezBackoffice",
									 "BloqueoAporte",
									 "BloqueoRescate"
							  FROM "BSCL.FondoMutuo.BD::FMM.Fondo";
							  
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CIERRE BLOQUEO AUTOMATICA FONDOS','FIN','FIN');
		END;
	END;
END;
