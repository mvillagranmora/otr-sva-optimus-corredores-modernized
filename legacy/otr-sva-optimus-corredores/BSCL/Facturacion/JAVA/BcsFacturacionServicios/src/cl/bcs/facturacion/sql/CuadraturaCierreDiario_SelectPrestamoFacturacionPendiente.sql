SELECT 
 	MovComprobante."IdMovimientoComprobante",
	MovComprobante."IdTransaccion",
    MovComprobante."FolioTransaccion",
    MovComprobante."FechaTransaccion" AS "FechaHoraTransaccion",
    MovComprobante."FechaHoraMovimientoComprobante",
    MovComprobante."IdClasificacionTipoInstrumento",
    MovComprobante."IdTipoInstrumento",
    MovComprobante."CodigoBolsa" AS "NombreBolsa",
    MovComprobante."IdTipoOperacion",
    MovComprobante."IdCondicionLiquidacion",
    MovComprobante."IdMoneda",
    MovComprobante."NombreTipoInstrumento",
    MovComprobante."NemotecnicoInstrumento",
    MovComprobante."Cantidad",
    MovComprobante."Precio",
    MovComprobante."Monto",
    MovComprobante."IdEstadoFacturacion" AS "EstadoFacturacion",
	TO_DECIMAL(0)   AS "CantidadAsignada",
	TO_DECIMAL(0)	AS "CantidadPorAsignar",
	TO_DECIMAL(0)	AS "CantidadEnviada",
	TO_DECIMAL(0)	AS "CantidadFacturada",
	TO_DECIMAL(0)	AS "CantidadPorEnviar",
	TO_DECIMAL(0)	AS "CantidadPorFacturar",
	CASE WHEN MovComprobante."IdEstadoFacturacion" IN ('FAC', 'FAE', 'POL') THEN MovComprobante."Monto" ELSE TO_DECIMAL(0) END AS "MontoFacturado",
	CASE WHEN MovComprobante."IdEstadoFacturacion" IN ('ING') THEN MovComprobante."Monto" ELSE TO_DECIMAL(0) END AS "MontoPorFacturar"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MovComprobante
WHERE MovComprobante."FechaTransaccion" = :FechaInicio AND MovComprobante."IdTipoOperacion" = :IdOperacion

