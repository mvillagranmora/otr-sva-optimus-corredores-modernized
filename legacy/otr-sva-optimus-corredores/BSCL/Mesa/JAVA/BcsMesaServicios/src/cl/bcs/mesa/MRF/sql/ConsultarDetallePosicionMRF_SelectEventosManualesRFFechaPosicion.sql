SELECT 
	CAST(0 AS BIGINT) AS "FolioComprobante",
	CONMETEVEPOS."NombreEvento"  AS "TipoOperacion",
	CAST(posManual."Fecha" AS VARCHAR(10)) as "FechaComprobante", 
	CAST(posManual."Fecha" AS VARCHAR(10)) as "FechaLiquidacion",
	'-' AS "DocumentoIdentidad",
	CASE 
        WHEN posManual."MontoEvento" > 0 THEN 'S'
        ELSE 'R'
    END AS "SumaResta",
	posManual."MontoEvento" AS "MontoLiquidacionFinal"
FROM "BSCL.Mesa.BD::MES.PosicionMRF" posManual
INNER JOIN "BSCL.Mesa.BD::MES.MetodoEventoPosicion" metEvePos ON posManual."IdMatriz" = metEvePos."IdMatriz"
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarMetodoEventoPosicion" ConMetEvePos ON posManual."IdMatriz" = ConMetEvePos."IdMatriz"
INNER JOIN "BSCL.Mesa.BD::MES.MetodoPosicion" metPos ON metEvePos."IdMetodo" = metPos."IdMetodo"
WHERE TO_VARCHAR(posManual."Fecha", 'YYYY-MM-DD') = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD')
AND (metPos."IdTipoPosicion" <> :IdTipoPosicion OR (:IdTipoPosicion = '' AND metPos."IdTipoPosicion" <> 'INICIAL')) AND	posManual."MontoEvento" <> 0 

