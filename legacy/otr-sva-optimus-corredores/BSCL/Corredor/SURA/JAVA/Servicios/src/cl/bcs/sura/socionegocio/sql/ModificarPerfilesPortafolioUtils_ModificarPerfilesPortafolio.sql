UPDATE PORT 
SET 
	PORT."IdPerfilComercialBaseOrigen" = :IdPerfilComercial
	, PORT."IdPerfilProductos" = :IdPerfilProductos
	, PORT."IdPerfilCobros" = :IdPerfilCobros
FROM "BSCL.Portafolio.BD::POR.Portafolio" PORT
WHERE PORT."IdPortafolio" = :IdPortafolio;