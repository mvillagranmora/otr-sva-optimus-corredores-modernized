-- Aportes Cartera
UPDATE CAR
SET 
CAR."CostoInversionEnFactorAjuste" = :Valor,
CAR."INGCostoInversionEnFactorAjuste" = (:Valor * CAR."INGCantidad")/CAR."Cantidad"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL ON SOL."IdSolicitudAporteRescate" = CAR."IdOperacion" AND SOL."TipoSolicitud" = 'APO'
WHERE 
CAR."IdClasificacionTipoInstrumento" IN ('FM')
AND SOL."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate);