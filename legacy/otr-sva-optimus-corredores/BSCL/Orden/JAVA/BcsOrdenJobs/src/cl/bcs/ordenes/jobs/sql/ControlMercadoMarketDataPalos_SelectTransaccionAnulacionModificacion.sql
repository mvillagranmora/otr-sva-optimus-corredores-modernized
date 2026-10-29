SELECT "IdTransaccionHistorico", "IdEstadoRegistro", "Folio"
    FROM (
        SELECT
            "IdTransaccionHistorico", "IdEstadoRegistro", "Folio",
            ROW_NUMBER() OVER (
                PARTITION BY "Folio", "IdTipoOperacion", "FechaSistemaCreacion"
                ORDER BY "IdTransaccionHistorico" DESC
            ) rn
        FROM "BSCL.Orden.BD::HIS.Transaccion"
        WHERE "Folio" IN (:Folio)
          AND "TipoRegistro" = 'ANU'
          AND "EsModificacionMarketData" = 'S'
    ) x
WHERE rn = 1;