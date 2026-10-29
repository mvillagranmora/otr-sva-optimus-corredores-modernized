SELECT  
	"IdTipoEstadoCivil" AS "IdTipoEstadoCivil",
	"Nombre" AS "Nombre"
		FROM "BSCL.Plataforma.BD::REG.TipoEstadoCivil"	
			WHERE "IdTipoEstadoCivil" IN (:IdTipoEstadoCivil)