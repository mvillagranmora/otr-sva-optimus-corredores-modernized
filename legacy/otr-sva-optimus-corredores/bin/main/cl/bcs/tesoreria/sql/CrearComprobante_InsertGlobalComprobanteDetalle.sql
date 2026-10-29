-- Inserta en blobal datos para comprobante detalle
INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalle"
(
	"IdComprobanteDetalle","IdComprobante","IdTipoMovimiento","IdConcepto","NroLinea","IdMoneda","Monto",
	"SecuenciaAgrupacion","SecuenciaRelacionada","IdEmpleadoDigitador","IdSucursalEmpleadoDigitador",
	"IdTipoComprobanteOrigen","IdComprobanteOrigen","FechaComprobanteOrigen","FechaLiquidacionComprobanteOrigen",
	"IdTipoProducto","IdTipoOperacion","IdBolsa","IdMonedaOperacion","MontoOperacion","FechaParidad","IdTipoReajuste",
	"ValorParidad","FolioCartera","IdInstrumento","IdTipoInstrumento","IdClasificacionTipoInstrumento","IdCondicionDeLiquidacion"
)
SELECT "BSCL.Tesoreria.BD.Secuencias::ComprobanteDetalle".NEXTVAL AS "IdComprobanteDetalle",MI."IdComprobante",
	   MI."IdTipoMovimientoDetalle" as "IdTipoMovimiento",MI."IdConcepto",
	   ROW_NUMBER() over () as "NroLinea", -- no necesita particion ya que está agrupado 
	   MI."IdMoneda",SUM(MI."Monto") AS "Monto",
	   MI."SecuenciaAgrupacion",MI."SecuenciaRelacionada",MI."IdEmpleadoDigitador",MI."IdSucursalEmpleadoDigitador",MI."IdTipoComprobanteOrigen",
	   MI."IdComprobanteOrigen",MI."FechaComprobanteOrigen",MI."FechaLiquidacionComprobanteOrigen",MI."IdTipoProducto",MI."IdTipoOperacion",
	   MI."IdBolsa",MI."IdMonedaOperacion",MI."MontoOperacion",MI."FechaParidad",MI."IdTipoReajuste",MI."ValorParidad",
	   MI."FolioCartera",MI."IdInstrumento",MI."IdTipoInstrumento",MI."IdClasificacionTipoInstrumento",MI."IdCondicionDeLiquidacion"
  FROM "BSCL.Tesoreria.BD::GBL.MovimientoDetalleGenerarComprobante" as MI
 GROUP BY MI."IdComprobante",MI."IdTipoMovimientoDetalle",MI."IdConcepto",MI."IdMoneda",MI."SecuenciaAgrupacion",MI."SecuenciaRelacionada",
 	   MI."IdEmpleadoDigitador",MI."IdSucursalEmpleadoDigitador",MI."IdTipoComprobanteOrigen",MI."IdComprobanteOrigen",
	   MI."FechaComprobanteOrigen",MI."FechaLiquidacionComprobanteOrigen",MI."IdTipoProducto",MI."IdTipoOperacion",
	   MI."IdBolsa",MI."IdMonedaOperacion",MI."MontoOperacion",MI."FechaParidad",MI."IdTipoReajuste",MI."ValorParidad",
	   MI."FolioCartera",MI."IdInstrumento",MI."IdTipoInstrumento",MI."IdClasificacionTipoInstrumento",MI."IdCondicionDeLiquidacion";