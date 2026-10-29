INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado"
("IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais", "IdEstado")
SELECT 
--Funcion que sirve para contar y validar los IdFerido ya que no son auto-incrementables, asi se evita duplicaciones
	ROW_NUMBER() OVER (ORDER BY t."fecha") + 
	(SELECT COALESCE(MAX("IdFeriado"), 0) FROM "BSCL.Plataforma.BD::REG.DiaFeriado") AS "IdFeriado",  
	'FERIADO' AS "Tipo",  
	TO_DATE(t."fecha", 'YYYY-MM-DD') AS "Fecha",  
		CASE
			WHEN DAYOFWEEK(TO_DATE(t."fecha", 'YYYY-MM-DD')) = 2 THEN 0  -- Lunes
			WHEN DAYOFWEEK(TO_DATE(t."fecha", 'YYYY-MM-DD')) = 3 THEN 1  -- Martes
			WHEN DAYOFWEEK(TO_DATE(t."fecha", 'YYYY-MM-DD')) = 4 THEN 2  -- Miércoles
			WHEN DAYOFWEEK(TO_DATE(t."fecha", 'YYYY-MM-DD')) = 5 THEN 3  -- Jueves
			WHEN DAYOFWEEK(TO_DATE(t."fecha", 'YYYY-MM-DD')) = 6 THEN 4  -- Viernes
			WHEN DAYOFWEEK(TO_DATE(t."fecha", 'YYYY-MM-DD')) = 7 THEN 5  -- Sábado
			WHEN DAYOFWEEK(TO_DATE(t."fecha", 'YYYY-MM-DD')) = 1 THEN 6  -- Domingo
		ELSE NULL END AS "Dia",
	'Feriado Cargado desde Ruebas' AS "DescripcionDia",  
	YEAR(TO_DATE(t."fecha", 'YYYY-MM-DD')) AS "AgnoCarga",  
	44 AS "IdPais",  
	'ACT' AS "IdEstado"
FROM #p_TempFeriados t
WHERE NOT EXISTS (
	SELECT 1 
	FROM "BSCL.Plataforma.BD::REG.DiaFeriado" d  
	WHERE d."Tipo" = 'FERIADO'  
	AND d."Fecha" = TO_DATE(t."fecha", 'YYYY-MM-DD')
);
