UPDATE "BSCL.Plataforma.BD::COM.PerfilProductos"
SET "IdCategoriaInversionista" = :p_ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdPerfilProductos" = :p_IdPerfilProductos
