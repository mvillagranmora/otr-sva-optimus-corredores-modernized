SELECT 
	TO_VARCHAR("Fecha",'YYYY-MM-DD') AS "Fecha"
	,"IdIndiceFinanciero"
	,"AgnoMes"
	,"Agno"
	,"Valor"
FROM "BSCL.Plataforma.BD::MER.IndiceFinanciero"
WHERE "Fecha" IN (:Fecha) AND "IdIndiceFinanciero" IN (:IdIndiceFinanciero)