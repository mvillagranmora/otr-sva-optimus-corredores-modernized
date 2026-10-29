

SELECT 
 "IdMovimientoCarteraEnTransito" AS "IdMovimientoCarteraTransito"
,"IdTipoOperacion"
,"IdEstado"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
WHERE "IdTipoComprobante"='MES' 
AND   "IdEstado" = 'PEN'
AND   "FolioComprobante" IN (:IdOperacion)