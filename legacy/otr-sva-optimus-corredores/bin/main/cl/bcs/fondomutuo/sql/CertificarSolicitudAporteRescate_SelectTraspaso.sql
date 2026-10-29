SELECT
  TRASPASO."IdTraspaso"
 ,ORIGEN."IdDetalleTraspaso"
 ,ORIGEN."IdTraspaso"
 ,ORIGEN."IdSerieFondo"
 ,ORIGEN."IdSolicitudAporteRescate"
FROM "BSCL.FondoMutuo.BD::TRA.Traspaso" AS TRASPASO
INNER JOIN "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen" 		AS ORIGEN			ON  ( TRASPASO."IdTraspaso" 	=  ORIGEN."IdTraspaso")
WHERE TRASPASO."IdEmpresa"=:IdEmpresa AND ORIGEN."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate) AND TRASPASO."IdEstado"='VIG';