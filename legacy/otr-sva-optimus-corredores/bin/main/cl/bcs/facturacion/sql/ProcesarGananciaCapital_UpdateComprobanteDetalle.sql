UPDATE CMD SET CMD."MayorMenorValor"=CA."MmValor",CMD."MontoRetencionGananciaCap"=CA."Retencion"
FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" CMD
INNER JOIN (
		SELECT sum(cal."MayorMenorValor") AS "MmValor",sum(cal."MontoRetencionGananciaCap") AS "Retencion",cal."IdComprobanteDetalle"
		FROM (
			SELECT
			 mcdt."IdComprobanteDetalle"
			,mcdt."MayorMenorValor"
			,mcdt."MontoRetencionGananciaCap"
			FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" mcdt
			INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" cdt ON (cdt."IdComprobanteDetalle"=mcdt."IdComprobanteDetalle")
			WHERE mcdt."IdMovimientoComprobanteDetalle" IN (:IdMovimientoComprobanteDetalle)
		)AS cal GROUP BY cal."IdComprobanteDetalle"
) CA ON (CA."IdComprobanteDetalle"=CMD."IdComprobanteDetalle");