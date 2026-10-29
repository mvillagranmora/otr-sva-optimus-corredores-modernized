UPDATE "BSCL.Plataforma.BD::COM.PerfilProductos"
SET "IdReferencia" = :p_ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdPerfilProductos" = :p_IdPerfilProductos
