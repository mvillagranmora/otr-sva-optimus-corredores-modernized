DROP TABLE #p_MovimientosPorFecha;

CREATE LOCAL TEMPORARY TABLE #p_MovimientosPorFecha AS (
	SELECT FAC."FechaSistema" AS "Fecha", POR."IdCliente", POR."IdPortafolio", 
	CASE  
		WHEN FAC."IdTipoOperacion" IN ('COMSPT','VENSPT','ANVSPT','ANCSPT') THEN FAC."IdMonedaSpot"
		ELSE FAC."IdMoneda" 
	END AS "IdMoneda",
	CASE 
		WHEN FAC."IdTipoInstrumento" = 'CFI' AND FAC."IdMonedaLiquidacion" IN ('USD','CLP') THEN 
			CASE 
				WHEN FAC."IdTipoOperacion" IN ('ANCCFI','VENCFI') THEN 1 * ABS(FAC."Monto")
				WHEN FAC."IdTipoOperacion" IN ('ANVCFI','COMCFI') THEN -1 * ABS(FAC."Monto")
				ELSE  ABS(FAC."Monto")
			END
		--WHEN FAC."IdTipoInstrumento" = 'PAG' AND FAC."IdMoneda" = 'USD' THEN 0 
		WHEN FAC."IdTipoOperacion" = 'VENIIF' THEN  ABS(FAC."Monto")
		ELSE
			CASE FAC."IdTipoOperacion"
				WHEN 'VENSPT' THEN   ABS(FAC."Cantidad")
				WHEN 'COMSPT' THEN -1*ABS(FAC."Cantidad")
				WHEN 'COMACN' THEN -1 * ABS(FAC."Cantidad")
				WHEN 'VENACN' THEN ABS(FAC."Cantidad")
				ELSE 
					CASE 
						WHEN FAC."IdConcepto" = 'COMPUT' THEN -1 * FAC."MontoComprobanteFinal" 
						WHEN FAC."IdTipoOperacion" LIKE 'AN%' THEN 
							CASE 
								WHEN FAC."IdTipoOperacion" IN ('ANCSPT') THEN 1 * ABS(FAC."Cantidad")
								WHEN FAC."IdTipoOperacion" IN ('ANVSPT') THEN -1 * ABS(FAC."Cantidad")
								ELSE -1 * FAC."MontoComprobanteFinal"
							END
						ELSE FAC."MontoComprobanteFinal" 
					END
			END 
	END AS "Monto", 'FAC' AS "IdModulo"
	--, FAC."IdTipoInstrumento", FAC."IdMoneda", FAC."MontoComprobanteFinal", * 
	FROM BSCL."BSCL.Facturacion.BD::FAC.Comprobante" FAC 
	INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON FAC."IdPortafolio" = CTA."IdPortafolio" AND CTA."IdMoneda" = FAC."IdMoneda"
	INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
	WHERE FAC."FechaLiquidacion" > '2022-01-14' AND FAC."FechaSistema" > '2022-01-14' AND 
	FAC."IdEstadoComprobante" IN ('ENV','FAC') AND (FAC."IdMoneda" = 'USD'OR FAC."IdMonedaSpot"='USD') AND FAC."IdTipoOperacion" NOT IN ('COMSIM','VENSIM','ANSIMV','ANSIMC') --AND POR."IdPortafolio" = 5002 ORDER BY 1
	--AND CTA."IdCuentaInversion" = 10652 ORDER BY 1
		
	UNION ALL
	
	SELECT FAC."FechaLiquidacion" AS "Fecha", POR."IdCliente", POR."IdPortafolio", 
	FAC."IdMonedaComprobante" AS "IdMoneda",
	CASE FAC."IdTipoOperacion"
		WHEN 'COMSIM' THEN -1 * ABS(FAC."MontoComprobanteFinal")
		WHEN 'VENSIM' THEN ABS(FAC."MontoComprobanteFinal")
		ELSE FAC."MontoComprobanteFinal"
	END AS "Monto", 'SIM' AS "IdModulo"
	--, FAC."MontoComprobanteFinal", FAC."FechaComprobante", * 
	FROM BSCL."BSCL.Facturacion.BD::FAC.Comprobante" FAC 
	INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON FAC."IdPortafolio" = CTA."IdPortafolio" AND CTA."IdMoneda" = FAC."IdMonedaComprobante"
	INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
	WHERE FAC."FechaLiquidacion" > '2022-01-14' AND FAC."FechaComprobante" > '2022-01-14' AND 
	FAC."IdEstadoComprobante" IN ('ENV','FAC') AND FAC."IdMonedaComprobante" = 'USD' AND FAC."IdTipoOperacion" IN ('COMSIM','VENSIM') 
	AND FAC."FechaLiquidacion" <= (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema')
	--AND POR."IdPortafolio" = 7812 ORDER BY 1

	UNION ALL
	
	SELECT TES."FechaMovimiento" AS "Fecha", POR."IdCliente", POR."IdPortafolio", TES."IdMoneda",
	CASE TES."IdTipoMovimiento"
		WHEN 'MOVEGR' THEN -1*TES."Monto"
		WHEN 'MOVING' THEN TES."Monto" 
		ELSE TES."Monto"
	END AS "Monto", 'TES' AS "IdModulo" FROM BSCL."BSCL.Tesoreria.BD::TES.Comprobante" TES
	INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON TES."IdPortafolioTitularPago" = POR."IdPortafolio"
	WHERE TES."IdEstado" = 'LIQ' AND "FechaMovimiento" > '2022-01-14' AND "IdMoneda" = 'USD' --AND "IdPortafolioTitularPago" = 5541 ORDER BY 1
	
	UNION ALL
	
	SELECT FM."FechaCertificacion" AS "Fecha", POR."IdCliente", POR."IdPortafolio", FM."IdMoneda",
	CASE FM."TipoSolicitud"
		WHEN 'APO' THEN -1*FM."Monto"
		WHEN 'RES' THEN FM."Monto" 
		ELSE FM."Monto"
	END AS "Monto", 'FM' AS "IdModulo" 
	FROM BSCL."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" FM  
	INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON FM."IdPortafolio" = POR."IdPortafolio"
	WHERE FM."Estado" = 'CRT' AND FM."FechaCertificacion" > '2022-01-14' 
	AND "IdMoneda" = 'USD' AND "IdTipoMovimientoFondoMutuo" NOT IN ('INGTIT','RETTIT','RESTRASP') --AND POR."IdPortafolio" = 3334 ORDER BY 1
	
	UNION ALL
	
	SELECT EVEC."FechaPago" AS "Fecha", POR."IdCliente", POR."IdPortafolio", CASE WHEN EVEC."IdTipoEventoCapital" = 'CC' THEN EVEC."IdMoneda" ELSE EVE."IdMonedaLiquidacion" END AS "IdMoneda",--EVE."IdMoneda",
	EVEC."Total" AS "Monto", EVEC."IdTipoEventoCapital" AS "IdModulo" FROM BSCL."BSCL.Custodia.BD::CUS.EventoCapitalCliente" EVEC
	INNER JOIN BSCL."BSCL.Custodia.BD::CUS.EventoCapital" EVE ON EVE."IdEventoCapital" = EVEC."IdEventoCapital"
	INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON EVEC."IdPortafolio" = POR."IdPortafolio"
	WHERE EVEC."IdEstado" = 'CER' /*AND EVEC."TipoSocioNegocio" NOT IN ('CNT') AND EVEC."Total" >= 0*/ /*AND EVE."Origen" NOT IN ('RUE')*/ 
	AND (EVE."IdMonedaLiquidacion" = 'USD' OR (EVEC."IdMoneda" = 'USD' AND EVEC."IdTipoEventoCapital" = 'CC' AND EVEC."Total" >= 0)) --AND EVEC."IdPortafolio" =  5002 ORDER BY 1
	--EVEC."DocumentoIdentidad" = '76184068-1'
	
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
	WHEN 'VENTER' THEN -1 * MES."MontoOperacionFinalMoneda"
		WHEN 'COMTER' THEN ABS(MES."MontoOperacionFinalMoneda") 
		WHEN 'VENPAC' THEN -1 * MES."MontoOperacion"
		WHEN 'COMPAC' THEN MES."MontoOperacion"
		ELSE -1*ABS(MES."MontoOperacionMoneda")
	END AS "Monto", 'MES' AS "IdModulo" FROM BSCL."BSCL.Mesa.BD::MES.Operacion" MES 
	INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON MES."IdPortafolio" = POR."IdPortafolio"
	WHERE (MES."IdEstado" IN ('VIG','CERT') OR (MES."IdEstado" = 'LIQ' AND MES."IdTipoOperacion" IN ('VENPAC','COMPAC') )) AND MES."IdMonedaPrincipal" = 'USD' AND MES."IdMonedaSecundaria" = 'USD' --AND MES."IdPortafolio" = 4627 ORDER BY 1

	--SELECT * FROM BSCL."BSCL.Mesa.BD::MES.Operacion" WHERE "IdOperacion" = 116096
	--SELECT "FechaVencimiento", "IdEstado", "MontoOperacion", * FROM BSCL."BSCL.Mesa.BD::MES.Operacion" WHERE "IdPortafolio" = 4627 ORDER BY 1
	
	UNION ALL
	
	SELECT MES."FechaVencimiento" AS "Fecha",POR."IdCliente", POR."IdPortafolio", MES."IdMonedaPrincipal" AS "IdMoneda",
	CASE MES."IdTipoOperacion"
		WHEN 'VENPAC' THEN MES."MontoOperacionFinal"
		WHEN 'COMPAC' THEN -1 * MES."MontoOperacionFinal"
		ELSE MES."MontoOperacionFinal"
	END AS "Monto", 'VPC' AS "IdModulo" FROM BSCL."BSCL.Mesa.BD::MES.Operacion" MES 
	INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON MES."IdPortafolio" = POR."IdPortafolio"
	WHERE MES."IdEstado" = 'LIQ' AND MES."IdTipoOperacion" IN ('VENPAC','COMPAC') AND "IdMonedaPrincipal" = 'USD'   --AND MES."IdPortafolio" = 517 ORDER BY 1

	
	UNION ALL
	
	SELECT CAR."EGRFechaOperacion" AS "Fecha", CAR."IdCliente", CAR."IdPortafolio", CAR."IdMonedaSistema" AS "IdMoneda",
	CASE CAR."IdTipoOperacion"
		WHEN 'VENSIM' THEN -1 * CAR."INGMontoAPlazo"
		WHEN 'COMSIM' THEN CAR."INGMontoAPlazo" 
		ELSE CAR."INGMontoAPlazo"
	END AS "Monto", 'VSM' AS "IdModulo"--, "EGRFechaLiquidacion", "FechaLiquidacion", "INGFechaLiquidacion", "MontoAPlazo", "INGMontoAPlazo", "EGRMontoAPlazo", *
	FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR WHERE "EGRFechaOperacion" > '2022-01-14' AND "IdTipoInstrumento" IN ('SIM') AND "IdEstado" = 'VCD'
	AND CAR."IdMonedaSistema" = 'USD'  
	--AND "FolioCartera" NOT IN (SELECT CAR."FolioCartera" FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR  WHERE "EGRFechaOperacion" > '2022-01-14' AND "IdTipoInstrumento" IN ('SIM') AND "IdEstado" = 'ANT')
	--AND "IdPortafolio" = 1721 ORDER BY 1

	UNION ALL
	
	SELECT 
	CASE WHEN "EGRIdTipoOperacion" LIKE 'ANT%' 
		THEN CAR."EGRFechaOperacion"
		ELSE CAR."EGRFechaLiquidacion"
	END AS "Fecha", 
	CAR."IdCliente", CAR."IdPortafolio", CAR."IdMonedaSistema" AS "IdMoneda",
	-1 * CAR."EGRMontoLiquidacion" AS "Monto", 'VFW' AS "IdModulo"
	FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR WHERE "EGRFechaLiquidacion" > '2022-01-14' AND "IdTipoInstrumento" IN ('FWD') AND "IdEstado" IN ('VCD','LIQ') AND "EGRIdTipoOperacion" NOT IN('ANUFWD') 
	AND CAR."IdMonedaSistema" = 'USD' --AND "IdPortafolio" = 4950 ORDER BY 1
	
	/*
	SELECT 
	CASE WHEN "EGRIdTipoOperacion" LIKE 'ANT%' 
		THEN CAR."EGRFechaOperacion"
		ELSE CAR."EGRFechaLiquidacion"
	END AS "Fecha", 
	CAR."IdCliente", CAR."IdPortafolio", CAR."IdMonedaSistema" AS "IdMoneda",
	-1 * CAR."EGRMontoLiquidacion" AS "Monto", 'VFW' AS "IdModulo", "EGRIdTipoOperacion", "EGRFechaOperacion", "EGRFechaLiquidacion", *
	FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR WHERE  "IdPortafolio" = 4950 ORDER BY 9
	
	SELECT 
	"IdPortafolio",
	CASE WHEN "EGRIdTipoOperacion" LIKE 'ANT%' 
		THEN CAR."EGRFechaOperacion"
		ELSE CAR."EGRFechaLiquidacion"
	END AS "Fecha", 
	CAR."IdCliente", CAR."IdPortafolio", CAR."IdMonedaSistema" AS "IdMoneda",
	-1 * CAR."EGRMontoLiquidacion" AS "Monto", 'VFW' AS "IdModulo", "EGRIdTipoOperacion", "EGRFechaOperacion", "EGRFechaLiquidacion", * FROM "BSCL.Custodia.BD::CUS.Cartera" CAR WHERE "FolioCartera" = 81956
	*/
	UNION ALL
	
	
	SELECT 
	"EGRFechaOperacion" AS "Fecha", CAR."IdCliente", CAR."IdPortafolio", CAR."IdTipoReajuste" AS "IdMoneda",
	--"INGMontoATasaCompraActual" AS "Monto", 
	"EGRCantidad" * "EGRValorRescate" AS "Monto",
	'VIF' AS "IdModulo"
	FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR WHERE "EGRFechaOperacion" > '2022-01-14' AND "IdTipoInstrumento" IN ('PAG') AND "IdEstado" = 'VCD' 
	AND "IdTipoReajuste" = 'USD' --AND "IdPortafolio" = 4743--517 -- 4743  ORDER BY 1
	
	UNION ALL

	SELECT 
	"FechaVencimientoInstrumento" AS "Fecha", CAR."IdCliente", CAR."IdPortafolio", CAR."IdMonedaSistema" AS "IdMoneda",
	"INGCantidad" * "INGValorRescate" AS "Monto",
	'VIF' AS "IdModulo"--, "EGRFechaOperacion", "FechaVencimientoInstrumento", "IdEstado", "EGRIdTipoOperacion", "IdTipoReajuste", "IdTipoProducto", *
	FROM (
		SELECT ROW_NUMBER() OVER (PARTITION BY "FolioCartera" ORDER BY "IdCartera" DESC) AS nn,* FROM BSCL."BSCL.Custodia.BD::CUS.Cartera"
		WHERE "IdTipoInstrumento" = 'PAG'
	) CAR WHERE nn = 1 AND "FechaVencimientoInstrumento" > '2022-01-14' AND "EGRFechaOperacion" <> "FechaVencimientoInstrumento" AND "IdEstado" = 'VCD' 
	AND "EGRIdTipoOperacion" != 'VCDPAC' AND "IdTipoReajuste" = 'USD' /*AND "IdTipoProducto" = 'MERF'*/ --AND "IdPortafolio" = 5631 ORDER BY "IdCartera"
	
	UNION ALL
	
	SELECT CAR."EGRFechaOperacion" AS "Fecha", CAR."IdCliente", CAR."IdPortafolio", CAR."IdMonedaSistema" AS "IdMoneda", 
	CASE CAR."IdTipoOperacion"
		WHEN 'VENSIM' THEN -1 * CAR."EGRMontoARecibirAnticipo"
		WHEN 'COMSIM' THEN CAR."EGRMontoARecibirAnticipo" 
		ELSE CAR."EGRMontoARecibirAnticipo"
	END AS "Monto", 'ASM' AS "IdModulo"
	FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR  WHERE "EGRFechaOperacion" > '2022-01-14' AND "IdTipoInstrumento" IN ('SIM') AND "IdEstado" = 'ANT' --AND "IdPortafolio" = 7812 ORDER BY 1
	
	UNION ALL
	
	SELECT TRA."FechaOperacion" AS "Fecha", TRA."IdCliente", TRA."IdPortafolio", TRA."IdMonedaSistema" AS "IdMoneda",  
	CASE TRA."IdTipoOperacion"
		WHEN 'INGGTA' THEN -1 * TRA."MontoCompraMonedaInstrumento"
		WHEN 'RETGTA' THEN TRA."MontoCompraMonedaInstrumento" 
		ELSE TRA."MontoCompraMonedaInstrumento"
	END AS "Monto", 'GTA' AS "IdModulo"
	FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRA WHERE "IdOrigenGarantia" = 'CTA' AND "IdEstado" = 'CRT' --AND "IdPortafolio" = 6074 ORDER BY 1
	UNION ALL

	SELECT AJU."FechaDetectado" AS "Fecha", POR."IdCliente", POR."IdPortafolio", CTA."IdMoneda", AJU."MontoAjuste" AS "Monto", 'AJU' AS "IdModulo"
	FROM "BSCL.Plataforma.BD::CUA.MovimientosCuentaInversionAjusteOperaciones" AJU
	INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON AJU."IdCuentaInversion" = CTA."IdCuentaInversion"
	INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
);
	
DROP TABLE #p_SaldoCuentaInversionCalculado;
CREATE LOCAL TEMPORARY TABLE #p_SaldoCuentaInversionCalculado AS (
	SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", SUM("Monto") AS "Monto" FROM (
		SELECT 
			'2022-01-14' AS "Fecha",
			POR."IdCliente", POR."IdPortafolio",
			CASE CTA_CLI 
				WHEN 'CCTE' THEN 'USD'
				WHEN 'CDOL' THEN 'USD'
				WHEN 'EURO' THEN 'EUR'
			ELSE '' END AS "IdMoneda",
			SALDO_DISP + SALDO_RETEN AS "Monto",'INI' AS "IdModulo" FROM BSCL.TBCTCTCL CTCL	
		INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON POR."CodigoPortafolio" NOT LIKE '%APV%' AND TRIM(RUT_CLI) = POR."DocumentoIdentidad" AND SEC_RUT_CLI = POR."CodigoPortafolio"
		WHERE COD_PER = '2022-01' AND CTA_CLI = 'CDOL' --AND TRIM(RUT_CLI) = '97043000-8'
		UNION ALL
		SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", SUM("Monto") AS "Monto", "IdModulo" FROM 
			#p_MovimientosPorFecha
		GROUP BY "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "IdModulo"
	) 
	GROUP BY "Fecha", "IdCliente", "IdPortafolio", "IdMoneda"
	ORDER BY 1
);

DROP TABLE #p_SaldosPorFecha;
CREATE LOCAL TEMPORARY TABLE #p_SaldosPorFecha AS (
	SELECT "FechaSistema" AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "SaldoPorFechaMovimiento" AS "Saldo" FROM (
		SELECT RANK() OVER (PARTITION BY MOV."FechaSistema",MOV."IdCuentaInversion" ORDER BY MOV."IdCargoAbonoMovimiento" DESC)  as RANK, 
		MOV."FechaSistema", POR."IdCliente", POR."IdPortafolio", CTA."IdMoneda", MOV."SaldoPorFechaMovimiento"
		FROM BSCL."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV
		INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdCuentaInversion" = CTA."IdCuentaInversion"
		INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
		WHERE MOV."FechaSistema" > '2022-01-14' AND MOV."IdEstado" = 'CRT' AND CTA."IdMoneda" = 'USD' --AND POR."IdPortafolio" = 6970
	) WHERE RANK = 1
);



--CUADRE
SELECT CTA."IdCuentaInversion", POR."IdPortafolio", POR."DocumentoIdentidad", POR."CodigoPortafolio", CTA."IdMoneda", SALDO."SaldoCalculado", SALDO."SaldoConsulta", ROUND(SALDO."SaldoCalculado" - SALDO."SaldoConsulta", 0) AS "DiferenciaSaldo" FROM (
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
	WHERE SALDO."IdMoneda" = 'USD' 
	--AND SALDO."IdPortafolio" = 7336
	--AND SALDO."IdPortafolio" NOT IN (7036,2795,4517,517,4624,4627,4950,1991,5132,6860,4311,5797,4688,7360,4080,1339,7493,7116,3513,1860,3567,2911,4546,950,6164,6165,1714,2255,3695,4906,3077,39,4394,2225,2258,1319,3075,4558,5362,4743,3552,6104,5363,5364,3866,1760,2547,1144,327,5631,5860,4486,3160,4918,764,56,744,1701,1721,4741,7812,3253,4931,3575,5334,3304,7247,6353,5168,5576,6359,1127,623,270,1563,684,1894,5327,3747,4521,2033,5193,1338,1308,2065,7409,6844,6686,7613,4275,4632,4660,7581,1494,5047,5085,7558,4591,4641,5768,7757,5346,2260,3132,4551,1639,5565,5056,1925,7417,7311,373,230,828,6742,5648,68,4358,6512,4087,2683,5940,6007,5826,6100,6109,5463,1178,1180,4937,2555,2072,1916,2191,3445,3446,2456,4160,1299,5726,7159,6057,2660,3656,4383,215,6105,7734,3168,5967,5541,1219,3156,391,2011,2373,2375,3926,2480,4127,4007,1789,955,2270,2101,1706,3732,3840,6068,6797,2171,2774,3107,3,4967,4186,1674,3419,3473,3610,3611,3793,670,2653,4767,5824,4000,5002,4173,4131,6752,6697,2780,7062,1524,537,991,5303,7852,7855,7894,7901,7903,7915,7961,8015,8050)
	--AND SALDO."IdPortafolio" IN (6970,753,1506,6846,7226,5541,6316,453)
	--AND SALDO."IdPortafolio" = 5541
	AND ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) NOT BETWEEN -1 AND 1--1142 NOK --427 --332 --293 --199--312--300--235--191
	--AND SALDO."Saldo" = MOV."SaldoMov"--323 OK --1731 --2049 --2129
) SALDO 
INNER JOIN "BSCL.Portafolio.BD.Vistas.UI	::ConsultarClientePortafolio"	 POR ON SALDO."IdPortafolio" = POR."IdPortafolio"
INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON SALDO."IdMoneda" = CTA."IdMoneda" AND CTA."IdPortafolio" = POR."IdPortafolio" 