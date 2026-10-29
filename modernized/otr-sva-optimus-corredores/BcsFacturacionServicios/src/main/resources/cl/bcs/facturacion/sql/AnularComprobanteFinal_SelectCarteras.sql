SELECT "Cantidad","IdCliente","IdPortafolio","ValorRescate","IdEstado","IdInstrumento","FolioComprobante","IdTipoComprobante","IdModulo","Nemotecnico","INGCantidad","EGRCantidad",
"EGRFolioComprobante","IdTipoOperacion","IdCartera","FolioCartera",'CARTERA' as "TipoCartera"
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "IdModulo" = 'FAC' 
AND ("FolioComprobante" IN (:idsComprobante) OR "EGRFolioComprobante" IN (:idsComprobante)) 