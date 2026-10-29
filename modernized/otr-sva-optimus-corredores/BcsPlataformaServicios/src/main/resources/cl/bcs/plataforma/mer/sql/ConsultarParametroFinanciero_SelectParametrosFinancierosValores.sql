SELECT
"IdParametroFinanciero"
,TO_VARCHAR("Fecha",'YYYY-MM-DD') AS "Fecha"
,"AgnoMes"
,"Agno"
,"Valor"
FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero"
WHERE "IdParametroFinanciero" IN (:IdParametroFinanciero) AND "Fecha" BETWEEN :FechaDesde AND :FechaHasta