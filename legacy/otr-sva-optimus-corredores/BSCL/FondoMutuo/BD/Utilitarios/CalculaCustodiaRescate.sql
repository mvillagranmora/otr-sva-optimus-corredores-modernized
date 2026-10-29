SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.FondoMutuo.BD.Utilitarios::CalculaCustodiaRescate";

CREATE PROCEDURE
--  Proposito: Calcula Monto y cantidad de cuotas para rescate arrojando un error cuando tiene saldo 0 y una advertencia cuando no 
--  alcanza para rescatar lo solicitado, ademas entrega los valores que seran rescatados
--  Autor: Gat-Blac 
--  Fecha Creacion: 20/09/2016
"BSCL.FondoMutuo.BD.Utilitarios::CalculaCustodiaRescate" (
	IN p_IdEmpresa					BIGINT,
	IN p_IdPais						BIGINT,
	IN p_FechaSistema				NVARCHAR(10),
	IN p_IdUsuario					BIGINT,
	IN p_Solicitudes				TABLE	(
										"IdSolicitudAporteRescate"	BIGINT,
										"IdSerieFondo"		   		NVARCHAR(40),
									 	"CalculoPorMontoCuota" 		NVARCHAR(1),
									 	"Monto"				   		DECIMAL(26,8),
									 	"NumeroCuotas"		   		DECIMAL(26,8),
									 	"ValorCuota"		   		DECIMAL(26,8),
									 	"MontoDestino"		   		DECIMAL(26,8),
									 	"ValorParidad"		   		DECIMAL(26,8),
									 	"IdMonedaOrigen"	   		NVARCHAR(3),
									 	"IdMonedaDestino"	   		NVARCHAR(3),
									 	"TipoSolicitud"		   		NVARCHAR(3),
									 	"IdInstrumento"		   		BIGINT,
									 	"IdCliente"			   		BIGINT,
									 	"IdPortafolio"		   		BIGINT,
									 	"MontoMonedaSistema"		DECIMAL(26,8),
									 	"FechaCertificacion"   		DATE
								 	),
	OUT p_out_Resultado		TABLE	(
										"IdSolicitudAporteRescate" 	BIGINT,
									 	"Monto"				   		DECIMAL(26,8),
									 	"NumeroCuotas"		   		DECIMAL(26,8),
									 	"ValorCuota"		   		DECIMAL(26,8),
									 	"MontoDestino"	       		DECIMAL(26,8),
									 	"ValorParidad"		   		DECIMAL(26,8),
									 	"CantidadDisponible"		DECIMAL(26,8),
									 	"MontoMonedaSistema"		DECIMAL(26,8),
									 	"Estado"			   		NVARCHAR(3)
									)
	
)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_arr_portafolios 				BIGINT ARRAY;
	DECLARE v_arr_fechaCertificacion 		DATE ARRAY;
	DECLARE v_arr_instrumentos 				BIGINT ARRAY;
	DECLARE v_arr_saldo 					DECIMAL(26,8) ARRAY;
	DECLARE v_arr_cuotas 					DECIMAL(26,8) ARRAY;
	DECLARE v_arr_id_solicitudes 			BIGINT ARRAY;
	DECLARE v_arr_monto 					DECIMAL(26,8) ARRAY;
	DECLARE v_arr_valor_cuota 				DECIMAL(26,8) ARRAY;
	DECLARE v_arr_monto_destino 			DECIMAL(26,8) ARRAY;
	DECLARE v_arr_monto_sistema 			DECIMAL(26,8) ARRAY;
	DECLARE v_arr_valor_paridad 			DECIMAL(26,8) ARRAY;
	DECLARE v_arr_calculo_monto_cuota		NVARCHAR(1)   ARRAY;
	DECLARE v_portafolio 					BIGINT;
	DECLARE v_fechaCertificacion 			NVARCHAR(10);
	DECLARE v_instrumento 					BIGINT;
	DECLARE v_saldo_actual 					DECIMAL(26,8);
	DECLARE v_solicitudes 					BIGINT;
	DECLARE v_cuota 						DECIMAL(26,8);
	DECLARE v_monto							DECIMAL(26,8);
	DECLARE v_valor_cuota 					DECIMAL(26,8);
	DECLARE v_monto_destino 				DECIMAL(26,8);
	DECLARE v_monto_sistema 				DECIMAL(26,8);
	DECLARE v_valor_paridad 				DECIMAL(26,8);
	DECLARE v_cuotaRetirar 					DECIMAL(26,8);
	DECLARE v_arr_index 					BIGINT;
	DECLARE v_arr_index_solicitudes 		BIGINT;
	DECLARE v_count_advertencias_comision	BIGINT;
	DECLARE v_count_errores_comision		BIGINT;
	DECLARE	v_calculo_monto_cuota			NVARCHAR(1);
	DECLARE v_out_KeyFlujoCustodia			BIGINT;	
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DROP TABLE #RETIROS;
		RESIGNAL;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
	BEGIN
		DROP TABLE #RETIROS;
	END; 
	CREATE LOCAL TEMPORARY TABLE #RETIROS(
		"IdSolicitudAporteRescate" 	BIGINT,
		"Monto"						DECIMAL(26,8),
		"NumeroCuotas"				DECIMAL(26,8),
		"ValorCuota"				DECIMAL(26,8),
		"MontoDestino"				DECIMAL(26,8),
		"ValorParidad"				DECIMAL(26,8),
		"CantidadDisponible"		DECIMAL(26,8),
		"CalculoPorMontoCuota"		NVARCHAR(1),
		"MontoMonedaSistema"		DECIMAL(26,8),
		"Estado"					NVARCHAR(3)
	);
	
	CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CALCULAR CUSTODIA FONDO MUTUO MM','INICIO','INICIO');
	
						
	v_saldosPortafolio = SELECT carteras."IdPortafolio",carteras."IdInstrumento", SUM(COALESCE(carteras."INGCantidad",0)) AS "CantidadDisponiblePortafolio"
						FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" carteras
						INNER JOIN :p_Solicitudes clientes ON (carteras."IdCliente" = clientes."IdCliente" AND carteras."IdPortafolio" = clientes."IdPortafolio" AND clientes."IdInstrumento" = carteras."IdInstrumento")
						WHERE carteras."IdEmpresa" = p_IdEmpresa
						AND   carteras."IdClasificacionTipoInstrumento" = 'FM'
						AND carteras."IdEstado" = 'VIG'
						GROUP BY carteras."IdPortafolio",carteras."IdInstrumento";
	
	v_psolicitudes = SELECT sol."IdSolicitudAporteRescate",sol."IdSerieFondo",sol."CalculoPorMontoCuota",sol."Monto",
							CASE WHEN sol."CalculoPorMontoCuota" = 'T' AND COALESCE(tot."CantidadDisponiblePortafolio",0) != 0  THEN COALESCE(tot."CantidadDisponiblePortafolio",0) ELSE sol."NumeroCuotas" END AS "NumeroCuotas",
							sol."ValorCuota",sol."MontoDestino",sol."ValorParidad",sol."IdMonedaOrigen",
							sol."IdMonedaDestino",sol."TipoSolicitud",sol."IdInstrumento",sol."IdCliente",sol."IdPortafolio",sol."FechaCertificacion",sol."MontoMonedaSistema" 
							FROM :p_Solicitudes sol
							LEFT JOIN :v_saldosPortafolio tot ON (sol."IdPortafolio" = tot."IdPortafolio"
							AND tot."IdInstrumento" = sol."IdInstrumento")
							WHERE sol."TipoSolicitud" = 'RES';
						 
	v_SaldosCustodiaPorPortafolio = SELECT sol."IdPortafolio", sol."FechaCertificacion", sol."IdInstrumento", COALESCE(tot."CantidadDisponiblePortafolio",0) AS "CantidadDisponiblePortafolio",
											SUM(sol."NumeroCuotas") AS "CantidadUtilizadaPortafolio"
											FROM :v_psolicitudes sol
											LEFT JOIN :v_saldosPortafolio tot ON (sol."IdPortafolio" = tot."IdPortafolio"
											AND tot."IdInstrumento" = sol."IdInstrumento")
											WHERE sol."TipoSolicitud" = 'RES'
											GROUP BY sol."IdPortafolio",sol."FechaCertificacion",sol."IdInstrumento", tot."CantidadDisponiblePortafolio";
																			
	v_PortafoliosSinSaldoCustodia = SELECT * FROM :v_SaldosCustodiaPorPortafolio WHERE "CantidadUtilizadaPortafolio" > "CantidadDisponiblePortafolio";
	
	v_PortafoliosConSaldoCustodia = SELECT * FROM :v_SaldosCustodiaPorPortafolio WHERE "CantidadUtilizadaPortafolio" <= "CantidadDisponiblePortafolio";
	
	v_SolicitudesSinSaldoCustodia = SELECT  sol."IdSolicitudAporteRescate", sol."IdSerieFondo", sol."CalculoPorMontoCuota", sol."Monto", sol."NumeroCuotas", sol."IdMonedaOrigen", sol."IdMonedaDestino",
											sol."TipoSolicitud", sol."IdInstrumento", sol."IdCliente", sol."IdPortafolio", sol."FechaCertificacion", sol."ValorCuota",
											sol."MontoDestino", sol."ValorParidad", port."CantidadDisponiblePortafolio",sol."MontoMonedaSistema"
									FROM :v_psolicitudes sol
									INNER JOIN :v_PortafoliosSinSaldoCustodia port ON (sol."IdPortafolio" = port."IdPortafolio" and sol."IdInstrumento" = port."IdInstrumento" and sol."FechaCertificacion" = port."FechaCertificacion");
	
	v_SolicitudesConSaldoCustodia = SELECT sol."IdSolicitudAporteRescate",sol."IdSerieFondo",sol."CalculoPorMontoCuota",sol."Monto",sol."NumeroCuotas",sol."IdMonedaOrigen", sol."IdMonedaDestino",
											sol."TipoSolicitud",sol."IdInstrumento",sol."IdCliente",sol."IdPortafolio",sol."FechaCertificacion", sol."ValorCuota",
											sol."MontoDestino", sol."ValorParidad", port."CantidadDisponiblePortafolio",sol."MontoMonedaSistema"
									FROM :v_psolicitudes sol
									INNER JOIN :v_PortafoliosConSaldoCustodia port ON (sol."IdPortafolio" = port."IdPortafolio" and sol."IdInstrumento" = port."IdInstrumento" and sol."FechaCertificacion" = port."FechaCertificacion");
	
	INSERT INTO #RETIROS("IdSolicitudAporteRescate", "Monto", "NumeroCuotas", "ValorCuota", "MontoDestino", "ValorParidad","CantidadDisponible","CalculoPorMontoCuota","MontoMonedaSistema", "Estado")
	SELECT "IdSolicitudAporteRescate", "Monto", "NumeroCuotas", "ValorCuota", "MontoDestino", "ValorParidad", "CantidadDisponiblePortafolio","CalculoPorMontoCuota","MontoMonedaSistema", 'OK' FROM :v_SolicitudesConSaldoCustodia;
	
	--Comenzamos a iterar por Portafolio para los que no tienen saldo.
	
	v_arr_portafolios = ARRAY_AGG(:v_PortafoliosSinSaldoCustodia."IdPortafolio" ORDER BY "IdPortafolio" ASC);
	v_arr_fechaCertificacion = ARRAY_AGG(:v_PortafoliosSinSaldoCustodia."FechaCertificacion" ORDER BY "IdPortafolio" ASC);
	v_arr_instrumentos = ARRAY_AGG(:v_PortafoliosSinSaldoCustodia."IdInstrumento" ORDER BY "IdPortafolio" ASC);
	v_arr_saldo = ARRAY_AGG(:v_PortafoliosSinSaldoCustodia."CantidadDisponiblePortafolio" ORDER BY "IdPortafolio" ASC);

	FOR v_arr_index IN 1..CARDINALITY(:v_arr_portafolios) DO
		v_portafolio := :v_arr_portafolios[:v_arr_index];
		v_fechaCertificacion := :v_arr_fechaCertificacion[:v_arr_index];
		v_instrumento := :v_arr_instrumentos[:v_arr_index];
		v_solicitudes_cliente = SELECT "IdSolicitudAporteRescate", "NumeroCuotas", "Monto", "ValorCuota", "MontoDestino", "CalculoPorMontoCuota", "ValorParidad", "MontoMonedaSistema" FROM :v_SolicitudesSinSaldoCustodia 
								WHERE "IdPortafolio" = :v_portafolio 
									  AND "FechaCertificacion" = :v_fechaCertificacion
									  AND "IdInstrumento" = :v_instrumento;
		v_arr_cuotas = ARRAY_AGG(:v_solicitudes_cliente."NumeroCuotas" ORDER BY "IdSolicitudAporteRescate" ASC);
		v_arr_id_solicitudes = ARRAY_AGG(:v_solicitudes_cliente."IdSolicitudAporteRescate" ORDER BY "IdSolicitudAporteRescate" ASC);
		v_arr_monto = ARRAY_AGG(:v_solicitudes_cliente."Monto" ORDER BY "IdSolicitudAporteRescate" ASC);
		v_arr_valor_cuota = ARRAY_AGG(:v_solicitudes_cliente."ValorCuota" ORDER BY "IdSolicitudAporteRescate" ASC);
		v_arr_monto_destino = ARRAY_AGG(:v_solicitudes_cliente."MontoDestino" ORDER BY "IdSolicitudAporteRescate" ASC);
		v_arr_monto_sistema = ARRAY_AGG(:v_solicitudes_cliente."MontoMonedaSistema" ORDER BY "IdSolicitudAporteRescate" ASC);
		v_arr_valor_paridad = ARRAY_AGG(:v_solicitudes_cliente."ValorParidad" ORDER BY "IdSolicitudAporteRescate" ASC);
		v_arr_calculo_monto_cuota = ARRAY_AGG(:v_solicitudes_cliente."CalculoPorMontoCuota" ORDER BY "IdSolicitudAporteRescate" ASC);
		v_saldo_actual := :v_arr_saldo[:v_arr_index];
		FOR v_arr_index_solicitudes IN 1..CARDINALITY(:v_arr_id_solicitudes) DO
			v_calculo_monto_cuota :=:v_arr_calculo_monto_cuota[:v_arr_index_solicitudes];
			v_solicitudes := :v_arr_id_solicitudes[:v_arr_index_solicitudes];
			v_cuota := :v_arr_cuotas[:v_arr_index_solicitudes];
			v_monto := :v_arr_monto[:v_arr_index_solicitudes];
			v_valor_cuota := :v_arr_valor_cuota[:v_arr_index_solicitudes];
			v_monto_destino := :v_arr_monto_destino[:v_arr_index_solicitudes];
			v_monto_sistema := :v_arr_monto_sistema[:v_arr_index_solicitudes];
			v_valor_paridad := :v_arr_valor_paridad[:v_arr_index_solicitudes];
			v_cuotaRetirar := 0;
			IF :v_calculo_monto_cuota = 'T' THEN
				v_cuota := :v_saldo_actual; 
			END IF;
			IF :v_saldo_actual - :v_cuota > 0 THEN
				v_cuotaRetirar := :v_cuota;
			ELSE
				IF :v_saldo_actual > 0 THEN
					v_cuotaRetirar := :v_saldo_actual;
				END IF;
			END IF;
			INSERT INTO #RETIROS("IdSolicitudAporteRescate", "Monto", "NumeroCuotas", "ValorCuota", "MontoDestino", "ValorParidad","CantidadDisponible","CalculoPorMontoCuota", "Estado","MontoMonedaSistema")
			SELECT :v_solicitudes, 
				   :v_monto, 
				   CASE WHEN :v_cuotaRetirar = 0 OR :v_cuotaRetirar = :v_cuota THEN :v_cuota ELSE :v_cuotaRetirar END, 
				   :v_valor_cuota, 
				   :v_monto_destino,
				   :v_valor_paridad, 
				   :v_saldo_actual,
				   :v_calculo_monto_cuota,
				   CASE WHEN :v_cuotaRetirar = 0 THEN 'ERR'
				   		WHEN :v_cuotaRetirar > 0 AND :v_cuotaRetirar != :v_cuota AND :v_calculo_monto_cuota != 'T' THEN 'ADV'
				   		ELSE 'OK'
				   		END,
				   :v_monto_sistema
			 FROM DUMMY;
			 v_saldo_actual := :v_saldo_actual - :v_cuota;
		END FOR;
	END FOR;
	
	v_valores_cuota_calcular = SELECT "Retiro"."IdSolicitudAporteRescate" AS "IdInterno",
							 "FechaCertificacion" AS "Fecha",
							 "Solicitudes"."IdSerieFondo",
							 CASE WHEN "Retiro"."Estado" = 'ADV'  OR "Solicitudes"."CalculoPorMontoCuota" = 'T' THEN 'C' ELSE "Solicitudes"."CalculoPorMontoCuota" END AS "CalculoPorMontoCuota",
							 "Retiro"."MontoDestino" AS "Monto",
							 "Retiro"."NumeroCuotas",
							 "Solicitudes"."IdMonedaOrigen" AS "IdMonedaFondo",
							 "Solicitudes"."IdMonedaDestino" AS "IdMonedaInversion",
							 'RES' AS "TipoSolicitud",
							 'UF' AS "TipoFactorAjuste",
							 :p_IdEmpresa AS "IdEmpresa"
					  FROM #RETIROS AS "Retiro"
					  INNER JOIN :v_SolicitudesSinSaldoCustodia AS "Solicitudes" ON ("Retiro"."IdSolicitudAporteRescate" = "Solicitudes"."IdSolicitudAporteRescate")
					  WHERE "Retiro"."Estado" = 'ADV' OR "Retiro"."CalculoPorMontoCuota" = 'T'
					  UNION ALL
					  SELECT "Retiro"."IdSolicitudAporteRescate" AS "IdInterno",
							 "FechaCertificacion" AS "Fecha",
							 "Solicitudes"."IdSerieFondo",
							 CASE WHEN "Retiro"."Estado" = 'ADV' OR "Solicitudes"."CalculoPorMontoCuota" = 'T' THEN 'C' ELSE "Solicitudes"."CalculoPorMontoCuota" END AS "CalculoPorMontoCuota",
							 "Retiro"."MontoDestino" AS "Monto",
							 "Retiro"."NumeroCuotas",
							 "Solicitudes"."IdMonedaOrigen" AS "IdMonedaFondo",
							 "Solicitudes"."IdMonedaDestino" AS "IdMonedaInversion",
							 'RES' AS "TipoSolicitud",
							 'UF' AS "TipoFactorAjuste",
							 :p_IdEmpresa AS "IdEmpresa"
					  FROM #RETIROS AS "Retiro"
					  INNER JOIN :v_SolicitudesConSaldoCustodia AS "Solicitudes" ON ("Retiro"."IdSolicitudAporteRescate" = "Solicitudes"."IdSolicitudAporteRescate")
					  WHERE "Retiro"."CalculoPorMontoCuota" = 'T'; 
	
	CALL "BSCL.FondoMutuo.BD.Utilitarios::CalcularMontoNumeroCuota"(:p_IdEmpresa,:v_valores_cuota_calcular,v_valores_cuota);
									
	UPDATE RETIRO
	SET "Monto" = VALORES."Monto",
		"MontoDestino" = VALORES."MontoDestino",
		"MontoMonedaSistema" = VALORES."MontoMonedaSistema"
	FROM #RETIROS AS RETIRO
	INNER JOIN :v_valores_cuota AS VALORES ON (RETIRO."IdSolicitudAporteRescate" = VALORES."IdInterno")
	WHERE RETIRO."Estado" = 'ADV' OR RETIRO."CalculoPorMontoCuota" = 'T';
	
	p_out_Resultado = SELECT "IdSolicitudAporteRescate", "Monto", "NumeroCuotas", "ValorCuota", "MontoDestino","ValorParidad","CantidadDisponible","MontoMonedaSistema","Estado"
					  FROM #RETIROS;
	
	CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CALCULAR CUSTODIA FONDO MUTUO MM','FIN','FIN');
	
	SIGNAL SALIDA_CONTROLADA;
	
END;
