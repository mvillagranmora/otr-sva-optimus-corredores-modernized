SELECT INST."IdInstruccion"
	 , TRAN."IdTransaccion"
	 , INST."IdComprobante"  AS "IdComprobanteFacturacion"
	 , TCOMP."IdComprobante" AS "IdComprobanteTesoreria"
	 , TCOMP."IdEstado"
FROM "BSCL.Orden.BD::ORD.Instruccion" INST
	 LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRAN ON (TRAN."IdTransaccion" = INST."IdTransaccion" AND TRAN."IdTransaccion" IS NOT NULL)
	 LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" TCOMP ON (TCOMP."IdComprobante" = TRAN."IdComprobanteTesoreria" AND TCOMP."IdComprobante" IS NOT NULL)
WHERE INST."IdInstruccion" IN (:IdsInstrucciones);