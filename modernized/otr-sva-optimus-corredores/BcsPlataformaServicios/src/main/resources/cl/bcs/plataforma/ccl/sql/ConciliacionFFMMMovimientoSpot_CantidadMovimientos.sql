SELECT 
SOL."IdSolicitudAporteRescate"
,COUNT(OPE."IdOperacionSpotOrigen") AS "CantidadOperacionSpot"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" OPE ON OPE."IdOperacionSpotOrigen" = SOL."IdOperacionSpot"
WHERE SOL."IdCarga" = 0 AND SOL."IdMonedaPago" != '' AND SOL."IdMonedaPago" != SOL."IdMoneda" AND OPE."Estado" NOT IN ('MOD','ANU') AND SOL."Estado" != 'ANU'
GROUP BY SOL."IdSolicitudAporteRescate"
HAVING COUNT(1) > 1
UNION ALL
SELECT 
SOL."IdSolicitudAporteRescate"
,TO_BIGINT(0) AS "CantidadOperacionSpot"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" OPE ON OPE."IdOperacionSpotOrigen" = SOL."IdOperacionSpot" AND OPE."Estado" NOT IN ('MOD','ANU')
WHERE SOL."IdCarga" = 0 AND SOL."IdMonedaPago" != '' AND SOL."IdMonedaPago" != SOL."IdMoneda" AND OPE."IdOperacionSpotOrigen" IS NULL AND SOL."TipoSolicitud" = 'APO' AND SOL."Estado" != 'ANU'
UNION ALL 
SELECT 
SOL."IdSolicitudAporteRescate"
,TO_BIGINT(0) AS "CantidadOperacionSpot"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" OPE ON OPE."IdOperacionSpotOrigen" = SOL."IdOperacionSpot" AND OPE."Estado" NOT IN ('MOD','ANU')
WHERE SOL."IdCarga" = 0 AND SOL."IdMonedaPago" != '' AND SOL."IdMonedaPago" != SOL."IdMoneda" AND OPE."IdOperacionSpotOrigen" IS NULL AND SOL."TipoSolicitud" = 'RES' AND SOL."Estado" = 'CRT'
UNION ALL
SELECT 
SOL."IdSolicitudAporteRescate"
,TO_BIGINT(0) AS "CantidadOperacionSpot"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" OPE ON OPE."IdOperacionSpotOrigen" = SOL."IdOperacionSpot"
WHERE SOL."IdCarga" = 0 AND SOL."IdMonedaPago" != '' AND SOL."IdMonedaPago" != SOL."IdMoneda" AND OPE."Estado" NOT IN ('MOD','ANU') AND SOL."Estado" = 'ANU';