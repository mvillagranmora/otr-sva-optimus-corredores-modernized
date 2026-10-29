SELECT DISTINCT TRAN."IdTransaccion", CM."TipoMensaje", TRAN."FechaSistemaCreacion"
FROM "BSCL.Orden.BD::ORD.ControlMercado" CM 
INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" TRAN ON CM."IdMensaje1" = TO_VARCHAR(TRAN."FechaSistemaCreacion") AND CM."IdMensaje2" = TO_VARCHAR(TRAN."IdTransaccion")
WHERE CM."TipoMensaje" IN ('TRANSACCION_OPERADOR', 'ASIGNACION_DIRECTA', 'SHORTNAME')
AND TRAN."IdTransaccion" IN (:IdTransaccion)
AND TRAN."FechaSistemaCreacion" = :FechaSistemaCreacion;