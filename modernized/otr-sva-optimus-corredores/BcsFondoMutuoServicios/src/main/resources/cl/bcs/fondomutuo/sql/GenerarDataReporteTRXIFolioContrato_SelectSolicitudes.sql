SELECT 
"IdSolicitudAporteRescate"
,"FolioContrato"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
WHERE "FolioContrato" IN (:FolioContrato);