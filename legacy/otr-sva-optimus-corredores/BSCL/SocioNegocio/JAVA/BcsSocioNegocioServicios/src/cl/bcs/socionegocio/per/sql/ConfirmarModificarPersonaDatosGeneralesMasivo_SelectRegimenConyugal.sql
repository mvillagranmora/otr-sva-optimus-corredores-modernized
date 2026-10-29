SELECT 
	"IdTipoRegimenConyugal" AS "IdTipoRegimenConyugal",
	"Nombre" AS "Nombre" 
		FROM "BSCL.Plataforma.BD::REG.TipoRegimenConyugal"	
			WHERE "IdTipoRegimenConyugal" IN (:IdTipoRegimenConyugal)