SELECT DISTINCT OT."IdSolicitudAporteRescate"
FROM  "BSCL.APV.BD::TRA.OrdenTraspaso" AS OT
INNER JOIN "BSCL.APV.BD::TRA.Traspaso" AS T 
ON OT."IdTraspaso" = T."IdTraspaso"
AND T."RazonRetiro" != 'RES'
WHERE OT."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate)