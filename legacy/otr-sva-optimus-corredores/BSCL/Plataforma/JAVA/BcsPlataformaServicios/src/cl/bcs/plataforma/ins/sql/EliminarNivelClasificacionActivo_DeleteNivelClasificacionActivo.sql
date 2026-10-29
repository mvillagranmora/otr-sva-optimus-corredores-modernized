--Borra Mercado, Submercado o Tipomercado 
DELETE FROM   "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" WHERE "Padre" = :p_IdArbolClasificacionActivo and "Tipo"='U' OR "IdArbolClasificacionActivo" = :p_IdArbolClasificacionActivo and "Tipo"='U';
