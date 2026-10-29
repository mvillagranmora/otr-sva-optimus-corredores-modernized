SELECT "IdParametroFinanciero","Periodo","Nombre","Descripcion","IdMonedaParametro","IdMonedaValor"
FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero"
WHERE "Periodo" = :TipoPeriodo AND "IdMonedaValor" != ''