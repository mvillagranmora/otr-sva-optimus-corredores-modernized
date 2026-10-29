SELECT 
	IngEgrAGenerar."NumeroReferencia" as "IdComprobante",
	IngEgrAGenerar."IdIngresoEgreso",
	IngEgrAGenerar."TipoComprobante",
	IngrEgr."IdIngresoEgreso" as "IdIngresoEgreso",
	IngrEgr."IdIngresoEgreso",IngrEgr."IdTipoMovimiento",IngrEgr."IdConcepto",
	IngrEgr."IdCaja",IngrEgr."IdMoneda",IngrEgr."Monto",
	IngrEgr."FechaLiquidacion",IngrEgr."FechaIngresoRegistro",IngrEgr."FechaMovimiento",
	IngrEgr."IdTipoPersona",IngrEgr."IdPersona",
	IngrEgr."IdTipoDocumentoIdentidad",IngrEgr."IdTipoPersonaTitularPago",IngrEgr."IdPersonaTitularPago",
	IngrEgr."IdPortafolioTitularPago",IngrEgr."SecuenciaAgrupacion",
	IngrEgr."SecuenciaRelacionada",IngrEgr."Glosa",IngrEgr."IdEmpleadoDigitador",
	IngrEgr."IdSucursalEmpleadoDigitador",IngrEgr."IdTipoComprobanteOrigen",
	IngrEgr."IdComprobanteOrigen",IngrEgr."FechaComprobanteOrigen",
	IngrEgr."FechaLiquidacionComprobanteOrigen",IngrEgr."IdTipoProducto",
	IngrEgr."IdTipoOperacion",IngrEgr."IdBolsa",IngrEgr."IdMonedaOperacion",
	IngrEgr."MontoOperacion",IngrEgr."FechaParidad",IngrEgr."IdTipoReajuste",
	IngrEgr."ValorParidad",IngrEgr."FolioCartera",IngrEgr."IdInstrumento",
	IngrEgr."IdTipoInstrumento",IngrEgr."IdClasificacionTipoInstrumento",
	IngrEgr."EsBilateral",IngrEgr."TitularPago",IngrEgr."EsIngresoAutomatico",
	IngrEgr."NeteaComprobante",IngrEgr."AgrupaComprobante",
	IngrEgr."MueveCuentaInversion",IngrEgr."IdEstado",IngrEgr."NombrePersona", IngrEgr."DocumentoIdentidad",
	IngrEgr."NombrePersona" as "NombrePersonaTitularPago", IngrEgr."IdUsuarioBloqueo",
	IngrEgr."IdCarga" , IngrEgr."IdEvento", IngrEgr."IdUsuarioAnulacion","NeteaComprobante" as "ComprobanteNeteado"
	
	  FROM 
	  #ComprobanteIngresEgresoAProcesar  As IngEgrAGenerar
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" As IngrEgr ON IngEgrAGenerar."IdIngresoEgreso" = IngrEgr."IdIngresoEgreso"
	  WHERE IngrEgr."IdEmpresa" = :p_IdEmpresa;
		
		