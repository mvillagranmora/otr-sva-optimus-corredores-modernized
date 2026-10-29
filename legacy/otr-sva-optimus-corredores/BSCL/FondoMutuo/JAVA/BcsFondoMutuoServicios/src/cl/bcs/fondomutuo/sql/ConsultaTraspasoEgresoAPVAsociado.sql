SELECT Traspaso."IdTraspaso", Solicitud."FolioContrato"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS Solicitud
INNER JOIN "BSCL.APV.BD::TRA.Traspaso" AS Traspaso 
ON Traspaso."IdTraspaso" = Solicitud."FolioContrato"
WHERE Solicitud."IdCliente" IN (:IdCliente)
 AND Solicitud."FolioContrato" IN (:FolioContrato)
 AND Solicitud."IdTipoMovimientoFondoMutuo"= 'RESTRASP'
GROUP BY Traspaso."IdTraspaso", Solicitud."FolioContrato"