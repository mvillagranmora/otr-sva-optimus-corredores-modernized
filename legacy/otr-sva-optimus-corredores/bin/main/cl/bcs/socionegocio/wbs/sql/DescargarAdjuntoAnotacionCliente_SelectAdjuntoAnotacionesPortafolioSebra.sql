SELECT
	CAST(DA.dan_id AS BIGINT) AS "IdAnotacion"
	, COALESCE(AA.adj_nombre, '') AS "NombreArchivo"
	, COALESCE(AA.adj_adjunto, '') AS "Archivo" 
FROM DETALLE_ANOTACION AS DA
LEFT JOIN ANOTACION_ADJUNTO AS AA ON AA.dan_id = DA.dan_id
WHERE DA.dan_id IN (:IdAnotacion);