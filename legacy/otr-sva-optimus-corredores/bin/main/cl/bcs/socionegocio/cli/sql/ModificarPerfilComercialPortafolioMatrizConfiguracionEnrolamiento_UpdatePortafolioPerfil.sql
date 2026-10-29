UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdPerfilComercialBaseOrigen" = COALESCE(:IdPerfilComercial,"IdPerfilComercialBaseOrigen"),
	"IdPerfilProductos" = COALESCE(:IdPerfilProductos,"IdPerfilProductos"),
	"IdPerfilCobros"= COALESCE(:IdPerfilCobros,"IdPerfilCobros"),
	"FechaModificacion" = CURRENT_TIMESTAMP
WHERE "IdPortafolio" = :IdPortafolio