SELECT
	"IdPerfilComercialAdicional",
	"IdCategoriaInversionista",
	"IdFormaCobro",
	"CodigoPortafolio",
	"IdPerfilComercialBase",
	"IdPerfilProductos", 
	"IdPerfilCobros" 
FROM "BSCL.Plataforma.BD::COM.PerfilComercialAdicional"
WHERE "IdPerfilComercialAdicional" = :IdPerfilComercialAdicional;