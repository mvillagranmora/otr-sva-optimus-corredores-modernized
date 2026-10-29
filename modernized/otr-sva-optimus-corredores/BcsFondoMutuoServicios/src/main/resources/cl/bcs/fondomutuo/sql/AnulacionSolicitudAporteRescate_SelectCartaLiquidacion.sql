SELECT IE."IdComprobanteOrigen", SOL."IdSolicitudAporteRescate"
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL ON (IE."IdComprobanteOrigen" = SOL."FolioCartaLiquidacion")
WHERE (SOL."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate) OR SOL."IdSolicitudAporteRescateRelacionado" IN (:IdSolicitudAporteRescate)) AND IE."IdTipoProducto" = 'INFM' AND IE."IdEstado" != 'ANU' AND IE."IdTipoComprobanteOrigen" = 'LFM' AND SOL."FolioCartaLiquidacion" != 0;