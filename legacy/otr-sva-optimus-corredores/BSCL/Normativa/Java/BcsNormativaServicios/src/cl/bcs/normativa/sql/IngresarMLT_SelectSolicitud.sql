SELECT  "IdRegimenTributario"
FROM  "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
WHERE "IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate)
AND "IdEmpresa"=:IdEmpresa;