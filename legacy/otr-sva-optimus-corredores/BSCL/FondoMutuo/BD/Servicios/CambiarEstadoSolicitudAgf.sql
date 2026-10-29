SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CambiarEstadoSolicitudAgf";

CREATE PROCEDURE
--  Proposito: Modificar Estado de Solicitud informada a la AGF
--  Autor: - Gat-Blac 
--  Fecha Creacion: 09/11/2016
"BSCL.FondoMutuo.BD.Servicios::CambiarEstadoSolicitudAgf"(
	IN 	p_IdEmpresa				   	BIGINT,
	IN  p_SolicitudesAGF 			TABLE(
											"IdInterfazAgf"	BIGINT,
											"IdEstado"		NVARCHAR(3),
											"Observacion"	NVARCHAR(500)
										),
	OUT p_out_Resultado				TABLE(
										"IdInterfazAgf" 	BIGINT, 
										"Resultado" 		NVARCHAR(1), 
										"Codigo" 			NVARCHAR(40)
									),
	OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdInterfazAgf"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdInterfazAgf"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterfazAgf" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterfazAgf" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterfazAgf" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterfazAgf" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE v_FechaSistema NVARCHAR(10);
		
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			DROP TABLE #RESULTADO_CambiarEstadoSolicitudAporteRescateAGFMasivo;
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
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdInterfazAgf",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdInterfazAgf",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo";
							
			DROP TABLE #RESULTADO_CambiarEstadoSolicitudAporteRescateAGFMasivo; 
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
	
		BEGIN
			DECLARE v_count_solicitudes		BIGINT;
			DECLARE v_solicitudes_repetidas	BIGINT;
			
			CREATE LOCAL TEMPORARY TABLE #RESULTADO_CambiarEstadoSolicitudAporteRescateAGFMasivo (
				"IdInterfazAgf" BIGINT, 
				"Resultado" NVARCHAR(1), 
				"Codigo" NVARCHAR(40),
				"IdEstado" NVARCHAR(3),
				"Observacion" NVARCHAR(500)
			);
			
			SELECT COUNT(1) as "Count" into v_count_solicitudes
			FROM :p_SolicitudesAGF;
					
			IF v_count_solicitudes = 0 THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterfazAgf", 'NO_SE_INGRESARON_SOLICITUDES' AS "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			SELECT TOP 1 COUNT("IdInterfazAgf") as "Count" into v_solicitudes_repetidas
			FROM :p_SolicitudesAGF
			GROUP BY "IdInterfazAgf"
			ORDER BY "Count" DESC;
			
			IF v_solicitudes_repetidas > 1 THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterfazAgf", 'SOLICITUDES_REPETIDAS' AS "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		
			INSERT INTO #RESULTADO_CambiarEstadoSolicitudAporteRescateAGFMasivo("IdInterfazAgf", "Resultado", "Codigo", "IdEstado", "Observacion")
			SELECT p_sol."IdInterfazAgf", 'N' as "Resultado", 'CAMBIO_ESTADO_INVALIDO' as "Codigo", p_sol."IdEstado" AS "IdEstado", "Observacion"
			FROM :p_SolicitudesAGF p_sol;
			
			UPDATE RES
			SET "Resultado"='S' ,"Codigo" = 'CAMBIO_ESTADO_OK'
			FROM #RESULTADO_CambiarEstadoSolicitudAporteRescateAGFMasivo RES
			INNER JOIN "BSCL.FondoMutuo.BD::FMM.InterfazAgf" sol ON (sol."IdInterfazAgf" = RES."IdInterfazAgf")
			WHERE ((sol."IdEstado" ='PEN' AND RES."IdEstado" = 'RES') OR (sol."IdEstado" ='ERR' AND RES."IdEstado" = 'PEN') OR (sol."IdEstado" ='RES' AND RES."IdEstado" IN ('PRO','ERR')))
			AND sol."IdEmpresa" = :p_IdEmpresa;
			
			UPDATE SOLICITUD_AGF
			SET SOLICITUD_AGF."IdEstado" = RES."IdEstado",
				SOLICITUD_AGF."Observacion" = RES."Observacion"
			FROM "BSCL.FondoMutuo.BD::FMM.InterfazAgf" AS SOLICITUD_AGF
			INNER JOIN #RESULTADO_CambiarEstadoSolicitudAporteRescateAGFMasivo RES ON (RES."IdInterfazAgf" = SOLICITUD_AGF."IdInterfazAgf")
			WHERE RES."Resultado" = 'S' AND SOLICITUD_AGF."IdEmpresa" = :p_IdEmpresa;
			
			--ACTUALIZAMOS LOS RELACIONADOS SI EL PADRE TUVO ERRORES
			UPDATE SOLICITUD_AGF
			SET SOLICITUD_AGF."IdEstado" = 'ERR',
				SOLICITUD_AGF."Observacion" = RES."Observacion"
			FROM "BSCL.FondoMutuo.BD::FMM.InterfazAgf" AS SOLICITUD_AGF
			INNER JOIN "BSCL.FondoMutuo.BD::FMM.InterfazAgf" AS SOLICITUD_AGF_PADRE ON (SOLICITUD_AGF."IdSolicitudAporteRescate" = SOLICITUD_AGF_PADRE."IdSolicitudAporteRescateRelacionado")
			INNER JOIN #RESULTADO_CambiarEstadoSolicitudAporteRescateAGFMasivo RES ON (RES."IdInterfazAgf" = SOLICITUD_AGF_PADRE."IdInterfazAgf")
			WHERE SOLICITUD_AGF."IdEstado" = 'PEN' AND RES."Resultado" = 'S' AND RES."IdEstado" = 'ERR' AND SOLICITUD_AGF."IdEmpresa" = :p_IdEmpresa;
			
			p_out_Resultado = SELECT "IdInterfazAgf", "Resultado", "Codigo" FROM #RESULTADO_CambiarEstadoSolicitudAporteRescateAGFMasivo;
			
			v_out_ErroresDetalle = SELECT "IdInterfazAgf", "Codigo", '' AS "ValorParametro", '' as "NombreParametro"
								   FROM #RESULTADO_CambiarEstadoSolicitudAporteRescateAGFMasivo WHERE "Resultado" = 'N';
			
			SIGNAL SALIDA_CONTROLADA;
		END;
	END;
END;
	
	
