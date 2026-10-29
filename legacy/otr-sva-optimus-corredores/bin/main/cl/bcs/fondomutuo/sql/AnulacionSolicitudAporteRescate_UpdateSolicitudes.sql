UPDATE "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
SET "Estado" = :IdEstadoNuevo,
"EstadoLiquidacion" = 'ING',
"FolioCartaLiquidacion" = 0,
"FechaAnulacionRegistro" = CURRENT_UTCTIMESTAMP,
"FechaAnulacion" = :FechaAnulacion
WHERE "IdSolicitudAporteRescate" = :IdSolicitudAporteRescate