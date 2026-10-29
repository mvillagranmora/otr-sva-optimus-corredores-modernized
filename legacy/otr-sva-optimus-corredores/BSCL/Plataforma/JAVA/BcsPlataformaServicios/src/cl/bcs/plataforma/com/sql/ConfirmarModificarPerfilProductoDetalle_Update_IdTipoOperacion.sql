UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilProductos"
SET "IdTipoOperacion" = :p_ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilProductos" = :p_IdDetallePerfilProductos
