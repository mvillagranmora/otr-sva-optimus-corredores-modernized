WITH config AS (
    SELECT :p_IdEsquema AS IdNormativa,
           CASE
               WHEN :p_IdEsquema = 'REPO2P1' THEN 'BCC_REPO2P1_CODIGOREPORTE'
               ELSE 'BCC_REPO3P1_CODIGOREPORTE'
           END AS CodigoParametro,
           NULL AS ValorDefault
    FROM DUMMY

    UNION ALL
    SELECT :p_IdEsquema,
     		CASE
               WHEN :p_IdEsquema = 'REPO2P1' THEN 'BCC_SICAM_CODOPERACIONTOKEN2P1'
               ELSE 'BCC_SICAM_CODOPERACIONTOKEN'
           	END,
     		'RQT01' AS ValorDefault
    FROM DUMMY
    UNION ALL
    SELECT :p_IdEsquema,
    		CASE
               WHEN :p_IdEsquema = 'REPO2P1' THEN 'BCC_SICAM_CODOPERACIONTRANSACCION2P1'
               ELSE 'BCC_SICAM_CODOPERACIONTRANSACCION'
           	END,
           	'RQT02' AS ValorDefault
    FROM DUMMY
)
SELECT 
    CO.CodigoParametro AS "CodigoParametro",
    COALESCE(C."Valor", CO.ValorDefault) AS "Valor"
FROM config CO
LEFT JOIN "BSCL.Normativa.BD::NOR.Configuracion" C
  ON C."IdNormativa" = CO.IdNormativa
 AND C."CodigoParametro" = CO.CodigoParametro;



