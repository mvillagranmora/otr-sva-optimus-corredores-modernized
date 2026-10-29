SELECT "IdOrden",
	"IdEstadoOrden",
	"Cantidad",
	"MontoOrden",
	"Nemotecnico",
	"DocumentoIdentidad",
	"PrecioOrden",
	"IdMonedaOrden",
	"IdTipoInstrumento",
	"IdClasificacionTipoInstrumento"
FROM "BSCL.Orden.BD::ORD.Orden"
WHERE "IdOrden" IN (:ids);
