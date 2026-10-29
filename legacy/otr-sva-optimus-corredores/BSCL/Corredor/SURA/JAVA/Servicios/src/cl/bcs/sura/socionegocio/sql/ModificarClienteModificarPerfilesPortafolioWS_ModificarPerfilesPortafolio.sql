UPDATE PORT 
SET 
	PORT."IdPerfilComercialBaseOrigen" = PER."IdPerfilComercialBase" 
	, PORT."IdPerfilProductos" = PER."IdPerfilProductos" 
	, PORT."IdPerfilCobros" = PER."IdPerfilCobros" 
FROM "BSCL.Portafolio.BD::POR.Portafolio" PORT
INNER JOIN "BSCL.Plataforma.BD::COM.PerfilComercialAdicional" AS PER ON (PER."IdFormaCobro" = :IdFormaCobro AND PER."IdCategoriaInversionista" = :IdCategoriaInversionista)
WHERE PORT."IdPortafolio" = :IdPortafolio;