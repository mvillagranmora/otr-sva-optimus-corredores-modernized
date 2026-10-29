SELECT	"IdOperacionSpot",  
		"IdTipoOperacion", 
		"IdMonedaPrincipal", 
		"IdMonedaSecundaria", 
		"MontoMonedaPrincipal", 
		"MontoMonedaSecundaria", 
		TO_DATE("FechaOperacion") AS "FechaOperacion"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot"
WHERE "IdOperacionSpot" IN (:ids)