SELECT 
"IdSolicitudAporteRescate"
,"IdCliente"
,"IdPortafolio"
,"IdMoneda"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
WHERE "IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate) AND "IdEmpresa" = :IdEmpresa;