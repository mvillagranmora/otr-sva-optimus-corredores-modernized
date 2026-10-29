INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Entrada_IngresoEgresoGenerar"
("NumeroReferencia","IdTipoMovimiento","IdTipoPersona","IdPersona","IdPortafolio","IdMoneda","Monto","FechaLiquidacion","IdConcepto","IdTipoPersonaTitularPago"
,"IdPersonaTitularPago","IdPortafolioTitularPago","IdCaja","SecuenciaAgrupacion","SecuenciaRelacionada","Glosa","IdEmpleadoDigitador","IdSucursalEmpleadoDigitador"
,"IdTipoProducto","IdTipoOperacion","IdTipoComprobante","IdComprobante","FechaComprobante","FechaLiquidacionComprobante","IdOperacion","FechaOperacion"
,"IdCondicionLiquidacion","IdBolsa","IdMonedaOperacion","MontoOperacion","IdTipoReajuste","FechaParidad","ValorParidad","FolioCartera","IdInstrumento"
,"IdTipoInstrumento","IdClasificacionTipoInstrumento","Bilateral","GeneraComprobante","AgruparEnComprobante","MueveCuentaInversion","TitularPago")
VALUES(:NumeroReferencia,:IdTipoMovimiento,:IdTipoPersona,:IdPersona,:IdPortafolio,:IdMoneda,:Monto,:FechaLiquidacion,:IdConcepto,:IdTipoPersonaTitularPago
,:IdPersonaTitularPago,:IdPortafolioTitularPago,:IdCaja,:SecuenciaAgrupacion,:SecuenciaRelacionada,:Glosa,:IdEmpleadoDigitador,:IdSucursalEmpleadoDigitador
,:IdTipoProducto,:IdTipoOperacion,:IdTipoComprobante,:IdComprobante,:FechaComprobante,:FechaLiquidacionComprobante,:IdOperacion,:FechaOperacion
,:IdCondicionLiquidacion,:IdBolsa,:IdMonedaOperacion,:MontoOperacion,:IdTipoReajuste,:FechaParidad,:ValorParidad,:FolioCartera,:IdInstrumento
,:IdTipoInstrumento,:IdClasificacionTipoInstrumento,:Bilateral,:GeneraComprobante,:AgruparEnComprobante,:MueveCuentaInversion,:TitularPago);