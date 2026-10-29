SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ConsultarFechaSolicitudAporteRescate";
GO
CREATE PROCEDURE
--  Proposito: Calcula la fecha para una solicitud de aporte y rescate .
--  Autor: Gat-Blac 
--  Fecha Creacion: 30/09/2016
-------------------------------MODIFICACIONES-----------------------------------------------------------------
--  Proposito: Se agrega como parametro requerido  (TipoSolicitud","IdPaisFondo","IdPaisEmisor","IdPaisMoneda") por el utilitario BSCL.FondoMutuo.BD.Utilitarios::CalcularFechaSolicitudAporteRescate 
--             Se agrega como salida la fecha de valor cuota
--  Autor: OCT - Oscar Carreno Tolmo
--  Fecha Creacion: 04/12/2017

"BSCL.FondoMutuo.BD.Servicios::ConsultarFechaSolicitudAporteRescate" (
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa 								BIGINT,
	IN p_IdUsuario 								BIGINT,
	IN p_IdModulo 								NVARCHAR(3),
	IN p_SimularOperacion 						NVARCHAR(1),
    IN p_UsaReglasNegocio 						NVARCHAR(1),
    
    IN p_SolicitudAporteRescate					TABLE(
    												"IdInterno"			BIGINT,
    												"FechaEmision"		NVARCHAR(10),
    												"IdSerieFondo"		NVARCHAR(40),
    												"TipoSolicitud"		NVARCHAR(3)
    											),
  	OUT p_out_Resultado							TABLE(
  													"IdInterno"				BIGINT,
  													"IdSerieFondo"			NVARCHAR(40),
  													"FechaEmision"			DATE,
  													"FechaDigitacion"       DATE,
  													"FechaAceptacion"		DATE,
  													"FechaValorCuota"       DATE,
  													"FechaCertificacion"	DATE,
  													"FechaLiquidacion"		DATE
  												),
  	OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
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
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_log_certificacion		  = SELECT 0 AS "IdSolicitudAporteRescate", '' AS "Codigo", '' AS "IdEstado", '' AS "TipoSolicitud", '' AS "IdTipoMovimientoFondoMutuo", 0 AS "IdUsuario", 0 AS "IdEmpresa", '0001-01-01' AS "FechaSistema", '' AS "IdSerieFondo", '' AS "IdEmisor", 0 AS "Monto", '' AS "IdMoneda", 0 AS "MontoOriginal", 0 AS "TipoCambio", '' AS "IdMonedaOriginal", 0 AS "ValorCuota", 0 AS "NumeroCuotas", 0 AS "IdCliente", 0 AS "IdPortafolio", 'N' AS "Simulacion" FROM DUMMY WHERE 1=2; 
	END;
	
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE v_FechaSistema NVARCHAR(10);
		
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
		DECLARE v_resultadoRegla 	NVARCHAR(40);
		DECLARE v_count_solicitudes BIGINT;
		DECLARE v_Perfil			NVARCHAR(40);
		DECLARE v_IdPaisSistema 	BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CONSULTAR FECHA APORTE RESCATE','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		SELECT COUNT(1) as "Count" into v_count_solicitudes
		FROM :p_SolicitudAporteRescate AS "Solicitudes";
		
		if (v_count_solicitudes = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_SOLICITUDES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.FondoMutuo.BD.Utilitarios::ObtenerPerfilFondoMutuo"(p_IdUsuario, v_Perfil);
		IF v_Perfil = '' THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'PERFIL_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		SELECT "IdPais" INTO v_IdPaisSistema FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" WHERE "IdEmpresa" = :p_IdEmpresa;
		
		v_solicitudes = SELECT "Solicitudes"."IdInterno",
							   "Solicitudes"."IdSerieFondo",
							   "Solicitudes"."FechaEmision",
							   CASE WHEN "Solicitudes"."TipoSolicitud" = 'APO' AND v_Perfil = 'PERFIL_COMERCIAL'  THEN "Fondo"."EstadoAporteComercial"
													 WHEN "Solicitudes"."TipoSolicitud" = 'RES' AND v_Perfil = 'PERFIL_COMERCIAL'  THEN "Fondo"."EstadoRescateComercial"
													 WHEN "Solicitudes"."TipoSolicitud" = 'APO' AND v_Perfil = 'PERFIL_BACKOFFICE' THEN "Fondo"."EstadoAporteBackoffice"
													 WHEN "Solicitudes"."TipoSolicitud" = 'RES' AND v_Perfil = 'PERFIL_BACKOFFICE' THEN "Fondo"."EstadoRescateBackoffice"
							   END as "EstadoFondo",
							   CASE WHEN :v_Perfil = 'PERFIL_COMERCIAL'  THEN "Fondo"."EstadoLiquidezComercial"
													 WHEN :v_Perfil = 'PERFIL_BACKOFFICE' THEN "Fondo"."EstadoLiquidezBackoffice"
							   END as "EstadoLiquidacion",
							   CASE "Solicitudes"."TipoSolicitud" WHEN 'APO' THEN "SerieFondo"."DiasRetencionesAporte"
							   		WHEN 'RES' THEN "SerieFondo"."DiasRetencionesRescate"
							   		ELSE 0 END AS "NumeroDiasRetencion",
                               CASE "Solicitudes"."TipoSolicitud" WHEN 'APO' THEN "SerieFondo"."DiasValutaValorCuotaAporte" 
                                    WHEN 'RES' THEN "SerieFondo"."DiasValutaValorCuotaRescate"
                               ELSE 0
                               END AS "NumeroValutaValorCuota",
							   CASE "Solicitudes"."TipoSolicitud" WHEN 'APO' THEN "SerieFondo"."DiasValutaCertificacionAporte" 
							   		WHEN 'RES' THEN "SerieFondo"."DiasValutaCertificacionRescate"
							   ELSE 0
							   END AS "NumeroValutaCertificacion",
							   CASE "Solicitudes"."TipoSolicitud" WHEN 'APO' THEN "SerieFondo"."DiasValutaCurseAporte" 
							   		WHEN 'RES' THEN "SerieFondo"."DiasValutaCurseRescate"
							   ELSE 0 END AS "NumeroValutaCurse",
							   'N' AS "SolicitudBatch",
							   "Solicitudes"."TipoSolicitud",
							   "Fondo"."FondoExtranjero",
							   "Fondo"."IdPaisOrigen" AS "IdPaisFondo",
                               "Fondo"."IdPaisEmisor",
                               "Moneda"."IdPais" AS "IdPaisMoneda"
						FROM :p_SolicitudAporteRescate AS "Solicitudes"
						INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdSerieFondo" = "Solicitudes"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = :p_IdEmpresa)
						INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("SerieFondo"."IdFondo" = "Fondo"."IdFondo" AND "SerieFondo"."IdEmpresa" = "Fondo"."IdEmpresa")
						LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "Moneda" ON ("Moneda"."IdMoneda" = "Fondo"."IdMoneda");
						
		CALL "BSCL.FondoMutuo.BD.Utilitarios::CalcularFechaSolicitudAporteRescate"(
		  p_IdEmpresa                 =>:p_IdEmpresa,
		  p_IdPais                    =>:v_IdPaisSistema,
		  p_SolicitudAporteRescate    =>:v_solicitudes,
		  p_FechasAporteRescate       =>v_fechas_salida_solicitudes);
		
		p_out_Resultado = SELECT "IdInterno",
								 "IdSerieFondo",
								 "FechaEmision",
								 "FechaDigitacion",
								 "FechaAceptacion",
								 "FechaValorCuota",
								 "FechaCertificacion",
								 "FechaLiquidacion"
						 FROM :v_fechas_salida_solicitudes;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CONSULTAR FECHA APORTE RESCATE','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		END;
	END;
END;
GO