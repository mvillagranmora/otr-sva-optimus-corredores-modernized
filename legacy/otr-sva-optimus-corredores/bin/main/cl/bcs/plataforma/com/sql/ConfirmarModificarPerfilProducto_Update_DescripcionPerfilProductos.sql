UPDATE "BSCL.Plataforma.BD::COM.PerfilProductos"
SET "DescripcionPerfilProductos" = :p_ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdPerfilProductos" = :p_IdPerfilProductos
