UPDATE SOL
SET SOL."CostoUF" = CAR."EGRCostoInversionEnFactorAjuste"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
INNER JOIN (SELECT SUM("EGRCostoInversionEnFactorAjuste") AS "EGRCostoInversionEnFactorAjuste","EGRIdOperacion" FROM "BSCL.Custodia.BD::CUS.Cartera" WHERE "EGRIdOperacion" != 0 AND "IdClasificacionTipoInstrumento" = 'FM' GROUP BY "EGRIdOperacion") CAR ON CAR."EGRIdOperacion" = SOL."IdSolicitudAporteRescate"
WHERE SOL."TipoSolicitud" = 'RES'