SELECT 
	CAR."IdCartera",
	COALESCE(MOV."IdMovimientoCarteraEnTransito", 0 ) AS "IdMovimientoCarteraEnTransito",
	COALESCE(MOV."IdEstado",'') AS "IdEstado",
	CAR."EGRIdTipoOperacion",
	COALESCE(MOV."ExcluyeMovimientoEnCartola",'N') AS "ExcluyeMovimientoEnCartola"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV ON MOV."IdMovimientoCarteraEnTransito" = CAR."EGRIdMovimientoCarteraEnTransito"
WHERE CAR."IdCartera" IN (:idsCartera);