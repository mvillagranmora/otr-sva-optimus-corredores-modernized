UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilProductos"
SET "IdTipoProducto" = :p_ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilProductos" = :p_IdDetallePerfilProductos
