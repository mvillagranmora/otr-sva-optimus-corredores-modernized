SELECT 	
	"IdCiudad" AS "IdCiudad",
	"NombreCiudad" AS "NombreCiudad" 
		FROM "BSCL.Plataforma.BD::REG.Ciudad"
			WHERE "IdCiudad" IN (:IdCiudad)
			