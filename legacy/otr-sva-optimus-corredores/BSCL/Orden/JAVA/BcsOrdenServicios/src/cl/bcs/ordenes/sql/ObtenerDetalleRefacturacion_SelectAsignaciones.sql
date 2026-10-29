SELECT asig."IdTransaccion", 
       asig."IdOrden",
       asig."IdAsignacion",
       asig."CantidadAsignada",
       asig."MontoAsignado",
       asig."ValorRescateAsignado",
       asig."FechaAsignacion",
       asig."IdMovimientoComprobante",
       CbmteMvto."IdEstadoComprobante",
       asig."IdEstadoAsignacion", 
       Coalesce(Trans."IdMoneda", 'PEN') AS "IdMonedaOperacion",
       Coalesce(Trans."IdTipoInstrumento", '') AS "IdTipoInstrumento",
       Coalesce(insNeg."Nemotecnico",'') AS "InstrumentoNegociacion"
FROM "BSCL.Orden.BD::ORD.Asignacion" asig
  LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS Trans
    ON Trans."IdTransaccion" = asig."IdAsignacion"
  LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS Ord
    ON Ord."IdOrden" = asig."IdOrden"
  LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS insNeg
    ON insNeg."IdInstrumento" = Ord."IdInstrumentoNegociacion"
  LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS Mvto
    ON Mvto."IdAsignacion" = asig."IdAsignacion"
  LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS CbmteMvto
    ON CbmteMvto."IdComprobante" = Mvto."IdComprobante"  
WHERE asig."IdAsignacion" IN (:idsAsignacion)
ORDER BY asig."IdAsignacion"