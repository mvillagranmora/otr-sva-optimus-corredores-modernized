SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxLIQ";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxLIQ"  AS
	SELECT 
		CAR."FolioCartera",
		CAR."IdEstado",
		CAR."FechaVencimientoOperacion" AS "fecha",
		MAX(CAR."IdCartera") AS "IdCartera"
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CAR
	INNER JOIN "BSCL.Mesa.BD::MES.Operacion" OPE ON OPE."IdOperacion" =  CAR."IdOperacion"
	WHERE CAR."IdTipoProducto" = 'MEFW'
	AND CAR."IdEstado" = 'LIQ'
	AND OPE."IdEstado" = 'LIQ'
	GROUP BY CAR."FolioCartera", CAR."IdEstado", CAR."FechaVencimientoOperacion"
	ORDER BY CAR."FolioCartera"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1.MaxLIQ" TO bcs_desarrollador WITH GRANT OPTION;
