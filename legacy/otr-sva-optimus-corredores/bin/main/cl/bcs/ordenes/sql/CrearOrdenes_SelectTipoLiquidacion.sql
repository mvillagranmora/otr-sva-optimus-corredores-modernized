SELECT "IdTipoLiquidacionComisionFactura","TipoLiquidacion","TipoPorDefecto","CodigoDetalleIndicadorDefecto"
FROM "BSCL.Facturacion.BD::FAC.TipoLiquidacionComisionFactura"
WHERE "IdEstado" = 'ACT';