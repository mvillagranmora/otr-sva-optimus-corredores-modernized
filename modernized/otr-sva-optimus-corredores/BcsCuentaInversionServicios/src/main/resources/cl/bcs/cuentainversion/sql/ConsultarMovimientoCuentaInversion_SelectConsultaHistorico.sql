
SELECT
	"FechaSistema","FechaIngresoRegistro","FechaEfectivaMovimiento","NombreConcepto","EsVentaCorta",
	"NemotecnicoOriginal","IdCliente","NombreCliente","IdPortafolio","IdMoneda","CodigoMoneda",
	"NombreMoneda","SimboloMoneda","DecimalesMoneda","DocumentoIdentidad","CodigoPortafolio",
	"Descripcion","Cargo","Abono","SaldoPorFechaMovimiento",
	"SaldoPorFechaMovimiento" AS "SaldoPorFechaEfectiva",
	"SaldoPorFechaMovimiento" AS "SaldoSimulado",
	"EsCargoAbonoPorTraspasoTraducido","IdEstado","NombreEstado","IdTipoComprobante",
	"NombreTipoComprobante","IdComprobante","FolioComprobante","FechaComprobante","IdTipoProducto",
	"NombreTipoProducto","IdTipoOperacion","NombreTipoOperacion","FechaOperacion",
	"FechaVencimientoOperacion","IdOperacion","MontoOperacion","IdCargoAbonoMovimiento",
	"IdCargoAbonoRelacionadoTraspaso","NombreClienteRelacionado","CodigoPortafolioRelacionado",
	"FechaCertificacion","EsIngresoManual",
	"IdCarga",
	"IdEstadoSII","IdRespuestaDTE"
FROM (
SELECT
	"FechaSistema","FechaIngresoRegistro","FechaEfectivaMovimiento","NombreConcepto","EsVentaCorta",
	"NemotecnicoOriginal","IdCliente","NombreCliente",CMP."IdPortafolio",CMP."IdMoneda","CodigoMoneda",
	"NombreMoneda","SimboloMoneda","DecimalesMoneda","DocumentoIdentidad","CodigoPortafolio",
	"Descripcion","Cargo","Abono","SaldoPorFechaMovimiento",
	"SaldoPorFechaMovimiento" AS "SaldoPorFechaEfectiva",
	"SaldoPorFechaMovimiento" AS "SaldoSimulado",
	"EsCargoAbonoPorTraspasoTraducido",MOV."IdEstado","NombreEstado","IdTipoComprobante",
	"NombreTipoComprobante","IdComprobante","FolioComprobante","FechaComprobante","IdTipoProducto",
	"NombreTipoProducto","IdTipoOperacion","NombreTipoOperacion","FechaOperacion",
	"FechaVencimientoOperacion","IdOperacion","MontoOperacion","IdCargoAbonoMovimiento",
	"IdCargoAbonoRelacionadoTraspaso","NombreClienteRelacionado","CodigoPortafolioRelacionado",
	"FechaCertificacion","EsIngresoManual",
	"IdCarga",
	"IdEstadoSII","IdRespuestaDTE"
FROM "BSCL.CuentaInversion.BD::CTA.ConsultarMovimientoCuentaInversion_Proceso" AS CMP
LEFT JOIN  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimientoHistorico" MOV
ON CMP."IdPortafolio" = MOV."IdPortafolio"
AND CMP."IdMoneda" = MOV."IdMoneda"
WHERE "FechaSistema" BETWEEN "FechaDesde" AND "FechaHasta")
