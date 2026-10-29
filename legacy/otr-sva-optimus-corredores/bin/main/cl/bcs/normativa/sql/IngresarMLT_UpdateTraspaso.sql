UPDATE  TR SET TR."FolioMLT"=:FolioMandato
FROM "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino" DT 
INNER JOIN "BSCL.FondoMutuo.BD::TRA.Traspaso"  				TR ON (TR."IdTraspaso"=DT."IdTraspaso")
WHERE DT."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate);