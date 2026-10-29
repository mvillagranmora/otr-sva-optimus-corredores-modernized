UPDATE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro"
SET "IdEstado" = 'ANU'
WHERE "IdMovimientoComprobanteCobro" IN (:IdMovimientoComprobanteCobro);