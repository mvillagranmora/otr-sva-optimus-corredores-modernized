SELECT 
	posManual."IdPosicion", 
	CAST(posManual."Fecha" AS VARCHAR(10)) as "Fecha", 
	posManual."IdMatriz", 
	posManual."MontoEvento", 
	metPos."IdTipoPosicion" --INICIAL/DIARIA
FROM "BSCL.Mesa.BD::MES.PosicionMRF" posManual
INNER JOIN "BSCL.Mesa.BD::MES.MetodoEventoPosicion" metEvePos ON posManual."IdMatriz" = metEvePos."IdMatriz"
INNER JOIN "BSCL.Mesa.BD::MES.MetodoPosicion" metPos ON metEvePos."IdMetodo" = metPos."IdMetodo"
WHERE posManual."Fecha" = TO_DATE(:p_FechaPosicion, 'YYYY-MM-DD')
AND (metPos."IdTipoPosicion" = :IdTipoPosicion OR (:IdTipoPosicion = '' AND metPos."IdTipoPosicion" <> 'INICIAL'))
AND posManual."IdMatriz" = :p_IdMatriz
