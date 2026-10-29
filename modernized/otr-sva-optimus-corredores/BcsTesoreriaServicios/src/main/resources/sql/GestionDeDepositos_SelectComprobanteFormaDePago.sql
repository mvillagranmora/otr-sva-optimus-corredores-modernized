
-- Obtiene datos de las formas de pago del comprobante asociadas a los depositos

SELECT 
	COALESCE(CFP."IdComprobante", 0) AS "IdComprobante",
	CFP."IdTipoMovimiento",
	CFP."IdComprobanteFormaDePago",	
	COALESCE(CFP."IdCuentaBancariaCorredora",0) AS "IdCuentaBancariaCorredora",	
	DD."IdEstado" AS "IdEstadoDepositoDetalle",
	CFP."IdEstado" AS "IdEstadoComprobanteFormaDePago"

FROM "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD		 
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP  ON CFP."IdComprobanteFormaDePago" = DD."IdComprobanteFormaDePago" 	
WHERE (    DD."IdDepositoDetalle" IN (:IdsDetalle) OR DD."IdDeposito" IN (:IdsDeposito));
