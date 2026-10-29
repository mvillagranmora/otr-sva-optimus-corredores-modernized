SELECT 
"IdSolicitudAporteRescate"
,"FolioContrato"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
WHERE "IdCliente" IN (:IdCliente)
  AND "FolioContrato" IN (:FolioContrato);