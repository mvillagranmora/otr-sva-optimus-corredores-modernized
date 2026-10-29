SELECT "IdEvento", "IdTipoEvento", "NumeroOperacionConcepto", "IdEstado"
FROM "BSCL.Plataforma.BD::AUT.Evento"
WHERE "NumeroOperacionConcepto" = :IdComprobante AND "IdEstado" IN ('AUT','VIG')