UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilProductos"
SET "IdCanalIngreso" = :p_ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilProductos" = :p_IdDetallePerfilProductos
