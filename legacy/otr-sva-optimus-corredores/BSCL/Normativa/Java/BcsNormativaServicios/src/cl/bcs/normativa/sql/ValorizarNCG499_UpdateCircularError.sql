UPDATE NCG
  SET
    NCG."IndicadorValorizado"		= 'E'
	,NCG."ValResultado"			  	= 'ERROR: JAVA EXCEPTION'
FROM "BSCL.Custodia.BD::CUS.CircularCarteraCMF" AS NCG
WHERE NCG."IdCircularCartera" IN (:IdValorizacion);