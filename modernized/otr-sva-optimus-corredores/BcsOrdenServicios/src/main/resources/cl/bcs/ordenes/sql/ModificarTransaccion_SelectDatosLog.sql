SELECT 
	"IdTransaccion",
    "Folio" as "FolioTransaccion", 
	"Nemotecnico" as "Instrumento", 
	"IdTipoOperacion",
	"IdMoneda" as "IdMonedaTransaccion",
	"Monto" as "MontoTransaccion",
	"Precio" as "PrecioTransaccion",
	"Cantidad" as "CantidadTransaccion", 
	"IdEstado" as "IdEstadoTransaccion" 
FROM "BSCL.Orden.BD::ORD.Transaccion"
WHERE "IdTransaccion" IN (:IdTransaccion)