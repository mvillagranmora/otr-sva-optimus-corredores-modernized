SELECT 
"IdParametroFinanciero"
,"Periodo"
,"IdMonedaParametro"
,"IdMonedaValor"
FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero"
WHERE "IdMonedaParametro" NOT IN ('','USD') AND "IdMonedaValor" = :IdMonedaLocal;