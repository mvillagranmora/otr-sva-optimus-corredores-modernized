UPDATE sol
SET sol."FechaLiquidacionRegistro" = CURRENT_UTCTIMESTAMP,
sol."EstadoLiquidacion" = 'LIQ',
sol."FolioCartaLiquidacion" = :FolioCartaLiquidacion
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS sol
WHERE sol."IdSolicitudAporteRescate" = :IdSolicitudAporteRescate;