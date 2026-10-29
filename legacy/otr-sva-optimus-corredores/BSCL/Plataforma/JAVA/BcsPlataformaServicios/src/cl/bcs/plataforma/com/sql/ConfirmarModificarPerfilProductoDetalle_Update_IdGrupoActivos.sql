UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilProductos"
SET "IdGrupoActivos" = :p_ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilProductos" = :p_IdDetallePerfilProductos
