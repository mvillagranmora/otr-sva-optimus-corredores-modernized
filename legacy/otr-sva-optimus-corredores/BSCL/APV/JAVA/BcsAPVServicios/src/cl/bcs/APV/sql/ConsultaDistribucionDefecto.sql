
SELECT CAST(0 AS BIGINT) AS "IdContratoSAAPV"
	,'FFM' AS "IdTipoInstrumento"
	,FON."IdInstrumento"
	,FON."IdSerieFondo" AS "Nemotecnico"
	,CAST(0 AS DECIMAL(26,8)) AS "MontoPesos"
	,CAST(100 AS DECIMAL(26,8)) AS "Porcentaje" 
	,'VIG' AS "IdEstado"
	,COALESCE(FON."IdMoneda",'CLP') AS "IdMoneda"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo"  FON
INNER JOIN "BSCL.APV.BD::PLA.ConfiguracionAPV" Param 
ON Param."CodigoParametro" = 'APV.FondoDistribucionDefecto'
AND FON."IdSerieFondo" = Param."Valor"

