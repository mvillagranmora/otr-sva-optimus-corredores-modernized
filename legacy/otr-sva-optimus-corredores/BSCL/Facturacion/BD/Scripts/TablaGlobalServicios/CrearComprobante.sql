SET SCHEMA "BSCL";
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_IN_MovimientoComprobante" cascade;
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_AgrupadoMovimientos" cascade;
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_AgrupadoDetalleMovimientos" cascade;
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_DetalleMovimientos" cascade;
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_Comprobante" cascade;
GO
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_CargoAbono" cascade;
GO
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_IngresoEgreso" cascade;
GO
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_Ingresos" cascade;
GO
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_Retiros" cascade;
GO
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_InformacionDetalle" cascade;
GO
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_ResultadoProceso" cascade;
GO
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_AdvertenciasDetalle" cascade;
GO
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_ErroresDetalle" cascade;
GO

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_IN_MovimientoComprobante"
    ( "IdPortafolio" BIGINT, 
      "IdCliente" BIGINT, 
      "IdMovimientoComprobante" BIGINT,
      "IdTipoComprobante" NVARCHAR(3),
      "FechaComprobante" NVARCHAR(10)
    ) ON COMMIT DELETE ROWS;
GO    
    
    
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_AgrupadoMovimientos"
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
    

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_AgrupadoDetalleMovimientos"
    ( "IdComprobanteDetalle" Bigint, "IdComprobante" Bigint, "IdConcepto" NVARCHAR(6), "NroLinea" SmallInt, "FechaLiquidacion" Nvarchar(10), 
      "IdInstrumento" Bigint, "IdInstrumentoSubyacente" Bigint, "NemotecnicoInstrumento" Nvarchar(40), "IdTipoOperacion" Nvarchar(6), 
      "IdTipoProducto" Nvarchar(4), "Cantidad" Decimal(26,8), "Precio" Decimal(26,8), "IdMesa" BIGINT, "IdBolsa" Nvarchar(3), "IdEmpleadoEjecutivoPortafolio" Bigint, 
      "IdSucursalEmpleadoEjecutivoPortafolio" Bigint, "IdMoneda" Nvarchar(3), "IdMonedaLiquidacion" Nvarchar(3), "IdMonedaComprobanteDetalle" Nvarchar(3),
      "IdMonedaSubyacente" Nvarchar(3), "Monto" Decimal(26,8), "MontoComprobanteDetalle" Decimal(26,8), "MontoLiquidacion" Decimal(26,8), 
      "IdCliente" Bigint, "IdPortafolio" Bigint, "ModPrefFact" Bigint, "FechaComprobanteDetalle" Nvarchar(10), "IdTipoInstrumento" Nvarchar(3), 
      "IdTipoInstrumentoSubyacente" Nvarchar(3), "NemotecnicoSubyacente" Nvarchar(40), "NombreTipoInstrumento" Nvarchar(100), "NombreTipoInstrumentoSubyacente" Nvarchar(100), 
      "IdTransaccion" Bigint, "IdClasificacionTipoInstrumento" Nvarchar(3), "IdClasificacionTipoInstrumentoRelacionado" Nvarchar(3), 
      "IdInstrumentoRelacionado" Bigint, "IdTipoInstrumentoRelacionado" Nvarchar(3), "NemotecnicoRelacionado" Nvarchar(40), 
      "NombreClasificacionTipoInstrumentoRelacionado" Nvarchar(100), "NombreTipoInstrumentoRelacionado" Nvarchar(100), 
      "NombreClasificacionTipoInstrumento" Nvarchar(100), "EsVentaCorta" Nvarchar(1), "ValorTipoCambio" DECIMAL(26,8), 
      "IndicadorOrdenDirecta" Nvarchar(2), "IdFormaCustodia" NVARCHAR(3)
    ) ON COMMIT DELETE ROWS;
GO    
    

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_DetalleMovimientos"
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
      "ValorTipoCambio" DECIMAL(26,8), "IdFormaCustodia" NVARCHAR(3) 
    ) ON COMMIT DELETE ROWS;     
GO    

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_Comprobante"
    ( "IdComprobante" BIGINT) ON COMMIT DELETE ROWS;
GO

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_CargoAbono"
    ( "NumeroCargoAbono" BIGINT,
      "IdCargoAbonoMovimiento" BIGINT, 
      "IdTipoMovimiento" NVARCHAR(6)
    ) ON COMMIT DELETE ROWS; 
GO    

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_IngresoEgreso"
    ( "NumeroReferencia"  BIGINT,
      "IdIngresoEgreso" BIGINT, 
      "IdTipoMovimiento" NVARCHAR(6) 
    ) ON COMMIT DELETE ROWS; 
GO           
    
    
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_Ingresos"
    ( "IdGenerarMovimientoCustodia" BIGINT, 
      "IdGrupoOperacion" BIGINT, 
      "TipoRegistro" NVARCHAR(1), 
      "SubModulo" NVARCHAR(3), 
      "TablaAfectada" NVARCHAR(3),
      "IdCustodia" BIGINT,
      "FolioCustodia" BIGINT,
      "EsNominal" NVARCHAR(1),
      "Cantidad" DECIMAL(26,8),
      "ValorRescate" DECIMAL(26,8),
      "TipoFactorAjuste" NVARCHAR(3),
      "ValorFactorAjusteaFechaCurse" DECIMAL(26,8),
      "CostoEnFactorAjuste" DECIMAL(26,8)
    ) ON COMMIT DELETE ROWS;  
GO   

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_Retiros"
    ( "IdGenerarMovimientoCustodia" BIGINT,
      "IdGrupoOperacion" BIGINT,
      "TipoRegistro" NVARCHAR(1),
      "SubModulo" NVARCHAR(3),
      "TablaAfectada" NVARCHAR(3),
      "IdCustodia" BIGINT,
      "FolioCustodia" BIGINT,
      "EsNominal" NVARCHAR(1),
      "Cantidad" DECIMAL(26,8),
      "ValorRescate" DECIMAL(26,8),
      "TipoFactorAjuste" NVARCHAR(3),
      "ValorFactorAjusteaFechaCurse" DECIMAL(26,8),
      "CostoEnFactorAjuste" DECIMAL(26,8)
    ) ON COMMIT DELETE ROWS; 
GO    
    
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_InformacionDetalle"
    ( "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;     
GO    

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_ResultadoProceso"
    ( "Campo" VARCHAR(40),
      "Id" BIGINT, 
      "Servicio" VARCHAR(250), 
      "Resultado" VARCHAR(40)
    ) ON COMMIT DELETE ROWS;  
GO    
    
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_AdvertenciasDetalle"
    ( "IdMovimientoComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "RequiereAutorizacion" NVARCHAR(40),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS; 
GO    

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.CrearComprobante_OUT_ErroresDetalle"
    ( "IdMovimientoComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;         
GO