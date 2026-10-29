SELECT 
	"IdTipoPersona" AS "IdTipoPersona",
	"Descripcion" AS "Descripcion" 
		FROM "BSCL.SocioNegocio.BD::PER.TipoPersona"
			WHERE "IdTipoPersona" IN (:IdTipoPersona)