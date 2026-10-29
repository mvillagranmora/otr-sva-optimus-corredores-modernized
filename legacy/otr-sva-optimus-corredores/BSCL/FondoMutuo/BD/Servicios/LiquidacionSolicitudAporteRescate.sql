SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::LiquidacionSolicitudAporteRescate";

CREATE PROCEDURE
--  Proposito: Crear Liquidacion Aporte Rescate .
--  Autor: Gat-Blac 
--  Fecha Creacion: 30/08/2016
"BSCL.FondoMutuo.BD.Servicios::LiquidacionSolicitudAporteRescate" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 								BIGINT,
    IN p_IdUsuario 								BIGINT,
    IN p_IdModulo 								NVARCHAR(3),
    IN p_SimularOperacion 						NVARCHAR(1),
    IN p_UsaReglasNegocio 						NVARCHAR(1),
    IN p_Movimientos							TABLE("IdMovimiento" BIGINT),
    
    IN p_Filtros								TABLE(
    												"IdInterno"			 BIGINT,
    												"IdEmisor"			 NVARCHAR(40),
    												"TipoSolicitud"		 NVARCHAR(3),
    												"IdEstado"			 NVARCHAR(3),
    												"FechaCertificacion" NVARCHAR(10),
    												"FechaLiquidacion"	 NVARCHAR(10),
    												"IdSerieFondo"		 NVARCHAR(40)		
    											),
    OUT p_out_Resultado 						TABLE(
													"IdInterno"					 BIGINT,
													"IdSolicitudAporteRescate"	 BIGINT,
													"IdSerieFondo"				 NVARCHAR(40),
													"Codigo"					 NVARCHAR(100),
													"Estado"					 NVARCHAR(3),
													"NombreEstado"				 NVARCHAR(100)
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
	END;
	
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE v_FechaSistema NVARCHAR(10);
		
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			DROP TABLE #RESULTADO_LiquidacionSolicitudAporteRescateMasiva;
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
			
			
										
			DROP TABLE #RESULTADO_LiquidacionSolicitudAporteRescateMasiva; 
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
		DECLARE v_count								BIGINT;
		DECLARE v_resultadoRegla 					NVARCHAR(40);
		DECLARE v_liquidarOmnibus					NVARCHAR(40);
		DECLARE v_count_filtros						BIGINT;
		DECLARE v_count_movimientos					BIGINT;
		DECLARE v_FechaSistema 						NVARCHAR(10);
		DECLARE v_count_solicitudes					BIGINT;
		DECLARE v_count_aportes						BIGINT;
		DECLARE v_max_fecha_liquidacion_aporte		NVARCHAR(10);
		DECLARE v_count_saldo						BIGINT;
		DECLARE v_id_carta							BIGINT;
		DECLARE v_out_FlujoCajaAdvContador			BIGINT;
		DECLARE v_out_FlujoCajaErrContador			BIGINT;
		DECLARE v_count_ingreso_egresos_advertencia BIGINT;
		DECLARE v_count_ingreso_egresos_error		BIGINT;
		DECLARE v_count_ingreso_egreso				BIGINT;
		DECLARE v_count_informacion_cargo_abono		BIGINT;
		DECLARE v_count_advertencia_cargo_abono		BIGINT;
		DECLARE v_count_errores_cargo_abono			BIGINT;
		DECLARE v_count_informacion_ingreso_egreso	BIGINT;
		DECLARE v_cargo_abono						BIGINT;
		DECLARE v_LiquidacionCuentaInversion		NVARCHAR(255);
		
		CREATE LOCAL TEMPORARY TABLE #RESULTADO_LiquidacionSolicitudAporteRescateMasiva (
			"IdInterno"				   		BIGINT,
			"IdSolicitudAporteRescate" 		BIGINT,
			"Codigo" 			 	   		NVARCHAR(100),
			"Descripcion" 		       		NVARCHAR(520),
			"TipoSolicitud"			   		NVARCHAR(3),
			"IdSerieFondo"			   		NVARCHAR(40),
			"IdInstrumento"			   		BIGINT,
			"IdTipoInstrumento"		   		NVARCHAR(3),
			"IdPortafolio"			   		BIGINT,
			"IdMoneda"				   		NVARCHAR(3),
			"IdMonedaFondo"			   		NVARCHAR(3),
			"CostoMonedaLocal"		   		DECIMAL(26,8),
			"Monto"					   		DECIMAL(26,8),
			"ParidadFechaCertificacion"	   	DECIMAL(26,8),
			"FechaLiquidacion"		   		DATE,
			"FechaCertificacion"			DATE,
			"IdUsuario"				   		BIGINT,
			"IdPortafolioAGF"		   		BIGINT,
			"IdMedioDePago"			   		NVARCHAR(3),
			"IdPersonaAGF"			   		BIGINT,
			"IdMedioDePagoAbonoAGF"			NVARCHAR(3),
			"IdMedioDePagoCargoAGF"			NVARCHAR(3),
			"EstadoLiquidacion"				NVARCHAR(3),
			"MontoComision"					DECIMAL(26,8),
			"MontoPago"						DECIMAL(26,8),
			"MontoPagoLocal"				DECIMAL(26,8),
			"MontoComisionLocal"			DECIMAL(26,8),
			"FolioCartaLiquidacion"			BIGINT,
			"IdOmnibus"						BIGINT,
			"Rechazo"				   		BIGINT
		);
		
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'LIQUIDAR SOLICITUD APORTE RESCATE','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		if :v_resultadoRegla = 'RECH' then
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		SELECT COUNT(1) as "Count" into v_count_filtros
		FROM :p_Filtros;
		
		IF (v_count_filtros = 0) THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", 'NO_SE_INGRESARON_FILTROS' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		SELECT TOP 1 COUNT("IdInterno") as "Count" into v_count
		FROM :p_Filtros
		GROUP BY "IdInterno"
		ORDER BY "Count" DESC;
		
		if (v_count > 1) THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'CORRELATIVO_INCORRECTO' AS "Codigo", '' as "ValorParametro", '' as "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		CALL "BSCL.FondoMutuo.BD.Utilitarios::ConsultarParametroFondoMutuo" (:p_IdEmpresa,'LiquidacionCuentaInversionCliente',v_LiquidacionCuentaInversion);
		
		IF v_LiquidacionCuentaInversion = '' THEN
			v_LiquidacionCuentaInversion := 'N';
		END IF;
		
		--CALCULAMOS CUALES PODEMOS LIQUIDAR
		SELECT COUNT(1) as "Count" into v_count_movimientos
		FROM :p_Movimientos;
		
		IF (v_count_movimientos > 0) THEN
			v_solicitudes_liquidar = SELECT 0 AS "IdInterno",
										   "Solicitudes"."IdSolicitudAporteRescate",
										   "Solicitudes"."TipoSolicitud",
										   "Solicitudes"."IdSerieFondo",
										   "Solicitudes"."IdInstrumento",
										   "Solicitudes"."IdTipoInstrumento",
										   "Solicitudes"."IdCliente",
										   "Solicitudes"."IdPortafolio",
										   "Solicitudes"."IdMoneda",
										   "Solicitudes"."IdMonedaFondo",
										   "Solicitudes"."CostoMonedaLocal",
										   "Solicitudes"."Monto",
										   "Solicitudes"."ParidadFechaCertificacion",
										   "Solicitudes"."FechaLiquidacion",
										   "Solicitudes"."FechaCertificacion",
										   "Solicitudes"."IdUsuario",
										   "Solicitudes"."IdPortafolioAGF",
										   "Solicitudes"."IdMedioDePago",
										   "Solicitudes"."IdPersonaAGF",
										   "Solicitudes"."IdMedioDePagoAbonoAGF",
										   "Solicitudes"."IdMedioDePagoCargoAGF",
										   "Solicitudes"."EstadoLiquidacion",
										   "Solicitudes"."MontoPago",
										   "Solicitudes"."MontoComision",
										   "Solicitudes"."MontoPagoLocal",
										   "Solicitudes"."MontoComisionLocal",
										   "Solicitudes"."IdOmnibus"
								   FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS "Solicitudes"
								   INNER JOIN :p_Movimientos AS "Movimientos" 
								   ON ("Movimientos"."IdMovimiento" = "Solicitudes"."IdSolicitudAporteRescate")
								   WHERE "Solicitudes"."IdEmpresa" = :p_IdEmpresa
								   AND "Solicitudes"."EstadoLiquidacion" = 'ING'
								   AND "Solicitudes"."IdTipoMovimientoFondoMutuo" IN ('APONORM','RESNORM','APOREINV','RESREINV','APORESPRO','APOOVER');
		ELSE
			v_solicitudes_liquidar = SELECT "Filtros"."IdInterno",
										   "Solicitudes"."IdSolicitudAporteRescate",
										   "Solicitudes"."TipoSolicitud",
										   "Solicitudes"."IdSerieFondo",
										   "Solicitudes"."IdInstrumento",
										   "Solicitudes"."IdTipoInstrumento",
										   "Solicitudes"."IdCliente",
										   "Solicitudes"."IdPortafolio",
										   "Solicitudes"."IdMoneda",
										   "Solicitudes"."IdMonedaFondo",
										   "Solicitudes"."CostoMonedaLocal",
										   "Solicitudes"."Monto",
										   "Solicitudes"."ParidadFechaCertificacion",
										   "Solicitudes"."FechaLiquidacion",
										   "Solicitudes"."FechaCertificacion",
										   "Solicitudes"."IdUsuario",
										   "Solicitudes"."IdPortafolioAGF",
										   "Solicitudes"."IdMedioDePago",
										   "Solicitudes"."IdPersonaAGF",
										   "Solicitudes"."IdMedioDePagoAbonoAGF",
										   "Solicitudes"."IdMedioDePagoCargoAGF",
										   "Solicitudes"."EstadoLiquidacion",
										   "Solicitudes"."MontoPago",
										   "Solicitudes"."MontoComision",
										   "Solicitudes"."MontoPagoLocal",
										   "Solicitudes"."MontoComisionLocal",
										   "Solicitudes"."IdOmnibus"
								   FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS "Solicitudes",
								   :p_Filtros AS "Filtros"
								   WHERE 
										 "Filtros"."TipoSolicitud" = "Solicitudes"."TipoSolicitud"
										 AND ("Filtros"."IdEmisor" = '' OR "Filtros"."IdEmisor" = "Solicitudes"."IdEmisor")
										 AND (("Filtros"."IdEstado" = "Solicitudes"."EstadoSolicitud" AND "Solicitudes"."TipoSolicitud" = 'APO') OR ("Solicitudes"."TipoSolicitud" = 'RES' AND "Solicitudes"."EstadoSolicitud" = 'CRT'))
										 AND "Solicitudes"."FechaLiquidacion" = "Filtros"."FechaLiquidacion"
										 AND "Solicitudes"."FechaCertificacion" = "Filtros"."FechaCertificacion"
										 AND ("Filtros"."IdSerieFondo" = '' OR "Filtros"."IdSerieFondo" = "Solicitudes"."IdSerieFondo")
										 AND "Solicitudes"."EstadoLiquidacion" = 'ING'
										 AND "Solicitudes"."IdTipoMovimientoFondoMutuo" IN ('APONORM','RESNORM','APOREINV','RESREINV','APORESPRO','APOOVER')
										 AND "Solicitudes"."IdEmpresa" = :p_IdEmpresa;
		END IF;
		
		
		v_solicitudes = SELECT 
				sol."IdInterno",
				sol."IdSolicitudAporteRescate", 
				'UI_EXITO' AS "Codigo",
				'' as "Descripcion",
				sol."TipoSolicitud",
				sol."IdSerieFondo",
				sol."IdInstrumento",
				sol."IdTipoInstrumento",
				sol."IdPortafolio",
				sol."IdMoneda",
				sol."IdMonedaFondo",
				sol."CostoMonedaLocal",
				sol."Monto",
				sol."ParidadFechaCertificacion",
				sol."FechaLiquidacion",
				sol."FechaCertificacion",
				sol."IdUsuario",
				sol."IdPortafolioAGF",
				sol."IdMedioDePago",
				sol."IdPersonaAGF",
				sol."IdMedioDePagoAbonoAGF",
				sol."IdMedioDePagoCargoAGF",
				sol."MontoComision",
				sol."MontoPago",
				sol."MontoPagoLocal",
				sol."MontoComisionLocal",
				sol."IdOmnibus"
		FROM :v_solicitudes_liquidar sol;
		
		INSERT INTO #RESULTADO_LiquidacionSolicitudAporteRescateMasiva("IdInterno","IdSolicitudAporteRescate","Codigo", "Descripcion",
							   "TipoSolicitud","IdSerieFondo","IdInstrumento","IdTipoInstrumento","IdPortafolio",
							   "IdMoneda","IdMonedaFondo","CostoMonedaLocal","Monto","ParidadFechaCertificacion",
							   "FechaLiquidacion","FechaCertificacion","IdUsuario","IdPortafolioAGF","IdMedioDePago",
							   "IdPersonaAGF", "IdMedioDePagoAbonoAGF", "IdMedioDePagoCargoAGF","MontoComision", "MontoPago","MontoPagoLocal","MontoComisionLocal", "IdOmnibus", "Rechazo")
		SELECT "IdInterno", "IdSolicitudAporteRescate","Codigo", "Descripcion", "TipoSolicitud","IdSerieFondo", "IdInstrumento","IdTipoInstrumento",  
			   "IdPortafolio", "IdMoneda","IdMonedaFondo","CostoMonedaLocal","Monto","ParidadFechaCertificacion",
			   "FechaLiquidacion","FechaCertificacion","IdUsuario","IdPortafolioAGF","IdMedioDePago", 
			   "IdPersonaAGF", "IdMedioDePagoAbonoAGF", "IdMedioDePagoCargoAGF","MontoComision","MontoPago","MontoPagoLocal","MontoComisionLocal", "IdOmnibus",0 AS "Rechazo"
		FROM :v_solicitudes;
		
		SELECT COUNT(1) into v_count_solicitudes
		FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva;
		
		IF v_count_solicitudes = 0 THEN
			v_out_ErroresDetalle = SELECT 0 AS "IdInterno", 'UI_NO_EXISTEN_SOLICITUDES_CUMPLAN_FILTROS' AS "Codigo", '' AS "ValorParametro", '' AS "NombreParametro" FROM DUMMY;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.FondoMutuo.BD.Utilitarios::ConsultarParametroFondoMutuo" (:p_IdEmpresa, 'VALIDAR_LIQUIDACION_OMNIBUS', v_liquidarOmnibus);
		
		IF v_liquidarOmnibus = 'S' THEN
			UPDATE #RESULTADO_LiquidacionSolicitudAporteRescateMasiva
			SET "Rechazo" = 1,
			"Codigo" = 'UI_ERROR_APORTE_SIN_OMNIBUS'
			WHERE "IdOmnibus" = 0 AND "TipoSolicitud" = 'APO';
			
			v_out_ErroresDetalle =  SELECT 0 AS "IdInterno", 'UI_ERROR_APORTE_SIN_OMNIBUS' as "Codigo", CAST(RESULTADO."IdSolicitudAporteRescate" AS NVARCHAR(100)) as "ValorParametro", 'IdSolicitudAporteRescate' as "NombreParametro"
									FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva RESULTADO
									WHERE RESULTADO."IdOmnibus" = 0 AND "TipoSolicitud" = 'APO'
									UNION ALL SELECT * FROM :v_out_ErroresDetalle;
			
		END IF;

		--Preparamos llamada a ingreso y egreso para Cliente y AGF
		
		v_suma_aportes_rescates = SELECT ROW_NUMBER() OVER(ORDER BY "IdPersonaAGF") AS "Id_Suma", * FROM(
								  SELECT SUM("MontoPagoLocal") AS "Monto", "TipoSolicitud", "IdPersonaAGF", "IdMoneda", "IdPortafolioAGF", "IdMedioDePagoCargoAGF",
								  "IdMedioDePagoAbonoAGF","FechaLiquidacion","FechaCertificacion","IdInterno"
								  FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva
								  WHERE "Rechazo" = 0
								  GROUP BY "TipoSolicitud", "IdPersonaAGF", "IdMoneda", "IdPortafolioAGF","IdMedioDePagoCargoAGF","IdMedioDePagoAbonoAGF","FechaLiquidacion","FechaCertificacion","IdInterno");
		
		v_id_carta_liquidacion = SELECT "BSCL.FondoMutuo.BD.Secuencias::FolioCartaLiquidacionAporteRescate".NEXTVAL AS "Id_Carta", "SUMA".*
								 FROM :v_suma_aportes_rescates AS "SUMA";
		
		
		v_pre_ingreso_egresos = SELECT
	                    				"ID_LIQUIDACION"."Id_Suma" AS "NumeroReferencia",
	                    				CASE "SUMA"."TipoSolicitud" WHEN 'APO' THEN 'MOVEGR' ELSE 'MOVING' END AS "IdTipoMovimiento",
										CASE :v_LiquidacionCuentaInversion WHEN 'S' THEN 'CLI' ELSE 'CNT' END AS "IdTipoPersona",
										"SUMA"."IdPersonaAGF" AS "IdPersona",
										"SUMA"."IdPortafolioAGF" AS "IdPortafolio",
										"SUMA"."IdMoneda" AS "IdMoneda",
										"SUMA"."Monto" AS "Monto",
										"SUMA"."FechaLiquidacion" AS "FechaLiquidacion",
										CASE "SUMA"."TipoSolicitud" WHEN 'APO' THEN 'CAAEFM' ELSE 'ABREFM' END AS "IdConcepto",
										CASE :v_LiquidacionCuentaInversion WHEN 'S' THEN 'CLI' ELSE 'CNT' END  AS "IdTipoPersonaTitularPago",
										"SUMA"."IdPersonaAGF" AS "IdPersonaTitularPago",
										"SUMA"."IdPortafolioAGF" AS "IdPortafolioTitularPago",
										0 AS "IdCaja",
										'' AS "SecuenciaAgrupacion",
										0  AS "SecuenciaRelacionada",
										'' AS "Glosa",
										0 AS "IdEmpleadoDigitador",
										0 AS "IdSucursalEmpleadoDigitador",
										'INFM' AS "IdTipoProducto",
										CASE "SUMA"."TipoSolicitud" WHEN 'APO' THEN 'APORFM' ELSE 'RESCFM' END AS "IdTipoOperacion",
										'LFM' AS "IdTipoComprobante",
										"ID_LIQUIDACION"."Id_Carta" AS "IdComprobante",
										'0001-01-01' AS "FechaComprobante",
										'0001-01-01' AS "FechaLiquidacionComprobante",
										"SUMA"."FechaLiquidacion" AS "FechaOperacion",
										'T0' AS "IdCondicionLiquidacion",
										'BCS'	AS "IdBolsa",
										"SUMA"."IdMoneda" AS "IdMonedaOperacion",
										"SUMA"."Monto" AS "MontoOperacion",
										'' AS "IdTipoReajuste",
										'0001-01-01' AS "FechaParidad",
										0 AS "ValorParidad",
										0 AS "FolioCartera",
										0 AS "IdInstrumento",
										'' AS "IdTipoInstrumento",
										'' AS "IdClasificacionTipoInstrumento",
										'N' AS "Bilateral",
										'S' AS "GeneraComprobante",
										'N' AS "AgruparEnComprobante",
										CASE :v_LiquidacionCuentaInversion WHEN 'S' THEN 'N' ELSE 'S' END AS "MueveCuentaInversion",
										'N' AS "TitularPago",
										"SUMA"."TipoSolicitud",
										CASE "SUMA"."TipoSolicitud" WHEN 'APO' THEN "SUMA"."IdMedioDePagoCargoAGF" ELSE "SUMA"."IdMedioDePagoAbonoAGF" END AS "IdMedioDePago"
								FROM :v_suma_aportes_rescates AS "SUMA"
								INNER JOIN :v_id_carta_liquidacion AS "ID_LIQUIDACION" ON ("SUMA"."Id_Suma" = "ID_LIQUIDACION"."Id_Suma");
		
		v_ingreso_egresos = SELECT	"NumeroReferencia",
										"IdTipoMovimiento",
										"IdTipoPersona",
										"IdPersona",
										"IdPortafolio",
										"IdMoneda",
										"Monto",
										"FechaLiquidacion",
										"IdConcepto",
										"IdTipoPersonaTitularPago",
										"IdPersonaTitularPago",
										"IdPortafolioTitularPago",
										"IdCaja",
										"SecuenciaAgrupacion",
										"SecuenciaRelacionada",
										"Glosa",
										"IdEmpleadoDigitador",
										"IdSucursalEmpleadoDigitador",
										"IdTipoProducto",
										"IdTipoOperacion",
										"IdTipoComprobante",
										"IdComprobante",
										"FechaComprobante",
										"FechaLiquidacionComprobante",
										"BSCL.FondoMutuo.BD.Secuencias::SecuenciaComprobanteLiquidacion".NEXTVAL AS "IdOperacion",
										"FechaOperacion",
										"IdCondicionLiquidacion",
										"IdBolsa",
										"IdMonedaOperacion",
										"MontoOperacion",
										"IdTipoReajuste",
										"FechaParidad",
										"ValorParidad",
										"FolioCartera",
										"IdInstrumento",
										"IdTipoInstrumento",
										"IdClasificacionTipoInstrumento",
										"Bilateral",
										"GeneraComprobante",
										"AgruparEnComprobante",
										"MueveCuentaInversion",
										"TitularPago"
								FROM :v_pre_ingreso_egresos;
			
			v_formas_pagos = SELECT
								   	"IngresoEgreso"."NumeroReferencia",
								   	"PreIngresoEgreso"."IdMedioDePago"  AS "IdMedioDePago",
								   	'MODCAJ'   AS "IdModalidadDePago",
								   	"IngresoEgreso"."IdMoneda" AS "IdMoneda",
								   	"IngresoEgreso"."Monto" AS "Monto",
									0 AS "IdCuentaBancariaCorredora",
									0 AS "IdTarjetaBancariaCorredora",
									0 AS "IdChequera",
									0 AS "NumeroDocumentoCorredora",
									'0001-01-01' AS "FechaDocumentoPagoCorredora",
									0 AS "IdCuentaBancariaSocio",
									0 AS "IdBancoSocio",
									'' AS "NombreCuentaBancariaSocio",
									'' AS "NumeroCuentaBancariaSocio",
									'' AS "IdTipoCuentaBancariaSocio",
									'' AS "NombreTipoCuentaBancariaSocio",
									0 AS "IdTarjetaBancariaSocio",
									'' AS "IdTipoTarjetaBancariaSocio",
									'' AS "NombreTipoTarjetaBancariaSocio",
									'' AS "NumeroTarjetaBancariaSocio",
									'' AS "NumeroSerieSocio",
									0 AS "NumeroDocumentoSocio",
									'0001-01-01' AS "FechaDocumentoPagoSocio"
	        				FROM :v_ingreso_egresos AS "IngresoEgreso"
	        				INNER JOIN :v_pre_ingreso_egresos AS "PreIngresoEgreso" ON ("PreIngresoEgreso"."NumeroReferencia" = "IngresoEgreso"."NumeroReferencia");
	        
	        
	        INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"(
	        	"NumeroReferencia","IdTipoMovimiento","IdTipoPersona","IdPersona","IdPortafolio","IdMoneda","Monto","FechaLiquidacion","IdConcepto","IdTipoPersonaTitularPago",
				"IdPersonaTitularPago","IdPortafolioTitularPago","IdCaja","SecuenciaAgrupacion","SecuenciaRelacionada","Glosa","IdEmpleadoDigitador","IdSucursalEmpleadoDigitador",
				"IdTipoProducto","IdTipoOperacion","IdTipoComprobante","IdComprobante","FechaComprobante","FechaLiquidacionComprobante",
				"IdOperacion","FechaOperacion","IdCondicionLiquidacion","IdBolsa","IdMonedaOperacion","MontoOperacion","IdTipoReajuste","FechaParidad",
				"ValorParidad","FolioCartera","IdInstrumento","IdTipoInstrumento","IdClasificacionTipoInstrumento","Bilateral","GeneraComprobante",
				"AgruparEnComprobante","MueveCuentaInversion","TitularPago"
	        )
	        SELECT "NumeroReferencia","IdTipoMovimiento","IdTipoPersona","IdPersona","IdPortafolio","IdMoneda","Monto","FechaLiquidacion","IdConcepto","IdTipoPersonaTitularPago",
				"IdPersonaTitularPago","IdPortafolioTitularPago","IdCaja","SecuenciaAgrupacion","SecuenciaRelacionada","Glosa","IdEmpleadoDigitador","IdSucursalEmpleadoDigitador",
				"IdTipoProducto","IdTipoOperacion","IdTipoComprobante","IdComprobante","FechaComprobante","FechaLiquidacionComprobante",
				"IdOperacion","FechaOperacion","IdCondicionLiquidacion","IdBolsa","IdMonedaOperacion","MontoOperacion","IdTipoReajuste","FechaParidad",
				"ValorParidad","FolioCartera","IdInstrumento","IdTipoInstrumento","IdClasificacionTipoInstrumento","Bilateral","GeneraComprobante",
				"AgruparEnComprobante","MueveCuentaInversion","TitularPago"
			FROM :v_ingreso_egresos;
			
			INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_FormaDePago"(
				"NumeroReferencia","IdMedioDePago","IdModalidadDePago","IdMoneda","Monto","IdCuentaBancariaCorredora","IdTarjetaBancariaCorredora",
				"IdChequera","NumeroDocumentoCorredora","FechaDocumentoPagoCorredora","IdCuentaBancariaSocio","IdBancoSocio","NombreCuentaBancariaSocio",
				"NumeroCuentaBancariaSocio","IdTipoCuentaBancariaSocio","NombreTipoCuentaBancariaSocio","IdTarjetaBancariaSocio","IdTipoTarjetaBancariaSocio",
				"NombreTipoTarjetaBancariaSocio","NumeroTarjetaBancariaSocio","NumeroSerieSocio", "NumeroDocumentoSocio","FechaDocumentoPagoSocio"
			)
			SELECT "NumeroReferencia","IdMedioDePago","IdModalidadDePago","IdMoneda","Monto","IdCuentaBancariaCorredora","IdTarjetaBancariaCorredora",
				"IdChequera","NumeroDocumentoCorredora","FechaDocumentoPagoCorredora","IdCuentaBancariaSocio","IdBancoSocio","NombreCuentaBancariaSocio",
				"NumeroCuentaBancariaSocio","IdTipoCuentaBancariaSocio","NombreTipoCuentaBancariaSocio","IdTarjetaBancariaSocio","IdTipoTarjetaBancariaSocio",
				"NombreTipoTarjetaBancariaSocio","NumeroTarjetaBancariaSocio","NumeroSerieSocio", "NumeroDocumentoSocio","FechaDocumentoPagoSocio"
			FROM :v_formas_pagos;
			
	        --Ingreso e Egreso
	       CALL "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso" (
					 p_IdEmpresa				=> p_IdEmpresa
				    ,p_IdUsuario				=> p_IdUsuario
				    ,p_IdModulo					=> 'FMM'
				    ,p_simularOperacion			=> 'N'
					,p_UsaReglasNegocio			=> 'S'
					,p_EsIngresoAutomatico		=> 'N'
					,p_out_InformacionContador	=> v_count_informacion_ingreso_egreso
					,p_out_AdvertenciasContador	=> v_count_ingreso_egresos_advertencia
					,p_out_ErroresContador		=> v_count_ingreso_egresos_error
				);
			
			--Verificamos los errores que salieron al hacer el cargo abono
			
			v_out_ErroresDetalle =  SELECT 0 AS "IdInterno", 'UI_ERROR_INGRESO_EGRESO' as "Codigo", CAST(RESULTADO."Campo" AS NVARCHAR(100)) as "ValorParametro", 'Campo' as "NombreParametro"
						FROM "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ResultadoProceso" RESULTADO
						WHERE RESULTADO."Resultado" = 'RECH' AND RESULTADO."Campo" = 'NumeroReferencia'
						UNION ALL SELECT * FROM :v_out_ErroresDetalle;
			
			UPDATE SOLICITUDES
			SET "Rechazo" = 1,
			"Codigo" = 'UI_ERROR_INGRESO_EGRESO'
			FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva AS SOLICITUDES
			INNER JOIN :v_suma_aportes_rescates AS ID_SOLICITUD ON (
				SOLICITUDES."IdInterno" = ID_SOLICITUD."IdInterno"
				AND SOLICITUDES."TipoSolicitud" = ID_SOLICITUD."TipoSolicitud" 
				AND SOLICITUDES."IdPersonaAGF" = ID_SOLICITUD."IdPersonaAGF"
				AND SOLICITUDES."IdMoneda" = ID_SOLICITUD."IdMoneda"
				AND SOLICITUDES."IdPortafolioAGF" = ID_SOLICITUD."IdPortafolioAGF"
  				AND SOLICITUDES."IdMedioDePagoCargoAGF" = ID_SOLICITUD."IdMedioDePagoCargoAGF"
  				AND SOLICITUDES."IdMedioDePagoAbonoAGF" = ID_SOLICITUD."IdMedioDePagoAbonoAGF"
  				AND SOLICITUDES."FechaLiquidacion" = ID_SOLICITUD."FechaLiquidacion"
  				AND SOLICITUDES."FechaCertificacion" = ID_SOLICITUD."FechaCertificacion"
			)
			INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Salida_ResultadoProceso" AS RESULTADO ON  ID_SOLICITUD."Id_Suma" = RESULTADO."Id"
			WHERE RESULTADO."Resultado" = 'RECH' AND RESULTADO."Campo" = 'NumeroReferencia';
			
			UPDATE SOLICITUDES
			SET  SOLICITUDES."FolioCartaLiquidacion" = ID_SOLICITUD."Id_Carta"
			FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva AS SOLICITUDES
			INNER JOIN :v_id_carta_liquidacion AS ID_SOLICITUD ON (
				SOLICITUDES."IdInterno" = ID_SOLICITUD."IdInterno"
				AND SOLICITUDES."TipoSolicitud" = ID_SOLICITUD."TipoSolicitud" 
				AND SOLICITUDES."IdPersonaAGF" = ID_SOLICITUD."IdPersonaAGF"
				AND SOLICITUDES."IdMoneda" = ID_SOLICITUD."IdMoneda"
				AND SOLICITUDES."IdPortafolioAGF" = ID_SOLICITUD."IdPortafolioAGF"
  				AND SOLICITUDES."IdMedioDePagoCargoAGF" = ID_SOLICITUD."IdMedioDePagoCargoAGF"
  				AND SOLICITUDES."IdMedioDePagoAbonoAGF" = ID_SOLICITUD."IdMedioDePagoAbonoAGF"
  				AND SOLICITUDES."FechaLiquidacion" = ID_SOLICITUD."FechaLiquidacion"
  				AND SOLICITUDES."FechaCertificacion" = ID_SOLICITUD."FechaCertificacion"
			)
			WHERE SOLICITUDES."Rechazo" = 0;
			
			SELECT COUNT(1) into v_count_ingreso_egreso
			FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva
			WHERE "Rechazo" = 0;

			IF v_count_ingreso_egreso = 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			IF :v_LiquidacionCuentaInversion = 'S' THEN
			
				v_suma_aportes_rescates_cargo_abono = SELECT SUM("MontoPagoLocal") AS "Monto", "TipoSolicitud", "IdPersonaAGF", "IdMoneda", "IdPortafolioAGF",
													 "IdMedioDePagoCargoAGF", "IdMedioDePagoAbonoAGF", "FechaLiquidacion", "FolioCartaLiquidacion", "IdInterno"
													  FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva
													  WHERE "Rechazo" = 0
													  GROUP BY "TipoSolicitud", "IdPersonaAGF", "IdMoneda", "IdPortafolioAGF",
													  "IdMedioDePagoCargoAGF","IdMedioDePagoAbonoAGF","FechaLiquidacion",
													  "FolioCartaLiquidacion","IdInterno";
				
				v_suma_para_cargo_abono = SELECT 
		                    		--CARGO O ABONO A AGF
		                    		"IdPortafolioAGF" AS "IdPortafolio"
									,"IdMoneda" AS "IdMoneda"
									,CASE "TipoSolicitud" WHEN 'APO' THEN 'MOVABN' ELSE 'MOVCAR' END AS "IdTipoMovimiento"
									,CASE "TipoSolicitud" WHEN 'APO' THEN 'ABPEFM' ELSE 'CAPEFM' END AS "IdConcepto"
									,CASE "TipoSolicitud" WHEN 'APO' THEN 'APORTE_FONDO_MUTUO' ELSE 'RESCATE_FONDO_MUTUO' END AS "Descripcion"
									,'T0' AS "IdCondicionDeLiquidacion"
									,"FechaLiquidacion" AS "FechaCondicionDeLiquidacion"
									,"Monto" AS "Monto"
									,0 AS "IdEjecutivo"
									,0 AS "IdSucursal"
									,'INFM' AS "IdTipoProducto"
									,CASE "TipoSolicitud" WHEN 'APOR' THEN 'APORFM' ELSE 'RESCFM' END AS "IdTipoOperacion"
									,"FolioCartaLiquidacion" AS "IdOperacion"
									,"FechaLiquidacion" AS "FechaOperacion"
									,"FechaLiquidacion" AS "FechaLiquidacionOperacion"
									,null AS "FechaVencimientoOperacion"
									,"IdMoneda" AS "IdMonedaOperacion"
									,"Monto" AS "MontoOperacion"
									,0 AS "TipoCambioOperacion"
									,null AS "IdContraparte"
									,'FMM' AS "IdTipoComprobante"
									,"FolioCartaLiquidacion" AS "IdComprobanteDetalle"
									,"FolioCartaLiquidacion" AS "IdComprobanteFormaDePago"
									,'' AS "IdTipoMovimientoAsociado"
									,0 AS "IdMovimientoAsociado"
									,"FechaLiquidacion" AS "FechaComprobante"
									,null AS "IdPersonaPagoCobro"
									,0 AS "IdInstrumento"
									,'' AS "IdTipoInstrumento"
									,'' AS "Nemo"
									,null AS "EsIndicadorBilateral"
									,'N' AS "EsCargoAbonoPorTraspaso"
									,null AS "IdPortafolioTraspaso"
									,'N' AS "GeneroContraparteEnTesoreria"
									,'S' AS "CertificadoAutomatico"
							FROM :v_suma_aportes_rescates_cargo_abono;
				
				--Preparamos llamada a Cargo y Abono para Cliente y AGF
		        
		        v_cargos_abonos = 	SELECT ROW_NUMBER() OVER (ORDER BY "IdOperacion") AS "NumeroCargoAbono", * FROM(
		                    SELECT 
		                    		--CARGO O ABONO A AGF
		                    		"IdPortafolio"
									,"IdMoneda"
									,"IdTipoMovimiento"
									,"IdConcepto"
									,"Descripcion"
									,"IdCondicionDeLiquidacion"
									,"FechaCondicionDeLiquidacion"
									,"Monto"
									,"IdEjecutivo"
									,"IdSucursal"
									,"IdTipoProducto"
									,"IdTipoOperacion"
									,"IdOperacion"
									,"FechaOperacion"
									,"FechaLiquidacionOperacion"
									,"FechaVencimientoOperacion"
									,"IdMonedaOperacion"
									,"MontoOperacion"
									,"TipoCambioOperacion"
									,"IdContraparte"
									,"IdTipoComprobante"
									,"IdOperacion" AS "IdComprobante"
									,0 AS "FolioComprobante"
									,"IdComprobanteDetalle"
									,"IdComprobanteFormaDePago"
									,"IdTipoMovimientoAsociado"
									,"IdMovimientoAsociado"
									,"FechaComprobante"
									,"IdPersonaPagoCobro"
									, "IdInstrumento"
									,"IdTipoInstrumento"
									,"Nemo"
									,"EsIndicadorBilateral"
									,"EsCargoAbonoPorTraspaso"
									,"IdPortafolioTraspaso"
									,"GeneroContraparteEnTesoreria"
									,"CertificadoAutomatico"
							FROM :v_suma_para_cargo_abono);
		        
		        INSERT INTO "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar"("NumeroCargoAbono","IdPortafolio","IdMoneda","IdTipoMovimiento","IdConcepto","Descripcion","IdCondicionDeLiquidacion","FechaCondicionDeLiquidacion",
			    "Monto","IdEjecutivo","IdSucursal","IdTipoProducto","IdTipoOperacion","IdOperacion","FechaOperacion","FechaLiquidacionOperacion","FechaVencimientoOperacion",
			    "IdMonedaOperacion","MontoOperacion","TipoCambioOperacion","IdContraparte","IdTipoComprobante","IdComprobante","FolioComprobante",
				"IdComprobanteDetalle","IdComprobanteFormaDePago","IdTipoMovimientoAsociado","IdMovimientoAsociado","FechaComprobante","IdPersonaPagoCobro","IdInstrumento",
			    "IdTipoInstrumento","Nemo","EsIndicadorBilateral","EsCargoAbonoPorTraspaso","IdPortafolioTraspaso","GeneroContraparteEnTesoreria","CertificadoAutomatico")
		        SELECT "NumeroCargoAbono","IdPortafolio","IdMoneda","IdTipoMovimiento","IdConcepto","Descripcion","IdCondicionDeLiquidacion","FechaCondicionDeLiquidacion",
			    "Monto","IdEjecutivo","IdSucursal","IdTipoProducto","IdTipoOperacion","IdOperacion","FechaOperacion","FechaLiquidacionOperacion","FechaVencimientoOperacion",
			    "IdMonedaOperacion","MontoOperacion","TipoCambioOperacion","IdContraparte","IdTipoComprobante","IdComprobante","FolioComprobante",
				"IdComprobanteDetalle","IdComprobanteFormaDePago","IdTipoMovimientoAsociado","IdMovimientoAsociado","FechaComprobante","IdPersonaPagoCobro","IdInstrumento",
			    "IdTipoInstrumento","Nemo","EsIndicadorBilateral","EsCargoAbonoPorTraspaso","IdPortafolioTraspaso","GeneroContraparteEnTesoreria","CertificadoAutomatico"
		        FROM :v_cargos_abonos;
		        
		        
		        --Llamada a servicio CrearCargoAbono
		        
		        CALL "BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono" (
						 p_IdEmpresa				=> p_IdEmpresa
					    ,p_IdUsuario				=> p_IdUsuario
					    ,p_IdModulo					=> 'FMM'
					    ,p_simularOperacion			=> 'N'
						,p_UsaReglasNegocio			=> 'S'
						,p_FechaSistema				=> v_FechaSistema
						,p_EsIngresoManual			=> 'N'
						,p_out_InformacionContador	=> v_count_informacion_cargo_abono
						,p_out_AdvertenciasContador	=> v_count_advertencia_cargo_abono
						,p_out_ErroresContador		=> v_count_errores_cargo_abono
				);
				
				--Verificamos los errores que salieron al hacer el cargo abono
				
				v_out_ErroresDetalle =  SELECT 0 AS "IdInterno", RESULTADO."Codigo" as "Codigo", '' as "ValorParametro", '' as "NombreParametro"
							FROM "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle" RESULTADO
							UNION ALL SELECT * FROM :v_out_ErroresDetalle;
				
				UPDATE SOLICITUDES
				SET "Rechazo" = 1,
				"Codigo" = 'UI_ERROR_CARGO_ABONO'
				FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva SOLICITUDES
				INNER JOIN #RESULTADO_LiquidacionSolicitudAporteRescateMasiva SOLICITUDES_NEMO ON (SOLICITUDES."IdSerieFondo" = SOLICITUDES_NEMO."IdSerieFondo")
				INNER JOIN :v_cargos_abonos CARGOS_ABONOS ON (SOLICITUDES_NEMO."IdSolicitudAporteRescate" = CARGOS_ABONOS."IdOperacion")
				INNER JOIN "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle" RESULTADO ON  CARGOS_ABONOS."NumeroCargoAbono" = RESULTADO."NumeroCargoAbono";
							
				SELECT COUNT(1) into v_cargo_abono
				FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva
				WHERE "Rechazo" = 0;
				
				IF v_cargo_abono = 0 THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			
			END IF;
			
			
			UPDATE sol
			SET sol."FechaLiquidacionRegistro" = CURRENT_UTCTIMESTAMP,
				sol."EstadoLiquidacion" = 'LIQ',
				sol."FolioCartaLiquidacion" = RES_CRT."FolioCartaLiquidacion"
			FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS sol
			INNER JOIN #RESULTADO_LiquidacionSolicitudAporteRescateMasiva AS RES_CRT ON (RES_CRT."IdSolicitudAporteRescate" = sol."IdSolicitudAporteRescate")
			WHERE RES_CRT."Rechazo" = 0;
			
			v_log = SELECT 
						"IdInterno" AS "IdCabeceraLog"
						,'LIQUIDACION APORTE RESCATE' AS "IdTipoLog"
						,'FMM' AS "IdModulo"
						,:p_IdUsuario AS "IdUsuario"
						,:p_IdEmpresa AS "IdEmpresa"
						,:v_FechaSistema AS "FechaSistemaLog"
						,:p_IdUsuario AS "IdUsuarioAtendido"
						,0  AS "IdPersona"
						,0 AS "IdCliente"
						,'' AS "EstadoCliente"
						,'' AS "NombreCliente"
						,'' AS "IdTipoDocumentoIdentidad"
						,'' AS "IdDocumentoIdentidad"
						,0 AS "IdPortafolio"
						,'' AS "EstadoPortafolio"
						,'LIQUIDACION_APORTE_RESCATE' AS "IdTipoRegistro"
						,0 AS "IdRegistro"
						,'LIQ' AS "EstadoRegistro"
						,'' AS "IdTipoOperacion"
						,0 AS "Folio"
						,0 AS "Cantidad"
						,0 AS "PrecioTasa"
						,0 AS "Monto"
						,'' AS "IdMonedaOrigen"
						,'' AS "IdMonedaLiquidacion"
						,'' AS "TipoCambio"
						,'' AS "Nemo"
						,0 AS "IdProceso"
						,'' AS "EstadoProceso"
						,'' AS "NombreProceso"
					FROM :p_Filtros;
				
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
			
			
			p_out_Resultado = SELECT "Solicitud"."IdInterno",
								 "Solicitud"."IdSolicitudAporteRescate",
								 "Solicitud"."IdSerieFondo",
								 "Solicitud"."Codigo",
								 "Solicitud"."Estado",
								 "Estado"."Nombre" AS "NombreEstado"
						  FROM (SELECT "IdInterno",
								 "IdSolicitudAporteRescate",
								 "IdSerieFondo",
								 "Codigo",
								 CASE "Rechazo" WHEN 1 THEN 'RZD' ELSE 'APD' END AS "Estado"
						  FROM #RESULTADO_LiquidacionSolicitudAporteRescateMasiva) AS "Solicitud"
						  LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "Estado" ON ("Solicitud"."Estado" = "Estado"."IdEstado");
						  		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'LIQUIDAR SOLICITUD APORTE RESCATE','FIN','FIN');
			
		SIGNAL SALIDA_CONTROLADA;
		END;
	END;
END;
			
			
