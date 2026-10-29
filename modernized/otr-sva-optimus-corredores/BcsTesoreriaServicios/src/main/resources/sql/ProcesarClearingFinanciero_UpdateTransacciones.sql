UPDATE "BSCL.Orden.BD::ORD.Transaccion"
	SET "IdComprobanteTesoreria" = :IdComprobanteTesoreria
WHERE "IdTransaccion" = :IdTransaccion AND "IdComprobanteTesoreria" = 0;