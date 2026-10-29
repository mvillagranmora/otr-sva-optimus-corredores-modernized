UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdPerfilComercialBaseOrigen" = :IdPerfilComercialBase,
	"IdPerfilProductos" = :IdPerfilProductos,
	"FechaModificacion" = CURRENT_TIMESTAMP
WHERE "IdCliente" = :IdCliente;
