- Rescates solicitud relacionado
UPDATE CAR
SET CAR."EGRMontoCompraFactor" = (CAR."EGRCantidad"/CAR."Cantidad") * CAR."MontoCompraFactor"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL ON SOL."IdSolicitudAporteRescate" = CAR."EGRIdOperacion" AND SOL."TipoSolicitud" = 'RES'
INNER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON (SUBSTRING(SOL."FechaValorCuota",0,7) = PAR."AgnoMes")
WHERE CAR."IdClasificacionTipoInstrumento" IN ('FM') 
AND CAR."IdOperacion" IN (:IdSolicitudAporteRescate);