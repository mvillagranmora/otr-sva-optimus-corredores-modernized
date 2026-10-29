SELECT 
    ope."AfectaCustodia",
    ope."IdTipoOperacion",
    res."FolioCarteraOrigen" AS "FolioCarteraOrigenDetalle",
    res."IdCarteraOrigen" AS "IdCarteraOrigenDetalle"
FROM "BSCL.Mesa.BD::MES.Operacion" ope
INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" res ON (ope."IdOperacion" = res."IdOperacion" AND res."IdEstado" IN ('VIG','LIQ'))
WHERE ope."IdOperacion" IN (:IdOperacion) AND ope."IdEstado" = 'VIG'