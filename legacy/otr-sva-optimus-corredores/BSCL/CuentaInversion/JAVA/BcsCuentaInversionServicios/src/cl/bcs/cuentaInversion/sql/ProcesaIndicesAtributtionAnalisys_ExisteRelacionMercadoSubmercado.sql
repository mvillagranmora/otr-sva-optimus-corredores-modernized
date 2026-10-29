SELECT count(*) as "Existe"
FROM "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos"
WHERE "Indice" = :p_IdIndiceFinanciero;