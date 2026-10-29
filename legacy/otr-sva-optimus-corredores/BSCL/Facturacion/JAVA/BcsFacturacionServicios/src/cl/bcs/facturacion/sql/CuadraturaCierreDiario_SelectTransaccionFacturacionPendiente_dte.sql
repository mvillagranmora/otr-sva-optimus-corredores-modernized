SELECT
	"FolioComprobante" AS "FolioTransaccion",
	"IdComprobante" AS "IdTransaccion",
	"FechaLiquidacion" AS "FechaTransaccion",
	"FechaIngresoRegistro" AS "FechaHoraTransaccion",
	"IdClasificacionTipoInstrumento",
	"IdTipoInstrumento",
	"NombreBolsa",
	"NombreConcepto" AS "TipoOperacion",
	"IdCondicionLiquidacion",
	"IdMoneda",
	"NombreTipoInstrumento",
	"NemotecnicoInstrumento",
	"Precio",
	"Monto",
	"IdEstadoDTE" AS "IdEstadoRegistro"
FROM
	"BSCL.Facturacion.BD.Vistas::ConsultarComprobante"
WHERE
	"FechaComprobante" = :FechaInicio AND "IdEstadoComprobante" <> 'ANU';