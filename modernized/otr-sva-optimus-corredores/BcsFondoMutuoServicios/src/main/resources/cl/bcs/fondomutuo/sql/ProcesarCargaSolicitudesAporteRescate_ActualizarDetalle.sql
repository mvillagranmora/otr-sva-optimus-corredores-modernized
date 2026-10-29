UPDATE "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate"
SET "EstadoProceso" = :EstadoProceso,
"DescEstadoProceso" = :Codigo,
"IdSolicitudAporteRescate" = :IdSolicitudAporteRescate
WHERE "IdDetalleCargaSolicitudesAporteRescate" = :IdDetalleCarga