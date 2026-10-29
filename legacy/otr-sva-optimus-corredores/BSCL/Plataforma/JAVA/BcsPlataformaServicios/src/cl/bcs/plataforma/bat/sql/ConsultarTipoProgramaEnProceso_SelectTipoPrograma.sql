SELECT 
	TP."IdTipoPrograma", 
	'S' AS "Existe" 
FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" AS TP
WHERE TP."IdTipoPrograma" IN (:idTipoProgramas);