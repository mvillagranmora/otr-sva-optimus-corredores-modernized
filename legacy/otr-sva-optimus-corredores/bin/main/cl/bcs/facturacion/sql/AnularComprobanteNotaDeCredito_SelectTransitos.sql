SELECT "IdMovimientoCarteraEnTransito","IdEstado","FolioComprobante","EsNominal"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
WHERE "FolioComprobante" IN (:idsComprobante) AND "IdModulo" = 'FAC';