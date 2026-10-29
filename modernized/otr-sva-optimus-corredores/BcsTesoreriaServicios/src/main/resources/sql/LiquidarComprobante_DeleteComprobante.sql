
--------------------------------------------------------
-- Elimina los registros invalidos de TablaComprobante
--------------------------------------------------------

DELETE
FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobante"
WHERE "IdComprobanteInformado" = :IdComprobante;