
-----------------------------------------------------------------------
-- Se eliminan los registros invalidos de TablaComprobanteDetalle
-----------------------------------------------------------------------

DELETE
FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobanteDetalle"
WHERE "IdComprobanteInformado" = :IdComprobante;

