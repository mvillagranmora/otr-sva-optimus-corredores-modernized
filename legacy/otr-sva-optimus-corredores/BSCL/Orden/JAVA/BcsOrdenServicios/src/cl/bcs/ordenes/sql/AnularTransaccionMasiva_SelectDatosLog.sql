SELECT 
	"IdTransaccion",
	"Folio",
	"Nemotecnico",
	"IdTipoOperacion",
	"IdMoneda",
	"Monto",
	"Precio",
	"Cantidad"
FROM "BSCL.Orden.BD::ORD.Transaccion" 
WHERE "IdTransaccion" IN (:IdTransaccion)