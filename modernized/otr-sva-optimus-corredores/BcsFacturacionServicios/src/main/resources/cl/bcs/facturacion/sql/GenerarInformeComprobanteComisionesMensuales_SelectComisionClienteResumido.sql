SELECT * FROM ((SELECT 
2 AS "OrdenFila"
,PORTAFOLIO."CodigoPortafolio"
,COMP_COMISION."DocumentoIdentidad" AS "RutCliente"
,COMP_COMISION."NombreLargoCliente" AS "NombreCliente"
,CASE WHEN CC."IdFormatoCobro" = 'AGRRUT' THEN 'AGRUPADO' ELSE 'DETALLADO' END AS "TipoContrato"
,COMP_COMISION."IdMoneda"
,TO_VARCHAR(COMP_ORIGINAL."FechaComprobante",'MM-YYYY') AS "PeriodoComprobante"
,SUM(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'RV' THEN TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "MontoAcciones"
,SUM(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'RV' THEN TO_DECIMAL(COMISION."MontoProrrateadoComision", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "ComisionAcciones"
,SUM(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'IF' THEN TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "MontoIIF"
,SUM(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'IF' THEN TO_DECIMAL(COMISION."MontoProrrateadoComision", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "ComisionIIF"
,SUM(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'RF' THEN TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "MontoRF"
,SUM(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'RF' THEN TO_DECIMAL(COMISION."MontoProrrateadoComision", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "ComisionRF"
,SUM(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'SP' THEN TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "MontoMonetario"
,SUM(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'SP' THEN TO_DECIMAL(COMISION."MontoProrrateadoComision", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "ComisionMonetario"
,COMP_ORIGINAL."IdTipoComprobante" AS "TipoComprobante"
,DETALLEINDICADOR."DetalleIndicador" || '_' || COMP_COMISION."DocumentoIdentidad"
|| '_' || COMP_COMISION."NombreLargoCliente"
|| '_' || PORTAFOLIO."CodigoPortafolio"
|| '_' || TO_VARCHAR(COMP_ORIGINAL."FechaComprobante",'MM-YYYY') AS "AgruparDatosCliente"
FROM "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensual" AS COMP_GENERADO
INNER JOIN ( SELECT COMP_DETALLE."IdComprobanteComisionMensual",COMP_ORIGINAL."FechaComprobante", COMP_ORIGINAL."IdTipoComprobante", SUM(COMP_ORIGINAL."MontoLiquidacion") AS "MontoLiquidacion", COMP_DETALLE."IdContratoComision", COMP_ORIGINAL."ModPrefFact", COMP_ORIGINAL."IdClasificacionTipoInstrumento"
	FROM "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle" AS COMP_DETALLE
	LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_ORIGINAL ON COMP_ORIGINAL."IdComprobante" = COMP_DETALLE."IdComprobante"
	WHERE COMP_ORIGINAL."IdComprobanteReferencia" = 0 AND COMP_ORIGINAL."IdEstadoComprobante" <> 'ANU'
	GROUP BY COMP_DETALLE."IdComprobanteComisionMensual", COMP_ORIGINAL."FechaComprobante",COMP_ORIGINAL."IdTipoComprobante", COMP_DETALLE."IdContratoComision", COMP_ORIGINAL."ModPrefFact", COMP_ORIGINAL."IdClasificacionTipoInstrumento"
	) AS COMP_ORIGINAL ON COMP_ORIGINAL."IdComprobanteComisionMensual" = COMP_GENERADO."IdComprobanteComisionMensual" 
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_COMISION ON COMP_COMISION."IdComprobante" = COMP_GENERADO."IdComprobanteComisionMensual" 
LEFT JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS CC ON CC."IdContratoComision" = COMP_ORIGINAL."IdContratoComision"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = COMP_COMISION."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS DETALLEINDICADOR 
	    ON DETALLEINDICADOR."CodigoTipoIndicador" = 'FAC_MODFAC' 
	    	AND DETALLEINDICADOR."Codigo" = COMP_ORIGINAL."ModPrefFact" 
	    	AND DETALLEINDICADOR."IdEmpresa" = COMP_COMISION."IdEmpresa"
LEFT JOIN ( 
SELECT
    TOTAL_COMISION."IdComprobanteComisionMensual",
    SUM(TOTAL_COMISION."MontoProrrateadoComision") AS "MontoProrrateadoComision"
FROM (
SELECT
        DETALLE."IdComprobanteComisionMensual",
		CASE WHEN (COMP_ORIGINAL."IdComprobanteReferencia" = 0 AND COMP_ORIGINAL."IdEstadoComprobante" <> 'ANU')
		THEN (COMP_ORIGINAL."MontoLiquidacion" / SUM(COMP_ORIGINAL."MontoLiquidacion") OVER (PARTITION BY DETALLE."IdContratoComision", DETALLE."IdComprobanteComisionMensual")) * COMP_GENERADO."MontoComision" 
		ELSE 0
		END AS "MontoProrrateadoComision"
	FROM "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle" AS DETALLE
    LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_ORIGINAL
        ON COMP_ORIGINAL."IdComprobante" = DETALLE."IdComprobante"
    LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensual" AS COMP_GENERADO
        ON COMP_GENERADO."IdComprobanteComisionMensual" = DETALLE."IdComprobanteComisionMensual"
     ) TOTAL_COMISION GROUP BY TOTAL_COMISION."IdComprobanteComisionMensual") COMISION ON COMISION."IdComprobanteComisionMensual" = COMP_GENERADO."IdComprobanteComisionMensual"
WHERE CC."IdEstado" = 'ACT' AND COMP_COMISION."IdCliente" = CASE WHEN :IdCliente = 0 THEN COMP_COMISION."IdCliente" ELSE :IdCliente END 
AND ( CC."IdFormatoCobro" = 'AGRRUT' OR COMP_COMISION."IdPortafolio" = CASE WHEN :IdPortafolio = 0 THEN COMP_COMISION."IdPortafolio" ELSE :IdPortafolio END )
AND COMP_ORIGINAL."IdClasificacionTipoInstrumento" = CASE WHEN :IdClasificacionTipoInstrumento = '' THEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" ELSE :IdClasificacionTipoInstrumento END
AND ((:IdSegmentoCliente = 0 AND (CC."IdSegmentoCliente" IS NULL OR CC."IdSegmentoCliente" IS NOT NULL))
		OR :IdSegmentoCliente != 0 AND CC."IdSegmentoCliente" = :IdSegmentoCliente)
AND COMP_ORIGINAL."FechaComprobante" BETWEEN :FechaDesde AND :FechaHasta
AND COMP_COMISION."IdComprobanteReferencia" = 0
GROUP BY
		COMP_COMISION."DocumentoIdentidad",
		PORTAFOLIO."CodigoPortafolio",
		COMP_COMISION."IdMoneda",
		DETALLEINDICADOR."DetalleIndicador",
		COMP_COMISION."NombreLargoCliente",
		COMP_ORIGINAL."IdTipoComprobante",
		TO_VARCHAR(COMP_ORIGINAL."FechaComprobante",'MM-YYYY'),
		CC."IdFormatoCobro" )
UNION ALL
(SELECT
1 AS "OrdenFila"
,COMPROBANTE."CodigoPortafolio"
,COMPROBANTE."RutCliente"
,COMPROBANTE."NombreCliente"
,COMPROBANTE."TipoContrato"
,COMPROBANTE."IdMoneda"
,COMPROBANTE."PeriodoComprobante"
,SUM(CASE WHEN COMPROBANTE."IdClasificacionTipoInstrumento" = 'RV' THEN COMPROBANTE."MontoLiquidacion" ELSE TO_DECIMAL(0, 26, 8) END) AS "MontoAcciones"
,0 AS "ComisionAcciones"
,SUM(CASE WHEN COMPROBANTE."IdClasificacionTipoInstrumento" = 'IF' THEN COMPROBANTE."MontoLiquidacion" ELSE TO_DECIMAL(0, 26, 8) END) AS "MontoIIF"
,0 AS "ComisionIIF"
,SUM(CASE WHEN COMPROBANTE."IdClasificacionTipoInstrumento" = 'RF' THEN COMPROBANTE."MontoLiquidacion" ELSE TO_DECIMAL(0, 26, 8) END) AS "MontoRF"
,0 AS "ComisionRF"
,SUM(CASE WHEN COMPROBANTE."IdClasificacionTipoInstrumento" = 'SP' THEN COMPROBANTE."MontoLiquidacion" ELSE TO_DECIMAL(0, 26, 8) END) AS "MontoMonetario"
,0 AS "ComisionMonetario"
,COMPROBANTE."TipoComprobante"
,COMPROBANTE."AgruparDatosCliente"
FROM (
SELECT 
PORTAFOLIO."CodigoPortafolio" AS "CodigoPortafolio"
,COMP_ORIGINAL."DocumentoIdentidad" AS "RutCliente"
,COMP_ORIGINAL."NombreLargoCliente" AS "NombreCliente"
,CASE WHEN CC."IdFormatoCobro" = 'AGRRUT' THEN 'AGRUPADO' ELSE 'DETALLADO' END AS "TipoContrato"
,COMP_ORIGINAL."IdMoneda" AS "IdMoneda"
,TO_VARCHAR(COMP_ORIGINAL."FechaComprobante",'MM-YYYY') AS "PeriodoComprobante"
,COMP_ORIGINAL."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
,TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) AS "MontoLiquidacion"
,CASE WHEN NULLIF(SUM(TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8)) OVER (PARTITION BY COMP_DETALLE."IdContratoComision", COMP_DETALLE."IdComprobanteComisionMensual"),0) IS NULL THEN TO_DECIMAL(0, 26, 8) ELSE (TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) / NULLIF(SUM(TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8)) OVER (PARTITION BY COMP_DETALLE."IdContratoComision", COMP_DETALLE."IdComprobanteComisionMensual"),0)) * TO_DECIMAL(COMP_GENERADO."Monto", 26, 8) END AS "MontoComisionProrrateado"
,COMP_ORIGINAL."IdTipoComprobante" AS "TipoComprobante"
,DETALLEINDICADOR."DetalleIndicador" AS "DetalleIndicador"
,DETALLEINDICADOR."DetalleIndicador" || '_' || COMP_ORIGINAL."DocumentoIdentidad" || '_' || COMP_ORIGINAL."NombreLargoCliente" || '_' || PORTAFOLIO."CodigoPortafolio" || '_' || TO_VARCHAR(COMP_ORIGINAL."FechaComprobante",'MM-YYYY') AS "AgruparDatosCliente"
FROM "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensual" AS COMP_GENERADO
LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle" AS COMP_DETALLE ON COMP_DETALLE."IdComprobanteComisionMensual" = COMP_GENERADO."IdComprobanteComisionMensual" AND COMP_DETALLE."IdMovimientoComprobanteComisionMensual" = COMP_GENERADO."IdMovimientoComprobanteComisionMensual"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_ORIGINAL ON COMP_ORIGINAL."IdComprobante" = COMP_DETALLE."IdComprobante"
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_COMISION ON COMP_COMISION."IdComprobante" = COMP_GENERADO."IdComprobanteComisionMensual"
LEFT JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS CC ON CC."IdContratoComision" = COMP_DETALLE."IdContratoComision"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = COMP_ORIGINAL."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS DETALLEINDICADOR ON DETALLEINDICADOR."CodigoTipoIndicador" = 'FAC_MODFAC' AND DETALLEINDICADOR."Codigo" = COMP_ORIGINAL."ModPrefFact" AND DETALLEINDICADOR."IdEmpresa" = COMP_ORIGINAL."IdEmpresa"
WHERE CC."IdEstado" = 'ACT' AND CC."IdFormatoCobro" = 'AGRRUT' AND COMP_ORIGINAL."IdCliente" = CASE WHEN :IdCliente = 0 THEN COMP_ORIGINAL."IdCliente" ELSE :IdCliente END 
AND ( CC."IdFormatoCobro" = 'AGRRUT' OR COMP_ORIGINAL."IdPortafolio" = CASE WHEN :IdPortafolio = 0 THEN COMP_ORIGINAL."IdPortafolio" ELSE :IdPortafolio END )
AND COMP_ORIGINAL."IdClasificacionTipoInstrumento" = CASE WHEN :IdClasificacionTipoInstrumento = '' THEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" ELSE :IdClasificacionTipoInstrumento END
AND ((:IdSegmentoCliente = 0 AND (CC."IdSegmentoCliente" IS NULL OR CC."IdSegmentoCliente" IS NOT NULL))
		OR :IdSegmentoCliente != 0 AND CC."IdSegmentoCliente" = :IdSegmentoCliente)
AND COMP_ORIGINAL."FechaComprobante" BETWEEN :FechaDesde AND :FechaHasta
AND COMP_ORIGINAL."IdEstadoComprobante" <> 'ANU' AND COMP_ORIGINAL."IdComprobanteReferencia" = 0 AND COMP_COMISION."IdComprobanteReferencia" = 0
) COMPROBANTE
GROUP BY
COMPROBANTE."RutCliente"
,COMPROBANTE."CodigoPortafolio"
,COMPROBANTE."IdMoneda"
,COMPROBANTE."DetalleIndicador"
,COMPROBANTE."NombreCliente"
,COMPROBANTE."TipoComprobante"
,COMPROBANTE."PeriodoComprobante"
,COMPROBANTE."TipoContrato"
,COMPROBANTE."AgruparDatosCliente")
UNION ALL
(SELECT 
3 AS "OrdenFila"
,PORTAFOLIO."CodigoPortafolio"
,COMP_ORIGINAL."DocumentoIdentidad" AS "RutCliente"
,COMP_ORIGINAL."NombreLargoCliente" AS "NombreCliente"
,DETALLEINDICADOR."DetalleIndicador" AS "TipoContrato",
COMP_ORIGINAL."IdMoneda"
,TO_VARCHAR(COMP_ORIGINAL."FechaComprobante",'MM-YYYY') AS "PeriodoComprobante"
,SUM(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'RV' THEN TO_DECIMAL(COMP_GENERADO."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "MontoAcciones"
,MAX(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'RV' THEN TO_DECIMAL(COBRO_DETALLE."MontoCobro", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "ComisionAcciones"
,SUM(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'IF' THEN TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "MontoIIF"
,MAX(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'IF' THEN TO_DECIMAL(COBRO_DETALLE."MontoCobro", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "ComisionIIF"
,SUM(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'RF' THEN TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "MontoRF"
,MAX(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'RF' THEN TO_DECIMAL(COBRO_DETALLE."MontoCobro", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "ComisionRF"
,SUM(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'SP' THEN TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "MontoMonetario"
,MAX(CASE WHEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" = 'SP' THEN TO_DECIMAL(COBRO_DETALLE."MontoCobro", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "ComisionMonetario"
,COMP_ORIGINAL."IdTipoComprobante" AS "TipoComprobante"
,DETALLEINDICADOR."DetalleIndicador" || '_' || COMP_ORIGINAL."DocumentoIdentidad"
|| '_' || COMP_ORIGINAL."NombreLargoCliente"
|| '_' || PORTAFOLIO."CodigoPortafolio"
|| '_' || TO_VARCHAR(COMP_ORIGINAL."FechaComprobante",'MM-YYYY') AS "AgruparDatosCliente"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroMensual" AS COMP_GENERADO
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_ORIGINAL ON COMP_ORIGINAL."IdComprobante" = COMP_GENERADO."IdComprobante"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroMensual" COMP_NCE ON COMP_NCE."IdComprobante" = COMP_GENERADO."IdComprobanteReferencia" AND COMP_NCE."CodigoModoFacturacionComprobante"  = 'MODPREFCONV' 
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenioDetalle" COBRO_DETALLE ON COBRO_DETALLE."IdComprobante" = COMP_GENERADO."IdComprobante" AND COBRO_DETALLE."CodigoCobroClasificacion" = 'COMISION'
LEFT JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS CC ON CC."IdContratoComision" = CASE WHEN COMP_ORIGINAL."IdTipoComprobante" = 'NCE' THEN COMP_NCE."IdContratoComision" ELSE COMP_GENERADO."IdContratoComision" END
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = COMP_ORIGINAL."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS DETALLEINDICADOR 
	    ON DETALLEINDICADOR."CodigoTipoIndicador" = 'FAC_MODFAC' 
	    	AND DETALLEINDICADOR."Codigo" = CASE WHEN COMP_ORIGINAL."IdTipoComprobante" = 'NCE' THEN COMP_NCE."CodigoModoFacturacionComprobante"  ELSE COMP_ORIGINAL."ModPrefFact" END
	    	AND DETALLEINDICADOR."IdEmpresa" = COMP_ORIGINAL."IdEmpresa"
WHERE CC."IdEstado" = 'ACT' AND COMP_GENERADO."IdTipoContrato" = 'CONV' AND COMP_GENERADO."IdComprobanteReferencia" = CASE WHEN COMP_ORIGINAL."IdTipoComprobante" = 'NCE' THEN COMP_NCE."IdComprobante" ELSE 0 END AND
COMP_ORIGINAL."IdCliente" = CASE WHEN :IdCliente = 0 THEN COMP_ORIGINAL."IdCliente" ELSE :IdCliente END 
AND COMP_ORIGINAL."IdPortafolio" = CASE WHEN :IdPortafolio = 0 THEN COMP_ORIGINAL."IdPortafolio" ELSE :IdPortafolio END 
AND COMP_ORIGINAL."IdClasificacionTipoInstrumento" = CASE WHEN :IdClasificacionTipoInstrumento = '' THEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" ELSE :IdClasificacionTipoInstrumento END
AND ((:IdSegmentoCliente = 0 AND (CC."IdSegmentoCliente" IS NULL OR CC."IdSegmentoCliente" IS NOT NULL))
		OR :IdSegmentoCliente != 0 AND CC."IdSegmentoCliente" = :IdSegmentoCliente)
AND COMP_ORIGINAL."FechaComprobante" BETWEEN :FechaDesde AND :FechaHasta AND COMP_ORIGINAL."IdTipoComprobante"  <> 'NCE'
GROUP BY 
		COMP_ORIGINAL."DocumentoIdentidad",
		PORTAFOLIO."CodigoPortafolio",
		COMP_ORIGINAL."IdMoneda",
		DETALLEINDICADOR."DetalleIndicador",
		COMP_ORIGINAL."NombreLargoCliente",
		COMP_ORIGINAL."IdTipoComprobante",
		TO_VARCHAR(COMP_ORIGINAL."FechaComprobante",'MM-YYYY')
		) )
ORDER BY
CASE 
    WHEN "TipoContrato" = 'DETALLADO' THEN 1
    WHEN "TipoContrato" = 'AGRUPADO' THEN 2
END,
"RutCliente",
"PeriodoComprobante",
"OrdenFila",
"CodigoPortafolio";