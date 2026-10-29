UPDATE "BSCL.FondoMutuo.BD::FMM.InterfazAgf"
SET "IdEstado" = 'ERR',
"Observacion" = :Observacion
WHERE "IdEstado" = 'PEN' AND "IdSolicitudAporteRescate" = :IdSolicitudAporteRescateRelacionado