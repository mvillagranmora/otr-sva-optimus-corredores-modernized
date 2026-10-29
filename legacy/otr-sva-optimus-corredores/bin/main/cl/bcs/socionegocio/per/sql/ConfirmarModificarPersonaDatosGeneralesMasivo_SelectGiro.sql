SELECT 
	"IdGiro" AS "IdGiro",
	"Nombre" AS "Nombre" 
		FROM "BSCL.Plataforma.BD::REG.Giro"
			WHERE "IdGiro" IN (:IdGiro)