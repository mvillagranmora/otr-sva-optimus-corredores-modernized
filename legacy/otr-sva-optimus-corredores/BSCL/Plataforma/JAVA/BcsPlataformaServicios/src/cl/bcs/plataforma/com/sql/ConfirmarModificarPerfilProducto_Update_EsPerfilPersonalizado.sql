UPDATE "BSCL.Plataforma.BD::COM.PerfilProductos"
SET "EsPerfilPersonalizado" = :p_ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdPerfilProductos" = :p_IdPerfilProductos
