SELECT  *
FROM "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos"
WHERE "Descripcion" = :p_Descripcion OR "IdArbolClasificacionActivo"=:p_IdArbolClasificacionActivo;