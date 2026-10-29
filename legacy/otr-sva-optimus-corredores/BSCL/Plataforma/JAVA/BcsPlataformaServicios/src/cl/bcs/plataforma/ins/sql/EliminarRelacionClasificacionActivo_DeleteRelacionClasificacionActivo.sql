--Elimina datos de la relacion
	DELETE 
	FROM "BSCL.Plataforma.BD::INS.RelacionClasificacionActivos"  WHERE "IdArbolClasificacionActivo" IN (                
	SELECT "IdArbolClasificacionActivo" FROM   "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" WHERE "IdArbolClasificacionActivo" = :p_IdArbolClasificacionActivo);
