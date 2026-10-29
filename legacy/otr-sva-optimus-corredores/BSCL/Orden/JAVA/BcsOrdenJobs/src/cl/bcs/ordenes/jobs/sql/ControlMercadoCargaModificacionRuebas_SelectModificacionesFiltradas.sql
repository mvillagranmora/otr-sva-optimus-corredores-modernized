WITH MODIFICACIONES AS (
SELECT
	TEMP_RU."FolioMod",
	TEMP_RU."FechaMod",
	TEMP_RU."Fecha",
	TEMP_RU."CodComNuevo",
	TEMP_RU."CodVenNuevo",
	ROW_NUMBER() OVER (
            PARTITION BY TR."Folio", TEMP_RU."FechaMod"
            ORDER BY TEMP_RU."FechaMod" DESC
    ) AS rnk,
    TR."FechaOperacion",
    TR."IdTransaccionOd"
	FROM #TEMP_MODIFICACIONES_RUEBAS TEMP_RU
	INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" TR ON TR."FechaSistemaCreacion"=TEMP_RU."Fecha" AND TR."Folio"=TEMP_RU."FolioMod" AND TR."IdEstado" <> 'ANU'
	AND (
		(CASE WHEN LEFT(TR."IdTipoOperacion",3) = 'COM' THEN LTRIM(TR."CodigoOperadorCorredor",0) ELSE '' END) <> TO_NVARCHAR(TEMP_RU."CodComNuevo")
		OR (CASE WHEN LEFT(TR."IdTipoOperacion",3) = 'COM' THEN LTRIM(TR."CodigoOperadorContraparte",0) ELSE '' END) <> TO_NVARCHAR(TEMP_RU."CodVenNuevo")
		OR (CASE WHEN LEFT(TR."IdTipoOperacion",3) = 'VEN' THEN LTRIM(TR."CodigoOperadorCorredor",0) ELSE '' END) <> TO_NVARCHAR(TEMP_RU."CodVenNuevo")
		OR (CASE WHEN LEFT(TR."IdTipoOperacion",3) = 'VEN' THEN LTRIM(TR."CodigoOperadorContraparte",0) ELSE '' END) <> TO_NVARCHAR(TEMP_RU."CodComNuevo")
	)
)
SELECT 
    "FolioMod",
    "FechaMod",
    "Fecha",
    "CodComNuevo",
    "CodVenNuevo"
FROM MODIFICACIONES 
WHERE rnk = 1
ORDER BY "FechaMod" DESC;