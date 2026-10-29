SELECT
"IdIndiceFinanciero" as "IdIndiceFinanciero"
,TO_VARCHAR("Fecha",'YYYY-MM-DD') AS "Fecha"
,"AgnoMes"
,"Agno"
,"Valor"
FROM "BSCL.Plataforma.BD::MER.IndiceFinanciero" 
WHERE "IdIndiceFinanciero" IN (:IdIndiceFinanciero) AND "Fecha" BETWEEN :FechaDesde AND :FechaHasta
UNION
SELECT
"IdParametroFinanciero" as "IdIndiceFinanciero"
,TO_VARCHAR("Fecha",'YYYY-MM-DD') AS "Fecha"
,"AgnoMes"
,"Agno"
,"Valor"
FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero"	
WHERE "IdParametroFinanciero" IN (:IdIndiceFinanciero) AND "Fecha" BETWEEN :FechaDesde AND :FechaHasta