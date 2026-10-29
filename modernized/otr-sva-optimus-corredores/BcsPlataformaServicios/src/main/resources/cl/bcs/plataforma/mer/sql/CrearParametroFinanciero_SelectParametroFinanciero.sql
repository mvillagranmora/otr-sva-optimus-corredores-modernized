SELECT 
TO_VARCHAR("Fecha",'YYYY-MM-DD') AS "Fecha"
,"IdParametroFinanciero"
,"Valor"
FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero"
WHERE "Fecha" IN (:Fecha) AND "IdParametroFinanciero" IN (:IdParametroFinanciero)