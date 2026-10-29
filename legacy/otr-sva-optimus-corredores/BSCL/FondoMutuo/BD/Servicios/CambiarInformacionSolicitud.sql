SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CambiarInformacionSolicitud";
GO
CREATE PROCEDURE
--  Proposito: Cambiar Fecha Curse y calcula las otras fechas dependiendo de las valutas.
--  Autor: - Gat-Blac 
--  Fecha Creacion: 21/06/2017
"BSCL.FondoMutuo.BD.Servicios::CambiarInformacionSolicitud"(
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa 						BIGINT,
    IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    IN p_UsaReglasNegocio 				NVARCHAR(1),
    
    IN p_SolicitudesAporteRescate		TABLE(
    										"IdInterno"					BIGINT,
    										"IdSolicitudAporteRescate"	BIGINT,
    										"Fecha"						NVARCHAR(10)
    									),
    OUT p_out_Resultado					TABLE(
    										"IdInterno"						BIGINT,
    										"IdSolicitudAporteRescate"		BIGINT,
    										"Fecha"							NVARCHAR(10),
    										"IdEstado"						NVARCHAR(3)
    									),
    OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
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
		
		DECLARE v_IdPaisSistema		BIGINT;
		DECLARE v_resultadoRegla 	NVARCHAR(40);
		DECLARE v_count_actualizar	BIGINT;
		DECLARE v_count_solicitudes BIGINT;
		DECLARE v_count				BIGINT;
		DECLARE v_count_fechas		BIGINT;
		
		SELECT "IdPais" INTO v_IdPaisSistema FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" WHERE "IdEmpresa" = :p_IdEmpresa;
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);

		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		SELECT COUNT(1) as "Count" into v_count_solicitudes
		FROM :p_SolicitudesAporteRescate;
		
		if (v_count_solicitudes = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") as "Count" into v_count
		FROM :p_SolicitudesAporteRescate
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		if (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		v_pre_fechas_solicitudes = SELECT "p_AporteRescate"."IdInterno",
								  "SerieFondo"."IdSerieFondo",
								  "Fecha" AS "FechaEmision",
								  CASE WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'APO'  THEN "Fondo"."EstadoAporteBackoffice"
									   WHEN "SolicitudAporteRescate"."TipoSolicitud" = 'RES'  THEN "Fondo"."EstadoRescateBackoffice" END as "EstadoFondo",
								  "Fondo"."EstadoLiquidezBackoffice" AS "EstadoLiquidacion",
								  CASE "SolicitudAporteRescate"."TipoSolicitud" WHEN 'APO' THEN "SerieFondo"."DiasRetencionesAporte" ELSE "SerieFondo"."DiasRetencionesRescate" END AS "NumeroDiasRetencion",
								  CASE "SolicitudAporteRescate"."TipoSolicitud" WHEN 'APO' THEN "SerieFondo"."DiasValutaValorCuotaAporte" ELSE "SerieFondo"."DiasValutaValorCuotaRescate" END AS "NumeroValutaValorCuota",
								  CASE "SolicitudAporteRescate"."TipoSolicitud" WHEN 'APO' THEN "SerieFondo"."DiasValutaCertificacionAporte" ELSE "SerieFondo"."DiasValutaCertificacionRescate" END AS "NumeroValutaCertificacion",
								  CASE "SolicitudAporteRescate"."TipoSolicitud" WHEN 'APO' THEN "SerieFondo"."DiasValutaCurseAporte" ELSE "SerieFondo"."DiasValutaCurseRescate" END AS "NumeroValutaCurse",
								  "SolicitudAporteRescate"."SolicitudBatch",
								  "p_AporteRescate"."IdSolicitudAporteRescate",
								  "SolicitudAporteRescate"."TipoSolicitud",
                                  "Fondo"."FondoExtranjero",
                                  "Fondo"."IdPaisOrigen" AS "IdPaisFondo",
                                  "Fondo"."IdPaisEmisor",
                                  "Moneda"."IdPais" AS "IdPaisMoneda",
								  CASE WHEN COALESCE("SolicitudAporteRescate"."IdSolicitudAporteRescate",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoSolicitud"
						   		FROM :p_SolicitudesAporteRescate "p_AporteRescate"
						   		INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate" ON ("p_AporteRescate"."IdSolicitudAporteRescate" = "SolicitudAporteRescate"."IdSolicitudAporteRescate")
						   		INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondo")
						   		INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("SerieFondo"."IdFondo" = "Fondo"."IdFondo")
						   		LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "Moneda" ON ("Moneda"."IdMoneda" = "Fondo"."IdMoneda");
		
		
		v_out_ErroresDetalle = 	SELECT "IdInterno",'NO_EXISTE_SOLICITUD_APORTE_RESCATE' AS"Codigo",'' AS "ValorParametro",'' AS "NombreParametro"
										FROM :v_pre_fechas_solicitudes
										WHERE "ResultadoSolicitud" = 'ERR';
		
		v_fechas_solicitudes = SELECT "IdInterno",
								  "IdSerieFondo",
								  "FechaEmision",
								  "EstadoFondo",
								  "EstadoLiquidacion",
								  "NumeroDiasRetencion",
								  "NumeroValutaValorCuota",
								  "NumeroValutaCertificacion",
								  "NumeroValutaCurse",
								  "SolicitudBatch",
                                  "TipoSolicitud",
                                  "FondoExtranjero",
                                  "IdPaisFondo",
                                  "IdPaisEmisor",
                                  "IdPaisMoneda" 
						   		FROM :v_pre_fechas_solicitudes
						   		WHERE "ResultadoSolicitud" = 'OK';
						   		
		SELECT COUNT(1) into v_count_fechas
		FROM :v_fechas_solicitudes;
		
		IF v_count_fechas = 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
						   		
		CALL "BSCL.FondoMutuo.BD.Utilitarios::CalcularFechaSolicitudAporteRescate"(
            p_IdEmpresa                 => :p_IdEmpresa,
            p_IdPais                    => :v_IdPaisSistema,
            p_SolicitudAporteRescate    => :v_fechas_solicitudes,
            p_FechasAporteRescate       => v_fechas_salida_solicitudes);
		
		v_fechas = SELECT 
						  "Solicitudes"."IdInterno",
						  "Solicitudes"."IdSolicitudAporteRescate",
						  "Solicitudes"."FechaEmision",
						  "FechasSolicitudes"."FechaAceptacion",
						  "FechasSolicitudes"."FechaValorCuota",
						  "FechasSolicitudes"."FechaCertificacion",
						  "FechasSolicitudes"."FechaLiquidacion",
						  CASE WHEN COALESCE("FechasSolicitudes"."IdInterno",-1) = -1 THEN 'ERR' ELSE 'OK' END AS "ResultadoFechas"  
						  FROM :v_pre_fechas_solicitudes "Solicitudes"
						  LEFT OUTER JOIN :v_fechas_salida_solicitudes AS "FechasSolicitudes" ON ("FechasSolicitudes"."IdInterno" = "Solicitudes"."IdInterno" AND "FechasSolicitudes"."IdSerieFondo" = "Solicitudes"."IdSerieFondo" AND "FechasSolicitudes"."FechaEmision" = "Solicitudes"."FechaEmision");
		
		
		v_out_ErroresDetalle = 	SELECT "IdInterno",'ERROR_AL_CALCULAR_FECHAS' AS"Codigo",'' AS "ValorParametro",'' AS "NombreParametro"
								FROM :v_fechas
								WHERE "ResultadoFechas" = 'ERR';
								
		SELECT COUNT(1) into v_count_actualizar
		FROM :v_fechas
		WHERE "ResultadoFechas" = 'OK';
		
		IF v_count_actualizar = 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;						
		
		UPDATE SOLICITUDES
		SET "FechaAceptacion" = Fecha."FechaAceptacion",
		    "FechaValorCuota" = Fecha."FechaValorCuota",
			"FechaCertificacion" = Fecha."FechaCertificacion",
			"FechaLiquidacion" = Fecha."FechaLiquidacion"
		FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOLICITUDES
		INNER JOIN :v_fechas AS Fecha ON (SOLICITUDES."IdSolicitudAporteRescate" = Fecha."IdSolicitudAporteRescate")
		WHERE Fecha."ResultadoFechas" = 'OK';
		
		
		p_out_Resultado = SELECT "IdInterno",
								 "IdSolicitudAporteRescate",
								 "FechaEmision" AS "Fecha",
								 'ERR' AS "IdEstado"
						  FROM :v_pre_fechas_solicitudes WHERE "ResultadoSolicitud" = 'ERR'
						  UNION ALL
						  SELECT "IdInterno",
								 "IdSolicitudAporteRescate",
								 "FechaEmision" AS "Fecha",
								 'ERR' AS "IdEstado"
						  FROM :v_fechas WHERE "ResultadoFechas" = 'ERR'
						  UNION ALL
						  SELECT "IdInterno",
								 "IdSolicitudAporteRescate",
								 "FechaEmision" AS "Fecha",
								 'OK' AS "IdEstado"
						  FROM :v_fechas WHERE "ResultadoFechas" = 'OK';
						  
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
GO	