SET SCHEMA BSCL;

------------------------AJUSTES CASOS EXPLICADOS----------------------------
/*
--SELECT * FROM "BSCL.Plataforma.BD::CUA.MovimientosCuentaInversionAjusteOperaciones"
--DROP TABLE "BSCL.Plataforma.BD::CUA.MovimientosCuentaInversionAjusteOperaciones";
CREATE COLUMN TABLE "BSCL.Plataforma.BD::CUA.MovimientosCuentaInversionAjusteOperaciones" (
	"IdCuentaInversion" BIGINT,
	"FechaDetectado" DATE,
	"MontoAjuste" DECIMAL(26,8) DEFAULT 0,
	"Modulo" NVARCHAR(10) DEFAULT '',
	"Comentario" NVARCHAR(5000) DEFAULT ''
);

--CASO EXPLICADO
INSERT INTO "BSCL.Plataforma.BD::CUA.MovimientosCuentaInversionAjusteOperaciones"
SELECT 2531,'2022-05-11',8000000,'VENC IF','Problema en saldo inicial. Caso explicado por ticket  Caso ticket 13058' FROM DUMMY;

--SIM
INSERT INTO "BSCL.Plataforma.BD::CUA.MovimientosCuentaInversionAjusteOperaciones"
SELECT 2864,'2022-05-11',-416938,'SIM','Vencimiento parcial no regularizado en operación' FROM DUMMY UNION ALL
SELECT 3359,'2022-05-11',-3462520,'SIM','Vencimiento parcial no regularizado en operación' FROM DUMMY UNION ALL
SELECT 4550,'2022-05-11',-141,'SIM','Diferencias menores anticipos' FROM DUMMY UNION ALL
SELECT 6659,'2022-05-11',-6179,'SIM','Falta monto cargo regularización en operaciones' FROM DUMMY UNION ALL
SELECT 9656,'2022-05-11',59913352,'SIM','Monto operación en cero' FROM DUMMY UNION ALL
SELECT 10190,'2022-05-11',-150534157,'SIM','Falta monto anticipo simultánea en operaciones' FROM DUMMY UNION ALL
--SELECT 10385,'2022-05-11',-31346,'SIM','Diferencia por anticipo de préstamos' FROM DUMMY UNION ALL
SELECT 12116,'2022-05-11',-91057,'SIM','Vencimiento no regularizado en operaciones' FROM DUMMY UNION ALL
SELECT 12416,'2022-05-11',13899517,'SIM','Falta monto anticipo en operaciones' FROM DUMMY UNION ALL
SELECT 14507,'2022-05-11',3462521,'SIM','Vencimiento no regularizado en operaciones' FROM DUMMY UNION ALL
--SELECT 10385,'2022-05-12',31351,'SIM','Diferencia por anticipo de préstamos' FROM DUMMY UNION ALL
SELECT 13484,'2022-05-13',28344066,'SIM','Diferencia por operación mal registrada por error en Optimus ya corregido' FROM DUMMY UNION ALL
SELECT 13484,'2022-05-13',-33689,'SIM','Diferencia por regularización en cta inversión producto de error en un CFI ya corregido (CFI-OREIAE del 08-04-2022)' FROM DUMMY;

--SPOT

INSERT INTO "BSCL.Plataforma.BD::CUA.MovimientosCuentaInversionAjusteOperaciones"
SELECT 7685,'2022-05-11',1065709,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 7685,'2022-05-11',506760,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 1580,'2022-05-11',276000,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 7694,'2022-05-11',337600,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 7694,'2022-05-11',710472,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 1271,'2022-05-11',-187000,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 1271,'2022-05-11',-276000,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 3611,'2022-05-11',710472,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 3611,'2022-05-11',337600,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 12665,'2022-05-11',-243360,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 3371,'2022-05-11',337600,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 3371,'2022-05-11',710472,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 3716,'2022-05-11',337600,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 3716,'2022-05-11',710472,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 1160,'2022-05-11',-3102645,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 7799,'2022-05-11',1065709,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 7799,'2022-05-11',506760,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 3512,'2022-05-11',337600,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 3512,'2022-05-11',710472,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 3209,'2022-05-11',710472,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 3209,'2022-05-11',337600,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 1163,'2022-05-11',-6582319,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 1163,'2022-05-11',244920,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 1163,'2022-05-11',153600,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 7889,'2022-05-11',506760,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY UNION ALL
SELECT 7889,'2022-05-11',1065709,'SPOT','Diferencia por regularizacion SPOT' FROM DUMMY;

--VEN IF
INSERT INTO "BSCL.Plataforma.BD::CUA.MovimientosCuentaInversionAjusteOperaciones"
SELECT 44,'2022-05-13',-5060077,'VENC IF','Diferencia por regularizacion en cta inversión por Vencimiento de IF' FROM DUMMY UNION ALL
SELECT 116,'2022-05-13',-4196035,'VENC IF','Diferencia por regularizacion en cta inversión por Vencimiento de IF' FROM DUMMY UNION ALL
--SELECT 1163,'2022-05-13',-39155079,'VENC IF','Diferencia por regularizacion en cta inversión por Vencimiento de IF' FROM DUMMY UNION ALL
SELECT 1580,'2022-05-13',-960551723,'VENC IF','Diferencia por regularizacion en cta inversión por Vencimiento de IF' FROM DUMMY UNION ALL
SELECT 8411,'2022-05-13',-90643222,'VENC IF','Diferencia por regularizacion en cta inversión por Vencimiento de IF' FROM DUMMY UNION ALL
--SELECT 11570,'2022-05-13',-2466540509,'VENC IF','Diferencia por regularizacion en cta inversión por Vencimiento de IF' FROM DUMMY UNION ALL
--SELECT 14711,'2022-05-13',-4221939911,'VENC IF','Diferencia por regularizacion en cta inversión por Vencimiento de IF' FROM DUMMY UNION ALL
SELECT 137,'2022-05-13',-35448085,'VENC IF','Diferencia por regularizacion en cta inversión por Vencimiento de IF' FROM DUMMY UNION ALL
SELECT 23552,'2022-05-13',-32128000,'VENC IF','Diferencia por regularizacion en cta inversión por Vencimiento de IF' FROM DUMMY;

--FWD
INSERT INTO "BSCL.Plataforma.BD::CUA.MovimientosCuentaInversionAjusteOperaciones"
SELECT 1253,'2022-05-13',41535000,'VENC FWD','Diferencia por vencimiento FWD por cambio de cuentas entre mismo cliente' FROM DUMMY UNION ALL
SELECT 5291,'2022-05-13',-41535000,'VENC FWD','Diferencia por vencimiento FWD por cambio de cuentas entre mismo cliente' FROM DUMMY UNION ALL
SELECT 1436,'2022-05-13',8004000,'VENC FWD','Diferencia por vencimiento FWD por cambio de cuentas entre mismo cliente' FROM DUMMY UNION ALL
SELECT 8000,'2022-05-13',-8004000,'VENC FWD','Diferencia por vencimiento FWD por cambio de cuentas entre mismo cliente' FROM DUMMY;

--OTROS
INSERT INTO "BSCL.Plataforma.BD::CUA.MovimientosCuentaInversionAjusteOperaciones"
SELECT 8414,'2022-05-13',28201491,'CFI','Diferencia por CFI USD pagado en USD factura CFI secuencia 384282' FROM DUMMY UNION ALL
SELECT 12017,'2022-05-13',-17917322,'CFI','Diferencia por CFI USD pagado en USD factura CFI secuencia 398427' FROM DUMMY UNION ALL
SELECT 13724,'2022-05-13',-857880000,'SPOT','Diferencia por regularización MBI factura SPT secuencia 341585' FROM DUMMY UNION ALL
SELECT 614,'2022-05-13',-6253,'DIV','Diferencia por regularización de dividendo 142871 27-04-2022' FROM DUMMY UNION ALL
SELECT 899,'2022-05-13',895823,'CC','Diferencia por regularización manual MBI por corte cupon 17151 02-05-2022' FROM DUMMY UNION ALL
SELECT 4979,'2022-05-13',-119000,'MANUAL','Diferencia por regularización manual por cobro asociado a la FActura del 03-05 que se ingreso de forma manual en mov por facturar. La factura es 400785' FROM DUMMY UNION ALL
SELECT 3035,'2022-05-13',38812900,'SPOT','Diferencia por regularización manual por factura SPT origen FWD secuencia 383395' FROM DUMMY UNION ALL
SELECT 1736,'2022-05-13',668382048,'RC','Diferencia por regularización operacion de Reparto Capital del 01-03-2022' FROM DUMMY UNION ALL
SELECT 1280,'2022-05-13',558947,'CC','Diferencia por regularización operacion de Corte Cupon del 01-03-2022' FROM DUMMY UNION ALL
SELECT 209,'2022-05-13',-363431,'MANUAL','Diferencia por regularización manual MBI factura secuencia 64544 08-02-2022' FROM DUMMY UNION ALL
SELECT 665,'2022-05-17',-3120252,'MANUAL','Diferencia por regularización de dividendo' FROM DUMMY UNION ALL
SELECT 12665,'2022-05-13',-35622365,'CFI','Diferencia por CFI USD pagado en USD factura CFI secuencia 386937' FROM DUMMY;

*/

-------------RELACION EVENTO CAPITAL---------------------
UPDATE EVENTO
SET EVENTO."IdPortafolio" = CL."IdPortafolio"
FROM BSCL."BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EVENTO
INNER JOIN BSCL."BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" AS CL ON (EVENTO."DocumentoIdentidad" = CL."DocumentoIdentidad" AND CL."CodigoPortafolio"='0')
WHERE EVENTO."TipoSocioNegocio"='CNT' AND EVENTO."IdPortafolio"=0;

-------------REGULARIZACION CABECERA SIM------------------
UPDATE CAR SET CAR."INGMontoAPlazo" = MOV_TP."Monto" FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV
INNER JOIN BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."IdCartera" = MOV."IdComprobante"
INNER JOIN BSCL."BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV_T0 ON CAR."FolioComprobante" = MOV_T0."IdComprobante" AND MOV_T0."IdCondicionLiquidacion" = 'T0'
INNER JOIN BSCL."BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV_TP ON MOV_T0."IdOrden" = MOV_TP."IdOrden" AND MOV_TP."IdCondicionLiquidacion" = 'TP'
INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdCuentaInversion" = CTA."IdCuentaInversion"
INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
WHERE ROUND(CAR."INGMontoAPlazo",0) - ROUND(MOV_TP."Monto",0) <> 0 
AND CAR."EGRFechaOperacion" <> '0001-01-01' AND CAR."IdEstado" = 'VCD' 
AND MOV_TP."Monto" = MOV."Monto";


-------------OBTIENE OPERACIONES----------------------
DROP TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionOperacionesPorFecha";
CREATE COLUMN TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionOperacionesPorFecha" AS (
--DROP TABLE #p_MovimientosPorFecha;
--CREATE LOCAL TEMPORARY TABLE #p_MovimientosPorFecha AS (
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
								ELSE -1 * FAC."MontoComprobanteFinal"
							END
						ELSE FAC."MontoComprobanteFinal" 
					END
			END 
	END AS "Monto", COALESCE(CONCAT('FAC-',FAC."IdTipoInstrumento"),'FAC') AS "IdModulo", FAC."IdComprobante" AS "IdOperacion"
	--, FAC."IdTipoInstrumento", FAC."IdMoneda", FAC."MontoComprobanteFinal", * 
	FROM BSCL."BSCL.Facturacion.BD::FAC.Comprobante" FAC 
	INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON FAC."IdPortafolio" = CTA."IdPortafolio" AND CTA."IdMoneda" = FAC."IdMonedaComprobante"
	INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
	WHERE FAC."FechaLiquidacion" > '2022-01-14' AND FAC."FechaSistema" > '2022-01-14' AND 
	FAC."IdEstadoComprobante" IN ('ENV','FAC') AND FAC."IdMonedaComprobante" = 'CLP' AND FAC."IdTipoOperacion" NOT IN ('COMSIM','VENSIM','ANSIMV','ANSIMC') --AND POR."IdPortafolio" = 5002 ORDER BY 1
	--AND CTA."IdCuentaInversion" = 10652 ORDER BY 1
	--AND FAC."IdComprobante" IN (331202,335424,337349) ORDER BY 1
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
	
	--SELECT * FROM BSCL."BSCL.Mesa.BD::MES.Operacion" WHERE "IdTipoOperacion" IN ('VENTER','COMTER') AND "MontoOperacion" <> "MontoOperacionFinal"

	--SELECT * FROM BSCL."BSCL.Mesa.BD::MES.Operacion" WHERE "IdOperacion" = 122870
	--SELECT "FechaVencimiento", "IdEstado", "MontoOperacion", * FROM BSCL."BSCL.Mesa.BD::MES.Operacion" WHERE "IdPortafolio" = 4627 ORDER BY 1
	
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
	--AND CAR."IdCartera" = 125564
	--AND CAR."IdPortafolio" = 537 ORDER BY 1

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
	
	--SELECT * FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" WHERE "IdCargoAbonoMovimiento" = 46430
	
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

	--SELECT "FechaVencimientoInstrumento", "EGRFechaOperacion", "IdEstado", "EGRIdTipoOperacion", "IdTipoReajuste", "IdTipoInstrumento", "INGCantidad" * "INGValorRescate","EGRCantidad" * "EGRValorRescate",* 
	--FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" WHERE "IdPortafolio" = 5797 AND "IdCartera" = 80994
	--SELECT * FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" WHERE "IdCuentaInversion" = 50
	
	UNION ALL
	
	SELECT CAR."EGRFechaOperacion" AS "Fecha", CAR."IdCliente", CAR."IdPortafolio", CAR."IdMonedaSistema" AS "IdMoneda", 
	CASE CAR."IdTipoOperacion"
		WHEN 'VENSIM' THEN -1 * CAR."EGRMontoARecibirAnticipo"
		WHEN 'COMSIM' THEN CAR."EGRMontoARecibirAnticipo" 
		ELSE CAR."EGRMontoARecibirAnticipo"
	END AS "Monto", 'ASM' AS "IdModulo", CAR."IdCartera" AS "IdOperacion"
	FROM BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR  WHERE "EGRFechaOperacion" > '2022-01-14' AND "IdTipoInstrumento" IN ('SIM') AND "IdEstado" = 'ANT' --AND "IdPortafolio" = 7812 ORDER BY 1
	--AND CAR."IdCartera" = 130322
	
	/*
	SELECT "IdCargoAbonoMovimiento", POR."IdPortafolio", CTA."IdCuentaInversion", POR."DocumentoIdentidad", POR."CodigoPortafolio", CAR."EGRFechaOperacion" AS "Vencimiento", 
	CAR."INGMontoAPlazo" AS "MontoSim", MOV_TP."Monto" AS "MontoFactura", ROUND(CAR."INGMontoAPlazo",0) - ROUND(MOV_TP."Monto",0) AS "Diferencia", MOV."Monto" AS "MontoCtaInversion", CAR.*, MOV.* FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV
	INNER JOIN BSCL."BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."IdCartera" = MOV."IdComprobante"
	INNER JOIN BSCL."BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV_T0 ON CAR."FolioComprobante" = MOV_T0."IdComprobante" AND MOV_T0."IdCondicionLiquidacion" = 'T0'
	INNER JOIN BSCL."BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV_TP ON MOV_T0."IdOrden" = MOV_TP."IdOrden" AND MOV_TP."IdCondicionLiquidacion" = 'TP'
	INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdCuentaInversion" = CTA."IdCuentaInversion"
	INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
	WHERE POR."IdPortafolio" = 955
	*/
	
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
DROP TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoCalculado";
CREATE COLUMN TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoCalculado" AS (
--DROP TABLE #p_SaldoCuentaInversionCalculado;
--CREATE LOCAL TEMPORARY TABLE #p_SaldoCuentaInversionCalculado AS (
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
			BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionOperacionesPorFecha"
		GROUP BY "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "IdModulo"
	) 
	GROUP BY "Fecha", "IdCliente", "IdPortafolio", "IdMoneda"
	ORDER BY 1
);

-------------OBTIENE SALDOS CUENTA INVERSION----------------------
DROP TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha";
CREATE COLUMN TABLE BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha" AS (
--DROP TABLE #p_SaldosPorFecha;
--CREATE LOCAL TEMPORARY TABLE #p_SaldosPorFecha AS (
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
DROP TABLE #p_CuadraturaCuentaInversionIds;
CREATE LOCAL TEMPORARY TABLE #p_CuadraturaCuentaInversionIds AS (
SELECT DISTINCT CTA."IdCuentaInversion" FROM (
	SELECT SALDO."IdCliente", SALDO."IdPortafolio", SALDO."IdMoneda", MOV."SaldoMov" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta" FROM (
		SELECT S."Fecha", S."IdCliente", S."IdPortafolio", S."IdMoneda", S."Saldo" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha" S
		INNER JOIN (
			SELECT MAX("Fecha") AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha"
			GROUP BY "IdCliente", "IdPortafolio", "IdMoneda"
			) SFinal 
		ON S."Fecha" = SFinal."Fecha" AND S."IdCliente" = SFinal."IdCliente" AND S."IdPortafolio" = SFinal."IdPortafolio" AND S."IdMoneda" = SFinal."IdMoneda"
		) SALDO 
		LEFT JOIN (
			SELECT MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda", SUM(MOV."Monto") AS "SaldoMov" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoCalculado" MOV 
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
DROP TABLE #p_IdsCuentaInversionDIV;
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
	SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "Monto", "IdModulo", "IdOperacion" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionOperacionesPorFecha"
) MOV INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON MOV."IdPortafolio" = POR."IdPortafolio"
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON CTA."IdPortafolio" = POR."IdPortafolio"
WHERE CTA."IdCuentaInversion" IN (SELECT "IdCuentaInversion" FROM #p_CuadraturaCuentaInversionIds)
AND MOV."IdModulo" = 'DV' AND MOV."Monto" < 0
);

--CASOS PACTOS
DROP TABLE #p_IdsCuentaInversionPAC;
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
		SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "Monto", "IdModulo", "IdOperacion" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionOperacionesPorFecha"
	) MOV INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON MOV."IdPortafolio" = POR."IdPortafolio"
	INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON CTA."IdPortafolio" = POR."IdPortafolio"
	WHERE CTA."IdCuentaInversion" IN (SELECT "IdCuentaInversion" FROM #p_CuadraturaCuentaInversionIds)
	AND MOV."IdModulo" = 'MES' --AND MOV."Monto" < 0
	AND CTA."IdCuentaInversion" NOT IN (
		SELECT "IdCuentaInversion" FROM #p_IdsCuentaInversionDIV
	)
);

--CASOS SIM
DROP TABLE #p_IdsCuentaInversionSIM;
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
		SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "Monto", "IdModulo", "IdOperacion" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionOperacionesPorFecha"
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
DROP TABLE #p_IdsCuentaInversionSPOT;
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
		SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "Monto", "IdModulo", "IdOperacion" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionOperacionesPorFecha"
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
DROP TABLE #p_IdsCuentaInversionOTROS;
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
		SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "Monto", "IdModulo", "IdOperacion" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionOperacionesPorFecha"
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


--SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema'

/*
--DETALLE CASOS DIVIDENDOS
SELECT * FROM (
	SELECT 'DIV', CTA."IdCuentaInversion", POR."IdPortafolio", POR."DocumentoIdentidad", POR."CodigoPortafolio", CTA."IdMoneda", SALDO."SaldoCalculado", SALDO."SaldoConsulta", ROUND(SALDO."SaldoCalculado" - SALDO."SaldoConsulta", 0) AS "DiferenciaSaldo" FROM (
		SELECT SALDO."IdCliente", SALDO."IdPortafolio", SALDO."IdMoneda", MOV."SaldoMov" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta" FROM (
			SELECT S."Fecha", S."IdCliente", S."IdPortafolio", S."IdMoneda", S."Saldo" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha" S
			INNER JOIN (
				SELECT MAX("Fecha") AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha"
				GROUP BY "IdCliente", "IdPortafolio", "IdMoneda"
				) SFinal 
			ON S."Fecha" = SFinal."Fecha" AND S."IdCliente" = SFinal."IdCliente" AND S."IdPortafolio" = SFinal."IdPortafolio" AND S."IdMoneda" = SFinal."IdMoneda"
			) SALDO 
			LEFT JOIN (
				SELECT MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda", SUM(MOV."Monto") AS "SaldoMov" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoCalculado" MOV 
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
	SELECT 'PAC', CTA."IdCuentaInversion", POR."IdPortafolio", POR."DocumentoIdentidad", POR."CodigoPortafolio", CTA."IdMoneda", SALDO."SaldoCalculado", SALDO."SaldoConsulta", ROUND(SALDO."SaldoCalculado" - SALDO."SaldoConsulta", 0) AS "DiferenciaSaldo" FROM (
		SELECT SALDO."IdCliente", SALDO."IdPortafolio", SALDO."IdMoneda", MOV."SaldoMov" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta" FROM (
			SELECT S."Fecha", S."IdCliente", S."IdPortafolio", S."IdMoneda", S."Saldo" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha" S
			INNER JOIN (
				SELECT MAX("Fecha") AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha"
				GROUP BY "IdCliente", "IdPortafolio", "IdMoneda"
				) SFinal 
			ON S."Fecha" = SFinal."Fecha" AND S."IdCliente" = SFinal."IdCliente" AND S."IdPortafolio" = SFinal."IdPortafolio" AND S."IdMoneda" = SFinal."IdMoneda"
			) SALDO 
			LEFT JOIN (
				SELECT MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda", SUM(MOV."Monto") AS "SaldoMov" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoCalculado" MOV 
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
	SELECT 'SIM', CTA."IdCuentaInversion", POR."IdPortafolio", POR."DocumentoIdentidad", POR."CodigoPortafolio", CTA."IdMoneda", SALDO."SaldoCalculado", SALDO."SaldoConsulta", ROUND(SALDO."SaldoCalculado" - SALDO."SaldoConsulta", 0) AS "DiferenciaSaldo" FROM (
		SELECT SALDO."IdCliente", SALDO."IdPortafolio", SALDO."IdMoneda", MOV."SaldoMov" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta" FROM (
			SELECT S."Fecha", S."IdCliente", S."IdPortafolio", S."IdMoneda", S."Saldo" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha" S
			INNER JOIN (
				SELECT MAX("Fecha") AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha"
				GROUP BY "IdCliente", "IdPortafolio", "IdMoneda"
				) SFinal 
			ON S."Fecha" = SFinal."Fecha" AND S."IdCliente" = SFinal."IdCliente" AND S."IdPortafolio" = SFinal."IdPortafolio" AND S."IdMoneda" = SFinal."IdMoneda"
			) SALDO 
			LEFT JOIN (
				SELECT MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda", SUM(MOV."Monto") AS "SaldoMov" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoCalculado" MOV 
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
	SELECT 'SPT', CTA."IdCuentaInversion", POR."IdPortafolio", POR."DocumentoIdentidad", POR."CodigoPortafolio", CTA."IdMoneda", SALDO."SaldoCalculado", SALDO."SaldoConsulta", ROUND(SALDO."SaldoCalculado" - SALDO."SaldoConsulta", 0) AS "DiferenciaSaldo" FROM (
		SELECT SALDO."IdCliente", SALDO."IdPortafolio", SALDO."IdMoneda", MOV."SaldoMov" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta" FROM (
			SELECT S."Fecha", S."IdCliente", S."IdPortafolio", S."IdMoneda", S."Saldo" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha" S
			INNER JOIN (
				SELECT MAX("Fecha") AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha"
				GROUP BY "IdCliente", "IdPortafolio", "IdMoneda"
				) SFinal 
			ON S."Fecha" = SFinal."Fecha" AND S."IdCliente" = SFinal."IdCliente" AND S."IdPortafolio" = SFinal."IdPortafolio" AND S."IdMoneda" = SFinal."IdMoneda"
			) SALDO 
			LEFT JOIN (
				SELECT MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda", SUM(MOV."Monto") AS "SaldoMov" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoCalculado" MOV 
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
	SELECT 'OTR', CTA."IdCuentaInversion", POR."IdPortafolio", POR."DocumentoIdentidad", POR."CodigoPortafolio", CTA."IdMoneda", SALDO."SaldoCalculado", SALDO."SaldoConsulta", ROUND(SALDO."SaldoCalculado" - SALDO."SaldoConsulta", 0) AS "DiferenciaSaldo" FROM (
		SELECT SALDO."IdCliente", SALDO."IdPortafolio", SALDO."IdMoneda", MOV."SaldoMov" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta" FROM (
			SELECT S."Fecha", S."IdCliente", S."IdPortafolio", S."IdMoneda", S."Saldo" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha" S
			INNER JOIN (
				SELECT MAX("Fecha") AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha"
				GROUP BY "IdCliente", "IdPortafolio", "IdMoneda"
				) SFinal 
			ON S."Fecha" = SFinal."Fecha" AND S."IdCliente" = SFinal."IdCliente" AND S."IdPortafolio" = SFinal."IdPortafolio" AND S."IdMoneda" = SFinal."IdMoneda"
			) SALDO 
			LEFT JOIN (
				SELECT MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda", SUM(MOV."Monto") AS "SaldoMov" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoCalculado" MOV 
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
)
ORDER BY 1,2;

*/


----------------------TOTAL OPERACION  + SALDO INICIAL----------------------
/*
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
	SELECT "Fecha", "IdCliente", "IdPortafolio", "IdMoneda", "Monto", "IdModulo", "IdOperacion" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionOperacionesPorFecha"
) CTA INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
ORDER BY CTA."IdPortafolio", CTA."Fecha";
*/

----------------------CUADRE----------------------
/*
SELECT CTA."IdCuentaInversion", POR."IdPortafolio", POR."DocumentoIdentidad", POR."CodigoPortafolio", CTA."IdMoneda", SALDO."SaldoCalculado", SALDO."SaldoConsulta", ROUND(SALDO."SaldoCalculado" - SALDO."SaldoConsulta", 0) AS "DiferenciaSaldo" FROM (
	SELECT SALDO."IdCliente", SALDO."IdPortafolio", SALDO."IdMoneda", MOV."SaldoMov" AS "SaldoCalculado", SALDO."Saldo" AS "SaldoConsulta" FROM (
		SELECT S."Fecha", S."IdCliente", S."IdPortafolio", S."IdMoneda", S."Saldo" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha" S
		INNER JOIN (
			SELECT MAX("Fecha") AS "Fecha", "IdCliente", "IdPortafolio", "IdMoneda" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoPorFecha"
			GROUP BY "IdCliente", "IdPortafolio", "IdMoneda"
			) SFinal 
		ON S."Fecha" = SFinal."Fecha" AND S."IdCliente" = SFinal."IdCliente" AND S."IdPortafolio" = SFinal."IdPortafolio" AND S."IdMoneda" = SFinal."IdMoneda"
		) SALDO 
		LEFT JOIN (
			SELECT MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda", SUM(MOV."Monto") AS "SaldoMov" FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionSaldoCalculado" MOV 
			GROUP BY MOV."IdCliente", MOV."IdPortafolio", MOV."IdMoneda"
		) MOV ON SALDO."IdCliente" = MOV."IdCliente" AND SALDO."IdPortafolio" = MOV."IdPortafolio" AND SALDO."IdMoneda" = MOV."IdMoneda"
	WHERE SALDO."IdMoneda" = 'CLP' 
	--AND SALDO."IdPortafolio" IN (12116,584,185,794)
	--AND (ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) > 100 OR ROUND(SALDO."Saldo" - MOV."SaldoMov", 0) < -100) --1142 NOK --427 --332 --293 --199--312--300--235--191
) SALDO 
INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON SALDO."IdPortafolio" = POR."IdPortafolio"
INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON SALDO."IdMoneda" = CTA."IdMoneda" AND CTA."IdPortafolio" = POR."IdPortafolio"
WHERE POR."DocumentoIdentidad" <> '96921130-0' --CTA PROPIA
--AND CTA."IdCuentaInversion" IN (1580,12665)
--AND CTA."IdCuentaInversion" IN (SELECT "IdCuentaInversion" FROM #p_CuadraturaCuentaInversionIds)
--AND POR."IdPortafolio" = 4531
ORDER BY 1;
*/


----------------------CUADRE CUENTA INVERSIÓN MOV------------------------------------
/*
SELECT CTA."IdCuentaInversion", POR."DocumentoIdentidad", POR."NombreCompleto", CTA."IdMoneda", MOV."Saldo" AS "SaldoMovimientos", SALDO."Saldo" AS "SaldoTabla" FROM (
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
			MOV."FechaSistema", CTA."IdCuentaInversion", POR."IdCliente", POR."IdPortafolio", CTA."IdMoneda", MOV."SaldoPorFechaMovimiento"
			FROM BSCL."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" MOV
			INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdCuentaInversion" = CTA."IdCuentaInversion"
			INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
			WHERE MOV."FechaSistema" > '2022-01-14' AND MOV."IdEstado" = 'CRT' AND CTA."IdMoneda" = 'CLP' --AND POR."IdPortafolio" = 6970
		) WHERE RANK = 1
	)
) SALDO ON RANK = 1 AND MOV."IdCuentaInversion" = SALDO."IdCuentaInversion"
INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdCuentaInversion" = CTA."IdCuentaInversion"
INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" POR ON CTA."IdPortafolio" = POR."IdPortafolio"
WHERE MOV."Saldo" <> SALDO."Saldo";
*/

------------------------------OPERACIONES con fecha anterior al 14-01-2022----------------------------
/*
SELECT CTA."IdCuentaInversion", MOV.* FROM BSCL."BSCL.Plataforma.BD::CUA.CuentaInversionOperacionesPorFecha" MOV 
INNER JOIN BSCL."BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON MOV."IdPortafolio" = CTA."IdPortafolio" AND CTA."IdMoneda" = 'CLP'
WHERE MOV."Fecha" <= '2022-01-14'
ORDER BY 1
*/