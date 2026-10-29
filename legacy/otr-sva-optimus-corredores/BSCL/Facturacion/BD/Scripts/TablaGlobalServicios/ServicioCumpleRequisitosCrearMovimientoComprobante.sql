SET SCHEMA "BSCL"; 
GO 

DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobante" cascade;
GO 
DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobanteFormaDePago" cascade;
GO 
DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobanteCobro" cascade;
GO 
DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_OUT_MovimientosRechazados" cascade;
GO
DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_OUT_ErroresDetalle" cascade;
GO 


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobante"
    ( "NumeroMvtoCmbte" BIGINT,
      "IdEmpresa" BIGINT,
      "IdConcepto" NVARCHAR(6),
      "SecuenciaAgrupacion" NVARCHAR(40),
      "IdCliente" BIGINT,
      "IdPortafolio" BIGINT,
      "IdEmpleadoDigitador" BIGINT,
      "IdInstrumento" BIGINT,
      "IdInstrumentoRelacionado" BIGINT,
      "IdInstrumentoSubyacente" BIGINT,
      "IdTipoProducto" NVARCHAR(4),
      "IdMoneda" NVARCHAR(3), --Colocar como obligatoria cuando venga Monto 
      "IdMonedaSubyacente" NVARCHAR(3),
      "IdMonedaLiquidacion" NVARCHAR(3), --Colocar como obligatoria cuando venga MontoLiquidacion
      "IdTipoComprobante" NVARCHAR(3),
      "IdTipoOperacion" NVARCHAR(6),
      "IdBoveda" NVARCHAR(3),
      "IdCorredoraContraparte" BIGINT,
      "IdCondicionLiquidacion" NVARCHAR(2),
      "FechaLiquidacion" NVARCHAR(10),
      "IdBolsa" NVARCHAR(3),
      "IdTipoReajuste" NVARCHAR(3),
      "IdTipoConvenio" NVARCHAR(3),
      "IdFormaCustodia" NVARCHAR(3),
      "IdEmisor" NVARCHAR(40),
      "IdOrden" BIGINT,
      "IdAsignacion" BIGINT,
      "IdTipoCanalIngreso" NVARCHAR(3),
      "FechaOrden" NVARCHAR(40),
      "IdTransaccion" BIGINT,
      "FolioTransaccion" NVARCHAR(40),
      "FechaTransaccion" NVARCHAR(10),
      "FechaVencimiento" NVARCHAR(10),
      "PrecioAPlazo" DECIMAL(26,8),
      "MontoAPlazo" DECIMAL(26,8),
      "FechaParidad" NVARCHAR(10),
      "ValorParidad" DECIMAL(26,8),
      "FechaTipoCambio" NVARCHAR(10),
      "ValorTipoCambio" DECIMAL(26,8),
      "ValorCaptacionMonedaOrigen" DECIMAL(26,8),
      "Tasa" DECIMAL(26,8),
      "Cantidad" DECIMAL(26,8),
      "Precio" DECIMAL(26,8),
      "Monto" DECIMAL(26,8), --colocar obligatoria cuando venga IdMoneda
      "MontoMovimientoComprobante" DECIMAL(26,8), --colocar obligatoria
      "MontoLiquidacion" DECIMAL(26,8), --colocar obligatoria cuando venga IdMonedaLiquidacion
      "MontoDescuento" DECIMAL(26,8),
      "MontoInteres" DECIMAL(26,8),
      "Glosa" NVARCHAR(260),
      "ValorRescate" DECIMAL(26,8),
      "TasaAgente" DECIMAL(26,8),
      "Spread" DECIMAL(26,8),                                                                     
      "DiasPlazo" DECIMAL(26,8),
      "FechaMovimientoComprobante" NVARCHAR(10),
      "FolioCartera" BIGINT,
      "FechaEmision" NVARCHAR(10),
      "TasaEmision" DECIMAL(26,8),
      "EsComprobIndividual" NVARCHAR(1),
      "EsCuentaPropia" NVARCHAR(1),
      "IndicadorOrdenDirecta" NVARCHAR(1),
      "EsVentaCorta" NVARCHAR(1),
      "EsPrecioPrima" NVARCHAR(1),
      "EsEnRueda" NVARCHAR(1),
      "EsRuteo" NVARCHAR(1),
      "EsAPlazo" NVARCHAR(1),
      "EsFungible" NVARCHAR(1),
      "ActualizarCustodia" NVARCHAR(1),
      "ActualizarCuentaInversion" NVARCHAR(1),
      "ActualizarTesoreria" NVARCHAR(1),
      "ActualizarCustodiaRelacionado" NVARCHAR(1),
      "CrearComprobanteAutomatico" NVARCHAR(1),
      "IdUsuario" BIGINT
    ) ON COMMIT DELETE ROWS;
GO 
    
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobanteFormaDePago"
    ( "NumeroMvtoCmbte" BIGINT,
      "IdMedioDePago" NVARCHAR(3), 
      "NumeroSerie" NVARCHAR(40), 
      "NumeroDocumento" BIGINT, 
      "IdCuentaBancaria" BIGINT, 
      "IdTipoCuentaBancaria" NVARCHAR(4),
      "NumeroCuentaBancaria" NVARCHAR(100), 
      "IdTarjetaBancaria" BIGINT,
      "IdTipoTarjetaBancaria" NVARCHAR(40), 
      "NumeroTarjetaBancaria" NVARCHAR(100),
      "FechaVctoTarjetaBancaria" NVARCHAR(10), 
      "IdModalidadDePago" NVARCHAR(6), 
      "IdBanco" BIGINT, 
      "IdMoneda" NVARCHAR(3), 
      "FechaDocumentoPago" NVARCHAR(10), 
      "MontoLiquidacionFormaDePago" DECIMAL(26,8)
    ) ON COMMIT DELETE ROWS;    
GO     
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_IN_MovimientoComprobanteCobro"
    ( "NumeroMvtoCmbte" BIGINT,
      "IdCobro" NVARCHAR(6), 
      "IdMonedaCobro" NVARCHAR(3), 
      "IdMonedaLiquidacionCobro" NVARCHAR(3), 
      "MontoCobro" DECIMAL(26,8), 
      "MontoMovimientoComprobanteCobro" DECIMAL(26,8), 
      "MontoLiquidacionCobro" DECIMAL(26,8), 
      "Porcentaje" DECIMAL(26,8)
    ) ON COMMIT DELETE ROWS;    
GO     
    
    
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_OUT_MovimientosRechazados"
    ( "NumeroMvtoCmbte" BIGINT ) ON COMMIT DELETE ROWS;    
GO     
    
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearMovimientoComprobante_OUT_ErroresDetalle"
    ( "NumeroMvtoCmbte" BIGINT, 
       "Codigo" NVARCHAR(100),
       "Descripcion" NVARCHAR(520)
    ) ON COMMIT DELETE ROWS;    
GO     

commit;    
GO 