UPDATE "BSCL.Tesoreria.BD::TES.Comprobante"
SET "IdEstado" = 'ING'
WHERE "IdEstado" = 'PDA' AND "IdComprobante" = :IdComprobante