SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxVIG";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxVIG"  AS
	--SELECT "FolioCartera", CAST("FechaOperacion" AS CHAR(7)) AS "fecha", "IdCartera", 'NUE' AS "EventoReporte"
	SELECT
		"FolioCartera",
		"FechaOperacion" AS "fecha",
		MAX("IdCartera") AS "IdCartera",
		'NUE' AS "EventoReporte"
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite"
	WHERE "IdTipoProducto" = 'MEFW'
	AND "IdEstado" = 'VIG'
	AND "FolioCartera" NOT IN (SELECT "FolioCartera" FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxCarteras")
	GROUP BY "FolioCartera", "FechaOperacion"
		ORDER BY "FolioCartera", "fecha", "IdCartera"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxVIG" TO bcs_desarrollador WITH GRANT OPTION;