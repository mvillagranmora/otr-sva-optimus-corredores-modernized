INSERT INTO #AnularRegistros_ComprobantesAnular
SELECT COM."IdComprobante", COALESCE(CDO."IdIngresoEgreso",0) AS "IdIngresoEgresoOrigen", COALESCE(CDO."IdComprobante",0) AS "IdComprobanteOrigen"
FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS COM
INNER JOIN #AnularRegistros_p_RegistrosAProcesar AS RP ON (RP."Id" = COM."IdComprobante")
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD ON (CD."IdComprobante" = COM."IdComprobante")
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO ON (CDO."IdComprobanteDetalle" = CD."IdComprobanteDetalle")
WHERE RP."Entidad" = 'TES.Comprobante';  -- El estado lo evaluá la validación