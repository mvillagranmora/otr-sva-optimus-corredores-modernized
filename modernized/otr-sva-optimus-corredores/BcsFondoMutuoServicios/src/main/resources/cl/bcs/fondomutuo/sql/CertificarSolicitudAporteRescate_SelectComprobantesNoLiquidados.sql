SELECT 
i."IdComprobanteOrigen" AS "IdSolicitudAporteRescate",
i."IdComprobante" AS "IdComprobante"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarPorComprobanteOrigenLiquidado" i 
WHERE i."IdComprobanteOrigen" IN (:IdSolicitudAporteRescate) AND i."IdTipoComprobanteOrigen" = 'FMM' AND i."IdEstado" != 'LIQ'
