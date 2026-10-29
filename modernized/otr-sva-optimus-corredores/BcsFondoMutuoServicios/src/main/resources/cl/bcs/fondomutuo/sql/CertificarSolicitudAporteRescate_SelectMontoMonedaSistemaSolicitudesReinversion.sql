SELECT
"IdSolicitudAporteRescateRelacionado" AS "IdSolicitudAporteRescate"
,SUM("MontoMonedaSistema") AS "MontoMonedaSistema"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
WHERE "IdSolicitudAporteRescateRelacionado" IN (:IdSolicitudAporteRescateRelacionado)
GROUP BY "IdSolicitudAporteRescateRelacionado";