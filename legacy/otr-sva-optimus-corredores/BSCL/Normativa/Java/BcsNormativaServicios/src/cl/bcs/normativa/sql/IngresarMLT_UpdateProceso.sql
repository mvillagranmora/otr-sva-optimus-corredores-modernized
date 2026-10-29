UPDATE  MLT
SET MLT."IdEstado"='ANU'
FROM "BSCL.Normativa.BD::SIS.ProcesoMandatos" MLT
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL  ON MLT."IdAgrupador" = SOL."IdAgrupador" AND SOL."TipoSolicitud" = 'RES'
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON SOL."IdSolicitudAporteRescate" = CAR."EGRIdOperacion" 
WHERE  CAR."IdClasificacionTipoInstrumento" IN ('FM')
AND CAR."IdOperacion" IN (:IdSolicitudAporteRescate) AND  CAR."EGRIdOperacion">0;