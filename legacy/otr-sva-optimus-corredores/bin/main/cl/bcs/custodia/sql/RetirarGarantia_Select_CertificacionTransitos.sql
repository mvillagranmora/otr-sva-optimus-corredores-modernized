SELECT 
"IdMovimientoCarteraEnTransito"
,"IdTipoOperacion"
,'S' AS "MovimientoGarantia"
,'GAR' as "Origen" 
,"IdClasificacionTipoInstrumento"
,"Cantidad"
,"ValorRescate" 
,"IdOperacion"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
WHERE "IdOperacion" IN (:IdOperacion)
AND "IdClasificacionTipoInstrumento"  IN ('IF','RF')