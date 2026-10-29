SELECT TOP 1 A."Padre", R."IdArbolClasificacionActivo", A."Descripcion"
FROM "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" A, "BSCL.Plataforma.BD::INS.RelacionClasificacionActivos" R
WHERE A."Padre"=:p_IdArbolClasificacionActivo OR R."IdArbolClasificacionActivo"=:p_IdArbolClasificacionActivo; 