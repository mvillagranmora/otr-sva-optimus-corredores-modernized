SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearSolicitudAportePorDividendos";
GO
CREATE PROCEDURE
--  Proposito: Crear Solicitud de Aporte por dividendos.
--  Autor: Gat-Blac 
--  Fecha Creacion: 26/10/2016
"BSCL.FondoMutuo.BD.Servicios::CrearSolicitudAportePorDividendos"(
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT, 
    IN p_IdModulo 								NVARCHAR(3),
    IN p_SimularOperacion 						NVARCHAR(1),
    IN p_UsaReglasNegocio 						NVARCHAR(1),
    IN p_IdTipoCanalIngreso                     NVARCHAR(3) DEFAULT 'AGE',--OPTIMUS(AGE),WEBSERVICE(WSR),FIORI(AGE),CLIENTE(CLI)
    IN p_SolicitudesAporte						TABLE (
													"IdInterno"					 BIGINT,
													"IdCliente" 				 BIGINT,
											    	"IdPortafolio" 				 BIGINT,
											    	"IdSerieFondo" 				 NVARCHAR(40),
													"Monto" 					 DECIMAL(26,8),
													"NumeroCuotas" 				 DECIMAL(26,8),
											    	"IdMoneda" 					 NVARCHAR(3),
											    	"FolioContrato"              BIGINT--oct
												 ),
	OUT p_out_Resultado							TABLE (
													"IdInterno" 				 BIGINT,
													"IdSolicitudAporteRescate" 	 BIGINT,
													"TipoSolicitud" 			 NVARCHAR(3),
													"IdSerieFondo" 				 NVARCHAR(40),
													"FechaAceptacion"			 DATE,
													"FechaValorCuota"            DATE,
													"FechaCertificacion" 		 DATE,
													"FechaLiquidacion"			 DATE,
													"FechaIngresoRegistro" 		 TIMESTAMP,
													"Monto" 					 DECIMAL(26,8),
													"NumeroCuotas" 				 DECIMAL(26,8),
													"ValorCuota" 				 DECIMAL(26,8),
													"CostoMonedaLocal" 			 DECIMAL(26,8),
													"ParidadFechaInversion" 	 DECIMAL(26,8),
													"IdMoneda" 					 NVARCHAR(3),
													"MontoComision" 			 DECIMAL(26,8),
													"MontoPago" 				 DECIMAL(26,8),
													"MontoComisionLocal"		 DECIMAL(26,8),
													"MontoPagoLocal"		 	 DECIMAL(26,8),
													"FolioContrato"              BIGINT--oct
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
		
		DECLARE v_IdPaisSistema 	BIGINT;
		DECLARE v_FechaSistema 		NVARCHAR(10);
		DECLARE v_resultadoRegla 	NVARCHAR(40);
		DECLARE v_count_solicitudes BIGINT;
		DECLARE v_count				BIGINT;
		DECLARE v_count_valores		BIGINT;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CREAR SOLICITUD APORTE POR DIVIDENDOS','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		SELECT COUNT(1) as "Count" into v_count_solicitudes
		FROM :p_SolicitudesAporte;
		
		IF (v_count_solicitudes = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_SOLICITUDES_APORTE' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") as "Count" into v_count
		FROM :p_SolicitudesAporte
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		IF (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		SELECT "IdPais" INTO v_IdPaisSistema FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" WHERE "IdEmpresa" = :p_IdEmpresa;
		
		v_solicitudesAporte_maestro = SELECT
											 	"SolicitudesAporte"."IdInterno",
											 	"SolicitudesAporte"."IdCliente",
											 	"SolicitudesAporte"."IdPortafolio",
											 	'APO' AS "TipoSolicitud",
											 	"SerieFondo"."IdSerieFondo",
												'APODIV' AS "IdTipoMovimientoFondoMutuo",
												0  AS "IdDividendo",
												'' AS "IdSerieFondoDestino",
												'' AS "IdEmisorDestino",
												0  AS "IdClienteDestino",
												0  AS "IdPortafolioDestino",
												'N' AS "IndicadorMLT",
												'N' AS "Overnight",
												0   AS "IdSolicitudAporteRescateRelacionado",
												''  AS "IdTipoCartera",
												'C' AS "CalculoPorMontoCuota",
												:v_FechaSistema AS "FechaEmision",
												:v_FechaSistema as "FechaOriginalInversion",
												CURRENT_UTCTIMESTAMP as "FechaIngresoRegistro",
												CURRENT_UTCTIMESTAMP as "FechaAceptacionRegistro",
												"SolicitudesAporte"."IdMoneda",
												0 AS "MontoComision",
												0 AS "MontoComisionLocal",
												'ING' AS "Estado",
												'CIN' AS "IdMedioDePago",
												'' AS "Glosa",
												"SerieFondo"."NombreSerieFondo",
												"SerieFondo"."CodigoSerie",
												"SerieFondo"."IdEstado" AS "IdEstadoSerie",
												CASE WHEN ("Fondo"."IdMoneda" != "SolicitudesAporte"."IdMoneda" AND "SerieFondo"."Diferido" = 'S') OR ("Fondo"."IdMoneda" = "SolicitudesAporte"."IdMoneda") THEN 'S'
													 ELSE 'N' END AS "EsDiferido",
												'S' AS "EsMontoMaximo",
												'S' AS "EsMontoMinimo",
												"Fondo"."MoneyMarket" AS "EsMoneyMarket",
												"Fondo"."IdFondo",
												"Fondo"."Nemotecnico",
												"Fondo"."NombreFondo",
												"Fondo"."MoneyMarket",
												:v_FechaSistema AS "FechaSistema",
												"Fondo"."BloqueoRescate" AS "EstadoBloqueo",
												'A' as "EstadoFondo",
												'A' as "EstadoLiquidez",
												'S'  AS "EsFondoValido",
												:p_IdEmpresa as "IdEmpresa",
												:p_IdUsuario as "IdUsuario",
												:p_IdModulo as "IdModulo",
												"DiasRetencionesAporte" AS "NumeroDiasRetencion",
												"DiasValutaValorCuotaAporte" AS "NumeroValutaValorCuota",
												"DiasValutaCertificacionAporte" AS "NumeroValutaCertificacion",
												"DiasValutaCurseAporte" AS "NumeroValutaCurse",
												'S' AS "SolicitudBatch",
												'S' AS "OrigenOperacion",
												"Fondo"."FechaFondo",
												"SolicitudesAporte"."Monto" AS "MontoSolicitud",
												"SolicitudesAporte"."NumeroCuotas" AS "NumeroCuotasSolicitud",
												"Fondo"."IdMoneda" AS "IdMonedaFondo",
                                                "Fondo"."FondoExtranjero",
                                                "Fondo"."IdPaisOrigen" AS "IdPaisFondo",
                                                "Fondo"."IdPaisEmisor",
                                                "Moneda"."IdPais" AS "IdPaisMoneda",
                                                "SolicitudesAporte"."FolioContrato"
											FROM :p_SolicitudesAporte AS "SolicitudesAporte"
											LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SolicitudesAporte"."IdSerieFondo" = "SerieFondo"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = :p_IdEmpresa)
											LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo" AND "Fondo"."IdEmpresa" = :p_IdEmpresa)
											LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS "Moneda" ON ("Moneda"."IdMoneda" = "Fondo"."IdMoneda");
		
		v_fechas_solicitudes = SELECT "IdInterno",
								  "IdSerieFondo",
								  "FechaFondo" AS "FechaEmision",
								  "EstadoFondo",
								  "EstadoLiquidez" AS "EstadoLiquidacion",
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
						   		FROM :v_solicitudesAporte_maestro;
		
		CALL "BSCL.FondoMutuo.BD.Utilitarios::CalcularFechaSolicitudAporteRescate"(
            p_IdEmpresa                 => :p_IdEmpresa,
            p_IdPais                    => :v_IdPaisSistema,
            p_SolicitudAporteRescate    => :v_fechas_solicitudes,
            p_FechasAporteRescate       => v_fechas_salida_solicitudes);
		
		v_solicitudesAporteRescate_maestroFecha = SELECT "Solicitudes_Maestro".*,
														 "FechasAportes"."FechaAceptacion",
														 "FechasAportes"."FechaValorCuota",
														 "FechasAportes"."FechaCertificacion",
														 "FechasAportes"."FechaLiquidacion"
												  FROM :v_solicitudesAporte_maestro AS "Solicitudes_Maestro"
												  LEFT OUTER JOIN :v_fechas_salida_solicitudes AS "FechasAportes" ON ("FechasAportes"."IdInterno" = "Solicitudes_Maestro"."IdInterno" AND "FechasAportes"."IdSerieFondo" = "Solicitudes_Maestro"."IdSerieFondo" AND "FechasAportes"."FechaEmision" = "Solicitudes_Maestro"."FechaFondo");
		
		v_solicitudes_pre_valor_cuota_aporte = SELECT 
												 "Solicitudes"."IdInterno"
												,"Solicitudes"."FechaAceptacion" AS "Fecha"
											  	,"Solicitudes"."IdSerieFondo"
											  	,'C' AS "CalculoPorMontoCuota"
											  	,"Solicitudes"."MontoSolicitud" AS "Monto" 
											  	,"Solicitudes"."NumeroCuotasSolicitud" AS "NumeroCuotas"
											  	,"Solicitudes"."IdMonedaFondo" AS "IdMonedaFondo"
											  	,"Solicitudes"."IdMoneda" AS "IdMonedaInversion"
											  	,'APO' AS "TipoSolicitud"
											  	,'UF' AS "TipoFactorAjuste"
											  	,"Solicitudes"."IdEmpresa"
										FROM :v_solicitudesAporteRescate_maestroFecha AS "Solicitudes";
		
		CALL "BSCL.FondoMutuo.BD.Utilitarios::CalcularMontoNumeroCuota"(:p_IdEmpresa,:v_solicitudes_pre_valor_cuota_aporte,v_valores_cuota_aporte);
		
		v_out_ErroresDetalle = SELECT "IdInterno","CodigoMensaje" AS "Codigo",'' AS "ValorParametro",'' AS "NombreParametro" 
							   FROM :v_valores_cuota_aporte WHERE "Resultado" = 'ERR'
							   UNION SELECT * FROM :v_out_ErroresDetalle;
		
		v_solicitudes_valor_cuota = SELECT "SolicitudesAporte".* 
									FROM :v_solicitudesAporteRescate_maestroFecha AS "SolicitudesAporte"
									LEFT JOIN :v_valores_cuota_aporte AS "Valor" ON ("Valor"."IdInterno" = "SolicitudesAporte"."IdInterno" AND "Valor"."Resultado" = 'ERR')
									WHERE COALESCE("Valor"."IdInterno",-1) = -1;
									
		SELECT COUNT(1) INTO v_count_valores FROM :v_solicitudes_valor_cuota;
		
		IF v_count_valores = 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		v_solicitudes_maestro_final = SELECT 	"SolicitudesRescate".*,
											 	"ValorCuota"."Monto" AS "Monto",
												"ValorCuota"."NumeroCuotas" AS "NumeroCuotas",
												"ValorCuota"."ValorCuota",
												"ValorCuota"."CostoEnFactorAjuste" AS "CostoUF",
												"ValorCuota"."MontoDestino" AS "CostoMonedaLocal",
												"ValorCuota"."ValorFactorAjuste" AS "ValorUFFechaInversion",
												"ValorCuota"."Monto" AS "MontoPago",
												"ValorCuota"."MontoDestino" AS "MontoPagoLocal",
												"ValorCuota"."IdMonedaSistema",
												"ValorCuota"."MontoMonedaSistema",
												CASE WHEN "SolicitudesRescate"."IdMoneda" = "SolicitudesRescate"."IdMonedaFondo" THEN 1 ELSE "ValorCuota"."ValorParidad" END AS "ParidadFechaInversion"
									FROM :v_solicitudes_valor_cuota AS "SolicitudesRescate"
									LEFT OUTER JOIN :v_valores_cuota_aporte AS "ValorCuota" ON ("SolicitudesRescate"."IdInterno" = "ValorCuota"."IdInterno" AND "ValorCuota"."Resultado" = 'OK');	
		
		
		v_solicitudesAportePrevalidacion = SELECT "SolicitudRescateMaestro"."IdInterno",
												  "SolicitudRescateMaestro"."IdCliente",
												  "SolicitudRescateMaestro"."IdPortafolio",
												  "SolicitudRescateMaestro"."IdSerieFondo",
												  "TipoMovimiento"."IdTipoMovimientoFondoMutuo",
												  "SolicitudRescateMaestro"."IdEstadoSerie",
												  "SolicitudRescateMaestro"."Monto",
												  "SolicitudRescateMaestro"."NumeroCuotas",
												  "SolicitudRescateMaestro"."EstadoFondo",
												  "SolicitudRescateMaestro"."EstadoBloqueo",
												  "SolicitudRescateMaestro"."EsFondoValido",
												  'S' AS "EsFechaValidaAporte",
												  'S' AS "EsFechaValidaRescate",
												  "SolicitudRescateMaestro"."EsDiferido",
												  "SolicitudRescateMaestro"."EsMontoMaximo",
												  "SolicitudRescateMaestro"."EsMontoMinimo",
												  "SolicitudRescateMaestro"."ValorCuota",
												  "SolicitudRescateMaestro"."Overnight",
												  ''  AS "EstadoLiquidez",
												  'S' AS "EsMoneyMarket",
												  'S' AS "EsFamiliaEquivalente",
												  'S' AS "EsPortafolioRegimenValido",
												  'S' AS "EsRegimenValido",
												  'S' AS "PermiteRescateMandato",
												  'S' AS "PermiteCambioGrupo",
												  'S' AS "EsPortafolioValido",
												  'S' AS "EsCuentaInversionValido"
											FROM  :v_solicitudes_maestro_final AS "SolicitudRescateMaestro"
											LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimiento" ON ("TipoMovimiento"."IdTipoMovimientoFondoMutuo" = "SolicitudRescateMaestro"."IdTipoMovimientoFondoMutuo");
		
		v_portafoliosPrevalidacion = 	SELECT  sol."IdInterno", 
												sol."IdPortafolio", 
												port."IdPortafolio" 		as "IdPortafolioReal", 
												port."IdEstado" 			as "IdEstadoPortafolio",
												port."IdCliente", 
												cli."IdCliente" 			as "IdClienteReal",
												cli."IdEstado" 				as "IdEstadoCliente", 
												0							as "IdClienteDestino",
												0							as "IdClienteRealDestino",
												0							as "IdPortafolioDestino",
												0						    as "IdPortafolioRealDestino",
												''						    as "IdEstadoClienteDestino",
												''						    as "IdEstadoPortafolioDestino",
												cli."DocumentoIdentidad",
												cli."IdEmpleadoEjecutivo" 	as "IdEmpleadoPortafolio"
										FROM :v_solicitudesAportePrevalidacion sol
										LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port on sol."IdPortafolio" = port."IdPortafolio"
										LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on port."IdCliente" = cli."IdCliente";
		
		CALL "BSCL.FondoMutuo.BD.Reglas::ServicioCumpleRequisitosCrearSolicitudAporteRescate"(:v_solicitudesAportePrevalidacion,:v_portafoliosPrevalidacion,:v_out_SalidaDetalleAporte,:v_out_SalidaAporte);
		
		v_out_AdvertenciasDetalle = SELECT "IdInterno" ,"Codigo",'N' as "RequiereAutorizacion","ValorParametro","NombreParametro"
										FROM :v_out_SalidaDetalleAporte
										WHERE "Resultado" = 'ADV'
										UNION ALL
										SELECT "IdInterno" ,"Codigo",'S' as "RequiereAutorizacion","ValorParametro","NombreParametro"
										FROM :v_out_SalidaDetalleAporte
										WHERE "Resultado" = 'AUT';
										
		v_out_ErroresDetalle = 		SELECT "IdInterno" ,"Codigo","ValorParametro","NombreParametro"
										FROM :v_out_SalidaDetalleAporte
										WHERE "Resultado" = 'ERR';
		
		v_solicitudes_Aporte = SELECT Solicitudes_Maestro.* , port."IdEstado" AS "IdEstadoPortafolio", cli."IdEstado" AS "IdEstadoCliente", cli."NombreCompleto", cli."DocumentoIdentidad", cli."TipoDocumentoIdentidad"
								FROM :v_solicitudes_maestro_final Solicitudes_Maestro
								INNER JOIN :v_solicitudesAportePrevalidacion Solicitudes on Solicitudes_Maestro."IdInterno" = Solicitudes."IdInterno"
								INNER JOIN :v_out_SalidaAporte SalidaAporte on SalidaAporte."IdInterno" = Solicitudes."IdInterno" 
								LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port on Solicitudes_Maestro."IdPortafolio" = port."IdPortafolio"
								LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on port."IdCliente" = cli."IdCliente"
								WHERE SalidaAporte."Resultado" IN ('ADV','AUT','OK');
		
		SELECT COUNT(1) as "count" into v_count_solicitudes
		FROM :v_solicitudes_Aporte;
		
		IF (v_count_solicitudes = 0) THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		IF :p_SimularOperacion = 'N' THEN
			
			v_id_solicitudesAporte = SELECT "Solicitudes"."IdInterno","BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate".NEXTVAL as "IdSolicitudAporteRescate"
									  FROM :v_solicitudes_Aporte AS "Solicitudes";
			
			v_solicitudesAporte_finales = SELECT
													"Solicitudes_Maestro"."IdInterno",
													"Id_Solicitudes"."IdSolicitudAporteRescate",
													"Solicitudes_Maestro"."IdCliente",
													"Solicitudes_Maestro"."IdPortafolio",
													"Solicitudes_Maestro"."IdEstadoPortafolio",
													"Solicitudes_Maestro"."IdEstadoCliente",
													"Solicitudes_Maestro"."NombreCompleto",
													"Solicitudes_Maestro"."DocumentoIdentidad",
													"Solicitudes_Maestro"."TipoDocumentoIdentidad",
													"Solicitudes_Maestro"."TipoSolicitud",
													"Solicitudes_Maestro"."IdSerieFondo",
													"Solicitudes_Maestro"."IdTipoMovimientoFondoMutuo",
													"Solicitudes_Maestro"."IdDividendo",
													"Solicitudes_Maestro"."IdSerieFondoDestino",
													"Solicitudes_Maestro"."IdEmisorDestino",
													"Solicitudes_Maestro"."IdClienteDestino",
													"Solicitudes_Maestro"."IdPortafolioDestino",
													"Solicitudes_Maestro"."IndicadorMLT",
													"Solicitudes_Maestro"."Overnight",
													0 AS "IdSolicitudAporteRescateRelacionado",
													"Solicitudes_Maestro"."IdTipoCartera",
													"Solicitudes_Maestro"."CalculoPorMontoCuota",
													"Solicitudes_Maestro"."FechaEmision",
													"Solicitudes_Maestro"."FechaAceptacion",
													"Solicitudes_Maestro"."FechaValorCuota",
													"Solicitudes_Maestro"."FechaCertificacion",
													"Solicitudes_Maestro"."FechaLiquidacion",
													"Solicitudes_Maestro"."FechaOriginalInversion",
													"Solicitudes_Maestro"."FechaIngresoRegistro",
													"Solicitudes_Maestro"."FechaAceptacionRegistro",
													"Solicitudes_Maestro"."Monto",
													"Solicitudes_Maestro"."NumeroCuotas",
													"Solicitudes_Maestro"."ValorCuota",
													"Solicitudes_Maestro"."CostoUF",
													"Solicitudes_Maestro"."CostoMonedaLocal",
													"Solicitudes_Maestro"."ValorUFFechaInversion",
													"Solicitudes_Maestro"."ParidadFechaInversion",
													"Solicitudes_Maestro"."IdMoneda",
													"Solicitudes_Maestro"."MontoComision",
													"Solicitudes_Maestro"."MontoPago",
													"Solicitudes_Maestro"."MontoComisionLocal",
													"Solicitudes_Maestro"."MontoPagoLocal",
													"Solicitudes_Maestro"."Estado",
													"Solicitudes_Maestro"."IdMedioDePago",
													"Solicitudes_Maestro"."Glosa",
													"Solicitudes_Maestro"."IdEmpresa",
													"Solicitudes_Maestro"."IdUsuario",
													"Solicitudes_Maestro"."IdModulo",
													"Solicitudes_Maestro"."EstadoFondo",
													"Solicitudes_Maestro"."SolicitudBatch",
													"Solicitudes_Maestro"."OrigenOperacion",
													"Solicitudes_Maestro"."IdMonedaSistema",
													"Solicitudes_Maestro"."MontoMonedaSistema",
													"Solicitudes_Maestro"."FolioContrato"
												FROM :v_solicitudes_Aporte AS "Solicitudes_Maestro"
												INNER JOIN :v_id_solicitudesAporte AS "Id_Solicitudes" ON ("Id_Solicitudes"."IdInterno" = "Solicitudes_Maestro"."IdInterno");
			
			INSERT INTO "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"(
					"IdSolicitudAporteRescate", "IdCliente", "IdPortafolio", "TipoSolicitud", "IdSerieFondo", "IdTipoMovimientoFondoMutuo", "IdDividendo", "IdSerieFondoDestino",
					"IdEmisorDestino", "IdClienteDestino", "IdPortafolioDestino", "IndicadorMLT", "Overnight", "IdSolicitudAporteRescateRelacionado", "IdTipoCartera", "CalculoPorMontoCuota", "FechaEmision",
					"FechaAceptacion", "FechaValorCuota", "FechaCertificacion", "FechaLiquidacion",
					"FechaOriginalInversion", "FechaIngresoRegistro", "FechaAceptacionRegistro", "Monto", "NumeroCuotas", "ValorCuota", "CostoUF", "CostoMonedaLocal",
					"ValorUFFechaInversion", "ParidadFechaInversion", "IdMoneda", "MontoComision", "MontoPago",  "MontoInversion", "NumeroCuotasInversion", "ValorCuotaInversion", "CostoMonedaLocalInversion" , "ParidadFechaCertificacion", "Estado", "IdMedioDePago", 
					"Glosa", "IdEmpresa", "IdUsuario", "IdModulo", "MontoComisionLocal", "MontoPagoLocal", "SolicitudBatch", "OrigenOperacion","IdMonedaSistema","MontoMonedaSistema","IdTipoCanalIngreso","FolioContrato")
			SELECT 	"IdSolicitudAporteRescate", "IdCliente", "IdPortafolio", "TipoSolicitud", "IdSerieFondo", "IdTipoMovimientoFondoMutuo", "IdDividendo", "IdSerieFondoDestino",
					"IdEmisorDestino", "IdClienteDestino", "IdPortafolioDestino", "IndicadorMLT", "Overnight", "IdSolicitudAporteRescateRelacionado", "IdTipoCartera", "CalculoPorMontoCuota", "FechaEmision",
					"FechaAceptacion", "FechaValorCuota", "FechaCertificacion", "FechaLiquidacion",
					"FechaOriginalInversion", "FechaIngresoRegistro", "FechaAceptacionRegistro", "Monto", "NumeroCuotas", "ValorCuota", "CostoUF", "CostoMonedaLocal",
					"ValorUFFechaInversion", "ParidadFechaInversion", "IdMoneda", "MontoComision", "MontoPago", "Monto", "NumeroCuotas", "ValorCuota", "CostoMonedaLocal", "ParidadFechaInversion", "Estado", "IdMedioDePago",
					"Glosa", "IdEmpresa","IdUsuario","IdModulo", "MontoComisionLocal", "MontoPagoLocal", "SolicitudBatch", "OrigenOperacion","IdMonedaSistema","MontoMonedaSistema",:p_IdTipoCanalIngreso,
					(CASE WHEN "FolioContrato" > 0 THEN "FolioContrato" ELSE "IdSolicitudAporteRescate" END) AS  "FolioContrato"
			FROM :v_solicitudesAporte_finales;
			
			v_log = SELECT 
						"IdSolicitudAporteRescate" AS "IdCabeceraLog"
						,'INGRESO' AS "IdTipoLog"
						,'FMM' AS "IdModulo"
						,:p_IdUsuario AS "IdUsuario"
						,:p_IdEmpresa AS "IdEmpresa"
						,TO_NVARCHAR("FechaEmision", 'YYYY-MM-DD') AS "FechaSistemaLog"
						,:p_IdUsuario AS "IdUsuarioAtendido"
						,0 AS "IdPersona"
						,"IdCliente"
						,"IdEstadoCliente" AS "EstadoCliente"
						,"NombreCompleto" AS "NombreCliente"
						,"TipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidad"
						,"DocumentoIdentidad" AS "IdDocumentoIdentidad"
						,"IdPortafolio" AS "IdPortafolio"
						,"IdEstadoPortafolio" AS "EstadoPortafolio"
						,'SOLICITUD_APORTE_DIVIDENDOS' AS "IdTipoRegistro"
						,"IdSolicitudAporteRescate" AS "IdRegistro"
						,"Estado" AS "EstadoRegistro"
						,"IdTipoMovimientoFondoMutuo" AS "IdTipoOperacion"
						,"IdSolicitudAporteRescate" AS "Folio"
						,"NumeroCuotas" AS "Cantidad"
						,"ValorCuota" AS "PrecioTasa"
						,"Monto" AS "Monto"
						,"IdMoneda" AS "IdMonedaOrigen"
						,"IdMoneda" AS "IdMonedaLiquidacion"
						,'' AS "TipoCambio"
						,"IdSerieFondo" AS "Nemo"
						,0 AS "IdProceso"
						,'' AS "EstadoProceso"
						,'' AS "NombreProceso"
					FROM :v_solicitudesAporte_finales;
			
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
			
			p_out_Resultado = SELECT "IdInterno"
									,"IdSolicitudAporteRescate"
									,"TipoSolicitud"
									,"IdSerieFondo"
									,"FechaAceptacion"
									,"FechaValorCuota"
									,"FechaCertificacion"
									,"FechaLiquidacion"
									,"FechaIngresoRegistro"
									,"Monto"
									,"NumeroCuotas"
									,"ValorCuota"
									,"CostoMonedaLocal"
									,"ParidadFechaInversion"
									,"IdMoneda"
									,"MontoComision"
									,"MontoPago"
									,"MontoComisionLocal"
									,"MontoPagoLocal"
									,"FolioContrato"
							  FROM :v_solicitudesAporte_finales;
			
		ELSE
			
			p_out_Resultado = SELECT "Solicitudes_Maestro"."IdInterno"
									,0 AS "IdSolicitudAporteRescate"
									,"Solicitudes_Maestro"."TipoSolicitud"
									,"Solicitudes_Maestro"."IdSerieFondo"
									,"Solicitudes_Maestro"."FechaAceptacion"
									,"Solicitudes_Maestro"."FechaValorCuota"
									,"Solicitudes_Maestro"."FechaCertificacion"
									,"Solicitudes_Maestro"."FechaLiquidacion"
									,"Solicitudes_Maestro"."FechaIngresoRegistro"
									,"Solicitudes_Maestro"."Monto"
									,"Solicitudes_Maestro"."NumeroCuotas"
									,"Solicitudes_Maestro"."ValorCuota"
									,"Solicitudes_Maestro"."CostoMonedaLocal"
									,"Solicitudes_Maestro"."ParidadFechaInversion"
									,"Solicitudes_Maestro"."IdMoneda"
									,"Solicitudes_Maestro"."MontoComision"
									,"Solicitudes_Maestro"."MontoPago"
									,"Solicitudes_Maestro"."MontoComisionLocal"
									,"Solicitudes_Maestro"."MontoPagoLocal"
									,"Solicitudes_Maestro"."FolioContrato"
							  FROM :v_solicitudes_Aporte AS "Solicitudes_Maestro";
							  
		END IF;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CREAR SOLICITUD APORTE POR DIVIDENDOS','FIN','FIN');	
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;	
GO