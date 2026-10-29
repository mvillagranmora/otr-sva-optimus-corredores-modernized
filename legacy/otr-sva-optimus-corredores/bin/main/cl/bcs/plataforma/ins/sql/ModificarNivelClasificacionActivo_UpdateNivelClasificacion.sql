UPDATE "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos"
SET "Descripcion"=:p_Descripcion, "Padre" =:p_Padre 
WHERE "IdArbolClasificacionActivo"=:p_IdArbolClasificacionActivo  and "Tipo"='U';