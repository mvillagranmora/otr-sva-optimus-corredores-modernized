SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ModificarComisionSolicitudPorPerfeccionamiento";

CREATE PROCEDURE
--  Proposito: Modificar Comision por perfeccionamiento para proceso de carga de solicitudes.
--  Autor: Gat-Blac 
--  Fecha Creacion: 25/10/2016
"BSCL.FondoMutuo.BD.Servicios::ModificarComisionSolicitudPorPerfeccionamiento" (
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
    IN p_SimularOperacion 						NVARCHAR(1),
    IN p_UsaReglasNegocio 						NVARCHAR(1),
	IN p_Solicitudes							TABLE (
													"IdInterno"					 BIGINT,
													"IdSolicitudAporteRescate"	 BIGINT,
													"IdCliente" 				 BIGINT,
											    	"IdPortafolio" 				 BIGINT,
											    	"IdSerieFondo" 		 		 NVARCHAR(40),
											    	"CalculoPorMontoCuota"		 NVARCHAR(1),
											    	"TipoSolicitud"				 NVARCHAR(3),
											    	"IdMoneda"					 NVARCHAR(3),
											    	"Monto"						 DECIMAL(26,8),
											    	"NumeroCuotas"				 DECIMAL(26,8),
											    	"MontoComision"				 DECIMAL(26,8)
												),
	OUT p_out_Resultado 						TABLE(
													"IdInterno"					 BIGINT,
													"IdSolicitudAporteRescate"	 BIGINT,
													"IdCliente"					 BIGINT,
													"IdPortafolio"				 BIGINT,
													"IdSerieFondo"				 NVARCHAR(40),
													"CalculoPorMontoCuota"		 NVARCHAR(1),
													"TipoSolicitud"				 NVARCHAR(3),
													"IdMoneda"					 NVARCHAR(3),
											    	"Monto"						 DECIMAL(26,8),
											    	"NumeroCuotas"				 DECIMAL(26,8),
											    	"MontoComision"				 DECIMAL(26,8)
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
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
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
			DECLARE v_resultadoRegla 	NVARCHAR(40);
			DECLARE v_count_solicitudes BIGINT;
			DECLARE v_count				BIGINT;
			DECLARE v_count_correctas	BIGINT;
			DECLARE v_FechaSistema		NVARCHAR(10);
			DECLARE v_count_valores		BIGINT;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'MODIFICAR COMISION SOLICITUD PERFECCIONAMIENTO','INICIO','INICIO');
		
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			if :v_resultadoRegla = 'RECH' then
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			end if;
			
			SELECT COUNT(1) as "Count" into v_count_solicitudes
			FROM :p_Solicitudes;
			
			IF (v_count_solicitudes = 0) THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_SOLICITUDES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			SELECT TOP 1 COUNT("IdInterno") as "Count" into v_count
			FROM :p_Solicitudes
			GROUP BY "IdInterno"
			ORDER BY "Count" DESC;
			
			IF (v_count > 1) THEN
				v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			v_solicitudes_pre_valor_cuota = SELECT 	"Solicitudes"."IdInterno"
												,:v_FechaSistema AS "Fecha"
											  	,"Solicitudes"."IdSerieFondo"
											  	,"Solicitudes"."CalculoPorMontoCuota"
											  	,"Solicitudes"."Monto"
											  	,"Solicitudes"."NumeroCuotas"
											  	,"Fondo"."IdMoneda" AS "IdMonedaFondo"
											  	,"Solicitudes"."IdMoneda" AS "IdMonedaInversion"
											  	,"Solicitudes"."TipoSolicitud" AS "TipoSolicitud"
											  	,'UF' AS "TipoFactorAjuste"
											  	,:p_IdEmpresa AS "IdEmpresa"
										FROM :p_Solicitudes  AS "Solicitudes"
										LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("Solicitudes"."IdSerieFondo" = "SerieFondo"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = :p_IdEmpresa)
										LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo" AND "Fondo"."IdEmpresa" = :p_IdEmpresa);
			
			CALL "BSCL.FondoMutuo.BD.Utilitarios::CalcularMontoNumeroCuota"(:p_IdEmpresa,:v_solicitudes_pre_valor_cuota,v_valor_cuota);
			
			v_out_ErroresDetalle = SELECT "IdInterno","CodigoMensaje" AS "Codigo",'' AS "ValorParametro",'' AS "NombreParametro" 
							   FROM :v_valor_cuota WHERE "Resultado" = 'ERR'
							   UNION SELECT * FROM :v_out_ErroresDetalle;
		
			v_solicitudes_valor_cuota = SELECT "Solicitudes".* 
										FROM :p_Solicitudes AS "Solicitudes"
										LEFT JOIN :v_valor_cuota AS "Valor" ON ("Valor"."IdInterno" = "Solicitudes"."IdInterno" AND "Valor"."Resultado" = 'ERR')
										WHERE COALESCE("Valor"."IdInterno",-1) = -1;
										
			SELECT COUNT(1) INTO v_count_valores FROM :v_solicitudes_valor_cuota;
			
			IF v_count_valores = 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			v_solicitudes_validacion = SELECT "p_Solicitudes"."IdInterno"
											  ,"p_Solicitudes"."IdSolicitudAporteRescate"
											  ,"p_Solicitudes"."IdCliente"
											  ,"p_Solicitudes"."IdPortafolio"
											  ,"p_Solicitudes"."IdSerieFondo"
											  ,"p_Solicitudes"."CalculoPorMontoCuota"
											  ,"p_Solicitudes"."TipoSolicitud"
											  ,"p_Solicitudes"."IdMoneda"
											  ,"p_Solicitudes"."Monto"
											  ,"p_Solicitudes"."NumeroCuotas"
											  ,"p_Solicitudes"."MontoComision"
											  ,CASE WHEN COALESCE("SolicitudAporteRescate"."IdSolicitudAporteRescate",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoIdSolicitudAporteRescate"
											  ,CASE WHEN "SolicitudAporteRescate"."IdCliente" = "p_Solicitudes"."IdCliente" THEN 'OK' ELSE 'ERR' END AS "ResultadoIdCliente"
											  ,CASE WHEN "SolicitudAporteRescate"."IdPortafolio" = "p_Solicitudes"."IdPortafolio" THEN 'OK' ELSE 'ERR' END AS "ResultadoIdPortafolio"
											  ,CASE WHEN "SolicitudAporteRescate"."IdMoneda" = "p_Solicitudes"."IdMoneda" THEN 'OK' ELSE 'ERR' END AS "ResultadoIdMoneda"
											  ,CASE WHEN "ValorCuota"."Monto" = "p_Solicitudes"."Monto" THEN 'OK' ELSE 'ERR' END AS "ResultadoMonto"
											  ,CASE WHEN "ValorCuota"."NumeroCuotas" = "p_Solicitudes"."NumeroCuotas" THEN 'OK' ELSE 'ERR' END AS "ResultadoNumeroCuotas"
										FROM :v_solicitudes_valor_cuota AS "p_Solicitudes"
										INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate" ON ("SolicitudAporteRescate"."IdSolicitudAporteRescate" = "p_Solicitudes"."IdSolicitudAporteRescate" AND "IdEmpresa" = :p_IdEmpresa)
										INNER JOIN :v_valor_cuota AS "ValorCuota" ON ("ValorCuota"."IdInterno" = "p_Solicitudes"."IdInterno" AND "ValorCuota"."Resultado" = 'OK');
			
			v_out_ErroresDetalle = SELECT "IdInterno",
										  CASE WHEN "ResultadoIdSolicitudAporteRescate" = 'ERR' THEN 'SOLICITUD_NO_EXISTE'
										  	   WHEN "ResultadoIdCliente" = 'ERR' THEN 'CLIENTE_INVALIDO'
										  	   WHEN "ResultadoIdPortafolio" = 'ERR' THEN 'PORTAFOLIO_INVALIDO'
										  	   WHEN "ResultadoIdMoneda" = 'ERR' THEN 'MONEDA_INVALIDO'
										  	   WHEN "ResultadoMonto" = 'ERR' THEN 'MONTO_INVALIDO'
										  	   WHEN "ResultadoNumeroCuotas" = 'ERR' THEN 'NUMERO_CUOTAS_INVALIDO'
										  END AS "Codigo",
										  '' AS "ValorParametro",
										  '' AS "NombreParametro"
									FROM :v_solicitudes_validacion
									WHERE 'ERR' IN ("ResultadoIdSolicitudAporteRescate","ResultadoIdCliente","ResultadoIdPortafolio","ResultadoIdMoneda"
													,"ResultadoMonto", "ResultadoNumeroCuotas");
			
			SELECT COUNT(1) into v_count_correctas
			FROM :v_solicitudes_validacion 
			WHERE 'ERR' NOT IN ("ResultadoIdSolicitudAporteRescate","ResultadoIdCliente","ResultadoIdPortafolio","ResultadoIdMoneda"
								,"ResultadoMonto", "ResultadoNumeroCuotas");
			
			IF v_count_correctas = 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			UPDATE SOLICITUDES
			SET SOLICITUDES."MontoComision" = P_SOLICITUDES."MontoComision",
				SOLICITUDES."MontoComisionLocal" = P_SOLICITUDES."MontoComision",
				SOLICITUDES."MontoPago" = TO_DECIMAL(ROUND(SOLICITUDES."MontoPago" - P_SOLICITUDES."MontoComision",8),26,8),
				SOLICITUDES."MontoPagoLocal" = TO_DECIMAL(ROUND(SOLICITUDES."MontoPagoLocal" - P_SOLICITUDES."MontoComision",8),26,8),
				SOLICITUDES."ComisionInformada" = 'S'
			FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SOLICITUDES
			INNER JOIN :v_solicitudes_validacion AS P_SOLICITUDES ON (SOLICITUDES."IdSolicitudAporteRescate" = P_SOLICITUDES."IdSolicitudAporteRescate")
			WHERE 'ERR' NOT IN ("ResultadoIdSolicitudAporteRescate","ResultadoIdCliente","ResultadoIdPortafolio","ResultadoIdMoneda"
								,"ResultadoMonto", "ResultadoNumeroCuotas");
			
			p_out_Resultado = SELECT "IdInterno"
									 ,"IdSolicitudAporteRescate"
									 ,"IdCliente"
									 ,"IdPortafolio"
									 ,"IdSerieFondo"
									 ,"CalculoPorMontoCuota"
									 ,"TipoSolicitud"
									 ,"IdMoneda"
									 ,"Monto"
									 ,"NumeroCuotas"
									 ,"MontoComision"
							FROM :v_solicitudes_validacion
							WHERE 'ERR' NOT IN ("ResultadoIdSolicitudAporteRescate","ResultadoIdCliente","ResultadoIdPortafolio","ResultadoIdMoneda"
								,"ResultadoMonto", "ResultadoNumeroCuotas");
			
													  
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'MODIFICAR COMISION SOLICITUD PERFECCIONAMIENTO','FIN','FIN');
			
			SIGNAL SALIDA_CONTROLADA;
			
		END;
	END;
END;
