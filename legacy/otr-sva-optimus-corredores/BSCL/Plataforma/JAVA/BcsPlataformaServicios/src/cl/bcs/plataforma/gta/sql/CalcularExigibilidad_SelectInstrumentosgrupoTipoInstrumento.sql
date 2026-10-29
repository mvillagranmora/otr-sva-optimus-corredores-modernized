SELECT 
	 INS."Nemotecnico"
	,INS."IdInstrumento"
	,INS."IdMoneda"
	,INS."IdTipoInstrumento" AS "IdTipoInstrumento"
	,COALESCE ( CASE WHEN IFNULL(GRU."CodigoClasificacion",'')=''THEN INS."ValorClasificacionRiesgo" ELSE '' END,'')     AS "ClasificacionRiesgo"
	,COALESCE ( CASE WHEN IFNULL(GRU."CodigoClasificacion",'')=''THEN INS."IdEntidadClasificadoraRiesgo" ELSE '' END,'') AS "IdEntidadClasificadoraRiesgo"
	,IFNULL(INS."IdTipoClasificacionRiesgo",'')    																		 AS "IdTipoClasificacionRiesgo"
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS 
LEFT JOIN "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" AS GRU ON INS."IdTipoInstrumento" =GRU."IdTipoInstrumento"
WHERE INS."IdInstrumento" IN (:IdInstrumento);

