
-------------------------------------------------------------------------
-- Obtiene los datos de las cuentas bancarias para actualizar saldos
-------------------------------------------------------------------------

SELECT DISTINCT "IdCuentaBancariaCorredora" as "IdCuentaBanco"
FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
INNER JOIN "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobante" AS CAL ON (CFP."IdComprobante" = CAL."IdComprobanteInformado")
WHERE COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0;
