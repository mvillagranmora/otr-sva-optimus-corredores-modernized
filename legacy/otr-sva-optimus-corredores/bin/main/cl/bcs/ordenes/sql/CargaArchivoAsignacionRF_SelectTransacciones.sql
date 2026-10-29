SELECT tran."IdTransaccion", tipoope."AccionOrden","Folio","FechaSistemaCreacion","IdClasificacionTipoInstrumento","Nemotecnico","EsFungible"
FROM "BSCL.Orden.BD::ORD.Transaccion" tran
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on tipoope."IdTipoOperacion" = tran."IdTipoOperacion"
WHERE "Folio" in (:foliosTransaccion);
