SELECT "IdComprobanteFormaDePago", "IdComprobante", "NombreCuentaBancariaCorredora", "NombreTipoCuentaBancariaSocio"
FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
WHERE "IdComprobanteFormaDePago" IN (:IdComprobante);