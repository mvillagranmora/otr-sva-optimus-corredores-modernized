SELECT "IdTransaccion","Folio","FechaSistemaCreacion","AccionOrden", "IdClasificacionTipoInstrumento"
FROM "BSCL.Orden.BD::ORD.Transaccion" TRAN
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIPOINS ON TRAN."IdTipoOperacion" = TIPOINS."IdTipoOperacion"
WHERE TRAN."Folio" || TRAN."FechaSistemaCreacion" || TRAN."IdBolsa" IN (:transaccionKeys);
