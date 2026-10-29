SELECT "IdMovimientoCarteraEnTransito","IdEstado","FolioComprobante"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
WHERE "FolioComprobante" IN (:idsComprobante) AND "IdModulo" = 'FAC';
