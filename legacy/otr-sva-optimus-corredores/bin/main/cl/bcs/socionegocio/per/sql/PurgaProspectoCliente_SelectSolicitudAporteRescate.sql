SELECT "IdPortafolio", "IdSolicitudAporteRescate" 
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 
WHERE "IdPortafolio" IN (:IdsPortafolio);