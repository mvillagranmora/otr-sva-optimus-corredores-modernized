SELECT 
	"IdAtributoAnotacion",
	"TipoAtributoAnotacion"
FROM "BSCL.SocioNegocio.BD::CLI.AtributoAnotacion"
WHERE "IdEmpresa" = :IdEmpresa;