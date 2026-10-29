SELECT "IdArbolClasificacionActivo" 
FROM "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos"
WHERE "Padre"!= 0
	OR ("Padre" = 0 AND "Indice" != '')