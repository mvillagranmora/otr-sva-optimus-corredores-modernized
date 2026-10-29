SELECT IE."IdComprobanteOrigen", SOL."IdSolicitudAporteRescate"
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL ON (IE."IdComprobanteOrigen" = SOL."IdSolicitudAporteRescate")
WHERE (sol."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate) OR sol."IdSolicitudAporteRescateRelacionado" IN (:IdSolicitudAporteRescate)) AND IE."IdTipoProducto" = 'INFM' AND IE."IdEstado" != 'ANU' AND IE."IdTipoComprobanteOrigen" = 'FMM' AND SOL."IdMedioDePago" != 'CIN';