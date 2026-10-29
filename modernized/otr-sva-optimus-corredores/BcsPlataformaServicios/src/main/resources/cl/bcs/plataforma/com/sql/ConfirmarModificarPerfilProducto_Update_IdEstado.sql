UPDATE "BSCL.Plataforma.BD::COM.PerfilProductos"
SET "IdEstado" = :p_ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdPerfilProductos" = :p_IdPerfilProductos
