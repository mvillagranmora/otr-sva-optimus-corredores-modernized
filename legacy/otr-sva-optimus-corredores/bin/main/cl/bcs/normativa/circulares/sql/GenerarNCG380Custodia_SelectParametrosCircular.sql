WITH Parametros AS (
	SELECT "IdSolicitudCircular","TipoParametro","Valor","IdSistema","OrigenIdSistema"
	FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" 
	WHERE "IdSolicitudCircular" = :IdSolicitudCircular 
	AND "TipoParametro" IN ('EMP','FINI','FTER','RUT','CLAINS','NEMO')
),
ConteoNemotecnico AS (
	SELECT COUNT("Valor") AS "ConteoNemotecnico", "TipoParametro"
	FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" 
	WHERE "IdSolicitudCircular" = :IdSolicitudCircular
	AND "TipoParametro" IN ('NEMO')
	AND COALESCE("Valor", '') <> ''
	GROUP BY "TipoParametro"
)

SELECT Par."IdSolicitudCircular",
	CASE WHEN Par."TipoParametro" = 'EMP' THEN 'IdEmpresa'
		WHEN Par."TipoParametro" = 'FINI' THEN 'FechaInicio'
    	WHEN Par."TipoParametro" = 'FTER' THEN 'FechaTermino'
    	WHEN Par."TipoParametro" = 'RUT' THEN 'IdCliente'
    	WHEN Par."TipoParametro" = 'CLAINS' THEN 'Mercado'
    	WHEN Par."TipoParametro" = 'NEMO' AND Conteo."ConteoNemotecnico" > 1 THEN 'Nemotecnico'
    	WHEN Par."TipoParametro" = 'NEMO' THEN 'IdInstrumento'
    	ELSE NULL END AS "Parametro",
	CASE WHEN Par."TipoParametro" = 'EMP' THEN Par."Valor" 
		WHEN Par."TipoParametro" = 'FINI' THEN Par."Valor"
    	WHEN Par."TipoParametro" = 'FTER' THEN Par."Valor"
    	WHEN Par."TipoParametro" = 'RUT' THEN Par."IdSistema"
    	WHEN Par."TipoParametro" = 'CLAINS' THEN Par."Valor"
    	WHEN Par."TipoParametro" = 'NEMO' AND Conteo."ConteoNemotecnico" > 1 THEN Par."Valor"
    	WHEN Par."TipoParametro" = 'NEMO' THEN  COALESCE(Par."IdSistema",'0')
    	ELSE NULL END AS "Valor"
FROM Parametros AS Par
LEFT JOIN ConteoNemotecnico AS Conteo ON Par."TipoParametro" = Conteo."TipoParametro";