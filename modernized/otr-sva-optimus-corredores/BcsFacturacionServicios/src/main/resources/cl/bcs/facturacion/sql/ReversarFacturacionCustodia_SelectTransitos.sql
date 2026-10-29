SELECT "IdMovimientoCarteraEnTransito","IdEstado","FolioComprobante"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
WHERE ("IdCarga" > 0 AND "IdModulo" = 'CUS' AND "FolioComprobante" IN (:idsComprobante)) OR ("FolioComprobante" IN (:idsComprobante) AND "IdModulo" = 'FAC');