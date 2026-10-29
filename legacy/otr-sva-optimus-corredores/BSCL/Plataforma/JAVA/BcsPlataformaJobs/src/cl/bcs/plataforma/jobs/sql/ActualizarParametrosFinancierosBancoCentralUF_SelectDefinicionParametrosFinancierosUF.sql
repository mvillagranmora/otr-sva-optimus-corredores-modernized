SELECT "IdParametroFinanciero",
       "Periodo",
       "IdMonedaParametro",
       "IdMonedaValor",
       "CodigoSerieBancoCentral"
FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero"
WHERE "IdParametroFinanciero" = 'UF';