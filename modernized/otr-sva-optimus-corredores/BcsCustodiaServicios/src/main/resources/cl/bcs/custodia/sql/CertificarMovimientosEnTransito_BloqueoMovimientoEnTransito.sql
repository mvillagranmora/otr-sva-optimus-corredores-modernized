SELECT "IdMovimientoCarteraEnTransito","IdEstado" ,"IdClasificacionTipoInstrumento","IdConcepto" ,"IdTipoOperacion"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" As MvtoCarTr 
WHERE MvtoCarTr."IdEstado" IN ('PDA','PEN','MAU') 
AND MvtoCarTr."IdMovimientoCarteraEnTransito" IN (:idsMovimientoCarteraEnTransito) FOR UPDATE; 