SET SCHEMA BSCL;
--CALL "BSCL.CuentaInversion.BD.Servicios::ConsultarCuadraturaCalculadaCuentaInversion" (1,0)
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::ConsultarCuadraturaCalculadaCuentaInversion";
CREATE PROCEDURE 
"BSCL.CuentaInversion.BD.Servicios::ConsultarCuadraturaCalculadaCuentaInversion" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa				BIGINT,			--ID Empresa destino
	-- Parametros entrada del servicio publico
	IN p_IdPortafolio			BIGINT
	-- Parametros salida del servicio publico
	/*OUT p_Registros	TABLE (
						"Clasificacion" NVARCHAR(10), 
						"IdCuentaInversion" BIGINT, 
						"IdPortafolio" BIGINT, 
						"DocumentoIdentidad" NVARCHAR(12), 
						"CodigoPortafolio" NVARCHAR(10), 
						"IdMoneda" NVARCHAR(3), 
						"SaldoCalculado" DECIMAL(28,8), 
						"SaldoConsulta" DECIMAL(28,8), 
						"DiferenciaSaldo" DECIMAL(28,8)
					) default empty
	*/
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	--------------------
	--DECLARE Manejo de Errores
	--------------------			
	
	BEGIN	
		
		CREATE LOCAL TEMPORARY COLUMN TABLE #p_MovimientosPorFecha AS (
			SELECT FAC."FechaSistema" AS "Fecha", POR."IdCliente", POR."IdPortafolio", 
			FAC."IdMonedaComprobante" AS "IdMoneda", 
			CASE 
				WHEN FAC."IdTipoInstrumento" = 'CFI' AND FAC."IdMonedaLiquidacion" = 'USD' THEN 
					CASE 
						WHEN FAC."IdTipoOperacion" IN ('ANCCFI') THEN 1 * ABS(FAC."MontoCobro")
						WHEN FAC."IdTipoOperacion" IN ('ANVCFI') THEN -1 * ABS(FAC."MontoCobro")
						ELSE -1 * ABS(FAC."MontoCobro")
					END
				WHEN FAC."IdTipoInstrumento" = 'PAG' AND FAC."IdMonedaLiquidacion" = 'USD' THEN 0 
				ELSE
					CASE FAC."IdTipoOperacion"
						--COMPRA SPOT CARGO EN DOLAR, ABONO EN PESOS
						--VENTA SPOT ABONO EN DOLAR, CARGO EN PESOS
						WHEN 'VENSPT' THEN -1 * ABS(FAC."MontoComprobanteFinal")
						WHEN 'COMSPT' THEN ABS(FAC."MontoComprobanteFinal")
						WHEN 'COMCFI' THEN -1 * ABS(FAC."MontoComprobanteFinal")
						WHEN 'VENCFI' THEN ABS(FAC."MontoComprobanteFinal")
						WHEN 'COMACN' THEN -1 * ABS(FAC."MontoComprobanteFinal")
						WHEN 'VENACN' THEN ABS(FAC."MontoComprobanteFinal")
						ELSE 
							CASE 
								WHEN FAC."IdConcepto" = 'COMPUT' THEN -1 * FAC."MontoComprobanteFinal" 
								WHEN FAC."IdTipoOperacion" LIKE 'AN%' THEN 
									CASE 
										WHEN FAC."IdTipoOperacion" IN ('ANCSPT') THEN -1 * ABS(FAC."MontoComprobanteFinal")
										WHEN FAC."IdTipoOperacion" IN ('ANVSPT') THEN 1 * ABS(FAC."MontoComprobanteFinal")
										WHEN FAC."IdTipoOperacion" IN ('ANTPRT') THEN FAC."MontoComprobanteFinal"
										WHEN FAC."IdTipoOperacion" IN ('ANTEMP') THEN FAC."MontoComprobanteFinal"
										ELSE -1 * FAC."MontoComprobanteFinal"
									END
								ELSE FAC."MontoComprobanteFinal" 
							END
					END 
			END AS "Monto", COALESCE(CONCAT('FAC-',FAC."IdTipoInstrumento"),'FAC') AS "IdModulo", FAC."IdComprobante" AS "IdOperacion"
			--,FAC."IdTipoInstrumento", FAC."IdMoneda", FAC."MontoComprobanteFinal", FAC."IdEstadoComprobante", * 
			FROM BSCL."BSCL.Facturacion.BD::FAC.Comprobante" FAC 
			INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON FAC."IdPortafolio" = CTA."IdPortafolio" AND CTA."IdMoneda" = FAC."IdMonedaComprobante"
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
			WHERE FAC."FechaLiquidacion" > '2022-01-14' AND FAC."FechaSistema" > '2022-01-14' AND 
			FAC."IdEstadoComprobante" IN ('ENV','FAC') AND FAC."IdMonedaComprobante" = 'CLP' AND FAC."IdTipoOperacion" NOT IN ('COMSIM','VENSIM','ANSIMV','ANSIMC') --AND POR."IdPortafolio" = 5002 ORDER BY 1
			--AND CTA."IdCuentaInversion" = 239 ORDER BY 1
			--AND FAC."IdComprobante" IN (416037,416038) ORDER BY 1
			--SELECT "FechaSistema", "IdMonedaComprobante", "IdMoneda", "IdMonedaLiquidacion", "Cantidad", "MontoComprobanteFinal", * FROM BSCL."BSCL.Facturacion.BD::FAC.Comprobante" WHERE "IdPortafolio" = 2683 order by 1
			
			UNION ALL
			
			SELECT FAC."FechaLiquidacion" AS "Fecha", POR."IdCliente", POR."IdPortafolio", 
			FAC."IdMonedaComprobante" AS "IdMoneda",
			CASE FAC."IdTipoOperacion"
				WHEN 'COMSIM' THEN -1 * ABS(FAC."MontoComprobanteFinal")
				WHEN 'VENSIM' THEN ABS(FAC."MontoComprobanteFinal")
				ELSE FAC."MontoComprobanteFinal"
			END AS "Monto", 'SIM' AS "IdModulo", FAC."IdComprobante" AS "IdOperacion"
			--, FAC."MontoComprobanteFinal", FAC."FechaComprobante", "EsAPlazo", * 
			FROM BSCL."BSCL.Facturacion.BD::FAC.Comprobante" FAC 
			INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON FAC."IdPortafolio" = CTA."IdPortafolio" AND CTA."IdMoneda" = FAC."IdMonedaComprobante"
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
			WHERE FAC."FechaLiquidacion" > '2022-01-14' AND FAC."FechaComprobante" > '2022-01-14' AND 
			FAC."IdEstadoComprobante" IN ('ENV','FAC') AND FAC."IdMonedaComprobante" = 'CLP' AND FAC."IdTipoOperacion" IN ('COMSIM','VENSIM') 
			AND FAC."FechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
			AND FAC."EsAPlazo" = 'N'
			--AND FAC."IdComprobante" = 361659
			--AND POR."IdPortafolio" = 537 ORDER BY 1
		
			UNION ALL
			
			SELECT TES."FechaMovimiento" AS "Fecha", POR."IdCliente", POR."IdPortafolio", TES."IdMoneda",
			CASE TES."IdTipoMovimiento"
				WHEN 'MOVEGR' THEN -1*TES."Monto"
				WHEN 'MOVING' THEN TES."Monto" 
				ELSE TES."Monto"
			END AS "Monto", 'TES' AS "IdModulo", TES."IdComprobante" AS "IdOperacion" FROM BSCL."BSCL.Tesoreria.BD::TES.Comprobante" TES
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON TES."IdPortafolioTitularPago" = POR."IdPortafolio"
			WHERE TES."IdEstado" = 'LIQ' AND "FechaMovimiento" > '2022-01-14' AND "IdMoneda" = 'CLP' --AND "IdPortafolioTitularPago" = 5541 ORDER BY 1
			
			UNION ALL
			
			SELECT FM."FechaCertificacion" AS "Fecha", POR."IdCliente", POR."IdPortafolio", FM."IdMoneda",
			CASE FM."TipoSolicitud"
				WHEN 'APO' THEN -1*FM."Monto"
				WHEN 'RES' THEN FM."Monto" 
				ELSE FM."Monto"
			END AS "Monto", 'FM' AS "IdModulo", FM."IdSolicitudAporteRescate" AS "IdOperacion"
			FROM BSCL."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" FM  
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON FM."IdPortafolio" = POR."IdPortafolio"
			WHERE FM."Estado" = 'CRT' AND FM."FechaCertificacion" > '2022-01-14' 
			AND "IdMoneda" = 'CLP' AND "IdTipoMovimientoFondoMutuo" NOT IN ('INGTIT','RETTIT') --AND POR."IdPortafolio" = 3334 ORDER BY 1
			
			UNION ALL
			
			SELECT EVEC."FechaPago" AS "Fecha", POR."IdCliente", POR."IdPortafolio", CASE WHEN EVEC."IdTipoEventoCapital" = 'CC' THEN EVEC."IdMoneda" ELSE EVE."IdMonedaLiquidacion" END AS "IdMoneda",--EVE."IdMoneda",
			EVEC."Total" AS "Monto", EVEC."IdTipoEventoCapital" AS "IdModulo", EVEC."IdEventoCapitalCliente" AS "IdOperacion" FROM BSCL."BSCL.Custodia.BD::CUS.EventoCapitalCliente" EVEC
			INNER JOIN BSCL."BSCL.Custodia.BD::CUS.EventoCapital" EVE ON EVE."IdEventoCapital" = EVEC."IdEventoCapital"
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON EVEC."IdPortafolio" = POR."IdPortafolio"
			WHERE EVEC."IdEstado" = 'CER' /*AND EVEC."TipoSocioNegocio" NOT IN ('CNT') AND EVEC."Total" >= 0*/ /*AND EVE."Origen" NOT IN ('RUE')*/ 
			AND (EVE."IdMonedaLiquidacion" = 'CLP' OR (EVEC."IdMoneda" = 'CLP' AND EVEC."IdTipoEventoCapital" = 'CC' /*AND EVEC."Total" >= 0*/)) --AND EVEC."IdPortafolio" = 647 ORDER BY 1
			--EVEC."DocumentoIdentidad" = '76184068-1'
		
			--SELECT "FechaPago", "IdTipoEventoCapital", "IdMoneda", "Total", "IdEstado", * FROM BSCL."BSCL.Custodia.BD::CUS.EventoCapitalCliente" WHERE "IdPortafolio" = 5486
			
			UNION ALL
			
			SELECT MES."FechaOperacion" AS "Fecha",POR."IdCliente", POR."IdPortafolio", MES."IdMonedaPrincipal" AS "IdMoneda",
			CASE MES."IdTipoOperacion"
			/*
				WHEN 'VENTER' THEN -1 * MES."MontoOperacionFinal"
				WHEN 'COMTER' THEN MES."MontoOperacionFinal" 
				WHEN 'VENPAC' THEN -1 * MES."MontoOperacionFinal"
				WHEN 'COMPAC' THEN MES."MontoOperacionFinal"
				ELSE MES."MontoOperacionFinal"
			*/
			WHEN 'VENTER' THEN -1 * MES."MontoOperacionFinal"
				WHEN 'COMTER' THEN MES."MontoOperacionFinal" 
				WHEN 'VENPAC' THEN -1 * MES."MontoOperacion"
				WHEN 'COMPAC' THEN MES."MontoOperacion"
				ELSE MES."MontoOperacion"
			END AS "Monto", 'MES' AS "IdModulo", MES."IdOperacion" AS "IdOperacion" FROM BSCL."BSCL.Mesa.BD::MES.Operacion" MES 
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON MES."IdPortafolio" = POR."IdPortafolio"
			WHERE (MES."IdEstado" = 'VIG' OR (MES."IdEstado" IN ('LIQ','ANT') AND MES."IdTipoOperacion" IN ('VENPAC','COMPAC'))) AND "IdMonedaPrincipal" = 'CLP' 
			AND MES."FechaOperacion" > '2022-01-14'
			--AND MES."IdPortafolio" = 5132 ORDER BY 1
		
			UNION ALL
			
			SELECT MES."FechaVencimiento" AS "Fecha",POR."IdCliente", POR."IdPortafolio", MES."IdMonedaPrincipal" AS "IdMoneda",
			CASE MES."IdTipoOperacion"
				WHEN 'VENPAC' THEN MES."MontoOperacionFinal"
				WHEN 'COMPAC' THEN -1 * MES."MontoOperacionFinal"
				ELSE MES."MontoOperacionFinal"
			END AS "Monto", 'VPC' AS "IdModulo", MES."IdOperacion" AS "IdOperacion" FROM BSCL."BSCL.Mesa.BD::MES.Operacion" MES 
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON MES."IdPortafolio" = POR."IdPortafolio"
			WHERE MES."IdEstado" = 'LIQ' AND MES."IdTipoOperacion" IN ('VENPAC','COMPAC') AND "IdMonedaPrincipal" = 'CLP' --AND MES."IdPortafolio" = 517 ORDER BY 1
			
			UNION ALL
			
			SELECT MES."FechaLiquidacion" AS "Fecha",POR."IdCliente", POR."IdPortafolio", MES."IdMonedaPrincipal" AS "IdMoneda",
			CASE MES."IdTipoOperacion"
				WHEN 'VENPAC' THEN MES."MontoOperacionFinalAnticipo"
				WHEN 'COMPAC' THEN -1 * MES."MontoOperacionFinalAnticipo"
				ELSE MES."MontoOperacionFinalAnticipo"
			END AS "Monto", 'APC' AS "IdModulo", MES."IdOperacion" AS "IdOperacion" FROM BSCL."BSCL.Mesa.BD::MES.Operacion" MES 
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON MES."IdPortafolio" = POR."IdPortafolio"
			WHERE MES."IdEstado" = 'ANT' AND MES."IdTipoOperacion" IN ('VENPAC','COMPAC') AND "IdMonedaPrincipal" = 'CLP' --AND MES."IdPortafolio" = 517 ORDER BY 1
			--AND "IdOperacion" = 116946
		
			UNION ALL
			
			SELECT CAR."EGRFechaOperacion" AS "Fecha", CAR."IdCliente", CAR."IdPortafolio", CAR."IdMonedaSistema" AS "IdMoneda",
			CASE CAR."IdTipoOperacion"
				WHEN 'VENSIM' THEN -1 * CAR."INGMontoAPlazo"
				WHEN 'COMSIM' THEN CAR."INGMontoAPlazo" 
				ELSE CAR."INGMontoAPlazo"
			END AS "Monto", 'VSM' AS "IdModulo", CAR."IdCartera" AS "IdOperacion"--, "EGRFechaLiquidacion", "FechaLiquidacion", "INGFechaLiquidacion", "MontoAPlazo", "INGMontoAPlazo", "EGRMontoAPlazo", *
			FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR WHERE "EGRFechaOperacion" > '2022-01-14' AND "IdTipoInstrumento" IN ('SIM') AND "IdEstado" = 'VCD'
			AND CAR."IdMonedaSistema" = 'CLP'  
			--AND "FolioCartera" NOT IN (SELECT CAR."FolioCartera" FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR  WHERE "EGRFechaOperacion" > '2022-01-14' AND "IdTipoInstrumento" IN ('SIM') AND "IdEstado" = 'ANT')
			--AND "IdPortafolio" = 1721 ORDER BY 1
		
			UNION ALL
			
			SELECT 
			CASE WHEN "EGRIdTipoOperacion" LIKE 'ANT%' 
				THEN CAR."EGRFechaOperacion"
				ELSE CAR."EGRFechaLiquidacion"
			END AS "Fecha", 
			CAR."IdCliente", CAR."IdPortafolio", CAR."IdMonedaSistema" AS "IdMoneda",
			-1 * CAR."EGRMontoLiquidacion" AS "Monto", 'VFW' AS "IdModulo", CAR."IdCartera" AS "IdOperacion"
			FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR WHERE "EGRFechaLiquidacion" > '2022-01-14' AND "IdTipoInstrumento" IN ('FWD') AND "IdEstado" IN ('VCD','LIQ') AND "EGRIdTipoOperacion" NOT IN('ANUFWD') 
			AND CAR."IdMonedaSistema" = 'CLP' --AND "IdPortafolio" = 4950 ORDER BY 1
			--AND CAR."IdPortafolio" = 2683 ORDER BY 1
			
			UNION ALL
		
			SELECT 
			"EGRFechaOperacion" AS "Fecha", CAR."IdCliente", CAR."IdPortafolio", CAR."IdMonedaSistema" AS "IdMoneda",
			--"INGMontoATasaCompraActual" AS "Monto", 
			"EGRCantidad" * "EGRValorRescate" AS "Monto",
			'VIF' AS "IdModulo", CAR."IdCartera" AS "IdOperacion"--, "EGRCantidad", "EGRValorRescate", "IdTipoOperacion", "EGRIdTipoOperacion", *
			FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR WHERE "EGRFechaOperacion" > '2022-01-14' AND "IdTipoInstrumento" IN ('PAG') AND "IdEstado" = 'VCD' 
			AND "IdTipoReajuste" = 'CLP' AND "EGRFechaOperacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
			AND "EGRIdTipoOperacion" NOT IN ('VCDPAC') --AND "IdTipoOperacion" <> 'VENPAC'
			--AND "IdTipoOperacion" NOT IN ('VENPAC','COMPAC') 
			--AND "IdPortafolio" = 5797--517 -- 4743  ORDER BY 1
			
			UNION ALL
		
			SELECT 
			"FechaVencimientoInstrumento" AS "Fecha", CAR."IdCliente", CAR."IdPortafolio", CAR."IdMonedaSistema" AS "IdMoneda",
			"INGCantidad" * "INGValorRescate" AS "Monto",
			'VIF' AS "IdModulo", CAR."IdCartera" AS "IdOperacion"--, "IdTipoOperacion", "EGRFechaOperacion", "FechaVencimientoInstrumento", "IdEstado", "EGRIdTipoOperacion", "IdTipoReajuste", "IdTipoProducto", *
			FROM (
				SELECT ROW_NUMBER() OVER (PARTITION BY "FolioCartera" ORDER BY "IdCartera" DESC) AS nn,* FROM BSCL."BSCL.Custodia.BD::CUS.Cartera"
				WHERE "IdTipoInstrumento" = 'PAG'
			) CAR WHERE nn = 1 AND "FechaVencimientoInstrumento" > '2022-01-14' AND "EGRFechaOperacion" <> "FechaVencimientoInstrumento" AND "IdEstado" = 'VCD' 
			AND "EGRIdTipoOperacion" != 'VCDPAC' AND "IdTipoReajuste" = 'CLP' /*AND "IdTipoProducto" = 'MERF'*/ --AND "EGRFechaOperacion" = '0001-01-01'
			--AND "IdTipoOperacion" NOT IN ('VENPAC','COMPAC')
			--AND "IdTipoOperacion" <> 'VENPAC'
			AND "FechaVencimientoInstrumento" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
			--AND "IdPortafolio" = 5797 --ORDER BY "IdCartera"
			
			UNION ALL
			
			SELECT CAR."EGRFechaOperacion" AS "Fecha", CAR."IdCliente", CAR."IdPortafolio", CAR."IdMonedaSistema" AS "IdMoneda", 
			CASE CAR."IdTipoOperacion"
				WHEN 'VENSIM' THEN -1 * CAR."EGRMontoARecibirAnticipo"
				WHEN 'COMSIM' THEN CAR."EGRMontoARecibirAnticipo" 
				ELSE CAR."EGRMontoARecibirAnticipo"
			END AS "Monto", 'ASM' AS "IdModulo", CAR."IdCartera" AS "IdOperacion"
			FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR  WHERE "EGRFechaOperacion" > '2022-01-14' AND "IdTipoInstrumento" IN ('SIM') AND "IdEstado" = 'ANT' --AND "IdPortafolio" = 7812 ORDER BY 1
			--AND CAR."IdCartera" = 130322
			
			UNION ALL
			
			SELECT TRA."FechaOperacion" AS "Fecha", TRA."IdCliente", TRA."IdPortafolio", TRA."IdMonedaSistema" AS "IdMoneda",  
			CASE TRA."IdTipoOperacion"
				WHEN 'INGGTA' THEN -1 * TRA."MontoCompraMonedaInstrumento"
				WHEN 'RETGTA' THEN TRA."MontoCompraMonedaInstrumento" 
				ELSE TRA."MontoCompraMonedaInstrumento"
			END AS "Monto", 'GTA' AS "IdModulo", TRA."IdMovimientoCarteraEnTransito" AS "IdOperacion"
			FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRA WHERE "IdOrigenGarantia" = 'CTA' AND "IdEstado" = 'CRT' --AND "IdPortafolio" = 6074 ORDER BY 1
			
			UNION ALL
			
			SELECT AJU."FechaDetectado" AS "Fecha", POR."IdCliente", POR."IdPortafolio", CTA."IdMoneda", AJU."MontoAjuste" AS "Monto", 'AJU' AS "IdModelo", 0 AS "IdOperacion" 
			FROM "BSCL.Plataforma.BD::CUA.MovimientosCuentaInversionAjusteOperaciones" AJU
			INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON AJU."IdCuentaInversion" = CTA."IdCuentaInversion"
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
		);
		
		-------------CALCULA SALDOS CON OPERACIONES----------------------
		--DROP TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoCalculado";
		--CREATE COLUMN TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoCalculado" AS (
		--DROP TABLE #p_SaldoCuentaInversionCalculado;
		CREATE LOCAL TEMPORARY TABLE #p_SaldoCuentaInversionCalculado AS (
			SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", SUM("Monto") AS "Monto" FROM (
				SELECT 
					'2022-01-14' AS "Fecha",
					POR."IdCliente", POR."IdPortafolio",
					CASE CTA_CLI 
						WHEN 'CCTE' THEN 'CLP'
						WHEN 'CDOL' THEN 'USD'
						WHEN 'EURO' THEN 'EUR'
					ELSE '' END AS "IdMoneda",
					SALDO_DISP + SALDO_RETEN AS "Monto",'INI' AS "IdModulo" FROM BSCL.TBCTCTCL CTCL	
				INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON POR."CodigoPortafolio" NOT LIKE '%APV%' AND TRIM(RUT_CLI) = POR."DocumentoIdentidad" AND SEC_RUT_CLI = POR."CodigoPortafolio"
				WHERE COD_PER = '2022-01' AND CTA_CLI = 'CCTE' --AND TRIM(RUT_CLI) = '97043000-8'
				UNION ALL
				SELECT LEFT("Fecha",10) AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", SUM("Monto") AS "Monto", "IdModulo" FROM 
					#p_MovimientosPorFecha
				GROUP BY "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "IdModulo"
			) 
			GROUP BY "Fecha", "IdCliente", "IdPortafolio", "IdMoneda"
			ORDER BY 1
		);
		
		-------------OBTIENE SALDOS CUENTA INVERSION-------------------
		--DROP TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha";
		--CREATE COLUMN TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha" AS (
		--DROP TABLE #p_SaldosPorFecha;
		CREATE LOCAL TEMPORARY TABLE #p_SaldosPorFecha AS (
			SELECT "FechaSistema" AS "Fecha", "IdCuentaInversion", "IdCliente", "IdPortafolio", "IdMoneda", "SaldoPorFechaMovimiento" AS "Saldo" FROM (
				SELECT RANK() OVER (PARTITION BY MOV."FechaSistema",MOV."IdCuentaInversion" ORDER BY MOV."IdCargoAbonoMovimiento" DESC)  as RANK, 
				MOV."FechaSistema", CTA."IdCuentaInversion", POR."IdCliente", POR."IdPortafolio", CTA."IdMoneda", MOV."SaldoPorFechaMovimiento"
				FROM BSCL."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV
				INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdCuentaInversion" = CTA."IdCuentaInversion"
				INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
				WHERE MOV."FechaSistema" > '2022-01-14' AND MOV."IdEstado" = 'CRT' AND CTA."IdMoneda" = 'CLP' --AND POR."IdPortafolio" = 6970
			) WHERE RANK = 1
		);
		
		--SE GUARDAN IDS DESCUADRADOS
		--SELECT * FROM #p_CuadraturaCuentaInversionIds
		--DROP TABLE #p_CuadraturaCuentaInversionIds;
		CREATE LOCAL TEMPORARY TABLE #p_CuadraturaCuentaInversionIds AS (
		SELECT DISTINCT CTA."IdCuentaInversion" FROM (
			SELECT SALDO."IdCliente", SALDO."IdPortafolio", SALDO."IdMoneda", MOV."SaldoMov" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta" FROM (
				SELECT S."Fecha", S."IdCliente", S."IdPortafolio", S."IdMoneda", S."Saldo" FROM #p_SaldosPorFecha S
				INNER JOIN (
					SELECT MAX("Fecha") AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda" FROM #p_SaldosPorFecha
					GROUP BY "IdCliente", "IdPortafolio", "IdMoneda"
					) SFinal 
				ON S."Fecha" = SFinal."Fecha" AND S."IdCliente" = SFinal."IdCliente" AND S."IdPortafolio" = SFinal."IdPortafolio" AND S."IdMoneda" = SFinal."IdMoneda"
				) SALDO 
				LEFT JOIN (
					SELECT MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda", SUM(MOV."Monto") AS "SaldoMov" FROM #p_SaldoCuentaInversionCalculado MOV 
					GROUP BY MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda"
				) MOV ON SALDO."IdCliente" = MOV."IdCliente" AND SALDO."IdPortafolio" = MOV."IdPortafolio" AND SALDO."IdMoneda" = MOV."IdMoneda"
			WHERE SALDO."IdMoneda" = 'CLP' 
			--AND SALDO."IdPortafolio" IN (12116,584,185,794)
			AND (ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) > 100 OR ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) < -100) --1142 NOK --427 --332 --293 --199--312--300--235--191
		) SALDO 
		INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON SALDO."IdPortafolio" = POR."IdPortafolio"
		INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON SALDO."IdMoneda" = CTA."IdMoneda" AND CTA."IdPortafolio" = POR."IdPortafolio"
		WHERE POR."DocumentoIdentidad" <> '96921130-0' --CTA PROPIA
		ORDER BY 1
		);
		
		--CASOS DIVIDENDOS
		--DROP TABLE #p_IdsCuentaInversionDIV;
		CREATE LOCAL TEMPORARY TABLE #p_IdsCuentaInversionDIV AS (
		SELECT DISTINCT CTA."IdCuentaInversion"
		--, POR."NombreCompleto", POR."DocumentoIdentidad", POR."CodigoPortafolio", MOV."Fecha" AS "FechaOperacion", MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda" AS "MonedaOperacion", MOV."Monto" AS "MontoOperacion", MOV."IdModulo", MOV."IdOperacion" 
		FROM (
			SELECT 
				'2022-01-14' AS "Fecha",
				POR."IdCliente", POR."IdPortafolio",
				CASE CTA_CLI 
					WHEN 'CCTE' THEN 'CLP'
					WHEN 'CDOL' THEN 'USD'
					WHEN 'EURO' THEN 'EUR'
				ELSE '' END AS "IdMoneda",
				SALDO_DISP + SALDO_RETEN AS "Monto",'INI' AS "IdModulo", 0 AS "IdOperacion" FROM BSCL.TBCTCTCL CTCL	
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON POR."CodigoPortafolio" NOT LIKE '%APV%' AND TRIM(RUT_CLI) = POR."DocumentoIdentidad" AND SEC_RUT_CLI = POR."CodigoPortafolio"
			WHERE COD_PER = '2022-01' AND CTA_CLI = 'CCTE' --AND TRIM(RUT_CLI) = '97043000-8'
			UNION ALL
			SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "Monto", "IdModulo", "IdOperacion" FROM #p_MovimientosPorFecha
		) MOV INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON MOV."IdPortafolio" = POR."IdPortafolio"
		INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON CTA."IdPortafolio" = POR."IdPortafolio"
		WHERE CTA."IdCuentaInversion" IN (SELECT "IdCuentaInversion" FROM #p_CuadraturaCuentaInversionIds)
		AND MOV."IdModulo" = 'DV' AND MOV."Monto" < 0
		);
		
		--CASOS PACTOS
		--DROP TABLE #p_IdsCuentaInversionPAC;
		CREATE LOCAL TEMPORARY TABLE #p_IdsCuentaInversionPAC AS (
		SELECT DISTINCT CTA."IdCuentaInversion"
			--, POR."NombreCompleto", POR."DocumentoIdentidad", POR."CodigoPortafolio", MOV."Fecha" AS "FechaOperacion", MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda" AS "MonedaOperacion", MOV."Monto" AS "MontoOperacion", MOV."IdModulo", MOV."IdOperacion" 
			FROM (
				SELECT 
					'2022-01-14' AS "Fecha",
					POR."IdCliente", POR."IdPortafolio",
					CASE CTA_CLI 
						WHEN 'CCTE' THEN 'CLP'
						WHEN 'CDOL' THEN 'USD'
						WHEN 'EURO' THEN 'EUR'
					ELSE '' END AS "IdMoneda",
					SALDO_DISP + SALDO_RETEN AS "Monto",'INI' AS "IdModulo", 0 AS "IdOperacion" FROM BSCL.TBCTCTCL CTCL	
				INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON POR."CodigoPortafolio" NOT LIKE '%APV%' AND TRIM(RUT_CLI) = POR."DocumentoIdentidad" AND SEC_RUT_CLI = POR."CodigoPortafolio"
				WHERE COD_PER = '2022-01' AND CTA_CLI = 'CCTE' --AND TRIM(RUT_CLI) = '97043000-8'
				UNION ALL
				SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "Monto", "IdModulo", "IdOperacion" FROM #p_MovimientosPorFecha
			) MOV INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON MOV."IdPortafolio" = POR."IdPortafolio"
			INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON CTA."IdPortafolio" = POR."IdPortafolio"
			WHERE CTA."IdCuentaInversion" IN (SELECT "IdCuentaInversion" FROM #p_CuadraturaCuentaInversionIds)
			AND MOV."IdModulo" = 'MES' --AND MOV."Monto" < 0
			AND CTA."IdCuentaInversion" NOT IN (
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionDIV
			)
		);
		
		--CASOS SIM
		--DROP TABLE #p_IdsCuentaInversionSIM;
		CREATE LOCAL TEMPORARY TABLE #p_IdsCuentaInversionSIM AS (
		SELECT DISTINCT CTA."IdCuentaInversion"
			--, POR."NombreCompleto", POR."DocumentoIdentidad", POR."CodigoPortafolio", MOV."Fecha" AS "FechaOperacion", MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda" AS "MonedaOperacion", MOV."Monto" AS "MontoOperacion", MOV."IdModulo", MOV."IdOperacion" 
			FROM (
				SELECT 
					'2022-01-14' AS "Fecha",
					POR."IdCliente", POR."IdPortafolio",
					CASE CTA_CLI 
						WHEN 'CCTE' THEN 'CLP'
						WHEN 'CDOL' THEN 'USD'
						WHEN 'EURO' THEN 'EUR'
					ELSE '' END AS "IdMoneda",
					SALDO_DISP + SALDO_RETEN AS "Monto",'INI' AS "IdModulo", 0 AS "IdOperacion" FROM BSCL.TBCTCTCL CTCL	
				INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON POR."CodigoPortafolio" NOT LIKE '%APV%' AND TRIM(RUT_CLI) = POR."DocumentoIdentidad" AND SEC_RUT_CLI = POR."CodigoPortafolio"
				WHERE COD_PER = '2022-01' AND CTA_CLI = 'CCTE' --AND TRIM(RUT_CLI) = '97043000-8'
				UNION ALL
				SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "Monto", "IdModulo", "IdOperacion" FROM #p_MovimientosPorFecha
			) MOV INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON MOV."IdPortafolio" = POR."IdPortafolio"
			INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON CTA."IdPortafolio" = POR."IdPortafolio"
			WHERE CTA."IdCuentaInversion" IN (SELECT "IdCuentaInversion" FROM #p_CuadraturaCuentaInversionIds)
			AND MOV."IdModulo" = 'SIM' --AND MOV."Monto" < 0
			AND CTA."IdCuentaInversion" NOT IN (
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionDIV
				UNION ALL
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionPAC
			)
		);
		
		--CASOS SPOT
		--DROP TABLE #p_IdsCuentaInversionSPOT;
		CREATE LOCAL TEMPORARY TABLE #p_IdsCuentaInversionSPOT AS (
			SELECT DISTINCT CTA."IdCuentaInversion"
			--, POR."NombreCompleto", POR."DocumentoIdentidad", POR."CodigoPortafolio", MOV."Fecha" AS "FechaOperacion", MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda" AS "MonedaOperacion", MOV."Monto" AS "MontoOperacion", MOV."IdModulo", MOV."IdOperacion" 
			FROM (
				SELECT 
					'2022-01-14' AS "Fecha",
					POR."IdCliente", POR."IdPortafolio",
					CASE CTA_CLI 
						WHEN 'CCTE' THEN 'CLP'
						WHEN 'CDOL' THEN 'USD'
						WHEN 'EURO' THEN 'EUR'
					ELSE '' END AS "IdMoneda",
					SALDO_DISP + SALDO_RETEN AS "Monto",'INI' AS "IdModulo", 0 AS "IdOperacion" FROM BSCL.TBCTCTCL CTCL	
				INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON POR."CodigoPortafolio" NOT LIKE '%APV%' AND TRIM(RUT_CLI) = POR."DocumentoIdentidad" AND SEC_RUT_CLI = POR."CodigoPortafolio"
				WHERE COD_PER = '2022-01' AND CTA_CLI = 'CCTE' --AND TRIM(RUT_CLI) = '97043000-8'
				UNION ALL
				SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "Monto", "IdModulo", "IdOperacion" FROM #p_MovimientosPorFecha
			) MOV INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON MOV."IdPortafolio" = POR."IdPortafolio"
			INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON CTA."IdPortafolio" = POR."IdPortafolio"
			WHERE CTA."IdCuentaInversion" IN (SELECT "IdCuentaInversion" FROM #p_CuadraturaCuentaInversionIds)
			AND MOV."IdModulo" = 'FAC-SPT' --AND MOV."Monto" < 0
			AND CTA."IdCuentaInversion" NOT IN (
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionDIV
				UNION ALL
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionPAC
				UNION ALL
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionSIM
			)
		);
		
		--CASOS OTROS
		--DROP TABLE #p_IdsCuentaInversionOTROS;
		CREATE LOCAL TEMPORARY TABLE #p_IdsCuentaInversionOTROS AS (
			SELECT DISTINCT CTA."IdCuentaInversion"
			--, POR."NombreCompleto", POR."DocumentoIdentidad", POR."CodigoPortafolio", MOV."Fecha" AS "FechaOperacion", MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda" AS "MonedaOperacion", MOV."Monto" AS "MontoOperacion", MOV."IdModulo", MOV."IdOperacion" 
			FROM (
				SELECT 
					'2022-01-14' AS "Fecha",
					POR."IdCliente", POR."IdPortafolio",
					CASE CTA_CLI 
						WHEN 'CCTE' THEN 'CLP'
						WHEN 'CDOL' THEN 'USD'
						WHEN 'EURO' THEN 'EUR'
					ELSE '' END AS "IdMoneda",
					SALDO_DISP + SALDO_RETEN AS "Monto",'INI' AS "IdModulo", 0 AS "IdOperacion" FROM BSCL.TBCTCTCL CTCL	
				INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON POR."CodigoPortafolio" NOT LIKE '%APV%' AND TRIM(RUT_CLI) = POR."DocumentoIdentidad" AND SEC_RUT_CLI = POR."CodigoPortafolio"
				WHERE COD_PER = '2022-01' AND CTA_CLI = 'CCTE' --AND TRIM(RUT_CLI) = '97043000-8'
				UNION ALL
				SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "Monto", "IdModulo", "IdOperacion" FROM #p_MovimientosPorFecha
			) MOV INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON MOV."IdPortafolio" = POR."IdPortafolio"
			INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON CTA."IdPortafolio" = POR."IdPortafolio"
			WHERE CTA."IdCuentaInversion" IN (SELECT "IdCuentaInversion" FROM #p_CuadraturaCuentaInversionIds)
			--AND MOV."IdModulo" = 'FAC-SPT' --AND MOV."Monto" < 0
			AND CTA."IdCuentaInversion" NOT IN (
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionDIV
				UNION ALL
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionPAC
				UNION ALL
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionSIM
				UNION ALL
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionSPOT
			)
		);
		/*
		SELECT  count(DISTINCT MOV."IdCuentaInversion") AS "Cantidad",'CUENTAS' AS "Descripcion"
		FROM BSCL."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV 
		INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdCuentaInversion" = CTA."IdCuentaInversion"
		WHERE MOV."FechaSistema" > '2022-01-14' AND MOV."IdEstado" = 'CRT'
		UNION ALL
		SELECT  count(DISTINCT MOV."IdCuentaInversion"),'CUENTAS CLP' 
		FROM BSCL."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV 
		INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdCuentaInversion" = CTA."IdCuentaInversion"
		WHERE MOV."FechaSistema" > '2022-01-14' AND MOV."IdEstado" = 'CRT' AND CTA."IdMoneda" IN ('CLP')
		UNION ALL
		SELECT  count(DISTINCT MOV."IdCuentaInversion"),'CUENTAS USD' 
		FROM BSCL."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV 
		INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdCuentaInversion" = CTA."IdCuentaInversion"
		WHERE MOV."FechaSistema" > '2022-01-14' AND MOV."IdEstado" = 'CRT' AND CTA."IdMoneda" IN ('USD')
		UNION ALL
		SELECT  count(DISTINCT MOV."IdCuentaInversion"),'CUENTAS EUR'  
		FROM BSCL."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV 
		INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdCuentaInversion" = CTA."IdCuentaInversion"
		WHERE MOV."FechaSistema" > '2022-01-14' AND MOV."IdEstado" = 'CRT' AND CTA."IdMoneda" IN ('EUR')
		UNION ALL
		SELECT COUNT("IdCuentaInversion"),'TOTAL DIF' FROM #p_CuadraturaCuentaInversionIds
		UNION ALL
		SELECT COUNT("IdCuentaInversion"),'DIF DIV' FROM #p_IdsCuentaInversionDIV
		UNION ALL
		SELECT COUNT("IdCuentaInversion"),'DIF PAC' FROM #p_IdsCuentaInversionPAC
		UNION ALL
		SELECT COUNT("IdCuentaInversion"),'DIF SIM' FROM #p_IdsCuentaInversionSIM
		UNION ALL
		SELECT COUNT("IdCuentaInversion"),'DIF SPOT' FROM #p_IdsCuentaInversionSPOT
		UNION ALL
		SELECT COUNT("IdCuentaInversion"),'DIF OTROS' FROM #p_IdsCuentaInversionOTROS;
		*/
		
		--p_Registros = 
		--SELECT  * FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionConDiferencias";
		DROP TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionConDiferencias";
		CREATE COLUMN TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionConDiferencias" AS (
			--DETALLE DIFERENCIAS MOB CARGO ABONO SALDO CUENTA INVERSION
			SELECT 1 AS "Orden", 'Mov C.A. vs Saldo' AS "Clasificacion", CTA."IdCuentaInversion", POR."IdPortafolio", POR."DocumentoIdentidad", POR."CodigoPortafolio", "NombreCompleto", CTA."IdMoneda", MOV."Saldo" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta", ROUND(MOV."Saldo"-SALDO."Saldo", 0) AS "DiferenciaSaldo", 'Ejecutar recalculo de saldo cuenta inversión' AS "Accion" FROM (
				SELECT MOV."IdCuentaInversion", SUM(CASE MOV."IdTipoMovimiento"
					WHEN 'MOVABN' THEN MOV."Monto"
					WHEN 'MOVCAR' THEN -1 * MOV."Monto"
					END) AS "Saldo"
				FROM BSCL."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV 
				INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdCuentaInversion" = CTA."IdCuentaInversion"
				WHERE MOV."IdEstado" = 'CRT' AND CTA."IdMoneda" = 'CLP'
				GROUP BY MOV."IdCuentaInversion"
			) MOV INNER JOIN (
				SELECT 
				RANK() OVER (PARTITION BY "IdCuentaInversion" ORDER BY "Fecha" DESC)  as RANK, *
				FROM (
					SELECT "FechaSistema" AS "Fecha", "IdCuentaInversion", "IdCliente", "IdPortafolio", "IdMoneda", "SaldoPorFechaMovimiento" AS "Saldo" FROM (
						SELECT RANK() OVER (PARTITION BY MOV."FechaSistema",MOV."IdCuentaInversion" ORDER BY MOV."IdCargoAbonoMovimiento" DESC)  as RANK, 
						MOV."FechaSistema" as "FechaSistema", CTA."IdCuentaInversion", POR."IdCliente", POR."IdPortafolio", CTA."IdMoneda", MOV."SaldoPorFechaMovimiento" as "SaldoPorFechaMovimiento"
						FROM BSCL."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV
						INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdCuentaInversion" = CTA."IdCuentaInversion"
						INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
						WHERE MOV."FechaSistema" > '2022-01-14' AND MOV."IdEstado" = 'CRT' AND CTA."IdMoneda" = 'CLP' --AND POR."IdPortafolio" = 6970
					) WHERE RANK = 1
				)
			) SALDO ON RANK = 1 AND MOV."IdCuentaInversion" = SALDO."IdCuentaInversion"
			INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdCuentaInversion" = CTA."IdCuentaInversion"
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
			WHERE (ROUND(MOV."Saldo"-SALDO."Saldo", 0) > 100 OR ROUND(MOV."Saldo"-SALDO."Saldo", 0) < -100)
			
			UNION ALL
			--DETALLE CASOS DIVIDENDOS
			SELECT 2 AS "Orden", 'DIV' AS "Clasificacion", CTA."IdCuentaInversion", POR."IdPortafolio", POR."DocumentoIdentidad", POR."CodigoPortafolio", "NombreCompleto", CTA."IdMoneda", SALDO."SaldoCalculado", SALDO."SaldoConsulta", ROUND(SALDO."SaldoCalculado" - SALDO."SaldoConsulta", 0) AS "DiferenciaSaldo", 'Realizar inspección del caso' AS "Accion" FROM (
				SELECT SALDO."IdCliente", SALDO."IdPortafolio", SALDO."IdMoneda", MOV."SaldoMov" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta" FROM (
					SELECT S."Fecha", S."IdCliente", S."IdPortafolio", S."IdMoneda", S."Saldo" FROM #p_SaldosPorFecha S
					INNER JOIN (
						SELECT MAX("Fecha") AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda" FROM #p_SaldosPorFecha
						GROUP BY "IdCliente", "IdPortafolio", "IdMoneda"
						) SFinal 
					ON S."Fecha" = SFinal."Fecha" AND S."IdCliente" = SFinal."IdCliente" AND S."IdPortafolio" = SFinal."IdPortafolio" AND S."IdMoneda" = SFinal."IdMoneda"
					) SALDO 
					LEFT JOIN (
						SELECT MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda", SUM(MOV."Monto") AS "SaldoMov" FROM #p_SaldoCuentaInversionCalculado MOV 
						GROUP BY MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda"
					) MOV ON SALDO."IdCliente" = MOV."IdCliente" AND SALDO."IdPortafolio" = MOV."IdPortafolio" AND SALDO."IdMoneda" = MOV."IdMoneda"
				WHERE SALDO."IdMoneda" = 'CLP' 
				--AND SALDO."IdPortafolio" IN (12116,584,185,794)
				AND (ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) > 100 OR ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) < -100) --1142 NOK --427 --332 --293 --199--312--300--235--191
			) SALDO 
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON SALDO."IdPortafolio" = POR."IdPortafolio"
			INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON SALDO."IdMoneda" = CTA."IdMoneda" AND CTA."IdPortafolio" = POR."IdPortafolio"
			WHERE POR."DocumentoIdentidad" <> '96921130-0' --CTA PROPIA
			AND CTA."IdCuentaInversion" IN (
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionDIV
			)
			
			UNION ALL
			--DETALLE CASOS PACTOS
			SELECT 3 AS "Orden", 'PAC' AS "Clasificacion", CTA."IdCuentaInversion", POR."IdPortafolio", POR."DocumentoIdentidad", POR."CodigoPortafolio", "NombreCompleto", CTA."IdMoneda", SALDO."SaldoCalculado", SALDO."SaldoConsulta", ROUND(SALDO."SaldoCalculado" - SALDO."SaldoConsulta", 0) AS "DiferenciaSaldo", 'Realizar inspección del caso' AS "Accion" FROM (
				SELECT SALDO."IdCliente", SALDO."IdPortafolio", SALDO."IdMoneda", MOV."SaldoMov" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta" FROM (
					SELECT S."Fecha", S."IdCliente", S."IdPortafolio", S."IdMoneda", S."Saldo" FROM #p_SaldosPorFecha S
					INNER JOIN (
						SELECT MAX("Fecha") AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda" FROM #p_SaldosPorFecha
						GROUP BY "IdCliente", "IdPortafolio", "IdMoneda"
						) SFinal 
					ON S."Fecha" = SFinal."Fecha" AND S."IdCliente" = SFinal."IdCliente" AND S."IdPortafolio" = SFinal."IdPortafolio" AND S."IdMoneda" = SFinal."IdMoneda"
					) SALDO 
					LEFT JOIN (
						SELECT MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda", SUM(MOV."Monto") AS "SaldoMov" FROM #p_SaldoCuentaInversionCalculado MOV 
						GROUP BY MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda"
					) MOV ON SALDO."IdCliente" = MOV."IdCliente" AND SALDO."IdPortafolio" = MOV."IdPortafolio" AND SALDO."IdMoneda" = MOV."IdMoneda"
				WHERE SALDO."IdMoneda" = 'CLP' 
				--AND SALDO."IdPortafolio" IN (12116,584,185,794)
				AND (ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) > 100 OR ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) < -100) --1142 NOK --427 --332 --293 --199--312--300--235--191
			) SALDO 
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON SALDO."IdPortafolio" = POR."IdPortafolio"
			INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON SALDO."IdMoneda" = CTA."IdMoneda" AND CTA."IdPortafolio" = POR."IdPortafolio"
			WHERE POR."DocumentoIdentidad" <> '96921130-0' --CTA PROPIA
			AND CTA."IdCuentaInversion" IN (
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionPAC
			)	
			UNION ALL
			--DETALLE CASOS SIM
			SELECT 4 AS "Orden", 'SIM' AS "Clasificacion", CTA."IdCuentaInversion", POR."IdPortafolio", POR."DocumentoIdentidad", POR."CodigoPortafolio", "NombreCompleto", CTA."IdMoneda", SALDO."SaldoCalculado", SALDO."SaldoConsulta", ROUND(SALDO."SaldoCalculado" - SALDO."SaldoConsulta", 0) AS "DiferenciaSaldo", 'Realizar inspección del caso' AS "Accion" FROM (
				SELECT SALDO."IdCliente", SALDO."IdPortafolio", SALDO."IdMoneda", MOV."SaldoMov" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta" FROM (
					SELECT S."Fecha", S."IdCliente", S."IdPortafolio", S."IdMoneda", S."Saldo" FROM #p_SaldosPorFecha S
					INNER JOIN (
						SELECT MAX("Fecha") AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda" FROM #p_SaldosPorFecha
						GROUP BY "IdCliente", "IdPortafolio", "IdMoneda"
						) SFinal 
					ON S."Fecha" = SFinal."Fecha" AND S."IdCliente" = SFinal."IdCliente" AND S."IdPortafolio" = SFinal."IdPortafolio" AND S."IdMoneda" = SFinal."IdMoneda"
					) SALDO 
					LEFT JOIN (
						SELECT MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda", SUM(MOV."Monto") AS "SaldoMov" FROM #p_SaldoCuentaInversionCalculado MOV 
						GROUP BY MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda"
					) MOV ON SALDO."IdCliente" = MOV."IdCliente" AND SALDO."IdPortafolio" = MOV."IdPortafolio" AND SALDO."IdMoneda" = MOV."IdMoneda"
				WHERE SALDO."IdMoneda" = 'CLP' 
				--AND SALDO."IdPortafolio" IN (12116,584,185,794)
				AND (ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) > 100 OR ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) < -100) --1142 NOK --427 --332 --293 --199--312--300--235--191
			) SALDO 
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON SALDO."IdPortafolio" = POR."IdPortafolio"
			INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON SALDO."IdMoneda" = CTA."IdMoneda" AND CTA."IdPortafolio" = POR."IdPortafolio"
			WHERE POR."DocumentoIdentidad" <> '96921130-0' --CTA PROPIA
			AND CTA."IdCuentaInversion" IN (
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionSIM
			)	
			UNION ALL
			--DETALLE CASOS SPOT
			SELECT 5 AS "Orden", 'SPT' AS "Clasificacion", CTA."IdCuentaInversion", POR."IdPortafolio", POR."DocumentoIdentidad", POR."CodigoPortafolio", "NombreCompleto", CTA."IdMoneda", SALDO."SaldoCalculado", SALDO."SaldoConsulta", ROUND(SALDO."SaldoCalculado" - SALDO."SaldoConsulta", 0) AS "DiferenciaSaldo", 'Realizar inspección del caso' AS "Accion" FROM (
				SELECT SALDO."IdCliente", SALDO."IdPortafolio", SALDO."IdMoneda", MOV."SaldoMov" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta" FROM (
					SELECT S."Fecha", S."IdCliente", S."IdPortafolio", S."IdMoneda", S."Saldo" FROM #p_SaldosPorFecha S
					INNER JOIN (
						SELECT MAX("Fecha") AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda" FROM #p_SaldosPorFecha
						GROUP BY "IdCliente", "IdPortafolio", "IdMoneda"
						) SFinal 
					ON S."Fecha" = SFinal."Fecha" AND S."IdCliente" = SFinal."IdCliente" AND S."IdPortafolio" = SFinal."IdPortafolio" AND S."IdMoneda" = SFinal."IdMoneda"
					) SALDO 
					LEFT JOIN (
						SELECT MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda", SUM(MOV."Monto") AS "SaldoMov" FROM #p_SaldoCuentaInversionCalculado MOV 
						GROUP BY MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda"
					) MOV ON SALDO."IdCliente" = MOV."IdCliente" AND SALDO."IdPortafolio" = MOV."IdPortafolio" AND SALDO."IdMoneda" = MOV."IdMoneda"
				WHERE SALDO."IdMoneda" = 'CLP' 
				--AND SALDO."IdPortafolio" IN (12116,584,185,794)
				AND (ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) > 100 OR ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) < -100) --1142 NOK --427 --332 --293 --199--312--300--235--191
			) SALDO 
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON SALDO."IdPortafolio" = POR."IdPortafolio"
			INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON SALDO."IdMoneda" = CTA."IdMoneda" AND CTA."IdPortafolio" = POR."IdPortafolio"
			WHERE POR."DocumentoIdentidad" <> '96921130-0' --CTA PROPIA
			AND CTA."IdCuentaInversion" IN (
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionSPOT
			)	
			UNION ALL
			--DETALLE CASOS OTROS
			SELECT 6 AS "Orden", 'OTR' AS "Clasificacion", CTA."IdCuentaInversion", POR."IdPortafolio", POR."DocumentoIdentidad", POR."CodigoPortafolio", "NombreCompleto", CTA."IdMoneda", SALDO."SaldoCalculado", SALDO."SaldoConsulta", ROUND(SALDO."SaldoCalculado" - SALDO."SaldoConsulta", 0) AS "DiferenciaSaldo", 'Realizar inspección del caso' AS "Accion" FROM (
				SELECT SALDO."IdCliente", SALDO."IdPortafolio", SALDO."IdMoneda", MOV."SaldoMov" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta" FROM (
					SELECT S."Fecha", S."IdCliente", S."IdPortafolio", S."IdMoneda", S."Saldo" FROM #p_SaldosPorFecha S
					INNER JOIN (
						SELECT MAX("Fecha") AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda" FROM #p_SaldosPorFecha
						GROUP BY "IdCliente", "IdPortafolio", "IdMoneda"
						) SFinal 
					ON S."Fecha" = SFinal."Fecha" AND S."IdCliente" = SFinal."IdCliente" AND S."IdPortafolio" = SFinal."IdPortafolio" AND S."IdMoneda" = SFinal."IdMoneda"
					) SALDO 
					LEFT JOIN (
						SELECT MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda", SUM(MOV."Monto") AS "SaldoMov" FROM #p_SaldoCuentaInversionCalculado MOV 
						GROUP BY MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda"
					) MOV ON SALDO."IdCliente" = MOV."IdCliente" AND SALDO."IdPortafolio" = MOV."IdPortafolio" AND SALDO."IdMoneda" = MOV."IdMoneda"
				WHERE SALDO."IdMoneda" = 'CLP' 
				--AND SALDO."IdPortafolio" IN (12116,584,185,794)
				AND (ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) > 100 OR ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) < -100) --1142 NOK --427 --332 --293 --199--312--300--235--191
			) SALDO 
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON SALDO."IdPortafolio" = POR."IdPortafolio"
			INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON SALDO."IdMoneda" = CTA."IdMoneda" AND CTA."IdPortafolio" = POR."IdPortafolio"
			WHERE POR."DocumentoIdentidad" <> '96921130-0' --CTA PROPIA
			AND CTA."IdCuentaInversion" IN (
				SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionOTROS
			)
		);
		
		----------------------TOTAL OPERACION  + SALDO INICIAL----------------------
		--SELECT * FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionOperacionesSaldoCalculado";
		DROP TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionOperacionesSaldoCalculado";
		CREATE COLUMN TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionOperacionesSaldoCalculado" AS (
			SELECT POR."NombreCompleto", POR."DocumentoIdentidad", POR."CodigoPortafolio", CTA."Fecha" AS "FechaOperacion", CTA."IdCliente", CTA."IdPortafolio", CTA."IdMoneda" AS "MonedaOperacion", CTA."Monto" AS "MontoOperacion", CTA."IdModulo", CTA."IdOperacion" FROM (
				SELECT 
					'2022-01-14' AS "Fecha",
					POR."IdCliente", POR."IdPortafolio",
					CASE CTA_CLI 
						WHEN 'CCTE' THEN 'CLP'
						WHEN 'CDOL' THEN 'USD'
						WHEN 'EURO' THEN 'EUR'
					ELSE '' END AS "IdMoneda",
					SALDO_DISP + SALDO_RETEN AS "Monto",'INI' AS "IdModulo", 0 AS "IdOperacion" FROM BSCL.TBCTCTCL CTCL	
				INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON POR."CodigoPortafolio" NOT LIKE '%APV%' AND TRIM(RUT_CLI) = POR."DocumentoIdentidad" AND SEC_RUT_CLI = POR."CodigoPortafolio"
				WHERE COD_PER = '2022-01' AND CTA_CLI = 'CCTE' --AND TRIM(RUT_CLI) = '97043000-8'
				UNION ALL
				SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "Monto", "IdModulo", "IdOperacion" FROM #p_MovimientosPorFecha
			) CTA INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
			ORDER BY CTA."IdPortafolio", CTA."Fecha"
		);
		
		--------------DESPLIEGUE INFO ALERTA RESUMIDA----------------
		SELECT "Clasificacion","IdCuentaInversion","DocumentoIdentidad","CodigoPortafolio",LEFT("NombreCompleto",25) AS "NombreCompleto","IdMoneda", CAST(ROUND("SaldoCalculado",0) AS BIGINT) AS "SaldoCalculado",CAST(ROUND("SaldoConsulta",0) AS BIGINT) AS "SaldoConsulta", CAST(ROUND("DiferenciaSaldo",0) AS BIGINT) AS "Diferencia", "Accion" FROM (
			SELECT "Orden", "Clasificacion", "IdCuentaInversion", "IdPortafolio", "DocumentoIdentidad", "CodigoPortafolio", "NombreCompleto", "IdMoneda", "SaldoCalculado", "SaldoConsulta", "DiferenciaSaldo", "Accion"
			FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionConDiferencias"
			WHERE "Orden" = 1
			UNION ALL
			SELECT TOP 3 "Orden", "Clasificacion", "IdCuentaInversion", "IdPortafolio", "DocumentoIdentidad", "CodigoPortafolio", "NombreCompleto", "IdMoneda", "SaldoCalculado", "SaldoConsulta", "DiferenciaSaldo", "Accion"
			FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionConDiferencias"
			WHERE "Orden" = 2
			UNION ALL
			SELECT TOP 3 "Orden", "Clasificacion", "IdCuentaInversion", "IdPortafolio", "DocumentoIdentidad", "CodigoPortafolio", "NombreCompleto", "IdMoneda", "SaldoCalculado", "SaldoConsulta", "DiferenciaSaldo", "Accion"
			FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionConDiferencias"
			WHERE "Orden" = 3
			UNION ALL
			SELECT TOP 3 "Orden", "Clasificacion", "IdCuentaInversion", "IdPortafolio", "DocumentoIdentidad", "CodigoPortafolio", "NombreCompleto", "IdMoneda", "SaldoCalculado", "SaldoConsulta", "DiferenciaSaldo", "Accion"
			FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionConDiferencias"
			WHERE "Orden" = 4
			UNION ALL
			SELECT TOP 3 "Orden", "Clasificacion", "IdCuentaInversion", "IdPortafolio", "DocumentoIdentidad", "CodigoPortafolio", "NombreCompleto", "IdMoneda", "SaldoCalculado", "SaldoConsulta", "DiferenciaSaldo", "Accion"
			FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionConDiferencias"
			WHERE "Orden" = 5
			UNION ALL
			SELECT TOP 3 "Orden", "Clasificacion", "IdCuentaInversion", "IdPortafolio", "DocumentoIdentidad", "CodigoPortafolio", "NombreCompleto", "IdMoneda", "SaldoCalculado", "SaldoConsulta", "DiferenciaSaldo", "Accion"
			FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionConDiferencias"
			WHERE "Orden" = 6
		)
		ORDER BY "Orden", "IdCuentaInversion";

	END;	
END;

