UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilProductos"
SET "IdEstado" = :p_ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilProductos" = :p_IdDetallePerfilProductos
