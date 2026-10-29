SELECT INS."IdInstruccion"
     , INS."IdCliente"
     , INS."IdPortafolioCliente"
	 , INS."IdEstadoInstruccion"
	 , INS."IdTransaccion"
	 , INS."IdOrden"
	 , INS."IdAsignacion"
	 , INS."IdComprobante"
	 , INS."IdMovimientoComprobante"
	 , COMP."IdTipoComprobante"
FROM "BSCL.Orden.BD::ORD.Instruccion" INS
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COMP ON (COMP."IdComprobante" = INS."IdComprobante")
WHERE INS."IdInstruccion" IN (:IdsInstrucciones);