SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CargarSolicitudesAporteRescate";

CREATE PROCEDURE
--  Proposito: Carga Solicitudes de Aporte, Rescate, Reinversion, Dividendos y Comisiones.
--  Autor: - Gat-Blac 
--  Fecha Creacion: 12/10/2016
"BSCL.FondoMutuo.BD.Servicios::CargarSolicitudesAporteRescate"(
	--VARIABLES OBLIGATORIAS
	IN p_IdEmpresa 						BIGINT,
    IN p_IdUsuario 						BIGINT,
    IN p_IdModulo 						NVARCHAR(3),
    IN p_UsaReglasNegocio 				NVARCHAR(1),
    
    IN p_Solicitudes					TABLE(
    										"RUTEMISOR"						NVARCHAR(40),
    										"CODIGOPORTAFOLIOEMISOR"		NVARCHAR(100),
    										"TIPOMOVIMIENTO"				NVARCHAR(10),
    										"FECHAMOVIMIENTO"				NVARCHAR(10),
											"FECHAVALORCUOTA"				NVARCHAR(10),
    										"IDSOLICITUDAPORTERESCATE"		BIGINT,
    										"DOCUMENTOIDENTIDAD"			NVARCHAR(40),
    										"CODIGOPORTAFOLIO"				NVARCHAR(100),
    										"CALCULOPORMONTOCUOTA"			NVARCHAR(1),
    										"IDSERIEFONDO"					NVARCHAR(40),
    										"NUMEROCUOTAS"					DECIMAL(26,8),
    										"VALORCUOTA"					DECIMAL(26,8),
    										"MONTO"							DECIMAL(26,8),
    										"MONTOCOMISION"					DECIMAL(26,8),
    										"IDMONEDA"						NVARCHAR(3),
    										"IDSERIEFONDODESTINO"			NVARCHAR(40),
    										"INDICADORMLT"					NVARCHAR(1),
    										"CONCEPTO"						NVARCHAR(10),
    										"IDREGIMENTRIBUTARIOORIGEN"		NVARCHAR(40),
    										"IDREGIMENTRIBUTARIODESTINO"	NVARCHAR(40),
    										"IDPLAN"						BIGINT,
    										"IDPRODUCTO"                	BIGINT,
    										"IDREGIMENPORTAFOLIO"			BIGINT,
    										"IDMONEDAPAGO"					NVARCHAR(3),						---- aqui modificado 04-01-22
											"IDMEDIODEPAGO"					NVARCHAR(3),
											"IDMATRIZGRAUPADORAPV"			BIGINT,
    										"MENSAJEVALIDACION"				NVARCHAR(5000)
    									),
    OUT p_out_Resultado					TABLE(
    										"RutEmisor"						NVARCHAR(40),
    										"CodigoPortafolioEmisor"		NVARCHAR(100),
    										"TipoMovimiento"				NVARCHAR(10),
    										"FechaMovimiento"				DATE,
											"FechaValorCuota"				DATE,
    										"IdSolicitudAporteRescate"		BIGINT,
    										"DocumentoIdentidad"			NVARCHAR(40),
    										"CodigoPortafolio"				NVARCHAR(100),
    										"CalculoPorMontoCuota"			NVARCHAR(1),
    										"IdSerieFondo"					NVARCHAR(40),
    										"NumeroCuotas"					DECIMAL(26,8),
    										"ValorCuota"					DECIMAL(26,8),
    										"Monto"							DECIMAL(26,8),
    										"MontoComision"					DECIMAL(26,8),
    										"IdMoneda"						NVARCHAR(3),
    										"IdSerieFondoDestino"			NVARCHAR(40),
    										"Estado"						NVARCHAR(3),
    										"DescEstado"					NVARCHAR(100),
    										"EstadoProceso"					NVARCHAR(3),
    										"DecimalesCuota"				BIGINT,
    										"DecimalesValorCuota"			BIGINT,
    										"IdMonedaFondo"					NVARCHAR(3),
    										"IndicadorMLT"					NVARCHAR(1),
    										"Concepto"						NVARCHAR(10),
    										"IdRegimenTributarioOrigen"		NVARCHAR(40),
    										"IdRegimenTributarioDestino"	NVARCHAR(40),
    										"IdEmpresa"						BIGINT
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
			DECLARE v_resultadoRegla 			  NVARCHAR(40);
			DECLARE v_count						  BIGINT;
			DECLARE v_FechaSistema 				  NVARCHAR(10);
			DECLARE v_idCarga					  BIGINT;
			
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CARGAR SOLICITUDES','INICIO','INICIO');
			
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
			SELECT COUNT(1) as "Count" into v_count
			FROM :p_Solicitudes;
			
			IF (v_count = 0) THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_SOLICITUDES' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			v_solicitudes_ingresadas =  SELECT ROW_NUMBER() OVER(ORDER BY "RUTEMISOR") AS "IdSolicitudIngresada",
											   "SOLICITUDES"."RUTEMISOR" AS "RutEmisor",
											   "SOLICITUDES"."CODIGOPORTAFOLIOEMISOR" AS "CodigoPortafolioEmisor",
											   "SOLICITUDES"."TIPOMOVIMIENTO" AS "TipoMovimiento",
											   "SOLICITUDES"."FECHAMOVIMIENTO" AS "FechaMovimiento",
											   CASE WHEN "SOLICITUDES"."TIPOMOVIMIENTO" IN ('APONORM','RESNORM','APOREINV','RESREINV','RESCOM','APODIV','INGGTA','RETGTA','APODIVCFI') THEN '0001-01-01' ELSE "SOLICITUDES"."FECHAVALORCUOTA" END AS "FechaValorCuota",
											   "SOLICITUDES"."IDSOLICITUDAPORTERESCATE" AS "IdSolicitudAporteRescate",
											   "SOLICITUDES"."DOCUMENTOIDENTIDAD" AS "DocumentoIdentidad",
											   "SOLICITUDES"."CODIGOPORTAFOLIO" AS "CodigoPortafolio",
											   "SOLICITUDES"."IDSERIEFONDO" AS "IdSerieFondo",
											   CASE WHEN COALESCE("SOLICITUDES"."TIPOMOVIMIENTO",'') IN ('APONORM','APOREINV','APODIVCFI') THEN 'M' ELSE "SOLICITUDES"."CALCULOPORMONTOCUOTA" END AS "CalculoPorMontoCuota",
											   "SOLICITUDES"."NUMEROCUOTAS" AS "NumeroCuotas",
											   "SOLICITUDES"."VALORCUOTA" AS "ValorCuota",
											   "SOLICITUDES"."MONTO" AS "Monto",
											   "SOLICITUDES"."MONTOCOMISION" AS "MontoComision",
											   "SOLICITUDES"."IDMONEDA" AS "IdMoneda",
											   "SOLICITUDES"."IDSERIEFONDODESTINO" AS "IdSerieFondoDestino",
											   :p_IdEmpresa AS "IdEmpresa",
											   "SOLICITUDES"."CONCEPTO" AS "Concepto",
											   "SOLICITUDES"."INDICADORMLT" AS "IndicadorMLT",
											   "SOLICITUDES"."IDREGIMENTRIBUTARIOORIGEN" AS "IdRegimenTributarioOrigen",
											   "SOLICITUDES"."IDREGIMENTRIBUTARIODESTINO" AS "IdRegimenTributarioDestino",
											   CASE WHEN "SOLICITUDES"."IDMATRIZGRAUPADORAPV" = 0 THEN "SOLICITUDES"."IDPLAN" ELSE "MAA"."IdPlanTipoInversion" END AS "IdPlan",
											   CASE WHEN "SOLICITUDES"."IDMATRIZGRAUPADORAPV" = 0 THEN "SOLICITUDES"."IDPRODUCTO" ELSE "MAA"."IdProductoAgrupador" END AS "IdProducto",
											   CASE WHEN "SOLICITUDES"."IDMATRIZGRAUPADORAPV" = 0 THEN "SOLICITUDES"."IDREGIMENPORTAFOLIO" ELSE "MAA"."IdRegimenTributario" END AS "IdRegimenPortafolio",
											   "SOLICITUDES"."IDMONEDAPAGO" AS "IdMonedaPago",				---- aqui modificado 04-01-22
											   "SOLICITUDES"."IDMEDIODEPAGO" AS "IdMedioDePago",
											   "SOLICITUDES"."MENSAJEVALIDACION" AS "MensajeValidacion"
										  FROM :p_Solicitudes AS "SOLICITUDES"
										  LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS "MAA" ON ("MAA"."IdMatrizAgrupadorAPV" = "SOLICITUDES"."IDMATRIZGRAUPADORAPV");
			
			v_pre_calculo_valor_cuota = SELECT "SOLICITUDES"."IdSolicitudIngresada" AS "IdInterno",
												:v_FechaSistema AS "Fecha",
												CASE "TipoMovimiento" WHEN 'APOREINV' THEN "SOLICITUDES"."IdSerieFondoDestino" ELSE "SOLICITUDES"."IdSerieFondo" END AS "IdSerieFondo",
												"SOLICITUDES"."CalculoPorMontoCuota",
												"SOLICITUDES"."Monto",
												"SOLICITUDES"."NumeroCuotas",
												"SOLICITUDES"."IdMoneda" AS "IdMonedaFondo",
												"SOLICITUDES"."IdMoneda" AS "IdMonedaInversion",
												CASE WHEN "TipoMovimiento" IN ('RESNORM','RESREINV','RESCOM','RETTIT','RETGTA') THEN 'RES'
												ELSE 'APO' END AS "TipoSolicitud",
												'UF' AS "TipoFactorAjuste",
												"IdEmpresa"
										FROM :v_solicitudes_ingresadas AS "SOLICITUDES"
										WHERE "MensajeValidacion" = 'OK' AND "TipoMovimiento" IN ('EGRTIT','APONORM','RESNORM','APOREINV','RESREINV','APODIV','RESCOM','INGTIT','RETTIT','INGGTA','RETGTA','APODIVCFI','APOFUSION','RESFUSION');
			
			CALL "BSCL.FondoMutuo.BD.Utilitarios::CalcularMontoNumeroCuota"(:p_IdEmpresa,:v_pre_calculo_valor_cuota,v_valores_cuota);

			v_comparacion_valor_cuota = SELECT "SOLICITUDES"."IdSolicitudIngresada" AS "IdInterno",
			                                 CASE
			                                 	WHEN ("VALOR_CUOTA_HISTORICO"."ValorCuota" IS NOT NULL AND "VALOR_CUOTA_HISTORICO"."ValorCuota" = "SOLICITUDES"."ValorCuota")
			                                 		THEN 'S'
			                                 	ELSE 'N'
											 END AS "ValorCuotaIngresadoCoincideConRegistro"
									  FROM :v_solicitudes_ingresadas AS "SOLICITUDES"
									  LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" AS "VALOR_CUOTA_HISTORICO" ON ("VALOR_CUOTA_HISTORICO"."IdSerieFondo" = "SOLICITUDES"."IdSerieFondo" AND "VALOR_CUOTA_HISTORICO"."FechaCierre" = TO_DATE("SOLICITUDES"."FechaValorCuota", 'YYYY-MM-DD') AND "VALOR_CUOTA_HISTORICO"."EsUltimaCargaDiaria" = 'S');
			
			v_solicitudes = SELECT 
								   "SOLICITUDES"."IdSolicitudIngresada",
								   "SOLICITUDES"."RutEmisor",
								   "SOLICITUDES"."CodigoPortafolioEmisor",
								   "SOLICITUDES"."TipoMovimiento",
								   "SOLICITUDES"."FechaMovimiento",
								   "SOLICITUDES"."FechaValorCuota",
								   "SOLICITUDES"."IdSolicitudAporteRescate",
								   "SOLICITUDES"."DocumentoIdentidad",
								   "SOLICITUDES"."CodigoPortafolio",
								   "SOLICITUDES"."IdSerieFondo",
								   "SOLICITUDES"."CalculoPorMontoCuota",
								   "SOLICITUDES"."NumeroCuotas",
								   "SOLICITUDES"."ValorCuota",
								   "SOLICITUDES"."Monto",
								   "SOLICITUDES"."MontoComision",
								   "SOLICITUDES"."IdMoneda",
								   "SOLICITUDES"."IdMonedaPago",									---aqui modificado 04-01-22
								   "SOLICITUDES"."IdMedioDePago",
								   "SOLICITUDES"."IdSerieFondoDestino",
								   "Cliente"."IdCliente",
								   "Portafolio"."IdPortafolio",
								   "Emisor"."IdEmisor",
								   "SerieFondo"."DecimalesCuota",
								   "SerieFondo"."DecimalesValorCuota" AS "DecimalesValorCuota",
								   "SOLICITUDES"."IdEmpresa",
								   "SOLICITUDES"."Concepto",
								   "SOLICITUDES"."IndicadorMLT",
								   "Fondo"."IdMoneda" AS "IdMonedaFondo",
								   "SOLICITUDES"."IdRegimenTributarioOrigen",
								   "SOLICITUDES"."IdRegimenTributarioDestino",
								   CASE WHEN COALESCE("SOLICITUDES"."IdSolicitudAporteRescate",0) = 0 THEN 'N' ELSE 'S' END AS "EsActualizacion",
								   CASE WHEN COALESCE("SOLICITUDES"."TipoMovimiento",'') IN ('EGRTIT','APONORM','RESNORM','APOREINV','RESREINV','APODIV','RESCOM','INGTIT','RETTIT','INGGTA','RETGTA','APODIVCFI','APOFUSION','RESFUSION') THEN 'OK' ELSE 'ERR' END "ResultadoTipoMovimiento",
								   CASE WHEN COALESCE("Emisor"."IdEmisor",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoEmisor",
								   CASE WHEN COALESCE("Fondo"."IdFondo",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoFondo",
								   CASE WHEN COALESCE("SerieFondo"."IdSerieFondo",'') = '' AND "TipoMovimiento" != 'APOREINV' THEN 'ERR' ELSE 'OK' END AS "ResultadoSerieFondo",
								   CASE WHEN COALESCE("SOLICITUDES"."TipoMovimiento",'') IN ('APOREINV','RESREINV') AND COALESCE("SerieFondoDestino"."IdSerieFondo", '') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoSerieFondoDestino",
								   CASE WHEN COALESCE("Cliente"."IdCliente",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoCliente",
								   CASE WHEN (COALESCE("Portafolio"."IdPortafolio",0) = 0 AND "Portafolio"."IdMatrizAgrupadorAPV" > 0) THEN 'ERR' ELSE 'OK' END AS "ResultadoPortafolio",
								   CASE WHEN COALESCE("Fondo"."IdMoneda",'') = COALESCE("SOLICITUDES"."IdMoneda",'-') THEN 'OK' ELSE 'ERR' END AS "ResultadoMoneda",
								   CASE WHEN COALESCE("SOLICITUDES"."CalculoPorMontoCuota",'') IN ('M','C','T') THEN 'OK' ELSE 'ERR' END AS "ResultadoCalculoMontoCuota",
								   CASE WHEN COALESCE("SOLICITUDES"."FechaMovimiento",'0001-01-01') = :v_FechaSistema THEN 'OK' ELSE 'ERR' END AS "ResultadoFechaMovimiento", 
								   CASE WHEN "SOLICITUDES"."IdSolicitudAporteRescate" != 0 AND COALESCE("SolicitudesRescate"."IdSolicitudAporteRescate",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoIdSolicitudAporteRescate",
								   CASE WHEN (COALESCE("SOLICITUDES"."NumeroCuotas",0) > 0 AND COALESCE("SOLICITUDES"."CalculoPorMontoCuota",'') = 'C') OR (COALESCE("SOLICITUDES"."CalculoPorMontoCuota",'') != 'C') THEN 'OK' ELSE 'ERR' END AS "ResultadoNumeroCuotas",
								   CASE WHEN (COALESCE("SOLICITUDES"."Monto",0) > 0 AND COALESCE("SOLICITUDES"."CalculoPorMontoCuota",'') = 'M') OR (COALESCE("SOLICITUDES"."CalculoPorMontoCuota",'') != 'M') THEN 'OK' ELSE 'ERR' END AS "ResultadoMonto",
								   CASE WHEN "SOLICITUDES"."IdSolicitudAporteRescate" != 0 AND COALESCE("SOLICITUDES"."MontoComision",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoMontoComision",
								   CASE WHEN COALESCE("RegimenOrigen"."IdRegimenFondoMutuo",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoRegimenOrigen",
								   CASE WHEN COALESCE("SOLICITUDES"."TipoMovimiento",'') = 'RESREINV' AND COALESCE("RegimenDestino"."IdRegimenFondoMutuo",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoRegimenDestino",
								   CASE WHEN "ValidacionValorCuota"."ValorCuotaIngresadoCoincideConRegistro" = 'S' OR "SOLICITUDES"."TipoMovimiento" IN ('APONORM','RESNORM','APOREINV','RESREINV','RESCOM','APODIV','INGGTA','RETGTA','APODIVCFI') THEN 'OK' ELSE 'ERR' END AS "ResultadoValidacionValorCuota"
							FROM :v_solicitudes_ingresadas AS "SOLICITUDES"
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "Cliente" ON ("Cliente"."DocumentoIdentidad" = "SOLICITUDES"."DocumentoIdentidad" AND "Cliente"."IdEmpresa" = :p_IdEmpresa)
							LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "Portafolio" ON ("Cliente"."IdCliente" = "Portafolio"."IdCliente" AND "Portafolio"."CodigoPortafolio" = "SOLICITUDES"."CodigoPortafolio" AND "Portafolio"."IdProductoAgrupador" = "SOLICITUDES"."IdProducto" AND "Portafolio"."IdRegimenTributario" = "SOLICITUDES"."IdRegimenPortafolio")
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdSerieFondo" = "SOLICITUDES"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = :p_IdEmpresa)
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondoDestino" ON ("SerieFondoDestino"."IdSerieFondo" = "SOLICITUDES"."IdSerieFondoDestino" AND "SerieFondoDestino"."IdEmpresa" = :p_IdEmpresa)
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = CASE "TipoMovimiento" WHEN 'APOREINV' THEN "SerieFondoDestino"."IdFondo" ELSE "SerieFondo"."IdFondo" END AND "Fondo"."IdEmpresa" = :p_IdEmpresa)
							LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS "Emisor" ON ("Emisor"."DocumentoIdentidad" = "SOLICITUDES"."RutEmisor" AND "Fondo"."IdEmisor" = "Emisor"."IdEmisor")
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS "ValorCuota" ON ("ValorCuota"."IdSerieFondo" = CASE "TipoMovimiento" WHEN 'APOREINV' THEN "SOLICITUDES"."IdSerieFondoDestino" ELSE "SOLICITUDES"."IdSerieFondo" END AND "ValorCuota"."IdEmpresa" = :p_IdEmpresa)
							LEFT JOIN :v_valores_cuota AS "MontoValorCuota" ON ("MontoValorCuota"."IdInterno" = "SOLICITUDES"."IdSolicitudIngresada")
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudesRescate" ON ("SolicitudesRescate"."IdSolicitudAporteRescate" = "SOLICITUDES"."IdSolicitudAporteRescate" AND "SolicitudesRescate"."IdSerieFondo" = "SOLICITUDES"."IdSerieFondo")
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS "RegimenOrigen" ON ("RegimenOrigen"."IdRegimenFondoMutuo" = "SOLICITUDES"."IdRegimenTributarioOrigen")
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS "RegimenDestino" ON ("RegimenDestino"."IdRegimenFondoMutuo" = "SOLICITUDES"."IdRegimenTributarioDestino")
							LEFT JOIN :v_comparacion_valor_cuota AS "ValidacionValorCuota" ON ("ValidacionValorCuota"."IdInterno" = "SOLICITUDES"."IdSolicitudIngresada")
							WHERE "SOLICITUDES"."MensajeValidacion" = 'OK'
							AND "Portafolio"."IdMatrizAgrupadorAPV" > 0
							UNION 
							SELECT 
								   "SOLICITUDES"."IdSolicitudIngresada",
								   "SOLICITUDES"."RutEmisor",
								   "SOLICITUDES"."CodigoPortafolioEmisor",
								   "SOLICITUDES"."TipoMovimiento",
								   "SOLICITUDES"."FechaMovimiento",
								   "SOLICITUDES"."FechaValorCuota",
								   "SOLICITUDES"."IdSolicitudAporteRescate",
								   "SOLICITUDES"."DocumentoIdentidad",
								   "SOLICITUDES"."CodigoPortafolio",
								   "SOLICITUDES"."IdSerieFondo",
								   "SOLICITUDES"."CalculoPorMontoCuota",
								   "SOLICITUDES"."NumeroCuotas",
								   "SOLICITUDES"."ValorCuota",
								   "SOLICITUDES"."Monto",
								   "SOLICITUDES"."MontoComision",
								   "SOLICITUDES"."IdMoneda",
								   "SOLICITUDES"."IdMonedaPago",												---aqui modificado 04-01-22
								   "SOLICITUDES"."IdMedioDePago",
								   "SOLICITUDES"."IdSerieFondoDestino",
								   "Cliente"."IdCliente",
								   "Portafolio"."IdPortafolio",
								   "Emisor"."IdEmisor",
								   "SerieFondo"."DecimalesCuota",
								   "SerieFondo"."DecimalesValorCuota" AS "DecimalesValorCuota",
								   "SOLICITUDES"."IdEmpresa",
								   "SOLICITUDES"."Concepto",
								   "SOLICITUDES"."IndicadorMLT",
								   "Fondo"."IdMoneda" AS "IdMonedaFondo",
								   "SOLICITUDES"."IdRegimenTributarioOrigen",
								   "SOLICITUDES"."IdRegimenTributarioDestino",
								   CASE WHEN COALESCE("SOLICITUDES"."IdSolicitudAporteRescate",0) = 0 THEN 'N' ELSE 'S' END AS "EsActualizacion",
								   CASE WHEN COALESCE("SOLICITUDES"."TipoMovimiento",'') IN ('APONORM','RESNORM','APOREINV','RESREINV','APODIV','RESCOM','INGTIT','RETTIT','INGGTA','RETGTA','APODIVCFI','APOFUSION','RESFUSION') THEN 'OK' ELSE 'ERR' END "ResultadoTipoMovimiento",
								   CASE WHEN COALESCE("Emisor"."IdEmisor",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoEmisor",
								   CASE WHEN COALESCE("Fondo"."IdFondo",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoFondo",
								   CASE WHEN COALESCE("SerieFondo"."IdSerieFondo",'') = '' AND "TipoMovimiento" != 'APOREINV' THEN 'ERR' ELSE 'OK' END AS "ResultadoSerieFondo",
								   CASE WHEN COALESCE("SOLICITUDES"."TipoMovimiento",'') IN ('APOREINV','RESREINV') AND COALESCE("SerieFondoDestino"."IdSerieFondo", '') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoSerieFondoDestino",
								   CASE WHEN COALESCE("Cliente"."IdCliente",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoCliente",
								   CASE WHEN (COALESCE("Portafolio"."IdPortafolio",0) = 0 AND "Portafolio"."IdMatrizAgrupadorAPV" = 0) THEN 'ERR' ELSE 'OK' END AS "ResultadoPortafolio",
								   CASE WHEN COALESCE("Fondo"."IdMoneda",'') = COALESCE("SOLICITUDES"."IdMoneda",'-') THEN 'OK' ELSE 'ERR' END AS "ResultadoMoneda",
								   CASE WHEN COALESCE("SOLICITUDES"."CalculoPorMontoCuota",'') IN ('M','C','T') THEN 'OK' ELSE 'ERR' END AS "ResultadoCalculoMontoCuota",
								   CASE WHEN COALESCE("SOLICITUDES"."FechaMovimiento",'0001-01-01') = :v_FechaSistema THEN 'OK' ELSE 'ERR' END AS "ResultadoFechaMovimiento", 
								   CASE WHEN "SOLICITUDES"."IdSolicitudAporteRescate" != 0 AND COALESCE("SolicitudesRescate"."IdSolicitudAporteRescate",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoIdSolicitudAporteRescate",
								   CASE WHEN (COALESCE("SOLICITUDES"."NumeroCuotas",0) > 0 AND COALESCE("SOLICITUDES"."CalculoPorMontoCuota",'') = 'C') OR (COALESCE("SOLICITUDES"."CalculoPorMontoCuota",'') != 'C') THEN 'OK' ELSE 'ERR' END AS "ResultadoNumeroCuotas",
								   CASE WHEN (COALESCE("SOLICITUDES"."Monto",0) > 0 AND COALESCE("SOLICITUDES"."CalculoPorMontoCuota",'') = 'M') OR (COALESCE("SOLICITUDES"."CalculoPorMontoCuota",'') != 'M') THEN 'OK' ELSE 'ERR' END AS "ResultadoMonto",
								   CASE WHEN "SOLICITUDES"."IdSolicitudAporteRescate" != 0 AND COALESCE("SOLICITUDES"."MontoComision",0) = 0 THEN 'ERR' ELSE 'OK' END AS "ResultadoMontoComision",
								   CASE WHEN COALESCE("RegimenOrigen"."IdRegimenFondoMutuo",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoRegimenOrigen",
								   CASE WHEN COALESCE("SOLICITUDES"."TipoMovimiento",'') = 'RESREINV' AND COALESCE("RegimenDestino"."IdRegimenFondoMutuo",'') = '' THEN 'ERR' ELSE 'OK' END AS "ResultadoRegimenDestino",
								   CASE WHEN "ValidacionValorCuota"."ValorCuotaIngresadoCoincideConRegistro" = 'S' OR "SOLICITUDES"."TipoMovimiento" IN ('APONORM','RESNORM','APOREINV','RESREINV','RESCOM','APODIV','INGGTA','RETGTA','APODIVCFI') THEN 'OK' ELSE 'ERR' END AS "ResultadoValidacionValorCuota"
							FROM :v_solicitudes_ingresadas AS "SOLICITUDES"
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "Cliente" ON ("Cliente"."DocumentoIdentidad" = "SOLICITUDES"."DocumentoIdentidad" AND "Cliente"."IdEmpresa" = :p_IdEmpresa)
							LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "Portafolio" ON ("Cliente"."IdCliente" = "Portafolio"."IdCliente" AND "Portafolio"."CodigoPortafolio" = "SOLICITUDES"."CodigoPortafolio")
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SerieFondo"."IdSerieFondo" = "SOLICITUDES"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = :p_IdEmpresa)
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondoDestino" ON ("SerieFondoDestino"."IdSerieFondo" = "SOLICITUDES"."IdSerieFondoDestino" AND "SerieFondoDestino"."IdEmpresa" = :p_IdEmpresa)
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = CASE "TipoMovimiento" WHEN 'APOREINV' THEN "SerieFondoDestino"."IdFondo" ELSE "SerieFondo"."IdFondo" END AND "Fondo"."IdEmpresa" = :p_IdEmpresa)
							LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS "Emisor" ON ("Emisor"."DocumentoIdentidad" = "SOLICITUDES"."RutEmisor" AND "Fondo"."IdEmisor" = "Emisor"."IdEmisor")
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS "ValorCuota" ON ("ValorCuota"."IdSerieFondo" = CASE "TipoMovimiento" WHEN 'APOREINV' THEN "SOLICITUDES"."IdSerieFondoDestino" ELSE "SOLICITUDES"."IdSerieFondo" END AND "ValorCuota"."IdEmpresa" = :p_IdEmpresa)
							LEFT JOIN :v_valores_cuota AS "MontoValorCuota" ON ("MontoValorCuota"."IdInterno" = "SOLICITUDES"."IdSolicitudIngresada")
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudesRescate" ON ("SolicitudesRescate"."IdSolicitudAporteRescate" = "SOLICITUDES"."IdSolicitudAporteRescate" AND "SolicitudesRescate"."IdSerieFondo" = "SOLICITUDES"."IdSerieFondo")
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS "RegimenOrigen" ON ("RegimenOrigen"."IdRegimenFondoMutuo" = "SOLICITUDES"."IdRegimenTributarioOrigen")
							LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS "RegimenDestino" ON ("RegimenDestino"."IdRegimenFondoMutuo" = "SOLICITUDES"."IdRegimenTributarioDestino")
							LEFT JOIN :v_comparacion_valor_cuota AS "ValidacionValorCuota" ON ("ValidacionValorCuota"."IdInterno" = "SOLICITUDES"."IdSolicitudIngresada")
							WHERE "SOLICITUDES"."MensajeValidacion" = 'OK'
							AND "Portafolio"."IdMatrizAgrupadorAPV" = 0;
			
			SELECT "BSCL.FondoMutuo.BD.Secuencias::CargaSolicitudesAporteRescate".NEXTVAL into v_idCarga
			FROM DUMMY;
			
			v_id = SELECT "IdSolicitudIngresada","BSCL.FondoMutuo.BD.Secuencias::DetalleCargaSolicitudesAporteRescate".NEXTVAL AS "IdDetalleCargaSolicitudesAporteRescate"
			FROM :v_solicitudes_ingresadas;
			
			v_solicitudes_validacion = SELECT 
											  "Id_solicitudes"."IdDetalleCargaSolicitudesAporteRescate",
											  :v_idCarga AS "IdCargaSolicitudesAporteRescate", 
											  COALESCE("RutEmisor",'') AS "RutEmisor",
											  COALESCE("CodigoPortafolioEmisor",'') AS "CodigoPortafolioEmisor",
											  COALESCE("TipoMovimiento",'') AS "TipoMovimiento",
											  COALESCE("FechaMovimiento",'0001-01-01') AS "FechaMovimiento",
											  COALESCE("FechaValorCuota",'0001-01-01') AS "FechaValorCuota",
											  COALESCE("IdSolicitudAporteRescate",0) AS "IdSolicitudAporteRescate",
											  COALESCE("DocumentoIdentidad",'') AS "DocumentoIdentidad",
											  COALESCE("CodigoPortafolio",'') AS "CodigoPortafolio",
											  COALESCE("IdSerieFondo",'') AS "IdSerieFondo",
											  COALESCE("CalculoPorMontoCuota",'') AS "CalculoPorMontoCuota",
											  COALESCE("NumeroCuotas",0) AS "NumeroCuotas",
											  COALESCE("ValorCuota",0) AS "ValorCuota",
											  COALESCE("Monto",0) AS "Monto",
											  COALESCE("MontoComision",0) AS "MontoComision",
											  COALESCE("IdMoneda",'') AS "IdMoneda",
											  COALESCE("IdMonedaPago",'') AS "IdMonedaPago",								---Aqui moficiado	04-01-22
											  COALESCE("IdMedioDePago",'') AS "IdMedioDePago",
											  COALESCE("IdSerieFondoDestino",'') AS "IdSerieFondoDestino",
											  COALESCE("IdCliente",0) AS "IdCliente",
											  COALESCE("IdPortafolio",0) AS "IdPortafolio",
											  COALESCE("IdEmisor",'') AS "IdEmisor",
											  COALESCE("IdRegimenTributarioOrigen",'') AS "IdRegimenTributarioOrigen",
											  COALESCE("IdRegimenTributarioDestino",'') AS "IdRegimenTributarioDestino",
											  CASE WHEN 'ERR' IN ("ResultadoTipoMovimiento","ResultadoSerieFondo","ResultadoSerieFondoDestino","ResultadoCliente","ResultadoPortafolio","ResultadoEmisor",
											  					  "ResultadoMoneda","ResultadoValidacionValorCuota","ResultadoCalculoMontoCuota","ResultadoFechaMovimiento","ResultadoIdSolicitudAporteRescate",
											  					  "ResultadoNumeroCuotas","ResultadoMonto","ResultadoMontoComision",
											  					  "ResultadoFondo","ResultadoRegimenOrigen","ResultadoRegimenDestino") THEN 'ERR'
											  	   ELSE 'PRO' END AS "Estado",
											  CASE WHEN "ResultadoTipoMovimiento" = 'ERR' THEN 'UI_TIPO_MOVIMIENTO_INVALIDO'
											  	   WHEN "ResultadoEmisor" = 'ERR' THEN 'UI_EMISOR_NO_EXISTE'
											  	   WHEN "ResultadoFondo" = 'ERR' THEN 'UI_FONDO_NO_EXISTE'
											  	   WHEN "ResultadoSerieFondo" = 'ERR' THEN 'UI_SERIE_FONDO_NO_EXISTE'
											  	   WHEN "ResultadoSerieFondoDestino" = 'ERR' THEN 'UI_SERIE_FONDO_DESTINO_NO_EXISTE'
											  	   WHEN "ResultadoCliente" = 'ERR' THEN 'UI_CLIENTE_NO_EXISTE'
											  	   WHEN "ResultadoPortafolio" = 'ERR' THEN 'UI_PORTAFOLIO_CLIENTE_NO_EXISTE'
											  	   WHEN "ResultadoMoneda" = 'ERR' THEN 'UI_MONEDA_INVALIDA'
											       WHEN "ResultadoValidacionValorCuota" = 'ERR' THEN 'UI_VALOR_CUOTA_INGRESADO_NO_COINCIDE'
											  	   WHEN "ResultadoCalculoMontoCuota" = 'ERR' THEN 'UI_CALCULO_MONTO_CUOTA_INVALIDO'
											  	   WHEN "ResultadoFechaMovimiento" = 'ERR' THEN 'UI_FECHA_MOVIMIENTO_INVALIDO'
											  	   WHEN "ResultadoIdSolicitudAporteRescate" = 'ERR' THEN 'UI_SOLICITUD_APORTE_RESCATE_NO_EXISTE'
											  	   WHEN "ResultadoNumeroCuotas" = 'ERR' THEN 'UI_NUMERO_CUOTAS_INVALIDO'
											  	   WHEN "ResultadoMonto" = 'ERR' THEN 'UI_MONTO_INVALIDO'
											  	   WHEN "ResultadoMontoComision" = 'ERR' THEN 'UI_MONTO_COMISION_INVALIDO'
											 	   WHEN "ResultadoRegimenOrigen" = 'ERR' THEN 'UI_REGIMEN_ORIGEN_INVALIDO'
											 	   WHEN "ResultadoRegimenDestino" = 'ERR' THEN 'UI_REGIMEN_DESTINO_INVALIDO'
											  	   ELSE 'UI_EXITO' END AS "DescEstado",
											  	   CASE WHEN 'ERR' IN ("ResultadoTipoMovimiento","ResultadoSerieFondo","ResultadoSerieFondoDestino","ResultadoCliente","ResultadoPortafolio","ResultadoEmisor",
											  					  "ResultadoMoneda","ResultadoValidacionValorCuota","ResultadoCalculoMontoCuota","ResultadoFechaMovimiento","ResultadoIdSolicitudAporteRescate",
											  					  "ResultadoNumeroCuotas","ResultadoMonto","ResultadoMontoComision","ResultadoRegimenOrigen","ResultadoRegimenDestino") THEN 'ERR'
											  	   ELSE 'PEN' END "EstadoProceso",
											  'N' AS "Eliminado",
											  COALESCE("DecimalesCuota",0) AS "DecimalesCuota",
											  COALESCE("DecimalesValorCuota",0) AS "DecimalesValorCuota",
											  COALESCE("IdMonedaFondo",'') AS "IdMonedaFondo",
											  :v_FechaSistema AS "FechaCarga",
											  CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro",
											  "Concepto",
											  "IndicadorMLT",
											  "EsActualizacion",
											  "IdEmpresa"
										FROM :v_solicitudes AS "Solicitudes"
										INNER JOIN :v_id AS "Id_solicitudes" ON ("Id_solicitudes"."IdSolicitudIngresada" = "Solicitudes"."IdSolicitudIngresada")
									UNION ALL
									SELECT 	  
											  "Id_solicitudes"."IdDetalleCargaSolicitudesAporteRescate",
											  :v_idCarga AS "IdCargaSolicitudesAporteRescate", 
											  COALESCE("RutEmisor",'') AS "RutEmisor",
											  COALESCE("CodigoPortafolioEmisor",'') AS "CodigoPortafolioEmisor",
											  COALESCE("TipoMovimiento",'') AS "TipoMovimiento",
											  COALESCE("FechaMovimiento",'0001-01-01') AS "FechaMovimiento",
											  COALESCE("FechaValorCuota",'0001-01-01') AS "FechaValorCuota",
											  COALESCE("IdSolicitudAporteRescate",0) AS "IdSolicitudAporteRescate",
											  COALESCE("DocumentoIdentidad",'') AS "DocumentoIdentidad",
											  COALESCE("CodigoPortafolio",'') AS "CodigoPortafolio",
											  COALESCE("IdSerieFondo",'') AS "IdSerieFondo",
											  COALESCE("CalculoPorMontoCuota",'') AS "CalculoPorMontoCuota",
											  COALESCE("NumeroCuotas",0) AS "NumeroCuotas",
											  COALESCE("ValorCuota",0) AS "ValorCuota",
											  COALESCE("Monto",0) AS "Monto",
											  COALESCE("MontoComision",0) AS "MontoComision",
											  COALESCE("IdMoneda",'') AS "IdMoneda",
											  COALESCE("IdMonedaPago",'') AS "IdMonedaPago",							--- aqui modificado		04-01-22
											  COALESCE("IdMedioDePago",'') AS "IdMedioDePago",
											  COALESCE("IdSerieFondoDestino",'') AS "IdSerieFondoDestino",
											  0 AS "IdCliente",
											  0 AS "IdPortafolio",
											  '' AS "IdEmisor",
											  COALESCE("IdRegimenTributarioOrigen",'') AS "IdRegimenTributarioOrigen",
											  COALESCE("IdRegimenTributarioDestino",'') AS "IdRegimenTributarioDestino",
											  'ERR' AS "Estado",
											  'UI_FORMATO_INVALIDO' AS "DescEstado",
											  'ERR' AS "EstadoProceso",
											  'N' AS "Eliminado",
											  0 AS "DecimalesCuota",
											  0 AS "DecimalesValorCuota",
											  '' AS "IdMonedaFondo",
											  :v_FechaSistema AS "FechaCarga",
											  CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro",
											  "Concepto",
											  "IndicadorMLT",
											  CASE WHEN  COALESCE("IdSolicitudAporteRescate",0) = 0 THEN 'N' ELSE 'S' END AS "EsActualizacion",
											  "IdEmpresa"
									FROM :v_solicitudes_ingresadas  AS "Solicitudes"
									INNER JOIN :v_id AS "Id_solicitudes" ON ("Id_solicitudes"."IdSolicitudIngresada" = "Solicitudes"."IdSolicitudIngresada")
									WHERE "MensajeValidacion" != 'OK';
			
			INSERT INTO "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate"(
				"IdDetalleCargaSolicitudesAporteRescate", "IdCargaSolicitudesAporteRescate", "RutEmisor", "CodigoPortafolioEmisor",
				"TipoMovimiento", "FechaMovimiento", "FechaValorCuota", "IdSolicitudAporteRescate", "DocumentoIdentidad", "CodigoPortafolio", "IdSerieFondo", "CalculoPorMontoCuota",
				"NumeroCuotas", "ValorCuota", "Monto", "MontoComision", "IdMoneda", "IdSerieFondoDestino", "IdCliente", "IdPortafolio", "IdEmisor",
				"FechaCarga", "FechaIngresoRegistro", "Estado", "DescEstado", "EstadoProceso", "Eliminado", "IndicadorMLT", "Concepto", "IdRegimenTributarioOrigen", "IdRegimenTributarioDestino", "EsActualizacion", "IdEmpresa",
				"IdMonedaPago", "IdMedioDePago"
			)
			SELECT "IdDetalleCargaSolicitudesAporteRescate", "IdCargaSolicitudesAporteRescate", "RutEmisor", "CodigoPortafolioEmisor",
				"TipoMovimiento", "FechaMovimiento", "FechaValorCuota", "IdSolicitudAporteRescate", "DocumentoIdentidad", "CodigoPortafolio", "IdSerieFondo", "CalculoPorMontoCuota",
				"NumeroCuotas", "ValorCuota", "Monto", "MontoComision", "IdMoneda", "IdSerieFondoDestino", "IdCliente", "IdPortafolio", "IdEmisor",
				"FechaCarga", "FechaIngresoRegistro", "Estado", "DescEstado", "EstadoProceso", "Eliminado", "IndicadorMLT", "Concepto", "IdRegimenTributarioOrigen", "IdRegimenTributarioDestino", "EsActualizacion", "IdEmpresa", 
				"IdMonedaPago", "IdMedioDePago"
			FROM :v_solicitudes_validacion;
			
			p_out_Resultado = SELECT "RutEmisor",
									  "CodigoPortafolioEmisor",
									  "TipoMovimiento",
									  "FechaMovimiento",
									  "FechaValorCuota",
									  "IdSolicitudAporteRescate",
									  "DocumentoIdentidad",
									  "CodigoPortafolio",
									  "CalculoPorMontoCuota",
									  "IdSerieFondo",
									  "NumeroCuotas",
									  "ValorCuota",
									  "Monto",
									  "MontoComision",
									  "IdMoneda",
									  "IdSerieFondoDestino",
									  "Estado",
									  "DescEstado",
									  "EstadoProceso",
									  "DecimalesCuota",
									  "DecimalesValorCuota",
									  "IdMonedaFondo",
									  "IndicadorMLT",
									  "Concepto",
									  "IdRegimenTributarioOrigen",
									  "IdRegimenTributarioDestino",
									  "IdEmpresa"
							FROM :v_solicitudes_validacion;
			
			--LOG DE AUDITORIA 				
			BEGIN
				DECLARE numero_solicitudes BIGINT;
			
				v_log = SELECT :v_idCarga AS "IdCabeceraLog"
							,'INGRESO' AS "IdTipoLog"
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
							,0  AS "IdPortafolio"
							,'' AS "EstadoPortafolio"
							,'CARGA_SOLICITUD_APORTE_RESCATE' AS "IdTipoRegistro"
							,:v_idCarga AS "IdRegistro"
							,'PRO' AS "EstadoRegistro"
							,'' AS "IdTipoOperacion"
							,0 AS "Folio"
							,0 AS "Cantidad"
							,0 AS "PrecioTasa"
							,0 AS "Monto"
							,'' AS "IdMonedaOrigen"
							,'' AS "IdMonedaLiquidacion"
							,'' AS "TipoCambio"
							,'' AS "Nemo"
							,0  AS "IdProceso" 
							,'' AS "EstadoProceso"
							,'' AS "NombreProceso"
						FROM DUMMY;

				SELECT COUNT(*) INTO numero_solicitudes
				FROM :p_Solicitudes;
				
				v_LogDetalle = SELECT :v_idCarga AS "IdCabeceraLog"
									,'IdCarga: ' || COALESCE(TO_VARCHAR(:v_idCarga), '') AS "SubRegistro"
									,'Numero de Operaciones' AS "CampoRegistro"
									,'' AS "ValorAntiguo"
									,COALESCE(TO_VARCHAR(:numero_solicitudes), '') AS "ValorNuevo"
						FROM DUMMY;
						
				CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
			END;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CARGAR SOLICITUDES','FIN','FIN');
			
			SIGNAL SALIDA_CONTROLADA;
			
		END;
	END;
END;
