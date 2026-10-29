SET SCHEMA "BSCL"; 
GO
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteSegunPreferencias_IN_MovimientoComprobante" cascade;
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteSegunPreferencias_OUT_AgrupadoMovimientos" cascade;
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteSegunPreferencias_OUT_AgrupadoDetalleMovimientos" cascade;
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteSegunPreferencias_OUT_DetalleMovimientos" cascade;
GO


CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteSegunPreferencias_IN_MovimientoComprobante"
    ( "IdEmpresa" Bigint, "IdPortafolio" Bigint, "IdCliente" Bigint, "ModPrefFact" Bigint, 
      "IdMovimientoComprobante" Bigint, "IdTipoComprobante" NVARCHAR(3), "SeparaPorOperacion" Nvarchar(1), 
      "SeparaPorAgente" Nvarchar(1), "SeparaPorBolsa" Nvarchar(1), "SeparaPorFechaLiq" Nvarchar(1), 
      "SeparaPorMoneda" Nvarchar(1), "SeparaPorProducto" Nvarchar(1), "SeparaPorSucursal" Nvarchar(1), 
      "IdTipoOperacion" Nvarchar(6), "IdTipoProducto" Nvarchar(4), "IdMesa" Bigint, "IdBolsa" Nvarchar(3), 
      "IdEmpleadoEjecutivoPortafolio" Bigint, "NombreEmpleadoEjecutivoPortafolio" nvarchar(100), "IdSucursalEmpleadoEjecutivoPortafolio" Bigint, 
      "NombreSucursalEmpleadoEjecutivoPortafolio" NVARCHAR(100), "FechaLiquidacion" Nvarchar(10), "IdMoneda" Nvarchar(3), "IdMonedaSubyacente" Nvarchar(3), 
      "IdTransaccion" Bigint, "FechaTransaccion" SECONDDATE, "IdInstrumento" Bigint, "NemotecnicoInstrumento" Nvarchar(40), "IdInstrumentoSubyacente" Bigint, 
      "NemotecnicoSubyacente" Nvarchar(40), "IdConceptoMovimiento" Nvarchar(6), "Precio" Decimal(26,8), "Cantidad" Decimal(26,8), 
      "Monto" Decimal(26,8), "MontoMovimientoComprobante" Decimal(26,8), "Spread" Decimal(26,8), "MontoLiquidacion" Decimal(26,8), 
      "Tasa" Decimal(26,8), "TasaEmision" Decimal(26,8), "ValorRescate" Decimal(26,8), "IdMonedaMovimientoComprobante" Nvarchar(3), 
      "IdMonedaLiquidacion" Nvarchar(3), "ValorParidad" Decimal(26,8), "ValorCaptacionMonedaOrigen" Decimal(26,8), 
      "DocumentoIdentidad" Nvarchar(40), "TipoDocumentoIdentidad" Nvarchar(1), "NombreLargoCliente" Nvarchar(300), 
      "DireccionFacturacion" Nvarchar(100), "IdTipoInstrumento" Nvarchar(3), "NombreTipoInstrumento" Nvarchar(100),
      "IdTipoInstrumentoSubyacente" Nvarchar(3), "NombreTipoInstrumentoSubyacente" Nvarchar(100), 
      "IdClasificacionTipoInstrumento" Nvarchar(3), "IdCondicionLiquidacion" Nvarchar(2), "EsComprobIndividual" Nvarchar(1),
      "FusionaPorPrecio" Nvarchar(1), "SeparaPorInstrumento" Nvarchar(1), "SeparaPorTransaccion" Nvarchar(1), 
      "FechaMovimientoComprobante" Nvarchar(10), "IdClasificacionTipoInstrumentoRelacionado" Nvarchar(3), 
      "IdInstrumentoRelacionado" Bigint, "IdTipoInstrumentoRelacionado" Nvarchar(3), "NemotecnicoRelacionado" Nvarchar(40), 
      "NombreClasificacionTipoInstrumentoRelacionado" Nvarchar(100), "NombreTipoInstrumentoRelacionado" Nvarchar(100), 
      "NombreClasificacionTipoInstrumento" Nvarchar(100), "CodigoIFRS" Nvarchar(40), "IdTipoReajuste" Nvarchar(3), 
      "FolioTransaccion" Nvarchar(40), "EsEnRueda" Nvarchar(1), "IndicadorOrdenDirecta" Nvarchar(2), "IdComprobanteReferencia" Bigint, 
      "IdTipoComprobanteReferencia" Nvarchar(3), "FolioComprobanteReferencia" Bigint, 
      "FechaComprobanteReferencia" Nvarchar(10), "FechaComprobante" Nvarchar(10), "EsVentaCorta" Nvarchar(1), "ValorTipoCambio" DECIMAL(26,8),
      "IdFormaCustodia" NVARCHAR(3), "NemoSVS" Nvarchar(40) 
    ) ON COMMIT DELETE ROWS;
GO    
    

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteSegunPreferencias_OUT_AgrupadoMovimientos"
    ( "IdComprobante" Bigint, "IdEmpresa" Bigint, "IdPortafolio" Bigint, "ModPrefFact" Bigint, "IdCliente" Bigint, 
      "DocumentoIdentidad" Nvarchar(40), "TipoDocumentoIdentidad" Nvarchar(1), "NombreLargoCliente" Nvarchar(300), 
      "DireccionFacturacion" Nvarchar(100), "FolioComprobante" Bigint, "IdTipoComprobante" Nvarchar(3), "IdConcepto" NVARCHAR(6), 
      "IdTipoOperacion" Nvarchar(6), "IdTipoProducto" Nvarchar(4), "IdClasificacionTipoInstrumento" Nvarchar(3), 
      "IdTipoInstrumento" Nvarchar(3), "Cantidad" Decimal(26,8), "Monto" Decimal(26,8), "MontoComprobante" Decimal(26,8),
      "MontoLiquidacion" Decimal(26,8), "IdBolsa" Nvarchar(3), "IdEmpleadoEjecutivoPortafolio" Bigint, "NombreEmpleadoEjecutivoPortafolio" NVARCHAR(100),
      "IdSucursalEmpleadoEjecutivoPortafolio" Bigint, "NombreSucursalEmpleadoEjecutivoPortafolio" NVARCHAR(100), "IdCondicionLiquidacion" Nvarchar(2), "FechaLiquidacion" Nvarchar(10), 
      "IdMoneda" Nvarchar(3), "IdMonedaComprobante" Nvarchar(3), "IdMonedaLiquidacion" Nvarchar(3), "IdComprobanteReferencia" Bigint, 
      "IdTipoComprobanteReferencia" Nvarchar(3), "FolioComprobanteReferencia" Bigint, "FechaComprobanteReferencia" Nvarchar(10), 
      "FechaComprobante" Nvarchar(10), "EsVentaCorta" Nvarchar(1)
    ) ON COMMIT DELETE ROWS;
GO    
    
    
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteSegunPreferencias_OUT_AgrupadoDetalleMovimientos"
    ( "IdComprobanteDetalle" Bigint, "IdComprobante" Bigint, "IdConcepto" NVARCHAR(6), "NroLinea" SmallInt, "FechaLiquidacion" Nvarchar(10), 
      "IdInstrumento" Bigint, "IdInstrumentoSubyacente" Bigint, "NemotecnicoInstrumento" Nvarchar(40), "IdTipoOperacion" Nvarchar(6), 
      "IdTipoProducto" Nvarchar(4), "Cantidad" Decimal(26,8), "Precio" Decimal(26,8), "IdMesa" Bigint, "IdBolsa" Nvarchar(3), "IdEmpleadoEjecutivoPortafolio" Bigint, 
      "IdSucursalEmpleadoEjecutivoPortafolio" Bigint, "IdMoneda" Nvarchar(3), "IdMonedaLiquidacion" Nvarchar(3), "IdMonedaComprobanteDetalle" Nvarchar(3),
      "IdMonedaSubyacente" Nvarchar(3), "Monto" Decimal(26,8), "MontoComprobanteDetalle" Decimal(26,8), "MontoLiquidacion" Decimal(26,8), 
      "IdCliente" Bigint, "IdPortafolio" Bigint, "ModPrefFact" Bigint, "FechaComprobanteDetalle" Nvarchar(10), "IdTipoInstrumento" Nvarchar(3), 
      "IdTipoInstrumentoSubyacente" Nvarchar(3), "NemotecnicoSubyacente" Nvarchar(40), "NombreTipoInstrumento" Nvarchar(100), "NombreTipoInstrumentoSubyacente" Nvarchar(100), 
      "IdTransaccion" Bigint, "FechaTransaccion" SECONDDATE, "IdClasificacionTipoInstrumento" Nvarchar(3), "IdClasificacionTipoInstrumentoRelacionado" Nvarchar(3), 
      "IdInstrumentoRelacionado" Bigint, "IdTipoInstrumentoRelacionado" Nvarchar(3), "NemotecnicoRelacionado" Nvarchar(40), 
      "NombreClasificacionTipoInstrumentoRelacionado" Nvarchar(100), "NombreTipoInstrumentoRelacionado" Nvarchar(100), 
      "NombreClasificacionTipoInstrumento" Nvarchar(100), "EsVentaCorta" Nvarchar(1), "ValorTipoCambio" DECIMAL(26,8), 
      "IndicadorOrdenDirecta" Nvarchar(2), "IdFormaCustodia" NVARCHAR(3), "NemoSVS" Nvarchar(40)
    ) ON COMMIT DELETE ROWS;
GO    
    
    
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteSegunPreferencias_OUT_DetalleMovimientos"
    ( "IdComprobanteDetalle" Bigint, "IdMovimientoComprobanteDetalle" Bigint, "IdPortafolio" Bigint, "ModPrefFact" BIGINT, "IdCliente" Bigint, 
      "IdTipoComprobante" Nvarchar(3), "IdTipoOperacion" Nvarchar(6), "IdTipoProducto" Nvarchar(4), "IdMesa" Bigint, "IdBolsa" Nvarchar(3), 
      "IdEmpleadoEjecutivoPortafolio" Bigint, "IdSucursalEmpleadoEjecutivoPortafolio" Bigint, "FechaLiquidacion" Nvarchar(10), 
      "IdMoneda" Nvarchar(3), "IdMovimientoComprobante" Bigint, "IdMonedaSubyacente" Nvarchar(3), "IdTransaccion" Bigint, 
      "IdInstrumento" Bigint, "NemotecnicoInstrumento" Nvarchar(40), "IdInstrumentoSubyacente" Bigint, "IdConceptoMovimiento" Nvarchar(6),
      "Cantidad" Decimal(26,8), "Precio" Decimal(26,8), "Monto" Decimal(26,8), "MontoMovimientoComprobante" Decimal(26,8),
      "MontoLiquidacion" Decimal(26,8), "Tasa" Decimal(26,8), "TasaEmision" Decimal(26,8), "Spread" Decimal(26,8), "ValorRescate" Decimal(26,8),
      "IdMonedaMovimientoComprobante" Nvarchar(3), "IdMonedaLiquidacion" Nvarchar(3), "ValorParidad" Decimal(26,8), "ValorCaptacionMonedaOrigen" Decimal(26,8), 
      "DocumentoIdentidad" Nvarchar(40), "TipoDocumentoIdentidad" Nvarchar(1), "NombreLargoCliente" Nvarchar(300), "DireccionFacturacion" Nvarchar(100), 
      "IdTipoInstrumento" Nvarchar(3), "IdClasificacionTipoInstrumento" Nvarchar(3), "IdCondicionLiquidacion" Nvarchar(2), "IdComprobante" Bigint, 
      "FolioComprobante" Bigint, "IdTipoInstrumentoSubyacente" Nvarchar(3), "NemotecnicoSubyacente" Nvarchar(40), "NombreTipoInstrumento" Nvarchar(100), 
      "NombreTipoInstrumentoSubyacente" Nvarchar(100), "IdClasificacionTipoInstrumentoRelacionado" Nvarchar(3), "IdInstrumentoRelacionado" Bigint, 
      "IdTipoInstrumentoRelacionado" Nvarchar(3), "NemotecnicoRelacionado" Nvarchar(40), "NombreClasificacionTipoInstrumentoRelacionado" Nvarchar(100), 
      "NombreTipoInstrumentoRelacionado" Nvarchar(100), "NombreClasificacionTipoInstrumento" Nvarchar(100), "CodigoIFRS" Nvarchar(40), "IdTipoReajuste" Nvarchar(3), 
      "FolioTransaccion" Nvarchar(40), "EsEnRueda" Nvarchar(1), "IndicadorOrdenDirecta" Nvarchar(2), "EsVentaCorta" Nvarchar(1), 
      "ValorTipoCambio" DECIMAL(26,8), "IdFormaCustodia" NVARCHAR(3), "NemoSVS" Nvarchar(40)
    ) ON COMMIT DELETE ROWS;
GO
    

Commit;
