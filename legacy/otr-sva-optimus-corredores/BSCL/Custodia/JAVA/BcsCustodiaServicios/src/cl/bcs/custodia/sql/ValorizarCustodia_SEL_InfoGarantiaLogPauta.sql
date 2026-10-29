SELECT
	'GARANTIA'          AS "TablaOrigen",
	GAR."IdGarantia"    AS "IdOrigen",
	GAR."FolioGarantia" AS "FolioOrigen",
	GAR."Nemotecnico"   AS "Nemotecnico"
FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
WHERE GAR."IdGarantia" IN (:IdsGarantia)