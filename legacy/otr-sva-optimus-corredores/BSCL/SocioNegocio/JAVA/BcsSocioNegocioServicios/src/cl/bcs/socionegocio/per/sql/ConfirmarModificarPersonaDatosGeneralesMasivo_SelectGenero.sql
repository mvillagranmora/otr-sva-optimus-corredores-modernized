SELECT 
	"IdTipoGenero",
	"Descripcion" 
			FROM "BSCL.SocioNegocio.BD::PER.TipoGenero"
				WHERE "IdTipoGenero" IN (:IdTipoGenero)