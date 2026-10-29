SET SCHEMA "BSCL";
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTMSCF";

CREATE VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBCTMSCF" AS
WITH
	cobros as (
		SELECT 
			"IdComprobante",
			SUM("COFI"+"COSI"+"COVA"+"COAI"+"COIF"+"CCOR") AS "Comision",
			SUM("DESI"+"DEFI"+"DEVA") AS "Derechos",
			SUM("COEX"+"DEEX"+"GAEX") AS "Exentos",
			SUM("GFOD"+"GFFA") AS "Gastos",
			SUM("IVA") AS "IVA"
		FROM (
			SELECT  "IdComprobante",
				--COMISIONES
				COALESCE(MAX(CASE WHEN "IdCobro" = 'COFI' THEN "MontoCobro" END),0) AS "COFI",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'COSI' THEN "MontoCobro" END),0) AS "COSI",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'COVA' THEN "MontoCobro" END),0) AS "COVA",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'COAI' THEN "MontoCobro" END),0) AS "COAI",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'COIF' THEN "MontoCobro" END),0) AS "COIF",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'CCOR' THEN "MontoCobro" END),0) AS "CCOR",
				--EXENTOS
				COALESCE(MAX(CASE WHEN "IdCobro" = 'COEX' THEN "MontoCobro" END),0) AS "COEX",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'DEEX' THEN "MontoCobro" END),0) AS "DEEX",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'GAEX' THEN "MontoCobro" END),0) AS "GAEX",
				--DERECHOS
				COALESCE(MAX(CASE WHEN "IdCobro" = 'DESI' THEN "MontoCobro" END),0) AS "DESI",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'DEFI' THEN "MontoCobro" END),0) AS "DEFI",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'DEVA' THEN "MontoCobro" END),0) AS "DEVA",
				--GASTOS
				COALESCE(MAX(CASE WHEN "IdCobro" = 'GFOD' THEN "MontoCobro" END),0) AS "GFOD",
				COALESCE(MAX(CASE WHEN "IdCobro" = 'GFFA' THEN "MontoCobro" END),0) AS "GFFA",
				--IVA
				COALESCE(MAX(CASE WHEN "IdCobro" = 'IVA' THEN "MontoCobro" END),0) AS "IVA"
			FROM (
					SELECT "IdComprobante", "IdCobro", "MontoCobro"
					FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro"
			     )
			GROUP BY "IdComprobante"
			)
		GROUP BY "IdComprobante"
	)
SELECT
	 COM."IdTipoComprobante" AS TIPO_COMP_FAC										--char
	,COM."FolioComprobante" AS FOLIO_COMP_FAC										--decimal
	,COM."FechaComprobante" AS FEC_ING_COMP											--char
	,COM."DocumentoIdentidad" AS RUT_CLI											--char
	,PORT."CodigoPortafolio" AS SEC_RUT_CLI												--decimal
	,COM."IdSucursalEmpleadoEjecutivoPortafolio" AS COD_SUC							--char
	,COM."IdEmpleadoEjecutivoPortafolio" AS COD_AGENTE								--char
	, 0 AS COD_CORR_CONTRA		--decimal
	,'' AS COD_GASTO_FACT		--char
	,COM."IdEstadoComprobante" AS EST_COMP											--char
	,COM."FechaEmision" AS FEC_EMI_COMP												--char
	,COM."FechaLiquidacion" AS FEC_PAGO												--char
	,COM."FolioComprobanteReferencia" AS FOLIO_COMP_ASOC							--decimal
	, 0 AS FOLIO_COMP_CONT		--decimal
	,'' AS IND_BOLSA			--char
	,'' AS IND_CHEQUE			--char
	,'' AS IND_CTA_PRO			--char
	,'' AS IND_DEST				--char
	,'' AS IND_FACTURA			--char
	,'' AS IND_LIQUID			--char
	,'' AS IND_PAGO_COBRO		--char
	,CASE WHEN COM."IdTipoProducto" = 'INSI' THEN 'TS'
		  ELSE 'NOR'
	  END AS IND_SIMUL			--char
	,'' AS MERCADO				--char
	,CASE WHEN TOPE."Accion" = 'COMPRA' THEN (ABS(COM."MontoComprobanteFinal") - ABS(COM."MontoComprobanteCobro")) 
		  ELSE 0
	  END AS MONTO_COM						--decimal
	, 0 AS MONTO_COMIS_CALC					--decimal
	,COB."Comision" AS MONTO_COMISION		--decimal
	,COB."Derechos" AS MONTO_DERECHOS		--decimal
	,COB."Exentos" AS MONTO_EXENTO			--decimal
	,COB."Gastos" AS MONTO_GASTO			--decimal
	,COB."IVA" AS MONTO_IVA					--decimal
	,COM."MontoLiquidacionFinal" AS MONTO_PAGADO											--decimal
	,COM."MontoComprobanteFinal" AS MONTO_TOTAL												--decimal
	,CASE WHEN TOPE."Accion" = 'VENTA' THEN (ABS(COM."MontoComprobanteFinal") - ABS(COM."MontoComprobanteCobro")) 
		  ELSE 0
	  END AS MONTO_VEN			--decimal
	,'' AS PRODUCTO				--char
	,'' AS IND_CONV				--char
	, 0 AS MONTO_DERECHOS_CONV	--decimal
	, 0 AS MONTO_COMISION_CONV	--decimal
	, 0 AS MONTO_GASTOS_CONV	--decimal
	, 0 AS SEC_DIST				--decimal
	,'' AS IND_PROC_LIQUID		--char
	,'' AS IND_PRECIO_PRIMA		--char
	, 0 AS PLAZO_PRIMA			--numeric
	, 0 AS PARIDAD_MORIGEN		--decimal
	, 0 AS CAPTACION_MORIGEN	--decimal
	,COM."IdMonedaLiquidacion" AS MON_MORIGEN			--char
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS COM
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON PORT."IdPortafolio" = COM."IdPortafolio"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPE ON TOPE."IdTipoOperacion" = COM."IdTipoOperacion"
LEFT JOIN cobros AS COB ON COB."IdComprobante" = COM."IdComprobante"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaTBCTMSCF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
