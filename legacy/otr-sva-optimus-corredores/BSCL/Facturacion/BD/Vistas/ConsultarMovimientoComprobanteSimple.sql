SET SCHEMA "BSCL"; 
GO 
  
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple"; 
GO 

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" As 
SELECT "IdMovimientoComprobante", "IdEmpresa", "IdConceptoMovimiento", "SecuenciaAgrupacion", "IdCliente", "IdPortafolio",
       "IdInstrumento", "IdInstrumentoSubyacente", "IdTipoInstrumento", "IdTipoInstrumentoSubyacente", "IdClasificacionTipoInstrumento",
       "IdTipoProducto", "IdCuentaInversion", "IdMoneda", "IdTipoComprobante", "IdComprobante", "IdTipoOperacion", "IdMovimientoRelacionado",
       "IdBoveda", "IdCorredoraContraparte", "IdCondicionLiquidacion", "FechaLiquidacion", "IdBolsa", "IdTipoReajuste", "IdTipoConvenio",
       "IdEstadoFacturacion", "IdOrden", "IdAsignacion", "IdTipoCanalIngreso", "FechaOrden", "IdTransaccion", "FolioTransaccion", "FechaTransaccion",
       "FechaVencimiento", "FechaParidad", "ValorParidad", "ValorCaptacionMonedaOrigen", "FolioComprobante", "Tasa", "Cantidad",
       "Precio", "Monto", "MontoDescuento", "MontoInteres", "Glosa", "ValorRescate", "TasaAgente", "Spread", "DiasPlazo", "CodigoCanalIngreso",
       "DocumentoIdentidad", "TipoDocumentoIdentidad", "NombreLargoCliente", "DireccionFacturacion", "FolioCartera", "CodigoTipoProducto", 
       "CodigoTipoOperacion", "CodigoBolsa", "NombreTipoInstrumento", "NombreTipoInstrumentoSubyacente", "NombreClasificacionTipoInstrumento",
       "NemotecnicoInstrumento", "NemotecnicoSubyacente", "RazonSocialEmisor", "FechaEmision", "TasaEmision", "FechaIngresoRegistro",
       "FechaSistema", "EsComprobIndividual", "EsCuentaPropia", "IndicadorOrdenDirecta", "EsPrecioPrima", "EsEnRueda", "EsRuteo", "EsIngresoAutomatico",
       "IdUsuario", "IdUsuarioBloqueo", "IdCarga", "EsAPlazo", "MontoAPlazo", "PrecioAPlazo", "IdMovimientoOdRelacionado", "IdMonedaSubyacente",
       "IdMonedaLiquidacion", "ValorTipoCambio", "EsVentaCorta", "SecMovimientoFacturarMigracion", "ActualizarCustodia",
       "FechaMovimientoComprobante", "FechaTipoCambio", "ActualizarCustodiaRelacionado", "IdMonedaMovimientoComprobante", "MontoMovimientoComprobante",
       "MontoLiquidacion", "IdEmisor", "EsFungible", "IdFormaCustodia", "ActualizarCuentaInversion", "ActualizarTesoreria", "IdInstrumentoRelacionado",
       "IdTipoInstrumentoRelacionado", "IdClasificacionTipoInstrumentoRelacionado", "NombreTipoInstrumentoRelacionado", "NombreClasificacionTipoInstrumentoRelacionado",
       "NemotecnicoRelacionado", "IdAsignacionRef", "IdEmpleadoDigitador", "IdEmpleadoEjecutivoPortafolio", "IdSucursalEmpleadoEjecutivoPortafolio",
       "ModPrefFact", "IdSucursalEmpleadoDigitador", "NombreEmpleadoDigitador", "NombreSucursalEmpleadoDigitador", "NombreEmpleadoEjecutivoPortafolio",
       "NombreSucursalEmpleadoEjecutivoPortafolio" 	   
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante";
GO 

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO 
