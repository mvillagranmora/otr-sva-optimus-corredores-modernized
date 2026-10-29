(SELECT 
COMP_ORIGINAL."IdClasificacionTipoInstrumento"
,INSTRUMENTO."Nombre" AS "NombreInstrumento"
,COMP_ORIGINAL."IdTipoComprobante" AS "TipoComprobante"
,COMP_ORIGINAL."IdMoneda"
,SUM(TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8)) AS "TotalCompraVenta"
,SUM(PRORRATEO."MontoProrrateadoIva") AS "TotalIVA"
,SUM(PRORRATEO."MontoProrrateadoDerecho") AS "TotalDerecho"
,SUM(PRORRATEO."MontoProrrateadoComision") AS "TotalComision"
,SUM(CASE WHEN OPERACION."Accion" = 'VENTA' THEN TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "TotalVenta" 
,SUM(CASE WHEN OPERACION."Accion" = 'COMPRA' THEN TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "TotalCompra"
,CASE WHEN CC."IdFormatoCobro" = 'AGRRUT' THEN DETALLEINDICADOR."DetalleIndicador" || '_' || COMP_ORIGINAL."DocumentoIdentidad"
|| '_' || COMP_COMISION."NombreLargoCliente" ELSE DETALLEINDICADOR."DetalleIndicador" || '_' || COMP_ORIGINAL."DocumentoIdentidad"
|| '_' || COMP_ORIGINAL."NombreLargoCliente"  || '_' || PORTAFOLIO_COMISION."CodigoPortafolio" END AS "AgruparSeccion"
FROM "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensual" AS COMP_GENERADO
LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle" AS DETALLE ON DETALLE."IdComprobanteComisionMensual" = COMP_GENERADO."IdComprobanteComisionMensual" AND DETALLE."IdMovimientoComprobanteComisionMensual" = COMP_GENERADO."IdMovimientoComprobanteComisionMensual"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_ORIGINAL ON COMP_ORIGINAL."IdComprobante" = DETALLE."IdComprobante" 
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_COMISION ON COMP_COMISION."IdComprobante" = COMP_GENERADO."IdComprobanteComisionMensual" 
LEFT JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS CC ON CC."IdContratoComision" = DETALLE."IdContratoComision"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPERACION ON OPERACION."IdTipoOperacion" = COMP_ORIGINAL."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS INSTRUMENTO ON INSTRUMENTO."IdClasificacionTipoInstrumento" = COMP_ORIGINAL."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = COMP_ORIGINAL."IdPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO_COMISION ON PORTAFOLIO_COMISION."IdPortafolio" = COMP_COMISION."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS DETALLEINDICADOR 
	    ON DETALLEINDICADOR."CodigoTipoIndicador" = 'FAC_MODFAC' 
	    	AND DETALLEINDICADOR."Codigo" = COMP_ORIGINAL."ModPrefFact" 
	    	AND DETALLEINDICADOR."IdEmpresa" = COMP_ORIGINAL."IdEmpresa"
LEFT JOIN (
    SELECT
        DETALLE."IdContratoComision",
        DETALLE."IdComprobanteComisionMensual",
        COMP_ORIGINAL."IdComprobante",
		(COMP_ORIGINAL."MontoLiquidacion" / SUM(COMP_ORIGINAL."MontoLiquidacion") OVER (PARTITION BY DETALLE."IdContratoComision", DETALLE."IdComprobanteComisionMensual")) * COMP_GENERADO."MontoComision" AS "MontoProrrateadoComision",
    	(COMP_ORIGINAL."MontoLiquidacion" / SUM(COMP_ORIGINAL."MontoLiquidacion") OVER (PARTITION BY DETALLE."IdContratoComision", DETALLE."IdComprobanteComisionMensual")) * COMP_GENERADO."MontoDerecho" AS "MontoProrrateadoDerecho",
    	(COMP_ORIGINAL."MontoLiquidacion" / SUM(COMP_ORIGINAL."MontoLiquidacion") OVER (PARTITION BY DETALLE."IdContratoComision", DETALLE."IdComprobanteComisionMensual")) * COMP_GENERADO."MontoIva" AS "MontoProrrateadoIva"
	FROM "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle" AS DETALLE
    LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_ORIGINAL
        ON COMP_ORIGINAL."IdComprobante" = DETALLE."IdComprobante"
    LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensual" AS COMP_GENERADO
        ON COMP_GENERADO."IdComprobanteComisionMensual" = DETALLE."IdComprobanteComisionMensual"
) PRORRATEO
ON PRORRATEO."IdComprobante" = COMP_ORIGINAL."IdComprobante" 
	AND PRORRATEO."IdContratoComision" = DETALLE."IdContratoComision"
   	AND PRORRATEO."IdComprobanteComisionMensual" = DETALLE."IdComprobanteComisionMensual"
WHERE CC."IdEstado" = 'ACT' AND COMP_ORIGINAL."IdCliente" IN(:IdCliente) 
AND ( CC."IdFormatoCobro" = 'AGRRUT' OR PORTAFOLIO."CodigoPortafolio" IN(:CodigoPortafolio) )
AND COMP_ORIGINAL."FechaComprobante" BETWEEN :FechaDesde AND :FechaHasta AND COMP_ORIGINAL."IdEstadoComprobante" <> 'ANU'
AND COMP_ORIGINAL."IdClasificacionTipoInstrumento" = CASE WHEN :IdClasificacionTipoInstrumento = '' THEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" ELSE :IdClasificacionTipoInstrumento END
AND ( COMP_COMISION."IdComprobanteReferencia" = 0 AND COMP_COMISION."IdTipoComprobanteReferencia" <> 'NEO' AND COMP_COMISION."IdEstadoComprobante" <> 'ANU')
GROUP BY COMP_GENERADO."IdComprobanteComisionMensual",
		COMP_ORIGINAL."IdClasificacionTipoInstrumento",
		INSTRUMENTO."Nombre",
		COMP_ORIGINAL."IdTipoComprobante",
		COMP_ORIGINAL."IdMoneda",
		DETALLEINDICADOR."DetalleIndicador",
		COMP_ORIGINAL."NombreLargoCliente",
		COMP_ORIGINAL."DocumentoIdentidad",
		PORTAFOLIO_COMISION."CodigoPortafolio",
		CC."IdFormatoCobro",
		COMP_COMISION."NombreLargoCliente")
UNION ALL 

(SELECT 
COMP_ORIGINAL."IdClasificacionTipoInstrumento"
,INSTRUMENTO."Nombre" AS "NombreInstrumento"
,CCM."IdTipoComprobante" AS "TipoComprobante"
,COMP_ORIGINAL."IdMoneda"
,SUM(TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8)) AS "TotalCompraVenta"
,SUM(PRORRATEO."MontoProrrateadoIva") AS "TotalIVA"
,SUM(PRORRATEO."MontoProrrateadoDerecho") AS "TotalDerecho"
,SUM(PRORRATEO."MontoProrrateadoComision") AS "TotalComision"
,SUM(CASE WHEN OPERACION."Accion" = 'VENTA' THEN TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "TotalVenta" 
,SUM(CASE WHEN OPERACION."Accion" = 'COMPRA' THEN TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "TotalCompra"
,CASE WHEN CC."IdFormatoCobro" = 'AGRRUT' THEN DETALLEINDICADOR."DetalleIndicador" || '_' || COMP_ORIGINAL."DocumentoIdentidad"
|| '_' || COMP_COMISION."NombreLargoCliente" ELSE DETALLEINDICADOR."DetalleIndicador" || '_' || COMP_ORIGINAL."DocumentoIdentidad"
|| '_' || COMP_ORIGINAL."NombreLargoCliente"  || '_' || PORTAFOLIO_COMISION."CodigoPortafolio" END AS "AgruparSeccion"
FROM "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensual" AS COMP_GENERADO
LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle" AS DETALLE ON DETALLE."IdComprobanteComisionMensual" = COMP_GENERADO."IdComprobanteComisionMensual" AND DETALLE."IdMovimientoComprobanteComisionMensual" = COMP_GENERADO."IdMovimientoComprobanteComisionMensual"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroMensual" AS CCM ON CCM."IdComprobanteReferencia" = DETALLE."IdComprobante"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_ORIGINAL ON COMP_ORIGINAL."IdComprobante" = CCM."IdComprobanteReferencia"
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_COMISION ON COMP_COMISION."IdComprobante" = COMP_GENERADO."IdComprobanteComisionMensual" 
LEFT JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS CC ON CC."IdContratoComision" = DETALLE."IdContratoComision"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPERACION ON OPERACION."IdTipoOperacion" = COMP_ORIGINAL."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS INSTRUMENTO ON INSTRUMENTO."IdClasificacionTipoInstrumento" = COMP_ORIGINAL."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = COMP_ORIGINAL."IdPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO_COMISION ON PORTAFOLIO_COMISION."IdPortafolio" = COMP_COMISION."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS DETALLEINDICADOR 
	    ON DETALLEINDICADOR."CodigoTipoIndicador" = 'FAC_MODFAC' 
	    	AND DETALLEINDICADOR."Codigo" = COMP_ORIGINAL."ModPrefFact" 
	    	AND DETALLEINDICADOR."IdEmpresa" = COMP_ORIGINAL."IdEmpresa"
LEFT JOIN (
    SELECT
        DETALLE."IdContratoComision",
        DETALLE."IdComprobanteComisionMensual",
        COMP_ORIGINAL."IdComprobante",
		(COMP_ORIGINAL."MontoLiquidacion" / SUM(COMP_ORIGINAL."MontoLiquidacion") OVER (PARTITION BY DETALLE."IdContratoComision", DETALLE."IdComprobanteComisionMensual")) * COMP_GENERADO."MontoComision" AS "MontoProrrateadoComision",
    	(COMP_ORIGINAL."MontoLiquidacion" / SUM(COMP_ORIGINAL."MontoLiquidacion") OVER (PARTITION BY DETALLE."IdContratoComision", DETALLE."IdComprobanteComisionMensual")) * COMP_GENERADO."MontoDerecho" AS "MontoProrrateadoDerecho",
    	(COMP_ORIGINAL."MontoLiquidacion" / SUM(COMP_ORIGINAL."MontoLiquidacion") OVER (PARTITION BY DETALLE."IdContratoComision", DETALLE."IdComprobanteComisionMensual")) * COMP_GENERADO."MontoIva" AS "MontoProrrateadoIva"
	FROM "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle" AS DETALLE
    LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_ORIGINAL
        ON COMP_ORIGINAL."IdComprobante" = DETALLE."IdComprobante"
    LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensual" AS COMP_GENERADO
        ON COMP_GENERADO."IdComprobanteComisionMensual" = DETALLE."IdComprobanteComisionMensual"
) PRORRATEO
ON PRORRATEO."IdComprobante" = COMP_ORIGINAL."IdComprobante" 
	AND PRORRATEO."IdContratoComision" = DETALLE."IdContratoComision"
   	AND PRORRATEO."IdComprobanteComisionMensual" = DETALLE."IdComprobanteComisionMensual"
WHERE CCM."Estado" = 'NO DISPONIBLE' AND CCM."IdTipoComprobante" = 'NCE' 
AND CC."IdEstado" = 'ACT' AND COMP_ORIGINAL."IdCliente" IN(:IdCliente) 
AND ( CC."IdFormatoCobro" = 'AGRRUT' OR PORTAFOLIO."CodigoPortafolio" IN(:CodigoPortafolio) )
AND CCM."FechaComprobante" BETWEEN :FechaDesde AND :FechaHasta AND COMP_ORIGINAL."IdEstadoComprobante" <> 'ANU'
AND ( COMP_COMISION."IdComprobanteReferencia" = 0 AND COMP_COMISION."IdTipoComprobanteReferencia" <> 'NEO' AND COMP_COMISION."IdEstadoComprobante" <> 'ANU')
GROUP BY COMP_GENERADO."IdComprobanteComisionMensual", 
		COMP_ORIGINAL."IdClasificacionTipoInstrumento",
		INSTRUMENTO."Nombre",
		CCM."IdTipoComprobante",
		COMP_ORIGINAL."IdMoneda",
		DETALLEINDICADOR."DetalleIndicador",
		COMP_ORIGINAL."NombreLargoCliente",
		COMP_ORIGINAL."DocumentoIdentidad",
		PORTAFOLIO_COMISION."CodigoPortafolio",
		CC."IdFormatoCobro",
		COMP_COMISION."NombreLargoCliente")
UNION ALL 
(SELECT 
COMP_ORIGINAL."IdClasificacionTipoInstrumento"
,INSTRUMENTO."Nombre" AS "NombreInstrumento"
,COMP_ORIGINAL."IdTipoComprobante" AS "TipoComprobante"
,COMP_ORIGINAL."IdMoneda"
,SUM(DISTINCT TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8)) AS "TotalCompraVenta"
,SUM(DISTINCT CASE WHEN COBRO_DETALLE."CodigoCobroClasificacion" = 'IMPUESTO' THEN TO_DECIMAL(COBRO_DETALLE."MontoCobro", 26, 8) ELSE 0 END ) AS "TotalIVA"
,SUM(DISTINCT CASE WHEN COBRO_DETALLE."CodigoCobroClasificacion" = 'DERECHO' THEN TO_DECIMAL(COBRO_DETALLE."MontoCobro", 26, 8) ELSE 0 END ) AS "TotalDerecho"
,SUM(DISTINCT CASE WHEN COBRO_DETALLE."CodigoCobroClasificacion" = 'COMISION' THEN TO_DECIMAL(COBRO_DETALLE."MontoCobro", 26, 8) ELSE 0 END ) AS "TotalComision"
,SUM(DISTINCT CASE WHEN OPERACION."Accion" = 'VENTA' THEN TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "TotalVenta" 
,SUM(DISTINCT CASE WHEN OPERACION."Accion" = 'COMPRA' THEN TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) ELSE TO_DECIMAL(0, 26, 8) END) AS "TotalCompra"
,DETALLEINDICADOR."DetalleIndicador" || '_' || COMP_ORIGINAL."DocumentoIdentidad"
|| '_' || COMP_ORIGINAL."NombreLargoCliente"	|| '_' || PORTAFOLIO."CodigoPortafolio" AS "AgruparSeccion"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroMensual" AS COMP_GENERADO
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_ORIGINAL ON COMP_ORIGINAL."IdComprobante" = COMP_GENERADO."IdComprobante"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroMensual" COMP_NCE ON COMP_NCE."IdComprobante" = COMP_GENERADO."IdComprobanteReferencia" AND COMP_NCE."CodigoModoFacturacionComprobante"  = 'MODPREFCONV' 
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenioDetalle" COBRO_DETALLE ON COBRO_DETALLE."IdComprobante" = COMP_GENERADO."IdComprobante"
LEFT JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS CC ON CC."IdContratoComision" = CASE WHEN COMP_ORIGINAL."IdTipoComprobante" = 'NCE' THEN COMP_NCE."IdContratoComision" ELSE COMP_GENERADO."IdContratoComision" END
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPERACION ON OPERACION."IdTipoOperacion" = COMP_ORIGINAL."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS INSTRUMENTO ON INSTRUMENTO."IdClasificacionTipoInstrumento" = COMP_ORIGINAL."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = COMP_ORIGINAL."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS DETALLEINDICADOR 
	    ON DETALLEINDICADOR."CodigoTipoIndicador" = 'FAC_MODFAC' 
	    	AND DETALLEINDICADOR."Codigo" = CASE WHEN COMP_ORIGINAL."IdTipoComprobante" = 'NCE' THEN COMP_NCE."CodigoModoFacturacionComprobante"  ELSE COMP_ORIGINAL."ModPrefFact" END
	    	AND DETALLEINDICADOR."IdEmpresa" = COMP_ORIGINAL."IdEmpresa"
WHERE CC."IdEstado" = 'ACT' AND COMP_GENERADO."IdTipoContrato" = 'CONV' AND COMP_GENERADO."IdComprobanteReferencia" = CASE WHEN COMP_ORIGINAL."IdTipoComprobante" = 'NCE' THEN COMP_NCE."IdComprobante" ELSE 0 END AND
COMP_ORIGINAL."IdCliente" IN(:IdCliente) 
AND PORTAFOLIO."CodigoPortafolio" IN(:CodigoPortafolio) 
AND COMP_ORIGINAL."FechaComprobante" BETWEEN :FechaDesde AND :FechaHasta AND COMP_ORIGINAL."IdEstadoComprobante" <> 'ANU'
AND COMP_ORIGINAL."IdClasificacionTipoInstrumento" = CASE WHEN :IdClasificacionTipoInstrumento = '' THEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" ELSE :IdClasificacionTipoInstrumento END
GROUP BY COMP_GENERADO."IdComprobanteComisionMensual",
		COMP_ORIGINAL."IdClasificacionTipoInstrumento",
		INSTRUMENTO."Nombre",
		COMP_ORIGINAL."IdTipoComprobante",
		COMP_ORIGINAL."IdMoneda",
		DETALLEINDICADOR."DetalleIndicador",
		COMP_ORIGINAL."NombreLargoCliente",
		COMP_ORIGINAL."DocumentoIdentidad",
		PORTAFOLIO."CodigoPortafolio")
ORDER BY "AgruparSeccion", "TipoComprobante"