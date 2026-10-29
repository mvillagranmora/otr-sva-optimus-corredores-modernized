UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilProductos"
SET "IdMoneda" = :p_ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilProductos" = :p_IdDetallePerfilProductos
