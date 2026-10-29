SELECT "IdTransaccion",
	"Folio" AS "FolioTransaccion",
	"FechaSistemaCreacion" AS "FechaTransaccion",
	"IdEstado",
	"Cantidad",
	"Monto",
	"Nemotecnico",
	"IdBolsa",
	"Precio",
	"IdMoneda",
	"IdTipoInstrumento",
	"IdClasificacionTipoInstrumento"
FROM "BSCL.Orden.BD::ORD.Transaccion"
WHERE "IdTransaccion" IN (:ids);