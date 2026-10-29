SELECT 
com."MontoRetencionGananciaCap",
com."IdPortafolio",
com."IdMoneda",
com."IdMonedaComprobante",
com."MontoComprobanteFinal",
com."FechaLiquidacion",
com."IdEmpleadoEjecutivoPortafolio" AS "IdEjecutivo",
com."IdComprobante",
com."IdTipoComprobante",
com."FolioComprobante",
com."FechaComprobante",
com."IdInstrumento",
com."IdTipoInstrumento",
com."NemotecnicoInstrumento" AS "Nemo",
com."IdTipoProducto",
com."IdTipoOperacion",
com."IdSucursalEmpleadoEjecutivoPortafolio" AS "IdSucursal"

FROM  "BSCL.Facturacion.BD::FAC.Comprobante" com
	INNER JOIN 
		(
			SELECT cdt."IdComprobante"
			FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" mcdt
			INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" cdt ON (cdt."IdComprobanteDetalle"=mcdt."IdComprobanteDetalle")
			WHERE mcdt."IdMovimientoComprobanteDetalle" IN (:IdMovimientoComprobanteDetalle)
			GROUP BY cdt."IdComprobante"
		) det ON (det."IdComprobante"=com."IdComprobante")
WHERE com."CargoGananciaCap"='C' AND com."MontoRetencionGananciaCap">0;	
