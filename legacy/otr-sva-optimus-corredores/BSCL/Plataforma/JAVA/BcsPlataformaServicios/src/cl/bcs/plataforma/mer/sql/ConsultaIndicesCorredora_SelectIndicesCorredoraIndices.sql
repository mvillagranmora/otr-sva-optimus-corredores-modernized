SELECT "IdIndiceFinanciero"  as "IdIndiceFinanciero","Periodo","Nombre","Descripcion","IdMonedaParametro","IdMonedaValor", 'USU' as "TipoIndice"
FROM "BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero"
WHERE "Periodo" = :TipoPeriodo --AND COALESCE("IdMonedaValor",'') = ''
UNION
SELECT "IdParametroFinanciero" as "IdIndiceFinanciero","Periodo","Nombre","Descripcion","IdMonedaParametro","IdMonedaValor",'SIS' as "TipoIndice"
FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero"
WHERE "Periodo" = :TipoPeriodo AND COALESCE("IdMonedaValor",'') = ''