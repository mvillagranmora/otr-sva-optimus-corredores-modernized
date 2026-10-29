SELECT "FolioComprobante", "IdMovimientoCarteraEnTransito" , "IdTipoOperacion"  
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" 
WHERE "FolioComprobante" IN (:FolioCoprobante)