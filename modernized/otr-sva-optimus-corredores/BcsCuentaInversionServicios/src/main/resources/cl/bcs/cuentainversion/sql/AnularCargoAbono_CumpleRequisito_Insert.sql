INSERT INTO "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoAnularCargoAbono_IN_CargoAbonoAAnular"
	  	("IdInterno","IdCargoAbonoMovimiento","IdCuentaInversion","IdEstado","IdEvento","IdTipoMovimiento","IdTipoOperacion","IdOperacion","IdMonedaOperacion",
		"TipoCambioOperacion","MontoOperacion","FechaOperacion","FechaLiquidacionOperacion","FechaVencimientoOperacion","IdContraparte","IdTipoComprobante",
		"IdComprobante","FolioComprobante","IdComprobanteDetalle","IdComprobanteFormaDePago","IdTipoMovimientoAsociado","IdMovimientoAsociado","FechaComprobante",
		"IdPersonaPagoCobro","IdInstrumento","IdTipoInstrumento","Nemo","EsIndicadorBilateral","GeneroContraparteEnTesoreria","IdConcepto","FechaSistema","FechaEfectivaMovimiento",
		"IdCliente","IdPortafolio","IdTipoProducto","Monto","IdMoneda","IdEjecutivo","IdSucursal","IdEmpresa","GlosaAnulacion","PERMITEANULARMOVIMIENTO","ANULACIONREQUIEREAUTORIZACION",
		"EsCargoAbonoPorTraspaso","IdCargoAbonoRelacionadoTraspaso","Informado")
  	SELECT "IdInterno","IdCargoAbonoMovimiento","IdCuentaInversion","IdEstado","IdEvento","IdTipoMovimiento","IdTipoOperacion","IdOperacion","IdMonedaOperacion",
		"TipoCambioOperacion","MontoOperacion","FechaOperacion","FechaLiquidacionOperacion","FechaVencimientoOperacion","IdContraparte","IdTipoComprobante",
		"IdComprobante","FolioComprobante","IdComprobanteDetalle","IdComprobanteFormaDePago","IdTipoMovimientoAsociado","IdMovimientoAsociado","FechaComprobante",
		"IdPersonaPagoCobro","IdInstrumento","IdTipoInstrumento","Nemo","EsIndicadorBilateral","GeneroContraparteEnTesoreria","IdConcepto","FechaSistema","FechaEfectivaMovimiento",
		"IdCliente","IdPortafolio","IdTipoProducto","Monto","IdMoneda","IdEjecutivo","IdSucursal","IdEmpresa","GlosaAnulacion","PERMITEANULARMOVIMIENTO","ANULACIONREQUIEREAUTORIZACION",
		"EsCargoAbonoPorTraspaso","IdCargoAbonoRelacionadoTraspaso","Informado" 
FROM "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular";