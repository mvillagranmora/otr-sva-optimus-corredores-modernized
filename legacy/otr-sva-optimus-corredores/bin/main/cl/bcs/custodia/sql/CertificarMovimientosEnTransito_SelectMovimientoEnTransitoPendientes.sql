SELECT "IdMovimientoCarteraEnTransito","IdEstado" ,"IdClasificacionTipoInstrumento","IdConcepto" ,"IdTipoOperacion"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito" 
WHERE "IdEstado" IN ('PDA','PEN','MAU') 
AND "IdMovimientoCarteraEnTransito" IN (:idsMovimientoCarteraEnTransito); 
