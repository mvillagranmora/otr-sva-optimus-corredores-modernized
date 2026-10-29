SELECT 
	"IdProfesion" AS "IdProfesion",
	"Nombre" AS "Nombre" 	
		FROM "BSCL.Plataforma.BD::REG.Profesion"		
			WHERE "IdProfesion" IN (:IdProfesion)