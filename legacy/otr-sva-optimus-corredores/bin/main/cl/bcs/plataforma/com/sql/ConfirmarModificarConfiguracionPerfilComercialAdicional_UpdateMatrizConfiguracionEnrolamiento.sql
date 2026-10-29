UPDATE "BSCL.Plataforma.BD::COM.PerfilComercialAdicional"
SET "IdCategoriaInversionista" = COALESCE(:IdCategoriaInversionista,"IdCategoriaInversionista"),
	"IdFormaCobro" = COALESCE(:IdFormaCobro,"IdFormaCobro"),
	"IdPerfilComercialBase"= COALESCE(:IdPerfilComercialBase,"IdPerfilComercialBase"),
	"IdPerfilCobros" = COALESCE(:IdPerfilCobros,"IdPerfilCobros"),
	"IdPerfilProductos" = COALESCE(:IdPerfilProductos,"IdPerfilProductos"),
	"CodigoPortafolio" = COALESCE(:CodigoPortafolio,"CodigoPortafolio")
WHERE "IdPerfilComercialAdicional" = :IdPerfilComercialAdicional;