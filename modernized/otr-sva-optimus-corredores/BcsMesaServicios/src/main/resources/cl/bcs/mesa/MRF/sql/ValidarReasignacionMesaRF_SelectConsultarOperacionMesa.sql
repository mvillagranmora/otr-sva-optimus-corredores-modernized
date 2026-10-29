SELECT 	"FolioOperacion",
		"NombreOperacion",
		"IdOperacion",
		CAST ("FechaOperacion" AS DATE) AS "FechaOperacion",
		"DocumentoIdentidad",
		"CodigoPortafolio",
		"MontoOperacion",
		"Tasa",
		CAST ("FechaVencimiento" AS DATE) AS "FechaVencimiento",
		COALESCE("Glosa", '') AS "Glosa"
FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones"
WHERE "FolioOperacion" = :FolioOperacion