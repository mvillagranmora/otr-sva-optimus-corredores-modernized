SELECT "IdReservaHistorico", "FechaRespaldo", "IdReserva", "IdOperacion", "FolioCarteraOrigen", "FolioCarteraCustodia", "FolioOperacion", "IdEstado", "IdUsuarioCreacion",
"IdTipoProducto", "IdConcepto", "Tasa", "TasaMercado", "Precio", "PrecioMercado", "MontoValorizado", "IdClasificacionTipoInstrumento", "IdInstrumento", 
"IdTipoInstrumento", "IdTipoReajuste", "IdEmisor", "FechaVencimiento", "EsFungible", "IdBolsa", "IdBoveda", "CodigoIFRS", "CorteMinimo", "CantidadInicial",
"ValorRescateInicial", "MontoValorizadoReserva", "FechaVencimientoReserva", "FechaVencimientoProducto", "FechaCreacion", "IngIdUsuario", "IngCantidad", "IngValorRescate", 
"IngMontoValorizado", "IngFecha", "EgrIdUsuario", "EgrCantidad", "EgrValorRescate", "EgrMontoValorizado", "EgrFecha", "SecuenciaCompromisoMigracion", "IdCarga", 
"IdCarteraCustodia", "IdMonedaLiquidacionInstrumento", "IdMonedaEmisionInstrumento", "IdCarteraOrigen", "IdMoneda", "MontoLiquidacionOperacion", "MontoLiquidacion", 
"IdMonedaLiquidacionOperacion", "Nemotecnico", "FolioSubyacente"
FROM "BSCL.Custodia.BD::HIS.Reserva"
WHERE CAST("FechaRespaldo" AS DATE) BETWEEN :p_Desde AND :p_Hasta
ORDER BY "IdReservaHistorico", "FechaRespaldo" DESC
LIMIT :p_Limit OFFSET :p_Offset;
