SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxCarteras";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxCarteras"  AS
	SELECT
		 "FolioCartera", "fecha", MAX("IdCartera") AS "IdCartera", "EventoReporte"
	FROM 
		(
		SELECT 'CES' AS "EventoReporte", * FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxVCD" 
		UNION
		SELECT 'AOC' AS "EventoReporte", * FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxLIQ" 
		UNION
		SELECT 'AOC' AS "EventoReporte", * FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxANT" 
		UNION
		SELECT 'OMD' AS "EventoReporte", * FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxMOD" 
		--UNION
		--SELECT * FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxVIG" 
		)
	GROUP BY "FolioCartera", "fecha", "EventoReporte"	
	ORDER BY "FolioCartera", "fecha", "IdCartera"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxCarteras" TO bcs_desarrollador WITH GRANT OPTION;
