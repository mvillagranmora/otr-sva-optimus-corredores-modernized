SELECT "IdUso" || '_' || "IdParametroFinanciero" AS "IdUsoIdParametroFinanciero", "IdUso", "IdParametroFinanciero", "Prioridad" 
FROM "BSCL.Plataforma.BD::MER.UsoParidadParametroFinanciero"
WHERE "IdUso" IN (:IdUso)
ORDER BY "IdUso","Prioridad" ASC;