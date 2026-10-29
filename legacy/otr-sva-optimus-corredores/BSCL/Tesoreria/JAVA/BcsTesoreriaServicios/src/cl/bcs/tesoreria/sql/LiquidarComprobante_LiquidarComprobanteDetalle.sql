
-----------------------------------------------
-- Liquida Detalle de los comprobantes
-----------------------------------------------

UPDATE COM
SET COM."IdEstado" = 'LIQ'
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" COM
INNER JOIN (    -- Obtiene estado de liquidacion de las formas de pago
			SELECT
				  COM."IdComprobante",
				  (COUNT(CFP."IdComprobanteFormaDePago") - COUNT(CASE WHEN CFP."IdEstado" = 'LIQ' THEN 1 END)) AS "LiquidacionParcial"
			FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS COM
			INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON (CFP."IdComprobante" = COM."IdComprobante")
			INNER JOIN "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobante" AS CAL ON (CAL."IdComprobanteInformado" = COM."IdComprobante")
			WHERE CFP."IdEstado" NOT IN ('ANU','MOD')
			AND   CAL."IdComprobante" > 0
			GROUP BY COM."IdComprobante"
			HAVING COUNT(CASE WHEN CFP."IdEstado" = 'LIQ' THEN 1 END) > 0 -- por lo menos una forma de pago debe estar liquidada para cambiar estado del comprobante.
		) AS ELC ON (ELC."IdComprobante" = COM."IdComprobante")
WHERE ELC."LiquidacionParcial" = 0; -- Si no es liquidacion parcial

		
		