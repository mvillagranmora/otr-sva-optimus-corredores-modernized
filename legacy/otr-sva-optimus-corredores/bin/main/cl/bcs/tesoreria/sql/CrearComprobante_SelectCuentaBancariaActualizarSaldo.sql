-- Selecciona las cuentas bancarias de la corredora para actualizacion de saldos
SELECT DISTINCT "IdCuentaBancariaCorredora" as "IdCuentaBanco"
FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago"
WHERE COALESCE("IdCuentaBancariaCorredora",0) > 0;
			 