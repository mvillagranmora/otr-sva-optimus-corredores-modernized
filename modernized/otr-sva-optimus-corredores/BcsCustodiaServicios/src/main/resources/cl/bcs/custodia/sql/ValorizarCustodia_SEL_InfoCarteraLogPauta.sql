SELECT
	'CARTERA'          AS "TablaOrigen",
	CAR."IdCartera"    AS "IdOrigen",
	CAR."FolioCartera" AS "FolioOrigen",
	CAR."Nemotecnico"  AS "Nemotecnico"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
WHERE CAR."IdCartera" IN (:IdsCartera)