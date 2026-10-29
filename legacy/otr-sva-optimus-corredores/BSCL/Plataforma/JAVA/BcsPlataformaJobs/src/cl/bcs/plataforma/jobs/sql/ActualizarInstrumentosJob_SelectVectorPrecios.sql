WITH UltimaFecha AS(
	SELECT
		MAX(fecha) AS fecha
	FROM PT$VectorPrecios
)
SELECT
	VP.FECHA,
	NEMO,
	SEC_LISTA,
	TIPO,
	VALOR,
	VALOR_95,
	VALOR_98,
	VALOR_01,
	VALOR_02,
	VALOR_03,
	VALOR_04,
	VALOR_05,
	PRECIO_BASE
FROM PT$VectorPrecios VP
LEFT JOIN UltimaFecha F ON VP.fecha = F.fecha
WHERE F.fecha is not null