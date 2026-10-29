SELECT "IdTransaccion", "Folio", "Nemotecnico", "Cantidad", "Precio", "Monto", "FechaSistemaCreacion", "FechaLiquidacion", "IdCamara", "EsBilateral"
FROM "BSCL.Orden.BD::ORD.Transaccion" 
WHERE "IdEmpresa" = :IdEmpresa AND "FechaSistemaCreacion" = :FechaSistemaCreacion 
AND (:FolioTransaccion = '' OR "Folio" = :FolioTransaccion);