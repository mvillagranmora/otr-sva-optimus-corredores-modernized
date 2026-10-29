SELECT 
	"IdPais" AS "IdPais", 
	"NombrePais" AS "NombrePais"
		FROM "BSCL.Plataforma.BD::REG.Pais"
			WHERE "IdPais" IN (:IdPais)
