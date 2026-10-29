SELECT
	"IdPerfilComercialAdicional",
	"IdCategoriaInversionista",
	"IdFormaCobro",
	"CodigoPortafolio"
FROM "BSCL.Plataforma.BD::COM.PerfilComercialAdicional"
WHERE 
	"IdPerfilComercialAdicional" != :IdPerfilComercialAdicional AND 
	"CodigoPortafolio" = :CodigoPortafolio AND 
	"IdCategoriaInversionista" = :IdCategoriaInversionista AND 
	UPPER("IdFormaCobro") = UPPER(:IdFormaCobro);