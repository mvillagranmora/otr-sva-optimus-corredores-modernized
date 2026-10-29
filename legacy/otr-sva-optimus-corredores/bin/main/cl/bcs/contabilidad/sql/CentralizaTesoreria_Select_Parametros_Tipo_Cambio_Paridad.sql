SELECT 
PAR."IdMoneda"
,PAR."Fecha"
,PAR."ParidadMercado"
,PAR."TipoCambioMercado"
FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" DEF
INNER JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR ON PAR."IdMoneda" = DEF."IdParametroFinanciero"
WHERE DEF."IdParametroFinanciero" IN (:IdParametroFinanciero) AND PAR."Fecha" BETWEEN TO_SECONDDATE(:FechaMinima) AND TO_SECONDDATE(:FechaMaxima) AND NOT PAR."TipoCambioMercado" = 0 
ORDER BY PAR."Fecha" DESC
LIMIT 1;