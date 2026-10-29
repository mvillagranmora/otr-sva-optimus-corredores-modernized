SELECT "IdUso" || '_' || "IdParametroFinanciero" AS "IdUsoIdParametroFinanciero", "IdUso", "IdParametroFinanciero", "Prioridad" 
FROM "BSCL.Plataforma.BD::MER.UsoParidadParametroFinanciero"
WHERE "IdUso" = :IdUso AND ("IdParametroFinanciero" = :IdParametroFinanciero OR "Prioridad" = :Prioridad)
ORDER BY "IdUso","Prioridad" ASC;