SELECT 
CAR."IdCliente"
,CAR."EGRCantidad" AS "Cantidad"
,CAR."PrecioCompra"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON (CAR."IdPortafolio" = POR."IdPortafolio")
INNER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MA ON MA."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" REG ON REG."IdRegimenTributario" = MA."IdRegimenTributario"
WHERE CAR."IdCliente" IN (:IdCliente)
AND REG."Codigo" = 'APV-A'
AND CAR."IdClasificacionTipoInstrumento" IN ('RV','FM')
AND CAR."FechaOperacion" BETWEEN :FechaDesdeAhorro AND :FechaHastaAhorro
AND CAR."EGRFechaOperacion" BETWEEN :FechaDesdeAhorro AND :FechaHasta 
AND CAR."EGRCantidad" > 0
AND CAR."EGRIdOperacion" != 0
AND NOT EXISTS (SELECT 1 FROM "BSCL.APV.BD::TRA.OrdenTraspaso" DTR INNER JOIN "BSCL.APV.BD::TRA.Traspaso" TRA ON TRA."IdTraspaso" = DTR."IdTraspaso" WHERE ((DTR."IdSolicitudAporteRescate" = CAR."EGRIdOperacion" AND CAR."IdModulo" = 'FMM') OR (DTR."IdOrden" = CAR."EGRIdOperacion" AND CAR."IdModulo" = 'FAC')) AND CAR."IdCliente" = TRA."IdCliente" AND CAR."IdModulo" IN ('FMM', 'FAC'))
AND CAR."IdEstado" != 'ANU'
AND POR."IdEstado" <> 'INA'
AND CAR."EGRIdConcepto" NOT IN ('RESFUS','RESREI') 
AND NOT(CAR."INGIdConcepto" IN ('APOFUS','APOREI') AND CAR."EGRIdConcepto" = '');