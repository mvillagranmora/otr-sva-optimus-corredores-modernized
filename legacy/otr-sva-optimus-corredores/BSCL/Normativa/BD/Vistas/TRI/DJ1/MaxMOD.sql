SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxMOD";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxMOD"  AS
	SELECT 
		"FolioCartera",
		"IdEstado",
		"FechaVencimientoOperacion" AS "fecha",
		--CAST("FechaVencimientoOperacion" AS CHAR(7)) AS "fecha",
		MAX("IdCartera") AS "IdCartera"
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite"
	WHERE "IdTipoProducto" = 'MEFW'
	AND "IdEstado" = 'MOD'
	GROUP BY "FolioCartera", "IdEstado", "FechaVencimientoOperacion"
	--GROUP BY "FolioCartera", "IdEstado", CAST("FechaVencimientoOperacion" AS CHAR(7))
	ORDER BY "FolioCartera"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxMOD" TO bcs_desarrollador WITH GRANT OPTION;
