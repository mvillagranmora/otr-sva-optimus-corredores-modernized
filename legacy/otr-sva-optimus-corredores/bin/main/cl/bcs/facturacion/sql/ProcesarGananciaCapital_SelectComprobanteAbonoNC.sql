SELECT 
det."MontoRetencionGananciaCap",
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
			SELECT mct."IdComprobante", com_org."MontoRetencionGananciaCap"
			FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 	mct
				INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" mcdt 	ON (mcdt."IdMovimientoComprobante"=mct."IdMovimientoComprobante")
				INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" 					com_org ON (com_org."IdComprobante"=mct."IdComprobanteReferencia" AND  com_org."CargoGananciaCap"='C' AND com_org."MontoRetencionGananciaCap">0 ) 
			
			WHERE mcdt."IdMovimientoComprobanteDetalle" IN (:IdMovimientoComprobanteDetalle) and 
			mct."IdComprobanteReferencia">0
			GROUP BY mct."IdComprobante", com_org."MontoRetencionGananciaCap"
		) det ON (com."IdComprobante"=det."IdComprobante")

