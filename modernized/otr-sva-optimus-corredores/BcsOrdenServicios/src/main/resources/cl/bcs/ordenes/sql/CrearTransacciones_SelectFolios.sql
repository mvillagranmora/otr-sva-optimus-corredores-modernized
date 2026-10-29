SELECT T."IdTransaccion", (T."Folio" || ' ' || TO_NVARCHAR(T."FechaSistemaCreacion")) AS "FolioFecha"
FROM "BSCL.Orden.BD::ORD.Transaccion" T
INNER JOIN #TEMP_FOLIOS TF ON
    (T."Folio" || ' ' || TO_NVARCHAR(t."FechaSistemaCreacion")) = TF."FolioFecha"
WHERE T."IdEmpresa" = :idEmpresa
AND T."IdEstado" != 'ANU';