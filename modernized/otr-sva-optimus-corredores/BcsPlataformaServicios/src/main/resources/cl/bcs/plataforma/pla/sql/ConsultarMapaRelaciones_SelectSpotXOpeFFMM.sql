SELECT SolAporRes."IdSolicitudAporteRescate",
       OpeSpot."IdOperacionSpot"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MvtoCmbte
  INNER JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS OpeSpot
      ON OpeSpot."IdOperacionSpot" = MvtoCmbte."FolioTransaccion"
  INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SolAporRes 
      ON SolAporRes."IdOperacionSpot" = OpeSpot."IdOperacionSpot"