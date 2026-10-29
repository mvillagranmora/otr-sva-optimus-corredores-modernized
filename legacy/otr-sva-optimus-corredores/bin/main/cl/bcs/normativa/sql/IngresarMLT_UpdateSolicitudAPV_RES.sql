 Rescates solicitud
UPDATE SOL
SET SOL."RentabilidadReal" = ((SOL."MontoMonedaSistema"/PAR."Valor") - CAR."EGRMontoCompraFactor")
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
INNER JOIN (SELECT SUM("EGRMontoCompraFactor") AS "EGRMontoCompraFactor","EGRIdOperacion","IdOperacion" FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "EGRIdOperacion" != 0 AND "IdClasificacionTipoInstrumento" = 'FM' GROUP BY "EGRIdOperacion","IdOperacion") CAR ON CAR."EGRIdOperacion" = SOL."IdSolicitudAporteRescate"
INNER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON (SUBSTRING(SOL."FechaValorCuota",0,7) = PAR."AgnoMes")
WHERE SOL."TipoSolicitud" = 'RES' AND PAR."IdParametroFinanciero" = 'UTM'
AND CAR."IdOperacion" IN (:IdSolicitudAporteRescate);