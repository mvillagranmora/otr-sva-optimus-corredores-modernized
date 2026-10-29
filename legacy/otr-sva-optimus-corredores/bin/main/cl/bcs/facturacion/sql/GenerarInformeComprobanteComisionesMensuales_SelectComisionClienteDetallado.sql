(SELECT 
DETALLE."IdComprobanteComisionMensualDetalle"
,COMP_ORIGINAL."IdCliente"
,PORTAFOLIO."CodigoPortafolio"
,PORTAFOLIO_COMISION."CodigoPortafolio" AS "CodigoPortafolioComision"
,COMP_ORIGINAL."DocumentoIdentidad" AS "RutCliente"
,COMP_COMISION."NombreLargoCliente" AS "NombreCliente"
,DETALLE."IdComprobante" AS "NumeroComprobante"
,COMP_ORIGINAL."FolioComprobante" AS "FolioDte"
,COMP_ORIGINAL."IdTipoComprobante" AS "TipoComprobante"
,COMP_ORIGINAL."IdClasificacionTipoInstrumento"
,TO_VARCHAR(COMP_ORIGINAL."FechaComprobante",'DD-MM-YYYY') AS "FechaComprobante"
,DETALLEINDICADOR."DetalleIndicador" AS "TipoContrato"
,COMP_ORIGINAL."IdTipoOperacion"
,TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) AS "MontoLiquidacion"
,(CASE WHEN DETALLEINDICADOR."DetalleIndicador" = 'CONVENIO' THEN TO_DECIMAL(COBROCONVENIO."MontoComision", 28, 8) ELSE TO_DECIMAL(0, 28, 8) END) AS "MontoComision"
,(CASE WHEN DETALLEINDICADOR."DetalleIndicador" = 'CONVENIO' THEN TO_DECIMAL(COBROCONVENIO."MontoDerecho", 28, 8) ELSE TO_DECIMAL(0, 28, 8) END) AS "MontoDerecho"
,TOPER."Accion"
,COMP_ORIGINAL."IdMoneda"
,(CASE WHEN CC."IdFormatoCobro" = 'AGRRUT' THEN 'A' ELSE 'D' END) AS "FormatoCobro" 
,CASE WHEN CC."IdFormatoCobro" = 'AGRRUT' THEN DETALLEINDICADOR."DetalleIndicador" || '_' || COMP_ORIGINAL."DocumentoIdentidad"
|| '_' || COMP_COMISION."NombreLargoCliente" ELSE DETALLEINDICADOR."DetalleIndicador" || '_' || COMP_ORIGINAL."DocumentoIdentidad"
|| '_' || COMP_ORIGINAL."NombreLargoCliente"  || '_' || PORTAFOLIO_COMISION."CodigoPortafolio" END AS "AgruparSeccion"
FROM "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle" AS DETALLE
LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensual" AS COMISION ON COMISION."IdMovimientoComprobanteComisionMensual" = DETALLE."IdMovimientoComprobanteComisionMensual" 
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_ORIGINAL ON COMP_ORIGINAL."IdComprobante" = DETALLE."IdComprobante"
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_COMISION ON COMP_COMISION."IdComprobante" = COMISION."IdComprobanteComisionMensual" 
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenio" AS COBROCONVENIO ON COBROCONVENIO."IdComprobante" = COMP_ORIGINAL."IdComprobante"
INNER JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS CC ON CC."IdContratoComision" = DETALLE."IdContratoComision"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = COMP_ORIGINAL."IdPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO_COMISION ON PORTAFOLIO_COMISION."IdPortafolio" = COMP_COMISION."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPER ON TOPER."IdTipoOperacion" = COMP_ORIGINAL."IdTipoOperacion" 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS DETALLEINDICADOR 
	    ON DETALLEINDICADOR."CodigoTipoIndicador" = 'FAC_MODFAC' 
	    	AND DETALLEINDICADOR."Codigo" = COMP_ORIGINAL."ModPrefFact" 
	    	AND DETALLEINDICADOR."IdEmpresa" = COMP_ORIGINAL."IdEmpresa"
WHERE CC."IdEstado" = 'ACT' AND COMP_ORIGINAL."IdCliente" = CASE WHEN :IdCliente = 0 THEN COMP_ORIGINAL."IdCliente" ELSE :IdCliente END 
AND ( CC."IdFormatoCobro" = 'AGRRUT' OR COMP_ORIGINAL."IdPortafolio" = CASE WHEN :IdPortafolio = 0  THEN COMP_ORIGINAL."IdPortafolio" ELSE :IdPortafolio END )
AND COMP_ORIGINAL."IdClasificacionTipoInstrumento" = CASE WHEN :IdClasificacionTipoInstrumento = '' THEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" ELSE :IdClasificacionTipoInstrumento END
AND ((:IdSegmentoCliente = 0 AND (CC."IdSegmentoCliente" IS NULL OR CC."IdSegmentoCliente" IS NOT NULL))
		OR :IdSegmentoCliente != 0 AND CC."IdSegmentoCliente" = :IdSegmentoCliente)
AND COMP_ORIGINAL."FechaComprobante" BETWEEN :FechaDesde AND :FechaHasta AND COMP_ORIGINAL."IdEstadoComprobante" <> 'ANU'
AND ( COMP_COMISION."IdComprobanteReferencia" = 0 AND COMP_COMISION."IdTipoComprobanteReferencia" <> 'NEO' AND COMP_COMISION."IdEstadoComprobante" <> 'ANU')
)
UNION ALL 

(SELECT 
DETALLE."IdComprobanteComisionMensualDetalle"
,CCM."IdCliente"
,CCM."CodigoPortafolio"
,PORTAFOLIO_COMISION."CodigoPortafolio" AS "CodigoPortafolioComision"
,CCM."DocumentoIdentidad" AS "RutCliente"
,COMP_COMISION."NombreLargoCliente" AS "NombreCliente"
,CCM."IdComprobante" AS "NumeroComprobante"
,CCM."FolioComprobante" AS "FolioDte"
,CCM."IdTipoComprobante" AS "TipoComprobante"
,CCM."IdClasificacionTipoInstrumento"
,TO_VARCHAR(CCM."FechaComprobante",'DD-MM-YYYY') AS "FechaComprobante"
,DETALLEINDICADOR."DetalleIndicador" AS "TipoContrato"
,COMP_ORIGINAL."IdTipoOperacion"
,TO_DECIMAL(CCM."MontoLiquidacion", 26, 8) AS "MontoLiquidacion"
,(CASE WHEN DETALLEINDICADOR."DetalleIndicador" = 'CONVENIO' THEN TO_DECIMAL(COBROCONVENIO."MontoComision", 28, 8) ELSE TO_DECIMAL(0, 28, 8) END) AS "MontoComision"
,(CASE WHEN DETALLEINDICADOR."DetalleIndicador" = 'CONVENIO' THEN TO_DECIMAL(COBROCONVENIO."MontoDerecho", 28, 8) ELSE TO_DECIMAL(0, 28, 8) END) AS "MontoDerecho"
,TOPER."Accion"
,CCM."IdMoneda"
,(CASE WHEN CC."IdFormatoCobro" = 'AGRRUT' THEN 'A' ELSE 'D' END) AS "FormatoCobro" 
,CASE WHEN CC."IdFormatoCobro" = 'AGRRUT' THEN DETALLEINDICADOR."DetalleIndicador" || '_' || COMP_ORIGINAL."DocumentoIdentidad"
|| '_' || COMP_COMISION."NombreLargoCliente" ELSE DETALLEINDICADOR."DetalleIndicador" || '_' || COMP_ORIGINAL."DocumentoIdentidad"
|| '_' || COMP_ORIGINAL."NombreLargoCliente"  || '_' || PORTAFOLIO_COMISION."CodigoPortafolio" END AS "AgruparSeccion"
FROM "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle" AS DETALLE
LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensual" AS COMISION ON COMISION."IdMovimientoComprobanteComisionMensual" = DETALLE."IdMovimientoComprobanteComisionMensual" 
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroMensual" AS CCM ON CCM."IdComprobanteReferencia" = DETALLE."IdComprobante"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_ORIGINAL ON COMP_ORIGINAL."IdComprobante" = CCM."IdComprobanteReferencia"
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_COMISION ON COMP_COMISION."IdComprobante" = COMISION."IdComprobanteComisionMensual" 
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenio" AS COBROCONVENIO ON COBROCONVENIO."IdComprobante" = COMP_ORIGINAL."IdComprobante"
LEFT JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS CC ON CC."IdContratoComision" = DETALLE."IdContratoComision"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO_COMISION ON PORTAFOLIO_COMISION."IdPortafolio" = COMP_COMISION."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPER ON TOPER."IdTipoOperacion" = COMP_ORIGINAL."IdTipoOperacion" 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS DETALLEINDICADOR 
	    ON DETALLEINDICADOR."CodigoTipoIndicador" = 'FAC_MODFAC' 
	    	AND DETALLEINDICADOR."Codigo" = COMP_ORIGINAL."ModPrefFact" 
	    	AND DETALLEINDICADOR."IdEmpresa" = COMP_ORIGINAL."IdEmpresa"
WHERE CCM."Estado" = 'NO DISPONIBLE' AND CCM."IdTipoComprobante" = 'NCE'
AND CCM."IdCliente" = CASE WHEN :IdCliente = 0 THEN CCM."IdCliente" ELSE :IdCliente END 
AND ( CC."IdFormatoCobro" = 'AGRRUT' OR COMP_ORIGINAL."IdPortafolio" = CASE WHEN :IdPortafolio = 0  THEN COMP_ORIGINAL."IdPortafolio" ELSE :IdPortafolio END )
AND CCM."IdClasificacionTipoInstrumento" = CASE WHEN :IdClasificacionTipoInstrumento = '' THEN CCM."IdClasificacionTipoInstrumento" ELSE :IdClasificacionTipoInstrumento END
AND ((:IdSegmentoCliente = 0 AND (CC."IdSegmentoCliente" IS NULL OR CC."IdSegmentoCliente" IS NOT NULL))
		OR :IdSegmentoCliente != 0 AND CC."IdSegmentoCliente" = :IdSegmentoCliente)
AND CCM."FechaComprobante" BETWEEN :FechaDesde AND :FechaHasta AND COMP_ORIGINAL."IdEstadoComprobante" <> 'ANU'
AND ( COMP_COMISION."IdComprobanteReferencia" = 0 AND COMP_COMISION."IdTipoComprobanteReferencia" <> 'NEO' AND COMP_COMISION."IdEstadoComprobante" <> 'ANU')
)
UNION ALL 

(SELECT 
DETALLE."IdComprobante" AS "IdComprobanteComisionMensualDetalle"
,COMP_ORIGINAL."IdCliente"
,PORTAFOLIO."CodigoPortafolio"
,PORTAFOLIO_COMISION."CodigoPortafolio" AS "CodigoPortafolioComision"
,COMP_ORIGINAL."DocumentoIdentidad" AS "RutCliente"
,COMP_ORIGINAL."NombreLargoCliente" AS "NombreCliente"
,DETALLE."IdComprobante" AS "NumeroComprobante"
,COMP_ORIGINAL."FolioComprobante" AS "FolioDte"
,COMP_ORIGINAL."IdTipoComprobante" AS "TipoComprobante"
,COMP_ORIGINAL."IdClasificacionTipoInstrumento"
,TO_VARCHAR(COMP_ORIGINAL."FechaComprobante",'DD-MM-YYYY') AS "FechaComprobante"
,DETALLEINDICADOR."DetalleIndicador" AS "TipoContrato"
,COMP_ORIGINAL."IdTipoOperacion"
,TO_DECIMAL(COMP_ORIGINAL."MontoLiquidacion", 26, 8) AS "MontoLiquidacion"
,(CASE WHEN DETALLEINDICADOR."DetalleIndicador" = 'CONVENIO' THEN COALESCE(TO_DECIMAL(COBROCONVENIO."MontoComision", 28, 8), 0) ELSE TO_DECIMAL(0, 28, 8) END) AS "MontoComision"
,(CASE WHEN DETALLEINDICADOR."DetalleIndicador" = 'CONVENIO' THEN COALESCE(TO_DECIMAL(COBROCONVENIO."MontoDerecho", 28, 8), 0) ELSE TO_DECIMAL(0, 28, 8) END) AS "MontoDerecho"
,TOPER."Accion"
,COMP_ORIGINAL."IdMoneda"
,'' AS "FormatoCobro" 
,DETALLEINDICADOR."DetalleIndicador" || '_' || COMP_ORIGINAL."DocumentoIdentidad"
|| '_' || COMP_ORIGINAL."NombreLargoCliente"	|| '_' || PORTAFOLIO."CodigoPortafolio" AS "AgruparSeccion"	-- Campos para agrupar por seccion en hoja de Excel
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroMensual" AS DETALLE
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COMP_ORIGINAL ON COMP_ORIGINAL."IdComprobante" = DETALLE."IdComprobante"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobantesCobroMensual" COMP_NCE ON COMP_NCE."IdComprobante" = DETALLE."IdComprobanteReferencia" AND COMP_NCE."CodigoModoFacturacionComprobante"  = 'MODPREFCONV'
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarFacturacionCobroConvenio" AS COBROCONVENIO ON COBROCONVENIO."IdComprobante" = COMP_ORIGINAL."IdComprobante"
LEFT JOIN "BSCL.Plataforma.BD::COM.ContratoComision" AS CC ON CC."IdContratoComision" = CASE WHEN COMP_ORIGINAL."IdTipoComprobante" = 'NCE' THEN COMP_NCE."IdContratoComision" ELSE DETALLE."IdContratoComision" END
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO ON PORTAFOLIO."IdPortafolio" = COMP_ORIGINAL."IdPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO_COMISION ON PORTAFOLIO."IdPortafolio" = CC."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPER ON TOPER."IdTipoOperacion" = COMP_ORIGINAL."IdTipoOperacion" 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" AS DETALLEINDICADOR 
	    ON DETALLEINDICADOR."CodigoTipoIndicador" = 'FAC_MODFAC' 
	    	AND DETALLEINDICADOR."Codigo" = CASE WHEN COMP_ORIGINAL."IdTipoComprobante" = 'NCE' THEN COMP_NCE."CodigoModoFacturacionComprobante"  ELSE COMP_ORIGINAL."ModPrefFact" END
	    	AND DETALLEINDICADOR."IdEmpresa" = COMP_ORIGINAL."IdEmpresa"
WHERE CC."IdEstado" = 'ACT' AND DETALLE."IdTipoContrato" = 'CONV' AND DETALLE."IdComprobanteReferencia" = CASE WHEN COMP_ORIGINAL."IdTipoComprobante" = 'NCE' THEN COMP_NCE."IdComprobante" ELSE 0 END AND
COMP_ORIGINAL."IdCliente" = CASE WHEN :IdCliente = 0 THEN COMP_ORIGINAL."IdCliente" ELSE :IdCliente END 
AND COMP_ORIGINAL."IdPortafolio" = CASE WHEN :IdPortafolio = 0 THEN COMP_ORIGINAL."IdPortafolio" ELSE :IdPortafolio END 
AND COMP_ORIGINAL."IdClasificacionTipoInstrumento" = CASE WHEN :IdClasificacionTipoInstrumento = '' THEN COMP_ORIGINAL."IdClasificacionTipoInstrumento" ELSE :IdClasificacionTipoInstrumento END
AND ((:IdSegmentoCliente = 0 AND (CC."IdSegmentoCliente" IS NULL OR CC."IdSegmentoCliente" IS NOT NULL))
		OR :IdSegmentoCliente != 0 AND CC."IdSegmentoCliente" = :IdSegmentoCliente)
AND COMP_ORIGINAL."FechaComprobante" BETWEEN :FechaDesde AND :FechaHasta AND COMP_ORIGINAL."IdEstadoComprobante" <> 'ANU')



ORDER BY "AgruparSeccion", "IdComprobanteComisionMensualDetalle", "NumeroComprobante" 