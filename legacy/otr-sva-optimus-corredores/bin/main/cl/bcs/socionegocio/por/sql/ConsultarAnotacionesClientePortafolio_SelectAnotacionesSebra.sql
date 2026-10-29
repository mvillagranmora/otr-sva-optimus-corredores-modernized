SELECT 
	CAST(DA.dan_id AS BIGINT) AS "IdAnotacion",
	:IdPortafolio AS "IdPortafolio",
	DA.dan_titulo AS "Titulo",
	DA.dan_nota AS "Detalle",
	CASE
		WHEN dan_ind_ambito = 'PRI' THEN 'PRIVAD' WHEN dan_ind_ambito = 'PUB' THEN 'PUBLIC' ELSE ''
	END AS "IdAmbito",
	CASE
		WHEN dan_ind_relevancia = 'S' THEN 'ALTA' WHEN dan_ind_relevancia = 'N' THEN 'NORMAL' ELSE ''
	END AS "IdRelevancia",
	CASE
		WHEN par_tan_id = 'TANOTCARTO' THEN 'ENVCAR' WHEN par_tan_id = 'TANOTEBURS' THEN 'EVTBUR' WHEN par_tan_id = 'TANOTRECOR' THEN 'RECORD' ELSE 'OTROS'
	END AS "IdMotivo",
	IIF(AA.adj_nombre IS NOT NULL, 'S', 'N') AS "ExisteArchivo",
	COALESCE(AA.adj_nombre, '') AS "NombreArchivo",
	'' AS "NombreAmbito",
	'' AS "NombreMotivo",
	'' AS "NombreRelevancia",
	CONVERT(NVARCHAR(10),DA.dan_fec_ingreso, 23) AS "FechaAnotacion",
	CONVERT(NVARCHAR(10),DA.dan_fec_ingreso, 23) AS "FechaIngreso",
	CONVERT(NVARCHAR(10),DA.dan_fec_ingreso, 23) AS "FechaCreacion",
	CONVERT(NVARCHAR(10),DA.dan_fec_ingreso, 23) AS "FechaModificacion",
	CONVERT(NVARCHAR(10),DA.dan_fec_inicio, 23) AS "ValidoDesde",
	CONVERT(NVARCHAR(10),DA.dan_fec_vence, 23) AS "ValidoHasta",
	'N' AS "Borrado",
	CAST(0 AS BIGINT) AS "IdUsuarioCreador",
	'SEBRA' AS "Origen"
FROM DETALLE_ANOTACION AS DA
LEFT JOIN ANOTACION_ADJUNTO AS AA ON AA.dan_id = DA.dan_id
LEFT JOIN PERSONA AS PER ON PER.per_id = DA.per_id
WHERE CONCAT(PER.per_rut, '-',PER.per_dv) = :DocumentoIdentidad AND DA.CUENTA = :CodigoPortafolio;