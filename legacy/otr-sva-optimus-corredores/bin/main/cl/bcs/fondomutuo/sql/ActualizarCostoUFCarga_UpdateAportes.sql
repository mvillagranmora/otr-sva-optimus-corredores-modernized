UPDATE SOL
SET SOL."CostoUF" = CAR."CostoInversionEnFactorAjuste"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."IdOperacion" = SOL."IdSolicitudAporteRescate" AND CAR."IdClasificacionTipoInstrumento" = 'FM'
WHERE SOL."TipoSolicitud" = 'APO'