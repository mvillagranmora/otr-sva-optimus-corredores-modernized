-– cartera Aportes actualiza rentabilidad ing x usuario
UPDATE CAR
SET CAR."MontoCompraFactor" = :Valor
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL ON SOL."IdSolicitudAporteRescate" = CAR."IdOperacion" AND SOL."TipoSolicitud" = 'APO'
WHERE PAR."IdParametroFinanciero" = 'UTM' AND CAR."IdClasificacionTipoInstrumento" IN ('FM')
AND SOL."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate);