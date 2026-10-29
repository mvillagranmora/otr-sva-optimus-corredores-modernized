SELECT "IdComprobante", "IdTipoMovimiento", 'CLI' AS "AfectaCuentaInversion" FROM "BSCL.Tesoreria.BD::TES.Comprobante" WHERE "IdComprobante" IN (:idsComprobantes)
