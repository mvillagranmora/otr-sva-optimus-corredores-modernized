SELECT A."IdOperacionSpot", B."IdMovimientoComprobante"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" A
	INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" B 
		ON A."IdOperacionSpot" = B."IdTransaccion" AND A."IdInstrumento" = B."IdInstrumento"