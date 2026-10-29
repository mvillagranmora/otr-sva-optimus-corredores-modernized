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
FROM "BSCL.Facturacion.BD::FAC.Comprobante" com
where com."IdComprobante" = :IdComprobante