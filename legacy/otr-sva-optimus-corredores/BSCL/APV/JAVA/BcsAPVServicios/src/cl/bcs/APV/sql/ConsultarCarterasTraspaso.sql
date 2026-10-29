SELECT 
CAR."IdCliente"
,CAR."EGRCantidad" AS "Cantidad"
,CAR."PrecioCompra"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CAR
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON (CAR."IdPortafolio" = POR."IdPortafolio")
INNER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MA ON MA."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" REG ON REG."IdRegimenTributario" = MA."IdRegimenTributario"
INNER JOIN "BSCL.APV.BD::TRA.OrdenTraspaso" DTR ON (DTR."IdSolicitudAporteRescate" = CAR."EGRIdOperacion" AND CAR."IdModulo" = 'FMM') OR (DTR."IdOrden" = CAR."EGRIdOperacion" AND CAR."IdModulo" = 'FAC')
INNER JOIN "BSCL.APV.BD::TRA.Traspaso" TRA ON TRA."IdTraspaso" = DTR."IdTraspaso" AND CAR."IdCliente" = TRA."IdCliente"
WHERE CAR."IdCliente" IN (:IdCliente)
AND REG."Codigo" = 'APV-A'
AND CAR."IdClasificacionTipoInstrumento" IN ('RV','FM')
AND CAR."IdModulo" IN ('FMM', 'FAC')
AND CAR."FechaOperacion" BETWEEN :FechaDesdeAhorro AND :FechaHastaAhorro
AND CAR."EGRFechaOperacion" BETWEEN :FechaDesde AND :FechaHasta 
AND CAR."EGRCantidad" > 0
AND CAR."EGRIdOperacion" != 0
AND CAR."IdEstado" != 'ANU'
AND POR."IdEstado" <> 'INA';