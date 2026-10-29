
----------------------------------------------------------------------------
-- Se eliminan los registros invalidos de TablaComprobanteFormaDePago
----------------------------------------------------------------------------

DELETE
FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobanteFormaDePago"
WHERE "IdComprobanteInformado" = :IdComprobante;

