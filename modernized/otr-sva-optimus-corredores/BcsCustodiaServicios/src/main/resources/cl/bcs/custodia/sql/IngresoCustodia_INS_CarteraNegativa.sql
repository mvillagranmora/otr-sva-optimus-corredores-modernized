INSERT INTO "BSCL.Custodia.BD::CUS.CarteraNegativa" (
"IdCarteraNegativa",
"FechaIngresoRegistro", "FechaNeteo", "IdEstado", "IdUsuarioIngreso", "IdUsuarioNeteo", "FechaOperacion", "IdTipoOperacion", "IdEmpresa", 
"IdCliente", "IdPortafolio", "IdInstrumento", "Cantidad", "ValorRescate", "IdConcepto", "IdCuentaCustodioPortafolio", "IdCondicionLiquidacion", "FechaLiquidacion", 
"CodigoIFRS", "IdMesa", "Nemotecnico", "IdEmisor", "FechaVencimientoOperacion", "EsFungible", "IdTipoReajuste", "IdOperacionMesa", 
"IdTipoComprobante", "FolioComprobante", "IdMovimientoComprobante", "PrecioOperacion", "MontoOperacionMonedaInstrumento", "TasaOperacion", "IdBoveda", 
"IdTipoProducto", "IdCorredoraContraparte", "FolioTransaccion", "FechaTransaccion", "IdBolsa",
"IdOperacion", "IdAgente", "IdMonedaInstrumento", "IdTipoCanalIngreso", "CorteMinimo", "FolioCustodia", "FolioExternoReferencia", "IdSucursal", "Glosa", "NemoSVS", "IdMonedaEmision") 
VALUES(
:IdCarteraNegativa,
CURRENT_UTCTIMESTAMP, :FechaNeteo, :IdEstado, :IdUsuarioIngreso, :IdUsuarioNeteo, :FechaOperacion, :IdTipoOperacion, :IdEmpresa, 
:IdCliente, :IdPortafolio, :IdInstrumento, :Cantidad, :ValorRescate, :IdConcepto, :IdCuentaCustodioPortafolio, :IdCondicionLiquidacion, :FechaLiquidacion, 
:CodigoIFRS, :IdMesa, :Nemotecnico, :IdEmisor, :FechaVencimientoOperacion, :EsFungible, :IdTipoReajuste, :IdOperacionMesa, 
:IdTipoComprobante, :FolioComprobante, :IdMovimientoComprobante, :PrecioOperacion, :MontoOperacionMonedaInstrumento, :TasaOperacion, :IdBoveda, 
:IdTipoProducto, :IdCorredoraContraparte, :FolioTransaccion, :FechaTransaccion, :IdBolsa, 
:IdOperacion, :IdAgente, :IdMonedaInstrumento, :IdTipoCanalIngreso, :CorteMinimo, :FolioCustodia, :FolioExternoReferencia, :IdSucursal, :Glosa, :NemoSVS, :IdMonedaEmision
);