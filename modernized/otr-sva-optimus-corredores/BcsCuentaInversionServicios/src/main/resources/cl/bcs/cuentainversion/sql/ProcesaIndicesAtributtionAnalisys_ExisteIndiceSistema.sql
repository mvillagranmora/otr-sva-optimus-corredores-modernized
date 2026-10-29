SELECT count(*) as "Existe"
FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero"
WHERE "IdParametroFinanciero"=:p_IdIndiceFinanciero;